package lk.randika.ultramobile.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import lk.randika.ultramobile.R;
import lk.randika.ultramobile.activity.SignInActivity;
import lk.randika.ultramobile.databinding.FragmentProfileBinding;
import lk.randika.ultramobile.model.User;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            loadUserProfile(currentUser.getUid());
            loadStats(currentUser.getUid());
        }

        setupClickListeners();
    }

    private void loadUserProfile(String uid) {
        firebaseFirestore.collection("users").document(uid).get()
                .addOnSuccessListener(ds -> {
                    if (ds.exists()) {
                        User user = ds.toObject(User.class);
                        if (user != null) {
                            binding.profileName.setText(user.getName());
                            binding.profileEmail.setText(user.getEmail());

                            if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                                FirebaseStorage.getInstance().getReference("profile-images/" + user.getProfilePicUrl())
                                        .getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            if (isAdded()) {
                                                Glide.with(this)
                                                        .load(uri)
                                                        .circleCrop()
                                                        .placeholder(R.drawable.person_24px)
                                                        .into(binding.profilePic);
                                            }
                                        }).addOnFailureListener(e -> {
                                            Log.e("ProfileFragment", "Error loading image: " + e.getMessage());
                                        });
                            } else {
                                binding.profilePic.setImageResource(R.drawable.person_24px);
                            }
                        }
                    }
                }).addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Error loading profile: " + e.getMessage());
                });
    }

    private void loadStats(String uid) {
        // Load Orders count
        firebaseFirestore.collection("orders")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (binding != null) {
                        binding.profileOrdersCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                    }
                });

        // Load Wishlist count
        firebaseFirestore.collection("users").document(uid).collection("wishlist")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (binding != null) {
                        binding.profileWishlistCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                    }
                });
    }

    private void setupClickListeners() {
        binding.profileMenuEdit.setOnClickListener(v -> {
            loadFragment(new EditProfileFragment());
        });

        binding.profileMenuOrders.setOnClickListener(v -> {
            loadFragment(new OrdersFragment());
        });

        binding.profileMenuWishlist.setOnClickListener(v -> {
            loadFragment(new WishlistFragment());
        });

        binding.profileMenuSettings.setOnClickListener(v -> {
            loadFragment(new SettingsFragment());
        });

        binding.profileMenuLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            startActivity(intent);
            getActivity().finish();
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
