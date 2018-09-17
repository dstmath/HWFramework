package com.huawei.g11n.tmr.address;

public class Match {
    private Integer endPos;
    private String matchedAddr;
    private Integer startPos;

    public Match(int begin, int end, String regex) {
        this.startPos = Integer.valueOf(begin);
        this.endPos = Integer.valueOf(end);
        this.matchedAddr = regex;
    }

    public Integer getStartPos() {
        return this.startPos;
    }

    public void setStartPos(Integer startPos) {
        this.startPos = startPos;
    }

    public Integer getEndPos() {
        return this.endPos;
    }

    public void setEndPos(Integer endPos) {
        this.endPos = endPos;
    }

    public String getMatchedAddr() {
        return this.matchedAddr;
    }

    public void setMatchedAddr(String matchedAddr) {
        this.matchedAddr = matchedAddr;
    }
}
