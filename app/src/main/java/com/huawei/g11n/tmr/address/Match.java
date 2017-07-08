package com.huawei.g11n.tmr.address;

public class Match {
    private Integer endPos;
    private String matchedAddr;
    private Integer startPos;

    public Match(int i, int i2, String str) {
        this.startPos = Integer.valueOf(i);
        this.endPos = Integer.valueOf(i2);
        this.matchedAddr = str;
    }

    public Integer getStartPos() {
        return this.startPos;
    }

    public void setStartPos(Integer num) {
        this.startPos = num;
    }

    public Integer getEndPos() {
        return this.endPos;
    }

    public void setEndPos(Integer num) {
        this.endPos = num;
    }

    public String getMatchedAddr() {
        return this.matchedAddr;
    }

    public void setMatchedAddr(String str) {
        this.matchedAddr = str;
    }
}
