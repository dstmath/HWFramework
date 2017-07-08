package com.huawei.g11n.tmr.phonenumber;

public class MatchedNumberInfo {
    private int begin;
    private String content;
    private int end;

    public void setBegin(int i) {
        this.begin = i;
    }

    public int getBegin() {
        return this.begin;
    }

    public void setEnd(int i) {
        this.end = i;
    }

    public int getEnd() {
        return this.end;
    }

    public void setContent(String str) {
        this.content = str;
    }

    public String getContent() {
        return this.content;
    }
}
