package lk.randika.ultramobile.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.randika.ultramobile.R;
import lk.randika.ultramobile.adapter.ListingAdapter;
import lk.randika.ultramobile.databinding.FragmentSearchBinding;
import lk.randika.ultramobile.model.Product;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private String searchQuery = "";
    private FirebaseFirestore db;
    private List<Product> allProducts = new ArrayList<>();
    private ListingAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchQuery = getArguments().getString("searchQuery", "");
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.searchRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
        setupRecyclerView(new ArrayList<>());

        loadAllProductsAndFilter();
    }

    private void loadAllProductsAndFilter() {
        if (binding == null) return;
        binding.searchProgress.setVisibility(View.VISIBLE);
        binding.layoutNoResults.setVisibility(View.GONE);

        db.collection("products").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (binding == null) return;
            allProducts.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Product p = doc.toObject(Product.class);
                allProducts.add(p);
            }
            performSearch(searchQuery);
        }).addOnFailureListener(e -> {
            if (binding == null) return;
            binding.searchProgress.setVisibility(View.GONE);
            Log.e("SearchFragment", "Error fetching products", e);
        });
    }

    public void performSearch(String query) {
        this.searchQuery = query;
        if (binding == null) return;

        binding.searchProgress.setVisibility(View.GONE);

        List<Product> filteredList = new ArrayList<>();
        if (query != null && !query.isEmpty()) {
            for (Product p : allProducts) {
                if (p.getTitle() != null && p.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(p);
                }
            }
        } else {
            filteredList.addAll(allProducts);
        }

        if (adapter != null) {
            adapter.setProducts(filteredList);
        }

        if (filteredList.isEmpty() && !allProducts.isEmpty()) {
            binding.layoutNoResults.setVisibility(View.VISIBLE);
        } else {
            binding.layoutNoResults.setVisibility(View.GONE);
        }
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
        });
        binding.searchRecycler.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}