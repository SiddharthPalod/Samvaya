package com.eventverse.notificationservice.dto;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterRequest {
    @NotBlank 
    @JsonProperty("partnerId")
    @JsonAlias("partnerName")
    private String partnerId;
    
    @NotBlank 
    @JsonProperty("url")
    @JsonAlias("callbackUrl")
    private String url;
    
    // getters/setters

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}