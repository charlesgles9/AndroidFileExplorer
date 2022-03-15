package com.file.manager.ui.utils;

import com.file.manager.ui.Models.CustomFile;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class FileFilters {

    private FileFilters(){}

    public static String getExtension(String name){
        if(!name.contains("."))
            return "";
        return name.substring(name.lastIndexOf("."));
    }

    public static boolean isImage(String name){
        return  getExtension(name).equalsIgnoreCase(".jpg")|
                getExtension(name).equalsIgnoreCase(".jpeg")|
                getExtension(name).equalsIgnoreCase(".png")|
                getExtension(name).equalsIgnoreCase(".bmp")|
                getExtension(name).equalsIgnoreCase(".svg")|
                getExtension(name).equalsIgnoreCase(".webp")|
                getExtension(name).equalsIgnoreCase(".gif");
    }

    public static boolean isVideo(String name){
        return getExtension(name).equalsIgnoreCase(".mkv")|
                getExtension(name).equalsIgnoreCase(".vob")|
                getExtension(name).equalsIgnoreCase(".mp4")|
                getExtension(name).equalsIgnoreCase(".3gp");
    }

    public static boolean isCompressed(String name){
        return getExtension(name).equalsIgnoreCase(".zip");
    }

    public static boolean isApk(String name){
        return getExtension(name).equalsIgnoreCase(".apk");
    }
    public static boolean isDocument(String name){
        boolean flag=false;
        switch (getExtension(name).toLowerCase()){
            case ".txt":
            case ".pdf":
            case ".xls":
            case ".doc":
            case ".docx":
            case ".xml":
                flag=true;
                break;
        }
        return flag;
    }

    public static boolean isAudio(String name){
       return getExtension(name).equalsIgnoreCase(".mp3")|
                getExtension(name).equalsIgnoreCase(".ogg");
    }

    public static FilenameFilter FilterLargeFiles(final long bytes){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                CustomFile file=new CustomFile(name,dir);
                if(file.isHidden())
                    return false;
                return file.length()>=bytes|file.isDirectory();
            }
        };
    }
    public static FilenameFilter FilterFolders(final int level){
      return new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
              File file=new File(dir,name);

              if(file.isDirectory()){
                  if(file.getName().equals("Android"))
                      return false;
                  String []arr=file.getPath().split("/");
                  if(arr!=null)
                          return arr.length<=level;
              }
              return false;
          }
      };
    }
    public static FilenameFilter FilterFoldersLargeFiles(final int level){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File file=new File(dir,name);

                if(file.isDirectory()){
                    String []arr=file.getPath().split("/");
                    if(arr!=null)
                        return arr.length<=level;
                }
                return false;
            }
        };
    }
    public static FilenameFilter FilterFoldersImageGallery(){

        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                CustomFile file=new CustomFile(name,dir);
                if(file.isHidden())
                    return false;
                if(file.isDirectory()){

                    return   !file.isAndroidDirectory();
                }


                return isImage(name)|file.isDirectory();
            }
        };
    }

    public static FilenameFilter FilterFoldersVideoGallery(){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                CustomFile file=new CustomFile(name,dir);
                if(file.isHidden())
                    return false;
                if(file.isDirectory()){
                    return !file.isAndroidDirectory();
                }
                return isVideo(name)|file.isDirectory();
            }
        };
    }

    public static FilenameFilter FilterFoldersAudioGallery(){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                CustomFile file=new CustomFile(name,dir);
                if(file.isHidden())
                    return false;
                if(file.isDirectory()){
                    return !file.isAndroidDirectory();
                }
                return isAudio(name)|file.isDirectory();
            }
        };
    }

    public static FilenameFilter FilterFoldersDocumentGallery(final int level){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                CustomFile file=new CustomFile(name,dir);
                if(file.isHidden())
                    return false;
                if(file.isDirectory()){
                    String []arr=file.getPath().split("/");
                    return arr.length<=level&!file.isAndroidDirectory();
                }
                return isDocument(name)|file.isDirectory();
            }
        };
    }
    public static FilenameFilter FilterFoldersCompressedGallery(final int level){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                CustomFile file=new CustomFile(name,dir);
                if(file.isHidden())
                    return false;
                if(file.isDirectory()){
                    String []arr=file.getPath().split("/");
                    return arr.length<=level&!file.isAndroidDirectory();
                }
                return isCompressed(name)|file.isDirectory();
            }
        };
    }
    public static FilenameFilter FilterFoldersApplicationsGallery(final int level){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                CustomFile file=new CustomFile(name,dir);
                if(file.isHidden())
                    return false;
                if(file.isDirectory()){
                    String []arr=file.getPath().split("/");
                    return arr.length<=level&!file.isAndroidDirectory();
                }
                return isApk(name)|file.isDirectory();
            }
        };
    }


    public static FilenameFilter FilterMediaFiles(){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                CustomFile file = new CustomFile(name, dir);
                if (file.isHidden())
                    return false;
                if (file.isDirectory())
                    return !file.isAndroidDirectory();
                return isVideo(name) | isImage(name);
            }
        };
    }
    // filters out folders and displays only images for our gallery
    public static FilenameFilter FilesOnlyImages(){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {

                CustomFile file=new CustomFile(name,dir);
                return isImage(name)&file.isFile();
            }
        };
    }

    // filters out folders and displays only videos for our gallery
    public static FilenameFilter FilesOnlyVideos(){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return isVideo(name);
            }
        };
    }

    public static FilenameFilter FoldersOnly(){
        return  new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {

                File file= new File(dir,name);

                return file.isDirectory();
            }
        };
    }

    public static FilenameFilter ApkOnly(){
        return  new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                CustomFile file=new CustomFile(name,dir);
                return isApk(name);
            }
        };
    }

    public static FilenameFilter VideosOnly(){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return isVideo(name) ;
            }
        };
    }

    public static FilenameFilter ImagesOnly(){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return isImage(name) ;
            }
        };
    }

    public static FilenameFilter DocumentsOnly(){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean flag=false;
                switch (getExtension(name).toLowerCase()){
                    case ".txt":
                    case ".pdf":
                    case ".xls":
                    case ".doc":
                    case ".docx":
                    case ".xml":
                     flag=true;
                     break;
                }
                return flag;
            }
        };
    }

    public static FilenameFilter QueryFilesByName(final String str){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().contains(str.toLowerCase());
            }
        };
    }

    public static FilenameFilter AudioOnly(){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return isAudio(name);
            }
        };
    }

    public static FilenameFilter CompressedOnly(){
        return  new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return getExtension(name).equalsIgnoreCase(".zip")|
                       getExtension(name).equalsIgnoreCase(".7z") ;
            }
        };
    }

    public static FilenameFilter Default(final boolean showHidden){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {

                if(new File(dir,name).isHidden()&!showHidden)
                    return false;
                return true;
            }
        };
    }


}
