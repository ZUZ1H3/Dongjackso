package com.example.holymoly;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PuzzleAdapter extends RecyclerView.Adapter<PuzzleAdapter.PuzzleViewHolder> {

    private Context context;
    private List<String> imageUrlList;
    private OnItemClickListener listener;

    public PuzzleAdapter(Context context, List<String> imageUrlList) {
        this.context = context;
        this.imageUrlList = imageUrlList;
    }

    @Override
    public PuzzleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_background, parent, false);
        return new PuzzleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PuzzleViewHolder holder, int position) {
        String imageUrl = imageUrlList.get(position);
        // Glide를 사용하여 이미지 URL 로드
        Glide.with(context).load(imageUrl).into(holder.imageView);

        // 이미지에 패딩 설정 (좌, 상, 우, 하 패딩 각각 5px)
        int padding = 5;
        holder.imageView.setPadding(padding, padding, padding, padding);

        // 이미지에 백그라운드 설정
        Drawable background = ContextCompat.getDrawable(context, R.drawable.puzzle_stroke_box);
        holder.imageView.setBackground(background);

        // 이미지 클릭 리스너 설정
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(imageUrl);
            }
        });
    }

    @Override
    public int getItemCount() { return imageUrlList.size(); }


    // 클릭 리스너 인터페이스 정의
    public interface OnItemClickListener {
        void onItemClick(String imageUrl);
    }

    // 클릭 리스너 설정 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // PuzzleViewHolder 클래스를 PuzzleAdapter 내부 클래스로 작성
    class PuzzleViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PuzzleViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.IV);
        }
    }
}
