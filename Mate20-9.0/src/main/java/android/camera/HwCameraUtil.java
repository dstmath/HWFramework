package android.camera;

import android.app.ActivityThread;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;

public class HwCameraUtil implements IHwCameraUtil {
    private static final int AUX_CAMERA_ID_DEFAULT_QCOM = 2;
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

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    public boolean notifySurfaceFlingerCameraStatus(boolean isFront, boolean isOpend) {
        Log.i(TAG, "notifySurfaceFlingerCameraStatus : isFront = " + isFront + " , isOpend = " + isOpend);
        boolean isTransactSuccess = false;
        if (!isFront) {
            return false;
        }
        Parcel dataIn = Parcel.obtain();
        try {
            IBinder sfBinder = ServiceManager.getService("SurfaceFlinger");
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
        }
        dataIn.recycle();
        return isTransactSuccess;
    }

    public boolean needHideAuxCamera(int deviceNum) {
        if (deviceNum <= 2) {
            return false;
        }
        String specialList = SystemProperties.get("ro.camera.aux.packagelist");
        if (specialList == null || specialList.isEmpty()) {
            return false;
        }
        String packageName = ActivityThread.currentOpPackageName();
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
        return needHideAuxCamera(deviceNum) && Integer.parseInt(cameraId) >= 2;
    }
}
