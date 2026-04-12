package lk.randika.ultramobile.adapter;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import lk.randika.ultramobile.R;
import lk.randika.ultramobile.model.Notification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notificationList;
    private OnNotificationClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notificationList, OnNotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.title.setText(notification.getTitle() != null ? notification.getTitle() : "Notification");
        holder.message.setText(notification.getBody() != null ? notification.getBody() : "");

        if (notification.getCreatedAt() != null && !notification.getCreatedAt().isEmpty()) {
            try {
                Date date = dateFormat.parse(notification.getCreatedAt());
                if (date != null) {
                    CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                            date.getTime(),
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                    );
                    holder.date.setText(timeAgo);
                } else {
                    holder.date.setText(notification.getCreatedAt());
                }
            } catch (ParseException e) {
                Log.e("NotificationAdapter", "Date Parse Exception", e);
                holder.date.setText(notification.getCreatedAt());
            }
        } else {
            holder.date.setText("Just now");
        }

        if (notification.isRead()) {
            holder.title.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.um_text_secondary));
            holder.unreadDot.setVisibility(View.GONE);
        } else {
            holder.title.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.um_text_primary));
            holder.unreadDot.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.click_animation);
            v.startAnimation(animation);
            
            notification.setRead(true);
            notifyItemChanged(position);
            
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, date;
        View unreadDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notification_title);
            message = itemView.findViewById(R.id.notification_message);
            date = itemView.findViewById(R.id.notification_date);
            unreadDot = itemView.findViewById(R.id.notification_unread_dot);
        }
    }
}
