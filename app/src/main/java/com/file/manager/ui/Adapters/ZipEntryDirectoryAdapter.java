package com.file.manager.ui.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;

import java.util.ArrayList;
import java.util.Collections;

public class ZipEntryDirectoryAdapter extends RecyclerView.Adapter<ZipEntryDirectoryAdapter.ZipEntryViewHolder> {

    private ArrayList<String>array;
    private LayoutInflater inflater;
    private OnItemClickListener onItemClickListener;
    public ZipEntryDirectoryAdapter(Context context){
        this.array= new ArrayList<>();
        this.inflater=LayoutInflater.from(context);
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ZipEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.path_layout,parent,false);
        return new ZipEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ZipEntryViewHolder holder, int position) {
     holder.TextViewName.setText(array.get(position));
    }

    public void setDirectory(String path){
        array.clear();
        path=path.replace(" ","");
        Collections.addAll(array,(" /"+path).split("/"));
        notifyDataSetChanged();
    }

    public String getString(int end){
        String string="";
        for(int i=0;i<=end;i++){
            string+= array.get(i)+"/";
        }
        return string.substring(0,string.length()-1).replace(" /","");
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    class ZipEntryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView TextViewName;
        public ZipEntryViewHolder(View view){
            super(view);
            TextViewName=view.findViewById(R.id.name);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        onItemClickListener.onClick(getAdapterPosition());
        }
    }

    public interface  OnItemClickListener{
        void onClick(int position);
    }
}
