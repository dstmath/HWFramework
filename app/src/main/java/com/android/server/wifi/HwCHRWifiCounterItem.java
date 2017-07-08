package com.android.server.wifi;

public class HwCHRWifiCounterItem {
    private static final int TYPE_INT = 1;
    private static final int TYPE_STR = 2;
    private static final int TYPE_UNINTIAL = 0;
    protected String str;
    protected String tag;
    private int type;
    protected int v;

    public HwCHRWifiCounterItem() {
        this.type = 0;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setV(int v) {
        this.v = v;
        this.type = TYPE_INT;
    }

    public void setStr(String str) {
        this.str = str;
        this.type = TYPE_STR;
    }

    public String toString() {
        if (this.type == 0) {
            return "";
        }
        if (this.type == TYPE_INT) {
            return this.tag + "=" + this.v;
        }
        return this.tag + "=" + this.str;
    }

    public void parseString(String line) {
        if (line != null && !line.equalsIgnoreCase("")) {
            String[] item = line.split("=");
            setTag(item[0]);
            if (isNumberic(item[TYPE_INT])) {
                setV(Integer.parseInt(item[TYPE_INT]));
            } else {
                setStr(item[TYPE_INT]);
            }
        }
    }

    public boolean isNumberic(String str) {
        if (str == null || str.equals("")) {
            return false;
        }
        str = str.trim();
        for (int i = 0; i < str.length(); i += TYPE_INT) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public String getTag() {
        return this.tag;
    }

    public int getV() {
        return this.v;
    }

    public String getStr() {
        return this.str;
    }

    public boolean match(String str) {
        return true;
    }

    public void incr() {
        if (TYPE_INT == this.type) {
            this.v += TYPE_INT;
        }
    }
}
