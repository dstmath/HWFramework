package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class dc extends JceStruct {
    public int gn = 1;

    public void readFrom(JceInputStream jceInputStream) {
        this.gn = jceInputStream.read(this.gn, 0, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (1 != this.gn) {
            jceOutputStream.write(this.gn, 0);
        }
    }
}
