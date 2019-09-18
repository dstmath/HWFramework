package com.android.server.wifi.wifipro.hwintelligencewifi;

public class LocationAddress {
    private double distanceFromHome;
    private boolean isInvalid;
    private boolean isOversea;
    private double latitude;
    private double longitude;
    private long updateTime;

    public LocationAddress() {
        this(-1.0d, -1.0d);
    }

    public LocationAddress(double lat, double lng) {
        this(lat, lng, Long.valueOf(System.currentTimeMillis()));
    }

    public LocationAddress(double lat, double lng, Long time) {
        this(lat, lng, -1.0d, time);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public LocationAddress(double lat, double lng, double distance, Long time) {
        this(lat, lng, distance, false, lat < 0.0d || lng < 0.0d || (lat == 0.0d && lng == 0.0d), time);
    }

    public LocationAddress(double lat, double lng, double distance, boolean isOversea2, boolean isInvalid2, Long time) {
        this.latitude = lat;
        this.longitude = lng;
        this.distanceFromHome = distance;
        this.isOversea = isOversea2;
        this.isInvalid = isInvalid2;
        this.updateTime = time.longValue();
    }

    public boolean isHome() {
        return this.distanceFromHome >= 0.0d && this.distanceFromHome < 200.0d;
    }

    public long getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(long time) {
        this.updateTime = time;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLatitude(double latitude2) {
        this.latitude = latitude2;
    }

    public void setLongitude(double longitude2) {
        this.longitude = longitude2;
    }

    public void setinvalid(boolean invalid) {
        this.isInvalid = invalid;
    }

    public boolean isInvalid() {
        return this.isInvalid || (this.latitude <= 0.0d && this.longitude <= 0.0d);
    }

    public void setOversea(boolean Oversea) {
        this.isOversea = Oversea;
    }

    public boolean isOversea() {
        return this.isOversea;
    }

    public String toString() {
        return "  latitude: " + this.latitude + ",  longitude: " + this.longitude + ",  distanceFromHome: " + this.distanceFromHome + ",  updateTime: " + this.updateTime + ",  isInvalid: " + this.isInvalid + ",  isOversea: " + this.isOversea;
    }
}
