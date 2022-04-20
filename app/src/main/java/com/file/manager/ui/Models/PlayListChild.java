package com.file.manager.ui.Models;

import androidx.annotation.NonNull;

import java.io.File;

public class PlayListChild extends File {

    private boolean selected=false;

    public PlayListChild(@NonNull String pathname) {
        super(pathname);
    }


    @NonNull
    @Override
    public String getName() {
        return super.getName();
    }

    @NonNull
    @Override
    public String getPath() {
        return super.getPath();
    }

    @Override
    public boolean exists() {
        return super.exists();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }
}
