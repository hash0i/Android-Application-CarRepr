package com.fyp.amenms.database;

import java.io.Serializable;

public class ProviderHelperClass implements Serializable {

    String name="";
    String cnic="";
    String email="";
    String password="";
    String mobNumber="";
    private String expertise="";
    private String workingHours="";
    private String experience="";
    private String address="";
    private double latitude=0;
    private double longitude=0;
    private String uid = "";
    private int basicCharges = 0;

    public ProviderHelperClass() {}

    public ProviderHelperClass(String name, String cnic, String email, String password, String mobNumber) {
        this.name = name;
        this.cnic = cnic;
        this.email = email;
        this.password = password;
        this.mobNumber = mobNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
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

    public String getMobNumber() {
        return mobNumber;
    }

    public void setMobNumber(String mobNumber) {
        this.mobNumber = mobNumber;
    }


    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getBasicCharges() {
        return basicCharges;
    }

    public void setBasicCharges(int basicCharges) {
        this.basicCharges = basicCharges;
    }
}
