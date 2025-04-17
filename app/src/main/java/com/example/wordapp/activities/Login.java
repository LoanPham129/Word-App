package com.example.wordapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class Login extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnLogin, btnToRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference usernamesRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnToRegister = findViewById(R.id.btnToRegister);

        mAuth = FirebaseAuth.getInstance();
        usernamesRef = FirebaseDatabase.getInstance().getReference("usernames");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            boolean hasError = false;

            if (username.isEmpty()) {
                edtUsername.setError("Không được để trống");
                hasError = true;
            }

            if (password.isEmpty()) {
                edtPassword.setError("Không được để trống");
                hasError = true;
            }

            if (hasError) return;

            // Tìm UID từ username
            usernamesRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String uid = snapshot.getValue(String.class);

                        // Tìm email từ UID
                        usersRef.child(uid).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot emailSnap) {
                                if (emailSnap.exists()) {
                                    String email = emailSnap.getValue(String.class);

                                    // Đăng nhập bằng email + password
                                    mAuth.signInWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(Login.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(Login.this, MainActivity.class));
                                                    finish();
                                                } else {
                                                    Toast.makeText(Login.this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(Login.this, "Không tìm thấy email", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Toast.makeText(Login.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(Login.this, "Username không tồn tại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(Login.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, Register.class));
        });
    }
}
