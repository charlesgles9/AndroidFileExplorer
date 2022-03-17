package com.file.manager.ui.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.file.manager.helpers.PermissionsHelper;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.home.RecentFilesContainer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FileHandleUtil {

    private FileHandleUtil(){

    }

    public static ArrayList<CustomFile> ListFiles(Context context,CustomFile file, ArrayList<CustomFile>array, FilenameFilter filter){
        String[] files =file.list(filter);
        int position=0;
        if(files!=null) {
            for (String name : files) {
                CustomFile customFile = new CustomFile(name, file);
                customFile.position = position;
                array.add(customFile);
                position++;
                customFile.setTempThumbnail();
                if (customFile.isDirectory())
                    customFile.getLocalThumbnail().setLoaded(true);
            }
        }else{
            File mount=new File(DiskUtils.getInstance().getStartDirectory(file));
            Uri uri=PermissionsHelper.getInstance().getUriFromSharedPreference(mount);
            if(uri==null)
                return array;
            SAFListFiles(context,file,uri,array);
        }
        return array;
    }


    public static DocumentFile[] ListFiles(Context context,Uri muri){
        final ContentResolver resolver=context.getContentResolver();
        final Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(muri,
                DocumentsContract.getDocumentId(muri));
        final ArrayList<Uri>results= new ArrayList<>();
        Cursor c=null;
        try{
            c=resolver.query(children,new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            },DocumentsContract.Document.COLUMN_SIZE,null,null);
            while (c.moveToNext()){
                final String documentId=c.getString(0);
                final Uri documentUri=DocumentsContract.buildDocumentUriUsingTree(muri,
                        documentId);
                results.add(documentUri);
            }
        }catch (Exception e){

        }finally {
         if(c!=null)
            c.close();
        }

        final Uri[]result=results.toArray(new Uri[results.size()]);
        final DocumentFile[] files=new DocumentFile[result.length];
        for(int i=0;i<result.length;i++){
            files[i]= DocumentFile.fromSingleUri(context, result[i]);
        }
        return files;
    }

    public static void SAFListFiles(Context context,CustomFile file,Uri uri,ArrayList<CustomFile>array){
        DocumentFile documentFile=DocumentFile.fromTreeUri(context,uri);
        documentFile=walkToFile(file,documentFile);
        DocumentFile docArray[] = ListFiles(context,documentFile.getUri());
        String startDirectory=DiskUtils.getInstance().getStartDirectory(file);
        int position = 0;
        for (DocumentFile doc : docArray) {
            CustomFile customFile = new CustomFile(startDirectory+"/"+treeUriToFilePath(doc.getUri()));
            customFile.position = position;
            array.add(customFile);
            position++;
            if (customFile.isFile())
                customFile.setTempThumbnail();
        }
    }


    public static String treeUriToFilePath(Uri uri){
        String path=uri.getPath();
        return path.substring(path.lastIndexOf(":")+1);

    }

    // converts a uriTree or a uriFile to standard java file path
    public static String uriToFilePath(Uri uri){
        String path=uri.getPath();
        int isTreeUri=path.indexOf(":");
        File[]mounts=DiskUtils.getInstance().getStorageDirs();
      if(isTreeUri!=-1){
            for(File mount:mounts){
                if(mount!=null){
                    File dir=new File(mount.getPath()+"/"+treeUriToFilePath(uri));
                    if(dir.exists()){
                        return dir.getPath();
                    }
                }
            }
        }else{
          // it's probably file path then
          for(File mount:mounts) {
              int index = path.indexOf(mount.getPath());
              if (index != -1)
                  return path.substring(index);
          }
        }
        return null;
    }


    public static DocumentFile walkToFile(File file,Context context){
        String start=DiskUtils.getInstance().getStartDirectory(file);
        Uri uri= PermissionsHelper.getInstance().getUriFromSharedPreference(new File(start));
        String paths=file.getPath().replace(start,"");
        String dirs[]=paths.split("/");
        DocumentFile doc=DocumentFile.fromTreeUri(context,uri);
        for(int i=1;i<dirs.length;i++){
            String dir=dirs[i];
            DocumentFile ndoc=doc.findFile(dir);
            if(ndoc!=null)
                doc=ndoc;
        }
        return doc;
    }
    public static DocumentFile walkToFile(File file,DocumentFile doc){
        if(file.getName().equals(doc.getName()))
            return doc;
        String start=DiskUtils.getInstance().getStartDirectory(file);
        String paths=file.getPath().replace(start,"");
        String dirs[]=paths.split("/");
        for(int i=1;i<dirs.length;i++){
            String dir=dirs[i];
            DocumentFile ndoc=doc.findFile(dir);
            if(ndoc!=null)
              doc=ndoc;
        }
        return doc;
    }
    public static ArrayList<CustomFile> ListFilesRecursively(CustomFile file, FilenameFilter filter){
        ArrayList<CustomFile>subFiles= new ArrayList<>();
        ArrayList<CustomFile>subFolders= new ArrayList<>();
        ArrayList<CustomFile>files= new ArrayList<>();
        ListFilesRecursively(file,new ArrayList<CustomFile>(),subFiles,subFolders,filter);
        files.addAll(subFiles);
        files.addAll(subFolders);
        return files;
    }

    public static void ListFilesRecursively(CustomFile file,ArrayList<CustomFile>files,ArrayList<CustomFile>dirs,FilenameFilter filter){
        ListFilesRecursively(file,new ArrayList<CustomFile>(),files,dirs,filter);
    }

    public static void ListFilesRecursively(CustomFile file,ArrayList<CustomFile>dirs,ArrayList<CustomFile>foundFiles,ArrayList<CustomFile>foundFolders,FilenameFilter filter){
        String[] file_names =file.list(filter);
        for(String name:file_names){
            CustomFile child= new CustomFile(name,file);
            if(child.isDirectory()){
                dirs.add(child);
                foundFolders.add(child);
            }else {
                foundFiles.add(child);
                child.setTempThumbnail();
            }

        }

        for(int i=0;i<dirs.size();i++){
            CustomFile folder=dirs.get(i);
            dirs.remove(i);
            ListFilesRecursively(folder,dirs,foundFiles,foundFolders,filter);
        }

    }

    public static void ListFilesRecursivelyGalleryGrid(CustomFile file, ArrayList<CustomFile>files, ArrayList<CustomFile>folders, FilenameFilter filter){
        ListFilesRecursivelyGalleryGrid(file,new ArrayList<CustomFile>(),files,folders,filter);
    }

    public static void ListFilesRecursivelyGalleryGrid(CustomFile source, ArrayList<CustomFile>dirs, ArrayList<CustomFile>foundFiles, ArrayList<CustomFile>foundFolders, FilenameFilter filter){
        String[] array=source.list(filter);;
        if(array!=null)
        for(String name:array){
            CustomFile child= new CustomFile(name,source);
            if(child.isDirectory()){
                dirs.add(child);
            }else {
                source.setLocalThumbnail(child.getLocalThumbnail());
                foundFiles.add(child);
                child.setTempThumbnail();
            }

        }
        if(!foundFiles.isEmpty()&!source.isStartDirectory()) {
            foundFolders.add(source);
            foundFiles.clear();
        }
        for(int i=0;i<dirs.size();i++){
            CustomFile folder=dirs.get(i);
            dirs.remove(i);
            ListFilesRecursivelyGalleryGrid(folder,dirs,foundFiles,foundFolders,filter);
        }

    }

    public static void ListFilesRecursivelyGalleryList(CustomFile file, ArrayList<CustomFile>files, ArrayList<CustomFile>folders, FilenameFilter filter){
        ListFilesRecursivelyGalleryList(file,new ArrayList<CustomFile>(),files,folders,filter);
    }

    public static void ListFilesRecursivelyGalleryList(CustomFile source, ArrayList<CustomFile>dirs, ArrayList<CustomFile>foundFiles, ArrayList<CustomFile>foundFolders, FilenameFilter filter){
        String[] array=source.list(filter);
        if(array!=null)
            for(String name:array){
                CustomFile child= new CustomFile(name,source);
                if(child.isDirectory()){
                    dirs.add(child);
                }else {
                    source.setLocalThumbnail(child.getLocalThumbnail());
                    foundFiles.add(child);
                    child.setTempThumbnail();
                }

            }

        for(int i=0;i<dirs.size();i++){
            CustomFile folder=dirs.get(i);
            dirs.remove(i);
            ListFilesRecursivelyGalleryList(folder,dirs,foundFiles,foundFolders,filter);
        }

    }




    public static void ListLargeFilesRecursively(CustomFile source,ArrayList<CustomFile>files,FilenameFilter filter){
        ListLargeFilesRecursively(source,new ArrayList<CustomFile>(),files,new ArrayList<CustomFile>(),filter);
    }

    public static void ListLargeFilesRecursively(CustomFile file,ArrayList<CustomFile>dirs,ArrayList<CustomFile>foundFiles,ArrayList<CustomFile>foundFolders,FilenameFilter filter){
        String[] array =file.list(filter);
        if(array!=null)
        for(String name:array){
            CustomFile child= new CustomFile(name,file);
            if(child.isDirectory()){
                dirs.add(child);
                foundFolders.add(child);
            }else {
                foundFiles.add(child);
                child.setTempThumbnail();
            }
        }
        for(int i=0;i<dirs.size();i++){
            CustomFile folder=dirs.get(i);
            dirs.remove(i);
            ListLargeFilesRecursively(folder,dirs,foundFiles,foundFolders,filter);
        }
    }


    public static void ListAllImagesFileAPI(CustomFile src, ArrayList<CustomFile>files){
        ListMediaRecursively(src,new ArrayList<CustomFile>(),files,new ArrayList<CustomFile>(),FileFilters.FilterFoldersImageGallery());
    }


    public static void ListAllVideoFileAPI(CustomFile src, ArrayList<CustomFile>files){
        ListMediaRecursively(src,new ArrayList<CustomFile>(),files,new ArrayList<CustomFile>(),FileFilters.FilterFoldersVideoGallery());
    }
    public static void ListAllMediaFileAPI(CustomFile src, ArrayList<CustomFile>files){
        ListMediaRecursively(src,new ArrayList<CustomFile>(),files,new ArrayList<CustomFile>(),FileFilters.FilterMediaFiles());
    }
    public static void ListAllAudioFileAPI(CustomFile src, ArrayList<CustomFile>files){
        ListMediaRecursively(src,new ArrayList<CustomFile>(),files,new ArrayList<CustomFile>(),FileFilters.FilterFoldersAudioGallery());
    }
    public static void ListAllDocumentFileAPI(CustomFile src, ArrayList<CustomFile>files){
        ListMediaRecursively(src,new ArrayList<CustomFile>(),files,new ArrayList<CustomFile>(),FileFilters.FilterFoldersDocumentGallery(11));
    }

    public static void ListAllCompressedFileAPI(CustomFile src, ArrayList<CustomFile>files){
        ListMediaRecursively(src,new ArrayList<CustomFile>(),files,new ArrayList<CustomFile>(),FileFilters.FilterFoldersCompressedGallery(11));
    }

    public static void ListDocuments(ArrayList<CustomFile>files){
        File[]mounts=DiskUtils.getInstance().getStorageDirs();
        for (File mount:mounts) {
            if(mount!=null){
                ListMediaRecursively(new CustomFile(mount.getPath()),new ArrayList<CustomFile>(),files,
                        new ArrayList<CustomFile>(),FileFilters.FilterFoldersDocumentGallery(10));
            }
        }

    }
    public static void ListCompressed(ArrayList<CustomFile>files){
        File[]mounts=DiskUtils.getInstance().getStorageDirs();
        for (File mount:mounts) {
            if(mount!=null){
                ListMediaRecursively(new CustomFile(mount.getPath()),new ArrayList<CustomFile>(),files,
                        new ArrayList<CustomFile>(),FileFilters.FilterFoldersCompressedGallery(10));
            }
        }
    }
    public static void ListApplication(ArrayList<CustomFile>files){
        File[]mounts=DiskUtils.getInstance().getStorageDirs();
        for (File mount:mounts) {
            if(mount!=null){
                ListMediaRecursively(new CustomFile(mount.getPath()),new ArrayList<CustomFile>(),files,
                        new ArrayList<CustomFile>(),FileFilters.FilterFoldersApplicationsGallery(10));
            }
        }

    }
    public static void ListMediaRecursively(CustomFile file,ArrayList<CustomFile>dirs,ArrayList<CustomFile>foundFiles,ArrayList<CustomFile>foundFolders,FilenameFilter filter){
        String[] file_names =file.list(filter);
        if(file_names!=null)
            for(String name:file_names){
                CustomFile child= new CustomFile(name,file);
                if(child.isDirectory()){
                    dirs.add(child);
                    foundFolders.add(child);
                }else {
                    foundFiles.add(child);
                    child.setTempThumbnail();
                }

            }

        for(int i=0;i<dirs.size();i++){
            CustomFile folder=dirs.get(i);
            dirs.remove(i);
            ListMediaRecursively(folder,dirs,foundFiles,foundFolders,filter);
        }

    }
    public static long getFileSizeArray(ArrayList<CustomFile>files){
        long bytes=0L;
        ArrayList<CustomFile> subFolders = new ArrayList<>();
        ArrayList<CustomFile> subFiles= new ArrayList<>();
        for (CustomFile file : files) {
            if(file.isDirectory())
            ListFilesRecursively(file, new ArrayList<CustomFile>(), subFiles, subFolders,FileFilters.Default(true));
            else subFiles.add(file);
        }
        for (CustomFile file : subFiles) {
            bytes += file.length();
        }
        return bytes;
    }

    public static void fetchImageFilesGallery(Context context, ArrayList<CustomFile>files) {
        final String[]projection={MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED};
        MergeCursor cursor=new MergeCursor(new Cursor[]{
                context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection,
                        null,null,null),
                context.getContentResolver().query(MediaStore.Images.Media.INTERNAL_CONTENT_URI,projection,
                        null,null,null)
        });

        HashMap<String,CustomFile>map= new HashMap<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            int lastPoint = path.lastIndexOf(".");
            path = path.substring(0, lastPoint) + path.substring(lastPoint);
            CustomFile file = new CustomFile(path);
            if(!file.exists()) {
                cursor.moveToNext();
                continue;
            }
            CustomFile parent = new CustomFile(file.getParent());
            file.setTempThumbnail();
            parent.setLocalThumbnail(file.getLocalThumbnail());
            if(!DiskUtils.getInstance().isStartDirectory(parent.getPath()))
                map.put(parent.getPath(), parent);
            else map.put(path,file);
            parent.setTempThumbnail();
            cursor.moveToNext();
        }
        cursor.close();
        for (Map.Entry<String, CustomFile> stringCustomFileEntry : map.entrySet()) {
            files.add(stringCustomFileEntry.getValue());
        }
    }

    public static void fetchImageFiles(Context context, ArrayList<CustomFile>files) {
        final String[]projection={MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATE_MODIFIED};
        String sortOrder= MediaStore.Images.Media.DATE_MODIFIED+" DESC";
        MergeCursor cursor=new MergeCursor(new Cursor[]{
                context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection,
                        null,null,sortOrder),
                context.getContentResolver().query(MediaStore.Images.Media.INTERNAL_CONTENT_URI,projection,
                        null,null,sortOrder)
        });
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            if(!path.contains(".")) {
                cursor.moveToNext();
                continue;
            }
            int lastPoint = path.lastIndexOf(".");
            path = path.substring(0, lastPoint) + path.substring(lastPoint);
            CustomFile file = new CustomFile(path);
            if(!file.exists()) {
                cursor.moveToNext();
                continue;
            }
            file.setTempThumbnail();
            files.add(file);
            cursor.moveToNext();
        }
        cursor.close();
    }

    public static void fetchVideoFiles(Context context, ArrayList<CustomFile>files) {
        final String[]columns={MediaStore.Video.Media.DATA};
        String sortOrder=MediaStore.Video.Media.DATE_MODIFIED+" DESC";
        MergeCursor cursor= new MergeCursor(new Cursor[]{
                context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,columns,
                        null,null,sortOrder),
                context.getContentResolver().query(MediaStore.Video.Media.INTERNAL_CONTENT_URI,columns,
                        null,null,sortOrder)
        });
         cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            if(!path.contains(".")) {
                cursor.moveToNext();
                continue;}
            int lastPoint = path.lastIndexOf(".");
            path = path.substring(0, lastPoint) + path.substring(lastPoint);
            CustomFile file = new CustomFile(path);
            if(!file.exists()) {
                cursor.moveToNext();
                continue;
            }
            file.setTempThumbnail();
            files.add(file);
            cursor.moveToNext();
        }
        cursor.close();
    }

    public static void fetchVideoFilesGallery(Context context, ArrayList<CustomFile>files) {
        final String[]columns={MediaStore.Video.Media.DATA};
        Cursor cursor= new MergeCursor(new Cursor[]{
                context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,columns,
                        null,null,null),
                context.getContentResolver().query(MediaStore.Video.Media.INTERNAL_CONTENT_URI,columns,
                        null,null,null)
        });
        HashMap<String,CustomFile>map= new HashMap<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            if(!path.contains(".")) {
                cursor.moveToNext();
                continue;}
            int lastPoint = path.lastIndexOf(".");

            path = path.substring(0, lastPoint) + path.substring(lastPoint);
            CustomFile file = new CustomFile(path);
            if(!file.exists()) {
                cursor.moveToNext();
                continue;
            }
            CustomFile parent = new CustomFile(file.getParent());
            file.setTempThumbnail();
            parent.setLocalThumbnail(file.getLocalThumbnail());
            if(!DiskUtils.getInstance().isStartDirectory(parent.getPath()))
                map.put(parent.getPath(), parent);
            else map.put(path,file);
            parent.setTempThumbnail();
            cursor.moveToNext();
        }
        cursor.close();
        for (Map.Entry<String, CustomFile> stringCustomFileEntry : map.entrySet()) {
            files.add(stringCustomFileEntry.getValue());
        }
    }

    public static void fetchAudioFiles(Context context,ArrayList<CustomFile>files) {
        final String[]columns={MediaStore.Audio.Media.DATA};
        Cursor cursor= new MergeCursor(new Cursor[]{
                context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,columns,
                        null,null,null),
                context.getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,columns,
                        null,null,null)
        });
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            if(!path.contains(".")) {
                cursor.moveToNext();
                continue;
            }
            int lastPoint = path.lastIndexOf(".");
            path = path.substring(0, lastPoint) + path.substring(lastPoint);
            CustomFile file = new CustomFile(path);
            if(file.exists()&file.length()>0)
            files.add(file);
            file.setTempThumbnail();
            cursor.moveToNext();
        }
        cursor.close();
    }

    public static void fetchRecentMedia(Context context,RecentFilesContainer container) throws Exception{
        ArrayList<CustomFile>images= new ArrayList<>();
        ArrayList<CustomFile>videos=new ArrayList<>();
        ArrayList<CustomFile>files= new ArrayList<>();
         fetchVideoFiles(context,videos);
         fetchImageFiles(context,images);
        for (int i=0;i<Math.min(90,images.size());i++)
            files.add(images.get(i));
        for (int i=0;i<Math.min(90,videos.size());i++)
            files.add(videos.get(i));
        Sorter.sortByDate(files);
        for(int i=0;i<files.size();i++){
            container.add(files.get(i));
        }
    }


    public static void DeleteFiles(CustomFile file,Context context) throws FileNotFoundException,NullPointerException {
        DocumentsContract.deleteDocument(context.getContentResolver(),walkToFile(file,context).getUri());
    }
    public static void DeleteFiles(CustomFile file){
        file.delete();
    }

    public static boolean renameTo(File file,CustomFile nFile){
       return file.renameTo(nFile);
    }

    public static void SAFRenameTo(DocumentFile doc,File file,String name){
        String startDir=DiskUtils.getInstance().getStartDirectory(file);
        String []dirs=file.getPath().replace(startDir,"").split("/");
        for(String dir:dirs){
            DocumentFile next=doc.findFile(dir);
            if(next!=null)doc=next;
        }
        if(doc!=null)doc.renameTo(name);
    }

    public static CustomFile createFolder(Context context, CustomFile parent, String name) throws NullPointerException{
        String start=DiskUtils.getInstance().getStartDirectory(parent);
        CustomFile folder=new CustomFile(name,parent);
        if(!parent.canWrite()|parent.isAndroidDirectory()){
            Uri uri=PermissionsHelper.getInstance().getUriFromSharedPreference(new File(start));
            DocumentFile documentFile=walkToFile(parent,DocumentFile.fromTreeUri(context,uri));
            documentFile.createDirectory(name);
        }else {
            folder.mkdirs();
        }
        return folder;
    }

    public static String mimeTypeFromUri(String uri){
        String extension= MimeTypeMap.getFileExtensionFromUrl(uri);
        if(extension!=null)
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return "";
    }

    public interface OnTaskComplete{
        void onComplete();
    }


}
