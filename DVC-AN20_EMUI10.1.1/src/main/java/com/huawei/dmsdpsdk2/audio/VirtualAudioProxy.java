package com.huawei.dmsdpsdk2.audio;

import com.huawei.dmsdpsdk2.HwLog;
import java.util.Map;

public class VirtualAudioProxy {
    private static final String LOG_TAG = "VirtualAudioProxy";

    private VirtualAudioProxy() {
    }

    public static synchronized VirtualAudioProxy getInstance() {
        VirtualAudioProxy virtualAudioProxy;
        synchronized (VirtualAudioProxy.class) {
            virtualAudioProxy = VirtualAudioProxyHolder.INSTANCE;
        }
        return virtualAudioProxy;
    }

    public int stopAudioService(String deviceId, String serviceId, int serviceType, Map<String, Object> dataMap) {
        int audioStopResult = invokeVirtualAudio("removeVirtualAudio", deviceId, serviceId, serviceType, dataMap);
        HwLog.i(LOG_TAG, "audioStopResult is:" + audioStopResult);
        return audioStopResult;
    }

    private int invokeVirtualAudio(String methondName, String deviceId, String serviceId, int serviceType, Map<String, Object> map) {
        HwLog.i(LOG_TAG, "in hw parts invokeVirtualAudio");
        return -1;
    }

    /* access modifiers changed from: private */
    public static class VirtualAudioProxyHolder {
        private static final VirtualAudioProxy INSTANCE = new VirtualAudioProxy();

        private VirtualAudioProxyHolder() {
        }
    }
}
