package com.file.manager.utils;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

public class Timer extends Handler {
        private static final int TIMER_MESSAGE_ID=0x32;
        private boolean running=false;
        private long seconds=0;
        private TimerListener listener;
        private int intervals = 1000;
        public void start(){
            running=true;
            this.seconds+=1;
            removeMessages(TIMER_MESSAGE_ID,this);

            sendMessageDelayed(obtainMessage(TIMER_MESSAGE_ID,this), intervals);
        }

        public void stop(){
            running=false;
            reset();
            removeCallbacksAndMessages(this);
        }

        public void reset(){
           seconds=0;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(running&&(msg.what==TIMER_MESSAGE_ID)){
                running=false;
                start();
                listener.calculate(seconds);
            }
        }

    public void setIntervals(int seconds) {
        this.intervals = seconds*1000;
    }

    public void setIntervalsMillis(int intervals){
            this.intervals=intervals;
    }
    public void setListener(TimerListener listener) {
            this.listener = listener;
        }

        public interface TimerListener{
            void calculate(long seconds);
        }

}
