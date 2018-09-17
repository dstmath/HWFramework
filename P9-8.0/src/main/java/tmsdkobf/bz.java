package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class bz extends JceStruct {
    public int eM = 0;

    public JceStruct newInit() {
        return new bz();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.eM = jceInputStream.read(this.eM, 0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.eM, 0);
    }
}
