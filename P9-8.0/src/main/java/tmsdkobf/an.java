package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class an extends JceStruct {
    static byte[] bB = new byte[1];
    public byte[] bA = null;
    public int bz = 0;
    public int status = 0;

    static {
        bB[0] = (byte) 0;
    }

    public JceStruct newInit() {
        return new an();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.bz = jceInputStream.read(this.bz, 0, true);
        this.bA = jceInputStream.read(bB, 1, false);
        this.status = jceInputStream.read(this.status, 2, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.bz, 0);
        if (this.bA != null) {
            jceOutputStream.write(this.bA, 1);
        }
        if (this.status != 0) {
            jceOutputStream.write(this.status, 2);
        }
    }
}
