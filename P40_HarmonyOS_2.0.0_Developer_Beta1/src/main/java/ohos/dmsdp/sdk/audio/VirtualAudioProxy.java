package ohos.dmsdp.sdk.audio;

import java.util.Map;
import ohos.dmsdp.sdk.HwLog;

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

    public int stopAudioService(String str, String str2, int i, Map<String, Object> map) {
        int invokeVirtualAudio = invokeVirtualAudio("removeVirtualAudio", str, str2, i, map);
        HwLog.i(LOG_TAG, "audioStopResult is:" + invokeVirtualAudio);
        return invokeVirtualAudio;
    }

    private int invokeVirtualAudio(String str, String str2, String str3, int i, Map<String, Object> map) {
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
