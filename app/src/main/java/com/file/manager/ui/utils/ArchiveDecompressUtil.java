package com.file.manager.ui.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.documentfile.provider.DocumentFile;

import com.file.manager.OnTaskCompleteListener;
import com.file.manager.ui.Models.CustomFile;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.LocalFileHeader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArchiveDecompressUtil  {


  private ZipFile zipFile;
  private Context context;
  private CustomFile source;
  private int firstKey=-1;
  private int dirKey=1;
  private List<FileHeader>fileHeaders;
  private List<FileHeader>currentDir= new ArrayList<>();
  private Map<Integer,List<FileHeader>>map= new HashMap<>();
  private String destination;
  private OnExtractCompleteCallback onExtractCompleteCallback;
  private String password="";
  private Uri treeUri;
  //western europe as default
  private String charset="ISO-8859-1";
  private String errorMessage;
  private boolean error=false;
  private boolean running=false;
    public ArchiveDecompressUtil(Context context,Uri treeUri, CustomFile source)  {
        this.context=context;
        this.source=source;
        this.treeUri=treeUri;
    }

    private void extractAll() throws Exception {
        if(new File(destination).canWrite()) {
            if (isEncrypted()) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                zipFile = new ZipFile(source.getPath(), password.toCharArray());
            }
            zipFile.setCharset(Charset.forName(charset));
            zipFile.extractAll(destination);
        }else {
            if(treeUri!=null)
                streamExtract(treeUri);

        }
    }


    private void extractFile(FileHeader header) throws Exception {
        if(new File(destination).canWrite()) {
            if (isEncrypted()) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                zipFile = new ZipFile(source.getPath(), password.toCharArray());
            }
            zipFile=new ZipFile(source.getPath());
            zipFile.setCharset(Charset.forName(charset));
            zipFile.extractFile(header, destination);
        }else {
            if(treeUri!=null)
                streamExtract(treeUri);
        }

    }



    private void streamExtract(Uri treeUri) throws IOException {
        LocalFileHeader localFileHeader;
        int count;
        byte[]buffer=new byte[4096];
        InputStream inputStream= new FileInputStream(source);
        ZipInputStream zipInputStream= new ZipInputStream(inputStream,password.toCharArray());
        while ((localFileHeader=zipInputStream.getNextEntry())!=null){
            File extractedFile=new File(destination+"/"+localFileHeader.getFileName());
            zipFile.getProgressMonitor().setFileName(extractedFile.getPath());
            DocumentFile parent=FileHandleUtil.walkToFile(extractedFile.getParentFile(),DocumentFile.fromTreeUri(context,treeUri));
            DocumentFile file;
            if(!localFileHeader.isDirectory()) {
                file = parent.createFile("", extractedFile.getName());
            } else {
                if(parent.findFile(extractedFile.getName())==null){
                    parent.createDirectory(extractedFile.getName());
                }
                continue;
            }
            OutputStream out=context.getContentResolver().openOutputStream(file.getUri());
            while ((count=zipInputStream.read(buffer))!=-1){
                out.write(buffer,0,count);
            }
        }

    }


    public ExtractAllTask extract(){
        return new ExtractAllTask();
    }

    public ExtractSingleTask extract(FileHeader header){
        return new ExtractSingleTask(header);
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void moveNext(){
        dirKey++;
        setDirKey(dirKey);
    }

    public void movePrev(){
        dirKey--;
        setDirKey(dirKey);
    }

    public int getFirstKey() {
        return firstKey;
    }

    public void setDirKey(int dirKey) {
        this.dirKey = dirKey;
        List<FileHeader>ll=map.get(dirKey);
        if(ll!=null)
        currentDir=ll;
        else currentDir=new ArrayList<>();
    }

    public int getDirKey() {
        return dirKey;
    }

    public List<FileHeader> getCurrentDirList() {
        return currentDir;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public ListFileHeaders listFileHeadersTask(OnTaskCompleteListener onTaskCompleteListener){
        return new ListFileHeaders(onTaskCompleteListener);
    }

    private void groupHeaders(){
        this.zipFile=new ZipFile(source);
        try {
            this.fileHeaders=zipFile.getFileHeaders();
        } catch (ZipException e) {
            e.printStackTrace();
            fileHeaders= new ArrayList<>();
        }
        for(FileHeader fileHeader:fileHeaders){
            List<FileHeader>list;
            System.out.println(fileHeader.getFileName());
            String name=!fileHeader.isDirectory()?fileHeader.getFileName()+"/":fileHeader.getFileName();
            String[]arr=name.split("/");
            // get the first key
            if(firstKey==-1)
                firstKey=arr.length;
            if(map.containsKey(arr.length)){
              list=map.get(arr.length);
            }else {
                list= new ArrayList<>();
                map.put(arr.length,list);
            }
            list.add(fileHeader);
        }
        for (Map.Entry<Integer, List<FileHeader>> entry:map.entrySet())
             Sorter.sortZipEntry(entry.getValue());
        dirKey=firstKey;
        List<FileHeader>ll=map.get(dirKey);
        if(ll!=null)
        currentDir=ll;
    }


    public static String getHeaderName(FileHeader header){
        String name=!header.isDirectory()?header.getFileName()+"/":header.getFileName();
        String[]arr=name.split("/");
        return arr[arr.length-1];
    }


    public void setPassword(String password){
        this.password=password;
    }
    public boolean isEncrypted() throws ZipException {
        return zipFile.isEncrypted();
    }
    public String getDestination(){
        return destination;
    }
    public ZipFile getZipFile(){
        return zipFile;
    }
    public int size(){
        return fileHeaders.size();
    }
    public class ExtractAllTask extends AsyncTask<String,Integer,String>{


        @Override
        protected String doInBackground(String... strings) {
            running=true;
            try {
                File dest=new File(destination);
                if(!dest.exists())
                    dest.mkdirs();
                extractAll();
            } catch (ZipException e) {
                e.printStackTrace();
                error=true;
                errorMessage=e.getMessage();
                onExtractCompleteCallback.onError();
            } catch (Exception e) {
                error=true;
                errorMessage=e.getMessage();
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            running=false;
            onExtractCompleteCallback.onComplete(destination);
        }
        public void cancel(){
            zipFile.getProgressMonitor().setCancelAllTasks(true);
            running=false;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        public boolean isRunning() {
            return running&!isCancelled();
        }
    }
    public void cancel(){
        zipFile.getProgressMonitor().setCancelAllTasks(true);
        running=false;
    }

    public class ListFileHeaders extends AsyncTask<String,Integer,String>{

        private OnTaskCompleteListener onListFileHeaderListener;
        public ListFileHeaders(OnTaskCompleteListener onListFileHeaderListener){
            this.onListFileHeaderListener=onListFileHeaderListener;
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                groupHeaders();
            }catch (Exception e){
                onListFileHeaderListener.onTaskComplete();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            onListFileHeaderListener.onTaskComplete();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
    public class ExtractSingleTask extends AsyncTask<String,Integer,String>{

        private FileHeader header;
        private OnExtractCompleteCallback onComplete;
        public ExtractSingleTask(FileHeader header){
            this.header=header;
        }
        @Override
        protected String doInBackground(String... strings) {
            running=true;
            try {
                extractFile(header);
            } catch (ZipException e) {
                e.printStackTrace();
                running=false;
                error=true;
                errorMessage=e.getMessage();
                onComplete.onError();
            } catch (Exception e) {
                e.printStackTrace();
                running=false;
                error=true;
                errorMessage=e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            onComplete.onComplete(destination);
            running=false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        public void cancel(){
            zipFile.getProgressMonitor().setCancelAllTasks(true);
            running=false;
        }

        public void setOnComplete(OnExtractCompleteCallback onComplete) {
            this.onComplete = onComplete;
        }

        public boolean isRunning() {
            return running;
        }

        public String getDestination(){
            return destination+"/"+header.getFileName();
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isError() {
        return error;
    }

    public boolean isRunning() {
        return running;
    }

    public void setOnExtractCompleteCallback(OnExtractCompleteCallback onExtractCompleteCallback) {
        this.onExtractCompleteCallback = onExtractCompleteCallback;
    }

    public interface OnExtractCompleteCallback{
        void onComplete(String dest);
        void onError();
    }



}
