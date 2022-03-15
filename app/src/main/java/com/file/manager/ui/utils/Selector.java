package com.file.manager.ui.utils;
import java.io.Serializable;
import java.util.ArrayList;

public class Selector implements Serializable {

    private ArrayList<Object>list=new ArrayList<>();
    private static Selector instance=new Selector();
    private Selector(ArrayList<Object>list){
        this.list.addAll(list);
    }
    private Selector(){

    }
    public void clear(){
        list.clear();
    }

    public  Object get(int index){
        return list.get(index);
    }
    public ArrayList<Object> getList() {
        return list;
    }

    public void addAll(ArrayList<Object>array){
        list.addAll(array);
    }

    public void add(Object obj){
        list.add(obj);
    }

    public void remove(Object obj){
        list.remove(obj);
    }

    public Selector createNewInstance(){
        return new Selector(list);
    }
    public static Selector getInstance() {
        return instance;
    }
}
