package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ct extends fs {
    static ArrayList<co> ga;
    static ArrayList<Integer> gb;
    public int category;
    public String fR;
    public String fS;
    public int fT;
    public String fU;
    public String fV;
    public int fW;
    public int fX;
    public String fY;
    public ArrayList<Integer> fZ;
    public int fr;
    public String officialCertMd5;
    public String officialPackName;
    public ArrayList<co> plugins;
    public int product;
    public int safeLevel;

    public ct() {
        this.fr = 0;
        this.fR = "";
        this.fS = "";
        this.fT = 0;
        this.fU = "";
        this.fV = "";
        this.fW = 0;
        this.fX = 0;
        this.fY = "";
        this.safeLevel = 0;
        this.product = 0;
        this.plugins = null;
        this.fZ = null;
        this.category = 0;
        this.officialPackName = "";
        this.officialCertMd5 = "";
    }

    public void readFrom(fq fqVar) {
        this.fr = fqVar.a(this.fr, 0, true);
        this.fR = fqVar.a(1, false);
        this.fS = fqVar.a(2, false);
        this.fT = fqVar.a(this.fT, 3, false);
        this.fU = fqVar.a(4, false);
        this.fV = fqVar.a(5, false);
        this.fW = fqVar.a(this.fW, 6, false);
        this.fX = fqVar.a(this.fX, 7, false);
        this.fY = fqVar.a(8, false);
        this.safeLevel = fqVar.a(this.safeLevel, 9, false);
        this.product = fqVar.a(this.product, 10, false);
        if (ga == null) {
            ga = new ArrayList();
            ga.add(new co());
        }
        this.plugins = (ArrayList) fqVar.b(ga, 11, false);
        if (gb == null) {
            gb = new ArrayList();
            gb.add(Integer.valueOf(0));
        }
        this.fZ = (ArrayList) fqVar.b(gb, 12, false);
        this.category = fqVar.a(this.category, 13, false);
        this.officialPackName = fqVar.a(14, false);
        this.officialCertMd5 = fqVar.a(15, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.fr, 0);
        if (this.fR != null) {
            frVar.a(this.fR, 1);
        }
        if (this.fS != null) {
            frVar.a(this.fS, 2);
        }
        frVar.write(this.fT, 3);
        if (this.fU != null) {
            frVar.a(this.fU, 4);
        }
        if (this.fV != null) {
            frVar.a(this.fV, 5);
        }
        frVar.write(this.fW, 6);
        frVar.write(this.fX, 7);
        if (this.fY != null) {
            frVar.a(this.fY, 8);
        }
        frVar.write(this.safeLevel, 9);
        frVar.write(this.product, 10);
        if (this.plugins != null) {
            frVar.a(this.plugins, 11);
        }
        if (this.fZ != null) {
            frVar.a(this.fZ, 12);
        }
        frVar.write(this.category, 13);
        if (this.officialPackName != null) {
            frVar.a(this.officialPackName, 14);
        }
        if (this.officialCertMd5 != null) {
            frVar.a(this.officialCertMd5, 15);
        }
    }
}
