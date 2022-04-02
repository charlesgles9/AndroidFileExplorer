package com.file.manager.ui.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Models.BatchRenameModel;
import com.file.manager.ui.utils.DateUtils;

import java.util.ArrayList;

public class BatchRenameAdapter extends RecyclerView.Adapter<BatchRenameAdapter.BatchViewHolder> {


    private ArrayList<BatchRenameModel> list;
    private LayoutInflater inflater;
    public BatchRenameAdapter(Context context,ArrayList<BatchRenameModel>list){
        this.inflater=LayoutInflater.from(context);
        this.list=list;

    }
    @NonNull
    @Override
    public BatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= inflater.inflate(R.layout.storage_linear_file_layout,parent,false);

        return new BatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BatchViewHolder holder, int position) {
        BatchRenameModel model=list.get(position);
        model.getFile().getLocalThumbnail().setThumbnailToImageView(holder.thumbnail);
        holder.date.setText(DateUtils.getDateString(model.getFile().lastModified()));
        holder.name.setText(model.getRename());
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    static class BatchViewHolder extends RecyclerView.ViewHolder{

        ImageView thumbnail;
        TextView name;
        TextView date;
        public BatchViewHolder(View view){
            super(view);
            thumbnail=view.findViewById(R.id.thumbnail);
            name=view.findViewById(R.id.name);
            date=view.findViewById(R.id.date);

        }
    }
}
