package com.file.manager.ui.Models;

import com.file.manager.utils.FileFilters;

import java.io.File;
import java.io.FilenameFilter;

public class FolderSizeModel {

    private File folder;
    private int length=0;
    private FilenameFilter filter= FileFilters.Default(true);
    private int position;
    public FolderSizeModel(File folder){
        this.folder=folder;
    }

    public void initialize(){
        String[] files = folder.list(filter);
        if(files!=null)
        length=files.length;
    }

    public void setFilter(FilenameFilter filter) {
        this.filter = filter;
    }

    public FilenameFilter getFilter() {
        return filter;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getLength() {
        return length;
    }

    public int getPosition() {
        return position;
    }



}
