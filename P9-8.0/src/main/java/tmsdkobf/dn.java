package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class dn extends JceStruct {
    static ArrayList<dm> gB = new ArrayList();
    static Map<Integer, dp> hF = new HashMap();
    static Map<Integer, de> hG = new HashMap();
    public ArrayList<dm> gz = null;
    public Map<Integer, dp> hC = null;
    public Map<Integer, de> hD = null;
    public int hE = 0;

    static {
        gB.add(new dm());
        hF.put(Integer.valueOf(0), new dp());
        hG.put(Integer.valueOf(0), new de());
    }

    public JceStruct newInit() {
        return new dn();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.gz = (ArrayList) jceInputStream.read(gB, 0, false);
        this.hC = (Map) jceInputStream.read(hF, 1, false);
        this.hD = (Map) jceInputStream.read(hG, 2, false);
        this.hE = jceInputStream.read(this.hE, 3, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.gz != null) {
            jceOutputStream.write(this.gz, 0);
        }
        if (this.hC != null) {
            jceOutputStream.write(this.hC, 1);
        }
        if (this.hD != null) {
            jceOutputStream.write(this.hD, 2);
        }
        if (this.hE != 0) {
            jceOutputStream.write(this.hE, 3);
        }
    }
}
