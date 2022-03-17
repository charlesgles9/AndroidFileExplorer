package com.file.manager.ui.Models;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;

import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MusicHelperSingleton {
    private MediaPlayer mediaPlayer;
    private ArrayList<CustomFile> data;
    private ArrayList<CustomFile>allSongs;
    private int current=0;
    private int currentVideoLength;
    private MutableLiveData<String> currentFileName;
    private boolean alive;
    private boolean looping;
    private boolean reset;
    private boolean updateNotification;
    private String playList;
    public static final int REPEAT=0;
    public static final int SHUFFLE=1;
    public static final int ORDER=2;
    private  int MODE;
    private static MusicHelperSingleton instance= new MusicHelperSingleton();
    private MusicHelperSingleton(){
        data= new ArrayList<>();
        allSongs= new ArrayList<>();
        currentFileName= new MutableLiveData<>(" ");
        setMode(ORDER);
        createMediaPlayerInstance();

    }

    public void add(ArrayList<CustomFile>files){
        for(int i=0;i<files.size();i++) {
            // ignore ogg files
            if(files.get(i).getExtension().equals(".ogg"))
                continue;
            this.data.add(files.get(i));
            this.allSongs.add(files.get(i));
        }
    }

    public void add(String file){
        this.data.add(new CustomFile(file));
    }

    public ArrayList<CustomFile>getData(){
        return data;
    }

    public ArrayList<CustomFile> getAllSongs() {
        return allSongs;
    }

    public String getCurrentUri(){
        return data.get(current).getPath();
    }

    public CustomFile getCurrentFile(){
        return data.get(current);
    }
    public void setCurrent(int current) {
        this.current = current;
        this.currentFileName.setValue(new File((getCurrentUri())).getName());
    }

    public void setCurrentByFile(File current){
        for(int i=0;i<getData().size();i++){
            CustomFile file=getData().get(i);
            if(file.getPath().equals(current.getPath())){
                setCurrent(i);
                break;
            }
        }
    }
    public int getCurrent() {
        return current;
    }

    public void Next(){
        switch (MODE){
            case SHUFFLE:
                Random random=new Random();
                current=current<data.size()?current:0;
                int size=Math.max(data.size()-current,1);
                int w=current+random.nextInt(size);
                this.current=Math.min(w,data.size()-1);
                break;
            case ORDER:
                this.current=(current+1)%data.size();
                break;
            case REPEAT:
                //just ignore
                break;
        }
        this.currentFileName.setValue(new File((getCurrentUri())).getName());
    }

    public void Prev(){
        switch (MODE){
            case SHUFFLE:
                Random random=new Random();
                current=current<data.size()?current:0;
                int size=Math.max(data.size()-current,1);
                int w=current+random.nextInt(size);
                this.current=Math.min(w,data.size()-1);
                break;
            case ORDER:
                this.current=Math.max(current-1,0);
                break;
            case REPEAT:
                //just ignore instead just loop
                break;
        }
        this.currentFileName.setValue(new File((getCurrentUri())).getName());
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    private int timeStamp=0;
    public void setPaused(boolean paused) {
       if(paused) {
           timeStamp=mediaPlayer.getCurrentPosition();
           mediaPlayer.pause();
       } else{
           mediaPlayer.start();
           mediaPlayer.seekTo(timeStamp);
       }
    }

    public int getMode() {
        return MODE;
    }

    public void setMode(int MODE) {
       this.MODE = MODE;
    }

    public void setReset(boolean reset) {
        this.reset = reset;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void seekTo(int timeStamp) {
        mediaPlayer.seekTo(timeStamp);
    }

    public void setCurrentVideoLength() {
        this.currentVideoLength =(int)getMusicLength();
    }

    public int getCurrentVideoLength() {
        return currentVideoLength;
    }

    private long getMusicLength(){
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        try {
            retriever.setDataSource(getCurrentUri());
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return Long.parseLong(time);
        }catch (Exception e){
            //catch runtime exception & nullptr
        }
        return 0;
    }

    public MutableLiveData<String> getCurrentFileName() {
        return currentFileName;
    }

    public int getSeek() {
        return mediaPlayer.getCurrentPosition();
    }

    public boolean isReset() {
        return reset;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isLooping() {
        return looping;
    }

    public static MusicHelperSingleton getInstance() {
        return instance;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void createMediaPlayerInstance(){
        mediaPlayer= new MediaPlayer();
    }
    public void startPlayer() throws IOException {
        try {
            mediaPlayer.reset();
        }catch (IllegalStateException e){
            mediaPlayer= new MediaPlayer();
        }
        mediaPlayer.setDataSource(getCurrentUri());
        mediaPlayer.prepare();
        mediaPlayer.start();
        setAlive(true);
    }

    public void setUpdateNotification(boolean updateNotification) {
        this.updateNotification = updateNotification;
    }

    public boolean isUpdateNotification() {
        return updateNotification;
    }

    public boolean isEmpty(){
        return data.isEmpty();
    }

    public void setPlayList(String playList) {
        this.playList = playList;
    }

    public String getPlayList() {
        return playList;
    }

    public void clear(){
        data.clear();
        current=0;
    }

}
