package com.example.wordapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordapp.R;
import com.example.wordapp.models.FolderAdapter;
import com.example.wordapp.models.Folders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText edtSearch;
    private Button btnAddFolder, btnQuiz, btnLogout;
    private RecyclerView recyclerFolders;
    private List<Folders> folderList = new ArrayList<>();
    private FolderAdapter folderAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, foldersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        edtSearch = findViewById(R.id.edtSearch);
        btnAddFolder = findViewById(R.id.btnAddFolder);
        btnQuiz = findViewById(R.id.btnQuiz);
        btnLogout = findViewById(R.id.btnLogout);
        setSupportActionBar(toolbar);

        recyclerFolders = findViewById(R.id.recyclerFolders);
        recyclerFolders.setLayoutManager(new LinearLayoutManager(this));
        folderAdapter = new FolderAdapter(MainActivity.this, folderList);
        recyclerFolders.setAdapter(folderAdapter);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Lấy username và hiển thị
        usersRef.child(uid).get().addOnSuccessListener(snapshot -> {
            String username = snapshot.child("username").getValue(String.class);
            if (username != null) {
                getSupportActionBar().setTitle("Hi, " + username);
            }
        });

        foldersRef = FirebaseDatabase.getInstance().getReference("folders");
        foldersRef.orderByChild("owner").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                folderList.clear(); // xóa danh sách cũ (nếu có)
                for (DataSnapshot folderSnap : snapshot.getChildren()) {
                    String id = folderSnap.getKey();
                    String name = folderSnap.child("name").getValue(String.class);
                    String owner = folderSnap.child("owner").getValue(String.class);

                    Folders folder = new Folders(id, name, owner);
                    folderList.add(folder);
                }
                folderAdapter.notifyDataSetChanged(); // cập nhật giao diện
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            String keyword = edtSearch.getText().toString().trim();
            if (!keyword.isEmpty()) {
                searchWordAcrossFolders(keyword);
            }
            return true;
        });

        btnAddFolder.setOnClickListener(v -> {
            EditText edtFolderName = new EditText(this);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Nhập tên chủ đề")
                    .setView(edtFolderName)
                    .setPositiveButton("Thêm", (dialogInterface, i) -> {
                        String folderName = edtFolderName.getText().toString().trim();
                        if (!folderName.isEmpty()) {
                            foldersRef = FirebaseDatabase.getInstance().getReference("folders");
                            String folderId = foldersRef.push().getKey();

                            Map<String, Object> folderData = new HashMap<>();
                            folderData.put("name", folderName);
                            folderData.put("owner", uid); // Dùng lại uid đã khai báo

                            // Lưu vào "folders"
                            foldersRef.child(folderId).setValue(folderData).addOnSuccessListener(aVoid -> {
                                // Sau khi lưu xong, thêm vào danh sách và cập nhật RecyclerView
                                Folders newFolder = new Folders(folderId, folderName, uid);
                                folderList.add(newFolder);
                                folderAdapter.notifyItemInserted(folderList.size() - 1);
                                Toast.makeText(this, "Đã thêm chủ đề", Toast.LENGTH_SHORT).show();
                            });
                            // Lưu folderId vào user
                            usersRef.child(uid).child("folders").child(folderId).setValue(true);

                        } else {
                            Toast.makeText(this, "Tên chủ đề không được để trống", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .create();
            dialog.show();
        });




        // Nút quiz
        btnQuiz.setOnClickListener(v -> {
            startActivity(new Intent(this, Quiz.class));
        });

        // Nút thoát
        btnLogout.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc muốn thoát khỏi ứng dụng?")
                    .setPositiveButton("Thoát", (dialog, which) -> {
                        mAuth.signOut();
                        startActivity(new Intent(this, Login.class));
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }
    private void searchWordAcrossFolders(String keyword) {
        DatabaseReference wordsRef = FirebaseDatabase.getInstance().getReference("words");
        List<String> resultList = new ArrayList<>();

        wordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot folderSnap : snapshot.getChildren()) {
                    for (DataSnapshot wordSnap : folderSnap.getChildren()) {
                        String word = wordSnap.child("word").getValue(String.class);
                        String meaning = wordSnap.child("meaning").getValue(String.class);
                        if (word != null && word.toLowerCase().contains(keyword.toLowerCase())) {
                            resultList.add(word + " - " + meaning);
                        }
                    }
                }
                if (resultList.isEmpty()) {
                    showSearchDialog("Không tìm thấy kết quả");
                } else {
                    showSearchDialog(android.text.TextUtils.join("\n\n", resultList));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showSearchDialog(String content) {
        new AlertDialog.Builder(this)
                .setTitle("Kết quả tìm kiếm")
                .setMessage(content)
                .setPositiveButton("Đóng", null)
                .show();
    }
}