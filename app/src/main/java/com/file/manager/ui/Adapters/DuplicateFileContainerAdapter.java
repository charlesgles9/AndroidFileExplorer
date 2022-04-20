package com.file.manager.ui.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.DuplicateFileContainer;
import com.file.manager.ui.Models.DuplicateFileModel;
import com.file.manager.ui.Models.LocalThumbnail;
import com.file.manager.utils.DateUtils;
import com.file.manager.utils.ThumbnailLoader;

import java.util.List;


public class DuplicateFileContainerAdapter extends RecyclerView.Adapter<DuplicateFileContainerAdapter.DuplicateFileViewHolder> {


    private DuplicateFileContainer container;
    private LayoutInflater inflater;
    private Context context;
    private OnItemClickListener onItemClickListener;
    public DuplicateFileContainerAdapter(Context context, DuplicateFileContainer container){
      this.inflater=LayoutInflater.from(context);
      this.container=container;
      this.context=context;
    }


    @Override
    public void onBindViewHolder(@NonNull DuplicateFileViewHolder holder, int position) {
        final DuplicateFileModel model=container.getArray().get(position);
        final CustomFile file=model.getArray().get(0);
        holder.name.setText(file.getName());
        holder.path.setText(file.getPath());
        holder.count.setText(model.size()+" Items");
        holder.date.setText(DateUtils.getDateString(file.lastModified()));
        file.getLocalThumbnail().setThumbnailToImageView(holder.thumbnail);
        holder.initDropdownItem();
        holder.dropDownList.setVisibility(model.isDisplayItems()?View.VISIBLE:View.GONE);
    }

    @NonNull
    @Override
    public DuplicateFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.duplicate_files_dropdown_layout,parent,false);
        return new DuplicateFileViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return container.getArray().size();
    }
    public void LoadThumbnail(final int start, final int stop){
       LoadThumbnails(start, stop, new ThumbnailLoader.onThumbnailComplete() {
           @Override
           public void onComplete(List<Integer> positions) {
               for(int i=0;i<positions.size();i++) {
                   notifyItemChanged(positions.get(i));
               }
           }
       });
    }

    private void LoadThumbnails(int start,int stop,ThumbnailLoader.onThumbnailComplete complete){
        if(stop>=container.getArray().size()|start==-1)
            return;

        for(int i=start;i<=stop;i++) {
            CustomFile file=container.getArray().get(i).getArray().get(0);
            file.getLocalThumbnail().setAdapterPosition(i);
            if (!file.getLocalThumbnail().isLoaded()) {
                ThumbnailLoader<LocalThumbnail> loader = new ThumbnailLoader<>(file.getLocalThumbnail(), context);
                loader.setHeight(64);
                loader.setWidth(64);
                loader.setOnThumbnailLoadedListener(complete);
                loader.ExecuteTask();
            }
        }

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class DuplicateFileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

       final TextView name;
       final TextView path;
       final TextView count;
       final TextView date;
       final ImageView thumbnail;
       final ToggleButton dropDownToggle;
       final RecyclerView dropDownList;
       final LinearLayoutManager manager;
       final View view;
        DuplicateFileItemAdapter adapter;
        public DuplicateFileViewHolder(View view){
            super(view);
            this.view=view;
            name=view.findViewById(R.id.name);
            path=view.findViewById(R.id.path);
            count=view.findViewById(R.id.title_count);
            date=view.findViewById(R.id.date);
            thumbnail=view.findViewById(R.id.thumbnail);
            dropDownToggle=view.findViewById(R.id.dropdownToggle);
            dropDownList=view.findViewById(R.id.dropDownList);
            dropDownList.setItemAnimator(null);
            view.setOnClickListener(this);
            manager= new LinearLayoutManager(context);
            manager.setOrientation(RecyclerView.VERTICAL);
        }

        private void initDropdownItem(){
            final int[] p ={getAdapterPosition()};
            adapter= new DuplicateFileItemAdapter(context,container.getArray().get(p[0]));
            adapter.setOnItemClickListener(onItemClickListener);
            dropDownList.setLayoutManager(manager);
            dropDownList.setAdapter(adapter);
        }

        private void toggleViewVisibility(View view){
            DuplicateFileModel model=container.getArray().get(getAdapterPosition());
            model.setDisplayItems(!model.isDisplayItems());
            view.setVisibility(model.isDisplayItems()?View.VISIBLE:View.GONE);
            if(model.isDisplayItems()){
                adapter.LoadThumbnails();
            }

        }

        @Override
        public void onClick(View v) {
            toggleViewVisibility(dropDownList);

        }
    }

    public interface OnItemClickListener{
        void onClick(View view,int position);
    }
}
