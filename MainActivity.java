package com.example.smartlibraryapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    TextView totalBooks, issuedBooks, welcomeText, totalLabel, issuedLabel, quoteText;
    ImageView profileIcon;
    BottomNavigationView bottomNavigation;
    RecyclerView recentBooksRecyclerView, featuredRecyclerView;
    
    FirebaseFirestore db;
    String userRole, currentUsername;
    Set<String> wishlistSet = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userRole = pref.getString("role", "student"); 
        currentUsername = pref.getString("username", "User");

        totalBooks = findViewById(R.id.totalBooks);
        issuedBooks = findViewById(R.id.issuedBooks);
        welcomeText = findViewById(R.id.welcomeText);
        quoteText = findViewById(R.id.quoteText);
        profileIcon = findViewById(R.id.profileIcon);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        recentBooksRecyclerView = findViewById(R.id.recentBooksRecyclerView);
        featuredRecyclerView = findViewById(R.id.featuredRecyclerView);
        
        totalLabel = findViewById(R.id.totalBooksLabel);
        issuedLabel = findViewById(R.id.issuedBooksLabel);

        welcomeText.setText("Hello " + currentUsername + " 👋");

        setRandomQuote();
        loadWishlist();
        setupFeaturedBooks();
        setupRecentBooks();
        applyRoleRestrictions();

        profileIcon.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_add) {
                startActivity(new Intent(MainActivity.this, AddBookActivity.class));
                return true;
            } else if (id == R.id.nav_scan) {
                startActivity(new Intent(MainActivity.this, ScanActivity.class));
                return true;
            } else if (id == R.id.nav_view) {
                startActivity(new Intent(MainActivity.this, BookListActivity.class));
                return true;
            } else if (id == R.id.nav_ai) {
                startActivity(new Intent(MainActivity.this, AiAssistantActivity.class));
                return true;
            }
            return false;
        });

        loadStats();
    }

    private void loadWishlist() {
        db.collection("wishlist_records")
          .whereEqualTo("username", currentUsername)
          .get()
          .addOnSuccessListener(queryDocumentSnapshots -> {
              wishlistSet.clear();
              for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                  wishlistSet.add(doc.getString("qr"));
              }
              // Refresh adapters after loading wishlist
              if (recentBooksRecyclerView.getAdapter() != null) recentBooksRecyclerView.getAdapter().notifyDataSetChanged();
              if (featuredRecyclerView.getAdapter() != null) featuredRecyclerView.getAdapter().notifyDataSetChanged();
          });
    }

    private void setRandomQuote() {
        String[] quotes = {
            "A book is a dream that you hold in your hand.",
            "Today a reader, tomorrow a leader.",
            "There is no friend as loyal as a book.",
            "Reading is a conversation. All books talk.",
            "The more that you read, the more things you will know.",
            "A room without books is like a body without a soul.",
            "Books are a uniquely portable magic."
        };
        if (quoteText != null) {
            quoteText.setText(quotes[new Random().nextInt(quotes.length)]);
        }
    }

    private void applyRoleRestrictions() {
        if ("student".equals(userRole)) {
            bottomNavigation.getMenu().findItem(R.id.nav_add).setVisible(false);
            bottomNavigation.getMenu().findItem(R.id.nav_scan).setVisible(false);
            
            if (totalLabel != null) totalLabel.setText("In Library");
            if (issuedLabel != null) issuedLabel.setText("My Books");
        } else {
            if (totalLabel != null) totalLabel.setText("Total Inventory");
            if (issuedLabel != null) issuedLabel.setText("Global Issued");
        }
    }

    private void setupFeaturedBooks() {
        List<Book> featuredList = new ArrayList<>();
        featuredList.add(new Book("The Alchemist", "Paulo Coelho", "available", "book1", "drawable/book1", System.currentTimeMillis()));
        featuredList.add(new Book("Atomic Habits", "James Clear", "available", "book2", "drawable/book2", System.currentTimeMillis()));
        featuredList.add(new Book("Think & Grow Rich", "Napoleon Hill", "available", "book3", "drawable/book3", System.currentTimeMillis()));

        featuredRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        featuredRecyclerView.setAdapter(new DashboardBookAdapter(featuredList));
    }

    private void setupRecentBooks() {
        List<Book> recentList = new ArrayList<>();
        DashboardBookAdapter adapter = new DashboardBookAdapter(recentList);
        recentBooksRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recentBooksRecyclerView.setAdapter(adapter);

        db.collection("books")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(4)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recentList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Book b = doc.toObject(Book.class);
                        if (b.title == null) b.title = doc.getString("title");
                        if (b.qr == null) b.qr = doc.getId();
                        recentList.add(b);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
        loadWishlist();
        setupRecentBooks();
    }

    private void loadStats() {
        db.collection("books").get().addOnSuccessListener(snapshots -> {
            long totalInventory = 0;
            long globalIssuedCount = 0;
            for (QueryDocumentSnapshot doc : snapshots) {
                Long qty = doc.getLong("quantity");
                Long iCount = doc.getLong("issuedCount");
                totalInventory += (qty != null) ? qty : 1;
                globalIssuedCount += (iCount != null) ? iCount : 0;
            }
            
            final long finalGlobalIssued = globalIssuedCount;
            final long finalInventory = totalInventory;

            db.collection("issued_records").get().addOnSuccessListener(issuedSnaps -> {
                if ("librarian".equals(userRole)) {
                    totalBooks.setText(String.valueOf(finalInventory - finalGlobalIssued));
                    issuedBooks.setText(String.valueOf(finalGlobalIssued));
                } else {
                    long myIssued = 0;
                    for (QueryDocumentSnapshot doc : issuedSnaps) {
                        if (currentUsername.equals(doc.getString("username"))) {
                            myIssued++;
                        }
                    }
                    totalBooks.setText(String.valueOf(finalInventory - finalGlobalIssued));
                    issuedBooks.setText(String.valueOf(myIssued));
                }
            });
        });
    }

    private class DashboardBookAdapter extends RecyclerView.Adapter<DashboardBookAdapter.ViewHolder> {
        List<Book> books;
        DashboardBookAdapter(List<Book> books) { this.books = books; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_dashboard, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Book b = books.get(position);
            holder.name.setText(b.title);
            holder.author.setText(b.author != null ? b.author : "Unknown Author");
            
            if (b.imageUrl != null && b.imageUrl.startsWith("drawable/")) {
                String drawableName = b.imageUrl.replace("drawable/", "");
                int resId = getResources().getIdentifier(drawableName, "drawable", getPackageName());
                holder.img.setImageResource(resId != 0 ? resId : android.R.drawable.ic_menu_agenda);
            } else {
                Glide.with(MainActivity.this).load(b.imageUrl).placeholder(android.R.drawable.ic_menu_agenda).centerCrop().into(holder.img);
            }

            // 🔥 Dashboard Wishlist Toggle Logic
            boolean isWishlisted = wishlistSet.contains(b.qr);
            holder.wishBtn.setColorFilter(isWishlisted ? Color.parseColor("#EC4899") : Color.parseColor("#80FFFFFF"));
            holder.wishBtn.setImageResource(isWishlisted ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

            holder.wishBtn.setOnClickListener(v -> {
                String wishId = currentUsername + "_" + b.qr;
                if (isWishlisted) {
                    db.collection("wishlist_records").document(wishId).delete();
                    wishlistSet.remove(b.qr);
                    Toast.makeText(MainActivity.this, "Removed from Favorites ❤️", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> data = new HashMap<>();
                    data.put("username", currentUsername);
                    data.put("qr", b.qr);
                    data.put("bookTitle", b.title);
                    db.collection("wishlist_records").document(wishId).set(data);
                    wishlistSet.add(b.qr);
                    Toast.makeText(MainActivity.this, "Added to Favorites ❤️", Toast.LENGTH_SHORT).show();
                }
                notifyItemChanged(position);
            });
        }

        @Override
        public int getItemCount() { return books.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, author;
            ImageView img, wishBtn;
            ViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.bookName);
                author = itemView.findViewById(R.id.bookAuthor);
                img = itemView.findViewById(R.id.bookImage);
                wishBtn = itemView.findViewById(R.id.wishlistBtnDash);
            }
        }
    }
}
