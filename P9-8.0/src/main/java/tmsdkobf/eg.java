package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class eg extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    public String I = "";

    static {
        boolean z = false;
        if (!eg.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public eg() {
        m(this.I);
    }

    public String b() {
        return this.I;
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
        if (obj == null) {
            return false;
        }
        return d.equals(this.I, ((eg) obj).I);
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void m(String str) {
        this.I = str;
    }

    public void readFrom(JceInputStream jceInputStream) {
        m(jceInputStream.readString(0, true));
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.I, 0);
    }
}
