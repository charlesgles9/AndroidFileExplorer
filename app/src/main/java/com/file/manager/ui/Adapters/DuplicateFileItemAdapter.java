package com.file.manager.ui.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.DuplicateFileModel;
import com.file.manager.ui.Models.LocalThumbnail;
import com.file.manager.ui.utils.DateUtils;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.ThumbnailLoader;

import java.util.ArrayList;
import java.util.List;

public class DuplicateFileItemAdapter extends RecyclerView.Adapter<DuplicateFileItemAdapter.DuplicateFileViewHolder> {

    private ArrayList<CustomFile>array;
    private LayoutInflater inflater;
    private Context context;
    private DuplicateFileContainerAdapter.OnItemClickListener onItemClickListener;
    public DuplicateFileItemAdapter(Context context, DuplicateFileModel model){
        this.inflater=LayoutInflater.from(context);
        this.array=model.getArray();
        this.context=context;
    }

    @NonNull
    @Override
    public DuplicateFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.duplicate_file_item,parent,false);

        return new DuplicateFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DuplicateFileViewHolder holder, int position) {
        final CustomFile file=array.get(position);
         holder.name.setText(file.getName());
         holder.path.setText(file.getPath());
         holder.size.setText(DiskUtils.getInstance().getSize(file));
         holder.date.setText(DateUtils.getDateStringHHMMSS(file.lastModified()));
         holder.selected.setChecked(file.IsSelected());
         file.getLocalThumbnail().setThumbnailToImageView(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return array.size();
    }


    public void LoadThumbnails(){

        ThumbnailLoader<ArrayList<CustomFile>> loader = new ThumbnailLoader<>(array, context);
                loader.setOnThumbnailLoadedListener(new ThumbnailLoader.onThumbnailComplete() {
                    @Override
                    public void onComplete(List<Integer>positions) {
                        for(int i=0;i<positions.size();i++)
                        notifyItemChanged(positions.get(i));
                    }
                });
                loader.ExecuteTask();

    }

    public void setOnItemClickListener(DuplicateFileContainerAdapter.OnItemClickListener  onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class DuplicateFileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView path;
        TextView size;
        TextView date;
        ToggleButton selected;
        ImageView thumbnail;
        public DuplicateFileViewHolder(View view){
            super(view);
            name=view.findViewById(R.id.name);
            path=view.findViewById(R.id.path);
            size=view.findViewById(R.id.size);
            date=view.findViewById(R.id.date);
            selected=view.findViewById(R.id.selected);
            selected.setOnClickListener(this);
            thumbnail=view.findViewById(R.id.thumbnail);

        }

        @Override
        public void onClick(View v) {
            CustomFile file=array.get(getAdapterPosition());
            file.setSelected(selected.isChecked());
            onItemClickListener.onClick(selected,getAdapterPosition());
        }
    }

}
