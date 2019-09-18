package com.leisen.wallet.sdk.oma;

public class SmartCardBean {
    public static final int READER_TYPE_AUTO = -1;
    public static final int READER_TYPE_ESE = 0;
    public static final int READER_TYPE_ESE1 = 1;
    public static final int READER_TYPE_ESE2 = 2;
    private String aid;
    private String command;
    private int reader = -1;

    public SmartCardBean(int reader2, String aid2) {
        this.reader = reader2;
        this.aid = aid2;
    }

    public SmartCardBean(int reader2, String aid2, String command2) {
        this.reader = reader2;
        this.aid = aid2;
        this.command = command2;
    }

    public int getReader() {
        return this.reader;
    }

    public void setReader(int reader2) {
        this.reader = reader2;
    }

    public String getReaderName() {
        switch (this.reader) {
            case -1:
                return "AUTO";
            case 0:
                return "eSE";
            case 1:
                return "eSE1";
            case 2:
                return "eSE2";
            default:
                return null;
        }
    }

    public String getAid() {
        return this.aid;
    }

    public void setAid(String aid2) {
        this.aid = aid2;
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command2) {
        this.command = command2;
    }
}
