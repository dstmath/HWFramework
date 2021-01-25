package com.huawei.dmsdpsdk2.camera;

import java.util.HashMap;
import java.util.Map;

public class CameraCustomDefine {
    private Map<Integer, CameraExtInfo> androidCamInfos = new HashMap(0);
    private Map<String, CameraExtInfo> nativeCamInfos = new HashMap(0);

    public Map<Integer, CameraExtInfo> getAndroidCamInfos() {
        return this.androidCamInfos;
    }

    public void setAndroidCamInfos(Map<Integer, CameraExtInfo> androidCamInfos2) {
        this.androidCamInfos = androidCamInfos2;
    }

    public Map<String, CameraExtInfo> getNativeCamInfos() {
        return this.nativeCamInfos;
    }

    public void setNativeCamInfos(Map<String, CameraExtInfo> nativeCamInfos2) {
        this.nativeCamInfos = nativeCamInfos2;
    }

    public String toString() {
        return "CameraCustomDefine{androidCamInfos=" + this.androidCamInfos.toString() + ", nativeCamInfos='" + this.nativeCamInfos.toString() + "'}";
    }
}
