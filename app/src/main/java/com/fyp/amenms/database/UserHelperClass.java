package com.fyp.amenms.database;

import java.io.Serializable;

public class UserHelperClass implements Serializable {

    String name="", cnic="", email="", password="", mobNumber="";

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



}
