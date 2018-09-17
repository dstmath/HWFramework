package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class au extends JceStruct {
    public String T = "";

    public JceStruct newInit() {
        return new au();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.T = jceInputStream.readString(0, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.T != null) {
            jceOutputStream.write(this.T, 0);
        }
    }
}
