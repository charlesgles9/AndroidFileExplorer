package com.file.manager.ui.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.DocumentsContract;

import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.file.manager.ui.Models.CustomFile;

import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class DeleteFilesUtility extends AsyncTask<String,Integer,String> {

    private Context context;
    private Uri uri;
    private ArrayList<CustomFile>files;
    private String From;
    private int fileCount;
    private int fileDeleted;
    private String message;
    private boolean running=false;
    private MutableLiveData<Boolean>update;
    private boolean error;
    public DeleteFilesUtility(Context context, Uri uri, ArrayList<CustomFile>files){
        this.context=context;
        this.uri=uri;
        this.files=files;
        this.update= new MutableLiveData<>();
    }

    public  void ListFilesRecursively(CustomFile file, ArrayList<CustomFile>dirs, ArrayList<CustomFile>foundFiles, ArrayList<CustomFile>foundFolders, FilenameFilter filter){
        String[] file_names =file.list(filter);
        for(String name:file_names){
            CustomFile child= new CustomFile(name,file);
            if(!isRunning())
                break;
            if(child.isDirectory()){
                dirs.add(child);
                foundFolders.add(child);
            }else {
                foundFiles.add(child);
                child.setTempThumbnail();
            }

        }

        for(int i=0;i<dirs.size();i++){
            if(!isRunning())
                break;
            CustomFile folder=dirs.get(i);
            dirs.remove(i);
            ListFilesRecursively(folder,dirs,foundFiles,foundFolders,filter);
        }

    }

    public  void DeleteFiles(){
        ArrayList<CustomFile> subFolders = new ArrayList<>();
        ArrayList<CustomFile> subFiles= new ArrayList<>();

        for (CustomFile file : files) {
            if(!isRunning())
                break;
            if(file.isDirectory()) {
              if(uri==null)
               ListFilesRecursively(file, new ArrayList<CustomFile>(), subFiles, subFolders,FileFilters.Default(true));
              else subFolders.add(file);
            }else {
                subFiles.add(file);

            }
        }

        fileCount+=subFolders.size()+subFiles.size();
        for (int i=subFiles.size()-1;i>=0;i--) {
            if(!isRunning())
                break;
            CustomFile file=subFiles.get(i);
            From=file.getPath();
            FileHandleUtil.DeleteFiles(file);
            subFiles.remove(i);
            fileDeleted++;
            update.postValue(true);
        }

        for(int i=subFolders.size()-1;i>=0;i--){
            if(!isRunning())
                break;

            try {
                if(uri!=null)
                FileHandleUtil.DeleteFiles(subFolders.get(i),context);
                else
                    FileHandleUtil.DeleteFiles(subFolders.get(i));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (NullPointerException ignore){

            }
            From=subFolders.get(i).getPath();
            fileDeleted++;
            update.postValue(true);
        }


    }

    public  void SAFDeleteFiles() throws FileNotFoundException {
        fileCount+=files.size();
        for (CustomFile file:files) {
            if(!file.exists())
                continue;
            From=file.getPath();
            update.postValue(true);
            DocumentFile documentFile = FileHandleUtil.walkToFile(file,DocumentFile.fromTreeUri(context,uri));
            SAFDeleteFiles(context,documentFile);
            fileDeleted++;
        }
    }
    public  void SAFDeleteFiles(Context context, DocumentFile file) throws FileNotFoundException,IllegalStateException {
        DocumentsContract.deleteDocument(context.getContentResolver(),file.getUri());
    }

    public String getMessage() {
        return message;
    }

    public void Execute(){
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected String doInBackground(String... strings) {
        if(files.get(0).canWrite()){
            DeleteFiles();
        }else {
            try {
                SAFDeleteFiles();
            } catch (Exception e) {
                e.printStackTrace();
                error=true;
                cancel();
            }
        }
        return null;
    }

    public void cancel(){
        cancel(true);
        running=false;
        update.postValue(true);
    }

    public boolean isError(){
        return error;
    }

    public boolean isRunning(){
        return  running;
    }

    public LiveData<Boolean> getUpdate() {
        return update;
    }

    public String getFrom() {
        return From;
    }

    public ArrayList<CustomFile> getFiles() {
        return files;
    }

    public int getFileCount() {
        return fileCount;
    }

    public int getFileDeleted() {
        return fileDeleted;
    }

    public int getPercent(){
        return (int)((float)fileDeleted/(float)fileCount*100);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        running=false;
        update.postValue(true);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        running=true;
    }

    public interface OnDeleteCompleteListener{
        void onSuccess(ArrayList<CustomFile>data);
    }
}
