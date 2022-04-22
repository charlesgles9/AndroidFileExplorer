package com.file.manager.Fragments;

import android.content.Context;
import android.os.AsyncTask;
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
import com.file.manager.ui.Models.AudioPlayList;

public class PlayListFragment extends Fragment {

    private RecyclerView playList;
    private RecyclerView fileList;
    private AudioPlayList audioPlayList;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root= inflater.inflate(R.layout.audio_playlist_specific_music_fragment,container,false);
        playList=root.findViewById(R.id.playList);
        fileList=root.findViewById(R.id.fileList);
        LinearLayoutManager manager1= new LinearLayoutManager(getContext());
        manager1.setOrientation(RecyclerView.HORIZONTAL);
        LinearLayoutManager manager2= new LinearLayoutManager(getContext());
        manager1.setOrientation(RecyclerView.VERTICAL);
        playList.setLayoutManager(manager1);
        fileList.setLayoutManager(manager2);



        return root;
    }


    class LoadPlayListTask extends AsyncTask<String,Integer,String>{

        private Context context;
        public LoadPlayListTask(Context context){
            this.context=context;
        }
        @Override
        protected String doInBackground(String... strings) {
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

}