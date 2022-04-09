package com.file.manager.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.file.manager.Activities.MainActivity;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.utils.DiskUtils;
import com.file.manager.utils.FileHandleUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MIMETypesHelper {

    private Context context;
    private CustomFile file;
    private Intent intent;
    public MIMETypesHelper(Context context,CustomFile file){
        this.context=context;
        this.file=file;

    }

    public void startDefault(){
        if(!isStoragePermissionGranted(file)){
            getStoragePermission(file);
            return;
        }
        FileHandleUtil.fileToUri(context,file,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        intent=OpenWithDefaults(context,getMimeType(file),uri);
                        if(intent!=null)
                            context.startActivity(intent);
                        else
                            Toast.makeText(context,"No app found to open this file",Toast.LENGTH_LONG).show();
                    }
                });

    }

    public void startNoDefaults(final String MimeType){
        if(!isStoragePermissionGranted(file)){
            getStoragePermission(file);
            return;
        }
        FileHandleUtil.fileToUri(context,file,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        intent= OpenFileAs(context, MimeType,uri);
                        if(intent!=null)
                            context.startActivity(intent);
                        else Toast.makeText(context,"No app found to open this file",Toast.LENGTH_LONG).show();
                    }
                });

    }

    public void startShare(){
        if(!isStoragePermissionGranted(file)){
            getStoragePermission(file);
            return;
        }
        FileHandleUtil.fileToUri(context,file,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        intent= OpenShareFile(context, uri);
                        if(intent!=null)
                            context.startActivity(intent);
                    }
                });

    }

    private String getMimeType(CustomFile file){
        if(file.getExtension()==null||file.getExtension().lastIndexOf(".")==-1)
            return null;
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                file.getExtension().substring(file.getExtension().lastIndexOf(".")+1));
    }

    private Intent OpenWithDefaults(Context context, String MimeType,Uri uri) throws IllegalArgumentException{

        // exit if completely unavailable exit
        if(MimeType==null)
            return null;
        /*try {
            uri = FileHandleUtil.walkToFile(file, context).getUri();// FileProvider.getUriForFile(context,context.getPackageName()+".provider",file);
        }catch (Exception e){
            Toast.makeText(context,"No app found to open this file",Toast.LENGTH_LONG).show();
            return null;
        }*/
        Intent intent= new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri,MimeType);

        // check if there is a default file opener for this extension
        final PackageManager packageManager= context.getPackageManager();
        ResolveInfo defaults=packageManager.resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY);

        try {
            if (!defaults.activityInfo.name.endsWith("ResolverActivity")) {
                return intent;
            }}catch (NullPointerException ignored){}

        // retrieve all valid apps for our intent
        ArrayList<Intent> targets= new ArrayList<>();
        List<ResolveInfo> appInfoList=packageManager.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
        if(appInfoList.isEmpty()){
            return null;
        }

        for(ResolveInfo appInfo:appInfoList){
            // don't include self in the list
            String packageName=appInfo.activityInfo.packageName;
            if(packageName.equals(context.getPackageName()))
                continue;
            Intent target= new Intent();
            target.setPackage(packageName);
            targets.add(target);
        }

        Intent chooserIntent=Intent.createChooser(targets.remove(targets.size()-1),"Open with")
                .putExtra(Intent.EXTRA_INITIAL_INTENTS,targets);
        chooserIntent.setAction(Intent.ACTION_VIEW);
        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        chooserIntent.setDataAndType(uri,MimeType);
        chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return chooserIntent;

    }
    private Intent OpenFileAs(Context context,String MimeType, Uri uri){
        // exit if completely unavailable exit
        if(MimeType==null)
            return null;
       // Uri uri= FileHandleUtil.walkToFile(file,context).getUri(); // FileProvider.getUriForFile(context,context.getPackageName()+".provider",file);
        Intent intent= new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri,MimeType);

        final PackageManager packageManager= context.getPackageManager();
        // retrieve all valid apps for our intent
        ArrayList<Intent> targets= new ArrayList<>();
        List<ResolveInfo> appInfoList=packageManager.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
        if(appInfoList.isEmpty()){
            return null;
        }

        for(ResolveInfo appInfo:appInfoList){
            // don't include self in the list
            String packageName=appInfo.activityInfo.packageName;
            if(packageName.equals(context.getPackageName()))
                continue;
            Intent target= new Intent();
            target.setPackage(packageName);
            targets.add(target);
        }

        Intent chooserIntent=Intent.createChooser(targets.remove(targets.size()-1),"Open with")
                .putExtra(Intent.EXTRA_INITIAL_INTENTS,targets);
        chooserIntent.setAction(Intent.ACTION_VIEW);
        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        chooserIntent.setDataAndType(uri,MimeType);
        chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      return chooserIntent;
    }


    private Intent OpenShareFile(Context context, Uri uri){
        String MimeType=getMimeType(file);
        // exit if completely unavailable exit
        if(MimeType==null)
            return null;
       // Uri uri= FileHandleUtil.walkToFile(file,context).getUri();//  FileProvider.getUriForFile(context,context.getPackageName()+".provider",file);
        Intent intent= new Intent();
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.setDataAndType(uri,MimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        return  Intent.createChooser(intent,"Share");
    }

    private boolean isStoragePermissionGranted(CustomFile file){
        String storage= DiskUtils.getInstance().getStartDirectory(file);
        return PermissionsHelper.getInstance().uriValid(new File(storage),context);
    }

    private void getStoragePermission(CustomFile file){
        MainActivity activity=(MainActivity)context;
        String storage=DiskUtils.getInstance().getStartDirectory(file);
        if (!PermissionsHelper.getInstance().uriValid(new File(storage),context)) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            activity.startActivityForResult(intent, 32);
        }
    }
}
