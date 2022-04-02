package com.file.manager.ui.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.file.manager.R;
import com.file.manager.ui.utils.ArchiveDecompressUtil;
import com.file.manager.ui.utils.DateUtils;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.Timer;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class ZipExtractDialog extends Dialog {

    private ArchiveDecompressUtil archiveDecompressUtil;
    private Context context;
    public ZipExtractDialog(Context context, ArchiveDecompressUtil archiveDecompressUtil) {
        super(context);
        this.context=context;
        this.archiveDecompressUtil = archiveDecompressUtil;
    }
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
        final Button cancel=findViewById(R.id.cancel);

        final Timer timer= new Timer();
        final long[]pBytes={1};
        final long[]average={1};
        final long[] bytesCopied={1};
        final long[] pBytesCopied={1};
        final String pFileName[]={""};
        final ArchiveDecompressUtil.ExtractAllTask task= archiveDecompressUtil.extract();
        final Map<String, File>map= new HashMap<>();
        timer.setListener(new Timer.TimerListener() {
            @Override
            public void calculate(long seconds) {
                ZipFile zipFile = archiveDecompressUtil.getZipFile();
                source.setText("From: " + zipFile.getProgressMonitor().getFileName());

                if (zipFile.getProgressMonitor().getFileName() != null) {
                    File file = new File(zipFile.getProgressMonitor().getFileName());
                    if (pFileName[0].equals(file.getPath())) {
                        bytesCopied[0] = bytesCopied[0] + file.length() - pBytesCopied[0];
                    } else {
                        bytesCopied[0] += file.length();
                    }
                    pBytesCopied[0] = file.length();
                    pFileName[0] = file.getPath();
                    map.put(file.getPath(), file);
                    destination.setText("To:" + archiveDecompressUtil.getDestination());
                    long w = file.length();
                    long speed = (Math.abs(w - pBytes[0]));
                    if (speed != 0)
                        average[0] = (int) ((speed + average[0]) * 0.5f);
                    pBytes[0] = w;
                    writeSpeed.setText(DiskUtils.getInstance().getSize(average[0]) + "/s");
                    int a = (int) (((float) map.size() / (float) archiveDecompressUtil.size()) * 100);
                    int b = (int) ((float) bytesCopied[0] / (float) zipFile.getFile().length() * 100);
                    int p = (int) ((a + b) * 0.5f);
                    long r = (long) ((float) (zipFile.getFile().length() - bytesCopied[0] + 1) / (float) average[0]);
                    remainingTime.setText("elapsed: " + DateUtils.getHoursMinutesSec(seconds) + " remaining: " + DateUtils.getHoursMinutesSec(r));
                    progress.setProgress(p);
                    percent.setText(p + "%");
                    if (!task.isRunning()) {
                        progress.setProgress(100);
                        percent.setText("100%");
                        remainingTime.setText("elapsed: " + DateUtils.getHoursMinutesSec(seconds) + " remaining: " + DateUtils.getHoursMinutesSec(0));
                        timer.stop();
                        cancel.setText("close");
                        if(archiveDecompressUtil.isError()){
                            cancel();
                        }
                    }
                }


            }

        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(task.isRunning()) {
                    cancel.setText("close");
                    task.cancel();
                }else
                    cancel();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        timer.start();
    }

    public String errorMessage(){
        return archiveDecompressUtil.getErrorMessage();
    }
}
