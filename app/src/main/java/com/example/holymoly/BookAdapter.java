package com.example.holymoly;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookViewHolder> {
    private List<String> imageUrls;
    private Context context;

    public BookAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_books, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BookViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(context).load(imageUrl).into(holder.coverIV);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }
}
// recyclerView 어댑터
class BookViewHolder extends RecyclerView.ViewHolder {
    public ImageView coverIV;

    public BookViewHolder(View imageView) {
        super(imageView);
        coverIV = imageView.findViewById(R.id.coversIV);
    }
}

