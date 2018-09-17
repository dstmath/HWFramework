package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;
import java.util.ArrayList;

public final class er extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    static ArrayList<dy> kF;
    public ArrayList<dy> kC = null;
    public int kD = 0;
    public String kE = "";

    static {
        boolean z = false;
        if (!er.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public er() {
        c(this.kC);
        p(this.kD);
        s(this.kE);
    }

    public String c() {
        return this.kE;
    }

    public void c(ArrayList<dy> arrayList) {
        this.kC = arrayList;
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

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        er erVar = (er) obj;
        if (d.equals(this.kC, erVar.kC) && d.equals(this.kD, erVar.kD) && d.equals(this.kE, erVar.kE)) {
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

    public void p(int i) {
        this.kD = i;
    }

    public void readFrom(JceInputStream jceInputStream) {
        if (kF == null) {
            kF = new ArrayList();
            kF.add(new dy());
        }
        c((ArrayList) jceInputStream.read(kF, 1, true));
        p(jceInputStream.read(this.kD, 2, true));
        s(jceInputStream.readString(3, false));
    }

    public void s(String str) {
        this.kE = str;
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.kC, 1);
        jceOutputStream.write(this.kD, 2);
        if (this.kE != null) {
            jceOutputStream.write(this.kE, 3);
        }
    }
}
