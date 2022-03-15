package com.file.manager.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.file.manager.GlobalFileOperations;
import com.file.manager.IOnBackPressed;
import com.file.manager.MainActivity;
import com.file.manager.OnTaskCompleteListener;
import com.file.manager.Operations;
import com.file.manager.R;
import com.file.manager.helpers.MIMETypesHelper;
import com.file.manager.ui.Adapters.GlobalSearchAdapter;
import com.file.manager.ui.BatchFileRenameDialog;
import com.file.manager.ui.ConfirmDeleteDialog;
import com.file.manager.ui.FolderPickerDialog;
import com.file.manager.ui.Models.CustomFile;

import com.file.manager.ui.OpenAsDialog;
import com.file.manager.ui.PropertiesDialog;
import com.file.manager.ui.SingleFileRenameDialog;
import com.file.manager.ui.storage.FilterType;
import com.file.manager.ui.storage.StorageFragment;
import com.file.manager.ui.utils.CopyHelper;
import com.file.manager.ui.utils.CutHelper;
import com.file.manager.ui.utils.DeleteFilesUtility;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.FileFilters;
import com.file.manager.ui.utils.SoftwareKeyboardListener;
import com.file.manager.ui.utils.ThumbnailLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GlobalSearchFragment extends Fragment implements SoftwareKeyboardListener.Listener, IOnBackPressed {

    private ArrayList<CustomFile> queries= new ArrayList<>();
    private SearchTask currentThread;
    private ProgressBar loading;
    private TextView results;
    private GlobalSearchAdapter adapter;
    private LinearLayoutManager manager;
    private String previousQuery="";
    private RadioGroup FILE_HANDLE_OPTIONS;
    private SearchView searchView;
    private Toolbar toolbar;
    private Toolbar mainToolbar;
    private MutableLiveData<Integer>update= new MutableLiveData<>();
    private View root;
    private HomeFragment homeFragment;
    private GlobalFileOperations globalFileOperations;
    public GlobalSearchFragment(HomeFragment homeFragment, GlobalFileOperations globalFileOperations){
        this.homeFragment=homeFragment;
        this.globalFileOperations=globalFileOperations;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.activity_global_search, container, false);
        final RecyclerView recyclerView=root.findViewById(R.id.fileList);
        FILE_HANDLE_OPTIONS=root.findViewById(R.id.FILE_HANDLE_OPTIONS1);
        mainToolbar=((MainActivity)getContext()).toolbar;
        searchView=root.findViewById(R.id.search);
        toolbar=root.findViewById(R.id.toolbar);
        mainToolbar.setVisibility(View.GONE);
        searchView.setVisibility(View.VISIBLE);
        results=root.findViewById(R.id.results);
        SoftwareKeyboardListener softwareKeyboardListener=(SoftwareKeyboardListener)root.findViewById(R.id.main);
        softwareKeyboardListener.setListener(this);
        loading=root.findViewById(R.id.progress);
        manager= new LinearLayoutManager(getContext());
        manager.setOrientation(androidx.recyclerview.widget.RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(null);
        adapter= new GlobalSearchAdapter(getContext(),queries);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        final Fragment fragment=this;
        adapter.setItemListener(new GlobalSearchAdapter.ItemListener() {
            @Override
            public void onItemClick(int position) {
                final CustomFile file=queries.get(position);
                if(adapter.getOperations().equals(Operations.NAVIGATE)){
                    if(file.isFile()) {
                        new MIMETypesHelper(getContext(), file).startDefault();
                    }else {
                        mainToolbar.setSubtitle("Local");
                        MainActivity activity=((MainActivity)getContext());
                        activity.setSubtitle("Local");
                        StorageFragment storageFragment=new StorageFragment(file.getPath(),
                                mainToolbar.getSubtitle().toString(), FilterType.DEFAULT,fragment,globalFileOperations);
                        //prevents it from being saved in the window utility
                        storageFragment.setDeleted(true);
                        activity.setFragment(storageFragment);

                    }
                }else {
                    adapter.toggleSelect(position);
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onItemLongClick(int position) {
                adapter.toggleSelect(position);
                selectMode();
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter.getOperations().equals(Operations.SELECT)){
                    navigationMode();
                }else {
                   onBackPressed();
                }
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                adapter.toggleSelectAll();
                adapter.notifyDataSetChanged();
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.equals("")&!previousQuery.equals(query)) {
                    queries.clear();
                    loading.setVisibility(View.GONE);
                    results.setVisibility(View.GONE);
                    toolbar.getMenu().getItem(0).setVisible(false);

                    return false;
                }
                queries.clear();
                search(searchView.getQuery().toString());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                if(text.isEmpty()){
                    results.setText("");
                    results.setVisibility(View.GONE);
                }
                return false;
            }
        });


        update.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer position) {
                if(position!=-1){
                    adapter.notifyItemInserted(position);
                }
                results.setText(String.valueOf("("+queries.size()+") Items Found"));
            }
        });

        adapter.getUpdates().observe(getViewLifecycleOwner(), new Observer<List<Integer>>() {
            @Override
            public void onChanged(List<Integer> position) {
                int start=position.get(0);
                int end=position.get(position.size()-1);
                adapter.notifyItemRangeChanged(start,end);
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState==RecyclerView.SCROLL_STATE_IDLE){
                    loadThumbnails();
                }
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        initStorageOptions(root);

        return root;
    }

    private void search(String query){
        // cancel the previous search to prevent memory leaks
        if(currentThread!=null){
            if(currentThread.isRunning()){
                currentThread.cancel(true);
            }
        }
        SearchTask searchTask= new SearchTask(query, new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete() {
                loading.setVisibility(View.INVISIBLE);
                loadThumbnails();
            }
        });
        currentThread=searchTask;
        searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        loading.setVisibility(View.VISIBLE);
        results.setVisibility(View.VISIBLE);
        previousQuery=query;
    }
    private void initStorageOptions(View root){
        final RadioButton COPY=root.findViewById(R.id.COPY);
        final RadioButton DELETE=root.findViewById(R.id.DELETE);
        final RadioButton CUT=root.findViewById(R.id.CUT);
        final RadioButton RENAME=root.findViewById(R.id.RENAME);
        final RadioButton MORE=root.findViewById(R.id.MORE);
        final RadioGroup FILE_HANDLE_OPTIONS=root.findViewById(R.id.FILE_HANDLE_OPTIONS1);
        final Context context=getContext();
        final View.OnClickListener listener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<CustomFile>array= new ArrayList<>();
                for(CustomFile file:adapter.getFiles()){
                    if(file.IsSelected()){
                        array.add(file);
                    }
                }
                 FolderPickerDialog folderPickerDialog;
                switch (v.getId()){
                    case R.id.COPY:
                        CopyHelper.getInstance().add(array);
                        folderPickerDialog= new FolderPickerDialog(context,0);
                        folderPickerDialog.setOnDirPickedListener(new FolderPickerDialog.OnDirPickedListener() {
                            @Override
                            public void picked(String path) {

                            }

                            @Override
                            public void cancelled(String path) {
                                 CopyHelper.getInstance().reset();
                            }
                        });
                        folderPickerDialog.show();
                        FILE_HANDLE_OPTIONS.setVisibility(View.GONE);
                        navigationMode();
                        break;
                    case R.id.CUT:
                        CutHelper.getInstance().add(array);
                        folderPickerDialog= new FolderPickerDialog(context,1);
                        folderPickerDialog.setOnDirPickedListener(new FolderPickerDialog.OnDirPickedListener() {
                            @Override
                            public void picked(String path) {
                                adapter.getFiles().removeAll(array);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void cancelled(String path) {
                                CutHelper.getInstance().reset();

                            }
                        });
                        folderPickerDialog.show();
                        FILE_HANDLE_OPTIONS.setVisibility(View.GONE);
                        navigationMode();
                        break;
                    case R.id.DELETE:
                        final ConfirmDeleteDialog confirmDeleteDialog= new ConfirmDeleteDialog(context,array);
                        confirmDeleteDialog.setOnCompleteListener(new DeleteFilesUtility.OnDeleteCompleteListener() {
                            @Override
                            public void onSuccess(ArrayList<CustomFile> data) {
                                adapter.notifyDataSetChanged();
                                results.setText(queries.size()+" items found");
                            }
                        });
                        confirmDeleteDialog.show();
                        navigationMode();
                        break;
                    case R.id.RENAME:
                        if(array.isEmpty())
                            return;
                        if(array.size()<=1){
                            SingleFileRenameDialog singleFileRenameDialog= new SingleFileRenameDialog(getContext(),array.get(0));
                            singleFileRenameDialog.setOnCompleteListener(new SingleFileRenameDialog.OnRenameComplete() {
                                @Override
                                public void onComplete(CustomFile nFile) {
                                    adapter.replace(array.get(0),nFile);
                                    adapter.notifyItemChanged(nFile.position);
                                }
                            });
                            singleFileRenameDialog.show();
                        }else {
                            BatchFileRenameDialog batchFileRenameDialog= new BatchFileRenameDialog(getContext(),array);
                            batchFileRenameDialog.setOnCompleteListener(new BatchFileRenameDialog.OnRenameComplete() {
                                @Override
                                public void update(CustomFile oFile, CustomFile nFile) {
                                    adapter.replace(oFile, nFile);
                                }

                                @Override
                                public void complete() {
                                   for(CustomFile file:array){
                                       adapter.notifyItemChanged(file.position);
                                   }
                                }
                            });
                            batchFileRenameDialog.show();
                            navigationMode();
                        }
                        break;
                    case R.id.MORE:
                        moreOptionsPopup(v,array);
                        break;

                }

            }
        };
        COPY.setOnClickListener(listener);
        CUT.setOnClickListener(listener);
        DELETE.setOnClickListener(listener);
        RENAME.setOnClickListener(listener);
        MORE.setOnClickListener(listener);
    }

    private void moreOptionsPopup(View anchor, final ArrayList<CustomFile>array){
        LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view=inflater.inflate(R.layout.more_layout,null);
        final PopupWindow popupWindow= new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,true);
        popupWindow.showAtLocation(anchor, Gravity.BOTTOM|Gravity.RIGHT,0,0);
        final RadioButton open=view.findViewById(R.id.open);
        final RadioButton openAs=view.findViewById(R.id.openAs);
        final RadioButton share=view.findViewById(R.id.share);
        final RadioButton compress=view.findViewById(R.id.compress);
        if(array.size()>1){
            openAs.setEnabled(false);
            share.setEnabled(false);
            open.setEnabled(false);
        }
        final View.OnClickListener onClickListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CustomFile file=array.get(0);
                switch (v.getId()){

                    case R.id.properties:
                        final PropertiesDialog propertiesDialog= new PropertiesDialog(getContext(),
                                array);
                        propertiesDialog.show();
                        break;
                    case R.id.open:
                        new MIMETypesHelper(getContext(),file).startDefault();
                        popupWindow.dismiss();
                        navigationMode();
                        break;
                    case R.id.share:
                        new MIMETypesHelper(getContext(),file).startShare();
                        popupWindow.dismiss();
                        navigationMode();
                        break;
                    case R.id.bookmark:
                        addFilesToBookMarks();
                        Toast.makeText(getContext(),"Added to bookmarks",Toast.LENGTH_LONG).show();
                        break;
                    case R.id.openAs:
                        OpenAsDialog openAsDialog= new OpenAsDialog(getContext(),file);
                        openAsDialog.show();
                        popupWindow.dismiss();
                        navigationMode();
                        break;
                    case R.id.compress:
                        navigationMode();
                        break;
                }
                popupWindow.dismiss();
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
        final ArrayList<CustomFile>array= new ArrayList<>();
        for(CustomFile file:adapter.getFiles()){
            if(file.IsSelected()){
                array.add(file);
            }
        }
        if(array.isEmpty())
            return;
        SharedPreferences preferences=getContext().getSharedPreferences("Bookmarks",Context.MODE_PRIVATE);
        if(preferences==null)
            return;
        SharedPreferences.Editor editor;
        for(CustomFile file:array){
            File book=file.isDirectory()?file:file.getParentFile();
            editor=preferences.edit();
            editor.putString(book.getPath(),book.getName());
            editor.apply();
        }

    }
    private void navigationMode(){
        FILE_HANDLE_OPTIONS.setVisibility(View.GONE);
        searchView.setVisibility(View.VISIBLE);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_light);
        toolbar.getMenu().getItem(0).setVisible(false);
        adapter.setOperations(Operations.NAVIGATE);
        adapter.notifyDataSetChanged();
        adapter.resetSelect();
    }

    private void selectMode(){
        FILE_HANDLE_OPTIONS.setVisibility(View.VISIBLE);
        searchView.setVisibility(View.GONE);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.getMenu().getItem(0).setVisible(true);
        adapter.setOperations(Operations.SELECT);
        adapter.notifyDataSetChanged();
    }

    private void loadThumbnails(){
        int start=manager.findFirstVisibleItemPosition();
        int stop=manager.findLastVisibleItemPosition();
        adapter.loadThumbnails(start, stop, new ThumbnailLoader.onThumbnailComplete() {
            @Override
            public void onComplete(List<Integer> positions) {
                for(int i=0;i<positions.size();i++){
                    adapter.notifyItemChanged(positions.get(i));
                }
            }
        });
    }

    class SearchTask extends AsyncTask<String,Integer,String>{
        private OnTaskCompleteListener onTaskCompleteListener;
        private String query;
        private boolean running=false;
        private boolean executed=false;
        public SearchTask(String query,OnTaskCompleteListener onTaskCompleteListener){
            this.onTaskCompleteListener=onTaskCompleteListener;
            this.query=query;
        }

        @Override
        protected String doInBackground(String... strings) {
            running=true;
            executed=true;
            search();
            return null;
        }

        public void search(){
            // search the internal storage & external storage
            File[]mounts=DiskUtils.getInstance().getStorageDirs();
            for(File mount:mounts){
                if(mount!=null)
                search(mount,new ArrayList<CustomFile>(),FileFilters.Default(false));
            }
        }

        public void search(File file, ArrayList<CustomFile>dirs, FilenameFilter filter){

            String[] array =file.list(filter);
            if(array!=null)
            for(String name:array){
                CustomFile child= new CustomFile(name,file);
                if(!isRunning())
                    return;
                if(child.getName().toLowerCase().contains(query.toLowerCase())) {
                    int position=queries.size();
                    queries.add(child);
                    update.postValue(position);
                    child.setTempThumbnail();
                }
                if(child.isDirectory())
                    dirs.add(child);
            }

            for(int i=0;i<dirs.size();i++){
                CustomFile folder=dirs.get(i);
                dirs.remove(i);
                if(!isRunning())
                    return;
                search(folder,dirs,filter);
            }

        }

        public boolean isRunning() {
            return running&!isCancelled();
        }

        public boolean isExecuted() {
            return executed;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            onTaskCompleteListener.onTaskComplete();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        searchView.setVisibility(View.GONE);
        mainToolbar.setVisibility(View.VISIBLE);
    }


    @Override
    public void onBackPressed() {
        ((MainActivity)getContext()).setFragment(homeFragment);
    }

    @Override
    public void onSoftKeyboardShown(boolean showing) {
        loadThumbnails();

    }

    interface OnItemInserted{
        void item(int position);
    }

}
