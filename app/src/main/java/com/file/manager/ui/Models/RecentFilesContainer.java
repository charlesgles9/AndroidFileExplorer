package com.file.manager.ui.Models;
import com.file.manager.utils.DateUtils;
import com.file.manager.utils.FileFilters;
import com.file.manager.utils.Sorter;

import java.util.ArrayList;
import java.util.HashMap;


public class RecentFilesContainer  {

    private HashMap<String,RecentFileModel> map= new HashMap<>();
    private ArrayList<RecentFileModel> array= new ArrayList<>();
    public RecentFilesContainer(){

    }

    public void clear(){
        map.clear();
        array.clear();
    }


    public boolean isEmpty(){
        return array.isEmpty();
    }

    public void sort(){
        Sorter.sortRecentByDate(array);
    }
    public void add(final CustomFile file){
        file.setTempThumbnail();
        String type="";
        if(FileFilters.isImage(file.getName().toLowerCase())){
            type="image";
        }else if(FileFilters.isVideo(file.getName().toLowerCase())){
            type="video";
        }
        final String lastModified=DateUtils.getDateString(file.lastModified());
        final String id= file.getParentFile().getName()+type+lastModified;
        if(map.containsKey(id)){
            RecentFileModel model=map.get(id);
            model.add(file);
        }else {
            final RecentFileModel model= new RecentFileModel();
            model.add(file);
            map.put(id,model);
            array.add(model);
        }

    }

    public ArrayList<RecentFileModel> getArray() {
        return array;
    }

    public int size(){
        return array.size();
    }
}
