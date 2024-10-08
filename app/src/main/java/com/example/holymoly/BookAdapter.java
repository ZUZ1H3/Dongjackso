package com.example.holymoly;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;


public class BookAdapter extends RecyclerView.Adapter<BookViewHolder> {
    private List<String> imageUrls, titles;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public BookAdapter(Context context, List<String> imageUrls, List<String> titles) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.titles = titles;
    }
    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_books, parent, false);
        return new BookViewHolder(view);
    }
    @Override
    public void onBindViewHolder(BookViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        String title = titles.get(position);

        Glide.with(context).load(imageUrl).into(holder.coverIV);
        holder.title.setText(title);

        Handler handler = new Handler();
        Runnable longPressRunnable = () -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemLongClick(position, imageUrl);
            }
        };

        holder.coverIV.setOnClickListener(v -> {
            // 클릭 이벤트 처리
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position, imageUrl);
            }
        });

        holder.coverIV.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 롱 클릭 처리를 위해 2초 후 Runnable 실행
                    handler.postDelayed(longPressRunnable, 2000);
                    return true;
                case MotionEvent.ACTION_UP:
                    // 터치가 끝났을 때 롱 클릭이 아니라면 Runnable 취소
                    handler.removeCallbacks(longPressRunnable);
                case MotionEvent.ACTION_CANCEL:
                    // 롱 클릭 취소
                    handler.removeCallbacks(longPressRunnable);
                    // 클릭 이벤트 처리
                    v.performClick();
                    return true;
                default:
                    return false;
            }
        });
    }

    @Override
    public int getItemCount () { return imageUrls.size(); }

    // 클릭 리스너 인터페이스 정의
    public interface OnItemClickListener {
        void onItemClick(int position, String imageUrl);
        void onItemLongClick(int position, String imageUrl);
    }

    // 클릭 리스너 설정 메서드
    public void setOnItemClickListener (OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}
// recyclerView 어댑터
class BookViewHolder extends RecyclerView.ViewHolder {
    public ImageView coverIV;
    public TextView title;
    public BookViewHolder(View itemView) {
        super(itemView);
        coverIV = itemView.findViewById(R.id.coversIV);
        title = itemView.findViewById(R.id.tv_title);
    }
}
