package com.file.manager.ui.Models;


public class PropertyModel {

    private String title;
    private String details;
    public PropertyModel(String title,String details){
        this.title=title;
        this.details=details;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getTitle() {
        return title;
    }

    public String getDetails() {
        return details;
    }
}
