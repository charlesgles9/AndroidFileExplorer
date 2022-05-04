package com.file.manager.ui.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Models.CpuModel;

import java.util.ArrayList;

public class CpuInfoAdapter extends RecyclerView.Adapter<CpuInfoAdapter.CpuInfoViewHolder> {

   private LayoutInflater inflater;
   private ArrayList<CpuModel>array;
    public CpuInfoAdapter(Context context,ArrayList<CpuModel>array){
        this.inflater=LayoutInflater.from(context);
        this.array=array;
    }

    @NonNull
    @Override
    public CpuInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.cpu_info_layout,parent,false);
        return new CpuInfoViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CpuInfoViewHolder holder, int position) {
        CpuModel cpuModel=array.get(position);
        holder.processor.setText(cpuModel.getName());
        holder.usage.setText(cpuModel.getCurUsage()+"%");
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    static class CpuInfoViewHolder extends RecyclerView.ViewHolder{
        TextView processor;
        TextView usage;
        public CpuInfoViewHolder(View view){
            super(view);
            processor=view.findViewById(R.id.processor);
            usage=view.findViewById(R.id.usage);
        }
    }
}
