package com.fyp.amenms.database;

import com.fyp.amenms.Utilities.Constants;

import java.io.Serializable;

public class RequestHelperClass implements Serializable {
    private String requestId= "";
    private String userUid = "";
    private String description = "";
    private String orderDateTime = "";
    private String mobNumber = "";
    private String providerUid = "";
    private String alternateMobNumber = "";
    private String address = "";
    private String alternateAddress = "";
    private String workingHours = "";
    private double latitude = 0;
    private double longitude = 0;
    private int status = 0;
    private String statusUpdatedBy = Constants.RequestStatusUpdatedBy.USER;
    private boolean notified = false;
    private String paymentMethod="Cash";

    private int charges = 0;

    private ProviderHelperClass providerData;
    private UserHelperClass userData;

    public RequestHelperClass() {
    }

    public RequestHelperClass(String userUid, String providerUid, String description, String orderDateTime, String mobNumber,
                              String alternateMobNumber, String address, String alternateAddress, String workingHours,
                              double latitude, double longitude) {
        setUserUid(userUid);
        setProviderUid(providerUid);
        setDescription(description);
        setOrderDateTime(orderDateTime);
        setMobNumber(mobNumber);
        setAlternateMobNumber(alternateMobNumber);
        setAddress(address);
        setAlternateAddress(alternateAddress);
        setWorkingHours(workingHours);
        setLatitude(latitude);
        setLongitude(longitude);
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrderDateTime() {
        return orderDateTime;
    }

    public void setOrderDateTime(String orderDateTime) {
        this.orderDateTime = orderDateTime;
    }

    public String getMobNumber() {
        return mobNumber;
    }

    public void setMobNumber(String mobNumber) {
        this.mobNumber = mobNumber;
    }

    public String getProviderUid() {
        return providerUid;
    }

    public void setProviderUid(String providerUid) {
        this.providerUid = providerUid;
    }

    public String getAlternateMobNumber() {
        return alternateMobNumber;
    }

    public void setAlternateMobNumber(String alternateMobNumber) {
        this.alternateMobNumber = alternateMobNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAlternateAddress() {
        return alternateAddress;
    }

    public void setAlternateAddress(String alternateAddress) {
        this.alternateAddress = alternateAddress;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean getNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public String getStatusUpdatedBy() {
        return statusUpdatedBy;
    }

    public void setStatusUpdatedBy(String statusUpdatedBy) {
        this.statusUpdatedBy = statusUpdatedBy;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public ProviderHelperClass getProviderData() {
        return providerData;
    }

    public void setProviderData(ProviderHelperClass providerData) {
        this.providerData = providerData;
    }

    public UserHelperClass getUserData() {
        return userData;
    }

    public void setUserData(UserHelperClass userData) {
        this.userData = userData;
    }

    public int getCharges() {
        return charges;
    }

    public void setCharges(int charges) {
        this.charges = charges;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
