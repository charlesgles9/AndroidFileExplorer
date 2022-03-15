package com.file.manager;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.ui.Adapters.GlobalSearchAdapter;
import com.file.manager.ui.FolderPickerDialog;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.FileFilters;
import com.file.manager.ui.utils.FileHandleUtil;
import com.file.manager.ui.utils.SoftwareKeyboardListener;
import com.file.manager.ui.utils.ThumbnailLoader;

import java.util.ArrayList;
import java.util.List;

public class GlobalSearchActivity extends AppCompatActivity implements SoftwareKeyboardListener.Listener {

    private MutableLiveData<Integer>LiveData=new MutableLiveData<>();
    private ArrayList<CustomFile>queries= new ArrayList<>();
    private ArrayList<CustomFile>folders= new ArrayList<>();
    private LoadFilesTask currentThread;
    private ProgressBar loading;
    private TextView results;
    private GlobalSearchAdapter adapter;
    private LinearLayoutManager manager;
    private String previousQuery="null";
    private RadioGroup FILE_HANDLE_OPTIONS;
    private SearchView searchView;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_search);
        searchView= findViewById(R.id.search);
        final RecyclerView recyclerView=findViewById(R.id.fileList);
        FILE_HANDLE_OPTIONS=findViewById(R.id.FILE_HANDLE_OPTIONS1);
        toolbar=findViewById(R.id.toolbar);
        results=findViewById(R.id.results);
        SoftwareKeyboardListener softwareKeyboardListener=(SoftwareKeyboardListener)findViewById(R.id.main);
        softwareKeyboardListener.setListener(this);
        loading=findViewById(R.id.progress);
        manager= new LinearLayoutManager(this);
        manager.setOrientation(androidx.recyclerview.widget.RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(null);
        adapter= new GlobalSearchAdapter(this,queries);
        recyclerView.setAdapter(adapter);

        adapter.setItemListener(new GlobalSearchAdapter.ItemListener() {
            @Override
            public void onItemClick(int position) {

                if(adapter.getOperations().equals(Operations.NAVIGATE)){

                }else {
                  adapter.toggleSelect(position);
                  adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onItemLongClick(int position) {
                selectMode();
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FILE_HANDLE_OPTIONS.getVisibility()==View.VISIBLE){
                   navigationMode();
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
                    LiveData.postValue(-1);
                    loading.setVisibility(View.GONE);
                    results.setVisibility(View.GONE);
                    toolbar.getMenu().getItem(0).setVisible(false);
                    return false;
                }
                LoadFilesTask task= new LoadFilesTask(query, new OnItemInserted() {
                    @Override
                    public void item(int position) {
                        LiveData.postValue(position);
                    }
                });

                if(currentThread!=null){
                    if(!currentThread.isCancelled()){
                        if(currentThread.isRunning()){
                            currentThread.cancel(true);
                        }
                    }
                }
                currentThread=task;
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                loading.setVisibility(View.VISIBLE);
                results.setVisibility(View.VISIBLE);
                previousQuery=query;
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

        final LoadFoldersTask loadFoldersTask= new LoadFoldersTask(new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete() {

            }
        });
        loadFoldersTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


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
        initStorageOptions();
    }

    private void initStorageOptions(){
        final RadioButton COPY=findViewById(R.id.COPY);
        final RadioButton DELETE=findViewById(R.id.DELETE);
        final RadioButton CUT=findViewById(R.id.CUT);
        final RadioButton RENAME=findViewById(R.id.RENAME);
        final RadioButton MORE=findViewById(R.id.MORE);
        final RadioGroup FILE_HANDLE_OPTIONS=findViewById(R.id.FILE_HANDLE_OPTIONS1);
        final Context context=this;
        final View.OnClickListener listener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.COPY:
                        FolderPickerDialog folderPickerDialog= new FolderPickerDialog(context,0);
                        folderPickerDialog.show();
                        FILE_HANDLE_OPTIONS.setVisibility(View.GONE);
                        break;
                    case R.id.CUT:
                        FILE_HANDLE_OPTIONS.setVisibility(View.GONE);
                        break;
                }
            }
        };
        COPY.setOnClickListener(listener);
        CUT.setOnClickListener(listener);
    }

    private void navigationMode(){
        FILE_HANDLE_OPTIONS.setVisibility(View.GONE);
        searchView.setVisibility(View.VISIBLE);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_light);
        toolbar.getMenu().getItem(0).setVisible(false);
        adapter.setOperations(Operations.NAVIGATE);
        adapter.notifyDataSetChanged();
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
    @SuppressLint("StaticFieldLeak")
    class LoadFoldersTask extends AsyncTask<String,Integer,String>{

        private OnTaskCompleteListener listener;
        public LoadFoldersTask(OnTaskCompleteListener listener){
           this.listener=listener;
        }
        @Override
        protected String doInBackground(String... strings) {
            FileHandleUtil.ListFilesRecursively(new CustomFile(DiskUtils.getInstance().getDirectory(0).getPath()),
                    new ArrayList<CustomFile>(),new ArrayList<CustomFile>(),folders, FileFilters.FoldersOnly());
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            listener.onTaskComplete();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

   @SuppressLint("StaticFieldLeak")
   class LoadFilesTask extends AsyncTask<String,Integer,String>{
        private String phrase;
        private OnItemInserted onItemInserted;
        private boolean running=true;
        public LoadFilesTask(String phrase,OnItemInserted onItemInserted){
            this.phrase=phrase;
            this.onItemInserted=onItemInserted;
        }
      @Override
      protected String doInBackground(String... strings) {
          queries.clear();
          LiveData.postValue(-1);
          try {
              for (int i=0;i<folders.size();i++) {
                  checkIsCancelled();
                  CustomFile file= folders.get(i);
                 // System.out.println(file.getPath());
                  if (file.getName().contains(phrase)) {
                      queries.add(file);
                  }

                  if (file.isDirectory()) {
                      String[] list = file.list(FileFilters.QueryFilesByName(phrase));
                      checkIsCancelled();
                      if (list != null) {
                          for (String name : list) {
                              checkIsCancelled();
                              CustomFile child=new CustomFile(name, file);
                              queries.add(child);
                         //    System.out.println(child.getPath());
                          }
                      }
                  }
              }
          }catch (InterruptedException ie){
             //  System.out.println("Interrupted...");
               running=false;
               queries.clear();
               LiveData.postValue(-1);
               return null;
          }
          for(int i=0;i<queries.size();i++){
              onItemInserted.item(i);
          }
          return null;
      }

      private void checkIsCancelled() throws InterruptedException {
            if(isCancelled())
                throw  new InterruptedException();
      }

      public boolean isRunning(){
            return running;
      }
      @Override
      protected void onPreExecute() {
          super.onPreExecute();
      }

      @Override
      protected void onPostExecute(String s) {
          super.onPostExecute(s);
          running=false;
          loading.setVisibility(View.GONE);
          results.setVisibility(View.VISIBLE);
          results.setText(queries.size()+" items found");
          loadThumbnails();
      }
  }


    @Override
    public void onSoftKeyboardShown(boolean showing) {
        loadThumbnails();

    }

    interface OnItemInserted{
        void item(int position);
  }


}