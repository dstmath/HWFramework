package com.huawei.dmsdpsdk2.audio;

import com.huawei.android.media.AudioServiceEx;
import com.huawei.dmsdpsdk2.HwLog;
import java.lang.reflect.InvocationTargetException;
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

    private int invokeVirtualAudio(String methondName, String deviceId, String serviceId, int serviceType, Map<String, Object> dataMap) {
        NoSuchMethodException e;
        IllegalAccessException e2;
        InvocationTargetException e3;
        HwLog.i(LOG_TAG, "in hw parts invokeVirtualAudio");
        try {
            AudioServiceEx audioServiceEx = AudioServiceEx.getInstance();
            try {
                return ((Integer) audioServiceEx.getClass().getDeclaredMethod(methondName, String.class, String.class, Integer.TYPE, Map.class).invoke(audioServiceEx, deviceId, serviceId, Integer.valueOf(serviceType), dataMap)).intValue();
            } catch (NoSuchMethodException e4) {
                e = e4;
                HwLog.e(LOG_TAG, e.getMessage());
                return -1;
            } catch (IllegalAccessException e5) {
                e2 = e5;
                HwLog.e(LOG_TAG, e2.getMessage());
                return -1;
            } catch (InvocationTargetException e6) {
                e3 = e6;
                HwLog.e(LOG_TAG, e3.getMessage());
                return -1;
            }
        } catch (NoSuchMethodException e7) {
            e = e7;
            HwLog.e(LOG_TAG, e.getMessage());
            return -1;
        } catch (IllegalAccessException e8) {
            e2 = e8;
            HwLog.e(LOG_TAG, e2.getMessage());
            return -1;
        } catch (InvocationTargetException e9) {
            e3 = e9;
            HwLog.e(LOG_TAG, e3.getMessage());
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public static class VirtualAudioProxyHolder {
        private static final VirtualAudioProxy INSTANCE = new VirtualAudioProxy();

        private VirtualAudioProxyHolder() {
        }
    }
}
