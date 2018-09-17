package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class cs extends JceStruct {
    public int fP = 0;
    public int time = 0;

    public JceStruct newInit() {
        return new cs();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.fP = jceInputStream.read(this.fP, 0, true);
        this.time = jceInputStream.read(this.time, 1, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.fP, 0);
        jceOutputStream.write(this.time, 1);
    }
}
