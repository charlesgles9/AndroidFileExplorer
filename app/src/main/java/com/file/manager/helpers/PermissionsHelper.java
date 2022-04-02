package com.file.manager.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.file.manager.ui.utils.DiskUtils;

import java.io.File;
import java.util.List;

public class PermissionsHelper {

    private  Activity context;
    private static PermissionsHelper Instance;
    private PermissionsHelper(Activity context){
        this.context=context;
    }

    public  void grantStorageReadWrite(){
        // check for storage permission
        if(checkStoragePermissionDenied()){
            // ask for the permission
            ActivityCompat.requestPermissions(context,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }

    public boolean checkStoragePermissionDenied(){
        // check for storage permission
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED;
    }


    public Uri getUriFromSharedPreference(File file) throws NullPointerException{
        List<UriPermission> permissions=context.getContentResolver().getPersistedUriPermissions();
        for(UriPermission uriPermission:permissions){
            DocumentFile doc=DocumentFile.fromTreeUri(context,uriPermission.getUri());
            if(doc.getName()!=null)
            if(doc.getName().equals(file.getName())){
                return uriPermission.getUri();
            }
        }
        return null;
    }

    public boolean isFingerPrintPermissionGranted(){
        return ActivityCompat.checkSelfPermission(context,Manifest.permission.USE_FINGERPRINT)==PackageManager.PERMISSION_GRANTED;
    }

    public boolean uriValid(File file, Context context){
        Uri uri=getUriFromSharedPreference(file);
        if(uri==null)
            return false;
        DocumentFile doc=DocumentFile.fromTreeUri(context,uri);
        return doc != null && doc.canWrite();
    }

    public static void createInstance(Activity context){
        Instance= new PermissionsHelper(context);
    }

    public static PermissionsHelper getInstance(){
        return Instance;
    }
}
