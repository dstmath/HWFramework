package com.android.contacts.hap.numbermark.hwtoms.model.request;

public class TomsRequestInfoForHW extends TomsRequestBase {
    private String custClass;
    private String keyword;
    private String poiR;
    private String poiX;
    private String poiY;
    private String regionCode;
    private String regionName;
    private String resultCount;
    private String start;

    public String getStart() {
        return this.start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getResultCount() {
        return this.resultCount;
    }

    public void setResultCount(String resultCount) {
        this.resultCount = resultCount;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCustClass() {
        return this.custClass;
    }

    public void setCustClass(String custClass) {
        this.custClass = custClass;
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

    public String getPoiR() {
        return this.poiR;
    }

    public void setPoiR(String poiR) {
        this.poiR = poiR;
    }
}
