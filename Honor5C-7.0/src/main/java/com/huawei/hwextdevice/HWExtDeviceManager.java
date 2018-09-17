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
import com.huawei.motiondetection.MotionTypeApps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HWExtDeviceManager {
    public static final boolean DEBUG_FLAG = false;
    private static final int DEVICE_KEY_RATE = 100;
    private static final int ENABLE_DEVICE_SUCCESS = 1;
    private static final int ENABLE_DEVICE_SUCCESS_REPEAT = 2;
    public static final String TAG = "HWExtDeviceManager";
    private static HWExtDeviceManager mInstance;
    private static HWExtDeviceEvent mLastOritation;
    private static Object mOritationLock;
    private static Object sDeviceModuleLock;
    private static final SparseArray<IHWExtDevice> sHandleToDeviceList = null;
    private Context mContext;
    private HashMap<HWExtDeviceEventListener, DeviceEventQueue> mDeviceListenerMap;
    private ArrayList<IHWExtDevice> mFullDevicesList;
    private ArrayList<Integer> mHWExtDeviceTypeList;
    private boolean sFullDeviceGetOver;

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
            this.DEVICE_MSG_TYPE_RESULT = MotionTypeApps.TYPE_TAKE_OFF_EAR;
            this.mListener = null;
            this.mActiveDevices = new SparseBooleanArray();
            this.mDeviceEvents = new SparseArray();
            this.mDeviceValues = new float[MotionTypeApps.TYPE_TAP];
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
            this.DEVICE_MSG_TYPE_RESULT = MotionTypeApps.TYPE_TAKE_OFF_EAR;
            this.mListener = null;
            this.mActiveDevices = new SparseBooleanArray();
            this.mDeviceEvents = new SparseArray();
            this.mDeviceValues = new float[MotionTypeApps.TYPE_TAP];
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
                return HWExtDeviceManager.DEBUG_FLAG;
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
                return HWExtDeviceManager.DEBUG_FLAG;
            }
            boolean res = disableDevice(device);
            this.mActiveDevices.put(deviceUniqueId, HWExtDeviceManager.DEBUG_FLAG);
            removeDeviceEvent(device);
            return res;
        }

        public boolean hasDevices() {
            return this.mActiveDevices.indexOfValue(true) >= 0 ? true : HWExtDeviceManager.DEBUG_FLAG;
        }

        public boolean removeAllDevices() {
            for (int i = 0; i < this.mActiveDevices.size(); i += HWExtDeviceManager.ENABLE_DEVICE_SUCCESS) {
                if (this.mActiveDevices.valueAt(i)) {
                    int deviceUniqueId = this.mActiveDevices.keyAt(i);
                    IHWExtDevice device = (IHWExtDevice) HWExtDeviceManager.sHandleToDeviceList.get(deviceUniqueId);
                    disableDevice(device);
                    this.mActiveDevices.put(deviceUniqueId, HWExtDeviceManager.DEBUG_FLAG);
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
            if (deviceEvent.getDeviceType() == HWExtDeviceManager.ENABLE_DEVICE_SUCCESS) {
                int deviceUniqueId = HWExtDeviceManager.getDeviceUniqueId(deviceEvent.getDeviceType(), deviceEvent.getSubDeviceType());
                HWExtDeviceEvent tmpDeviceEvent = (HWExtDeviceEvent) this.mDeviceEvents.get(deviceUniqueId);
                tmpDeviceEvent.setDevice((IHWExtDevice) HWExtDeviceManager.sHandleToDeviceList.get(deviceUniqueId));
                tmpDeviceEvent.setDeviceType(deviceEvent.getDeviceType());
                tmpDeviceEvent.setSubDeviceType(deviceEvent.getSubDeviceType());
                tmpDeviceEvent.setDeviceValues(deviceEvent.getDeviceValues(), deviceEvent.getDeviceValuesLen());
                if (this.mDeviceHandler != null) {
                    this.mDeviceHandler.sendMessage(this.mDeviceHandler.obtainMessage(this.DEVICE_MSG_TYPE_RESULT, tmpDeviceEvent.clone()));
                    if (tmpDeviceEvent.getSubDeviceType() == MotionTypeApps.TYPE_ROTATION) {
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
            if (enableDeviceResult == HWExtDeviceManager.ENABLE_DEVICE_SUCCESS) {
                return true;
            }
            if (enableDeviceResult != HWExtDeviceManager.ENABLE_DEVICE_SUCCESS_REPEAT || device.getHWExtDeviceSubType() != MotionTypeApps.TYPE_ROTATION) {
                return HWExtDeviceManager.DEBUG_FLAG;
            }
            dispatchLastOritationEvent();
            return true;
        }

        private boolean disableDevice(IHWExtDevice device) {
            if (nativeDisableDevice(this.nDevcieEventQueue, device) == HWExtDeviceManager.ENABLE_DEVICE_SUCCESS) {
                return true;
            }
            return HWExtDeviceManager.DEBUG_FLAG;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.hwextdevice.HWExtDeviceManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.hwextdevice.HWExtDeviceManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hwextdevice.HWExtDeviceManager.<clinit>():void");
    }

    private native void nativeDispose();

    private native int nativeGetDeviceList();

    private native void nativeInit(boolean z);

    private HWExtDeviceManager(Context context) {
        this.mDeviceListenerMap = new HashMap();
        this.mFullDevicesList = new ArrayList();
        this.mHWExtDeviceTypeList = new ArrayList();
        this.sFullDeviceGetOver = DEBUG_FLAG;
        this.mContext = null;
        this.mContext = context;
        synchronized (sDeviceModuleLock) {
            nativeInit(DEBUG_FLAG);
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
        this.sFullDeviceGetOver = DEBUG_FLAG;
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
                return DEBUG_FLAG;
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
                return DEBUG_FLAG;
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
            return DEBUG_FLAG;
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<IHWExtDevice> getHWExtSubDeviceList(int hwextDeviceType) {
        ArrayList<IHWExtDevice> devcieList = new ArrayList();
        if (this.sFullDeviceGetOver) {
            synchronized (this.mFullDevicesList) {
                int i = 0;
                while (true) {
                    if (i >= this.mFullDevicesList.size()) {
                        break;
                    }
                    IHWExtDevice device = (IHWExtDevice) this.mFullDevicesList.get(i);
                    if (hwextDeviceType == device.getHWExtDeviceType()) {
                        devcieList.add(device);
                    }
                    i += ENABLE_DEVICE_SUCCESS;
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
        for (int i = 0; i < deviceArr.length; i += ENABLE_DEVICE_SUCCESS) {
            IHWExtDevice device = deviceArr[i];
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
            case ENABLE_DEVICE_SUCCESS /*1*/:
                return new HWExtMotion(ENABLE_DEVICE_SUCCESS);
            default:
                return null;
        }
    }

    private static int getDeviceUniqueId(IHWExtDevice device) {
        return (device.getHWExtDeviceSubType() * DEVICE_KEY_RATE) + device.getHWExtDeviceType();
    }

    private static int getDeviceUniqueId(int deviceType, int deviceSubType) {
        return (deviceSubType * DEVICE_KEY_RATE) + deviceType;
    }
}
