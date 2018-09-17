package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class eh extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    static ex kh;
    public String kf = "";
    public ex kg = null;

    static {
        boolean z = false;
        if (!eh.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public eh() {
        n(this.kf);
        a(this.kg);
    }

    public void a(ex exVar) {
        this.kg = exVar;
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
        eh ehVar = (eh) obj;
        if (d.equals(this.kf, ehVar.kf) && d.equals(this.kg, ehVar.kg)) {
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

    public void n(String str) {
        this.kf = str;
    }

    public void readFrom(JceInputStream jceInputStream) {
        n(jceInputStream.readString(0, true));
        if (kh == null) {
            kh = new ex();
        }
        a((ex) jceInputStream.read(kh, 1, false));
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.kf, 0);
        if (this.kg != null) {
            jceOutputStream.write(this.kg, 1);
        }
    }
}
