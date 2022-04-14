package com.file.manager.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class GraphView extends View {

    private final Paint LinePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint GridPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private Handler handler;
    private GraphRunnable runnable;
    int x=0;
    int y=0;

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
        GridPaint.setColor(Color.WHITE);
        runnable= new GraphRunnable();
        handler.postDelayed(runnable,16);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(int i=0;i<10;i++)
        canvas.drawLine(0,50*i,50,50*i+100,LinePaint);


    }

    private class GraphRunnable implements Runnable{

        @Override
        public void run() {
            invalidate();
            handler.postDelayed(runnable,16);
        }
    }
}
