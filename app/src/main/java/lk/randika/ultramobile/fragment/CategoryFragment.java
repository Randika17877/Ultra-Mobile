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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lk.randika.ultramobile.R;
import lk.randika.ultramobile.adapter.CategoryAdapter;
import lk.randika.ultramobile.databinding.FragmentCategoryBinding;
import lk.randika.ultramobile.model.Category;
import lk.randika.ultramobile.model.Product;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private CategoryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.categoryRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Uncomment the line below to seed the database with categories and products
//         seedDatabase(db);

        loadCategories(db);
    }

    private void seedDatabase(FirebaseFirestore db) {
        WriteBatch batch = db.batch();

        // 1. Define Categories
        List<Category> categories = Arrays.asList(
                new Category("cat_mobiles", "Mobile Phones", ""),
                new Category("cat_laptops", "Laptops", ""),
                new Category("cat_tablets", "Tablets", ""),
                new Category("cat_watches", "Smart Watches", ""),
                new Category("cat_bands", "Smart Wristbands", ""),
                new Category("cat_parts", "PC Parts", ""),
                new Category("cat_prebuilds", "Prebuild PC's", "")
        );

        for (Category c : categories) {
            DocumentReference ref = db.collection("categories").document(c.getCategoryId());
            batch.set(ref, c);
        }

        // 2. Define Sample Products
        List<Product> products = new ArrayList<>();

        // Mobile Phones
        products.add(new Product("p1", "iPhone 15 Pro", "A17 Pro chip, Titanium design.", 999.0, "cat_mobiles", new ArrayList<>(), 50, true, 4.9f, null));
        products.add(new Product("p2", "Samsung Galaxy S24 Ultra", "AI-powered, 200MP camera.", 1199.0, "cat_mobiles", new ArrayList<>(), 40, true, 4.8f, null));

        // Laptops
        products.add(new Product("p3", "MacBook Pro M3", "Supercharged by M3 chip.", 1599.0, "cat_laptops", new ArrayList<>(), 20, true, 4.9f, null));
        products.add(new Product("p4", "ASUS ROG Zephyrus G14", "Gaming powerhouse.", 1449.0, "cat_laptops", new ArrayList<>(), 15, true, 4.7f, null));

        // Tablets
        products.add(new Product("p5", "iPad Air", "Powerful, colorful, wonderful.", 599.0, "cat_tablets", new ArrayList<>(), 30, true, 4.8f, null));

        // Smart Watches
        products.add(new Product("p6", "Apple Watch Ultra 2", "The ultimate sports watch.", 799.0, "cat_watches", new ArrayList<>(), 25, true, 4.9f, null));

        // Smart Wristbands
        products.add(new Product("p7", "Xiaomi Mi Band 8", "Active life, smart choice.", 45.0, "cat_bands", new ArrayList<>(), 100, true, 4.5f, null));

        // PC Parts
        products.add(new Product("p8", "NVIDIA RTX 4080 Super", "Beyond Fast.", 999.0, "cat_parts", new ArrayList<>(), 10, true, 4.8f, null));

        // Prebuild PC's
        products.add(new Product("p9", "Alienware Aurora R16", "Elite Gaming Desktop.", 2299.0, "cat_prebuilds", new ArrayList<>(), 5, true, 4.6f, null));

        for (Product p : products) {
            DocumentReference ref = db.collection("products").document(p.getProductId());
            batch.set(ref, p);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Database Seeded Successfully!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Seeding Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadCategories(FirebaseFirestore db) {
        db.collection("categories").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<Category> categories = task.getResult().toObjects(Category.class);
                            adapter = new CategoryAdapter(categories, category -> {
                                Bundle bundle = new Bundle();
                                bundle.putString("categoryId", category.getCategoryId());

                                ListingFragment fragment = new ListingFragment();
                                fragment.setArguments(bundle);

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, fragment)
                                        .addToBackStack(null)
                                        .commit();
                            });
                            binding.categoryRecycler.setAdapter(adapter);
                        } else {
                            Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}