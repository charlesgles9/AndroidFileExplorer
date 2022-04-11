package com.file.manager.ui.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.Operations;
import com.file.manager.R;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.Folder;
import com.file.manager.ui.Models.FolderSizeModel;
import com.file.manager.FilterType;
import com.file.manager.utils.DateUtils;
import com.file.manager.utils.DiskUtils;
import com.file.manager.utils.FileFilters;
import com.file.manager.utils.ThumbnailLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.FileViewHolder> {


    private Folder folder;
    private LayoutInflater inflater;
    private Context context;
    private ItemListener itemListener;
    private Operations operations=Operations.NAVIGATE;
    private SharedPreferences preferences;
    private Size display;
    public StorageAdapter(Folder folder, Context context){
     this.folder=folder;
     this.context=context;
     this.inflater=LayoutInflater.from(context);
     this.preferences=context.getSharedPreferences("MyPref",Context.MODE_PRIVATE);
     this.display=new Size(context.getResources().getDisplayMetrics().widthPixels,
                context.getResources().getDisplayMetrics().heightPixels);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {

        if(holder instanceof ListViewHolder){
            ((ListViewHolder)holder).init(folder.get(position));
        }else {
            ((GridViewHolder)holder).init(folder.get(position));
        }

    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        String prefStr=preferences.getString(folder.getType().toString(),"LIST");
        if(!(prefStr.equals("LIST"))&
                !folder.getType().equals(FilterType.FOLDERS)) {
             view=inflater.inflate(R.layout.storage_grid_folder_layout,parent,false);
            return new GridViewHolder(view);
        }else
             view = inflater.inflate(R.layout.storage_linear_file_layout, parent, false);

        return new ListViewHolder(view);
    }


    @Override
    public void onViewAttachedToWindow(@NonNull FileViewHolder holder) {
        super.onViewAttachedToWindow(holder);

    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

    }

    // load within range
    public void LoadThumbnails(int start, int stop, ThumbnailLoader.onThumbnailComplete onThumbnailComplete){
        if(start==-1)
            return;
        String prefStr=preferences.getString(folder.getType().toString(),"LIST");
        String ratio= PreferenceManager.getDefaultSharedPreferences(context).getString("thumbRatio","64x64");
        String []arr=ratio.split("x");
        int rw=64;
        int rh=64;
        try {
            rw = Integer.parseInt(arr[0]);
            rh = Integer.parseInt(arr[1]);
        }catch (NumberFormatException ignore){}
        for(int i=start;i<=stop;i++) {
            if(folder.getFiles().get(i).isDirectory()&prefStr.equals("LIST"))
                continue;
            folder.loadThumbnails(i, i, rw, rh, folder.getFiles(),onThumbnailComplete);
        }
    }


    public String getPath(){
        return folder.getPath();
    }

    public void search(String name,Folder.onSearchComplete onSearchComplete){
        folder.searchFile(name,onSearchComplete);

    }
    @Override
    public int getItemCount() {
        return folder.size();
    }

    public void setItemListener(ItemListener listener){
        this.itemListener=listener;
    }

    public CustomFile get(int position){

        return folder.get(position);
    }
    public void setFolder(Folder folder){
        this.folder=folder;

    }

    public void setOperations(Operations operations){
        this.operations=operations;

    }

    @SuppressLint("StaticFieldLeak")
    public void initFolderSize(final int start, final int stop){
        // if its a folder get the folder size
        new AsyncTask<String,Integer,String>() {

            @Override
            protected String doInBackground(String... strings) {
                for(int i = start;i<=stop;i++) {
                    CustomFile file = folder.get(i);
                    if (file.isDirectory()) {
                        switch (folder.getType()) {
                            case IMAGE:
                                file.initFolderSize(FileFilters.ImagesOnly());
                                break;
                            case AUDIO:
                                file.initFolderSize(FileFilters.AudioOnly());
                                break;
                            case VIDEO:
                                file.initFolderSize(FileFilters.VideosOnly());
                                break;
                            case DOCUMENT:
                                file.initFolderSize(FileFilters.DocumentsOnly());
                                break;
                            case COMPRESSED:
                                file.initFolderSize(FileFilters.CompressedOnly());
                                break;
                            case APPLICATION:
                                file.initFolderSize(FileFilters.ApkOnly());
                                break;
                            default:
                                file.initFolderSize(FileFilters.Default(true));
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                for(int i=start;i<=stop;i++){
                    notifyItemChanged(i);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public class ListViewHolder extends FileViewHolder{

        private TextView name;
        private AppCompatImageView thumbnail;
        private TextView size;
        private TextView date;
        private ToggleButton selected;
        private RelativeLayout layout;
        public ListViewHolder(View view) {
            super(view);
            name=view.findViewById(R.id.name);
            thumbnail=view.findViewById(R.id.thumbnail);
            size=view.findViewById(R.id.file_size);
            date=view.findViewById(R.id.date);
            layout=view.findViewById(R.id.layout);
            selected=view.findViewById(R.id.selected);
            selected.setVisibility(View.INVISIBLE);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @SuppressLint({"SetTextI18n", "StaticFieldLeak"})
        public void init(final CustomFile file){
            name.setText(file.getName());

            file.getLocalThumbnail().setThumbnailToImageView(thumbnail);
            // highlight searched files in the list
            if(file.highlighted)
                layout.setBackgroundResource(R.color.searchHighlight);
            else layout.setBackgroundResource(0);

            // toggle select view
            selected.setVisibility(operations.equals(Operations.SELECT)? View.VISIBLE:View.INVISIBLE);
            if(file.isDirectory()){
                size.setText(""+file.getFolderLength()+" items");

            }

            else
                size.setText(DiskUtils.getInstance().getSize(file));
            date.setText(DateUtils.getDateString(file.lastModified()));
            selected.setChecked(file.IsSelected());
        }


    }

    public class GridViewHolder extends FileViewHolder{

        private TextView name;
        private ImageView thumbnail;
        private ToggleButton selected;
        private RelativeLayout layout;
        private TextView size;
        public GridViewHolder(View view) {
            super(view);
            name=view.findViewById(R.id.name);
            thumbnail=view.findViewById(R.id.thumbnail);
            layout=view.findViewById(R.id.layout);
            size=view.findViewById(R.id.size);
            selected=view.findViewById(R.id.selected);
            selected.setVisibility(View.INVISIBLE);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(display.getWidth()/3,display.getHeight()/5);
            thumbnail.setLayoutParams(params);
        }

        @SuppressLint("SetTextI18n")
        public void init(CustomFile file){
            name.setText(file.getName());
            file.getLocalThumbnail().setThumbnailToImageView(thumbnail);
            // highlight searched files in the list
            if(file.highlighted)
                layout.setBackgroundResource(R.color.searchHighlight);
            else
                layout.setBackgroundResource(0);

            // toggle select view
            selected.setVisibility(operations.equals(Operations.SELECT)? View.VISIBLE:View.INVISIBLE);
            if(file.isDirectory()){
                name.setVisibility(View.VISIBLE);
                size.setVisibility(View.VISIBLE);
                size.setText("("+file.getFolderLength()+" items)");
            } else {
                // don't show the file name in the video and image gallery if it's not a directory
                if(!folder.getType().equals(FilterType.VIDEO)&
                   !folder.getType().equals(FilterType.IMAGE)){
                    name.setVisibility(View.VISIBLE);
                    size.setVisibility(View.VISIBLE);
                    size.setText(DiskUtils.getInstance().getSize(file));
                }else {
                    name.setVisibility(View.GONE);
                    size.setVisibility(View.GONE);
                }

            }

            selected.setChecked(file.IsSelected());

        }
    }

    public class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public FileViewHolder(View view){
            super(view);
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

            return true;
        }
    }


    public interface ItemListener{
         void onItemClick(int position);
         void onItemLongClick(int position);
    }
}
