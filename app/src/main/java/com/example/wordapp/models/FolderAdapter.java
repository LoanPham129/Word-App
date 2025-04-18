package com.example.wordapp.models;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordapp.R;
import com.example.wordapp.activities.AddWord;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {
    private List<Folders> folderList;
    private Context context;

    public FolderAdapter(Context context, List<Folders> folderList) {
        this.context = context;
        this.folderList = folderList;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        Folders folder = folderList.get(position);
        holder.txtFolderName.setText(folder.getName());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddWord.class);
            intent.putExtra("folderId", folder.getId());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });

        holder.btnAddWord.setOnClickListener(v -> {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            int padding = (int) (16 * context.getResources().getDisplayMetrics().density); // padding 16dp
            layout.setPadding(padding, padding, padding, padding);

            EditText edtWord = new EditText(context);
            edtWord.setHint("Từ mới");
            layout.addView(edtWord);

            EditText edtType = new EditText(context);
            edtType.setHint("Loại từ");
            layout.addView(edtType);

            EditText edtMeaning = new EditText(context);
            edtMeaning.setHint("Nghĩa");
            layout.addView(edtMeaning);

            new AlertDialog.Builder(context)
                    .setTitle("Thêm từ vào: " + folder.getName())
                    .setView(layout)
                    .setPositiveButton("Thêm", (dialog, which) -> {
                        String word = edtWord.getText().toString().trim();
                        String type = edtType.getText().toString().trim();
                        String meaning = edtMeaning.getText().toString().trim();

                        if (word.isEmpty() || meaning.isEmpty()) {
                            Toast.makeText(context, "Từ và nghĩa không được để trống", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String wordId = FirebaseDatabase.getInstance().getReference("words")
                                .child(folder.getId()).push().getKey();

                        Word newWord = new Word(wordId, word, type, meaning);
                        FirebaseDatabase.getInstance().getReference("words")
                                .child(folder.getId())
                                .child(wordId)
                                .setValue(newWord)
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(context, "Đã thêm từ thành công", Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        holder.btnDeleteFolder.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xoá")
                    .setMessage("Bạn có chắc muốn xoá chủ đề này không?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        FirebaseDatabase.getInstance().getReference("folders")
                                .child(folder.getId())
                                .removeValue()
                                .addOnSuccessListener(unused -> {
                                    folderList.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                    Toast.makeText(context, "Đã xoá chủ đề", Toast.LENGTH_SHORT).show();
                                });

                        // Cũng xoá cả liên kết trong users nếu cần:
                        // FirebaseDatabase.getInstance().getReference("users")
                        //     .child(currentUserId).child("folders").child(folder.getId()).removeValue();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView txtFolderName;
        ImageButton btnAddWord, btnDeleteFolder;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFolderName = itemView.findViewById(R.id.txtFolderName);
            btnAddWord = itemView.findViewById(R.id.btnAddWord);
            btnDeleteFolder = itemView.findViewById(R.id.btnDeleteFolder);
        }
    }
}
