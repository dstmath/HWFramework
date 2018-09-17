package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class q extends JceStruct {
    public int Y = 0;
    public int af = 0;
    public long al = 0;
    public long am = 0;
    public int result = 0;

    public JceStruct newInit() {
        return new q();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.al = jceInputStream.read(this.al, 0, false);
        this.am = jceInputStream.read(this.am, 1, false);
        this.af = jceInputStream.read(this.af, 2, false);
        this.Y = jceInputStream.read(this.Y, 3, false);
        this.result = jceInputStream.read(this.result, 4, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.al != 0) {
            jceOutputStream.write(this.al, 0);
        }
        if (this.am != 0) {
            jceOutputStream.write(this.am, 1);
        }
        if (this.af != 0) {
            jceOutputStream.write(this.af, 2);
        }
        jceOutputStream.write(this.Y, 3);
        if (this.result != 0) {
            jceOutputStream.write(this.result, 4);
        }
    }
}
