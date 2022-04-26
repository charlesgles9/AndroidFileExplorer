package com.file.manager.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.file.manager.BackgroundServices.MusicPlayerService;
import com.file.manager.R;
import com.file.manager.ui.Dialogs.AudioPlayListBottomSheet;

import com.file.manager.ui.Models.MusicHelperSingleton;
import com.file.manager.utils.DateUtils;
import com.file.manager.utils.FileHandleUtil;
import com.file.manager.utils.Timer;

import java.io.File;
import java.io.IOException;

public class MusicPlayerActivity extends AppCompatActivity {

    private ToggleButton playButton;
    private SeekBar seekBar;
    private TextView currentTime;
    private TextView endTime;
    private TextView title;
    private MusicHelperSingleton musicHelperSingleton;
    private int[]modeDrawables={R.drawable.ic_repeat_one,
                               R.drawable.ic_shuffle,
                               R.drawable.ic_repeat};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.argb(80,24,72,94));
        setContentView(R.layout.audio_player_activity);
        Button playList = findViewById(R.id.playlist);
        final Button modeButton=findViewById(R.id.mode);
        seekBar=findViewById(R.id.seekTo);
        currentTime=findViewById(R.id.currentTime);
        endTime=findViewById(R.id.endTime);
        title=findViewById(R.id.title);
        playButton=findViewById(R.id.play);
        Button next = findViewById(R.id.next);
        Button prev = findViewById(R.id.prev);
        title.setSelected(true);
        musicHelperSingleton = MusicHelperSingleton.getInstance();
        String externalPath=onSharedIntent(getIntent());
        if(externalPath!=null){
            FileHandleUtil.fetchAudioFiles(this,musicHelperSingleton.getData());
            musicHelperSingleton.setCurrentByFile(new File(externalPath));
            musicHelperSingleton.getAllSongs().addAll(musicHelperSingleton.getData());
        }
        musicHelperSingleton.setPlayList("AllMusic");
        final Context context=this;
        final AudioPlayListBottomSheet audioPlayListBottomSheet= new AudioPlayListBottomSheet();
        playList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayListBottomSheet.show(getSupportFragmentManager(),"TAG");
            }
        });
        Toolbar toolbar=findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        musicHelperSingleton.setCurrentVideoLength();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int stamp=(int)((float)musicHelperSingleton.getCurrentVideoLength()*(float)seekBar.getProgress()/100);
                 musicHelperSingleton.seekTo(stamp);
            }
        });


        final Timer timer= new Timer();
        setTextTitle();
        timer.setListener(new Timer.TimerListener() {
            @Override
            public void calculate(long seconds) {
                if(musicHelperSingleton.isReset()&musicHelperSingleton.isAlive()){
                    musicHelperSingleton.setCurrentVideoLength();
                    setTextTitle();
                    // update the playlist adapter or the music adapter
                    if(musicHelperSingleton.getPlayList().equals("PlayList"))
                       audioPlayListBottomSheet.getPlayListFragment().getPlayListAdapter().notifyDataSetChanged();
                    else
                        audioPlayListBottomSheet.getMusicListFragment().getAdapter().notifyDataSetChanged();
                    musicHelperSingleton.setReset(false);
                }
                if(isDestroyed()){
                    timer.stop();

                }
                if(musicHelperSingleton.isAlive()) {
                    endTime.setText(DateUtils.getDateStringHHMMSS(musicHelperSingleton.getCurrentVideoLength()));
                    currentTime.setText(DateUtils.getDateStringHHMMSS(musicHelperSingleton.getSeek()));
                    int p = (int) ((musicHelperSingleton.getSeek() + 1.0f) /
                            (1.0f + musicHelperSingleton.getCurrentVideoLength()) * 100);
                    seekBar.setProgress(p);
                }

                playButton.setChecked(musicHelperSingleton.isAlive()&&musicHelperSingleton.getMediaPlayer().isPlaying());
            }
        });

        playButton.setChecked(true);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!musicHelperSingleton.isAlive()&!musicHelperSingleton.isEmpty()){
                    musicHelperSingleton.createMediaPlayerInstance();
                    Intent intent= new Intent(context,MusicPlayerService.class);
                    startService(intent);
                }else
                musicHelperSingleton.setPaused(!playButton.isChecked());
                musicHelperSingleton.setUpdateNotification(true);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!musicHelperSingleton.isAlive()&!musicHelperSingleton.isEmpty())
                    return;
                // go to the next music file
                try {
                musicHelperSingleton.Next();
                musicHelperSingleton.setReset(true);
                musicHelperSingleton.setUpdateNotification(true);
                musicHelperSingleton.startPlayer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!musicHelperSingleton.isAlive()||musicHelperSingleton.isEmpty())
                    return;
                // go to the previous music file
                try {
                    musicHelperSingleton.Prev();
                    musicHelperSingleton.setReset(true);
                    musicHelperSingleton.setUpdateNotification(true);
                    musicHelperSingleton.startPlayer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        modeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderPreference(modeButton);
            }
        });
        timer.start();
        if(!MusicHelperSingleton.getInstance().isAlive()) {
            Intent intent = new Intent(this, MusicPlayerService.class);
            startService(intent);
        }
    }

    private String onSharedIntent(Intent intent){
        String rAction=intent.getAction();
        String rType=intent.getType();
        if(rAction!=null&&(rAction.equals(Intent.ACTION_SEND)|rAction.equals(Intent.ACTION_VIEW))){
            if(rType!=null){
                Uri uri;
                if(rAction.equals(Intent.ACTION_SEND))
                    uri=intent.getParcelableExtra(Intent.EXTRA_STREAM);
                else
                    uri=intent.getData();
                return FileHandleUtil.uriToFilePath(this,uri);
            }
        }
        return null;
    }

    private void orderPreference(final Button anchor){
        LayoutInflater inflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        final View view=inflater.inflate(R.layout.audio_mode_layout,null);
        view.measure(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        final PopupWindow popupWindow= new PopupWindow(view,view.getMeasuredWidth(),view.getMeasuredHeight(),true);
        popupWindow.showAsDropDown(anchor);
        final View.OnClickListener onClickListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.shuffle:
                        musicHelperSingleton.setMode(MusicHelperSingleton.SHUFFLE);
                        break;
                    case R.id.order:
                        musicHelperSingleton.setMode(MusicHelperSingleton.ORDER);
                        break;
                    case R.id.repeatOne:
                        musicHelperSingleton.setMode(MusicHelperSingleton.REPEAT);
                        break;

                }
                anchor.setBackgroundResource(modeDrawables[musicHelperSingleton.getMode()]);
                popupWindow.dismiss();
            }
        };
        view.findViewById(R.id.shuffle).setOnClickListener(onClickListener);
        view.findViewById(R.id.order).setOnClickListener(onClickListener);
        view.findViewById(R.id.repeatOne).setOnClickListener(onClickListener);
        musicHelperSingleton.setPlayList("AllMusic");
    }

    @SuppressLint("SetTextI18n")
    private void setTextTitle(){
        title.setText("\t\t\t\t" + musicHelperSingleton.getCurrentFileName().getValue() + "\t\t\t\t");
    }


    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }
}