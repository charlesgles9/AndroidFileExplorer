package com.file.manager.ui.Models;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.storage.StorageManager;

import androidx.annotation.RequiresApi;

import com.file.manager.R;

import java.io.File;
import java.io.IOException;

public class PackageModel {

    private Boolean isSelected=false;
    private ApplicationInfo applicationInfo;
    private Drawable drawable=null;
    private Context context;
    final StorageStatsManager storageStatsManager;

    public PackageModel(ApplicationInfo applicationInfo,StorageStatsManager storageStatsManager,Context context){
        this.applicationInfo=applicationInfo;
        this.context=context;
        this.storageStatsManager=storageStatsManager;
    }



    public long getPackageSizeLong(){
        File file= new File(applicationInfo.publicSourceDir);

        /*StorageStats stats= null;
        try {
            stats = storageStatsManager.queryStatsForUid(applicationInfo.storageUuid,0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long cacheSize = stats.getCacheBytes();*/
        return file.length();
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public Drawable getDrawable(PackageManager packageManager){
        if(drawable!=null)
            return drawable;
        try {
            drawable=packageManager.getApplicationIcon(applicationInfo.packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        return drawable;
    }
    public Boolean getSelected() {

        return isSelected;
    }

    public ApplicationInfo getApplicationInfo() {
        return applicationInfo;
    }
}
