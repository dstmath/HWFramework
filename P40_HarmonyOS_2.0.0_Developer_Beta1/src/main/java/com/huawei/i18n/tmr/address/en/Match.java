package com.huawei.i18n.tmr.address.en;

public class Match {
    private Integer endPos;
    private String matchedAddr;
    private Integer startPos;

    public Match() {
    }

    public Match(int begin, int end, String regex) {
        this.startPos = Integer.valueOf(begin);
        this.endPos = Integer.valueOf(end);
        this.matchedAddr = regex;
    }

    public Integer getStartPos() {
        return this.startPos;
    }

    public void setStartPos(Integer startPos2) {
        this.startPos = startPos2;
    }

    public Integer getEndPos() {
        return this.endPos;
    }

    public void setEndPos(Integer endPos2) {
        this.endPos = endPos2;
    }

    public String getMatchedAddr() {
        return this.matchedAddr;
    }

    public void setMatchedAddr(String matchedAddr2) {
        this.matchedAddr = matchedAddr2;
    }
}
