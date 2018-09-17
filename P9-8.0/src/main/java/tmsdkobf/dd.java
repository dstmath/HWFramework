package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class dd extends JceStruct {
    public String go = "";
    public String gp = "";

    public void readFrom(JceInputStream jceInputStream) {
        this.go = jceInputStream.readString(0, false);
        this.gp = jceInputStream.readString(1, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.go != null) {
            jceOutputStream.write(this.go, 0);
        }
        if (this.gp != null) {
            jceOutputStream.write(this.gp, 1);
        }
    }
}
