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
import com.file.manager.utils.ArchiveDecompressUtil;
import com.file.manager.utils.DateUtils;
import com.file.manager.utils.DiskUtils;
import com.file.manager.utils.ThumbnailLoader;

import net.lingala.zip4j.model.FileHeader;
import java.util.List;

public class ZipEntryAdapter extends RecyclerView.Adapter<ZipEntryAdapter.ZipEntryViewHolder> {


    private ArchiveDecompressUtil archiveDecompressUtil;
    private LayoutInflater inflater;
    private OnItemClickListener onItemClickListener;
    public ZipEntryAdapter(Context context, ArchiveDecompressUtil archiveDecompressUtil){
        this.archiveDecompressUtil = archiveDecompressUtil;
        this.inflater=LayoutInflater.from(context);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }



    @Override
    public void onBindViewHolder(@NonNull ZipEntryViewHolder holder, int position) {
        List<FileHeader> headers= archiveDecompressUtil.getCurrentDirList();
        FileHeader header=headers.get(position);
        holder.name.setText(ArchiveDecompressUtil.getHeaderName(header));
        holder.date.setText(DateUtils.getDateString(header.getLastModifiedTime()));
        ThumbnailLoader.setThumbnailToZipEntry(header,holder.thumbnail);
        if(!header.isDirectory())
        holder.size.setText(DiskUtils.getInstance().getSize(header.getUncompressedSize()));
        else holder.size.setText("");

    }

    @NonNull
    @Override
    public ZipEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= inflater.inflate(R.layout.storage_linear_file_layout,parent,false);
        return new ZipEntryViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return archiveDecompressUtil.getCurrentDirList().size();
    }

    class ZipEntryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView name;
        private ImageView thumbnail;
        private TextView size;
        private TextView date;

        public ZipEntryViewHolder(View view){
            super(view);
            name=view.findViewById(R.id.name);
            thumbnail=view.findViewById(R.id.thumbnail);
            size=view.findViewById(R.id.file_size);
            date=view.findViewById(R.id.date);
            ToggleButton selected = view.findViewById(R.id.selected);
            selected.setVisibility(View.INVISIBLE);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            onItemClickListener.onClick(archiveDecompressUtil.getCurrentDirList().get(getAdapterPosition()));

        }
    }
    public interface  OnItemClickListener{
        void onClick(FileHeader fileHeader);
    }
}
