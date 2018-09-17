package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class eu extends JceStruct {
    static et kU;
    public et kT = null;

    public void readFrom(JceInputStream jceInputStream) {
        if (kU == null) {
            kU = new et();
        }
        this.kT = (et) jceInputStream.read(kU, 1, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.kT, 1);
    }
}
