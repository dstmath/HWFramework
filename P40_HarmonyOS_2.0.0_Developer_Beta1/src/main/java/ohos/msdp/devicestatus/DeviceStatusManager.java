package ohos.msdp.devicestatus;

import android.content.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceStatusManager {
    private static final int DEFAULT_SIZE = 12;
    private static final Map<String, Integer> DEVICE_STATUS_A2H_TRANSFER_MAP = new HashMap();
    private static final Map<Integer, String> DEVICE_STATUS_H2A_TRANSFER_MAP = new HashMap();
    private static final String PERMISSION = "ohos.permission.DEVICE_ACTIVITY_MOTION";
    private static final String TAG = "DeviceStatusManager";
    private static DeviceStatusManager sInstance;
    private ServiceConnection mConnection;
    private HwMSDPDeviceStatus mHwMSDPDeviceStatus;
    private DeviceStatusListener mListener;
    private final HwMSDPDeviceStatusChangedCallBack mSdkCallback = new HwMSDPDeviceStatusChangedCallBack() {
        /* class ohos.msdp.devicestatus.DeviceStatusManager.AnonymousClass2 */

        @Override // ohos.msdp.devicestatus.HwMSDPDeviceStatusChangedCallBack
        public void onDeviceStatusChanged(HwMSDPDeviceStatusChangeEvent hwMSDPDeviceStatusChangeEvent) {
            Iterable<HwMSDPDeviceStatusEvent> deviceStatusRecognitionEvents;
            if (!(DeviceStatusManager.this.mListener == null || hwMSDPDeviceStatusChangeEvent == null || (deviceStatusRecognitionEvents = hwMSDPDeviceStatusChangeEvent.getDeviceStatusRecognitionEvents()) == null)) {
                ArrayList arrayList = new ArrayList(12);
                for (HwMSDPDeviceStatusEvent hwMSDPDeviceStatusEvent : deviceStatusRecognitionEvents) {
                    if (hwMSDPDeviceStatusEvent != null && DeviceStatusManager.DEVICE_STATUS_A2H_TRANSFER_MAP.containsKey(hwMSDPDeviceStatusEvent.getDeviceStatus())) {
                        arrayList.add(new DeviceStatusEvent(((Integer) DeviceStatusManager.DEVICE_STATUS_A2H_TRANSFER_MAP.get(hwMSDPDeviceStatusEvent.getDeviceStatus())).intValue(), hwMSDPDeviceStatusEvent.getEventType(), hwMSDPDeviceStatusEvent.getTimestampNs()));
                    }
                }
                LogUtils.i(DeviceStatusManager.TAG, "onDeviceStatusChanged size:" + arrayList.size());
                if (arrayList.size() > 0) {
                    DeviceStatusManager.this.mListener.onDeviceStatusChanged(arrayList);
                }
            }
        }
    };
    private final HwMSDPDeviceStatusServiceConnection mSdkConnection = new HwMSDPDeviceStatusServiceConnection() {
        /* class ohos.msdp.devicestatus.DeviceStatusManager.AnonymousClass1 */

        @Override // ohos.msdp.devicestatus.HwMSDPDeviceStatusServiceConnection
        public void onServiceConnected() {
            if (DeviceStatusManager.this.mConnection != null) {
                LogUtils.i(DeviceStatusManager.TAG, "onServiceConnected");
                DeviceStatusManager.this.mConnection.onServiceConnected();
            }
        }

        @Override // ohos.msdp.devicestatus.HwMSDPDeviceStatusServiceConnection
        public void onServiceDisconnected() {
            if (DeviceStatusManager.this.mConnection != null) {
                LogUtils.i(DeviceStatusManager.TAG, "onServiceDisconnected");
                DeviceStatusManager.this.mConnection.onServiceDisconnected();
            }
        }
    };

    private boolean isValidEventType(int i) {
        return i == 1 || i == 2;
    }

    static {
        DEVICE_STATUS_H2A_TRANSFER_MAP.put(19, HwMSDPDeviceStatusConstant.MSDP_DEVICETSTATUS_TYPE_CELL_CHANGED);
        DEVICE_STATUS_H2A_TRANSFER_MAP.put(18, HwMSDPDeviceStatusConstant.MSDP_DEVICETSTATUS_TYPE_COARSE_STILL);
        DEVICE_STATUS_H2A_TRANSFER_MAP.put(17, HwMSDPDeviceStatusConstant.MSDP_DEVICETSTATUS_TYPE_FINE_STILL);
        DEVICE_STATUS_H2A_TRANSFER_MAP.put(16, HwMSDPDeviceStatusConstant.MSDP_DEVICETSTATUS_TYPE_HIGH_STILL);
        DEVICE_STATUS_H2A_TRANSFER_MAP.put(20, HwMSDPDeviceStatusConstant.TYPE_CAR_BLUETOOTH);
        DEVICE_STATUS_H2A_TRANSFER_MAP.put(1, HwMSDPDeviceStatusConstant.TYPE_STILL_STATUS);
        DEVICE_STATUS_H2A_TRANSFER_MAP.forEach($$Lambda$DeviceStatusManager$xdYLlkcQbFh6ndDBOtbBovxTqM.INSTANCE);
    }

    private DeviceStatusManager(Context context) {
        this.mHwMSDPDeviceStatus = new HwMSDPDeviceStatus(context);
    }

    public static synchronized DeviceStatusManager getInstance(ohos.app.Context context, ServiceConnection serviceConnection) {
        synchronized (DeviceStatusManager.class) {
            LogUtils.i(TAG, "Get DeviceStatusManager instance begin");
            if (context == null) {
                LogUtils.e(TAG, "Context is null.");
                return null;
            }
            int verifySelfPermission = context.verifySelfPermission(PERMISSION);
            LogUtils.i(TAG, "permissionResult is:" + verifySelfPermission);
            if (verifySelfPermission != 0) {
                return null;
            }
            if (sInstance == null) {
                LogUtils.i(TAG, "Create DeviceStatusManager Instance now");
                ohos.app.Context applicationContext = context.getApplicationContext();
                if (applicationContext == null) {
                    LogUtils.e(TAG, "appContext is null");
                    return null;
                } else if (applicationContext.getHostContext() == null) {
                    LogUtils.e(TAG, "HostContext is null");
                    return null;
                } else if (!(applicationContext.getHostContext() instanceof Context)) {
                    LogUtils.e(TAG, "context transform failed");
                    return null;
                } else {
                    sInstance = new DeviceStatusManager((Context) applicationContext.getHostContext());
                    if (!sInstance.connectService(serviceConnection)) {
                        sInstance = null;
                        return null;
                    }
                }
            }
            LogUtils.i(TAG, "Get DeviceStatusManager Instance end");
            return sInstance;
        }
    }

    private boolean connectService(ServiceConnection serviceConnection) {
        if (serviceConnection == null) {
            LogUtils.e(TAG, "connectService failed! connection is null");
            return false;
        }
        this.mConnection = serviceConnection;
        HwMSDPDeviceStatus hwMSDPDeviceStatus = this.mHwMSDPDeviceStatus;
        if (hwMSDPDeviceStatus != null) {
            return hwMSDPDeviceStatus.connectService(this.mSdkCallback, this.mSdkConnection);
        }
        LogUtils.e(TAG, "connectService failed! mHwMSDPDeviceStatus is null");
        return false;
    }

    public boolean addEventListener(DeviceStatusListener deviceStatusListener) {
        if (deviceStatusListener == null) {
            LogUtils.e(TAG, "addEventListener failed, Listener is null");
            return false;
        }
        this.mListener = deviceStatusListener;
        return true;
    }

    public void removeEventListener() {
        this.mListener = null;
    }

    public boolean releaseInstance() {
        LogUtils.i(TAG, "release DeviceStatusManager Instance begin");
        synchronized (DeviceStatusManager.class) {
            if (sInstance == null) {
                return true;
            }
            if (this.mHwMSDPDeviceStatus == null || this.mHwMSDPDeviceStatus.disconnectService()) {
                release();
                return true;
            }
            LogUtils.e(TAG, "disconnectService failed");
            return false;
        }
    }

    private static synchronized void release() {
        synchronized (DeviceStatusManager.class) {
            sInstance = null;
        }
    }

    public boolean subscribe(int i, int i2, long j) {
        if (this.mHwMSDPDeviceStatus == null) {
            LogUtils.e(TAG, "enableEvent failed! mHwMSDPDeviceStatus is null");
            return false;
        } else if (!isValidEventType(i2) || j <= 0) {
            LogUtils.e(TAG, "enableEvent failed! eventType: " + i2 + ", reportLatencyNs: " + j);
            return false;
        } else {
            String str = DEVICE_STATUS_H2A_TRANSFER_MAP.get(Integer.valueOf(i));
            if (str != null) {
                return this.mHwMSDPDeviceStatus.enableDeviceStatusEvent(str, i2, j);
            }
            LogUtils.e(TAG, "enableEvent failed! Not supported " + i);
            return false;
        }
    }

    public boolean unsubscribe(int i, int i2) {
        if (this.mHwMSDPDeviceStatus == null) {
            LogUtils.e(TAG, "disableEvent failed! mHwMSDPDeviceStatus is null");
            return false;
        } else if (!isValidEventType(i2)) {
            LogUtils.e(TAG, "disableEvent failed! eventType: " + i2);
            return false;
        } else {
            String str = DEVICE_STATUS_H2A_TRANSFER_MAP.get(Integer.valueOf(i));
            if (str != null) {
                return this.mHwMSDPDeviceStatus.disableDeviceStatusEvent(str, i2);
            }
            LogUtils.e(TAG, "disableEvent failed!  Not supported " + i);
            return false;
        }
    }

    public List<DeviceStatusEvent> getCurrentStatus() {
        ArrayList arrayList = new ArrayList(12);
        HwMSDPDeviceStatus hwMSDPDeviceStatus = this.mHwMSDPDeviceStatus;
        if (hwMSDPDeviceStatus == null) {
            LogUtils.e(TAG, "getCurrentDeviceStatus failed! mHwMSDPDeviceStatus is null");
            return arrayList;
        }
        HwMSDPDeviceStatusChangeEvent currentDeviceStatus = hwMSDPDeviceStatus.getCurrentDeviceStatus();
        if (currentDeviceStatus == null) {
            LogUtils.e(TAG, "getCurrentDeviceStatus failed! changeEvent is null");
            return arrayList;
        }
        Iterable<HwMSDPDeviceStatusEvent> deviceStatusRecognitionEvents = currentDeviceStatus.getDeviceStatusRecognitionEvents();
        if (deviceStatusRecognitionEvents == null) {
            LogUtils.e(TAG, "getCurrentDeviceStatus failed! deviceStatusEvents is null");
            return arrayList;
        }
        for (HwMSDPDeviceStatusEvent hwMSDPDeviceStatusEvent : deviceStatusRecognitionEvents) {
            if (hwMSDPDeviceStatusEvent != null && DEVICE_STATUS_A2H_TRANSFER_MAP.containsKey(hwMSDPDeviceStatusEvent.getDeviceStatus())) {
                arrayList.add(new DeviceStatusEvent(DEVICE_STATUS_A2H_TRANSFER_MAP.get(hwMSDPDeviceStatusEvent.getDeviceStatus()).intValue(), hwMSDPDeviceStatusEvent.getEventType(), hwMSDPDeviceStatusEvent.getTimestampNs()));
            }
        }
        return arrayList;
    }

    public int[] getSupportedList() {
        HwMSDPDeviceStatus hwMSDPDeviceStatus = this.mHwMSDPDeviceStatus;
        if (hwMSDPDeviceStatus == null) {
            LogUtils.e(TAG, "getSupportedDeviceStatus failed! mHwMSDPDeviceStatus is null");
            return new int[0];
        }
        String[] supportedDeviceStatus = hwMSDPDeviceStatus.getSupportedDeviceStatus();
        if (supportedDeviceStatus == null) {
            LogUtils.e(TAG, "getSupportedDeviceStatus failed! supportedDeviceStatusInHw is null");
            return new int[0];
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < supportedDeviceStatus.length; i++) {
            arrayList.add(Integer.valueOf(DEVICE_STATUS_A2H_TRANSFER_MAP.containsKey(supportedDeviceStatus[i]) ? DEVICE_STATUS_A2H_TRANSFER_MAP.get(supportedDeviceStatus[i]).intValue() : -1));
        }
        return arrayList.stream().filter($$Lambda$DeviceStatusManager$ANK4suywJuKiEzt10jS5FFpnc0.INSTANCE).mapToInt($$Lambda$DeviceStatusManager$gfCssnBJI7TKfXb_Jmv7raVYNkY.INSTANCE).toArray();
    }

    static /* synthetic */ boolean lambda$getSupportedList$1(Integer num) {
        return num.intValue() >= 0;
    }
}
