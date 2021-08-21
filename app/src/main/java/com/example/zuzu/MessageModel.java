package com.example.zuzu;

import java.util.Calendar;

public class MessageModel {

    public enum MessageType {TEXT, IMAGE};

    private MessageType type;
    private String authorID;
    private String authorName;
    private String authorEmail;
    private String messageContent;
    private String creationDate;

    public MessageModel() {
        //Empty Constructor
    }

    public MessageModel(String authorID, String authorEmail,String authorName, String messageContent, MessageType type) {
        this.authorID = authorID;
        this.messageContent = messageContent;
        this.type = type;
        this.creationDate = Calendar.getInstance().getTime().toString();
        this.authorName = authorName;
        this.authorEmail = authorEmail;
    }


    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) {
        this.authorID = authorID;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }
}
