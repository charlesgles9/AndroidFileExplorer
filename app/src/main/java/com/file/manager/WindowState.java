package com.file.manager;

import androidx.fragment.app.Fragment;

public interface WindowState  {

    void setFragmentID(int id);
    int getFragmentID();
    void setDeleted(boolean flag);
    boolean isDeleted();
    void setParent(Fragment fragment);
    Fragment getParent();
}
