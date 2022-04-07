package com.file.manager.ui.Adapters;

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
import com.file.manager.ui.utils.DateUtils;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.FileFilters;
import com.file.manager.ui.utils.ThumbnailLoader;

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


    public void initFolderSize(){
        new sizeTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    // load within range
    public void LoadThumbnails(int start, int stop, ThumbnailLoader.onThumbnailComplete onThumbnailComplete){
        String ratio= PreferenceManager.getDefaultSharedPreferences(context).getString("thumbRatio","64x64");
        String []arr=ratio.split("x");
        int rw=64;
        int rh=64;
        try {
            rw = Integer.parseInt(arr[0]);
            rh = Integer.parseInt(arr[1]);
        }catch (NumberFormatException ignore){}
        for(int i=start;i<=stop;i++) {
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

        public void init(CustomFile file){
            name.setText(file.getName());

            file.getLocalThumbnail().setThumbnailToImageView(thumbnail);
            // highlight searched files in the list
            if(file.highlighted)
                layout.setBackgroundResource(R.color.searchHighlight);
            else layout.setBackgroundResource(0);

            // toggle select view
            selected.setVisibility(operations.equals(Operations.SELECT)? View.VISIBLE:View.INVISIBLE);
            if(file.isDirectory()){
                size.setText(file.getFolderSizeModel().getLength()+" items");
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
                size.setText("("+file.getFolderSizeModel().getLength()+" items)");
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
            String ratio= PreferenceManager.getDefaultSharedPreferences(context).getString("thumbRatio","64x64");
            String []arr=ratio.split("x");

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
    class sizeTask extends AsyncTask<String,Integer,String>{


        @Override
        protected String doInBackground(String... strings) {
            List<Integer>positions= new ArrayList<>();
            for(int i=0;i<folder.getFiles().size();i++) {
                CustomFile file=folder.getFiles().get(i);
                if(!file.isDirectory())
                    continue;
            switch (folder.getType()){

                case IMAGE:
                    file.getFolderSizeModel().setFilter(FileFilters.ImagesOnly());
                    break;
                case AUDIO:
                    file.getFolderSizeModel().setFilter(FileFilters.AudioOnly());
                    break;
                case VIDEO:
                    file.getFolderSizeModel().setFilter(FileFilters.VideosOnly());
                    break;
                case DOCUMENT:
                    file.getFolderSizeModel().setFilter(FileFilters.DocumentsOnly());
                    break;
                case COMPRESSED:
                    file.getFolderSizeModel().setFilter(FileFilters.CompressedOnly());
                    break;
                case APPLICATION:
                    file.getFolderSizeModel().setFilter(FileFilters.ApkOnly());
                    break;
                default:
                    file.getFolderSizeModel().setFilter(FileFilters.Default(true));
            }
               FolderSizeModel model=file.getFolderSizeModel();
               model.initialize();
               positions.add(i);
             }
            return null;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }


    public interface ItemListener{

         void onItemClick(int position);
         void onItemLongClick(int position);
    }
}
