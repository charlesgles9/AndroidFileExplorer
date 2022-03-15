package com.file.manager.ui;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.file.manager.R;
import com.google.android.material.textfield.TextInputEditText;

public class PlayListNameDialog extends Dialog {

    private PlayListCallBack playListCallBack;
    public PlayListNameDialog(Context context){
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.add_playlist_dialog);
        final Button cancel=findViewById(R.id.cancel);
        final Button okay=findViewById(R.id.okay);
        final TextInputEditText name=findViewById(R.id.name);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playListCallBack.cancel();
                dismiss();
            }
        });

        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(),"Field cannot be empty!",Toast.LENGTH_SHORT).show();
                    return;
                }
                playListCallBack.accept(name.getText().toString());
                dismiss();
            }
        });
    }


    public void setPlayListCallBack(PlayListCallBack playListCallBack) {
        this.playListCallBack = playListCallBack;
    }

    public interface PasswordCallback{
        void accept(String password);
        void cancel();
    }
    public interface PlayListCallBack {
        void accept(String name);
        void cancel();
    }
}
