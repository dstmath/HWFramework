package com.huawei.android.pushagent.model.flowcontrol;

import android.text.TextUtils;
import com.huawei.android.pushagent.utils.d.c;

class b implements Comparable<b> {
    private boolean dw;
    private long dx;

    /* synthetic */ b(b bVar) {
        this();
    }

    private b() {
    }

    public boolean me(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        try {
            String[] split = str.split(";");
            if (split.length < 2) {
                c.sf("PushLog2951", "load connectinfo " + str + " error");
                return false;
            }
            this.dx = Long.parseLong(split[0]);
            this.dw = Boolean.parseBoolean(split[1]);
            return true;
        } catch (Throwable e) {
            c.se("PushLog2951", "load connectinfo " + str + " error:" + e.toString(), e);
            return false;
        }
    }

    public long mc() {
        return this.dx;
    }

    public void mg(long j) {
        this.dx = j;
    }

    public boolean md() {
        return this.dw;
    }

    public void mf(boolean z) {
        this.dw = z;
    }

    public String toString() {
        if (this.dx <= 0) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.dx).append(";").append(this.dw);
        return stringBuffer.toString();
    }

    /* renamed from: mb */
    public int compareTo(b bVar) {
        return (int) ((mc() - bVar.mc()) / 1000);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj != null && getClass() == obj.getClass() && (obj instanceof b) && this.dw == ((b) obj).dw && this.dx == ((b) obj).dx;
    }

    public int hashCode() {
        return (this.dw ? 1 : 0) + ((((int) (this.dx ^ (this.dx >>> 32))) + 527) * 31);
    }
}
