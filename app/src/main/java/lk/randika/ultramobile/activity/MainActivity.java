package lk.randika.ultramobile.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lk.randika.ultramobile.R;
import lk.randika.ultramobile.databinding.ActivityMainBinding;
import lk.randika.ultramobile.databinding.SideNavHeaderBinding;
import lk.randika.ultramobile.fragment.CartFragment;
import lk.randika.ultramobile.fragment.CategoryFragment;
import lk.randika.ultramobile.fragment.HomeFragment;
import lk.randika.ultramobile.fragment.ListingFragment;
import lk.randika.ultramobile.fragment.OrdersFragment;
import lk.randika.ultramobile.fragment.ProfileFragment;
import lk.randika.ultramobile.fragment.SearchFragment;
import lk.randika.ultramobile.fragment.SettingsFragment;
import lk.randika.ultramobile.fragment.WishlistFragment;
import lk.randika.ultramobile.model.User;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnItemSelectedListener, SensorEventListener {


    private ActivityMainBinding binding;
    private SideNavHeaderBinding sideNavHeaderBinding;
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private SensorManager sensorManager;
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View headerView = binding.sideNavigationView.getHeaderView(0);

        sideNavHeaderBinding = SideNavHeaderBinding.bind(headerView);

        drawerLayout = binding.drawerLayout;
        toolbar = binding.toolbar;
        navigationView = binding.sideNavigationView;
        bottomNavigationView = binding.bottomNavigationView;

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    FragmentManager fm = getSupportFragmentManager();
                    if (fm.getBackStackEntryCount() > 0) {
                        fm.popBackStack();
                    } else {
                        finish();
                    }
                }
            }
        });

        // Search functionality
        binding.textInputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                FragmentManager fm = getSupportFragmentManager();
                Fragment currentFragment = fm.findFragmentById(R.id.fragment_container);

                if (!query.isEmpty()) {
                    if (!(currentFragment instanceof SearchFragment)) {
                        performSearch(query);
                    } else {
                        ((SearchFragment) currentFragment).performSearch(query);
                    }
                } else if (currentFragment instanceof SearchFragment) {
                    ((SearchFragment) currentFragment).performSearch("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.textInputSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = binding.textInputSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
                return true;
            }
            return false;
        });


        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnItemSelectedListener(this);

        // Listen for fragment changes to toggle search visibility
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            updateSearchVisibility(currentFragment);
        });

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            navigationView.getMenu().findItem(R.id.side_nav_home).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_home).setChecked(true);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        //check and load user details
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            updateUserDetails(currentUser.getUid());
            requestNotificationPermission();
            updateFCMToken(currentUser.getUid());
        }

        // Initialize Sensor Manager for shake detection
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        acceleration = 10f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

    }

    private void updateUserDetails(String uid) {
        firebaseFirestore.collection("users").document(uid).get()
                .addOnSuccessListener(ds -> {
                    if (ds.exists()) {
                        User user = ds.toObject(User.class);
                        if (user != null) {
                            sideNavHeaderBinding.headerUserName.setText(user.getName());
                            sideNavHeaderBinding.headerUserEmail.setText(user.getEmail());

                            if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                                FirebaseStorage.getInstance().getReference("profile-images/" + user.getProfilePicUrl()).getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            Glide.with(MainActivity.this)
                                                    .load(uri)
                                                    .circleCrop()
                                                    .into(sideNavHeaderBinding.headerProfilePic);
                                        }).addOnFailureListener(e -> {
                                            sideNavHeaderBinding.headerProfilePic.setImageResource(R.drawable.person_24px);
                                        });
                            } else {
                                sideNavHeaderBinding.headerProfilePic.setImageResource(R.drawable.person_24px);
                            }
                        }
                    }
                });

        // Update UI for logged in user
        navigationView.getMenu().findItem(R.id.side_nav_login).setVisible(false);
        navigationView.getMenu().findItem(R.id.side_nav_profile).setVisible(true);
        navigationView.getMenu().findItem(R.id.side_nav_orders).setVisible(true);
        navigationView.getMenu().findItem(R.id.side_nav_wishlist).setVisible(true);
        navigationView.getMenu().findItem(R.id.side_nav_cart).setVisible(true);
        navigationView.getMenu().findItem(R.id.side_nav_logout).setVisible(true);

        sideNavHeaderBinding.headerProfilePic.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            activityResultLauncher.launch(intent);
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void updateFCMToken(String uid) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult();
                Map<String, Object> map = new HashMap<>();
                map.put("fcmToken", token);
                firebaseFirestore.collection("users").document(uid).update(map);
            }
        });
    }

    private void performSearch(String query) {
        Bundle bundle = new Bundle();
        bundle.putString("searchQuery", query);
        SearchFragment searchFragment = new SearchFragment();
        searchFragment.setArguments(bundle);
        loadFragment(searchFragment);
    }

    private void updateSearchVisibility(Fragment fragment) {
        if (fragment instanceof HomeFragment || fragment instanceof CategoryFragment || fragment instanceof ListingFragment || fragment instanceof SearchFragment) {
            binding.textInputSearch.setVisibility(View.VISIBLE);
        } else {
            binding.textInputSearch.setVisibility(View.GONE);
        }
    }


    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        Glide.with(MainActivity.this)
                                .load(uri)
                                .circleCrop()
                                .into(sideNavHeaderBinding.headerProfilePic);

                        String imageId = UUID.randomUUID().toString();
                        FirebaseStorage.getInstance().getReference("profile-images").child(imageId).putFile(uri)
                                .addOnSuccessListener(taskSnapshot -> {
                                    firebaseFirestore.collection("users")
                                            .document(firebaseAuth.getUid())
                                            .update("profilePicUrl", imageId)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(MainActivity.this, "Profile image changed!", Toast.LENGTH_SHORT).show();
                                            });
                                });
                    }
                }
            }
    );


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (itemId == R.id.side_nav_home || itemId == R.id.bottom_nav_home) {
            if (!(currentFragment instanceof HomeFragment)) loadFragment(new HomeFragment());
            navigationView.setCheckedItem(R.id.side_nav_home);
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_home).setChecked(true);
        } else if (itemId == R.id.side_nav_profile || itemId == R.id.bottom_nav_profile) {
            if (firebaseAuth.getCurrentUser() == null) {
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                finish();
                return false;
            }
            if (!(currentFragment instanceof ProfileFragment)) loadFragment(new ProfileFragment());
            navigationView.setCheckedItem(R.id.side_nav_profile);
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_profile).setChecked(true);
        } else if (itemId == R.id.side_nav_orders) {
            if (!(currentFragment instanceof OrdersFragment)) loadFragment(new OrdersFragment());
            navigationView.setCheckedItem(R.id.side_nav_orders);
        } else if (itemId == R.id.side_nav_wishlist) {
            if (!(currentFragment instanceof WishlistFragment)) loadFragment(new WishlistFragment());
            navigationView.setCheckedItem(R.id.side_nav_wishlist);
        } else if (itemId == R.id.side_nav_cart || itemId == R.id.bottom_nav_cart) {
            if (firebaseAuth.getCurrentUser() == null) {
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                finish();
                return false;
            }
            if (!(currentFragment instanceof CartFragment)) loadFragment(new CartFragment());
            navigationView.setCheckedItem(R.id.side_nav_cart);
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_cart).setChecked(true);
        } else if (itemId == R.id.side_nav_settings) {
            if (!(currentFragment instanceof SettingsFragment)) loadFragment(new SettingsFragment());
            navigationView.setCheckedItem(R.id.side_nav_settings);
        } else if (itemId == R.id.bottom_nav_category) {
            if (!(currentFragment instanceof CategoryFragment)) loadFragment(new CategoryFragment());
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_category).setChecked(true);
        } else if (itemId == R.id.side_nav_login) {
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
        } else if (itemId == R.id.side_nav_logout) {
            firebaseAuth.signOut();
            loadFragment(new HomeFragment());
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.side_nav_menu);
            navigationView.removeHeaderView(sideNavHeaderBinding.getRoot());
            navigationView.inflateHeaderView(R.layout.side_nav_header);
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            lastAcceleration = currentAcceleration;
            currentAcceleration = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = currentAcceleration - lastAcceleration;
            acceleration = acceleration * 0.9f + delta;

            if (acceleration > 12) {
                Toast.makeText(this, "Shake detected!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
