package tmsdk.common.module.numbermarker;

import android.content.Context;
import android.text.TextUtils;
import java.util.LinkedHashMap;
import java.util.List;
import tmsdk.common.NumMarker.MarkFileInfo;
import tmsdk.common.creator.BaseManagerC;
import tmsdkobf.jg;
import tmsdkobf.ma;

/* compiled from: Unknown */
public class NumMarkerManager extends BaseManagerC {
    private a CN;

    public int cloudFetchNumberInfo(List<NumQueryReq> list, INumQueryRetListener iNumQueryRetListener) {
        if (jg.cl()) {
            return -1;
        }
        if (list == null || list.size() == 0 || iNumQueryRetListener == null) {
            return -2;
        }
        this.CN.a(list, iNumQueryRetListener);
        return 0;
    }

    public boolean cloudReportPhoneNum(List<NumberMarkEntity> list, OnNumMarkReportFinish onNumMarkReportFinish) {
        if (jg.cl() || list == null || list.size() == 0 || onNumMarkReportFinish == null) {
            return false;
        }
        this.CN.cloudReportPhoneNum(list, onNumMarkReportFinish);
        return true;
    }

    public int getConfigValue(int i) {
        return !jg.cl() ? this.CN.getConfigValue(i) : -1;
    }

    public String getDataMd5(String str) {
        return (jg.cl() || str == null) ? null : this.CN.getDataMd5(str);
    }

    public MarkFileInfo getMarkFileInfo() {
        return !jg.cl() ? this.CN.getMarkFileInfo() : null;
    }

    public String getTagName(int i) {
        return !jg.cl() ? this.CN.getTagName(i) : null;
    }

    public LinkedHashMap<Integer, String> getTagNameMap() {
        return !jg.cl() ? this.CN.getTagNameMap() : null;
    }

    public NumQueryRet localFetchNumberInfo(String str) {
        if (jg.cl() || TextUtils.isEmpty(str)) {
            return null;
        }
        ma.bx(29961);
        return this.CN.localFetchNumberInfo(str);
    }

    public void onCreate(Context context) {
        this.CN = new a();
        this.CN.onCreate(context);
        a(this.CN);
    }

    public void reInit() {
        if (!jg.cl()) {
            this.CN.reInit();
        }
    }

    public void refreshTagMap() {
        if (!jg.cl()) {
            this.CN.refreshTagMap();
        }
    }

    @Deprecated
    public int reportPhoneNumber(List<NumberMarkEntity> list) {
        return this.CN.reportPhoneNumber(list);
    }

    public int updateMarkFile(String str, String str2) {
        return !jg.cl() ? this.CN.updateMarkFile(str, str2) : -1;
    }
}
