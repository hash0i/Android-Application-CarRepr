package com.fyp.amenms.database;

import java.io.Serializable;

public class UserHelperClass implements Serializable {

    String name="", cnic="", email="", password="", mobNumber="";

    private double latitude=0;
    private double longitude=0;
    private String address = "";

    public UserHelperClass() {}

    public UserHelperClass(String name, String cnic, String email, String password, String mobNumber) {
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
