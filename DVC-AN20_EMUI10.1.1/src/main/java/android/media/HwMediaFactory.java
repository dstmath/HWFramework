package android.media;

import android.camera.DefaultHwCameraUtil;
import android.common.FactoryLoader;
import android.mtp.DefaultHwMtpDatabaseManager;
import com.huawei.annotation.HwSystemApi;
import com.huawei.media.scan.DefaultHwMediaScanner;
import com.huawei.media.scan.DefaultHwMediaStore;

@HwSystemApi
public class HwMediaFactory {
    private static final String MEDIA_FACTORY_NAME = "android.media.HwMediaFactoryImpl";
    private static final String TAG = "HwMediaFactory";
    private static DefaultHwMediaFactory hwMediaFactory;

    static {
        hwMediaFactory = (DefaultHwMediaFactory) FactoryLoader.loadFactory(MEDIA_FACTORY_NAME);
        if (hwMediaFactory == null) {
            hwMediaFactory = new DefaultHwMediaFactory();
        }
    }

    public static DefaultHwMediaRecorder getHwMediaRecorder() {
        return hwMediaFactory.getHwMediaRecorder();
    }

    public static DefaultHwAudioRecord getHwAudioRecord() {
        return hwMediaFactory.getHwAudioRecord();
    }

    public static DefaultHwMediaMonitor getHwMediaMonitor() {
        return hwMediaFactory.getHwMediaMonitor();
    }

    public static DefaultHwDrmManager getHwDrmManager() {
        return hwMediaFactory.getHwDrmManager();
    }

    public static DefaultHwMtpDatabaseManager getHwMtpDatabaseManager() {
        return hwMediaFactory.getHwMtpDatabaseManager();
    }

    public static DefaultHwCameraUtil getHwCameraUtil() {
        return hwMediaFactory.getHwCameraUtil();
    }

    public static DefaultHwMediaScanner getHwMediaScanner() {
        return hwMediaFactory.getHwMediaScanner();
    }

    public static DefaultHwMediaStore getHwMediaStore() {
        return hwMediaFactory.getHwMediaStore();
    }
}
