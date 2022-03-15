package com.file.manager.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.PropertyModel;
import com.file.manager.ui.utils.DateUtils;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.FileFilters;
import com.file.manager.ui.utils.FileHandleUtil;
import com.file.manager.ui.utils.MD5;
import com.file.manager.ui.utils.ThumbnailLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PropertiesDialog extends Dialog implements View.OnClickListener {

   private ArrayList<CustomFile>files;
   private ImageView icon;
    public PropertiesDialog(Context context,ArrayList<CustomFile>files){
        super(context);
        this.files=files;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.properties_layout);
        final RecyclerView recyclerView=findViewById(R.id.list);
        icon=findViewById(R.id.icon);
        final LinearLayoutManager layoutManager= new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new PropertyAdapter(getContext()));
        findViewById(R.id.okay).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        cancel();
    }



    private void copyTextToClipBoard(String text){
        ClipboardManager clipboardManager=(ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData=ClipData.newPlainText("Text Copied",text);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(getContext(),"Text Copied!",Toast.LENGTH_SHORT).show();
    }

    class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder>{
        private ArrayList<PropertyModel>list= new ArrayList<>();
        private LayoutInflater inflater;
        @SuppressLint("StaticFieldLeak")
        public PropertyAdapter(Context context){
            final DiskUtils diskUtils=DiskUtils.getInstance();
            if (files.size()==1) {
                for (final CustomFile file : files) {
                    list.add(new PropertyModel("name", file.getName()));
                    list.add(new PropertyModel("path", file.getPath()));
                    list.add(new PropertyModel("date", DateUtils.getDateString(file.lastModified())));
                    if(file.isFile()) {
                        String extension=file.getExtension();
                       if(extension!=null&&extension.lastIndexOf(".")!=-1) {
                           list.add(new PropertyModel("mimeType", MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1))));
                           list.add(new PropertyModel("extension",extension.substring(1)));
                       }
                        list.add(new PropertyModel("size", diskUtils.getSize(file) + "\n" + file.length() + "bytes"));
                        final PropertyModel MD5Hash=new PropertyModel("MD5 Checksum","calculating...");
                        list.add(MD5Hash);
                        new AsyncTask<String,Integer,String>(){
                            @Override
                            protected String doInBackground(String... strings) {
                                MD5Hash.setDetails(MD5.fileToMD5(file.getPath()));
                                return null;
                            }

                            @Override
                            protected void onPostExecute(String s) {
                                if(isShowing())
                                notifyDataSetChanged();
                                super.onPostExecute(s);
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }else {
                        final PropertyModel size=new PropertyModel("size","calculating...");
                        list.add(size);

                        new AsyncTask<String,Integer,String>(){

                            @Override
                            protected String doInBackground(String... strings) {
                                size.setDetails(DiskUtils.getInstance().getFolderSize(file));
                                return null;
                            }

                            @Override
                            protected void onPostExecute(String s) {
                                notifyDataSetChanged();
                                super.onPostExecute(s);
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    list.add(new PropertyModel("permission", !file.canWrite() ? "-r" : "-rw"));
                }
            }else if(files.size()>1){
                final PropertyModel size=new PropertyModel("size","calculating...");
                final PropertyModel content=new PropertyModel("content","calculating...");
                list.add(size);
                list.add(content);
                new AsyncTask<String,Integer,String>(){
                    int folderCount=0,fileCount=0;
                    long bytes=0L;

                    @Override
                    protected String doInBackground(String... strings) {
                        for(int i=0;i<files.size();i++){
                            CustomFile file=files.get(i);
                            if(file.isDirectory()){
                                ArrayList<CustomFile>dirsList= new ArrayList<>();
                                ArrayList<CustomFile>filesList= new ArrayList<>();
                                FileHandleUtil.ListFilesRecursively(file,filesList,dirsList, FileFilters.Default(true));
                                for(CustomFile child:filesList){
                                    bytes+=child.length();
                                }
                                folderCount+=dirsList.size();
                                fileCount+=filesList.size();
                                folderCount++;
                            }else {
                                bytes+=file.length();
                                fileCount++;
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        size.setDetails(DiskUtils.getInstance().getSize(bytes));
                        content.setDetails("file"+fileCount+",folder"+folderCount);
                        notifyDataSetChanged();
                        super.onPostExecute(s);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                 // display the selected file list
                for (CustomFile file:files){
                    list.add(new PropertyModel("",file.getName()+(file.isDirectory()?"(folder)":"(file)")+"\n"));
                }
            }

            final CustomFile file=files.get(0);
            if(file.isFile()){
                if(FileFilters.isImage(file.getName())|
                        FileFilters.isVideo(file.getName())){
                    ThumbnailLoader<CustomFile>loader=new ThumbnailLoader(file.getLocalThumbnail(),getContext());
                    DisplayMetrics metrics=getContext().getResources().getDisplayMetrics();
                    loader.setWidth(metrics.widthPixels/2);
                    loader.setHeight((int)(200*1.5f));
                    loader.setOnThumbnailLoadedListener(new ThumbnailLoader.onThumbnailComplete() {
                        @Override
                        public void onComplete(List<Integer> positions) {
                            icon.setVisibility(View.VISIBLE);
                            file.getLocalThumbnail().setThumbnailToImageView(icon);
                        }
                    });
                    loader.ExecuteTask();
                }
            }

            inflater=LayoutInflater.from(context);
        }
        @NonNull
        @Override
        public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view=inflater.inflate(R.layout.file_properties_details,parent,false);
            return new PropertyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
            final PropertyModel model=list.get(position);
            holder.title.setText(model.getTitle());
            holder.details.setText(model.getDetails());

        }

        @Override
        public int getItemCount() {
            return list.size();
        }
        class PropertyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
             TextView title;
             TextView details;
            public PropertyViewHolder(@NonNull View itemView) {
                super(itemView);
                title=itemView.findViewById(R.id.title);
                details=itemView.findViewById(R.id.details);
                itemView.setOnLongClickListener(this);
            }

            @Override
            public boolean onLongClick(View v) {
                copyTextToClipBoard(details.getText().toString());
                return false;
            }
        }
    }


}
