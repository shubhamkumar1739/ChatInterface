package com.example.chatinterface.model;

public class UsersModel {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUid() {
        return uid;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    private String name;
    private String status;
    private String uid;
    private String image;

}
