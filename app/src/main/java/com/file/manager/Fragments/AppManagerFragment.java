package com.file.manager.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.IOnBackPressed;
import com.file.manager.Activities.MainActivity;
import com.file.manager.OnTaskCompleteListener;
import com.file.manager.R;
import com.file.manager.ui.Adapters.AppManagerAdapter;
import com.file.manager.ui.Dialogs.FolderPickerDialog;
import com.file.manager.ui.Models.AppManagerModel;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.utils.CopyHelper;

public class AppManagerFragment extends Fragment implements IOnBackPressed {

    private AppManagerAdapter adapter;
    private Toolbar activityToolbar;
    private RecyclerView recyclerView;
    private Fragment parent;
    private Toolbar toolbar;
    private View loadingView;
    private TextView loadingPercentage;
    private TextView loadingMessage;
    private ProgressBar loadingProgress;
    private MainActivity activity;
    public AppManagerFragment(Fragment parent){
        this.parent=parent;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.installed_apps_fragment, container, false);
        recyclerView= root.findViewById(R.id.fileList);
        activity=(MainActivity)(getContext());
        loadingView=root.findViewById(R.id.loadingView);
        loadingPercentage=root.findViewById(R.id.loadingPercentage);
        loadingMessage=root.findViewById(R.id.loadingMessage);
        loadingProgress=root.findViewById(R.id.loadingProgress);
        activityToolbar=activity.toolbar;
        activityToolbar.setVisibility(View.GONE);
        toolbar= root.findViewById(R.id.toolbar);
        LinearLayoutManager manager= new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(manager);

        if(adapter==null) {
            adapter = new AppManagerAdapter(getContext(), new OnTaskCompleteListener() {
                @Override
                public void onTaskComplete() {
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    loadingView.setVisibility(View.INVISIBLE);
                    fileCount();
                }
            });
        }else {
            recyclerView.setAdapter(adapter);
            loadingView.setVisibility(View.INVISIBLE);
            fileCount();
        }
        adapter.setOnItemClickListener(new AppManagerAdapter.OnItemClickListener() {
            @Override
            public void open(View view,int position) {
                appOptions(view,adapter.get(position));
            }

            @Override
            public void options(View view,int position) {
                appOptions(view,adapter.get(position));
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
                   filterWindow(toolbar);
                }
                return false;
            }
        });

        adapter.getMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                loadingPercentage.setText(adapter.getProgress()+"%");
                loadingProgress.setProgress(adapter.getProgress());
                loadingMessage.setText(s);
                loadingProgress.setIndeterminate(!adapter.isDeterminate());
            }
        });
        return root;
    }

    public void appOptions(View anchor, final AppManagerModel model){
        final LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view=inflater.inflate(R.layout.installed_apps_operations,null);
        view.measure(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        final PopupWindow popupWindow= new PopupWindow(view, view.getMeasuredWidth(),view.getMeasuredHeight(),true);
        popupWindow.showAsDropDown(anchor);
        RadioButton open=view.findViewById(R.id.open);
        RadioButton backup=view.findViewById(R.id.backup);
        RadioButton uninstall=view.findViewById(R.id.uninstall);
        RadioButton properties=view.findViewById(R.id.properties);
        final View.OnClickListener onClickListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.open:
                        openApp(model);
                        popupWindow.dismiss();
                        break;
                    case R.id.backup:
                        backUpApp(model.getFile().getPath());
                        popupWindow.dismiss();
                        break;
                    case R.id.uninstall:
                       Intent uIntent=new Intent(Intent.ACTION_DELETE);
                       uIntent.setData(Uri.parse("package:"+model.getInfo().packageName));
                       uIntent.putExtra(Intent.EXTRA_RETURN_RESULT,true);
                       startActivityForResult(uIntent,0x86);
                       popupWindow.dismiss();
                        break;
                    case R.id.properties:
                        appSettings(model);
                        popupWindow.dismiss();
                        break;

                }
            }
        };
        open.setOnClickListener(onClickListener);
        backup.setOnClickListener(onClickListener);
        uninstall.setOnClickListener(onClickListener);
        properties.setOnClickListener(onClickListener);
    }

    public void filterWindow(View anchor){
        final LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view=inflater.inflate(R.layout.installed_app_filter,null);
        final PopupWindow popupWindow= new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,true);
        popupWindow.showAsDropDown(anchor,0,0,Gravity.RIGHT);
        RadioButton option1=view.findViewById(R.id.option1);
        RadioButton option2=view.findViewById(R.id.option2);
        RadioButton option3=view.findViewById(R.id.option3);
        RadioButton AZ=view.findViewById(R.id.AZ);
        RadioButton ZA=view.findViewById(R.id.ZA);
        RadioButton size=view.findViewById(R.id.size);
        RadioButton lastModified=view.findViewById(R.id.lastModified);
        if(adapter.getMode()== AppManagerAdapter.USER_APPS){
            option1.setChecked(true);
        }else if(adapter.getMode()== AppManagerAdapter.SYSTEM_APPS){
            option2.setChecked(true);
        }else {
            option3.setChecked(true);
        }

        if(adapter.getSortMode()== AppManagerAdapter.SORT_AZ){
            AZ.setChecked(true);
        }else if(adapter.getSortMode()== AppManagerAdapter.SORT_ZA){
            ZA.setChecked(true);
        }else if(adapter.getSortMode()== AppManagerAdapter.SORT_BY_SIZE){
            size.setChecked(true);
        }else {
            lastModified.setChecked(true);
        }

        final View.OnClickListener onClickListener1= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.option1:
                        adapter.setMode(AppManagerAdapter.USER_APPS);
                        break;
                    case R.id.option2:
                        adapter.setMode(AppManagerAdapter.SYSTEM_APPS);
                        break;
                    case R.id.option3:
                        adapter.setMode(AppManagerAdapter.ALL_APPS);
                        break;

                }
                adapter.reset();
                adapter.notifyDataSetChanged();
                loadingView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.INVISIBLE);
                popupWindow.dismiss();
            }
        };

        final View.OnClickListener onClickListener2= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.AZ:
                        adapter.setSortBy(AppManagerAdapter.SORT_AZ);
                        break;
                    case R.id.ZA:
                        adapter.setSortBy(AppManagerAdapter.SORT_ZA);
                        break;
                    case R.id.size:
                        adapter.setSortBy(AppManagerAdapter.SORT_BY_SIZE);
                        break;
                    case R.id.lastModified:
                        adapter.setSortBy(AppManagerAdapter.SORT_LAST_MODIFIED);
                        break;
                }
                adapter.sortList();
                adapter.notifyDataSetChanged();
                popupWindow.dismiss();
            }
        };
       option1.setOnClickListener(onClickListener1);
       option2.setOnClickListener(onClickListener1);
       option3.setOnClickListener(onClickListener1);
       AZ.setOnClickListener(onClickListener2);
       ZA.setOnClickListener(onClickListener2);
       size.setOnClickListener(onClickListener2);
       lastModified.setOnClickListener(onClickListener2);

    }

    private void backUpApp(String path){
        CopyHelper.getInstance().reset();
        CopyHelper.getInstance().add(new CustomFile(path));
        FolderPickerDialog dialog= new FolderPickerDialog(getContext(),0);
        dialog.show();
    }

    private void openApp(AppManagerModel model){
        try {
            Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(model.getInfo().packageName);
            getContext().startActivity(intent);
        }catch (NullPointerException ne){
            appSettings(model);
        }

    }

    private void appSettings(AppManagerModel model){
        Intent intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:"+model.getInfo().packageName));
        startActivityForResult(intent,0x86);
    }


    private void fileCount(){
        toolbar.getMenu().getItem(0).setTitle(adapter.getItemCount()+" apps");
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         adapter.verify();
         fileCount();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activityToolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if(parent!=null)
            activity.setFragment(parent);
    }
}
