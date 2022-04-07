package com.file.manager.ui.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Models.WindowModel;
import com.file.manager.utils.WindowUtil;

import java.util.ArrayList;


public class SwitchWindowAdapter extends RecyclerView.Adapter<SwitchWindowAdapter.SwitchWindowViewHolder> {


    private Context context;
    private LayoutInflater inflater;
    private  ArrayList<WindowModel>list;
    private  onWindowSelectListener listener;
    public SwitchWindowAdapter(Context context){
        this.context=context;
        this.inflater=LayoutInflater.from(context);
        list= WindowUtil.getInstance().getAsListModel();
    }

    @NonNull
    @Override
    public SwitchWindowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
          View view= inflater.inflate(R.layout.dialog_window_layout,parent,false);
        return new SwitchWindowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SwitchWindowViewHolder holder, int position) {
        WindowModel model=list.get(position);
       if(model.getView()!=null)
        holder.screen.setImageBitmap(model.getView());
        holder.delete.setVisibility(model.getTitle().equals("Home")?View.INVISIBLE:View.VISIBLE);
        if(model.isActive())
        holder.highLight.setBackgroundResource(R.drawable.drawable_highlight6);
        else holder.highLight.setBackgroundResource(R.drawable.drawable_highlight4);
    }

    public void setOnWindowSelectListener(onWindowSelectListener l){
        listener=l;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public ArrayList<WindowModel> getList() {
        return list;
    }

    public int getHighlightedPosition(){
        for(int i=0;i<list.size();i++)
            if(list.get(i).isActive())
                return i;
             return 0;
    }

    class SwitchWindowViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView delete;
        ImageView screen;
        View highLight;
        public SwitchWindowViewHolder(View view) {
            super(view);
             this.delete = view.findViewById(R.id.DELETE);
             this.screen = view.findViewById(R.id.ic_screen);
             this.highLight=view.findViewById(R.id.highlight);
             delete.setOnClickListener(this);
             screen.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            int adapterP=getAdapterPosition();
            int position=adapterP;
            if (v.getId() == R.id.ic_screen) {
                if (listener != null)
                    listener.onWindowSelect(list.get(position));
            }else {
                if(position>=1) {
                    position--;
                }else if(position<list.size()-1) {
                    position++;
                }
                WindowUtil window=WindowUtil.getInstance();
                window.resetHighlights();
                if(list.get(getAdapterPosition()).getId()==window.getCurrent().getId())
                    listener.onWindowSelect(list.get(position));
                 else
                    listener.onWindowSelect(window.getCurrent());

                WindowUtil.getInstance().remove(list.get(adapterP));
                notifyItemRemoved(adapterP);
            }

        }
    }

    public interface onWindowSelectListener{

        void onWindowSelect(WindowModel model);
        void onCancel();
    }
}