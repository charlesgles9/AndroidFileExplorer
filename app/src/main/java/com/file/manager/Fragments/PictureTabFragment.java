package com.file.manager.Fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.file.manager.R;
import com.jsibbold.zoomage.ZoomageView;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class PictureTabFragment extends Fragment {


    private View root;
    private ZoomageView picture;
    private File file;
    private int rw=0;
    private int rh=0;
    public PictureTabFragment(File file){
        this.file=file;
    }
    @SuppressLint("StaticFieldLeak")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(root==null)
        root=inflater.inflate(R.layout.picture_viewer_fragment_tab,container,false);
        picture=root.findViewById(R.id.picture);
        try {
            Glide.with(this).asBitmap()
                    .load(file).into(picture);
        }catch (Exception ignore){ }

        new AsyncTask<String,Integer,String>(){
            @Override
            protected String doInBackground(String... strings) {
                try {
                    Bitmap bitmap=Glide.with(getContext()).asBitmap().load(file).submit().get();
                    rw=bitmap.getWidth();
                    rh=bitmap.getHeight();
                } catch (ExecutionException | InterruptedException ignored) {}

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        return root;
    }

    public File getFile() {
        return file;
    }

    public String getName(){
        return file.getName();
    }

    public String getResolution(){
        return rw+"x"+rh;
    }


}
