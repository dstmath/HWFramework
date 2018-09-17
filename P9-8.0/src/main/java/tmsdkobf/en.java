package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class en extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    public int kt = 0;
    public int ku = 0;

    static {
        boolean z = false;
        if (!en.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public en() {
        k(this.kt);
        l(this.ku);
    }

    public en(int i, int i2) {
        k(i);
        l(i2);
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
        en enVar = (en) obj;
        if (d.equals(this.kt, enVar.kt) && d.equals(this.ku, enVar.ku)) {
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

    public void k(int i) {
        this.kt = i;
    }

    public void l(int i) {
        this.ku = i;
    }

    public void readFrom(JceInputStream jceInputStream) {
        k(jceInputStream.read(this.kt, 0, true));
        l(jceInputStream.read(this.ku, 1, true));
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.kt, 0);
        jceOutputStream.write(this.ku, 1);
    }
}
