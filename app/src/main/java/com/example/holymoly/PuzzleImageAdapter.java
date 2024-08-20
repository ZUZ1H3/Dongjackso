package com.example.holymoly;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PuzzleImageAdapter extends RecyclerView.Adapter<PuzzleImageAdapter.ImageViewHolder> {

    private Context context;
    private List<Bitmap> images;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Bitmap bitmap);
    }

    public PuzzleImageAdapter(Context context, List<Bitmap> images, OnItemClickListener listener) {
        this.context = context;
        this.images = images;
        this.listener = listener;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_list, parent, false);
        return new ImageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        holder.bind(images.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
        }

        public void bind(final Bitmap bitmap, final OnItemClickListener listener) {
            imageView.setImageBitmap(bitmap);
            itemView.setOnClickListener(v -> listener.onItemClick(bitmap));
        }
    }
}
