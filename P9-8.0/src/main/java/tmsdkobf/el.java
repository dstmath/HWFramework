package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class el extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    public int ko = 0;
    public int kp = 0;
    public int kq = 0;

    static {
        boolean z = false;
        if (!el.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public el() {
        h(this.ko);
        i(this.kp);
        j(this.kq);
    }

    public el(int i, int i2, int i3) {
        h(i);
        i(i2);
        j(i3);
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
        el elVar = (el) obj;
        if (d.equals(this.ko, elVar.ko) && d.equals(this.kp, elVar.kp) && d.equals(this.kq, elVar.kq)) {
            z = true;
        }
        return z;
    }

    public void h(int i) {
        this.ko = i;
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void i(int i) {
        this.kp = i;
    }

    public void j(int i) {
        this.kq = i;
    }

    public void readFrom(JceInputStream jceInputStream) {
        h(jceInputStream.read(this.ko, 1, true));
        i(jceInputStream.read(this.kp, 2, true));
        j(jceInputStream.read(this.kq, 3, true));
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.ko, 1);
        jceOutputStream.write(this.kp, 2);
        jceOutputStream.write(this.kq, 3);
    }
}
