package com.file.manager.ui.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;

import com.file.manager.R;
import com.file.manager.helpers.AuthenticationHelper;

public class FingerPrintAuthDialog extends Dialog {
    private Context context;
    private AuthenticationHelper.OnAuthSuccess onAuthSuccess;
    private boolean cancelable;
    public FingerPrintAuthDialog(Context context,boolean cancelable){
        super(context,R.style.DialogStyle);
        this.context=context;
        this.cancelable=cancelable;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(cancelable);
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.dialog_finger_print_prompt);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        new AuthenticationHelper().requestAuthentication(context,onAuthSuccess);
    }


    public void setOnAuthSuccess(AuthenticationHelper.OnAuthSuccess onAuthSuccess) {
        this.onAuthSuccess = onAuthSuccess;
    }

    @Override
    public void dismiss() {
        super.dismiss();

    }
}
