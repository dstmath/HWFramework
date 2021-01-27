package ohos.sensor.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import ohos.global.icu.text.Bidi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.sensor.bean.CoreBody;
import ohos.sensor.bean.CoreEnvironment;
import ohos.sensor.bean.CoreLight;
import ohos.sensor.bean.CoreMotion;
import ohos.sensor.bean.CoreOrientation;
import ohos.sensor.bean.CoreOther;
import ohos.sensor.bean.SensorBean;
import ohos.sensor.bean.SensorEvent;
import ohos.sensor.data.CoreBodyData;
import ohos.sensor.data.CoreEnvironmentData;
import ohos.sensor.data.CoreLightData;
import ohos.sensor.data.CoreMotionData;
import ohos.sensor.data.CoreOrientationData;
import ohos.sensor.data.CoreOtherData;
import ohos.sensor.data.CoreSensorData;
import ohos.sensor.listener.CoreSensorDataCallback;

public class SubscribeManager {
    private static final int[] ACCURACY = {29, 2, 120, 0, 0, 0};
    private static final int CATEGORY = 63;
    private static final int CHANNEL_RES = 0;
    private static final int CLASS_INIT_RES = 0;
    private static final int COMMAND_COMPLETE_ID = 67371264;
    private static final int COMMAND_EXECUTE_FAILED = -1;
    private static final int COMMAND_PARAMETER_FLUSH = 0;
    private static final int COMMAND_PARAMETER_SET_MODE = 1;
    private static final int COMMAND_RESULT_OK = 0;
    private static final int COMMAND_TYPE_FLUSH = 0;
    private static final int COMMAND_TYPE_SET_MODE = 1;
    private static final int DESTROY_RESULT_OK = 0;
    private static final int DISABLE_RESULT_OK = 0;
    private static final int ENABLE_RESULT_OK = 0;
    private static final Object FIRST_INIT_LOCK = new Object();
    private static final int GET_SAMPLE_INTERVAL_FAILED = 0;
    private static final int SENSOR_CATEGORY_BODY = 5;
    private static final int SENSOR_CATEGORY_DEVICEMOTION = 0;
    private static final int SENSOR_CATEGORY_ENVIRONMENT = 1;
    private static final int SENSOR_CATEGORY_LIGHT = 3;
    private static final int SENSOR_CATEGORY_ORIENTATION = 2;
    private static final int SENSOR_CATEGORY_OTHER = 4;
    private static final Object SENSOR_INIT_LOCK = new Object();
    private static final Object SENSOR_LIST_LOCK = new Object();
    private static final int SHIFT_FLAG = 1;
    private static final SensorBean SINGLE_SENSOR = new SensorBean();
    private static final int STATE_INDEX_LATENCY = 1;
    private static final int STATE_INDEX_SAMPLING = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218113808, "SubscribeManager");
    private static final int[] TYPE = {1023, CATEGORY, Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT, CATEGORY, 15, 3};
    private static boolean hasInitNative = false;
    private static volatile SubscribeManager instance;
    private final Map<Integer, List<SensorBean>> allSensorWithCategory = new ConcurrentHashMap();
    private boolean channelFlag = false;
    private boolean hasGetListByNative = false;
    private final Map<Integer, List<SensorBean>> sensorBodyWithType = new ConcurrentHashMap();
    private final Map<Integer, List<SensorBean>> sensorEnvironmentWithType = new ConcurrentHashMap();
    private final Map<Integer, Integer> sensorIdAccuracy = new ConcurrentHashMap();
    private final Map<Integer, List<CoreSensorDataCallback>> sensorIdListener = new ConcurrentHashMap();
    private final Map<Integer, List<Long>> sensorIdParameter = new ConcurrentHashMap();
    private final Map<Integer, Integer> sensorIdState = new ConcurrentHashMap();
    private final Map<Integer, Long> sensorIdToSamplePeriod = new ConcurrentHashMap();
    private final Map<Integer, SensorBean> sensorIdToSensor = new ConcurrentHashMap();
    private final Map<Integer, List<SensorBean>> sensorLightWithType = new ConcurrentHashMap();
    private final Map<Integer, List<SensorBean>> sensorMotionWithType = new ConcurrentHashMap();
    private final Map<Integer, List<SensorBean>> sensorOrientationWithType = new ConcurrentHashMap();
    private final Map<Integer, List<SensorBean>> sensorOtherWithType = new ConcurrentHashMap();

    private static native int nativeClassInit();

    private static native int nativeCreateSensorChannel();

    private static native int nativeDestroySensorChannel();

    private static native int nativeDisableSensor(int i);

    private static native int nativeEnableSensor(int i, long j, long j2);

    private static native ArrayList<SensorBean> nativeGetAllSensor();

    private static native int nativeRunCommand(int i, int i2, int i3);

    private int parserGroup(int i) {
        return (-16777216 & i) >> 24;
    }

    private int parserType(int i) {
        return (16711680 & i) >> 16;
    }

    static {
        System.loadLibrary("sensor_jni.z");
    }

    private SubscribeManager() {
    }

    public static SubscribeManager getInstance() {
        if (instance == null) {
            HiLog.debug(TAG, "getInstance start native class load", new Object[0]);
            synchronized (SubscribeManager.class) {
                if (instance == null) {
                    instance = new SubscribeManager();
                }
            }
            HiLog.debug(TAG, "getInstance end native class load", new Object[0]);
        }
        return instance;
    }

    public boolean setSensorDataCallback(CoreSensorDataCallback coreSensorDataCallback, SensorBean sensorBean, long j, long j2) {
        int i;
        if (!isNativeInitSuccess() || coreSensorDataCallback == null || sensorBean == null || j < 0 || j2 < 0) {
            return false;
        }
        hasCreateChannel();
        int sensorId = sensorBean.getSensorId();
        updateListenerList(coreSensorDataCallback, sensorId);
        if (updateSensorIdParameter(sensorBean, sensorId, j, j2)) {
            i = enableSensor(sensorId);
            HiLog.debug(TAG, "setSensorDataCallback the parameter is true, the enable result is %{public}d", Integer.valueOf(i));
        } else if (this.sensorIdState.isEmpty() || !this.sensorIdState.containsKey(Integer.valueOf(sensorId)) || this.sensorIdState.get(Integer.valueOf(sensorId)).intValue() != 0) {
            i = enableSensor(sensorId);
        } else {
            HiLog.debug(TAG, "setSensorDataCallback the sensor has been enable!", new Object[0]);
            return true;
        }
        if (i != 0) {
            return false;
        }
        this.sensorIdState.put(Integer.valueOf(sensorId), Integer.valueOf(i));
        this.sensorIdToSensor.put(Integer.valueOf(sensorId), sensorBean);
        HiLog.debug(TAG, "setSensorDataCallback the subscribe result is : %{public}b", Integer.valueOf(i));
        return true;
    }

    private boolean updateSensorIdParameter(SensorBean sensorBean, int i, long j, long j2) {
        boolean z;
        long max = Math.max(j, sensorBean.getMinInterval());
        long max2 = Math.max(j2, 0L);
        if (this.sensorIdParameter.containsKey(Integer.valueOf(i))) {
            if (this.sensorIdParameter.get(Integer.valueOf(i)).get(0).longValue() != max) {
                this.sensorIdParameter.get(Integer.valueOf(i)).set(0, Long.valueOf(max));
                HiLog.debug(TAG, "updateSensorIdParameter the sensor's sampling update success!", new Object[0]);
                z = true;
            } else {
                z = false;
            }
            if (this.sensorIdParameter.get(Integer.valueOf(i)).get(1).longValue() == max2) {
                return z;
            }
            this.sensorIdParameter.get(Integer.valueOf(i)).set(1, Long.valueOf(max2));
            HiLog.debug(TAG, "updateSensorIdParameter the sensor's report latency update success!", new Object[0]);
            return true;
        }
        CopyOnWriteArrayList copyOnWriteArrayList = new CopyOnWriteArrayList();
        copyOnWriteArrayList.add(Long.valueOf(max));
        copyOnWriteArrayList.add(Long.valueOf(max2));
        this.sensorIdParameter.put(Integer.valueOf(i), copyOnWriteArrayList);
        HiLog.debug(TAG, "updateSensorIdParameter the sensor's parameter is added for the first time!", new Object[0]);
        return true;
    }

    private void updateListenerList(CoreSensorDataCallback coreSensorDataCallback, int i) {
        if (!this.sensorIdListener.containsKey(Integer.valueOf(i))) {
            CopyOnWriteArrayList copyOnWriteArrayList = new CopyOnWriteArrayList();
            copyOnWriteArrayList.add(coreSensorDataCallback);
            this.sensorIdListener.put(Integer.valueOf(i), copyOnWriteArrayList);
            HiLog.debug(TAG, "updateListenerList the sensor's listener is first add!", new Object[0]);
        } else if (!this.sensorIdListener.get(Integer.valueOf(i)).contains(coreSensorDataCallback)) {
            this.sensorIdListener.get(Integer.valueOf(i)).add(coreSensorDataCallback);
            HiLog.debug(TAG, "updateListenerList the sensor's listener is again add!", new Object[0]);
        }
    }

    private void hasCreateChannel() {
        if (this.channelFlag) {
            return;
        }
        if (nativeCreateSensorChannel() == 0) {
            HiLog.debug(TAG, "hasCreateChannel create channel success", new Object[0]);
            this.channelFlag = true;
            return;
        }
        HiLog.error(TAG, "hasCreateChannel create channel failed", new Object[0]);
    }

    private void hasDestroyChannel() {
        if (!this.sensorIdListener.isEmpty()) {
            return;
        }
        if (nativeDestroySensorChannel() == 0) {
            HiLog.debug(TAG, "hasDestroyChannel destroy channel success", new Object[0]);
            this.channelFlag = false;
            return;
        }
        HiLog.error(TAG, "hasDestroyChannel destroy channel failed", new Object[0]);
    }

    private int enableSensor(int i) {
        return nativeEnableSensor(i, this.sensorIdParameter.get(Integer.valueOf(i)).get(0).longValue(), this.sensorIdParameter.get(Integer.valueOf(i)).get(1).longValue());
    }

    private int disableSensor(int i) {
        int nativeDisableSensor = nativeDisableSensor(i);
        if (nativeDisableSensor == 0) {
            HiLog.debug(TAG, "disableSensor disable sensor success", new Object[0]);
            this.sensorIdState.remove(Integer.valueOf(i));
        } else {
            HiLog.error(TAG, "disableSensor disable sensor failed", new Object[0]);
        }
        return nativeDisableSensor;
    }

    public void processSensorData(SensorEvent sensorEvent) {
        int sensorId = sensorEvent.getSensorId();
        if (sensorId == COMMAND_COMPLETE_ID) {
            int i = sensorEvent.getReserved()[0];
            List<CoreSensorDataCallback> list = this.sensorIdListener.get(Integer.valueOf(i));
            CoreSensorData coreSensorData = new CoreSensorData();
            coreSensorData.setSensor(this.sensorIdToSensor.get(Integer.valueOf(i)));
            if (list != null && !list.isEmpty()) {
                for (CoreSensorDataCallback coreSensorDataCallback : list) {
                    coreSensorDataCallback.onCommandCompleted(coreSensorData.getSensor());
                }
            } else {
                return;
            }
        }
        SensorBean sensorBean = this.sensorIdToSensor.get(Integer.valueOf(sensorId));
        if (sensorBean != null) {
            int parserGroup = parserGroup(sensorId);
            int parserType = parserType(sensorId);
            if (!this.sensorIdListener.isEmpty()) {
                List<CoreSensorDataCallback> list2 = this.sensorIdListener.get(Integer.valueOf(sensorId));
                boolean hasAccuracyDemand = hasAccuracyDemand(parserGroup, parserType);
                CoreSensorData packageData = packageData(sensorBean, parserGroup, sensorEvent, hasAccuracyDemand);
                if (packageData == null) {
                    HiLog.debug(TAG, "processSensorData the sensor data is not exist", new Object[0]);
                } else {
                    dispatchData(sensorId, packageData, list2, hasAccuracyDemand);
                }
            }
        }
    }

    private boolean hasAccuracyDemand(int i, int i2) {
        if (i < 0) {
            return false;
        }
        int[] iArr = ACCURACY;
        return i < iArr.length && ((CATEGORY >> i) & 1) == 1 && ((iArr[i] >> i2) & 1) == 1;
    }

    private CoreSensorData packageData(SensorBean sensorBean, int i, SensorEvent sensorEvent, boolean z) {
        if (i == 0 && (sensorBean instanceof CoreMotion)) {
            return new CoreMotionData((CoreMotion) sensorBean, sensorEvent.getTimestamp(), sensorEvent.getData(), sensorEvent.getAccuracy(), z);
        }
        if (i == 1 && (sensorBean instanceof CoreEnvironment)) {
            return new CoreEnvironmentData((CoreEnvironment) sensorBean, sensorEvent.getTimestamp(), sensorEvent.getData(), sensorEvent.getAccuracy(), z);
        }
        if (i == 2 && (sensorBean instanceof CoreOrientation)) {
            return new CoreOrientationData((CoreOrientation) sensorBean, sensorEvent.getTimestamp(), sensorEvent.getData(), sensorEvent.getAccuracy(), z);
        }
        if (i == 3 && (sensorBean instanceof CoreLight)) {
            return new CoreLightData((CoreLight) sensorBean, sensorEvent.getTimestamp(), sensorEvent.getData(), sensorEvent.getAccuracy(), z);
        }
        if (i == 4 && (sensorBean instanceof CoreOther)) {
            return new CoreOtherData((CoreOther) sensorBean, sensorEvent.getTimestamp(), sensorEvent.getData(), sensorEvent.getAccuracy(), z);
        }
        if (i == 5 && (sensorBean instanceof CoreBody)) {
            return new CoreBodyData((CoreBody) sensorBean, sensorEvent.getTimestamp(), sensorEvent.getData(), sensorEvent.getAccuracy(), z);
        }
        HiLog.error(TAG, "packageData there is no sensor data!", new Object[0]);
        return null;
    }

    private void dispatchData(int i, CoreSensorData coreSensorData, List<CoreSensorDataCallback> list, boolean z) {
        if (!list.isEmpty()) {
            if (z) {
                if (!this.sensorIdAccuracy.containsKey(Integer.valueOf(i))) {
                    this.sensorIdAccuracy.put(Integer.valueOf(i), Integer.valueOf(coreSensorData.getAccuracy()));
                }
                if (this.sensorIdAccuracy.containsKey(Integer.valueOf(i)) && coreSensorData.getAccuracy() >= 0 && this.sensorIdAccuracy.get(Integer.valueOf(i)).intValue() != coreSensorData.getAccuracy()) {
                    for (CoreSensorDataCallback coreSensorDataCallback : list) {
                        coreSensorDataCallback.onAccuracyDataModified(coreSensorData.getSensor(), coreSensorData.getAccuracy());
                    }
                    this.sensorIdAccuracy.put(Integer.valueOf(i), Integer.valueOf(coreSensorData.getAccuracy()));
                }
            }
            for (CoreSensorDataCallback coreSensorDataCallback2 : list) {
                coreSensorDataCallback2.onSensorDataModified(coreSensorData);
            }
        }
    }

    public boolean releaseSensorDataCallback(CoreSensorDataCallback coreSensorDataCallback, SensorBean sensorBean) {
        if (!isNativeInitSuccess()) {
            return false;
        }
        if (coreSensorDataCallback == null) {
            HiLog.error(TAG, "releaseSensorDataCallback callback cannot be null", new Object[0]);
            return false;
        } else if (this.sensorIdListener.isEmpty()) {
            HiLog.error(TAG, "releaseSensorDataCallback sensorIdListener cannot be empty", new Object[0]);
            return true;
        } else if (sensorBean == null) {
            return releaseSensorDataCallback(coreSensorDataCallback);
        } else {
            int sensorId = sensorBean.getSensorId();
            if (this.sensorIdListener.get(Integer.valueOf(sensorId)) == null || this.sensorIdListener.get(Integer.valueOf(sensorId)).isEmpty()) {
                HiLog.error(TAG, "releaseSensorDataCallback sensorIdListener cannot be null or empty", new Object[0]);
                return true;
            } else if (disableSensor(sensorId) != 0) {
                return false;
            } else {
                this.sensorIdListener.get(Integer.valueOf(sensorId)).remove(coreSensorDataCallback);
                this.sensorIdToSensor.remove(Integer.valueOf(sensorId));
                if (this.sensorIdListener.get(Integer.valueOf(sensorId)).isEmpty()) {
                    this.sensorIdListener.remove(Integer.valueOf(sensorId));
                }
                hasDestroyChannel();
                return true;
            }
        }
    }

    public boolean releaseSensorDataCallback(CoreSensorDataCallback coreSensorDataCallback) {
        HiLog.debug(TAG, "releaseSensorDataCallback dispatch disable sensor", new Object[0]);
        int i = 0;
        for (Map.Entry<Integer, List<CoreSensorDataCallback>> entry : this.sensorIdListener.entrySet()) {
            int intValue = entry.getKey().intValue();
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                int i2 = i;
                for (int i3 = 0; i3 < entry.getValue().size(); i3++) {
                    if (entry.getValue().get(i3) == coreSensorDataCallback) {
                        i2 = disableSensor(intValue);
                        this.sensorIdListener.get(Integer.valueOf(intValue)).remove(coreSensorDataCallback);
                        this.sensorIdToSensor.remove(Integer.valueOf(intValue));
                    }
                }
                i = i2;
            }
        }
        hasDestroyChannel();
        if (i == 0) {
            return true;
        }
        return false;
    }

    public int runSensorCommand(int i, int i2, int i3) {
        if (!isNativeInitSuccess()) {
            return -1;
        }
        if (!this.sensorIdToSensor.containsKey(Integer.valueOf(i))) {
            HiLog.error(TAG, "runSensorCommand the sensor Id is not exist or sensor is not subscribed", new Object[0]);
            return -1;
        } else if (i2 != 0 && i2 != 1) {
            HiLog.error(TAG, "runSensorCommand the command type is invalid", new Object[0]);
            return -1;
        } else if (i3 != 0 && i3 != 1) {
            HiLog.error(TAG, "runSensorCommand the command parameter is invalid", new Object[0]);
            return -1;
        } else if (nativeRunCommand(i, i2, i3) != 0) {
            HiLog.error(TAG, "runSensorCommand run command is failed", new Object[0]);
            return -1;
        } else {
            HiLog.debug(TAG, "runSensorCommand run command is successful", new Object[0]);
            return 0;
        }
    }

    public List<SensorBean> getAllSensorsWithCategory(int i) {
        if (!isNativeInitSuccess()) {
            return Collections.emptyList();
        }
        if (!this.hasGetListByNative) {
            synchronized (SENSOR_INIT_LOCK) {
                init();
            }
            this.hasGetListByNative = true;
        }
        if (((CATEGORY >> i) & 1) != 1) {
            HiLog.error(TAG, "getAllSensorsWithCategory the sensor category is not exist!", new Object[0]);
            return Collections.emptyList();
        }
        List<SensorBean> list = this.allSensorWithCategory.get(Integer.valueOf(i));
        if (list != null) {
            return Collections.unmodifiableList(list);
        }
        HiLog.error(TAG, "getAllSensorsWithCategory the sensor category list cannot be null!", new Object[0]);
        return Collections.emptyList();
    }

    public List<SensorBean> getAllSensorsWithType(int i, int i2) {
        if (!isNativeInitSuccess()) {
            return Collections.emptyList();
        }
        if (!this.hasGetListByNative) {
            synchronized (SENSOR_INIT_LOCK) {
                init();
            }
            this.hasGetListByNative = true;
        }
        List<SensorBean> arrayList = new ArrayList<>();
        List<SensorBean> list = this.allSensorWithCategory.get(Integer.valueOf(i));
        synchronized (SENSOR_LIST_LOCK) {
            if (i == 0) {
                arrayList = updateMotionList(i2, list);
            } else if (i == 1) {
                arrayList = updateEnvironmentList(i2, list);
            } else if (i == 2) {
                arrayList = updateOrientationList(i2, list);
            } else if (i == 3) {
                arrayList = updateLightList(i2, list);
            } else if (i == 4) {
                arrayList = updateOtherList(i2, list);
            } else if (i != 5) {
                try {
                    HiLog.error(TAG, "getAllSensorsWithType the sensor category is not exist!", new Object[0]);
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                arrayList = updateBodyList(i2, list);
            }
        }
        return arrayList;
    }

    private boolean isNativeInitSuccess() {
        synchronized (FIRST_INIT_LOCK) {
            if (!hasInitNative) {
                if (nativeClassInit() == 0) {
                    hasInitNative = true;
                    HiLog.debug(TAG, "isNativeInitSuccess nativeClassInit success", new Object[0]);
                } else {
                    HiLog.error(TAG, "isNativeInitSuccess nativeClassInit failed", new Object[0]);
                    return false;
                }
            }
            return true;
        }
    }

    private List<SensorBean> updateMotionList(int i, List<SensorBean> list) {
        return getSensorListByType(i, list, 0, this.sensorMotionWithType);
    }

    private List<SensorBean> updateEnvironmentList(int i, List<SensorBean> list) {
        return getSensorListByType(i, list, 1, this.sensorEnvironmentWithType);
    }

    private List<SensorBean> updateOrientationList(int i, List<SensorBean> list) {
        return getSensorListByType(i, list, 2, this.sensorOrientationWithType);
    }

    private List<SensorBean> updateLightList(int i, List<SensorBean> list) {
        return getSensorListByType(i, list, 3, this.sensorLightWithType);
    }

    private List<SensorBean> updateOtherList(int i, List<SensorBean> list) {
        return getSensorListByType(i, list, 4, this.sensorOtherWithType);
    }

    private List<SensorBean> updateBodyList(int i, List<SensorBean> list) {
        return getSensorListByType(i, list, 5, this.sensorBodyWithType);
    }

    private List<SensorBean> getSensorListByType(int i, List<SensorBean> list, int i2, Map<Integer, List<SensorBean>> map) {
        if (i2 >= 0) {
            int[] iArr = TYPE;
            if (i2 < iArr.length && ((iArr[i2] >> i) & 1) != 1) {
                HiLog.error(TAG, "getSensorListByType the sensor type is not exist!", new Object[0]);
                return Collections.emptyList();
            }
        }
        List<SensorBean> list2 = map.get(Integer.valueOf(i));
        if (list2 != null) {
            return list2;
        }
        List<SensorBean> mySensorListByType = getMySensorListByType(list, i);
        map.put(Integer.valueOf(i), mySensorListByType);
        return mySensorListByType;
    }

    public SensorBean getSingleSensorWithType(int i, int i2) {
        if (!isNativeInitSuccess()) {
            return SINGLE_SENSOR;
        }
        if (((CATEGORY >> i) & 1) != 1) {
            HiLog.error(TAG, "getSingleSensorWithType the sensor category is not exist!", new Object[0]);
            return SINGLE_SENSOR;
        }
        if (i >= 0) {
            int[] iArr = TYPE;
            if (i < iArr.length && ((iArr[i] >> i2) & 1) != 1) {
                HiLog.error(TAG, "getSingleSensorWithType the sensor type is not exist!", new Object[0]);
                return SINGLE_SENSOR;
            }
        }
        List<SensorBean> allSensorsWithType = getAllSensorsWithType(i, i2);
        if (!allSensorsWithType.isEmpty()) {
            return allSensorsWithType.get(0);
        }
        HiLog.error(TAG, "getSingleSensorWithType the default sensor is not exist!", new Object[0]);
        return SINGLE_SENSOR;
    }

    public long getSensorMinSampleInterval(int i) {
        if (!isNativeInitSuccess()) {
            return 0;
        }
        if (!this.hasGetListByNative) {
            synchronized (SENSOR_INIT_LOCK) {
                init();
            }
            this.hasGetListByNative = true;
        }
        if (!this.sensorIdToSamplePeriod.isEmpty() && this.sensorIdToSamplePeriod.containsKey(Integer.valueOf(i))) {
            return this.sensorIdToSamplePeriod.get(Integer.valueOf(i)).longValue();
        }
        HiLog.error(TAG, "getSensorMinSampleInterval the Id of the sensor is not exist!", new Object[0]);
        return 0;
    }

    private void init() {
        initSensorCategory();
        ArrayList<SensorBean> nativeGetAllSensor = nativeGetAllSensor();
        if (nativeGetAllSensor != null) {
            for (SensorBean sensorBean : Collections.unmodifiableList(nativeGetAllSensor)) {
                int parserGroup = parserGroup(sensorBean.getSensorId());
                this.sensorIdToSamplePeriod.put(Integer.valueOf(sensorBean.getSensorId()), Long.valueOf(sensorBean.getMinInterval()));
                if (parserGroup == 0) {
                    this.allSensorWithCategory.get(0).add(setValue(sensorBean, new CoreMotion()));
                } else if (parserGroup == 1) {
                    this.allSensorWithCategory.get(1).add(setValue(sensorBean, new CoreEnvironment()));
                } else if (parserGroup == 2) {
                    this.allSensorWithCategory.get(2).add(setValue(sensorBean, new CoreOrientation()));
                } else if (parserGroup == 3) {
                    this.allSensorWithCategory.get(3).add(setValue(sensorBean, new CoreLight()));
                } else if (parserGroup == 4) {
                    this.allSensorWithCategory.get(4).add(setValue(sensorBean, new CoreOther()));
                } else if (parserGroup == 5) {
                    this.allSensorWithCategory.get(5).add(setValue(sensorBean, new CoreBody()));
                }
            }
        }
    }

    private void initSensorCategory() {
        this.allSensorWithCategory.put(0, new ArrayList());
        this.allSensorWithCategory.put(1, new ArrayList());
        this.allSensorWithCategory.put(2, new ArrayList());
        this.allSensorWithCategory.put(3, new ArrayList());
        this.allSensorWithCategory.put(4, new ArrayList());
        this.allSensorWithCategory.put(5, new ArrayList());
    }

    private SensorBean setValue(SensorBean sensorBean, SensorBean sensorBean2) {
        sensorBean2.setSensorId(sensorBean.getSensorId());
        sensorBean2.setName(sensorBean.getName());
        sensorBean2.setVendor(sensorBean.getVendor());
        sensorBean2.setVersion(sensorBean.getVersion());
        sensorBean2.setUpperRange(sensorBean.getUpperRange());
        sensorBean2.setResolution(sensorBean.getResolution());
        sensorBean2.setFlags(sensorBean.getFlags());
        sensorBean2.setCacheMaxCount(sensorBean.getCacheMaxCount());
        sensorBean2.setMinInterval(sensorBean.getMinInterval());
        sensorBean2.setMaxInterval(sensorBean.getMaxInterval());
        return sensorBean2;
    }

    private List<SensorBean> getMySensorListByType(List<SensorBean> list, int i) {
        ArrayList arrayList = new ArrayList();
        if (list == null) {
            return arrayList;
        }
        for (SensorBean sensorBean : list) {
            if (parserType(sensorBean.getSensorId()) == i) {
                arrayList.add(sensorBean);
            }
        }
        if (!arrayList.isEmpty()) {
            return Collections.unmodifiableList(arrayList);
        }
        HiLog.error(TAG, "getMySensorListByType the sensor type list cannot be empty", new Object[0]);
        return Collections.emptyList();
    }
}
