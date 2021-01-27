package ohos.dmsdp.sdk.camera;

import java.util.HashMap;
import java.util.Map;
import ohos.global.icu.impl.PatternTokenizer;

public class CameraCustomDefine {
    private static final int COLLECTION_SIZE = 8;
    private Map<Integer, CameraExtInfo> androidCamInfos = new HashMap(8);
    private Map<String, CameraExtInfo> nativeCamInfos = new HashMap(8);

    public Map<Integer, CameraExtInfo> getAndroidCamInfos() {
        return this.androidCamInfos;
    }

    public void setAndroidCamInfos(Map<Integer, CameraExtInfo> map) {
        this.androidCamInfos = map;
    }

    public Map<String, CameraExtInfo> getNativeCamInfos() {
        return this.nativeCamInfos;
    }

    public void setNativeCamInfos(Map<String, CameraExtInfo> map) {
        this.nativeCamInfos = map;
    }

    public String toString() {
        return "CameraCustomDefine{androidCamInfos=" + this.androidCamInfos.toString() + ", nativeCamInfos='" + this.nativeCamInfos.toString() + PatternTokenizer.SINGLE_QUOTE + '}';
    }
}
