package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class ec extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    public String dl = "";
    public String dm = "";
    public String dy = "";
    public int iN = 0;
    public String iO = "";
    public String iP = "";
    public String iQ = "";
    public String iR = "";
    public String iccid = "";
    public String imsi = "";

    static {
        boolean z = false;
        if (!ec.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public ec() {
        d(this.dl);
        e(this.imsi);
        f(this.dm);
        g(this.iccid);
        h(this.dy);
        e(this.iN);
        i(this.iO);
        j(this.iP);
        k(this.iQ);
        l(this.iR);
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

    public void e(int i) {
        this.iN = i;
    }

    public void e(String str) {
        this.imsi = str;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        ec ecVar = (ec) obj;
        if (d.equals(this.dl, ecVar.dl) && d.equals(this.imsi, ecVar.imsi) && d.equals(this.dm, ecVar.dm) && d.equals(this.iccid, ecVar.iccid) && d.equals(this.dy, ecVar.dy) && d.equals(this.iN, ecVar.iN) && d.equals(this.iO, ecVar.iO) && d.equals(this.iP, ecVar.iP) && d.equals(this.iQ, ecVar.iQ) && d.equals(this.iR, ecVar.iR)) {
            z = true;
        }
        return z;
    }

    public void f(String str) {
        this.dm = str;
    }

    public void g(String str) {
        this.iccid = str;
    }

    public void h(String str) {
        this.dy = str;
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void i(String str) {
        this.iO = str;
    }

    public void j(String str) {
        this.iP = str;
    }

    public void k(String str) {
        this.iQ = str;
    }

    public void l(String str) {
        this.iR = str;
    }

    public void readFrom(JceInputStream jceInputStream) {
        d(jceInputStream.readString(0, true));
        e(jceInputStream.readString(1, false));
        f(jceInputStream.readString(2, false));
        g(jceInputStream.readString(3, false));
        h(jceInputStream.readString(4, false));
        e(jceInputStream.read(this.iN, 5, false));
        i(jceInputStream.readString(6, false));
        j(jceInputStream.readString(7, false));
        k(jceInputStream.readString(8, false));
        l(jceInputStream.readString(9, false));
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.dl, 0);
        if (this.imsi != null) {
            jceOutputStream.write(this.imsi, 1);
        }
        if (this.dm != null) {
            jceOutputStream.write(this.dm, 2);
        }
        if (this.iccid != null) {
            jceOutputStream.write(this.iccid, 3);
        }
        if (this.dy != null) {
            jceOutputStream.write(this.dy, 4);
        }
        jceOutputStream.write(this.iN, 5);
        if (this.iO != null) {
            jceOutputStream.write(this.iO, 6);
        }
        if (this.iP != null) {
            jceOutputStream.write(this.iP, 7);
        }
        if (this.iQ != null) {
            jceOutputStream.write(this.iQ, 8);
        }
        if (this.iR != null) {
            jceOutputStream.write(this.iR, 9);
        }
    }
}
