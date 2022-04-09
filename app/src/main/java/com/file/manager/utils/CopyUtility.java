package com.file.manager.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.DocumentsContract;

import androidx.documentfile.provider.DocumentFile;

import com.file.manager.helpers.PermissionsHelper;
import com.file.manager.ui.Models.CopyProgressMonitor;
import com.file.manager.ui.Models.CustomFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class CopyUtility extends AsyncTask<String,Integer,String> {

    public boolean running=false;
    private ArrayList<CustomFile>newFiles=new ArrayList<>();
    private ArrayList<CustomFile> filesToCopy = new ArrayList<>();
    private CustomFile destination;
    private File sourceDirectory;
    private CopyProgressMonitor progressMonitor;
    private String message="";
    private OnCopyListener onCopyListener;
    private File current=null;
    private Uri uri;
    private int id;
    @SuppressLint("StaticFieldLeak")
    private Context context;
    private String startDestinationDir;
    // flag for copy streams
    public static byte FILE_STREAM=0;
    // flag for benchmarking tool
    public static byte BENCHMARK=1;
    private byte mode=FILE_STREAM;
    private long benchByteSize;
    private long benchBuffer;
    public CopyUtility(Context context,Uri uri){
        this.context=context;
        this.uri=uri;
        this.filesToCopy.addAll(CopyHelper.getInstance().getData());
        this.destination=CopyHelper.getInstance().getDestination();
        this.startDestinationDir=DiskUtils.getInstance().getStartDirectory(destination);
        this.progressMonitor= new CopyProgressMonitor();
        CopyHelper.getInstance().reset();
    }

    public CopyUtility(Context context,byte mode,CustomFile destination){
        this.context=context;
        this.progressMonitor= new CopyProgressMonitor();
        this.destination=destination;
        this.mode=mode;
    }

    private boolean createDirectoryFile(CustomFile file){
        File parent=file.getParentFile();
        if(!parent.exists())
            parent.mkdirs();
        if(!file.exists())
             file.mkdirs();

        return file.exists();
    }

    private boolean createDirectoryFile(String destination,String name){
        return createDirectoryFile(new CustomFile(destination+name));
    }

    private String combinePaths(String a,String b){
        return a+b;
    }

    public  void CopyFilesRecursively(CustomFile source,CustomFile file, ArrayList<CustomFile>dirs, FilenameFilter filter) {
        String[] array =file.list(filter);
        if(array!=null) {
            // count the number of files and folders in this directory
            for(String name:array) {
                // exit if cancelled
                if(!isRunning())
                    return;
                if(new File(file,name).isDirectory()) {
                    progressMonitor.addWorkToBeDone(1);
                }else{
                    progressMonitor.addTotalFilesCount(1);
                }
            }
            // copy the files recursively
            for (String name : array) {
                // exit if cancelled
                if (!isRunning())
                    return;
                CustomFile child = new CustomFile(name, file);
                progressMonitor.setDestination(combinePaths(destination.getPath(), trimPath(source.getPath(), child.getPath())));
                progressMonitor.setSource(child.getPath());
                if (child.isDirectory()) {
                    dirs.add(child);
                    createDirectoryFile(destination.getPath(), trimPath(source.getParent(), child.getPath()));
                    current=new CustomFile(combinePaths(destination.getPath(), trimPath(source.getParent(), child.getPath())));
                    progressMonitor.addWorkCompleted(1);
                } else {
                    // size in bytes of the file to be copied
                    progressMonitor.addWorkToBeDone(child.length());
                    progressMonitor.addTotalFilesCopied(1);
                    progressMonitor.addWorkToBeDone(file.length());
                    // test if the parent exists if not create the parentFolder
                    createDirectoryFile(destination.getPath(), trimPath(source.getParent(), child.getParent()));
                    CustomFile folder =new CustomFile(combinePaths(destination.getPath(), trimPath(source.getParent(), child.getParent())));
                    CustomFile new_file=new CustomFile(child.getName(),folder);
                    try {
                        new_file.createNewFile();
                        current=new_file;
                        CopyFiles(child, new_file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        for(int i=0;i<dirs.size();i++){
            CustomFile folder=dirs.get(i);
            dirs.remove(i);
            CopyFilesRecursively(source,folder,dirs,filter);
        }
    }

    private void CopyFilesFileAPI(){
        // file count
        progressMonitor.addTotalFilesCount(filesToCopy.size());
        for(CustomFile file:filesToCopy){
            if(!isRunning())
                break;
            progressMonitor.addTotalFilesCopied(1);
            CustomFile new_file=new CustomFile(combinePaths(destination.getPath(),trimPath(file.getParent(),file.getPath())));
            current=new_file;
            newFiles.add(new_file);
            if(file.isDirectory()) {
                createDirectoryFile(destination.getPath(), "/" + file.getName());
                CopyFilesRecursively(file, file, new ArrayList<CustomFile>(), FileFilters.Default(true));
            }else {
                try {
                    if(!new_file.exists())
                    new_file.createNewFile();
                    progressMonitor.setWorkToBeDone(file.length());
                    CopyFiles(file, new_file);
                } catch (Exception e) {
                    e.printStackTrace();
                    cancel();
                }
            }
        }

    }

    private String trimPath(String parent,String path){
        return path.replace(parent,"");
    }

    public  void CopyFiles(CustomFile source, CustomFile destination) throws IOException{

        InputStream in= new FileInputStream(source);
        progressMonitor.setSource(source.getParentFile().getName()+"/"+source.getName());
        progressMonitor.setDestination(destination.getParentFile().getName()+"/"+destination.getName());
        long bytes=progressMonitor.getWorkCompleted();
        try {
            if(!destination.exists())destination.createNewFile();
            OutputStream out= new FileOutputStream(destination);
            BufferedOutputStream bout=new BufferedOutputStream(out);
            long size=(long)(Math.min(source.length(),DiskUtils.SIZE_MB*200)*0.25f);
            if (source.length()<=DiskUtils.SIZE_MB*5)
                size=source.length();
            try {
                byte []buffer= new byte[(int)size];
                int len;
                while ((len=in.read(buffer))>0){
                    bout.write(buffer,0,len);
                    progressMonitor.setWorkCompleted(bytes+destination.length());
                  if(!isRunning())
                      break;
                }
            }finally {
                bout.close();
            }
        }finally {
            in.close();
        }
    }

    public  ArrayList<CustomFile> SAFCopyFiles(Context context,ArrayList<CustomFile>files, ArrayList<CustomFile>nFiles, CustomFile destination) throws Exception{
        progressMonitor.addTotalFilesCount(files.size());
        for(int i=0;i<files.size();i++){
            CustomFile source=files.get(i);
            current=source;
            progressMonitor.addWorkToBeDone(source.length());
            progressMonitor.addTotalFilesCopied(1);
            if(!isRunning())
                break;
            if(source.isFile()) {
                nFiles.add(new CustomFile(destination.getPath() + "/" + source.getName()));
                DocumentFile destDocFile=FileHandleUtil.walkToFile(destination,context);
                SAFCopyFiles(context,source, destDocFile.createFile("",source.getName()));
            }else {
                DocumentFile destDocFile=FileHandleUtil.walkToFile(destination,context);
                CustomFile newFolder=new CustomFile(destination.getPath() + "/" + source.getName());
                if(!newFolder.exists())
                destDocFile=destDocFile.createDirectory(source.getName());
                 else
                destDocFile=destDocFile.findFile(source.getName());
                // add the parent folder since user may cancel the operation
                nFiles.add(newFolder);
                progressMonitor.setSource(source.getParentFile().getName()+"/"+source.getName());
                progressMonitor.setDestination(newFolder.getParentFile().getName()+"/"+newFolder.getName());
                SAFCopyDirsRecursively(FileHandleUtil.walkToFile(source,context),destDocFile,destDocFile);
            }
        }
        return nFiles;
    }


    private void SAFCopyDirsRecursively(DocumentFile source,DocumentFile destination,DocumentFile parent){
        ArrayList<DocumentFile>folders= new ArrayList<>();
        try {
            SAFCopyDirsRecursively(source,source, destination,folders);
        } catch (Exception e) {
            e.printStackTrace();
            cancel();
        }
    }

    private void SAFCopyDirsRecursively(DocumentFile source,DocumentFile origin,DocumentFile destination,ArrayList<DocumentFile>folders) throws Exception {
        DocumentFile[] files =source.listFiles();
        progressMonitor.setSource(source.getParentFile().getName()+"/"+source.getName());
        progressMonitor.addTotalFilesCount(files.length);
        for (DocumentFile doc : files) {
            String str=FileHandleUtil.treeUriToFilePath(origin.getUri());
            String path=FileHandleUtil.treeUriToFilePath(doc.getUri()).substring(str.length());
            String[]names=path.split("/");
            String entry=FileHandleUtil.treeUriToFilePath(destination.getUri());
            if(!isRunning())
                break;
            if(doc.isDirectory()) {
                createDirs(entry,destination,names);
                folders.add(doc);
            }else {
                DocumentFile file=null;
                try {
                    file= createDirs(entry,destination, Arrays.copyOf(names,names.length-1));
                }catch (NullPointerException ignore){
                    cancel();
                }
               if(file!=null) {
                   progressMonitor.addWorkToBeDone(doc.length());
                   CustomFile fileCopyPath=new CustomFile(this.destination+"/"+FileHandleUtil.treeUriToFilePath(doc.getUri()));
                   // if the file doesn't exist create another
                   DocumentFile fileCopy=fileCopyPath.exists()?file.findFile(doc.getName()):
                           file.createFile(FileHandleUtil.mimeTypeFromUri(doc.getUri().toString()), doc.getName());
                   SAFCopyFiles(context,new File(sourceDirectory+"/"+FileHandleUtil.treeUriToFilePath(doc.getUri())), fileCopy);
               }
            }
            progressMonitor.addTotalFilesCopied(1);
        }
        for(int i=0;i<folders.size();i++){
            if(!isRunning())
                break;
            DocumentFile doc =folders.get(i);
            folders.remove(i);
            SAFCopyDirsRecursively(doc,origin,destination,folders);
        }
    }

    private DocumentFile createDirs(String parent,DocumentFile newFile,String[]names){
        File entry=new File(startDestinationDir+"/"+parent);
        for (String name : names) {
            if(name.equals(""))
                continue;
            entry=new File(entry,name);
            current=entry;
            progressMonitor.setDestination(entry.getParentFile().getName()+"/"+entry.getName());
            try {
                newFile = entry.exists() ? newFile.findFile(name) : newFile.createDirectory(name);
            }catch (NullPointerException e){
                // cancel operation
                cancel();
            }
        }
        return newFile;
    }


    public  void SAFCopyFiles(Context context, File source,DocumentFile destination) throws IOException{
        try (InputStream in = new FileInputStream(source)) {
            current = source;
            progressMonitor.setSource(source.getParentFile().getName() + "/" + source.getName());
            long bytes = progressMonitor.getWorkCompleted();
            DocumentFile parent = destination.getParentFile();
            progressMonitor.setDestination(parent != null ? parent.getName() + "/" + destination.getName() : destination.getName());
            try (OutputStream out = context.getContentResolver().openOutputStream(destination.getUri())) {
                byte[] buffer = new byte[(int) DiskUtils.SIZE_MB];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                    progressMonitor.setWorkCompleted(bytes + destination.length());
                    if (!isRunning())
                        break;
                }
            }
        }

    }

    public  void startBenchmark() {
        File buffer=null;
        try {
            progressMonitor.setDestination(destination.getPath() + "/" + "Bench" + UUID.randomUUID().hashCode());
            buffer= new File(destination + "/Buffer" + UUID.randomUUID().hashCode());
            destination = new CustomFile(progressMonitor.getDestination());
            progressMonitor.setWorkToBeDone(benchByteSize);
            destination.createNewFile();
            buffer.createNewFile();
            OutputStream out1 = new FileOutputStream(destination);
            OutputStream out2 = new FileOutputStream(buffer);
            startBenchmark(out1,out2,buffer);
        }catch (Exception e){
           //error
        }
        // free memory
        if(destination.exists())
            destination.delete();
        if(buffer.exists())
            buffer.delete();
    }

    private void startBenchmarkSAF(){
        DocumentFile document=DocumentFile.fromTreeUri(context,
                PermissionsHelper.getInstance().getUriFromSharedPreference(destination));
        DocumentFile destination=document.createFile("","Bench"+UUID.randomUUID().hashCode());
        DocumentFile bufferDoc=document.createFile("","buffer"+UUID.randomUUID().hashCode());
        try {
        progressMonitor.setWorkToBeDone(benchByteSize);
        File buffer=new File(this.destination+"/"+bufferDoc.getName());
        OutputStream out1= context.getContentResolver().openOutputStream(destination.getUri());
        OutputStream out2=context.getContentResolver().openOutputStream(bufferDoc.getUri());
        startBenchmark(out1,out2,buffer);
        }catch (Exception e){
            //error
        }
        //free memory
        try {
            DocumentsContract.deleteDocument(context.getContentResolver(),destination.getUri());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            DocumentsContract.deleteDocument(context.getContentResolver(),bufferDoc.getUri());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startBenchmark(OutputStream bout1,OutputStream bout2,File buffer) throws IOException{
        try {
            // create a temporary output file to simulate read & write
            byte[] data = new byte[(int) benchBuffer];
            Arrays.fill(data, (byte) (0x32<<2));
            bout2.write(data, 0, data.length);
            InputStream in = new FileInputStream(buffer);
            while (!progressMonitor.isFinished()) {
                in.read(data, 0, data.length);
                bout1.write(data, 0, data.length);
                progressMonitor.addWorkCompleted(data.length);
                if (!isRunning()) {
                    break;
                }
            }
            in.close();
        }catch (Exception e){
            bout1.close();
            bout2.close();
        } finally {
            bout1.close();
            bout2.close();

        }

    }
    public void setBenchByteSize(long benchByteSize) {
        this.benchByteSize = benchByteSize;
    }

    public void setBenchBuffer(long benchBuffer) {
        this.benchBuffer = benchBuffer;
    }

    public File getCurrent() {
        return current;
    }

    public CopyProgressMonitor getProgressMonitor() {
        return progressMonitor;
    }

    @Override
    protected String doInBackground(String... strings) {

        if(mode==FILE_STREAM) {
            try {
                try {
                    sourceDirectory = new File(DiskUtils.getInstance().getStartDirectory(filesToCopy.get(0)));
                }catch (NullPointerException ignore){
                    // this exception is thrown when the file is an installed package
                }
                if (destination.canWrite()) {
                    CopyFilesFileAPI();
                } else {
                    SAFCopyFiles(context, filesToCopy, newFiles, destination);
                }
            } catch( Exception e) {
                e.printStackTrace();
                message = "An Error Occurred!";
                if (onCopyListener != null)
                    onCopyListener.onSuccess(newFiles);
            }

        }else {
            if(destination.canWrite()){
                startBenchmark();
            }else
               startBenchmarkSAF();
        }

        return null;
    }

    public boolean isRunning(){
        return  running;
    }

    public String getMessage() {
        return message;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        running=true;
    }

    public Context getContext() {
        return context;
    }

    public void cancel(){
        cancel(true);
        running=false;
        message="An Error Occurred!";
    }

    public ArrayList<CustomFile> getNewFiles() {
        return newFiles;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        running=false;
        if(!isCancelled()) {
            message = "Operation Complete!";
        }else {
            message="An Error Occurred!";
        }
        if(onCopyListener!=null)
            onCopyListener.onSuccess(newFiles);

    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setCopyListener(OnCopyListener onCopyListener){
        this.onCopyListener=onCopyListener;
    }

    public void Execute(){
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface OnCopyListener{
        void onSuccess(ArrayList<CustomFile> files);
        void onFailed(ArrayList<CustomFile> files);
    }
}
