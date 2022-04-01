package com.file.manager.ui.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.file.manager.R;
import com.file.manager.helpers.MIMETypesHelper;
import com.file.manager.ui.Models.CustomFile;

public class OpenAsDialog extends Dialog implements View.OnClickListener {

    private CustomFile file;
    public OpenAsDialog(Context context, CustomFile file){
        super(context);
        this.file=file;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.open_as_window_layout);
        findViewById(R.id.audio).setOnClickListener(this);
        findViewById(R.id.text).setOnClickListener(this);
        findViewById(R.id.video).setOnClickListener(this);
        findViewById(R.id.image).setOnClickListener(this);
        findViewById(R.id.other).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.audio:
                new MIMETypesHelper(getContext(),file).startNoDefaults("audio/*");
                break;
            case R.id.text:
                new MIMETypesHelper(getContext(),file).startNoDefaults("text/csv");
                break;
            case R.id.video:
                new MIMETypesHelper(getContext(),file).startNoDefaults("video/*");
                break;
            case R.id.image:
                new MIMETypesHelper(getContext(),file).startNoDefaults("image/*");
                break;
            case R.id.other:
                new MIMETypesHelper(getContext(),file).startNoDefaults("*/*");
                break;

        }
        cancel();
    }
}
