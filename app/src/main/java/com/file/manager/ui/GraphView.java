package com.file.manager.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GraphView extends View {

    private final Paint LinePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ColumnTextPaint= new Paint();
    private final Paint GridPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Wave>waveList= new ArrayList<>();
    private Handler handler= new Handler();
    private GraphRunnable runnable;
    private Path path= new Path();
    int x=0;
    int y=0;
    int index=0;
    public GraphView(Context context) {
        super(context);
        init();
    }

    public GraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public void init(){
        LinePaint.setColor(Color.RED);
        GridPaint.setColor(Color.GRAY);
        ColumnTextPaint.setColor(Color.BLUE);
        GridPaint.setStyle(Paint.Style.STROKE);
        LinePaint.setAntiAlias(true);
        LinePaint.setStrokeWidth(2f);
        GridPaint.setStrokeWidth(2f);
        ColumnTextPaint.setTextSize(18);


        runnable= new GraphRunnable();
        handler.postDelayed(runnable,300);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        int sp=getHeight()/5;
        int offsetX=10;
        int offsetY=10;
        if(waveList.isEmpty()){
            this.addWave(new GraphView.Wave(100,getWidth()));
        }

        // draw all other previous coordinates
        for(Wave wave:waveList){
            for(int i=0;i<wave.coordinates.length-1;i++){
                if(wave.coordinates[i]==null|wave.coordinates[i+1]==null)
                    continue;
                Pair<Float, Float>pair1=wave.coordinates[i];
                Pair<Float, Float>pair2=wave.coordinates[i+1];
                path.moveTo(pair1.first-10,pair1.second);
                path.lineTo(pair2.first-10,pair2.second);
            }
        }
        for(Wave wave:waveList){
            float amplitude=20;
            wave.x=index*amplitude*(float)Math.cos(90);
            wave.y=wave.frequency*index*(float)Math.cos(90);
            float quadrant=getHeight()-sp-wave.frequency;
            float y=quadrant+amplitude;
            path.moveTo(getWidth()-wave.x,y);
            path.lineTo(getWidth()-wave.x-amplitude,y);
            wave.set(index,new Pair<Float, Float>(getWidth()-wave.x-20,y));
        }

        index=(index+1)%waveList.get(0).coordinates.length;
        System.out.println(index);
        // draw the grid lines
        // draw columns
        path.moveTo(10,10);
        path.lineTo(getWidth()-50,10);
        path.moveTo(10,sp);
        path.lineTo(getWidth()-50,sp);
        path.moveTo(10,sp*2);
        path.lineTo(getWidth()-50,sp*2);
        path.moveTo(10,sp*3);
        path.lineTo(getWidth()-50,sp*3);
        path.moveTo(10,sp*4);
        path.lineTo(getWidth()-50,sp*4);

        // draw rows
        for(int i=0;i<6;i++){
            path.moveTo(offsetX+i*(getWidth()-50)/6.0f,10);
            path.lineTo(offsetX+i*(getWidth()-50)/6.0f,getHeight()-sp);
        }
        path.moveTo(getWidth()-50,10);
        path.lineTo(getWidth()-50,getHeight()-sp);

        // draw column markers
        canvas.drawText("100%",getWidth()-50+10,15,ColumnTextPaint);
        canvas.drawText("75%",getWidth()-50+10,sp,ColumnTextPaint);
        canvas.drawText("50%",getWidth()-50+10,sp*2,ColumnTextPaint);
        canvas.drawText("25%",getWidth()-50+10,sp*3,ColumnTextPaint);

        // draw row markers
        canvas.drawText("60",offsetX,getHeight()-sp+20,ColumnTextPaint);
        canvas.drawText("50",offsetX+(getWidth()-50)/6.0f,getHeight()-sp+20,ColumnTextPaint);
        canvas.drawText("40",offsetX+2*(getWidth()-50)/6.0f,getHeight()-sp+20,ColumnTextPaint);
        canvas.drawText("30",offsetX+3*(getWidth()-50)/6.0f,getHeight()-sp+20,ColumnTextPaint);
        canvas.drawText("20",offsetX+4*(getWidth()-50)/6.0f,getHeight()-sp+20,ColumnTextPaint);
        canvas.drawText("10",offsetX+5*(getWidth()-50)/6.0f,getHeight()-sp+20,ColumnTextPaint);
        canvas.drawText("0",offsetX+6*(getWidth()-50)/6.0f,getHeight()-sp+20,ColumnTextPaint);
        canvas.drawPath(path,GridPaint);


    }

    public void setWaveFreq(int position,int freq){
        waveList.get(position).setFrequency(freq);
    }

    public void addWave(Wave wave){
        waveList.add(wave);
    }


    public static class Wave {
        private int frequency=180;
        private  int pFrequency=180;
        private float x,y;
        private Pair<Float,Float>[]coordinates;
        public Wave(int frequency,int width){
            this.frequency=frequency;
            this.pFrequency=frequency;
            this.coordinates=new Pair[width/10];
        }
        public void set(int index,Pair<Float,Float> pair) {
             coordinates[index]=pair;
        }
        public Pair<Float, Float> getLast() {
            return coordinates[coordinates.length-1];
        }
        public Pair<Float, Float> getFirst() {
            return coordinates[0];
        }
        public void setFrequency(int frequency) {
            this.frequency = frequency;
        }

        public int getFrequency() {
            return frequency;
        }

        public int amplitude(){
            int diff= frequency-pFrequency;
            pFrequency=frequency;
            return diff;
        }

        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }
    }
    private class GraphRunnable implements Runnable{

        @Override
        public void run() {
            path.reset();
            invalidate();
            handler.postDelayed(runnable,300);
        }
    }
}
