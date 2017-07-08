package tmsdkobf;

/* compiled from: Unknown */
public final class bj extends fs {
    public String cC;
    public String cD;
    public String cE;
    public String cF;
    public String cG;
    public String cH;
    public int cI;
    public int cJ;
    public boolean cK;
    public String cL;
    public String cM;
    public int cN;
    public String cO;
    public short cP;
    public int cQ;
    public String cR;
    public String cS;
    public int cT;
    public String cU;
    public long cV;
    public long cW;
    public long cX;
    public String cY;
    public String cZ;
    public String dA;
    public int dB;
    public String da;
    public String db;
    public String dc;
    public String dd;
    public String de;
    public String df;
    public int dg;
    public int dh;
    public boolean di;
    public String dj;
    public String dk;
    public String dl;
    public boolean dm;
    public boolean dn;
    public int do;
    public String dp;
    public String dq;
    public String dr;
    public String ds;
    public String dt;
    public String du;
    public String dv;
    public String dw;
    public String dx;
    public long dy;
    public String dz;
    public String imsi;
    public int product;
    public int u;
    public String version;

    public bj() {
        this.cC = "";
        this.imsi = "";
        this.cD = "";
        this.cE = "";
        this.cF = "";
        this.product = 1;
        this.cG = "";
        this.u = 0;
        this.cH = "";
        this.cI = 0;
        this.cJ = 0;
        this.cK = false;
        this.cL = "";
        this.cM = "";
        this.cN = 0;
        this.cO = "";
        this.cP = (short) 0;
        this.cQ = 0;
        this.cR = "";
        this.cS = "";
        this.cT = 0;
        this.cU = "";
        this.cV = 0;
        this.cW = 0;
        this.cX = 0;
        this.cY = "";
        this.cZ = "";
        this.da = "";
        this.db = "";
        this.dc = "";
        this.dd = "";
        this.de = "";
        this.df = "";
        this.dg = 0;
        this.dh = 0;
        this.di = false;
        this.dj = "";
        this.dk = "";
        this.dl = "";
        this.dm = false;
        this.dn = false;
        this.version = "";
        this.do = 1;
        this.dp = "";
        this.dq = "";
        this.dr = "";
        this.ds = "";
        this.dt = "";
        this.du = "";
        this.dv = "";
        this.dw = "";
        this.dx = "";
        this.dy = 0;
        this.dz = "";
        this.dA = "";
        this.dB = 0;
    }

    public fs newInit() {
        return new bj();
    }

    public void readFrom(fq fqVar) {
        this.cC = fqVar.a(0, true);
        this.imsi = fqVar.a(1, false);
        this.cD = fqVar.a(2, false);
        this.cE = fqVar.a(3, false);
        this.cF = fqVar.a(4, false);
        this.product = fqVar.a(this.product, 5, false);
        this.cG = fqVar.a(6, false);
        this.u = fqVar.a(this.u, 7, false);
        this.cH = fqVar.a(8, false);
        this.cI = fqVar.a(this.cI, 9, false);
        this.cJ = fqVar.a(this.cJ, 10, false);
        this.cK = fqVar.a(this.cK, 11, false);
        this.cL = fqVar.a(12, false);
        this.cM = fqVar.a(13, false);
        this.cN = fqVar.a(this.cN, 14, false);
        this.cO = fqVar.a(15, false);
        this.cP = (short) fqVar.a(this.cP, 16, false);
        this.cQ = fqVar.a(this.cQ, 17, false);
        this.cR = fqVar.a(18, false);
        this.cS = fqVar.a(19, false);
        this.cT = fqVar.a(this.cT, 20, false);
        this.cU = fqVar.a(21, false);
        this.cV = fqVar.a(this.cV, 22, false);
        this.cW = fqVar.a(this.cW, 23, false);
        this.cX = fqVar.a(this.cX, 24, false);
        this.cY = fqVar.a(25, false);
        this.cZ = fqVar.a(26, false);
        this.da = fqVar.a(27, false);
        this.db = fqVar.a(28, false);
        this.dc = fqVar.a(29, false);
        this.dd = fqVar.a(30, false);
        this.de = fqVar.a(31, false);
        this.df = fqVar.a(32, false);
        this.dg = fqVar.a(this.dg, 33, false);
        this.dh = fqVar.a(this.dh, 34, false);
        this.di = fqVar.a(this.di, 35, false);
        this.dj = fqVar.a(36, false);
        this.dk = fqVar.a(37, false);
        this.dl = fqVar.a(38, false);
        this.dm = fqVar.a(this.dm, 39, false);
        this.dn = fqVar.a(this.dn, 40, false);
        this.version = fqVar.a(41, false);
        this.do = fqVar.a(this.do, 42, false);
        this.dp = fqVar.a(43, false);
        this.dq = fqVar.a(44, false);
        this.dr = fqVar.a(45, false);
        this.ds = fqVar.a(46, false);
        this.dt = fqVar.a(47, false);
        this.du = fqVar.a(48, false);
        this.dv = fqVar.a(49, false);
        this.dw = fqVar.a(50, false);
        this.dx = fqVar.a(51, false);
        this.dy = fqVar.a(this.dy, 52, false);
        this.dz = fqVar.a(53, false);
        this.dA = fqVar.a(54, false);
        this.dB = fqVar.a(this.dB, 55, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.cC, 0);
        if (this.imsi != null) {
            frVar.a(this.imsi, 1);
        }
        if (this.cD != null) {
            frVar.a(this.cD, 2);
        }
        if (this.cE != null) {
            frVar.a(this.cE, 3);
        }
        if (this.cF != null) {
            frVar.a(this.cF, 4);
        }
        if (1 != this.product) {
            frVar.write(this.product, 5);
        }
        if (this.cG != null) {
            frVar.a(this.cG, 6);
        }
        if (this.u != 0) {
            frVar.write(this.u, 7);
        }
        if (this.cH != null) {
            frVar.a(this.cH, 8);
        }
        if (this.cI != 0) {
            frVar.write(this.cI, 9);
        }
        if (this.cJ != 0) {
            frVar.write(this.cJ, 10);
        }
        if (this.cK) {
            frVar.a(this.cK, 11);
        }
        if (this.cL != null) {
            frVar.a(this.cL, 12);
        }
        if (this.cM != null) {
            frVar.a(this.cM, 13);
        }
        if (this.cN != 0) {
            frVar.write(this.cN, 14);
        }
        if (this.cO != null) {
            frVar.a(this.cO, 15);
        }
        if (this.cP != (short) 0) {
            frVar.a(this.cP, 16);
        }
        if (this.cQ != 0) {
            frVar.write(this.cQ, 17);
        }
        if (this.cR != null) {
            frVar.a(this.cR, 18);
        }
        if (this.cS != null) {
            frVar.a(this.cS, 19);
        }
        if (this.cT != 0) {
            frVar.write(this.cT, 20);
        }
        if (this.cU != null) {
            frVar.a(this.cU, 21);
        }
        if (this.cV != 0) {
            frVar.b(this.cV, 22);
        }
        if (this.cW != 0) {
            frVar.b(this.cW, 23);
        }
        if (this.cX != 0) {
            frVar.b(this.cX, 24);
        }
        if (this.cY != null) {
            frVar.a(this.cY, 25);
        }
        if (this.cZ != null) {
            frVar.a(this.cZ, 26);
        }
        if (this.da != null) {
            frVar.a(this.da, 27);
        }
        if (this.db != null) {
            frVar.a(this.db, 28);
        }
        if (this.dc != null) {
            frVar.a(this.dc, 29);
        }
        if (this.dd != null) {
            frVar.a(this.dd, 30);
        }
        if (this.de != null) {
            frVar.a(this.de, 31);
        }
        if (this.df != null) {
            frVar.a(this.df, 32);
        }
        if (this.dg != 0) {
            frVar.write(this.dg, 33);
        }
        if (this.dh != 0) {
            frVar.write(this.dh, 34);
        }
        if (this.di) {
            frVar.a(this.di, 35);
        }
        if (this.dj != null) {
            frVar.a(this.dj, 36);
        }
        if (this.dk != null) {
            frVar.a(this.dk, 37);
        }
        if (this.dl != null) {
            frVar.a(this.dl, 38);
        }
        if (this.dm) {
            frVar.a(this.dm, 39);
        }
        if (this.dn) {
            frVar.a(this.dn, 40);
        }
        if (this.version != null) {
            frVar.a(this.version, 41);
        }
        if (this.do != 1) {
            frVar.write(this.do, 42);
        }
        if (this.dp != null) {
            frVar.a(this.dp, 43);
        }
        if (this.dq != null) {
            frVar.a(this.dq, 44);
        }
        if (this.dr != null) {
            frVar.a(this.dr, 45);
        }
        if (this.ds != null) {
            frVar.a(this.ds, 46);
        }
        if (this.dt != null) {
            frVar.a(this.dt, 47);
        }
        if (this.du != null) {
            frVar.a(this.du, 48);
        }
        if (this.dv != null) {
            frVar.a(this.dv, 49);
        }
        if (this.dw != null) {
            frVar.a(this.dw, 50);
        }
        if (this.dx != null) {
            frVar.a(this.dx, 51);
        }
        if (this.dy != 0) {
            frVar.b(this.dy, 52);
        }
        if (this.dz != null) {
            frVar.a(this.dz, 53);
        }
        if (this.dA != null) {
            frVar.a(this.dA, 54);
        }
        if (this.dB != 0) {
            frVar.write(this.dB, 55);
        }
    }
}
