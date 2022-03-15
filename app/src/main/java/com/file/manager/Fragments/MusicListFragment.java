package com.file.manager.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Adapters.AllMusicAdapter;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.MusicHelperSingleton;
import com.file.manager.ui.utils.Timer;

import java.io.IOException;
import java.util.ArrayList;

public class MusicListFragment extends Fragment {

    private ArrayList<CustomFile>files;
    private RecyclerView fileListRecycleView;
    private AllMusicAdapter allMusicAdapter;
    private View root;
    private MutableLiveData<String> mode= new MutableLiveData<>();
    private Timer adapterUIUpdates=new Timer();
    public MusicListFragment(ArrayList<CustomFile>files){
        this.files=files;
        this.mode.setValue("Navigation");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root=inflater.inflate(R.layout.playlist_all_music_fragment,container,false);
        fileListRecycleView=root.findViewById(R.id.playList);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        fileListRecycleView.setLayoutManager(linearLayoutManager);
        fileListRecycleView.setItemAnimator(null);
        if(allMusicAdapter==null)
        allMusicAdapter = new AllMusicAdapter(getContext(),files);
        allMusicAdapter.setOnItemClickListener(new AllMusicAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {

                CustomFile file=files.get(position);
                if(mode.getValue().equals("Select")) {
                    file.setSelected(!file.IsSelected());
                    allMusicAdapter.setSelectCount(file.IsSelected()?allMusicAdapter.getSelectCount()+1:allMusicAdapter.getSelectCount()-1);
                    updateMode("Select");
                    allMusicAdapter.notifyItemChanged(position);
                }else {
                    try {
                        allMusicAdapter.setHighlight(-1);
                        allMusicAdapter.notifyItemChanged(MusicHelperSingleton.getInstance().getCurrent());
                        MusicHelperSingleton.getInstance().setCurrent(position);
                        MusicHelperSingleton.getInstance().setReset(true);
                        MusicHelperSingleton.getInstance().startPlayer();
                        allMusicAdapter.setHighlight(position);
                        allMusicAdapter.notifyItemChanged(position);
                    }catch (IOException ignore){

                    }
                }

            }

            @Override
            public void onLongClick(int position) {
               updateMode("Select");
            }
        });

        // update in intervals to prevent lag in the UI to increase user experience
        // better than a loading dialog
        adapterUIUpdates.setIntervalsMillis(50);
        adapterUIUpdates.setListener(new Timer.TimerListener() {
            @Override
            public void calculate(long seconds) {
                ArrayList<CustomFile>data=MusicHelperSingleton.getInstance().getAllSongs();
                if(files.size()>=data.size()|!MusicHelperSingleton.getInstance().getPlayList().equals("All Songs")){
                    adapterUIUpdates.stop();
                    return;
                }
                CustomFile file=data.get(files.size());
                updateAdapter(files.size()+1,file);
            }
        });

        fileListRecycleView.setAdapter(allMusicAdapter);
        fileListRecycleView.scrollToPosition(Math.max(MusicHelperSingleton.getInstance().getCurrent()-5,0));
        // if the file name changes update the UI
        MutableLiveData<String> currentFileName= MusicHelperSingleton.getInstance().getCurrentFileName();
        currentFileName.removeObservers(this);
        currentFileName.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(MusicHelperSingleton.getInstance().equals("All Songs"))
                highlightItem(MusicHelperSingleton.getInstance().getCurrent());
            }
        });
        return root;
    }

    public View getRoot() {
        return root;
    }

    public ArrayList<CustomFile>getSelected(){
        ArrayList<CustomFile>selected= new ArrayList<>();
        for (CustomFile file:files){
            if(file.IsSelected()){
                selected.add(file);
            }
        }
        return selected;
    }

    public void highlightItem(int position){
        if(root==null)
            return;
        int highlight = allMusicAdapter.getHighlight();
        allMusicAdapter.setHighlight(position);
        allMusicAdapter.notifyItemChanged(position);
        fileListRecycleView.scrollToPosition(position);
        if (highlight != -1)
            allMusicAdapter.notifyItemChanged(highlight);
    }

    public void updateAdapter(){
        allMusicAdapter.notifyDataSetChanged();
    }

    private void updateAdapter(int position,CustomFile file){
        files.add(file);
        allMusicAdapter.notifyItemInserted(position);
    }


    public void startAdapterUITimer(){
        adapterUIUpdates.stop();
        adapterUIUpdates.start();
    }

    public void stopAdapterUITimer(){
        adapterUIUpdates.stop();
    }
    public void updateMode(String value){
        if(mode==null)
            return;
        if(value.equals("Navigation")&mode.getValue().equals("Select"))
        allMusicAdapter.reset();
        allMusicAdapter.setActivateSelect(value.equals("Select"));
        if(!(value.equals("Navigation")&mode.getValue().equals("Navigation")))
        allMusicAdapter.notifyDataSetChanged();
        if(mode.getValue().equals("Navigation")&value.equals("Select"))
            allMusicAdapter.setSelectCount(0);
        mode.setValue(value);

    }

    public MutableLiveData<String> getMode() {
        return mode;
    }
    public int getSelectCount() {
        return allMusicAdapter.getSelectCount();
    }
}
