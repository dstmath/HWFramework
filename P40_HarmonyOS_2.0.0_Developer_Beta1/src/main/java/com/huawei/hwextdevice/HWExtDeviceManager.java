package com.huawei.hwextdevice;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.HwLog;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.huawei.hwextdevice.devices.HWExtMotion;
import com.huawei.hwextdevice.devices.IHWExtDevice;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HWExtDeviceManager {
    public static final boolean DEBUG_FLAG = false;
    private static final int DEFAULT_MOTION_ACTION = -1;
    private static final int DEVICE_KEY_RATE = 100;
    private static final int ENABLE_DEVICE_SUCCESS = 1;
    private static final int ENABLE_DEVICE_SUCCESS_REPEAT = 2;
    private static final int MAX_MOTION_ACTION = 100;
    public static final String TAG = "HWExtDeviceManager";
    private static HWExtDeviceManager mInstance = null;
    private static HWExtDeviceEvent mLastOritation = null;
    private static Object mOritationLock = new Object();
    private static Object sDeviceModuleLock = new Object();
    private static final SparseArray<IHWExtDevice> sHandleToDeviceList = new SparseArray<>();
    private static final int userType = SystemProperties.getInt("ro.logsystem.usertype", 1);
    private Context mContext = null;
    private final HashMap<HWExtDeviceEventListener, DeviceEventQueue> mDeviceListenerMap = new HashMap<>();
    private final ArrayList<IHWExtDevice> mFullDevicesList = new ArrayList<>();
    private final ArrayList<Integer> mHWExtDeviceTypeList = new ArrayList<>();
    private boolean sFullDeviceGetOver = false;

    private native void nativeDispose();

    private native int nativeGetDeviceList();

    private native void nativeInit(boolean z);

    private native boolean nativeSupportMotionFeature(int i);

    private HWExtDeviceManager(Context context) {
        this.mContext = context;
        synchronized (sDeviceModuleLock) {
            nativeInit(false);
            getFullHWExtDeviceList();
        }
    }

    public static synchronized HWExtDeviceManager getInstance(Context context) {
        HWExtDeviceManager hWExtDeviceManager;
        synchronized (HWExtDeviceManager.class) {
            if (mInstance == null) {
                mInstance = new HWExtDeviceManager(context);
            }
            hWExtDeviceManager = mInstance;
        }
        return hWExtDeviceManager;
    }

    public void dispose() throws Exception {
        synchronized (this.mDeviceListenerMap) {
            int listenerSize = this.mDeviceListenerMap.size();
            if (listenerSize > 0) {
                throw new Exception("mDeviceListenerMap is not empty, please ungrister all devices first listenerSize: " + listenerSize);
            }
        }
        nativeDispose();
        destroyInstance();
        synchronized (this.mFullDevicesList) {
            this.mFullDevicesList.clear();
        }
        synchronized (this.mHWExtDeviceTypeList) {
            this.mHWExtDeviceTypeList.clear();
        }
        this.sFullDeviceGetOver = false;
    }

    private static synchronized void destroyInstance() {
        synchronized (HWExtDeviceManager.class) {
            mInstance = null;
        }
    }

    public boolean registerDeviceListener(HWExtDeviceEventListener listener, IHWExtDevice device) {
        boolean result;
        if (device != null) {
            synchronized (this.mDeviceListenerMap) {
                DeviceEventQueue queue = this.mDeviceListenerMap.get(listener);
                if (queue == null) {
                    queue = new DeviceEventQueue(listener);
                    this.mDeviceListenerMap.put(listener, queue);
                }
                result = queue.addDevice(device);
                if (result) {
                    dubaiReportMotionState(device, 1);
                }
            }
            return result;
        }
        throw new IllegalArgumentException("device cannot be null");
    }

    public boolean registerDeviceListener(HWExtDeviceEventListener listener, IHWExtDevice device, Handler handler) {
        boolean result;
        if (device != null) {
            synchronized (this.mDeviceListenerMap) {
                DeviceEventQueue queue = this.mDeviceListenerMap.get(listener);
                if (queue == null) {
                    queue = new DeviceEventQueue(listener, handler);
                    this.mDeviceListenerMap.put(listener, queue);
                }
                result = queue.addDevice(device);
                if (result) {
                    dubaiReportMotionState(device, 1);
                }
            }
            return result;
        }
        throw new IllegalArgumentException("device cannot be null");
    }

    public boolean unregisterDeviceListener(HWExtDeviceEventListener listener, IHWExtDevice device) {
        synchronized (this.mDeviceListenerMap) {
            DeviceEventQueue queue = this.mDeviceListenerMap.get(listener);
            if (queue == null) {
                return false;
            }
            if (device == null) {
                queue.removeAllDevices();
            } else {
                queue.removeDevice(device);
            }
            if (!queue.hasDevices()) {
                queue.dispose();
                this.mDeviceListenerMap.remove(listener);
            }
            dubaiReportMotionState(device, 0);
            return true;
        }
    }

    public boolean supportMotionFeature(int motionType) {
        return nativeSupportMotionFeature(motionType / 100);
    }

    private void dubaiReportMotionState(IHWExtDevice device, int state) {
        if (device != null && userType == 3) {
            int type = device.getHWExtDeviceSubType();
            String name = "N/A";
            Context context = this.mContext;
            if (context != null) {
                name = context.getPackageName();
            }
            HwLog.dubaie("DUBAI_TAG_MOTION_RECOGNITION", "name=" + name + " type=" + type + " state=" + state);
        }
    }

    public ArrayList<Integer> getHWExtDeviceTypes() {
        ArrayList<Integer> arrayList;
        synchronized (this.mHWExtDeviceTypeList) {
            arrayList = this.mHWExtDeviceTypeList;
        }
        return arrayList;
    }

    public List<IHWExtDevice> getFullHWExtDeviceList() {
        ArrayList<IHWExtDevice> arrayList;
        if (!this.sFullDeviceGetOver && nativeGetDeviceList() > 0) {
            this.sFullDeviceGetOver = true;
        }
        synchronized (this.mFullDevicesList) {
            arrayList = this.mFullDevicesList;
        }
        return arrayList;
    }

    public List<IHWExtDevice> getHWExtSubDeviceList(int hwextDeviceType) {
        ArrayList<IHWExtDevice> devcieList = new ArrayList<>();
        if (this.sFullDeviceGetOver) {
            synchronized (this.mFullDevicesList) {
                int deviceListSize = this.mFullDevicesList.size();
                for (int i = 0; i < deviceListSize; i++) {
                    IHWExtDevice device = this.mFullDevicesList.get(i);
                    if (hwextDeviceType == device.getHWExtDeviceType()) {
                        devcieList.add(device);
                    }
                }
            }
        } else {
            getFullHWExtDeviceList();
        }
        return devcieList;
    }

    private void setDeviceList(IHWExtDevice[] deviceArr) {
        if (deviceArr == null) {
            Log.d(TAG, "motion setDeviceList deviceArr is null ");
            return;
        }
        for (IHWExtDevice device : deviceArr) {
            if (device != null) {
                synchronized (this.mFullDevicesList) {
                    this.mFullDevicesList.add(device);
                    sHandleToDeviceList.put(getDeviceUniqueId(device), device);
                }
                Integer deviceType = Integer.valueOf(device.getHWExtDeviceType());
                synchronized (this.mHWExtDeviceTypeList) {
                    if (!this.mHWExtDeviceTypeList.contains(deviceType)) {
                        this.mHWExtDeviceTypeList.add(deviceType);
                    }
                }
            }
        }
    }

    private IHWExtDevice getDeviceByType(int hwextDeviceType) {
        if (hwextDeviceType != 1) {
            return null;
        }
        return new HWExtMotion(1);
    }

    /* access modifiers changed from: private */
    public static int getDeviceUniqueId(IHWExtDevice device) {
        return (device.getHWExtDeviceSubType() * 100) + device.getHWExtDeviceType();
    }

    /* access modifiers changed from: private */
    public static int getDeviceUniqueId(int deviceType, int deviceSubType) {
        return (deviceSubType * 100) + deviceType;
    }

    /* access modifiers changed from: private */
    public static int getDeviceActionId(IHWExtDevice device) {
        int action = (-65536 & device.getHWExtDeviceAction()) >> 16;
        if (action < -1 || action >= 100) {
            action = -1;
        }
        return (device.getHWExtDeviceSubType() * 100) + device.getHWExtDeviceType() + action;
    }

    static final class DeviceEventQueue {
        private int DEVICE_MSG_TYPE_RESULT;
        private SparseBooleanArray mActiveDeviceActions;
        private SparseBooleanArray mActiveDevices;
        private SparseArray<HWExtDeviceEvent> mDeviceEvents;
        private HWExtDeviceHandler mDeviceHandler;
        private float[] mDeviceValues;
        private HWExtDeviceEventListener mListener;
        private long nDevcieEventQueue;

        public native long nativeCreateEventQueue(DeviceEventQueue deviceEventQueue, float[] fArr);

        public native void nativeDestroyEventQueue(long j);

        public native int nativeDisableDevice(long j, IHWExtDevice iHWExtDevice);

        public native int nativeEnableDevice(long j, IHWExtDevice iHWExtDevice);

        public DeviceEventQueue(HWExtDeviceEventListener listener) {
            this.DEVICE_MSG_TYPE_RESULT = 1001;
            this.mListener = null;
            this.mActiveDevices = new SparseBooleanArray();
            this.mActiveDeviceActions = new SparseBooleanArray();
            this.mDeviceEvents = new SparseArray<>();
            this.mDeviceValues = new float[500];
            this.mDeviceHandler = null;
            this.mListener = listener;
            Looper looper = Looper.myLooper();
            if (looper != null) {
                this.mDeviceHandler = new HWExtDeviceHandler(looper);
            } else {
                Looper looper2 = Looper.getMainLooper();
                if (looper2 != null) {
                    this.mDeviceHandler = new HWExtDeviceHandler(looper2);
                } else {
                    this.mDeviceHandler = null;
                }
            }
            Log.d(HWExtDeviceManager.TAG, "motion DeviceEventQueue mDeviceHandler: " + this.mDeviceHandler);
            this.nDevcieEventQueue = nativeCreateEventQueue(this, this.mDeviceValues);
        }

        public DeviceEventQueue(HWExtDeviceEventListener listener, Handler handler) {
            Looper looper;
            this.DEVICE_MSG_TYPE_RESULT = 1001;
            this.mListener = null;
            this.mActiveDevices = new SparseBooleanArray();
            this.mActiveDeviceActions = new SparseBooleanArray();
            this.mDeviceEvents = new SparseArray<>();
            this.mDeviceValues = new float[500];
            this.mDeviceHandler = null;
            this.mListener = listener;
            if (handler == null || (looper = handler.getLooper()) == null) {
                Looper looper2 = Looper.myLooper();
                if (looper2 != null) {
                    this.mDeviceHandler = new HWExtDeviceHandler(looper2);
                } else {
                    Looper looper3 = Looper.getMainLooper();
                    if (looper3 != null) {
                        this.mDeviceHandler = new HWExtDeviceHandler(looper3);
                    } else {
                        this.mDeviceHandler = null;
                    }
                }
            } else {
                this.mDeviceHandler = new HWExtDeviceHandler(looper);
            }
            Log.d(HWExtDeviceManager.TAG, "motion DeviceEventQueue mDeviceHandler: " + this.mDeviceHandler);
            this.nDevcieEventQueue = nativeCreateEventQueue(this, this.mDeviceValues);
        }

        public boolean addDevice(IHWExtDevice device) {
            int deviceUniqueId = HWExtDeviceManager.getDeviceUniqueId(device);
            if (!this.mActiveDevices.get(deviceUniqueId) || !isActiveAction(device)) {
                this.mActiveDevices.put(deviceUniqueId, true);
                addActiveAction(device);
                addDeviceEvent(device);
                return enableDevice(device);
            }
            Log.w(HWExtDeviceManager.TAG, "addDevice device has been activated with this listener");
            return false;
        }

        private void addDeviceEvent(IHWExtDevice device) {
            if (this.mDeviceEvents != null) {
                this.mDeviceEvents.put(HWExtDeviceManager.getDeviceUniqueId(device), new HWExtDeviceEvent(device.getMaxLenValueArray()));
            }
        }

        private void removeDeviceEvent(IHWExtDevice device) {
            SparseArray<HWExtDeviceEvent> sparseArray = this.mDeviceEvents;
            if (sparseArray != null) {
                sparseArray.remove(HWExtDeviceManager.getDeviceUniqueId(device));
            }
        }

        public boolean removeDevice(IHWExtDevice device) {
            int deviceUniqueId = HWExtDeviceManager.getDeviceUniqueId(device);
            if (!this.mActiveDevices.get(deviceUniqueId)) {
                return false;
            }
            boolean res = disableDevice(device);
            this.mActiveDevices.put(deviceUniqueId, false);
            removeActiveAction(device);
            removeDeviceEvent(device);
            return res;
        }

        private boolean isActiveAction(IHWExtDevice device) {
            if (device.getHWExtDeviceAction() == -1) {
                return true;
            }
            return this.mActiveDeviceActions.get(HWExtDeviceManager.getDeviceActionId(device));
        }

        private void addActiveAction(IHWExtDevice device) {
            if (device.getHWExtDeviceAction() != -1 && device.getHWExtDeviceSubType() != 3100) {
                this.mActiveDeviceActions.put(HWExtDeviceManager.getDeviceActionId(device), true);
            }
        }

        private void removeActiveAction(IHWExtDevice device) {
            if (!(device.getHWExtDeviceAction() == -1 || device.getHWExtDeviceSubType() == 3100)) {
                int size = this.mActiveDeviceActions.size();
                int deviceUniqueId = HWExtDeviceManager.getDeviceUniqueId(device);
                for (int i = 0; i < size; i++) {
                    int deviceActionId = this.mActiveDeviceActions.keyAt(i);
                    int deviceAction = deviceActionId - deviceUniqueId;
                    if (this.mActiveDeviceActions.valueAt(i) && deviceAction / 100 == 0) {
                        this.mActiveDeviceActions.put(deviceActionId, false);
                    }
                }
            }
        }

        private void removeAllActiveAction() {
            int actionSize = this.mActiveDeviceActions.size();
            for (int i = 0; i < actionSize; i++) {
                if (this.mActiveDevices.valueAt(i)) {
                    this.mActiveDeviceActions.put(this.mActiveDeviceActions.keyAt(i), false);
                }
            }
        }

        public boolean hasDevices() {
            if (this.mActiveDevices.indexOfValue(true) >= 0) {
                return true;
            }
            return false;
        }

        public boolean removeAllDevices() {
            int deviceSize = this.mActiveDevices.size();
            for (int i = 0; i < deviceSize; i++) {
                if (this.mActiveDevices.valueAt(i)) {
                    int deviceUniqueId = this.mActiveDevices.keyAt(i);
                    IHWExtDevice device = (IHWExtDevice) HWExtDeviceManager.sHandleToDeviceList.get(deviceUniqueId);
                    disableDevice(device);
                    this.mActiveDevices.put(deviceUniqueId, false);
                    removeDeviceEvent(device);
                }
            }
            removeAllActiveAction();
            return true;
        }

        public void dispose() {
            nativeDestroyEventQueue(this.nDevcieEventQueue);
            this.mActiveDevices.clear();
            this.mActiveDeviceActions.clear();
        }

        /* access modifiers changed from: protected */
        public void dispatchDeviceEvent(HWExtDeviceEvent deviceEvent) {
            if (deviceEvent == null) {
                Log.e(HWExtDeviceManager.TAG, "Error: deviceEvent is nullt");
            } else if (deviceEvent.getDeviceType() == 1) {
                int deviceUniqueId = HWExtDeviceManager.getDeviceUniqueId(deviceEvent.getDeviceType(), deviceEvent.getSubDeviceType());
                IHWExtDevice device = (IHWExtDevice) HWExtDeviceManager.sHandleToDeviceList.get(deviceUniqueId);
                HWExtDeviceEvent tmpDeviceEvent = this.mDeviceEvents.get(deviceUniqueId);
                if (tmpDeviceEvent == null) {
                    Log.e(HWExtDeviceManager.TAG, "Error: tmpDeviceEvent is null");
                    return;
                }
                tmpDeviceEvent.setDevice(device);
                tmpDeviceEvent.setDeviceType(deviceEvent.getDeviceType());
                tmpDeviceEvent.setSubDeviceType(deviceEvent.getSubDeviceType());
                tmpDeviceEvent.setDeviceValues(deviceEvent.getDeviceValues(), deviceEvent.getDeviceValuesLen());
                HWExtDeviceHandler hWExtDeviceHandler = this.mDeviceHandler;
                if (hWExtDeviceHandler != null) {
                    this.mDeviceHandler.sendMessage(hWExtDeviceHandler.obtainMessage(this.DEVICE_MSG_TYPE_RESULT, tmpDeviceEvent.clone()));
                    if (tmpDeviceEvent.getSubDeviceType() == 700) {
                        synchronized (HWExtDeviceManager.mOritationLock) {
                            HWExtDeviceEvent unused = HWExtDeviceManager.mLastOritation = tmpDeviceEvent;
                        }
                        return;
                    }
                    return;
                }
                Log.e(HWExtDeviceManager.TAG, "motion dispatchDeviceEvent mDeviceHandler is null ...... ");
            } else {
                Log.w(HWExtDeviceManager.TAG, "deviceType : " + deviceEvent.getDeviceType());
            }
        }

        /* access modifiers changed from: protected */
        public void dispatchLastOritationEvent() {
            if (this.mDeviceHandler != null) {
                Log.d(HWExtDeviceManager.TAG, "dispatchLastOritationEvent ...... ");
                synchronized (HWExtDeviceManager.mOritationLock) {
                    if (HWExtDeviceManager.mLastOritation != null) {
                        this.mDeviceHandler.sendMessage(this.mDeviceHandler.obtainMessage(this.DEVICE_MSG_TYPE_RESULT, HWExtDeviceManager.mLastOritation.clone()));
                    } else {
                        Log.e(HWExtDeviceManager.TAG, "mdispatchLastOritationEvent mLastOritation is null ...... ");
                    }
                }
                return;
            }
            Log.e(HWExtDeviceManager.TAG, "dispatchLastOritationEvent mDeviceHandler is null ...... ");
        }

        private boolean enableDevice(IHWExtDevice device) {
            int enableDeviceResult = nativeEnableDevice(this.nDevcieEventQueue, device);
            if (enableDeviceResult == 1) {
                return true;
            }
            if (enableDeviceResult != 2 || device.getHWExtDeviceSubType() != 700) {
                return false;
            }
            dispatchLastOritationEvent();
            return true;
        }

        private boolean disableDevice(IHWExtDevice device) {
            if (nativeDisableDevice(this.nDevcieEventQueue, device) == 1) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: private */
        public class HWExtDeviceHandler extends Handler {
            public HWExtDeviceHandler(Looper looper) {
                super(looper);
            }

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                HWExtDeviceEvent event = (HWExtDeviceEvent) msg.obj;
                Log.i(HWExtDeviceManager.TAG, "trigger onDeviceDataChanged : mListener : " + DeviceEventQueue.this.mListener + ",type : " + event.getSubDeviceType());
                if (DeviceEventQueue.this.mListener != null) {
                    DeviceEventQueue.this.mListener.onDeviceDataChanged(event);
                }
            }
        }
    }
}
