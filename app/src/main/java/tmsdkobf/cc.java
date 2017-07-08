package tmsdkobf;

/* compiled from: Unknown */
public final class cc extends fs {
    public String eC;
    public String eD;
    public String eE;
    public String eF;
    public int eG;
    public int eH;
    public long eI;
    public String eJ;
    public int eK;
    public String eL;
    public int eM;
    public String eN;
    public String eO;
    public String eP;
    public int eQ;
    public String eR;
    public String eS;
    public int eT;
    public int eU;
    public String ej;
    public String location;
    public String source;
    public int tagCount;
    public int tagType;

    public cc() {
        this.ej = "";
        this.tagType = 0;
        this.tagCount = 0;
        this.eC = "";
        this.eD = "";
        this.eE = "";
        this.source = "";
        this.eF = "";
        this.eG = 0;
        this.eH = 0;
        this.eI = 0;
        this.eJ = "";
        this.eK = 0;
        this.location = "";
        this.eL = "";
        this.eM = 0;
        this.eN = "";
        this.eO = "";
        this.eP = "";
        this.eQ = 0;
        this.eR = "";
        this.eS = "";
        this.eT = 0;
        this.eU = 0;
    }

    public fs newInit() {
        return new cc();
    }

    public void readFrom(fq fqVar) {
        this.ej = fqVar.a(1, true);
        this.tagType = fqVar.a(this.tagType, 2, true);
        this.tagCount = fqVar.a(this.tagCount, 3, true);
        this.eC = fqVar.a(4, false);
        this.eD = fqVar.a(5, false);
        this.eE = fqVar.a(6, false);
        this.source = fqVar.a(7, false);
        this.eF = fqVar.a(8, false);
        this.eG = fqVar.a(this.eG, 9, false);
        this.eH = fqVar.a(this.eH, 10, false);
        this.eI = fqVar.a(this.eI, 11, false);
        this.eJ = fqVar.a(12, false);
        this.eK = fqVar.a(this.eK, 13, false);
        this.location = fqVar.a(14, false);
        this.eL = fqVar.a(15, false);
        this.eM = fqVar.a(this.eM, 16, false);
        this.eN = fqVar.a(17, false);
        this.eO = fqVar.a(18, false);
        this.eP = fqVar.a(19, false);
        this.eQ = fqVar.a(this.eQ, 20, false);
        this.eR = fqVar.a(21, false);
        this.eS = fqVar.a(22, false);
        this.eT = fqVar.a(this.eT, 23, false);
        this.eU = fqVar.a(this.eU, 24, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.ej, 1);
        frVar.write(this.tagType, 2);
        frVar.write(this.tagCount, 3);
        if (this.eC != null) {
            frVar.a(this.eC, 4);
        }
        if (this.eD != null) {
            frVar.a(this.eD, 5);
        }
        if (this.eE != null) {
            frVar.a(this.eE, 6);
        }
        if (this.source != null) {
            frVar.a(this.source, 7);
        }
        if (this.eF != null) {
            frVar.a(this.eF, 8);
        }
        if (this.eG != 0) {
            frVar.write(this.eG, 9);
        }
        if (this.eH != 0) {
            frVar.write(this.eH, 10);
        }
        if (this.eI != 0) {
            frVar.b(this.eI, 11);
        }
        if (this.eJ != null) {
            frVar.a(this.eJ, 12);
        }
        if (this.eK != 0) {
            frVar.write(this.eK, 13);
        }
        if (this.location != null) {
            frVar.a(this.location, 14);
        }
        if (this.eL != null) {
            frVar.a(this.eL, 15);
        }
        if (this.eM != 0) {
            frVar.write(this.eM, 16);
        }
        if (this.eN != null) {
            frVar.a(this.eN, 17);
        }
        if (this.eO != null) {
            frVar.a(this.eO, 18);
        }
        if (this.eP != null) {
            frVar.a(this.eP, 19);
        }
        if (this.eQ != 0) {
            frVar.write(this.eQ, 20);
        }
        if (this.eR != null) {
            frVar.a(this.eR, 21);
        }
        if (this.eS != null) {
            frVar.a(this.eS, 22);
        }
        if (this.eT != 0) {
            frVar.write(this.eT, 23);
        }
        if (this.eU != 0) {
            frVar.write(this.eU, 24);
        }
    }
}
