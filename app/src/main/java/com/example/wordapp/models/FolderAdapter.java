package com.example.wordapp.models;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordapp.R;
import com.example.wordapp.activities.AddWord;

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
            // Thêm nhanh từ vào folder, ví dụ mở dialog nhập từ
            Toast.makeText(context, "Thêm từ vào " + folder.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView txtFolderName;
        ImageButton btnAddWord;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFolderName = itemView.findViewById(R.id.txtFolderName);
            btnAddWord = itemView.findViewById(R.id.btnAddWord);
        }
    }
}
