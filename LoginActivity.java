package com.example.smartlibraryapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText username, password;
    Button loginBtn;
    TextView goToRegister;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        
        // 🔥 Stay Logged In Check
        if (pref.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        db = FirebaseFirestore.getInstance();

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        goToRegister = findViewById(R.id.goToRegister);

        loginBtn.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            loginBtn.setEnabled(false);
            loginBtn.setText("Logging in...");

            // 🔥 Firestore se user check kar rahe hain taaki Alice aur Lakshita dono exist karein
            db.collection("users").document(user).get()
                .addOnSuccessListener(documentSnapshot -> {
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Login");

                    if (documentSnapshot.exists()) {
                        String dbPass = documentSnapshot.getString("password");
                        String role = documentSnapshot.getString("role");

                        if (pass.equals(dbPass)) {
                            // Login Success
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("username", user);
                            editor.putString("role", role);
                            editor.putBoolean("isLoggedIn", true);
                            editor.apply();

                            Toast.makeText(this, "Welcome Back! ✅", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Wrong Password! ❌", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "User not found! Please Register.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Login");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });

        goToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}
