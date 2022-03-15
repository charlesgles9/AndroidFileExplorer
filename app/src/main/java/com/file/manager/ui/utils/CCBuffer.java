package com.file.manager.ui.utils;

import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.Folder;

import java.util.ArrayList;
//cut copy buffer
public class CCBuffer {

    private ArrayList<CustomFile>files= new ArrayList<>();
    private CustomFile destination;
    private static CCBuffer instance= new CCBuffer();
    private boolean duplicate=false;
    private Folder source;
    private CCBuffer(){}

    public void setFiles(ArrayList<CustomFile>files){
        this.files.addAll(files);
    }

    public void setSource(Folder source){
        this.source=source;
    }

    public Folder getSource() {
        return source;
    }

    public ArrayList<CustomFile>getFiles(){
        return files;
    }

    public CustomFile getDestination() {
        return destination;
    }

    public void  setDestination(CustomFile destination){
        this.destination=destination;
    }

    public void reset(){
        files.clear();
        destination=null;
        source=null;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public boolean isEmpty(){
        return files.isEmpty();
    }
    public static CCBuffer getInstance() {
        return instance;
    }
}
