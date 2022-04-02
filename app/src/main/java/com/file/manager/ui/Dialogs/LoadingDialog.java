package com.file.manager.ui.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.file.manager.R;

public class LoadingDialog extends Dialog {

    private DismissListener dismissListener;
    public LoadingDialog(Context context){
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setContentView(R.layout.dialog_loading);
    }


    public void setDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dismissListener.onDismiss();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        dismissListener.onDismiss();
    }

    public interface DismissListener{
        void onDismiss();
    }
}
