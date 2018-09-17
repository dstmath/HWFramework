package tmsdk.fg.module.qscanner;

import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.lang.MultiLangManager;
import tmsdk.common.module.qscanner.QScanAdBehaviorInfo;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.tcc.QFile;
import tmsdk.common.tcc.QSdcardScanner;
import tmsdk.common.tcc.QSdcardScanner.ProgressListener;
import tmsdk.common.tcc.SdcardScannerFactory;
import tmsdk.common.utils.l;
import tmsdk.common.utils.n;
import tmsdk.fg.creator.BaseManagerF;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;
import tmsdkobf.cp;
import tmsdkobf.ct;
import tmsdkobf.cw;
import tmsdkobf.cx;
import tmsdkobf.ee;
import tmsdkobf.er;
import tmsdkobf.ew;
import tmsdkobf.jw;
import tmsdkobf.ka;
import tmsdkobf.kb;
import tmsdkobf.ly;
import tmsdkobf.ma;
import tmsdkobf.ms;
import tmsdkobf.mw;
import tmsdkobf.nb;
import tmsdkobf.nj;
import tmsdkobf.py;
import tmsdkobf.qa;
import tmsdkobf.qt;
import tmsdkobf.qv.a;
import tmsdkobf.qw;
import tmsdkobf.qx;

/* compiled from: Unknown */
final class d extends BaseManagerF {
    private String LS;
    private AmScannerStatic LY;
    private c LZ;
    private ka Ma;
    private MultiLangManager Mb;
    private byte[] Mc;
    private boolean Md;
    private Object Me;
    private boolean Mf;
    private boolean Mg;
    private Object Mh;
    private String[] Mi;
    private Context mContext;
    private boolean mPaused;
    private qa pi;

    /* compiled from: Unknown */
    /* renamed from: tmsdk.fg.module.qscanner.d.1 */
    class AnonymousClass1 implements a {
        final /* synthetic */ QScanListenerV2 Mj;
        final /* synthetic */ ArrayList Mk;
        final /* synthetic */ d Ml;

        AnonymousClass1(d dVar, QScanListenerV2 qScanListenerV2, ArrayList arrayList) {
            this.Ml = dVar;
            this.Mj = qScanListenerV2;
            this.Mk = arrayList;
        }

        public void onFound(int i, QFile qFile) {
            tmsdk.common.utils.d.e("QScannerManagerV2", "onFound-ruleId:[" + i + "][" + qFile.filePath + "]");
            this.Ml.a(1, this.Mj);
            if (!this.Ml.b(1, this.Mj)) {
                py pyVar = new py();
                pyVar.O(true);
                pyVar.aS(qFile.filePath);
                QScanResultEntity a = this.Ml.d(pyVar);
                if (a != null) {
                    this.Mk.add(a);
                    if (this.Mj != null) {
                        this.Mj.onScanProgress(1, -1, a);
                    }
                }
            }
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdk.fg.module.qscanner.d.2 */
    class AnonymousClass2 implements ProgressListener {
        final /* synthetic */ QScanListenerV2 Mj;
        final /* synthetic */ d Ml;
        final /* synthetic */ List Mm;

        AnonymousClass2(d dVar, List list, QScanListenerV2 qScanListenerV2) {
            this.Ml = dVar;
            this.Mm = list;
            this.Mj = qScanListenerV2;
        }

        public boolean onScanPathChange(String str) {
            String cG = nb.cG(str);
            if (this.Mm.contains(cG)) {
                return false;
            }
            this.Mm.add(cG);
            this.Ml.a(1, this.Mj);
            if (this.Ml.b(1, this.Mj)) {
                return false;
            }
            if (this.Ml.Mi != null) {
                for (String str2 : this.Ml.Mi) {
                    if (str.endsWith("/" + str2)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdk.fg.module.qscanner.d.3 */
    class AnonymousClass3 implements b {
        final /* synthetic */ QScanListenerV2 Mj;
        final /* synthetic */ d Ml;

        AnonymousClass3(d dVar, QScanListenerV2 qScanListenerV2) {
            this.Ml = dVar;
            this.Mj = qScanListenerV2;
        }

        public boolean fH() {
            this.Ml.a(3, this.Mj);
            return this.Ml.b(3, this.Mj);
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdk.fg.module.qscanner.d.4 */
    class AnonymousClass4 implements b {
        final /* synthetic */ QScanListenerV2 Mj;
        final /* synthetic */ d Ml;

        AnonymousClass4(d dVar, QScanListenerV2 qScanListenerV2) {
            this.Ml = dVar;
            this.Mj = qScanListenerV2;
        }

        public boolean fH() {
            this.Ml.a(4, this.Mj);
            return this.Ml.b(4, this.Mj);
        }
    }

    d() {
        this.Mc = new byte[0];
        this.mPaused = false;
        this.Me = new Object();
        this.Mf = false;
        this.Mg = false;
        this.Mh = new Object();
        this.Mi = new String[]{"image", "icon", "photo", "music", "dcim", "weibo"};
    }

    private void D(List<cp> list) {
        Object arrayList = new ArrayList();
        for (cp cpVar : list) {
            if (cpVar.b() != 0) {
                arrayList.add(new ee(cpVar.fy, cpVar.fE));
            }
        }
        if (arrayList.size() > 0) {
            ms.a(this.mContext, arrayList, "label_sa_cfg", "sa_cfg.dat");
        }
    }

    private ArrayList<QScanResultEntity> a(int i, List<py> list, QScanListenerV2 qScanListenerV2, boolean z) {
        List arrayList = new ArrayList();
        tmsdk.common.utils.d.e("QScannerManagerV2", "nativeScan-size:[" + list.size() + "]");
        if (list == null || list.size() == 0) {
            tmsdk.common.utils.d.c("QScannerManagerV2", "appList == null || appList.size() == 0");
            return arrayList;
        }
        if (qScanListenerV2 != null) {
            tmsdk.common.utils.d.g("QScannerManagerV2", "onScanStarted-type:[" + i + "]");
            qScanListenerV2.onScanStarted(i);
        }
        synchronized (this.Mc) {
            this.Md = true;
        }
        int size = list.size();
        for (int i2 = 0; i2 < size; i2++) {
            a(i, qScanListenerV2);
            if (b(i, qScanListenerV2)) {
                tmsdk.common.utils.d.f("QScannerManagerV2", "isCanceled:[" + i2 + "]");
                break;
            }
            long currentTimeMillis = System.currentTimeMillis();
            QScanResultEntity d = d((py) list.get(i2));
            if (d != null) {
                arrayList.add(d);
                if (qScanListenerV2 != null) {
                    qScanListenerV2.onScanProgress(i, ((i2 + 1) * 100) / size, d);
                }
            }
            currentTimeMillis = System.currentTimeMillis() - currentTimeMillis;
            if ((currentTimeMillis >= 20 ? 1 : null) == null) {
                try {
                    tmsdk.common.utils.d.e("QScannerManagerV2", "beg sleep:[" + (20 - currentTimeMillis) + "]");
                    Thread.sleep(20 - currentTimeMillis);
                    tmsdk.common.utils.d.e("QScannerManagerV2", "end sleep:[" + (20 - currentTimeMillis) + "]");
                } catch (InterruptedException e) {
                    tmsdk.common.utils.d.e("QScannerManagerV2", "sleep InterruptedException:[" + e + "");
                }
            }
        }
        synchronized (this.Mh) {
            if (!this.Mf) {
                a(i, qScanListenerV2, arrayList, z);
            }
        }
        synchronized (this.Mc) {
            this.Md = false;
        }
        return arrayList;
    }

    private ArrayList<QScanResultEntity> a(int i, QScanListenerV2 qScanListenerV2, boolean z) {
        tmsdk.common.utils.d.e("QScannerManagerV2", "nativeScanInstalledPackages");
        List arrayList = new ArrayList();
        ArrayList jg = jg();
        if (jg != null) {
            String packageName = this.mContext.getPackageName();
            Iterator it = jg.iterator();
            while (it.hasNext()) {
                py pyVar = (py) it.next();
                if (pyVar != null) {
                    if (packageName != null) {
                        if (packageName.equals(pyVar.getPackageName())) {
                        }
                    }
                    arrayList.add(pyVar);
                }
            }
        }
        return a(i, arrayList, qScanListenerV2, z);
    }

    private List<QScanResultEntity> a(int i, List<QScanResultEntity> list, QScanListenerV2 qScanListenerV2) {
        tmsdk.common.utils.d.e("QScannerManagerV2", "cloudScan");
        ma.bx(29953);
        if (qScanListenerV2 != null) {
            tmsdk.common.utils.d.g("QScannerManagerV2", "onScanStarted-QScanConstants.SCAN_CLOUD");
            qScanListenerV2.onScanStarted(2);
        }
        if (jw.cH().cJ()) {
            a(2, qScanListenerV2);
        } else {
            a(2, qScanListenerV2);
        }
        if (b(2, qScanListenerV2)) {
            return list;
        }
        synchronized (this.Mc) {
            this.Md = true;
        }
        int i2 = i(this.mContext, this.LS);
        List arrayList = new ArrayList();
        for (QScanResultEntity a : list) {
            arrayList.add(kb.a(a, i2));
        }
        List arrayList2 = new ArrayList();
        int a2 = ((qt) ManagerCreatorC.getManager(qt.class)).a(arrayList, arrayList2, this.Mb.getCurrentLang());
        a(2, qScanListenerV2);
        if (b(2, qScanListenerV2)) {
            synchronized (this.Mc) {
                this.Md = false;
            }
            a(i, qScanListenerV2, (List) list, true);
            return list;
        }
        if (a2 == 0) {
            a((List) list, (ArrayList) arrayList2);
            a((List) list, arrayList2);
            D(arrayList2);
        } else if (qScanListenerV2 != null) {
            tmsdk.common.utils.d.g("QScannerManagerV2", "onScanError-QScanConstants.SCAN_CLOUD:[" + a2 + "]");
            qScanListenerV2.onScanError(2, a2);
        }
        a(i, qScanListenerV2, (List) list, true);
        synchronized (this.Mc) {
            this.Md = false;
        }
        ly.ep();
        return list;
    }

    private void a(int i, QScanListenerV2 qScanListenerV2) {
        synchronized (this.Me) {
            try {
                if (this.mPaused) {
                    if (qScanListenerV2 != null) {
                        tmsdk.common.utils.d.g("QScannerManagerV2", "onScanPaused:[" + i + "]");
                        qScanListenerV2.onScanPaused(i);
                    }
                    this.Me.wait();
                    if (qScanListenerV2 != null) {
                        tmsdk.common.utils.d.g("QScannerManagerV2", "onScanContinue:[" + i + "]");
                        qScanListenerV2.onScanContinue(i);
                    }
                    this.mPaused = false;
                }
            } catch (InterruptedException e) {
                tmsdk.common.utils.d.c("QScannerManagerV2", e.getMessage());
            }
        }
    }

    private void a(int i, QScanListenerV2 qScanListenerV2, List<QScanResultEntity> list, boolean z) {
        synchronized (this.Me) {
            this.mPaused = false;
        }
        synchronized (this.Mh) {
            this.Mf = false;
            this.Mg = false;
        }
        if (qScanListenerV2 != null && z) {
            Object arrayList = new ArrayList();
            if (list != null && list.size() > 0) {
                for (QScanResultEntity qScanResultEntity : list) {
                    if (!(qScanResultEntity.safeLevel == 1 || qScanResultEntity.safeLevel == 2 || qScanResultEntity.safeLevel == 3)) {
                        if (qScanResultEntity.plugins != null) {
                            if (qScanResultEntity.plugins.size() <= 0) {
                            }
                        }
                    }
                    arrayList.add(qScanResultEntity);
                }
            }
            tmsdk.common.utils.d.g("QScannerManagerV2", "onScanFinished-type[" + i + "]size:[" + arrayList.size() + "]");
            qScanListenerV2.onScanFinished(i, arrayList);
        }
    }

    private void a(List<QScanResultEntity> list, ArrayList<cp> arrayList) {
        for (QScanResultEntity qScanResultEntity : list) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                cp cpVar = (cp) it.next();
                if (qScanResultEntity.size == cpVar.fy.fileSize && qScanResultEntity.packageName.equals(cpVar.fy.ip)) {
                    if (qScanResultEntity.type == cpVar.fA.fr && qScanResultEntity.advice == cpVar.fA.fW && qScanResultEntity.category == cpVar.fA.category && cpVar.fA.fr == 0) {
                        if (cpVar.fA.plugins != null) {
                            if (cpVar.fA.plugins.size() <= 0) {
                            }
                        }
                    }
                    tmsdk.common.utils.d.d("QScannerManagerV2", "cloudAnalyse: " + qScanResultEntity.packageName + ", category: " + qScanResultEntity.category + " - " + cpVar.fA.category);
                    ct ctVar = cpVar.fA;
                    qScanResultEntity.type = ctVar.fr;
                    qScanResultEntity.category = ctVar.category;
                    qScanResultEntity.officialPackName = ctVar.officialPackName;
                    qScanResultEntity.officialCertMd5 = ctVar.officialCertMd5;
                    if (qScanResultEntity.type == 9) {
                        qScanResultEntity.isInPayList = true;
                    } else if (qScanResultEntity.type == 10) {
                        qScanResultEntity.isInStealAccountList = true;
                    }
                    qScanResultEntity.advice = ctVar.fW;
                    qScanResultEntity.name = ctVar.fR;
                    qScanResultEntity.discription = ctVar.fU;
                    qScanResultEntity.malwareid = ctVar.fX;
                    qScanResultEntity.url = ctVar.fY;
                    qScanResultEntity.safeLevel = ctVar.safeLevel;
                    qScanResultEntity.product = ctVar.product;
                    qScanResultEntity.official = cpVar.fz.official;
                    qScanResultEntity.plugins = kb.l(ctVar.plugins);
                    if (this.LZ != null) {
                        this.LZ.a(qScanResultEntity.packageName, qScanResultEntity.size, qScanResultEntity);
                    }
                }
            }
        }
    }

    private void a(List<QScanResultEntity> list, List<cp> list2) {
        for (cp cpVar : list2) {
            er a = cpVar.a();
            if (!(a == null || a.g() == null || a.g().size() <= 0)) {
                for (QScanResultEntity qScanResultEntity : list) {
                    if (qScanResultEntity.packageName.equals(cpVar.fy.ip) && qScanResultEntity.size == cpVar.fy.fileSize) {
                        mw mwVar = new mw(this.mContext);
                        mwVar.setUrl("http://uploadserver.3g.qq.com/upload_v3");
                        mwVar.a(qScanResultEntity.path, a);
                    }
                }
            }
        }
    }

    private ArrayList<QScanResultEntity> b(int i, QScanListenerV2 qScanListenerV2, boolean z) {
        String[] strArr;
        tmsdk.common.utils.d.e("QScannerManagerV2", "nativeScanUninstalledApks");
        List arrayList = new ArrayList();
        List arrayList2 = new ArrayList();
        qx qxVar = new qx();
        String[] strArr2 = new String[]{"apk"};
        List p = ms.p(TMSDKContext.getApplicaionContext());
        tmsdk.common.utils.d.e("QScannerManagerV2", "sdcard path:[" + p + "]");
        if (p.size() > 0) {
            strArr = new String[]{"/storage_int", "/HWUserData"};
        } else {
            strArr = new String[]{"/storage/emulated/0", "/storage_int", "/HWUserData"};
            p = ms.eY();
        }
        qxVar.KG.add(new qw(0, null, strArr2));
        qxVar.KH = strArr;
        QSdcardScanner qSdcardScanner = SdcardScannerFactory.getQSdcardScanner(2, new AnonymousClass1(this, qScanListenerV2, arrayList), qxVar);
        qSdcardScanner.registerProgressListener(9999, new AnonymousClass2(this, arrayList2, qScanListenerV2));
        if (qScanListenerV2 != null) {
            tmsdk.common.utils.d.g("QScannerManagerV2", "onScanStarted-QScanConstants.SCAN_UNINSTALLEDAPKS");
            qScanListenerV2.onScanStarted(1);
        }
        synchronized (this.Mc) {
            this.Md = true;
        }
        for (String str : r1) {
            tmsdk.common.utils.d.e("QScannerManagerV2", "startScan[" + str + "]");
            qSdcardScanner.startScan(str);
        }
        synchronized (this.Mh) {
            if (!this.Mf) {
                a(1, qScanListenerV2, arrayList, z);
            }
        }
        synchronized (this.Mc) {
            this.Md = false;
        }
        arrayList2.clear();
        qSdcardScanner.release();
        return arrayList;
    }

    private boolean b(int i, QScanListenerV2 qScanListenerV2) {
        synchronized (this.Mh) {
            if (!this.Mf) {
                return false;
            }
            if (!(qScanListenerV2 == null || this.Mg)) {
                tmsdk.common.utils.d.g("QScannerManagerV2", "onScanCanceled:[" + i + "]");
                qScanListenerV2.onScanCanceled(i);
                this.Mg = true;
            }
            return true;
        }
    }

    private ArrayList<QScanResultEntity> c(int i, List<String> list, QScanListenerV2 qScanListenerV2, boolean z) {
        if (list == null || list.size() == 0) {
            return new ArrayList();
        }
        List arrayList = new ArrayList(list.size());
        for (String str : list) {
            py pyVar = new py();
            pyVar.O(true);
            pyVar.aS(str);
            arrayList.add(pyVar);
        }
        return a(i, arrayList, qScanListenerV2, z);
    }

    private ApkKey c(py pyVar, int i) {
        return pyVar != null ? new ApkKey(l.dk(pyVar.getPackageName()), l.dk(pyVar.getAppName()), l.dk(pyVar.hD()), l.dk(pyVar.getVersion()), pyVar.hB(), (int) pyVar.getSize(), l.dk(pyVar.aZ()), i) : null;
    }

    private QScanResultEntity d(py pyVar) {
        int i = 0;
        String str = null;
        if (pyVar != null) {
            py e = e(pyVar);
            if (e != null) {
                if (e.hG()) {
                    i = 2;
                } else if (e.hA()) {
                    i = 1;
                }
                if (this.LZ == null) {
                    return null;
                }
                QScanResultEntity a = this.LZ.a(e.getPackageName(), e.aZ(), e.getSize(), i);
                if (a != null) {
                    tmsdk.common.utils.d.e("QScannerManagerV2", "nativeScan, from cache:[" + e.getPackageName() + "]");
                }
                if (a == null) {
                    py f = f(e);
                    if (f != null) {
                        ApkKey c = c(f, i);
                        QScanResult scanApk = this.LY.scanApk(c);
                        a = kb.a(scanApk);
                        if (a == null) {
                            tmsdk.common.utils.d.c("QScannerManagerV2", "nativeScan, result2ResultEntity == null");
                        } else {
                            a = a == null ? null : this.Ma.a(a);
                        }
                        if (scanApk != null) {
                            str = scanApk.apkkey.certMd5;
                        }
                        c.certMd5 = str;
                        this.LZ.a(c, a);
                    } else {
                        tmsdk.common.utils.d.c("QScannerManagerV2", "nativeScan, loadAppDetailInfo == null");
                        return a;
                    }
                }
                return a;
            }
            tmsdk.common.utils.d.c("QScannerManagerV2", "nativeScan, loadAppSimpleInfo == null");
            return null;
        }
        tmsdk.common.utils.d.c("QScannerManagerV2", "nativeScan, appEntity == null");
        return null;
    }

    private py e(py pyVar) {
        return !pyVar.hG() ? TMServiceFactory.getSystemInfoService().a(pyVar, 73) : this.pi.b(pyVar, 9);
    }

    private py f(py pyVar) {
        return !pyVar.hG() ? TMServiceFactory.getSystemInfoService().a(pyVar, 16) : pyVar;
    }

    static int i(Context context, String str) {
        tmsdk.common.utils.d.e("QScannerManagerV2", "getVirusBaseIntVersion-amfFilePath:[" + str + "]");
        ew h = n.h(context, str);
        return h == null ? 0 : h.h();
    }

    private ArrayList<py> jg() {
        return TMServiceFactory.getSystemInfoService().c(2, 2);
    }

    ArrayList<QScanResultEntity> a(QScanListenerV2 qScanListenerV2) {
        b anonymousClass3 = new AnonymousClass3(this, qScanListenerV2);
        if (qScanListenerV2 != null) {
            tmsdk.common.utils.d.g("QScannerManagerV2", "onScanStarted-QScanConstants.SCAN_SYSTEMFLAWS");
            qScanListenerV2.onScanStarted(3);
        }
        synchronized (this.Mc) {
            this.Md = true;
        }
        List b = g.L(this.mContext).b(qScanListenerV2, anonymousClass3);
        synchronized (this.Mh) {
            if (!this.Mf) {
                a(3, qScanListenerV2, b, true);
            }
        }
        synchronized (this.Mc) {
            this.Md = false;
        }
        return b;
    }

    void a(List<String> list, QScanListenerV2 qScanListenerV2, boolean z, boolean z2) {
        tmsdk.common.utils.d.e("QScannerManagerV2", "[Beg]scanSelectedPackagesOrApks-size:[" + list.size() + "]bCloudScan[" + z + "]bPackages[" + z2 + "]");
        boolean z3 = !z;
        List b;
        if (z2) {
            b = b(0, list, qScanListenerV2, z3);
            if (!(b == null || !z || b(0, null))) {
                a(0, b, qScanListenerV2);
            }
        } else {
            b = c(1, list, qScanListenerV2, z3);
            if (!(b == null || !z || b(1, null))) {
                a(1, b, qScanListenerV2);
            }
        }
        tmsdk.common.utils.d.e("QScannerManagerV2", "[End]scanSelectedPackagesOrApks");
    }

    void a(QScanListenerV2 qScanListenerV2, boolean z, boolean z2) {
        tmsdk.common.utils.d.e("QScannerManagerV2", "[Beg]scanPackagesOrApks-scanListener:[" + qScanListenerV2 + "]bCloudScan[" + z + "]bPackages[" + z2 + "]");
        synchronized (this.Me) {
            this.mPaused = false;
        }
        synchronized (this.Mh) {
            this.Mf = false;
            this.Mg = false;
        }
        boolean z3 = !z;
        List a;
        if (z2) {
            a = a(0, qScanListenerV2, z3);
            if (!(a == null || !z || b(0, null))) {
                a(0, a, qScanListenerV2);
            }
        } else {
            a = b(1, qScanListenerV2, z3);
            if (!(a == null || !z || b(1, null))) {
                a(1, a, qScanListenerV2);
            }
        }
        tmsdk.common.utils.d.e("QScannerManagerV2", "[End]scanPackagesOrApks");
    }

    ArrayList<QScanResultEntity> b(int i, List<String> list, QScanListenerV2 qScanListenerV2, boolean z) {
        if (list == null || list.size() == 0) {
            return new ArrayList();
        }
        List arrayList = new ArrayList(list.size());
        for (String str : list) {
            py pyVar = new py();
            pyVar.cS(str);
            arrayList.add(pyVar);
        }
        return a(i, arrayList, qScanListenerV2, z);
    }

    ArrayList<QScanResultEntity> b(QScanListenerV2 qScanListenerV2) {
        b anonymousClass4 = new AnonymousClass4(this, qScanListenerV2);
        if (qScanListenerV2 != null) {
            tmsdk.common.utils.d.g("QScannerManagerV2", "onScanStarted-QScanConstants.SCAN_SPECIALS");
            qScanListenerV2.onScanStarted(4);
        }
        synchronized (this.Mc) {
            this.Md = true;
        }
        List a = f.K(this.mContext).a(qScanListenerV2, anonymousClass4);
        synchronized (this.Mh) {
            if (!this.Mf) {
                a(4, qScanListenerV2, a, true);
            }
        }
        synchronized (this.Mc) {
            this.Md = false;
        }
        return a;
    }

    void cancelScan() {
        synchronized (this.Mh) {
            this.Mf = true;
        }
        synchronized (this.Me) {
            this.Me.notifyAll();
        }
    }

    public QScanResultEntity certCheckInstalledPackage(String str) {
        QScanResultEntity qScanResultEntity = new QScanResultEntity();
        qScanResultEntity.packageName = str;
        qScanResultEntity.safeLevel = 0;
        py b = TMServiceFactory.getSystemInfoService().b(str, 16);
        if (b == null) {
            return qScanResultEntity;
        }
        qScanResultEntity.certMd5 = b.hD();
        if (this.Ma == null) {
            this.Ma = new ka(this.mContext);
            this.Ma.cN();
        }
        return this.Ma.a(qScanResultEntity);
    }

    void continueScan() {
        synchronized (this.Me) {
            this.Me.notifyAll();
            this.mPaused = false;
        }
    }

    void freeScanner() {
        tmsdk.common.utils.d.e("QScannerManagerV2", "[Beg]Impl-freeScanner");
        synchronized (this.Mc) {
            if (this.Md) {
                tmsdk.common.utils.d.e("QScannerManagerV2", "mIsScanning:[true]");
                return;
            }
            if (this.LZ != null) {
                tmsdk.common.utils.d.e("QScannerManagerV2", "QScanCacheManager exit");
                this.LZ.jf();
                this.LZ = null;
            }
            if (this.LY != null) {
                tmsdk.common.utils.d.e("QScannerManagerV2", "AmScannerStatic finalize");
                this.LY.finalize();
                this.LY = null;
            }
            if (this.Ma != null) {
                tmsdk.common.utils.d.e("QScannerManagerV2", "CertCheckerV2 unregisterObserver");
                this.Ma.cP();
                this.Ma = null;
            }
            tmsdk.common.utils.d.e("QScannerManagerV2", "[End]Impl-freeScanner");
        }
    }

    public int getSingletonType() {
        return 2;
    }

    String getVirusBaseVersion(Context context) {
        Date date = new Date(((long) i(context, this.LS)) * 1000);
        return new SimpleDateFormat("yyyyMMdd").format(date) + (date.getHours() <= 12 ? "A" : "B");
    }

    boolean handleSpecial(QScanResultEntity qScanResultEntity) {
        return f.K(this.mContext).handleSpecial(qScanResultEntity);
    }

    boolean handleSystemFlaw(QScanResultEntity qScanResultEntity) {
        return g.L(this.mContext).handleSystemFlaw(qScanResultEntity);
    }

    int initScanner() {
        tmsdk.common.utils.d.e("QScannerManagerV2", "initScanner-mCacheManager:[" + this.LZ + "]mCertChecker[" + this.Ma + "mAmScanner[" + this.LY + "]");
        if (this.LZ == null) {
            this.LZ = new c(this.Mb.getCurrentLang(), this.LS);
        }
        if (this.Ma == null) {
            this.Ma = new ka(this.mContext);
            this.Ma.cN();
        }
        if (this.LY == null) {
            this.LY = new AmScannerStatic(this.mContext, this.LS);
        }
        return 0;
    }

    List<QScanAdBehaviorInfo> loadBehaviorConfig() {
        cx cxVar = (cx) nj.a(this.mContext, UpdateConfig.ADB_DES_LIST_NAME, UpdateConfig.intToString(40002), new cx(), "UTF-8");
        if (cxVar == null || cxVar.gw == null || cxVar.gw.size() == 0) {
            tmsdk.common.utils.d.f("QScannerManagerV2", "Empty ad behavior config!");
            return new ArrayList();
        }
        List arrayList = new ArrayList(cxVar.gw.size());
        Iterator it = cxVar.gw.iterator();
        while (it.hasNext()) {
            cw cwVar = (cw) it.next();
            QScanAdBehaviorInfo qScanAdBehaviorInfo = new QScanAdBehaviorInfo();
            try {
                int parseInt = Integer.parseInt(cwVar.go);
                long parseInt2 = (long) Integer.parseInt(cwVar.gp);
                switch (parseInt) {
                    case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                        qScanAdBehaviorInfo.behavior = parseInt2;
                        break;
                    case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                        qScanAdBehaviorInfo.behavior = parseInt2 << 32;
                        break;
                    default:
                        tmsdk.common.utils.d.c("QScannerManagerV2", "Bad behavior category " + parseInt);
                        continue;
                }
                qScanAdBehaviorInfo.description = cwVar.gq;
                qScanAdBehaviorInfo.damage = cwVar.gr;
                qScanAdBehaviorInfo.level = cwVar.gs;
                arrayList.add(qScanAdBehaviorInfo);
            } catch (Throwable e) {
                tmsdk.common.utils.d.a("QScannerManagerV2", "skipping a config item", e);
            }
        }
        return arrayList;
    }

    public void onCreate(Context context) {
        tmsdk.common.utils.d.e("QScannerManagerV2", "Impl-onCreate");
        this.mContext = context;
        this.pi = (qa) ManagerCreatorC.getManager(qa.class);
        this.Mb = (MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class);
        if (this.Mb.isENG()) {
            this.LS = ms.a(context, UpdateConfig.VIRUS_BASE_EN_NAME, null);
            tmsdk.common.utils.d.e("QScannerManagerV2", "ENG--[" + this.LS + "]");
            return;
        }
        this.LS = ms.a(context, UpdateConfig.VIRUS_BASE_NAME, null);
        tmsdk.common.utils.d.e("QScannerManagerV2", "CHS--[" + this.LS + "]");
    }

    void pauseScan() {
        synchronized (this.Me) {
            this.mPaused = true;
        }
    }
}
