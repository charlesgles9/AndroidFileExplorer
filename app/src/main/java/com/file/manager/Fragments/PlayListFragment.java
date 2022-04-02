package com.file.manager.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.file.manager.R;
import com.file.manager.ui.Adapters.PlayListAdapter;
import com.file.manager.ui.Models.MusicHelperSingleton;
import com.file.manager.ui.Models.PlayListHierarchy;
import com.file.manager.ui.utils.Timer;


import java.io.IOException;
import java.util.ArrayList;

public class PlayListFragment extends Fragment {

    private RecyclerView playList;
    private View root;
    private View back;
    private TextView titleMessage;
    private PlayListAdapter adapter;
    private ArrayList<PlayListHierarchy.PlayListModel>array= new ArrayList<>();
    private PlayListHierarchy playListHierarchy;
    private MutableLiveData<String> mode= new MutableLiveData<>();
    private PlayListHierarchy.PlayListModel key;
    private Timer adapterUIUpdates=new Timer();
    private ArrayList<PlayListHierarchy.PlayListModel> filesURI;
    private String currentUri="";
    private boolean initialized=false;
    public PlayListFragment(){
       mode.setValue("Navigation");
    }
    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root= inflater.inflate(R.layout.audio_playlist_specific_music_fragment,container,false);
        playList=root.findViewById(R.id.playList);
        back=root.findViewById(R.id.back);
        titleMessage=root.findViewById(R.id.title);
        LinearLayoutManager manager= new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        playList.setLayoutManager(manager);
        if(adapter==null)
        adapter= new PlayListAdapter(getContext(),array);
        adapter.setOnItemClickListener(new PlayListAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                PlayListHierarchy.PlayListModel model=array.get(position);
                if(mode.getValue().equals("Navigation")) {
                    if (!model.isChild()) {
                        adapterUIUpdates.stop();
                        array.clear();
                        key = model;
                        MusicHelperSingleton.getInstance().setPlayList(key.getName());
                        filesURI=playListHierarchy.getArray(model.getName());
                        adapterUIUpdates.start();
                        back.setVisibility(View.VISIBLE);
                    }else {
                       highlightItem(position);
                    }
                    if(!array.isEmpty()){
                        MusicHelperSingleton.getInstance().setPlayList(key.getName());
                        setSongs();
                        try {
                            startPlayList(position);
                            MusicHelperSingleton.getInstance().setCurrent(position);
                            currentUri=MusicHelperSingleton.getInstance().getCurrentUri();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }else {
                    model.setSelected(!model.isSelected());
                    int count=adapter.getSelectCount();
                    adapter.setSelectCount(model.isSelected()?count+1:count-1);
                    updateMode("Select");
                    adapter.notifyItemChanged(position);
                }

            }

            @Override
            public void onLongClick(int position) {
                updateMode("Select");
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                array.clear();
                key=null;
                for(String key:playListHierarchy.getKeys()){
                    PlayListHierarchy.PlayListModel model=new PlayListHierarchy.PlayListModel(key,key);
                    model.setChild(false);
                    array.add(model);
                }
                if(array.isEmpty()){
                    titleMessage.setVisibility(View.VISIBLE);
                    titleMessage.setText("No PlayList Item Added");
                }else {
                    titleMessage.setVisibility(View.GONE);
                }
                adapter.setHighlight(-1);
                adapter.notifyDataSetChanged();
                // disable the select icon
                updateMode("Navigation");
                back.setVisibility(View.GONE);
            }
        });
        // update in intervals to prevent lag in the UI to increase user experience
        // better than a loading dialog
        adapterUIUpdates.setIntervalsMillis(50);
        adapterUIUpdates.setListener(new Timer.TimerListener() {
            @Override
            public void calculate(long seconds) {
                if(filesURI!=null&&seconds>=filesURI.size()&array.size()<filesURI.size()){
                    adapterUIUpdates.stop();
                    return;
                }
                PlayListHierarchy.PlayListModel listModel =filesURI.get((array.size()));
                array.add(listModel);
                if(!MusicHelperSingleton.getInstance().getPlayList().equals("All Songs")&
                        listModel.getPath().equals(MusicHelperSingleton.getInstance().getCurrentUri())){
                    adapter.setHighlight(array.size()-1);
                    playList.scrollToPosition(array.size()-1);
                }
                adapter.notifyItemInserted(array.size()-1);
                listModel.setChild(true);
            }
        });

       if(!initialized) {
           loadData();
       }else {
           if(key!=null)
           back.setVisibility(View.VISIBLE);
           playList.setAdapter(adapter);
       }

       showMessageIfEmpty();

        return root;
    }

    private void startPlayList(int position) throws IOException {
        adapter.notifyItemChanged(MusicHelperSingleton.getInstance().getCurrent());
        MusicHelperSingleton.getInstance().setCurrent(position);
        MusicHelperSingleton.getInstance().setReset(true);
        MusicHelperSingleton.getInstance().startPlayer();
    }

    @SuppressLint("SetTextI18n")
    public void showMessageIfEmpty(){
        if(array.isEmpty()){
            titleMessage.setVisibility(View.VISIBLE);
            titleMessage.setText("No PlayList Item Added");
        }else {
            titleMessage.setVisibility(View.GONE);
        }
    }

    public void highlightItem(int position){
        if(root==null)
            return;
        int highlight = adapter.getHighlight();
        currentUri=MusicHelperSingleton.getInstance().getCurrentUri();
        adapter.setHighlight(position);
        adapter.notifyItemChanged(position);
        playList.scrollToPosition(position);
        if (highlight != -1)
            adapter.notifyItemChanged(highlight);
    }

    public void updateMode(String value){
        if(mode==null)
            return;
        if(value.equals("Navigation")&mode.getValue().equals("Select"))
         adapter.reset();
         adapter.setActivateSelect(value.equals("Select"));
        if(!(value.equals("Navigation")&mode.getValue().equals("Navigation")))
         adapter.notifyDataSetChanged();
         mode.setValue(value);

    }

    public int getSelectCount(){
        return adapter.getSelectCount();
    }

    public int size(){
        return adapter.getItemCount();
    }
    @SuppressLint("SetTextI18n")
    public void removeSelected(){
        if(!array.isEmpty()&&!array.get(0).isChild()){
            if(getContext()!=null) {
                // remove from key preferences
                SharedPreferences keysPref = getContext().getSharedPreferences("PlayListKeysPref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor keyEditor = keysPref.edit();
                    ArrayList< PlayListHierarchy.PlayListModel> objects= new ArrayList<>();
                for (int i=0;i<array.size();i++) {
                    PlayListHierarchy.PlayListModel model = array.get(i);
                    if (!model.isSelected())
                        continue;
                    String name =  model.getName() ;
                    keyEditor.remove(name);
                    keyEditor.apply();
                    // finally remove all it's children
                    ArrayList<PlayListHierarchy.PlayListModel>children=playListHierarchy.getArray(model.getName());
                    SharedPreferences playListPref = getContext().getSharedPreferences(model.getName(), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = playListPref.edit();
                    if(children!=null)
                    for (PlayListHierarchy.PlayListModel playListModel : children) {
                        editor.remove(playListModel.getName());
                    }
                    objects.add(model);
                    editor.apply();
                }
                for(int i=0;i<objects.size();i++) {
                    array.remove(objects.get(i));
                }

            }
        }else {
            if(key!=null&getContext()!=null) {
                // selected files shared preference
                SharedPreferences playListPref=getContext().getSharedPreferences(key.getName(),Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=playListPref.edit();
                ArrayList<PlayListHierarchy.PlayListModel> objects= new ArrayList<>();
                for (int i=0;i<array.size();i++) {
                    PlayListHierarchy.PlayListModel model=array.get(i);
                    if(!model.isSelected())
                        continue;
                    editor.remove(model.getName());
                    playListHierarchy.getArray(key.getName()).remove(model);
                    objects.add(model);
                }
                // remove all objects
                for(int i=0;i<objects.size();i++){
                    array.remove(objects.get(i));
                }
               // remove playlist if list is empty
                if(array.isEmpty()){
                    SharedPreferences.Editor keyEditor=getContext().getSharedPreferences("PlayListKeysPref",Context.MODE_PRIVATE).edit();
                    keyEditor.remove(key.getName());
                    keyEditor.apply();
                    playListHierarchy.remove(key.getName());
                }
                editor.apply();
            }
        }
        showMessageIfEmpty();
        adapter.setActivateSelect(false);
        adapter.notifyDataSetChanged();
    }
    public MutableLiveData<String> getMode() {
        return mode;
    }

    public void loadData(){
        new PlayListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void add(String parent, ArrayList<PlayListHierarchy.PlayListModel>models){
        playListHierarchy.add(parent,models);

        if(key==null) {
            array.clear();
            for (String key : playListHierarchy.getKeys()) {
                PlayListHierarchy.PlayListModel model = new PlayListHierarchy.PlayListModel(key, key);
                model.setChild(false);
                array.add(model);
                adapter.notifyDataSetChanged();
            }
        }  else {
            array.clear();
            filesURI=playListHierarchy.getArray(key.getName());
            for(PlayListHierarchy.PlayListModel model:filesURI) {
                model.setChild(true);
                array.add(model);
            }
            setSongs();
            adapter.notifyDataSetChanged();

        }
    }

    public void setSongs(){
        if(key==null|adapter.getItemCount()<=0)
            return;
        MusicHelperSingleton.getInstance().clear();
        for (PlayListHierarchy.PlayListModel file:array)
        MusicHelperSingleton.getInstance().add(file.getName());
        MusicHelperSingleton.getInstance().setCurrent(0);
    }

    private void attachAdapter(){
        if(!playListHierarchy.isEmpty()) {
            for(String key:playListHierarchy.getKeys()){
                PlayListHierarchy.PlayListModel model= new PlayListHierarchy.PlayListModel(key,key);
                model.setChild(false);
                array.add(model);
            }
        }

        if(array.isEmpty()){
            titleMessage.setVisibility(View.VISIBLE);
            titleMessage.setText("No PlayList Item Added");
        }
        playList.setAdapter(adapter);
    }

    @SuppressLint("StaticFieldLeak")
   class PlayListTask extends AsyncTask<String,Integer,String>{

       @Override
       protected String doInBackground(String... strings) {
           array.clear();
           if(playListHierarchy==null&getContext()!=null)
            playListHierarchy= new PlayListHierarchy(getContext(),"PlayListKeysPref");
           else
               playListHierarchy.reset();
            playListHierarchy.fetchFilesFromPreference();
            initialized=true;
           return null;
       }

       @Override
       protected void onPreExecute() {
           super.onPreExecute();
       }

       @Override
       protected void onPostExecute(String s) {
           super.onPostExecute(s);
          attachAdapter();

       }
   }
}