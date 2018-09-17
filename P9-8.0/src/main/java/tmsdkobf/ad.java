package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class ad extends JceStruct {
    static byte[] aI = new byte[1];
    public int aE = 0;
    public byte[] aF = null;
    public int aG = 0;
    public int aH = 0;
    public int timestamp = 0;
    public int version = 0;

    static {
        aI[0] = (byte) 0;
    }

    public JceStruct newInit() {
        return new ad();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.aE = jceInputStream.read(this.aE, 0, true);
        this.aF = jceInputStream.read(aI, 1, true);
        this.timestamp = jceInputStream.read(this.timestamp, 2, true);
        this.aG = jceInputStream.read(this.aG, 3, false);
        this.aH = jceInputStream.read(this.aH, 4, false);
        this.version = jceInputStream.read(this.version, 5, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.aE, 0);
        jceOutputStream.write(this.aF, 1);
        jceOutputStream.write(this.timestamp, 2);
        if (this.aG != 0) {
            jceOutputStream.write(this.aG, 3);
        }
        if (this.aH != 0) {
            jceOutputStream.write(this.aH, 4);
        }
        if (this.version != 0) {
            jceOutputStream.write(this.version, 5);
        }
    }
}
