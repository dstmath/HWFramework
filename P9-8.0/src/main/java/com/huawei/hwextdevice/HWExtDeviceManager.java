package com.huawei.hwextdevice;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
    private static final int DEVICE_KEY_RATE = 100;
    private static final int ENABLE_DEVICE_SUCCESS = 1;
    private static final int ENABLE_DEVICE_SUCCESS_REPEAT = 2;
    public static final String TAG = "HWExtDeviceManager";
    private static HWExtDeviceManager mInstance = null;
    private static HWExtDeviceEvent mLastOritation = null;
    private static Object mOritationLock = new Object();
    private static Object sDeviceModuleLock = new Object();
    private static final SparseArray<IHWExtDevice> sHandleToDeviceList = new SparseArray();
    private Context mContext = null;
    private HashMap<HWExtDeviceEventListener, DeviceEventQueue> mDeviceListenerMap = new HashMap();
    private ArrayList<IHWExtDevice> mFullDevicesList = new ArrayList();
    private ArrayList<Integer> mHWExtDeviceTypeList = new ArrayList();
    private boolean sFullDeviceGetOver = false;

    static final class DeviceEventQueue {
        private int DEVICE_MSG_TYPE_RESULT;
        private SparseBooleanArray mActiveDevices;
        private SparseArray<HWExtDeviceEvent> mDeviceEvents;
        private HWExtDeviceHandler mDeviceHandler;
        private float[] mDeviceValues;
        private HWExtDeviceEventListener mListener;
        private long nDevcieEventQueue;

        private class HWExtDeviceHandler extends Handler {
            public HWExtDeviceHandler(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message msg) {
                if (DeviceEventQueue.this.mListener != null) {
                    DeviceEventQueue.this.mListener.onDeviceDataChanged((HWExtDeviceEvent) msg.obj);
                }
            }
        }

        public native long nativeCreateEventQueue(DeviceEventQueue deviceEventQueue, float[] fArr);

        public native void nativeDestroyEventQueue(long j);

        public native int nativeDisableDevice(long j, IHWExtDevice iHWExtDevice);

        public native int nativeEnableDevice(long j, IHWExtDevice iHWExtDevice);

        public DeviceEventQueue(HWExtDeviceEventListener listener) {
            this.DEVICE_MSG_TYPE_RESULT = 1001;
            this.mListener = null;
            this.mActiveDevices = new SparseBooleanArray();
            this.mDeviceEvents = new SparseArray();
            this.mDeviceValues = new float[500];
            this.mDeviceHandler = null;
            this.mListener = listener;
            Looper looper = Looper.myLooper();
            if (looper != null) {
                this.mDeviceHandler = new HWExtDeviceHandler(looper);
            } else {
                looper = Looper.getMainLooper();
                if (looper != null) {
                    this.mDeviceHandler = new HWExtDeviceHandler(looper);
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
            this.mDeviceEvents = new SparseArray();
            this.mDeviceValues = new float[500];
            this.mDeviceHandler = null;
            this.mListener = listener;
            if (handler != null) {
                looper = handler.getLooper();
                if (looper != null) {
                    this.mDeviceHandler = new HWExtDeviceHandler(looper);
                    Log.d(HWExtDeviceManager.TAG, "motion DeviceEventQueue mDeviceHandler: " + this.mDeviceHandler);
                    this.nDevcieEventQueue = nativeCreateEventQueue(this, this.mDeviceValues);
                }
            }
            looper = Looper.myLooper();
            if (looper != null) {
                this.mDeviceHandler = new HWExtDeviceHandler(looper);
            } else {
                looper = Looper.getMainLooper();
                if (looper != null) {
                    this.mDeviceHandler = new HWExtDeviceHandler(looper);
                } else {
                    this.mDeviceHandler = null;
                }
            }
            Log.d(HWExtDeviceManager.TAG, "motion DeviceEventQueue mDeviceHandler: " + this.mDeviceHandler);
            this.nDevcieEventQueue = nativeCreateEventQueue(this, this.mDeviceValues);
        }

        public boolean addDevice(IHWExtDevice device) {
            int deviceUniqueId = HWExtDeviceManager.getDeviceUniqueId(device);
            if (this.mActiveDevices.get(deviceUniqueId)) {
                Log.w(HWExtDeviceManager.TAG, "addDevice device has been activated with this listener");
                return false;
            }
            this.mActiveDevices.put(deviceUniqueId, true);
            addDeviceEvent(device);
            return enableDevice(device);
        }

        private void addDeviceEvent(IHWExtDevice device) {
            if (this.mDeviceEvents != null) {
                this.mDeviceEvents.put(HWExtDeviceManager.getDeviceUniqueId(device), new HWExtDeviceEvent(device.getMaxLenValueArray()));
            }
        }

        private void removeDeviceEvent(IHWExtDevice device) {
            if (this.mDeviceEvents != null) {
                this.mDeviceEvents.remove(HWExtDeviceManager.getDeviceUniqueId(device));
            }
        }

        public boolean removeDevice(IHWExtDevice device) {
            int deviceUniqueId = HWExtDeviceManager.getDeviceUniqueId(device);
            if (!this.mActiveDevices.get(deviceUniqueId)) {
                return false;
            }
            boolean res = disableDevice(device);
            this.mActiveDevices.put(deviceUniqueId, false);
            removeDeviceEvent(device);
            return res;
        }

        public boolean hasDevices() {
            return this.mActiveDevices.indexOfValue(true) >= 0;
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
            return true;
        }

        public void dispose() {
            nativeDestroyEventQueue(this.nDevcieEventQueue);
            this.mActiveDevices.clear();
        }

        protected void dispatchDeviceEvent(HWExtDeviceEvent deviceEvent) {
            if (deviceEvent == null) {
                Log.e(HWExtDeviceManager.TAG, "Error: deviceEvent is nullt");
                return;
            }
            if (deviceEvent.getDeviceType() == 1) {
                int deviceUniqueId = HWExtDeviceManager.getDeviceUniqueId(deviceEvent.getDeviceType(), deviceEvent.getSubDeviceType());
                HWExtDeviceEvent tmpDeviceEvent = (HWExtDeviceEvent) this.mDeviceEvents.get(deviceUniqueId);
                tmpDeviceEvent.setDevice((IHWExtDevice) HWExtDeviceManager.sHandleToDeviceList.get(deviceUniqueId));
                tmpDeviceEvent.setDeviceType(deviceEvent.getDeviceType());
                tmpDeviceEvent.setSubDeviceType(deviceEvent.getSubDeviceType());
                tmpDeviceEvent.setDeviceValues(deviceEvent.getDeviceValues(), deviceEvent.getDeviceValuesLen());
                if (this.mDeviceHandler != null) {
                    this.mDeviceHandler.sendMessage(this.mDeviceHandler.obtainMessage(this.DEVICE_MSG_TYPE_RESULT, tmpDeviceEvent.clone()));
                    if (tmpDeviceEvent.getSubDeviceType() == 700) {
                        synchronized (HWExtDeviceManager.mOritationLock) {
                            HWExtDeviceManager.mLastOritation = tmpDeviceEvent;
                        }
                    }
                } else {
                    Log.e(HWExtDeviceManager.TAG, "motion dispatchDeviceEvent mDeviceHandler is null ...... ");
                }
            }
        }

        protected void dispatchLastOritationEvent() {
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
    }

    private native void nativeDispose();

    private native int nativeGetDeviceList();

    private native void nativeInit(boolean z);

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
        if (device == null) {
            throw new IllegalArgumentException("device cannot be null");
        }
        synchronized (this.mDeviceListenerMap) {
            DeviceEventQueue queue = (DeviceEventQueue) this.mDeviceListenerMap.get(listener);
            if (queue == null) {
                queue = new DeviceEventQueue(listener);
                this.mDeviceListenerMap.put(listener, queue);
                if (queue.addDevice(device)) {
                    return true;
                }
                return false;
            }
            boolean addDevice = queue.addDevice(device);
            return addDevice;
        }
    }

    public boolean registerDeviceListener(HWExtDeviceEventListener listener, IHWExtDevice device, Handler handler) {
        if (device == null) {
            throw new IllegalArgumentException("device cannot be null");
        }
        synchronized (this.mDeviceListenerMap) {
            DeviceEventQueue queue = (DeviceEventQueue) this.mDeviceListenerMap.get(listener);
            if (queue == null) {
                queue = new DeviceEventQueue(listener, handler);
                this.mDeviceListenerMap.put(listener, queue);
                if (queue.addDevice(device)) {
                    return true;
                }
                return false;
            }
            boolean addDevice = queue.addDevice(device);
            return addDevice;
        }
    }

    public boolean unregisterDeviceListener(HWExtDeviceEventListener listener, IHWExtDevice device) {
        synchronized (this.mDeviceListenerMap) {
            DeviceEventQueue queue = (DeviceEventQueue) this.mDeviceListenerMap.get(listener);
            if (queue != null) {
                if (device == null) {
                    queue.removeAllDevices();
                } else {
                    queue.removeDevice(device);
                }
                if (!queue.hasDevices()) {
                    queue.dispose();
                    this.mDeviceListenerMap.remove(listener);
                }
                return true;
            }
            return false;
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
        List list;
        if (!this.sFullDeviceGetOver && nativeGetDeviceList() > 0) {
            this.sFullDeviceGetOver = true;
        }
        synchronized (this.mFullDevicesList) {
            list = this.mFullDevicesList;
        }
        return list;
    }

    public List<IHWExtDevice> getHWExtSubDeviceList(int hwextDeviceType) {
        ArrayList<IHWExtDevice> devcieList = new ArrayList();
        if (this.sFullDeviceGetOver) {
            synchronized (this.mFullDevicesList) {
                int deviceListSize = this.mFullDevicesList.size();
                for (int i = 0; i < deviceListSize; i++) {
                    IHWExtDevice device = (IHWExtDevice) this.mFullDevicesList.get(i);
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
        switch (hwextDeviceType) {
            case 1:
                return new HWExtMotion(1);
            default:
                return null;
        }
    }

    private static int getDeviceUniqueId(IHWExtDevice device) {
        return (device.getHWExtDeviceSubType() * 100) + device.getHWExtDeviceType();
    }

    private static int getDeviceUniqueId(int deviceType, int deviceSubType) {
        return (deviceSubType * 100) + deviceType;
    }
}
