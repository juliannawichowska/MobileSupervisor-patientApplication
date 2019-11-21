package com.example.mobilesupervisor_patientapp;


public class ModelChat {

    String message, receiver, sender, timestamp, userType, messageType;

    public ModelChat() {
    }

    public ModelChat(String message, String receiver, String sender, String timestamp, String userType, String messageType) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.timestamp = timestamp;
        this.userType = userType;
        this.messageType = messageType;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
