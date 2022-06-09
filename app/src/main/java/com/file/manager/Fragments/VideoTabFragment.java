package com.file.manager.Fragments;

import android.app.Activity;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.file.manager.R;
import com.file.manager.ui.TapGesture;
import com.file.manager.Activities.VideoPlayerActivity;
import com.file.manager.utils.DateUtils;
import com.file.manager.utils.Timer;

import java.io.File;
import java.io.IOException;

public class VideoTabFragment extends Fragment {


    private SurfaceView videoView;
    private Activity activity;
    private MediaPlayer player;
    private ToggleButton play;
    private Button subtitle;
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
        subtitle=root.findViewById(R.id.subtitle);
        activity=getActivity();
        play=root.findViewById(R.id.play);
        seekTo=root.findViewById(R.id.seekTo);
        rotate=root.findViewById(R.id.rotate);
        currentTime=root.findViewById(R.id.currentTime);
        endTime=root.findViewById(R.id.endTime);
        controller=root.findViewById(R.id.controller);
        play.setChecked(executeOnStart);
        showPlayButton(executeOnStart);
        player=new MediaPlayer();

        videoView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                player.setDisplay(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                   handleAspectRatio();

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        try {
            player.setDataSource(file.getPath());
            player.prepare();
            fetchSubtitle();
        } catch (IOException e) {
            e.printStackTrace();
            getActivity().finish();
        }

         player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
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
              if(!player.isPlaying()) {
                  playVideo();
                  timer.start();
              }else {
                  player.pause();
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
               if(player.isPlaying())
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
               currentTime.setText(DateUtils.getDateStringHHMMSS(player.getCurrentPosition()));
                if(player.getCurrentPosition()<length){
                seekTo.setProgress((int)((float)player.getCurrentPosition()/(float)length*100));
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
                player.seekTo(stamp);
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

    private void fetchSubtitle() throws IOException {
        MediaPlayer player=new MediaPlayer();
        player.setDataSource(file.getPath());
        player.prepare();
        final MediaPlayer.TrackInfo[]infos=player.getTrackInfo();
        for (MediaPlayer.TrackInfo info : infos) {
            if(info.getTrackType()== MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE){
                final String mime=info.getFormat().getString(MediaFormat.KEY_MIME);
                System.out.println("MIME "+mime);
                Toast.makeText(getContext(),mime,Toast.LENGTH_LONG).show();
            }
        }
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
        if(player!=null&&player.isPlaying()) {
            player.pause();
            videoViewUpdates.stop();
            showPlayButton(true);
            play.setChecked(false);
        }
    }

    public void playVideo(){
        if(player!=null&&!player.isPlaying()){
            timer.start();
            player.start();
            videoViewUpdates.start();
        }
    }

    public void showPlayButton(boolean isVisible){
        if(play!=null) {
            play.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
            controller.setVisibility(play.getVisibility());
            rotate.setVisibility(play.getVisibility());
            isControllerVisible=isVisible;
            subtitle.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);

        }

    }

    public void skipFrames(int seconds){
        int position=player.getCurrentPosition()+seconds*1000;
        player.seekTo(position);
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
            player.seekTo(stopPosition);
            player.start();
            videoViewUpdates.start();
            paused=false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(player.isPlaying()) {
            player.pause();
            videoViewUpdates.stop();
            paused=true;
            stopPosition=player.getCurrentPosition();
        }
    }

    private void handleAspectRatio(){
        DisplayMetrics metrics= new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int sWidth=metrics.widthPixels;
        int sHeight=metrics.heightPixels;
        float video_width=player.getVideoWidth();
        float video_height=player.getVideoHeight();
        float ratio_width=sWidth/video_width;
        float ratio_height=sHeight/video_height;
        float aspect=video_width/video_height;
        ViewGroup.LayoutParams layoutParams=videoView.getLayoutParams();

        if(sWidth<sHeight) {
            layoutParams.width = (int) (sWidth < video_width ? sWidth / ratio_width : sWidth);
            layoutParams.height = (int) ( sHeight>video_height?sHeight/ratio_height:sHeight );
            Toast.makeText(getContext(),"Width high",Toast.LENGTH_SHORT).show();
        }else {
            layoutParams.width= (int)(sWidth>video_width?sWidth/ratio_width:sWidth*aspect);
            layoutParams.height=(int)(sHeight<video_height?sHeight/ratio_height:sHeight*aspect);
            Toast.makeText(getContext(),"Height high",Toast.LENGTH_SHORT).show();
        }
        videoView.setLayoutParams(layoutParams);
    }
}
