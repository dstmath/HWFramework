package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class fd extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    public int ay = 0;
    public int lF = 2;
    public int timestamp = 0;
    public int version = 0;

    static {
        boolean z = false;
        if (!fd.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
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

    public int e() {
        return this.timestamp;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        fd fdVar = (fd) obj;
        if (d.equals(this.timestamp, fdVar.timestamp) && d.equals(this.version, fdVar.version) && d.equals(this.lF, fdVar.lF) && d.equals(this.ay, fdVar.ay)) {
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
        this.timestamp = jceInputStream.read(this.timestamp, 0, true);
        this.version = jceInputStream.read(this.version, 1, true);
        this.lF = jceInputStream.read(this.lF, 2, false);
        this.ay = jceInputStream.read(this.ay, 3, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.timestamp, 0);
        jceOutputStream.write(this.version, 1);
        jceOutputStream.write(this.lF, 2);
        jceOutputStream.write(this.ay, 3);
    }
}
