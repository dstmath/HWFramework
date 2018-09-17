package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class r extends JceStruct {
    public int Y = 0;
    public int action = 0;
    public int af = 0;
    public long al = 0;
    public long am = 0;
    public int an = 0;
    public int ao = 0;
    public int result = 0;
    public int time = 0;

    public JceStruct newInit() {
        return new r();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.al = jceInputStream.read(this.al, 0, false);
        this.am = jceInputStream.read(this.am, 1, false);
        this.action = jceInputStream.read(this.action, 2, false);
        this.af = jceInputStream.read(this.af, 3, false);
        this.Y = jceInputStream.read(this.Y, 4, false);
        this.an = jceInputStream.read(this.an, 5, false);
        this.result = jceInputStream.read(this.result, 6, false);
        this.ao = jceInputStream.read(this.ao, 7, false);
        this.time = jceInputStream.read(this.time, 8, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.al != 0) {
            jceOutputStream.write(this.al, 0);
        }
        if (this.am != 0) {
            jceOutputStream.write(this.am, 1);
        }
        jceOutputStream.write(this.action, 2);
        if (this.af != 0) {
            jceOutputStream.write(this.af, 3);
        }
        jceOutputStream.write(this.Y, 4);
        jceOutputStream.write(this.an, 5);
        jceOutputStream.write(this.result, 6);
        jceOutputStream.write(this.ao, 7);
        if (this.time != 0) {
            jceOutputStream.write(this.time, 8);
        }
    }
}
