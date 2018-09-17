package tmsdkobf;

import com.qq.taf.jce.JceDisplayer;
import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;
import java.util.ArrayList;

public final class ao extends JceStruct implements Cloneable {
    static ArrayList<ap> bE = new ArrayList();
    static final /* synthetic */ boolean bF;
    public int bC = 0;
    public ArrayList<ap> bD = null;

    static {
        boolean z = false;
        if (!ao.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
        bE.add(new ap());
    }

    public ao(int i, ArrayList<ap> arrayList) {
        this.bC = i;
        this.bD = arrayList;
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
        jceDisplayer.display(this.bC, "reportID");
        jceDisplayer.display(this.bD, "vecReportInfo");
    }

    public void displaySimple(StringBuilder stringBuilder, int i) {
        JceDisplayer jceDisplayer = new JceDisplayer(stringBuilder, i);
        jceDisplayer.displaySimple(this.bC, true);
        jceDisplayer.displaySimple(this.bD, false);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        ao aoVar = (ao) obj;
        if (d.equals(this.bC, aoVar.bC) && d.equals(this.bD, aoVar.bD)) {
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
        this.bC = jceInputStream.read(this.bC, 0, true);
        this.bD = (ArrayList) jceInputStream.read(bE, 1, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.bC, 0);
        jceOutputStream.write(this.bD, 1);
    }
}
