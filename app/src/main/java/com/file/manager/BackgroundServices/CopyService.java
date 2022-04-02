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
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.file.manager.R;
import com.file.manager.ui.Models.CopyProgressMonitor;
import com.file.manager.ui.Models.CopyServiceQueue;
import com.file.manager.ui.utils.CopyUtility;
import com.file.manager.ui.utils.DateUtils;
import com.file.manager.ui.utils.Timer;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class CopyService extends IntentService  {

    private NotificationModel model;
    private Notification notification;
    private ArrayList<NotificationModel>notificationModels= new ArrayList<>();
    private NotificationManagerCompat notificationManagerCompact;
    private Intent updates= new Intent();

    public CopyService() {
        super("CopyService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
    @Override
    public void onCreate() {
        super.onCreate();


    }

    private void  startJob(final NotificationCompat.Builder builder, final RemoteViews layout1, final RemoteViews layout2, final NotificationModel model){


        final Timer timer= new Timer();
        timer.setListener(new Timer.TimerListener() {
            @Override
            public void calculate(long seconds) {
                CopyProgressMonitor monitor=model.getUtility().getProgressMonitor();
                String WriteSpeed=monitor.getWriteSpeedString(seconds);
                int rem=(int)((float)monitor.getWorkToBeDone()/(float)monitor.getWriteSpeedInt(seconds));
                String remainingTime= DateUtils.getHoursMinutesSec(Math.abs(rem-seconds));
                File file=model.getUtility().getCurrent();
                String name=file.getParentFile().getName()+"/"+file.getName();
                layout1.setTextViewText(R.id.title,"Copying... "+ name);
                layout1.setTextViewText(R.id.header,WriteSpeed+" "+remainingTime+" left");
                layout1.setTextViewText(R.id.percent,"("+monitor.getPercent()+"%)");
                layout1.setProgressBar(R.id.progress,100,monitor.getPercent(),false);
                layout2.setTextViewText(R.id.title,"Copying... "+name);
                layout2.setTextViewText(R.id.header,WriteSpeed+" "+remainingTime+" left");
                layout2.setTextViewText(R.id.percent,"("+monitor.getPercent()+"%)");
                layout2.setProgressBar(R.id.progress,100,monitor.getPercent(),false);
                layout2.setTextViewText(R.id.cancel,model.getUtility().isRunning()?"CANCEL":"CLOSE");
                notificationManagerCompact.notify(model.getId(),notification);
                updates.setAction(getPackageName());
                sendBroadcast(updates);
                if(!model.getUtility().isRunning()){
                    if(!model.getUtility().isCancelled()) {
                        layout2.setProgressBar(R.id.progress, 100, 100, false);
                        layout2.setTextViewText(R.id.header, WriteSpeed+" "+DateUtils.getHoursMinutesSec(Math.abs(0)) + " left ");
                        layout2.setTextViewText(R.id.percent,"("+100+"%)");
                        layout1.setTextViewText(R.id.percent,"("+100+"%)");
                        layout1.setProgressBar(R.id.progress,100,100,false);
                    }
                    notificationManagerCompact.notify(model.getId(),notification);
                    timer.stop();
                }
            }
        });

        model.getUtility().Execute();
        timer.start();

    }


    private void startNotification(){
        final RemoteViews layout1=new RemoteViews(getPackageName(),R.layout.notification_copy_small);
        final RemoteViews layout2=new RemoteViews(getPackageName(),R.layout.notification_copy_big);
        final NotificationCompat.Builder builder= new NotificationCompat.Builder(this,String.valueOf(model.getId()))
                .setSmallIcon(R.drawable.ic_launcher)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(layout1)
                .setCustomBigContentView(layout2)
                .setOnlyAlertOnce(true)
                 .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        createNotificationChannel();
        notificationManagerCompact=NotificationManagerCompat.from(getApplicationContext());
        Intent closeIntent=new Intent();
        closeIntent.setAction(String.valueOf(model.getId()));
        PendingIntent pIntent=PendingIntent.getBroadcast(this,0,closeIntent,0);
        layout2.setOnClickPendingIntent(R.id.cancel,pIntent);
        model.setBuilder(builder);
        notification=builder.build();
        notificationManagerCompact.notify(model.getId(), notification);
        startForeground(model.getId(), notification);
        startJob(builder,layout1,layout2,model);
        // prevents memory leaks of other unclosed notifications
        for(int i=0;i<notificationModels.size()-1;i++){
            NotificationModel pModel=notificationModels.get(i);
            startForeground(pModel.getId(),pModel.getBuilder().build());
        }

    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(String.valueOf(model.getId()), "Copy", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Copying background task");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
           model= new NotificationModel();
           model.initialize();
           notificationModels.add(model);
           startNotification();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class NotificationModel{
        private CopyUtility utility;
        private int Id;
        private BroadcastReceiver receiver;
        private NotificationCompat.Builder builder;
        public NotificationModel(){
            Id=UUID.randomUUID().hashCode();
            utility = CopyServiceQueue.getInstance().getFirst();
            CopyServiceQueue.getInstance().removeFirst();
        }

        public void cancel(){
          if(utility.getNewFiles().size()>0)
            updates.putExtra("folder",utility.getNewFiles().get(0).getParent());
            updates.setAction(getPackageName());
            utility.cancel();
        }

        public void remove(){
            for(int i=0;i<notificationModels.size();i++){
                if(this==notificationModels.get(i)) {
                    notificationModels.remove(i);
                    this.unregister();
                    break;
                }
            }
          if(notificationModels.isEmpty())
            stopSelf();
        }

        public void initialize(){
            IntentFilter filter = new IntentFilter();
            filter.addAction(String.valueOf(getId()));
            receiver=new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(utility.isRunning()){
                        cancel();
                    }else {
                        notificationManagerCompact.cancel(Id);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            stopForeground(STOP_FOREGROUND_REMOVE);

                        }
                        remove();
                    }

                }
            };
            registerReceiver(receiver, filter);
        }

        public void setBuilder(NotificationCompat.Builder builder) {
            this.builder = builder;
        }

        public NotificationCompat.Builder getBuilder() {
            return builder;
        }

        public int getId() {
            return Id;
        }

        public CopyUtility getUtility() {
            return utility;
        }

      public void unregister(){
            unregisterReceiver(receiver);
      }
    }

}
