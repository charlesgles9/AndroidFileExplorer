package com.file.manager.ui.Models;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayListHierarchy {

    private SharedPreferences preferences;
    private String PrefName;
    private Context context;
    private HashMap<String,ArrayList<PlayListModel>>object;
    public PlayListHierarchy(Context context,String PrefName){
        this.preferences=context.getSharedPreferences(PrefName,Context.MODE_PRIVATE);
        this.PrefName=PrefName;
        this.object=new HashMap<>();
        this.context=context;
    }

    public void reset(){
        clear();
        fetchFilesFromPreference();
    }

    public void fetchFilesFromPreference(){
        Map<String,?> keys=preferences.getAll();
        SharedPreferences.Editor editor=preferences.edit();
        for(Map.Entry<String,?>entry:keys.entrySet()){
            String string=entry.getKey();
            SharedPreferences valuesPref=context.getSharedPreferences(string,Context.MODE_PRIVATE);
            ArrayList<PlayListModel> array= new ArrayList<>();
            PlayListModel keyModel=new PlayListModel(string,string);
            keyModel.setChild(false);
            object.put(keyModel.getName(),array);
            Map<String,?> values=valuesPref.getAll();
            for(Map.Entry<String,?>value:values.entrySet()){
                PlayListModel valueModel=new PlayListModel(value.getKey(),(String)value.getValue());
                valueModel.setChild(true);
                array.add(valueModel);
            }
        }
        editor.apply();
    }

    public void remove(String key){
        object.remove(key);
    }

    public void clear(){
        object.clear();
    }
    public ArrayList<String>getKeys(){
        return new ArrayList<>(object.keySet());
    }

    public ArrayList<PlayListModel> getArray(String key){
        return object.get(key);
    }

    public boolean isEmpty(){
        return object.isEmpty();
    }

    public void add(String parent,ArrayList<PlayListModel> models){
        ArrayList<PlayListModel>list=getArray(parent);
        if(list!=null){
            list.addAll(models);
        }else {
            object.put(parent,models);
        }
    }
    public static class PlayListModel{
        String name;
        String path;
        boolean Child;
        boolean selected;
        public PlayListModel(String name,String path){
            this.name=name;
            this.path=path;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setChild(boolean child) {
            Child = child;
        }

        public boolean isChild() {
            return Child;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }
    }
}
