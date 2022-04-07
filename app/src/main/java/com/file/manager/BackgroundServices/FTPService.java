package com.file.manager.BackgroundServices;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.file.manager.R;
import com.file.manager.ui.Models.FtpServerInstance;
import com.file.manager.utils.Timer;


public class FTPService extends IntentService {

    private NotificationManagerCompat notificationManagerCompact;
    private NotificationCompat.Builder builder;
    private Notification notification;
    private IntentFilter filter= new IntentFilter();
    private int ID=0x86;
    public FTPService(){
        super("FTPServer");

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    final BroadcastReceiver receiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!FtpServerInstance.getInstance().isRunning()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE);
                }
                notificationManagerCompact.cancel(ID);
                stopSelf();
            }
            FtpServerInstance.getInstance().stop();
        }
    };

    private void serviceWatcher(final RemoteViews small,final RemoteViews big){
        final Timer timer= new Timer();
        final FtpServerInstance ftpServerInstance=FtpServerInstance.getInstance();

        timer.setListener(new Timer.TimerListener() {
            @Override
            public void calculate(long seconds) {
                boolean running=ftpServerInstance.isRunning();
                small.setTextViewText(R.id.status,running?"Running":"Stopped");
                big.setTextViewText(R.id.status,running?"Running":"Stopped");
                big.setTextViewText(R.id.serverIP,ftpServerInstance.getIpAddress());
                big.setTextViewText(R.id.port,String.valueOf(ftpServerInstance.getPort()));
                big.setTextViewText(R.id.password,ftpServerInstance.getPassword());
                if(!running){
                    big.setTextViewText(R.id.cancel,"CLOSE");
                  if(ftpServerInstance.getLiveData().hasActiveObservers())
                    ftpServerInstance.getLiveData().setValue(true);
                    ftpServerInstance.stop();
                    timer.stop();
                }
                notificationManagerCompact.notify(ID,notification);
            }
        });

        timer.start();
    }

    private void start(){
        final RemoteViews layout1=new RemoteViews(getPackageName(), R.layout.notification_ftp_small);
        final RemoteViews layout2=new RemoteViews(getPackageName(),R.layout.notification_ftp_big);
         builder= new NotificationCompat.Builder(this,String.valueOf(ID))
                .setSmallIcon(R.drawable.ic_ftp_server_icon)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(layout1)
                .setCustomBigContentView(layout2)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        createNotificationChannel();
        notificationManagerCompact= NotificationManagerCompat.from(getApplicationContext());
        Intent closeIntent=new Intent();
        closeIntent.setAction(String.valueOf(ID));
        PendingIntent pIntent=PendingIntent.getBroadcast(this,0,closeIntent,0);
        layout2.setOnClickPendingIntent(R.id.cancel,pIntent);
        filter.addAction(String.valueOf(ID));
        registerReceiver(receiver,filter);
        notification=builder.build();
        startForeground(ID, notification);
        serviceWatcher(layout1,layout2);
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(String.valueOf(ID), "FTP SERVER", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("FTP SERVER SERVICE");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

        start();
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }



}
