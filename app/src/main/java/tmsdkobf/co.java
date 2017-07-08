package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class co extends fs {
    static ArrayList<String> fw;
    static ArrayList<String> fx;
    public ArrayList<String> banIps;
    public ArrayList<String> banUrls;
    public int fu;
    public int fv;
    public int id;
    public String name;
    public int type;

    public co() {
        this.id = 0;
        this.type = 0;
        this.fu = 0;
        this.fv = 0;
        this.banUrls = null;
        this.banIps = null;
        this.name = "";
    }

    public void readFrom(fq fqVar) {
        this.id = fqVar.a(this.id, 0, true);
        this.type = fqVar.a(this.type, 1, true);
        this.fu = fqVar.a(this.fu, 2, true);
        this.fv = fqVar.a(this.fv, 3, true);
        if (fw == null) {
            fw = new ArrayList();
            fw.add("");
        }
        this.banUrls = (ArrayList) fqVar.b(fw, 4, true);
        if (fx == null) {
            fx = new ArrayList();
            fx.add("");
        }
        this.banIps = (ArrayList) fqVar.b(fx, 5, true);
        this.name = fqVar.a(6, true);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.id, 0);
        frVar.write(this.type, 1);
        frVar.write(this.fu, 2);
        frVar.write(this.fv, 3);
        frVar.a(this.banUrls, 4);
        frVar.a(this.banIps, 5);
        frVar.a(this.name, 6);
    }
}
