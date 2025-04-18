package com.example.wordapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordapp.R;

public class Result extends AppCompatActivity {

    private TextView tvResult;
    private Button btnRetry, btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvResult = findViewById(R.id.tvResult);
        btnRetry = findViewById(R.id.btnRetry);
        btnExit = findViewById(R.id.btnExit);

        int correct = getIntent().getIntExtra("score", 0);
        int total = getIntent().getIntExtra("total", 0);

        TextView tvResult = findViewById(R.id.tvResult);
        tvResult.setText("Bạn đã trả lời đúng " + correct + "/" + total + " câu");

        btnRetry.setOnClickListener(v -> {
            Intent intent = new Intent(Result.this, Quiz.class);
            intent.putExtra("folderId", getIntent().getStringExtra("folderId")); // truyền lại folder nếu cần
            startActivity(intent);
            finish();
        });

        btnExit.setOnClickListener(v -> {
            Intent intent = new Intent(Result.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // clear ngăn xếp
            startActivity(intent);
            finish();
        });
    }
}
