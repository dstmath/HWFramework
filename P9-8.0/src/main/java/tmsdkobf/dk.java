package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class dk extends JceStruct {
    static byte[] hb = new byte[1];
    static byte[] hc = new byte[1];
    static Map<Integer, ArrayList<byte[]>> hu = new HashMap();
    public int eB = 0;
    public int gI = 0;
    public byte[] gJ = null;
    public String gK = "";
    public byte[] gL = null;
    public long gM = 0;
    public Map<Integer, ArrayList<byte[]>> hs = null;
    public int ht = 0;

    static {
        hb[0] = (byte) 0;
        hc[0] = (byte) 0;
        Integer valueOf = Integer.valueOf(0);
        ArrayList arrayList = new ArrayList();
        byte[] bArr = new byte[1];
        bArr[0] = (byte) 0;
        arrayList.add(bArr);
        hu.put(valueOf, arrayList);
    }

    public JceStruct newInit() {
        return new dk();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.gI = jceInputStream.read(this.gI, 0, true);
        this.gJ = jceInputStream.read(hb, 1, false);
        this.gK = jceInputStream.readString(2, false);
        this.gL = jceInputStream.read(hc, 3, false);
        this.gM = jceInputStream.read(this.gM, 4, false);
        this.hs = (Map) jceInputStream.read(hu, 5, false);
        this.ht = jceInputStream.read(this.ht, 6, false);
        this.eB = jceInputStream.read(this.eB, 7, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.gI, 0);
        if (this.gJ != null) {
            jceOutputStream.write(this.gJ, 1);
        }
        if (this.gK != null) {
            jceOutputStream.write(this.gK, 2);
        }
        if (this.gL != null) {
            jceOutputStream.write(this.gL, 3);
        }
        if (this.gM != 0) {
            jceOutputStream.write(this.gM, 4);
        }
        if (this.hs != null) {
            jceOutputStream.write(this.hs, 5);
        }
        if (this.ht != 0) {
            jceOutputStream.write(this.ht, 6);
        }
        if (this.eB != 0) {
            jceOutputStream.write(this.eB, 7);
        }
    }
}
