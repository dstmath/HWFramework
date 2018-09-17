package tmsdk.common.module.update;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.qq.taf.jce.JceStruct;
import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.bg.tcc.TelNumberLocator;
import tmsdk.common.NumMarker;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.numbermarker.NumMarkerManager;
import tmsdk.common.module.qscanner.impl.AmScannerV2;
import tmsdk.common.utils.f;
import tmsdk.common.utils.i;
import tmsdk.common.utils.r;
import tmsdk.common.utils.s;
import tmsdk.common.utils.u;
import tmsdkobf.aa;
import tmsdkobf.ad;
import tmsdkobf.ai;
import tmsdkobf.aj;
import tmsdkobf.du;
import tmsdkobf.eo;
import tmsdkobf.fd;
import tmsdkobf.fg;
import tmsdkobf.gd;
import tmsdkobf.im;
import tmsdkobf.ir;
import tmsdkobf.jy;
import tmsdkobf.k;
import tmsdkobf.kl;
import tmsdkobf.lq;
import tmsdkobf.lu;
import tmsdkobf.lx;

final class a extends BaseManagerC {
    private String JQ = null;
    private AtomicBoolean JR = new AtomicBoolean(false);
    private HashMap<Long, SoftReference<IUpdateObserver>> JS = new HashMap();
    private b JT;
    private Context mContext;

    a() {
    }

    /* JADX WARNING: Missing block: B:2:0x000f, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean B(String str, String str2) {
        String dataMd5 = ((NumMarkerManager) ManagerCreatorC.getManager(NumMarkerManager.class)).getDataMd5(str);
        return (str2 == null || dataMd5 == null || !str2.toLowerCase().equals(dataMd5.toLowerCase())) ? false : true;
    }

    private List<ad> D(long j) {
        ArrayList arrayList = new ArrayList();
        if (j == UpdateConfig.UPDATA_FLAG_NUM_MARK) {
            arrayList.add(bJ(40458));
            arrayList.add(bJ(UpdateConfig.getLargeMarkFileId()));
        } else if (j == 2) {
            arrayList.add(r.m(50001, r.b(this.mContext, 50001, ".sdb")));
        } else if (j == UpdateConfig.UPDATE_FLAG_YELLOW_PAGEV2_Large) {
            ad n = r.n(40461, r.b(this.mContext, 40461, ".sdb"));
            if (n != null) {
                f.f("gjj", "fileId:" + n.aE + " timestamp:" + n.timestamp + " pfutimestamp:" + n.aG + " version:" + n.version);
                arrayList.add(n);
            }
        } else {
            arrayList.add(r.b(this.mContext, j));
        }
        return arrayList;
    }

    @Deprecated
    private ad E(long j) {
        ad adVar = null;
        if (j != 4) {
            if (j == UpdateConfig.UPDATE_FLAG_VIRUS_BASE || j == UpdateConfig.UPDATE_FLAG_VIRUS_BASE_ENG) {
                adVar = r.i(this.mContext, r.a(this.mContext, j));
            } else if (j != 2) {
                return r.b(this.mContext, j);
            } else {
                adVar = r.m(50001, r.a(this.mContext, j));
            }
        }
        return adVar;
    }

    private void a(int i, ICheckListener iCheckListener) {
        if (iCheckListener != null) {
            if (i != 0) {
                f.d("UpdateMgr", "[callback]onCheckEvent:[" + i + "]");
                iCheckListener.onCheckEvent(i);
            }
            f.d("UpdateMgr", "[callback]onCheckFinished--null");
            iCheckListener.onCheckFinished(null);
        }
    }

    private void a(UpdateInfo updateInfo) {
        TelNumberLocator telNumberLocator = TelNumberLocator.getDefault(this.mContext);
        aj ajVar = (aj) updateInfo.data1;
        if (ajVar.bf) {
            telNumberLocator.patchLocation(this.JQ + File.separator + updateInfo.fileName, lq.bytesToHexString(ajVar.aF));
        }
        telNumberLocator.reload();
    }

    private void b(UpdateInfo updateInfo) {
        String str = this.JQ + File.separator + updateInfo.fileName;
        aj ajVar = (aj) updateInfo.data1;
        if (ajVar != null) {
            String str2;
            if (ajVar.bl == 2) {
                str2 = this.JQ + File.separator + "zipTemp" + File.separator;
                try {
                    File file = new File(str2);
                    if (file.exists()) {
                        kl.b(file);
                    }
                    gd.b(str, str2);
                    File[] listFiles = file.listFiles();
                    if (listFiles != null) {
                        if (listFiles.length != 0 && listFiles.length == 1) {
                            File file2 = new File(str);
                            if (file2.exists()) {
                                file2.delete();
                            }
                            kl.copyFile(listFiles[0], file2);
                            kl.b(file);
                        } else {
                            return;
                        }
                    }
                    return;
                } catch (Throwable e) {
                    f.b("UpdateMgr", "unzip num mark file failed", e);
                    return;
                }
            } else if (ajVar.bl != 1) {
                f.f("UpdateMgr", "normal num mark file");
            } else {
                f.e("UpdateMgr", "num mark file should not zip encrypt");
                return;
            }
            str2 = "";
            str2 = ajVar.bf ? lq.bytesToString(ajVar.bg) : lq.bytesToString(ajVar.aF);
            NumMarkerManager numMarkerManager = (NumMarkerManager) ManagerCreatorC.getManager(NumMarkerManager.class);
            if (numMarkerManager != null && numMarkerManager.updateMarkFile(str, str2) == 0) {
                numMarkerManager.refreshTagMap();
            }
        }
    }

    private ad bJ(int i) {
        ad o = r.o(i, r.b(this.mContext, i, ".sdb"));
        if (o == null) {
            o = new ad();
            o.aE = i;
            o.aF = new byte[0];
            o.timestamp = 0;
        }
        if (o.aF == null) {
            o.aF = lq.at("");
        }
        f.f(NumMarker.Tag, "fileId:" + o.aE + " timestamp:" + o.timestamp + " pfutimestamp:" + o.aG + " version:" + o.version);
        return o;
    }

    private int bK(int i) {
        final AtomicReference atomicReference = new AtomicReference(Integer.valueOf(0));
        fd j = r.j(this.mContext, r.a(this.mContext, UpdateConfig.UPDATE_FLAG_VIRUS_BASE));
        if (j == null) {
            f.g("UpdateMgr", "getVirusClientInfo return null!");
            atomicReference.set(Integer.valueOf(-2));
        } else {
            j.lF = 3;
            j.ay = i;
            JceStruct duVar = new du();
            duVar.hZ = j;
            final long currentTimeMillis = System.currentTimeMillis();
            f.d("UpdateMgr", "[Shark]Cmd_CSUpdateVirusInfos, sendShark");
            final Object obj = new Object();
            im.bK().a(2006, duVar, new eo(), 0, new jy() {
                public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                    f.d("UpdateMgr", "[Shark]onFinish-Cmd_CSUpdateVirusInfos, elapsed time:[" + (System.currentTimeMillis() - currentTimeMillis) + "]cmdId:[" + i2 + "]retCode:[" + i3 + "]dataRetCode: " + i4);
                    if (i3 != 0 || i4 != 0) {
                        f.g("UpdateMgr", "retCode != 0 || dataRetCode != 0");
                        if (i3 % 20 != -4) {
                            atomicReference.set(Integer.valueOf(-999));
                        } else {
                            atomicReference.set(Integer.valueOf(-206));
                        }
                        if (i4 != 0) {
                            atomicReference.set(Integer.valueOf(-205));
                        }
                    } else if (jceStruct == null) {
                        f.g("UpdateMgr", "SCUpdateVirusInfos is null!");
                        atomicReference.set(Integer.valueOf(-6));
                    } else {
                        eo eoVar = (eo) jceStruct;
                        fg fgVar = eoVar.kv;
                        Object obj = eoVar.kw;
                        if (fgVar == null) {
                            f.g("UpdateMgr", "SCUpdateVirusInfos.serverinfo is null!");
                            atomicReference.set(Integer.valueOf(-6));
                        } else if (fgVar.f()) {
                            f.g("UpdateMgr", "need update engine, donnot update virus base!");
                        } else if (obj != null && obj.size() > 0) {
                            String a = r.a(a.this.mContext, UpdateConfig.UPDATE_FLAG_VIRUS_BASE);
                            int a2 = AmScannerV2.a(a.this.mContext, a, fgVar, obj);
                            atomicReference.set(Integer.valueOf(a2));
                            if (a2 != 0) {
                                f.g("UpdateMgr", "amf file error, delete:[" + a + "]");
                                lu.bK(a);
                            }
                            f.d("UpdateMgr", "native updateBase, size: " + obj.size() + " ret: " + a2);
                        } else {
                            f.g("UpdateMgr", "no update info, virusInfoList: " + obj);
                        }
                    }
                    synchronized (obj) {
                        obj.notify();
                    }
                }
            }, 60000);
            Object obj2 = obj;
            synchronized (obj) {
                try {
                    obj.wait();
                } catch (Throwable e) {
                    f.c("UpdateMgr", "SCAN_LOCK.wait(): " + e, e);
                }
            }
        }
        return ((Integer) atomicReference.get()).intValue();
    }

    private void c(UpdateInfo updateInfo) {
        String str = this.JQ + File.separator + updateInfo.fileName;
        aj ajVar = (aj) updateInfo.data1;
        if (ajVar != null) {
            String str2;
            if (ajVar.bl == 2) {
                str2 = this.JQ + File.separator + "zipTemp" + File.separator;
                try {
                    File file = new File(str2);
                    if (file.exists()) {
                        kl.b(file);
                    }
                    gd.b(str, str2);
                    File[] listFiles = file.listFiles();
                    if (listFiles != null) {
                        if (listFiles.length != 0 && listFiles.length == 1) {
                            File file2 = new File(str);
                            if (file2.exists()) {
                                file2.delete();
                            }
                            kl.copyFile(listFiles[0], file2);
                            kl.b(file);
                        } else {
                            return;
                        }
                    }
                    return;
                } catch (Throwable e) {
                    f.b("UpdateMgr", "unzip num mark big file failed", e);
                    return;
                }
            } else if (ajVar.bl != 1) {
                f.f("UpdateMgr", "normal num mark big file");
            } else {
                f.e("UpdateMgr", "num mark big file should not zip encrypt");
                return;
            }
            str2 = "";
            str2 = ajVar.bf ? lq.bytesToString(ajVar.bg) : lq.bytesToString(ajVar.aF);
            NumMarkerManager numMarkerManager = (NumMarkerManager) ManagerCreatorC.getManager(NumMarkerManager.class);
            if (numMarkerManager != null && numMarkerManager.updateMarkBigFile(str, str2) == 0) {
                numMarkerManager.refreshTagMap();
            }
        }
    }

    public void a(long j, ICheckListener iCheckListener) {
        f.d("UpdateMgr", "check-checkFlag:[" + j + "]");
        this.JR.set(false);
        if (iCheckListener != null) {
            f.d("UpdateMgr", "[callback]onCheckStarted");
            iCheckListener.onCheckStarted();
        }
        long prepareCheckFlag = UpdateConfig.prepareCheckFlag(j);
        final List arrayList = new ArrayList();
        if (!ir.bU().bV()) {
            CheckResult checkResult = new CheckResult();
            checkResult.mTitle = "Warning";
            checkResult.mMessage = "Expired! Please contact TMS(Tencent Mobile Secure) group.";
            checkResult.mUpdateInfoList = arrayList;
            if (iCheckListener != null) {
                f.d("UpdateMgr", "[callback]onCheckFinished--Licence Expired");
                iCheckListener.onCheckFinished(checkResult);
            }
        } else if (this.JR.get()) {
            if (iCheckListener != null) {
                f.d("UpdateMgr", "[callback]111onCheckCanceled");
                iCheckListener.onCheckCanceled();
            }
            a(0, iCheckListener);
        } else {
            ArrayList arrayList2 = new ArrayList();
            for (long j2 : UpdateConfig.UPDATE_FLAGS) {
                if ((j2 & prepareCheckFlag) != 0) {
                    if (j2 == UpdateConfig.UPDATA_FLAG_NUM_MARK) {
                        arrayList2.addAll(D(UpdateConfig.UPDATA_FLAG_NUM_MARK));
                    } else if (j2 == 2) {
                        arrayList2.addAll(D(2));
                    } else if (j2 == UpdateConfig.UPDATE_FLAG_YELLOW_PAGEV2_Large) {
                        arrayList2.addAll(D(UpdateConfig.UPDATE_FLAG_YELLOW_PAGEV2_Large));
                    } else {
                        ad E = E(j2);
                        if (E == null) {
                            E = new ad();
                            E.aE = UpdateConfig.getFileIdByFlag(j2);
                            E.aF = new byte[0];
                            E.timestamp = 0;
                        }
                        if (E.aF == null) {
                            E.aF = lq.at("");
                        }
                        if (j2 == UpdateConfig.UPDATE_FLAG_VIRUS_BASE) {
                            f.d("UpdateMgr", "req::UpdateConfig.UPDATE_FLAG_VIRUS_BASE");
                            f.d("UpdateMgr", "req::fileId:[" + E.aE + "]");
                        } else if (j2 == UpdateConfig.UPDATE_FLAG_VIRUS_BASE_ENG) {
                            f.d("UpdateMgr", "req::UpdateConfig.UPDATE_FLAG_VIRUS_BASE_ENG");
                            f.d("UpdateMgr", "req::fileId:[" + E.aE + "]");
                        }
                        arrayList2.add(E);
                    }
                }
            }
            JceStruct aaVar = new aa();
            aaVar.ax = arrayList2;
            aaVar.ay = 1;
            JceStruct aiVar = new ai();
            final long currentTimeMillis = System.currentTimeMillis();
            f.d("UpdateMgr", "[Shark]Cmd_CSConfInfo, sendShark");
            final ICheckListener iCheckListener2 = iCheckListener;
            im.bK().a(108, aaVar, aiVar, 0, new jy() {
                public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                    f.d("UpdateMgr", "[Shark]onFinish-Cmd_CSConfInfo, elapsed time:[" + (System.currentTimeMillis() - currentTimeMillis) + "]cmdId:[" + i2 + "]retCode:[" + i3 + "]dataRetCode: " + i4);
                    if (jceStruct != null) {
                        ai aiVar = (ai) jceStruct;
                        if (aiVar == null || aiVar.ba == null) {
                            f.g("UpdateMgr", "(SCConfInfo)resp.vecConfInfo empty");
                            a.this.a(-205, iCheckListener2);
                            return;
                        } else if (aiVar.ba.size() > 0) {
                            if (i3 != 0) {
                                f.d("UpdateMgr", "failed, retCode: " + i3);
                                if (i3 % 20 != -4) {
                                    a.this.a(-999, iCheckListener2);
                                } else {
                                    a.this.a(-206, iCheckListener2);
                                }
                            } else {
                                int i5 = 0;
                                Iterator it = aiVar.ba.iterator();
                                while (it.hasNext()) {
                                    aj ajVar = (aj) it.next();
                                    if (ajVar != null) {
                                        int i6 = i5 + 1;
                                        f.d("UpdateMgr", "[" + i5 + "]resp::fileId:[" + ajVar.aE + "]isIncreUpdate:[" + ajVar.bf + "]");
                                        UpdateInfo updateInfo = new UpdateInfo();
                                        String fileNameByFileId = (40458 == ajVar.aE || UpdateConfig.getLargeMarkFileId() == ajVar.aE) ? !ajVar.bf ? UpdateConfig.intToString(ajVar.aE) + ".sdb" : UpdateConfig.intToString(ajVar.aE) + ".sdb" + UpdateConfig.PATCH_SUFIX : !ajVar.bf ? UpdateConfig.getFileNameByFileId(ajVar.aE) : UpdateConfig.getFileNameByFileId(ajVar.aE) + UpdateConfig.PATCH_SUFIX;
                                        updateInfo.fileName = fileNameByFileId;
                                        updateInfo.mFileID = ajVar.aE;
                                        f.d("UpdateMgr", "[" + i6 + "]resp::fileName:[" + updateInfo.fileName + "]url:[" + ajVar.url + "]");
                                        updateInfo.flag = UpdateConfig.getFlagByFileId(ajVar.aE);
                                        updateInfo.type = 0;
                                        updateInfo.url = ajVar.url;
                                        updateInfo.data1 = ajVar;
                                        updateInfo.fileSize = ajVar.fileSize;
                                        arrayList.add(updateInfo);
                                        i5 = i6;
                                    }
                                }
                                CheckResult checkResult = new CheckResult();
                                k kVar = aiVar.aY;
                                checkResult.mTitle = kVar == null ? "" : kVar.title;
                                checkResult.mMessage = kVar == null ? "" : kVar.T;
                                checkResult.mUpdateInfoList = arrayList;
                                if (iCheckListener2 != null) {
                                    f.d("UpdateMgr", "[callback]onCheckFinished");
                                    iCheckListener2.onCheckFinished(checkResult);
                                }
                                f.d("UpdateMgr", "title:[" + checkResult.mTitle + "]msg:[" + checkResult.mMessage + "]");
                            }
                            return;
                        } else {
                            f.d("UpdateMgr", "size: 0, no available db");
                            a.this.a(i3, iCheckListener2);
                            return;
                        }
                    }
                    f.g("UpdateMgr", "null == resp");
                    a.this.a(-205, iCheckListener2);
                }
            });
        }
    }

    public void addObserver(long j, IUpdateObserver iUpdateObserver) {
        synchronized (this.JS) {
            if (iUpdateObserver != null) {
                this.JS.put(Long.valueOf(j), new SoftReference(iUpdateObserver));
            }
        }
    }

    public void cancel() {
        this.JR.set(true);
    }

    public void d(UpdateInfo updateInfo) {
        synchronized (this.JS) {
            for (Entry entry : this.JS.entrySet()) {
                if ((((Long) entry.getKey()).longValue() & updateInfo.flag) != 0) {
                    IUpdateObserver iUpdateObserver = (IUpdateObserver) ((SoftReference) entry.getValue()).get();
                    if (iUpdateObserver != null) {
                        iUpdateObserver.onChanged(updateInfo);
                    }
                }
            }
        }
    }

    public String getFileSavePath() {
        return this.JQ;
    }

    public int getSingletonType() {
        return 1;
    }

    public void onCreate(Context context) {
        this.mContext = context;
        this.JQ = context.getFilesDir().getAbsolutePath();
        this.JT = b.hL();
    }

    public void removeObserver(long j) {
        synchronized (this.JS) {
            this.JS.remove(Long.valueOf(j));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:89:0x02f2  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean update(List<UpdateInfo> list, final IUpdateListener iUpdateListener) {
        f.d("UpdateMgr", "update-updateInfoList:[" + list + "]updateListener:[" + iUpdateListener + "]");
        s.bW(4);
        this.JR.set(false);
        if (iUpdateListener != null) {
            f.d("UpdateMgr", "[callback]onUpdateStarted");
            iUpdateListener.onUpdateStarted();
        }
        if (!ir.bU().bV()) {
            if (iUpdateListener != null) {
                f.d("UpdateMgr", "[callback]00onUpdateFinished");
                iUpdateListener.onUpdateFinished();
            }
            return false;
        } else if (this.JR.get()) {
            if (iUpdateListener != null) {
                f.d("UpdateMgr", "[callback]onUpdateCanceled");
                iUpdateListener.onUpdateCanceled();
                f.d("UpdateMgr", "[callback]11onUpdateFinished");
                iUpdateListener.onUpdateFinished();
            }
            return false;
        } else {
            boolean z = true;
            final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
            f.d("UpdateMgr", "updateInfoList size: " + list.size());
            for (int i = 0; i < list.size(); i++) {
                atomicBoolean.set(false);
                final UpdateInfo updateInfo = (UpdateInfo) list.get(i);
                if (updateInfo != null) {
                    f.d("UpdateMgr", "[" + i + "]updateInfo fileName:[" + updateInfo.fileName + "]url:[" + updateInfo.url + "]");
                    if (list.size() != 1) {
                        if (iUpdateListener != null) {
                            f.d("UpdateMgr", "[callback]onProgressChanged:[" + i + "]");
                            iUpdateListener.onProgressChanged(updateInfo, (i * 100) / list.size());
                        }
                    } else if (iUpdateListener != null) {
                        f.d("UpdateMgr", "[callback]onProgressChanged:[" + i + "]");
                        iUpdateListener.onProgressChanged(updateInfo, 50);
                    }
                    if (updateInfo.flag == UpdateConfig.UPDATE_FLAG_VIRUS_BASE || updateInfo.flag == UpdateConfig.UPDATE_FLAG_VIRUS_BASE_ENG) {
                        int bK = bK(1);
                        updateInfo.downType = (byte) 2;
                        if (bK != 0) {
                            atomicBoolean.set(true);
                            z = false;
                            updateInfo.errorCode = bK;
                            if (iUpdateListener != null) {
                                f.d("UpdateMgr", "[callback]onUpdateEvent:[" + bK + "]");
                                iUpdateListener.onUpdateEvent(updateInfo, bK);
                            }
                        }
                    } else if (!(updateInfo == null || updateInfo.url == null)) {
                        int a;
                        lx lxVar = new lx(this.mContext);
                        lxVar.bP(this.JQ + "/");
                        lxVar.bQ(updateInfo.fileName);
                        lxVar.a(new tmsdkobf.lv.a() {
                            public void a(Bundle bundle) {
                                int i = bundle.getInt("key_errcode");
                                if (iUpdateListener != null) {
                                    f.d("UpdateMgr", "[callback]onUpdateEvent:[" + i + "]");
                                    iUpdateListener.onUpdateEvent(updateInfo, i);
                                }
                                atomicBoolean.set(true);
                                updateInfo.errorCode = i;
                                updateInfo.errorMsg = bundle.getString("key_errorMsg");
                                updateInfo.downSize = bundle.getInt("key_downSize");
                                updateInfo.fileSize = bundle.getInt("key_total");
                                updateInfo.sdcardStatus = bundle.getInt("key_sdcardstatus");
                                updateInfo.downType = (byte) bundle.getByte("key_downType");
                            }

                            public void b(Bundle bundle) {
                            }
                        });
                        tmsdkobf.lx.a aVar = null;
                        if (updateInfo.flag == UpdateConfig.UPDATA_FLAG_NUM_MARK) {
                            aVar = new tmsdkobf.lx.a() {
                                public boolean bS(String -l_3_R) {
                                    Throwable e;
                                    aj ajVar = (aj) updateInfo.data1;
                                    if (ajVar == null) {
                                        return true;
                                    }
                                    Log.e("UpdateMgr", "isMatch confSrc.md5Bin = " + (ajVar.aF != null ? Arrays.toString(ajVar.aF) : "null"));
                                    String bytesToString = lq.bytesToString(ajVar.aF);
                                    if (ajVar.bl == 2) {
                                        String str = new File(-l_3_R).getParentFile().getPath() + File.separator + "tempZip" + File.separator;
                                        try {
                                            File file = new File(str);
                                            if (file.exists()) {
                                                kl.b(file);
                                            }
                                            gd.b(-l_3_R, str);
                                            File[] listFiles = file.listFiles();
                                            if (listFiles != null) {
                                                if (listFiles.length != 0) {
                                                    if (listFiles.length != 1) {
                                                        return false;
                                                    }
                                                    String str2 = ".zip.tmp";
                                                    -l_3_R = -l_3_R.length() <= str2.length() ? -l_3_R + ".tmp" : -l_3_R.substring(0, -l_3_R.length() - str2.length());
                                                    try {
                                                        File file2 = new File(-l_3_R);
                                                        if (file2.exists()) {
                                                            file2.delete();
                                                        }
                                                        kl.copyFile(listFiles[0], file2);
                                                        kl.b(file);
                                                    } catch (Exception e2) {
                                                        e = e2;
                                                    }
                                                }
                                            }
                                            return false;
                                        } catch (Exception e3) {
                                            e = e3;
                                            Object obj = null;
                                        }
                                    } else if (ajVar.bl != 1) {
                                        f.f("UpdateMgr", "normal num mark file");
                                    } else {
                                        f.e("UpdateMgr", "num mark file should not zip encrypt");
                                        return false;
                                    }
                                    if (TextUtils.isEmpty(-l_3_R)) {
                                        return false;
                                    }
                                    boolean a = a.this.B(-l_3_R, bytesToString);
                                    f.f(NumMarker.Tag, "DataMd5Cheker isMatch() isMth: " + a);
                                    return a;
                                    f.b("UpdateMgr", "unzip num mark file failed", e);
                                    return false;
                                }
                            };
                        }
                        f.d("UpdateMgr", "before invoke httpGetFile.doGetFile()");
                        do {
                            a = lxVar.a(null, updateInfo.url, false, aVar);
                        } while (a == -7);
                        if (!atomicBoolean.get() && updateInfo.flag == 2 && a == 0) {
                            a(updateInfo);
                        }
                        if (!atomicBoolean.get() && 40458 == updateInfo.mFileID && a == 0) {
                            b(updateInfo);
                        }
                        if (!atomicBoolean.get() && UpdateConfig.getLargeMarkFileId() == updateInfo.mFileID && a == 0) {
                            c(updateInfo);
                        }
                        if (a != 0) {
                            z = false;
                            updateInfo.errorCode = a;
                        }
                    }
                    if (atomicBoolean.get()) {
                        updateInfo.success = (byte) 0;
                        updateInfo.downnetType = (byte) i.iG().value();
                        updateInfo.downNetName = i.getNetworkName();
                        updateInfo.rssi = u.aK(5);
                    } else {
                        d(updateInfo);
                        updateInfo.success = (byte) 1;
                        ad E = UpdateConfig.getLargeMarkFileId() != updateInfo.mFileID ? 40458 != updateInfo.mFileID ? 40461 != updateInfo.mFileID ? 50001 != updateInfo.mFileID ? E(updateInfo.flag) : r.m(50001, r.b(this.mContext, 50001, ".sdb")) : r.n(40461, r.b(this.mContext, 40461, ".sdb")) : bJ(40458) : bJ(UpdateConfig.getLargeMarkFileId());
                        if (E != null) {
                            updateInfo.checkSum = lq.bytesToHexString(E.aF);
                            updateInfo.timestamp = E.timestamp;
                        }
                    }
                    this.JT.e(updateInfo);
                    if (this.JR.get()) {
                        if (iUpdateListener != null) {
                            f.d("UpdateMgr", "[callback]onUpdateCanceled");
                            iUpdateListener.onUpdateCanceled();
                        }
                        if (iUpdateListener != null) {
                            f.d("UpdateMgr", "[callback]onUpdateFinished");
                            iUpdateListener.onUpdateFinished();
                            this.JT.en();
                        }
                        return z;
                    }
                }
            }
            if (iUpdateListener != null) {
            }
            return z;
        }
    }
}
