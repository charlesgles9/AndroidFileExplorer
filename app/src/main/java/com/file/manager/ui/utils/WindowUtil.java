package com.file.manager.ui.utils;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.file.manager.Tools.AppManagerFragment;
import com.file.manager.Tools.DuplicateFileFragment;
import com.file.manager.Tools.LargeFileFragment;
import com.file.manager.WindowState;
import com.file.manager.ui.Models.WindowModel;
import com.file.manager.ui.home.GlobalSearchFragment;
import com.file.manager.ui.home.HomeFragment;
import com.file.manager.ui.storage.FilterType;
import com.file.manager.ui.storage.StorageFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class WindowUtil {

    private static WindowUtil instance=new WindowUtil();
    private HashMap<Integer, WindowModel> fragmentHashMap= new LinkedHashMap<>();
    private HashMap<String,Integer> activeFragment= new HashMap<>();
    private WindowModel current=null;
    private WindowUtil(){

    }

    public void put(Fragment fragment, String type, View view){
        WindowModel windowModel;
        WindowState state=(WindowState)fragment;
        int fragmentId=state.getFragmentID();
        if(!state.isDeleted()) {
            if (fragmentHashMap.containsKey(fragmentId)) {
                windowModel = fragmentHashMap.get(fragmentId);
            } else {
                windowModel = new WindowModel(fragmentId, fragment);
                fragmentHashMap.put(fragmentId, windowModel);

            }
            windowModel.setView(view);
            if(type.equals("Internal"))
            activeFragment.put("Internal",fragmentId);
            else if(type.equals("External"))
                activeFragment.put("External",fragmentId);
            else activeFragment.put(type,fragmentId);
        }

    }

    public ArrayList<WindowModel>getAsListModel(){
        final ArrayList<WindowModel> list= new ArrayList<>();
        for (Map.Entry<Integer, WindowModel> entry : fragmentHashMap.entrySet()) {
            list.add(entry.getValue());

        }
        return list;
    }

    public boolean remove(WindowModel model){
        boolean flag=false;
        for (Map.Entry<Integer, WindowModel> entry : fragmentHashMap.entrySet()) {
            if(entry.getValue().equals(model)){
              WindowState state=(WindowState) entry.getValue().getFragment();
              state.setDeleted(true);
                fragmentHashMap.remove(entry.getKey());
              flag=true;
              break;
            }
        }

          String key="";
          for (Map.Entry<String,Integer> entry:activeFragment.entrySet()){
              if(entry.getValue()==model.getId()){
                  key=entry.getKey();
                  break;
              }
          }
        activeFragment.remove(key);

        return flag;
    }

    public void resetHighlights(){
        ArrayList<WindowModel>array=getAsListModel();
        for(WindowModel model:array){
            model.setActive(false);
        }
    }

    public boolean isEmpty(){
        return fragmentHashMap.isEmpty();
    }
    public void setCurrent(int id){
        current=get(id);
    }

    public boolean contains(int id){
        return get(id)!=null;
    }
    public WindowModel getCurrent(){
        return current;
    }
    public WindowModel get(int id){
        return fragmentHashMap.get(id);
    }

    public static WindowUtil getInstance() {
        return instance;
    }
    public void clear(){
     instance= new WindowUtil();
    }
    public Fragment getActiveFragment(String type){
        if(!activeFragment.containsKey(type))
            return null;
        return get(activeFragment.get(type)).getFragment();
    }
}
