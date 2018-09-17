package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class ex extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    public String dv = "";

    static {
        boolean z = false;
        if (!ex.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public ex() {
        v(this.dv);
    }

    public ex(String str) {
        v(str);
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
        return d.equals(this.dv, ((ex) obj).dv);
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
        v(jceInputStream.readString(0, false));
    }

    public void v(String str) {
        this.dv = str;
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.dv != null) {
            jceOutputStream.write(this.dv, 0);
        }
    }
}
