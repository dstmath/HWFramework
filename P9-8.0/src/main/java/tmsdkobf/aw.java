package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class aw extends JceStruct {
    static byte[] cg = new byte[1];
    public byte[] cf = null;

    static {
        cg[0] = (byte) 0;
    }

    public JceStruct newInit() {
        return new aw();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.cf = jceInputStream.read(cg, 0, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.cf != null) {
            jceOutputStream.write(this.cf, 0);
        }
    }
}
