package com.android.contacts.hap.numbermark.hwtoms.model.response;

public class TomsResponseTelForHW {
    private String classname1;
    private String classname2;
    private String errorCode;
    private String name;
    private String type;

    public TomsResponseTelForHW(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassname1() {
        return this.classname1;
    }

    public void setClassname1(String classname1) {
        this.classname1 = classname1;
    }

    public String getClassname2() {
        return this.classname2;
    }

    public void setClassname2(String classname2) {
        this.classname2 = classname2;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        return "[errorCode = " + this.errorCode + ", name = " + this.name + ", type = " + this.type + ", classname1 = " + this.classname1 + ", classname2 = " + this.classname2 + "]";
    }
}
