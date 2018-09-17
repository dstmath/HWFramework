package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class dj extends JceStruct {
    static byte[] hq = new byte[1];
    static byte[] hr = new byte[1];
    public int hg = 0;
    public int hh = 0;
    public int hi = 0;
    public int hj = 0;
    public byte[] hk = null;
    public int hl = 0;
    public int hm = 0;
    public long hn = 0;
    public byte[] ho = null;
    public int hp = 0;
    public int language = 0;

    static {
        hq[0] = (byte) 0;
        hr[0] = (byte) 0;
    }

    public JceStruct newInit() {
        return new dj();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.hg = jceInputStream.read(this.hg, 0, false);
        this.language = jceInputStream.read(this.language, 1, false);
        this.hh = jceInputStream.read(this.hh, 2, false);
        this.hi = jceInputStream.read(this.hi, 3, false);
        this.hj = jceInputStream.read(this.hj, 4, false);
        this.hk = jceInputStream.read(hq, 5, false);
        this.hl = jceInputStream.read(this.hl, 6, false);
        this.hm = jceInputStream.read(this.hm, 7, false);
        this.hn = jceInputStream.read(this.hn, 8, false);
        this.ho = jceInputStream.read(hr, 9, false);
        this.hp = jceInputStream.read(this.hp, 10, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.hg != 0) {
            jceOutputStream.write(this.hg, 0);
        }
        jceOutputStream.write(this.language, 1);
        jceOutputStream.write(this.hh, 2);
        if (this.hi != 0) {
            jceOutputStream.write(this.hi, 3);
        }
        if (this.hj != 0) {
            jceOutputStream.write(this.hj, 4);
        }
        if (this.hk != null) {
            jceOutputStream.write(this.hk, 5);
        }
        if (this.hl != 0) {
            jceOutputStream.write(this.hl, 6);
        }
        if (this.hm != 0) {
            jceOutputStream.write(this.hm, 7);
        }
        if (this.hn != 0) {
            jceOutputStream.write(this.hn, 8);
        }
        if (this.ho != null) {
            jceOutputStream.write(this.ho, 9);
        }
        if (this.hp != 0) {
            jceOutputStream.write(this.hp, 10);
        }
    }
}
