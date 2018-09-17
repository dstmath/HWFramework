package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class i extends JceStruct {
    public String I = "";
    public String J = "";
    public String K = "";
    public int L = 0;
    public int M = 0;
    public long N = 0;
    public int O = 0;
    public String P = "";
    public int authType = 0;
    public int s = 2;

    public JceStruct newInit() {
        return new i();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.s = jceInputStream.read(this.s, 0, true);
        this.authType = jceInputStream.read(this.authType, 1, true);
        this.I = jceInputStream.readString(2, false);
        this.J = jceInputStream.readString(3, false);
        this.K = jceInputStream.readString(4, false);
        this.L = jceInputStream.read(this.L, 5, false);
        this.M = jceInputStream.read(this.M, 6, false);
        this.N = jceInputStream.read(this.N, 7, false);
        this.O = jceInputStream.read(this.O, 8, false);
        this.P = jceInputStream.readString(9, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.s, 0);
        jceOutputStream.write(this.authType, 1);
        if (this.I != null) {
            jceOutputStream.write(this.I, 2);
        }
        if (this.J != null) {
            jceOutputStream.write(this.J, 3);
        }
        if (this.K != null) {
            jceOutputStream.write(this.K, 4);
        }
        if (this.L != 0) {
            jceOutputStream.write(this.L, 5);
        }
        if (this.M != 0) {
            jceOutputStream.write(this.M, 6);
        }
        if (this.N != 0) {
            jceOutputStream.write(this.N, 7);
        }
        if (this.O != 0) {
            jceOutputStream.write(this.O, 8);
        }
        if (this.P != null) {
            jceOutputStream.write(this.P, 9);
        }
    }
}
