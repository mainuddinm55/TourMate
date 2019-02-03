package com.nullpointers.toutmate.Model;

public class User {

    private String name;
    private String mobile;
    private String email;
    private String photoPath;

    public User() {
        //required for firebase
    }

    public User(String name, String mobile, String email, String photoPath) {
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.photoPath = photoPath;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoPath() {
        return photoPath;
    }


}
