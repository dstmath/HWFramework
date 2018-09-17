package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class ba extends JceStruct {
    public int aH = 0;
    public int cj = 0;
    public String imsi = "";

    public JceStruct newInit() {
        return new ba();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.aH = jceInputStream.read(this.aH, 0, true);
        this.imsi = jceInputStream.readString(1, false);
        this.cj = jceInputStream.read(this.cj, 2, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.aH, 0);
        if (this.imsi != null) {
            jceOutputStream.write(this.imsi, 1);
        }
        if (this.cj != 0) {
            jceOutputStream.write(this.cj, 2);
        }
    }
}
