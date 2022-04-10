package com.file.manager.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.file.manager.GlobalFileOperations;
import com.file.manager.IOnBackPressed;
import com.file.manager.Operations;
import com.file.manager.R;
import com.file.manager.Fragments.AppManagerFragment;
import com.file.manager.Fragments.DuplicateFileFragment;
import com.file.manager.Fragments.FtpServerFragment;
import com.file.manager.Fragments.LargeFileFragment;
import com.file.manager.Fragments.StorageBenchmarkFragment;
import com.file.manager.helpers.AuthenticationHelper;
import com.file.manager.helpers.PermissionsHelper;

import com.file.manager.ui.Dialogs.FingerPrintAuthDialog;
import com.file.manager.ui.Models.WindowModel;
import com.file.manager.Fragments.GlobalSearchFragment;
import com.file.manager.Fragments.HomeFragment;
import com.file.manager.FilterType;
import com.file.manager.Fragments.StorageFragment;
import com.file.manager.utils.DiskUtils;
import com.file.manager.utils.FileHandleUtil;
import com.file.manager.utils.WindowUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
public class MainActivity extends AppCompatActivity  {

    private AppBarConfiguration mAppBarConfiguration;
    public  Toolbar toolbar;
    private DrawerLayout drawer;
    private Fragment fragment;
    private HomeFragment homeFragment;
    private FtpServerFragment ftpServerFragment;
    private Operations operations=Operations.NAVIGATE;
    private String subtitle="";

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if(PreferenceManager.getDefaultSharedPreferences(this).getString("theme","Light").equals("Light")){
            setTheme(R.style.AppThemeLight);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryLight));
        }else {
            setTheme(R.style.AppThemeDark);
            getWindow().setStatusBarColor(getResources().getColor(R.color.darkStatusBar));
        }
        setContentView(R.layout.main_activity);
        toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("Home");
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        DiskUtils.init(this);
        mAppBarConfiguration= new AppBarConfiguration.Builder().build();
        // in case the theme has changed remove all previous windows
        WindowUtil.getInstance().clear();
        homeFragment= new HomeFragment(globalFileOperations);
        ftpServerFragment= new FtpServerFragment(homeFragment);
        fragment=homeFragment;
        setFragment(fragment);

       drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
           @Override
           public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
               // add fade in and fade out animation
             /*  Animation animation= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in_fade_out);
               drawerView.setAnimation(animation);*/
           }

           @Override
           public void onDrawerOpened(@NonNull View drawerView) {



           }

           @Override
           public void onDrawerClosed(@NonNull View drawerView) {

              if(!PermissionsHelper.getInstance().checkStoragePermissionDenied())
              setFragment(fragment);
              else PermissionsHelper.getInstance().grantStorageReadWrite();

           }

           @Override
           public void onDrawerStateChanged(int newState) {

           }
       });


       toolbar.setNavigationOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               if(operations.equals(Operations.NAVIGATE)){
                   openDrawer();
               }else{
                   if(fragment instanceof StorageFragment){
                       StorageFragment storageFragment=(StorageFragment)fragment;
                       toolbar.setSubtitle(subtitle);
                       storageFragment.exitSelectMode();
                   }
               }

           }
       });

        RadioButton internal=findViewById(R.id.internal);
        RadioButton external=findViewById(R.id.external);
        RadioButton system=findViewById(R.id.system);
        RadioButton images=findViewById(R.id.images);
        RadioButton music=findViewById(R.id.music);
        RadioButton videos=findViewById(R.id.videos);
        RadioButton documents=findViewById(R.id.documents);
        RadioButton applications=findViewById(R.id.applications);
        RadioButton compressed=findViewById(R.id.compressed);
        RadioButton largeFiles=findViewById(R.id.largeFilesFinder);
        RadioButton duplicateFiles=findViewById(R.id.duplicateFilesFinder);
        RadioButton appManager=findViewById(R.id.appManager);
        RadioButton home=findViewById(R.id.home);
        RadioButton benchmark=findViewById(R.id.benchmark);
        RadioButton ftpServer=findViewById(R.id.FTP);

        View.OnClickListener onClickListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.close();
                if(PermissionsHelper.getInstance().checkStoragePermissionDenied()){
                    Toast.makeText(getApplicationContext(),"Storage Access not granted!",Toast.LENGTH_SHORT).show();
                    PermissionsHelper.getInstance().grantStorageReadWrite();
                    return;
                }
                WindowUtil window=WindowUtil.getInstance();
                Fragment instance;
                switch (v.getId()){
                    case R.id.internal:
                        subtitle="Local";
                         instance= window.getActiveFragment("Internal");
                        // 0 denotes the internal storage
                        toolbar.setSubtitle(subtitle);
                        if(instance==null)
                        fragment= new StorageFragment(DiskUtils.getInstance().getDirectory(0).getPath(),toolbar.getSubtitle().toString(),FilterType.DEFAULT,fragment,globalFileOperations);
                          else fragment=instance;
                        break;
                    case R.id.external:

                        File file=DiskUtils.getInstance().getDirectory(1);
                        if(file==null) {
                            Toast.makeText(getApplicationContext(),"Removable SDCard not mounted!",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        subtitle="Local";
                        instance= window.getActiveFragment("External");
                        toolbar.setSubtitle(subtitle);

                        // 1 denotes the external storage
                        if(instance==null)
                        fragment= new StorageFragment(file.getPath(),toolbar.getSubtitle().toString(),FilterType.DEFAULT,fragment,globalFileOperations);
                        else fragment=instance;
                        break;
                    case R.id.system:
                        subtitle="System";
                        instance= window.getActiveFragment(FilterType.SYSTEM.toString());
                        // 0 denotes the internal storage
                        toolbar.setSubtitle(subtitle);
                        if(instance==null)
                            fragment= new StorageFragment("/system",toolbar.getSubtitle().toString(),FilterType.SYSTEM,fragment,globalFileOperations);
                        else fragment=instance;
                        break;
                    case R.id.images:
                        subtitle="Images";
                        toolbar.setSubtitle(subtitle);
                        instance=window.getActiveFragment(FilterType.IMAGE.toString());
                        if(instance==null)
                        fragment= new StorageFragment(DiskUtils.getInstance().getDirectory(0).getPath(),toolbar.getSubtitle().toString(),FilterType.IMAGE,fragment,globalFileOperations);
                        else fragment=instance;
                        break;
                    case R.id.videos:
                        subtitle="Videos";
                        toolbar.setSubtitle(subtitle);
                        instance=window.getActiveFragment(FilterType.VIDEO.toString());
                        if(instance==null)
                        fragment= new StorageFragment(DiskUtils.getInstance().getDirectory(0).getPath(),toolbar.getSubtitle().toString(),FilterType.VIDEO,fragment,globalFileOperations);
                        else fragment=instance;
                        break;
                    case R.id.applications:
                        subtitle="Applications";
                        toolbar.setSubtitle(subtitle);
                        instance=window.getActiveFragment(FilterType.APPLICATION.toString());
                        if(instance==null)
                        fragment= new StorageFragment(DiskUtils.getInstance().getDirectory(0).getPath(),toolbar.getSubtitle().toString(),FilterType.APPLICATION,fragment,globalFileOperations);
                        else fragment=instance;
                        break;
                    case R.id.compressed:
                        subtitle="Archive";
                        toolbar.setSubtitle(subtitle);
                        instance=window.getActiveFragment(FilterType.COMPRESSED.toString());
                        if(instance==null)
                        fragment= new StorageFragment(DiskUtils.getInstance().getDirectory(0).getPath(),toolbar.getSubtitle().toString(),FilterType.COMPRESSED,fragment,globalFileOperations);
                        else fragment=instance;
                        break;
                    case R.id.documents:
                        subtitle="Documents";
                        toolbar.setSubtitle(subtitle);
                        instance=window.getActiveFragment(FilterType.DOCUMENT.toString());
                        if(instance==null)
                        fragment= new StorageFragment(DiskUtils.getInstance().getDirectory(0).getPath(),toolbar.getSubtitle().toString(),FilterType.DOCUMENT,fragment,globalFileOperations);
                        else fragment=instance;
                        break;
                    case R.id.music:
                        subtitle="Music";
                        toolbar.setSubtitle(subtitle);
                        instance=window.getActiveFragment(FilterType.AUDIO.toString());
                        if(instance==null)
                        fragment= new StorageFragment(DiskUtils.getInstance().getDirectory(0).getPath(),toolbar.getSubtitle().toString(),FilterType.AUDIO,fragment,globalFileOperations);
                        else
                            fragment=instance;
                        break;
                    case R.id.largeFilesFinder:
                        fragment=new LargeFileFragment(fragment);
                        break;
                    case R.id.duplicateFilesFinder:
                        fragment=new DuplicateFileFragment(fragment);
                        break;
                    case R.id.appManager:
                        fragment=new AppManagerFragment(fragment);
                        break;
                    case R.id.FTP:
                        fragment=ftpServerFragment;
                        break;
                    case R.id.home:
                        subtitle="Home";
                        toolbar.setSubtitle(subtitle);
                        fragment=homeFragment;
                        break;
                    case R.id.benchmark:
                        fragment=new StorageBenchmarkFragment(fragment);

                        break;
                }
            }
        };

        findViewById(R.id.new_tab_internal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subtitle="Local";
                // 0 denotes the internal storage
                toolbar.setSubtitle(subtitle);
                fragment= new StorageFragment(DiskUtils.getInstance().getDirectory(0).getPath(),toolbar.getSubtitle().toString(),FilterType.DEFAULT,fragment,globalFileOperations);
                drawer.close();
            }
        });

        findViewById(R.id.new_tab_external).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file=DiskUtils.getInstance().getDirectory(1);
                if(file==null) {
                    Toast.makeText(getApplicationContext(),"Removable SDCard not mounted!",Toast.LENGTH_SHORT).show();
                    return;
                }
                subtitle="Local";
                toolbar.setSubtitle(subtitle);
                // 1 denotes the external storage
                fragment= new StorageFragment(file.getPath(),toolbar.getSubtitle().toString(),FilterType.DEFAULT,fragment,globalFileOperations);
                 drawer.close();
            }
        });
        internal.setOnClickListener(onClickListener);
        external.setOnClickListener(onClickListener);
        system.setOnClickListener(onClickListener);
        images.setOnClickListener(onClickListener);
        videos.setOnClickListener(onClickListener);
        applications.setOnClickListener(onClickListener);
        compressed.setOnClickListener(onClickListener);
        documents.setOnClickListener(onClickListener);
        music.setOnClickListener(onClickListener);
        largeFiles.setOnClickListener(onClickListener);
        duplicateFiles.setOnClickListener(onClickListener);
        appManager.setOnClickListener(onClickListener);
        home.setOnClickListener(onClickListener);
        benchmark.setOnClickListener(onClickListener);
        ftpServer.setOnClickListener(onClickListener);

        // Format the App name to be displayed at the navigation header
        final TextView app_title=findViewById(R.id.app_title);
        setFont(app_title,this, "Fonts/Rocket.ttf");
        PermissionsHelper.createInstance(this);

        onSharedIntent();
        passwordAuthentication();
        PermissionsHelper.getInstance().grantStorageReadWrite();

         if(DiskUtils.getInstance().isExternalSdCorrupt()){
             Toast.makeText(this,"external SdCard is corrupt!",Toast.LENGTH_SHORT).show();
         }

    }


    private void passwordAuthentication(){
        if(PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("fingerPrint",false)){
            final FingerPrintAuthDialog  authDialog= new FingerPrintAuthDialog(this ,false);
            authDialog.setOnAuthSuccess(new AuthenticationHelper.OnAuthSuccess() {
                @Override
                public void onSuccess() {
                    authDialog.dismiss();
                }

                @Override
                public void onFailed(String message) {

                }

                @Override
                public void onError(String message) {

                }
            });
            authDialog.show();
        }
        
    }


    public void setFragment(Fragment fragment){
        FragmentTransaction transaction= getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment,fragment);
        transaction.commit();
        this.fragment=fragment;
    }

   public void setSubtitle(String subtitle){
        this.subtitle=subtitle;
        toolbar.setSubtitle(subtitle);
   }
    public void openDrawer(){
        drawer.open();
    }


    public void openGlobalSearchFragment(){
        final GlobalSearchFragment fragment=new GlobalSearchFragment((HomeFragment)this.fragment,globalFileOperations);
        setFragment(fragment);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public BottomNavigationView getGlobalFileHandleLayout() {
        return (BottomNavigationView)findViewById(R.id.FILE_HANDLE);
    }

    private GlobalFileOperations globalFileOperations= new GlobalFileOperations() {
        @Override
        public void copy() {
            toolbar.setSubtitle(subtitle);
            findViewById(R.id.FILE_HANDLE).setVisibility(View.VISIBLE);
        }

        @Override
        public void paste() {

        }

        @Override
        public void cut() {
            findViewById(R.id.FILE_HANDLE).setVisibility(View.VISIBLE);
        }

        @Override
        public void select(Operations op) {
            operations=op;
            invalidateOptionsMenu();
        }

        @Override
        public void itemCount(int count) {

            getGlobalFileHandleLayout().getMenu().getItem(1)
                    .setTitle("Items: "+count);

        }

        @Override
        public void switchWindow(WindowModel model) {
            setFragment(model.getFragment());
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(operations.equals(Operations.NAVIGATE)) {
            getMenuInflater().inflate(R.menu.main_activity2, menu);
            toolbar.setNavigationIcon(R.drawable.ic_menu_open_24);
        }else {
            getMenuInflater().inflate(R.menu.multi_select_menu, menu);
            toolbar.setNavigationIcon(R.drawable.ic_close);
        }

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data==null)
            return;
        if(requestCode==32) {
            final Uri uri = data.getData();
            DocumentFile doc = DocumentFile.fromTreeUri(getApplicationContext(), uri);

            if(doc==null||!doc.canWrite()) {
                Toast.makeText(getApplicationContext(),"Failed to grant permission!",Toast.LENGTH_LONG).show();
                return;
            }
            String storage=DiskUtils.getInstance().getStorage(doc.getName());
            grantUriPermission(getPackageName(), uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            SharedPreferences.Editor editor = getSharedPreferences("MyPref", MODE_PRIVATE).edit();
            editor.putString(storage, uri.getPath());
            editor.apply();
        }

    }


    private void onSharedIntent() throws NullPointerException{
        Intent rIntent=getIntent();
        if(rIntent==null){
            return;
        }
        String rAction=rIntent.getAction();
        String rType=rIntent.getType();
        if(rAction!=null&&rAction.equals(Intent.ACTION_SEND)|rAction.equals(Intent.ACTION_VIEW)){
            if(rType!=null){
                Uri uri;
                if(rAction.equals(Intent.ACTION_SEND))
                  uri=rIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                  else
                  uri=rIntent.getData();
                if(uri!=null){
                    subtitle="Local";
                    String path=FileHandleUtil.uriToFilePath(this,uri);
                    if(path!=null) {
                    toolbar.setSubtitle(subtitle);
                    StorageFragment storageFragment= new StorageFragment(new File(path).getParent(),toolbar.getSubtitle().toString(),FilterType.DEFAULT,fragment,globalFileOperations);
                    fragment=storageFragment;
                    storageFragment.setAction(rAction);
                    storageFragment.setUriAction(path);
                    setFragment(fragment);
                    }else {
                        Toast.makeText(getApplicationContext(),"Invalid Uri",Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController,mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void setFont(TextView textView,Context context,String path){
        AssetManager assetManager=context.getAssets();
        Typeface typeface=Typeface.createFromAsset(assetManager,path);
        textView.setTypeface(typeface);
    }

    @Override
    public void onBackPressed() {
        if(fragment instanceof IOnBackPressed)
            ((IOnBackPressed)fragment).onBackPressed();
    }
}