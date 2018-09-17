package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class cx extends JceStruct {
    static cy gf = new cy();
    static ct gg = new ct();
    public cy gd = null;
    public ct ge = null;
    public int level = 0;
    public int linkType = 0;
    public int riskType = 0;
    public String url = "";

    public JceStruct newInit() {
        return new cx();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.url = jceInputStream.readString(0, true);
        this.level = jceInputStream.read(this.level, 1, true);
        this.linkType = jceInputStream.read(this.linkType, 2, true);
        this.riskType = jceInputStream.read(this.riskType, 3, false);
        this.gd = (cy) jceInputStream.read(gf, 4, false);
        this.ge = (ct) jceInputStream.read(gg, 5, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.url, 0);
        jceOutputStream.write(this.level, 1);
        jceOutputStream.write(this.linkType, 2);
        jceOutputStream.write(this.riskType, 3);
        if (this.gd != null) {
            jceOutputStream.write(this.gd, 4);
        }
        if (this.ge != null) {
            jceOutputStream.write(this.ge, 5);
        }
    }
}
