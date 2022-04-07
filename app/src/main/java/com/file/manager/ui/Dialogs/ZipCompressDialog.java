package com.file.manager.ui.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.file.manager.R;
import com.file.manager.utils.ArchiveCompressUtil;
import com.file.manager.utils.DateUtils;
import com.file.manager.utils.DiskUtils;
import com.file.manager.utils.Timer;

public class ZipCompressDialog extends Dialog {

   private ArchiveCompressUtil archiveCompressUtil;
   private long pBytes=1L;
    public ZipCompressDialog(Context context, ArchiveCompressUtil archiveCompressUtil){
        super(context);
        this.archiveCompressUtil=archiveCompressUtil;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.dialog_zip_progress);
        final TextView source=findViewById(R.id.source);
        final TextView destination=findViewById(R.id.destination);
        final ProgressBar progress=findViewById(R.id.progress);
        final TextView percent=findViewById(R.id.percent);
        final TextView writeSpeed=findViewById(R.id.writeSpeed);
        final TextView remainingTime=findViewById(R.id.remainingTime);
        final TextView title=findViewById(R.id.title);
        final Button cancel=findViewById(R.id.cancel);
        title.setText("Compressing...");
        final Timer timer= new Timer();
        final long[] average={1L};
        timer.setListener(new Timer.TimerListener() {
            @Override
            public void calculate(long seconds) {
                source.setText("From: "+archiveCompressUtil.getProgressMonitor().getFileName());
                destination.setText("To: "+archiveCompressUtil.getToLocation().getPath());
                float w=archiveCompressUtil.getZipFile().getFile().length();
                float t=archiveCompressUtil.getSize();
                int p=(int)((w/t)*100);
                long speed=(archiveCompressUtil.getZipFile().getFile().length()-pBytes);
                speed=(speed+average[0])/2;
                average[0]=speed;
                writeSpeed.setText(DiskUtils.getInstance().getSize(speed)+"/s");
                long rem=(long)(float)(archiveCompressUtil.getSize()/(float)(speed));
                remainingTime.setText("Remaining Time: "+DateUtils.getHoursMinutesSec(rem));
                pBytes=archiveCompressUtil.getZipFile().getFile().length();
                progress.setProgress(p);
                percent.setText(progress.getProgress()+"%");
                if(!archiveCompressUtil.isRunning()){
                    progress.setProgress(100);
                    percent.setText("100%");
                    remainingTime.setText("Remaining Time: "+DateUtils.getHoursMinutesSec(0));
                    timer.stop();
                    cancel.setText("close");
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(archiveCompressUtil.isRunning()) {
                    timer.stop();
                    archiveCompressUtil.cancel();
                    cancel.setText("Close");
                }else {
                    cancel();
                }

            }
        });
        archiveCompressUtil.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        timer.start();
    }
}
