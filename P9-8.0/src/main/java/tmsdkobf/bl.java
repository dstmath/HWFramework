package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class bl extends JceStruct {
    public int cO = 0;
    public int cP = 0;
    public long cQ = -1;
    public int cR = -1;
    public boolean cS = true;
    public int cT = 255;
    public int city = 0;
    public int province = 0;

    public JceStruct newInit() {
        return new bl();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.province = jceInputStream.read(this.province, 0, true);
        this.city = jceInputStream.read(this.city, 1, true);
        this.cO = jceInputStream.read(this.cO, 2, true);
        this.cP = jceInputStream.read(this.cP, 3, false);
        this.cQ = jceInputStream.read(this.cQ, 4, false);
        this.cR = jceInputStream.read(this.cR, 5, false);
        this.cS = jceInputStream.read(this.cS, 6, false);
        this.cT = jceInputStream.read(this.cT, 7, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.province, 0);
        jceOutputStream.write(this.city, 1);
        jceOutputStream.write(this.cO, 2);
        if (this.cP != 0) {
            jceOutputStream.write(this.cP, 3);
        }
        if (this.cQ != -1) {
            jceOutputStream.write(this.cQ, 4);
        }
        if (this.cR != -1) {
            jceOutputStream.write(this.cR, 5);
        }
        if (!this.cS) {
            jceOutputStream.write(this.cS, 6);
        }
        if (this.cT != 255) {
            jceOutputStream.write(this.cT, 7);
        }
    }
}
