package tmsdkobf;

import com.qq.taf.jce.JceDisplayer;
import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;
import java.util.ArrayList;

public final class ei extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    static ArrayList<String> kk;
    public String ki = "";
    public ArrayList<String> kj = null;

    static {
        boolean z = false;
        if (!ei.class.desiredAssertionStatus()) {
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
        jceDisplayer.display(this.ki, "typeName");
        jceDisplayer.display(this.kj, "keySet");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        ei eiVar = (ei) obj;
        if (d.equals(this.ki, eiVar.ki) && d.equals(this.kj, eiVar.kj)) {
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
        this.ki = jceInputStream.readString(0, true);
        if (kk == null) {
            kk = new ArrayList();
            kk.add("");
        }
        this.kj = (ArrayList) jceInputStream.read(kk, 1, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.ki, 0);
        if (this.kj != null) {
            jceOutputStream.write(this.kj, 1);
        }
    }
}
