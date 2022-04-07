package com.file.manager.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.DocumentsContract;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.file.manager.ui.Models.CustomFile;

import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class CutUtility extends AsyncTask<String,Integer,String> {


    public boolean running=false;
    private ArrayList<CustomFile>newFiles=new ArrayList<>();
    private String From;
    private String To;
    private String message="";
    private MutableLiveData<Boolean>update;
    private int filesToCopy=0;
    private int filesCopied=0;
    private Context context;
    private CustomFile destination;
    public CutUtility(Context context){
     this.update= new MutableLiveData<>();
     this.update.setValue(false);
     this.context=context;
     this.destination=CutHelper.getInstance().getDestination();
    }
    public  void MoveFilesRecursively(CustomFile file, ArrayList<CustomFile>dirs,ArrayList<CustomFile>folders,ArrayList<CustomFile>files, FilenameFilter filter){
        String[] array =file.list(filter);
        if(array!=null) {
            for (String name : array) {
                filesToCopy++;
                update.postValue(true);
                CustomFile child = new CustomFile(name, file);
                if (!isRunning())
                    break;
                if (child.isDirectory()) {
                    dirs.add(child);
                    folders.add(child);
                }else {
                    files.add(child);
                }
            }

        }
        for(int i=0;i<dirs.size();i++){
            if(!isRunning())
                break;
            CustomFile folder=dirs.get(i);
            dirs.remove(i);
            MoveFilesRecursively(folder,dirs,folders,files,filter);
        }

    }

    private void moveFiles(ArrayList<CustomFile>files){
        filesToCopy+=files.size();
        To=destination.getPath();
        ArrayList<CustomFile>folders= new ArrayList<>();
        for(CustomFile file:files){
            if(!isRunning())
                break;
            From=file.getPath();
            filesCopied++;
            update.postValue(true);
            // create a new file and add to our list
            CustomFile nFile=new CustomFile(MoveFiles(file,file,destination));
            newFiles.add(nFile);
            /* in case we used DocumentContract to move files we
             will have to delete the files manually
             */
            if(file.isDirectory())
               folders.add(file);
            ArrayList<CustomFile>subFiles= new ArrayList<>();
            ArrayList<CustomFile>subFolders= new ArrayList<>();
            MoveFilesRecursively(file,new ArrayList<CustomFile>(),subFolders,subFiles,FileFilters.Default(true));
            for(CustomFile subFile:subFolders){
                if(!isRunning())
                    break;
                From=subFile.getPath();
                filesCopied++;
                update.postValue(true);
                MoveFiles(subFile,file,destination);
            }
            for(CustomFile subFile:subFiles){
                if(!isRunning())
                    break;
                From=subFile.getPath();
                filesCopied++;
                update.postValue(true);
                MoveFiles(subFile,file,destination);
            }

        }
        // in case we used the documentContract API
        // it fails to move folders so delete residue folders manually
        if(!destination.canWrite()&isRunning()) {
            filesCopied-=folders.size();
            for (CustomFile file : folders) {
                if (!file.exists())
                    continue;
                From=file.getPath();
                update.postValue(true);
                try {
                    FileHandleUtil.DeleteFiles(file, context);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                filesCopied+=1;
            }
        }

    }

    public int getPercent(){
        return (int)(((float)filesCopied/(float)filesToCopy)*100);
    }
    public int getFilesCopied() {
        return filesCopied;
    }

    public int getFilesToCopy() {
        return filesToCopy;
    }

    public String getFrom() {
        return From;
    }

    public String getTo() {
        return To;
    }

    public ArrayList<CustomFile> getNewFiles() {
        return newFiles;
    }

    public  String MoveFiles(CustomFile source, CustomFile parent, CustomFile destination){
        String name=source.getPath().replace(parent.getParent(),"");
        CustomFile newPath=new CustomFile(destination.getPath()+name);
        try {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N&&!destination.canWrite()) {
            Uri sourceUri=FileHandleUtil.walkToFile(source,context).getUri();
            Uri sourceParentUri=FileHandleUtil.walkToFile(source.getParentFile(),context).getUri();
            Uri targetUri=FileHandleUtil.walkToFile(new CustomFile(newPath.getParent()),context).getUri();

                if (source.isFile())
                    DocumentsContract.moveDocument(context.getContentResolver(), sourceUri, sourceParentUri, targetUri);
                else
                    if(!newPath.exists())
                    FileHandleUtil.createFolder(context, new CustomFile(newPath.getParent()), newPath.getName());
        }else {

            FileHandleUtil.renameTo(source,newPath);
        }
        }catch (Exception e){
            // cancel operation
            cancel();
        }
        return newPath.getPath();
    }
    @Override
    protected String doInBackground(String... strings) {
        moveFiles(CutHelper.getInstance().getData());
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        running=true;
    }

    public void cancel(){
        cancel(true);
        running=false;
        message="An Error Occurred!";
        update.postValue(true);
    }

    public boolean isRunning(){
        return running;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(!isCancelled())
            message="Operation Success!";
        running=false;
        update.postValue(true);
    }

    public void Execute(){
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public String getMessage() {
        return message;
    }

    public LiveData<Boolean> getUpdate() {
        return update;
    }


    public interface OnCutListener{
        void onSuccess(ArrayList<CustomFile> files);
    }
}
