package com.example.smartlibraryapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Book> list;
    BookAdapter adapter;
    FirebaseFirestore db;
    EditText searchBar;
    String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userRole = pref.getString("role", "student");

        searchBar = findViewById(R.id.searchBar);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        // 🔥 Passing Context to Adapter for SharedPreferences access
        adapter = new BookAdapter(list, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        if ("librarian".equals(userRole)) {
            setupSwipeToDelete();
        }
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Book deletedBook = list.get(position);
                        db.collection("books").document(deletedBook.qr).delete()
                                .addOnSuccessListener(aVoid -> Toast.makeText(BookListActivity.this, "Book Deleted", Toast.LENGTH_SHORT).show());
                        list.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBooks();
    }

    private void loadBooks() {
        db.collection("books")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    list.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Book b = doc.toObject(Book.class);
                        if (b.title == null) b.title = doc.getString("title");
                        if (b.author == null) b.author = doc.getString("author");
                        if (b.qr == null) b.qr = doc.getId();
                        list.add(b);
                    }
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> loadBooksWithoutSort());
    }

    private void loadBooksWithoutSort() {
        db.collection("books").get().addOnSuccessListener(queryDocumentSnapshots -> {
            list.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Book b = doc.toObject(Book.class);
                if (b.qr == null) b.qr = doc.getId();
                list.add(b);
            }
            adapter.updateList(list);
        });
    }
}
