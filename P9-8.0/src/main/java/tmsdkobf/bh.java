package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class bh extends JceStruct {
    static ArrayList<be> cE = new ArrayList();
    public ArrayList<be> cD = null;
    public int cj = 0;
    public String imsi = "";

    static {
        cE.add(new be());
    }

    public JceStruct newInit() {
        return new bh();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.cD = (ArrayList) jceInputStream.read(cE, 0, true);
        this.imsi = jceInputStream.readString(1, false);
        this.cj = jceInputStream.read(this.cj, 2, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.cD, 0);
        if (this.imsi != null) {
            jceOutputStream.write(this.imsi, 1);
        }
        if (this.cj != 0) {
            jceOutputStream.write(this.cj, 2);
        }
    }
}
