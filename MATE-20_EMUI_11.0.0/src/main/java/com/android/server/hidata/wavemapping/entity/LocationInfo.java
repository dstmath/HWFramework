package com.android.server.hidata.wavemapping.entity;

public class LocationInfo {
    private String cellCluster;
    private String dataImsi;
    private String dateType;
    private String id;
    private String mainAp;
    private String place;
    private String update;
    private String wifiCluster;

    public LocationInfo(String id2, String place2) {
        this.id = id2;
        this.place = place2;
    }

    public String getUpdate() {
        return this.update;
    }

    public void setUpdate(String update2) {
        this.update = update2;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id2) {
        this.id = id2;
    }

    public String getPlace() {
        return this.place;
    }

    public void setPlace(String place2) {
        this.place = place2;
    }

    public String getDateType() {
        return this.dateType;
    }

    public void setDateType(String dateType2) {
        this.dateType = dateType2;
    }

    public String getMainAp() {
        return this.mainAp;
    }

    public void setMainAp(String mainAp2) {
        this.mainAp = mainAp2;
    }

    public String getWifiCluster() {
        return this.wifiCluster;
    }

    public void setWifiCluster(String wifiCluster2) {
        this.wifiCluster = wifiCluster2;
    }

    public String getCellCluster() {
        return this.cellCluster;
    }

    public void setCellCluster(String cellCluster2) {
        this.cellCluster = cellCluster2;
    }

    public String getDataImsi() {
        return this.dataImsi;
    }

    public void setDataImsi(String dataImsi2) {
        this.dataImsi = dataImsi2;
    }
}
