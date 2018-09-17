package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class br extends JceStruct {
    public int L = 0;
    public int dA = 0;
    public String dB = "";
    public String dC = "";
    public int dD = 0;
    public String dE = "";
    public long dF = 0;
    public long dG = 0;
    public long dH = 0;
    public String dI = "";
    public String dJ = "";
    public String dK = "";
    public String dL = "";
    public String dM = "";
    public String dN = "";
    public String dO = "";
    public String dP = "";
    public int dQ = 0;
    public int dR = 0;
    public boolean dS = false;
    public String dT = "";
    public String dU = "";
    public String dV = "";
    public boolean dW = false;
    public boolean dX = false;
    public int dY = 1;
    public String dZ = "";
    public String dl = "";
    public String dm = "";
    public String dn = "";
    public String do = "";
    public int dp = 1;
    public String dq = "";
    public String dr = "";
    public int ds = 0;
    public int dt = 0;
    public boolean du = false;
    public String dv = "";
    public String dw = "";
    public int dx = 0;
    public String dy = "";
    public short dz = (short) 0;
    public String ea = "";
    public String eb = "";
    public String ec = "";
    public String ed = "";
    public String ee = "";
    public String ef = "";
    public String eg = "";
    public String eh = "";
    public long ei = 0;
    public String ej = "";
    public String ek = "";
    public int el = 0;
    public String em = "";
    public String en = "";
    public String eo = "";
    public String imsi = "";
    public String version = "";

    public JceStruct newInit() {
        return new br();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.dl = jceInputStream.readString(0, true);
        this.imsi = jceInputStream.readString(1, false);
        this.dm = jceInputStream.readString(2, false);
        this.dn = jceInputStream.readString(3, false);
        this.do = jceInputStream.readString(4, false);
        this.dp = jceInputStream.read(this.dp, 5, false);
        this.dq = jceInputStream.readString(6, false);
        this.L = jceInputStream.read(this.L, 7, false);
        this.dr = jceInputStream.readString(8, false);
        this.ds = jceInputStream.read(this.ds, 9, false);
        this.dt = jceInputStream.read(this.dt, 10, false);
        this.du = jceInputStream.read(this.du, 11, false);
        this.dv = jceInputStream.readString(12, false);
        this.dw = jceInputStream.readString(13, false);
        this.dx = jceInputStream.read(this.dx, 14, false);
        this.dy = jceInputStream.readString(15, false);
        this.dz = (short) jceInputStream.read(this.dz, 16, false);
        this.dA = jceInputStream.read(this.dA, 17, false);
        this.dB = jceInputStream.readString(18, false);
        this.dC = jceInputStream.readString(19, false);
        this.dD = jceInputStream.read(this.dD, 20, false);
        this.dE = jceInputStream.readString(21, false);
        this.dF = jceInputStream.read(this.dF, 22, false);
        this.dG = jceInputStream.read(this.dG, 23, false);
        this.dH = jceInputStream.read(this.dH, 24, false);
        this.dI = jceInputStream.readString(25, false);
        this.dJ = jceInputStream.readString(26, false);
        this.dK = jceInputStream.readString(27, false);
        this.dL = jceInputStream.readString(28, false);
        this.dM = jceInputStream.readString(29, false);
        this.dN = jceInputStream.readString(30, false);
        this.dO = jceInputStream.readString(31, false);
        this.dP = jceInputStream.readString(32, false);
        this.dQ = jceInputStream.read(this.dQ, 33, false);
        this.dR = jceInputStream.read(this.dR, 34, false);
        this.dS = jceInputStream.read(this.dS, 35, false);
        this.dT = jceInputStream.readString(36, false);
        this.dU = jceInputStream.readString(37, false);
        this.dV = jceInputStream.readString(38, false);
        this.dW = jceInputStream.read(this.dW, 39, false);
        this.dX = jceInputStream.read(this.dX, 40, false);
        this.version = jceInputStream.readString(41, false);
        this.dY = jceInputStream.read(this.dY, 42, false);
        this.dZ = jceInputStream.readString(43, false);
        this.ea = jceInputStream.readString(44, false);
        this.eb = jceInputStream.readString(45, false);
        this.ec = jceInputStream.readString(46, false);
        this.ed = jceInputStream.readString(47, false);
        this.ee = jceInputStream.readString(48, false);
        this.ef = jceInputStream.readString(49, false);
        this.eg = jceInputStream.readString(50, false);
        this.eh = jceInputStream.readString(51, false);
        this.ei = jceInputStream.read(this.ei, 52, false);
        this.ej = jceInputStream.readString(53, false);
        this.ek = jceInputStream.readString(54, false);
        this.el = jceInputStream.read(this.el, 55, false);
        this.em = jceInputStream.readString(56, false);
        this.en = jceInputStream.readString(57, false);
        this.eo = jceInputStream.readString(58, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.dl, 0);
        if (this.imsi != null) {
            jceOutputStream.write(this.imsi, 1);
        }
        if (this.dm != null) {
            jceOutputStream.write(this.dm, 2);
        }
        if (this.dn != null) {
            jceOutputStream.write(this.dn, 3);
        }
        if (this.do != null) {
            jceOutputStream.write(this.do, 4);
        }
        if (1 != this.dp) {
            jceOutputStream.write(this.dp, 5);
        }
        if (this.dq != null) {
            jceOutputStream.write(this.dq, 6);
        }
        if (this.L != 0) {
            jceOutputStream.write(this.L, 7);
        }
        if (this.dr != null) {
            jceOutputStream.write(this.dr, 8);
        }
        if (this.ds != 0) {
            jceOutputStream.write(this.ds, 9);
        }
        if (this.dt != 0) {
            jceOutputStream.write(this.dt, 10);
        }
        jceOutputStream.write(this.du, 11);
        if (this.dv != null) {
            jceOutputStream.write(this.dv, 12);
        }
        if (this.dw != null) {
            jceOutputStream.write(this.dw, 13);
        }
        if (this.dx != 0) {
            jceOutputStream.write(this.dx, 14);
        }
        if (this.dy != null) {
            jceOutputStream.write(this.dy, 15);
        }
        if (this.dz != (short) 0) {
            jceOutputStream.write(this.dz, 16);
        }
        if (this.dA != 0) {
            jceOutputStream.write(this.dA, 17);
        }
        if (this.dB != null) {
            jceOutputStream.write(this.dB, 18);
        }
        if (this.dC != null) {
            jceOutputStream.write(this.dC, 19);
        }
        if (this.dD != 0) {
            jceOutputStream.write(this.dD, 20);
        }
        if (this.dE != null) {
            jceOutputStream.write(this.dE, 21);
        }
        if (this.dF != 0) {
            jceOutputStream.write(this.dF, 22);
        }
        if (this.dG != 0) {
            jceOutputStream.write(this.dG, 23);
        }
        if (this.dH != 0) {
            jceOutputStream.write(this.dH, 24);
        }
        if (this.dI != null) {
            jceOutputStream.write(this.dI, 25);
        }
        if (this.dJ != null) {
            jceOutputStream.write(this.dJ, 26);
        }
        if (this.dK != null) {
            jceOutputStream.write(this.dK, 27);
        }
        if (this.dL != null) {
            jceOutputStream.write(this.dL, 28);
        }
        if (this.dM != null) {
            jceOutputStream.write(this.dM, 29);
        }
        if (this.dN != null) {
            jceOutputStream.write(this.dN, 30);
        }
        if (this.dO != null) {
            jceOutputStream.write(this.dO, 31);
        }
        if (this.dP != null) {
            jceOutputStream.write(this.dP, 32);
        }
        if (this.dQ != 0) {
            jceOutputStream.write(this.dQ, 33);
        }
        if (this.dR != 0) {
            jceOutputStream.write(this.dR, 34);
        }
        jceOutputStream.write(this.dS, 35);
        if (this.dT != null) {
            jceOutputStream.write(this.dT, 36);
        }
        if (this.dU != null) {
            jceOutputStream.write(this.dU, 37);
        }
        if (this.dV != null) {
            jceOutputStream.write(this.dV, 38);
        }
        jceOutputStream.write(this.dW, 39);
        jceOutputStream.write(this.dX, 40);
        if (this.version != null) {
            jceOutputStream.write(this.version, 41);
        }
        if (this.dY != 1) {
            jceOutputStream.write(this.dY, 42);
        }
        if (this.dZ != null) {
            jceOutputStream.write(this.dZ, 43);
        }
        if (this.ea != null) {
            jceOutputStream.write(this.ea, 44);
        }
        if (this.eb != null) {
            jceOutputStream.write(this.eb, 45);
        }
        if (this.ec != null) {
            jceOutputStream.write(this.ec, 46);
        }
        if (this.ed != null) {
            jceOutputStream.write(this.ed, 47);
        }
        if (this.ee != null) {
            jceOutputStream.write(this.ee, 48);
        }
        if (this.ef != null) {
            jceOutputStream.write(this.ef, 49);
        }
        if (this.eg != null) {
            jceOutputStream.write(this.eg, 50);
        }
        if (this.eh != null) {
            jceOutputStream.write(this.eh, 51);
        }
        if (this.ei != 0) {
            jceOutputStream.write(this.ei, 52);
        }
        if (this.ej != null) {
            jceOutputStream.write(this.ej, 53);
        }
        if (this.ek != null) {
            jceOutputStream.write(this.ek, 54);
        }
        if (this.el != 0) {
            jceOutputStream.write(this.el, 55);
        }
        if (this.em != null) {
            jceOutputStream.write(this.em, 56);
        }
        if (this.en != null) {
            jceOutputStream.write(this.en, 57);
        }
        if (this.eo != null) {
            jceOutputStream.write(this.eo, 58);
        }
    }
}
