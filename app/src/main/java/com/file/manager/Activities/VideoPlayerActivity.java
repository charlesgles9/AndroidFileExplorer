package com.file.manager.Activities;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.file.manager.Fragments.VideoTabFragment;
import com.file.manager.R;
import com.file.manager.TapGesture;
import com.file.manager.ui.Adapters.VideoPagerAdapter;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.storage.SortBy;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.FileFilters;
import com.file.manager.ui.utils.FileHandleUtil;
import com.file.manager.ui.utils.Sorter;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayerActivity extends AppCompatActivity {

    private List<CustomFile>videoArray= new ArrayList<>();
    private List<VideoTabFragment>fragments= new ArrayList<>();
    private VideoPagerAdapter adapter;
    private ViewPager pager;
    private Toolbar toolbar;
    private CustomFile chosen;
    private TapGesture tapGesture;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_viewer);
        DiskUtils.init(this);
        final DisplayMetrics metrics=getResources().getDisplayMetrics();
        final Intent intent=getIntent();
        toolbar=findViewById(R.id.toolbar);
        String path=intent.getStringExtra("path");
        if(path==null){
            path=onSharedIntent(intent);
        }

        if(path==null)finish();
        chosen=new CustomFile(path);
        final CustomFile parent=new CustomFile(chosen.getParent());
        adapter=new VideoPagerAdapter(getApplicationContext(),getSupportFragmentManager(),fragments);
        pager=findViewById(R.id.pager);
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int left=position-1;
                int right=position+1;
                if(left>=0)
                ((VideoTabFragment)adapter.getItem(left)).pauseVideo();
                if(right<=adapter.getCount()-1)
                ((VideoTabFragment)adapter.getItem(right)).pauseVideo();
                toolbar.setSubtitle(adapter.getPageTitle(position));
                if(tapGesture!=null) {
                    ((VideoTabFragment)adapter.getItem(position)).setControllerVisible(false);
                    toolbar.setVisibility(View.VISIBLE);
                    tapGesture.onClick();
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

         tapGesture= new TapGesture(this){
             @Override
             public void onTap(MotionEvent event) {
                 VideoTabFragment videoTab=(VideoTabFragment)adapter.getItem(pager.getCurrentItem());
                 int seconds=metrics.widthPixels*0.5f<=event.getX()?10:-10;
                 videoTab.skipFrames(seconds);
             }
             @Override
             public void onClick() {
                 VideoTabFragment videoTab=(VideoTabFragment)adapter.getItem(pager.getCurrentItem());
                 videoTab.setControllerVisible(!videoTab.getControllerVisibility());
                 if(!videoTab.getControllerVisibility()) {
                     toolbar.setVisibility(View.INVISIBLE);
                     videoTab.showPlayButton(false);
                 }else {
                     toolbar.setVisibility(View.VISIBLE);
                     videoTab.showPlayButton(true);
                 }
             }
         };
        // fetch all the videos in the current directory
        ArrayList<CustomFile>array=new ArrayList<>();
        // only fetch all videos in the directory if we are using this application
        // not from an external intent
        if(intent.getStringExtra("path")!=null) {
            SortBy sortBy=(SortBy)intent.getSerializableExtra("sortOrder");
            FileHandleUtil.ListFiles(this, new CustomFile(parent.getPath()), array, FileFilters.FilesOnlyVideos());
            if(sortBy!=null)
            switch (sortBy){
                case AZ:
                    Sorter.AtoZ(array);
                    break;
                case DATE:
                    Sorter.sortByDate(array);
                    break;
                case SIZE:
                    Sorter.sortBySize(array);
                    break;
                case EXTENSION:
                    Sorter.sortByExtension(array);
                    break;
            }
        } else
            array.add(chosen);
        int current=0;
            for (int i=0;i<array.size();i++) {
                CustomFile file=array.get(i);
                if(!FileFilters.isVideo(file.getName()))
                    continue;
                createPagedVideos(file);
                videoArray.add(file);
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
        toolbar.setVisibility(View.VISIBLE);
    }


    private void createPagedVideos(CustomFile file){
        VideoTabFragment tab=new VideoTabFragment(file, tapGesture);
        tab.setExecuteOnStart(file.getPath().equals(chosen.getPath()));
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

    public void changeOrientation(){
        if(getResources().getConfiguration().orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
