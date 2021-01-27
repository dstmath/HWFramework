package com.android.server.am;

import android.content.ContentResolver;

public class HwCustActivityManagerService {
    /* access modifiers changed from: protected */
    public boolean isAllowRamCompress() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void setEvent(String event) {
    }

    /* access modifiers changed from: protected */
    public int addProcesstoPersitList(ProcessRecord proc) {
        return proc.maxAdj;
    }

    /* access modifiers changed from: protected */
    public boolean isIQIEnable() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isAddRestrictedForCust(String pkgName) {
        return false;
    }

    public boolean isInMultiWinBlackList(String pkg, ContentResolver resolver) {
        return false;
    }

    public boolean notKillProcessWhenRemoveTask(ProcessRecord processRecord, ContentResolver resolver) {
        return true;
    }
}
