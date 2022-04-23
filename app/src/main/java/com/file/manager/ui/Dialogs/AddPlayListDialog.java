package com.file.manager.ui.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Adapters.PlayListHeaderAdapter;
import com.file.manager.ui.Models.AudioPlayList;
import com.file.manager.ui.Models.CustomFile;

import java.io.File;
import java.util.ArrayList;

public class AddPlayListDialog extends Dialog {


    private RecyclerView playList;
    private AudioPlayList audioPlayList;
    private PlayListHeaderAdapter adapter;
    private TextView message;
    private ArrayList<File>files;
    private int selected=-1;
    public AddPlayListDialog(Context context, ArrayList<File>files){
        super(context);
        this.files=files;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_add_play_list_dialog);
        final Button okay=findViewById(R.id.okay);
        final Button cancel=findViewById(R.id.cancel);
        final Button addPlayListHeader=findViewById(R.id.addPlayList);
        message=findViewById(R.id.message);
        playList=findViewById(R.id.playList);
        LinearLayoutManager manager= new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        playList.setLayoutManager(manager);
        audioPlayList= new AudioPlayList();
        adapter= new PlayListHeaderAdapter(getContext());
        adapter.setHeaders(audioPlayList.getHeaders());
        playList.setAdapter(adapter);
        adapter.setOnItemClickListener(new PlayListHeaderAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                selected=position;

            }

            @Override
            public void onLongClick(int position) {

            }
        });

        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selected==-1) {
                    Toast.makeText(getContext(),"Select a playlist!",Toast.LENGTH_LONG).show();
                    return;
                }

                audioPlayList.get(selected).addChild(getContext(),files);
                adapter.notifyDataSetChanged();
                dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        addPlayListHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PlayListHeaderDialog headerDialog= new PlayListHeaderDialog(getContext());
                headerDialog.setPlayListCallBack(new PlayListHeaderDialog.PlayListCallBack() {
                    @Override
                    public void accept(String name) {
                        audioPlayList.addHeader(getContext(),name,"date");
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void cancel() {

                    }
                });
                headerDialog.show();

            }
        });

        new FetchHeadersTask(getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);




    }

    class FetchHeadersTask extends AsyncTask<String,Integer,String>{

        private Context context;
        public FetchHeadersTask(Context context){
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
            adapter.notifyDataSetChanged();
            if(audioPlayList.isEmpty()){
                message.setVisibility(View.VISIBLE);
            }else
                message.setVisibility(View.GONE);
        }
    }
}
