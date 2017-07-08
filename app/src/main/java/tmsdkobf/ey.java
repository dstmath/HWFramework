package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ey extends fs {
    static ArrayList<Integer> gb;
    static ArrayList<ex> lw;
    static ArrayList<cs> lx;
    static ArrayList<String> ly;
    static ArrayList<String> lz;
    public int advice;
    public int bG;
    public int category;
    public String description;
    public ArrayList<Integer> fZ;
    public int id;
    public String label;
    public int level;
    public byte ll;
    public ArrayList<ex> lm;
    public int ln;
    public int lo;
    public int lp;
    public int lq;
    public ArrayList<cs> lr;
    public int ls;
    public int lt;
    public ArrayList<String> lu;
    public ArrayList<String> lv;
    public String name;
    public int product;
    public int safelevel;
    public int timestamp;
    public String url;

    public ey() {
        this.id = 0;
        this.name = "";
        this.timestamp = 0;
        this.ll = (byte) 0;
        this.description = "";
        this.lm = null;
        this.ln = 0;
        this.advice = 0;
        this.label = "";
        this.lo = 0;
        this.lp = 0;
        this.level = 0;
        this.bG = 0;
        this.url = "";
        this.lq = 0;
        this.safelevel = 0;
        this.lr = null;
        this.product = 0;
        this.ls = 0;
        this.lt = 0;
        this.lu = null;
        this.lv = null;
        this.fZ = null;
        this.category = 0;
    }

    public void readFrom(fq fqVar) {
        this.id = fqVar.a(this.id, 0, true);
        this.name = fqVar.a(1, true);
        this.timestamp = fqVar.a(this.timestamp, 2, true);
        this.ll = (byte) fqVar.a(this.ll, 3, true);
        this.description = fqVar.a(4, true);
        if (lw == null) {
            lw = new ArrayList();
            lw.add(new ex());
        }
        this.lm = (ArrayList) fqVar.b(lw, 5, true);
        this.ln = fqVar.a(this.ln, 6, false);
        this.advice = fqVar.a(this.advice, 7, false);
        this.label = fqVar.a(8, false);
        this.lo = fqVar.a(this.lo, 9, false);
        this.lp = fqVar.a(this.lp, 10, false);
        this.level = fqVar.a(this.level, 11, false);
        this.bG = fqVar.a(this.bG, 12, false);
        this.url = fqVar.a(13, false);
        this.lq = fqVar.a(this.lq, 14, false);
        this.safelevel = fqVar.a(this.safelevel, 15, false);
        if (lx == null) {
            lx = new ArrayList();
            lx.add(new cs());
        }
        this.lr = (ArrayList) fqVar.b(lx, 16, false);
        this.product = fqVar.a(this.product, 17, false);
        this.ls = fqVar.a(this.ls, 18, false);
        this.lt = fqVar.a(this.lt, 19, false);
        if (ly == null) {
            ly = new ArrayList();
            ly.add("");
        }
        this.lu = (ArrayList) fqVar.b(ly, 20, false);
        if (lz == null) {
            lz = new ArrayList();
            lz.add("");
        }
        this.lv = (ArrayList) fqVar.b(lz, 21, false);
        if (gb == null) {
            gb = new ArrayList();
            gb.add(Integer.valueOf(0));
        }
        this.fZ = (ArrayList) fqVar.b(gb, 22, false);
        this.category = fqVar.a(this.category, 23, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.id, 0);
        frVar.a(this.name, 1);
        frVar.write(this.timestamp, 2);
        frVar.b(this.ll, 3);
        frVar.a(this.description, 4);
        frVar.a(this.lm, 5);
        frVar.write(this.ln, 6);
        frVar.write(this.advice, 7);
        if (this.label != null) {
            frVar.a(this.label, 8);
        }
        frVar.write(this.lo, 9);
        frVar.write(this.lp, 10);
        frVar.write(this.level, 11);
        frVar.write(this.bG, 12);
        if (this.url != null) {
            frVar.a(this.url, 13);
        }
        frVar.write(this.lq, 14);
        frVar.write(this.safelevel, 15);
        if (this.lr != null) {
            frVar.a(this.lr, 16);
        }
        frVar.write(this.product, 17);
        frVar.write(this.ls, 18);
        frVar.write(this.lt, 19);
        if (this.lu != null) {
            frVar.a(this.lu, 20);
        }
        if (this.lv != null) {
            frVar.a(this.lv, 21);
        }
        if (this.fZ != null) {
            frVar.a(this.fZ, 22);
        }
        frVar.write(this.category, 23);
    }
}
