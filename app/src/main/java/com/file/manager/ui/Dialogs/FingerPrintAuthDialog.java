package com.file.manager.ui.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import com.file.manager.R;
import com.file.manager.helpers.AuthenticationHelper;

public class FingerPrintAuthDialog extends Dialog {
    private Context context;
    private AuthenticationHelper.OnAuthSuccess onAuthSuccess;
    private boolean cancelable;
    public FingerPrintAuthDialog(Context context,boolean cancelable){
        super(context);
        this.context=context;
        this.cancelable=cancelable;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(cancelable);
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.finger_print_dialog_prompt);
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
