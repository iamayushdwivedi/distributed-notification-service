package com.ayush.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NotificationRequest {

    @NotBlank(message = "Recipient is a required field")
    @Email(message = "Recipient must be a valid email")
    @Size(max = 320, message = "Recipient must not exceed 320 characters")
    private String recipient;
    @NotBlank(message = "Message is a required field")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;
    @NotBlank(message = "Channel is a required field")
    private String channel;

    // Getters and setters
    public String getRecipient(){
        return recipient;
    }

    public void setRecipient(String recipient){
        this.recipient = recipient;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public String getChannel(){
        return channel;
    }

    public void setChannel(String channel){
        this.channel = channel;
    }
}