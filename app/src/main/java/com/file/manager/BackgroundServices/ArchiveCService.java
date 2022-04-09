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
import android.os.AsyncTask;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.file.manager.R;
import com.file.manager.utils.ArchiveCompressUtil;

import com.file.manager.utils.DateUtils;
import com.file.manager.utils.DiskUtils;
import com.file.manager.utils.Selector;
import com.file.manager.utils.Timer;

import java.util.ArrayList;
import java.util.UUID;

public class ArchiveCService extends IntentService {

    private final ArrayList<NotificationModel> notifications=new ArrayList<>();
    private Notification notification;
    private NotificationManagerCompat notificationManagerCompact;
    private final Intent updates= new Intent();

    public ArchiveCService(){
        super("ArchiveCompression");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void startWorkerThread(final NotificationCompat.Builder builder, final RemoteViews layout1, final RemoteViews layout2, final NotificationModel model){
        final Timer timer= new Timer();
        final long[] average={1L};
        final long[]pBytes={1L};
        final ArchiveCompressUtil archiveCompressUtil=model.getUtil();
        timer.setListener(new Timer.TimerListener() {
            @Override
            public void calculate(long seconds) {
                String name=archiveCompressUtil.getProgressMonitor().getFileName();
                String writeSpeed="";
                String remainingTime="";
                layout1.setTextViewText(R.id.title,"Compressing... "+ name);
                layout2.setTextViewText(R.id.title,"Compressing... "+name);
                float w=archiveCompressUtil.getZipFile().getFile().length();
                float t=archiveCompressUtil.getSize();
                int p=(int)((w/t)*100);
                long speed=(archiveCompressUtil.getZipFile().getFile().length()-pBytes[0]);
                speed=(speed+average[0])/2;
                average[0]=speed;
                writeSpeed=DiskUtils.getInstance().getSize(speed)+"/s";
                long rem=(long)(float)(archiveCompressUtil.getSize()/(float)(speed));
                remainingTime= DateUtils.getHoursMinutesSec(rem);
                pBytes[0]=archiveCompressUtil.getZipFile().getFile().length();
                layout2.setTextViewText(R.id.header,writeSpeed+" "+remainingTime+" left");
                layout2.setProgressBar(R.id.progress,100,p,false);
                layout2.setTextViewText(R.id.percent,"("+p+"%)");
                layout1.setTextViewText(R.id.header,writeSpeed+" "+remainingTime+" left");
                layout1.setProgressBar(R.id.progress,100,p,false);
                layout1.setTextViewText(R.id.percent,"("+p+"%)");
                layout2.setTextViewText(R.id.cancel,archiveCompressUtil.isRunning()?"CANCEL":"CLOSE");
                notificationManagerCompact.notify(model.getId(),notification);
                if(!archiveCompressUtil.isRunning()){
                    layout2.setProgressBar(R.id.progress, 100, 100, false);
                    layout2.setTextViewText(R.id.header, writeSpeed+" "+DateUtils.getHoursMinutesSec(Math.abs(0)) + " left ");
                    layout2.setTextViewText(R.id.percent,"("+100+"%)");
                    layout1.setTextViewText(R.id.percent,"("+100+"%)");
                    layout1.setProgressBar(R.id.progress,100,100,false);
                    notificationManagerCompact.notify(model.getId(),notification);
                    model.cancel();
                    timer.stop();
                }
            }
        });

        archiveCompressUtil.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        timer.start();
    }

    private void startNotification(NotificationModel model){
        final RemoteViews layout1=new RemoteViews(getPackageName(), R.layout.archive_notification_small);
        final RemoteViews layout2=new RemoteViews(getPackageName(),R.layout.archive_notification_big);
        NotificationCompat.Builder builder= new NotificationCompat.Builder(this,String.valueOf(model.getId()))
                .setSmallIcon(R.drawable.ic_launcher)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(layout1)
                .setCustomBigContentView(layout2)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        createNotificationChannel(model);
        notificationManagerCompact= NotificationManagerCompat.from(getApplicationContext());
        Intent closeIntent=new Intent();
        closeIntent.setAction(String.valueOf(model.getId()));
        PendingIntent pIntent=PendingIntent.getBroadcast(this,0,closeIntent,0);
        layout2.setOnClickPendingIntent(R.id.cancel,pIntent);
        notification=builder.build();
        notificationManagerCompact.notify(model.getId(), notification);
        startForeground(model.getId(), notification);
        startWorkerThread(builder,layout1,layout2,model);
    }

    private void createNotificationChannel(NotificationModel model){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(String.valueOf(model.getId()), "Archive Compress", NotificationManager.IMPORTANCE_MIN);
            channel.setDescription("Archive Compress background task");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        notifications.add(new NotificationModel(Selector.getInstance().createNewInstance()));
        notifications.get(notifications.size()-1).start();
        Selector.getInstance().clear();
        return START_NOT_STICKY;
    }


    class NotificationModel{
        private final int Id;
        private BroadcastReceiver receiver;
        private final Selector selector;
        public NotificationModel(Selector selector){
            this.selector=selector;
            this.Id= UUID.randomUUID().hashCode();
        }
        public void remove(){
            for(int i=0;i<notifications.size();i++){
                if(this.equals(notifications.get(i))) {
                    notifications.remove(i);
                    unregister();
                    break;
                }
            }
            if(notifications.isEmpty())
                stopSelf();
        }

        public void start(){
            initialize();
            startNotification(this);
        }

        private void cancel(){
            updates.putExtra("folder",getUtil().getToLocation().getPath());
            updates.setAction(getPackageName());
        }

        private void initialize(){
            IntentFilter filter = new IntentFilter();
            filter.addAction(String.valueOf(getId()));
            final ArchiveCompressUtil utility=getUtil();
            receiver=new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(utility.isRunning()){
                        utility.cancel();
                    }else {
                        notificationManagerCompact.cancel(Id);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            stopForeground(STOP_FOREGROUND_REMOVE);

                        }
                        cancel();
                        remove();
                    }

                }
            };
            registerReceiver(receiver, filter);
        }
        public void unregister(){
            unregisterReceiver(receiver);
        }
        public int getId() {
            return Id;
        }
        public ArchiveCompressUtil getUtil(){
            return (ArchiveCompressUtil) selector.get(0);
        }
    }
}
