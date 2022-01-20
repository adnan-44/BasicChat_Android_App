package com.addy.basicchat;

public class UserInformation {

    // Model class for all_user_info
    private String bio, full_name, email, password, uid, image_url, status;

    // Empty public constructor
    public UserInformation() {

    }

    public String getStatus() {
        return status;
    }

    // Getter and setters
    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String fullName) {
        this.full_name = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
