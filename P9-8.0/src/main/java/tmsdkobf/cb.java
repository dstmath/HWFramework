package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class cb extends JceStruct {
    public String eN = "";

    public JceStruct newInit() {
        return new cb();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.eN = jceInputStream.readString(0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.eN, 0);
    }
}
