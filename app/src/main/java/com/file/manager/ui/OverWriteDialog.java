package com.file.manager.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.file.manager.OnTaskCompleteListener;
import com.file.manager.R;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.Folder;
import java.util.ArrayList;

public class OverWriteDialog extends Dialog {


    private ArrayList<CustomFile>files;
    private TextView source;
    private OnTaskCompleteListener onCompleteListener;
    private int index=0;
    private int SKIP=0;
    private int OVERWRITE=1;

    public OverWriteDialog(Context context, ArrayList<CustomFile>files){
        super(context);
        this.files=files;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.overwrite_dialog_layout);
        source=findViewById(R.id.source);
        final View doForAllLayout=findViewById(R.id.doForAllLayout);
        final ToggleButton doForAll=findViewById(R.id.doForAll);
        final Button cancel=findViewById(R.id.cancel);
        final Button skip=findViewById(R.id.skip);
        final Button overwrite=findViewById(R.id.overwrite);

        doForAllLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doForAll.setChecked(!doForAll.isChecked());
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        overwrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeConflicts(doForAll.isChecked(),OVERWRITE);
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               removeConflicts(doForAll.isChecked(),SKIP);
            }
        });
        source.setText(files.get(index).getPath());
    }

    public void setOnCompleteListener(OnTaskCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public void removeConflicts(boolean all, int choice){
        if(!all){
            if(SKIP==choice){
                files.remove(index);
            }else {
                index=index>=files.size()?files.size()-1:index+1;
            }

        }else {
            ArrayList<CustomFile>skipped=new ArrayList<>();
            for(int i=index;i<files.size();i++){
              if(SKIP==choice) {
                  skipped.add(files.get(i));
              }
                index=i+1;
            }
            files.removeAll(skipped);
        }

        if(index>=files.size()){
            // exit here
            onCompleteListener.onTaskComplete();
            dismiss();
        }else {
            // set the next item path
            source.setText(files.get(index).getPath());
        }
    }


}
