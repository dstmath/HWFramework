package com.android.contacts.hap.numbermark.hwtoms.model.response;

public class TomsResponseInfoForHW {
    private String address;
    private String classCode1;
    private String classCode2;
    private String classname1;
    private String classname2;
    private String custName;
    private String errorCode;
    private String id;
    private String logo;
    private String poiX;
    private String poiY;
    private String regionCode;
    private String regionName;
    private String tel;
    private String type;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustName() {
        return this.custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getTel() {
        return this.tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLogo() {
        return this.logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getRegionCode() {
        return this.regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getRegionName() {
        return this.regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getClassCode1() {
        return this.classCode1;
    }

    public void setClassCode1(String classCode1) {
        this.classCode1 = classCode1;
    }

    public String getClassname1() {
        return this.classname1;
    }

    public void setClassname1(String classname1) {
        this.classname1 = classname1;
    }

    public String getClassCode2() {
        return this.classCode2;
    }

    public void setClassCode2(String classCode2) {
        this.classCode2 = classCode2;
    }

    public String getClassname2() {
        return this.classname2;
    }

    public void setClassname2(String classname2) {
        this.classname2 = classname2;
    }

    public String getPoiX() {
        return this.poiX;
    }

    public void setPoiX(String poiX) {
        this.poiX = poiX;
    }

    public String getPoiY() {
        return this.poiY;
    }

    public void setPoiY(String poiY) {
        this.poiY = poiY;
    }

    public String toString() {
        return "[errorCode = " + this.errorCode + ", id = " + this.id + ", custName = " + this.custName + ", tel = " + this.tel + ", address = " + this.address + ", logo = " + this.logo + ", regionCode = " + this.regionCode + ", regionName = " + this.regionName + ", classCode1 = " + this.classCode1 + ", classname1 = " + this.classname1 + ", classCode2 = " + this.classCode2 + ", classname2 = " + this.classname2 + ", poiX = " + this.poiX + ", poiY = " + this.poiY + ", type = " + this.type + "]";
    }
}
