package com.leisen.wallet.sdk.oma;

public class SmartCardBean {
    public static final int READER_TYPE_ESE = 1;
    public static final int READER_TYPE_SD = 2;
    public static final int READER_TYPE_SIM = 0;
    private String aid;
    private String command;
    private int reader = -1;

    public SmartCardBean(int reader, String aid) {
        this.reader = reader;
        this.aid = aid;
    }

    public SmartCardBean(int reader, String aid, String command) {
        this.reader = reader;
        this.aid = aid;
        this.command = command;
    }

    public int getReader() {
        return this.reader;
    }

    public void setReader(int reader) {
        this.reader = reader;
    }

    public String getReaderName() {
        switch (this.reader) {
            case 0:
                return "SIM";
            case 1:
                return "eSE";
            case 2:
                return "SD";
            default:
                return null;
        }
    }

    public String getAid() {
        return this.aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
