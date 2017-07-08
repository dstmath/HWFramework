package tmsdk.fg.module.qscanner;

import android.content.Context;
import android.util.SparseArray;
import java.util.ArrayList;
import tmsdk.common.module.qscanner.QScanConstants;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdkobf.ea;

/* compiled from: Unknown */
final class f {
    private static f Ms;
    private SparseArray<a> Mt;
    private Context mContext;

    private f(Context context) {
        this.Mt = new SparseArray();
        this.mContext = context;
    }

    static f K(Context context) {
        if (Ms == null) {
            synchronized (f.class) {
                if (Ms == null) {
                    Ms = new f(context);
                }
            }
        }
        Ms.ji();
        return Ms;
    }

    private void ji() {
        for (ea eaVar : SystemScanConfigManager.M(this.mContext).jo()) {
            switch (eaVar.id) {
                case QScanConstants.SPECIAL_KUNGFU_VIRUS /*110001*/:
                    this.Mt.append(QScanConstants.SPECIAL_KUNGFU_VIRUS, new e(eaVar));
                    break;
                default:
                    break;
            }
        }
    }

    ArrayList<QScanResultEntity> a(QScanListenerV2 qScanListenerV2, b bVar) {
        ArrayList<QScanResultEntity> arrayList = new ArrayList();
        if (this.Mt.size() == 0) {
            return arrayList;
        }
        int size = this.Mt.size();
        for (int i = 0; i < size; i++) {
            if (bVar != null && bVar.fH()) {
                return arrayList;
            }
            QScanResultEntity je = ((a) this.Mt.valueAt(i)).je();
            if (je != null) {
                arrayList.add(je);
                if (qScanListenerV2 != null) {
                    qScanListenerV2.onScanProgress(4, ((i + 1) * 100) / size, je);
                }
            }
        }
        return arrayList;
    }

    boolean handleSpecial(QScanResultEntity qScanResultEntity) {
        if (qScanResultEntity == null) {
            return false;
        }
        a aVar = (a) this.Mt.get(qScanResultEntity.special);
        return aVar != null ? aVar.b(qScanResultEntity) : false;
    }
}
