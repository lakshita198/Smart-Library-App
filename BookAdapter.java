package com.example.smartlibraryapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    private List<Book> list;
    private List<Book> listFull;
    private Set<String> wishlistSet = new HashSet<>();
    private String currentUsername;
    private FirebaseFirestore db;

    public BookAdapter(List<Book> list, Context context) {
        this.list = list;
        this.listFull = new ArrayList<>(list);
        this.db = FirebaseFirestore.getInstance();
        
        SharedPreferences pref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        this.currentUsername = pref.getString("username", "User");
        
        loadWishlist();
    }

    private void loadWishlist() {
        db.collection("wishlist_records")
          .whereEqualTo("username", currentUsername)
          .get()
          .addOnSuccessListener(queryDocumentSnapshots -> {
              wishlistSet.clear();
              for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                  wishlistSet.add(doc.getString("qr"));
              }
              notifyDataSetChanged();
          });
    }

    public void updateList(List<Book> newList) {
        this.list = newList;
        this.listFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        List<Book> filteredList = new ArrayList<>();
        for (Book item : listFull) {
            if (item.title.toLowerCase().contains(text.toLowerCase()) || 
                item.author.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        list = filteredList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, author, status;
        ImageView bookImage, wishlistBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            author = itemView.findViewById(R.id.author);
            status = itemView.findViewById(R.id.status);
            bookImage = itemView.findViewById(R.id.bookIconInList);
            wishlistBtn = itemView.findViewById(R.id.wishlistBtn);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book b = list.get(position);

        holder.title.setText(b.title);
        holder.author.setText(b.author);

        if (b.imageUrl != null && !b.imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(b.imageUrl).into(holder.bookImage);
        } else {
            holder.bookImage.setImageResource(android.R.drawable.ic_menu_agenda);
        }

        if (b.issuedCount >= b.quantity) {
            holder.status.setText("Issued");
            holder.status.setBackgroundResource(R.drawable.status_bg_red);
        } else {
            holder.status.setText("Available");
            holder.status.setBackgroundResource(R.drawable.status_bg);
        }

        // 🔥 Wishlist Logic
        boolean isWishlisted = wishlistSet.contains(b.qr);
        if (isWishlisted) {
            holder.wishlistBtn.setImageResource(android.R.drawable.btn_star_big_on);
            holder.wishlistBtn.setColorFilter(android.graphics.Color.parseColor("#EC4899")); // Red/Pink
        } else {
            holder.wishlistBtn.setImageResource(android.R.drawable.btn_star_big_off);
            holder.wishlistBtn.setColorFilter(android.graphics.Color.parseColor("#CCCCCC")); // Grey
        }

        holder.wishlistBtn.setOnClickListener(v -> {
            String wishId = currentUsername + "_" + b.qr;
            if (isWishlisted) {
                // Remove from Wishlist
                db.collection("wishlist_records").document(wishId).delete();
                wishlistSet.remove(b.qr);
                Toast.makeText(v.getContext(), "Removed from Favorites ❤️", Toast.LENGTH_SHORT).show();
            } else {
                // Add to Wishlist
                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("username", currentUsername);
                data.put("qr", b.qr);
                data.put("bookTitle", b.title);
                db.collection("wishlist_records").document(wishId).set(data);
                wishlistSet.add(b.qr);
                Toast.makeText(v.getContext(), "Added to Favorites ❤️", Toast.LENGTH_SHORT).show();
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
