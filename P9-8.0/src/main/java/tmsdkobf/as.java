package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class as extends JceStruct {
    static byte[] bX = new byte[1];
    public int bR = 0;
    public long bS = 0;
    public String bT = "";
    public byte[] bU = null;
    public boolean bV = false;
    public short bW = (short) 0;
    public int i = 0;
    public int valueType = 0;

    static {
        bX[0] = (byte) 0;
    }

    public JceStruct newInit() {
        return new as();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.valueType = jceInputStream.read(this.valueType, 0, false);
        this.bR = jceInputStream.read(this.bR, 1, false);
        this.i = jceInputStream.read(this.i, 2, false);
        this.bS = jceInputStream.read(this.bS, 3, false);
        this.bT = jceInputStream.readString(4, false);
        this.bU = jceInputStream.read(bX, 5, false);
        this.bV = jceInputStream.read(this.bV, 6, false);
        this.bW = (short) jceInputStream.read(this.bW, 7, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.valueType != 0) {
            jceOutputStream.write(this.valueType, 0);
        }
        if (this.bR != 0) {
            jceOutputStream.write(this.bR, 1);
        }
        if (this.i != 0) {
            jceOutputStream.write(this.i, 2);
        }
        if (this.bS != 0) {
            jceOutputStream.write(this.bS, 3);
        }
        if (this.bT != null) {
            jceOutputStream.write(this.bT, 4);
        }
        if (this.bU != null) {
            jceOutputStream.write(this.bU, 5);
        }
        if (this.bV) {
            jceOutputStream.write(this.bV, 6);
        }
        if (this.bW != (short) 0) {
            jceOutputStream.write(this.bW, 7);
        }
    }
}
