package com.example.holymoly;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private final List<Message> messages;
    private final OnCocoClickListener listener;

    public MessageAdapter(List<Message> messages, OnCocoClickListener listener) {
        this.messages = messages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        if (viewType == Message.TYPE_USER) {
            view = inflater.inflate(R.layout.item_user_message, parent, false);
        } else {
            view = inflater.inflate(R.layout.item_bot_message, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);

        holder.messageTextView.setText(message.getText());

        // bot message 에서만
        if(holder.coco != null) {
            holder.coco.setOnClickListener(v -> {
                if(listener != null) listener.onCocoClick(message);
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView coco;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            coco = itemView.findViewById(R.id.coco);
        }
    }
    public interface OnCocoClickListener {
        void onCocoClick(Message message);
    }
}