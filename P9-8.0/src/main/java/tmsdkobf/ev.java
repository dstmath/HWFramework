package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class ev extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    public boolean Q = true;
    public int R = 0;
    public int S = 0;
    public int time = 0;

    static {
        boolean z = false;
        if (!ev.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public ev() {
        w(this.time);
        a(this.Q);
        x(this.R);
        y(this.S);
    }

    public void a(boolean z) {
        this.Q = z;
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
        ev evVar = (ev) obj;
        if (d.equals(this.time, evVar.time) && d.a(this.Q, evVar.Q) && d.equals(this.R, evVar.R) && d.equals(this.S, evVar.S)) {
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
        w(jceInputStream.read(this.time, 0, true));
        a(jceInputStream.read(this.Q, 1, true));
        x(jceInputStream.read(this.R, 2, false));
        y(jceInputStream.read(this.S, 3, false));
    }

    public void w(int i) {
        this.time = i;
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.time, 0);
        jceOutputStream.write(this.Q, 1);
        jceOutputStream.write(this.R, 2);
        jceOutputStream.write(this.S, 3);
    }

    public void x(int i) {
        this.R = i;
    }

    public void y(int i) {
        this.S = i;
    }
}
