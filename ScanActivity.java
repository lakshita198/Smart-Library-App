package com.example.smartlibraryapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

public class ScanActivity extends AppCompatActivity {

    FirebaseFirestore db;
    LinearLayout loadingLayout;
    TextView statusText;
    ProgressBar progressBar;
    String userRole, targetStudentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        db = FirebaseFirestore.getInstance();
        loadingLayout = findViewById(R.id.loadingLayout);
        statusText = findViewById(R.id.statusText);
        progressBar = findViewById(R.id.progressBar);

        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userRole = pref.getString("role", "student");
        String myName = pref.getString("username", "User");

        if ("librarian".equals(userRole)) {
            askStudentName();
        } else {
            targetStudentName = myName;
            startScanner();
        }
    }

    private void askStudentName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Issue/Return For");
        
        final EditText input = new EditText(this);
        input.setHint("Enter Student Username");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        
        // Simple padding for the dialog input
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 20, 50, 0);
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Next", (dialog, which) -> {
            targetStudentName = input.getText().toString().trim();
            if (!targetStudentName.isEmpty()) {
                startScanner();
            } else {
                Toast.makeText(this, "Student name is required!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> finish());
        builder.setCancelable(false);
        builder.show();
    }

    private void startScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivityPortrait.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning for: " + targetStudentName);
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            handlePersonalToggle(result.getContents().trim());
        } else {
            finish();
        }
    }

    private void handlePersonalToggle(String qrId) {
        statusText.setText("Updating records for " + targetStudentName + "...");
        String issueId = targetStudentName + "_" + qrId;
        
        db.collection("issued_records").document(issueId).get().addOnSuccessListener(issueDoc -> {
            if (issueDoc.exists()) {
                returnBook(qrId, issueId);
            } else {
                issueBook(qrId, issueId);
            }
        }).addOnFailureListener(e -> showError("Connection Failed"));
    }

    private void issueBook(String qrId, String issueId) {
        db.collection("books").document(qrId).get().addOnSuccessListener(bookDoc -> {
            if (bookDoc.exists()) {
                long total = bookDoc.contains("quantity") ? bookDoc.getLong("quantity") : 1;
                long issued = bookDoc.contains("issuedCount") ? bookDoc.getLong("issuedCount") : 0;

                if (issued < total) {
                    Map<String, Object> issueData = new HashMap<>();
                    issueData.put("qr", qrId);
                    issueData.put("username", targetStudentName);
                    issueData.put("bookTitle", bookDoc.getString("title"));
                    issueData.put("issueTimestamp", System.currentTimeMillis());

                    db.collection("issued_records").document(issueId).set(issueData)
                        .addOnSuccessListener(unused -> {
                            db.collection("books").document(qrId).update(
                                "issuedCount", FieldValue.increment(1),
                                "status", (issued + 1 >= total) ? "issued" : "available"
                            );
                            showSuccess("Book ISSUED to " + targetStudentName + " ✅");
                        });
                } else {
                    showError("Out of Stock! ❌");
                }
            } else {
                showError("Book Not Found!");
            }
        });
    }

    private void returnBook(String qrId, String issueId) {
        db.collection("issued_records").document(issueId).delete().addOnSuccessListener(unused -> {
            db.collection("books").document(qrId).update(
                "issuedCount", FieldValue.increment(-1),
                "status", "available"
            );
            showSuccess("Book RETURNED ✅");
        });
    }

    private void showSuccess(String msg) {
        statusText.setText(msg);
        statusText.setTextColor(getResources().getColor(R.color.primary));
        progressBar.setVisibility(View.GONE);
        new Handler().postDelayed(this::finish, 1500);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        statusText.setText(message);
        statusText.setTextColor(android.graphics.Color.RED);
        Button btn = new Button(this);
        btn.setText("Back");
        btn.setOnClickListener(v -> finish());
        loadingLayout.addView(btn);
    }
}
