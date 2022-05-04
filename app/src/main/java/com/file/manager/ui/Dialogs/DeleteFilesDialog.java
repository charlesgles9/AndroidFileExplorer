package com.file.manager.ui.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Observer;

import com.file.manager.Activities.MainActivity;
import com.file.manager.R;
import com.file.manager.helpers.PermissionsHelper;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.utils.DeleteFilesUtility;
import com.file.manager.utils.DiskUtils;

import java.io.File;
import java.util.ArrayList;

public class DeleteFilesDialog  extends Dialog {

    private ArrayList<CustomFile> files;
    private Context context;
    private DeleteFilesUtility.OnDeleteCompleteListener onCompleteListener;
    private DeleteFilesUtility deleteFilesUtility;
    public DeleteFilesDialog(Context context,ArrayList<CustomFile>files){
        super(context);
        this.context=context;
        this.files=files;

    }
    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.dialog_delete_progress);
        final Button cancel=findViewById(R.id.cancel);
        final TextView percent=findViewById(R.id.percent);
        final TextView source=findViewById(R.id.source);
        final ProgressBar progress=findViewById(R.id.progress);
        Uri uri= PermissionsHelper.getInstance().getUriFromSharedPreference(
                new File(DiskUtils.getInstance().getStartDirectory(files.get(0))));


        deleteFilesUtility= new DeleteFilesUtility(getContext(),uri,files);
        deleteFilesUtility.getUpdate().observe((MainActivity) context, new Observer<Boolean>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(Boolean update) {
                if(update){
                    progress.setProgress(deleteFilesUtility.getPercent());
                    int fC=deleteFilesUtility.getFileCount();
                    int fD=deleteFilesUtility.getFileDeleted();
                    percent.setText(String.valueOf("( "+fC+"/"+fD+" "+deleteFilesUtility.getPercent()+"% )"));
                    source.setText("From: "+deleteFilesUtility.getFrom());
                    if(!deleteFilesUtility.isRunning()){
                        cancel.setText("Close");
                        testFilesIfDeleted();
                        onCompleteListener.onSuccess(deleteFilesUtility.getFiles());
                        if(deleteFilesUtility.isError()) {
                            Toast.makeText(getContext(), "Storage Volume Error!", Toast.LENGTH_SHORT).show();
                            cancel();
                        }
                    }
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if(deleteFilesUtility.isRunning()) {
                    deleteFilesUtility.cancel();
                    cancel.setText("close");
                }else {
                    testFilesIfDeleted();
                    onCompleteListener.onSuccess(deleteFilesUtility.getFiles());
                    cancel();
                }
            }
        });
        deleteFilesUtility.Execute();
    }


    private void testFilesIfDeleted(){

        for(int i=0;i<files.size();i++){
            if(files.get(i).exists()){
                files.remove(i);
            }
        }
    }
    public void setOnCompleteListener(DeleteFilesUtility.OnDeleteCompleteListener onCompleteListener){
        this.onCompleteListener=onCompleteListener;
    }

}
