package defpackage;

import android.text.TextUtils;

/* renamed from: ak */
public class ak implements Comparable {
    private long bj;
    private boolean bk;

    private ak() {
    }

    public int a(ak akVar) {
        return (int) ((bE() - akVar.bE()) / 1000);
    }

    public long bE() {
        return this.bj;
    }

    public boolean bF() {
        return this.bk;
    }

    public /* synthetic */ int compareTo(Object obj) {
        return a((ak) obj);
    }

    public boolean equals(Object obj) {
        return this == obj ? true : obj == null ? false : getClass() != obj.getClass() ? false : !(obj instanceof ak) ? false : this.bk == ((ak) obj).bk && this.bj == ((ak) obj).bj;
    }

    public int hashCode() {
        return (this.bk ? 1 : 0) + ((((int) (this.bj ^ (this.bj >>> 32))) + 527) * 31);
    }

    public void j(long j) {
        this.bj = j;
    }

    public void j(boolean z) {
        this.bk = z;
    }

    public boolean load(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        try {
            String[] split = str.split(";");
            if (split == null || split.length < 2) {
                aw.e("PushLog2828", "load connectinfo " + str + " error");
                return false;
            }
            this.bj = Long.parseLong(split[0]);
            this.bk = Boolean.parseBoolean(split[1]);
            return true;
        } catch (Throwable e) {
            aw.d("PushLog2828", "load connectinfo " + str + " error:" + e.toString(), e);
            return false;
        }
    }

    public String toString() {
        if (this.bj <= 0) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.bj).append(";").append(this.bk);
        return stringBuffer.toString();
    }
}
