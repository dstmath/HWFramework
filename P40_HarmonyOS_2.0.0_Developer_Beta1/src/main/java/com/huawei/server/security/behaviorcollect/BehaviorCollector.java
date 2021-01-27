package com.huawei.server.security.behaviorcollect;

import android.app.AlarmManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import com.android.server.am.PointerEventListenerEx;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.HiViewEx;
import com.huawei.android.app.IProcessObserverEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.hwpartsecurityservices.BuildConfig;
import com.huawei.server.policy.WindowManagerFuncsEx;
import com.huawei.server.security.behaviorcollect.IBehaviorAuthService;
import com.huawei.server.security.behaviorcollect.bean.OnceData;
import com.huawei.server.security.behaviorcollect.bean.SensorData;
import com.huawei.server.security.behaviorcollect.bean.TouchPoint;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;

public class BehaviorCollector extends DefaultBehaviorCollector {
    private static final String AUTH_SERVICE_CLASS = "com.huawei.behaviorauth.HwBehaviorAuthService";
    private static final String AUTH_SERVICE_PACKAGE = "com.huawei.behaviorauth";
    private static final int BIND_AUTH_SERVICE_TIMEOUT = 600;
    private static final Object BIND_STATUS_LOCK = new Object();
    private static final String BOT_RESULT = "botResult";
    private static final String BUNDLE_KEY = "behavior_data";
    private static final int DB_REPORT_EVENT_ID = 991311050;
    private static final int DEFAULT_DATA_SIZE = 10;
    public static final int EVENT_GAME_OFF = 2;
    public static final int EVENT_GAME_ON = 1;
    public static final int EVENT_SCREEN_ON = 3;
    private static final String FAIL_REASON = "failedReason";
    private static final long KILL_AUTH_SERVICE_TIMEOUT = 180000;
    private static final int LATCH_COUNT = 1;
    private static final int MAX_COUNT_DATA = 10;
    private static final int MAX_SENSOR_DATA_COUNT = 100;
    private static final int MAX_TRY_TIMES_TO_BINDSERVICE = 3;
    private static final String PKG_NAME = "pkgName";
    private static final String RUN_TIME = "runTime";
    private static final int SENSOR_DATA_SCALE = 4;
    private static final String TAG = BehaviorCollector.class.getSimpleName();
    private static volatile BehaviorCollector single = null;
    private Sensor accelerometerSensor = null;
    private final AlarmManager.OnAlarmListener alarmListener = new AlarmManager.OnAlarmListener() {
        /* class com.huawei.server.security.behaviorcollect.BehaviorCollector.AnonymousClass1 */

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            Log.i(BehaviorCollector.TAG, "start force stop auth service");
            synchronized (BehaviorCollector.this) {
                BehaviorCollector.this.getMostRecentBotDetectResult();
            }
            BehaviorCollector.this.unbindAuthService();
            try {
                ActivityManagerEx.forceStopPackage(BehaviorCollector.AUTH_SERVICE_PACKAGE, UserHandleEx.myUserId());
            } catch (RemoteException e) {
                Log.e(BehaviorCollector.TAG, "forceStopPackage cause remote exception");
            }
        }
    };
    private AlarmManager alarmManager = null;
    private volatile Map<String, CollectData> behaviorCache = new ConcurrentHashMap(10);
    private Context behaviorContext = null;
    private IBehaviorAuthService behaviorDetectClient = null;
    private BehaviorPointerEventListener behaviorPointerListener = null;
    private BehaviorSensorEventListener behaviorSensorEventListener = null;
    private WindowManagerFuncsEx behaviorWindowManagerFuncs = null;
    private volatile BindServiceStatus bindServiceStatus = BindServiceStatus.NOT_BIND;
    private CountDownLatch countDownLatch = null;
    private volatile Map<String, Float> detectResult = new ConcurrentHashMap(10);
    private BehaviorDetectServiceConnection detectServiceConnection = null;
    private Sensor gyroscopeSensor = null;
    private Handler handler = null;
    private HandlerThread handlerThread = null;
    private volatile boolean hasGameHalted = false;
    private volatile boolean hasRegistPointListener = false;
    private volatile boolean hasRegistSensorListener = false;
    private HwProcessObserver hwProcessObserver = null;
    private ArrayList<SensorData> sensorDatas = new ArrayList<>((int) MAX_SENSOR_DATA_COUNT);
    private SensorManager sensorManager = null;
    private ArrayList<TouchPoint> touchPoints = new ArrayList<>(10);

    /* access modifiers changed from: private */
    public enum BindServiceStatus {
        NOT_BIND,
        BINDING,
        CANNOT_BIND,
        BIND_FAILED_DIE,
        BIND_FAILED_DISCONNECTED,
        BIND_CONNECTED
    }

    /* access modifiers changed from: private */
    public class CollectData {
        private int count;
        private List<OnceData> rawData;

        private CollectData() {
            this.count = 0;
            this.rawData = new ArrayList(10);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addBehaviorData(OnceData onceData) {
            this.rawData.add(onceData);
            this.count++;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean hasCollectedFull() {
            return this.count >= 10;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearData() {
            this.rawData.clear();
            this.count = 0;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private List<OnceData> getBehaviorData() {
            return this.rawData;
        }
    }

    public static BehaviorCollector getInstance() {
        if (single == null) {
            synchronized (BehaviorCollector.class) {
                if (single == null) {
                    single = new BehaviorCollector();
                }
            }
        }
        return single;
    }

    public void init(Context context, WindowManagerFuncsEx windowManagerFuncs) {
        Log.i(TAG, "init behavior collect");
        this.behaviorContext = context;
        this.behaviorWindowManagerFuncs = windowManagerFuncs;
    }

    public synchronized int addPackage(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            Log.e(TAG, "addPackage:packagename is null.");
            return -1;
        }
        String str = TAG;
        Log.i(str, "call addPackage pkgName = " + pkgName);
        if (!hasInit()) {
            return -9;
        }
        if (this.behaviorCache.containsKey(pkgName)) {
            return -2;
        }
        if (!bindAuthService()) {
            return -10;
        }
        if (this.behaviorCache.size() == 0) {
            readyResource();
        }
        this.behaviorCache.put(pkgName, new CollectData());
        registerTouchListener();
        return 0;
    }

    public synchronized int removePackage(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            Log.e(TAG, "removePackage:packagename is null.");
            return -1;
        }
        String str = TAG;
        Log.i(str, "call removePackage pkgName = " + pkgName);
        if (!hasInit()) {
            return -9;
        }
        if (!this.behaviorCache.containsKey(pkgName)) {
            return -3;
        }
        scheduleServiceStopTask();
        this.behaviorCache.remove(pkgName);
        if (this.behaviorCache.size() == 0) {
            releaseResource();
        }
        return 0;
    }

    public synchronized float getBotResultFromModel(String pkgName) {
        float result;
        long startTime = SystemClock.elapsedRealtime();
        if (TextUtils.isEmpty(pkgName)) {
            Log.e(TAG, "getBotResultFromModel: packagename is null.");
            dataReport(null, SystemClock.elapsedRealtime() - startTime, -1.0f);
            return -1.0f;
        }
        String str = TAG;
        Log.i(str, "call getBotResultFromModel pkgName = " + pkgName);
        if (!hasInit()) {
            dataReport(pkgName, SystemClock.elapsedRealtime() - startTime, -9.0f);
            return -9.0f;
        } else if (!this.behaviorCache.containsKey(pkgName)) {
            dataReport(pkgName, SystemClock.elapsedRealtime() - startTime, -3.0f);
            return -3.0f;
        } else if (this.behaviorDetectClient == null || this.bindServiceStatus != BindServiceStatus.BIND_CONNECTED) {
            return getCachedDetectResult(startTime, pkgName);
        } else {
            scheduleServiceStopTask();
            if (this.behaviorCache.get(pkgName) == null || this.behaviorCache.get(pkgName).count == 0) {
                Log.e(TAG, "has no behavior data collect");
                dataReport(pkgName, SystemClock.elapsedRealtime() - startTime, -6.0f);
                return -6.0f;
            }
            try {
                result = this.behaviorDetectClient.getBotDetectResult(getBehaviorRawData(pkgName));
            } catch (RemoteException e) {
                Log.e(TAG, "behaviorDetectClient.getBotDetectResult cause remote exception");
                result = -5.0f;
            }
            this.behaviorCache.get(pkgName).clearData();
            registerTouchListener();
            dataReport(pkgName, SystemClock.elapsedRealtime() - startTime, result);
            return result;
        }
    }

    private float getCachedDetectResult(long startTime, String pkgName) {
        if (!this.detectResult.containsKey(pkgName)) {
            String str = TAG;
            Log.e(str, "the detect result of " + pkgName + " has never been cached.");
            dataReport(pkgName, SystemClock.elapsedRealtime() - startTime, -4.0f);
            return -4.0f;
        } else if (this.detectResult.get(pkgName).floatValue() >= 0.0f || this.behaviorCache.get(pkgName) == null || this.behaviorCache.get(pkgName).count == 0) {
            this.handler.post(new Runnable() {
                /* class com.huawei.server.security.behaviorcollect.BehaviorCollector.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    if (!BehaviorCollector.this.bindAuthService()) {
                        Log.e(BehaviorCollector.TAG, "reconnect behavior auth service failed.");
                    }
                }
            });
            dataReport(pkgName, SystemClock.elapsedRealtime() - startTime, this.detectResult.get(pkgName).floatValue());
            return this.detectResult.get(pkgName).floatValue();
        } else {
            Log.i(TAG, "update the old error detect result.");
            float result = updateDetectResult(pkgName);
            dataReport(pkgName, SystemClock.elapsedRealtime() - startTime, result);
            return result;
        }
    }

    private float updateDetectResult(String pkgName) {
        float result = -4.0f;
        if (bindAuthService() && this.behaviorDetectClient != null) {
            try {
                result = this.behaviorDetectClient.getBotDetectResult(getBehaviorRawData(pkgName));
            } catch (RemoteException e) {
                Log.e(TAG, "updateDetectResult cause remote exception");
                result = -5.0f;
            }
        }
        this.behaviorCache.get(pkgName).clearData();
        registerTouchListener();
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getMostRecentBotDetectResult() {
        float result;
        if (this.bindServiceStatus != BindServiceStatus.BIND_CONNECTED || this.behaviorDetectClient == null) {
            Log.e(TAG, "getMostRecentBotDetectResult failed for invalid status of behavior auth service");
        } else if (this.behaviorCache.size() == 0) {
            Log.i(TAG, "no callers bot detect result need to update.");
        } else {
            this.detectResult.clear();
            for (String pkgName : this.behaviorCache.keySet()) {
                if (this.behaviorCache.get(pkgName) == null || this.behaviorCache.get(pkgName).count != 0) {
                    this.detectResult.put(pkgName, Float.valueOf(-6.0f));
                } else {
                    try {
                        result = this.behaviorDetectClient.getBotDetectResult(getBehaviorRawData(pkgName));
                    } catch (RemoteException e) {
                        String str = TAG;
                        Log.e(str, "getMostRecentBotDetectResult failed because of remote exception " + pkgName);
                        result = -5.0f;
                    } catch (Throwable th) {
                        this.detectResult.put(pkgName, Float.valueOf(-6.0f));
                        this.behaviorCache.get(pkgName).clearData();
                        throw th;
                    }
                    this.detectResult.put(pkgName, Float.valueOf(result));
                    this.behaviorCache.get(pkgName).clearData();
                }
            }
            registerTouchListener();
        }
    }

    public void notifyEvent(int eventType) {
        if (eventType == 1) {
            Log.i(TAG, "receive game on event");
            boolean isRegisterBefore = this.hasRegistPointListener;
            unRegistPointListener();
            if (isRegisterBefore && !this.hasRegistPointListener) {
                this.hasGameHalted = true;
            }
        } else if (eventType != 2) {
            String str = TAG;
            Log.i(str, "unknown event " + eventType);
        } else {
            Log.i(TAG, "receive game off event");
            if (this.hasGameHalted) {
                this.hasGameHalted = false;
                if (this.behaviorCache.size() != 0) {
                    registerTouchListener();
                }
            }
        }
    }

    private boolean hasInit() {
        return (this.behaviorContext == null || this.behaviorWindowManagerFuncs == null) ? false : true;
    }

    private void scheduleServiceStopTask() {
        if (this.alarmManager == null) {
            this.alarmManager = (AlarmManager) this.behaviorContext.getSystemService(AlarmManager.class);
        }
        if (this.alarmManager == null) {
            Log.e(TAG, "alarmManager is null");
            return;
        }
        long origId = Binder.clearCallingIdentity();
        try {
            Log.i(TAG, "start scheduleStopService");
            this.alarmManager.cancel(this.alarmListener);
            this.alarmManager.setExact(2, KILL_AUTH_SERVICE_TIMEOUT + SystemClock.elapsedRealtime(), null, this.alarmListener, null);
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    private synchronized void registerTouchListener() {
        if (this.hasRegistPointListener) {
            Log.i(TAG, "has already regist the point listener");
        } else if (this.behaviorWindowManagerFuncs == null || this.behaviorPointerListener == null) {
            Log.e(TAG, "registerTouchListener failed: ManagerFuncs or Listener is empty");
        } else {
            this.behaviorWindowManagerFuncs.registerPointerEventListener(this.behaviorPointerListener, 0);
            this.hasRegistPointListener = true;
            Log.i(TAG, "regist the point listener successfully");
        }
    }

    private synchronized void unRegistPointListener() {
        if (!this.hasRegistPointListener) {
            Log.i(TAG, "has already release the point listener");
        } else if (this.behaviorWindowManagerFuncs == null || this.behaviorPointerListener == null) {
            Log.e(TAG, "unRegistPointListener failed: ManagerFuncs or Listener is empty");
        } else {
            this.behaviorWindowManagerFuncs.unregisterPointerEventListener(this.behaviorPointerListener, 0);
            this.hasRegistPointListener = false;
            Log.i(TAG, "release the point listener successfullly");
        }
    }

    private Bundle getBehaviorRawData(String pkgName) {
        List<OnceData> dataList = this.behaviorCache.get(pkgName).getBehaviorData();
        String str = TAG;
        Log.i(str, "dataList.size = " + dataList.size());
        Bundle bundle = new Bundle();
        if (dataList instanceof ArrayList) {
            bundle.putParcelableArrayList(BUNDLE_KEY, (ArrayList) dataList);
        }
        return bundle;
    }

    private void readyResource() {
        Log.i(TAG, "begin prepare the environment");
        Object sensorObj = this.behaviorContext.getSystemService("sensor");
        if (sensorObj instanceof SensorManager) {
            this.sensorManager = (SensorManager) sensorObj;
            this.accelerometerSensor = this.sensorManager.getDefaultSensor(1);
            this.gyroscopeSensor = this.sensorManager.getDefaultSensor(4);
        }
        this.handlerThread = new HandlerThread("Behavior Data Thread");
        this.handlerThread.start();
        if (this.handlerThread.getLooper() != null) {
            this.handler = new Handler(this.handlerThread.getLooper());
        }
        this.behaviorPointerListener = new BehaviorPointerEventListener();
        this.behaviorSensorEventListener = new BehaviorSensorEventListener();
        registerProcessObserver();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void releaseResource() {
        Log.i(TAG, "begin release the environment");
        unRegistSensorEventListener();
        unRegistPointListener();
        unRegisterProcessObserver();
        this.sensorManager = null;
        this.accelerometerSensor = null;
        this.gyroscopeSensor = null;
        this.behaviorPointerListener = null;
        this.behaviorSensorEventListener = null;
        if (this.handlerThread != null) {
            this.handlerThread.quitSafely();
            this.handlerThread = null;
        }
        this.handler = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized boolean bindAuthService() {
        bindAuthServiceOnce();
        if (this.bindServiceStatus == BindServiceStatus.BIND_FAILED_DIE) {
            bindAuthServiceOnce();
        }
        scheduleServiceStopTask();
        return this.bindServiceStatus == BindServiceStatus.BIND_CONNECTED;
    }

    private synchronized void bindAuthServiceOnce() {
        Throwable th;
        Log.i(TAG, "begin bindAuthService");
        if (this.bindServiceStatus == BindServiceStatus.BIND_CONNECTED) {
            Log.i(TAG, "has already connected the detect service");
            return;
        }
        this.bindServiceStatus = BindServiceStatus.BINDING;
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(AUTH_SERVICE_PACKAGE, AUTH_SERVICE_CLASS));
        this.detectServiceConnection = new BehaviorDetectServiceConnection();
        this.countDownLatch = new CountDownLatch(1);
        long origId = Binder.clearCallingIdentity();
        try {
            boolean canBindService = this.behaviorContext.bindService(intent, this.detectServiceConnection, 1);
            String str = TAG;
            Log.i(str, "start bind behaviorauth service canBindService = " + canBindService);
            if (!canBindService) {
                this.bindServiceStatus = BindServiceStatus.CANNOT_BIND;
                Log.e(TAG, "can not bind auth service");
                return;
            }
            try {
                this.countDownLatch.await(600, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Log.e(TAG, "await bind auth service failed, cause InterruptedException");
            }
            synchronized (BIND_STATUS_LOCK) {
                try {
                    if (this.bindServiceStatus == BindServiceStatus.BINDING) {
                        try {
                            Log.e(TAG, "connect the bind auth service failed for other reason");
                            this.behaviorContext.unbindService(this.detectServiceConnection);
                            this.bindServiceStatus = BindServiceStatus.NOT_BIND;
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unbindAuthService() {
        synchronized (BIND_STATUS_LOCK) {
            if (this.bindServiceStatus == BindServiceStatus.BIND_CONNECTED || this.bindServiceStatus == BindServiceStatus.BINDING) {
                if (!(this.behaviorContext == null || this.detectServiceConnection == null)) {
                    this.behaviorContext.unbindService(this.detectServiceConnection);
                }
                this.bindServiceStatus = BindServiceStatus.NOT_BIND;
                Log.i(TAG, "unbind behaviorauth service success");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class BehaviorDetectServiceConnection implements ServiceConnection {
        BehaviorDetectServiceConnection() {
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (BehaviorCollector.BIND_STATUS_LOCK) {
                if (BehaviorCollector.this.bindServiceStatus == BindServiceStatus.BINDING) {
                    Log.i(BehaviorCollector.TAG, "behaviorAuthService onServiceConnected.");
                    BehaviorCollector.this.behaviorDetectClient = IBehaviorAuthService.Stub.asInterface(service);
                    BehaviorCollector.this.bindServiceStatus = BindServiceStatus.BIND_CONNECTED;
                    BehaviorCollector.this.countDownLatch.countDown();
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            Log.i(BehaviorCollector.TAG, "behaviorAuthService onServiceDisconnected.");
            BehaviorCollector.this.behaviorDetectClient = null;
            BehaviorCollector.this.bindServiceStatus = BindServiceStatus.BIND_FAILED_DISCONNECTED;
            BehaviorCollector.this.countDownLatch.countDown();
        }

        @Override // android.content.ServiceConnection
        public void onBindingDied(ComponentName name) {
            Log.i(BehaviorCollector.TAG, "behaviorAuthService onBindingDied.");
            BehaviorCollector.this.behaviorDetectClient = null;
            BehaviorCollector.this.bindServiceStatus = BindServiceStatus.BIND_FAILED_DIE;
            BehaviorCollector.this.countDownLatch.countDown();
        }
    }

    private void cacheBehaviorData() {
        OnceData onceData = new OnceData(sIsActiveTouched, this.sensorDatas, this.touchPoints);
        synchronized (this) {
            for (String pkgName : this.behaviorCache.keySet()) {
                if (this.behaviorCache.get(pkgName) != null) {
                    if (!this.behaviorCache.get(pkgName).hasCollectedFull()) {
                        this.behaviorCache.get(pkgName).addBehaviorData(onceData);
                    }
                }
            }
        }
    }

    private synchronized void unRegistListenerIfAllFull() {
        boolean isAllFullCollected = true;
        Iterator<String> it = this.behaviorCache.keySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            if (!this.behaviorCache.get(it.next()).hasCollectedFull()) {
                isAllFullCollected = false;
                break;
            }
        }
        if (isAllFullCollected) {
            Log.i(TAG, "beacuse cache data has collected, so unregist listeners");
            unRegistPointListener();
            unRegistSensorEventListener();
        }
    }

    private void clearSensorData() {
        String str = TAG;
        Log.i(str, "start clearSensorData sIsActiveTouched = " + sIsActiveTouched);
        sIsActiveTouched = false;
        this.sensorDatas.clear();
        this.touchPoints.clear();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpEvent(MotionEvent event) {
        if (event != null) {
            handleMotionEvent(event);
            unRegistSensorEventListener();
            cacheBehaviorData();
            clearSensorData();
            unRegistListenerIfAllFull();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDownEvent(MotionEvent event) {
        if (event != null) {
            clearSensorData();
            handleMotionEvent(event);
            registSensorEventListener();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMoveEvent(MotionEvent event) {
        if (event != null) {
            handleMotionEvent(event);
        }
    }

    private void handleMotionEvent(MotionEvent ev) {
        TouchPoint tp = new TouchPoint((double) ev.getEventTime(), (double) ev.getX(), (double) ev.getY(), (double) ev.getPressure(), (double) ev.getSize());
        tp.setPointerId(ev.getPointerId(ev.getActionIndex()));
        tp.setOri((double) ev.getOrientation());
        tp.setAction(ev.getAction());
        this.touchPoints.add(tp);
    }

    private synchronized void registSensorEventListener() {
        if (!this.hasRegistSensorListener) {
            if (this.sensorManager != null) {
                if (this.behaviorSensorEventListener != null) {
                    this.sensorManager.registerListener(this.behaviorSensorEventListener, this.accelerometerSensor, 0);
                    this.sensorManager.registerListener(this.behaviorSensorEventListener, this.gyroscopeSensor, 0);
                    this.hasRegistSensorListener = true;
                }
            }
        }
    }

    private synchronized void unRegistSensorEventListener() {
        if (this.hasRegistSensorListener) {
            if (this.sensorManager != null) {
                if (this.behaviorSensorEventListener != null) {
                    this.sensorManager.unregisterListener(this.behaviorSensorEventListener);
                    this.hasRegistSensorListener = false;
                }
            }
        }
    }

    private void registerProcessObserver() {
        if (this.hwProcessObserver == null) {
            this.hwProcessObserver = new HwProcessObserver();
            long origId = Binder.clearCallingIdentity();
            try {
                ActivityManagerEx.registerProcessObserver(this.hwProcessObserver);
                Log.i(TAG, "register process observer success");
            } catch (RemoteException e) {
                Log.e(TAG, "register process observer failed");
                this.hwProcessObserver = null;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
            Binder.restoreCallingIdentity(origId);
        }
    }

    private void unRegisterProcessObserver() {
        Log.i(TAG, "start unregister process observer");
        if (this.hwProcessObserver != null) {
            long origId = Binder.clearCallingIdentity();
            try {
                ActivityManagerEx.unregisterProcessObserver(this.hwProcessObserver);
                Log.i(TAG, "unregister process observer success");
            } catch (RemoteException e) {
                Log.e(TAG, "unregister process observer failed");
            } catch (Throwable th) {
                this.hwProcessObserver = null;
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
            this.hwProcessObserver = null;
            Binder.restoreCallingIdentity(origId);
        }
    }

    private void dataReport(String pkgName, long runTime, float botResult) {
        int failedReason;
        JSONObject bdBehaviorAuth = new JSONObject();
        if (botResult < 0.0f) {
            failedReason = (int) botResult;
        } else {
            failedReason = 0;
        }
        try {
            bdBehaviorAuth.put(PKG_NAME, pkgName);
            bdBehaviorAuth.put(RUN_TIME, runTime + BuildConfig.FLAVOR);
            bdBehaviorAuth.put(BOT_RESULT, botResult + BuildConfig.FLAVOR);
            bdBehaviorAuth.put(FAIL_REASON, failedReason + BuildConfig.FLAVOR);
            HiViewEx.report(HiViewEx.byJson((int) DB_REPORT_EVENT_ID, bdBehaviorAuth).putAppInfo(this.behaviorContext));
            String str = TAG;
            Log.i(str, "report data : " + bdBehaviorAuth.toString());
        } catch (JSONException e) {
            Log.e(TAG, "reportBigData Error");
        }
    }

    /* access modifiers changed from: private */
    public class HwProcessObserver extends IProcessObserverEx {
        private HwProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean isForegroundActivities) {
        }

        public void onProcessDied(int pid, int uid) {
            PackageManager packageManager;
            String[] pkgNames;
            if (BehaviorCollector.this.behaviorCache.size() != 0 && BehaviorCollector.this.behaviorContext != null && (packageManager = BehaviorCollector.this.behaviorContext.getPackageManager()) != null && (pkgNames = packageManager.getPackagesForUid(uid)) != null && pkgNames.length > 0) {
                String pkgName = pkgNames[0];
                if (BehaviorCollector.this.behaviorCache.containsKey(pkgName)) {
                    synchronized (BehaviorCollector.this) {
                        BehaviorCollector.this.behaviorCache.remove(pkgName);
                        String str = BehaviorCollector.TAG;
                        Log.i(str, "remove dead package " + pkgName);
                        if (BehaviorCollector.this.behaviorCache.size() == 0) {
                            BehaviorCollector.this.releaseResource();
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class BehaviorPointerEventListener extends PointerEventListenerEx {
        BehaviorPointerEventListener() {
        }

        public void onPointerEvent(MotionEvent event) {
            int action = event.getAction();
            if (action == 0) {
                BehaviorCollector.this.handleDownEvent(event);
            } else if (action == 1) {
                BehaviorCollector.this.handleUpEvent(event);
            } else if (action != 2) {
                String str = BehaviorCollector.TAG;
                Log.i(str, "unknown motion event " + event);
            } else {
                BehaviorCollector.this.handleMoveEvent(event);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class BehaviorSensorEventListener implements SensorEventListener {
        BehaviorSensorEventListener() {
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (BehaviorCollector.this.sensorDatas.size() < BehaviorCollector.MAX_SENSOR_DATA_COUNT) {
                BehaviorCollector.this.sensorDatas.add(new SensorData(sensorEvent.timestamp, sensorEvent.sensor.getType(), BigDecimal.valueOf((double) sensorEvent.values[0]).setScale(4, 4).doubleValue(), BigDecimal.valueOf((double) sensorEvent.values[1]).setScale(4, 4).doubleValue(), BigDecimal.valueOf((double) sensorEvent.values[2]).setScale(4, 4).doubleValue()));
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
