package com.file.manager;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.GestureDetectorCompat;
public  class TapGesture implements View.OnTouchListener{
        private final GestureDetectorCompat gestureDetector;
        public TapGesture(Context context){
            gestureDetector= new GestureDetectorCompat(context, new TapGesture.GestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        public void onTap(){

        }

        public void onTap(MotionEvent event){

        }
        public void onClick(){

        }
        private class GestureListener extends GestureDetector.SimpleOnGestureListener{

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                onClick();
                return super.onSingleTapUp(e);
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                onTap();
                onTap(e);
                return super.onDoubleTap(e);
            }

        }
}
