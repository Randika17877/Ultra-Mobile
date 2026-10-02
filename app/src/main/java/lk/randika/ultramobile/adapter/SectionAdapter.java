package lk.randika.ultramobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import lk.randika.ultramobile.R;
import lk.randika.ultramobile.helper.ImageHelper;
import lk.randika.ultramobile.model.Product;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {

    private List<Product> products;
    private OnListingItemClickListener listener;

    public SectionAdapter(List<Product> products, OnListingItemClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_recycler, parent, false);

        if (parent instanceof RecyclerView) {
            RecyclerView.LayoutManager lm = ((RecyclerView) parent).getLayoutManager();
            if (lm instanceof LinearLayoutManager && ((LinearLayoutManager) lm).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                if (lp != null) {
                    lp.width = (int) (160 * parent.getContext().getResources().getDisplayMetrics().density);
                    view.setLayoutParams(lp);
                }
            }
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionAdapter.ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.productTitle.setText(product.getTitle());
        holder.productPrice.setText("LKR " + product.getPrice());

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            ImageHelper.loadImage(holder.itemView.getContext(), product.getImages().get(0), holder.productImage);
        } else {
            holder.productImage.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.itemView.setOnClickListener(v -> {

            Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.click_animation);
            v.startAnimation(animation);

            if (listener != null) {
                listener.onListingItemClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productTitle;
        TextView productPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.item_product_r_image);
            productTitle = itemView.findViewById(R.id.item_product_r_name);
            productPrice = itemView.findViewById(R.id.item_product_r_price);
        }
    }

    public interface OnListingItemClickListener {
        void onListingItemClick(Product product);
    }
}
