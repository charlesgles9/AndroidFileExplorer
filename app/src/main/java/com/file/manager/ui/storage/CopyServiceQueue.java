package com.file.manager.ui.storage;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.file.manager.ui.utils.CopyUtility;
import java.util.LinkedList;
import java.util.Queue;

public class CopyServiceQueue  {


    private Queue<CopyUtility> queue;
    private static CopyServiceQueue instance= new CopyServiceQueue();
    private MutableLiveData<CopyUtility> observe;
    private CopyServiceQueue(){
        queue= new LinkedList<>();
        observe= new MutableLiveData<>();
    }

    public void update(Context context, Uri uri){
        queue.add(new CopyUtility(context, uri));
        observe.setValue(getFirst());
    }

    public void removeFirst(){
        queue.poll();
    }

    public CopyUtility getFirst(){
        return queue.peek();
    }

    public static CopyServiceQueue getInstance() {
        return instance;
    }

    public LiveData<CopyUtility> getObserve() {
        return observe;
    }
}
