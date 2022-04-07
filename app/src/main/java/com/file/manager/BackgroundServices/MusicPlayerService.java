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
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.file.manager.R;
import com.file.manager.ui.Models.MusicHelperSingleton;
import com.file.manager.utils.Timer;
import java.io.IOException;

public class MusicPlayerService extends IntentService {
    private NotificationManagerCompat notificationManagerCompact;
    private NotificationCompat.Builder builder;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private Notification notification;
    private RemoteViews small;
    private MusicHelperSingleton singleton;
    private Intent closeIntent= new Intent();
    private Intent playIntent= new Intent();
    private Intent nextIntent = new Intent();
    private Intent prevIntent= new Intent();
    private IntentFilter filter= new IntentFilter();
    private int ID=0x56;
    public MusicPlayerService(){
        super("MusicPlayer");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }


    final BroadcastReceiver button= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // close the notification and stop the service or pause
            if(intent.getAction().equals("close")&singleton.isAlive()) {
                singleton.getMediaPlayer().stop();
                singleton.setAlive(false);
                notificationManagerCompact.cancel(ID);
                stopSelf();
            }else if(intent.getAction().equals("play")) {
                singleton.setPaused(singleton.getMediaPlayer().isPlaying());
                updateNotification();
            }else if(intent.getAction().equals("next")&singleton.isAlive()&!singleton.isEmpty()){
                // go to the next music file
                singleton.Next();
                singleton.setReset(true);
                startPlayer();
                singleton.setUpdateNotification(true);
            }else if(intent.getAction().equals("prev")&singleton.isAlive()&!singleton.isEmpty()){
                // go to the next music file
                singleton.Prev();
                singleton.setReset(true);
                startPlayer();
                singleton.setUpdateNotification(true);
            }
        }
    };


    private int[] res ={R.drawable.ic_play_clip,R.drawable.ic_pause_clip};
    private void updateNotification(){
        small.setImageViewResource(R.id.play,singleton.getMediaPlayer().isPlaying()?res[1]:res[0]);
        small.setTextViewText(R.id.title,singleton.getCurrentFileName().getValue());
        notificationManagerCompact.notify(ID,notification);
    }

    private void progressWatcher(){
        final Timer timer= new Timer();
        timer.setListener(new Timer.TimerListener() {
            @Override
            public void calculate(long seconds) {
                if(!singleton.isAlive()){
                    timer.stop();
                    return;
                }
                if(singleton.isUpdateNotification()) {
                    updateNotification();
                    singleton.setUpdateNotification(false);
                }
            }
        });
        timer.start();
        singleton.getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // go to the next music file
                singleton.Next();
                singleton.setReset(true);
                startPlayer();
            }
        });

    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    private void startPlayer() {
        try {
          singleton.startPlayer();
          singleton.setUpdateNotification(true);
        }catch (IOException io){
            Toast.makeText(getApplicationContext(),"an Error occurred!",Toast.LENGTH_LONG).show();
            singleton.setAlive(false);
            stopSelf();
        }

    }

    private void initNotification(){
        small=new RemoteViews(getPackageName(), R.layout.notification_audio_small);
        builder= new NotificationCompat.Builder(this,String.valueOf(ID))
                .setSmallIcon(R.drawable.ic_audio_icon_home)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(small)
                .setCustomBigContentView(small)
                .setNotificationSilent()
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        createNotificationChannel();
        notificationManagerCompact= NotificationManagerCompat.from(getApplicationContext());
        closeIntent.setAction("close");
        playIntent.setAction("play");
        nextIntent.setAction("next");
        prevIntent.setAction("prev");
        PendingIntent pIntentClose=PendingIntent.getBroadcast(this,0,closeIntent,0);
        PendingIntent pIntentPlay=PendingIntent.getBroadcast(this,0,playIntent,0);
        PendingIntent pIntentNext=PendingIntent.getBroadcast(this,0,nextIntent,0);
        PendingIntent pIntentPrev=PendingIntent.getBroadcast(this,0,prevIntent,0);
        small.setOnClickPendingIntent(R.id.close,pIntentClose);
        small.setOnClickPendingIntent(R.id.play,pIntentPlay);
        small.setOnClickPendingIntent(R.id.next,pIntentNext);
        small.setOnClickPendingIntent(R.id.prev,pIntentPrev);
        filter.addAction("close");
        filter.addAction("play");
        filter.addAction("next");
        filter.addAction("prev");
        registerReceiver(button,filter);
        small.setTextViewText(R.id.title,singleton.getCurrentFileName().getValue());
        notification=builder.build();
        startForeground(ID, notification);
        updateNotification();
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(String.valueOf(ID), "AUDIO SERVER", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("AUDIO SERVICE");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void initAudioFocus(){
        audioManager= (AudioManager)getSystemService(AUDIO_SERVICE);
        AudioAttributes attributes= new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                        @Override
                        public void onAudioFocusChange(int focusChange) {
                            if(focusChange==AudioManager.AUDIOFOCUS_GAIN){
                                singleton.setPaused(false);
                            }else{
                                singleton.setPaused(true);
                            }
                            singleton.setUpdateNotification(true);

                        }
                    }).build();
            audioManager.requestAudioFocus(audioFocusRequest);
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        try {
            singleton =MusicHelperSingleton.getInstance();
            startPlayer();
            initNotification();
            progressWatcher();
        }catch (NullPointerException e){
            Toast.makeText(getApplicationContext(),"an Error occurred try again!",Toast.LENGTH_LONG).show();
            stopSelf();
        }
        // set audio focus
         initAudioFocus();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(singleton.getMediaPlayer()!=null) {
            singleton.getMediaPlayer().release();
        }
        unregisterReceiver(button);
    }
}
