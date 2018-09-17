package tmsdkobf;

import com.qq.taf.jce.JceDisplayer;
import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;
import java.util.HashMap;
import java.util.Map;

public final class ap extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF = (!ap.class.desiredAssertionStatus());
    static Map<Integer, String> bH = new HashMap();
    public Map<Integer, String> bG = null;

    static {
        bH.put(Integer.valueOf(0), "");
    }

    public ap(Map<Integer, String> map) {
        this.bG = map;
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
        new JceDisplayer(stringBuilder, i).display(this.bG, "mapRecord");
    }

    public void displaySimple(StringBuilder stringBuilder, int i) {
        new JceDisplayer(stringBuilder, i).displaySimple(this.bG, false);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return d.equals(this.bG, ((ap) obj).bG);
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
        this.bG = (Map) jceInputStream.read(bH, 0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.bG, 0);
    }
}
