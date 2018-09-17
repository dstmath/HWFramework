package tmsdk.fg.module.qscanner;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.module.qscanner.QScanAdBehaviorInfo;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.common.utils.d;
import tmsdk.common.utils.m;
import tmsdk.fg.creator.BaseManagerF;
import tmsdkobf.jg;
import tmsdkobf.ma;

/* compiled from: Unknown */
public class QScannerManagerV2 extends BaseManagerF {
    private d LX;

    public void cancelScan() {
        if (!jg.cl()) {
            d.g("QScannerManagerV2", "Impl--cancelScan");
            this.LX.cancelScan();
        }
    }

    public QScanResultEntity certCheckInstalledPackage(String str) {
        return !jg.cl() ? this.LX.certCheckInstalledPackage(str) : new QScanResultEntity();
    }

    public void continueScan() {
        if (!jg.cl()) {
            d.g("QScannerManagerV2", "Impl--continueScan");
            this.LX.continueScan();
        }
    }

    public void freeScanner() {
        d.g("QScannerManagerV2", "freeScanner");
        if (!jg.cl()) {
            this.LX.freeScanner();
        }
    }

    public String getVirusBaseVersion(Context context) {
        return this.LX.getVirusBaseVersion(context);
    }

    public boolean handleSpecial(QScanResultEntity qScanResultEntity) {
        return !jg.cl() ? this.LX.handleSpecial(qScanResultEntity) : false;
    }

    public boolean handleSystemFlaw(QScanResultEntity qScanResultEntity) {
        return !jg.cl() ? this.LX.handleSystemFlaw(qScanResultEntity) : false;
    }

    public int initScanner() {
        d.g("QScannerManagerV2", "initScanner");
        m.wakeup();
        return !jg.cl() ? this.LX.initScanner() : -100;
    }

    public List<QScanAdBehaviorInfo> loadBehaviorConfig() {
        return this.LX.loadBehaviorConfig();
    }

    public List<QScanResultEntity> nativeScanSpecials(QScanListenerV2 qScanListenerV2) {
        if (jg.cl()) {
            return new ArrayList(0);
        }
        d.g("QScannerManagerV2", "Impl--nativeScanSpecials");
        return this.LX.b(qScanListenerV2);
    }

    public List<QScanResultEntity> nativeScanSystemFlaws(QScanListenerV2 qScanListenerV2) {
        if (jg.cl()) {
            return new ArrayList(0);
        }
        d.g("QScannerManagerV2", "Impl--nativeScanSystemFlaws");
        return this.LX.a(qScanListenerV2);
    }

    public void onCreate(Context context) {
        this.LX = new d();
        this.LX.onCreate(context);
        a(this.LX);
    }

    public void pauseScan() {
        if (!jg.cl()) {
            d.g("QScannerManagerV2", "Impl--pauseScan");
            this.LX.pauseScan();
        }
    }

    public void scanInstalledPackages(QScanListenerV2 qScanListenerV2, boolean z) {
        ma.bx(29952);
        if (!jg.cl()) {
            d.g("QScannerManagerV2", "Impl--scanPackagesOrApks");
            this.LX.a(qScanListenerV2, z, true);
        }
    }

    public void scanSelectedApks(List<String> list, QScanListenerV2 qScanListenerV2, boolean z) {
        if (!jg.cl()) {
            d.g("QScannerManagerV2", "Impl--scanSelectedPackagesOrApks");
            this.LX.a((List) list, qScanListenerV2, z, false);
        }
    }

    public void scanSelectedPackages(List<String> list, QScanListenerV2 qScanListenerV2, boolean z) {
        ma.bx(29952);
        if (!jg.cl()) {
            d.g("QScannerManagerV2", "Impl--scanSelectedPackagesOrApks");
            this.LX.a((List) list, qScanListenerV2, z, true);
        }
    }

    public void scanUninstalledApks(QScanListenerV2 qScanListenerV2, boolean z) {
        ma.bx(29952);
        if (!jg.cl()) {
            d.g("QScannerManagerV2", "Impl--scanPackagesOrApks");
            this.LX.a(qScanListenerV2, z, false);
        }
    }
}
