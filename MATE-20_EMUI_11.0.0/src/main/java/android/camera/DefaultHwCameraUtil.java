package android.camera;

import android.util.ArrayMap;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DefaultHwCameraUtil implements IHwCameraUtil {
    private static final String TAG = "DefaultHwCameraUtil";
    private static DefaultHwCameraUtil defaultHwCameraUtil = new DefaultHwCameraUtil();

    public static DefaultHwCameraUtil getDefault() {
        return defaultHwCameraUtil;
    }

    @Override // android.camera.IHwCameraUtil
    public boolean notifySurfaceFlingerCameraStatus(boolean isFront, boolean isOpen) {
        Log.w(TAG, "notifySurfaceFlingerCameraStatus");
        return true;
    }

    @Override // android.camera.IHwCameraUtil
    public boolean needHideAuxCamera(int deviceNum) {
        Log.d(TAG, "needHideAuxCamera");
        return true;
    }

    @Override // android.camera.IHwCameraUtil
    public boolean isIllegalAccessAuxCamera(int deviceNum, String cameraId) {
        Log.d(TAG, "isIllegalAccessAuxCamera");
        return true;
    }

    @Override // android.camera.IHwCameraUtil
    public int filterVirtualCamera(ArrayMap<String, Integer> arrayMap, int deviceSize) {
        Log.d(TAG, "filterVirtualCamera");
        return deviceSize;
    }
}
