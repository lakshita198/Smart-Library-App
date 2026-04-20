package com.example.smartlibraryapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ChatActivity extends AppCompatActivity {

    EditText userInput;
    Button sendBtn;
    TextView responseText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userInput = findViewById(R.id.userInput);
        sendBtn = findViewById(R.id.sendBtn);
        responseText = findViewById(R.id.responseText);

        sendBtn.setOnClickListener(v -> {
            String question = userInput.getText().toString();
            handleUserQuery(question);
        });
    }

    private void handleUserQuery(String input) {

        input = input.toLowerCase();

        // 📚 SHOW BOOKS
        if (input.contains("show") || input.contains("my books")) {

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("books")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {

                        StringBuilder result = new StringBuilder("🤖 Your Books:\n");

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                            String title = doc.getString("title");
                            String author = doc.getString("author");

                            result.append("📖 ").append(title)
                                    .append(" by ").append(author).append("\n");
                        }

                        responseText.setText(result.toString());
                    });
        }

        // 🎯 SUGGEST BOOK
        else if (input.contains("suggest")) {

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("books")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {

                        if (queryDocumentSnapshots.size() > 0) {

                            int random = (int)(Math.random() * queryDocumentSnapshots.size());

                            String title = queryDocumentSnapshots.getDocuments().get(random).getString("title");
                            String author = queryDocumentSnapshots.getDocuments().get(random).getString("author");

                            responseText.setText("🤖 Try reading: " + title + " by " + author);
                        }
                    });
        }

        // 🧠 NORMAL AI
        else {
            String response = AIHelper.getResponse(input);
            responseText.setText(response);
        }
    }
}
