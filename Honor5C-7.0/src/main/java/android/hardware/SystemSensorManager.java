package android.hardware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager.DynamicSensorCallback;
import android.hsm.HwSystemManager;
import android.net.ProxyInfo;
import android.os.BatteryStats.HistoryItem;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.StrictMode;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.GuardedBy;
import dalvik.system.CloseGuard;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class SystemSensorManager extends SensorManager {
    private static boolean DEBUG_DYNAMIC_SENSOR;
    @GuardedBy("sLock")
    private static InjectEventQueue sInjectEventQueue;
    private static final Object sLock = null;
    @GuardedBy("sLock")
    private static boolean sNativeClassInited;
    private final Context mContext;
    private BroadcastReceiver mDynamicSensorBroadcastReceiver;
    private HashMap<DynamicSensorCallback, Handler> mDynamicSensorCallbacks;
    private boolean mDynamicSensorListDirty;
    private List<Sensor> mFullDynamicSensorsList;
    private final ArrayList<Sensor> mFullSensorsList;
    private final HashMap<Integer, Sensor> mHandleToSensor;
    private final Looper mMainLooper;
    private final long mNativeInstance;
    private final HashMap<SensorEventListener, SensorEventQueue> mSensorListeners;
    private final int mTargetSdkLevel;
    private final HashMap<TriggerEventListener, TriggerEventQueue> mTriggerListeners;

    /* renamed from: android.hardware.SystemSensorManager.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ List val$addedList;
        final /* synthetic */ DynamicSensorCallback val$callback;
        final /* synthetic */ List val$removedList;

        AnonymousClass1(List val$addedList, DynamicSensorCallback val$callback, List val$removedList) {
            this.val$addedList = val$addedList;
            this.val$callback = val$callback;
            this.val$removedList = val$removedList;
        }

        public void run() {
            for (Sensor s : this.val$addedList) {
                this.val$callback.onDynamicSensorConnected(s);
            }
            for (Sensor s2 : this.val$removedList) {
                this.val$callback.onDynamicSensorDisconnected(s2);
            }
        }
    }

    private static abstract class BaseEventQueue {
        protected static final int OPERATING_MODE_DATA_INJECTION = 1;
        protected static final int OPERATING_MODE_NORMAL = 0;
        private final SparseBooleanArray mActiveSensors;
        private final CloseGuard mCloseGuard;
        protected final SystemSensorManager mManager;
        protected final SparseIntArray mSensorAccuracies;
        private long nSensorEventQueue;

        private static native void nativeDestroySensorEventQueue(long j);

        private static native int nativeDisableSensor(long j, int i);

        private static native int nativeEnableSensor(long j, int i, int i2, int i3);

        private static native int nativeFlushSensor(long j);

        private static native long nativeInitBaseEventQueue(long j, WeakReference<BaseEventQueue> weakReference, MessageQueue messageQueue, String str, int i, String str2);

        private static native int nativeInjectSensorData(long j, int i, float[] fArr, int i2, long j2);

        protected abstract void addSensorEvent(Sensor sensor);

        protected abstract void dispatchFlushCompleteEvent(int i);

        protected abstract void dispatchSensorEvent(int i, float[] fArr, int i2, long j);

        protected abstract void removeSensorEvent(Sensor sensor);

        BaseEventQueue(Looper looper, SystemSensorManager manager, int mode, String packageName) {
            this.mActiveSensors = new SparseBooleanArray();
            this.mSensorAccuracies = new SparseIntArray();
            this.mCloseGuard = CloseGuard.get();
            if (packageName == null) {
                packageName = ProxyInfo.LOCAL_EXCL_LIST;
            }
            this.nSensorEventQueue = nativeInitBaseEventQueue(manager.mNativeInstance, new WeakReference(this), looper.getQueue(), packageName, mode, manager.mContext.getOpPackageName());
            this.mCloseGuard.open("dispose");
            this.mManager = manager;
        }

        public void dispose() {
            dispose(false);
        }

        public boolean addSensor(Sensor sensor, int delayUs, int maxBatchReportLatencyUs) {
            int handle = sensor.getHandle();
            if (this.mActiveSensors.get(handle)) {
                return false;
            }
            this.mActiveSensors.put(handle, true);
            addSensorEvent(sensor);
            if (enableSensor(sensor, delayUs, maxBatchReportLatencyUs) == 0 || (maxBatchReportLatencyUs != 0 && (maxBatchReportLatencyUs <= 0 || enableSensor(sensor, delayUs, 0) == 0))) {
                return true;
            }
            removeSensor(sensor, false);
            return false;
        }

        public boolean removeAllSensors() {
            for (int i = 0; i < this.mActiveSensors.size(); i += OPERATING_MODE_DATA_INJECTION) {
                if (this.mActiveSensors.valueAt(i)) {
                    int handle = this.mActiveSensors.keyAt(i);
                    Sensor sensor = (Sensor) this.mManager.mHandleToSensor.get(Integer.valueOf(handle));
                    if (sensor != null) {
                        disableSensor(sensor);
                        this.mActiveSensors.put(handle, false);
                        removeSensorEvent(sensor);
                    }
                }
            }
            return true;
        }

        public boolean removeSensor(Sensor sensor, boolean disable) {
            if (!this.mActiveSensors.get(sensor.getHandle())) {
                return false;
            }
            if (disable) {
                disableSensor(sensor);
            }
            this.mActiveSensors.put(sensor.getHandle(), false);
            removeSensorEvent(sensor);
            return true;
        }

        public int flush() {
            if (this.nSensorEventQueue != 0) {
                return nativeFlushSensor(this.nSensorEventQueue);
            }
            throw new NullPointerException();
        }

        public boolean hasSensors() {
            return this.mActiveSensors.indexOfValue(true) >= 0;
        }

        protected void finalize() throws Throwable {
            try {
                dispose(true);
            } finally {
                super.finalize();
            }
        }

        private void dispose(boolean finalized) {
            if (this.mCloseGuard != null) {
                if (finalized) {
                    this.mCloseGuard.warnIfOpen();
                }
                this.mCloseGuard.close();
            }
            if (this.nSensorEventQueue != 0) {
                nativeDestroySensorEventQueue(this.nSensorEventQueue);
                this.nSensorEventQueue = 0;
            }
        }

        private int enableSensor(Sensor sensor, int rateUs, int maxBatchReportLatencyUs) {
            if (this.nSensorEventQueue == 0) {
                throw new NullPointerException();
            } else if (sensor != null) {
                return nativeEnableSensor(this.nSensorEventQueue, sensor.getHandle(), rateUs, maxBatchReportLatencyUs);
            } else {
                throw new NullPointerException();
            }
        }

        protected int injectSensorDataBase(int handle, float[] values, int accuracy, long timestamp) {
            return nativeInjectSensorData(this.nSensorEventQueue, handle, values, accuracy, timestamp);
        }

        private int disableSensor(Sensor sensor) {
            if (this.nSensorEventQueue == 0) {
                throw new NullPointerException();
            } else if (sensor != null) {
                return nativeDisableSensor(this.nSensorEventQueue, sensor.getHandle());
            } else {
                throw new NullPointerException();
            }
        }

        protected void dispatchAdditionalInfoEvent(int handle, int type, int serial, float[] floatValues, int[] intValues) {
        }
    }

    final class InjectEventQueue extends BaseEventQueue {
        public InjectEventQueue(Looper looper, SystemSensorManager manager, String packageName) {
            super(looper, manager, 1, packageName);
        }

        int injectSensorData(int handle, float[] values, int accuracy, long timestamp) {
            return injectSensorDataBase(handle, values, accuracy, timestamp);
        }

        protected void dispatchSensorEvent(int handle, float[] values, int accuracy, long timestamp) {
        }

        protected void dispatchFlushCompleteEvent(int handle) {
        }

        protected void addSensorEvent(Sensor sensor) {
        }

        protected void removeSensorEvent(Sensor sensor) {
        }
    }

    static final class SensorEventQueue extends BaseEventQueue {
        private final SensorEventListener mListener;
        private final SparseArray<SensorEvent> mSensorsEvents;

        public SensorEventQueue(SensorEventListener listener, Looper looper, SystemSensorManager manager, String packageName) {
            super(looper, manager, 0, packageName);
            this.mSensorsEvents = new SparseArray();
            this.mListener = listener;
        }

        public void addSensorEvent(Sensor sensor) {
            SensorEvent t = new SensorEvent(Sensor.getMaxLengthValuesArray(sensor, this.mManager.mTargetSdkLevel));
            synchronized (this.mSensorsEvents) {
                this.mSensorsEvents.put(sensor.getHandle(), t);
            }
        }

        public void removeSensorEvent(Sensor sensor) {
            synchronized (this.mSensorsEvents) {
                this.mSensorsEvents.delete(sensor.getHandle());
            }
        }

        protected void dispatchSensorEvent(int handle, float[] values, int inAccuracy, long timestamp) {
            Sensor sensor = (Sensor) this.mManager.mHandleToSensor.get(Integer.valueOf(handle));
            if (sensor != null) {
                SensorEvent t;
                synchronized (this.mSensorsEvents) {
                    t = (SensorEvent) this.mSensorsEvents.get(handle);
                }
                if (t != null) {
                    System.arraycopy(values, 0, t.values, 0, t.values.length);
                    t.timestamp = timestamp;
                    t.accuracy = inAccuracy;
                    t.sensor = sensor;
                    int accuracy = this.mSensorAccuracies.get(handle);
                    if (t.accuracy >= 0 && accuracy != t.accuracy) {
                        this.mSensorAccuracies.put(handle, t.accuracy);
                        this.mListener.onAccuracyChanged(t.sensor, t.accuracy);
                    }
                    this.mListener.onSensorChanged(t);
                }
            }
        }

        protected void dispatchFlushCompleteEvent(int handle) {
            if (this.mListener instanceof SensorEventListener2) {
                Sensor sensor = (Sensor) this.mManager.mHandleToSensor.get(Integer.valueOf(handle));
                if (sensor != null) {
                    ((SensorEventListener2) this.mListener).onFlushCompleted(sensor);
                }
            }
        }

        protected void dispatchAdditionalInfoEvent(int handle, int type, int serial, float[] floatValues, int[] intValues) {
            if (this.mListener instanceof SensorEventCallback) {
                Sensor sensor = (Sensor) this.mManager.mHandleToSensor.get(Integer.valueOf(handle));
                if (sensor != null) {
                    ((SensorEventCallback) this.mListener).onSensorAdditionalInfo(new SensorAdditionalInfo(sensor, type, serial, intValues, floatValues));
                }
            }
        }
    }

    static final class TriggerEventQueue extends BaseEventQueue {
        private final TriggerEventListener mListener;
        private final SparseArray<TriggerEvent> mTriggerEvents;

        public TriggerEventQueue(TriggerEventListener listener, Looper looper, SystemSensorManager manager, String packageName) {
            super(looper, manager, 0, packageName);
            this.mTriggerEvents = new SparseArray();
            this.mListener = listener;
        }

        public void addSensorEvent(Sensor sensor) {
            TriggerEvent t = new TriggerEvent(Sensor.getMaxLengthValuesArray(sensor, this.mManager.mTargetSdkLevel));
            synchronized (this.mTriggerEvents) {
                this.mTriggerEvents.put(sensor.getHandle(), t);
            }
        }

        public void removeSensorEvent(Sensor sensor) {
            synchronized (this.mTriggerEvents) {
                this.mTriggerEvents.delete(sensor.getHandle());
            }
        }

        protected void dispatchSensorEvent(int handle, float[] values, int accuracy, long timestamp) {
            Sensor sensor = (Sensor) this.mManager.mHandleToSensor.get(Integer.valueOf(handle));
            if (sensor != null) {
                TriggerEvent t;
                synchronized (this.mTriggerEvents) {
                    t = (TriggerEvent) this.mTriggerEvents.get(handle);
                }
                if (t == null) {
                    Log.e("SensorManager", "Error: Trigger Event is null for Sensor: " + sensor);
                    return;
                }
                System.arraycopy(values, 0, t.values, 0, t.values.length);
                t.timestamp = timestamp;
                t.sensor = sensor;
                this.mManager.cancelTriggerSensorImpl(this.mListener, sensor, false);
                this.mListener.onTrigger(t);
            }
        }

        protected void dispatchFlushCompleteEvent(int handle) {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.SystemSensorManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.SystemSensorManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.SystemSensorManager.<clinit>():void");
    }

    private static native void nativeClassInit();

    private static native long nativeCreate(String str);

    private static native void nativeGetDynamicSensors(long j, List<Sensor> list);

    private static native boolean nativeGetSensorAtIndex(long j, Sensor sensor, int i);

    private static native boolean nativeIsDataInjectionEnabled(long j);

    public SystemSensorManager(Context context, Looper mainLooper) {
        this.mFullSensorsList = new ArrayList();
        this.mFullDynamicSensorsList = new ArrayList();
        this.mDynamicSensorListDirty = true;
        this.mHandleToSensor = new HashMap();
        this.mSensorListeners = new HashMap();
        this.mTriggerListeners = new HashMap();
        this.mDynamicSensorCallbacks = new HashMap();
        synchronized (sLock) {
            if (!sNativeClassInited) {
                sNativeClassInited = true;
                nativeClassInit();
            }
        }
        this.mMainLooper = mainLooper;
        this.mTargetSdkLevel = context.getApplicationInfo().targetSdkVersion;
        this.mContext = context;
        this.mNativeInstance = nativeCreate(context.getOpPackageName());
        int index = 0;
        while (true) {
            Sensor sensor = new Sensor();
            if (nativeGetSensorAtIndex(this.mNativeInstance, sensor, index)) {
                this.mFullSensorsList.add(sensor);
                this.mHandleToSensor.put(Integer.valueOf(sensor.getHandle()), sensor);
                index++;
            } else {
                return;
            }
        }
    }

    protected List<Sensor> getFullSensorList() {
        return this.mFullSensorsList;
    }

    protected List<Sensor> getFullDynamicSensorList() {
        setupDynamicSensorBroadcastReceiver();
        updateDynamicSensorList();
        return this.mFullDynamicSensorsList;
    }

    protected boolean registerListenerImpl(SensorEventListener listener, Sensor sensor, int delayUs, Handler handler, int maxBatchReportLatencyUs, int reservedFlags) {
        if (listener == null || sensor == null) {
            Log.e("SensorManager", "sensor or listener is null");
            return false;
        } else if (sensor.getReportingMode() == 2) {
            Log.e("SensorManager", "Trigger Sensors should use the requestTriggerSensor.");
            return false;
        } else if (maxBatchReportLatencyUs < 0 || delayUs < 0) {
            Log.e("SensorManager", "maxBatchReportLatencyUs and delayUs should be non-negative");
            return false;
        } else {
            int sensorType = sensor.getType();
            if ((19 == sensorType || 18 == sensorType || 1 == sensorType) && !HwSystemManager.allowOp(StrictMode.PENALTY_DEATH_ON_FILE_URI_EXPOSURE)) {
                Log.d("SensorManager", "registerListenerImpl blocked, sensor type " + sensorType);
                return false;
            } else if (21 != sensorType || HwSystemManager.allowOp(HistoryItem.STATE_WIFI_SCAN_FLAG)) {
                synchronized (this.mSensorListeners) {
                    SensorEventQueue queue = (SensorEventQueue) this.mSensorListeners.get(listener);
                    if (queue == null) {
                        String fullClassName;
                        Looper looper = handler != null ? handler.getLooper() : this.mMainLooper;
                        if (listener.getClass().getEnclosingClass() != null) {
                            fullClassName = listener.getClass().getEnclosingClass().getName();
                        } else {
                            fullClassName = listener.getClass().getName();
                        }
                        queue = new SensorEventQueue(listener, looper, this, fullClassName);
                        if (queue.addSensor(sensor, delayUs, maxBatchReportLatencyUs)) {
                            this.mSensorListeners.put(listener, queue);
                            return true;
                        }
                        queue.dispose();
                        return false;
                    }
                    boolean addSensor = queue.addSensor(sensor, delayUs, maxBatchReportLatencyUs);
                    return addSensor;
                }
            } else {
                Log.d("SensorManager", "registerListenerImpl blocked, sensor type " + sensorType);
                return false;
            }
        }
    }

    protected void unregisterListenerImpl(SensorEventListener listener, Sensor sensor) {
        if (sensor == null || sensor.getReportingMode() != 2) {
            synchronized (this.mSensorListeners) {
                SensorEventQueue queue = (SensorEventQueue) this.mSensorListeners.get(listener);
                if (queue != null) {
                    boolean result;
                    if (sensor == null) {
                        result = queue.removeAllSensors();
                    } else {
                        result = queue.removeSensor(sensor, true);
                    }
                    if (result && !queue.hasSensors()) {
                        this.mSensorListeners.remove(listener);
                        queue.dispose();
                    }
                }
            }
        }
    }

    protected boolean requestTriggerSensorImpl(TriggerEventListener listener, Sensor sensor) {
        if (sensor == null) {
            throw new IllegalArgumentException("sensor cannot be null");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        } else if (sensor.getReportingMode() != 2) {
            return false;
        } else {
            synchronized (this.mTriggerListeners) {
                TriggerEventQueue queue = (TriggerEventQueue) this.mTriggerListeners.get(listener);
                if (queue == null) {
                    String fullClassName;
                    if (listener.getClass().getEnclosingClass() != null) {
                        fullClassName = listener.getClass().getEnclosingClass().getName();
                    } else {
                        fullClassName = listener.getClass().getName();
                    }
                    queue = new TriggerEventQueue(listener, this.mMainLooper, this, fullClassName);
                    if (queue.addSensor(sensor, 0, 0)) {
                        this.mTriggerListeners.put(listener, queue);
                        return true;
                    }
                    queue.dispose();
                    return false;
                }
                boolean addSensor = queue.addSensor(sensor, 0, 0);
                return addSensor;
            }
        }
    }

    protected boolean cancelTriggerSensorImpl(TriggerEventListener listener, Sensor sensor, boolean disable) {
        if (sensor != null && sensor.getReportingMode() != 2) {
            return false;
        }
        synchronized (this.mTriggerListeners) {
            TriggerEventQueue queue = (TriggerEventQueue) this.mTriggerListeners.get(listener);
            if (queue != null) {
                boolean result;
                if (sensor == null) {
                    result = queue.removeAllSensors();
                } else {
                    result = queue.removeSensor(sensor, disable);
                }
                if (result && !queue.hasSensors()) {
                    this.mTriggerListeners.remove(listener);
                    queue.dispose();
                }
                return result;
            }
            return false;
        }
    }

    protected boolean flushImpl(SensorEventListener listener) {
        boolean z = false;
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        synchronized (this.mSensorListeners) {
            SensorEventQueue queue = (SensorEventQueue) this.mSensorListeners.get(listener);
            if (queue == null) {
                return false;
            }
            if (queue.flush() == 0) {
                z = true;
            }
            return z;
        }
    }

    protected boolean initDataInjectionImpl(boolean enable) {
        synchronized (sLock) {
            if (enable) {
                if (!nativeIsDataInjectionEnabled(this.mNativeInstance)) {
                    Log.e("SensorManager", "Data Injection mode not enabled");
                    return false;
                } else if (sInjectEventQueue == null) {
                    sInjectEventQueue = new InjectEventQueue(this.mMainLooper, this, this.mContext.getPackageName());
                }
            } else if (sInjectEventQueue != null) {
                sInjectEventQueue.dispose();
                sInjectEventQueue = null;
            }
            return true;
        }
    }

    protected boolean injectSensorDataImpl(Sensor sensor, float[] values, int accuracy, long timestamp) {
        synchronized (sLock) {
            if (sInjectEventQueue == null) {
                Log.e("SensorManager", "Data injection mode not activated before calling injectSensorData");
                return false;
            }
            int ret = sInjectEventQueue.injectSensorData(sensor.getHandle(), values, accuracy, timestamp);
            if (ret != 0) {
                sInjectEventQueue.dispose();
                sInjectEventQueue = null;
            }
            boolean z = ret == 0;
            return z;
        }
    }

    private void cleanupSensorConnection(Sensor sensor) {
        this.mHandleToSensor.remove(Integer.valueOf(sensor.getHandle()));
        HashMap hashMap;
        if (sensor.getReportingMode() == 2) {
            hashMap = this.mTriggerListeners;
            synchronized (hashMap) {
            }
            for (TriggerEventListener l : this.mTriggerListeners.keySet()) {
                if (DEBUG_DYNAMIC_SENSOR) {
                    Log.i("SensorManager", "removed trigger listener" + l.toString() + " due to sensor disconnection");
                }
                cancelTriggerSensorImpl(l, sensor, true);
            }
        } else {
            hashMap = this.mSensorListeners;
            synchronized (hashMap) {
            }
            for (SensorEventListener l2 : this.mSensorListeners.keySet()) {
                if (DEBUG_DYNAMIC_SENSOR) {
                    Log.i("SensorManager", "removed event listener" + l2.toString() + " due to sensor disconnection");
                }
                unregisterListenerImpl(l2, sensor);
            }
        }
    }

    private void updateDynamicSensorList() {
        synchronized (this.mFullDynamicSensorsList) {
            if (this.mDynamicSensorListDirty) {
                List<Sensor> list = new ArrayList();
                nativeGetDynamicSensors(this.mNativeInstance, list);
                List<Sensor> updatedList = new ArrayList();
                List<Sensor> addedList = new ArrayList();
                List<Sensor> removedList = new ArrayList();
                if (diffSortedSensorList(this.mFullDynamicSensorsList, list, updatedList, addedList, removedList)) {
                    if (DEBUG_DYNAMIC_SENSOR) {
                        Log.i("SensorManager", "DYNS dynamic sensor list cached should be updated");
                    }
                    this.mFullDynamicSensorsList = updatedList;
                    for (Sensor s : addedList) {
                        this.mHandleToSensor.put(Integer.valueOf(s.getHandle()), s);
                    }
                    Handler mainHandler = new Handler(this.mContext.getMainLooper());
                    for (Entry<DynamicSensorCallback, Handler> entry : this.mDynamicSensorCallbacks.entrySet()) {
                        (entry.getValue() == null ? mainHandler : (Handler) entry.getValue()).post(new AnonymousClass1(addedList, (DynamicSensorCallback) entry.getKey(), removedList));
                    }
                    for (Sensor s2 : removedList) {
                        cleanupSensorConnection(s2);
                    }
                }
                this.mDynamicSensorListDirty = false;
            }
        }
    }

    private void setupDynamicSensorBroadcastReceiver() {
        if (this.mDynamicSensorBroadcastReceiver == null) {
            this.mDynamicSensorBroadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction() == Intent.ACTION_DYNAMIC_SENSOR_CHANGED) {
                        if (SystemSensorManager.DEBUG_DYNAMIC_SENSOR) {
                            Log.i("SensorManager", "DYNS received DYNAMIC_SENSOR_CHANED broadcast");
                        }
                        SystemSensorManager.this.mDynamicSensorListDirty = true;
                        SystemSensorManager.this.updateDynamicSensorList();
                    }
                }
            };
            IntentFilter filter = new IntentFilter("dynamic_sensor_change");
            filter.addAction(Intent.ACTION_DYNAMIC_SENSOR_CHANGED);
            this.mContext.registerReceiver(this.mDynamicSensorBroadcastReceiver, filter);
        }
    }

    private void teardownDynamicSensorBroadcastReceiver() {
        this.mDynamicSensorCallbacks.clear();
        this.mContext.unregisterReceiver(this.mDynamicSensorBroadcastReceiver);
        this.mDynamicSensorBroadcastReceiver = null;
    }

    protected void registerDynamicSensorCallbackImpl(DynamicSensorCallback callback, Handler handler) {
        if (DEBUG_DYNAMIC_SENSOR) {
            Log.i("SensorManager", "DYNS Register dynamic sensor callback");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        } else if (!this.mDynamicSensorCallbacks.containsKey(callback)) {
            setupDynamicSensorBroadcastReceiver();
            this.mDynamicSensorCallbacks.put(callback, handler);
        }
    }

    protected void unregisterDynamicSensorCallbackImpl(DynamicSensorCallback callback) {
        if (DEBUG_DYNAMIC_SENSOR) {
            Log.i("SensorManager", "Removing dynamic sensor listerner");
        }
        this.mDynamicSensorCallbacks.remove(callback);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean diffSortedSensorList(List<Sensor> oldList, List<Sensor> newList, List<Sensor> updated, List<Sensor> added, List<Sensor> removed) {
        boolean changed = false;
        int i = 0;
        int j = 0;
        while (true) {
            if (j < oldList.size() && (i >= newList.size() || ((Sensor) newList.get(i)).getHandle() > ((Sensor) oldList.get(j)).getHandle())) {
                changed = true;
                if (removed != null) {
                    removed.add((Sensor) oldList.get(j));
                }
                j++;
            } else if (i < newList.size() && (j >= oldList.size() || ((Sensor) newList.get(i)).getHandle() < ((Sensor) oldList.get(j)).getHandle())) {
                changed = true;
                if (added != null) {
                    added.add((Sensor) newList.get(i));
                }
                if (updated != null) {
                    updated.add((Sensor) newList.get(i));
                }
                i++;
            } else if (i >= newList.size() || j >= oldList.size() || ((Sensor) newList.get(i)).getHandle() != ((Sensor) oldList.get(j)).getHandle()) {
                return changed;
            } else {
                if (updated != null) {
                    updated.add((Sensor) oldList.get(j));
                }
                i++;
                j++;
            }
        }
        return changed;
    }
}
