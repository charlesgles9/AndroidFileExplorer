package com.file.manager;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;


public class ButtonReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
       // Bundle bundle=intent.getExtras();
      //  IntentService service=(IntentService)bundle.getSerializable("service");
       // if(service!=null){
            Toast.makeText(context,"Test",Toast.LENGTH_SHORT).show();
      //  }

       // NotificationManagerCompat.from(context).cancel(0x32);
    }
}