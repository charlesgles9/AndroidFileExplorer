package com.file.manager.Fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.Activities.MusicPlayerActivity;
import com.file.manager.Activities.SettingsActivity;
import com.file.manager.BackgroundServices.CopyService;
import com.file.manager.GlobalFileOperations;
import com.file.manager.IOnBackPressed;
import com.file.manager.Activities.MainActivity;
import com.file.manager.Activities.PictureViewerActivity;
import com.file.manager.Activities.VideoPlayerActivity;
import com.file.manager.ui.Models.MusicHelperSingleton;
import com.file.manager.ui.Dialogs.NewFolderDialog;
import com.file.manager.OnTaskCompleteListener;
import com.file.manager.Operations;
import com.file.manager.ui.Dialogs.OverWriteDialog;
import com.file.manager.R;
import com.file.manager.WindowState;
import com.file.manager.helpers.MIMETypesHelper;
import com.file.manager.helpers.PermissionsHelper;
import com.file.manager.ui.Adapters.DirectoryPathAdapter;
import com.file.manager.ui.Adapters.StorageAdapter;
import com.file.manager.ui.Adapters.SwitchWindowAdapter;
import com.file.manager.ui.Dialogs.ArchiveCompressorDialog;
import com.file.manager.ui.Dialogs.ArchiveExtractorDialog;
import com.file.manager.ui.Dialogs.BatchFileRenameDialog;
import com.file.manager.ui.Dialogs.ConfirmDeleteDialog;
import com.file.manager.ui.Dialogs.TaskDialogPrompt;
import com.file.manager.ui.Dialogs.CopyFileDialog;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.DirectoryManager;
import com.file.manager.ui.Models.Folder;
import com.file.manager.ui.Models.WindowModel;
import com.file.manager.ui.Dialogs.MoveFileDialog;
import com.file.manager.ui.Dialogs.OpenAsDialog;
import com.file.manager.ui.Dialogs.PropertiesDialog;
import com.file.manager.ui.Dialogs.SingleFileRenameDialog;
import com.file.manager.ui.Dialogs.SwitchWindowDialog;
import com.file.manager.ViewMode;
import com.file.manager.ui.Models.CopyServiceQueue;
import com.file.manager.FilterType;
import com.file.manager.SortBy;
import com.file.manager.utils.ArchiveCompressUtil;
import com.file.manager.utils.ArchiveDecompressUtil;
import com.file.manager.utils.CopyHelper;
import com.file.manager.utils.CopyUtility;
import com.file.manager.utils.CutHelper;
import com.file.manager.utils.CutUtility;
import com.file.manager.utils.DeleteFilesUtility;
import com.file.manager.utils.DiskUtils;
import com.file.manager.utils.FileFilters;
import com.file.manager.utils.FileHandleUtil;
import com.file.manager.utils.ThumbnailLoader;
import com.file.manager.utils.WindowUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StorageFragment extends Fragment implements IOnBackPressed, WindowState {


    private RecyclerView FileListRecyclerView;
    private RecyclerView PathListRecyclerView;
    private StorageAdapter storageAdapter;
    private DirectoryPathAdapter pathAdapter;
    private DirectoryManager directoryManager;
    private FloatingActionButton addFolderFloatingButton;
    private  ProgressBar loadingProgress;
    private TextView ItemCount;
    private TextView statusMessage;
    private String EntryPath;
    private int FragmentID;
    private View root;
    private GlobalFileOperations globalFileOperations;
    private String title;
    private Operations operations=Operations.NAVIGATE;
    private BottomNavigationView FileOperations;
    private boolean deleted=false;
    private Fragment parent;
    private FilterType type;
    private String key;
    private String action="";
    private String uriAction="";
    private MainActivity activity;
    public StorageFragment(String EntryPath,String title,FilterType type,Fragment parent,GlobalFileOperations globalFileOperations){
        this.EntryPath=EntryPath;
        this.FragmentID= UUID.randomUUID().hashCode();
        this.globalFileOperations=globalFileOperations;
        this.title=title;
        this.directoryManager= new DirectoryManager(type);
        this.type=type;
        this.parent=parent;
        if(type.equals(FilterType.DEFAULT))
            this.key=DiskUtils.getInstance().isInternalStorage(EntryPath)?"Internal":"External";
        else
            this.key=type.toString();
    }

    public String getTitle() {
        return title;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
         root = inflater.inflate(R.layout.storage_fragment, container, false);
         setTitleBarSubtitle();
         WindowUtil.getInstance().put(this,key,root);
         FileListRecyclerView=root.findViewById(R.id.fileList);
         PathListRecyclerView=root.findViewById(R.id.pathList);
         loadingProgress =root.findViewById(R.id.progress);
         statusMessage=root.findViewById(R.id.status);
         addFolderFloatingButton=root.findViewById(R.id.addFolderFloatingButton);
         ItemCount=root.findViewById(R.id.ItemCount);
        this.activity=(MainActivity)getContext();
        final LinearLayoutManager pathLinearLayoutManager= new LinearLayoutManager(getContext());
        pathLinearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        PathListRecyclerView.setLayoutManager(pathLinearLayoutManager);
        FileListRecyclerView.setItemAnimator(null);
        FileListRecyclerView.setAnimation(null);
        FileListRecyclerView.setHasFixedSize(true);
        FileListRecyclerView.setItemViewCacheSize(20);
        FileListRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        FileOperations=root.findViewById(R.id.FILE_HANDLE);
        FileOperations.setItemIconTintList(null);
        unCheckFileOperationMenu();
        final CustomFile entryFile= new CustomFile(EntryPath);

      if(!directoryManager.contains(entryFile)) {
              directoryManager.createDir(getContext(), entryFile);
              final Folder folder = directoryManager.currentDir();
              storageAdapter = new StorageAdapter(folder, getContext());
              pathAdapter = new DirectoryPathAdapter(getContext());
              initializeFolders(folder);
      }else{
          storageAdapter.setFolder(directoryManager.currentDir());
          pathAdapter.setSegments(directoryManager.currentDir().getPath());
          loadingProgress.setVisibility(View.INVISIBLE);
        }

        setFileView();
       final SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getContext());
        storageAdapter.setItemListener(new StorageAdapter.ItemListener() {
            @Override
            public void onItemClick(int position) {

                if(position<0)
                    return;
                final CustomFile file=storageAdapter.get(position);
                final Folder parentFolder=directoryManager.currentDir();
                // create a new directory if the file is a directory
                if(file.isDirectory()&operations.equals(Operations.NAVIGATE)) {
                     openFolder(file,parentFolder);
                }else if(!file.isDirectory()&operations.equals(Operations.NAVIGATE)){
                    boolean openZipWithThisApp=preferences.getBoolean("zipFileOpen",true);
                    boolean openImageWithThisApp=preferences.getBoolean("imageFileOpen",true);
                    boolean openVideoWithThisApp=preferences.getBoolean("videoFileOpen",true);
                    // open compressed with inbuilt zip extractor
                    if(FileFilters.isCompressed(file.getName())&openZipWithThisApp){
                       openZipFileAction(file);
                       // open image with inbuilt imageViewer
                    }else if(FileFilters.isImage(file.getName())&openImageWithThisApp) {
                        Intent pictureViewerIntent = new Intent(getContext(), PictureViewerActivity.class);
                        pictureViewerIntent.putExtra("path", file.getPath());
                        startActivityForResult(pictureViewerIntent, 0);
                    }else if(FileFilters.isVideo(file.getName())&openVideoWithThisApp) {
                        Intent videoViewerIntent = new Intent(getContext(), VideoPlayerActivity.class);
                        videoViewerIntent.putExtra("path", file.getPath());
                        videoViewerIntent.putExtra("sortOrder",getFolder().getSortBy());
                        startActivityForResult(videoViewerIntent, 0);
                    }else if(FileFilters.isAudio(file.getName())){
                        Intent musicPlayerIntent=new Intent(getContext(), MusicPlayerActivity.class);
                         MusicHelperSingleton.getInstance().clear();
                         MusicHelperSingleton.getInstance().getAllSongs().clear();
                         MusicHelperSingleton.getInstance().add(getFolder().getFiles());
                         MusicHelperSingleton.getInstance().setCurrent(position);
                         startActivity(musicPlayerIntent);
                    }else
                        try {
                            if(file.exists())
                            new MIMETypesHelper(getContext(), file).startDefault();
                            else Toast.makeText(getContext(),"File is deleted!",Toast.LENGTH_LONG).show();
                        }catch (IllegalArgumentException ignore){ }
                }else{

                    // select mode
                    storageAdapter.get(position).toggleSelect();
                    storageAdapter.notifyItemChanged(position);
                    // add it or remove to our list for later file op
                     if(storageAdapter.get(position).IsSelected())
                    directoryManager.currentDir().addToMultiSelectList(position);
                     else
                    directoryManager.currentDir().removeFromMultiSelectList(storageAdapter.get(position));
                     globalFileOperations.itemCount(directoryManager.currentDir().getMultiSelectedFiles().size());
                    ItemCount.setText(directoryManager.currentDir().getMultiSelectedFiles().size()+"/"+getFolder().size());
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onItemLongClick(int position) {
                if(position<0)
                    return;
                showFloatingButton(false);
                // open file-handle options
                FileOperations.setVisibility(View.VISIBLE);
                operations=Operations.SELECT;
                storageAdapter.setOperations(operations);
                if(!storageAdapter.get(position).IsSelected())
                    directoryManager.currentDir().addToMultiSelectList(position);
                storageAdapter.get(position).setSelected(true);
                storageAdapter.notifyDataSetChanged();
                globalFileOperations.select(operations);
                globalFileOperations.itemCount(directoryManager.currentDir().getMultiSelectedFiles().size());
                ItemCount.setText(directoryManager.currentDir().getMultiSelectedFiles().size()+"/"+getFolder().size());
                ItemCount.setVisibility(View.VISIBLE);
            }
        });



        FileListRecyclerView.clearOnScrollListeners();
           FileListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
               @Override
               public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                   if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                       Pair<Integer,Integer>range=getVisibleItemRange();
                       storageAdapter.LoadThumbnails(range.first, range.second, new ThumbnailLoader.onThumbnailComplete() {
                           @Override
                           public void onComplete(List<Integer> positions) {
                               for(int i=0;i<positions.size();i++){
                                   storageAdapter.notifyItemChanged(positions.get(i));
                               }
                           }
                       });
                     if(operations.equals(Operations.NAVIGATE))
                       showFloatingButton(true);
                   }else  if(newState==RecyclerView.SCROLL_STATE_DRAGGING){
                       showFloatingButton(false);
                       getFolder().setPosition(getVisibleItemRange().second);
                   }
               }
               @Override
               public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                   super.onScrolled(recyclerView, dx, dy);
               }
           });


           pathAdapter.setItemListener(new DirectoryPathAdapter.ItemListener() {
               @Override
               public void onItemClick(int position) {
                   if(!directoryManager.currentDir().getType().equals(FilterType.DEFAULT)&
                           !directoryManager.currentDir().getType().equals(FilterType.SYSTEM))
                       return;
                   String path=pathAdapter.getSegmentAsString(position);
                   String nP=entryFile.getPath()+"/"+path;
                   path=nP;
                 final CustomFile parent=new CustomFile(path);
                   Folder folder;
                 if(!directoryManager.contains(parent.getPath())){
                     folder=directoryManager.moveTo(getContext(),parent);
                     storageAdapter.setFolder(folder);
                     storageAdapter.notifyDataSetChanged();
                     FileListRecyclerView.scrollToPosition(folder.getPosition());
                     initializeFolders(directoryManager.currentDir());
                 }else {
                     folder=directoryManager.moveTo(getContext(),parent);
                     storageAdapter.setFolder(folder);
                     folder.removeDeleted();
                     storageAdapter.notifyDataSetChanged();
                     FileListRecyclerView.scrollToPosition(folder.getPosition());
                 }
                   FileListRecyclerView.scrollToPosition(directoryManager.currentDir().getAdapterPosition());
                   setCurrentDirectoryPath(directoryManager.currentDir());

               }
           });

           final TextInputEditText searchTextView=root.findViewById(R.id.search_text);
           final ImageView EnableSearch=root.findViewById(R.id.filter);
           final ImageView CancelSearch=root.findViewById(R.id.cancel_search);

           searchTextView.addTextChangedListener(new TextWatcher() {
               @Override
               public void beforeTextChanged(CharSequence s, int start, int count, int after) {

               }

               @Override
               public void onTextChanged(CharSequence s, int start, int before, int count) {

                   storageAdapter.search(s.toString(), new Folder.onSearchComplete() {
                       @Override
                       public void onComplete(ArrayList<Integer> positions) {
                           if(positions.isEmpty()) {
                               getFolder().resetSearchHighlights();
                               storageAdapter.notifyDataSetChanged();
                               return;
                           }
                           // move to the searched item  to first position
                           int position=positions.get(0);
                           FileListRecyclerView.scrollToPosition(position);
                           int start;
                           int stop;
                           if(FileListRecyclerView.getLayoutManager() instanceof LinearLayoutManager){
                               LinearLayoutManager LinearManager=(LinearLayoutManager)FileListRecyclerView.getLayoutManager();
                               start=LinearManager.findFirstVisibleItemPosition();
                               stop=LinearManager.findLastVisibleItemPosition();
                               directoryManager.currentDir().setAdapterPosition(LinearManager.findLastCompletelyVisibleItemPosition());
                           }else {
                               GridLayoutManager GridManager=(GridLayoutManager)FileListRecyclerView.getLayoutManager();
                               start=GridManager.findFirstVisibleItemPosition();
                               stop=GridManager.findLastVisibleItemPosition();
                               directoryManager.currentDir().setAdapterPosition(GridManager.findLastCompletelyVisibleItemPosition());
                           }
                           //push the found contents to the top when searching
                           int count=stop-start;
                           try {
                           if(storageAdapter.getItemCount()>position+count){
                               if(position>=(start+count/2)) {
                                   FileListRecyclerView.scrollToPosition(position + count);
                               }else {
                                   FileListRecyclerView.scrollToPosition(position - count);
                               }
                               stop=position+count;
                               storageAdapter.notifyDataSetChanged(); }
                           }catch (IndexOutOfBoundsException | NullPointerException ignore){

                           }
                           LoadThumbnails(start,stop);

                       }
                   });
               }

               @Override
               public void afterTextChanged(Editable s) {


               }
           });

           EnableSearch.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   root.findViewById(R.id.search_layout).setVisibility(View.VISIBLE);
                   EnableSearch.setVisibility(View.INVISIBLE);
                   CancelSearch.setVisibility(View.VISIBLE);
               }
           });
           CancelSearch.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   root.findViewById(R.id.search_layout).setVisibility(View.GONE);
                   EnableSearch.setVisibility(View.VISIBLE);
                   CancelSearch.setVisibility(View.INVISIBLE);
                   searchTextView.setText("");
                   getFolder().resetSearchHighlights();
                   storageAdapter.notifyDataSetChanged();
               }
           });
        FileListRecyclerView.setAdapter(storageAdapter);
        PathListRecyclerView.setAdapter(pathAdapter);

        // init storage operations
        storageOperations();
        initToolBarMenuClickListener();
        final MainActivity activity=((MainActivity)getContext());
        activity.getGlobalFileHandleLayout().setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                CustomFile file=getFolder().getFile();
                switch (item.getItemId()){
                    case R.id.Paste:
                        String storage=DiskUtils.getInstance().getStartDirectory(file);
                        if(!isStoragePermissionGranted(file)) {
                            getStoragePermission(new CustomFile(storage));
                        }else {
                            //paste files
                           copyOrCutFiles();
                        }
                        break;
                    case R.id.Cancel:
                        activity.getGlobalFileHandleLayout().setVisibility(View.GONE);
                        // clear copy and cut buffers here
                        CopyHelper.getInstance().reset();
                        CutHelper.getInstance().reset();
                        break;
                }
                return false;
            }
        });


        addFolderFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 createFolder();
            }
        });

        // set it to invisible in the gallery mode
        if(!getFolder().getType().equals(FilterType.DEFAULT))
            addFolderFloatingButton.setVisibility(View.INVISIBLE);

        // in case there is an intent call
        if(!(action.equals("")&uriAction.equals(""))) {
            CustomFile fileAction=new CustomFile(uriAction);
            if (FileFilters.isCompressed(fileAction.getName())) {
               openAction(fileAction);
            }else {
                saveFileAction(fileAction);
            }
        }

      return root;
    }

    private void openAction(CustomFile fileAction){
        if(fileAction.getName().endsWith(".zip")){
            openZipFileAction(fileAction);
        }
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setUriAction(String uriAction) {
        this.uriAction = uriAction;
    }


    private void openZipFileAction(CustomFile file){
        ArchiveExtractorDialog archiveExtractorDialog= new ArchiveExtractorDialog(getContext(),file);
        archiveExtractorDialog.setOnExtractCompleteCallback(new ArchiveDecompressUtil.OnExtractCompleteCallback() {
            @Override
            public void onComplete(String dest) {
                if(getContext()!=null)
                    Toast.makeText(getContext(), "Complete!", Toast.LENGTH_SHORT).show();
                File folder = new File(dest);
                if (directoryManager.contains(folder.getPath())) {
                    refresh(folder.getPath());
                }

            }

            @Override
            public void onError() {
                //  Toast.makeText(getContext(),"error!",Toast.LENGTH_SHORT).show();
            }
        });
        archiveExtractorDialog.show();
    }

    private void saveFileAction(CustomFile file){
        CopyHelper.getInstance().add(file);
        activity.getGlobalFileHandleLayout().setVisibility(View.VISIBLE);
        getFolder().getMultiSelectedFiles().add(file);
        globalFileOperations.itemCount(1);
    }


    private void showFloatingButton(final boolean flag){
        if(!getFolder().getType().equals(FilterType.DEFAULT)){
            addFolderFloatingButton.setVisibility(View.INVISIBLE);
            return;
        }

        Animation animation;
        if(flag)
         animation= AnimationUtils.loadAnimation(getContext(),R.anim.fade_in_fade_out);
        else
         animation= AnimationUtils.loadAnimation(getContext(),R.anim.popup_fadeout);
        addFolderFloatingButton.setAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                addFolderFloatingButton.setVisibility(flag?View.VISIBLE:View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
      addFolderFloatingButton.startAnimation(animation);


    }

    private boolean isItemVisible(int position){
        Pair<Integer,Integer>pair=getVisibleItemRange();
        return (pair.first<=position&position<=pair.second);
    }

    private Pair<Integer,Integer> getVisibleItemRange(){
        int start;
        int stop;
        if(FileListRecyclerView.getLayoutManager() instanceof LinearLayoutManager){
            LinearLayoutManager LinearManager=(LinearLayoutManager)FileListRecyclerView.getLayoutManager();
            start=LinearManager.findFirstVisibleItemPosition();
            stop=LinearManager.findLastVisibleItemPosition();
            directoryManager.currentDir().setAdapterPosition(LinearManager.findLastCompletelyVisibleItemPosition());

        }else {
            GridLayoutManager GridManager=(GridLayoutManager)FileListRecyclerView.getLayoutManager();
            start=GridManager.findFirstVisibleItemPosition();
            stop=GridManager.findLastVisibleItemPosition();
            directoryManager.currentDir().setAdapterPosition(GridManager.findLastCompletelyVisibleItemPosition());

        }
        return new Pair<>(start,stop);
    }


    private void openFolder(CustomFile file,final Folder parentFolder){
        if (directoryManager.contains(file)) {
            final Folder folder=directoryManager.moveTo(getContext(),file);
            folder.removeDeleted();
            storageAdapter.setFolder(folder);
            storageAdapter.notifyDataSetChanged();
            folder.setPosition(0);
            setCurrentDirectoryPath(folder);
        } else {
            String storage=DiskUtils.getInstance().getStartDirectory(file);
            if(!isStoragePermissionGranted(file)&file.isAndroidDirectory()) {
                getStoragePermission(new CustomFile(storage));
                return;
            }
            directoryManager.createDir(getContext(), file);
            storageAdapter.setFolder(directoryManager.currentDir());
            directoryManager.currentDir().setParent(parentFolder);
            storageAdapter.notifyDataSetChanged();
            directoryManager.currentDir().setPosition(0);
            initializeFolders(directoryManager.currentDir());
        }
        if(FileListRecyclerView.getLayoutManager()!=null)
        FileListRecyclerView.getLayoutManager().scrollToPosition(0);
    }

    private void moveBack(){
        final CustomFile parent= new CustomFile(directoryManager.currentDir().getFile().getParent());
        Folder folder;
        Folder parentFolder=directoryManager.currentDir().getParent();
        // move to previous folder
        if(parentFolder!=null){
            directoryManager.setCurrent(parentFolder);
            storageAdapter.setFolder(parentFolder);
            setCurrentDirectoryPath(parentFolder);
            parentFolder.removeDeleted();
            storageAdapter.initFolderSize();
            storageAdapter.notifyDataSetChanged();
            FileListRecyclerView.scrollToPosition(parentFolder.getPosition());
        }
        else if(!directoryManager.contains(parent)){
            folder= directoryManager.moveTo(getContext(),parent);
            setCurrentDirectoryPath(folder);
            storageAdapter.setFolder(folder);
            storageAdapter.notifyDataSetChanged();
            storageAdapter.initFolderSize();
            FileListRecyclerView.scrollToPosition(folder.getPosition());
            initializeFolders(directoryManager.currentDir());
        }else {
            folder=directoryManager.moveTo(getContext(),parent);
            setCurrentDirectoryPath(folder);
            directoryManager.setCurrent(folder);
            storageAdapter.setFolder(folder);
            folder.removeDeleted();
            storageAdapter.initFolderSize();
            FileListRecyclerView.scrollToPosition(folder.getPosition());
            storageAdapter.notifyDataSetChanged();
        }

    }

    private void initializeFolders(final Folder folder){
         loadingProgress.setVisibility(View.VISIBLE);
         FileListRecyclerView.setVisibility(View.INVISIBLE);
        folder.init( new FileHandleUtil.OnTaskComplete() {
            @Override
            public void onComplete() {
              FileListRecyclerView.setVisibility(View.VISIBLE);
              FileListRecyclerView.scrollToPosition(folder.getPosition());
             if(!folder.getType().equals(FilterType.DEFAULT))
               displayStatusMessage(folder.isEmpty(),"No Files here");
                storageAdapter.notifyDataSetChanged();
                loadingProgress.setVisibility(View.INVISIBLE);
                checkIfContentDisplay();
                setCurrentDirectoryPath(folder);
                storageAdapter.initFolderSize();
            }

        });
    }

    private void displayStatusMessage(boolean visibility,String message){
        statusMessage.setVisibility(visibility?View.VISIBLE:View.INVISIBLE);
        statusMessage.setText(message);
    }
    private void checkIfContentDisplay(){
        FileListRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Pair<Integer,Integer>range=getVisibleItemRange();
                LoadThumbnails(range.first,range.second);
                if(!getFolder().getType().equals(FilterType.DEFAULT))
                displayStatusMessage(getFolder().isEmpty(),"No Files");
                FileListRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }


    private void LoadThumbnails(int start,int stop){
        storageAdapter.LoadThumbnails(start,stop,new ThumbnailLoader.onThumbnailComplete() {
            @Override
            public void onComplete(List<Integer> positions) {
                for(int i=0;i<positions.size();i++){
                   if(isItemVisible(positions.get(i)))
                    storageAdapter.notifyItemChanged(positions.get(i));
                }
            }
        });
    }

    private boolean isStoragePermissionGranted(CustomFile file){
        String storage=DiskUtils.getInstance().getStartDirectory(file);
        return PermissionsHelper.getInstance().uriValid(new File(storage),getContext());
    }

    private void getStoragePermission(CustomFile file){
        String storage=DiskUtils.getInstance().getStartDirectory(file);
            if (!PermissionsHelper.getInstance().uriValid(new File(storage),getContext())) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                activity.startActivityForResult(intent, 32);
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

    @Nullable
    @Override
    public View getView() {
        return super.getView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // update the screen view
        updateWindowView();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void setCurrentDirectoryPath(final Folder folder){
        pathAdapter.setSegments(folder.getPath());
        PathListRecyclerView.scrollToPosition(pathAdapter.getItemCount()-1);

    }

   // creates and updates the window
    public void updateWindowView(){
        if(!isDeleted()) {
            final Fragment fragment = this;
            WindowUtil.getInstance().put(fragment,key, root);
            WindowModel model = WindowUtil.getInstance().get(getFragmentID());
            if(type.equals(FilterType.DEFAULT)) {
                model.setPath(storageAdapter.getPath());
            } else {
                model.setPath(type.toString());
            }
            model.setTitle(title);
            WindowUtil.getInstance().setCurrent(getFragmentID());
        }
    }

    @SuppressLint("SetTextI18n")
    public void exitSelectMode(){
        showFloatingButton(true);
        directoryManager.currentDir().resetMultiSelectedList();
        operations=Operations.NAVIGATE;
        storageAdapter.setOperations(operations);
        storageAdapter.notifyDataSetChanged();
        globalFileOperations.select(operations);
        FileOperations.setVisibility(View.GONE);
        ItemCount.setVisibility(View.GONE);
        ItemCount.setText("0/" + getFolder().size());
    }

    @SuppressLint("SetTextI18n")
    public void selectAll(){
        directoryManager.currentDir().addAllToMultiselectList();
        storageAdapter.notifyDataSetChanged();
        globalFileOperations.itemCount(directoryManager.currentDir().getMultiSelectedFiles().size());
        ItemCount.setText(directoryManager.currentDir().getMultiSelectedFiles().size()+"/"+getFolder().size());
    }

    private void storageOperations(){
        FileOperations.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                CustomFile file;
                String storage;
                switch (item.getItemId()){
                    case R.id.COPY:
                        initCopyHelper();
                        globalFileOperations.copy();
                        exitSelectMode();
                        FileOperations.setVisibility(View.GONE);
                        break;
                    case R.id.DELETE:
                         file=getFolder().getMultiSelectedFiles().get(0);
                         storage=DiskUtils.getInstance().getStartDirectory(file);
                        if(!isStoragePermissionGranted(file)) {
                            getStoragePermission(new CustomFile(storage));
                        } else {
                            Delete();exitSelectMode();
                        FileOperations.setVisibility(View.GONE);}
                        break;
                    case R.id.CUT:
                        initCutHelper();
                        globalFileOperations.cut();
                        exitSelectMode();
                        FileOperations.setVisibility(View.GONE);
                        break;
                    case R.id.RENAME:
                         file=getFolder().getMultiSelectedFiles().get(0);
                         storage=DiskUtils.getInstance().getStartDirectory(file);
                        if(!isStoragePermissionGranted(file)) {
                            getStoragePermission(new CustomFile(storage));
                        }else {
                            Rename();
                            exitSelectMode();
                            FileOperations.setVisibility(View.GONE);
                        }
                        break;
                    case R.id.MORE:
                        moreOptionsPopup(FileOperations);
                        break;

                }

                unCheckFileOperationMenu();
                return false;
            }
        });
    }

    private void unCheckFileOperationMenu(){
        FileOperations.getMenu().setGroupCheckable(0,true,false);
        for(int i=0;i<FileOperations.getMenu().size();i++){
            FileOperations.getMenu().getItem(i).setChecked(false);
        }
        FileOperations.getMenu().setGroupCheckable(0,true,true);

    }


    private void Delete() {
        final Folder folder=directoryManager.currentDir();
        if(folder.getMultiSelectedFiles().isEmpty())

            return;
        Toast.makeText(getContext(),"test",Toast.LENGTH_SHORT).show();
        ConfirmDeleteDialog deleteDialog= new ConfirmDeleteDialog(getContext(),folder.getMultiSelectedFiles());
        deleteDialog.setOnCompleteListener(new DeleteFilesUtility.OnDeleteCompleteListener() {
            @Override
            public void onSuccess(ArrayList<CustomFile> data) {
                folder.removeDeleted(data);
                storageAdapter.notifyDataSetChanged();

            }
        });
        deleteDialog.show();

    }

    private void initCopyHelper(){
        final Folder folder=directoryManager.currentDir();
        if(folder.getMultiSelectedFiles().isEmpty())
            return;
        CopyHelper.getInstance().add(folder.getMultiSelectedFiles());
        CopyHelper.getInstance().setParent(folder.getFile());
    }

    private void initCutHelper(){
        final Folder folder=directoryManager.currentDir();
        if(folder.getMultiSelectedFiles().isEmpty())
            return;
        CutHelper.getInstance().add(folder.getMultiSelectedFiles());
        CutHelper.getInstance().setParent(folder.getFile());
    }

    private void  Cut(){
        final Folder folder = directoryManager.currentDir();
        if(CutHelper.getInstance().getDestination().canWrite()| (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N&!CutHelper.getInstance().getDestination().canWrite())) {
            final MoveFileDialog dialog = new MoveFileDialog(getContext());
            dialog.setOnCutListener(new CutUtility.OnCutListener() {
                @Override
                public void onSuccess(ArrayList<CustomFile> files) {
                    folder.add(files);
                    FileListRecyclerView.setAdapter(storageAdapter);
                    Pair<Integer,Integer>range=getVisibleItemRange();
                    LoadThumbnails(range.first,range.second);
                    storageAdapter.initFolderSize();
                    CutHelper.getInstance().reset();
                    Toast.makeText(getContext(),dialog.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        }else {
            /* for devices less than api 24 document documentContract doesn't
            support moving & copying files*/
            Toast.makeText(getContext(),"unsupported by volume",Toast.LENGTH_SHORT).show();
            // we copy the files instead if the volume doesn't support move files
            CopyHelper.getInstance().setDestination(CutHelper.getInstance().getDestination());
            CopyHelper.getInstance().add(CutHelper.getInstance().getData());
            CopyHelper.getInstance().setDeleteOldFiles(true);
            try {
                final CopyFileDialog dialog = new CopyFileDialog(getContext());
                dialog.setCompleteListener(new CopyUtility.OnCopyListener() {
                    @Override
                    public void onSuccess(ArrayList<CustomFile> files) {
                        folder.add(files);
                        FileListRecyclerView.setAdapter(storageAdapter);
                        Pair<Integer,Integer>range=getVisibleItemRange();
                        LoadThumbnails(range.first,range.second);
                        CopyHelper.getInstance().reset();
                    }

                    @Override
                    public void onFailed(ArrayList<CustomFile> files) {

                    }

                });

                dialog.show();
            }catch (NullPointerException ne){
                Toast.makeText(getContext(),"An error occurred!",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void copy(){

        final Folder folder=directoryManager.currentDir();

        try {

            final CopyFileDialog dialog = new CopyFileDialog(getContext());
            dialog.setCompleteListener(new CopyUtility.OnCopyListener() {

                @Override
                public void onSuccess(ArrayList<CustomFile> files) {
                    folder.add(files);
                    FileListRecyclerView.setAdapter(storageAdapter);
                    Pair<Integer,Integer>range=getVisibleItemRange();
                    LoadThumbnails(range.first,range.second);
                    storageAdapter.initFolderSize();
                    CopyHelper.getInstance().reset();
                    Toast.makeText(getContext(),dialog.getMessage(),Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onFailed(ArrayList<CustomFile> files) {
                    folder.add(files);
                    FileListRecyclerView.setAdapter(storageAdapter);
                    Pair<Integer,Integer>range=getVisibleItemRange();
                    LoadThumbnails(range.first,range.second);
                    storageAdapter.initFolderSize();
                    CopyHelper.getInstance().reset();
                    Toast.makeText(getContext(),dialog.getMessage(),Toast.LENGTH_SHORT).show();
                }

            });
            dialog.show();
        }catch (NullPointerException ne){
           Toast.makeText(getContext(),"An error occurred!",Toast.LENGTH_SHORT).show();
        }
    }

    public void CopyPaste(){
        if(!CopyHelper.getInstance().isEmpty()) {
            CopyHelper.getInstance().setDestination(getFolder().getFile());
            final String startDir = DiskUtils.getInstance().getStartDirectory(getFolder().getFile());
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            final SharedPreferences.Editor editor = preferences.edit();
            final String copy = preferences.getString("copy", "Ask me Always");

            boolean askMeAlways = copy.equals("Ask me Always");
            if (askMeAlways) {
                final TaskDialogPrompt taskDialogPrompt = new TaskDialogPrompt(getContext());
                taskDialogPrompt.setOnItemClickListener(new TaskDialogPrompt.OnItemClickListener() {
                    @Override
                    public void onClick() {
                        String choice = "Ask me Always";
                        if (taskDialogPrompt.isForeground()) {
                            CopyServiceQueue.getInstance().update(getContext(), PermissionsHelper.getInstance().getUriFromSharedPreference(
                                    new File(startDir)));
                            setCopyCompleteListener(CopyServiceQueue.getInstance().getFirst());
                            getContext().startService(new Intent(getActivity(), CopyService.class));
                            choice = taskDialogPrompt.rememberChoice() ? "Background Always" : choice;
                        } else {
                            choice = taskDialogPrompt.rememberChoice() ? "Foreground Always" : choice;
                            copy();
                        }

                        editor.putString("copy", choice);
                        editor.apply();
                    }
                });
                taskDialogPrompt.show();
            } else {
                if (copy.equals("Background Always")) {
                    CopyServiceQueue.getInstance().update(getContext(), PermissionsHelper.getInstance().getUriFromSharedPreference(
                            new File(startDir)));
                    setCopyCompleteListener(CopyServiceQueue.getInstance().getFirst());
                    Intent intent = new Intent(getActivity(), CopyService.class);
                    getContext().startService(intent);
                } else {
                    copy();
                }
            }
        }else {
            exitSelectMode();
        }
    }

    private void setCopyCompleteListener(CopyUtility utility){
        utility.setCopyListener(new CopyUtility.OnCopyListener() {
            @Override
            public void onSuccess(ArrayList<CustomFile> files) {
                getFolder().add(files);
                FileListRecyclerView.setAdapter(storageAdapter);
                Pair<Integer,Integer>range=getVisibleItemRange();
                LoadThumbnails(range.first,range.second);
                storageAdapter.initFolderSize();
                CopyHelper.getInstance().reset();
                if(getContext()!=null)
                    Toast.makeText(getContext(),"success! files copied",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(ArrayList<CustomFile> files) {

            }
        });
    }
    public void CutPaste(){
        CutHelper.getInstance().setDestination(getFolder().getFile());
        if(!CutHelper.getInstance().isEmpty()) {
            Cut();
        }else {
            exitSelectMode();
        }
    }

    private boolean copyOrCutFiles(){
        final ArrayList<CustomFile>data;
        if(!CopyHelper.getInstance().isEmpty()) {
            data = CopyHelper.getInstance().getData();
            if(CopyHelper.getInstance().contains(getFolder().getPath())){
                exitSelectMode();
                Toast.makeText(getContext(),"Choose different Directory!",Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            data=CutHelper.getInstance().getData();
            if(CutHelper.getInstance().contains(getFolder().getPath())){
                exitSelectMode();
                Toast.makeText(getContext(),"Choose different Directory!"+getFolder().getPath(),Toast.LENGTH_SHORT).show();
                return false;
            }

        }

        // if there is a naming conflict open the overwrite dialog
        if(!getFolder().containsName(data).isEmpty()){
            final OverWriteDialog overWriteDialog= new OverWriteDialog(getContext(),data);
            overWriteDialog.setOnCompleteListener(new OnTaskCompleteListener() {
                @Override
                public void onTaskComplete() {
                  pasteFiles(data);
                }
            });
            overWriteDialog.show();
            return true;
        }else {
             pasteFiles(data);
        }
        return false;
    }

    private void pasteFiles(ArrayList<CustomFile>data){
        if(!data.isEmpty()) {
            if (!CopyHelper.getInstance().isEmpty()) {
                CopyPaste();
            } else {
                CutPaste();
            }
        }else {
            exitSelectMode();
        }
        globalFileOperations.paste();
        activity.getGlobalFileHandleLayout().setVisibility(View.GONE);
    }
    private void Rename(){
        final Folder folder=directoryManager.currentDir();
        if(folder.getMultiSelectedFiles().isEmpty())
            return;
        if(folder.getMultiSelectedFiles().size()<=1) {
            final CustomFile rFile = folder.getMultiSelectedFiles().get(0);
            SingleFileRenameDialog renameDialog = new SingleFileRenameDialog(getContext(), rFile);
            renameDialog.show();
            renameDialog.setOnCompleteListener(new SingleFileRenameDialog.OnRenameComplete() {
                @Override
                public void onComplete(CustomFile nFile) {
                    folder.replace(rFile, nFile);
                    if(nFile.isDirectory()) {
                       if(directoryManager.contains(nFile)){
                           directoryManager.remove(nFile);
                       }
                    }
                    FileListRecyclerView.setAdapter(storageAdapter);
                }
            });
        }else{
            BatchFileRenameDialog batchFileRenameDialog= new BatchFileRenameDialog(getContext(),folder.getMultiSelectedFiles());
            batchFileRenameDialog.show();
            batchFileRenameDialog.setOnCompleteListener(new BatchFileRenameDialog.OnRenameComplete() {
                @Override
                public void update(CustomFile oFile,CustomFile nFile) {
                    folder.replace(oFile, nFile);
                    if(nFile.isDirectory()) {
                        if(directoryManager.contains(nFile)){
                            directoryManager.remove(nFile);
                        }
                    }
                }

                @Override
                public void complete() {
                    FileListRecyclerView.setAdapter(storageAdapter);
                    Pair<Integer,Integer>range=getVisibleItemRange();
                    LoadThumbnails(range.first,range.second);
                }
            });
        }
    }


    private void createFolder(){
        CustomFile file=getFolder().getFile();
        String storage=DiskUtils.getInstance().getStartDirectory(file);
        if(!PermissionsHelper.getInstance().uriValid(new File(storage),getContext())) {
            getStoragePermission(new CustomFile(storage));
            return;
        }
        NewFolderDialog newFolderDialog= new NewFolderDialog(getContext(),file);
        newFolderDialog.setOnCompleteListener(new NewFolderDialog.onCompleteListener() {
            @Override
            public void onComplete(CustomFile folder) {
                folder.setTempThumbnail();
                getFolder().add(folder);
                storageAdapter.notifyDataSetChanged();
            }
        });
        newFolderDialog.show();
    }
    private void initToolBarMenuClickListener(){
        final Toolbar toolbar=((MainActivity)getContext()).toolbar;
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.IC_WINDOW:
                        updateWindowView();
                        final SwitchWindowDialog switchWindowDialog= new SwitchWindowDialog(getContext());
                       final ViewTreeObserver.OnGlobalLayoutListener layoutListener= new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                // update the screen view
                                updateWindowView();
                                switchWindowDialog.getAdapter().notifyDataSetChanged();
                            }
                        };

                       root.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
                        switchWindowDialog.show();
                        switchWindowDialog.setOnWindowSelectListener(new SwitchWindowAdapter.onWindowSelectListener() {
                            @Override
                            public void onWindowSelect(WindowModel model) {
                                // switch fragment in mainActivity
                                globalFileOperations.switchWindow(model);
                                toolbar.setSubtitle(model.getTitle());
                                switchWindowDialog.cancel();
                                root.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);

                            }

                            @Override
                            public void onCancel() {
                                // if not removed we get terrible lag lol
                                root.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);

                            }
                        });

                        break;
                    case R.id.ACTION_SETTINGS:
                        preference(toolbar);
                        break;
                    case R.id.SELECT_ALL:
                       selectAll();
                        break;
                    case R.id.REFRESH:
                        refresh(getFolder().getPath());
                        break;
                }
                return false;
            }
        });
    }

    private void setFileView(){
        final SharedPreferences preferences=getContext().getSharedPreferences("MyPref",Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor=preferences.edit();
        final Folder folder=directoryManager.currentDir();
        final String type=folder.getType().toString();
        String mode="";
        if(folder.getType().equals(FilterType.IMAGE)|
           folder.getType().equals(FilterType.VIDEO)){
            mode=preferences.getString(type,"GRID");
        }else {
            mode=preferences.getString(type,"LIST");
        }

        if(mode.equals("LIST")) {
            editor.putString(type, mode);
            editor.apply();
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
             removeItemDecorations(FileListRecyclerView);
            FileListRecyclerView.setLayoutManager(linearLayoutManager);
        }else {
            editor.putString(type,mode);
            editor.commit();
            DividerItemDecoration vertical=new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL);
            vertical.setDrawable(ContextCompat.getDrawable(getContext(),R.drawable.divider_shape));
            GridLayoutManager gridLayoutManager= new GridLayoutManager(getContext(),3);
            removeItemDecorations(FileListRecyclerView);
            FileListRecyclerView.addItemDecoration(vertical);
            FileListRecyclerView.setLayoutManager(gridLayoutManager);
        }

    }


    // remove decoration incase the user changes the view
    private void removeItemDecorations(RecyclerView recyclerView){
        int count=recyclerView.getItemDecorationCount();
        for(int i=0;i<count;i++)
        recyclerView.removeItemDecorationAt(0);
    }

    private void preference(final View anchor){
        LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        final View view=inflater.inflate(R.layout.popup_preference_layout,null);
        final PopupWindow popupWindow= new PopupWindow(view,LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,true);
        popupWindow.showAsDropDown(anchor,0,0,Gravity.RIGHT);
        final RadioButton settings=view.findViewById(R.id.settings);
        final RadioButton viewType=view.findViewById(R.id.viewType);
        final RadioButton sortBy=view.findViewById(R.id.sortBy);

        final View.OnClickListener onClickListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.viewType:
                        viewTypePreference(anchor);
                        break;
                    case R.id.sortBy:
                        sortByPreference(anchor);
                        break;
                    case R.id.settings:
                        Intent intent=new Intent(getActivity(), SettingsActivity.class);
                        intent.putExtra("Theme",PreferenceManager.getDefaultSharedPreferences(getContext()).getString("theme","Light"));
                        startActivity(intent);
                        break;

                }
                popupWindow.dismiss();
            }
        };
       settings.setOnClickListener(onClickListener);
       sortBy.setOnClickListener(onClickListener);
       viewType.setOnClickListener(onClickListener);
    }

    private void sortByPreference(View anchor){
        LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=inflater.inflate(R.layout.popup_sortby_layout,null);
        final PopupWindow popupWindow= new PopupWindow(view,LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,true);
        popupWindow.showAsDropDown(anchor,0,0,Gravity.RIGHT);
        final Folder folder=directoryManager.currentDir();
        final View.OnClickListener onClickListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.name:
                        folder.setSortBy(SortBy.AZ);
                        break;
                    case R.id.date:
                        folder.setSortBy(SortBy.DATE);
                        break;
                    case R.id.size:
                        folder.setSortBy(SortBy.SIZE);
                        break;
                    case R.id.extension:
                        folder.setSortBy(SortBy.EXTENSION);
                        break;
                }
                popupWindow.dismiss();
                folder.sort();
                storageAdapter.notifyDataSetChanged();
                checkIfContentDisplay();
            }
        };
        view.findViewById(R.id.name).setOnClickListener(onClickListener);
        view.findViewById(R.id.date).setOnClickListener(onClickListener);
        view.findViewById(R.id.size).setOnClickListener(onClickListener);
        view.findViewById(R.id.extension).setOnClickListener(onClickListener);
    }

    private void viewTypePreference(View anchor){
        LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=inflater.inflate(R.layout.popup_view_type_layout,null);
        final PopupWindow popupWindow= new PopupWindow(view,LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,true);
        popupWindow.showAsDropDown(anchor,0,0,Gravity.RIGHT);
        final SharedPreferences.Editor edit=getContext().getSharedPreferences("MyPref",Context.MODE_PRIVATE).edit();
        final Folder folder=directoryManager.currentDir();
        final View.OnClickListener onClickListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==R.id.list){
                 edit.putString(folder.getType().toString(),ViewMode.LIST.toString());
                }else {
                 edit.putString(folder.getType().toString(),ViewMode.GRID.toString());
                }
                popupWindow.dismiss();
                edit.apply();
                setFileView();
                FileListRecyclerView.setAdapter(storageAdapter);
            }
        };
       view.findViewById(R.id.list).setOnClickListener(onClickListener);
       view.findViewById(R.id.grid).setOnClickListener(onClickListener);

    }

    private void moreOptionsPopup(View anchor){
        LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view=inflater.inflate(R.layout.popup_more_layout,null);
        final Folder folder=directoryManager.currentDir();
        final PopupWindow popupWindow= new PopupWindow(view,LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,true);
        popupWindow.showAtLocation(anchor,Gravity.BOTTOM|Gravity.RIGHT,0,0);
        final RadioButton open=view.findViewById(R.id.open);
        final RadioButton openAs=view.findViewById(R.id.openAs);
        final RadioButton share=view.findViewById(R.id.share);
        final RadioButton compress=view.findViewById(R.id.compress);
        if(folder.getMultiSelectedFiles().size()>1){
            openAs.setEnabled(false);
            share.setEnabled(false);
            open.setEnabled(false);
        }
        final View.OnClickListener onClickListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CustomFile file=folder.getMultiSelectedFiles().get(0);
                switch (v.getId()){

                    case R.id.properties:
                        final PropertiesDialog propertiesDialog= new PropertiesDialog(getContext(),
                                directoryManager.currentDir().getMultiSelectedFiles());
                        propertiesDialog.show();
                        break;
                    case R.id.open:
                        try {
                            new MIMETypesHelper(getContext(),file).startDefault();
                        }catch (Exception e){

                        }

                        break;
                    case R.id.share:
                        new MIMETypesHelper(getContext(),file).startShare();
                        break;
                    case R.id.openAs:
                        OpenAsDialog openAsDialog= new OpenAsDialog(getContext(),file);
                        openAsDialog.show();
                        break;
                    case R.id.bookmark:
                        addFilesToBookMarks();
                        Toast.makeText(getContext(),"Added to bookmarks",Toast.LENGTH_LONG).show();
                        break;
                    case R.id.compress:
                        ArchiveCompressorDialog compressorDialog= new ArchiveCompressorDialog(getContext(),folder.getMultiSelectedFiles());
                        compressorDialog.setCompleteCallback(new ArchiveCompressUtil.OnCompressionCompleteCallback() {
                            @Override
                            public void onComplete(File file) {
                                ArrayList<CustomFile>list= new ArrayList<>();
                                list.add(new CustomFile(file.getPath()));
                                for(CustomFile customFile:list)
                                    customFile.setTempThumbnail();
                                folder.add(list);
                                storageAdapter.notifyDataSetChanged();
                            }
                        });
                        compressorDialog.show();
                        break;
                }
                popupWindow.dismiss();
                exitSelectMode();
            }
        };
        view.findViewById(R.id.properties).setOnClickListener(onClickListener);
        view.findViewById(R.id.bookmark).setOnClickListener(onClickListener);
        open.setOnClickListener(onClickListener);
        share.setOnClickListener(onClickListener);
        openAs.setOnClickListener(onClickListener);
        compress.setOnClickListener(onClickListener);

    }


    private void addFilesToBookMarks(){
        if(getFolder().getMultiSelectedFiles().isEmpty())
            return;
        SharedPreferences preferences=getContext().getSharedPreferences("Bookmarks",Context.MODE_PRIVATE);
        if(preferences==null)
            return;
        SharedPreferences.Editor editor;
        for(CustomFile file:getFolder().getMultiSelectedFiles()){
                File book=file.isDirectory()?file:file.getParentFile();
                editor=preferences.edit();
                editor.putString(book.getPath(),book.getName());
                editor.apply();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    public Folder getFolder(){
        return directoryManager.currentDir();
    }

    public void refresh(String path){
        Folder dir= directoryManager.getDir(path);
        if(dir!=null) {
            dir.removeAll();
            if(directoryManager.currentDir().equals(dir))
                storageAdapter.notifyDataSetChanged();
                initializeFolders(dir);

        }
    }

    @Override
    public void setParent(Fragment parent) {
        this.parent = parent;
    }

    @Override
    public Fragment getParent() {
        return parent;
    }

    private void setTitleBarSubtitle(){
        MainActivity activity=(MainActivity)getContext();
        if(activity!=null)
        switch (type){
            case DEFAULT:
                activity.setSubtitle("Local");
                break;
            case SYSTEM:
                activity.setSubtitle("System");
                break;
            case IMAGE:
                activity.setSubtitle("Images");
                break;
            case AUDIO:
                activity.setSubtitle("Music");
                break;
            case VIDEO:
                activity.setSubtitle("Videos");
                break;
            case DOCUMENT:
                activity.setSubtitle("Documents");
                break;
            case COMPRESSED:
                activity.setSubtitle("Archive");
                break;
            case APPLICATION:
                activity.setSubtitle("Applications");
                break;
        }
    }
    @Override
    public void onBackPressed() {
     if(!DiskUtils.getInstance().isStartDirectory(directoryManager.currentDir().getPath())&
        !isDeleted())
        moveBack();
     else {
        WindowUtil window=WindowUtil.getInstance();
        if(parent instanceof StorageFragment) {
            StorageFragment fragment = (StorageFragment) parent;
            while (fragment!=null) {
                if (fragment.getParent() instanceof StorageFragment&!window.contains(fragment.getFragmentID())) {
                    fragment =(StorageFragment) fragment.getParent();
                } else {
                    parent=fragment;
                    activity.setFragment(fragment);
                   return;
                }
            }
        }

         activity.setFragment(parent);
     }
    }
}