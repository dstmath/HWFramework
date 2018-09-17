package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class ay extends JceStruct {
    public String ch = "";
    public int ci = 0;
    public int cj = 0;
    public String imsi = "";

    public JceStruct newInit() {
        return new ay();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.ch = jceInputStream.readString(0, true);
        this.ci = jceInputStream.read(this.ci, 1, true);
        this.imsi = jceInputStream.readString(2, false);
        this.cj = jceInputStream.read(this.cj, 3, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.ch, 0);
        jceOutputStream.write(this.ci, 1);
        if (this.imsi != null) {
            jceOutputStream.write(this.imsi, 2);
        }
        if (this.cj != 0) {
            jceOutputStream.write(this.cj, 3);
        }
    }
}
