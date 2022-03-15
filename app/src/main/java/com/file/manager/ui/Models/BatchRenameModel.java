package com.file.manager.ui.Models;

public class BatchRenameModel {

    private CustomFile file;
    private String rename;
    public BatchRenameModel(CustomFile file){
        this.file=file;
        this.rename=file.getName();
    }


    public void setRename(String rename) {
        this.rename = rename;
    }

    public String getRename() {
        return rename;
    }

    public void reset(){
        this.rename=file.getName();
    }

    public CustomFile getFile() {
        return file;
    }
}
