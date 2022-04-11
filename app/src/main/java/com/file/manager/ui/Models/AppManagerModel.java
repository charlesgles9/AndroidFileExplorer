package com.file.manager.ui.Models;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.file.manager.utils.DateUtils;
import com.file.manager.utils.DiskUtils;

import java.io.File;

public class AppManagerModel {

    private PackageInfo info;
    private Drawable icon;
    private String size;
    private String date;
    private String name;
    private File file;

    public AppManagerModel(PackageInfo info, PackageManager packageManager){
        this.info=info;
        this.icon=info.applicationInfo.loadIcon(packageManager);
        this.file=new File(info.applicationInfo.publicSourceDir);
        this.size= DiskUtils.getInstance().getSize(file);
        this.date= DateUtils.getDateString(file.lastModified());
        this.name=info.applicationInfo.loadLabel(packageManager).toString();

    }

    public Drawable getIcon() {
        return icon;
    }

    public PackageInfo getInfo() {
        return info;
    }

    public String getSize() {
        return size;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }


}
