package lk.randika.ultramobile.model;

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
public class Product {
    private String productId;
    private String title;
    private String description;
    private double price;
    private String categoryId;
    
    @lombok.Getter(lombok.AccessLevel.NONE)
    private Object images;
    
    private int stockCount;
    private boolean status;
    private float rating;
    private List<Attribute> attributes;

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
