package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class do extends JceStruct {
    static ArrayList<dl> gD = new ArrayList();
    static Map<Integer, dp> hF = new HashMap();
    static Map<Integer, de> hG = new HashMap();
    static Map<Integer, dh> hJ = new HashMap();
    public ArrayList<dl> gC = null;
    public Map<Integer, dp> hC = null;
    public Map<Integer, de> hD = null;
    public int hE = 0;
    public Map<Integer, dh> hH = null;
    public int hI = 0;

    static {
        gD.add(new dl());
        hF.put(Integer.valueOf(0), new dp());
        hG.put(Integer.valueOf(0), new de());
        hJ.put(Integer.valueOf(0), new dh());
    }

    public JceStruct newInit() {
        return new do();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.gC = (ArrayList) jceInputStream.read(gD, 0, false);
        this.hC = (Map) jceInputStream.read(hF, 1, false);
        this.hD = (Map) jceInputStream.read(hG, 2, false);
        this.hE = jceInputStream.read(this.hE, 3, false);
        this.hH = (Map) jceInputStream.read(hJ, 4, false);
        this.hI = jceInputStream.read(this.hI, 5, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.gC != null) {
            jceOutputStream.write(this.gC, 0);
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
        if (this.hH != null) {
            jceOutputStream.write(this.hH, 4);
        }
        jceOutputStream.write(this.hI, 5);
    }
}
