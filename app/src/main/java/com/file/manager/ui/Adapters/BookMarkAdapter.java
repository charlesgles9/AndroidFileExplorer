package com.file.manager.ui.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.utils.DateUtils;

import java.io.File;
import java.util.ArrayList;

public class BookMarkAdapter extends RecyclerView.Adapter<BookMarkAdapter.BookMarkViewHolder> {

    private LayoutInflater inflater;
    private ArrayList<File> bookmarks;
    private onItemClickListener onItemClickListener;
    private SharedPreferences preferences;
    public BookMarkAdapter(Context context,ArrayList<File>bookmarks){
        this.inflater=LayoutInflater.from(context);
        this.bookmarks=bookmarks;
        this.preferences=context.getSharedPreferences("Bookmarks",Context.MODE_PRIVATE);
    }


    @NonNull
    @Override
    public BookMarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.bookmark_layout,parent,false);
        return new BookMarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookMarkViewHolder holder, int position) {
        File file=bookmarks.get(position);
        holder.name.setText(file.getName());
        holder.path.setText(file.getPath());
        holder.date.setText(DateUtils.getDateString(file.lastModified()));
    }

    public void setOnItemClickListener(BookMarkAdapter.onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    class BookMarkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
         TextView name;
         TextView path;
         TextView date;
         ImageView delete;
        public BookMarkViewHolder(View view){
            super(view);
            this.name=view.findViewById(R.id.name);
            this.path=view.findViewById(R.id.path);
            this.date=view.findViewById(R.id.date);
            this.delete=view.findViewById(R.id.delete);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            delete.setOnClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            onItemClickListener.onLongClick(bookmarks.get(getAdapterPosition()).getPath());
            return false;
        }

        @Override
        public void onClick(View v) {
             if(v.getId()!=R.id.delete) {
                 onItemClickListener.onClick(bookmarks.get(getAdapterPosition()).getPath());
             }else{
                 int position=getAdapterPosition();
                  SharedPreferences.Editor editor=preferences.edit();
                  editor.remove(bookmarks.get(position).getPath());
                  editor.apply();
                  bookmarks.remove(position);
                  notifyItemRemoved(position);
             }
        }
    }

   public interface onItemClickListener{

        void onClick(String key);
        void onLongClick(String key);
    }
}
