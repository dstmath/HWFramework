package tmsdk.fg.module.qscanner;

import tmsdkobf.fq;
import tmsdkobf.fr;
import tmsdkobf.fs;

/* compiled from: Unknown */
public final class QScanRecord extends fs {
    static QScanResult LT;
    public long id;
    public QScanResult result;
    public int state;

    public QScanRecord() {
        this.id = 0;
        this.result = null;
        this.state = 0;
    }

    public QScanRecord(long j, QScanResult qScanResult, int i) {
        this.id = 0;
        this.result = null;
        this.state = 0;
        this.id = j;
        this.result = qScanResult;
        this.state = i;
    }

    public void readFrom(fq fqVar) {
        this.id = fqVar.a(this.id, 0, true);
        if (LT == null) {
            LT = new QScanResult();
        }
        this.result = (QScanResult) fqVar.a(LT, 1, true);
        this.state = fqVar.a(this.state, 2, false);
    }

    public void writeTo(fr frVar) {
        frVar.b(this.id, 0);
        frVar.a(this.result, 1);
        frVar.write(this.state, 2);
    }
}
