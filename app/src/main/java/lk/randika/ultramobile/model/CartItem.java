package lk.randika.ultramobile.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IgnoreExtraProperties
public class CartItem implements Serializable {
    @Getter(onMethod_ = {@Exclude})
    @Setter(onMethod_ = {@Exclude})
    private String documentId;
    private String productId;
    private int quantity;
    private List<Attribute> attributes;

    public CartItem(String productId, int quantity, List<Attribute> attributes) {
        this.productId = productId;
        this.quantity = quantity;
        this.attributes = attributes;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attribute implements Serializable {
        private String name;
        private String value;
    }
}

