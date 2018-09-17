package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class bu extends JceStruct {
    public String ep = "";
    public int ev = 0;
    public String ew = "";

    public JceStruct newInit() {
        return new bu();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.ev = jceInputStream.read(this.ev, 0, true);
        this.ew = jceInputStream.readString(1, false);
        this.ep = jceInputStream.readString(2, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.ev, 0);
        if (this.ew != null) {
            jceOutputStream.write(this.ew, 1);
        }
        if (this.ep != null) {
            jceOutputStream.write(this.ep, 2);
        }
    }
}
