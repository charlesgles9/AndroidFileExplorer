package com.file.manager.ui.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.utils.DiskUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class DirectoryPathAdapter extends RecyclerView.Adapter<DirectoryPathAdapter.PathViewHolder> {


    private final ArrayList<String>segments= new ArrayList<>();
    private LayoutInflater inflater;
    private ItemListener itemListener;
    public DirectoryPathAdapter(Context context){
        this.inflater=LayoutInflater.from(context);
    }
    @NonNull
    @Override
    public PathViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.storage_path_layout,parent,false);
        return new PathViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PathViewHolder holder, int position) {
        holder.TextViewName.setText(segments.get(position));
    }

    public void setSegments(String path){
        segments.clear();
        String start= DiskUtils.getInstance().getStartDirectory(new File(path));
        path=path.replace(start,"");
        String newS="Local "+path;
        path=newS;
        String []array=path.split("/");
        Collections.addAll(segments,array);
        notifyDataSetChanged();
    }

    public void setItemListener(ItemListener itemListener){
        this.itemListener=itemListener;
    }
    public String getSegmentAsString(int stop){
        String path="";
        for(int i=0;i<=stop;i++){
            String segment=segments.get(i);
            if(segment.equals("Local "))
                continue;
            path+=segment+"/";
        }
        return path;
    }

    @Override
    public int getItemCount() {
        return segments.size();
    }

      class PathViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView TextViewName;
        public PathViewHolder(View view){
            super(view);
            TextViewName=view.findViewById(R.id.name);
            view.setOnClickListener(this);
        }

          @Override
          public void onClick(View v) {
              if(itemListener!=null)
                  itemListener.onItemClick(getAdapterPosition());
          }
      }

    public interface ItemListener{
        void onItemClick(int position);
    }
}
