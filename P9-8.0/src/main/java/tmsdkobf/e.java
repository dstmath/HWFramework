package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class e extends JceStruct {
    public long l = 0;
    public long m = 0;

    public JceStruct newInit() {
        return new e();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.l = jceInputStream.read(this.l, 0, false);
        this.m = jceInputStream.read(this.m, 1, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.l != 0) {
            jceOutputStream.write(this.l, 0);
        }
        if (this.m != 0) {
            jceOutputStream.write(this.m, 1);
        }
    }
}
