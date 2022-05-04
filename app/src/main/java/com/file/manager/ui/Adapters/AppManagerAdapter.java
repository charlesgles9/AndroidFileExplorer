package com.file.manager.ui.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.OnTaskCompleteListener;
import com.file.manager.R;
import com.file.manager.ui.Models.AppManagerModel;
import com.file.manager.utils.Sorter;

import java.util.ArrayList;
import java.util.List;

public class AppManagerAdapter extends RecyclerView.Adapter<AppManagerAdapter.AppManagerViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private List<AppManagerModel> apps;
    private OnTaskCompleteListener onTaskCompleteListener;
    private OnItemClickListener onItemClickListener;
    public static final int SYSTEM_APPS =0;
    public static final int USER_APPS =1;
    public static final int ALL_APPS=2;
    public static final int SORT_AZ=0;
    public static final int SORT_ZA=1;
    public static final int SORT_LAST_MODIFIED=2;
    public static final int SORT_BY_SIZE=3;
    private int progress;
    private boolean determinate;
    private int sortBy=0;
    private int mode=1;
    private MutableLiveData<String>message;
    public AppManagerAdapter(Context context, OnTaskCompleteListener onTaskCompleteListener){
        this.inflater=LayoutInflater.from(context);
        this.context=context;
        this.apps=new ArrayList<>();
        this.onTaskCompleteListener=onTaskCompleteListener;
        message= new MutableLiveData<>();
        load();
    }



    FetchInstalledApps fetchInstalledApps;
    private void  load(){
        if(fetchInstalledApps!=null) {
            if (!fetchInstalledApps.isRunning()) {
                fetchInstalledApps = new FetchInstalledApps();
                fetchInstalledApps.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        else {
            fetchInstalledApps= new FetchInstalledApps();
            fetchInstalledApps.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void reset(){
        apps.removeAll(apps);
        notifyItemMoved(0,apps.size());
        load();
    }

    public int getMode() {
        return mode;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener=onItemClickListener;
    }
    @NonNull
    @Override
    public AppManagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.installed_apps_layout,parent,false);
        return new AppManagerViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AppManagerViewHolder holder, int position) {
         final AppManagerModel model=apps.get(position);
         holder.thumbnail.setImageDrawable(model.getIcon());
         holder.name.setText(model.getName());
         holder.package_name.setText(model.getInfo().packageName);
         holder.package_size.setText(model.getSize());
         holder.date.setText(model.getDate());
         holder.version.setText("version: "+model.getInfo().versionName);
    }

    private  boolean isPackageInstalled(String packageName,PackageManager packageManager){
        try {
            packageManager.getPackageInfo(packageName,0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    //check if the user has removed a package and remove it from the list
    public void verify(){
        PackageManager packageManager=context.getPackageManager();
        for(int i=0;i<apps.size();i++){
            AppManagerModel model=apps.get(i);
            if(!isPackageInstalled(model.getInfo().packageName,packageManager)){
                apps.remove(i);
            }
        }
       notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return apps.size();
    }

    class AppManagerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView thumbnail;
        final TextView name;
        final TextView package_size;
        final TextView package_name;
        final TextView date;
        final ImageView options;
        final TextView version;
        public AppManagerViewHolder(View view){
            super(view);
            thumbnail=view.findViewById(R.id.thumbnail);
            name=view.findViewById(R.id.name);
            package_name=view.findViewById(R.id.package_name);
            package_size=view.findViewById(R.id.package_size);
            date=view.findViewById(R.id.date);
            options=view.findViewById(R.id.options);
            version=view.findViewById(R.id.version);
            view.setOnClickListener(this);
            options.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.getId()==R.id.options)
            onItemClickListener.options(v,getAdapterPosition());
            else onItemClickListener.open(v,getAdapterPosition());
        }
    }


    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getSortMode() {
        return sortBy;
    }

    public void setSortBy(int sortBy) {
        this.sortBy = sortBy;
    }

    public void  sortList(){
        switch (sortBy){
            case SORT_AZ:
                Sorter.sortAppsAZ(apps);
                break;
            case SORT_ZA:
                Sorter.sortAppsZA(apps);
                break;
            case SORT_BY_SIZE:
                Sorter.sortAppsBySize(apps);
                break;
            case SORT_LAST_MODIFIED:
                Sorter.sortAppsByDate(apps);
                break;
        }
    }

    public AppManagerModel get(int position){
        return apps.get(position);
    }
    @SuppressLint("StaticFieldLeak")
    class FetchInstalledApps extends AsyncTask<String,Integer,String>{

        private boolean running=false;
        @Override
        protected String doInBackground(String... strings) {
            message.postValue("Retrieving packages...");
            List<PackageInfo> packageInfos=context.getPackageManager().getInstalledPackages(0);
            int pSize=packageInfos.size();
            int pCount=1;
            determinate=true;
            for(PackageInfo info:packageInfos){
                pCount++;
                message.postValue("filtering files...");
                progress=(int)((((float)pCount)/(float)(pSize))*100);
                if((info.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)!=0&mode==USER_APPS)
                    continue;
                if((info.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==0&mode==SYSTEM_APPS)
                    continue;

                if(isCancelled())
                    break;
                apps.add(new AppManagerModel(info,context.getPackageManager()));

            }
            determinate=false;
            message.postValue("Sorting apps...");
            try {
                sortList();
            }catch (IllegalArgumentException ignore){}
            return null;
        }

        public boolean isRunning(){
            return running;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            onTaskCompleteListener.onTaskComplete();
            running=false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            running=true;
        }
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isDeterminate() {
        return determinate;
    }

    public interface OnItemClickListener{
        void open(View view,int position);
        void options(View view,int position);
    }

}
