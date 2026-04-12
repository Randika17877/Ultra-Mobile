package lk.randika.ultramobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import lk.randika.ultramobile.R;
import lk.randika.ultramobile.model.Product;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    private List<Product> products;
    private OnListingItemClickListener listener;
    private boolean useProductItem;

    public ListingAdapter(List<Product> products, OnListingItemClickListener listener) {
        this(products, listener, false);
    }

    public ListingAdapter(List<Product> products, OnListingItemClickListener listener, boolean useProductItem) {
        this.products = products;
        this.listener = listener;
        this.useProductItem = useProductItem;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ListingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = useProductItem ? R.layout.item_product_recycler : R.layout.item_listing;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(view, useProductItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingAdapter.ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.productTitle.setText(product.getTitle());
        holder.productPrice.setText("LKR " + product.getPrice());

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImages().get(0))
                    .centerCrop()
                    .into(holder.productImage);
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

        public ViewHolder(@NonNull View itemView, boolean useProductItem) {
            super(itemView);
            if (useProductItem) {
                productImage = itemView.findViewById(R.id.item_product_r_image);
                productTitle = itemView.findViewById(R.id.item_product_r_name);
                productPrice = itemView.findViewById(R.id.item_product_r_price);
            } else {
                productImage = itemView.findViewById(R.id.listing_item_image);
                productTitle = itemView.findViewById(R.id.listing_item_name);
                productPrice = itemView.findViewById(R.id.listing_item_price);
            }
        }
    }

    public interface OnListingItemClickListener {
        void onListingItemClick(Product product);
    }
}