package ohos.msdp.motion;

import android.content.Context;
import com.huawei.hwextdevice.HWExtDeviceEvent;
import com.huawei.hwextdevice.HWExtDeviceEventListener;
import com.huawei.hwextdevice.HWExtDeviceManager;
import com.huawei.hwextdevice.devices.HWExtMotion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

public class DeviceMotionManager {
    private static final int DATA_LENGTH = 1;
    private static final int DOMAIN = 218112512;
    private static final HiLogLabel LABEL = new HiLogLabel(3, DOMAIN, TAG);
    private static final Map<Integer, Integer> MOTION_TYPE_A2H_MAP = new HashMap();
    private static final Map<Integer, Integer> MOTION_TYPE_H2A_MAP = new HashMap();
    private static final int RESULT_LENGTH = 0;
    private static final int RESULT_SUCCESS = 1;
    private static final String TAG = "DeviceMotionManager";
    private boolean destroyed = false;
    private HWExtDeviceManager extDeviceManager = null;
    private ArrayList<DeviceMotionListener> mEventListenerList = new ArrayList<>();
    private HWExtDeviceEventListener mHWEDListener = new HWExtDeviceEventListener() {
        /* class ohos.msdp.motion.DeviceMotionManager.AnonymousClass1 */

        public void onDeviceDataChanged(HWExtDeviceEvent hWExtDeviceEvent) {
            float[] deviceValues = hWExtDeviceEvent.getDeviceValues();
            if (deviceValues == null) {
                HiLog.error(DeviceMotionManager.LABEL, "onDeviceDataChanged deviceValues is null ", new Object[0]);
                return;
            }
            int length = deviceValues.length;
            if ((length >= 1 ? (int) deviceValues[0] : 0) != 1) {
                HiLog.error(DeviceMotionManager.LABEL, "Motion detection failed", new Object[0]);
                return;
            }
            int i = length >= 2 ? (int) deviceValues[1] : 0;
            int subDeviceType = hWExtDeviceEvent.getSubDeviceType();
            if (!DeviceMotionManager.MOTION_TYPE_A2H_MAP.containsKey(Integer.valueOf(subDeviceType))) {
                HiLog.error(DeviceMotionManager.LABEL, "subscribe invalid motion type", new Object[0]);
                return;
            }
            Integer num = (Integer) DeviceMotionManager.MOTION_TYPE_A2H_MAP.get(Integer.valueOf(subDeviceType));
            if (num == null) {
                HiLog.error(DeviceMotionManager.LABEL, "subscribe invalid inner motion type", new Object[0]);
                return;
            }
            Iterator it = DeviceMotionManager.this.mEventListenerList.iterator();
            while (it.hasNext()) {
                ((DeviceMotionListener) it.next()).onDeviceMotionChanged(new DeviceMotionEvent(num.intValue(), i));
            }
        }
    };
    private ConcurrentHashMap<Integer, HWExtMotion> mMotionAppsRegList = new ConcurrentHashMap<>();

    static {
        MOTION_TYPE_H2A_MAP.put(1, 100);
        MOTION_TYPE_H2A_MAP.put(2, 200);
        MOTION_TYPE_H2A_MAP.put(3, 300);
        MOTION_TYPE_H2A_MAP.put(4, 400);
        MOTION_TYPE_H2A_MAP.put(7, 700);
        MOTION_TYPE_H2A_MAP.put(10, 1000);
        MOTION_TYPE_H2A_MAP.put(14, Integer.valueOf((int) SystemAbilityDefinition.SUBSYS_DISTRIBUTEDSCHEDULE_SYS_ABILITY_ID_BEGIN));
        MOTION_TYPE_A2H_MAP.forEach($$Lambda$DeviceMotionManager$Hz2ze35Pc_ChYn6Yg4B6BiclYM.INSTANCE);
    }

    private DeviceMotionManager(Context context) {
        if (context == null) {
            HiLog.error(LABEL, "Context is null", new Object[0]);
        } else {
            this.extDeviceManager = HWExtDeviceManager.getInstance(context);
        }
    }

    public static synchronized DeviceMotionManager getInstance(ohos.app.Context context) {
        synchronized (DeviceMotionManager.class) {
            HiLog.debug(LABEL, "In get instance DeviceMotionManager", new Object[0]);
            if (context == null) {
                HiLog.error(LABEL, "Context is null", new Object[0]);
                return null;
            } else if (context.verifySelfPermission("ohos.permission.DEVICE_ACTIVITY_MOTION") != 0) {
                HiLog.error(LABEL, "Permission is wrong", new Object[0]);
                return null;
            } else {
                ohos.app.Context applicationContext = context.getApplicationContext();
                if (applicationContext == null) {
                    HiLog.error(LABEL, "appContext is null", new Object[0]);
                    return null;
                } else if (applicationContext.getHostContext() == null) {
                    HiLog.error(LABEL, "HostContext is null", new Object[0]);
                    return null;
                } else {
                    HiLog.info(LABEL, "Get Instance ok", new Object[0]);
                    return new DeviceMotionManager((Context) applicationContext.getHostContext());
                }
            }
        }
    }

    public void releaseInstance() {
        if (this.destroyed) {
            HiLog.error(LABEL, "releaseInstance() called already", new Object[0]);
            return;
        }
        unsubscribeAll();
        this.destroyed = true;
        ConcurrentHashMap<Integer, HWExtMotion> concurrentHashMap = this.mMotionAppsRegList;
        if (concurrentHashMap != null) {
            concurrentHashMap.clear();
            this.mMotionAppsRegList = null;
        }
        ArrayList<DeviceMotionListener> arrayList = this.mEventListenerList;
        if (arrayList != null) {
            arrayList.clear();
            this.mEventListenerList = null;
        }
        this.extDeviceManager = null;
    }

    public boolean subscribe(int i) {
        if (this.destroyed) {
            HiLog.error(LABEL, "subscribe releaseInstance called already", new Object[0]);
            return false;
        } else if (!MOTION_TYPE_H2A_MAP.containsKey(Integer.valueOf(i))) {
            HiLog.error(LABEL, "subscribe invalid motion type", new Object[0]);
            return false;
        } else {
            Integer num = MOTION_TYPE_H2A_MAP.get(Integer.valueOf(i));
            if (num == null) {
                HiLog.error(LABEL, "subscribe invalid inner motion type", new Object[0]);
                return false;
            } else if (this.mMotionAppsRegList.containsKey(Integer.valueOf(i))) {
                HiLog.error(LABEL, "subscribe repeat motionType:%{public}d ", Integer.valueOf(i));
                return false;
            } else {
                HWExtMotion hWExtMotion = new HWExtMotion(num.intValue());
                boolean registerDeviceListener = this.extDeviceManager.registerDeviceListener(this.mHWEDListener, hWExtMotion);
                HiLog.info(LABEL, "subscribe motion type %{public}d result:%{public}s", num, Boolean.valueOf(registerDeviceListener));
                if (registerDeviceListener) {
                    this.mMotionAppsRegList.put(Integer.valueOf(i), hWExtMotion);
                }
                return registerDeviceListener;
            }
        }
    }

    public boolean unsubscribe(int i) {
        if (this.destroyed) {
            HiLog.error(LABEL, "unsubscribe releaseInstance called already", new Object[0]);
            return false;
        } else if (!MOTION_TYPE_H2A_MAP.containsKey(Integer.valueOf(i))) {
            HiLog.error(LABEL, "unsubscribe invalid motion type", new Object[0]);
            return false;
        } else {
            Integer num = MOTION_TYPE_H2A_MAP.get(Integer.valueOf(i));
            if (num == null) {
                HiLog.error(LABEL, "unsubscribe invalid inner motion type", new Object[0]);
                return false;
            } else if (!this.mMotionAppsRegList.containsKey(Integer.valueOf(i))) {
                HiLog.error(LABEL, "unsubscribe not recognition motionType:%{public}d ", Integer.valueOf(i));
                return false;
            } else {
                boolean unregisterDeviceListener = this.extDeviceManager.unregisterDeviceListener(this.mHWEDListener, this.mMotionAppsRegList.get(Integer.valueOf(i)));
                HiLog.info(LABEL, "unsubscribe motion type %{public}d result:%{public}s", num, Boolean.valueOf(unregisterDeviceListener));
                if (unregisterDeviceListener) {
                    this.mMotionAppsRegList.remove(Integer.valueOf(i));
                }
                return unregisterDeviceListener;
            }
        }
    }

    public boolean addEventListener(DeviceMotionListener deviceMotionListener) {
        if (this.destroyed) {
            HiLog.error(LABEL, "addEventListener releaseInstance called already", new Object[0]);
            return false;
        } else if (deviceMotionListener == null) {
            HiLog.error(LABEL, "add listener is null", new Object[0]);
            return false;
        } else {
            ArrayList<DeviceMotionListener> arrayList = this.mEventListenerList;
            if (arrayList == null || arrayList.contains(deviceMotionListener)) {
                return false;
            }
            this.mEventListenerList.add(deviceMotionListener);
            return true;
        }
    }

    public boolean removeEventListener(DeviceMotionListener deviceMotionListener) {
        if (this.destroyed) {
            HiLog.error(LABEL, "removeEventListener releaseInstance called already", new Object[0]);
            return false;
        } else if (deviceMotionListener == null) {
            HiLog.error(LABEL, "remove listener is null", new Object[0]);
            return false;
        } else {
            ArrayList<DeviceMotionListener> arrayList = this.mEventListenerList;
            if (arrayList == null) {
                return false;
            }
            arrayList.remove(deviceMotionListener);
            return true;
        }
    }

    private void unsubscribeAll() {
        ConcurrentHashMap<Integer, HWExtMotion> concurrentHashMap = this.mMotionAppsRegList;
        if (concurrentHashMap != null && concurrentHashMap.size() > 0) {
            for (Map.Entry<Integer, HWExtMotion> entry : this.mMotionAppsRegList.entrySet()) {
                unsubscribe(entry.getKey().intValue());
            }
        }
    }
}
