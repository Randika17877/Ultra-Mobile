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
public class User {
    private String uid;
    private String name;
    private String email;
    private String profilePicUrl;
    private String fcmToken;

    public User(String uid, String name, String email, String profilePicUrl) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.profilePicUrl = profilePicUrl;
    }
}
