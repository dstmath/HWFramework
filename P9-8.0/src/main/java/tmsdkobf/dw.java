package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class dw extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    public long ig = 0;
    public String ih = "";
    public int state = 0;
    public float weight = 0.0f;

    static {
        boolean z = false;
        if (!dw.class.desiredAssertionStatus()) {
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

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        dw dwVar = (dw) obj;
        if (d.a(this.ig, dwVar.ig) && d.equals(this.weight, dwVar.weight) && d.equals(this.ih, dwVar.ih) && d.equals(this.state, dwVar.state)) {
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
        this.ig = jceInputStream.read(this.ig, 0, true);
        this.weight = jceInputStream.read(this.weight, 1, true);
        this.ih = jceInputStream.readString(2, true);
        this.state = jceInputStream.read(this.state, 3, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.ig, 0);
        jceOutputStream.write(this.weight, 1);
        jceOutputStream.write(this.ih, 2);
        jceOutputStream.write(this.state, 3);
    }
}
