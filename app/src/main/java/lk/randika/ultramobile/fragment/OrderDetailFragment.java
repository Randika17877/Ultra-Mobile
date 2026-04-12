package lk.randika.ultramobile.fragment;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Locale;
import lk.randika.ultramobile.R;
import lk.randika.ultramobile.model.Order;

public class OrderDetailFragment extends Fragment {

    private String orderId;
    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvShippingAddress, tvSubtotal, tvShippingFee, tvTotal;
    private LinearLayout itemsContainer;

    public static OrderDetailFragment newInstance(String orderId) {
        OrderDetailFragment fragment = new OrderDetailFragment();
        Bundle args = new Bundle();
        args.putString("orderId", orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_detail, container, false);

        tvOrderId = view.findViewById(R.id.detail_order_id);
        tvOrderDate = view.findViewById(R.id.detail_order_date);
        tvOrderStatus = view.findViewById(R.id.detail_order_status);
        tvShippingAddress = view.findViewById(R.id.detail_shipping_address);
        tvSubtotal = view.findViewById(R.id.detail_subtotal);
        tvShippingFee = view.findViewById(R.id.detail_shipping_fee);
        tvTotal = view.findViewById(R.id.detail_total);
        itemsContainer = view.findViewById(R.id.items_container);

        loadOrderDetails();

        return view;
    }

    private void loadOrderDetails() {
        db.collection("orders").whereEqualTo("orderId", orderId).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Order order = queryDocumentSnapshots.getDocuments().get(0).toObject(Order.class);
                        if (order != null) {
                            displayOrder(order);
                        }
                    }
                });
    }

    private void displayOrder(Order order) {
        tvOrderId.setText("Order #" + (order.getOrderId() != null ? order.getOrderId().toUpperCase() : "N/A"));
        
        if (order.getOrderDate() != null) {
            tvOrderDate.setText("Placed on " + dateFormat.format(order.getOrderDate().toDate()));
        }

        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "PENDING";
        tvOrderStatus.setText(status);
        
        int statusColor;
        switch (status) {
            case "PAID": case "DELIVERED": case "COMPLETED":
                statusColor = ContextCompat.getColor(requireContext(), R.color.um_success);
                break;
            case "SHIPPED": case "PROCESSING":
                statusColor = ContextCompat.getColor(requireContext(), R.color.um_primary);
                break;
            case "CANCELLED": case "FAILED":
                statusColor = ContextCompat.getColor(requireContext(), R.color.um_error);
                break;
            default:
                statusColor = ContextCompat.getColor(requireContext(), R.color.um_warning);
                break;
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(20f);
        shape.setColor(statusColor);
        tvOrderStatus.setBackground(shape);

        if (order.getShippingAddress() != null) {
            Order.Address addr = order.getShippingAddress();
            String addressText = addr.getName() + "\n" +
                    addr.getAddress1() + (addr.getAddress2() != null ? ", " + addr.getAddress2() : "") + "\n" +
                    addr.getCity() + ", " + addr.getPostcode() + "\n" +
                    "Contact: " + addr.getContact();
            tvShippingAddress.setText(addressText);
        }

        double subtotal = 0;
        itemsContainer.removeAllViews();
        if (order.getOrderItems() != null) {
            for (Order.OrderItem item : order.getOrderItems()) {
                subtotal += item.getUnitPrice() * item.getQuantity();
                addItemView(item);
            }
        }

        tvSubtotal.setText(String.format(Locale.getDefault(), "LKR %.2f", subtotal));
        tvShippingFee.setText(String.format(Locale.getDefault(), "LKR %.2f", order.getTotalAmount() - subtotal));
        tvTotal.setText(String.format(Locale.getDefault(), "LKR %.2f", order.getTotalAmount()));
    }

    private void addItemView(Order.OrderItem item) {
        View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_cart, itemsContainer, false);
        
        TextView title = itemView.findViewById(R.id.item_cart_title);
        TextView price = itemView.findViewById(R.id.item_cart_price);
        TextView qty = itemView.findViewById(R.id.item_cart_quantity);
        View deleteBtn = itemView.findViewById(R.id.item_cart_remove);
        View btnPlus = itemView.findViewById(R.id.item_cart_btn_plus);
        View btnMinus = itemView.findViewById(R.id.item_cart_btn_minus);
        
        if (deleteBtn != null) deleteBtn.setVisibility(View.GONE);
        if (btnPlus != null) btnPlus.setVisibility(View.GONE);
        if (btnMinus != null) btnMinus.setVisibility(View.GONE);
        
        title.setText(item.getProductTitle());
        price.setText(String.format(Locale.getDefault(), "LKR %.2f", item.getUnitPrice()));
        qty.setText(String.valueOf(item.getQuantity()));

        itemsContainer.addView(itemView);
    }
}
