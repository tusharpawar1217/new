package com.eligibilitygpt.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public class ProfileRequest {
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @NotBlank(message = "Category is required")
    @Pattern(regexp = "^(GENERAL|SC|ST|OBC|EWS)$", message = "Category must be one of: GENERAL, SC, ST, OBC, EWS")
    private String category;
    
    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be one of: MALE, FEMALE, OTHER")
    private String gender;
    
    @NotBlank(message = "Education level is required")
    private String educationLevel;
    
    private String state;
    private String district;
    private String address;
    private String phoneNumber;
    private Boolean physicallyChallenged = false;
    private Boolean exServiceman = false;
    
    public ProfileRequest() {}
    
    // Getters and Setters
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getEducationLevel() {
        return educationLevel;
    }
    
    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getDistrict() {
        return district;
    }
    
    public void setDistrict(String district) {
        this.district = district;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public Boolean getPhysicallyChallenged() {
        return physicallyChallenged;
    }
    
    public void setPhysicallyChallenged(Boolean physicallyChallenged) {
        this.physicallyChallenged = physicallyChallenged;
    }
    
    public Boolean getExServiceman() {
        return exServiceman;
    }
    
    public void setExServiceman(Boolean exServiceman) {
        this.exServiceman = exServiceman;
    }
}