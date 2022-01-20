package com.addy.basicchat;

public class Message {

    // Model class for Messages
    private String message, senderUid, time;
    private Boolean messageSeen;

    public String getMessage() {
        return message;
    }

    // getter and setters
    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Boolean getMessageSeen() {
        return messageSeen;
    }

    public void setMessageSeen(Boolean messageSeen) {
        this.messageSeen = messageSeen;
    }
}
