package com.file.manager.ui.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Adapters.PlayListAdapter;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.PlayListHierarchy;

import java.util.ArrayList;

public class AddPlayListDialog extends Dialog {


    private PlayListHierarchy playListHierarchy;
    private RecyclerView playList;
    private PlayListAdapter adapter;
    private TextView message;
    private ArrayList<PlayListHierarchy.PlayListModel> array= new ArrayList<>();
    private ArrayList<CustomFile>files;
    private int selected=-1;
    private OnItemCompleteListener onItemCompleteListener;
    public AddPlayListDialog(Context context, ArrayList<CustomFile>files){
        super(context);
        this.files=files;
    }

    public void setOnTaskCompleteListener(OnItemCompleteListener onItemCompleteListener) {
        this.onItemCompleteListener = onItemCompleteListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setContentView(R.layout.audio_add_play_list_dialog);
        playList=findViewById(R.id.playList);
        message=findViewById(R.id.message);
        Button addPlayList=findViewById(R.id.addPlayList);
        final Button okay=findViewById(R.id.okay);
        Button cancel=findViewById(R.id.cancel);
        LinearLayoutManager manager= new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        playList.setLayoutManager(manager);

        adapter= new PlayListAdapter(getContext(),array);
        playList.setAdapter(adapter);
        adapter.setOnItemClickListener(new PlayListAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
               int previous=adapter.getHighlight();
               adapter.setHighlight(position);
               selected=position;
               adapter.notifyItemChanged(previous);
               adapter.notifyItemChanged(position);
               okay.setEnabled(true);
            }

            @Override
            public void onLongClick(int position) {

            }
        });
        okay.setEnabled(false);
        addPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              getPlayListNameDialog((array.size()+1));
            }
        });
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selected==-1){
                    Toast.makeText(getContext(),"Select PlayList!",Toast.LENGTH_SHORT).show();
                    return;
                }
                PlayListHierarchy.PlayListModel parent=array.get(selected);
                SharedPreferences.Editor editor=getContext().getSharedPreferences(parent.getName(),Context.MODE_PRIVATE).edit();
                ArrayList<PlayListHierarchy.PlayListModel>models= new ArrayList<>();
                for(CustomFile file:files){
                    PlayListHierarchy.PlayListModel model=
                            new PlayListHierarchy.PlayListModel(file.getPath(),file.getName());
                    models.add(model);
                    model.setChild(true);
                  editor.putString(file.getPath(),file.getName());
                }
                editor.apply();
                if(onItemCompleteListener!=null){
                    onItemCompleteListener.onComplete(parent,models);
                }
                dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        new PlayListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

  private void getPlayListNameDialog(final int position){
      final SharedPreferences preferences=getContext().getSharedPreferences("PlayListKeysPref",Context.MODE_PRIVATE);
      PlayListNameDialog nameDialog= new PlayListNameDialog(getContext());
      nameDialog.setPlayListCallBack(new PlayListNameDialog.PlayListCallBack() {
          @Override
          public void accept(String name) {
              name=name+(position+1);
              PlayListHierarchy.PlayListModel listModel=new PlayListHierarchy.PlayListModel(name,name);
              array.add(listModel);
              SharedPreferences.Editor editor=preferences.edit();
              editor.putString(listModel.getName(),listModel.getName());
              editor.apply();
              adapter.notifyItemInserted(position-1);
              message.setVisibility(View.INVISIBLE);
          }

          @Override
          public void cancel() {

          }
      });
        nameDialog.show();
 }
    class PlayListTask extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... strings) {
            playListHierarchy= new PlayListHierarchy(getContext(),"PlayListKeysPref");
            playListHierarchy.fetchFilesFromPreference();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(!playListHierarchy.isEmpty()) {
                for(String key:playListHierarchy.getKeys()){
                    PlayListHierarchy.PlayListModel model= new PlayListHierarchy.PlayListModel(key,key);
                    array.add(model);
                }
            }else {
                message.setVisibility(View.VISIBLE);
            }
            playList.setAdapter(adapter);
        }
    }

public interface OnItemCompleteListener{
        void onComplete(PlayListHierarchy.PlayListModel parent, ArrayList<PlayListHierarchy.PlayListModel> array);
}
}
