package com.file.manager.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;
import com.file.manager.R;

public class TaskDialogPrompt extends Dialog implements View.OnClickListener {

    private OnItemClickListener onItemClickListener;
    private Button allow;
    private Button dismiss;
    private ToggleButton remember;
    private boolean foreground=false;
    public TaskDialogPrompt(Context context){
        super(context);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.copy_dialog_prompt);
        allow=findViewById(R.id.allow);
        dismiss=findViewById(R.id.dismiss);
        remember=findViewById(R.id.remember);
        allow.setOnClickListener(this);
        dismiss.setOnClickListener(this);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.allow){
            foreground=true;
        }
        cancel();
        onItemClickListener.onClick();
    }

    public boolean rememberChoice() {
        return remember.isChecked();
    }
    public boolean isForeground(){
        return foreground;
    }
    public interface OnItemClickListener{
        void onClick();
    }

}
