package com.file.manager.ui.utils;

import com.file.manager.ui.Models.CustomFile;

import java.util.ArrayList;

public class CopyHelper {
    private ArrayList<CustomFile> data= new ArrayList<>();
    private CustomFile destination;
    private static CopyHelper instance=new CopyHelper();
    private boolean deleteOldFiles=false;
    private CustomFile parent;
    private CopyHelper(){

    }

    public void add(ArrayList<CustomFile>files){
        this.data.addAll(files);
    }
    public void add(CustomFile file){
        this.data.add(file);
    }

    public boolean contains(String path){
        for(CustomFile file:data){
            if(file.getPath().equals(path)){
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty(){
        return data.isEmpty();
    }
    public ArrayList<CustomFile> getData() {
        return data;
    }

    public void setDestination(CustomFile destination) {
        this.destination = destination;
    }

    public CustomFile getDestination() {
        return destination;
    }

    public static CopyHelper getInstance() {
        return instance;
    }

    public void setParent(CustomFile parent) {
        this.parent = parent;
    }

    public CustomFile getParent() {
        return parent;
    }

    public  void reset(){
        data.clear();
        destination=null;
        parent=null;
        deleteOldFiles=false;
    }
    public void setDeleteOldFiles(boolean deleteOldFiles) {
        this.deleteOldFiles = deleteOldFiles;
    }

    public boolean isDeleteOldFiles() {
        return deleteOldFiles;
    }
}
