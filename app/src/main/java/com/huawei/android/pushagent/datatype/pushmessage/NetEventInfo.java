package com.huawei.android.pushagent.datatype.pushmessage;

import defpackage.au;
import java.io.Serializable;

public class NetEventInfo implements Serializable {
    private byte netEvent;
    private long netEventTime;
    private byte netType;

    public long aG() {
        return this.netEventTime;
    }

    public byte aH() {
        return this.netType;
    }

    public byte aI() {
        return this.netEvent;
    }

    public void b(byte b) {
        this.netType = b;
    }

    public void c(byte b) {
        this.netEvent = b;
    }

    public void f(long j) {
        this.netEventTime = j;
    }

    public String toString() {
        return new StringBuffer(getClass().getSimpleName()).append(" netEventTime:").append(au.a(this.netEventTime, "yyyy-MM-dd HH:mm:ss SSS")).append(" netType:").append(this.netType).append(" netEvent:").append(this.netEvent).toString();
    }
}
