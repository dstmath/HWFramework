package android.media;

import android.camera.DefaultHwCameraUtil;
import android.camera.HwCameraUtil;
import android.mtp.DefaultHwMtpDatabaseManager;
import android.mtp.HwMtpDatabaseImpl;
import com.huawei.media.scan.DefaultHwMediaScanner;
import com.huawei.media.scan.DefaultHwMediaStore;
import com.huawei.media.scan.HwMediaScannerImpl;
import com.huawei.media.scan.HwMediaStoreImpl;
import huawei.android.hwdrm.HwDrmManagerImpl;

public class HwMediaFactoryImpl extends DefaultHwMediaFactory {
    private static final String TAG = "HwMediaFactoryImpl";

    public DefaultHwMediaRecorder getHwMediaRecorder() {
        return HwMediaRecorderImpl.getDefault();
    }

    public DefaultHwAudioRecord getHwAudioRecord() {
        return HwAudioRecordImpl.getDefault();
    }

    public DefaultHwMediaMonitor getHwMediaMonitor() {
        return HwMediaMonitorImpl.getDefault();
    }

    public DefaultHwDrmManager getHwDrmManager() {
        return HwDrmManagerImpl.getDefault();
    }

    public DefaultHwMtpDatabaseManager getHwMtpDatabaseManager() {
        return HwMtpDatabaseImpl.getDefault();
    }

    public DefaultHwMediaScanner getHwMediaScanner() {
        return HwMediaScannerImpl.getDefault();
    }

    public DefaultHwMediaStore getHwMediaStore() {
        return HwMediaStoreImpl.getDefault();
    }

    public DefaultHwCameraUtil getHwCameraUtil() {
        return HwCameraUtil.getDefault();
    }
}
