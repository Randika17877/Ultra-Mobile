package lk.randika.ultramobile.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lk.randika.ultramobile.R;
import lk.randika.ultramobile.adapter.ListingAdapter;
import lk.randika.ultramobile.databinding.FragmentWishlistBinding;
import lk.randika.ultramobile.model.Product;
import lk.randika.ultramobile.helper.DatabaseHelper;


public class WishlistFragment extends Fragment {

    private FragmentWishlistBinding binding;
    private FirebaseFirestore db;
    private final List<Product> wishlistProducts = new ArrayList<>();
    private ListingAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWishlistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadWishlist();
    }

    private void setupRecyclerView() {
        adapter = new ListingAdapter(wishlistProducts, product -> {
            Bundle bundle = new Bundle();
            bundle.putString("productId", product.getProductId());

            ProductDetailsFragment fragment = new ProductDetailsFragment();
            fragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }, true);

        binding.wishlistRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.wishlistRecycler.setAdapter(adapter);
    }

    private void loadWishlist() {
        if (getContext() == null) return;
        
        new Thread(() -> {
            try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
                List<String> rawIds = dbHelper.getWishlistProductIds();
                Set<String> uniqueIds = new LinkedHashSet<>(rawIds);
                List<String> productIds = new ArrayList<>(uniqueIds);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> fetchProducts(productIds));
                }
            }
        }).start();
    }

    private void fetchProducts(List<String> productIds) {
        wishlistProducts.clear();
        adapter.notifyDataSetChanged();

        if (productIds == null || productIds.isEmpty() || getContext() == null) return;

        for (String pid : productIds) {
            if (pid == null || pid.trim().isEmpty()) {
                removeFromLocalWishlist(pid);
                continue;
            }

            db.collection("products").whereEqualTo("productId", pid)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (isAdded() && !queryDocumentSnapshots.isEmpty()) {
                            Product product = queryDocumentSnapshots.getDocuments().get(0).toObject(Product.class);
                            if (product != null) {
                                addProductToWishlistList(product);
                                return;
                            }
                        }

                        // Fallback: Check Firestore by document ID directly
                        db.collection("products").document(pid).get()
                                .addOnSuccessListener(doc -> {
                                    if (isAdded() && doc.exists()) {
                                        Product product = doc.toObject(Product.class);
                                        if (product != null) {
                                            if (product.getProductId() == null) product.setProductId(doc.getId());
                                            addProductToWishlistList(product);
                                            return;
                                        }
                                    }
                                    removeFromLocalWishlist(pid);
                                })
                                .addOnFailureListener(e -> removeFromLocalWishlist(pid));
                    })
                    .addOnFailureListener(e -> removeFromLocalWishlist(pid));
        }
    }
    
    private void removeFromLocalWishlist(String productId) {
        if (getContext() == null) return;
        new Thread(() -> {
            try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
                dbHelper.removeFromWishlist(productId);
            }
        }).start();
    }

    private synchronized void addProductToWishlistList(Product product) {
        boolean exists = false;
        for (Product p : wishlistProducts) {
            if (p.getProductId() != null && p.getProductId().equals(product.getProductId())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            wishlistProducts.add(product);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
