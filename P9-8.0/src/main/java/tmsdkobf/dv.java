package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;
import java.util.ArrayList;

public final class dv extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    static ArrayList<et> if;
    public int dp = ed.iT.value();
    public int ib = 0;
    public String ic = "";
    public String id = "";
    public ArrayList<et> ie = null;

    static {
        boolean z = false;
        if (!dv.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public dv() {
        b(this.id);
        a(this.dp);
        b(this.ib);
        c(this.ic);
        a(this.ie);
    }

    public void a(int i) {
        this.dp = i;
    }

    public void a(ArrayList<et> arrayList) {
        this.ie = arrayList;
    }

    public void b(int i) {
        this.ib = i;
    }

    public void b(String str) {
        this.id = str;
    }

    public void c(String str) {
        this.ic = str;
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
        dv dvVar = (dv) obj;
        if (d.equals(this.id, dvVar.id) && d.equals(this.dp, dvVar.dp) && d.equals(this.ib, dvVar.ib) && d.equals(this.ic, dvVar.ic) && d.equals(this.ie, dvVar.ie)) {
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
        b(jceInputStream.readString(0, true));
        a(jceInputStream.read(this.dp, 1, false));
        b(jceInputStream.read(this.ib, 2, false));
        c(jceInputStream.readString(3, false));
        if (if == null) {
            if = new ArrayList();
            if.add(new et());
        }
        a((ArrayList) jceInputStream.read(if, 4, false));
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.id, 0);
        jceOutputStream.write(this.dp, 1);
        jceOutputStream.write(this.ib, 2);
        if (this.ic != null) {
            jceOutputStream.write(this.ic, 3);
        }
        if (this.ie != null) {
            jceOutputStream.write(this.ie, 4);
        }
    }
}
