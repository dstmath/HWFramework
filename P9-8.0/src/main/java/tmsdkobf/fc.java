package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class fc extends JceStruct {
    static el lE = new el();
    public String I = "";
    public int L = 0;
    public String dl = "";
    public String dn = "";
    public String do = "";
    public int dp = 0;
    public String dq = "";
    public String dr = "";
    public String dw = "";
    public short dz = (short) 0;
    public int iN = 0;
    public int ib = 0;
    public String imsi = "";
    public String kz = "";
    public String lA = "";
    public double lB = 0.0d;
    public double lC = 0.0d;
    public String lD = "";
    public int language = 0;
    public int lx = 0;
    public el ly = null;
    public int lz = 0;

    public JceStruct newInit() {
        return new fc();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.dl = jceInputStream.readString(0, true);
        this.dn = jceInputStream.readString(1, false);
        this.do = jceInputStream.readString(2, false);
        this.kz = jceInputStream.readString(3, false);
        this.dq = jceInputStream.readString(4, false);
        this.dr = jceInputStream.readString(5, false);
        this.dw = jceInputStream.readString(6, false);
        this.lx = jceInputStream.read(this.lx, 7, false);
        this.dp = jceInputStream.read(this.dp, 8, false);
        this.ly = (el) jceInputStream.read(lE, 9, false);
        this.I = jceInputStream.readString(10, false);
        this.imsi = jceInputStream.readString(11, false);
        this.ib = jceInputStream.read(this.ib, 12, false);
        this.lz = jceInputStream.read(this.lz, 13, false);
        this.iN = jceInputStream.read(this.iN, 14, false);
        this.L = jceInputStream.read(this.L, 15, false);
        this.lA = jceInputStream.readString(16, false);
        this.dz = (short) jceInputStream.read(this.dz, 17, false);
        this.lB = jceInputStream.read(this.lB, 18, false);
        this.lC = jceInputStream.read(this.lC, 19, false);
        this.lD = jceInputStream.readString(20, false);
        this.language = jceInputStream.read(this.language, 21, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.dl, 0);
        if (this.dn != null) {
            jceOutputStream.write(this.dn, 1);
        }
        if (this.do != null) {
            jceOutputStream.write(this.do, 2);
        }
        if (this.kz != null) {
            jceOutputStream.write(this.kz, 3);
        }
        if (this.dq != null) {
            jceOutputStream.write(this.dq, 4);
        }
        if (this.dr != null) {
            jceOutputStream.write(this.dr, 5);
        }
        if (this.dw != null) {
            jceOutputStream.write(this.dw, 6);
        }
        if (this.lx != 0) {
            jceOutputStream.write(this.lx, 7);
        }
        if (this.dp != 0) {
            jceOutputStream.write(this.dp, 8);
        }
        if (this.ly != null) {
            jceOutputStream.write(this.ly, 9);
        }
        if (this.I != null) {
            jceOutputStream.write(this.I, 10);
        }
        if (this.imsi != null) {
            jceOutputStream.write(this.imsi, 11);
        }
        if (this.ib != 0) {
            jceOutputStream.write(this.ib, 12);
        }
        if (this.lz != 0) {
            jceOutputStream.write(this.lz, 13);
        }
        if (this.iN != 0) {
            jceOutputStream.write(this.iN, 14);
        }
        if (this.L != 0) {
            jceOutputStream.write(this.L, 15);
        }
        if (this.lA != null) {
            jceOutputStream.write(this.lA, 16);
        }
        if (this.dz != (short) 0) {
            jceOutputStream.write(this.dz, 17);
        }
        if (this.lB != 0.0d) {
            jceOutputStream.write(this.lB, 18);
        }
        if (this.lC != 0.0d) {
            jceOutputStream.write(this.lC, 19);
        }
        if (this.lD != null) {
            jceOutputStream.write(this.lD, 20);
        }
        if (this.language != 0) {
            jceOutputStream.write(this.language, 21);
        }
    }
}
