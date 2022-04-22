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


    private void loadHeaders(Context context){
        SharedPreferences preferences=context.getSharedPreferences(key,Context.MODE_PRIVATE);
        Map<String,?> values=preferences.getAll();
        for(Map.Entry<String,?>value:values.entrySet()) {
            PlayListHeader header= new PlayListHeader(value.getKey(),value.getValue().toString());
            headers.add(header);
            header.loadChildren(context);
        }
    }

    private void deleteHeader(Context context, String key){
        SharedPreferences preferences=context.getSharedPreferences(key,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.remove(key);
        editor.apply();
        remove(key);
    }

    private void deleteHeader(Context context, PlayListHeader header){
        SharedPreferences preferences=context.getSharedPreferences(header.getKey(),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.remove(header.getKey());
        editor.apply();
        remove(header);
    }

    public void remove(String key){
        for(int i=0;i<headers.size();i++){
            if(headers.get(i).getKey().equals(key)){
                headers.remove(headers.get(i));
                break;
            }
        }
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
