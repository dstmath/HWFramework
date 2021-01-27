package android.camera;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.Map;

public class HwCameraUtil extends DefaultHwCameraUtil {
    private static final int AUX_CAMERA_ID_DEFAULT_QCOM = 2;
    private static final int MIN_VIRTUAL_CAMERA_ID = 1000;
    private static final int NOTIFY_SURFACEFLINGER_FRONT_CAMERA_CLOSE = 8012;
    private static final int NOTIFY_SURFACEFLINGER_FRONT_CAMERA_OPEN = 8011;
    private static final String TAG = "HwCameraUtil";
    private static HwCameraUtil mInstance = null;
    private static final Object mLock = new Object();

    public static HwCameraUtil getDefault() {
        HwCameraUtil hwCameraUtil;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new HwCameraUtil();
            }
            hwCameraUtil = mInstance;
        }
        return hwCameraUtil;
    }

    public boolean notifySurfaceFlingerCameraStatus(boolean isFront, boolean isOpend) {
        Log.i(TAG, "notifySurfaceFlingerCameraStatus : isFront = " + isFront + " , isOpend = " + isOpend);
        boolean isTransactSuccess = false;
        if (!isFront) {
            return false;
        }
        Parcel dataIn = Parcel.obtain();
        try {
            IBinder sfBinder = ServiceManagerEx.getService("SurfaceFlinger");
            int status = isOpend ? NOTIFY_SURFACEFLINGER_FRONT_CAMERA_OPEN : NOTIFY_SURFACEFLINGER_FRONT_CAMERA_CLOSE;
            if (sfBinder == null) {
                Log.i(TAG, "sfBinder == null");
            } else if (sfBinder.transact(status, dataIn, null, 1)) {
                isTransactSuccess = true;
                Log.i(TAG, "notifySurfaceFlingerFrontCameraStatus " + status + " transact success!");
            } else {
                Log.e(TAG, "notifySurfaceFlingerFrontCameraStatus " + status + " transact failed!");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException notifySurfaceFlingerFrontCameraStatus");
        } catch (Throwable th) {
            dataIn.recycle();
            throw th;
        }
        dataIn.recycle();
        return isTransactSuccess;
    }

    public boolean needHideAuxCamera(int deviceNum) {
        String specialList;
        if (deviceNum <= 2 || (specialList = SystemPropertiesEx.get("ro.camera.aux.packagelist")) == null || specialList.isEmpty()) {
            return false;
        }
        String packageName = ActivityThreadEx.currentOpPackageName();
        if (packageName == null) {
            Log.i(TAG, "invalide package name, device number: " + deviceNum);
            return false;
        } else if (!specialList.contains(packageName)) {
            Log.i(TAG, "hide aux camera for app: " + packageName);
            return true;
        } else {
            Log.i(TAG, "package name: " + packageName);
            return false;
        }
    }

    public boolean isIllegalAccessAuxCamera(int deviceNum, String cameraId) {
        try {
            return needHideAuxCamera(deviceNum) && Integer.parseInt(cameraId) >= 2;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Fail to parse int: " + e.getMessage());
            return true;
        }
    }

    public int filterVirtualCamera(ArrayMap<String, Integer> deviceStatus, int deviceSize) {
        if (deviceStatus == null || deviceStatus.isEmpty()) {
            Log.e(TAG, "filterVirtualCamera : deviceStatus is null or empty");
            return deviceSize;
        }
        int newDeviceSize = deviceSize;
        for (Map.Entry<String, Integer> entry : deviceStatus.entrySet()) {
            String cameraId = entry.getKey();
            try {
                if (Integer.parseInt(cameraId) >= MIN_VIRTUAL_CAMERA_ID) {
                    newDeviceSize--;
                    Log.d(TAG, "Filter virtual cameraId: " + cameraId);
                }
            } catch (NumberFormatException e) {
                newDeviceSize--;
                Log.e(TAG, "Fail to parse cameraId: " + cameraId);
            }
        }
        return newDeviceSize;
    }
}
