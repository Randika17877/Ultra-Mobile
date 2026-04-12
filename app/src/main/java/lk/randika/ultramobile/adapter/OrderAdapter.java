package lk.randika.ultramobile.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import lk.randika.ultramobile.R;
import lk.randika.ultramobile.fragment.OrderDetailFragment;
import lk.randika.ultramobile.model.Order;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private final List<Order> orderList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        
        holder.tvOrderId.setText("Order #" + (order.getOrderId() != null ? 
                (order.getOrderId().length() > 8 ? order.getOrderId().substring(0, 8).toUpperCase() : order.getOrderId().toUpperCase()) 
                : "N/A"));
        
        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "PENDING";
        holder.tvOrderStatus.setText(status);
        
        // Dynamic Status Styling
        int statusColor;
        switch (status) {
            case "PAID":
            case "DELIVERED":
            case "COMPLETED":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.um_success);
                break;
            case "SHIPPED":
            case "PROCESSING":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.um_primary);
                break;
            case "CANCELLED":
            case "FAILED":
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.um_error);
                break;
            default: // PENDING
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.um_warning);
                break;
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(20f);
        shape.setColor(statusColor);
        holder.tvOrderStatus.setBackground(shape);

        holder.tvOrderTotal.setText(String.format(Locale.getDefault(), "LKR %.2f", order.getTotalAmount()));
        
        if (order.getOrderDate() != null) {
            holder.tvOrderDate.setText(dateFormat.format(order.getOrderDate().toDate()));
        }

        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            StringBuilder summary = new StringBuilder();
            int count = order.getOrderItems().size();
            summary.append(count).append(count == 1 ? " item: " : " items: ");
            
            for (int i = 0; i < Math.min(count, 2); i++) {
                summary.append(order.getOrderItems().get(i).getProductTitle());
                if (i < Math.min(count, 2) - 1) summary.append(", ");
            }
            if (count > 2) summary.append("...");
            
            holder.tvOrderSummary.setText(summary.toString());
        }

        holder.itemView.setOnClickListener(v -> {
            if (v.getContext() instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, OrderDetailFragment.newInstance(order.getOrderId()))
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderStatus, tvOrderDate, tvOrderTotal, tvOrderSummary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.order_id);
            tvOrderStatus = itemView.findViewById(R.id.order_status);
            tvOrderDate = itemView.findViewById(R.id.order_date);
            tvOrderTotal = itemView.findViewById(R.id.order_total);
            tvOrderSummary = itemView.findViewById(R.id.order_items_summary);
        }
    }
}
