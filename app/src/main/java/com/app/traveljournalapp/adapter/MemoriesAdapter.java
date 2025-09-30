package com.app.traveljournalapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.traveljournalapp.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MemoriesAdapter extends RecyclerView.Adapter<MemoriesAdapter.VH> {
    public static class MemoryItem {
        public final String imageUrl;
        public final String title;

        public MemoryItem(String imageUrl, String title) {
            this.imageUrl = imageUrl;
            this.title = title;
        }
    }

    private final List<MemoryItem> data = new ArrayList<>();
    private final Consumer<MemoryItem> onClick;

    public MemoriesAdapter(Consumer<MemoryItem> onClick) {
        this.onClick = onClick;
    }

    public void submit(List<MemoryItem> items) {
        data.clear();
        if (items != null) {
            data.addAll(items);
        }
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView title;

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.img);
            title = v.findViewById(R.id.title);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memory_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MemoryItem item = data.get(position);
        holder.title.setText(item.title);

        Glide.with(holder.itemView.getContext())
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_camera) // Placeholder for loading
                .into(holder.img);

        holder.itemView.setOnClickListener(v -> {
            if (onClick != null) {
                onClick.accept(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
