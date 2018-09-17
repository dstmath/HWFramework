package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class fe extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    public short lG = (short) 0;
    public String lH = "";

    static {
        boolean z = false;
        if (!fe.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public fe() {
        a(this.lG);
        w(this.lH);
    }

    public void a(short s) {
        this.lG = (short) s;
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
        fe feVar = (fe) obj;
        if (d.a(this.lG, feVar.lG) && d.equals(this.lH, feVar.lH)) {
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

    public void readFrom(JceInputStream jceInputStream) {
        a(jceInputStream.read(this.lG, 0, true));
        w(jceInputStream.readString(1, true));
    }

    public void w(String str) {
        this.lH = str;
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.lG, 0);
        jceOutputStream.write(this.lH, 1);
    }
}
