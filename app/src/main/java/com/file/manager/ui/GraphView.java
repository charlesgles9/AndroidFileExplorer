package com.file.manager.ui;

import android.annotation.SuppressLint;
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

import com.file.manager.utils.CpuStats;

import java.util.ArrayList;
import java.util.List;

public class GraphView extends View {

    private final Paint LinePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ColumnTextPaint= new Paint();
    private final Paint GridPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private Handler handler= new Handler();
    private GraphRunnable runnable;
    private CpuStats cpuStats= new CpuStats();
    private Pair<Float,Float>[]coordinates;
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

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        int sp=getHeight()/5;
        int lw=10;
        int offsetX=10;
        int offsetY=10;
         if(coordinates==null){
             coordinates= new Pair[getWidth()/10];
         }

         if(!cpuStats.isRunning())
             cpuStats.execute();

         if(!cpuStats.getCpuInfo().isEmpty()){
             coordinates[index]=new Pair<Float, Float>((float)index,(float)cpuStats.getCpuInfo().get(0).getCurUsage());
             for(int i=0;i<coordinates.length-1;i++){
                 if(coordinates[i]==null||coordinates[i+1]==null)
                     continue;
                 Pair<Float,Float>a=coordinates[i];
                 Pair<Float,Float>b=coordinates[i+1];
                 float x1=Math.abs(index-a.first)*lw;
                 float x2=Math.abs(index-b.first)*lw;;
                 path.moveTo(x1+lw,a.second);
                 path.lineTo(x1,b.second);
             }


         }

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


    private class GraphRunnable implements Runnable{

        @Override
        public void run() {
            path.reset();
            invalidate();
            handler.postDelayed(runnable,300);
        }
    }
}
