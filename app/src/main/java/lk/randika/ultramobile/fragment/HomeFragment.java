package lk.randika.ultramobile.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import lk.randika.ultramobile.R;
import lk.randika.ultramobile.adapter.CategoryAdapter;
import lk.randika.ultramobile.adapter.SectionAdapter;
import lk.randika.ultramobile.databinding.FragmentHomeBinding;
import lk.randika.ultramobile.model.Category;
import lk.randika.ultramobile.model.Product;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        setupCategories();
        loadTopSellProduct();
        loadAllProducts();
    }

    private void setupCategories() {
        binding.categoryRecyclerHome.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        db.collection("categories").get()
                .addOnSuccessListener(qds -> {
                    if (!qds.isEmpty()) {
                        List<Category> categories = qds.toObjects(Category.class);
                        CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
                            Bundle bundle = new Bundle();
                            bundle.putString("categoryId", category.getCategoryId());

                            ListingFragment fragment = new ListingFragment();
                            fragment.setArguments(bundle);

                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        });
                        binding.categoryRecyclerHome.setAdapter(adapter);
                    }
                });
    }

    private void loadTopSellProduct() {
        db.collection("products")
                .limit(10)
                .get()
                .addOnSuccessListener(qds -> {
                    if (!qds.isEmpty()) {
                        List<Product> products = qds.toObjects(Product.class);

                        LinearLayoutManager layoutManager =
                                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

                        binding.homeTopSellSection.itemSectionContainer.setLayoutManager(layoutManager);
                        binding.homeTopSellSection.itemSectionTitle.setText("Hot Deals");

                        SectionAdapter adapter = new SectionAdapter(products, product -> {
                            navigateToDetails(product.getProductId());
                        });

                        binding.homeTopSellSection.itemSectionContainer.setAdapter(adapter);
                    }
                });
    }

    private void loadAllProducts() {
        db.collection("products")
                .get()
                .addOnSuccessListener(qds -> {
                    if (!qds.isEmpty()) {
                        List<Product> products = qds.toObjects(Product.class);

                        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
                        // Make sure the grid is not scrollable within NestedScrollView if it's supposed to expand
                        // But NestedScrollView handles child heights usually. 
                        // However, for better performance with large lists, consider adding android:nestedScrollingEnabled="false"
                        binding.homeAllProductsSection.itemSectionContainer.setLayoutManager(layoutManager);
                        binding.homeAllProductsSection.itemSectionContainer.setNestedScrollingEnabled(false);
                        binding.homeAllProductsSection.itemSectionTitle.setText("Recently Added");

                        SectionAdapter adapter = new SectionAdapter(products, product -> {
                            navigateToDetails(product.getProductId());
                        });

                        binding.homeAllProductsSection.itemSectionContainer.setAdapter(adapter);
                    }
                });
    }

    private void navigateToDetails(String productId) {
        Bundle bundle = new Bundle();
        bundle.putString("productId", productId);

        ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
        productDetailsFragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, productDetailsFragment)
                .addToBackStack(null)
                .commit();
    }
}
