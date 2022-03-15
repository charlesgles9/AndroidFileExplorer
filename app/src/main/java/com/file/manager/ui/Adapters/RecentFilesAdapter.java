package com.file.manager.ui.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.MainActivity;
import com.file.manager.R;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.RecentFileModel;
import com.file.manager.ui.home.RecentFilesContainer;
import com.file.manager.ui.utils.DateUtils;
import com.file.manager.ui.utils.FileFilters;
import com.file.manager.ui.utils.ThumbnailLoader;

import java.util.ArrayList;
import java.util.List;

public class RecentFilesAdapter extends RecyclerView.Adapter<RecentFilesAdapter.RecentViewHolder> {


   private LayoutInflater inflater;
   private Context context;
   private RecentFilesContainer container;
   private ArrayList<RecentFileModel>array;
   private OnItemClickListener onItemClickListener;
   private MutableLiveData<List<Integer>>updates=new MutableLiveData<>();
    public RecentFilesAdapter(Context context,RecentFilesContainer container){
     this.inflater=LayoutInflater.from(context);
     this.context=context;
     this.container=container;
     this.array=container.getArray();
    }

    @NonNull
    @Override
    public RecentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = inflater.inflate(R.layout.recent_item_layout, parent,false);
        return new RecentViewHolder(view);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void LoadThumbnail(Context context,int start,int stop,ThumbnailLoader.onThumbnailComplete onThumbnailComplete){
        ThumbnailLoader<RecentFilesContainer> thumbnailLoader= new ThumbnailLoader<>(container,context);
        thumbnailLoader.setHeight(32);
        thumbnailLoader.setWidth(32);
        thumbnailLoader.setPoints(start,stop);
        thumbnailLoader.setOnThumbnailLoadedListener(onThumbnailComplete);
        thumbnailLoader.ExecuteTask();

    }

    public MutableLiveData<List<Integer>> getUpdates() {
        return updates;
    }

    @Override
    public void onBindViewHolder(@NonNull RecentViewHolder holder, int position) {
        final RecentFileModel model=array.get(position);
        final CustomFile file=model.get(0);
        final CustomFile parent=model.getParent();
        holder.name.setText(parent.getName());
        holder.date.setText(DateUtils.getDateString(file.lastModified()));
        parent.getLocalThumbnail().setThumbnailToImageView(holder.icon);
        holder.setFileToImageView(file, holder.thumb1, holder.v1);
         if(model.size()>=2){
             holder.setFileToImageView(model.get(1), holder.thumb2, holder.v2);
        }
         if(model.size()>=3){
             holder.setFileToImageView(model.get(2), holder.thumb3, holder.v3);
         }
         if(model.size()>=4){
             holder.setFileToImageView(model.get(3), holder.thumb4, holder.v4);
         }

    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    public void update(){
        array=container.getArray();
    }

    class RecentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView name;
        final TextView date;
        final ImageView icon;
        final ImageView thumb1;
        final ImageView v1;
        final ImageView thumb2;
        final ImageView v2;
        final ImageView thumb3;
        final ImageView v3;
        final ImageView thumb4;
        final ImageView v4;
        final RelativeLayout goTo;
        public RecentViewHolder(View view){
            super(view);
             name = view.findViewById(R.id.name);
             date=view.findViewById(R.id.date);
             icon=view.findViewById(R.id.icon);
             thumb1=view.findViewById(R.id.thumb1);
             v1=view.findViewById(R.id.v1);
             thumb2=view.findViewById(R.id.thumb2);
             v2=view.findViewById(R.id.v2);
             thumb3=view.findViewById(R.id.thumb3);
             v3=view.findViewById(R.id.v3);
             thumb4=view.findViewById(R.id.thumb4);
             v4=view.findViewById(R.id.v4);
             goTo=view.findViewById(R.id.goTo);
             goTo.setOnClickListener(this);
             thumb1.setOnClickListener(this);
             thumb2.setOnClickListener(this);
             thumb3.setOnClickListener(this);
             thumb4.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final RecentFileModel model=array.get(getAdapterPosition());
            switch (v.getId()) {
                case R.id.thumb1:
                    onItemClickListener.openImage(model.get(0));
                    break;
                case R.id.thumb2:
                    onItemClickListener.openImage(model.get(1));
                    break;
                case R.id.thumb3:
                    onItemClickListener.openImage(model.get(2));
                    break;
                case R.id.thumb4:
                    onItemClickListener.openImage(model.get(3));
                    break;
                case R.id.goTo:
                    onItemClickListener.openFolder(model.getParent());
                    break;
            }
        }

        public void setFileToImageView(CustomFile file, ImageView imageView1, ImageView imageView2){
            if(!file.getLocalThumbnail().isLoaded()) {
                file.getLocalThumbnail().setAdapterPosition(getAdapterPosition());
            }
            file.getLocalThumbnail().setThumbnailToImageView(imageView1);
            imageView1.setVisibility(View.VISIBLE);
            if(FileFilters.isVideo(file.getName())){
                imageView2.setVisibility(View.VISIBLE);
            }else {
                imageView2.setVisibility(View.INVISIBLE);
            }
        }
    }

    public interface OnItemClickListener{
        void openImage(CustomFile file);
        void openFolder(CustomFile file);
    }
}
