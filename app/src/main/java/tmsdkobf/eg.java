package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class eg extends fs {
    static dg fF;
    static ArrayList<df> jF;
    static ArrayList<Integer> jG;
    public boolean bv;
    public int category;
    public String dexSha1;
    public int fT;
    public dg fy;
    public String jA;
    public int jB;
    public int jC;
    public int jD;
    public ArrayList<Integer> jE;
    public int jv;
    public ArrayList<df> jw;
    public int jx;
    public int jy;
    public int jz;
    public int position;

    public eg() {
        this.fy = null;
        this.jv = 0;
        this.fT = 0;
        this.jw = null;
        this.jx = 0;
        this.bv = false;
        this.category = 0;
        this.position = 0;
        this.jy = 0;
        this.jz = 0;
        this.jA = "";
        this.jB = 0;
        this.jC = 0;
        this.jD = 0;
        this.dexSha1 = "";
        this.jE = null;
    }

    public void readFrom(fq fqVar) {
        if (fF == null) {
            fF = new dg();
        }
        this.fy = (dg) fqVar.a(fF, 0, true);
        this.jv = fqVar.a(this.jv, 1, true);
        this.fT = fqVar.a(this.fT, 2, true);
        if (jF == null) {
            jF = new ArrayList();
            jF.add(new df());
        }
        this.jw = (ArrayList) fqVar.b(jF, 3, false);
        this.jx = fqVar.a(this.jx, 4, false);
        this.bv = fqVar.a(this.bv, 5, false);
        this.category = fqVar.a(this.category, 6, false);
        this.position = fqVar.a(this.position, 7, false);
        this.jy = fqVar.a(this.jy, 8, false);
        this.jz = fqVar.a(this.jz, 9, false);
        this.jA = fqVar.a(10, false);
        this.jB = fqVar.a(this.jB, 11, false);
        this.jC = fqVar.a(this.jC, 12, false);
        this.jD = fqVar.a(this.jD, 13, false);
        this.dexSha1 = fqVar.a(14, false);
        if (jG == null) {
            jG = new ArrayList();
            jG.add(Integer.valueOf(0));
        }
        this.jE = (ArrayList) fqVar.b(jG, 15, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.fy, 0);
        frVar.write(this.jv, 1);
        frVar.write(this.fT, 2);
        if (this.jw != null) {
            frVar.a(this.jw, 3);
        }
        frVar.write(this.jx, 4);
        frVar.a(this.bv, 5);
        frVar.write(this.category, 6);
        frVar.write(this.position, 7);
        frVar.write(this.jy, 8);
        frVar.write(this.jz, 9);
        if (this.jA != null) {
            frVar.a(this.jA, 10);
        }
        frVar.write(this.jB, 11);
        frVar.write(this.jC, 12);
        frVar.write(this.jD, 13);
        if (this.dexSha1 != null) {
            frVar.a(this.dexSha1, 14);
        }
        if (this.jE != null) {
            frVar.a(this.jE, 15);
        }
    }
}
