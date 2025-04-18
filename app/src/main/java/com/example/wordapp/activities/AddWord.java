package com.example.wordapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordapp.R;
import com.example.wordapp.models.Word;
import com.example.wordapp.models.WordAdapter;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AddWord extends AppCompatActivity {

    private EditText edtWord, edtType, edtMeaning;
    private Button btnAdd, btnCancel;
    private RecyclerView recyclerWords;
    private WordAdapter wordAdapter;
    private List<Word> wordList;
    private MaterialCardView cardAdd;
    private FloatingActionButton fabAdd;
    private DatabaseReference wordsRef;
    private String folderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        // Lấy folderId được truyền sang
        folderId = getIntent().getStringExtra("folderId");
        if (folderId == null) {
            Toast.makeText(this, "Không có folderId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo Firebase
        wordsRef = FirebaseDatabase.getInstance().getReference("words").child(folderId);

        // Ánh xạ view
        edtWord = findViewById(R.id.edtWord);
        edtType = findViewById(R.id.edtType);
        edtMeaning = findViewById(R.id.edtMeaning);
        btnAdd = findViewById(R.id.btnAdd);
        btnCancel = findViewById(R.id.btnCancel);
        recyclerWords = findViewById(R.id.recyclerWords);

        cardAdd = findViewById(R.id.card_add_word);
        fabAdd = findViewById(R.id.fabAdd);

        // RecyclerView setup
        wordList = new ArrayList<>();
        wordAdapter = new WordAdapter(this, wordList, word -> {
            edtWord.setText(word.word);
            edtType.setText(word.type);
            edtMeaning.setText(word.meaning);
            btnAdd.setText("Cập nhật");
            btnAdd.setTag(word.id);
            cardAdd.setVisibility(View.VISIBLE);
            fabAdd.hide();
        });
        wordAdapter.setFolderId(folderId);
        recyclerWords.setAdapter(wordAdapter);
        recyclerWords.setLayoutManager(new LinearLayoutManager(this));

        loadWords();

        fabAdd.setOnClickListener(v -> {
            cardAdd.setVisibility(View.VISIBLE);
            recyclerWords.smoothScrollToPosition(wordList.size());
            fabAdd.hide();
        });

        btnCancel.setOnClickListener(v -> {
            resetAddForm();
        });

        btnAdd.setOnClickListener(v -> {
            String wordText = edtWord.getText().toString().trim();
            String type = edtType.getText().toString().trim();
            String meaning = edtMeaning.getText().toString().trim();

            if (wordText.isEmpty() || meaning.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ từ và nghĩa", Toast.LENGTH_SHORT).show();
                return;
            }

            Object tag = btnAdd.getTag();
            if (tag != null) {
                // Chế độ sửa
                String wordId = tag.toString();
                Word updated = new Word(wordId, wordText, type, meaning);
                wordsRef.child(wordId).setValue(updated)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Đã cập nhật từ", Toast.LENGTH_SHORT).show();
                            resetAddForm();
                        });
            } else {
                // Chế độ thêm mới
                String wordId = wordsRef.push().getKey();
                Word newWord = new Word(wordId, wordText, type, meaning);
                wordsRef.child(wordId).setValue(newWord)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Đã thêm từ", Toast.LENGTH_SHORT).show();
                            resetAddForm();
                        });
            }
        });

        // Xử lý nút back (nếu có)
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
    private void loadWords() {
        wordsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                wordList.clear();
                for (DataSnapshot wordSnap : snapshot.getChildren()) {
                    Word word = wordSnap.getValue(Word.class);
                    wordList.add(word);
                }
                wordAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AddWord.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void resetAddForm() {
        edtWord.setText("");
        edtType.setText("");
        edtMeaning.setText("");
        btnAdd.setText("Thêm");
        btnAdd.setTag(null);
        cardAdd.setVisibility(View.GONE);
        fabAdd.show();
    }
}

