package com.file.manager.ui.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.R;
import com.file.manager.ui.Adapters.PlayListAdapter;
import com.file.manager.ui.Models.CustomFile;

import java.util.ArrayList;

public class AddPlayListDialog extends Dialog {


    private RecyclerView playList;
    private PlayListAdapter adapter;
    private TextView message;
    private ArrayList<CustomFile>files;
    private int selected=-1;
    public AddPlayListDialog(Context context, ArrayList<CustomFile>files){
        super(context);
        this.files=files;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
