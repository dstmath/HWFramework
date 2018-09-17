package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class ds extends JceStruct {
    static ek hU = new ek();
    static fc hV = new fc();
    static dv hW = new dv();
    public ek hR = null;
    public fc hS = null;
    public dv hT = null;

    public JceStruct newInit() {
        return new ds();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.hR = (ek) jceInputStream.read(hU, 0, false);
        this.hS = (fc) jceInputStream.read(hV, 1, false);
        this.hT = (dv) jceInputStream.read(hW, 3, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.hR != null) {
            jceOutputStream.write(this.hR, 0);
        }
        if (this.hS != null) {
            jceOutputStream.write(this.hS, 1);
        }
        if (this.hT != null) {
            jceOutputStream.write(this.hT, 3);
        }
    }
}
