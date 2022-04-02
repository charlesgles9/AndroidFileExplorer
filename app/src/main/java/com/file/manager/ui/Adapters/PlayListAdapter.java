package com.file.manager.ui.Adapters;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.file.manager.R;
import com.file.manager.ui.Models.PlayListHierarchy;
import java.util.ArrayList;
public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.PlayListViewHolder> {


    private LayoutInflater inflater;
    private ArrayList<PlayListHierarchy.PlayListModel>array;
    private OnItemClickListener onItemClickListener;
    private boolean activateSelect;
    private int selectCount=0;
    private int highlight=-1;
    public PlayListAdapter(Context context,ArrayList<PlayListHierarchy.PlayListModel>array){
        this.inflater=LayoutInflater.from(context);
        this.array= array;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= inflater.inflate(R.layout.audio_playlist_item_layout,parent,false);
        return new PlayListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListViewHolder holder, int position) {
        PlayListHierarchy.PlayListModel model=array.get(position);
        holder.name.setText(model.getPath());
        holder.name.setTextColor(highlight==position? Color.argb(200,69,155,241):
                Color.argb(200,255,255,255));
        holder.selected.setVisibility(activateSelect?View.VISIBLE:View.INVISIBLE);
        holder.selected.setChecked(model.isSelected());
    }

    public void setSelectCount(int selectCount) {
        this.selectCount = selectCount;
    }

    public int getSelectCount() {
        return selectCount;
    }

    public void setHighlight(int highlight) {
        this.highlight = highlight;
    }

    public int getHighlight() {
        return highlight;
    }

    public void setActivateSelect(boolean activateSelect) {
        this.activateSelect = activateSelect;
    }

    public boolean isActivateSelect() {
        return activateSelect;
    }

    public void reset(){
        for(PlayListHierarchy.PlayListModel file:array){
            file.setSelected(false);
        }
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    class PlayListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView name;
        ToggleButton selected;
        public PlayListViewHolder(View view){
            super(view);
            name=view.findViewById(R.id.name);
            selected=view.findViewById(R.id.selected);
            selected.setClickable(false);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener!=null)
                onItemClickListener.onClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if(onItemClickListener!=null)
                onItemClickListener.onLongClick(getAdapterPosition());
            return false;
        }
    }

    public interface  OnItemClickListener{
        void onClick(int position);
        void onLongClick(int position);
    }
}
