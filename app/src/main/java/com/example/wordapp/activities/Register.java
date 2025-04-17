package com.example.wordapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private EditText edtEmail, edtPassword, edtUsername;
    private Button btnRegister, btnToLogin;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, usernamesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnToLogin = findViewById(R.id.btnToLogin);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        usernamesRef = FirebaseDatabase.getInstance().getReference("usernames");

        btnRegister.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            boolean hasError = false;

            if (username.isEmpty()) {
                edtUsername.setError("Không được để trống tên đăng nhập");
                hasError = true;
            }

            if (email.isEmpty()) {
                edtEmail.setError("Không được để trống email");
                hasError = true;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Email không hợp lệ");
                hasError = true;
            }

            if (password.isEmpty()) {
                edtPassword.setError("Không được để trống mật khẩu");
                hasError = true;
            }

            if (hasError) return;

            // Kiểm tra username có bị trùng không
            usernamesRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(Register.this, "Username đã tồn tại", Toast.LENGTH_SHORT).show();
                    } else {
                        // Nếu chưa tồn tại, tiến hành đăng ký
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        String uid = mAuth.getCurrentUser().getUid();

                                        Map<String, Object> userInfo = new HashMap<>();
                                        userInfo.put("username", username);
                                        userInfo.put("email", email);

                                        usersRef.child(uid).setValue(userInfo);
                                        usernamesRef.child(username).setValue(uid);

                                        Toast.makeText(Register.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Register.this, MainActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(Register.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(Register.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }
}
