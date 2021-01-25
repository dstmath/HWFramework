package android.media;

import android.camera.DefaultHwCameraUtil;
import android.mtp.DefaultHwMtpDatabaseManager;
import com.huawei.annotation.HwSystemApi;
import com.huawei.media.scan.DefaultHwMediaScanner;
import com.huawei.media.scan.DefaultHwMediaStore;

@HwSystemApi
public class DefaultHwMediaFactory {
    protected DefaultHwMediaFactory() {
    }

    public DefaultHwMediaRecorder getHwMediaRecorder() {
        return DefaultHwMediaRecorder.getDefault();
    }

    public DefaultHwAudioRecord getHwAudioRecord() {
        return DefaultHwAudioRecord.getDefault();
    }

    public DefaultHwMediaMonitor getHwMediaMonitor() {
        return DefaultHwMediaMonitor.getDefault();
    }

    public DefaultHwDrmManager getHwDrmManager() {
        return DefaultHwDrmManager.getDefault();
    }

    public DefaultHwMtpDatabaseManager getHwMtpDatabaseManager() {
        return DefaultHwMtpDatabaseManager.getDefault();
    }

    public DefaultHwCameraUtil getHwCameraUtil() {
        return DefaultHwCameraUtil.getDefault();
    }

    public DefaultHwMediaScanner getHwMediaScanner() {
        return DefaultHwMediaScanner.getDefault();
    }

    public DefaultHwMediaStore getHwMediaStore() {
        return DefaultHwMediaStore.getDefault();
    }
}
