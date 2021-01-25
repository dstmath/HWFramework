package com.huawei.server.camera;

import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.UEventObserverExt;
import com.huawei.hardware.CameraServiceEx;
import com.huawei.hwpartcameraservice.BuildConfig;
import java.util.HashMap;
import java.util.Map;

public class HwCameraLightStrapCaseImpl implements IBinder.DeathRecipient {
    private static final String CAMERA_SERVICE_BINDER_NAME = "media.camera";
    private static final int DEFAULT_FLAGS = 0;
    private static final String LIGHT_STRAP_CASE_RXID_KEY = "RXID";
    private static final String LIGHT_STRAP_CASE_STATUS_KEY = "LIGHTSTRAPCASE";
    private static final String LIGHT_STRAP_CASE_STATUS_ON = "ON";
    private static final String LIGHT_STRAP_CASE_UEVENT_KEY = "LIGHTSTRAPCASE=";
    private static final int MSG_START_WIRELESS_CHARGING = 1;
    private static final int MSG_STOP_WIRELESS_CHARGING = 2;
    private static final long STOP_WIRELESS_CHARING_DELAY = 70;
    private static final String TAG = "HwCameraLightStrapCaseImpl";
    private static final int VALID_FLASH_RING_RX_ID = 7;
    private static CameraServiceEx cameraServiceEx = null;
    private IBinder cameraServiceBinder = null;
    private Map<String, Integer> cameraStateMap = new HashMap();
    private Context context = null;
    private Handler handler;
    private volatile boolean isCameraOpened = false;
    private boolean isCameraServiceInit = false;
    private volatile boolean isLightStrapCaseOn = false;
    private boolean isStatusObserverStarted = false;
    private UEventObserverExt statusObserver = null;

    HwCameraLightStrapCaseImpl(Context context2) {
        this.context = context2;
        initHandler();
        initCameraService();
        startStatusObserver();
    }

    public void handleWithCameraState(String cameraId, int newCameraState) {
        if (!this.isCameraServiceInit) {
            Log.i(TAG, "CameraService is null, init CameraService again");
            initCameraService();
        }
        if (!this.isStatusObserverStarted && this.statusObserver != null) {
            Log.i(TAG, "StatusObserver has been stopped, start StatusObserver again");
            this.statusObserver.startObserving(LIGHT_STRAP_CASE_UEVENT_KEY);
            this.isStatusObserverStarted = true;
        }
        if (newCameraState == 0 || newCameraState == 3) {
            Log.i(TAG, "handleWithCameraState newCameraState = " + newCameraState);
            this.cameraStateMap.put(cameraId, Integer.valueOf(newCameraState));
            if (isCameraStateOpen()) {
                this.isCameraOpened = true;
                if (this.isLightStrapCaseOn) {
                    this.handler.removeMessages(MSG_STOP_WIRELESS_CHARGING);
                    Message.obtain(this.handler, 1).sendToTarget();
                } else {
                    Log.w(TAG, "camera opened, but isLightStrapCaseOn is false!");
                }
            }
            if (!isCameraStateOpen()) {
                this.isCameraOpened = false;
                if (this.isLightStrapCaseOn) {
                    this.handler.sendEmptyMessageDelayed(MSG_STOP_WIRELESS_CHARGING, STOP_WIRELESS_CHARING_DELAY);
                }
            }
        }
    }

    public boolean isLightStrapCaseOn() {
        StringBuilder sb = new StringBuilder();
        sb.append("Get lightStrapCase state ");
        sb.append(this.isLightStrapCaseOn ? "on" : "off");
        Log.i(TAG, sb.toString());
        return this.isLightStrapCaseOn;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initCameraService() {
        if (!this.isCameraServiceInit) {
            Log.d(TAG, "initCameraService");
            this.cameraServiceBinder = ServiceManagerEx.getService(CAMERA_SERVICE_BINDER_NAME);
            IBinder iBinder = this.cameraServiceBinder;
            if (iBinder == null) {
                Log.e(TAG, "The cameraServiceBinder is null");
                return;
            }
            try {
                iBinder.linkToDeath(this, DEFAULT_FLAGS);
            } catch (RemoteException e) {
                Log.e(TAG, "Could not link to death of native camera service");
            }
            if (cameraServiceEx == null) {
                cameraServiceEx = new CameraServiceEx(this.cameraServiceBinder);
                this.isCameraServiceInit = true;
            }
        }
    }

    private boolean isCameraStateOpen() {
        for (Integer camersState : this.cameraStateMap.values()) {
            if (camersState.equals(Integer.valueOf((int) DEFAULT_FLAGS))) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean startWirelessCharging() {
        Log.d(TAG, "startWirelessCharging");
        long ident = Binder.clearCallingIdentity();
        try {
            boolean result = cameraServiceEx.startWirelessCharging(this.context);
            if (!result) {
                Log.e(TAG, "startWirelessCharging failed, result = " + result);
            }
            return result;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean stopWirelessCharging() {
        Log.d(TAG, "stopWirelessCharging");
        long ident = Binder.clearCallingIdentity();
        try {
            boolean result = cameraServiceEx.stopWirelessCharging(this.context);
            if (!result) {
                Log.e(TAG, "stopWirelessCharging failed, result = " + result);
            }
            return result;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void startStatusObserver() {
        Log.i(TAG, "startStatusObserver for light strap case");
        this.statusObserver = new UEventObserverExt() {
            /* class com.huawei.server.camera.HwCameraLightStrapCaseImpl.AnonymousClass1 */

            public void onUEvent(UEventObserverExt.UEvent event) {
                String status = event.get(HwCameraLightStrapCaseImpl.LIGHT_STRAP_CASE_STATUS_KEY);
                Log.i(HwCameraLightStrapCaseImpl.TAG, "light strap case uevent arrived, status = " + status);
                if (HwCameraLightStrapCaseImpl.LIGHT_STRAP_CASE_STATUS_ON.equals(status) && !HwCameraLightStrapCaseImpl.this.isLightStrapCaseOn) {
                    if (!HwCameraLightStrapCaseImpl.this.isCameraServiceInit) {
                        Log.i(HwCameraLightStrapCaseImpl.TAG, "CameraService is null, init CameraService again");
                        HwCameraLightStrapCaseImpl.this.initCameraService();
                    }
                    if (HwCameraLightStrapCaseImpl.cameraServiceEx == null) {
                        Log.e(HwCameraLightStrapCaseImpl.TAG, "cameraServiceEx is null!");
                        return;
                    }
                    String rxIdStr = BuildConfig.FLAVOR;
                    try {
                        rxIdStr = event.get(HwCameraLightStrapCaseImpl.LIGHT_STRAP_CASE_RXID_KEY);
                        int rxId = Integer.parseInt(rxIdStr);
                        if (rxId == HwCameraLightStrapCaseImpl.VALID_FLASH_RING_RX_ID) {
                            HwCameraLightStrapCaseImpl.this.isLightStrapCaseOn = true;
                            if (HwCameraLightStrapCaseImpl.this.isCameraOpened) {
                                Message.obtain(HwCameraLightStrapCaseImpl.this.handler, 1).sendToTarget();
                                return;
                            }
                            return;
                        }
                        Log.w(HwCameraLightStrapCaseImpl.TAG, "light strap case on, but rxId is = " + rxId + ", not to charge");
                    } catch (NumberFormatException e) {
                        Log.e(HwCameraLightStrapCaseImpl.TAG, "light strap case rx id number format wrong! rxIdStr = " + rxIdStr);
                    }
                } else if (!HwCameraLightStrapCaseImpl.LIGHT_STRAP_CASE_STATUS_ON.equals(status)) {
                    HwCameraLightStrapCaseImpl.this.isLightStrapCaseOn = false;
                    if (HwCameraLightStrapCaseImpl.this.isCameraOpened) {
                        Message.obtain(HwCameraLightStrapCaseImpl.this.handler, (int) HwCameraLightStrapCaseImpl.MSG_STOP_WIRELESS_CHARGING).sendToTarget();
                    }
                }
            }
        };
        this.statusObserver.startObserving(LIGHT_STRAP_CASE_UEVENT_KEY);
        this.isStatusObserverStarted = true;
    }

    private void initHandler() {
        this.handler = new Handler(Looper.getMainLooper()) {
            /* class com.huawei.server.camera.HwCameraLightStrapCaseImpl.AnonymousClass2 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int what = msg.what;
                if (what == 1) {
                    HwCameraLightStrapCaseImpl.this.startWirelessCharging();
                } else if (what == HwCameraLightStrapCaseImpl.MSG_STOP_WIRELESS_CHARGING) {
                    HwCameraLightStrapCaseImpl.this.stopWirelessCharging();
                }
            }
        };
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        Log.w(TAG, "Native camera service has died");
        this.cameraServiceBinder = null;
        cameraServiceEx = null;
        this.isCameraServiceInit = false;
    }
}
