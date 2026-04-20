package com.example.smartlibraryapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    RecyclerView issuedRecyclerView, wishlistRecyclerView;
    IssuedBooksAdapter issuedAdapter;
    WishlistAdapter wishlistAdapter;
    List<Book> issuedBooksList, wishlistBooksList;
    FirebaseFirestore db;
    Button logoutBtn;
    TextView totalFineText;
    CardView fineCard;
    String currentUsername, userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SharedPreferences pref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentUsername = pref.getString("username", "Lakshita");
        userRole = pref.getString("role", "student");

        issuedRecyclerView = findViewById(R.id.issuedBooksRecyclerView);
        wishlistRecyclerView = findViewById(R.id.wishlistRecyclerView);
        logoutBtn = findViewById(R.id.logoutBtn);
        totalFineText = findViewById(R.id.totalFineText);
        fineCard = findViewById(R.id.fineCard);

        issuedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        wishlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        issuedBooksList = new ArrayList<>();
        wishlistBooksList = new ArrayList<>();
        
        issuedAdapter = new IssuedBooksAdapter(issuedBooksList);
        wishlistAdapter = new WishlistAdapter(wishlistBooksList);
        
        issuedRecyclerView.setAdapter(issuedAdapter);
        wishlistRecyclerView.setAdapter(wishlistAdapter);

        db = FirebaseFirestore.getInstance();
        loadIssuedBooksData();
        loadWishlistData();

        logoutBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.apply();
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        TextView profileName = findViewById(R.id.profileName);
        profileName.setText(currentUsername);
    }

    private void loadWishlistData() {
        db.collection("wishlist_records")
          .whereEqualTo("username", currentUsername)
          .get()
          .addOnSuccessListener(queryDocumentSnapshots -> {
              wishlistBooksList.clear();
              for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                  String title = doc.getString("bookTitle");
                  String qr = doc.getString("qr");
                  wishlistBooksList.add(new Book(title, "Favorite", "available", qr));
              }
              wishlistAdapter.notifyDataSetChanged();
          });
    }

    private void loadIssuedBooksData() {
        Query query;
        if ("librarian".equals(userRole)) {
            query = db.collection("issued_records");
        } else {
            query = db.collection("issued_records").whereEqualTo("username", currentUsername);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            issuedBooksList.clear();
            long totalFine = 0;

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String title = doc.getString("bookTitle");
                String qr = doc.getString("qr");
                String studentName = doc.getString("username");
                Long timestamp = doc.getLong("issueTimestamp");
                
                Book book = new Book(title, "Various", "issued", qr, timestamp);
                if ("librarian".equals(userRole)) {
                    book.author = studentName; 
                }
                
                issuedBooksList.add(book);

                if (timestamp != null) {
                    totalFine += calculateFine(timestamp);
                }
            }
            
            issuedAdapter.notifyDataSetChanged();

            if ("student".equals(userRole) && totalFine > 0) {
                fineCard.setVisibility(View.VISIBLE);
                totalFineText.setText(String.format(Locale.getDefault(), "₹%d", totalFine));
            } else {
                fineCard.setVisibility(View.GONE);
            }
        });
    }

    private long calculateFine(long issueTime) {
        long diff = System.currentTimeMillis() - issueTime;
        long daysPassed = diff / (1000 * 60 * 60 * 24);
        return (daysPassed > 10) ? (daysPassed - 10) * 5 : 0;
    }

    private class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {
        List<Book> books;
        WishlistAdapter(List<Book> books) { this.books = books; }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Book b = books.get(position);
            holder.title.setText(b.title);
            holder.author.setText("Added to favorites");
            holder.status.setText("Wishlisted");
            holder.status.setBackgroundResource(R.drawable.status_bg);
            if (holder.wishlistBtn != null) {
                holder.wishlistBtn.setVisibility(View.VISIBLE);
                holder.wishlistBtn.setImageResource(android.R.drawable.btn_star_big_on);
                holder.wishlistBtn.setColorFilter(ContextCompat.getColor(ProfileActivity.this, R.color.accent));
            }
            holder.itemView.setOnClickListener(null);
        }
        @Override
        public int getItemCount() { return books.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, author, status;
            ImageView wishlistBtn;
            ViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                author = itemView.findViewById(R.id.author);
                status = itemView.findViewById(R.id.status);
                wishlistBtn = itemView.findViewById(R.id.wishlistBtn);
            }
        }
    }

    private class IssuedBooksAdapter extends RecyclerView.Adapter<IssuedBooksAdapter.ViewHolder> {
        List<Book> books;
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        IssuedBooksAdapter(List<Book> books) { this.books = books; }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Book b = books.get(position);
            holder.title.setText(b.title);
            if (holder.wishlistBtn != null) holder.wishlistBtn.setVisibility(View.GONE);
            if ("librarian".equals(userRole)) {
                holder.author.setText(String.format("Issued to: %s", b.author));
                holder.author.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.primary));
            } else {
                holder.author.setText("Currently Reading");
            }
            if (b.issueTimestamp != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(b.issueTimestamp);
                cal.add(Calendar.DAY_OF_YEAR, 10);
                holder.status.setText(String.format("Due: %s", sdf.format(cal.getTime())));
                holder.status.setBackgroundResource(R.drawable.status_bg_red);
            }
            holder.itemView.setOnClickListener(null);
        }
        @Override
        public int getItemCount() { return books.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, author, status;
            ImageView wishlistBtn;
            ViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                author = itemView.findViewById(R.id.author);
                status = itemView.findViewById(R.id.status);
                wishlistBtn = itemView.findViewById(R.id.wishlistBtn);
            }
        }
    }
}
