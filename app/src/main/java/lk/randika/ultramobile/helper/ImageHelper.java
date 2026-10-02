package lk.randika.ultramobile.helper;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;

import lk.randika.ultramobile.R;

public class ImageHelper {
    private static final String TAG = "ImageHelper";

    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        loadImage(context, imageUrl, imageView, R.drawable.ic_launcher_background, R.drawable.ic_launcher_background);
    }

    public static void loadImage(Context context, String imageUrl, ImageView imageView, int placeholderRes, int errorRes) {
        if (context == null || imageView == null) return;

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageView.setImageResource(placeholderRes);
            return;
        }

        imageUrl = imageUrl.trim();

        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            if (imageUrl.contains("drive.google.com/file/d/")) {
                int startIndex = imageUrl.indexOf("/file/d/") + 8;
                int endIndex = imageUrl.indexOf("/", startIndex);
                if (endIndex == -1) endIndex = imageUrl.indexOf("?", startIndex);
                if (endIndex != -1) {
                    String fileId = imageUrl.substring(startIndex, endIndex);
                    imageUrl = "https://drive.google.com/thumbnail?id=" + fileId + "&sz=w1000";
                }
            } else if (imageUrl.contains("drive.google.com") && imageUrl.contains("id=")) {
                int idIndex = imageUrl.indexOf("id=") + 3;
                int endParam = imageUrl.indexOf("&", idIndex);
                String fileId = endParam != -1 ? imageUrl.substring(idIndex, endParam) : imageUrl.substring(idIndex);
                imageUrl = "https://drive.google.com/thumbnail?id=" + fileId + "&sz=w1000";
            }

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(placeholderRes)
                    .error(errorRes)
                    .into(imageView);
        } else if (imageUrl.startsWith("gs://")) {
            try {
                FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                        .getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            Glide.with(context)
                                    .load(uri)
                                    .placeholder(placeholderRes)
                                    .error(errorRes)
                                    .into(imageView);
                        })
                        .addOnFailureListener(e -> imageView.setImageResource(errorRes));
            } catch (Exception e) {
                Log.e(TAG, "Error resolving gs:// URL: " + imageUrl, e);
                imageView.setImageResource(errorRes);
            }
        } else {
            try {
                String refPath = imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl;
                FirebaseStorage.getInstance().getReference(refPath)
                        .getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            Glide.with(context)
                                    .load(uri)
                                    .placeholder(placeholderRes)
                                    .error(errorRes)
                                    .into(imageView);
                        })
                        .addOnFailureListener(e -> imageView.setImageResource(errorRes));
            } catch (Exception e) {
                Log.e(TAG, "Error resolving storage ref: " + imageUrl, e);
                imageView.setImageResource(errorRes);
            }
        }
    }
}
