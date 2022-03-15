package com.file.manager.ui.Models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

import androidx.fragment.app.Fragment;

public class WindowModel {

    private int Id;
    private Fragment fragment;
    private Bitmap bitmap;
    private String path;
    private String title;
    private boolean active;
    public WindowModel(int Id, Fragment fragment){
        this.Id=Id;
        this.fragment=fragment;
    }


    private Bitmap loadBitmapFromView(View root){
        if(bitmap==null)
         bitmap=Bitmap.createBitmap(root.getWidth(),root.getHeight(), Bitmap.Config.ARGB_8888);
        else bitmap.eraseColor(0);
        Canvas canvas= new Canvas(bitmap);
        root.draw(canvas);
        return bitmap;
    }



    public void setView(View root) {
        if(root!=null)
            if(root.getWidth()!=0&root.getHeight()!=0)
           loadBitmapFromView(root);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return Id;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public Bitmap getView() {
        return bitmap;
    }
}
