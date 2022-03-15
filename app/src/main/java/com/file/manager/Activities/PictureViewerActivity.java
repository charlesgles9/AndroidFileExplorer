package com.file.manager.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.file.manager.Fragments.PictureTabFragment;
import com.file.manager.R;
import com.file.manager.helpers.MIMETypesHelper;
import com.file.manager.ui.Adapters.PicturePagerAdapter;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.PhotoInfoDialog;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.FileFilters;
import com.file.manager.ui.utils.FileHandleUtil;

import java.util.ArrayList;
import java.util.List;

public class PictureViewerActivity extends AppCompatActivity  {

    private ArrayList<CustomFile>imagesArray=new ArrayList<>();
    private List<PictureTabFragment>fragments= new ArrayList<>();
    private PicturePagerAdapter adapter;
    private ViewPager pager;
    private Toolbar toolbar;
    private AppCompatImageView share;
    private boolean showToolbar=true;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_picture_viewer);
        DiskUtils.init(this);
        final Intent intent=getIntent();
         toolbar=findViewById(R.id.toolbar);
         share=findViewById(R.id.share);

        String path=intent.getStringExtra("path");
        // in case a photo is passed by an external application
        if(path==null){
            path=onSharedIntent(intent);
        }
        // if path is null then no images have been passed
        if(path==null)finish();
        CustomFile chosen=new CustomFile(path);
        final CustomFile parent=new CustomFile(chosen.getParent());
        adapter=new PicturePagerAdapter(getApplicationContext(),getSupportFragmentManager(),fragments);
        pager=findViewById(R.id.pager);
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                toolbar.setSubtitle(adapter.getPageTitle(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // fetch all the videos in the current directory
        ArrayList<CustomFile>array=new ArrayList<>();
        FileHandleUtil.ListFiles(this,new CustomFile(parent.getPath()),array,FileFilters.FilesOnlyImages());
        int current=0;
        for (int i=0;i<array.size();i++) {
            CustomFile file=array.get(i);
            // in case we used document file
            if(!FileFilters.isImage(file.getName()))
                continue;
            createPagedImages(file);
            imagesArray.add(file);
            if(chosen.getPath().equals(file.getPath()))
                current=i;
        }
        adapter.notifyDataSetChanged();
        pager.setCurrentItem(current);
        toolbar.setSubtitle(adapter.getPageTitle(pager.getCurrentItem()));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final Context context=this;
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // open menu options
                int position=pager.getCurrentItem();
                PhotoInfoDialog infoDialog= new PhotoInfoDialog(context,
                adapter.getResolution(position),adapter.getFile(position));
                infoDialog.show();
                return false;
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    new MIMETypesHelper(context, imagesArray.get(pager.getCurrentItem())).startShare();
                }catch (Exception ignore){}
            }
        });

    }



   private void hideToolbar(){
       Animation animation;
       if(showToolbar)
           animation= AnimationUtils.loadAnimation(this,R.anim.fade_in_fade_out);
       else
           animation= AnimationUtils.loadAnimation(this,R.anim.popup_fadeout);
       toolbar.setAnimation(animation);
       share.setAnimation(animation);
       animation.setAnimationListener(new Animation.AnimationListener() {
           @Override
           public void onAnimationStart(Animation animation) {

           }

           @Override
           public void onAnimationEnd(Animation animation) {
               toolbar.setVisibility(showToolbar?View.VISIBLE:View.INVISIBLE);
               share.setVisibility(showToolbar?View.VISIBLE:View.INVISIBLE);
           }

           @Override
           public void onAnimationRepeat(Animation animation) {

           }
       });
       share.startAnimation(animation);
       toolbar.startAnimation(animation);
   }

    private void createPagedImages(CustomFile file){
        PictureTabFragment tab=new PictureTabFragment(file);
        fragments.add(tab);
    }

    private String onSharedIntent(Intent intent){
        String rAction=intent.getAction();
        String rType=intent.getType();
        if(rAction.equals(Intent.ACTION_SEND)|rAction.equals(Intent.ACTION_VIEW)){
            if(rType!=null){
                Uri uri;
                if(rAction.equals(Intent.ACTION_SEND))
                    uri=intent.getParcelableExtra(Intent.EXTRA_STREAM);
                else
                    uri=intent.getData();
                String path= FileHandleUtil.uriToFilePath(uri);
                if(path!=null){
                    return path;
                }
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}