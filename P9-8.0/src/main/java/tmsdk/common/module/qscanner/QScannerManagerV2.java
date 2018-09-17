package tmsdk.common.module.qscanner;

import android.content.Context;
import java.util.List;
import tmsdk.common.module.qscanner.impl.f;
import tmsdk.fg.creator.BaseManagerF;
import tmsdkobf.kt;

public class QScannerManagerV2 extends BaseManagerF {
    public static final String LOG_TAG = "QScannerMgr";
    public static final String TAG = "TMSDK_QScannerManagerV2";
    private f BO;

    public void cancelScan() {
        this.BO.cancelScan();
    }

    public void continueScan() {
        this.BO.continueScan();
    }

    public void freeScanner() {
        this.BO.fm();
    }

    public String getVirusBaseVersion() {
        return this.BO.getVirusBaseVersion();
    }

    public int initScanner() {
        return this.BO.initScanner();
    }

    public void onCreate(Context context) {
        this.BO = new f();
        this.BO.onCreate(context);
        a(this.BO);
    }

    public void pauseScan() {
        this.BO.pauseScan();
    }

    public int scanInstalledPackages(int i, List<String> list, QScanListener qScanListener, int i2, long j) {
        tmsdk.common.utils.f.f(TAG, "scanInstalledPackages");
        kt.saveActionData(1320056);
        return this.BO.a(i, (List) list, qScanListener, i2, j);
    }

    public int scanUninstallApks(int i, List<String> list, QScanListener qScanListener, long j) {
        tmsdk.common.utils.f.f(TAG, "scanUninstallApks");
        kt.aE(1320057);
        return this.BO.a(i, (List) list, qScanListener, j);
    }
}
