package lk.randika.ultramobile.fragment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.randika.ultramobile.R;
import lk.randika.ultramobile.activity.MainActivity;
import lk.randika.ultramobile.activity.SignInActivity;
import lk.randika.ultramobile.adapter.ProductSliderAdapter;
import lk.randika.ultramobile.adapter.SectionAdapter;
import lk.randika.ultramobile.databinding.FragmentProductDetailsBinding;
import lk.randika.ultramobile.model.CartItem;
import lk.randika.ultramobile.model.Product;
import lk.randika.ultramobile.helper.DatabaseHelper;


public class ProductDetailsFragment extends Fragment {

    private FragmentProductDetailsBinding binding;
    private String productId;
    private int quantity = 1;
    private int avbQuantity;
    private boolean isInWishlist = false;
    private DatabaseHelper dbHelper;

    // Track attribute groups for dynamic selection
    private Map<String, ChipGroup> attributeGroups = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }
        dbHelper = new DatabaseHelper(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide main bottom navigation for an immersive details view
        getActivity().findViewById(R.id.bottom_navigation_view).setVisibility(View.GONE);

        // Custom back button handling
        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });


        // Fetch and display detailed product information from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products").whereEqualTo("productId", productId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                if (!qds.isEmpty()) {
                    Product product = qds.getDocuments().get(0).toObject(Product.class);

                    // Setup Image Slider with dots indicator
                    ProductSliderAdapter adapter = new ProductSliderAdapter(product.getImages());
                    binding.productImageSlider.setAdapter(adapter);
                    binding.dotsIndicator.attachTo(binding.productImageSlider);

                    // Set basic info
                    binding.productDetailsTitle.setText(product.getTitle());
                    binding.productDetailsRating.setRating(product.getRating());
                    binding.productDetailsRatingText.setText("(" + product.getRating() + ")");
                    binding.productDetailsPrice.setText("LKR " + product.getPrice());
                    binding.productDetailsDescription.setText(product.getDescription());

                    // Stock management
                    binding.productDetailsAvbQty.setText(String.valueOf(product.getStockCount()));
                    avbQuantity = product.getStockCount();

                    // Dynamically render attributes (colors, sizes, etc.)
                    if (product.getAttributes() != null) {
                        product.getAttributes().forEach(attribute -> {
                            renderAttribute(attribute, binding.productDetailsAttributeContainer);
                        });
                    }
                }
            }
        });

        checkWishlistStatus();

        binding.productDetailsWishlistBtn.setOnClickListener(v -> toggleWishlist());


        binding.productDetailsBtnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.productDetailsQuantity.setText(String.valueOf(quantity));
            }
        });

        binding.productDetailsBtnPlus.setOnClickListener(v -> {
            if (quantity < avbQuantity) {
                quantity++;
                binding.productDetailsQuantity.setText(String.valueOf(quantity));
            }
        });


        loadTopSellProduct();


        binding.productDetailsBtnAddCart.setOnClickListener(v -> {

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                startActivity(intent);
            } else {

                List<CartItem.Attribute> attributes = getFinalSelections();
                if (attributes == null) return;

                CartItem cartItem = new CartItem(productId, quantity, attributes);

                String uid = firebaseAuth.getCurrentUser().getUid();

                db.collection("users").document(uid).collection("cart").document()
                        .set(cartItem)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Item added to cart!", Toast.LENGTH_SHORT).show();
                    }
                });
            }


        });

        binding.productDetailsBtnBuyNow.setOnClickListener(v -> {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                startActivity(intent);
            } else {
                List<CartItem.Attribute> attributes = getFinalSelections();
                if (attributes == null) return;

                CartItem cartItem = new CartItem(productId, quantity, attributes);

                Bundle bundle = new Bundle();
                bundle.putSerializable("buyNowItem", cartItem);

                CheckoutFragment checkoutFragment = new CheckoutFragment();
                checkoutFragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, checkoutFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });


    }

    private void checkWishlistStatus() {
        isInWishlist = dbHelper.isInWishlist(productId);
        if (isInWishlist) {
            binding.productDetailsWishlistBtn.setColorFilter(getResources().getColor(R.color.um_primary));
        } else {
            binding.productDetailsWishlistBtn.setColorFilter(getResources().getColor(R.color.um_text_secondary));
        }
    }

    private void toggleWishlist() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if (isInWishlist) {
            if (dbHelper.removeFromWishlist(productId)) {
                isInWishlist = false;
                binding.productDetailsWishlistBtn.setColorFilter(getResources().getColor(R.color.um_text_secondary));
                Toast.makeText(getContext(), "Removed from wishlist", Toast.LENGTH_SHORT).show();
                if (uid != null) {
                    firestore.collection("users").document(uid).collection("wishlist").document(productId).delete();
                }
            }
        } else {
            if (dbHelper.addToWishlist(productId)) {
                isInWishlist = true;
                binding.productDetailsWishlistBtn.setColorFilter(getResources().getColor(R.color.um_primary));
                Toast.makeText(getContext(), "Added to wishlist", Toast.LENGTH_SHORT).show();
                if (uid != null) {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("productId", productId);
                    itemData.put("addedAt", com.google.firebase.Timestamp.now());
                    firestore.collection("users").document(uid).collection("wishlist").document(productId).set(itemData);
                }
            }
        }
    }

    private void loadTopSellProduct() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products").whereNotEqualTo("productId", productId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                if (!qds.isEmpty()) {
                    List<Product> products = qds.toObjects(Product.class);


                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

                    binding.productDetailsTopSellSection.itemSectionContainer.setLayoutManager(layoutManager);


                    SectionAdapter adapter = new SectionAdapter(products, product -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("productId", product.getProductId());

                        ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                        productDetailsFragment.setArguments(bundle);

                        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, productDetailsFragment).addToBackStack(null).commit();
                    });

                    binding.productDetailsTopSellSection.itemSectionTitle.setText("Related Products");
                    binding.productDetailsTopSellSection.itemSectionContainer.setAdapter(adapter);

                }
            }
        });

    }

    /**
     * Renders product attributes (e.g., Color chips or regular text chips)
     */
    private void renderAttribute(Product.Attribute attribute, ViewGroup container) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 16, 0, 16);

        // Attribute Name Label
        TextView label = new TextView(getContext());
        label.setText(attribute.getName().toUpperCase());
        label.setTextColor(getResources().getColor(R.color.um_text_secondary));
        label.setTextSize(12);
        label.setLetterSpacing(0.1f);
        label.setTypeface(label.getTypeface(), android.graphics.Typeface.BOLD);
        label.setPadding(0, 0, 0, 12);
        row.addView(label);

        // Attribute Options (Chips)
        ChipGroup group = new ChipGroup(getContext());
        group.setSelectionRequired(true);
        group.setSingleSelection(true);
        group.setChipSpacingHorizontal(12);

        attribute.getValues().forEach(value -> {
            Chip chip = new Chip(getContext());
            chip.setId(View.generateViewId());
            chip.setCheckable(true);
            chip.setTag(value);

            // Apply different styles based on attribute type
            if ("color".equals(attribute.getType())) {
                // Color circle chip
                chip.setChipBackgroundColor(ColorStateList.valueOf(safeParseColor(value)));
                chip.setText("");
                chip.setContentDescription(value);
                chip.setChipIconVisible(false);
                chip.setChipMinHeight(80);
                chip.setChipStartPadding(30);
                chip.setChipEndPadding(30);
                // Custom stroke for selection visibility
                chip.setChipStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.um_primary)));
                chip.setChipStrokeWidth(0f);
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    chip.setChipStrokeWidth(isChecked ? 4f : 0f);
                });
            } else {
                // Regular text chip for Size, Storage, etc.
                chip.setText(value);
                chip.setTextColor(getResources().getColor(R.color.um_text_primary));
                chip.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.um_surface_elevated)));
                chip.setChipStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.um_surface_border)));
                chip.setChipStrokeWidth(1f);
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(isChecked ?
                        getResources().getColor(R.color.um_primary_dim) :
                        getResources().getColor(R.color.um_surface_elevated)));
                });
            }

            group.addView(chip);
        });

        row.addView(group);
        container.addView(row);
        attributeGroups.put(attribute.getName(), group);
    }


    private List<CartItem.Attribute> getFinalSelections() {

        List<CartItem.Attribute> attributes = new ArrayList<>();

        for (Map.Entry<String, ChipGroup> entry : attributeGroups.entrySet()) {
            String attributeName = entry.getKey();
            ChipGroup chipGroup = entry.getValue();

            int checkedChipId = chipGroup.getCheckedChipId();
            if (checkedChipId != -1) {
                Chip chip = chipGroup.findViewById(checkedChipId);
                if (chip != null) {
                    String value = chip.getTag().toString();
                    attributes.add(new CartItem.Attribute(attributeName, value));
                }
            } else {
                Toast.makeText(getContext(), "Please select " + attributeName, Toast.LENGTH_SHORT).show();
                return null;
            }
        }


        return attributes;
    }

    private int safeParseColor(String colorStr) {
        if (colorStr == null) return Color.GRAY;
        colorStr = colorStr.trim();
        if (colorStr.isEmpty()) return Color.GRAY;

        try {
            return Color.parseColor(colorStr);
        } catch (IllegalArgumentException ignored) {}

        if (!colorStr.startsWith("#")) {
            try {
                return Color.parseColor("#" + colorStr);
            } catch (IllegalArgumentException ignored) {}
        }

        switch (colorStr.toLowerCase()) {
            case "black": return Color.BLACK;
            case "darkgray":
            case "dark grey":
            case "space gray":
            case "space grey":
            case "graphite":
                return Color.DKGRAY;
            case "gray":
            case "grey":
            case "silver":
                return Color.GRAY;
            case "lightgray":
            case "light grey":
            case "white":
                return Color.WHITE;
            case "red":
            case "crimson":
                return Color.RED;
            case "green": return Color.GREEN;
            case "blue":
            case "navy":
            case "pacific blue":
            case "sierra blue":
                return Color.BLUE;
            case "yellow":
            case "gold":
                return Color.YELLOW;
            case "cyan": return Color.CYAN;
            case "magenta":
            case "purple":
                return Color.MAGENTA;
            case "rose gold":
            case "pink":
                return Color.parseColor("#FFC0CB");
            case "midnight":
                return Color.parseColor("#191970");
            case "starlight":
                return Color.parseColor("#F5F5DC");
            default:
                return Color.GRAY;
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        // Restore bottom navigation when leaving details
        getActivity().findViewById(R.id.bottom_navigation_view).setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Re-hide bottom navigation if returning to this fragment
        getActivity().findViewById(R.id.bottom_navigation_view).setVisibility(View.GONE);
    }
}
