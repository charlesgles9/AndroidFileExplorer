package com.file.manager.ui.Models;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.file.manager.R;
import com.file.manager.helpers.PermissionsHelper;
import com.file.manager.ui.storage.FilterType;
import com.file.manager.ui.storage.SortBy;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.FileFilters;
import com.file.manager.ui.utils.FileHandleUtil;
import com.file.manager.ui.utils.Sorter;
import com.file.manager.ui.utils.ThumbnailLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class Folder {



    private CustomFile file;
    private ArrayList<CustomFile>files= new ArrayList<>();
    private ArrayList<CustomFile>multiSelectedFiles= new ArrayList<>();
    private int adapterPosition=0;
    private FilterType type=FilterType.DEFAULT;
    private SharedPreferences preferences;
    private Context context;
    private Folder parent;
    private SortBy sortBy=SortBy.AZ;
    private MutableLiveData<String> message=new MutableLiveData<>();
    private ThumbnailLoader<ArrayList<CustomFile>> loader;
    private int position=0;
    public Folder(Context context,CustomFile file){
        this.file=file;
        this.preferences=context.getSharedPreferences("MyPref",Context.MODE_PRIVATE);
        this.context=context;
        this.message.setValue("");
    }


    @SuppressLint("StaticFieldLeak")
    public void init(final FileHandleUtil.OnTaskComplete onTaskComplete){
            LoadFilesTask loadFilesTask = new LoadFilesTask(onTaskComplete);
            loadFilesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setType(FilterType type) {
        this.type = type;
    }

    public FilterType getType() {
        return type;
    }

    public ArrayList<CustomFile>getFiles(){
        return files;
    }

    public void add(ArrayList<CustomFile>data){
        for(CustomFile file:data)
            if(!containsName(file.getName()))
           this.files.add(file);
        sort();
    }

    public void add(CustomFile file){
        this.files.add(file);
        sort();
    }

    public CustomFile get(int position){
        return files.get(position);
    }

    public CustomFile getByName(String name){
        for(CustomFile file:files){
            if(file.getName().equals(name)){
                return file;
            }
        }
        return null;
    }
    public int size(){
        return getFiles().size();
    }

    public String getName(){
        return file.getName();
    }

    public String getPath(){
        return file.getPath();
    }


    public void searchFile(String name,onSearchComplete onSearchComplete){
        SearchFilesTask task= new SearchFilesTask(name,onSearchComplete);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private ArrayList<Integer> searchFileByName(String name){
        ArrayList<Integer>positions= new ArrayList<>();
        // remove all highlights
        resetSearchHighlights();
        if(name==null||name.equals(""))
            return positions;
        for(int i=0;i<files.size();i++){
            CustomFile file=files.get(i);
            file.highlighted=file.getName().toLowerCase().contains(name.toLowerCase());
            if(file.highlighted)
                positions.add(i);
        }
        return positions;
    }


    public void resetSearchHighlights(){
        for(int i=0;i<files.size();i++)
            files.get(i).highlighted=false;
    }


    public void loadThumbnails(int start, int stop, int width, int height, ArrayList< CustomFile>files,ThumbnailLoader.onThumbnailComplete onThumbnailComplete){
        boolean showThumbnail=!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("thumbnail",true);
        loader = new ThumbnailLoader<>(files,context);
        loader.setWidth(width);
        loader.setHeight(height);
        loader.setDefaultOnly(showThumbnail);
        loader.setPoints(start, stop);
        loader.setOnThumbnailLoadedListener(onThumbnailComplete);
        loader.ExecuteTask();

    }

    public void addToMultiSelectList(int position){
        multiSelectedFiles.add(files.get(position));
    }


    public void removeFromMultiSelectList(CustomFile file){
        multiSelectedFiles.remove(file);
    }

    public CustomFile getFile() {
        return file;
    }

    private boolean selectAll=true;
    public boolean getSelectAllStatus(){
        return selectAll;
    }
    public void addAllToMultiselectList(){
        multiSelectedFiles.clear();
        for(CustomFile file:files){
            file.setSelected(selectAll);
            multiSelectedFiles.add(file);
        }
        if(!selectAll)
            multiSelectedFiles.clear();
        this.selectAll=!selectAll;
    }
    public void resetMultiSelectedList(){
        for(CustomFile file:multiSelectedFiles){
            file.setSelected(false);
        }
        multiSelectedFiles.clear();
        selectAll=true;
    }
    public ArrayList<CustomFile>getMultiSelectedFiles(){
        return multiSelectedFiles;
    }

    public void removeDeleted(){

        for(int i=0;i<files.size();i++){
            if(!files.get(i).exists())
                files.remove(i);
        }
    }

    public void removeDeleted(ArrayList<CustomFile>data){
        for(CustomFile file1:data) {
            for (int i = 0; i < files.size(); i++) {
                CustomFile file2=files.get(i);
                if(file1.getPath().equals(file2.getPath())){
                    files.remove(i);
                }
            }
        }
    }

    public void setSortBy(SortBy sortBy) {
        this.sortBy = sortBy;
    }

    public void sort(){
        switch (sortBy){
            case AZ:
                Sorter.AtoZ(files);
                break;
            case DATE:
                Sorter.sortByDate(files);
                break;
            case SIZE:
                Sorter.sortBySize(files);
                break;
            case EXTENSION:
                Sorter.sortByExtension(files);
                break;
        }
    }

    public SortBy getSortBy() {
        return sortBy;
    }

    public void replace(CustomFile old, CustomFile file){
        files.remove(old);
        files.add(file);
        sort();
    }

    // test for name conflicts
   public boolean containsName(String name){
        for(CustomFile file:files){
            if(file.getName().equals(name)){
                return true;
            }
        }
        return false;
   }

   // test if the array of files are contained within this folder
   public ArrayList<CustomFile> containsName(ArrayList<CustomFile> data){
        ArrayList<CustomFile>conflicts=new ArrayList<>();
       for(CustomFile a:data){
          if(containsName(a.getName())){
              conflicts.add(a);
          }
       }
       return conflicts;
   }
    public void remove(CustomFile file){
        files.remove(file);
    }

    public void remove(ArrayList<CustomFile>list){
        files.removeAll(list);
    }

    public void setAdapterPosition(int adapterPosition) {
        this.adapterPosition = adapterPosition;
    }

    public int getAdapterPosition() {
        return adapterPosition;
    }


    @SuppressLint("StaticFieldLeak")
    class SearchFilesTask extends AsyncTask<String,Integer,String>{
        private String name;
        private ArrayList<Integer>positions= new ArrayList<>();
        private onSearchComplete onComplete;

        public SearchFilesTask(String name,onSearchComplete onComplete){
            this.name=name;
            this.onComplete=onComplete;
        }
        @Override
        protected String doInBackground(String... strings) {
            positions=searchFileByName(name);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            onComplete.onComplete(positions);
            super.onPostExecute(s);
        }
    }

    public boolean isEmpty(){
        return files.isEmpty();
    }
    public void removeAll(){
        files.removeAll(files);
    }
    @SuppressLint("StaticFieldLeak")
    class LoadFilesTask extends AsyncTask<String,Integer,String>{

        private FileHandleUtil.OnTaskComplete onTaskComplete;
        public LoadFilesTask(FileHandleUtil.OnTaskComplete onTaskComplete){
            this.onTaskComplete=onTaskComplete;

        }

        private void listVideos(FilenameFilter filterFile) throws Exception {
            if (!DiskUtils.getInstance().isStartDirectory(file.getPath()))
                FileHandleUtil.ListFiles(context,file, files, filterFile);
            else
                FileHandleUtil.fetchVideoFilesGallery(context,files);
        }

        private void listImages(FilenameFilter filter) throws Exception {
            if(!DiskUtils.getInstance().isStartDirectory(file.getPath()))
                FileHandleUtil.ListFiles(context,file, files, filter);
             else
                FileHandleUtil.fetchImageFilesGallery(context,files);
        }



        private void listAudio() throws Exception{
            FileHandleUtil.fetchAudioFiles(context,files);
        }
     private void LoadLargeFiles(){
         files.clear();
         DiskUtils diskUtils=DiskUtils.getInstance();
         SharedPreferences preferences=context.getSharedPreferences("MyPref",Context.MODE_PRIVATE);
         message.postValue("Filtering files....");
         long bytes=preferences.getLong("MinLargeFile",diskUtils.SIZE_MB*50);
         FileHandleUtil.ListLargeFilesRecursively(file,files,FileFilters.FilterLargeFiles(bytes));
        }

        @Override
        protected String doInBackground(String... strings) {
            String storage=DiskUtils.getInstance().getStartDirectory(file);
                switch (type) {
                    case DEFAULT:
                        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(context);
                        boolean  showHidden=preferences.getBoolean("hidden",false);
                      if(file.canRead()) {
                           try {
                               FileHandleUtil.ListFiles(context,file, files,FileFilters.Default(showHidden));
                         }catch (NullPointerException ne) {
                               if (preferences.contains(storage))
                                   FileHandleUtil.SAFListFiles(context, file,
                                           PermissionsHelper.getInstance().getUriFromSharedPreference(new File(storage)), files);
                           }
                      }
                        break;
                    case IMAGE:
                        try {
                            listImages(FileFilters.FilesOnlyImages());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case AUDIO:
                        try {
                            listAudio();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        sortBy=SortBy.EXTENSION;
                        break;
                    case VIDEO:
                        try {
                            listVideos(FileFilters.FilesOnlyVideos());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case DOCUMENT:
                        sortBy=SortBy.EXTENSION;
                        FileHandleUtil.ListDocuments(files);
                        break;
                    case COMPRESSED:
                        FileHandleUtil.ListCompressed(files);
                        break;
                    case APPLICATION:
                        FileHandleUtil.ListApplication(files);
                        break;
                    case FOLDERS:
                        FileHandleUtil.ListFiles(context,file,files,FileFilters.FoldersOnly());
                        for(CustomFile file:files){
                            file.getLocalThumbnail().setThumbnail(R.drawable.ic_folder_icon);
                            file.getLocalThumbnail().setLoaded(true);
                        }
                        break;
                    case LARGEFILES:
                        LoadLargeFiles();
                        break;
                }
            if(type.equals(FilterType.LARGEFILES)){
                message.postValue("Sorting Files...");
                Sorter.sortBySize(files);
            }
            else{
                sort();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            onTaskComplete.onComplete();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }


    public void setParent(Folder parent) {
        this.parent = parent;
    }

    public Folder getParent() {
        return parent;
    }

    public LiveData<String> getMessage(){
        return message;
    }
    public interface onSearchComplete{

        void onComplete(ArrayList<Integer>positions);
    }
}
