package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class dt extends JceStruct {
    static ek hU = new ek();
    static fc hV = new fc();
    static em hY = new em();
    public ek hR = null;
    public fc hS = null;
    public em hX = null;

    public JceStruct newInit() {
        return new dt();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.hR = (ek) jceInputStream.read(hU, 0, false);
        this.hS = (fc) jceInputStream.read(hV, 1, false);
        this.hX = (em) jceInputStream.read(hY, 2, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.hR != null) {
            jceOutputStream.write(this.hR, 0);
        }
        if (this.hS != null) {
            jceOutputStream.write(this.hS, 1);
        }
        if (this.hX != null) {
            jceOutputStream.write(this.hX, 2);
        }
    }
}
