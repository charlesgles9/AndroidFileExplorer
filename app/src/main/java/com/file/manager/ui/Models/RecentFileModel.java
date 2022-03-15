package com.file.manager.ui.Models;


import java.util.ArrayList;

public class RecentFileModel{

    private ArrayList<CustomFile>files= new ArrayList<>();
    private CustomFile parent;
    public RecentFileModel(){

    }

     public void add(CustomFile file){
        if(files.size()>=4)
            return;
         files.add(file);
         file.setTempThumbnail();
        if(parent==null) {
            parent = new CustomFile(file.getParent());
            parent.getLocalThumbnail().setPaddingRadius(3);
            parent.setTempThumbnail();
        }
     }

     public long getLastModified(){
        return files.get(0).lastModified();
     }

    public CustomFile getParent() {
        return parent;
    }

    public CustomFile get(int position){
        return files.get(position);
     }

    public ArrayList<CustomFile> getFiles() {
        return files;
    }

    public int size(){
        return files.size();
    }


}
