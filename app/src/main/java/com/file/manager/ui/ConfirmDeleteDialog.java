package com.file.manager.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.file.manager.R;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.utils.DeleteFilesUtility;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.FileHandleUtil;

import java.util.ArrayList;
import java.util.Objects;

public class ConfirmDeleteDialog extends Dialog implements View.OnClickListener {

    private ArrayList<CustomFile>files= new ArrayList<>();
    private Context context;
    private DeleteFilesUtility.OnDeleteCompleteListener onComplete;
    public ConfirmDeleteDialog(Context context,ArrayList<CustomFile>list){
        super(context);
        this.files.addAll(list);
        this.context=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getWindow()).setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.confirm_delete);
        final Button confirm=findViewById(R.id.confirm);
        final Button cancel=findViewById(R.id.cancel);
        final TextView size=findViewById(R.id.size);
        final TextView source=findViewById(R.id.source);
        confirm.setOnClickListener(this);
        cancel.setOnClickListener(this);
        source.setText(files.get(0).getPath());

       @SuppressLint("StaticFieldLeak")
       class FileSizeTask extends AsyncTask<String,Integer,String>{
           String sizeText="Calculating...";
           @Override
           protected String doInBackground(String... strings) {
               sizeText=DiskUtils.getInstance().getSize(FileHandleUtil.getFileSizeArray(files));
               return null;
           }
           @Override
           protected void onPreExecute() {
               super.onPreExecute();
               size.setText(sizeText);
           }
           @Override
           protected void onPostExecute(String s) {
               super.onPostExecute(s);
               size.setText(sizeText);
           }
       }
       FileSizeTask task= new FileSizeTask();
       task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.confirm){

            DeleteFilesDialog dialog= new DeleteFilesDialog(context,files);
            dialog.setOnCompleteListener(onComplete);
            dialog.show();
        }
        cancel();
    }
    public void setOnCompleteListener(DeleteFilesUtility.OnDeleteCompleteListener onComplete){
        this.onComplete=onComplete;
    }
}
