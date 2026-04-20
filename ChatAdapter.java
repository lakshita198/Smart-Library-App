package com.example.smartlibraryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<ChatMessage> chatMessages;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        if (message.isUser()) {
            holder.userLayout.setVisibility(View.VISIBLE);
            holder.aiLayout.setVisibility(View.GONE);
            holder.userText.setText(message.getMessage());
        } else {
            holder.userLayout.setVisibility(View.GONE);
            holder.aiLayout.setVisibility(View.VISIBLE);
            holder.aiText.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout userLayout, aiLayout;
        TextView userText, aiText;

        ViewHolder(View itemView) {
            super(itemView);
            userLayout = itemView.findViewById(R.id.userLayout);
            aiLayout = itemView.findViewById(R.id.aiLayout);
            userText = itemView.findViewById(R.id.userText);
            aiText = itemView.findViewById(R.id.aiText);
        }
    }
}
