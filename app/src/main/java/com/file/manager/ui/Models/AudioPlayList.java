package com.file.manager.ui.Models;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AudioPlayList {

    private List<PlayListHeader>headers;
    private String key="PlayListHeaders";
    public AudioPlayList(){
        this.headers= new ArrayList<>();
    }


    public void loadHeaders(Context context){
        SharedPreferences preferences=context.getSharedPreferences(key,Context.MODE_PRIVATE);
        Map<String,?> values=preferences.getAll();
        for(Map.Entry<String,?>value:values.entrySet()) {
            PlayListHeader header= new PlayListHeader(value.getKey(),value.getValue().toString());
            headers.add(header);
            header.loadChildren(context);
        }
    }


    public void addHeader(Context context,String key,String date){
        SharedPreferences preferences=context.getSharedPreferences(this.key,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString(key,date);
        editor.apply();
        headers.add(new PlayListHeader(key,date));
    }

    public void deleteHeader(Context context, String key){
        SharedPreferences preferences=context.getSharedPreferences(this.key,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.remove(key);
        editor.apply();
        remove(key);
    }

    public void deleteHeader(Context context, int position){
        deleteHeader(context, headers.get(position));
    }

    public void deleteHeader(Context context, PlayListHeader header){
        SharedPreferences preferences=context.getSharedPreferences(key,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.remove(header.getKey());
        editor.apply();
        remove(header.getKey());
    }

    public void remove(String key){
        for(PlayListHeader header:headers){
            if(header.getKey().equals(key)){
                headers.remove(header);
                break;
            }
        }
    }

    public PlayListHeader get(int position){
        return headers.get(position);
    }

    public int size(){
        return headers.size();
    }
    public void remove(PlayListHeader header){
       headers.remove(header);
    }
    public List<PlayListHeader> getHeaders() {
        return headers;
    }

    public boolean isEmpty(){
        return headers.isEmpty();
    }

    public void clear(){
        headers.clear();
    }
}
