package lk.randika.ultramobile.model;

import com.google.firebase.firestore.Exclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Exclude
    private String id;
    
    private String title;
    private String body;
    private String createdAt;
    private String orderId;
    private boolean read;
    private String status;
    private String type;
}
