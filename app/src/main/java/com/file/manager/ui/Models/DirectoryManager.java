package com.file.manager.ui.Models;

import android.content.Context;

import com.file.manager.ui.storage.FilterType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Stack;

public class DirectoryManager implements Serializable {

    private HashMap<String,Folder> directories= new HashMap<>();
    private Folder current;
    private FilterType type;
    public DirectoryManager(FilterType type){
        this.type=type;
    }

    public void put(final Folder folder){
        directories.put(folder.getPath(),folder);
        this.current=folder;
    }


    public void setCurrent(Folder current) {
        this.current = current;
    }

    public  Folder currentDir(){
        return current;
    }

    public Folder getDir(CustomFile file){
        return directories.get(file.getPath());
    }

    public Folder getDir(String path){
        return directories.get(path);
    }

    public  void createDir( Context context,CustomFile file){
        if(!contains(file)) {
            Folder folder = new Folder(context,file);
            folder.setType(type);
            put(folder);
        }else {
            put(directories.get(file));
        }
    }

    public boolean contains(CustomFile file){
       return directories.containsKey(file.getPath());
    }

    public boolean contains(String path){
        return directories.containsKey(path);
    }
    public void remove(CustomFile file){
        directories.remove(file.getPath());
    }
    public  Folder moveTo(Context context,CustomFile file){
        if(!directories.containsKey(file.getPath())){
            createDir(context,file);
        }else {
            put(directories.get(file.getPath()));
        }
        return currentDir();
    }

    public int size(){
        return directories.size();
    }


}
