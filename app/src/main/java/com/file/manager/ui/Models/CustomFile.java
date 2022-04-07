package com.file.manager.ui.Models;


import com.file.manager.utils.DiskUtils;
import com.file.manager.utils.ThumbnailLoader;

import java.io.File;

public class CustomFile extends File {

    public boolean highlighted=false;
    private boolean selected=false;
    private LocalThumbnail localThumbnail;
    public int position=0;
    private FolderSizeModel folderSizeModel;
    public CustomFile(String path){
        super(path);
        folderSizeModel=new FolderSizeModel(this);
        localThumbnail= new LocalThumbnail(this);
    }
    public void setTempThumbnail(){
        ThumbnailLoader.setTemporaryThumbnail(localThumbnail);
    }

    public void setLocalThumbnail(LocalThumbnail localThumbnail) {
        this.localThumbnail = localThumbnail;
    }

    public boolean isHidden(){
        return getName().charAt(0)=='.';
    }

    public boolean isStartDirectory(){
        return DiskUtils.getInstance().isStartDirectory(getPath());
    }

    public boolean isAndroidDirectory(){
        String start=DiskUtils.getInstance().getStartDirectory(this)+"/Android";
        if(start.length()<getPath().length())
            return false;
        return getPath().regionMatches(0,start,0,start.length());
    }

    public CustomFile(String name, File parent){
        super(parent.getPath(),name);
        localThumbnail= new LocalThumbnail(this);
        folderSizeModel=new FolderSizeModel(this);
    }

    public FolderSizeModel getFolderSizeModel() {
        return folderSizeModel;
    }

    public LocalThumbnail getLocalThumbnail() {
        return localThumbnail;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void toggleSelect(){
        this.selected=!selected;
    }

    public boolean IsSelected(){
        return selected;
    }
    public String getExtension(){
          if(!getName().contains("."))
              return "";
        return getName().substring(getName().lastIndexOf("."));
    }
}
