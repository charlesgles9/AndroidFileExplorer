package com.file.manager.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class SoftwareKeyboardListener extends RelativeLayout {

    private Listener listener;
    public SoftwareKeyboardListener(Context context, AttributeSet attrs){
        super(context,attrs);
    }


    public void setListener(Listener listener){
        this.listener=listener;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int height=MeasureSpec.getSize(heightMeasureSpec);
        Activity activity=(Activity)getContext();
        Rect rect=new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int statusBarHeight=rect.top;
        int screenHeight=activity.getWindowManager().getDefaultDisplay().getHeight();
        int diff=(screenHeight-statusBarHeight)-height;
        if(listener!=null){
            listener.onSoftKeyboardShown(diff>128);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public interface Listener{
        void onSoftKeyboardShown(boolean showing);
    }
}
