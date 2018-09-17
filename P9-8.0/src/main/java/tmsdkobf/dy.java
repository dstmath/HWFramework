package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;
import java.util.ArrayList;

public final class dy extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    static dr iq;
    static ev ir;
    static ew is;
    static ArrayList<dx> it;
    public dr im = null;
    public ev in = null;
    public ew io = null;
    public ArrayList<dx> ip = null;

    static {
        boolean z = false;
        if (!dy.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public dy() {
        a(this.im);
        a(this.in);
        a(this.io);
        b(this.ip);
    }

    public void a(dr drVar) {
        this.im = drVar;
    }

    public void a(ev evVar) {
        this.in = evVar;
    }

    public void a(ew ewVar) {
        this.io = ewVar;
    }

    public void b(ArrayList<dx> arrayList) {
        this.ip = arrayList;
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
        dy dyVar = (dy) obj;
        if (d.equals(this.im, dyVar.im) && d.equals(this.in, dyVar.in) && d.equals(this.io, dyVar.io) && d.equals(this.ip, dyVar.ip)) {
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
        if (iq == null) {
            iq = new dr();
        }
        a((dr) jceInputStream.read(iq, 0, true));
        if (ir == null) {
            ir = new ev();
        }
        a((ev) jceInputStream.read(ir, 1, true));
        if (is == null) {
            is = new ew();
        }
        a((ew) jceInputStream.read(is, 2, false));
        if (it == null) {
            it = new ArrayList();
            it.add(new dx());
        }
        b((ArrayList) jceInputStream.read(it, 3, false));
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.im, 0);
        jceOutputStream.write(this.in, 1);
        if (this.io != null) {
            jceOutputStream.write(this.io, 2);
        }
        if (this.ip != null) {
            jceOutputStream.write(this.ip, 3);
        }
    }
}
