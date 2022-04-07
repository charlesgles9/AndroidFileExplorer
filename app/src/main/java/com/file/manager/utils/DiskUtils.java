package com.file.manager.utils;

import android.content.Context;
import android.os.StatFs;

import com.file.manager.ui.Models.CustomFile;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class DiskUtils {

    public static long SIZE_KB=1024L;
    public static long SIZE_MB=SIZE_KB*SIZE_KB;
    public static long SIZE_GB=SIZE_MB*SIZE_KB;
    private static File[] dirs;
    private static DiskUtils instance=null;
    private DecimalFormat format= new DecimalFormat("#.##");
    private DiskUtils(Context context){
         dirs=context.getExternalFilesDirs(null);
         for(int i=0;i<dirs.length;i++){
             File file=dirs[i];
             if(file!=null)
             dirs[i]=new File(file.getPath().substring(0,file.getPath().indexOf("Android")));
         }
    }


    public boolean isStartDirectory(String dir){
        for (File file : dirs) {
            if(file!=null)
            if (file.getPath().equals(dir))
                return true;
        }return false;
    }

    public boolean isInternalStorage(String path){
        return dirs[0].getPath().equals(path);
    }

    public String getStorage(String name){
        for(File file:dirs){
            if(file!=null&&file.getName().equals(name)){
                return file.getPath();
            }
        }
        return null;
    }

    public boolean isExternalSdCorrupt(){
        return dirs.length>1&&dirs[1]==null;
    }
    public  long totalMemory(File file){
        StatFs statFs= new StatFs(file.getAbsolutePath());
        return (statFs.getBlockCountLong()*statFs.getBlockSizeLong());
    }

    public  long freeMemory(File file){
        StatFs statFs=new StatFs(file.getAbsolutePath());
        return (statFs.getAvailableBlocksLong()*statFs.getBlockSizeLong());
    }

    public  long usedMemory(File file){
        long total=totalMemory(file);
        long free=freeMemory(file);
        return total-free;
    }


    public  int getFreeStorageInPercent(File file){
        float used =usedMemory(file);
        float total=totalMemory(file);
        return (int)(used*100.0f/total);
    }

    public  String getFreeStorageString(File file){
      return getSize(freeMemory(file));

    }
    public  String getUsedStorageString(File file){
       return getSize(usedMemory(file));
    }

    public  String getTotalStorageString(File file){
       return getSize(totalMemory(file));
    }

    public   String getSize(long bytes){
        if(bytes<SIZE_KB )
            return bytes+" bytes";
        if(bytes < SIZE_MB)
            return format.format(bytes/(float)SIZE_KB)+" Kb";
        if(bytes < SIZE_GB)
            return format.format((float)bytes/(float)SIZE_MB)+" Mb";
        return format.format((float)bytes/(float)SIZE_GB)+" Gb";

    }
    public   String getSizeRounded(long bytes){
        if(bytes<SIZE_KB )
            return bytes+" bytes";
        if(bytes < SIZE_MB)
            return format.format(Math.round(bytes/(float)SIZE_KB))+" Kb";
        if(bytes < SIZE_GB)
            return format.format(Math.round(bytes/(float)SIZE_MB))+" Mb";
        return format.format(Math.round(bytes/(float)SIZE_GB))+" Gb";

    }
    public  File getDirectory(int index){
        if(index>=dirs.length)
            return null;
        return dirs[index];
    }

    public  String getDirectoryPath(int index){
        if(index>=dirs.length|dirs[index]==null)
            return null;
        return dirs[index].getPath();
    }

    public File[] getStorageDirs(){
        return dirs;
    }

    public String getStartDirectory(File file){
        for (File dir:dirs){
            if(file.getPath().contains(dir.getPath())){
                return dir.getPath();
            }
        }
        return null;
    }
    public String getFolderSize(CustomFile folder){
        ArrayList<CustomFile>contents=
                FileHandleUtil.ListFilesRecursively(folder,FileFilters.Default(true));
        long bytes=0L;
        for(CustomFile file:contents){
            bytes+=file.length();
        }
        return getSize(bytes);
    }

    public String getSize(File file){
        return getSize(file.length());
    }
    public boolean isSpaceAvailable(File file){
        return freeMemory(file) > 0;
    }
    public boolean isSpaceEnough(File file,long bytes){
        return freeMemory(file) >= bytes;
    }
    public static void init(Context context){
        instance= new DiskUtils(context);
    }
    public static DiskUtils getInstance(){
        return instance;
    }
}
