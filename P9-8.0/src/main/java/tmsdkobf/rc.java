package tmsdkobf;

import java.util.List;
import tmsdk.common.utils.f;

public class rc extends rb {
    public rc(qz qzVar) {
        super(qzVar);
    }

    protected boolean ka() {
        synchronized (this) {
            f.e("ZhongSi", "doScan start");
            if (this.Ob.jD()) {
                String[] strArr;
                String[] jE = this.Ob.jV().jE();
                if (jE != null) {
                    this.Pa.setWhitePaths(jE);
                    strArr = jE;
                    for (String str : jE) {
                        f.e("ZhongSi", "doScan white path:" + str);
                    }
                }
                strArr = this.Ob.jV().jF();
                if (strArr == null || strArr.length == 0) {
                    this.Ob.onScanError(-2);
                    return false;
                }
                String[] strArr2;
                int i;
                StringBuilder stringBuilder;
                this.Pa.setRootPaths(strArr);
                List jG = this.Ob.jV().jG();
                if (jG != null) {
                    strArr2 = new String[jG.size()];
                    for (i = 0; i < strArr2.length; i++) {
                        stringBuilder = new StringBuilder();
                        qt.a(stringBuilder, (qt) jG.get(i));
                        strArr2[i] = stringBuilder.toString();
                    }
                    this.Pa.setComRubRule(strArr2);
                }
                jG = this.Ob.jV().jH();
                if (jG != null) {
                    strArr2 = new String[jG.size()];
                    for (i = 0; i < strArr2.length; i++) {
                        stringBuilder = new StringBuilder();
                        qt.a(stringBuilder, (qt) jG.get(i));
                        strArr2[i] = stringBuilder.toString();
                    }
                    this.Pa.setOtherFilterRule(strArr2);
                }
                List<String> jZ = this.Ob.jZ();
                if (jZ != null && 1 <= jZ.size()) {
                    for (String str2 : jZ) {
                        if (!this.Pb) {
                            this.Pa.scanPath(str2, "/");
                        }
                    }
                    return true;
                }
                this.Ob.onScanError(-3);
                return false;
            }
            this.Ob.onScanError(-2);
            return false;
        }
    }

    protected void kb() {
        if (this.Ob.jR()) {
            this.Pc.bX(1);
        } else {
            this.Pc.bX(0);
        }
    }
}
