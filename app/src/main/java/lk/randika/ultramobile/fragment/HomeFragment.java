package lk.randika.ultramobile.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadCategories();
        loadTopSellProduct();
        loadAllProducts();

    }

    private void loadCategories() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("categories")
                .limit(6)
                .get()
                .addOnSuccessListener(qds -> {
                    if (!qds.isEmpty()) {
                        List<Category> categories = qds.toObjects(Category.class);

                        LinearLayoutManager layoutManager =
                                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

                        binding.homeCategorySection.itemSectionContainer.setLayoutManager(layoutManager);

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

                        binding.homeCategorySection.itemSectionTitle.setText("Categories");
                        binding.homeCategorySection.itemSectionSeeAll.setVisibility(View.VISIBLE);
                        binding.homeCategorySection.itemSectionSeeAll.setOnClickListener(v -> {
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new CategoryFragment())
                                    .addToBackStack(null)
                                    .commit();
                        });
                        binding.homeCategorySection.itemSectionContainer.setAdapter(adapter);
                    }
                });
    }

    private void loadTopSellProduct() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .limit(10)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()) {
                            List<Product> products = qds.toObjects(Product.class);


                            LinearLayoutManager layoutManager =
                                    new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false);

                            binding.homeTopSellSection.itemSectionContainer.setLayoutManager(layoutManager);


                            SectionAdapter adapter = new SectionAdapter(products, product -> {
                                Bundle bundle = new Bundle();
                                bundle.putString("productId", product.getProductId());

                                ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                                productDetailsFragment.setArguments(bundle);

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, productDetailsFragment)
                                        .addToBackStack(null)
                                        .commit();
                            });

                            binding.homeTopSellSection.itemSectionTitle.setText("Top Selling Products");
                            binding.homeTopSellSection.itemSectionContainer.setAdapter(adapter);

                        }
                    }
                });

    }

    private void loadAllProducts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()) {
                            List<Product> products = qds.toObjects(Product.class);

                            GridLayoutManager layoutManager =
                                    new GridLayoutManager(getContext(), 2);

                            binding.homeAllProductsSection.itemSectionContainer.setLayoutManager(layoutManager);

                            SectionAdapter adapter = new SectionAdapter(products, product -> {
                                Bundle bundle = new Bundle();
                                bundle.putString("productId", product.getProductId());

                                ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                                productDetailsFragment.setArguments(bundle);

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, productDetailsFragment)
                                        .addToBackStack(null)
                                        .commit();
                            });

                            binding.homeAllProductsSection.itemSectionTitle.setText("All Products");
                            binding.homeAllProductsSection.itemSectionContainer.setAdapter(adapter);
                        }
                    }
                });
    }
}
