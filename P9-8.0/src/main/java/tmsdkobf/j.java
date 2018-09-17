package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class j extends JceStruct {
    public boolean Q = true;
    public int R = 0;
    public int S = 0;
    public int time = 0;

    public void readFrom(JceInputStream jceInputStream) {
        this.time = jceInputStream.read(this.time, 0, true);
        this.Q = jceInputStream.read(this.Q, 1, true);
        this.R = jceInputStream.read(this.R, 2, false);
        this.S = jceInputStream.read(this.S, 3, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.time, 0);
        jceOutputStream.write(this.Q, 1);
        if (this.R != 0) {
            jceOutputStream.write(this.R, 2);
        }
        if (this.S != 0) {
            jceOutputStream.write(this.S, 3);
        }
    }
}
