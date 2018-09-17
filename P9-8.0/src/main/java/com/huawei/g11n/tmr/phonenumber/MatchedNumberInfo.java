package com.huawei.g11n.tmr.phonenumber;

public class MatchedNumberInfo {
    private int begin;
    private String content;
    private int end;

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getBegin() {
        return this.begin;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getEnd() {
        return this.end;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }
}
