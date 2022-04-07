package com.file.manager.utils;
;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.documentfile.provider.DocumentFile;

import com.file.manager.ui.Models.CustomFile;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import net.lingala.zip4j.progress.ProgressMonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class ArchiveCompressUtil extends AsyncTask<String,Integer,String> {

    private ArrayList<File>files;
    private File toLocation;
    private String name;
    private HashMap<String,String> stats =new HashMap<>();
    private boolean running=false;
    private String password;
    private OnCompressionCompleteCallback onCompressionCompleteCallback;
    private ZipFile zipFile;
    private CompressionLevel compressionLevel=CompressionLevel.MEDIUM_FAST;
    private long size;
    private Uri uriTree;
    private Context context;
    private CompressionMethod method=CompressionMethod.DEFLATE;
    public ArchiveCompressUtil(Context context,Uri uriTree, ArrayList<File> files){
        this.files=files;
        this.uriTree=uriTree;
        this.context=context;
    }


    public void setCompressionLevel(CompressionLevel compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public File compressFile() throws ZipException {
        ZipParameters zipParameters=new ZipParameters();
        // set encryption
        zipParameters.setCompressionMethod(method);
        zipParameters.setCompressionLevel(compressionLevel);
        if(!password.equals("")) {
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(EncryptionMethod.AES);
            zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
            zipFile.setPassword(password.toCharArray());
        }
        for(File file:files) {
            if (file.isDirectory()) {
                zipFile.addFolder(file, zipParameters);
            }else{
                zipFile.addFile(file,zipParameters);
             }

        }

       return zipFile.getFile();
    }

    public File compressFileSAF() throws Exception,RuntimeException {
        ZipParameters zipParameters = buildZipParams(!password.equals(""));
        ZipOutputStream zipOutputStream=getZipOutputStream(zipParameters);
        int count;
        byte[] buffer=new byte[4096];
        for(File file:files) {
            InputStream inputStream = new FileInputStream(file);
            zipParameters.setFileNameInZip(file.getName());
            zipOutputStream.putNextEntry(zipParameters);
            while ((count = inputStream.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, count);
            }
            zipOutputStream.closeEntry();
        }
        zipOutputStream.close();
        return zipFile.getFile();
    }

    private ZipOutputStream getZipOutputStream(ZipParameters zipParameters) throws Exception {
            DocumentFile documentFile = FileHandleUtil.walkToFile(files.get(0).getParentFile(), context);
            DocumentFile doc = documentFile.createFile(".zip", zipFile.getFile().getName());
            OutputStream stream = context.getContentResolver().openOutputStream(doc.getUri());
            ZipOutputStream zipOutputStream;
            if (zipParameters.isEncryptFiles()) {
                zipOutputStream = new ZipOutputStream(stream, password.toCharArray());
            } else {
                zipOutputStream = new ZipOutputStream(stream);
            }
        return zipOutputStream;
    }

    private ZipParameters buildZipParams(boolean encrypt){
        ZipParameters zipParameters=new ZipParameters();
        zipParameters.setCompressionMethod(CompressionMethod.AES_INTERNAL_ONLY);
        zipParameters.setCompressionLevel(compressionLevel);
        zipParameters.setEncryptFiles(encrypt);
        zipParameters.setEncryptionMethod(EncryptionMethod.AES);
        zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
        return zipParameters;
    }

    public static Map<String,CompressionLevel>getCompressionLevels() {
        return new HashMap<String, CompressionLevel>() {{
                put("Normal", CompressionLevel.NORMAL);
                put("Medium Fast", CompressionLevel.MEDIUM_FAST);
                put("Maximum", CompressionLevel.MAXIMUM);
                put("Higher", CompressionLevel.HIGHER);
                put("Fastest", CompressionLevel.FASTEST);
                put("Faster", CompressionLevel.FASTER);
                put("Fast", CompressionLevel.FAST); }
        };
    }


    public static Map<String,CompressionMethod>getCompressionMethod(){
        return new LinkedHashMap<String,CompressionMethod>(){{
           put("DEFLATE",CompressionMethod.DEFLATE);
           put("AES_INTERNAL",CompressionMethod.AES_INTERNAL_ONLY);
           put("STORE",CompressionMethod.STORE);
        }
       };
    }

    public void setMethod(CompressionMethod method) {
        this.method = method;
    }

    public void setToLocation(File toLocation) {
        this.toLocation = toLocation;
    }

    public void setName(String name) {
        this.name = name;
    }


    public ProgressMonitor getProgressMonitor(){
        return zipFile.getProgressMonitor();
    }

    public void setOnCompressionCompleteCallback(OnCompressionCompleteCallback onCompressionCompleteCallback){
        this.onCompressionCompleteCallback=onCompressionCompleteCallback;
    }
    @Override
    protected String doInBackground(String... strings) {
        running=true;
        try {
            zipFile=new ZipFile(toLocation.getPath()+"/"+name+".zip");
            for (File file:files){
                if(file.isFile()) {
                    size += file.length();
                }else {
                    ArrayList<CustomFile> subFiles=FileHandleUtil.ListFilesRecursively(
                            new CustomFile(file.getPath()), FileFilters.Default(true));
                    for (CustomFile sub:subFiles){
                        size+=sub.length();
                    }

                }
            }
            if(files.get(0).canWrite()) {
                compressFile();
            }else {
                compressFileSAF();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        running=false;
        onCompressionCompleteCallback.onComplete(zipFile.getFile());
    }


    public void cancel(){
        zipFile.getProgressMonitor().setCancelAllTasks(true);
        if(isRunning()){
        onCompressionCompleteCallback.onComplete(zipFile.getFile());}
        running=false;
        cancel(true);
    }

    public ZipFile getZipFile() {
        return zipFile;
    }

    public File getToLocation() {
        return toLocation;
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public long getSize() {
        return size;
    }

    public boolean isRunning() {
        return running&!isCancelled();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public HashMap<String, String> getStats() {
        return stats;
    }

    public interface OnCompressionCompleteCallback{
        void onComplete(File file);
    }
}
