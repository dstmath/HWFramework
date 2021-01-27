package com.android.server.mtm.iaware.appmng;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class AwareProcessBaseInfo {
    public String adjType = "";
    public int appUid;
    public int curAdj = 0;
    public boolean foregroundActivities = false;
    public boolean hasShownUi;
    public int setProcState;
    public int targetSdkVersion;
    public int uid;

    public AwareProcessBaseInfo copy() {
        AwareProcessBaseInfo dst = new AwareProcessBaseInfo();
        dst.uid = this.uid;
        dst.appUid = this.appUid;
        dst.targetSdkVersion = this.targetSdkVersion;
        dst.setProcState = this.setProcState;
        dst.curAdj = this.curAdj;
        dst.adjType = this.adjType;
        dst.foregroundActivities = this.foregroundActivities;
        dst.hasShownUi = this.hasShownUi;
        return dst;
    }
}
