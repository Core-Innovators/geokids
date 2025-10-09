package com.coreinnovators.geokids;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signup extends AppCompatActivity {

    private EditText usernameEt, emailEt, passwordEt, cpasswordEt;
    private Button signUpBtn;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        usernameEt = findViewById(R.id.username);
        emailEt = findViewById(R.id.email);
        passwordEt = findViewById(R.id.password);
        cpasswordEt = findViewById(R.id.cpassword);
        signUpBtn = findViewById(R.id.register);

        signUpBtn.setOnClickListener(v -> validateAndRegister());
    }

    private void validateAndRegister() {
        String username = usernameEt.getText().toString().trim();
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();
        String cpassword = cpasswordEt.getText().toString().trim();

        // --- Validation ---
        if (TextUtils.isEmpty(username)) { usernameEt.setError("Username required"); return; }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailEt.setError("Valid email required"); return; }
        if (TextUtils.isEmpty(password) || password.length() < 6) { passwordEt.setError("Password must be at least 6 characters"); return; }
        if (!password.equals(cpassword)) { cpasswordEt.setError("Passwords do not match"); return; }

        // --- Create user ---
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user == null) {
                            Toast.makeText(signup.this, "User not found after registration", Toast.LENGTH_LONG).show();
                            return;
                        }

                        String uid = user.getUid();

                        // --- Save basic user info to Firestore ---
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("username", username);
                        userMap.put("email", email);

                        db.collection("users").document(uid)
                                .set(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    // Navigate to role selection
                                    Intent intent = new Intent(signup.this, role_select.class);
                                    intent.putExtra("uid", uid);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(signup.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_LONG).show());

                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            emailEt.setError("Email already registered");
                        } else {
                            Toast.makeText(signup.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
