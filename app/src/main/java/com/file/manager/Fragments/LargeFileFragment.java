package com.file.manager.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.IOnBackPressed;
import com.file.manager.Activities.MainActivity;
import com.file.manager.Operations;
import com.file.manager.Activities.PictureViewerActivity;
import com.file.manager.R;
import com.file.manager.Activities.VideoPlayerActivity;
import com.file.manager.helpers.MIMETypesHelper;
import com.file.manager.ui.Adapters.StorageAdapter;
import com.file.manager.ui.Dialogs.ArchiveExtractorDialog;
import com.file.manager.ui.Dialogs.ConfirmDeleteDialog;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.Folder;
import com.file.manager.FilterType;
import com.file.manager.utils.ArchiveDecompressUtil;
import com.file.manager.utils.DeleteFilesUtility;
import com.file.manager.utils.DiskUtils;
import com.file.manager.utils.FileFilters;
import com.file.manager.utils.FileHandleUtil;
import com.file.manager.utils.ThumbnailLoader;

import java.util.ArrayList;
import java.util.List;

public class LargeFileFragment extends Fragment implements IOnBackPressed {


    private Toolbar activityToolbar;
    private StorageAdapter adapter;
    private Folder folder;
    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private View messageLayout;
    private boolean deleted=false;
    private Fragment parent;
    private View root;
    private Toolbar toolbar;
    public LargeFileFragment(Fragment parent){
        this.parent=parent;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root=inflater.inflate(R.layout.large_fragment_files,container,false);
       final Button Delete=root.findViewById(R.id.delete);
       final Button Refresh=root.findViewById(R.id.refresh);
       final Button SelectAll=root.findViewById(R.id.selectAll);
       final TextView messageTv=root.findViewById(R.id.messageTv);
         recyclerView=root.findViewById(R.id.fileList);
         messageLayout=root.findViewById(R.id.messageLayout);
        final MainActivity activity=(MainActivity)(getContext());
        activityToolbar=activity.toolbar;
        activityToolbar.setVisibility(View.GONE);

        toolbar=root.findViewById(R.id.toolbar);

        toolbar.setSubtitle("Large Files");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId()==R.id.ACTION_SETTINGS){
                    minSizePopUp(toolbar);
                }
                return false;
            }
        });

        manager= new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(manager);

        folder= new Folder(getContext(),new CustomFile(DiskUtils.getInstance().getDirectory(0).getPath()));
        folder.setType(FilterType.LARGEFILES);
        adapter= new StorageAdapter(folder,getContext());
        adapter.setOperations(Operations.SELECT);
        recyclerView.setItemAnimator(null);
        recyclerView.setAdapter(adapter);

        folder.getMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                messageTv.setText(s);
            }
        });
        initFolder(folder);
        final SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getContext());
        adapter.setItemListener(new StorageAdapter.ItemListener() {
            @Override
            public void onItemClick(int position) {
               final CustomFile file=folder.get(position);
                file.setSelected(!file.IsSelected());
                if(file.IsSelected()){
                    folder.addToMultiSelectList(position);
                }else {
                    folder.removeFromMultiSelectList(file);
                }
                updateSelectedItemCount();
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onItemLongClick(int position) {
                final CustomFile file=folder.get(position);
                boolean openZipWithThisApp=preferences.getBoolean("zipFileOpen",true);
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
                }else if(FileFilters.isCompressed(file.getName())&openZipWithThisApp) {
                    openZipFileAction(file);
                }else {
                    try {
                        new MIMETypesHelper(getContext(), file).startDefault();
                    } catch (IllegalArgumentException ignore) {}
                }
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState==RecyclerView.SCROLL_STATE_IDLE){
                    LoadThumbnails();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        Refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isLoading()) {
                    initFolder(folder);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if(isLoading())
                  return;
                DeleteFiles();

            }
        });

        SelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLoading())
                    return;
                SelectAll.setText(!folder.getSelectAllStatus()?"SelectAll":"DeselectAll");
                folder.addAllToMultiSelectList();
                updateSelectedItemCount();
                adapter.notifyDataSetChanged();
            }
        });
        return root;
    }

    private void openZipFileAction(CustomFile file){
        ArchiveExtractorDialog archiveExtractorDialog= new ArchiveExtractorDialog(getContext(),file);
        archiveExtractorDialog.setOnExtractCompleteCallback(new ArchiveDecompressUtil.OnExtractCompleteCallback() {
            @Override
            public void onComplete(String dest) {
             Toast.makeText(getContext(), "done!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError() {
                //  Toast.makeText(getContext(),"error!",Toast.LENGTH_SHORT).show();
            }
        });
        archiveExtractorDialog.show();
    }

    private void LoadThumbnails(){
        int start=manager.findFirstVisibleItemPosition();
        int stop=manager.findLastVisibleItemPosition();

       adapter.LoadThumbnails(start, stop, new ThumbnailLoader.onThumbnailComplete() {
           @Override
           public void onComplete(List<Integer> positions) {
               for(int i=0;i<positions.size();i++){
                   adapter.notifyItemChanged(positions.get(i));
               }
           }
       });
    }


    private void updateSelectedItemCount(){
        toolbar.getMenu().getItem(0).setTitle(
                folder.getMultiSelectedFiles().size()+"/"+folder.getFiles().size());
    }
    private void initFolder(final Folder folder){
        messageLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        folder.init(new FileHandleUtil.OnTaskComplete() {
            @Override
            public void onComplete() {
                recyclerView.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
                updateSelectedItemCount();
                checkIfContentDisplaysOnScreen();
            }
        });


    }


    private void checkIfContentDisplaysOnScreen(){
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                LoadThumbnails();
                messageLayout.setVisibility(View.GONE);
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private boolean isLoading(){
        return messageLayout.getVisibility()==View.VISIBLE;
    }

    private void minSizePopUp(View anchor){

        if(isLoading())
            return;
        final DiskUtils diskUtils=DiskUtils.getInstance();
        final SharedPreferences preferences=getContext().getSharedPreferences("MyPref",Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor=preferences.edit();
        final long pBytes=preferences.getLong("MinLargeFile",diskUtils.SIZE_MB*50);

        LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view=inflater.inflate(R.layout.popup_large_files,null);
        final RadioButton min_size1=view.findViewById(R.id.min_size1);
        final RadioButton min_size2=view.findViewById(R.id.min_size2);
        final RadioButton min_size3=view.findViewById(R.id.min_size3);
        final RadioButton min_size4=view.findViewById(R.id.min_size4);
        final Button accept=view.findViewById(R.id.accept);
        if(200*diskUtils.SIZE_MB==pBytes){
            min_size1.setChecked(true);
        }else if(100*diskUtils.SIZE_MB==pBytes){
            min_size2.setChecked(true);
        }else if(50*diskUtils.SIZE_MB==pBytes){
            min_size3.setChecked(true);
        }else {
            min_size4.setChecked(true);
        }
        final PopupWindow popupWindow= new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,true);
        popupWindow.showAsDropDown(anchor,0,0, Gravity.RIGHT);
        final View.OnClickListener onClickListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()){
                    case R.id.min_size1:
                        editor.putLong("MinLargeFile",200*diskUtils.SIZE_MB);
                        break;
                    case R.id.min_size2:
                        editor.putLong("MinLargeFile",100*diskUtils.SIZE_MB);
                        break;
                    case R.id.min_size3:
                        editor.putLong("MinLargeFile",50*diskUtils.SIZE_MB);
                        break;
                    case R.id.min_size4:
                        editor.putLong("MinLargeFile",20*diskUtils.SIZE_MB);
                        break;
                    case R.id.accept:
                        popupWindow.dismiss();
                        break;
                }
                editor.commit();
            }
        };
        min_size1.setOnClickListener(onClickListener);
        min_size2.setOnClickListener(onClickListener);
        min_size3.setOnClickListener(onClickListener);
        min_size4.setOnClickListener(onClickListener);
        accept.setOnClickListener(onClickListener);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //refresh the list
                final long cBytes=preferences.getLong("MinLargeFile",DiskUtils.getInstance().SIZE_MB*50);
                if(pBytes!=cBytes){
                   initFolder(folder);
                   adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void DeleteFiles(){
        if(folder.getMultiSelectedFiles().isEmpty()) {
            Toast.makeText(getContext(),"Empty!",Toast.LENGTH_SHORT).show();
            return;
        }
        final ConfirmDeleteDialog dialog= new ConfirmDeleteDialog(getContext(),folder.getMultiSelectedFiles());
        dialog.setOnCompleteListener(new DeleteFilesUtility.OnDeleteCompleteListener() {
            @Override
            public void onSuccess(ArrayList<CustomFile> data) {
                folder.removeDeleted(data);
                adapter.notifyDataSetChanged();
                updateSelectedItemCount();
            }
        });
        dialog.show();
        folder.resetMultiSelectedList();
    }


    @Override
    public void onBackPressed() {
        if(parent!=null)
            ((MainActivity)getContext()).setFragment(parent);
    }
    @Override
    public void onDetach() {
        super.onDetach();
        activityToolbar.setVisibility(View.VISIBLE);
    }
}
