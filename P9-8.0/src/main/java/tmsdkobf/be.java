package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class be extends JceStruct {
    static byte[] cy = new byte[1];
    public int cu = 0;
    public int cv = 0;
    public String cw = "";
    public byte[] cx = null;

    static {
        cy[0] = (byte) 0;
    }

    public JceStruct newInit() {
        return new be();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.cu = jceInputStream.read(this.cu, 0, true);
        this.cv = jceInputStream.read(this.cv, 1, false);
        this.cw = jceInputStream.readString(2, false);
        this.cx = jceInputStream.read(cy, 3, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.cu, 0);
        if (this.cv != 0) {
            jceOutputStream.write(this.cv, 1);
        }
        if (this.cw != null) {
            jceOutputStream.write(this.cw, 2);
        }
        if (this.cx != null) {
            jceOutputStream.write(this.cx, 3);
        }
    }
}
