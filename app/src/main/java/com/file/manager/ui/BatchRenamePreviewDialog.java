package com.file.manager.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Adapters.BatchRenameAdapter;
import com.file.manager.ui.Models.BatchRenameModel;

import java.util.ArrayList;

public class BatchRenamePreviewDialog extends Dialog {


  private ArrayList<BatchRenameModel>models;
    public BatchRenamePreviewDialog(Context context, ArrayList<BatchRenameModel>models){
        super(context);
        this.models=models;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.batch_rename_preview_layout);
        LinearLayoutManager layoutManager= new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        BatchRenameAdapter adapter= new BatchRenameAdapter(getContext(),models);
        RecyclerView recyclerView= findViewById(R.id.fileList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

    }


}
