package tmsdk.common.module.numbermarker;

import android.content.Context;
import android.util.SparseIntArray;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import tmsdk.common.NumMarker;
import tmsdk.common.NumMarker.MarkFileInfo;
import tmsdk.common.NumMarker.NativeNumMarkEntity;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.update.UpdateManager;
import tmsdk.common.utils.d;
import tmsdk.common.utils.f;
import tmsdkobf.bv;
import tmsdkobf.bx;
import tmsdkobf.by;
import tmsdkobf.ca;
import tmsdkobf.cc;
import tmsdkobf.cd;
import tmsdkobf.em;
import tmsdkobf.fs;
import tmsdkobf.jq;
import tmsdkobf.lg;
import tmsdkobf.lu;
import tmsdkobf.ly;
import tmsdkobf.pf;
import tmsdkobf.qt;

/* compiled from: Unknown */
class a extends BaseManagerC {
    private NumMarker CO;
    private LinkedHashMap<Integer, String> CP;
    private SparseIntArray CQ;
    private qt CR;
    private Context mContext;
    private pf yP;

    /* compiled from: Unknown */
    /* renamed from: tmsdk.common.module.numbermarker.a.1 */
    class AnonymousClass1 implements lg {
        final /* synthetic */ INumQueryRetListener CS;
        final /* synthetic */ a CT;

        AnonymousClass1(a aVar, INumQueryRetListener iNumQueryRetListener) {
            this.CT = aVar;
            this.CS = iNumQueryRetListener;
        }

        public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
            d.g(NumMarker.Tag, "Shark::onFinish() seqNo: " + i + " cmdId: " + i2 + " retCode: " + i3 + " dataRetCode: " + i4);
            List arrayList = new ArrayList();
            try {
                ca caVar = (ca) fsVar;
                if (i3 == 0 && caVar != null) {
                    if (caVar.en != null) {
                        Iterator it = caVar.en.iterator();
                        while (it.hasNext()) {
                            cc ccVar = (cc) it.next();
                            NumQueryRet numQueryRet = new NumQueryRet();
                            numQueryRet.a(ccVar);
                            arrayList.add(numQueryRet);
                            d.g(NumMarker.Tag, "phoneNum:[" + ccVar.ej + "]tagType:[" + ccVar.tagType + "]tagCount:[" + ccVar.tagCount + "]phoneName:[" + ccVar.eC + "]logo\u94fe\u63a5:[" + ccVar.eD + "]\u4f01\u4e1a\u5ba3\u4f20\u8bed:[" + ccVar.eE + "]\u53f7\u7801\u6765\u6e90:[" + ccVar.source + "]\u6765\u6e90\u7684\u8be6\u60c5url:[" + ccVar.eF + "]\u53f7\u7801\u5c5e\u6027:[" + ccVar.eG + "]\u5408\u6cd5\u6027\u5224\u65ad:[" + ccVar.eH + "]\u544a\u8b66\u989c\u8272:[" + ccVar.eI + "]\u8b66\u544a\u5b57\u6bb5:[" + ccVar.eJ + "]\u5f52\u5c5e\u5730:[" + ccVar.location + "]\u865a\u62df\u8fd0\u8425\u5546:[" + ccVar.eL + "]\u8ba4\u8bc1\u4fe1\u606f :[" + ccVar.eM + "]\u9ec4\u9875:[" + ccVar.eP + "]\u6302\u673a\u63d0\u793awording:[" + ccVar.eR + "]\u6302\u673a\u8df3\u8f6c\u4fe1\u606f :[" + ccVar.eS + "]\u6807\u8bb0\u6b21\u6570,\u6807\u8bb0\u9ec4\u9875\u751f\u6548:[" + ccVar.eT + "]\u662f\u5426\u62e6\u622a\u8be5\u53f7\u7801:[" + ccVar.eU);
                        }
                    }
                }
                if (this.CS != null) {
                    this.CS.onResult(i3, arrayList);
                }
            } catch (Throwable th) {
                if (this.CS != null) {
                    this.CS.onResult(i3, arrayList);
                }
            }
        }
    }

    a() {
    }

    private void fr() {
        if (this.CO == null) {
            this.CO = NumMarker.getDefault(this.mContext);
        }
    }

    private void fs() {
        d.d(NumMarker.Tag, "initTagMap()");
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        this.CO.getMarkList(arrayList, arrayList2);
        if (arrayList.size() <= 0 || arrayList2.size() <= 0) {
            d.c(NumMarker.Tag, "initTagMap() tagValues.size() <= 0 || tagNames.size() <= 0");
        } else if (arrayList.size() == arrayList2.size()) {
            this.CP = new LinkedHashMap();
            int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                this.CP.put(arrayList.get(i), arrayList2.get(i));
            }
            d.d(NumMarker.Tag, "initTagMap() end");
        } else {
            d.c(NumMarker.Tag, "initTagMap() tagValues.size() != tagNames.size()");
        }
    }

    private void ft() {
        d.d(NumMarker.Tag, "initConfigMap()");
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        this.CO.getConfigList(arrayList, arrayList2);
        if (arrayList.size() <= 0 || arrayList2.size() <= 0) {
            d.c(NumMarker.Tag, "initConfigMap() tagValues.size() <= 0 || tagValues.size() <= 0");
        } else if (arrayList.size() == arrayList2.size()) {
            this.CQ = new SparseIntArray();
            int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                this.CQ.put(((Integer) arrayList.get(i)).intValue(), ((Integer) arrayList2.get(i)).intValue());
            }
            d.d(NumMarker.Tag, "initConfigMap() end");
        } else {
            d.c(NumMarker.Tag, "initConfigMap() tagValues.size() != tagValues.size()");
        }
    }

    private List<em> r(List<NumberMarkEntity> list) {
        List<em> arrayList = new ArrayList();
        if (list == null || list.size() <= 0) {
            return arrayList;
        }
        for (NumberMarkEntity numberMarkEntity : list) {
            em emVar = new em();
            emVar.calltime = numberMarkEntity.calltime;
            emVar.clientlogic = numberMarkEntity.clientlogic;
            emVar.phonenum = numberMarkEntity.phonenum;
            emVar.tagtype = numberMarkEntity.tagtype;
            emVar.talktime = numberMarkEntity.talktime;
            emVar.teltype = numberMarkEntity.teltype;
            emVar.useraction = numberMarkEntity.useraction;
            arrayList.add(emVar);
        }
        return arrayList;
    }

    private ArrayList<cd> s(List<NumberMarkEntity> list) {
        ArrayList<cd> arrayList = new ArrayList();
        for (NumberMarkEntity toTelReport : list) {
            arrayList.add(toTelReport.toTelReport());
        }
        return arrayList;
    }

    protected void a(List<NumQueryReq> list, INumQueryRetListener iNumQueryRetListener) {
        d.g(NumMarker.Tag, "[cloudFetchNumberInfo]");
        fs bvVar = new bv();
        ArrayList arrayList = new ArrayList();
        for (NumQueryReq numQueryReq : list) {
            by byVar = new by();
            byVar.ej = lu.bW(numQueryReq.getNumber());
            int type = numQueryReq.getType();
            if (type == 16) {
                byVar.ek = 0;
            } else if (type == 17) {
                byVar.ek = 1;
            } else if (type == 18) {
                byVar.ek = 2;
            }
            arrayList.add(byVar);
            d.g(NumMarker.Tag, "number:[" + byVar.ej + "]numAttr:[" + byVar.ek + "]");
        }
        bvVar.eb = 1;
        bvVar.ea = arrayList;
        bvVar.ec = 0;
        fs caVar = new ca();
        d.g(NumMarker.Tag, "SharkQueueProxy::sendShark");
        this.yP.a(806, bvVar, caVar, 0, new AnonymousClass1(this, iNumQueryRetListener), 10000);
        ly.ep();
    }

    public boolean cloudReportPhoneNum(List<NumberMarkEntity> list, OnNumMarkReportFinish onNumMarkReportFinish) {
        if (!f.hv()) {
            return false;
        }
        fs bxVar = new bx();
        bxVar.eh = s(list);
        this.yP.a(802, bxVar, null, 0, onNumMarkReportFinish);
        return true;
    }

    protected void finalize() throws Throwable {
        ((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)).removeObserver(UpdateConfig.UPDATA_FLAG_NUM_MARK);
        super.finalize();
    }

    public int getConfigValue(int i) {
        return this.CQ != null ? this.CQ.get(i) : -1;
    }

    public String getDataMd5(String str) {
        String dataMd5 = this.CO.getDataMd5(str);
        d.d(NumMarker.Tag, "getDataMd5() filePath:" + str + " dataMd5:" + dataMd5);
        return dataMd5;
    }

    public MarkFileInfo getMarkFileInfo() {
        d.d(NumMarker.Tag, "getMarkFileInfo()");
        MarkFileInfo markFileInfo = this.CO.getMarkFileInfo();
        if (markFileInfo != null) {
            d.d(NumMarker.Tag, "getMarkFileInfo() version:" + markFileInfo.version + " timestampWhole:" + markFileInfo.timeStampSecondWhole + " timestampDiff:" + markFileInfo.timeStampSecondLastDiff + " md5:" + markFileInfo.md5);
        }
        return markFileInfo;
    }

    public int getSingletonType() {
        return 1;
    }

    public String getTagName(int i) {
        return this.CP != null ? (String) this.CP.get(Integer.valueOf(i)) : null;
    }

    public LinkedHashMap<Integer, String> getTagNameMap() {
        return this.CP;
    }

    protected NumQueryRet localFetchNumberInfo(String str) {
        d.d(NumMarker.Tag, "localFetchNumberInfo() num:" + str + "time:" + System.currentTimeMillis());
        NativeNumMarkEntity infoOfNum = this.CO.getInfoOfNum(str);
        if (infoOfNum == null) {
            d.d(NumMarker.Tag, "null == numMark");
            d.d(NumMarker.Tag, "localFetchNumberInfo() end time:" + System.currentTimeMillis());
            return null;
        }
        infoOfNum.tagName = getTagName(infoOfNum.tagValue);
        d.d(NumMarker.Tag, "num:[" + str + "]tagValue:[" + infoOfNum.tagValue + "]tagName:[" + infoOfNum.tagName + "]count:[" + infoOfNum.count + "]");
        NumQueryRet numQueryRet = new NumQueryRet();
        numQueryRet.property = 1;
        numQueryRet.number = infoOfNum.num;
        numQueryRet.name = infoOfNum.tagName;
        numQueryRet.tagType = infoOfNum.tagValue;
        numQueryRet.tagCount = infoOfNum.count;
        numQueryRet.usedFor = 16;
        return numQueryRet;
    }

    public void onCreate(Context context) {
        this.mContext = context;
        fr();
        fs();
        ft();
        this.CR = (qt) ManagerCreatorC.getManager(qt.class);
        this.yP = jq.cu();
    }

    public void reInit() {
        if (this.CO != null) {
            this.CO.destroy();
            this.CO = null;
        }
        fr();
        fs();
        ft();
    }

    protected void refreshTagMap() {
        fr();
        fs();
        ft();
    }

    public int reportPhoneNumber(List<NumberMarkEntity> list) {
        return (list != null && list.size() > 0) ? this.CR.y(r(list)) : -6;
    }

    public int updateMarkFile(String str, String str2) {
        d.d(NumMarker.Tag, "updateMarkFile() time:" + System.currentTimeMillis() + " desiredDataMd5:" + str2);
        int updateMarkFile = this.CO.updateMarkFile(str, str2);
        d.d(NumMarker.Tag, "updateMarkFile() end time:" + System.currentTimeMillis() + " errCode:" + updateMarkFile);
        return updateMarkFile;
    }
}
