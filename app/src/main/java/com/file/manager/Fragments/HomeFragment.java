package com.file.manager.Fragments;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.Activities.SettingsActivity;
import com.file.manager.GlobalFileOperations;
import com.file.manager.IOnBackPressed;
import com.file.manager.Activities.MainActivity;
import com.file.manager.Activities.PictureViewerActivity;
import com.file.manager.R;
import com.file.manager.Activities.VideoPlayerActivity;
import com.file.manager.WindowState;
import com.file.manager.helpers.MIMETypesHelper;
import com.file.manager.helpers.PermissionsHelper;
import com.file.manager.ui.Adapters.BookMarkAdapter;
import com.file.manager.ui.Adapters.RecentFilesAdapter;
import com.file.manager.ui.Adapters.SwitchWindowAdapter;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.WindowModel;
import com.file.manager.ui.Dialogs.SwitchWindowDialog;
import com.file.manager.FilterType;
import com.file.manager.ui.Models.RecentFilesContainer;
import com.file.manager.utils.DiskUtils;
import com.file.manager.utils.FileFilters;
import com.file.manager.utils.FileHandleUtil;
import com.file.manager.utils.ThumbnailLoader;
import com.file.manager.utils.WindowUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HomeFragment extends Fragment implements WindowState, IOnBackPressed {


    private int FragmentID;
    private View root;
    private Fragment parent;
    private MainActivity activity;
    private RecentFilesContainer recentFilesContainer;
    private RecentFilesTask recentFilesTask;
    private LinearLayoutManager manager;
    private ProgressBar progressBar;
    private TextView status;
    private GlobalFileOperations globalFileOperations;
    private RecyclerView recyclerView;
    private RecentFilesAdapter adapter;
    private Toolbar toolbar;
    private boolean deleted=false;
    private View bookMarkToggleLayout;
    private View bookMarkLayout;
    private ToggleButton bookToggleBtn;
    private RecyclerView bookMarkList;
    private BookMarkAdapter bookMarkAdapter;
    private SharedPreferences preferences;
    private ArrayList<File>bookmarksArray=new ArrayList<>();
    private ProgressBar videoProgress;
    private ProgressBar photoProgress;
    private ProgressBar appProgress;
    private ProgressBar archiveProgress;
    private ProgressBar docProgress;
    private ProgressBar audioProgress;
    private TextView calculatingText;
    public HomeFragment(GlobalFileOperations globalFileOperations){
        FragmentID= UUID.randomUUID().hashCode();
        this.globalFileOperations=globalFileOperations;
    }

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {

        // inflate the view only once
        if(root==null)
        root = inflater.inflate(R.layout.home_fragment, container, false);
        preferences=getContext().getSharedPreferences("Bookmarks",Context.MODE_PRIVATE);
        final LinearLayout searchLayout=root.findViewById(R.id.searchLayout);
        final ToggleButton showRecent=root.findViewById(R.id.showRecent);
        videoProgress=root.findViewById(R.id.videoProgress);
        photoProgress=root.findViewById(R.id.photoProgress);
        appProgress=root.findViewById(R.id.appProgress);
        audioProgress=root.findViewById(R.id.audioProgress);
        archiveProgress=root.findViewById(R.id.archiveProgress);
        docProgress=root.findViewById(R.id.docProgress);
        calculatingText=root.findViewById(R.id.calculationText);
        bookMarkToggleLayout=root.findViewById(R.id.bookMarkToggleLayout);
        bookMarkLayout=root.findViewById(R.id.bookMarkLayout);
        bookToggleBtn=root.findViewById(R.id.bookToggleBtn);
        bookMarkList=root.findViewById(R.id.bookMarkList);
        toolbar=((MainActivity)getContext()).toolbar;
        recyclerView=root.findViewById(R.id.fileList);
        status=root.findViewById(R.id.status);
        progressBar=root.findViewById(R.id.progress);

        initStorageProgress();
        initStorageCategoryStats();
        final Fragment fragment=this;
        activity=(MainActivity)getContext();
        activity.setSubtitle("Home");
        root.findViewById(R.id.storageDetails1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PermissionsHelper.getInstance().checkStoragePermissionDenied()) {
                    PermissionsHelper.getInstance().grantStorageReadWrite();
                    return;
                }
                openFragment("Internal",FilterType.DEFAULT,"Local",0);
                activity.toolbar.setSubtitle("Local");
            }
        });

        root.findViewById(R.id.storageDetails2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PermissionsHelper.getInstance().checkStoragePermissionDenied()) {
                    PermissionsHelper.getInstance().grantStorageReadWrite();
                    return;
                }
                openFragment("External",FilterType.DEFAULT,"Local",1);
                activity.toolbar.setSubtitle("Local");
            }
        });


        final View.OnClickListener quickAccessListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(PermissionsHelper.getInstance().checkStoragePermissionDenied()) {
                    PermissionsHelper.getInstance().grantStorageReadWrite();
                    return;
                }

                String subtitle="";
                switch (v.getId()){
                    case R.id.movie_layout:
                        subtitle="Videos";
                        openFragment(FilterType.VIDEO.toString(),FilterType.VIDEO,"Videos",0);
                        break;
                    case R.id.music_layout:
                        subtitle="Music";
                        openFragment(FilterType.AUDIO.toString(),FilterType.AUDIO,"Music",0);
                        break;
                    case R.id.android_layout:
                        subtitle="Applications";
                        openFragment(FilterType.APPLICATION.toString(),FilterType.APPLICATION,"Applications",0);
                        break;
                    case R.id.photo_layout:
                        subtitle="Images";
                        openFragment(FilterType.IMAGE.toString(),FilterType.IMAGE,"Images",0);
                        break;
                    case R.id.document_layout:
                        subtitle="Documents";
                        openFragment(FilterType.DOCUMENT.toString(),FilterType.DOCUMENT,"Documents",0);
                        break;
                    case R.id.compressed_layout:
                        subtitle="Archive";
                        openFragment(FilterType.COMPRESSED.toString(),FilterType.COMPRESSED,"Archive",0);
                        break;
                }
                activity.toolbar.setSubtitle(subtitle);
            }
        };
        root.findViewById(R.id.movie_layout).setOnClickListener(quickAccessListener);
        root.findViewById(R.id.music_layout).setOnClickListener(quickAccessListener);
        root.findViewById(R.id.android_layout).setOnClickListener(quickAccessListener);
        root.findViewById(R.id.photo_layout).setOnClickListener(quickAccessListener);
        root.findViewById(R.id.document_layout).setOnClickListener(quickAccessListener);
        root.findViewById(R.id.compressed_layout).setOnClickListener(quickAccessListener);

       root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
           @Override
           public void onGlobalLayout() {
               if(!isDeleted()) {
                   WindowUtil.getInstance().put(fragment,FilterType.HOME.toString(), root);
                   WindowModel model = WindowUtil.getInstance().get(getFragmentID());
                   model.setPath("Home");
                   model.setTitle("Home");
                   WindowUtil.getInstance().setCurrent(getFragmentID());
               }
           }
       });

        searchLayout.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(PermissionsHelper.getInstance().checkStoragePermissionDenied()) {
                   PermissionsHelper.getInstance().grantStorageReadWrite();
                   return;
               }
               ((MainActivity)getContext()).openGlobalSearchFragment();
           }
       });


        if(recentFilesContainer==null)
        recentFilesContainer=new RecentFilesContainer();
        manager= new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        if(adapter==null) {
            adapter = new RecentFilesAdapter(getContext(), recentFilesContainer);
        }else {
            LoadThumbnails();
        }

        if(!adapter.getUpdates().hasActiveObservers()){
            adapter.getUpdates().observe(getViewLifecycleOwner(), new Observer<List<Integer>>() {
                @Override
                public void onChanged(List<Integer> position) {
                    recyclerView.setAdapter(adapter);
                }
            });
        }
        recyclerView.setAdapter(adapter);
        recyclerView.clearOnScrollListeners();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState==RecyclerView.SCROLL_STATE_IDLE)
                LoadThumbnails();
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        final SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getContext());
        adapter.setOnItemClickListener(new RecentFilesAdapter.OnItemClickListener() {
            @Override
            public void openImage(CustomFile file) {
                boolean openImageWithThisApp=preferences.getBoolean("imageFileOpen",true);
                boolean openVideoWithThisApp=preferences.getBoolean("videoFileOpen",true);
                if(FileFilters.isImage(file.getName())&openImageWithThisApp){
                    Intent pictureViewerIntent=new Intent(getContext(), PictureViewerActivity.class);
                    pictureViewerIntent.putExtra("path",file.getPath());
                    startActivityForResult(pictureViewerIntent,0);
                }else if(FileFilters.isVideo(file.getName())&openVideoWithThisApp) {
                    Intent videoPlayerIntent=new Intent(getContext(), VideoPlayerActivity.class);
                    videoPlayerIntent.putExtra("path",file.getPath());
                    startActivityForResult(videoPlayerIntent,0);
                }else {
                    try {
                        new MIMETypesHelper(getContext(), file).startDefault();
                    } catch (IllegalArgumentException ignore) {}
                }

            }

            @Override
            public void openFolder(CustomFile file) {
                StorageFragment storageFragment= new StorageFragment(file.getPath(),
                        "Local", FilterType.DEFAULT,fragment,globalFileOperations);
                storageFragment.setDeleted(true);
                ((MainActivity)getContext()).setFragment(storageFragment);
            }
        });

        showRecent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showRecent.isChecked()){
                    initializeRecentFiles();
                    recyclerView.setVisibility(View.VISIBLE);
                }else {
                    recyclerView.setVisibility(View.GONE);
                    status.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
        status.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        updateWindowView(root);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId()==R.id.IC_WINDOW) {
                    updateWindowView(root);
                    openWindow(root,(MainActivity)getContext());
                }else if(item.getItemId()==R.id.ACTION_SETTINGS){
                   preference(toolbar);
                }else {
                    //refresh views
                    initBookMarks();
                    initStorageProgress();
                    initStorageCategoryStats();
                    recentFilesContainer.clear();
                    if(showRecent.isChecked())
                    initializeRecentFiles();
                }
                return false;
            }
        });

        initBookMarks();

        // set default values in shared preference settings
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        defaultSharedPreferences.getString("copy","Ask me Always");
        defaultSharedPreferences.getString("zip","Ask me Always");
        defaultSharedPreferences.getString("thumbRatio","64x64");

        return root;
    }


    private void openFragment(String key,FilterType type,String subtitle,int dir){
        WindowUtil window=WindowUtil.getInstance();
        Fragment instance=window.getActiveFragment(key);
        if(instance==null)
            activity.setFragment(new StorageFragment(DiskUtils.getInstance().getDirectory(dir).getPath(),
                    subtitle,type,this,globalFileOperations));
        else
            activity.setFragment(instance);
    }

    @SuppressLint("SetTextI18n")
    public void initStorageProgress(){
        // internal
        final ProgressBar InternalStorageProgress=root.findViewById(R.id.InternalSdProgress);
        final TextView textView_TotalSpace_internal=root.findViewById(R.id.totalInternal);
        final TextView textView_Used_internal=root.findViewById(R.id.usedInternal);
        final TextView textView_Free_internal=root.findViewById(R.id.availInternal);
        final TextView textView_percent_internal=root.findViewById(R.id.InternalPercentage);
        // external
        final ProgressBar ExternalStorageProgress=root.findViewById(R.id.ExternalSdProgress);
        final TextView textView_TotalSpace_external=root.findViewById(R.id.totalExternal);
        final TextView textView_Used_external=root.findViewById(R.id.usedExternal);
        final TextView textView_Free_external=root.findViewById(R.id.availExternal);
        final TextView textView_percent_external=root.findViewById(R.id.ExternalPercentage);

        final File internal=DiskUtils.getInstance().getDirectory(0);
        InternalStorageProgress.setProgress(DiskUtils.getInstance().getFreeStorageInPercent(internal));
        textView_TotalSpace_internal.setText("Total: "+DiskUtils.getInstance().getTotalStorageString(internal));
        textView_Used_internal.setText("Used: "+DiskUtils.getInstance().getUsedStorageString(internal));
        textView_Free_internal.setText("Avail: "+DiskUtils.getInstance().getFreeStorageString(internal));
        textView_percent_internal.setText(DiskUtils.getInstance().getFreeStorageInPercent(internal)+"%");
        final File external=DiskUtils.getInstance().getDirectory(1);
        if(external!=null){
            ExternalStorageProgress.setProgress(DiskUtils.getInstance().getFreeStorageInPercent(external));
            textView_TotalSpace_external.setText("Total: "+DiskUtils.getInstance().getTotalStorageString(external));
            textView_Used_external.setText("Used: "+DiskUtils.getInstance().getUsedStorageString(external));
            textView_Free_external.setText("Avail: "+DiskUtils.getInstance().getFreeStorageString(external));
            textView_percent_external.setText(DiskUtils.getInstance().getFreeStorageInPercent(external)+"%");

        }else {
            root.findViewById(R.id.storageDetails2).setVisibility(View.GONE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void initStorageCategoryStats(){
        // incase the user refreshes multiple times
        // prevents memory leaks
        if(calculatingText.getVisibility()==View.INVISIBLE)
        new AsyncTask<String ,Integer,String>(){
            ArrayList<CustomFile>videos= new ArrayList<>();
            ArrayList<CustomFile>audios= new ArrayList<>();
            ArrayList<CustomFile>apps= new ArrayList<>();
            ArrayList<CustomFile>photos= new ArrayList<>();
            ArrayList<CustomFile>docs= new ArrayList<>();
            ArrayList<CustomFile>archives= new ArrayList<>();
            @Override
            protected String doInBackground(String... strings) {
                Context context=getContext();
                FileHandleUtil.fetchVideoFiles(context,videos);
                FileHandleUtil.fetchImageFiles(context,photos);
                FileHandleUtil.fetchAudioFiles(context,audios);
                FileHandleUtil.ListApplication(apps);
                FileHandleUtil.ListDocuments(docs);
                FileHandleUtil.ListCompressed(archives);
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                calculatingText.setVisibility(View.VISIBLE);
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                long storageSize=1L;
                long videoSize=1L;
                long photoSize=1L;
                long audioSize=1L;
                long appSize=1L;
                long docSize=1L;
                long archiveSize=1L;
                for(File mount:DiskUtils.getInstance().getStorageDirs()) {
                       if(mount!=null) {
                          storageSize+= DiskUtils.getInstance().totalMemory(mount);

                       }
                }
                for(CustomFile file:videos)
                    videoSize+=file.length();
                for(CustomFile file:photos)
                    photoSize+=file.length();
                for(CustomFile file:audios)
                    audioSize+=file.length();
                for(CustomFile file:apps)
                    appSize+=file.length();
                for(CustomFile file:docs)
                    docSize+=file.length();
                for(CustomFile file:archives)
                    archiveSize+=file.length();
              videoProgress.setProgress((int)(((float)videoSize/(float)storageSize)*100)+1);
              photoProgress.setProgress((int)(((float)photoSize/(float)storageSize)*100)+1);
              audioProgress.setProgress((int)(((float)audioSize/(float)storageSize)*100)+1);
              appProgress.setProgress((int)(((float)appSize/(float)storageSize)*100)+1);
              docProgress.setProgress((int)(((float)docSize/(float)storageSize)*100)+1);
              archiveProgress.setProgress((int)(((float)archiveSize/(float)storageSize)*100)+1);
              ((TextView)root.findViewById(R.id.videoText)).setText("Video("+DiskUtils.getInstance().getSize(videoSize)+")");
              ((TextView)root.findViewById(R.id.audioText)).setText("Audio("+DiskUtils.getInstance().getSize(audioSize)+")");
              ((TextView)root.findViewById(R.id.androidText)).setText("App("+DiskUtils.getInstance().getSize(appSize)+")");
              ((TextView)root.findViewById(R.id.photoText)).setText("Photos("+DiskUtils.getInstance().getSize(photoSize)+")");
              ((TextView)root.findViewById(R.id.docText)).setText("Docs("+DiskUtils.getInstance().getSize(docSize)+")");
              ((TextView)root.findViewById(R.id.archiveText)).setText("Archive("+DiskUtils.getInstance().getSize(archiveSize)+")");
              calculatingText.setVisibility(View.INVISIBLE);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public void updateWindowView(View root){
        final Fragment fragment=this;
        WindowUtil.getInstance().put(fragment,FilterType.HOME.toString(),root);
        WindowModel model=WindowUtil.getInstance().get(getFragmentID());
        model.setPath("Home");
        model.setTitle("Home");
        WindowUtil.getInstance().setCurrent(getFragmentID());
    }

    private void openWindow(final View root,final MainActivity activity){
        final SwitchWindowDialog switchWindowDialog= new SwitchWindowDialog(getContext());
        switchWindowDialog.show();
        final ViewTreeObserver.OnGlobalLayoutListener layoutListener= new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // update the screen view
                updateWindowView(root);
                switchWindowDialog.getAdapter().notifyDataSetChanged();
            }
        };
        switchWindowDialog.setOnWindowSelectListener(new SwitchWindowAdapter.onWindowSelectListener() {
            @Override
            public void onWindowSelect(WindowModel model) {
                updateWindowView(root);
                switchWindowDialog.getAdapter().notifyDataSetChanged();
                // switch fragment in mainActivity
                activity.setFragment(model.getFragment());
                toolbar.setSubtitle(model.getTitle());
                switchWindowDialog.cancel();

            }

            @Override
            public void onCancel() {
                root.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
            }
        });
        root.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
    }

    public void initializeRecentFiles(){
        if(recentFilesContainer.isEmpty()&recentFilesTask==null||
           recentFilesContainer.isEmpty()&!recentFilesTask.isRunning()) {
            progressBar.setVisibility(View.VISIBLE);
            status.setVisibility(View.VISIBLE);
            recentFilesTask=new RecentFilesTask();
            recentFilesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    @Override
    public void setFragmentID(int fragmentID) {
        FragmentID = fragmentID;
    }

    @Override
    public int getFragmentID() {
        return FragmentID;
    }

    @Override
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public void setParent(Fragment fragment) {
       this.parent=fragment;
    }

    @Override
    public Fragment getParent() {
        return parent;
    }


    private void initBookMarks(){
        final HomeFragment parent=this;
        bookMarkAdapter= new BookMarkAdapter(getContext(),bookmarksArray);
        bookMarkAdapter.setOnItemClickListener(new BookMarkAdapter.onItemClickListener() {
            @Override
            public void onClick(String key) {
                if(PermissionsHelper.getInstance().checkStoragePermissionDenied()) {
                    PermissionsHelper.getInstance().grantStorageReadWrite();
                    return;
                }
                MainActivity activity=(MainActivity)getContext();
                    activity.setFragment(
                            new StorageFragment(key,
                                    "Local", FilterType.DEFAULT,parent,globalFileOperations));
            }

            @Override
            public void onLongClick(String key) {

            }
        });
        LinearLayoutManager manager= new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        bookMarkList.setLayoutManager(manager);
        bookMarkList.setAdapter(bookMarkAdapter);
        bookMarkToggleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bookToggleBtn.isChecked()){
                    new BookMarksTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    bookMarkLayout.setVisibility(View.VISIBLE);
                }else {
                    bookMarkLayout.setVisibility(View.GONE);
                }
                if(preferences.getAll().size()>0)
                bookToggleBtn.setChecked(!bookToggleBtn.isChecked());
            }
        });

        if(PreferenceManager.getDefaultSharedPreferences(getContext()).
                getBoolean("bookmarks",true)){
            bookMarkToggleLayout.callOnClick();
        }
    }

    class BookMarksTask extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... strings) {
            Map<String, ?> map=preferences.getAll();
            bookmarksArray.clear();
         if(preferences!=null)
           for( Map.Entry<String,?>entry:map.entrySet()){
               bookmarksArray.add(new File(entry.getKey()));
           }

            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // update adapter
            bookMarkAdapter.notifyDataSetChanged();

        }
    }
    class RecentFilesTask extends AsyncTask<String,Integer,String>{

        private boolean running;

        @Override
        protected String doInBackground(String... strings) {
            running=true;
            try {
                FileHandleUtil.fetchRecentMedia(getContext(),recentFilesContainer);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            status.setVisibility(View.GONE);
            LoadThumbnails();
            running=false;
        }
        public boolean isRunning(){
            return running;
        }

    }


    private void LoadThumbnails(){
        if(getContext()!=null) {

            adapter.LoadThumbnail(getContext(),manager.findFirstVisibleItemPosition(),manager.findLastVisibleItemPosition(), new ThumbnailLoader.onThumbnailComplete() {
                @Override
                public void onComplete(List<Integer> positions) {
                    for(int i=0;i<positions.size();i++){
                        adapter.notifyItemChanged(positions.get(i));
                    }
                }
            });
        }
    }
    private void preference(final View anchor){
        LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        final View view=inflater.inflate(R.layout.home_settings_popup_layout,null);
        final PopupWindow popupWindow= new PopupWindow(view,LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,true);
        popupWindow.showAsDropDown(anchor,0,0, Gravity.RIGHT);
        final View settings=view.findViewById(R.id.settings);
        final View exit=view.findViewById(R.id.exit);
        final View.OnClickListener onClickListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==R.id.settings) {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    intent.putExtra("Theme", PreferenceManager.getDefaultSharedPreferences(getContext()).getString("theme", "Light"));
                    startActivity(intent);
                }else {
                    //exit
                    onBackPressed();
                }
                popupWindow.dismiss();
            }
        };
        settings.setOnClickListener(onClickListener);
        exit.setOnClickListener(onClickListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
      if(bookToggleBtn!=null)
        if(bookToggleBtn.isChecked()){
            bookMarkToggleLayout.callOnClick();
        }
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        DialogInterface.OnClickListener listener= new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==DialogInterface.BUTTON_POSITIVE){
                    ((MainActivity)getContext()).finishAffinity();
                }else {
                    dialog.dismiss();
                }
            }
        };

        builder.setMessage("Exit application?").setNegativeButton("No",listener)
                .setPositiveButton("Yes",listener).show();

    }
}