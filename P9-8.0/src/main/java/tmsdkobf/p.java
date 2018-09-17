package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class p extends JceStruct {
    static byte[] ai = new byte[1];
    static y aj = new y();
    static z ak = new z();
    public int Y = 0;
    public byte[] ae = null;
    public int af = 0;
    public y ag = null;
    public z ah = null;

    static {
        ai[0] = (byte) 0;
    }

    public JceStruct newInit() {
        return new p();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.Y = jceInputStream.read(this.Y, 0, false);
        this.ae = jceInputStream.read(ai, 1, false);
        this.af = jceInputStream.read(this.af, 2, false);
        this.ag = (y) jceInputStream.read(aj, 3, false);
        this.ah = (z) jceInputStream.read(ak, 4, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.Y, 0);
        if (this.ae != null) {
            jceOutputStream.write(this.ae, 1);
        }
        if (this.af != 0) {
            jceOutputStream.write(this.af, 2);
        }
        if (this.ag != null) {
            jceOutputStream.write(this.ag, 3);
        }
        if (this.ah != null) {
            jceOutputStream.write(this.ah, 4);
        }
    }
}
