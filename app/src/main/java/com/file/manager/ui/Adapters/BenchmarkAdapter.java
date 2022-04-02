package com.file.manager.ui.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Models.CopyProgressMonitor;
import com.file.manager.ui.utils.CopyUtility;
import com.file.manager.ui.utils.DiskUtils;

import java.util.ArrayList;

public class BenchmarkAdapter extends RecyclerView.Adapter<BenchmarkAdapter.BenchmarkViewHolder> {

    private LayoutInflater inflater;
    private ArrayList<CopyUtility> array;
    private long seconds=1;
    public BenchmarkAdapter(Context context,ArrayList<CopyUtility>array){
        this.inflater=LayoutInflater.from(context);
        this.array=array;
    }

    @NonNull
    @Override
    public BenchmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.cpu_thread_stats_layout,parent,false);
        return new BenchmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BenchmarkViewHolder holder, int position) {
      final CopyUtility utility=array.get(position);
      final DiskUtils diskUtils=DiskUtils.getInstance();
      long  workCompleted=utility.getProgressMonitor().getWorkCompleted();
      long  workToBeDone=utility.getProgressMonitor().getWorkToBeDone();
      holder.speed.setText(CopyProgressMonitor.getWriteSpeedFromBytes(
              utility.getProgressMonitor().getWriteSpeedInt(seconds)));
      holder.progress.setProgress((int)(((float)workCompleted/(float)workToBeDone)*100));
      holder.bytes.setText(diskUtils.getSize(workCompleted)+"/"+diskUtils.getSize(workToBeDone));
      holder.percent.setText("(" + holder.progress.getProgress() + "%)");
      holder.title.setText("#" + (utility.getId() + 1));
    }


    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    class BenchmarkViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView speed;
        TextView bytes;
        ProgressBar progress;
        TextView percent;
        public BenchmarkViewHolder(View view){
            super(view);
            title=view.findViewById(R.id.title);
            speed=view.findViewById(R.id.speed);
            bytes=view.findViewById(R.id.bytes);
            progress=view.findViewById(R.id.progress);
            percent=view.findViewById(R.id.percent);
        }
    }
}
