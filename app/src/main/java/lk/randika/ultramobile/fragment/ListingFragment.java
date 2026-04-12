package lk.randika.ultramobile.fragment;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import lk.randika.ultramobile.R;
import lk.randika.ultramobile.adapter.ListingAdapter;
import lk.randika.ultramobile.databinding.FragmentListingBinding;
import lk.randika.ultramobile.model.Product;

public class ListingFragment extends Fragment {

    private FragmentListingBinding binding;
    private ListingAdapter adapter;
    private String categoryId;
    private String searchQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            searchQuery = getArguments().getString("searchQuery");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.listingRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));

        if (searchQuery != null) {
            performSearch(searchQuery);
        } else if (categoryId != null) {
            loadByCategory(categoryId);
        }

        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

    }

    private void loadByCategory(String categoryId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .whereEqualTo("categoryId", categoryId)
                .orderBy("title", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(ds -> {
                    if (!ds.isEmpty()) {
                        List<Product> products = ds.toObjects(Product.class);
                        setupRecyclerView(products);
                    }
                }).addOnFailureListener(e -> Log.e("Firestore", "Error:" + e.getMessage()));
    }

    private void performSearch(String query) {
        binding.listingTitle.setText("Search: " + query);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Product> filteredList = new ArrayList<>();
            for (Product p : queryDocumentSnapshots.toObjects(Product.class)) {
                if (p.getTitle() != null && p.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(p);
                }
            }
            setupRecyclerView(filteredList);
        });
    }

    private void setupRecyclerView(List<Product> products) {
        adapter = new ListingAdapter(products, product -> {
            Bundle bundle = new Bundle();
            bundle.putString("productId", product.getProductId());

            ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
            productDetailsFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, productDetailsFragment)
                    .addToBackStack(null)
                    .commit();
        }, true); // Pass true to use item_product_recycler.xml
        binding.listingRecycler.setAdapter(adapter);
    }
}
