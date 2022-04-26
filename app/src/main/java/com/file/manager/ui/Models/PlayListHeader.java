package com.file.manager.ui.Models;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayListHeader {

    private String key;
    private String date;
    private List<PlayListChild> childList;
    private boolean selected=false;
    public PlayListHeader(String key,String date){
        this.key=key;
        this.date=date;
        this.childList= new ArrayList<>();
    }


    public void addChild(Context context, File file){
        SharedPreferences preferences=context.getSharedPreferences(key,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString(file.getPath(),file.getName());
        childList.add(new PlayListChild(file.getPath()));
        editor.apply();
    }

    public void addChild(Context context, ArrayList<File> files){
        SharedPreferences preferences=context.getSharedPreferences(key,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        for (File file:files) {
            editor.putString(file.getPath(), file.getName());
            childList.add(new PlayListChild(file.getPath()));
        }
        editor.apply();
    }

    public void loadChildren(Context context){
        SharedPreferences preferences=context.getSharedPreferences(key,Context.MODE_PRIVATE);
        Map<String,?> values=preferences.getAll();
        SharedPreferences.Editor editor=preferences.edit();
        // stored files that were deleted or don't exit in memory
        ArrayList<PlayListChild>temp= new ArrayList<>();
        for(Map.Entry<String,?>value:values.entrySet()) {
         PlayListChild child=new PlayListChild(value.getKey());
         if(child.exists())
            childList.add(child);
            else
            temp.add(child);
        }
        // remove non-existent files that are available in the preference
        for(PlayListChild child :temp)
            editor.remove(child.getPath());
        editor.apply();
    }


    public void deleteChild(Context context,List<PlayListChild>children){
        for (PlayListChild child:children) {
            deleteChild(context, child);
           remove(child.getPath());
        }
    }

    public void remove(String path){
        for(PlayListChild child:childList){
            if(child.getPath().equals(path)){
                childList.remove(child);
                break;
            }
        }
    }
    public void deleteChild(Context context,PlayListChild child){
        SharedPreferences preferences=context.getSharedPreferences(key,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.remove(child.getPath());
        editor.apply();
        childList.remove(child);
    }

    public List<PlayListChild> getChildList() {
        return childList;
    }

    public PlayListChild get(int position){
        return childList.get(position);
    }
    public int size(){
        return childList.size();
    }
    public void setKey(String key) {
        this.key = key;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getDate() {
        return date;
    }

    public String getKey() {
        return key;
    }



}
