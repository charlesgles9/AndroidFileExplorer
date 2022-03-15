package com.file.manager.ui.Models;
import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.MutableLiveData;

import com.file.manager.OnTaskCompleteListener;
import com.file.manager.ui.utils.FileFilters;
import com.file.manager.ui.utils.FileHandleUtil;
import com.file.manager.ui.utils.MD5;
import com.file.manager.ui.utils.Sorter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DuplicateFileContainer {

    private HashMap<String,DuplicateFileModel>map;
    private MutableLiveData<String>message=new MutableLiveData<>();
    private CustomFile dir;
    private Context context;
    public static final byte IMAGE=0;
    public static final byte VIDEO=1;
    public static final byte AUDIO=2;
    public static final byte ALL_MEDIA=3;
    public static final byte DOCUMENT=4;
    public static final byte COMPRESSED=5;
    private byte mode=3;
    public DuplicateFileContainer(CustomFile dir, Context context){
        this.map= new HashMap<>();
        this.dir=dir;
        this.context=context;
    }

    private BackgroundTask task;
    public void searchForDuplicates(OnTaskCompleteListener onTaskCompleteListener){
      task= new BackgroundTask(onTaskCompleteListener);
      task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void cancel(){
     if(task!=null)
        task.cancel(true);
    }
    public void put(CustomFile value){
        final String key= MD5.fileToMD5Fast(value.getPath());
        DuplicateFileModel model=map.get(key);
        if(model!=null){
            model.add(value);
        }else {
            model= new DuplicateFileModel();
            model.add(value);
            map.put(key,model);
        }
    }
    private ArrayList<DuplicateFileModel>array=new ArrayList<>();
    public ArrayList<DuplicateFileModel> getArray(){
        return array;
    }

    public void clear(){
        map.clear();
        getArray().clear();
    }

    public void setDir(CustomFile dir) {
        this.dir = dir;
    }

    public CustomFile getDir() {
        return dir;
    }

    public void setMode(byte mode) {
        this.mode = mode;
    }

    public byte getMode() {
        return mode;
    }

    public int size(){
        return array.size();
    }
    class BackgroundTask extends AsyncTask<String,Integer,String>{

        private OnTaskCompleteListener onTaskCompleteListener;
        public BackgroundTask(OnTaskCompleteListener onTaskCompleteListener){
            this.onTaskCompleteListener=onTaskCompleteListener;
        }
        @Override
        protected String doInBackground(String... strings) {
            final ArrayList<CustomFile>files= new ArrayList<>();
            message.postValue("Fetching files...");
            switch (mode) {
                case IMAGE:
                    FileHandleUtil.ListAllImagesFileAPI(dir,files);
                    break;
                case VIDEO:
                    FileHandleUtil.ListAllVideoFileAPI(dir,files);
                    break;
                case DOCUMENT:
                    FileHandleUtil.ListAllDocumentFileAPI(dir,files);
                    break;
                case COMPRESSED:
                    FileHandleUtil.ListAllCompressedFileAPI(dir,files);
                    break;
                case AUDIO:
                    FileHandleUtil.ListAllAudioFileAPI(dir,files);
                    break;
                default:
                    FileHandleUtil.ListAllMediaFileAPI(dir,files);
                    break;
            }

            message.postValue("Sorting files...");
            Sorter.sortByDate(files);
            message.postValue("Comparing files...");
            int fCount=files.size();
            int gCount=1;
            for(CustomFile file:files){
                if(!file.exists())
                    continue;
                message.postValue("Comparing files..."+String.valueOf((int)(((float)gCount/(float)fCount)*100))+" %");
                if(isCancelled()){
                    clear();
                    return null;
                }
                put(file);
                gCount++;
                file.setTempThumbnail();
            }

            message.postValue("Grouping Files...");
            for (Map.Entry<String, DuplicateFileModel> entry : map.entrySet()) {
                if(isCancelled()){
                    clear();
                    return null;
                }
                if (entry.getValue().size() > 1)
                    array.add(entry.getValue());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            onTaskCompleteListener.onTaskComplete();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    public MutableLiveData<String> getMessage() {
        return message;
    }
}
