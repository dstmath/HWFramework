package tmsdk.common.module.numbermarker;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseIntArray;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tmsdk.common.NumMarker;
import tmsdk.common.NumMarker.MarkFileInfo;
import tmsdk.common.NumMarker.NumMark;
import tmsdk.common.TMSDKContext;
import tmsdk.common.YellowPages;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.update.UpdateManager;
import tmsdk.common.utils.d;
import tmsdk.common.utils.f;
import tmsdk.common.utils.i;
import tmsdk.common.utils.s;
import tmsdkobf.cj;
import tmsdkobf.cl;
import tmsdkobf.cm;
import tmsdkobf.co;
import tmsdkobf.cq;
import tmsdkobf.cr;
import tmsdkobf.dz;
import tmsdkobf.im;
import tmsdkobf.jy;
import tmsdkobf.km;
import tmsdkobf.kr;
import tmsdkobf.ky;
import tmsdkobf.kz;
import tmsdkobf.lh;
import tmsdkobf.ls;
import tmsdkobf.ob;

class a extends BaseManagerC {
    private NumMarker AB;
    private LinkedHashMap<Integer, String> AC;
    private SparseIntArray AD;
    final String AE = "L7946";
    private Context mContext;
    private ob wS;

    a() {
    }

    private void eZ() {
        if (this.AB == null) {
            this.AB = NumMarker.getDefault(this.mContext);
        }
    }

    private void fa() {
        f.f(NumMarker.Tag, "initTagMap()");
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        this.AB.getMarkList(arrayList, arrayList2);
        if (arrayList.size() <= 0 || arrayList2.size() <= 0) {
            f.e(NumMarker.Tag, "initTagMap() tagValues.size() <= 0 || tagNames.size() <= 0");
        } else if (arrayList.size() == arrayList2.size()) {
            this.AC = new LinkedHashMap();
            int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                this.AC.put(arrayList.get(i), arrayList2.get(i));
            }
            f.f(NumMarker.Tag, "initTagMap() end");
        } else {
            f.e(NumMarker.Tag, "initTagMap() tagValues.size() != tagNames.size()");
        }
    }

    private void fb() {
        f.f(NumMarker.Tag, "initConfigMap()");
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        this.AB.getConfigList(arrayList, arrayList2);
        if (arrayList.size() <= 0 || arrayList2.size() <= 0) {
            f.e(NumMarker.Tag, "initConfigMap() tagValues.size() <= 0 || tagValues.size() <= 0");
        } else if (arrayList.size() == arrayList2.size()) {
            this.AD = new SparseIntArray();
            int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                this.AD.put(((Integer) arrayList.get(i)).intValue(), ((Integer) arrayList2.get(i)).intValue());
            }
            f.f(NumMarker.Tag, "initConfigMap() end");
        } else {
            f.e(NumMarker.Tag, "initConfigMap() tagValues.size() != tagValues.size()");
        }
    }

    private ArrayList<cr> k(List<NumberMarkEntity> list) {
        ArrayList<cr> arrayList = new ArrayList();
        for (NumberMarkEntity toTelReport : list) {
            arrayList.add(toTelReport.toTelReport());
        }
        return arrayList;
    }

    protected void a(List<NumQueryReq> list, final INumQueryRetListener iNumQueryRetListener) {
        f.h(NumMarker.Tag, "[cloudFetchNumberInfo]");
        JceStruct cjVar = new cj();
        ArrayList arrayList = new ArrayList();
        for (NumQueryReq numQueryReq : list) {
            cm cmVar = new cm();
            cmVar.fe = km.aX(numQueryReq.getNumber());
            int type = numQueryReq.getType();
            if (type == 16) {
                cmVar.ff = 0;
            } else if (type == 17) {
                cmVar.ff = 1;
            } else if (type == 18) {
                cmVar.ff = 2;
            }
            arrayList.add(cmVar);
            f.h(NumMarker.Tag, "number:[" + cmVar.fe + "]numAttr:[" + cmVar.ff + "]");
        }
        cjVar.eW = 1;
        cjVar.eV = arrayList;
        cjVar.eX = 0;
        cjVar.version = 1;
        JceStruct coVar = new co();
        f.h(NumMarker.Tag, "SharkQueueProxy::sendShark");
        this.wS.a(806, cjVar, coVar, 0, new jy() {
            public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                f.h(NumMarker.Tag, "Shark::onFinish() seqNo: " + i + " cmdId: " + i2 + " retCode: " + i3 + " dataRetCode: " + i4);
                List arrayList = new ArrayList();
                try {
                    co coVar = (co) jceStruct;
                    if (i3 == 0 && coVar != null) {
                        if (coVar.fi != null) {
                            Iterator it = coVar.fi.iterator();
                            while (it.hasNext()) {
                                cq cqVar = (cq) it.next();
                                NumQueryRet numQueryRet = new NumQueryRet();
                                numQueryRet.a(cqVar);
                                arrayList.add(numQueryRet);
                            }
                        }
                    }
                    if (iNumQueryRetListener != null) {
                        iNumQueryRetListener.onResult(i3, arrayList);
                    }
                } catch (Throwable th) {
                    if (iNumQueryRetListener != null) {
                        iNumQueryRetListener.onResult(i3, arrayList);
                    }
                }
            }
        }, 10000);
        kr.dz();
    }

    public boolean cloudReportPhoneNum(List<NumberMarkEntity> list, OnNumMarkReportFinish onNumMarkReportFinish) {
        if (!i.hm()) {
            return false;
        }
        f.f(NumMarker.Tag, "[cloudReportPhoneNum]");
        JceStruct clVar = new cl();
        clVar.fc = k(list);
        this.wS.a(802, clVar, null, 0, (jy) onNumMarkReportFinish);
        kr.dz();
        return true;
    }

    /* JADX WARNING: Missing block: B:9:0x0020, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:19:0x003b, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean delLocalList(Set<String> -l_4_R) {
        synchronized (this) {
            boolean z = false;
            d dVar = new d(TMSDKContext.getApplicaionContext(), "L7946");
            if (dVar.h(dVar.iy(), false)) {
                ArrayList arrayList = dVar.LD;
                if (!(arrayList == null || arrayList.size() == 0)) {
                    Collection arrayList2 = new ArrayList();
                    Iterator it = arrayList.iterator();
                    while (it.hasNext()) {
                        dz dzVar = (dz) it.next();
                        if (-l_4_R.contains(dzVar.iu)) {
                            arrayList2.add(dzVar);
                            z = true;
                        }
                    }
                    if (z) {
                        arrayList.removeAll(arrayList2);
                        dVar.a(dVar.iy(), "L7946", dVar.Lz, arrayList);
                    }
                }
            } else {
                return false;
            }
        }
    }

    protected void finalize() throws Throwable {
        ((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)).removeObserver(UpdateConfig.UPDATA_FLAG_NUM_MARK);
        super.finalize();
    }

    public String getDataMd5(String str) {
        String dataMd5 = this.AB.getDataMd5(str);
        f.f(NumMarker.Tag, "getDataMd5() filePath:" + str + " dataMd5:" + dataMd5);
        return dataMd5;
    }

    public MarkFileInfo getMarkFileInfo(int i, String str) {
        f.f(NumMarker.Tag, "getMarkFileInfo()");
        MarkFileInfo markFileInfo = this.AB.getMarkFileInfo(i, str);
        if (markFileInfo != null) {
            f.f(NumMarker.Tag, "getMarkFileInfo() version:" + markFileInfo.version + " timestampWhole:" + markFileInfo.timeStampSecondWhole + " timestampDiff:" + markFileInfo.timeStampSecondLastDiff + " md5:" + markFileInfo.md5);
        }
        return markFileInfo;
    }

    public int getSingletonType() {
        return 1;
    }

    public String getTagName(int i) {
        return this.AC != null ? (String) this.AC.get(Integer.valueOf(i)) : null;
    }

    public LinkedHashMap<Integer, String> getTagNameMap() {
        return this.AC;
    }

    protected NumQueryRet localFetchNumberInfo(String str) {
        int i = 0;
        NumQueryRet numQueryRet = null;
        s.bW(32);
        NumMark infoOfNumForBigFile = this.AB.getInfoOfNumForBigFile(str);
        if (infoOfNumForBigFile == null) {
            infoOfNumForBigFile = this.AB.getInfoOfNum(str);
        }
        if (infoOfNumForBigFile != null) {
            infoOfNumForBigFile.tagName = getTagName(infoOfNumForBigFile.tagValue);
            f.f(NumMarker.Tag, "num:[" + str + "]tagValue:[" + infoOfNumForBigFile.tagValue + "]tagName:[" + infoOfNumForBigFile.tagName + "]count:[" + infoOfNumForBigFile.count + "]");
            numQueryRet = new NumQueryRet();
            numQueryRet.property = 1;
            numQueryRet.number = infoOfNumForBigFile.num;
            numQueryRet.name = infoOfNumForBigFile.tagName;
            numQueryRet.tagType = infoOfNumForBigFile.tagValue;
            numQueryRet.tagCount = infoOfNumForBigFile.count;
            numQueryRet.usedFor = 16;
        }
        ky aJ = kz.aJ(SmsCheckResult.ESCT_146);
        if (aJ != null && aJ.xZ) {
            if (numQueryRet != null) {
                i = numQueryRet.tagType;
            }
            lh.c(str, i);
        }
        return numQueryRet;
    }

    /* JADX WARNING: Missing block: B:6:0x001b, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public NumQueryRet localFetchNumberInfoUserMark(String -l_2_R) {
        synchronized (this) {
            d dVar = new d(TMSDKContext.getApplicaionContext(), "L7946");
            if (dVar.h(dVar.iy(), false)) {
                ArrayList arrayList = dVar.LD;
                if (arrayList != null) {
                    Iterator it = arrayList.iterator();
                    while (it.hasNext()) {
                        dz dzVar = (dz) it.next();
                        if (dzVar.iu.equals(-l_2_R)) {
                            NumQueryRet numQueryRet = new NumQueryRet();
                            numQueryRet.property = 4;
                            numQueryRet.number = -l_2_R;
                            numQueryRet.name = dzVar.iv;
                            numQueryRet.tagCount = 0;
                            numQueryRet.tagType = Integer.parseInt(dzVar.iw);
                            numQueryRet.usedFor = 16;
                            return numQueryRet;
                        }
                    }
                }
            }
        }
    }

    public NumQueryRet localYellowPageInfo(String str) {
        Object query = YellowPages.getInstance().query(str);
        if (TextUtils.isEmpty(query)) {
            return null;
        }
        NumQueryRet numQueryRet = new NumQueryRet();
        numQueryRet.property = 2;
        numQueryRet.name = query;
        numQueryRet.number = str;
        numQueryRet.usedFor = 16;
        return numQueryRet;
    }

    public void onCreate(Context context) {
        this.mContext = context;
        eZ();
        fa();
        fb();
        this.wS = im.bK();
    }

    public void reInit() {
        if (this.AB != null) {
            this.AB.destroy();
            this.AB = null;
        }
        eZ();
        fa();
        fb();
    }

    protected void refreshTagMap() {
        eZ();
        fa();
        fb();
    }

    public void saveNumberInfoUserMark(List<NumberMarkEntity> list) {
        synchronized (this) {
            d dVar = new d(TMSDKContext.getApplicaionContext(), "L7946");
            dVar.h(dVar.iy(), false);
            ArrayList arrayList = dVar.LD;
            Map hashMap = new HashMap();
            if (arrayList != null) {
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    dz dzVar = (dz) it.next();
                    hashMap.put(dzVar.iu, dzVar);
                }
            }
            for (NumberMarkEntity numberMarkEntity : list) {
                if (!TextUtils.isEmpty(numberMarkEntity.phonenum)) {
                    dz dzVar2 = new dz();
                    dzVar2.iu = numberMarkEntity.phonenum;
                    dzVar2.iv = numberMarkEntity.userDefineName;
                    dzVar2.iw = Integer.toString(numberMarkEntity.tagtype);
                    hashMap.put(dzVar2.iu, dzVar2);
                }
            }
            String str = "L7946";
            dVar.a(dVar.iy(), str, new ls(), new ArrayList(hashMap.values()));
        }
    }

    public int updateMarkBigFile(String str, String str2) {
        f.f(NumMarker.Tag, "updateMarkBigFile() time:" + System.currentTimeMillis() + " desiredDataMd5:" + str2);
        int updateMarkBigFile = this.AB.updateMarkBigFile(str, str2);
        f.f(NumMarker.Tag, "updateMarkBigFile() end time:" + System.currentTimeMillis() + " errCode:" + updateMarkBigFile);
        return updateMarkBigFile;
    }

    public int updateMarkFile(String str, String str2) {
        f.f(NumMarker.Tag, "updateMarkFile() time:" + System.currentTimeMillis() + " desiredDataMd5:" + str2);
        int updateMarkFile = this.AB.updateMarkFile(str, str2);
        f.f(NumMarker.Tag, "updateMarkFile() end time:" + System.currentTimeMillis() + " errCode:" + updateMarkFile);
        return updateMarkFile;
    }
}
