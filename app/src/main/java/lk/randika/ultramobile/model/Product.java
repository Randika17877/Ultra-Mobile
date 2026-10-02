package lk.randika.ultramobile.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IgnoreExtraProperties
public class Product {
    private String productId;
    private String title;
    private String description;
    private double price;
    private String categoryId;
    private String categoryDocId;
    private String categoryName;
    private Object createdAt;
    private int reviewCount;
    private Object updatedAt;
    
    @lombok.Getter(lombok.AccessLevel.NONE)
    private Object images;
    
    private int stockCount;
    private boolean status;
    private float rating;
    private List<Attribute> attributes;

    public Product(String productId, String title, String description, double price, String categoryId, Object images, int stockCount, boolean status, float rating, List<Attribute> attributes) {
        this.productId = productId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.images = images;
        this.stockCount = stockCount;
        this.status = status;
        this.rating = rating;
        this.attributes = attributes;
    }

    public List<String> getImages() {
        if (images instanceof List) {
            return (List<String>) images;
        } else if (images instanceof String) {
            return Collections.singletonList((String) images);
        }
        return new ArrayList<>();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attribute {
        private String name;
        private String type;
        private List<String> values;
    }
}
