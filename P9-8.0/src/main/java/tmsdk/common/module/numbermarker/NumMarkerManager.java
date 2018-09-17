package tmsdk.common.module.numbermarker;

import android.content.Context;
import android.text.TextUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import tmsdk.common.NumMarker.MarkFileInfo;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.utils.f;
import tmsdkobf.kt;

public class NumMarkerManager extends BaseManagerC {
    public static final String TAG = "TMSDK_NumMarkerManager";
    private a AA;

    public int cloudFetchNumberInfo(List<NumQueryReq> list, INumQueryRetListener iNumQueryRetListener) {
        f.f(TAG, "cloudFetchNumberInfo");
        if (list == null || list.size() == 0 || iNumQueryRetListener == null) {
            return -2;
        }
        kt.aE(1320044);
        this.AA.a(list, iNumQueryRetListener);
        return 0;
    }

    public boolean cloudReportPhoneNum(List<NumberMarkEntity> list, OnNumMarkReportFinish onNumMarkReportFinish) {
        kt.saveActionData(1320062);
        if (list == null || list.size() == 0 || onNumMarkReportFinish == null) {
            return false;
        }
        this.AA.cloudReportPhoneNum(list, onNumMarkReportFinish);
        return true;
    }

    public boolean delLocalList(Set<String> set) {
        return this.AA.delLocalList(set);
    }

    public String getDataMd5(String str) {
        return str != null ? this.AA.getDataMd5(str) : null;
    }

    public MarkFileInfo getMarkFileInfo(int i, String str) {
        return this.AA.getMarkFileInfo(i, str);
    }

    public String getTagName(int i) {
        return this.AA.getTagName(i);
    }

    public LinkedHashMap<Integer, String> getTagNameMap() {
        return this.AA.getTagNameMap();
    }

    public NumQueryRet localFetchNumberInfo(String str) {
        f.f(TAG, "localFetchNumberInfo");
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        kt.aE(29961);
        return this.AA.localFetchNumberInfo(str);
    }

    public NumQueryRet localFetchNumberInfoUserMark(String str) {
        return !TextUtils.isEmpty(str) ? this.AA.localFetchNumberInfoUserMark(str) : null;
    }

    public NumQueryRet localYellowPageInfo(String str) {
        f.f(TAG, "localYellowPageInfo");
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        kt.aE(1320065);
        return this.AA.localYellowPageInfo(str);
    }

    public void onCreate(Context context) {
        this.AA = new a();
        this.AA.onCreate(context);
        a(this.AA);
    }

    public void reInit() {
        this.AA.reInit();
    }

    public void refreshTagMap() {
        this.AA.refreshTagMap();
    }

    public void saveNumberInfoUserMark(List<NumberMarkEntity> list) {
        if (list != null && list.size() != 0) {
            this.AA.saveNumberInfoUserMark(list);
        }
    }

    public int updateMarkBigFile(String str, String str2) {
        f.f(TAG, "updateMarkBigFile");
        return this.AA.updateMarkBigFile(str, str2);
    }

    public int updateMarkFile(String str, String str2) {
        f.f(TAG, "updateMarkFile");
        return this.AA.updateMarkFile(str, str2);
    }
}
