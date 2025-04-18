package com.example.wordapp.models;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    private Context context;
    private List<Word> wordList;
    private String folderId;
    private OnEditWordListener editWordListener;
    public WordAdapter(Context context, List<Word> wordList, OnEditWordListener listener) {
        this.context = context;
        this.wordList = wordList;
        this.editWordListener = listener;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public interface OnEditWordListener {
        void onEditWord(Word word);
    }


    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = wordList.get(position);

        holder.tvWord.setText("Từ mới: " + word.word);
        holder.tvType.setText("Loại từ: " + word.type);
        holder.tvMeaning.setText("Nghĩa: " + word.meaning);

        // Xử lý nút xoá
        holder.btnDelete.setOnClickListener(v -> {
            if (folderId == null || word.id == null) return;

            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xoá")
                    .setMessage("Bạn có chắc muốn xoá từ này?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        DatabaseReference ref = FirebaseDatabase.getInstance()
                                .getReference("words")
                                .child(folderId)
                                .child(word.id);

                        ref.removeValue().addOnSuccessListener(unused -> {
                            int pos = holder.getAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION) {
                                wordList.remove(pos);
                                notifyItemRemoved(pos);
                            }
                            Toast.makeText(context, "Đã xoá từ", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });


        // Xử lý nút sửa
        holder.btnEdit.setOnClickListener(v -> {
            if (editWordListener != null) {
                editWordListener.onEditWord(word);
            }
        });

    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvType, tvMeaning;
        ImageButton btnEdit, btnDelete;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tvWord);
            tvType = itemView.findViewById(R.id.tvType);
            tvMeaning = itemView.findViewById(R.id.tvMeaning);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
