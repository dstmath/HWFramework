package tmsdk.common.module.qscanner.impl;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;
import com.qq.taf.jce.JceStruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.lang.MultiLangManager;
import tmsdk.common.module.qscanner.QScanConfig;
import tmsdk.common.module.qscanner.QScanListener;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.common.module.qscanner.QScanResultPluginEntity;
import tmsdk.common.module.qscanner.QScannerManagerV2;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.tcc.QFile;
import tmsdk.common.tcc.QSdcardScanner;
import tmsdk.common.tcc.QSdcardScanner.ProgressListener;
import tmsdk.common.tcc.SdcardScannerFactory;
import tmsdk.common.utils.q;
import tmsdk.common.utils.r;
import tmsdk.common.utils.s;
import tmsdk.fg.creator.BaseManagerF;
import tmsdkobf.df;
import tmsdkobf.dg;
import tmsdkobf.dh;
import tmsdkobf.di;
import tmsdkobf.dj;
import tmsdkobf.dk;
import tmsdkobf.dl;
import tmsdkobf.dm;
import tmsdkobf.dn;
import tmsdkobf.do;
import tmsdkobf.dp;
import tmsdkobf.dz;
import tmsdkobf.ea;
import tmsdkobf.fd;
import tmsdkobf.fn;
import tmsdkobf.ic;
import tmsdkobf.im;
import tmsdkobf.jy;
import tmsdkobf.kr;
import tmsdkobf.kt;
import tmsdkobf.lu;
import tmsdkobf.mc;
import tmsdkobf.md;
import tmsdkobf.mk;
import tmsdkobf.oa;
import tmsdkobf.ob;
import tmsdkobf.ov;
import tmsdkobf.ox;
import tmsdkobf.oy;
import tmsdkobf.py.a;
import tmsdkobf.pz;
import tmsdkobf.qa;

public final class f extends BaseManagerF {
    private static final String[] Cz = new String[]{"image", "icon", "photo", "music", "dcim", "weibo"};
    private String CA = "";
    private md Cp;
    private ox Cq;
    private AmScannerV2 Cr;
    private int Cs = 0;
    private byte[] Ct = new byte[0];
    private boolean Cu;
    private Object Cv = new Object();
    private boolean Cw = false;
    private boolean Cx = false;
    private Object Cy = new Object();
    private Context mContext;
    private Object mLock = new Object();
    private boolean mPaused = false;

    private int a(g gVar, AtomicReference<h> atomicReference) {
        if (!AmScannerV2.isSupported()) {
            return QScanConfig.ERR_NATIVE_LOAD;
        }
        int i = -999;
        try {
            fn fnVar = new fn();
            fnVar.B("UTF-8");
            fnVar.m();
            fnVar.put("reqfc", gVar);
            AtomicReference atomicReference2 = new AtomicReference();
            i = AmScannerV2.getOpcode(fnVar.l(), atomicReference2);
            if (i == 0) {
                byte[] bArr = (byte[]) atomicReference2.get();
                fnVar.k();
                fnVar.b(bArr);
                h hVar = (h) fnVar.a("rspfc", new h());
                if (hVar == null) {
                    tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "AmScannerV2.getOpcode rspfc == null");
                } else {
                    atomicReference.set(hVar);
                    i = 0;
                }
                return i;
            }
            tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "AmScannerV2.getOpcode ret: " + i);
            return i;
        } catch (Throwable th) {
            tmsdk.common.utils.f.b(QScannerManagerV2.LOG_TAG, "AmScannerV2.getOpcode exception: " + th, th);
        }
    }

    private ArrayList<e> a(int i, List<ov> list, QScanListener qScanListener, long j, int i2) {
        Object obj = null;
        Object obj2 = null;
        if ((i & 2) != 0) {
            obj = 1;
        }
        if ((i & 4) != 0) {
            obj2 = 1;
        }
        int i3 = 2;
        if (obj == null && obj2 != null) {
            i3 = 4;
        }
        List arrayList = new ArrayList();
        int i4 = 0;
        int size = list.size();
        for (ov ovVar : list) {
            a(i3, qScanListener);
            if (b(i3, qScanListener)) {
                tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "isCanceled");
                break;
            }
            e d = obj == null ? d(ovVar) : c(ovVar);
            if (d != null) {
                arrayList.add(d);
                if (obj != null) {
                    tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[callback]onScanProgress,scanType:[2]progress:[" + (((i4 + 1) * 100) / size) + "][" + d.packageName + "][" + d.softName + "]");
                    qScanListener.onScanProgress(2, i4 + 1, size, a(d));
                }
            }
            i4++;
        }
        if (!(obj == null || obj2 == null || b(i3, qScanListener))) {
            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[callback]onScanStarted, scanType:[4]");
            qScanListener.onScanStarted(4);
        }
        if (obj2 != null && arrayList.size() > 0) {
            a(arrayList, qScanListener, i2, j, null);
        }
        return arrayList;
    }

    private QScanResultEntity a(e eVar) {
        QScanResultEntity qScanResultEntity = new QScanResultEntity();
        qScanResultEntity.packageName = eVar.packageName;
        qScanResultEntity.softName = eVar.softName;
        qScanResultEntity.version = eVar.version;
        qScanResultEntity.versionCode = eVar.versionCode;
        qScanResultEntity.path = eVar.path;
        qScanResultEntity.plugins = new ArrayList();
        qScanResultEntity.virusName = eVar.name;
        qScanResultEntity.virusDiscription = eVar.BT;
        qScanResultEntity.virusUrl = eVar.fA;
        int i = 0;
        if (eVar.gS == 0) {
            i = 257;
        } else if (eVar.gS == 4) {
            i = 263;
        } else if ((eVar.category & 512) != 0 || (eVar.gS != 0 && eVar.Cg)) {
            i = 259;
        } else if (eVar.gS == 1 && eVar.BU != 0) {
            i = 260;
        } else if (eVar.gS != 0) {
            i = !eVar.Ch ? eVar.official != 2 ? 262 : 258 : 261;
        }
        if (eVar.plugins != null) {
            Iterator it = eVar.plugins.iterator();
            while (it.hasNext()) {
                b bVar = (b) it.next();
                QScanResultPluginEntity qScanResultPluginEntity = new QScanResultPluginEntity();
                qScanResultPluginEntity.type = bVar.type;
                qScanResultPluginEntity.banUrls = bVar.banUrls;
                qScanResultPluginEntity.banIps = bVar.banIps;
                qScanResultPluginEntity.name = bVar.name;
                qScanResultEntity.plugins.add(qScanResultPluginEntity);
            }
        }
        qScanResultEntity.scanResult = i;
        return qScanResultEntity;
    }

    private void a(int i, List<e> list, Map<Integer, dh> map, List<dk> list2) {
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "collectFeatureCheckInfo");
        long currentTimeMillis = System.currentTimeMillis();
        int size = list.size();
        for (Entry entry : map.entrySet()) {
            int intValue = ((Integer) entry.getKey()).intValue();
            dh dhVar = (dh) entry.getValue();
            if (dhVar == null || dhVar.gE == null || dhVar.gE.size() == 0) {
                tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "invalid: featureParam is null or empty: " + dhVar);
            } else if (intValue < size) {
                e eVar = (e) list.get(intValue);
                g gVar = new g();
                gVar.path = eVar.path;
                gVar.gE = dhVar.gE;
                gVar.gF = dhVar.gF;
                gVar.gG = dhVar.gG;
                tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "path: " + gVar.path + " mapParam: " + gVar.gE + " fileSimhashMinCnt: " + dhVar.gF + " fileSimhashMaxCnt: " + dhVar.gG);
                AtomicReference atomicReference = new AtomicReference();
                int a = a(gVar, atomicReference);
                tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "nativeGetOpCode:[" + a + "]");
                if (a == 0) {
                    h hVar = (h) atomicReference.get();
                    if (hVar != null) {
                        dk dkVar = new dk();
                        di a2 = c.a(eVar, intValue);
                        dkVar.gI = a2.gI;
                        dkVar.gJ = a2.gJ;
                        dkVar.gK = a2.gK;
                        dkVar.gL = a2.gL;
                        dkVar.gM = a2.gM;
                        dkVar.hs = hVar.hs;
                        dkVar.ht = hVar.ht;
                        dkVar.eB = hVar.eB;
                        list2.add(dkVar);
                    }
                }
            } else {
                tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "invalid: seq >= nativeCount: " + intValue + " " + size);
            }
        }
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[end]collectFeatureCheckInfo, time(millis) elapsed:[" + (System.currentTimeMillis() - currentTimeMillis) + "]");
    }

    private void a(int i, QScanListener qScanListener) {
        synchronized (this.Cv) {
            try {
                if (this.mPaused) {
                    if (qScanListener != null) {
                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[callback]onScanPaused, scanType:[" + i + "]");
                        qScanListener.onScanPaused(i);
                    }
                    this.Cv.wait();
                    if (qScanListener != null) {
                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[callback]onScanContinue, scanType:[" + i + "]");
                        qScanListener.onScanContinue(i);
                    }
                    this.mPaused = false;
                }
            } catch (Throwable e) {
                tmsdk.common.utils.f.b(QScannerManagerV2.LOG_TAG, "isPaused(): " + e.getMessage(), e);
            }
        }
    }

    private void a(List<e> list, QScanListener qScanListener, int i, long j, byte[] bArr) {
        if (list != null && list.size() > 0) {
            kt.saveActionData(29953);
            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "doCloudScanSync, requestType:[" + i + "]timeoutMillis:[" + j + "]size:[" + list.size() + "]");
            if ((j <= 0 ? 1 : null) != null) {
                j = 120000;
            }
            final long j2 = j / 2;
            a(2, qScanListener);
            if (!b(2, qScanListener)) {
                int p = p(list);
                final JceStruct dgVar = new dg();
                dgVar.gy = new dj();
                dgVar.gy.hg = 7;
                dgVar.gy.language = 1;
                if (((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG()) {
                    dgVar.gy.language = 2;
                }
                dgVar.gy.hh = i;
                dgVar.gy.hi = 3;
                dgVar.gy.hj = fp();
                dgVar.gy.hk = bArr;
                dgVar.gy.hl = 2;
                dgVar.gy.hm = (int) (System.currentTimeMillis() / 1000);
                dgVar.gy.hn = 0;
                dgVar.gy.hp = p;
                a((dg) dgVar);
                dgVar.gC = new ArrayList();
                for (int i2 = 0; i2 < list.size(); i2++) {
                    di a = c.a((e) list.get(i2), i2);
                    if (((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG()) {
                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "ELanguage.ELANG_ENG modify flag");
                        a.gn = 1;
                        a.gS = 0;
                        a.gT = 0;
                    }
                    dgVar.gC.add(a);
                }
                kr.dz();
                final Object obj = new Object();
                final ob bK = im.bK();
                final long currentTimeMillis = System.currentTimeMillis();
                tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[Shark]Cmd_CSVirusCheck, sendShark, guid:[" + bK.b() + "]");
                final QScanListener qScanListener2 = qScanListener;
                final List<e> list2 = list;
                bK.a(2016, dgVar, new do(), 1, (jy) new jy() {
                    public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[Shark]onFinish-Cmd_CSVirusCheck, elapsed time:[" + (System.currentTimeMillis() - currentTimeMillis) + "]cmdId:[" + i2 + "]retCode:[" + i3 + "]dataRetCode: " + i4);
                        f.this.a(2, qScanListener2);
                        if (!f.this.b(2, qScanListener2)) {
                            if (i3 != 0 || i4 != 0) {
                                tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "Cmd_CSVirusCheck-onFinish, fail-retCode:[" + i3 + "]dataRetCode:[" + i4 + "]");
                                if (i3 == 0) {
                                    tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[callback]onScanError, QScanConstants.SCAN_CLOUD-dataRetCode:[" + i4 + "]");
                                    qScanListener2.onScanError(-999);
                                } else {
                                    tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[callback]onScanError, QScanConstants.SCAN_CLOUD-retCode:[" + i3 + "]");
                                    if (i3 % 20 != -4) {
                                        qScanListener2.onScanError(-999);
                                    } else {
                                        qScanListener2.onScanError(-206);
                                    }
                                }
                            } else if (jceStruct == null) {
                                tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "Cmd_CSVirusCheck-onFinish, scVirusCheck is null!");
                                tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[callback]onScanError, QScanConstants.SCAN_CLOUD-:[-205]");
                                qScanListener2.onScanError(-205);
                            } else {
                                do doVar = (do) jceStruct;
                                tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "server handle time(micro seconds): " + doVar.hE);
                                f.this.bf(doVar.hI);
                                if (doVar.gC == null) {
                                    tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "scVirusCheck.vecApkInfo is null, maybe because same as local result!");
                                } else {
                                    f.this.a(list2, doVar);
                                }
                                if (doVar.hH == null || doVar.hH.size() == 0) {
                                    tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "not need FeatureCheck, scVirusCheck.mapFeatureParam: " + (doVar.hH != null ? "empty" : "null"));
                                } else {
                                    f.this.a(list2, qScanListener2, dgVar.gy, j2, bK, doVar.hH);
                                }
                            }
                        }
                        synchronized (obj) {
                            obj.notify();
                        }
                    }
                }, j2);
                Object obj2 = obj;
                synchronized (obj) {
                    try {
                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "doCloudScanSync(), block thread " + Thread.currentThread() + ", waiting for shark callback -->|");
                        obj.wait();
                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "doCloudScanSync(), continue thread: " + Thread.currentThread() + " |-->");
                    } catch (Throwable e) {
                        tmsdk.common.utils.f.c(QScannerManagerV2.LOG_TAG, "doCloudScanSync(), SCAN_LOCK.wait(): " + e, e);
                    }
                }
                return;
            }
            return;
        }
        tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "doCloudScanSync, nativeResults is null or size==0");
    }

    private void a(List<e> list, QScanListener qScanListener, dj djVar, long j, oa oaVar, Map<Integer, dh> map) {
        if (map != null) {
            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "doFeatureCheckSync, apk count: " + map.size());
            ArrayList arrayList = new ArrayList();
            a(djVar.hh, (List) list, (Map) map, (List) arrayList);
            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "needFeatureCheckList size:[" + arrayList.size() + "]");
            if (arrayList.size() > 0) {
                final Object obj = new Object();
                JceStruct dfVar = new df();
                dfVar.gy = djVar;
                dfVar.gz = arrayList;
                final long currentTimeMillis = System.currentTimeMillis();
                tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[Shark]Cmd_CSFeatureCheck, sendShark, guid:[" + oaVar.b() + "]");
                final QScanListener qScanListener2 = qScanListener;
                final List<e> list2 = list;
                oaVar.a(2019, dfVar, new dn(), 1, new jy() {
                    public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[Shark]onFinish-Cmd_CSFeatureCheck, elapsed time:[" + (System.currentTimeMillis() - currentTimeMillis) + "]cmdId:[" + i2 + "]retCode:[" + i3 + "]dataRetCode: " + i4);
                        f.this.a(2, qScanListener2);
                        if (!f.this.b(2, qScanListener2)) {
                            if (i3 != 0 || i4 != 0) {
                                tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "onFinish-Cmd_CSFeatureCheck fail" + i + " cmdId: " + i2 + " retCode: " + i3 + " dataRetCode: " + i4);
                            } else if (jceStruct == null) {
                                tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "scFeatureCheck is null!");
                            } else {
                                dn dnVar = (dn) jceStruct;
                                tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "server handle time(micro seconds): " + dnVar.hE);
                                if (dnVar.gz == null) {
                                    tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "scFeatureCheck.vecFeatureInfo is null!");
                                } else {
                                    f.this.a(list2, dnVar);
                                }
                            }
                        }
                        synchronized (obj) {
                            obj.notify();
                        }
                    }
                }, j);
                Object obj2 = obj;
                synchronized (obj) {
                    try {
                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "doFeatureCheckSync(), block thread " + Thread.currentThread() + ", waiting for shark callback -->|");
                        obj.wait();
                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "doFeatureCheckSync(), continue thread: " + Thread.currentThread() + " |-->");
                    } catch (Throwable th) {
                        tmsdk.common.utils.f.c(QScannerManagerV2.LOG_TAG, "doFeatureCheckSync(), SCAN_LOCK.wait(): " + th, th);
                    }
                }
            }
        }
    }

    private void a(List<e> list, dn dnVar) {
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "correctNativeResultsByFeatureCheck");
        if (list == null || dnVar == null) {
            tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "nativeResults == null || resp == null");
            return;
        }
        q(list);
        List<dm> list2 = dnVar.gz;
        Map map = dnVar.hC;
        Map map2 = dnVar.hD;
        if (list2 == null || list2.size() == 0) {
            tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "rspFeatureInfoList: " + (list2 != null ? "empty" : "null"));
            return;
        }
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "rspFeatureInfoList size:[" + list2.size() + "]");
        int size = list.size();
        for (dm dmVar : list2) {
            if (dmVar != null) {
                if (dmVar.hv < size) {
                    e eVar = (e) list.get(dmVar.hv);
                    eVar.gS = dmVar.gS;
                    eVar.BU = dmVar.gT;
                    eVar.category = dmVar.gU;
                    eVar.plugins = c.a(dmVar.gV, map2);
                    eVar.dp = dmVar.gY;
                    if (dmVar.gT > 0) {
                        if (map == null) {
                            tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "mapVirusInfo is null for virusId: " + dmVar.gT);
                        } else {
                            dp dpVar = (dp) map.get(Integer.valueOf(dmVar.gT));
                            if (dpVar == null) {
                                tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "cannot find VirusInfo for virusId: " + dmVar.gT);
                            } else {
                                eVar.label = dpVar.gv;
                                eVar.name = dpVar.gv;
                                eVar.BT = dpVar.hK;
                                eVar.fA = dpVar.hL;
                            }
                        }
                    }
                    eVar.lL = dmVar.hy;
                    eVar.url = dmVar.hz;
                    eVar.type = dmVar.gn;
                } else {
                    tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "invalid: rspFeatureInfo.nRefSeqNo >= nativeCount: " + dmVar.hv + " " + size);
                }
            }
        }
        q(list);
    }

    private void a(List<e> list, do doVar) {
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "correctNativeResults");
        if (list == null || doVar == null) {
            tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "nativeResults == null || resp == null");
            return;
        }
        ArrayList arrayList = doVar.gC;
        Map map = doVar.hC;
        Map map2 = doVar.hD;
        if (arrayList == null || arrayList.size() == 0) {
            tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "rspApkInfoList: " + (arrayList != null ? "empty" : "null"));
            return;
        }
        q(list);
        int size = list.size();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            dl dlVar = (dl) it.next();
            if (dlVar != null) {
                if (dlVar.hv < size) {
                    e eVar = (e) list.get(dlVar.hv);
                    eVar.gS = dlVar.gS;
                    eVar.BU = dlVar.gT;
                    eVar.category = dlVar.gU;
                    eVar.plugins = c.a(dlVar.gV, map2);
                    if ((dlVar.gW & 1) == 0) {
                        eVar.Cg = false;
                    } else {
                        eVar.Cg = true;
                    }
                    if ((dlVar.gW & 2) == 0) {
                        eVar.Ch = false;
                    } else {
                        eVar.Ch = true;
                    }
                    eVar.Cm = dlVar.gX;
                    eVar.dp = dlVar.gY;
                    eVar.official = dlVar.official;
                    if (dlVar.gT <= 0) {
                        eVar.label = dlVar.hw;
                        eVar.name = dlVar.hw;
                        eVar.BT = dlVar.hx;
                    } else if (map == null) {
                        tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "mapVirusInfo is null for virusId: " + dlVar.gT);
                    } else {
                        dp dpVar = (dp) map.get(Integer.valueOf(dlVar.gT));
                        if (dpVar == null) {
                            tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "cannot find VirusInfo for virusId: " + dlVar.gT);
                        } else {
                            eVar.label = dpVar.gv;
                            eVar.name = dpVar.gv;
                            eVar.BT = dpVar.hK;
                            eVar.fA = dpVar.hL;
                        }
                    }
                    eVar.lL = dlVar.hy;
                    eVar.url = dlVar.hz;
                    eVar.Ck = dlVar.hA;
                    eVar.Cl = dlVar.hB;
                    eVar.type = dlVar.gn;
                } else {
                    tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "invalid: rspApkInfo.nRefSeqNo >= nativeCount: " + dlVar.hv + " " + size);
                }
            }
        }
        q(list);
    }

    private void a(dg dgVar) {
        File fq = fq();
        if (!fq.exists()) {
            lu.b(this.mContext, UpdateConfig.WHITELIST_CLOUDSCAN_NAME, null);
        }
        dgVar.gy.ho = f(fq);
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "whitelist file Md5:[" + dgVar.gy.ho + "]");
    }

    private void a(ov ovVar, d dVar, e eVar) {
        try {
            ArrayList arrayList = new ArrayList();
            if (!(dVar == null || dVar.BS == null || TextUtils.isEmpty(dVar.BS.bZ))) {
                arrayList = c.ca(dVar.BS.bZ);
                eVar.Cb = dVar.BS.bZ;
            }
            ArrayList arrayList2;
            if (arrayList.size() > 0) {
                if (arrayList.size() > 1) {
                    if (ovVar.hC()) {
                        tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "more than one cert file, ignore for uninstalled apk");
                    } else {
                        arrayList2 = (ArrayList) oy.h(ovVar.getPackageName(), 10);
                        if (arrayList2.size() > 0) {
                            eVar.bZ = (String) arrayList2.get(0);
                        }
                        tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "more than one cert file, get by java api, certs: " + arrayList2);
                        tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "more than one cert file, get by java api, main cert: " + eVar.bZ);
                    }
                }
            } else if (ovVar.hC()) {
                tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "native cannot get certMd5, ignore for uninstalled apk");
            } else {
                arrayList2 = (ArrayList) oy.h(ovVar.getPackageName(), 10);
                if (arrayList2.size() > 0) {
                    arrayList.add(arrayList2);
                }
                tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "native cannot get certMd5, get by java api, certs: " + arrayList2);
            }
            if (arrayList.size() > 0) {
                if (eVar.bZ == null) {
                    eVar.bZ = (String) ((ArrayList) arrayList.get(0)).get(0);
                }
                if (TextUtils.isEmpty(eVar.Cb)) {
                    eVar.Cb = c.q(arrayList);
                }
            }
        } catch (Throwable th) {
            tmsdk.common.utils.f.c(QScannerManagerV2.LOG_TAG, "handleCert, exception: " + th, th);
        }
    }

    private static a b(ov ovVar, int i) {
        return ovVar != null ? new a(q.cI(ovVar.getPackageName()), q.cI(ovVar.getAppName()), q.cI(ovVar.hz()), q.cI(ovVar.getVersion()), ovVar.getVersionCode(), (int) ovVar.getSize(), q.cI(ovVar.hB()), i) : null;
    }

    /* JADX WARNING: Missing block: B:9:0x000e, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean b(int i, QScanListener qScanListener) {
        synchronized (this.Cy) {
            if (!this.Cw) {
                return false;
            } else if (!(qScanListener == null || this.Cx)) {
                tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[callback]onScanCanceled, scanType:[" + i + "]");
                qScanListener.onScanCanceled(i);
                this.Cx = true;
            }
        }
    }

    private boolean bd(int i) {
        return ((i & 2) == 0 && (i & 4) == 0) ? false : true;
    }

    private boolean be(int i) {
        return i == 3 || i == 4 || i == 12;
    }

    private void bf(int i) {
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "processWhitelistStatus, whitelistStatus:[" + i + "]");
        switch (i) {
            case 1:
                this.Cp.a("ew", true, true);
                return;
            case 2:
                this.Cp.a("ew", false, true);
                return;
            case 3:
                fr();
                return;
            default:
                return;
        }
    }

    private e c(ov ovVar) {
        if (ovVar != null) {
            ov e = e(ovVar);
            if (e == null) {
                tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "nativeScan, loadAppSimpleInfo == null::" + ovVar.getPackageName() + " " + ovVar.hB());
                return null;
            }
            int i = 0;
            if (e.hC()) {
                i = 2;
            } else if (e.hx()) {
                i = 1;
            }
            if (TextUtils.isEmpty(e.hB())) {
                tmsdk.common.utils.f.e(QScannerManagerV2.LOG_TAG, "nativeScan, appEntity.getApkPath() == null, unable to scan: " + e.getPackageName() + " " + e.hB());
                return null;
            }
            e eVar = new e();
            eVar.Co = false;
            eVar.lastModified = e.hy();
            eVar.Cn = e.hD();
            try {
                d a = this.Cr.a(b(e, i));
                if (a != null) {
                    c.a(a, eVar);
                    a(e, a, eVar);
                    if (TextUtils.isEmpty(eVar.BT)) {
                        eVar.BT = this.CA;
                    }
                }
            } catch (Throwable th) {
                tmsdk.common.utils.f.e(QScannerManagerV2.LOG_TAG, "nativeScan error:[" + th + "]");
            }
            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "nativeScan[" + eVar.packageName + "][" + eVar.softName + "][" + eVar.Cb + "][" + eVar.path + "]");
            this.Cs++;
            if (this.Cs > 800) {
                System.gc();
                this.Cs = 0;
            }
            return eVar;
        }
        tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "nativeScan, appEntity == null");
        return null;
    }

    private e d(ov ovVar) {
        Throwable th;
        e eVar = null;
        if (ovVar != null) {
            try {
                ov e = e(ovVar);
                if (e == null) {
                    tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "loadAppSimpleInfo == null::" + ovVar.getPackageName() + " " + ovVar.hB());
                    return null;
                }
                int i = 0;
                if (e.hC()) {
                    i = 2;
                } else if (e.hx()) {
                    i = 1;
                }
                if (TextUtils.isEmpty(e.hB())) {
                    tmsdk.common.utils.f.e(QScannerManagerV2.LOG_TAG, "genCloudScanEntity, appEntity.getApkPath() == null, unable to scan: " + e.getPackageName() + " " + e.hB());
                    return null;
                }
                e eVar2 = new e();
                try {
                    eVar2.Co = true;
                    eVar2.lastModified = e.hy();
                    eVar2.Cn = e.hD();
                    a b = b(e, i);
                    eVar2.packageName = b.nf;
                    eVar2.softName = b.softName;
                    eVar2.version = b.version;
                    eVar2.versionCode = b.versionCode;
                    eVar2.path = b.path;
                    eVar2.BQ = b.BQ;
                    eVar2.size = b.size;
                    eVar2.type = 1;
                    eVar2.lL = 0;
                    eVar2.BU = 0;
                    eVar2.name = null;
                    eVar2.label = null;
                    eVar2.BT = null;
                    eVar2.url = null;
                    eVar2.gS = 0;
                    eVar2.dp = 0;
                    eVar2.plugins = null;
                    eVar2.name = null;
                    eVar2.category = 0;
                    String str = null;
                    d b2 = AmScannerV2.b(b);
                    if (b2 != null) {
                        str = b2.BW;
                        try {
                            a(e, b2, eVar2);
                        } catch (Throwable th2) {
                            th = th2;
                            eVar = eVar2;
                        }
                    }
                    eVar2.cc = str;
                    if (TextUtils.isEmpty(eVar2.BT)) {
                        eVar2.BT = this.CA;
                    }
                    eVar = eVar2;
                } catch (Throwable th3) {
                    th = th3;
                    eVar = eVar2;
                }
                tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "genCloudScanEntity[" + eVar.packageName + "][" + eVar.softName + "][" + eVar.Cb + "][" + eVar.path + "]");
                return eVar;
            } catch (Throwable th4) {
                th = th4;
                tmsdk.common.utils.f.b(QScannerManagerV2.LOG_TAG, "genCloudScanEntity, exception: " + th, th);
                tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "genCloudScanEntity[" + eVar.packageName + "][" + eVar.softName + "][" + eVar.Cb + "][" + eVar.path + "]");
                return eVar;
            }
        }
        tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "genCloudScanEntity, appEntity == null");
        return null;
    }

    private ov e(ov ovVar) {
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "loadAppSimpleInfo");
        return !ovVar.hC() ? TMServiceFactory.getSystemInfoService().a(ovVar, 8265) : this.Cq.c(ovVar, 9);
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0034 A:{SYNTHETIC, Splitter: B:22:0x0034} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0042 A:{SYNTHETIC, Splitter: B:30:0x0042} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x004f A:{SYNTHETIC, Splitter: B:37:0x004f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static byte[] f(File file) {
        Throwable th;
        FileInputStream fileInputStream = null;
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            FileInputStream fileInputStream2 = new FileInputStream(file);
            try {
                byte[] bArr = new byte[8192];
                while (true) {
                    int read = fileInputStream2.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    instance.update(bArr, 0, read);
                }
                byte[] digest = instance.digest();
                if (fileInputStream2 != null) {
                    try {
                        fileInputStream2.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return digest;
            } catch (IOException e2) {
                fileInputStream = fileInputStream2;
                if (fileInputStream != null) {
                }
                return null;
            } catch (NoSuchAlgorithmException e3) {
                fileInputStream = fileInputStream2;
                if (fileInputStream != null) {
                }
                return null;
            } catch (Throwable th2) {
                th = th2;
                fileInputStream = fileInputStream2;
                if (fileInputStream != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            return null;
        } catch (NoSuchAlgorithmException e6) {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e52) {
                    e52.printStackTrace();
                }
            }
            return null;
        } catch (Throwable th3) {
            th = th3;
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e7) {
                    e7.printStackTrace();
                }
            }
            throw th;
        }
    }

    private void fn() {
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[beg] resetScanStatus this:[" + this + "]tid:[" + Thread.currentThread().getId() + "]");
        synchronized (this.Cy) {
            this.Cw = false;
            this.Cx = false;
            this.Cs = 0;
        }
        synchronized (this.Cv) {
            this.mPaused = false;
        }
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[end] resetScanStatus this:[" + this + "]tid:[" + Thread.currentThread().getId() + "]");
    }

    private ArrayList<ov> fo() {
        long currentTimeMillis = System.currentTimeMillis();
        ArrayList<ov> arrayList = new ArrayList();
        try {
            List<ApplicationInfo> installedApplications = this.mContext.getPackageManager().getInstalledApplications(0);
            if (installedApplications != null) {
                if (installedApplications.size() > 0) {
                    String packageName = this.mContext.getPackageName();
                    for (ApplicationInfo applicationInfo : installedApplications) {
                        if (!(applicationInfo == null || applicationInfo.packageName == null || applicationInfo.packageName.equals(packageName))) {
                            ov ovVar = new ov();
                            ovVar.cm(applicationInfo.packageName);
                            arrayList.add(ovVar);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            tmsdk.common.utils.f.c(QScannerManagerV2.LOG_TAG, "loadInstalledAppList, exception: ", e);
        }
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "loadInstalledAppList,size:[" + arrayList.size() + "]time(millis) elpased:[" + (System.currentTimeMillis() - currentTimeMillis) + "]");
        return arrayList;
    }

    private int fp() {
        int i = 0;
        fd j = r.j(this.mContext, lu.b(this.mContext, UpdateConfig.VIRUS_BASE_NAME, null));
        if (j != null) {
            i = j.e();
        }
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "getVirusBaseIntVersion:[" + i + "]");
        return i;
    }

    private File fq() {
        return new File(this.mContext.getFilesDir().toString() + File.separator + UpdateConfig.WHITELIST_CLOUDSCAN_NAME);
    }

    private boolean fr() {
        File fq = fq();
        boolean exists = fq.exists();
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "white list file exist:[" + exists + "]");
        if (!exists || !ft()) {
            return false;
        }
        try {
            fq.delete();
        } catch (Exception e) {
            tmsdk.common.utils.f.e(QScannerManagerV2.LOG_TAG, "e:[" + e + "]");
        }
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "deleteWhiteList");
        fs();
        return true;
    }

    private void fs() {
        int day = new Date().getDay();
        if (day == this.Cp.getInt("ldd", 0)) {
            int i = this.Cp.getInt("dtt", 0);
            this.Cp.a("ldd", day, true);
            this.Cp.a("dtt", i + 1, true);
            return;
        }
        this.Cp.a("ldd", day, true);
        this.Cp.a("dtt", 1, true);
    }

    private boolean ft() {
        if (new Date().getDay() != this.Cp.getInt("ldd", 0)) {
            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "today first delete operation");
        } else if (this.Cp.getInt("dtt", 0) >= 3) {
            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "today delete limit");
            return false;
        }
        return true;
    }

    private int p(List<e> list) {
        int i = 0;
        if (this.Cp.getBoolean("ew", true)) {
            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "filterWhiteList");
            ea eaVar = (ea) mk.a(this.mContext, UpdateConfig.WHITELIST_CLOUDSCAN_NAME, UpdateConfig.intToString(40427), new ea(), "UTF-8");
            if (!(eaVar == null || eaVar.iC == null || list == null)) {
                tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "whitelist size:[" + eaVar.iC.size() + "]");
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    e eVar = (e) it.next();
                    if (eVar != null) {
                        if (eVar.Cb == null || eVar.Cb.length() <= 32) {
                            if (eVar.cc != null && !eVar.cc.contains(",")) {
                                Iterator it2 = eaVar.iC.iterator();
                                while (it2.hasNext()) {
                                    dz dzVar = (dz) it2.next();
                                    if (eVar.bZ != null && eVar.bZ.equals(dzVar.iu)) {
                                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "is in whitelite:[" + eVar.packageName + "][" + eVar.softName + "]");
                                        i++;
                                        it.remove();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "hit whitelist size:[" + i + "]");
        }
        return i;
    }

    private void q(List<e> list) {
        if (list != null && list.size() > 0) {
            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "size:[" + list.size() + "]");
            int i = 0;
            int i2 = 0;
            for (e eVar : list) {
                if (eVar != null) {
                    if (eVar.Cg) {
                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "isInPayList:[" + eVar.packageName + "][" + eVar.softName + "]");
                    }
                    if (eVar.Ch) {
                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "isInStealAccountList:[" + eVar.packageName + "][" + eVar.softName + "]");
                    }
                    if (eVar.gS == 4) {
                        i2++;
                    } else if (eVar.gS != 0) {
                        i++;
                    }
                }
            }
            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "riskCount:[" + i + "]unknowCount:[" + i2 + "]");
        }
    }

    private List<QScanResultEntity> r(List<e> list) {
        if (list == null) {
            return new ArrayList();
        }
        List<QScanResultEntity> arrayList = new ArrayList(list.size());
        for (e a : list) {
            arrayList.add(a(a));
        }
        return arrayList;
    }

    /* JADX WARNING: Missing block: B:10:0x00a2, code:
            return tmsdk.common.module.qscanner.QScanConfig.ERR_ILLEGAL_ARG;
     */
    /* JADX WARNING: Missing block: B:32:0x00d5, code:
            return tmsdk.common.module.qscanner.QScanConfig.ERR_ILLEGAL_ARG;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int a(int i, List<String> list, QScanListener qScanListener, int i2, long j) {
        synchronized (this.mLock) {
            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[beg]scanInstalledPackagesImpl, scanType:[" + i + "]packageNames size:[" + (list != null ? list.size() : -1) + "]scanListener:[" + qScanListener + "]requestType:[" + i2 + "]timeoutMillis:[" + j + "]");
            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "scanInstalledPackagesImpl this:[" + this + "]tid:[" + Thread.currentThread().getId() + "]");
            s.bW(8);
            if (qScanListener != null && bd(i) && be(i2)) {
                if (i2 == 3 && (list == null || list.size() != 1)) {
                } else {
                    if ((j <= 0 ? 1 : null) == null) {
                        if ((j >= 2000 ? 1 : null) == null) {
                            return QScanConfig.ERR_ILLEGAL_ARG;
                        }
                    }
                    if (ic.bE()) {
                        tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "isExpired");
                        return QScanConfig.ERR_EXPIRED;
                    } else if (this.Cr != null) {
                        List arrayList;
                        fn();
                        synchronized (this.Ct) {
                            this.Cu = true;
                        }
                        Object obj = null;
                        Object obj2 = null;
                        if ((i & 2) != 0) {
                            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "QScanConfig.SCAN_LOCAL");
                            obj = 1;
                        }
                        if ((i & 4) != 0) {
                            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "QScanConfig.SCAN_CLOUD");
                            obj2 = 1;
                        }
                        int i3 = 2;
                        if (obj == null && obj2 != null) {
                            i3 = 4;
                        }
                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[callback]onScanStarted, scanType:[" + i3 + "]");
                        qScanListener.onScanStarted(i3);
                        long currentTimeMillis = System.currentTimeMillis();
                        if (list != null && list.size() > 0) {
                            arrayList = new ArrayList(list.size());
                            for (String str : list) {
                                ov ovVar = new ov();
                                ovVar.cm(str);
                                arrayList.add(ovVar);
                            }
                        } else {
                            arrayList = fo();
                        }
                        if (arrayList != null && arrayList.size() > 0) {
                            List a = a(i, arrayList, qScanListener, j, i2);
                            fn();
                            synchronized (this.Ct) {
                                this.Cu = false;
                            }
                            i3 = 4;
                            if (obj != null && obj2 == null) {
                                i3 = 2;
                            }
                            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[callback]onScanFinished, scanType:[" + i3 + "]size:[" + a.size() + "]");
                            qScanListener.onScanFinished(i3, r(a));
                            q(a);
                            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "scanInstalledPackagesImpl this:[" + this + "]tid:[" + Thread.currentThread().getId() + "]");
                            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[end]scanInstalledPackagesImpl, time(millis) elapsed:[" + (System.currentTimeMillis() - currentTimeMillis) + "]");
                            return 0;
                        }
                        fn();
                        synchronized (this.Ct) {
                            this.Cu = false;
                        }
                        tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "cannot featch installed packages");
                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[callback]onScanFinished, scanType:[" + i3 + "]size:[0]");
                        qScanListener.onScanFinished(i3, new ArrayList());
                        return QScanConfig.W_CANNOT_FEATCH_PKGINFO;
                    } else {
                        tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "not invoke initScanner");
                        return QScanConfig.W_NOT_INIT;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0095, code:
            return tmsdk.common.module.qscanner.QScanConfig.ERR_ILLEGAL_ARG;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int a(int i, List<String> list, QScanListener qScanListener, long j) {
        synchronized (this.mLock) {
            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[beg]scanUninstalledApksImpl, scanType:[" + i + "]apkPaths size:[" + (list != null ? list.size() : -1) + "]scanListener:[" + qScanListener + "]timeoutMillis:[" + j + "]");
            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "scanUninstalledApksImpl this:[" + this + "]tid:[" + Thread.currentThread().getId() + "]");
            s.bW(8);
            if (qScanListener != null && bd(i)) {
                if ((j <= 0 ? 1 : null) == null) {
                    if ((j >= 2000 ? 1 : null) == null) {
                        return QScanConfig.ERR_ILLEGAL_ARG;
                    }
                }
                if (ic.bE()) {
                    tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "isExpired");
                    return QScanConfig.ERR_EXPIRED;
                } else if (this.Cr != null) {
                    fn();
                    synchronized (this.Ct) {
                        this.Cu = true;
                    }
                    Object obj = null;
                    Object obj2 = null;
                    if ((i & 2) != 0) {
                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "QScanConfig.SCAN_LOCAL");
                        obj = 1;
                    }
                    if ((i & 4) != 0) {
                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "QScanConfig.SCAN_CLOUD");
                        obj2 = 1;
                    }
                    final int[] iArr = new int[]{2};
                    if (obj == null && obj2 != null) {
                        iArr[0] = 4;
                    }
                    long currentTimeMillis = System.currentTimeMillis();
                    final List arrayList = new ArrayList();
                    if (list != null && list.size() > 0) {
                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[callback]onScanStarted, scanType:[" + iArr[0] + "]");
                        qScanListener.onScanStarted(iArr[0]);
                        List arrayList2 = new ArrayList(list.size());
                        for (String str : list) {
                            ov ovVar = new ov();
                            ovVar.P(true);
                            ovVar.cn(str);
                            arrayList2.add(ovVar);
                        }
                        arrayList.addAll(a(i, arrayList2, qScanListener, j, 12));
                    } else {
                        qa qaVar = new qa();
                        String[] strArr = new String[]{"/storage/emulated/legacy", "/storage_int", "/HWUserData"};
                        qaVar.Lf.add(new pz(0, null, new String[]{"apk"}));
                        qaVar.Lg = strArr;
                        final ArrayList arrayList3 = new ArrayList();
                        final ArrayList arrayList4 = new ArrayList();
                        final QScanListener qScanListener2 = qScanListener;
                        final int i2 = i;
                        QSdcardScanner qSdcardScanner = SdcardScannerFactory.getQSdcardScanner(2, new a() {
                            public void onFound(int i, QFile qFile) {
                                tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "onFound,ruleId:[" + i + "]path:[" + qFile.filePath + "]");
                                f.this.a(iArr[0], qScanListener2);
                                if (f.this.b(iArr[0], qScanListener2)) {
                                    if (arrayList4.size() > 0 && arrayList4.get(0) != null) {
                                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "QSdcardScanner cancel");
                                        ((QSdcardScanner) arrayList4.get(0)).cancleScan();
                                    }
                                    return;
                                }
                                ov ovVar = new ov();
                                ovVar.P(true);
                                ovVar.cn(qFile.filePath);
                                if ((i2 & 2) == 0) {
                                    arrayList3.add(ovVar);
                                } else {
                                    e a = f.this.c(ovVar);
                                    if (a != null) {
                                        arrayList.add(a);
                                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[callback]onScanProgress,scanType:[2][" + a.packageName + "][" + a.softName + "][" + a.path + "]");
                                        qScanListener2.onScanProgress(2, -1, -1, f.this.a(a));
                                    }
                                }
                            }
                        }, qaVar);
                        if (qSdcardScanner != null) {
                            arrayList4.add(qSdcardScanner);
                            final List arrayList5 = new ArrayList();
                            qScanListener2 = qScanListener;
                            qSdcardScanner.registerProgressListener(9999, new ProgressListener() {
                                public boolean onScanPathChange(String str) {
                                    tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "onScanPathChange,path:[" + str + "]");
                                    f.this.a(iArr[0], qScanListener2);
                                    if (f.this.b(iArr[0], qScanListener2)) {
                                        if (arrayList4.size() > 0 && arrayList4.get(0) != null) {
                                            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "QSdcardScanner cancel");
                                            ((QSdcardScanner) arrayList4.get(0)).cancleScan();
                                        }
                                        return false;
                                    }
                                    String bU = mc.bU(str);
                                    if (arrayList5.contains(bU)) {
                                        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "has scanned path:[" + str + "]");
                                        return false;
                                    }
                                    arrayList5.add(bU);
                                    if (f.Cz != null) {
                                        for (String str2 : f.Cz) {
                                            if (str.endsWith("/" + str2)) {
                                                tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "mIgnoreDirs path:[" + str + "]");
                                                return false;
                                            }
                                        }
                                    }
                                    return true;
                                }
                            });
                            tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[callback]onScanStarted, scanType:[" + iArr[0] + "]");
                            qScanListener.onScanStarted(iArr[0]);
                            for (String str2 : lu.s(TMSDKContext.getApplicaionContext())) {
                                tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "startScan path:[" + str2 + "]");
                                qSdcardScanner.startScan(str2);
                            }
                            qSdcardScanner.release();
                            arrayList5.clear();
                            if (obj2 != null) {
                                if (arrayList.size() > 0) {
                                    tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[callback]onScanStarted, scanType:[4]");
                                    qScanListener.onScanStarted(4);
                                    a(arrayList, qScanListener, 12, j, null);
                                } else if (arrayList3.size() > 0) {
                                    arrayList.addAll(a(4, (List) arrayList3, qScanListener, j, 12));
                                }
                            }
                            qa qaVar2 = qaVar;
                        } else {
                            tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "SdcardScannerFactory.getQSdcardScanner failed!");
                            return QScanConfig.W_GET_SDCARD_QSCANNER;
                        }
                    }
                    fn();
                    synchronized (this.Ct) {
                        this.Cu = false;
                    }
                    iArr[0] = 4;
                    if (obj != null && obj2 == null) {
                        iArr[0] = 2;
                    }
                    tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[callback]onScanFinished, scanType:[" + iArr[0] + "]size:[" + arrayList.size() + "]");
                    qScanListener.onScanFinished(iArr[0], r(arrayList));
                    q(arrayList);
                    tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "scanUninstalledApksImpl this:[" + this + "]tid:[" + Thread.currentThread().getId() + "]");
                    tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[end]scanUninstalledApksImpl, time(millis) elapsed:[" + (System.currentTimeMillis() - currentTimeMillis) + "]");
                    return 0;
                } else {
                    tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "not invoke initScanner");
                    return QScanConfig.W_NOT_INIT;
                }
            }
        }
    }

    public void cancelScan() {
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "cancelScan");
        synchronized (this.Cy) {
            this.Cw = true;
        }
        synchronized (this.Cv) {
            this.Cv.notifyAll();
        }
    }

    public void continueScan() {
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "continueScan");
        synchronized (this.Cv) {
            this.Cv.notifyAll();
        }
    }

    /* JADX WARNING: Missing block: B:6:0x0041, code:
            r2 = r9.mLock;
     */
    /* JADX WARNING: Missing block: B:7:0x0043, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:10:0x0046, code:
            if (r9.Cr != null) goto L_0x009e;
     */
    /* JADX WARNING: Missing block: B:11:0x0048, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:12:0x0049, code:
            tmsdk.common.utils.f.f(tmsdk.common.module.qscanner.QScannerManagerV2.LOG_TAG, "[end]freeScanner, time(millis) elapsed:[" + (java.lang.System.currentTimeMillis() - r0) + "]this:[" + r9 + "]tid:[" + java.lang.Thread.currentThread().getId() + "]");
     */
    /* JADX WARNING: Missing block: B:13:0x008d, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:23:?, code:
            r9.Cr.exit();
            r9.Cr = null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int fm() {
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[beg]freeScanner this:[" + this + "]tid:[" + Thread.currentThread().getId() + "]");
        long currentTimeMillis = System.currentTimeMillis();
        synchronized (this.Ct) {
            if (this.Cu) {
                tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "is scanning");
                return QScanConfig.W_IS_SCANNING;
            }
        }
    }

    public int getSingletonType() {
        return 2;
    }

    public String getVirusBaseVersion() {
        Date date = new Date(((long) fp()) * 1000);
        String str = new SimpleDateFormat("yyyyMMdd").format(date) + (date.getHours() <= 12 ? "A" : "B");
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "getVirusBaseVersion:[" + str + "]");
        return str;
    }

    /* JADX WARNING: Missing block: B:10:0x004d, code:
            tmsdk.common.utils.f.f(tmsdk.common.module.qscanner.QScannerManagerV2.LOG_TAG, "[end]initScanner, time(millis) elapsed:[" + (java.lang.System.currentTimeMillis() - r0) + "this:[" + r9 + "]tid:[" + java.lang.Thread.currentThread().getId() + "]");
     */
    /* JADX WARNING: Missing block: B:11:0x0091, code:
            return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int initScanner() {
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "[beg]initScanner, this:[" + this + "]tid:[" + Thread.currentThread().getId() + "]");
        if (ic.bE()) {
            tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "licence expired, initScanner");
            return QScanConfig.ERR_EXPIRED;
        }
        long currentTimeMillis = System.currentTimeMillis();
        synchronized (this.mLock) {
            if (!AmScannerV2.isSupported()) {
                tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "AmScannerV2 no supported, initScanner");
                return QScanConfig.ERR_NATIVE_LOAD;
            } else if (this.Cr == null) {
                this.Cr = new AmScannerV2(this.mContext, lu.b(this.mContext, UpdateConfig.VIRUS_BASE_NAME, null));
                if (!this.Cr.fl()) {
                    this.Cr = null;
                    tmsdk.common.utils.f.g(QScannerManagerV2.LOG_TAG, "initScanner failed!!");
                    return QScanConfig.ERR_INIT;
                }
            }
        }
    }

    public void onCreate(Context context) {
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "onCreate, this:[" + this + "]");
        this.mContext = context;
        this.Cq = (ox) ManagerCreatorC.getManager(ox.class);
        this.Cp = new md("133_cs_wl");
    }

    public void pauseScan() {
        tmsdk.common.utils.f.f(QScannerManagerV2.LOG_TAG, "pauseScan");
        synchronized (this.Cv) {
            this.mPaused = true;
        }
    }
}
