package com.huawei.opcollect.location;

public class HwLocation implements Cloneable {
    private int accuracy;
    private double altitude;
    private String city;
    private String cityCode;
    private String country;
    private String detailAddress;
    private String district;
    private double latitude = 0.0d;
    private double longitude = 0.0d;
    private int priority;
    private String provider;
    private String province;
    private long timestamp = -1;

    public HwLocation(double longitude2, double latitude2, double altitude2, int priority2, String provider2, String city2, String country2, String detailAddress2, String district2, String province2, String cityCode2) {
        this.longitude = longitude2;
        this.latitude = latitude2;
        this.altitude = altitude2;
        this.priority = priority2;
        this.provider = provider2;
        this.city = city2;
        this.country = country2;
        this.detailAddress = detailAddress2;
        this.district = district2;
        this.province = province2;
        this.cityCode = cityCode2;
    }

    public HwLocation() {
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp2) {
        this.timestamp = timestamp2;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude2) {
        this.longitude = longitude2;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude2) {
        this.latitude = latitude2;
    }

    public double getAltitude() {
        return this.altitude;
    }

    public void setAltitude(double altitude2) {
        this.altitude = altitude2;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority2) {
        this.priority = priority2;
    }

    public String getProvider() {
        return this.provider;
    }

    public void setProvider(String provider2) {
        this.provider = provider2;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city2) {
        this.city = city2;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country2) {
        this.country = country2;
    }

    public String getDetailAddress() {
        return this.detailAddress;
    }

    public void setDetailAddress(String detailAddress2) {
        this.detailAddress = detailAddress2;
    }

    public String getDistrict() {
        return this.district;
    }

    public void setDistrict(String district2) {
        this.district = district2;
    }

    public String getProvince() {
        return this.province;
    }

    public void setProvince(String province2) {
        this.province = province2;
    }

    public String getCityCode() {
        return this.cityCode;
    }

    public void setCityCode(String cityCode2) {
        this.cityCode = cityCode2;
    }

    public int getAccuracy() {
        return this.accuracy;
    }

    public void setAccuracy(int accuracy2) {
        this.accuracy = accuracy2;
    }

    public HwLocation clone() throws CloneNotSupportedException {
        return (HwLocation) super.clone();
    }
}
