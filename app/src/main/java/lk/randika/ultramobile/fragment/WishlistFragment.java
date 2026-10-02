package lk.randika.ultramobile.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import lk.randika.ultramobile.R;
import lk.randika.ultramobile.adapter.ListingAdapter;
import lk.randika.ultramobile.databinding.FragmentWishlistBinding;
import lk.randika.ultramobile.model.Product;
import lk.randika.ultramobile.helper.DatabaseHelper;


public class WishlistFragment extends Fragment {

    private FragmentWishlistBinding binding;
    private FirebaseFirestore db;
    private List<Product> wishlistProducts = new ArrayList<>();
    private ListingAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWishlistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        dbHelper = new DatabaseHelper(requireContext());

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
        List<String> productIds = dbHelper.getWishlistProductIds();
        
        wishlistProducts.clear();
        if (productIds.isEmpty()) {
            adapter.notifyDataSetChanged();
            return;
        }

        fetchProducts(productIds);
    }

    private void fetchProducts(List<String> productIds) {
        for (String pid : productIds) {
            db.collection("products").whereEqualTo("productId", pid)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Product product = queryDocumentSnapshots.getDocuments().get(0).toObject(Product.class);
                            if (product != null) {
                                wishlistProducts.add(product);
                                adapter.notifyItemInserted(wishlistProducts.size() - 1);
                            }
                        }
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
