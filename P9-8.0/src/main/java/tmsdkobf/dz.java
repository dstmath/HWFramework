package tmsdkobf;

import com.qq.taf.jce.JceDisplayer;
import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class dz extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    public String iA = "";
    public String iB = "";
    public String iu = "";
    public String iv = "";
    public String iw = "";
    public String ix = "";
    public String iy = "";
    public String iz = "";

    static {
        boolean z = false;
        if (!dz.class.desiredAssertionStatus()) {
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

    public void display(StringBuilder stringBuilder, int i) {
        JceDisplayer jceDisplayer = new JceDisplayer(stringBuilder, i);
        jceDisplayer.display(this.iu, "data1");
        jceDisplayer.display(this.iv, "data2");
        jceDisplayer.display(this.iw, "data3");
        jceDisplayer.display(this.ix, "data4");
        jceDisplayer.display(this.iy, "data5");
        jceDisplayer.display(this.iz, "data6");
        jceDisplayer.display(this.iA, "data7");
        jceDisplayer.display(this.iB, "data8");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        dz dzVar = (dz) obj;
        if (d.equals(this.iu, dzVar.iu) && d.equals(this.iv, dzVar.iv) && d.equals(this.iw, dzVar.iw) && d.equals(this.ix, dzVar.ix) && d.equals(this.iy, dzVar.iy) && d.equals(this.iz, dzVar.iz) && d.equals(this.iA, dzVar.iA) && d.equals(this.iB, dzVar.iB)) {
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
        this.iu = jceInputStream.readString(0, false);
        this.iv = jceInputStream.readString(1, false);
        this.iw = jceInputStream.readString(3, false);
        this.ix = jceInputStream.readString(4, false);
        this.iy = jceInputStream.readString(5, false);
        this.iz = jceInputStream.readString(6, false);
        this.iA = jceInputStream.readString(7, false);
        this.iB = jceInputStream.readString(8, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.iu != null) {
            jceOutputStream.write(this.iu, 0);
        }
        if (this.iv != null) {
            jceOutputStream.write(this.iv, 1);
        }
        if (this.iw != null) {
            jceOutputStream.write(this.iw, 3);
        }
        if (this.ix != null) {
            jceOutputStream.write(this.ix, 4);
        }
        if (this.iy != null) {
            jceOutputStream.write(this.iy, 5);
        }
        if (this.iz != null) {
            jceOutputStream.write(this.iz, 6);
        }
        if (this.iA != null) {
            jceOutputStream.write(this.iA, 7);
        }
        if (this.iB != null) {
            jceOutputStream.write(this.iB, 8);
        }
    }
}
