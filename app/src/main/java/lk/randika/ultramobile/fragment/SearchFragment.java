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
import java.util.Collections;
import java.util.Comparator;
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

    // Filter states
    private float minPrice = 0;
    private float maxPrice = 500000;
    private int sortType = 0; // 0: Default, 1: Price Asc, 2: Price Desc

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

        setupFilterListeners();
        loadAllProductsAndFilter();
    }

    private void setupFilterListeners() {
        binding.btnFilterToggle.setOnClickListener(v -> {
            boolean isVisible = binding.filterContainer.getVisibility() == View.VISIBLE;
            binding.filterContainer.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        binding.priceRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            minPrice = values.get(0);
            maxPrice = values.get(1);
            binding.txtMinPrice.setText("Min: LKR " + String.format("%.0f", minPrice));
            binding.txtMaxPrice.setText("Max: LKR " + String.format("%.0f", maxPrice));
        });

        binding.btnApplyFilters.setOnClickListener(v -> {
            int checkedId = binding.sortChipGroup.getCheckedChipId();
            if (checkedId == R.id.chip_sort_price_asc) {
                sortType = 1;
            } else if (checkedId == R.id.chip_sort_price_desc) {
                sortType = 2;
            } else {
                sortType = 0;
            }
            binding.filterContainer.setVisibility(View.GONE);
            performSearch(searchQuery);
        });
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
        
        // 1. Text Search & Price Filter
        for (Product p : allProducts) {
            boolean matchesQuery = (query == null || query.isEmpty()) || 
                                 (p.getTitle() != null && p.getTitle().toLowerCase().contains(query.toLowerCase()));
            
            boolean matchesPrice = p.getPrice() >= minPrice && p.getPrice() <= maxPrice;

            if (matchesQuery && matchesPrice) {
                filteredList.add(p);
            }
        }

        // 2. Sorting
        if (sortType == 1) {
            Collections.sort(filteredList, Comparator.comparingDouble(Product::getPrice));
        } else if (sortType == 2) {
            Collections.sort(filteredList, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
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
        }, true);
        binding.searchRecycler.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
