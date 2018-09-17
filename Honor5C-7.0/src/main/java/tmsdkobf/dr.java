package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class dr extends fs {
    static ArrayList<el> iN;
    public ArrayList<el> iM;

    public dr() {
        this.iM = null;
    }

    public void readFrom(fq fqVar) {
        if (iN == null) {
            iN = new ArrayList();
            iN.add(new el());
        }
        this.iM = (ArrayList) fqVar.b(iN, 1, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.iM, 1);
    }
}
