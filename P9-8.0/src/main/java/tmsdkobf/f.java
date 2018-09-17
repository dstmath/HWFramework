package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class f extends JceStruct {
    public int n = 0;
    public int o = 0;
    public int start = 0;

    public JceStruct newInit() {
        return new f();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.start = jceInputStream.read(this.start, 0, false);
        this.n = jceInputStream.read(this.n, 1, false);
        this.o = jceInputStream.read(this.o, 2, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.start != 0) {
            jceOutputStream.write(this.start, 0);
        }
        if (this.n != 0) {
            jceOutputStream.write(this.n, 1);
        }
        if (this.o != 0) {
            jceOutputStream.write(this.o, 2);
        }
    }
}
