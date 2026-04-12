package lk.randika.ultramobile.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.randika.ultramobile.R;
import lk.randika.ultramobile.adapter.NotificationAdapter;
import lk.randika.ultramobile.model.Notification;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        notificationList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.notification_recycler);
        emptyState = view.findViewById(R.id.notification_empty_state);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notificationList, notification -> {
            if (!notification.isRead()) {
                db.collection("users").document(auth.getCurrentUser().getUid())
                        .collection("notifications").document(notification.getId())
                        .update("read", true);
            }
            
            // Note: If type is ORDER_STATUS, we could navigate to OrderDetails here
            if ("ORDER_STATUS".equals(notification.getType()) && notification.getOrderId() != null) {
                // Navigate to Order details if needed
                // getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, OrderDetailFragment.newInstance(notification.getOrderId())).addToBackStack(null).commit();
            }
        });
        recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        if (auth.getCurrentUser() == null) {
            emptyState.setVisibility(View.VISIBLE);
            return;
        }

        // According to user's schema, ordering by createdAt (which is a string ISO-8601, so string sorting works perfectly!)
        db.collection("users").document(auth.getCurrentUser().getUid())
                .collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("NotificationFragment", "Error loading notifications", error);
                        return;
                    }

                    if (value != null) {
                        notificationList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Notification notification = doc.toObject(Notification.class);
                            notification.setId(doc.getId());
                            notificationList.add(notification);
                        }
                        
                        adapter.notifyDataSetChanged();
                        
                        if (notificationList.isEmpty()) {
                            emptyState.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyState.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
}
