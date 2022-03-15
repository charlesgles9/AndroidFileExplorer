package com.file.manager.ui.Models;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.file.manager.R;
import com.file.manager.ui.utils.FileFilters;

public class LocalThumbnail {

    private CustomFile from;
    private Object thumbnail;
    private boolean isLoaded=false;
    private int adapterPosition;
    private int paddingRadius=10;
    public LocalThumbnail(CustomFile from){
        this.from=from;
    }


    public void setFrom(CustomFile from) {
        this.from = from;
    }

    public CustomFile getFrom(){
        return from;
    }

    public void setPaddingRadius(int paddingRadius) {
        this.paddingRadius = paddingRadius;
    }

    public void setAdapterPosition(int adapterPosition) {
        this.adapterPosition = adapterPosition;
    }

    public int getAdapterPosition() {
        return adapterPosition;
    }

    public void setThumbnailToImageView(ImageView imageView){
        int padding=(int)(paddingRadius*1.5f);
        if(thumbnail instanceof Integer){
            imageView.setImageResource((Integer) thumbnail);
        } else if(thumbnail instanceof Drawable){
            imageView.setImageDrawable((Drawable)thumbnail);
        }else{
            imageView.setImageBitmap((Bitmap)thumbnail);
        }

        if(from.isFile()){
         imageView.setBackgroundResource(0);
         imageView.setPadding(0,0,0,0);
        }else {
            imageView.setBackgroundResource(R.drawable.blue_rounded_drawable);
            imageView.setPadding(padding,padding,padding,padding);
        }
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setThumbnail(Object thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Object getThumbnail() {
        return thumbnail;
    }
}
