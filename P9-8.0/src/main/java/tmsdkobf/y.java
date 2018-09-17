package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class y extends JceStruct {
    public int R = 0;

    public JceStruct newInit() {
        return new y();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.R = jceInputStream.read(this.R, 0, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.R != 0) {
            jceOutputStream.write(this.R, 0);
        }
    }
}
