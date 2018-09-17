package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class bq extends JceStruct {
    static bg dk = new bg();
    public int cj = 0;
    public String dd = "";
    public String de = "";
    public int df = 0;
    public bg dg = null;
    public int dh = 0;
    public String di = "";
    public int dj = 0;
    public String imsi = "";
    public int status = 0;

    public JceStruct newInit() {
        return new bq();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.dd = jceInputStream.readString(0, true);
        this.de = jceInputStream.readString(1, true);
        this.df = jceInputStream.read(this.df, 2, true);
        this.dg = (bg) jceInputStream.read(dk, 3, true);
        this.dh = jceInputStream.read(this.dh, 4, true);
        this.di = jceInputStream.readString(5, true);
        this.status = jceInputStream.read(this.status, 6, false);
        this.imsi = jceInputStream.readString(7, false);
        this.dj = jceInputStream.read(this.dj, 8, false);
        this.cj = jceInputStream.read(this.cj, 9, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.dd, 0);
        jceOutputStream.write(this.de, 1);
        jceOutputStream.write(this.df, 2);
        jceOutputStream.write(this.dg, 3);
        jceOutputStream.write(this.dh, 4);
        jceOutputStream.write(this.di, 5);
        jceOutputStream.write(this.status, 6);
        if (this.imsi != null) {
            jceOutputStream.write(this.imsi, 7);
        }
        jceOutputStream.write(this.dj, 8);
        if (this.cj != 0) {
            jceOutputStream.write(this.cj, 9);
        }
    }
}
