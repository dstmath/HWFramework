package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class bc extends JceStruct {
    static ArrayList<bf> ct = new ArrayList();
    public int cj = 0;
    public ArrayList<bf> cs = null;
    public String imsi = "";

    static {
        ct.add(new bf());
    }

    public JceStruct newInit() {
        return new bc();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.cs = (ArrayList) jceInputStream.read(ct, 0, true);
        this.imsi = jceInputStream.readString(1, false);
        this.cj = jceInputStream.read(this.cj, 2, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.cs, 0);
        if (this.imsi != null) {
            jceOutputStream.write(this.imsi, 1);
        }
        if (this.cj != 0) {
            jceOutputStream.write(this.cj, 2);
        }
    }
}
