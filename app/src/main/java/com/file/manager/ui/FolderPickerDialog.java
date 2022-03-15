package com.file.manager.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.MainActivity;
import com.file.manager.R;
import com.file.manager.helpers.PermissionsHelper;
import com.file.manager.ui.Adapters.DirectoryPathAdapter;
import com.file.manager.ui.Adapters.StorageAdapter;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.DirectoryManager;
import com.file.manager.ui.Models.Folder;
import com.file.manager.ui.storage.FilterType;
import com.file.manager.ui.utils.CCBuffer;
import com.file.manager.ui.utils.CopyHelper;
import com.file.manager.ui.utils.CopyUtility;
import com.file.manager.ui.utils.CutHelper;
import com.file.manager.ui.utils.CutUtility;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.FileHandleUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderPickerDialog extends Dialog implements View.OnClickListener {

   private DirectoryManager directoryManager;
   private RecyclerView fileListRecycleView;
   private RecyclerView pathListRecycleView;
   private StorageAdapter storageAdapter;
   private DirectoryPathAdapter directoryPathAdapter;
   private ArrayAdapter<CharSequence>mountAdapter;
   private View prevFolder;
   private Context context;
   private ProgressBar loadingProgress;
   private OnDirPickedListener onDirPickedListener;
   public final int FLAG1=0;
   public final int FLAG2=1;
   public final int FLAG3=2;
   private int flag=-1;
   private String dir="";
    public FolderPickerDialog(Context context,int flag){
        super(context);
        this.directoryManager= new DirectoryManager(FilterType.FOLDERS);
        this.context=context;
        this.flag=flag;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.folder_picker_layout);
        final Button Paste=findViewById(R.id.PASTE);
        final Button Cancel_Paste=findViewById(R.id.CANCEL_PASTE);
        final Button pick_dir=findViewById(R.id.pick_dir);
        final Button cancel_picker=findViewById(R.id.cancel_picker);
        final TextView ItemCount=findViewById(R.id.ITEMS);
        final Spinner filter=findViewById(R.id.filter);
        loadingProgress=findViewById(R.id.progress);
        final LinearLayoutManager manager= new LinearLayoutManager(context);
        manager.setOrientation(RecyclerView.VERTICAL);
        final LinearLayoutManager manager2=new LinearLayoutManager(context);
        manager2.setOrientation(RecyclerView.HORIZONTAL);
         prevFolder=findViewById(R.id.back);
        fileListRecycleView =findViewById(R.id.fileList);
        pathListRecycleView=findViewById(R.id.pathList);
        fileListRecycleView.setLayoutManager(manager);
        fileListRecycleView.setItemAnimator(null);
        pathListRecycleView.setLayoutManager(manager2);
        directoryPathAdapter= new DirectoryPathAdapter(context);
        pathListRecycleView.setAdapter(directoryPathAdapter);

        if(flag==FLAG1|flag==FLAG2){
            findViewById(R.id.BottomNav1).setVisibility(View.VISIBLE);
        }else {
            findViewById(R.id.BottomNav2).setVisibility(View.VISIBLE);
        }
        directoryPathAdapter.setItemListener(new DirectoryPathAdapter.ItemListener() {
            @Override
            public void onItemClick(int position) {

            }
        });
        directoryManager.createDir(context,new CustomFile(DiskUtils.getInstance().getDirectory(0).getPath()));
        storageAdapter = new StorageAdapter(directoryManager.currentDir(),context);
        CCBuffer.getInstance().setDestination(directoryManager.currentDir().getFile());
        storageAdapter.setItemListener(new StorageAdapter.ItemListener() {
            @Override
            public void onItemClick(int position) {
                final CustomFile file=directoryManager.currentDir().get(position);
                final Folder parentFolder=directoryManager.currentDir();
                openFolder(file,parentFolder);
                CCBuffer.getInstance().setDestination(directoryManager.currentDir().getFile());
                togglePrevViewVisibility();
            }

            @Override
            public void onItemLongClick(int position) {

            }
        });

        prevFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final CustomFile parent=new CustomFile(directoryManager.currentDir().getFile().getParent());
                if(DiskUtils.getInstance().isStartDirectory(directoryManager.currentDir().getPath()))
                    return;
                if(directoryManager.size()>1){
                    final Folder folder=directoryManager.moveTo(context,parent);
                    if(folder==null)
                        return;
                    directoryManager.currentDir().removeDeleted();
                    storageAdapter.setFolder(folder);
                    storageAdapter.notifyDataSetChanged();
                    fileListRecycleView.scrollToPosition(directoryManager.currentDir().getAdapterPosition());
                    setCurrentDirectoryPath(directoryManager.currentDir());
                }
                if(DiskUtils.getInstance().isStartDirectory(parent.getPath()))
                    prevFolder.setVisibility(View.GONE);
            }
        });


        fileListRecycleView.setAdapter(storageAdapter);
        initializeFolders(directoryManager.currentDir());
        Paste.setOnClickListener(this);
        Cancel_Paste.setOnClickListener(this);


        ItemCount.setText(CopyHelper.getInstance().getData().size()+":Items");

        pick_dir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dir=directoryManager.currentDir().getPath();
                dismiss();
                onDirPickedListener.picked(dir);
            }
        });

        cancel_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dir="";
                dismiss();
                onDirPickedListener.cancelled(dir);
            }
        });

        initAvailableStorage(filter);
    }

    private void initAvailableStorage(Spinner spinner){
        final List<CharSequence> array= new ArrayList<>();
        final File[]mounts=DiskUtils.getInstance().getStorageDirs();
        final Map<String,File>map= new HashMap<>();
         map.put("Internal",mounts[0]);
         array.add("Internal");
         if(mounts.length>1) {
             map.put("External", mounts[1]);
             array.add("External");
         }
        mountAdapter= new ArrayAdapter<>(getContext(),R.layout.spinner_item_benchmark,array);
        mountAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item_benchmark);
        spinner.setAdapter(mountAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final Folder parentFolder=directoryManager.currentDir();
                CustomFile mount=new CustomFile(map.get(array.get(position).toString()).getPath());
                openFolder(mount,parentFolder);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initializeFolders(final Folder folder){
        loadingProgress.setVisibility(View.VISIBLE);
        folder.init( new FileHandleUtil.OnTaskComplete() {
            @Override
            public void onComplete() {
                storageAdapter.notifyDataSetChanged();
                storageAdapter.initFolderSize();
                loadingProgress.setVisibility(View.INVISIBLE);
                setCurrentDirectoryPath(folder);
            }});
    }

    private void setCurrentDirectoryPath(final Folder folder){

            directoryPathAdapter.setSegments(folder.getPath());
            pathListRecycleView.scrollToPosition(directoryPathAdapter.getItemCount() - 1);
            directoryPathAdapter.notifyDataSetChanged();

    }

    public void setOnDirPickedListener(OnDirPickedListener onDirPickedListener) {
        this.onDirPickedListener = onDirPickedListener;
    }

    @Override
    public void onClick(View v) {
       if(flag==FLAG1&v.getId()==R.id.PASTE){
               CopyHelper.getInstance().setDestination(directoryManager.currentDir().getFile());
               final CopyFileDialog copyFileDialog = new CopyFileDialog(getContext());
               copyFileDialog.setCompleteListener(new CopyUtility.OnCopyListener() {
                   @Override
                   public void onSuccess(ArrayList<CustomFile> files) {
                       Toast.makeText(context,"finished!",Toast.LENGTH_SHORT).show();
                       CopyHelper.getInstance().reset();
                   }

                   @Override
                   public void onFailed(ArrayList<CustomFile> files) {
                       Toast.makeText(context,"Failed!",Toast.LENGTH_SHORT).show();
                       CopyHelper.getInstance().reset();
                   }
               });
               copyFileDialog.show();

       }else if(flag==FLAG2&v.getId()==R.id.PASTE){
            CutHelper.getInstance().setDestination(directoryManager.currentDir().getFile());
           final MoveFileDialog moveFileDialog= new MoveFileDialog(context);
           moveFileDialog.setOnCutListener(new CutUtility.OnCutListener() {
               @Override
               public void onSuccess(ArrayList<CustomFile> files) {
                   Toast.makeText(getContext(),"Complete!",Toast.LENGTH_SHORT).show();
                   CutHelper.getInstance().reset();
               }
           });
           moveFileDialog.show();
       }
       dismiss();
       if(onDirPickedListener!=null)
           onDirPickedListener.picked(getPath());
    }


    public String getPath(){
        return dir;
    }

    private void togglePrevViewVisibility(){
        if(directoryPathAdapter.getItemCount()>=1)
            prevFolder.setVisibility(View.VISIBLE);
        else
            prevFolder.setVisibility(View.GONE);
    }
    private void openFolder(CustomFile file,final Folder parentFolder){
        if (directoryManager.contains(file)) {
            final Folder folder=directoryManager.moveTo(getContext(),file);
            folder.removeDeleted();
            storageAdapter.setFolder(folder);
            storageAdapter.notifyDataSetChanged();
            fileListRecycleView.scrollToPosition(folder.getAdapterPosition());
            setCurrentDirectoryPath(folder);
        } else {
            String storage=DiskUtils.getInstance().getStartDirectory(file);
            if(file.isAndroidDirectory()&!PermissionsHelper.getInstance().uriValid(new File(storage),getContext())) {
                getStoragePermission(new CustomFile(storage));
               //  prevFolder.setVisibility(View.GONE);
                return;
            }
            directoryManager.createDir(getContext(), file);
            storageAdapter.setFolder(directoryManager.currentDir());
            directoryManager.currentDir().setParent(parentFolder);
            storageAdapter.notifyDataSetChanged();
            initializeFolders(directoryManager.currentDir());

        }
        fileListRecycleView.getLayoutManager().scrollToPosition(0);
    }



    private void getStoragePermission(CustomFile file){
        String storage=DiskUtils.getInstance().getStartDirectory(file);
        if (!PermissionsHelper.getInstance().uriValid(new File(storage),getContext())) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            ((MainActivity) context).startActivityForResult(intent, 32);
        }

    }
    public void setFlag(int flag) {
        this.flag = flag;
    }


    public interface OnDirPickedListener{
        void picked(String path);
        void cancelled(String path);
    }
}
