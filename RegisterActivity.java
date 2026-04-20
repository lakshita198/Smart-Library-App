package com.example.smartlibraryapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText username, password;
    Button registerBtn;
    TextView goToLogin;
    RadioGroup roleGroup;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (pref.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_register);
        db = FirebaseFirestore.getInstance();

        username = findViewById(R.id.regEmail);
        password = findViewById(R.id.regPass);
        registerBtn = findViewById(R.id.registerBtn);
        goToLogin = findViewById(R.id.goToLogin);
        roleGroup = findViewById(R.id.roleGroup);

        registerBtn.setOnClickListener(v -> {
            if (username.getText() == null || password.getText() == null) return;
            
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();
            
            String roleValue = "student";
            if (roleGroup.getCheckedRadioButtonId() == R.id.radioLibrarian) {
                roleValue = "librarian";
            }
            final String role = roleValue;

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            registerBtn.setEnabled(false);
            registerBtn.setText("Registering...");

            // 🔥 Database mein user save kar rahe hain (Taaki overwrite na ho)
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", user);
            userData.put("password", pass);
            userData.put("role", role);

            db.collection("users").document(user).set(userData)
                .addOnSuccessListener(unused -> {
                    // Local login state update
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("username", user);
                    editor.putString("role", role);
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();

                    Toast.makeText(this, "Registration Successful! ✅", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    registerBtn.setEnabled(true);
                    registerBtn.setText("Register");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });

        goToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}
