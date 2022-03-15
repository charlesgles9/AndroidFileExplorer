package com.file.manager.ui.utils;


import com.file.manager.ui.Models.CustomFile;

import java.util.ArrayList;

public class CutHelper {
    private ArrayList<CustomFile> data= new ArrayList<>();
    private CustomFile destination;
    private static CutHelper instance=new CutHelper();
    private CustomFile parent;
    private CutHelper(){

    }

    public void add(ArrayList<CustomFile>files){
        this.data.addAll(files);
    }

    public boolean isEmpty(){
        return data.isEmpty();
    }
    public boolean contains(String path){
        for(CustomFile file:data){
            if(file.getPath().equals(path)){
                return true;
            }
        }
        return false;
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

    public static CutHelper getInstance() {
        return instance;
    }

    public void setParent(CustomFile parent) {
        this.parent = parent;
    }

    public CustomFile getParent() {
        return parent;
    }

    public void reset(){
        data.clear();
        destination=null;
        parent=null;
    }
}
