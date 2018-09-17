package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class ce extends JceStruct {
    static byte[] eF = new byte[1];
    static cd eP = new cd();
    public int bz = 0;
    public byte[] data = null;
    public int eB = 0;
    public int eC = 0;
    public int eE = 0;
    public cd eO = null;
    public int ey = 0;
    public int ez = 0;

    static {
        eF[0] = (byte) 0;
    }

    public JceStruct newInit() {
        return new ce();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.bz = jceInputStream.read(this.bz, 0, true);
        this.ey = jceInputStream.read(this.ey, 1, false);
        this.ez = jceInputStream.read(this.ez, 2, false);
        this.eB = jceInputStream.read(this.eB, 3, false);
        this.eC = jceInputStream.read(this.eC, 4, false);
        this.data = jceInputStream.read(eF, 5, false);
        this.eO = (cd) jceInputStream.read(eP, 6, false);
        this.eE = jceInputStream.read(this.eE, 7, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.bz, 0);
        if (this.ey != 0) {
            jceOutputStream.write(this.ey, 1);
        }
        if (this.ez != 0) {
            jceOutputStream.write(this.ez, 2);
        }
        jceOutputStream.write(this.eB, 3);
        if (this.eC != 0) {
            jceOutputStream.write(this.eC, 4);
        }
        if (this.data != null) {
            jceOutputStream.write(this.data, 5);
        }
        if (this.eO != null) {
            jceOutputStream.write(this.eO, 6);
        }
        if (this.eE != 0) {
            jceOutputStream.write(this.eE, 7);
        }
    }
}
