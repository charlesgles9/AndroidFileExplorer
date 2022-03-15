package com.file.manager.ui;

import android.app.Dialog;
import android.content.Context;;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.file.manager.Activities.MainActivity;
import com.file.manager.R;
import com.file.manager.ui.utils.CutUtility;

public class MoveFileDialog  extends Dialog implements View.OnClickListener {

   private Context context;

   private CutUtility cutUtility;
   private CutUtility.OnCutListener onCutListener;
    public MoveFileDialog(Context context){
        super(context);
       this.context=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.move_files_dialog_progress);
        final TextView source=findViewById(R.id.source);
        final TextView percent=findViewById(R.id.percent);
        final TextView destination=findViewById(R.id.destination);
        final ProgressBar progress=findViewById(R.id.progress);
        final Button cancel=findViewById(R.id.cancel);

        cutUtility= new CutUtility(context);
        cutUtility.getUpdate().observe((MainActivity) context, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean update) {
                if(update){
                  int f1=cutUtility.getFilesCopied();
                  int f2=cutUtility.getFilesToCopy();
                  int p=cutUtility.getPercent();
                  source.setText("From: "+cutUtility.getFrom());
                  destination.setText("To: "+cutUtility.getTo());
                  percent.setText(String.valueOf("("+f1+"/"+f2+" "+p+"% )"));
                  progress.setProgress(p);
                  if(!cutUtility.isRunning()){
                      cancel.setText("Close");
                      onCutListener.onSuccess(cutUtility.getNewFiles());
                  }
                }
            }
        });
        cutUtility.Execute();
        cancel.setOnClickListener(this);
    }

    public String getMessage(){
        return cutUtility.getMessage();
    }
    public void setOnCutListener(CutUtility.OnCutListener onCutListener) {
        this.onCutListener = onCutListener;
    }

    @Override
    public void onClick(View v) {
        if(R.id.cancel==v.getId()){
            if(cutUtility.isRunning()){
                cutUtility.cancel();
                onCutListener.onSuccess(cutUtility.getNewFiles());
            }
            cancel();
        }
    }

    @Override
    public void cancel() {
        super.cancel();
    }
}
