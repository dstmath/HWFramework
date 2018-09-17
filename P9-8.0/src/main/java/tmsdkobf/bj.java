package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class bj extends JceStruct {
    static byte[] cI = new byte[1];
    static byte[] cJ = new byte[1];
    public byte[] cF = null;
    public byte[] cG = null;
    public float cH = -1.0f;

    static {
        cI[0] = (byte) 0;
        cJ[0] = (byte) 0;
    }

    public JceStruct newInit() {
        return new bj();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.cF = jceInputStream.read(cI, 0, false);
        this.cG = jceInputStream.read(cJ, 1, false);
        this.cH = jceInputStream.read(this.cH, 2, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.cF != null) {
            jceOutputStream.write(this.cF, 0);
        }
        if (this.cG != null) {
            jceOutputStream.write(this.cG, 1);
        }
        if (this.cH != -1.0f) {
            jceOutputStream.write(this.cH, 2);
        }
    }
}
