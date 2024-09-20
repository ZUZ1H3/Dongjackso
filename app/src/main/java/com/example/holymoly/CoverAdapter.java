package com.example.holymoly;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CoverAdapter extends RecyclerView.Adapter<ViewHolder> {
    private Context context;
    private List<String> urlList;
    private final OnImageClickListener listener;

    public CoverAdapter(Context context, List<String> urlList, OnImageClickListener listener) {
        this.context = context;
        this.urlList = urlList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_covers, parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 이미지 로드
        String url = urlList.get(position);
        Glide.with(context).load(url).into(holder.imageView);

        // 클릭 이벤트 설정
        holder.imageView.setOnClickListener(v -> {
            listener.onClick(url);
        });
    }

    @Override
    public int getItemCount() { return urlList.size(); }

    public interface OnImageClickListener {
        void onClick(String imageUrl);
    }
}

class ViewHolder extends RecyclerView.ViewHolder {
    public ImageView imageView;

    public ViewHolder(View item) {
        super(item);
        imageView = item.findViewById(R.id.iv);
    }
}
