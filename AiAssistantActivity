package com.example.smartlibraryapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AiAssistantActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private EditText inputEditText;
    private ImageButton sendBtn, micBtn;
    private FirebaseFirestore db;
    private static final int SPEECH_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_assistant);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.chatRecyclerView);
        inputEditText = findViewById(R.id.aiInput);
        sendBtn = findViewById(R.id.aiSendBtn);
        micBtn = findViewById(R.id.aiMicBtn);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        addMessage("Hi Lakshita! How can I help you with your library today? ✨", false);

        sendBtn.setOnClickListener(v -> {
            String query = inputEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                addMessage(query, true);
                inputEditText.setText("");
                processAiResponse(query.toLowerCase());
            }
        });

        micBtn.setOnClickListener(v -> startVoiceInput());
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Voice input not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                inputEditText.setText(spokenText);
                sendBtn.performClick();
            }
        }
    }

    private void addMessage(String text, boolean isUser) {
        messageList.add(new ChatMessage(text, isUser));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.smoothScrollToPosition(messageList.size() - 1);
    }

    private void processAiResponse(String query) {
        new Handler().postDelayed(() -> {
            if (query.contains("total") || query.contains("how many books")) {
                getLibraryStats();
            } else if (query.contains("finance")) {
                String[] books = {"'The Intelligent Investor' by Benjamin Graham", "'Rich Dad Poor Dad' by Robert Kiyosaki", "'The Psychology of Money' by Morgan Housel", "'Think and Grow Rich'", "'Your Money or Your Life'"};
                addMessage("Master your finances with: " + getRandom(books), false);
            } else if (query.contains("hindi")) {
                String[] books = {"'Godan' by Premchand", "'Gunahon Ka Devta'", "'Madhushala'", "'Chidambara'", "'Kamayani'", "'Yama' by Mahadevi Varma"};
                addMessage("For Hindi literature lovers: " + getRandom(books), false);
            } else if (query.contains("english")) {
                String[] books = {"'To Kill a Mockingbird'", "'The Great Gatsby'", "'Pride and Prejudice'", "'Jane Eyre'", "'Wuthering Heights'", "'Frankenstein'"};
                addMessage("English classics you might like: " + getRandom(books), false);
            } else if (query.contains("android") || query.contains("app development") || query.contains("kotlin")) {
                String[] books = {"'Android Programming: Big Nerd Ranch'", "'Head First Android Development'", "'Kotlin in Action'", "'Clean Architecture for Android'", "'Android Programming: Pushing the Limits'"};
                addMessage("Master App Development with: " + getRandom(books), false);
            } else if (query.contains("programming") || query.contains("coding") || query.contains("java") || query.contains("python") || query.contains("c++") || query.contains("javascript")) {
                String[] books = {"'Clean Code'", "'Pragmatic Programmer'", "'Python Crash Course'", "'Head First Java'", "'Introduction to Algorithms'", "'Eloquent JavaScript'", "'Code Complete'"};
                addMessage("Level up your coding skills with: " + getRandom(books), false);
            } else if (query.contains("dccn") || query.contains("network")) {
                String[] books = {"'Computer Networks' by Tanenbaum", "'Data Communications' by Forouzan", "'TCP/IP Illustrated'", "'Network Programmability and Automation'"};
                addMessage("Networking essentials: " + getRandom(books), false);
            } else if (query.contains("math")) {
                String[] books = {"'Calculus' by Stewart", "'Introduction to Algorithms'", "'Advanced Engineering Math'", "'Linear Algebra' by Gilbert Strang", "'Godel, Escher, Bach'"};
                addMessage("Math recommendations: " + getRandom(books), false);
            } else if (query.contains("physics")) {
                String[] books = {"'Concepts of Physics' by HC Verma", "'Fundamentals of Physics' by Resnick'", "'Feynman Lectures on Physics'", "'The Elegant Universe'"};
                addMessage("Explore Physics with: " + getRandom(books), false);
            } else if (query.contains("chemistry")) {
                String[] books = {"'Modern Chemistry'", "'Organic Chemistry' by Morrison & Boyd", "'Physical Chemistry' by Atkins", "'Inorganic Chemistry' by JD Lee", "'The Disappearing Spoon'"};
                addMessage("Chemistry bestsellers: " + getRandom(books), false);
            } else if (query.contains("biology")) {
                String[] books = {"'Campbell Biology'", "'The Selfish Gene'", "'Sapiens'", "'Origin of Species'", "'The Immortal Life of Henrietta Lacks'"};
                addMessage("Dive into Biology: " + getRandom(books), false);
            } else if (query.contains("science")) {
                String[] books = {"'Cosmos' by Carl Sagan", "'A Brief History of Time'", "'The Selfish Gene'", "'What If?' by Randall Munroe", "'Astrophysics for People in a Hurry'"};
                addMessage("Fascinating Science reads: " + getRandom(books), false);
            } else if (query.contains("thriller") || query.contains("suspense")) {
                String[] books = {"'The Silent Patient'", "'Gone Girl'", "'Verity'", "'The Girl on the Train'", "'The Da Vinci Code'", "'The Guest List'", "'The Maid'"};
                addMessage("High-stakes suspense: " + getRandom(books), false);
            } else if (query.contains("fantasy")) {
                String[] books = {"'Harry Potter'", "'The Hobbit'", "'Game of Thrones'", "'The Name of the Wind'", "'Mistborn'", "'The Way of Kings'", "'Circe'"};
                addMessage("Enter a magical world: " + getRandom(books), false);
            } else if (query.contains("romance")) {
                String[] books = {"'It Ends with Us'", "'The Hating Game'", "'Me Before You'", "'The Notebook'", "'Normal People'", "'Beach Read'", "'Beach Read'"};
                addMessage("Heartwarming romance: " + getRandom(books), false);
            } else if (query.contains("self help") || query.contains("improve")) {
                String[] books = {"'Atomic Habits'", "'Thinking Fast and Slow'", "'The 5 AM Club'", "'Man's Search for Meaning'", "'The Power of Habit'", "'Grit' by Angela Duckworth"};
                addMessage("Improve your life with: " + getRandom(books), false);
            } else if (query.contains("mystery")) {
                String[] books = {"'Sherlock Holmes'", "'And Then There Were None'", "'The Guest List'", "'Murder on the Orient Express'", "'The Word Is Murder'"};
                addMessage("Solve the mystery: " + getRandom(books), false);
            } else if (query.contains("business") || query.contains("money")) {
                String[] books = {"'Rich Dad Poor Dad'", "'The Psychology of Money'", "'Zero to One'", "'Think and Grow Rich'", "'The Intelligent Investor'"};
                addMessage("Business & Finance tips: " + getRandom(books), false);
            } else if (query.contains("philosophy")) {
                String[] books = {"'Meditations' by Marcus Aurelius", "'The Republic' by Plato", "'Sophie's World'", "'Thus Spoke Zarathustra'"};
                addMessage("Deep thoughts: " + getRandom(books), false);
            } else if (query.contains("psychology")) {
                String[] books = {"'Thinking, Fast and Slow'", "'Predictably Irrational'", "'The Man Who Mistook His Wife for a Hat'", "'Quiet' by Susan Cain"};
                addMessage("Understand the mind with: " + getRandom(books), false);
            } else if (query.contains("travel")) {
                String[] books = {"'Vagabonding'", "'Into the Wild'", "'The Alchemist'", "'A Walk in the Woods'", "'The Beach'"};
                addMessage("Start your journey with: " + getRandom(books), false);
            } else if (query.contains("history")) {
                String[] books = {"'Sapiens'", "'The Book Thief'", "'Guns, Germs, and Steel'", "'A People's History of the United States'"};
                addMessage("Travel back in time: " + getRandom(books), false);
            } else if (query.contains("status") || query.contains("issued")) {
                getIssuedBooksCount();
            } else if (query.contains("hello") || query.contains("hi") || query.contains("hey")) {
                addMessage("Hi Lakshita! Need a book recommendation or library stats?", false);
            } else if (query.contains("thank")) {
                addMessage("You're welcome! Happy reading! 😊", false);
            } else if (query.contains("recommend") || query.contains("suggest") || query.contains("good book")) {
                String[] general = {"'The Alchemist'", "'1984'", "'The Little Prince'", "'Sapiens'", "'Deep Work'", "'The Kite Runner'", "'Born a Crime'", "'Educated'", "'The Book Thief'", "'Life of Pi'", "'A Man Called Ove'"};
                addMessage("I suggest you read " + getRandom(general) + ", it's an absolute masterpiece!", false);
            } else {
                addMessage("I'm not sure about that. Try asking for a category like Finance, Psychology, Math, Python, Thriller, or Romance!", false);
            }
        }, 1000);
    }

    private String getRandom(String[] array) {
        return array[new Random().nextInt(array.length)];
    }

    private void getLibraryStats() {
        db.collection("books").get().addOnSuccessListener(snapshots -> {
            addMessage("You have a total of " + snapshots.size() + " books in your library collection.", false);
        });
    }

    private void getIssuedBooksCount() {
        db.collection("books").whereEqualTo("status", "issued").get().addOnSuccessListener(snapshots -> {
            addMessage("There are currently " + snapshots.size() + " books issued.", false);
        });
    }
}
