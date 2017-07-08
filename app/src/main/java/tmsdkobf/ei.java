package tmsdkobf;

/* compiled from: Unknown */
public final class ei extends fs implements Comparable<ei> {
    public int category;
    public int fL;
    public String iI;
    public String iq;
    public int jC;
    public int jS;
    public String kJ;
    public String kK;
    public int kL;
    public String kM;
    public int kN;
    public int kO;
    public int kP;
    public int kQ;
    public String ky;
    public String name;
    public int source;
    public String version;

    public ei() {
        this.iI = "";
        this.kJ = "";
        this.version = "";
        this.kK = "";
        this.iq = "";
        this.kL = 0;
        this.name = "";
        this.fL = 0;
        this.kM = "";
        this.kN = 0;
        this.kO = 0;
        this.category = 0;
        this.kP = 0;
        this.source = 0;
        this.kQ = 0;
        this.jC = 0;
        this.jS = 0;
        this.ky = "";
    }

    public int a(ei eiVar) {
        int[] iArr = new int[]{ft.a(this.iI, eiVar.iI), ft.a(this.kJ, eiVar.kJ), ft.a(this.version, eiVar.version), ft.a(this.kK, eiVar.kK)};
        for (int i = 0; i < iArr.length; i++) {
            if (iArr[i] != 0) {
                return iArr[i];
            }
        }
        return 0;
    }

    public /* synthetic */ int compareTo(Object obj) {
        return a((ei) obj);
    }

    public void readFrom(fq fqVar) {
        this.iI = fqVar.a(0, true);
        this.kJ = fqVar.a(1, true);
        this.version = fqVar.a(2, true);
        this.kK = fqVar.a(3, false);
        this.iq = fqVar.a(4, false);
        this.kL = fqVar.a(this.kL, 5, false);
        this.name = fqVar.a(6, false);
        this.fL = fqVar.a(this.fL, 7, false);
        this.kM = fqVar.a(8, false);
        this.kN = fqVar.a(this.kN, 9, false);
        this.kO = fqVar.a(this.kO, 10, false);
        this.category = fqVar.a(this.category, 11, false);
        this.kP = fqVar.a(this.kP, 12, false);
        this.source = fqVar.a(this.source, 13, false);
        this.kQ = fqVar.a(this.kQ, 14, false);
        this.jC = fqVar.a(this.jC, 15, false);
        this.jS = fqVar.a(this.jS, 16, false);
        this.ky = fqVar.a(17, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.iI, 0);
        frVar.a(this.kJ, 1);
        frVar.a(this.version, 2);
        if (this.kK != null) {
            frVar.a(this.kK, 3);
        }
        if (this.iq != null) {
            frVar.a(this.iq, 4);
        }
        frVar.write(this.kL, 5);
        if (this.name != null) {
            frVar.a(this.name, 6);
        }
        frVar.write(this.fL, 7);
        if (this.kM != null) {
            frVar.a(this.kM, 8);
        }
        frVar.write(this.kN, 9);
        frVar.write(this.kO, 10);
        frVar.write(this.category, 11);
        frVar.write(this.kP, 12);
        frVar.write(this.source, 13);
        frVar.write(this.kQ, 14);
        frVar.write(this.jC, 15);
        frVar.write(this.jS, 16);
        if (this.ky != null) {
            frVar.a(this.ky, 17);
        }
    }
}
