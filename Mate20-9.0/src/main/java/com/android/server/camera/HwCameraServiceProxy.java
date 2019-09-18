package com.android.server.camera;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import com.huawei.hwextdevice.HWExtDeviceManager;
import com.huawei.iimagekit.blur.util.SystemUtil;

public class HwCameraServiceProxy implements IHwCameraServiceProxy {
    private static final String CONFIG_HALL = "ro.config.hw_hall_prop";
    private static final String MEDIA_ASSIST_INTERFACE = "com.huawei.systemserver.mediaassist.IMediaAssist";
    private static final String MEDIA_ASSIST_SERVICE = "media_assist";
    private static final String TAG = "HwCameraServiceProxy";
    private static final int TYPE_CAMERA_AUTO = 2;
    private static final int TYPE_CAMERA_MANAUL = 1;
    private static final int TYPE_CAMERA_NORMAL = 0;
    private int mCameraDeviceType = 0;
    private Context mContext = null;
    private HwCameraAutoImpl mHwCameraAuto = null;
    private HwCameraManaulImpl mHwCameraManaul = null;

    public HwCameraServiceProxy(Context context) {
        if (context == null) {
            Slog.w(TAG, "Context == null");
            return;
        }
        this.mContext = context;
        this.mCameraDeviceType = getCameraDeviceType();
        Slog.d(TAG, "CameraDeviceType = " + this.mCameraDeviceType);
        initHwCameraServiceProxyImpl();
    }

    private void initHwCameraServiceProxyImpl() {
        switch (this.mCameraDeviceType) {
            case 1:
                this.mHwCameraManaul = new HwCameraManaulImpl(this.mContext);
                return;
            case 2:
                this.mHwCameraAuto = new HwCameraAutoImpl(this.mContext);
                return;
            default:
                return;
        }
    }

    private int getCameraDeviceType() {
        HWExtDeviceManager hwDeviceManager = HWExtDeviceManager.getInstance(this.mContext);
        if (hwDeviceManager == null || !hwDeviceManager.supportMotionFeature(3100)) {
            return SystemUtil.getSystemProperty(CONFIG_HALL, 0) == 1 ? 1 : 0;
        }
        return 2;
    }

    public void updateActivityCount(String cameraId, int newCameraState, int facing, String clientName) {
        if (newCameraState == 0) {
            if (this.mHwCameraManaul != null) {
                this.mHwCameraManaul.unRegisterSlideHallService();
            }
            if (this.mCameraDeviceType == 1) {
                this.mHwCameraManaul = new HwCameraManaulImpl(this.mContext);
                this.mHwCameraManaul.updateActivityCount(cameraId, newCameraState, facing, clientName);
            }
        }
        if (this.mHwCameraAuto != null) {
            this.mHwCameraAuto.updateActivityCount(cameraId, newCameraState, facing, clientName);
        }
        if (newCameraState != 0 && this.mHwCameraManaul != null) {
            this.mHwCameraManaul.updateActivityCount(cameraId, newCameraState, facing, clientName);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0041, code lost:
        if (r3 != null) goto L_0x0050;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004e, code lost:
        if (r3 == null) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0050, code lost:
        r3.recycle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0053, code lost:
        r2.recycle();
     */
    public void notifyCameraStateChange(String cameraId, int newCameraState, int facing, String clientName) {
        if (newCameraState == 1 || newCameraState == 2) {
            Slog.d(TAG, "notifyCameraState");
            IBinder mediaAssist = ServiceManager.getService(MEDIA_ASSIST_SERVICE);
            if (mediaAssist != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                if (data == null) {
                    Slog.e(TAG, "data is null");
                    return;
                }
                try {
                    data.writeInterfaceToken(MEDIA_ASSIST_INTERFACE);
                    data.writeString(cameraId);
                    data.writeInt(facing);
                    data.writeInt(newCameraState);
                    data.writeString(clientName);
                    mediaAssist.transact(1, data, reply, 0);
                    if (reply != null) {
                        reply.readException();
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "notifyCameraState transact error");
                } catch (Throwable th) {
                    if (reply != null) {
                        reply.recycle();
                    }
                    data.recycle();
                    throw th;
                }
            }
        }
    }
}
