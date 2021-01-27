package com.huawei.i18n.tmr.phonenumber;

public class MatchedNumberInfo {
    private int begin;
    private String content;
    private int end;

    public void setBegin(int begin2) {
        this.begin = begin2;
    }

    public int getBegin() {
        return this.begin;
    }

    public void setEnd(int end2) {
        this.end = end2;
    }

    public int getEnd() {
        return this.end;
    }

    public void setContent(String content2) {
        this.content = content2;
    }

    public String getContent() {
        return this.content;
    }
}
