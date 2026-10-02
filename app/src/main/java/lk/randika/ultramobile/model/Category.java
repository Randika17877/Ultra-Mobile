package lk.randika.ultramobile.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IgnoreExtraProperties
public class Category {
    private String categoryId;
    private String name;
    private String imageUrl;
    private Object createdAt;
    private int productCount;
    private Object updatedAt;

    public Category(String categoryId, String name, String imageUrl) {
        this.categoryId = categoryId;
        this.name = name;
        this.imageUrl = imageUrl;
    }
}
