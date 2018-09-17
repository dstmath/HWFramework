package tmsdkobf;

import java.util.List;
import java.util.Map;

public class rd extends rb {
    public rd(qz qzVar) {
        super(qzVar);
    }

    protected boolean ka() {
        synchronized (this) {
            Map cX = this.Ob.jV().cX(this.Ob.jU());
            if (cX == null || cX.size() == 0) {
                this.Ob.onScanError(-2);
                return false;
            }
            String[] strArr;
            String[] strArr2 = (String[]) cX.keySet().toArray(new String[0]);
            this.Pa.setRootPaths(strArr2);
            List jG = this.Ob.jV().jG();
            if (jG != null) {
                strArr = new String[jG.size()];
                for (int i = 0; i < strArr.length; i++) {
                    StringBuilder stringBuilder = new StringBuilder();
                    qt.a(stringBuilder, (qt) jG.get(i));
                    strArr[i] = stringBuilder.toString();
                }
                this.Pa.setComRubRule(strArr);
            }
            List jH = this.Ob.jV().jH();
            if (jH != null) {
                strArr = new String[jH.size()];
                for (int i2 = 0; i2 < strArr.length; i2++) {
                    StringBuilder stringBuilder2 = new StringBuilder();
                    qt.a(stringBuilder2, (qt) jH.get(i2));
                    strArr[i2] = stringBuilder2.toString();
                }
                this.Pa.setOtherFilterRule(strArr);
            }
            List<String> jZ = this.Ob.jZ();
            if (jZ != null && 1 <= jZ.size()) {
                String[] strArr3 = strArr2;
                for (String str : strArr2) {
                    for (String str2 : jZ) {
                        if (!this.Pb) {
                            this.Pa.scanPath(str2, str);
                        }
                    }
                }
                return true;
            }
            this.Ob.onScanError(-3);
            return false;
        }
    }

    protected void kb() {
        this.Pc.bX(2);
    }
}
