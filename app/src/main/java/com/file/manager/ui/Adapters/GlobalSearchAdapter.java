package com.file.manager.ui.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.Operations;
import com.file.manager.R;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.utils.DiskUtils;
import com.file.manager.utils.ThumbnailLoader;

import java.util.ArrayList;
import java.util.List;

public class GlobalSearchAdapter extends RecyclerView.Adapter<GlobalSearchAdapter.SearchViewHolder> {

    private ArrayList<CustomFile>files;
    private LayoutInflater inflater;
    private Context context;
    private ItemListener itemListener;
    private Operations operations=Operations.NAVIGATE;
    private MutableLiveData<List<Integer>>updates;
    public GlobalSearchAdapter(Context context, ArrayList<CustomFile>files){
        this.files=files;
        this.inflater=LayoutInflater.from(context);
        this.context=context;
        this.updates=new MutableLiveData<>();

    }
    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.storage_linear_file_layout,parent,false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
       final CustomFile file=files.get(position);
       holder.name.setText(file.getName());
       file.getLocalThumbnail().setThumbnailToImageView(holder.thumbnail);
       if(file.isFile())
           holder.size.setText(DiskUtils.getInstance().getSize(file));

       else holder.size.setVisibility(View.INVISIBLE);
       holder.selected.setVisibility(operations.equals(Operations.SELECT)?View.VISIBLE:View.INVISIBLE);
       holder.selected.setChecked(file.IsSelected());
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public ArrayList<CustomFile> getFiles() {
        return files;
    }

    boolean selectAll =false;
    public void toggleSelectAll(){
        for(CustomFile file:files){
            file.setSelected(!selectAll);
        }
        selectAll =!selectAll;
    }

    public void toggleSelect(int position){
        final CustomFile file=files.get(position);
        file.setSelected(!file.IsSelected());
    }

    public void resetSelect(){
        for(CustomFile file:files){
            file.setSelected(false);
        }
    }

    public void remove(CustomFile file){
        files.remove(file);
    }

    public void replace(CustomFile oldF,CustomFile newF){
            files.set(oldF.position,newF);
            newF.position=oldF.position;
    }
    // to minimize memory leaks load thumbnails which are currently seen
    public void loadThumbnails(int start,int stop,ThumbnailLoader.onThumbnailComplete onThumbnailComplete){
        if(start==-1|start>=files.size()|stop>=files.size()|files.isEmpty())
            return;
          ThumbnailLoader<ArrayList<CustomFile>> loader = new ThumbnailLoader<>(files, context);
          loader.setPoints(start,stop);
          loader.setOnThumbnailLoadedListener(onThumbnailComplete);
          loader.setWidth(50);
          loader.setHeight(50);
          loader.ExecuteTask();

    }

    public void setOperations(Operations operations){
        this.operations=operations;
    }

    public Operations getOperations() {
        return operations;
    }

    public void setItemListener(ItemListener itemListener){
        this.itemListener=itemListener;
    }
    class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        final TextView name;
        final ImageView thumbnail;
        final TextView size;
        final RelativeLayout layout;
        private ToggleButton selected;
        public SearchViewHolder(View view){
            super(view);
            name=view.findViewById(R.id.name);
            thumbnail=view.findViewById(R.id.thumbnail);
            size=view.findViewById(R.id.file_size);
            layout=view.findViewById(R.id.layout);
            selected=view.findViewById(R.id.selected);
            layout.setOnLongClickListener(this);
            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(itemListener!=null)
                itemListener.onItemClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if(itemListener!=null)
                itemListener.onItemLongClick(getAdapterPosition());
            return false;
        }
    }

    public LiveData<List<Integer>> getUpdates() {
        return updates;
    }

    public interface ItemListener{

        void onItemClick(int position);
        void onItemLongClick(int position);
    }
}
