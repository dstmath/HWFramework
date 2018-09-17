package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class dp extends JceStruct {
    public String gv = "";
    public String hK = "";
    public String hL = "";

    public JceStruct newInit() {
        return new dp();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.gv = jceInputStream.readString(0, false);
        this.hK = jceInputStream.readString(1, false);
        this.hL = jceInputStream.readString(2, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.gv != null) {
            jceOutputStream.write(this.gv, 0);
        }
        if (this.hK != null) {
            jceOutputStream.write(this.hK, 1);
        }
        if (this.hL != null) {
            jceOutputStream.write(this.hL, 2);
        }
    }
}
