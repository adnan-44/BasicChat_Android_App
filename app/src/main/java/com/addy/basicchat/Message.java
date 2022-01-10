package com.addy.basicchat;

public class Message {

    // Model class for Messages
    private String message, senderUid, time;
    private Boolean messageSeen;

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

    public void setMessageSeen(Boolean messageSeen){
        this.messageSeen = messageSeen;
    }

    public Boolean getMessageSeen(){
        return messageSeen;
    }
}
