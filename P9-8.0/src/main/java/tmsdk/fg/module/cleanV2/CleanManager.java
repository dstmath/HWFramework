package tmsdk.fg.module.cleanV2;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import tmsdk.common.utils.f;
import tmsdk.fg.creator.BaseManagerF;
import tmsdkobf.kt;
import tmsdkobf.rh;

public class CleanManager extends BaseManagerF {
    public static final int DISK_SCAN_TAG = 0;
    public static final int EASY_SCAN_TAG = 1;
    public static final int ERROR_SCAN_ENGINE_FAILED = -1;
    public static final int ERROR_SCAN_EXCEPTION = -4;
    public static final int ERROR_SCAN_LOAD_APP_RULE_FAILED = -5;
    public static final int ERROR_SCAN_LOAD_RULE_FAILED = -2;
    public static final int ERROR_SCAN_NOT_INIT = -6;
    public static final int ERROR_SCAN_SEARCH_SD_FAILED = -3;
    public static final int PKG_SCAN_TAG = 2;
    public static final String TAG = "TMSDK_CleanManager";
    a Mj;

    public boolean SlowCleanRubbish(RubbishHolder rubbishHolder, ICleanTaskCallBack iCleanTaskCallBack) {
        return this.Mj != null ? this.Mj.SlowCleanRubbish(rubbishHolder, iCleanTaskCallBack) : false;
    }

    public boolean addUninstallPkg(String str) {
        return this.Mj != null ? this.Mj.addUninstallPkg(str) : false;
    }

    public void appendCustomSdcardRoots(String str) {
        rh.appendCustomSdcardRoots(str);
    }

    public boolean cancelClean() {
        return this.Mj != null ? this.Mj.cancelClean() : false;
    }

    public boolean cancelScan(int i) {
        return this.Mj != null ? this.Mj.cancelScan(i) : false;
    }

    public boolean cleanRubbish(RubbishHolder rubbishHolder, ICleanTaskCallBack iCleanTaskCallBack) {
        return this.Mj != null ? this.Mj.cleanRubbish(rubbishHolder, iCleanTaskCallBack) : false;
    }

    public void clearCustomSdcardRoots() {
        rh.clearCustomSdcardRoots();
    }

    public boolean delUninstallPkg(String str) {
        return this.Mj != null ? this.Mj.delUninstallPkg(str) : false;
    }

    public boolean easyScan(IScanTaskCallBack iScanTaskCallBack, Set<String> set) {
        f.f(TAG, "easyScan");
        if (this.Mj == null) {
            return false;
        }
        kt.saveActionData(1320005);
        return this.Mj.easyScan(iScanTaskCallBack, set);
    }

    public AppGroupDesc getGroupInfo(int i) {
        return this.Mj != null ? this.Mj.getGroupInfo(i) : null;
    }

    public List<String> getSdcardRoots() {
        return new ArrayList(rh.ke());
    }

    public void onCreate(Context context) {
        this.Mj = new a();
        this.Mj.onCreate(context);
        a(this.Mj);
    }

    public void onDestroy() {
        if (this.Mj != null) {
            this.Mj.onDestroy();
        }
    }

    public void privateAppScan(IScanTaskCallBack iScanTaskCallBack, String str) {
        f.f(TAG, "privateAppScan");
        this.Mj.a(iScanTaskCallBack, str);
    }

    public void privateAppScanCancel() {
        this.Mj.jl();
    }

    public boolean scan4app(String str, IScanTaskCallBack iScanTaskCallBack) {
        f.f(TAG, "scan4app");
        if (this.Mj == null) {
            return false;
        }
        kt.aE(29994);
        return this.Mj.scan4app(str, iScanTaskCallBack);
    }

    public boolean scanDisk(IScanTaskCallBack iScanTaskCallBack, Set<String> set) {
        f.f(TAG, "scanDisk");
        if (this.Mj != null) {
            kt.saveActionData(29965);
            return this.Mj.scanDisk(iScanTaskCallBack, set);
        }
        f.e("ZhongSi", "scanDisk: mImpl is null");
        return false;
    }

    public void setIgnoredSdcardRoots(List<String> list) {
        rh.E(list);
    }

    public boolean updateRule(IUpdateCallBack iUpdateCallBack, long j) {
        f.f(TAG, "updateRule");
        if (this.Mj == null) {
            return false;
        }
        kt.saveActionData(29963);
        return this.Mj.a(iUpdateCallBack);
    }
}
