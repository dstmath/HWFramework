package tmsdk.fg.module.qscanner;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.SparseArray;
import java.util.ArrayList;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.module.qscanner.QScanConstants;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.common.utils.j;
import tmsdkobf.ea;
import tmsdkobf.nd;
import tmsdkobf.py;

/* compiled from: Unknown */
final class g {
    private static g Mu;
    private SparseArray<a> Mv;
    private Context mContext;

    /* compiled from: Unknown */
    private static class a implements a {
        String Mo;
        int Mp;
        String Mq;
        int Mr;
        String mName;
        int mType;

        public a(ea eaVar) {
            this.mName = eaVar.name;
            this.Mo = eaVar.jk;
            this.Mp = eaVar.level;
            this.Mq = eaVar.iH;
            this.Mr = eaVar.advice;
            this.mType = eaVar.type;
        }

        public boolean b(QScanResultEntity qScanResultEntity) {
            return false;
        }

        public QScanResultEntity je() {
            QScanResultEntity qScanResultEntity = new QScanResultEntity();
            qScanResultEntity.systemFlaw = QScanConstants.SYSTEM_FLAW_ACCOUNT_CHEAT;
            qScanResultEntity.shortDesc = this.Mo;
            qScanResultEntity.discription = this.Mq;
            qScanResultEntity.name = this.mName;
            qScanResultEntity.type = this.mType;
            qScanResultEntity.safeLevel = this.Mp;
            qScanResultEntity.advice = this.Mr;
            qScanResultEntity.needOpenAppMonitorToHandle = false;
            return qScanResultEntity;
        }
    }

    /* compiled from: Unknown */
    private static class b implements a {
        String Mo;
        int Mp;
        String Mq;
        int Mr;
        String mName;
        int mType;

        b(ea eaVar) {
            this.mName = eaVar.name;
            this.Mo = eaVar.jk;
            this.Mp = eaVar.level;
            this.Mq = eaVar.iH;
            this.Mr = eaVar.advice;
            this.mType = eaVar.type;
        }

        private boolean jl() {
            if (!Build.BRAND.toLowerCase().contains("samsung")) {
                if (VERSION.RELEASE.startsWith("4.0")) {
                    if (!"HTC One X".equalsIgnoreCase(Build.MODEL)) {
                    }
                }
                if (!VERSION.RELEASE.startsWith("2.2")) {
                    return false;
                }
                if (!"HTC Desire".equalsIgnoreCase(Build.MODEL)) {
                    return false;
                }
            } else if (j.iM() > 15 || VERSION.RELEASE.contains("4.0.4")) {
                return false;
            } else {
                py b = TMServiceFactory.getSystemInfoService().b("com.sec.android.app.launcher", 8);
                if (b == null) {
                    return false;
                }
                String version = b.getVersion();
                if (version == null) {
                    return false;
                }
                if (!(version.startsWith("3") || version.startsWith("4"))) {
                    return false;
                }
            }
            return true;
        }

        public boolean b(QScanResultEntity qScanResultEntity) {
            return false;
        }

        public QScanResultEntity je() {
            QScanResultEntity qScanResultEntity = new QScanResultEntity();
            qScanResultEntity.systemFlaw = QScanConstants.SYSTEM_FLAW_DATACLEAR;
            qScanResultEntity.shortDesc = this.Mo;
            qScanResultEntity.discription = this.Mq;
            qScanResultEntity.name = this.mName;
            if (jl()) {
                qScanResultEntity.type = this.mType;
                qScanResultEntity.safeLevel = this.Mp;
                qScanResultEntity.advice = this.Mr;
                qScanResultEntity.needOpenAppMonitorToHandle = true;
            } else {
                qScanResultEntity.type = 1;
                qScanResultEntity.safeLevel = 0;
            }
            return qScanResultEntity;
        }
    }

    /* compiled from: Unknown */
    private static class c implements a {
        String Mo;
        int Mp;
        String Mq;
        int Mr;
        String mName;
        int mType;

        public c(ea eaVar) {
            this.mName = eaVar.name;
            this.Mo = eaVar.jk;
            this.Mp = eaVar.level;
            this.Mq = eaVar.iH;
            this.Mr = eaVar.advice;
            this.mType = eaVar.type;
        }

        public boolean b(QScanResultEntity qScanResultEntity) {
            return true;
        }

        public QScanResultEntity je() {
            QScanResultEntity qScanResultEntity = new QScanResultEntity();
            qScanResultEntity.systemFlaw = QScanConstants.SYSTEM_FLAW_MASTERKEY;
            qScanResultEntity.shortDesc = this.Mo;
            qScanResultEntity.discription = this.Mq;
            qScanResultEntity.name = this.mName;
            if (MasterKeyVul.scan(g.Mu.mContext)) {
                qScanResultEntity.type = this.mType;
                qScanResultEntity.safeLevel = this.Mp;
                qScanResultEntity.advice = this.Mr;
                qScanResultEntity.needOpenAppMonitorToHandle = false;
            } else {
                qScanResultEntity.type = 1;
                qScanResultEntity.safeLevel = 0;
            }
            return qScanResultEntity;
        }
    }

    /* compiled from: Unknown */
    private static class d implements a {
        String Mo;
        int Mp;
        String Mq;
        int Mr;
        String mName;
        int mType;

        public d(ea eaVar) {
            this.mName = eaVar.name;
            this.Mo = eaVar.jk;
            this.Mp = eaVar.level;
            this.Mq = eaVar.iH;
            this.Mr = eaVar.advice;
            this.mType = eaVar.type;
        }

        public boolean b(QScanResultEntity qScanResultEntity) {
            return false;
        }

        public QScanResultEntity je() {
            return null;
        }
    }

    /* compiled from: Unknown */
    private static class e implements a {
        String Mo;
        int Mp;
        String Mq;
        int Mr;
        String mName;
        int mType;

        public e(ea eaVar) {
            this.mName = eaVar.name;
            this.Mo = eaVar.jk;
            this.Mp = eaVar.level;
            this.Mq = eaVar.iH;
            this.Mr = eaVar.advice;
            this.mType = eaVar.type;
        }

        private boolean jm() {
            for (PackageInfo packageInfo : g.Mu.mContext.getPackageManager().getInstalledPackages(0)) {
                if ("com.sec.android.sCloudBackupProvider".equals(packageInfo.packageName)) {
                    return packageInfo.applicationInfo.enabled && packageInfo.versionCode == 14;
                }
            }
            return false;
        }

        public boolean b(QScanResultEntity qScanResultEntity) {
            return false;
        }

        public QScanResultEntity je() {
            QScanResultEntity qScanResultEntity = new QScanResultEntity();
            qScanResultEntity.systemFlaw = QScanConstants.SYSTEM_FLAW_S4_CLOUDBACKUP;
            qScanResultEntity.shortDesc = this.Mo;
            qScanResultEntity.discription = this.Mq;
            qScanResultEntity.name = this.mName;
            if (jm()) {
                qScanResultEntity.type = this.mType;
                qScanResultEntity.safeLevel = this.Mp;
                qScanResultEntity.advice = this.Mr;
                qScanResultEntity.needOpenAppMonitorToHandle = false;
            } else {
                qScanResultEntity.type = 1;
                qScanResultEntity.safeLevel = 0;
            }
            return qScanResultEntity;
        }
    }

    /* compiled from: Unknown */
    private static class f implements a {
        String Mo;
        int Mp;
        String Mq;
        int Mr;
        String mName;
        int mType;

        f(ea eaVar) {
            this.mName = eaVar.name;
            this.Mo = eaVar.jk;
            this.Mp = eaVar.level;
            this.Mq = eaVar.iH;
            this.Mr = eaVar.advice;
            this.mType = eaVar.type;
        }

        public boolean b(QScanResultEntity qScanResultEntity) {
            return false;
        }

        public QScanResultEntity je() {
            boolean z;
            QScanResultEntity qScanResultEntity = new QScanResultEntity();
            qScanResultEntity.systemFlaw = QScanConstants.SYSTEM_FLAW_SMISHING;
            qScanResultEntity.shortDesc = this.Mo;
            qScanResultEntity.discription = this.Mq;
            qScanResultEntity.name = this.mName;
            try {
                if (g.Mu.mContext.getPackageManager().getServiceInfo(new ComponentName("com.android.mms", "com.android.mms.transaction.SmsReceiverService"), 0).exported) {
                    z = true;
                    if (z) {
                        qScanResultEntity.type = 1;
                        qScanResultEntity.safeLevel = 0;
                    } else {
                        qScanResultEntity.type = this.mType;
                        qScanResultEntity.safeLevel = this.Mp;
                        qScanResultEntity.advice = this.Mr;
                        qScanResultEntity.needOpenAppMonitorToHandle = true;
                    }
                    return qScanResultEntity;
                }
            } catch (NameNotFoundException e) {
            } catch (RuntimeException e2) {
                nd.a(e2);
            }
            z = false;
            if (z) {
                qScanResultEntity.type = this.mType;
                qScanResultEntity.safeLevel = this.Mp;
                qScanResultEntity.advice = this.Mr;
                qScanResultEntity.needOpenAppMonitorToHandle = true;
            } else {
                qScanResultEntity.type = 1;
                qScanResultEntity.safeLevel = 0;
            }
            return qScanResultEntity;
        }
    }

    private g(Context context) {
        this.Mv = new SparseArray();
        this.mContext = context;
    }

    static g L(Context context) {
        if (Mu == null) {
            synchronized (g.class) {
                if (Mu == null) {
                    Mu = new g(context);
                }
            }
        }
        Mu.jj();
        return Mu;
    }

    private void jj() {
        for (ea eaVar : SystemScanConfigManager.M(this.mContext).jn()) {
            switch (eaVar.id) {
                case QScanConstants.SYSTEM_FLAW_SMISHING /*120001*/:
                    this.Mv.append(QScanConstants.SYSTEM_FLAW_SMISHING, new f(eaVar));
                    break;
                case QScanConstants.SYSTEM_FLAW_DATACLEAR /*120002*/:
                    this.Mv.append(QScanConstants.SYSTEM_FLAW_DATACLEAR, new b(eaVar));
                    break;
                case QScanConstants.SYSTEM_FLAW_ROOT /*120003*/:
                    this.Mv.append(QScanConstants.SYSTEM_FLAW_ROOT, new d(eaVar));
                    break;
                case QScanConstants.SYSTEM_FLAW_S4_CLOUDBACKUP /*120004*/:
                    this.Mv.append(QScanConstants.SYSTEM_FLAW_S4_CLOUDBACKUP, new e(eaVar));
                    break;
                case QScanConstants.SYSTEM_FLAW_MASTERKEY /*120005*/:
                    this.Mv.append(QScanConstants.SYSTEM_FLAW_MASTERKEY, new c(eaVar));
                    break;
                case QScanConstants.SYSTEM_FLAW_ACCOUNT_CHEAT /*120006*/:
                    this.Mv.append(QScanConstants.SYSTEM_FLAW_ACCOUNT_CHEAT, new a(eaVar));
                    break;
                default:
                    break;
            }
        }
    }

    ArrayList<QScanResultEntity> b(QScanListenerV2 qScanListenerV2, b bVar) {
        ArrayList<QScanResultEntity> arrayList = new ArrayList();
        if (this.Mv.size() == 0) {
            return arrayList;
        }
        int size = this.Mv.size();
        for (int i = 0; i < size; i++) {
            if (bVar != null && bVar.fH()) {
                return arrayList;
            }
            QScanResultEntity je = ((a) this.Mv.valueAt(i)).je();
            if (je != null) {
                arrayList.add(je);
                if (qScanListenerV2 != null) {
                    qScanListenerV2.onScanProgress(3, ((i + 1) * 100) / size, je);
                }
            }
        }
        return arrayList;
    }

    boolean handleSystemFlaw(QScanResultEntity qScanResultEntity) {
        if (qScanResultEntity == null) {
            return false;
        }
        a aVar = (a) this.Mv.get(qScanResultEntity.systemFlaw);
        return aVar != null ? aVar.b(qScanResultEntity) : false;
    }
}
