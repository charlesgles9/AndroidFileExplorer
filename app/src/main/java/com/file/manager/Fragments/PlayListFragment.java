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

public class PlayListFragment extends Fragment {

    private RecyclerView playList;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root= inflater.inflate(R.layout.audio_playlist_specific_music_fragment,container,false);
        playList=root.findViewById(R.id.playList);
        LinearLayoutManager manager= new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        playList.setLayoutManager(manager);


        return root;
    }


}