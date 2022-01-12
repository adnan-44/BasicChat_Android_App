package com.addy.basicchat;

public class UserInformation {

    // Model class for all_user_info
    private String bio, full_name, email, password, uid;

    // Empty public constructor
    public UserInformation(){
    
    }

    // Getter and setters
    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getBio() {
        return bio;
    }

    public void setFull_name(String fullName){
        this.full_name = fullName;
    }

    public String getFull_name(){
        return full_name;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail(){
        return email;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getPassword(){
        return password;
    }

    public void setUid(String uid){
        this.uid = uid;
    }

    public String getUid(){
        return uid;
    }
}
