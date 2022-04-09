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
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.file.manager.R;
import com.file.manager.utils.ArchiveDecompressUtil;
import com.file.manager.utils.DateUtils;
import com.file.manager.utils.DiskUtils;
import com.file.manager.utils.Selector;
import com.file.manager.utils.Timer;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArchiveDService extends IntentService {

    private final ArrayList<NotificationModel>notifications= new ArrayList<>();
    private Notification notification;
    private NotificationManagerCompat notificationManagerCompact;
    private final Intent updates= new Intent();

    public ArchiveDService(){
        super("ArchiveService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }


    private void startWorkerThread(final NotificationCompat.Builder builder, final RemoteViews layout1, final RemoteViews layout2, final NotificationModel model){

        final Timer timer= new Timer();
        final long[]pBytes={1};
        final long[]average={1};
        final long[] bytesCopied={1};
        final long[] pBytesCopied={1};
        final String[] pFileName ={""};
        final ArchiveDecompressUtil archiveDecompressUtil=model.getUtil();
        final ArchiveDecompressUtil.ExtractAllTask task= archiveDecompressUtil.extract();
        final Map<String, File> map= new HashMap<>();
        timer.setListener(new Timer.TimerListener() {
            @Override
            public void calculate(long seconds) {
                ZipFile zipFile = archiveDecompressUtil.getZipFile();
                String writeSpeed="";
                String remainingTime="";
                String name=zipFile.getProgressMonitor().getFileName();
                layout1.setTextViewText(R.id.title,"Extracting... "+ name);
                layout2.setTextViewText(R.id.title,"Extracting... "+name);
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
                    long w = file.length();
                    long speed = (Math.abs(w - pBytes[0]));
                    if (speed != 0)
                        average[0] = (int) ((speed + average[0]) * 0.5f);
                    pBytes[0] = w;
                    writeSpeed=DiskUtils.getInstance().getSize(average[0]) + "/s";
                    int a = (int) (((float) map.size() / (float) archiveDecompressUtil.size()) * 100);
                    int b = (int) ((float) bytesCopied[0] / (float) zipFile.getFile().length() * 100);
                    int p = (int) ((a + b) * 0.5f);
                    long r = (long) ((float) (zipFile.getFile().length() - bytesCopied[0] + 1) / (float) average[0]);
                    remainingTime= DateUtils.getHoursMinutesSec(r);
                    layout2.setTextViewText(R.id.header,writeSpeed+" "+remainingTime+" left");
                    layout2.setProgressBar(R.id.progress,100,p,false);
                    layout2.setTextViewText(R.id.percent,"("+p+"%)");
                    layout1.setTextViewText(R.id.header,writeSpeed+" "+remainingTime+" left");
                    layout1.setProgressBar(R.id.progress,100,p,false);
                    layout1.setTextViewText(R.id.percent,"("+p+"%)");
                    layout2.setTextViewText(R.id.cancel,task.isRunning()?"CANCEL":"CLOSE");
                    notificationManagerCompact.notify(model.getId(),notification);
                    if (!task.isRunning()) {
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


            }

        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        startForeground(model.getId(),notification);
        startWorkerThread(builder,layout1,layout2,model);
    }

    private void createNotificationChannel(NotificationModel model){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(String.valueOf(model.getId()), "Archive Decompress", NotificationManager.IMPORTANCE_MIN);
            channel.setDescription("Archive Decompress background task");
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


    public class NotificationModel{
        private final int Id;
        private BroadcastReceiver receiver;
        private final Selector selector;
        public NotificationModel( Selector selector){
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
        public void cancel(){
            final ArchiveDecompressUtil utility=(ArchiveDecompressUtil)selector.get(0);
            updates.putExtra("folder",utility.getDestination());
            updates.setAction(getPackageName());
        }
        private void initialize(){
            IntentFilter filter = new IntentFilter();
            filter.addAction(String.valueOf(getId()));
            final ArchiveDecompressUtil utility=(ArchiveDecompressUtil)selector.get(0);
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

        public ArchiveDecompressUtil getUtil(){
            return (ArchiveDecompressUtil)selector.get(0);
        }
    }
}
