package lk.randika.ultramobile.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

public class NetworkChangeReceiver extends BroadcastReceiver {

    public interface NetworkListener {
        void onNetworkChanged(boolean isConnected);
    }

    private final NetworkListener listener;

    public NetworkChangeReceiver(NetworkListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (listener != null) {
            listener.onNetworkChanged(isConnected(context));
        }
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities cap = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return cap != null &&
                (cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                 cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                 cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }
}
