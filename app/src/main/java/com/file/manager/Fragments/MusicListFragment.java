package com.file.manager.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Adapters.MusicAdapter;
import com.file.manager.ui.Dialogs.AudioPlayListBottomSheet;
import com.file.manager.ui.Models.MusicHelperSingleton;
import com.file.manager.ui.Models.PlayListChild;
import com.file.manager.utils.Timer;

import java.util.ArrayList;
import java.util.List;

public class MusicListFragment extends Fragment {

    private List<PlayListChild> files;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView fileListRecycleView;
    private MusicAdapter adapter;
    private MusicHelperSingleton singleton=MusicHelperSingleton.getInstance();
    private MusicAdapter.OnItemClickListener onItemClickListener;
    private AudioPlayListBottomSheet.Updatable updatable;
    public MusicListFragment(AudioPlayListBottomSheet.Updatable updatable){
        this.files=new ArrayList<>();
        this.updatable=updatable;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.audio_playlist_all_music_fragment,container,false);
        fileListRecycleView = root.findViewById(R.id.playList);
        linearLayoutManager= new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        fileListRecycleView.setLayoutManager(linearLayoutManager);
        fileListRecycleView.setItemAnimator(null);
        adapter= new MusicAdapter(getContext(),files);
        fileListRecycleView.setAdapter(adapter);
        adapter.setOnItemClickListener(onItemClickListener);
        // load the data only once
        if(files.isEmpty())
          populateAdapter();

        // main ui updates at the bottom sheet layout
          updatable.update();
        return root;
    }


    public void setData(){
        MusicHelperSingleton.getInstance().clear();
        MusicHelperSingleton.getInstance().add(MusicHelperSingleton.getInstance().getAllSongs());
    }

    public void setOnItemClickListener(MusicAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    // load the data in intervals avoids lag and wait time
    private void populateAdapter(){
        final Timer timer= new Timer();
        timer.setIntervalsMillis(2);
        timer.setListener(new Timer.TimerListener() {
            @Override
            public void calculate(long seconds) {
                if(singleton.getAllSongs().size()==files.size()) {
                    fileListRecycleView.setVerticalScrollBarEnabled(true);
                    timer.stop();
                    updatable.update();
                    return;
                }
                updatable.update();
                fileListRecycleView.setVerticalScrollBarEnabled(false);
                files.add(new PlayListChild(singleton.getAllSongs().get(files.size()).getPath()));
                adapter.notifyItemInserted(files.size()-1);
            }
        });

        timer.start();
    }


    public MusicAdapter getAdapter() {
        return adapter;
    }



}
