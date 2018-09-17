package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class cu extends JceStruct {
    static cw ga = new cw();
    public int fX = 0;
    public int fY = 0;
    public cw fZ = null;

    public JceStruct newInit() {
        return new cu();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.fX = jceInputStream.read(this.fX, 0, true);
        this.fY = jceInputStream.read(this.fY, 1, true);
        this.fZ = (cw) jceInputStream.read(ga, 2, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.fX, 0);
        jceOutputStream.write(this.fY, 1);
        jceOutputStream.write(this.fZ, 2);
    }
}
