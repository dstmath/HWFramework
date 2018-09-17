package tmsdkobf;

import com.qq.taf.jce.JceDisplayer;
import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;
import java.util.ArrayList;

public final class ea extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    static ArrayList<dz> iD;
    public ArrayList<dz> iC = null;

    static {
        boolean z = false;
        if (!ea.class.desiredAssertionStatus()) {
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
        new JceDisplayer(stringBuilder, i).display(this.iC, "vctCommList");
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return d.equals(this.iC, ((ea) obj).iC);
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
        if (iD == null) {
            iD = new ArrayList();
            iD.add(new dz());
        }
        this.iC = (ArrayList) jceInputStream.read(iD, 0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.iC, 0);
    }
}
