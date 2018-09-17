package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class bw extends JceStruct {
    static byte[] eF = new byte[1];
    static bv eG = new bv();
    public int bz = 0;
    public byte[] data = null;
    public long eA = 0;
    public int eB = 0;
    public int eC = 0;
    public bv eD = null;
    public int eE = 0;
    public int ey = 0;
    public int ez = 0;

    static {
        eF[0] = (byte) 0;
    }

    public JceStruct newInit() {
        return new bw();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.bz = jceInputStream.read(this.bz, 0, true);
        this.ey = jceInputStream.read(this.ey, 1, false);
        this.ez = jceInputStream.read(this.ez, 2, false);
        this.data = jceInputStream.read(eF, 3, false);
        this.eA = jceInputStream.read(this.eA, 4, false);
        this.eB = jceInputStream.read(this.eB, 5, false);
        this.eC = jceInputStream.read(this.eC, 6, false);
        this.eD = (bv) jceInputStream.read(eG, 7, false);
        this.eE = jceInputStream.read(this.eE, 8, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.bz, 0);
        if (this.ey != 0) {
            jceOutputStream.write(this.ey, 1);
        }
        if (this.ez != 0) {
            jceOutputStream.write(this.ez, 2);
        }
        if (this.data != null) {
            jceOutputStream.write(this.data, 3);
        }
        if (this.eA != 0) {
            jceOutputStream.write(this.eA, 4);
        }
        if (this.eB != 0) {
            jceOutputStream.write(this.eB, 5);
        }
        if (this.eC != 0) {
            jceOutputStream.write(this.eC, 6);
        }
        if (this.eD != null) {
            jceOutputStream.write(this.eD, 7);
        }
        if (this.eE != 0) {
            jceOutputStream.write(this.eE, 8);
        }
    }
}
