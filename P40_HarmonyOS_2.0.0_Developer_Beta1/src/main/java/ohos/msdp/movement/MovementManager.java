package ohos.msdp.movement;

import android.content.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovementManager {
    private static final int DEFAULT_SIZE = 16;
    private static final Map<Integer, String> MOVEMENT_H2A_TRANSFER_MAP = new HashMap();
    private static final Map<String, Integer> MOVEMENT_STATUS_A2H_TRANSFER_MAP = new HashMap();
    private static final String MSDP_MOVEMENT_LYING_POSTURE = "lying_posture";
    private static final String MSDP_MOVEMENT_WALKING_HANDHOLD = "walking_handhold";
    private static final HwMSDPOtherParameters PARAM_NO_WAKEUP = new HwMSDPOtherParameters(1.0d, 0.0d, 0.0d, 0.0d, "");
    private static final String PERMISSION = "ohos.permission.ACTIVITY_MOTION";
    private static final String TAG = "MovementManager";
    private static MovementManager sInstance;
    private ServiceConnection mConnection;
    private HwMSDPMovementManager mHwMovementManager;
    private MovementEventListener mListener;
    private final HwMSDPMovementStatusChangeCallback mSdkCallback = new HwMSDPMovementStatusChangeCallback() {
        /* class ohos.msdp.movement.MovementManager.AnonymousClass2 */

        @Override // ohos.msdp.movement.HwMSDPMovementStatusChangeCallback
        public void onMovementStatusChanged(int i, HwMSDPMovementChangeEvent hwMSDPMovementChangeEvent) {
            Iterable<HwMSDPMovementEvent> movementEvents;
            if (!(MovementManager.this.mListener == null || hwMSDPMovementChangeEvent == null || (movementEvents = hwMSDPMovementChangeEvent.getMovementEvents()) == null)) {
                ArrayList arrayList = new ArrayList(16);
                for (HwMSDPMovementEvent hwMSDPMovementEvent : movementEvents) {
                    if (hwMSDPMovementEvent != null && MovementManager.MOVEMENT_STATUS_A2H_TRANSFER_MAP.containsKey(hwMSDPMovementEvent.getMovement())) {
                        arrayList.add(new MovementEvent(((Integer) MovementManager.MOVEMENT_STATUS_A2H_TRANSFER_MAP.get(hwMSDPMovementEvent.getMovement())).intValue(), hwMSDPMovementEvent.getEventType(), hwMSDPMovementEvent.getTimestampNs()));
                    }
                }
                LogUtils.i(MovementManager.TAG, "onMovementStatusChanged size: " + arrayList.size());
                if (arrayList.size() > 0) {
                    MovementManager.this.mListener.onMovementChanged(arrayList);
                }
            }
        }
    };
    private final HwMSDPMovementServiceConnection mSdkConnection = new HwMSDPMovementServiceConnection() {
        /* class ohos.msdp.movement.MovementManager.AnonymousClass1 */

        @Override // ohos.msdp.movement.HwMSDPMovementServiceConnection
        public void onServiceConnected() {
            if (MovementManager.this.mConnection != null) {
                LogUtils.i(MovementManager.TAG, "onServiceConnected");
                MovementManager.this.mConnection.onServiceConnected();
            }
        }

        @Override // ohos.msdp.movement.HwMSDPMovementServiceConnection
        public void onServiceDisconnected(Boolean bool) {
            if (MovementManager.this.mConnection != null && bool != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("onServiceConnected isNeedToReconnect: ");
                sb.append(!bool.booleanValue());
                LogUtils.i(MovementManager.TAG, sb.toString());
                MovementManager.this.mConnection.onServiceDisconnected();
            }
        }
    };

    private boolean isValidEventType(int i) {
        return i == 1 || i == 2;
    }

    static {
        MOVEMENT_H2A_TRANSFER_MAP.put(0, MovementEventConstant.MSDP_MOVEMENT_IN_VEHICLE);
        MOVEMENT_H2A_TRANSFER_MAP.put(1, MovementEventConstant.MSDP_MOVEMENT_ON_BICYCLE);
        MOVEMENT_H2A_TRANSFER_MAP.put(2, MovementEventConstant.MSDP_MOVEMENT_WALKING);
        MOVEMENT_H2A_TRANSFER_MAP.put(3, MovementEventConstant.MSDP_MOVEMENT_RUNNING);
        MOVEMENT_H2A_TRANSFER_MAP.put(4, MovementEventConstant.MSDP_MOVEMENT_STILL);
        MOVEMENT_H2A_TRANSFER_MAP.put(24, MovementEventConstant.MSDP_MOVEMENT_FAST_WALKING);
        MOVEMENT_H2A_TRANSFER_MAP.put(20, MovementEventConstant.MSDP_MOVEMENT_HIGH_SPEED_RAIL);
        MOVEMENT_H2A_TRANSFER_MAP.put(28, MovementEventConstant.MSDP_MOVEMENT_ON_FOOT);
        MOVEMENT_H2A_TRANSFER_MAP.put(32, MovementEventConstant.MSDP_MOVEMENT_ELEVATOR);
        MOVEMENT_H2A_TRANSFER_MAP.put(33, MovementEventConstant.MSDP_MOVEMENT_RELATIVE_STILL);
        MOVEMENT_H2A_TRANSFER_MAP.put(37, MovementEventConstant.MSDP_MOVEMENT_SMART_FLIGHT);
        MOVEMENT_H2A_TRANSFER_MAP.forEach($$Lambda$MovementManager$KRVNrAi3M5w16d4guY13Yc6pHE.INSTANCE);
    }

    private MovementManager(Context context) {
        this.mHwMovementManager = new HwMSDPMovementManager(context);
    }

    public static synchronized MovementManager getInstance(ohos.app.Context context, ServiceConnection serviceConnection) {
        synchronized (MovementManager.class) {
            LogUtils.i(TAG, "Get MovementManager instance begin");
            if (context == null) {
                LogUtils.e(TAG, "Context is null");
                return null;
            }
            int verifySelfPermission = context.verifySelfPermission("ohos.permission.ACTIVITY_MOTION");
            LogUtils.i(TAG, "permissionResult is:" + verifySelfPermission);
            if (verifySelfPermission != 0) {
                return null;
            }
            if (sInstance == null) {
                LogUtils.i(TAG, "Create MovementManager Instance now");
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
                    sInstance = new MovementManager((Context) applicationContext.getHostContext());
                    if (!sInstance.connectService(serviceConnection)) {
                        sInstance = null;
                        return null;
                    }
                }
            }
            LogUtils.i(TAG, "Get MovementManager Instance end");
            return sInstance;
        }
    }

    private boolean connectService(ServiceConnection serviceConnection) {
        if (serviceConnection == null) {
            LogUtils.e(TAG, "connectService failed! connection is null");
            return false;
        }
        this.mConnection = serviceConnection;
        HwMSDPMovementManager hwMSDPMovementManager = this.mHwMovementManager;
        if (hwMSDPMovementManager != null) {
            return hwMSDPMovementManager.connectService(this.mSdkCallback, this.mSdkConnection);
        }
        LogUtils.e(TAG, "connectService failed! mHwMovementManager is null");
        return false;
    }

    public boolean addEventListener(MovementEventListener movementEventListener) {
        if (movementEventListener == null) {
            LogUtils.e(TAG, "addEventListener failed, Listener is null");
            return false;
        }
        this.mListener = movementEventListener;
        return true;
    }

    public void removeEventListener() {
        this.mListener = null;
    }

    public boolean releaseInstance() {
        synchronized (MovementManager.class) {
            LogUtils.i(TAG, "release MovementManager Instance begin");
            if (sInstance == null) {
                return true;
            }
            if (this.mHwMovementManager == null || this.mHwMovementManager.disconnectService()) {
                release();
                return true;
            }
            LogUtils.e(TAG, "disconnectService failed");
            return false;
        }
    }

    private static synchronized void release() {
        synchronized (MovementManager.class) {
            sInstance = null;
        }
    }

    public int[] getSupportedList() {
        HwMSDPMovementManager hwMSDPMovementManager = this.mHwMovementManager;
        if (hwMSDPMovementManager == null) {
            LogUtils.e(TAG, "getSupportedMovements failed! mHwMovementManager is null");
            return new int[0];
        }
        String[] supportedMovements = hwMSDPMovementManager.getSupportedMovements(1);
        if (supportedMovements == null || supportedMovements.length <= 0) {
            LogUtils.e(TAG, "getSupportedMovements failed! supportedMovements is empty");
            return new int[0];
        }
        ArrayList arrayList = new ArrayList(16);
        for (int i = 0; i < supportedMovements.length; i++) {
            if (supportedMovements[i] != null) {
                if (supportedMovements[i].contains(MSDP_MOVEMENT_WALKING_HANDHOLD) || supportedMovements[i].contains(MSDP_MOVEMENT_LYING_POSTURE)) {
                    LogUtils.w(TAG, "getSupportedMovements ignore walking_handhold && lying_posture");
                } else {
                    arrayList.add(Integer.valueOf(MOVEMENT_STATUS_A2H_TRANSFER_MAP.containsKey(supportedMovements[i]) ? MOVEMENT_STATUS_A2H_TRANSFER_MAP.get(supportedMovements[i]).intValue() : -1));
                }
            }
        }
        return arrayList.stream().filter($$Lambda$MovementManager$ZRVwJYHTnAsjNMdPNs9yo3vAF4c.INSTANCE).mapToInt($$Lambda$MovementManager$gfCssnBJI7TKfXb_Jmv7raVYNkY.INSTANCE).toArray();
    }

    static /* synthetic */ boolean lambda$getSupportedList$1(Integer num) {
        return num.intValue() >= 0;
    }

    public boolean subscribe(int i, int i2, long j) {
        if (!isValidEventType(i2) || j <= 0) {
            LogUtils.e(TAG, "subscribe failed! eventType: " + i2 + ", reportLatency: " + j);
            return false;
        }
        String str = MOVEMENT_H2A_TRANSFER_MAP.get(Integer.valueOf(i));
        if (str == null) {
            LogUtils.e(TAG, "subscribe failed! Not supported " + i);
            return false;
        }
        HwMSDPMovementManager hwMSDPMovementManager = this.mHwMovementManager;
        if (hwMSDPMovementManager != null) {
            return hwMSDPMovementManager.enableMovementEvent(1, str, i2, j, PARAM_NO_WAKEUP);
        }
        LogUtils.e(TAG, "subscribe failed! mHwMovementManager is null");
        return false;
    }

    public boolean unsubscribe(int i, int i2) {
        if (!isValidEventType(i2)) {
            LogUtils.e(TAG, "unsubscribe failed! eventType: " + i2);
            return false;
        }
        String str = MOVEMENT_H2A_TRANSFER_MAP.get(Integer.valueOf(i));
        if (str == null) {
            LogUtils.e(TAG, "unsubscribe failed! Not supported " + i);
            return false;
        }
        HwMSDPMovementManager hwMSDPMovementManager = this.mHwMovementManager;
        if (hwMSDPMovementManager != null) {
            return hwMSDPMovementManager.disableMovementEvent(1, str, i2);
        }
        LogUtils.e(TAG, "unsubscribe failed! mHwMovementManager is null");
        return false;
    }

    public List<MovementEvent> getCurrentMovement() {
        ArrayList arrayList = new ArrayList(16);
        HwMSDPMovementManager hwMSDPMovementManager = this.mHwMovementManager;
        if (hwMSDPMovementManager == null) {
            LogUtils.e(TAG, "getCurrentMovement failed! mHwMovementManager is null");
            return arrayList;
        }
        HwMSDPMovementChangeEvent currentMovement = hwMSDPMovementManager.getCurrentMovement(1);
        if (currentMovement == null) {
            LogUtils.e(TAG, "getCurrentMovement failed! changeEvent is null");
            return arrayList;
        }
        Iterable<HwMSDPMovementEvent> movementEvents = currentMovement.getMovementEvents();
        if (movementEvents == null) {
            LogUtils.e(TAG, "getCurrentMovement failed! movementEvents is null");
            return arrayList;
        }
        for (HwMSDPMovementEvent hwMSDPMovementEvent : movementEvents) {
            if (hwMSDPMovementEvent != null && MOVEMENT_STATUS_A2H_TRANSFER_MAP.containsKey(hwMSDPMovementEvent.getMovement())) {
                arrayList.add(new MovementEvent(MOVEMENT_STATUS_A2H_TRANSFER_MAP.get(hwMSDPMovementEvent.getMovement()).intValue(), hwMSDPMovementEvent.getEventType(), hwMSDPMovementEvent.getTimestampNs()));
            }
        }
        return arrayList;
    }
}
