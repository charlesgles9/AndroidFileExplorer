package com.file.manager.ui.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.file.manager.R;
import com.google.android.material.textfield.TextInputEditText;

public class EnterPasswordDialog extends Dialog {

    private PasswordCallback passwordCallback;
    public EnterPasswordDialog(Context context){
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.dialog_enter_password_layout);
        final Button cancel=findViewById(R.id.cancel);
        final Button okay=findViewById(R.id.okay);
        final TextInputEditText password=findViewById(R.id.password);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               passwordCallback.cancel();
               dismiss();
            }
        });

        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(),"Field cannot be empty!",Toast.LENGTH_SHORT).show();
                    return;
                }
                passwordCallback.accept(password.getText().toString());
                dismiss();
            }
        });
    }


    public void setPasswordCallback(PasswordCallback passwordCallback) {
        this.passwordCallback = passwordCallback;
    }

    public interface PasswordCallback{
        void accept(String password);
        void cancel();
    }
}
