package com.file.manager.utils;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.file.manager.R;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.LocalThumbnail;
import com.file.manager.ui.Models.RecentFileModel;
import com.file.manager.ui.Models.RecentFilesContainer;

import net.lingala.zip4j.model.FileHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;;

public class ThumbnailLoader <T> extends AsyncTask<String,Integer,String> {


    private T localThumbnail;
    private  int HEIGHT = 128;
    private  int WIDTH = 128;
    private boolean defaultOnly;
    private Context _context;
    private onThumbnailComplete onThumbnailComplete;
    private boolean running;
    private List<Integer>positions= new ArrayList<>();
    private Point points= new Point(-1,-1);
    public ThumbnailLoader(T localThumbnail, Context _context) {
        this.localThumbnail = localThumbnail;
        this._context = _context;
    }

    private Bitmap getThumbnailFromImage(CustomFile file) {
        Bitmap bitmap = null;
        try {
            bitmap = Glide.with(_context)
                    .asBitmap()
                    .load(file)
                    .submit(WIDTH, HEIGHT).get();
            bitmap = Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, false);
        } catch (ExecutionException | InterruptedException ignored) {
        }

        return bitmap;
    }


    private Bitmap getVideoThumbnail(CustomFile file) {
        Bitmap bitmap = null;
       try {
            bitmap = Glide.with(_context)
                    .asBitmap().timeout(2000)
                    .load(file)
                    .submit(WIDTH, HEIGHT).get();
            bitmap = Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, false);
        } catch (ExecutionException | InterruptedException  ee) {}

        return bitmap;
    }


    private Drawable getThumbnailFromApk(final CustomFile file) {
        Drawable icon = null;
        try {
            PackageManager pm = _context.getPackageManager();
            PackageInfo pi = pm.getPackageArchiveInfo(file.getPath(), 0);
            pi.applicationInfo.sourceDir = file.getPath();
            pi.applicationInfo.publicSourceDir = file.getPath();
            icon = pi.applicationInfo.loadIcon(pm);
        } catch (NullPointerException ne) {
            ne.printStackTrace();
        }

        return icon;

    }

    public void setOnThumbnailLoadedListener(onThumbnailComplete onThumbnailComplete){
       this.onThumbnailComplete=onThumbnailComplete;
    }

    @SuppressWarnings("NewApi")
    public void ExecuteTask() {
        this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected String doInBackground(String... strings) {
        Load();

        return null;
    }


    public void setPoints(int start, int stop) {
        this.points.set(start,stop);
    }

    public void setDefaultOnly(boolean defaultOnly) {
        this.defaultOnly = defaultOnly;
    }

    public static void setThumbnailToZipEntry(FileHeader header, ImageView imageView){
        String name= header.getFileName();
        String extension = FileFilters.getExtension(name);
        int padding=(int)(10*1.5f);
        if(!header.isDirectory()) {
            imageView.setBackgroundResource(0);
            imageView.setPadding(0,0,0,0);
        }else {
            imageView.setBackgroundResource(R.drawable.blue_rounded_drawable);
            imageView.setPadding(padding,padding,padding,padding);
        }
        if (extension != null&!header.isDirectory())
            switch (extension) {
                case ".png":
                case ".jpg":
                case ".svg":
                case ".webp":
                case ".jpeg":
                case ".bmp":
                    imageView.setImageResource(R.drawable.ic_image);

                    break;
                case ".mkv":
                case ".mp4":
                case ".vob":
                case ".3gp":
                    imageView.setImageResource( R.drawable.ic_video);
                    break;
                case ".html":
                case ".swf":
                case ".doc":
                case ".docx":
                case ".gif":
                case ".txt":
                    imageView.setImageResource( R.drawable.ic_document);
                    break;
                case ".pdf":
                    imageView.setImageResource( R.drawable.ic_pdf);
                    break;
                case ".mp3":
                case ".ogg":
                    imageView.setImageResource( R.drawable.ic_music);
                    break;
                case ".zip":
                    imageView.setImageResource( R.drawable.ic_zip);
                    break;
                case ".apk":
                    imageView.setImageResource( R.drawable.ic_android);
                    break;
                default:
                    // in case the extension is unsupported and the file is or not a directory
                    imageView.setImageResource( R.drawable.ic_document);


            }
        else {
            // in case the Extension returned null
            imageView.setImageResource(header.isDirectory() ? R.drawable.ic_folder_icon : R.drawable.ic_document);
        }
    }

    public static void setTemporaryThumbnail(LocalThumbnail localThumbnail){

        CustomFile file=localThumbnail.getFrom();
        String extension = file.getExtension().toLowerCase();
        if (extension != null&file.isFile())
            switch (extension) {
                case ".png":
                case ".jpg":
                case ".jpeg":
                case ".webp":
                case ".svg":
                case ".bmp":
                    localThumbnail.setThumbnail( R.drawable.ic_image);

                    break;
                case ".mkv":
                case ".mp4":
                case ".vob":
                case ".3gp":
                    localThumbnail.setThumbnail( R.drawable.ic_video);
                    break;
                case ".html":
                case ".swf":
                case ".doc":
                case ".docx":
                case ".gif":
                    localThumbnail.setThumbnail( R.drawable.ic_document);
                    break;
                case ".pdf":
                    localThumbnail.setThumbnail( R.drawable.ic_pdf);
                    break;
                case ".txt":
                    localThumbnail.setThumbnail( R.drawable.ic_document);
                    break;

                case ".mp3":
                case ".ogg":
                    localThumbnail.setThumbnail( R.drawable.ic_music);
                    break;
                case ".zip":
                    localThumbnail.setThumbnail( R.drawable.ic_zip);
                    break;
                case ".apk":
                    localThumbnail.setThumbnail( R.drawable.ic_android);
                    break;
                default:
                    // in case the extension is unsupported and the file is or not a directory
                    localThumbnail.setThumbnail(file.isDirectory() ? R.drawable.ic_folder_icon : R.drawable.ic_document);


            }
        else {
            // in case the Extension returned null
            localThumbnail.setThumbnail(file.isDirectory() ? R.drawable.ic_folder_icon : R.drawable.ic_document);
        }
    }


    private void Load(){

        if(localThumbnail instanceof LocalThumbnail){
            if(!defaultOnly) {
                Load((LocalThumbnail) localThumbnail);
                positions.add(((LocalThumbnail) localThumbnail).getAdapterPosition());
            } else
                setTemporaryThumbnail((LocalThumbnail)localThumbnail);


        }else if(localThumbnail instanceof ArrayList){
            ArrayList<CustomFile>list=(ArrayList<CustomFile>)localThumbnail;
            int start=points.x!=-1?points.x:0;
            int stop=points.y!=-1?points.y:list.size()-1;
            for(int i=start;i<=Math.min(stop,list.size()-1);i++) {
                if(!isRunning())
                    break;
                CustomFile file=list.get(i);
                if(file.getLocalThumbnail().isLoaded())
                    continue;
                if (!defaultOnly)
                    Load(file.getLocalThumbnail());
                else
                    setTemporaryThumbnail(file.getLocalThumbnail());
                file.getLocalThumbnail().setAdapterPosition(i);
                positions.add(i);
            }

        }else if(localThumbnail instanceof RecentFilesContainer){
            RecentFilesContainer container=(RecentFilesContainer)localThumbnail;
            int start=points.x!=-1?points.x:0;
            int stop=points.y!=-1?points.y:container.size()-1;
            for(int i=start;i<Math.min(stop,container.size()-1);i++){
                if(isCancelled())
                    break;
                RecentFileModel model=container.getArray().get(i);
                for(CustomFile file:model.getFiles()){
                  if(!file.getLocalThumbnail().isLoaded()) {
                      Load(file.getLocalThumbnail());
                      positions.add(i);
                  }
                }

            }

        }
    }


    public void setWidth(int WIDTH) {
        this.WIDTH = WIDTH;
    }

    public void setHeight(int HEIGHT) {
        this.HEIGHT = HEIGHT;
    }

    private void Load(LocalThumbnail localThumbnail){
            CustomFile file=localThumbnail.getFrom();
            String extension = file.getExtension().toLowerCase();
            localThumbnail.setLoaded(true);
            Bitmap bitmap;
            if (extension != null)
                switch (extension) {
                    case ".png":
                    case ".jpg":
                    case ".svg":
                    case ".webp":
                    case ".jpeg":
                    case ".bmp":
                        bitmap = getThumbnailFromImage(localThumbnail.getFrom());
                        localThumbnail.setThumbnail(file.isDirectory() ? R.drawable.ic_folder_icon : bitmap != null ? bitmap : R.drawable.ic_image);

                        break;
                    case ".mp4":
                    case ".3gp":
                    case ".mkv":
                    case ".vob":
                        bitmap = getVideoThumbnail(localThumbnail.getFrom());
                        localThumbnail.setThumbnail(file.isDirectory() ? R.drawable.ic_folder_icon : bitmap != null ? bitmap : R.drawable.ic_video);
                        break;
                    case ".html":
                    case ".swf":
                    case ".doc":
                    case ".docx":
                    case ".gif":
                        localThumbnail.setThumbnail(file.isDirectory() ? R.drawable.ic_folder_icon : R.drawable.ic_document);
                        break;
                    case ".pdf":
                        localThumbnail.setThumbnail(file.isDirectory() ? R.drawable.ic_folder_icon : R.drawable.ic_pdf);
                        break;
                    case ".txt":
                        localThumbnail.setThumbnail(file.isDirectory() ? R.drawable.ic_folder_icon : R.drawable.ic_document);
                        break;

                    case ".mp3":
                    case ".ogg":
                        localThumbnail.setThumbnail(file.isDirectory() ? R.drawable.ic_folder_icon : R.drawable.ic_music);
                        break;
                    case ".zip":
                        localThumbnail.setThumbnail(file.isDirectory() ? R.drawable.ic_folder_icon : R.drawable.ic_zip);
                        break;
                    case ".apk":
                        // if the drawable is null or its a folder with a .apk extension
                        Drawable drawable = getThumbnailFromApk(localThumbnail.getFrom());
                        localThumbnail.setThumbnail(!file.isDirectory() && drawable != null ? drawable : file.isDirectory() ? R.drawable.ic_folder_icon : R.drawable.ic_android);
                        break;
                    default:
                        // in case the extension is unsupported and the file is or not a directory
                        localThumbnail.setThumbnail(file.isDirectory() ? R.drawable.ic_folder_icon : R.drawable.ic_document);


                }
            else {
                // in case the Extension returned null
                localThumbnail.setThumbnail(file.isDirectory() ? R.drawable.ic_folder_icon : R.drawable.ic_document);
            }

    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        running=true;
    }

    @Override
    protected void onPostExecute(String s) {
        running=false;
        if(onThumbnailComplete!=null)
            if(localThumbnail instanceof LocalThumbnail)
        onThumbnailComplete.onComplete(positions);
              else onThumbnailComplete.onComplete(positions);

    }

    public void setRunning(boolean running){
        this.running=running;
    }

    public boolean isRunning(){
        return running&!isCancelled();
    }

    public void cancel(){
        running=false;
        cancel(true);
    }
    @Override
    protected void onCancelled() {
        super.onCancelled();

    }

    public interface onThumbnailComplete{
         void onComplete(List<Integer>positions);
    }
}
