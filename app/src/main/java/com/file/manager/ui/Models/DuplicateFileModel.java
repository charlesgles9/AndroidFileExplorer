package com.file.manager.ui.Models;

import java.util.ArrayList;

public class DuplicateFileModel {

    private ArrayList<CustomFile>array= new ArrayList<>();
    private ArrayList<CustomFile>deleted= new ArrayList<>();
    private boolean displayItems;
    private boolean checked;
    public DuplicateFileModel(){

    }

    public void selectOldest(){
        for(int i=1;i<array.size();i++){
            array.get(i).setSelected(true);
        }
        array.get(0).setSelected(false);
    }

    public void selectNewest(){
        for(int i=0;i<array.size()-1;i++){
            array.get(i).setSelected(true);
        }
        array.get(array.size()-1).setSelected(false);
    }

    public void resetSelect(){
        for(int i=0;i<array.size();i++){
            array.get(i).setSelected(false);
        }
    }
    public void add(CustomFile file){
        array.add(file);
        file.setSelected(false);
    }

    public void removeDeleted(){

        for(int i=0;i<deleted.size();i++){
          if(!deleted.get(i).exists())
             array.remove(deleted.get(i));
        }
        deleted.clear();
    }

    public void addDeleted(CustomFile file) {
        this.deleted.add(file);
    }

    public int size(){
        return array.size();
    }
    public ArrayList<CustomFile> getArray() {
        return array;
    }
    public void setDisplayItems(boolean displayItems) {
        this.displayItems = displayItems;
    }

    public boolean isDisplayItems() {
        return displayItems;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
