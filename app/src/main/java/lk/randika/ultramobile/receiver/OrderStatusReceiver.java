package lk.randika.ultramobile.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OrderStatusReceiver extends BroadcastReceiver {

    public static final String ACTION_ORDER_UPDATED = "lk.randika.ultramobile.ORDER_UPDATED";
    public static final String EXTRA_ORDER_ID       = "orderId";
    public static final String EXTRA_NEW_STATUS     = "newStatus";

    public interface OrderUpdateListener {
        void onOrderStatusChanged(String orderId, String newStatus);
    }

    private final OrderUpdateListener listener;

    public OrderStatusReceiver(OrderUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_ORDER_UPDATED.equals(intent.getAction()) && listener != null) {
            String orderId   = intent.getStringExtra(EXTRA_ORDER_ID);
            String newStatus = intent.getStringExtra(EXTRA_NEW_STATUS);
            listener.onOrderStatusChanged(orderId, newStatus);
        }
    }
}
