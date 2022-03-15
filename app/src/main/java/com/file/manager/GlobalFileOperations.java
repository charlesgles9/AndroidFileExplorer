package com.file.manager;

import com.file.manager.ui.Models.WindowModel;

public interface GlobalFileOperations {
     void copy();
     void paste();
     void cut();
     void select(Operations operations);
     void itemCount(int count);
     void switchWindow(WindowModel model);
}
