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

import com.file.manager.R;
import com.file.manager.helpers.PermissionsHelper;
import com.file.manager.ui.Models.CopyProgressMonitor;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.utils.CopyHelper;
import com.file.manager.utils.CopyUtility;
import com.file.manager.utils.DateUtils;
import com.file.manager.utils.DiskUtils;
import com.file.manager.utils.Timer;

import java.io.File;

public class CopyFileDialog extends Dialog  {

    private CopyUtility.OnCopyListener completeListener;
    private Context context;
    private boolean error=false;
    private Button cancel;
    private Timer writeSpeedTimer;
    private CopyUtility copyUtility;
    private Uri uri;
    public CopyFileDialog(Context context) throws NullPointerException{
        super(context);
        this.context=context;
        CustomFile file=CopyHelper.getInstance().getDestination();
        String startDir=DiskUtils.getInstance().getStartDirectory(file);
             uri= PermissionsHelper.getInstance().getUriFromSharedPreference(
                    new File(startDir));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.dialog_copy_progress);
        setCancelable(false);
        final ProgressBar currentFileProgress=findViewById(R.id.currentFileProgress);
        final TextView WriteSpeed=findViewById(R.id.writeSpeed);
        final TextView RemainingTime=findViewById(R.id.remainingTime);
        final TextView percent=findViewById(R.id.percent);
        final TextView source=findViewById(R.id.source);
        final TextView destination=findViewById(R.id.destination);
         cancel=findViewById(R.id.cancel);
         copyUtility= new CopyUtility(context,uri);
         writeSpeedTimer= new Timer();
        writeSpeedTimer.setListener(new Timer.TimerListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void calculate(long seconds) {
                CopyProgressMonitor monitor=copyUtility.getProgressMonitor();
                currentFileProgress.setProgress(monitor.getPercent());
                percent.setText(Math.min(monitor.getPercent(),100)+"%"+" ("+monitor.getTotalFilesCopied()+"/"+monitor.getTotalFilesCount()+") ");
                WriteSpeed.setText(monitor.getWriteSpeedString(seconds));
                int rem=(int)((float)monitor.getWorkToBeDone()/(float)monitor.getWriteSpeedInt(seconds));
                RemainingTime.setText("Elapsed: "+DateUtils.getHoursMinutesSec(seconds)+" Remaining: "+
                        DateUtils.getHoursMinutesSec(Math.abs(rem-seconds)));
                source.setText("From: "+monitor.getSource());
                destination.setText("To: "+monitor.getDestination());
                if(!copyUtility.isRunning()) {
                    currentFileProgress.setProgress(100);
                    percent.setText(100+"%"+" ("+monitor.getTotalFilesCopied()+"/"+monitor.getTotalFilesCount()+") ");
                    RemainingTime.setText("Elapsed: "+DateUtils.getHoursMinutesSec(seconds)+" Remaining: "+
                            DateUtils.getHoursMinutesSec(0));
                    finished();
                    update();
                }
            }
        });

        writeSpeedTimer.start();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(copyUtility.isRunning()) {
                    copyUtility.cancel();
                }
                finished();
                update();
               cancel();
            }
        });
        copyUtility.Execute();
    }

    public String getMessage(){
        return copyUtility.getMessage();
    }
    boolean updated=false;
    private void update(){
        if(updated)
            return;
        if(copyUtility.isCancelled()){
            completeListener.onFailed(copyUtility.getNewFiles());
        }else {

            completeListener.onSuccess(copyUtility.getNewFiles());
        }
        updated=true;
    }
    public void finished(){
        cancel.setText("Close");
        writeSpeedTimer.stop();
    }

    public void setCompleteListener(CopyUtility.OnCopyListener completeListener) {
        this.completeListener = completeListener;
    }

}
