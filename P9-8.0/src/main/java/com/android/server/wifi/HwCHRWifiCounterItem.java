package com.android.server.wifi;

public class HwCHRWifiCounterItem {
    private static final int TYPE_INT = 1;
    private static final int TYPE_STR = 2;
    private static final int TYPE_UNINTIAL = 0;
    protected String str;
    protected String tag;
    private int type = 0;
    protected int v;

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setV(int v) {
        this.v = v;
        this.type = 1;
    }

    public void setStr(String str) {
        this.str = str;
        this.type = 2;
    }

    public String toString() {
        if (this.type == 0) {
            return "";
        }
        if (this.type == 1) {
            return this.tag + "=" + this.v;
        }
        return this.tag + "=" + this.str;
    }

    public void parseString(String line) {
        if (line != null && !line.equalsIgnoreCase("")) {
            String[] item = line.split("=");
            setTag(item[0]);
            if (isNumberic(item[1])) {
                setV(Integer.parseInt(item[1]));
            } else {
                setStr(item[1]);
            }
        }
    }

    public boolean isNumberic(String str) {
        if (str == null || str.equals("")) {
            return false;
        }
        str = str.trim();
        int strLength = str.length();
        for (int i = 0; i < strLength; i++) {
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
        if (1 == this.type) {
            this.v++;
        }
    }
}
