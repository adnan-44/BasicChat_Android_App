package com.addy.basicchat;

public class Message {

    // Model class for Messages
    private String message, senderUid, time;

    // getter and setters
    public void setMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return message;
    }

    public void setSenderUid(String senderUid){
        this.senderUid = senderUid;
    }

    public String getSenderUid(){
        return senderUid;
    }

    public void setTime(String time){
        this.time = time;
    }

    public String getTime(){
        return time;
    }

}
