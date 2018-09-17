package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class o extends JceStruct {
    public String Z = "";
    public String aa = "";
    public String ab = "";
    public String ac = "";
    public String ad = "";

    public JceStruct newInit() {
        return new o();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.Z = jceInputStream.readString(0, false);
        this.aa = jceInputStream.readString(1, false);
        this.ab = jceInputStream.readString(2, false);
        this.ac = jceInputStream.readString(3, false);
        this.ad = jceInputStream.readString(4, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.Z != null) {
            jceOutputStream.write(this.Z, 0);
        }
        if (this.aa != null) {
            jceOutputStream.write(this.aa, 1);
        }
        if (this.ab != null) {
            jceOutputStream.write(this.ab, 2);
        }
        if (this.ac != null) {
            jceOutputStream.write(this.ac, 3);
        }
        if (this.ad != null) {
            jceOutputStream.write(this.ad, 4);
        }
    }
}
