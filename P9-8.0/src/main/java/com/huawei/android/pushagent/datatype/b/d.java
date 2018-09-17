package com.huawei.android.pushagent.datatype.b;

public class d {
    private String hd;
    private int he;
    private boolean hf;

    public d(String str, int i, boolean z) {
        this.hd = str;
        this.he = i;
        this.hf = z;
    }

    public String toString() {
        return new StringBuffer().append("ip:").append(this.hd).append(" port:").append(this.he).append(" useProxy:").append(this.hf).toString();
    }

    public String xd() {
        return this.hd;
    }

    public void xg(String str) {
        this.hd = str;
    }

    public int xe() {
        return this.he;
    }

    public void xh(int i) {
        this.he = i;
    }

    public boolean xf() {
        return this.hf;
    }
}
