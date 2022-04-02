package com.file.manager.ui.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Adapters.SwitchWindowAdapter;
import com.file.manager.ui.Models.WindowModel;
import com.file.manager.ui.utils.WindowUtil;

public class SwitchWindowDialog  extends Dialog  implements View.OnClickListener {


    private SwitchWindowAdapter adapter;
    private SwitchWindowAdapter.onWindowSelectListener listener;
    public SwitchWindowDialog(Context context){
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_window_chooser_layout);
        setCancelable(false);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        final RecyclerView windowRecyclerView=findViewById(R.id.windowList);
        final ImageView cancel=findViewById(R.id.CANCEL);
        final TextView Path=findViewById(R.id.path);
        final LinearLayoutManager layoutManager= new LinearLayoutManager(getContext());
        adapter= new SwitchWindowAdapter(getContext());
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        windowRecyclerView.setLayoutManager(layoutManager);
        windowRecyclerView.setAdapter(adapter);
      final WindowModel model= WindowUtil.getInstance().getCurrent();

      // current active window path
        Path.setText(model.getPath());
        cancel.setOnClickListener(this);
        WindowUtil.getInstance().resetHighlights();
        model.setActive(true);
        windowRecyclerView.scrollToPosition(adapter.getHighlightedPosition());
        adapter.notifyDataSetChanged();

    }

    public void setOnWindowSelectListener(SwitchWindowAdapter.onWindowSelectListener listener){
        adapter.setOnWindowSelectListener(listener);
        this.listener=listener;
    }



   public SwitchWindowAdapter getAdapter(){
        return adapter;
   }
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.CANCEL) {
            cancel();
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        listener.onCancel();
    }
}
