package com.file.manager.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.file.manager.R;
import com.file.manager.ui.Adapters.PlayListAdapter;
import com.file.manager.ui.Adapters.PlayListHeaderAdapter;
import com.file.manager.ui.Models.AudioPlayList;

public class PlayListFragment extends Fragment {

    private TextView message;
    private PlayListAdapter playListAdapter;
    private PlayListHeaderAdapter headerAdapter;
    private AudioPlayList audioPlayList=new AudioPlayList();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root= inflater.inflate(R.layout.audio_playlist_specific_music_fragment,container,false);
        RecyclerView playList = root.findViewById(R.id.playList);
        RecyclerView fileList = root.findViewById(R.id.fileList);
        message=root.findViewById(R.id.message);
        LinearLayoutManager manager1= new LinearLayoutManager(getContext());
        manager1.setOrientation(RecyclerView.HORIZONTAL);
        LinearLayoutManager manager2= new LinearLayoutManager(getContext());
        manager1.setOrientation(RecyclerView.VERTICAL);
        playList.setLayoutManager(manager1);
        fileList.setLayoutManager(manager2);
        headerAdapter= new PlayListHeaderAdapter(getContext());
        playListAdapter= new PlayListAdapter(getContext(),null);

        playListAdapter.setOnItemClickListener(new PlayListAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {

            }

            @Override
            public void onLongClick(int position) {

            }
        });

        headerAdapter.setOnItemClickListener(new PlayListHeaderAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                playListAdapter.setHeader(audioPlayList.get(position));
                playListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLongClick(int position) {

            }
        });
        fileList.setAdapter(playListAdapter);
        playList.setAdapter(headerAdapter);

        if(audioPlayList.isEmpty())
            new LoadPlayListTask(getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        return root;
    }


    @SuppressLint("StaticFieldLeak")
    private class LoadPlayListTask extends AsyncTask<String,Integer,String>{

        private Context context;
        public LoadPlayListTask(Context context){
            this.context=context;
        }
        @Override
        protected String doInBackground(String... strings) {
            audioPlayList.loadHeaders(context);

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            headerAdapter.setHeaders(audioPlayList.getHeaders());
            headerAdapter.notifyDataSetChanged();
            if(!audioPlayList.getHeaders().isEmpty()){
                message.setVisibility(View.GONE);
                playListAdapter.setHeader(audioPlayList.get(0));
                playListAdapter.notifyDataSetChanged();
            }else {
                message.setVisibility(View.VISIBLE);
            }

        }
    }

}