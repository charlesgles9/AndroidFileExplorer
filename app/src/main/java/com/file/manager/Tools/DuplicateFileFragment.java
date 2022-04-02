package com.file.manager.Tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.IOnBackPressed;
import com.file.manager.Activities.MainActivity;
import com.file.manager.OnTaskCompleteListener;
import com.file.manager.R;
import com.file.manager.ui.Adapters.DuplicateFileContainerAdapter;
import com.file.manager.ui.Dialogs.ConfirmDeleteDialog;
import com.file.manager.ui.Dialogs.LoadingDialog;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.DuplicateFileContainer;
import com.file.manager.ui.Models.DuplicateFileModel;
import com.file.manager.ui.utils.DeleteFilesUtility;
import com.file.manager.ui.utils.DiskUtils;

import java.util.ArrayList;

public class DuplicateFileFragment extends Fragment implements IOnBackPressed {

    private DuplicateFileContainerAdapter adapter;
    private DuplicateFileContainer duplicateFileContainer;
    private Toolbar activityToolbar;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private TextView message;
    private View root;
    private boolean deleted=false;
    private Fragment parent;
    private Toolbar toolbar;
    public DuplicateFileFragment(Fragment parent){
        this.parent=parent;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

         root=inflater.inflate(R.layout.duplicate_fragment,container,false);
        final Button refresh=root.findViewById(R.id.refresh);
        final Button delete=root.findViewById(R.id.delete);
        final Button selectAll=root.findViewById(R.id.selectAll);
        toolbar=root.findViewById(R.id.toolbar);
         message=root.findViewById(R.id.message);
        recyclerView=root.findViewById(R.id.fileList);
         progressBar=root.findViewById(R.id.progress);
        recyclerView.setItemAnimator(null);
        manager= new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(manager);
        final MainActivity activity=(MainActivity)(getContext());
        activityToolbar=activity.toolbar;
        activityToolbar.setVisibility(View.GONE);
        toolbar.setSubtitle("Duplicates");
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState==RecyclerView.SCROLL_STATE_IDLE){
                    int start=manager.findFirstVisibleItemPosition();
                    int stop=manager.findLastVisibleItemPosition();
                    adapter.LoadThumbnail(start,stop);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if(!isLoading())
                loadDuplicates();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isLoading())
                DeleteFilesDialog();
            }
        });

        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isLoading())
                selectAllPopUp(v);
            }
        });
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
                    filterPopUp(toolbar);
                }
                return false;
            }
        });
        duplicateFileContainer = new DuplicateFileContainer(new CustomFile(DiskUtils.getInstance().getDirectory(0).getPath()),getContext());
        return root;
    }


    private boolean isLoading(){
        return progressBar.getVisibility()==View.VISIBLE;
    }

    private void loadDuplicates(){
        if(duplicateFileContainer!=null) {
            progressBar.setVisibility(View.VISIBLE);
            duplicateFileContainer.cancel();
            duplicateFileContainer.clear();
            selectedItemsCount();
            if(adapter!=null)
            adapter.notifyDataSetChanged();
            message.setVisibility(View.VISIBLE);
            duplicateFileContainer.searchForDuplicates(new OnTaskCompleteListener() {
                @Override
                public void onTaskComplete() {
                    recyclerView.setAdapter(adapter);
                    progressBar.setVisibility(View.GONE);
                    selectedItemsCount();
                    if(duplicateFileContainer.size()==0){
                        message.setText("No duplicates files found!");
                        return;
                    }
                    checkIfContentDisplaysOnScreen();
                    selectedItemsCount();
                    message.setVisibility(View.INVISIBLE);
                }
            });

        }else {
            progressBar.setVisibility(View.VISIBLE);
            message.setVisibility(View.VISIBLE);
            duplicateFileContainer = new DuplicateFileContainer(new CustomFile(DiskUtils.getInstance().getDirectory(0).getPath()),getContext());
            duplicateFileContainer.searchForDuplicates(new OnTaskCompleteListener() {
                @Override
                public void onTaskComplete() {
                    recyclerView.setAdapter(adapter);
                    progressBar.setVisibility(View.GONE);
                    if(duplicateFileContainer.size()==0){
                        message.setText("No duplicates files found!");
                        return;
                    }
                    message.setVisibility(View.GONE);
                    checkIfContentDisplaysOnScreen();
                    selectedItemsCount();
                    // Toast.makeText(getContext(),""+adapter.getItemCount(),Toast.LENGTH_SHORT).show();
                }
            });
        }

       if(!duplicateFileContainer.getMessage().hasActiveObservers()) {
           ((LiveData<String>) duplicateFileContainer.getMessage()).observe(getViewLifecycleOwner(), new Observer<String>() {
               @Override
               public void onChanged(String s) {
                   message.setText(s);
               }
           });
       }
       if(adapter==null) {
           adapter = new DuplicateFileContainerAdapter(getContext(), duplicateFileContainer);
           adapter.setOnItemClickListener(new DuplicateFileContainerAdapter.OnItemClickListener() {
               @Override
               public void onClick(View view, int position) {
                   selectedItemsCount();
               }
           });
       }
    }

    private void DeleteFilesDialog(){
        final LoadingDialog loadingDialog= new LoadingDialog(getContext());
        @SuppressLint("StaticFieldLeak") final AsyncTask<String,Integer,String> task= new AsyncTask<String, Integer, String>() {
            final ArrayList<CustomFile>files= new ArrayList<>();
            @Override
            protected String doInBackground(String... strings) {
                for(DuplicateFileModel model:duplicateFileContainer.getArray()){
                    if(isCancelled())
                        break;
                    for(CustomFile file:model.getArray()){
                        if(isCancelled())
                            break;
                        if(file.IsSelected()){
                            files.add(file);
                            model.addDeleted(file);
                        }
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loadingDialog.dismiss();
                DeleteFiles(files);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
        };
        LoadingDialog.DismissListener dismissListener= new LoadingDialog.DismissListener() {
            @Override
            public void onDismiss() {
               task.cancel(true);
            }
        };

        loadingDialog.setDismissListener(dismissListener);
        loadingDialog.show();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    private void DeleteFiles(ArrayList<CustomFile>files){
        if(files.isEmpty()){
            Toast.makeText(getContext(),"No file selected!",Toast.LENGTH_SHORT).show();
            return;
        }
        final ConfirmDeleteDialog deleteDialog= new ConfirmDeleteDialog(getContext(),files);
        deleteDialog.setOnCompleteListener(new DeleteFilesUtility.OnDeleteCompleteListener() {
            @Override
            public void onSuccess(ArrayList<CustomFile> data) {
                for(int i=0;i<duplicateFileContainer.size();i++){
                    DuplicateFileModel model=duplicateFileContainer.getArray().get(i);
                     model.removeDeleted();
                    if(model.getArray().size()<=1){
                        duplicateFileContainer.getArray().remove(i);
                    }
                }
                adapter.notifyDataSetChanged();
                selectedItemsCount();
                if(duplicateFileContainer.size()<=0){
                    message.setText("No duplicates Available");
                    message.setVisibility(View.VISIBLE);
                }

            }});
        deleteDialog.show();
    }

    private void selectAllItemsNewest(){
        int count=0;
        int selected=0;
        for(DuplicateFileModel model:duplicateFileContainer.getArray()){
            model.selectNewest();
            for(CustomFile file:model.getArray()){
                if(file.IsSelected())
                    selected+=1;
            }
            count+=model.getArray().size();
        }
        toolbar.getMenu().getItem(0).setTitle(
                selected+"/"+count);
    }

    private void selectedItemsCount(){
        int selected=0;
        int count=0;
        for(DuplicateFileModel model:duplicateFileContainer.getArray()){
            for(CustomFile file:model.getArray()){
                if(file.IsSelected())
                selected++;
            }
            count+=model.getArray().size();
        }
        toolbar.getMenu().getItem(0).setTitle(
                selected+"/"+count);
    }

    private void selectAllItemsOldest(){
        int count=0;
        int selected=0;
        for(DuplicateFileModel model:duplicateFileContainer.getArray()){
            model.selectOldest();
            for(CustomFile file:model.getArray()){
                if(file.IsSelected())
                    selected+=1;
            }
            count+=model.getArray().size();
        }
        toolbar.getMenu().getItem(0).setTitle(
                selected+"/"+count);
    }

    private void resetSelectAllItems(){
        int count=0;
        for(DuplicateFileModel model:duplicateFileContainer.getArray()){
          for(CustomFile file:model.getArray()){
              file.setSelected(false);
          }
        }
        toolbar.getMenu().getItem(0).setTitle(
                0+"/"+count);
    }

    private void selectAllPopUp(View anchor){
        final LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view=inflater.inflate(R.layout.duplicate_select_all_dialog,null);
        final PopupWindow popupWindow= new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,true);
        popupWindow.showAtLocation(anchor,Gravity.BOTTOM|Gravity.RIGHT,0, (int)(80*1.5f));
        final RadioButton option1=view.findViewById(R.id.option1);
        RadioButton option2=view.findViewById(R.id.option2);
        RadioButton option3=view.findViewById(R.id.option3);
        Button okay=view.findViewById(R.id.okay);
        Button cancel=view.findViewById(R.id.cancel);

        final View.OnClickListener onClickListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.option1:
                        selectAllItemsOldest();
                        break;

                    case R.id.option2:
                        selectAllItemsNewest();
                        break;
                    case R.id.okay:
                        popupWindow.dismiss();
                        adapter.notifyDataSetChanged();
                        break;
                    case R.id.option3:
                        resetSelectAllItems();
                        break;
                    case R.id.cancel:
                        popupWindow.dismiss();
                        break;
                }
                selectedItemsCount();
            }
        };
        popupWindow.setOutsideTouchable(false);
        option1.setOnClickListener(onClickListener);
        option2.setOnClickListener(onClickListener);
        option3.setOnClickListener(onClickListener);
        okay.setOnClickListener(onClickListener);
        cancel.setOnClickListener(onClickListener);
    }


    private void filterOptions(View v){
        final DiskUtils diskUtils=DiskUtils.getInstance();
        switch (v.getId()){
            case R.id.option1:
                duplicateFileContainer.setMode(DuplicateFileContainer.IMAGE);
                break;
            case R.id.option2:
                duplicateFileContainer.setMode(DuplicateFileContainer.VIDEO);
                break;
            case R.id.option3:
                duplicateFileContainer.setMode(DuplicateFileContainer.ALL_MEDIA);
                break;
            case R.id.option4:
                duplicateFileContainer.setMode(DuplicateFileContainer.DOCUMENT);
                break;
            case R.id.option5:
                duplicateFileContainer.setMode(DuplicateFileContainer.COMPRESSED);
                break;
            case R.id.option8:
                duplicateFileContainer.setMode(DuplicateFileContainer.AUDIO);
                break;
            case R.id.option6:
                duplicateFileContainer.setDir(new CustomFile(diskUtils.getDirectoryPath(0)));
                break;
            case R.id.option7:
                if(diskUtils.getDirectoryPath(1)!=null)
                    duplicateFileContainer.setDir(new CustomFile(diskUtils.getDirectoryPath(1)));
                break;
        }
        loadDuplicates();
    }

    private void filterPopUp(View anchor){
        if(isLoading())
            return;
        final DiskUtils diskUtils=DiskUtils.getInstance();
        final LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view=inflater.inflate(R.layout.duplicate_file_settings,null);
        final PopupWindow popupWindow= new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,true);
        popupWindow.showAsDropDown(anchor,0,0, Gravity.RIGHT);
        final RadioButton option1=view.findViewById(R.id.option1);
        final RadioButton option2=view.findViewById(R.id.option2);
        final RadioButton option3=view.findViewById(R.id.option3);
        final RadioButton option4=view.findViewById(R.id.option4);
        final RadioButton option5=view.findViewById(R.id.option5);
        final RadioButton option6=view.findViewById(R.id.option6);
        final RadioButton option7=view.findViewById(R.id.option7);
        final RadioButton option8=view.findViewById(R.id.option8);
        Button search=view.findViewById(R.id.search);
        option7.setVisibility(diskUtils.getDirectory(1)==null?View.GONE:View.VISIBLE);
        if(duplicateFileContainer.getDir().getPath().equals(diskUtils.getDirectoryPath(0))){
            option6.setChecked(true);
            option7.setChecked(false);
        }else {
            option7.setChecked(true);
            option6.setChecked(false);
        }

        final RadioButton []radioButtons={option1,option2,option3,option8,option4,option5};
        final View.OnClickListener onClickListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             switch (v.getId()){
                 case R.id.option1:
                     duplicateFileContainer.setMode((byte) 0);
                     break;
                 case R.id.option2:
                     duplicateFileContainer.setMode((byte) 1);
                     break;
                 case R.id.option3:
                     duplicateFileContainer.setMode((byte) 3);
                     break;
                 case R.id.option4:
                     duplicateFileContainer.setMode((byte) 4);
                     break;
                 case R.id.option5:
                     duplicateFileContainer.setMode((byte) 5);
                     break;
                 case R.id.option8:
                     duplicateFileContainer.setMode((byte) 2);
                     break;
             }
            }
        };
        switch (duplicateFileContainer.getMode()) {
            case 0:
                option1.setChecked(true);
                break;
            case 1:
                option2.setChecked(true);
                break;
            case 2:
                option8.setChecked(true);
                break;
            case 3:
                option3.setChecked(true);
                break;
            case 4:
                option4.setChecked(true);
                break;
            case 5:
                option5.setChecked(true);
                break;
        }
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(option1.isChecked()){
                   filterOptions(option1);
                } else if(option2.isChecked()){
                   filterOptions(option2);
                } else if(option3.isChecked()){
                   filterOptions(option3);
                }else if(option4.isChecked()){
                   filterOptions(option4);
                }else if(option5.isChecked()){
                   filterOptions(option5);
                }else if(option6.isChecked()) {
                   filterOptions(option6);
                }else if(option8.isChecked()){
                   filterOptions(option8);
                }else {
                  filterOptions(option7);
                }
                popupWindow.dismiss();
            }
        });
        for(RadioButton radioButton:radioButtons){
            radioButton.setOnClickListener(onClickListener);
        }
    }

    private void checkIfContentDisplaysOnScreen(){
        if(adapter==null)
            return;
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int start=manager.findFirstVisibleItemPosition();
                int stop=manager.findLastVisibleItemPosition();
                adapter.LoadThumbnail(start,stop);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(duplicateFileContainer!=null)
            duplicateFileContainer.cancel();
        if(parent!=null)
            ((MainActivity)getContext()).setFragment(parent);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        activityToolbar.setVisibility(View.VISIBLE);
    }

}
