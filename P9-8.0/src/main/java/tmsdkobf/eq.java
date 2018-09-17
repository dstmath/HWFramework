package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class eq extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    public int L = 0;
    public String dl = "";
    public String dn = "";
    public String do = "";
    public String dq = "";
    public String imsi = "";
    public int kA = 0;
    public int kB = 0;
    public String kz = "";
    public String version = "";

    static {
        boolean z = false;
        if (!eq.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public eq() {
        d(this.dl);
        o(this.dn);
        setPhone(this.do);
        p(this.kz);
        q(this.dq);
        e(this.imsi);
        r(this.version);
        m(this.L);
        n(this.kA);
        o(this.kB);
    }

    public Object clone() {
        Object obj = null;
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            if (bF) {
                return obj;
            }
            throw new AssertionError();
        }
    }

    public void d(String str) {
        this.dl = str;
    }

    public void e(String str) {
        this.imsi = str;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        eq eqVar = (eq) obj;
        if (d.equals(this.dl, eqVar.dl) && d.equals(this.dn, eqVar.dn) && d.equals(this.do, eqVar.do) && d.equals(this.kz, eqVar.kz) && d.equals(this.dq, eqVar.dq) && d.equals(this.imsi, eqVar.imsi) && d.equals(this.version, eqVar.version) && d.equals(this.L, eqVar.L) && d.equals(this.kA, eqVar.kA) && d.equals(this.kB, eqVar.kB)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void m(int i) {
        this.L = i;
    }

    public void n(int i) {
        this.kA = i;
    }

    public void o(int i) {
        this.kB = i;
    }

    public void o(String str) {
        this.dn = str;
    }

    public void p(String str) {
        this.kz = str;
    }

    public void q(String str) {
        this.dq = str;
    }

    public void r(String str) {
        this.version = str;
    }

    public void readFrom(JceInputStream jceInputStream) {
        d(jceInputStream.readString(0, true));
        o(jceInputStream.readString(1, false));
        setPhone(jceInputStream.readString(2, false));
        p(jceInputStream.readString(3, false));
        q(jceInputStream.readString(4, false));
        e(jceInputStream.readString(5, false));
        r(jceInputStream.readString(6, false));
        m(jceInputStream.read(this.L, 7, false));
        n(jceInputStream.read(this.kA, 8, false));
        o(jceInputStream.read(this.kB, 9, false));
    }

    public void setPhone(String str) {
        this.do = str;
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.dl, 0);
        if (this.dn != null) {
            jceOutputStream.write(this.dn, 1);
        }
        if (this.do != null) {
            jceOutputStream.write(this.do, 2);
        }
        if (this.kz != null) {
            jceOutputStream.write(this.kz, 3);
        }
        if (this.dq != null) {
            jceOutputStream.write(this.dq, 4);
        }
        if (this.imsi != null) {
            jceOutputStream.write(this.imsi, 5);
        }
        if (this.version != null) {
            jceOutputStream.write(this.version, 6);
        }
        jceOutputStream.write(this.L, 7);
        jceOutputStream.write(this.kA, 8);
        jceOutputStream.write(this.kB, 9);
    }
}
