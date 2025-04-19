package com.example.wordapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordapp.R;
import com.example.wordapp.models.Word;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class Quiz extends AppCompatActivity {
    private TextView tvQuestion;
    private RadioGroup answerGroup;
    private RadioButton[] options = new RadioButton[4];
    private Button btnSubmit;
    private List<Word> allWords = new ArrayList<>();
    private Word correctAnswer;
    private int questionIndex = 0, score = 0;
    private int totalQuestions = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tvQuestion = findViewById(R.id.tvQuestion);
        answerGroup = findViewById(R.id.answerGroup);
        options[0] = findViewById(R.id.option1);
        options[1] = findViewById(R.id.option2);
        options[2] = findViewById(R.id.option3);
        options[3] = findViewById(R.id.option4);
        btnSubmit = findViewById(R.id.btnSubmit);

        loadAllWords();

        btnSubmit.setOnClickListener(v -> {
            int checkedId = answerGroup.getCheckedRadioButtonId();
            if (checkedId == -1) {
                Toast.makeText(this, "Vui lòng chọn 1 đáp án", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selected = findViewById(checkedId);
            if (selected.getText().toString().equals(correctAnswer.meaning)) {
                score++;
                Toast.makeText(this, "Đúng rồi!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Sai! Đáp án: " + correctAnswer.meaning, Toast.LENGTH_SHORT).show();
            }

            answerGroup.clearCheck();
            questionIndex++;
            if (questionIndex < totalQuestions && questionIndex < allWords.size()) {
                showQuestion();
            } else {
                Intent intent = new Intent(Quiz.this, Result.class);
                intent.putExtra("score", score);
                intent.putExtra("total", totalQuestions);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loadAllWords() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userFoldersRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("folders");

        userFoldersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> folderIds = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    folderIds.add(snap.getKey());
                }
                if (folderIds.isEmpty()) {
                    Toast.makeText(Quiz.this, "Bạn chưa có từ vựng nào!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                int[] loadedFolders = {0}; // Dùng mảng để mutable
                int totalFolders = folderIds.size();

                for (String folderId : folderIds) {
                    FirebaseDatabase.getInstance().getReference("words").child(folderId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    for (DataSnapshot wordSnap : snapshot.getChildren()) {
                                        Word word = wordSnap.getValue(Word.class);
                                        if (word != null && word.word != null && word.meaning != null) {
                                            allWords.add(word);
                                        }
                                    }
                                    loadedFolders[0]++;
                                    if (loadedFolders[0] == totalFolders) {
                                        // Khi đã tải xong tất cả folder
                                        if (allWords.size() >= 4) {
                                            Collections.shuffle(allWords);
                                            if (allWords.size() > 10) {
                                                allWords = allWords.subList(0, 10);
                                            }
                                            totalQuestions = allWords.size();
                                            showQuestion();
                                        } else {
                                            Toast.makeText(Quiz.this, "Không đủ từ để tạo quiz", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError error) {
                                    Toast.makeText(Quiz.this, "Lỗi tải từ", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Quiz.this, "Lỗi tải folder", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQuestion() {
        correctAnswer = allWords.get(questionIndex);
        tvQuestion.setText(correctAnswer.word); // hiển thị từ

        List<String> optionsList = new ArrayList<>();
        optionsList.add(correctAnswer.meaning);

        for (Word w : allWords) {
            if (optionsList.size() >= 4) break;
            if (!w.meaning.equals(correctAnswer.meaning)) {
                optionsList.add(w.meaning);
            }
        }
        Collections.shuffle(optionsList);
        for (int i = 0; i < 4; i++) {
            options[i].setText(optionsList.get(i));
        }
    }
}
