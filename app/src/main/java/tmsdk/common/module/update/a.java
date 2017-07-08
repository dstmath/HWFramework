package tmsdk.common.module.update;

import android.content.Context;
import android.os.Bundle;
import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
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
import tmsdk.common.module.lang.MultiLangManager;
import tmsdk.common.module.numbermarker.NumMarkerManager;
import tmsdk.common.utils.d;
import tmsdk.common.utils.f;
import tmsdk.common.utils.n;
import tmsdk.common.utils.p;
import tmsdk.fg.module.qscanner.AmScannerStatic;
import tmsdkobf.ae;
import tmsdkobf.af;
import tmsdkobf.ew;
import tmsdkobf.ez;
import tmsdkobf.fs;
import tmsdkobf.h;
import tmsdkobf.jq;
import tmsdkobf.jw;
import tmsdkobf.lg;
import tmsdkobf.mo;
import tmsdkobf.mv;
import tmsdkobf.qt;
import tmsdkobf.w;
import tmsdkobf.z;

/* compiled from: Unknown */
final class a extends BaseManagerC {
    private qt CR;
    private String JO;
    private AtomicBoolean JP;
    private HashMap<Long, SoftReference<IUpdateObserver>> JQ;
    private IExecutionRetLis JR;
    private b JS;
    private Context mContext;

    /* compiled from: Unknown */
    /* renamed from: tmsdk.common.module.update.a.1 */
    class AnonymousClass1 implements lg {
        final /* synthetic */ ICheckListener JT;
        final /* synthetic */ ArrayList JU;
        final /* synthetic */ a JV;

        AnonymousClass1(a aVar, ICheckListener iCheckListener, ArrayList arrayList) {
            this.JV = aVar;
            this.JT = iCheckListener;
            this.JU = arrayList;
        }

        public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
            d.d("UpdateManager", "onFinish() seqNo: " + i + " cmdId: " + i2 + " retCode: " + i3 + " dataRetCode: " + i4);
            if (fsVar != null) {
                ae aeVar = (ae) fsVar;
                if (aeVar == null || aeVar.aI == null) {
                    d.c("UpdateManager", "onFinish() empty");
                    this.JV.a(i3, this.JT);
                    return;
                } else if (aeVar.aI.size() > 0) {
                    if (i3 != 0) {
                        d.d("UpdateManager", "onFinish() failed, retCode: " + i3);
                        this.JV.a(i3, this.JT);
                    } else {
                        d.d("UpdateManager", "onFinish() succ");
                        if (d.isEnable()) {
                            this.JV.a(aeVar);
                        }
                        Iterator it = aeVar.aI.iterator();
                        int i5 = 0;
                        while (it.hasNext()) {
                            int i6;
                            af afVar = (af) it.next();
                            if (afVar == null) {
                                i6 = i5;
                            } else {
                                StringBuilder append = new StringBuilder().append("[");
                                int i7 = i5 + 1;
                                d.e("UpdateManager", append.append(i5).append("]resp::fileId:[").append(afVar.am).append("]isIncreUpdate:[").append(afVar.aN).append("]").toString());
                                UpdateInfo updateInfo = new UpdateInfo();
                                updateInfo.fileName = !afVar.aN ? UpdateConfig.getFileNameByFileId(afVar.am) : UpdateConfig.getFileNameByFileId(afVar.am) + UpdateConfig.PATCH_SUFIX;
                                d.e("UpdateManager", "[" + i7 + "]resp::fileName:[" + updateInfo.fileName + "]url:[" + afVar.url + "]");
                                updateInfo.flag = UpdateConfig.getFlagByFileId(afVar.am);
                                updateInfo.type = 0;
                                updateInfo.url = afVar.url;
                                updateInfo.data1 = afVar;
                                this.JU.add(updateInfo);
                                i6 = i7;
                            }
                            i5 = i6;
                        }
                        CheckResult checkResult = new CheckResult();
                        h hVar = aeVar.aG;
                        checkResult.mTitle = hVar == null ? "" : hVar.title;
                        checkResult.mMessage = hVar == null ? "" : hVar.C;
                        checkResult.mUpdateInfoList = this.JU;
                        if (this.JT != null) {
                            d.g("UpdateManager", "onCheckFinished");
                            this.JT.onCheckFinished(checkResult);
                        }
                        d.e("UpdateManager", "title:[" + checkResult.mTitle + "]msg:[" + checkResult.mMessage + "]");
                    }
                    return;
                } else {
                    d.d("UpdateManager", "onFinish() size: 0");
                    this.JV.a(i3, this.JT);
                    return;
                }
            }
            d.c("UpdateManager", "onFinish() null == resp");
            this.JV.a(i3, this.JT);
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdk.common.module.update.a.2 */
    class AnonymousClass2 implements tmsdkobf.mt.a {
        final /* synthetic */ a JV;
        final /* synthetic */ IUpdateListener JW;
        final /* synthetic */ UpdateInfo JX;
        final /* synthetic */ AtomicBoolean JY;

        AnonymousClass2(a aVar, IUpdateListener iUpdateListener, UpdateInfo updateInfo, AtomicBoolean atomicBoolean) {
            this.JV = aVar;
            this.JW = iUpdateListener;
            this.JX = updateInfo;
            this.JY = atomicBoolean;
        }

        public void a(Bundle bundle) {
            int i = bundle.getInt("key_errcode");
            if (this.JW != null) {
                d.g("UpdateManager", "onUpdateEvent:[" + i + "]");
                this.JW.onUpdateEvent(this.JX, i);
            }
            this.JY.set(true);
            this.JX.errorCode = i;
            this.JX.errorMsg = bundle.getString("key_errorMsg");
            this.JX.downSize = bundle.getInt("key_downSize");
            this.JX.fileSize = bundle.getInt("key_total");
            this.JX.sdcardStatus = bundle.getInt("key_sdcardstatus");
            this.JX.downType = (byte) bundle.getByte("key_downType");
        }

        public void b(Bundle bundle) {
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdk.common.module.update.a.3 */
    class AnonymousClass3 implements tmsdkobf.mv.a {
        final /* synthetic */ a JV;
        final /* synthetic */ UpdateInfo JX;

        AnonymousClass3(a aVar, UpdateInfo updateInfo) {
            this.JV = aVar;
            this.JX = updateInfo;
        }

        public boolean cE(String str) {
            af afVar = (af) this.JX.data1;
            if (afVar == null) {
                return true;
            }
            boolean a = this.JV.a(str, mo.bytesToString(afVar.an), this.JX.flag);
            d.d(NumMarker.Tag, "DataMd5Cheker isMatch() isMth: " + a);
            return a;
        }
    }

    a() {
        this.JO = null;
        this.JP = new AtomicBoolean(false);
        this.JQ = new HashMap();
    }

    private void C(ArrayList<z> arrayList) {
        d.d("UpdateManager", "printUpdateInfo()");
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            z zVar = (z) it.next();
            if (zVar != null) {
                d.d("UpdateManager", "printUpdateInfo() fileId:" + zVar.am + ", " + mo.bytesToHexString(zVar.an) + ", " + zVar.timestamp);
            }
        }
    }

    private void a(int i, ICheckListener iCheckListener) {
        if (iCheckListener != null) {
            if (i != 0) {
                d.g("UpdateManager", "onCheckEvent:[" + i + "]");
                iCheckListener.onCheckEvent(i);
            }
            d.g("UpdateManager", "onCheckFinished--null");
            iCheckListener.onCheckFinished(null);
        }
    }

    private void a(UpdateInfo updateInfo) {
        d.e("UpdateManager", "updateLocation()");
        TelNumberLocator telNumberLocator = TelNumberLocator.getDefault(this.mContext);
        af afVar = (af) updateInfo.data1;
        if (afVar.aN) {
            telNumberLocator.patchLocation(this.JO + File.separator + updateInfo.fileName, mo.bytesToHexString(afVar.an));
        }
        telNumberLocator.reload();
    }

    private void a(ae aeVar) {
        d.d("UpdateManager", "printConfCmdInfo()");
        if (aeVar != null) {
            ArrayList arrayList = aeVar.aI;
            if (arrayList != null) {
                if (arrayList.size() > 0) {
                    Iterator it = arrayList.iterator();
                    while (it.hasNext()) {
                        af afVar = (af) it.next();
                        if (afVar.am > 0) {
                            d.d("UpdateManager", "printConfCmdInfo() fileId:" + afVar.am + ", " + afVar.url + ", " + mo.bytesToHexString(afVar.an));
                        } else {
                            d.c("UpdateManager", "printConfCmdInfo() fileId: 0");
                        }
                    }
                } else {
                    d.d("UpdateManager", "printConfCmdInfo() size: 0");
                }
                return;
            }
            d.c("UpdateManager", "printConfCmdInfo() confSrc null");
            return;
        }
        d.c("UpdateManager", "printConfCmdInfo() info null");
    }

    private boolean a(String str, String str2, long j) {
        String dataMd5 = ((NumMarkerManager) ManagerCreatorC.getManager(NumMarkerManager.class)).getDataMd5(str);
        if (str2 == null || dataMd5 == null) {
            return false;
        }
        d.d(NumMarker.Tag, "isMd5MatchFile() rightMd5:" + str2.toLowerCase() + " dlMd5:" + dataMd5.toLowerCase());
        if (str2.toLowerCase().equals(dataMd5.toLowerCase())) {
            return true;
        }
        b(j, 0, 0);
        return false;
    }

    private void b(long j, int i, int i2) {
        if (this.JR != null) {
            this.JR.onExecutionCode(j, i, i2);
        }
    }

    private void b(UpdateInfo updateInfo) {
        String str = this.JO + File.separator + updateInfo.fileName;
        d.d(NumMarker.Tag, "updateMarkFile() filePath: " + str);
        af afVar = (af) updateInfo.data1;
        if (afVar != null) {
            String str2 = "";
            str2 = afVar.aN ? mo.bytesToString(afVar.aO) : mo.bytesToString(afVar.an);
            d.d(NumMarker.Tag, "updateMarkFile() isincreupdate: " + afVar.aN + " checksum:" + mo.bytesToString(afVar.an) + " iuchecksum:" + mo.bytesToString(afVar.aO));
            d.d(NumMarker.Tag, "updateMarkFile()  filePath:" + str + " wholeMd5:" + str2);
            NumMarkerManager numMarkerManager = (NumMarkerManager) ManagerCreatorC.getManager(NumMarkerManager.class);
            int updateMarkFile = numMarkerManager.updateMarkFile(str, str2);
            d.d(NumMarker.Tag, "updateMarkFile()  filePath:" + str + " wholeMd5:" + str2 + " ret:" + updateMarkFile);
            if (updateMarkFile == 0) {
                numMarkerManager.refreshTagMap();
            }
            d.d(NumMarker.Tag, "updateMarkFile()  ret:" + updateMarkFile);
            b(updateInfo.flag, -1, updateMarkFile);
        }
    }

    private z w(long j) {
        if (j == 4) {
            return null;
        }
        if (j == UpdateConfig.UPDATE_FLAG_VIRUS_BASE || j == UpdateConfig.UPDATE_FLAG_VIRUS_BASE_ENG) {
            return n.g(this.mContext, n.a(this.mContext, j));
        }
        if (j == 2) {
            return n.h(50001, n.a(this.mContext, j));
        }
        z i;
        if (j == UpdateConfig.UPDATA_FLAG_NUM_MARK) {
            i = n.i(50002, n.a(this.mContext, j));
            d.d(NumMarker.Tag, "fileId:" + i.am + " timestamp:" + i.timestamp + " pfutimestamp:" + i.ao + " version:" + i.version);
            return i;
        } else if (j == UpdateConfig.UPDATE_FLAG_NUMMARK_LARGE) {
            i = n.i(40283, n.a(this.mContext, j));
            d.d("QQPimSecure", "fileId:" + i.am + " timestamp:" + i.timestamp + " pfutimestamp:" + i.ao + " version:" + i.version);
            return i;
        } else if (j != Long.MIN_VALUE) {
            return n.do(n.a(this.mContext, j));
        } else {
            i = n.i(40331, n.a(this.mContext, j));
            d.d("QQPimSecure", "fileId:" + i.am + " timestamp:" + i.timestamp + " pfutimestamp:" + i.ao + " version:" + i.version);
            return i;
        }
    }

    private int x(long j) {
        d.e("UpdateManager", "updateVirus()--flag:[" + j + "]");
        if (j == UpdateConfig.UPDATE_FLAG_VIRUS_BASE) {
            d.e("UpdateManager", "UPDATE_FLAG_VIRUS_BASE");
        } else if (j == UpdateConfig.UPDATE_FLAG_VIRUS_BASE_ENG) {
            d.e("UpdateManager", "UPDATE_FLAG_VIRUS_BASE_ENG");
        }
        ew h = n.h(this.mContext, n.a(this.mContext, j));
        if (h == null) {
            return 0;
        }
        h.X(3);
        AtomicReference atomicReference = new AtomicReference();
        Object arrayList = new ArrayList();
        int currentLang = ((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).getCurrentLang();
        d.e("UpdateManager", "language:[" + currentLang + "]");
        int a = this.CR.a(h, atomicReference, arrayList, currentLang);
        if (a == 0) {
            ez ezVar = (ez) atomicReference.get();
            if (ezVar != null) {
                if (ezVar.i()) {
                    d.d("UpdateManager", "updateVirus() getBUpdate()");
                } else if (arrayList != null && arrayList.size() > 0) {
                    d.d("UpdateManager", "updateVirus() size:[" + arrayList.size() + "]ret:[" + AmScannerStatic.updateBase(this.mContext, n.a(this.mContext, j), ezVar, arrayList) + "]");
                }
            }
        }
        return a;
    }

    public void addObserver(long j, IUpdateObserver iUpdateObserver) {
        synchronized (this.JQ) {
            if (iUpdateObserver != null) {
                this.JQ.put(Long.valueOf(j), new SoftReference(iUpdateObserver));
            }
        }
    }

    public void c(UpdateInfo updateInfo) {
        synchronized (this.JQ) {
            for (Entry entry : this.JQ.entrySet()) {
                if ((((Long) entry.getKey()).longValue() & updateInfo.flag) != 0) {
                    IUpdateObserver iUpdateObserver = (IUpdateObserver) ((SoftReference) entry.getValue()).get();
                    if (iUpdateObserver != null) {
                        iUpdateObserver.onChanged(updateInfo);
                    }
                }
            }
        }
    }

    public void cancel() {
        this.JP.set(true);
    }

    public void check(long j, ICheckListener iCheckListener) {
        d.g("UpdateManager", "check-checkFlag:[" + j + "]");
        this.JP.set(false);
        if (iCheckListener != null) {
            d.g("UpdateManager", "onCheckStarted");
            iCheckListener.onCheckStarted();
        }
        long prepareCheckFlag = UpdateConfig.prepareCheckFlag(j);
        List arrayList = new ArrayList();
        if (!jw.cH().cJ()) {
            CheckResult checkResult = new CheckResult();
            checkResult.mTitle = "Warning";
            checkResult.mMessage = "Expired! Please contact TMS(Tencent Mobile Secure) group.";
            checkResult.mUpdateInfoList = arrayList;
            if (iCheckListener != null) {
                d.g("UpdateManager", "onCheckFinished--Licence Expired");
                iCheckListener.onCheckFinished(checkResult);
            }
        } else if (this.JP.get()) {
            if (iCheckListener != null) {
                d.g("UpdateManager", "111onCheckCanceled");
                iCheckListener.onCheckCanceled();
            }
            a(0, iCheckListener);
        } else {
            ArrayList arrayList2 = new ArrayList();
            for (long j2 : UpdateConfig.UPDATE_FLAGS) {
                if ((j2 & prepareCheckFlag) != 0) {
                    z w = w(j2);
                    if (w == null) {
                        w = new z();
                        w.am = UpdateConfig.getFileIdByFlag(j2);
                        w.an = new byte[0];
                        w.timestamp = 0;
                    }
                    if (w.an == null) {
                        w.an = mo.cw("");
                    }
                    if (j2 == UpdateConfig.UPDATE_FLAG_VIRUS_BASE) {
                        d.e("UpdateManager", "req::UpdateConfig.UPDATE_FLAG_VIRUS_BASE");
                        d.e("UpdateManager", "req::fileId:[" + w.am + "]");
                    } else if (j2 == UpdateConfig.UPDATE_FLAG_VIRUS_BASE_ENG) {
                        d.e("UpdateManager", "req::UpdateConfig.UPDATE_FLAG_VIRUS_BASE_ENG");
                        d.e("UpdateManager", "req::fileId:[" + w.am + "]");
                    }
                    arrayList2.add(w);
                }
            }
            if (d.isEnable()) {
                C(arrayList2);
            }
            fs wVar = new w();
            wVar.ag = arrayList2;
            jq.cu().a(108, wVar, new ae(), 0, new AnonymousClass1(this, iCheckListener, arrayList));
        }
    }

    public String getFileSavePath() {
        return this.JO;
    }

    public int getSingletonType() {
        return 1;
    }

    public void onCreate(Context context) {
        this.mContext = context;
        this.JO = context.getFilesDir().getAbsolutePath();
        this.CR = (qt) ManagerCreatorC.getManager(qt.class);
        this.JS = b.hN();
    }

    public void removeObserver(long j) {
        synchronized (this.JQ) {
            this.JQ.remove(Long.valueOf(j));
        }
    }

    public boolean update(List<UpdateInfo> list, IUpdateListener iUpdateListener) {
        d.g("UpdateManager", "update-updateInfoList:[" + list + "]updateListener:[" + iUpdateListener + "]");
        this.JP.set(false);
        if (iUpdateListener != null) {
            d.g("UpdateManager", "onUpdateStarted");
            iUpdateListener.onUpdateStarted();
        }
        if (!jw.cH().cJ()) {
            if (iUpdateListener != null) {
                d.g("UpdateManager", "00onUpdateFinished");
                iUpdateListener.onUpdateFinished();
            }
            return false;
        } else if (this.JP.get()) {
            if (iUpdateListener != null) {
                d.g("UpdateManager", "onUpdateCanceled");
                iUpdateListener.onUpdateCanceled();
                d.g("UpdateManager", "11onUpdateFinished");
                iUpdateListener.onUpdateFinished();
            }
            return false;
        } else {
            AtomicBoolean atomicBoolean = new AtomicBoolean(false);
            d.e("UpdateManager", "updateInfoList size: " + list.size());
            boolean z = true;
            for (int i = 0; i < list.size(); i++) {
                atomicBoolean.set(false);
                UpdateInfo updateInfo = (UpdateInfo) list.get(i);
                if (updateInfo != null) {
                    d.e("UpdateManager", "updateInfo fileName: " + UpdateConfig.getFileNameByFlag(updateInfo.flag));
                    if (list.size() != 1) {
                        if (iUpdateListener != null) {
                            d.g("UpdateManager", "onProgressChanged:[" + i + "]");
                            iUpdateListener.onProgressChanged(updateInfo, (i * 100) / list.size());
                        }
                    } else if (iUpdateListener != null) {
                        d.g("UpdateManager", "onProgressChanged:[" + i + "]");
                        iUpdateListener.onProgressChanged(updateInfo, 50);
                    }
                    if (updateInfo.flag == UpdateConfig.UPDATE_FLAG_VIRUS_BASE || updateInfo.flag == UpdateConfig.UPDATE_FLAG_VIRUS_BASE_ENG) {
                        int x = x(updateInfo.flag);
                        updateInfo.downType = (byte) 2;
                        if (x != 0) {
                            atomicBoolean.set(true);
                            z = false;
                            updateInfo.errorCode = x;
                        }
                    } else if (!(updateInfo == null || updateInfo.url == null)) {
                        int a;
                        mv mvVar = new mv(this.mContext);
                        mvVar.cB(this.JO + "/");
                        mvVar.cC(updateInfo.fileName);
                        mvVar.a(new AnonymousClass2(this, iUpdateListener, updateInfo, atomicBoolean));
                        tmsdkobf.mv.a aVar = null;
                        if (updateInfo.flag == UpdateConfig.UPDATA_FLAG_NUM_MARK || updateInfo.flag == Long.MIN_VALUE || updateInfo.flag == UpdateConfig.UPDATE_FLAG_NUMMARK_LARGE) {
                            aVar = new AnonymousClass3(this, updateInfo);
                        }
                        d.e("UpdateManager", "before invoke httpGetFile.doGetFile()");
                        do {
                            a = mvVar.a(null, updateInfo.url, false, aVar);
                        } while (a == -7);
                        if (!atomicBoolean.get() && updateInfo.flag == 2 && a == 0) {
                            a(updateInfo);
                        }
                        if (!atomicBoolean.get() && ((updateInfo.flag == UpdateConfig.UPDATA_FLAG_NUM_MARK || updateInfo.flag == UpdateConfig.UPDATE_FLAG_NUMMARK_LARGE || updateInfo.flag == Long.MIN_VALUE) && a == 0)) {
                            b(updateInfo);
                        }
                        if (a != 0) {
                            z = false;
                            updateInfo.errorCode = a;
                        }
                    }
                    if (atomicBoolean.get()) {
                        updateInfo.success = (byte) 0;
                        updateInfo.downnetType = (byte) f.iw().value();
                        updateInfo.downNetName = f.getNetworkName();
                        updateInfo.rssi = p.cD(5);
                    } else {
                        c(updateInfo);
                        updateInfo.success = (byte) 1;
                        z w = w(updateInfo.flag);
                        if (w != null) {
                            updateInfo.checkSum = mo.bytesToHexString(w.an);
                            updateInfo.timestamp = w.timestamp;
                        }
                    }
                    this.JS.d(updateInfo);
                    if (this.JP.get()) {
                        if (iUpdateListener != null) {
                            d.g("UpdateManager", "onUpdateCanceled");
                            iUpdateListener.onUpdateCanceled();
                        }
                        if (iUpdateListener != null) {
                            d.g("UpdateManager", "onUpdateFinished");
                            iUpdateListener.onUpdateFinished();
                            this.JS.hO();
                        }
                        return z;
                    }
                }
            }
            if (iUpdateListener != null) {
                d.g("UpdateManager", "onUpdateFinished");
                iUpdateListener.onUpdateFinished();
                this.JS.hO();
            }
            return z;
        }
    }
}
