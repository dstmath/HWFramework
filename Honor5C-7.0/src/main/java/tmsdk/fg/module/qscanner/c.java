package tmsdk.fg.module.qscanner;

import android.content.Context;
import android.os.Bundle;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
final class c {
    private final String LN;
    private final String LO;
    private Bundle LP;
    private boolean LQ;
    private int LR;
    private String LS;
    private Context mContext;

    public c(int i, String str) {
        this.LN = "cache_4.8_02";
        this.LO = "cache_eng_4.8_02";
        this.LP = new Bundle();
        this.LQ = false;
        d.e("QScannerManagerV2-QScanCacheManager", "QScanCacheManager::nLang:[" + i + "]amfFilePath[" + str + "]");
        this.mContext = TMSDKContext.getApplicaionContext();
        this.LR = i;
        this.LS = str;
        dt(str);
    }

    private QScanResultEntity b(String str, int i, QScanResultEntity qScanResultEntity) {
        if (str == null || qScanResultEntity == null) {
            return null;
        }
        QScanResultEntity qScanResultEntity2 = new QScanResultEntity();
        qScanResultEntity2.packageName = qScanResultEntity.packageName;
        qScanResultEntity2.softName = qScanResultEntity.softName;
        qScanResultEntity2.version = qScanResultEntity.version;
        qScanResultEntity2.versionCode = qScanResultEntity.versionCode;
        qScanResultEntity2.path = str;
        qScanResultEntity2.apkType = i;
        qScanResultEntity2.certMd5 = qScanResultEntity.certMd5;
        qScanResultEntity2.size = qScanResultEntity.size;
        qScanResultEntity2.dexSha1 = qScanResultEntity.dexSha1;
        qScanResultEntity2.plugins = qScanResultEntity.plugins;
        qScanResultEntity2.name = qScanResultEntity.name;
        qScanResultEntity2.type = qScanResultEntity.type;
        qScanResultEntity2.advice = qScanResultEntity.advice;
        qScanResultEntity2.malwareid = qScanResultEntity.malwareid;
        qScanResultEntity2.name = qScanResultEntity.name;
        qScanResultEntity2.label = qScanResultEntity.name;
        qScanResultEntity2.discription = qScanResultEntity.discription;
        qScanResultEntity2.url = qScanResultEntity.url;
        qScanResultEntity2.safeLevel = qScanResultEntity.safeLevel;
        qScanResultEntity2.shortDesc = qScanResultEntity.shortDesc;
        qScanResultEntity2.dirtyDataPathes = qScanResultEntity.dirtyDataPathes;
        qScanResultEntity2.special = qScanResultEntity.special;
        qScanResultEntity2.systemFlaw = qScanResultEntity.systemFlaw;
        qScanResultEntity2.isInPayList = qScanResultEntity.isInPayList;
        qScanResultEntity2.isInStealAccountList = qScanResultEntity.isInStealAccountList;
        qScanResultEntity2.needRootToHandle = qScanResultEntity.needRootToHandle;
        qScanResultEntity2.needOpenAppMonitorToHandle = qScanResultEntity.needOpenAppMonitorToHandle;
        qScanResultEntity2.product = qScanResultEntity.product;
        qScanResultEntity2.category = qScanResultEntity.category;
        qScanResultEntity2.officialPackName = qScanResultEntity.officialPackName;
        qScanResultEntity2.officialCertMd5 = qScanResultEntity.officialCertMd5;
        return qScanResultEntity2;
    }

    private void dt(String str) {
    }

    private void y(String str, String str2) {
    }

    public tmsdk.common.module.qscanner.QScanResultEntity a(java.lang.String r6, java.lang.String r7, long r8, int r10) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:19:0x0033
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.modifyBlocksTree(BlockProcessor.java:248)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:52)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r5 = this;
        r1 = 0;
        if (r6 != 0) goto L_0x0004;
    L_0x0003:
        return r1;
    L_0x0004:
        r2 = r5.LP;	 Catch:{ RuntimeException -> 0x0029 }
        monitor-enter(r2);	 Catch:{ RuntimeException -> 0x0029 }
        r0 = r5.LP;	 Catch:{ all -> 0x0026 }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0026 }
        r3.<init>();	 Catch:{ all -> 0x0026 }
        r3 = r3.append(r6);	 Catch:{ all -> 0x0026 }
        r3 = r3.append(r8);	 Catch:{ all -> 0x0026 }
        r3 = r3.toString();	 Catch:{ all -> 0x0026 }
        r0 = r0.getParcelable(r3);	 Catch:{ all -> 0x0026 }
        r0 = (tmsdk.common.module.qscanner.QScanResultEntity) r0;	 Catch:{ all -> 0x0026 }
        monitor-exit(r2);	 Catch:{ all -> 0x0036 }
    L_0x0021:
        r0 = r5.b(r7, r10, r0);
        return r0;
    L_0x0026:
        r0 = move-exception;
    L_0x0027:
        monitor-exit(r2);	 Catch:{ all -> 0x0026 }
        throw r0;	 Catch:{ RuntimeException -> 0x0029 }
    L_0x0029:
        r0 = move-exception;
    L_0x002a:
        r0 = new android.os.Bundle;
        r0.<init>();
        r5.LP = r0;
        r0 = r1;
        goto L_0x0021;
        r1 = move-exception;
        r1 = r0;
        goto L_0x002a;
    L_0x0036:
        r1 = move-exception;
        r4 = r1;
        r1 = r0;
        r0 = r4;
        goto L_0x0027;
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.fg.module.qscanner.c.a(java.lang.String, java.lang.String, long, int):tmsdk.common.module.qscanner.QScanResultEntity");
    }

    public void a(String str, int i, QScanResultEntity qScanResultEntity) {
        if (str != null) {
            synchronized (this.LP) {
                this.LP.putParcelable(str + i, qScanResultEntity);
            }
            this.LQ = true;
        }
    }

    public void a(ApkKey apkKey, QScanResultEntity qScanResultEntity) {
        if (apkKey != null) {
            synchronized (this.LP) {
                this.LP.putParcelable(apkKey.pkgName + apkKey.size, qScanResultEntity);
            }
            this.LQ = true;
        }
    }

    public void jf() {
        d.e("QScannerManagerV2-QScanCacheManager", "exit");
        String str = "qscan.cache";
        String str2 = "cache_4.8_02";
        if (this.LR == 2) {
            str = "qscan_eng.cache";
            str2 = "cache_eng_4.8_02";
        }
        y(str, str2);
    }
}
