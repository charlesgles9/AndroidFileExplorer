package com.file.manager.Fragments;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.file.manager.R;
import com.file.manager.TapGesture;
import com.file.manager.Activities.VideoPlayerActivity;
import com.file.manager.utils.DateUtils;
import com.file.manager.utils.Timer;

import java.io.File;

public class VideoTabFragment extends Fragment {


    private VideoView videoView;
    private ToggleButton play;
    private Button rotate;
    private View controller;
    private File file;
    private boolean executeOnStart;
    private TapGesture tapGesture;
    private Timer timer;
    private Timer videoViewUpdates;
    private SeekBar seekTo;
    private TextView currentTime;
    private TextView endTime;
    private long length;
    private boolean isControllerVisible;
    private int stopPosition=0;
    private boolean paused=false;
    public VideoTabFragment(File file, TapGesture tapGesture){
        this.file=file;
        this.tapGesture=tapGesture;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root=inflater.inflate(R.layout.video_player_fragment,container,false);
        videoView=root.findViewById(R.id.video);
        play=root.findViewById(R.id.play);
        seekTo=root.findViewById(R.id.seekTo);
        rotate=root.findViewById(R.id.rotate);
        currentTime=root.findViewById(R.id.currentTime);
        endTime=root.findViewById(R.id.endTime);
        controller=root.findViewById(R.id.controller);
        play.setChecked(executeOnStart);
        showPlayButton(executeOnStart);
        videoView.setVideoPath(file.getPath());
         videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                play.setChecked(false);
               if(!isControllerVisible&&isVisible())
                tapGesture.onClick();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if(!videoView.isPlaying()) {
                  playVideo();
                  timer.start();
              }else {
                  videoView.pause();
              }

            }
        });

        timer= new Timer();
        timer.setIntervals(2);
        timer.setListener(new Timer.TimerListener() {
            @Override
            public void calculate(long seconds) {
                isControllerVisible=true;
                //prevents the play video ui from being suddenly in other fragments
                // since the tap gesture interface is shared
               if(videoView.isPlaying())
                tapGesture.onClick();
                timer.stop();
            }
        });

        length=getVideoLength();
        videoViewUpdates= new Timer();
        videoViewUpdates.setListener(new Timer.TimerListener() {
            @Override
            public void calculate(long seconds) {
               endTime.setText(DateUtils.getDateStringHHMMSS(length));
               currentTime.setText(DateUtils.getDateStringHHMMSS(videoView.getCurrentPosition()));
                if(videoView.getCurrentPosition()<length){
                seekTo.setProgress((int)((float)videoView.getCurrentPosition()/(float)length*100));
               }else{
                seekTo.setProgress(100);
                videoViewUpdates.stop();
                }

            }
        });

        seekTo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int stamp=(int)((float)length*(float)seekBar.getProgress()/100);
                videoView.seekTo(stamp);
            }
        });
        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoPlayerActivity activity=(VideoPlayerActivity)getContext();
                activity.changeOrientation();
            }
        });
        if(executeOnStart)playVideo();
        root.setOnTouchListener(tapGesture);
        executeOnStart=false;
        return root;
    }

    int tries=0;
    private long getVideoLength(){
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        try {
            retriever.setDataSource(file.getPath());
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return Long.parseLong(time);
        }catch (Exception e){
            //catch runtime exception & nullptr
            tries++;
            if(tries<=2)
            getVideoLength();
        }
        tries=0;
        return 0;
    }

    public void pauseVideo(){
        if(videoView!=null&&videoView.isPlaying()) {
            videoView.pause();
            videoViewUpdates.stop();
            showPlayButton(true);
            play.setChecked(false);
        }
    }

    public void playVideo(){
        if(videoView!=null&&!videoView.isPlaying()){
            timer.start();
            videoView.start();
            videoViewUpdates.start();
        }
    }

    public void showPlayButton(boolean isVisible){
        if(play!=null) {
            play.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
            controller.setVisibility(play.getVisibility());
            rotate.setVisibility(play.getVisibility());
            isControllerVisible=isVisible;

        }

    }

    public void skipFrames(int seconds){
        int position=videoView.getCurrentPosition()+seconds*1000;
        videoView.seekTo(position);
    }
    public void setControllerVisible(boolean controllerVisible) {
        isControllerVisible = controllerVisible;
    }

    public boolean getControllerVisibility() {
        return isControllerVisible;
    }


    public void setExecuteOnStart(boolean executeOnStart) {
        this.executeOnStart = executeOnStart;
    }

    public String getName(){
        return file.getName();
    }
    @Override
    public void onDetach() {
        super.onDetach();

    }


    @Override
    public void onResume() {
        super.onResume();
        if(paused) {
            videoView.seekTo(stopPosition);
            videoView.start();
            videoViewUpdates.start();
            paused=false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(videoView.isPlaying()) {
            videoView.pause();
            videoViewUpdates.stop();
            paused=true;
            stopPosition=videoView.getCurrentPosition();
        }
    }
}
