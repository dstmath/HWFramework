package ohos.sensor.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;
import ohos.sensor.bean.CategoryMotion;
import ohos.sensor.bean.CoreMotion;
import ohos.sensor.bean.SensorBean;
import ohos.sensor.data.CategoryMotionData;
import ohos.sensor.data.CoreMotionData;
import ohos.sensor.listener.CoreMotionDataCallback;
import ohos.sensor.listener.ICategoryMotionDataCallback;
import ohos.sensor.manager.SensorCore;

public class CategoryMotionAgent extends SensorAgent<CategoryMotion, CategoryMotionData, ICategoryMotionDataCallback> {
    private static final Map<ICategoryMotionDataCallback, CoreMotionDataCallback> CALLBACK_MAP = new ConcurrentHashMap();
    private static final Map<ICategoryMotionDataCallback, List<Integer>> CALLBACK_MOTION_SENSOR_ID = new ConcurrentHashMap();
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218113824, "CategoryMotionAgent");
    private final CategoryMotionAdapter adapter = new CategoryMotionAdapter();

    @Override // ohos.sensor.agent.SensorAgent
    public List<CategoryMotion> getAllSensors() {
        HiTraceId begin = HiTrace.begin("getAllSensors", 1);
        List<SensorBean> allSensors = SensorCore.getInstance().getAllSensors(0);
        HiTrace.end(begin);
        return this.adapter.convertCoreToCategoryList(allSensors);
    }

    @Override // ohos.sensor.agent.SensorAgent
    public List<CategoryMotion> getAllSensors(int i) {
        HiTraceId begin = HiTrace.begin("getAllSensors", 1);
        List<SensorBean> allSensors = SensorCore.getInstance().getAllSensors(0, i);
        HiTrace.end(begin);
        return this.adapter.convertCoreToCategoryList(allSensors);
    }

    @Override // ohos.sensor.agent.SensorAgent
    public CategoryMotion getSingleSensor(int i) {
        HiTraceId begin = HiTrace.begin("getSingleSensor", 1);
        SensorBean singleSensor = SensorCore.getInstance().getSingleSensor(0, i);
        HiTrace.end(begin);
        if (singleSensor != null) {
            return this.adapter.convertCoreToCategory(singleSensor);
        }
        return null;
    }

    public boolean setSensorDataCallback(ICategoryMotionDataCallback iCategoryMotionDataCallback, CategoryMotion categoryMotion, long j) {
        return setSensorDataCallback(iCategoryMotionDataCallback, categoryMotion, j, 0L);
    }

    public boolean setSensorDataCallback(ICategoryMotionDataCallback iCategoryMotionDataCallback, CategoryMotion categoryMotion, long j, long j2) {
        CoreMotionDataCallback coreMotionDataCallback;
        if (!subscribeParamsCheck(iCategoryMotionDataCallback, categoryMotion, j, j2)) {
            return false;
        }
        List<Integer> list = CALLBACK_MOTION_SENSOR_ID.get(iCategoryMotionDataCallback);
        if (list != null && list.contains(Integer.valueOf(categoryMotion.getSensorId()))) {
            return true;
        }
        if (CALLBACK_MAP.get(iCategoryMotionDataCallback) != null) {
            HiLog.debug(LABEL, "setSensorDataCallback get the core callback from cache", new Object[0]);
            coreMotionDataCallback = CALLBACK_MAP.get(iCategoryMotionDataCallback);
        } else {
            HiLog.debug(LABEL, "setSensorDataCallback new core callback", new Object[0]);
            coreMotionDataCallback = new CoreMotionDataCallbackImpl(iCategoryMotionDataCallback, this.adapter);
        }
        SensorBean convertCategoryToCore = this.adapter.convertCategoryToCore(categoryMotion);
        HiTraceId begin = HiTrace.begin("setSensorDataCallback", 1);
        boolean sensorDataCallback = SensorCore.getInstance().setSensorDataCallback(coreMotionDataCallback, convertCategoryToCore, j, j2);
        HiTrace.end(begin);
        if (!sensorDataCallback) {
            HiLog.error(LABEL, "setSensorDataCallback set the motion sensorData callback failed", new Object[0]);
            return false;
        }
        CALLBACK_MAP.putIfAbsent(iCategoryMotionDataCallback, coreMotionDataCallback);
        if (CALLBACK_MOTION_SENSOR_ID.get(iCategoryMotionDataCallback) == null) {
            CALLBACK_MOTION_SENSOR_ID.put(iCategoryMotionDataCallback, new ArrayList());
        }
        CALLBACK_MOTION_SENSOR_ID.get(iCategoryMotionDataCallback).add(Integer.valueOf(categoryMotion.getSensorId()));
        return true;
    }

    public boolean setSensorDataCallback(ICategoryMotionDataCallback iCategoryMotionDataCallback, CategoryMotion categoryMotion, int i) {
        return setSensorDataCallback(iCategoryMotionDataCallback, categoryMotion, i, 0L);
    }

    public boolean setSensorDataCallback(ICategoryMotionDataCallback iCategoryMotionDataCallback, CategoryMotion categoryMotion, int i, long j) {
        return setSensorDataCallback(iCategoryMotionDataCallback, categoryMotion, getInterval(i), j);
    }

    public boolean releaseSensorDataCallback(ICategoryMotionDataCallback iCategoryMotionDataCallback, CategoryMotion categoryMotion) {
        if (iCategoryMotionDataCallback == null) {
            HiLog.error(LABEL, "releaseSensorDataCallback callback cannot be null", new Object[0]);
            return false;
        } else if (categoryMotion == null) {
            return releaseSensorDataCallback(iCategoryMotionDataCallback);
        } else {
            if (CALLBACK_MAP.get(iCategoryMotionDataCallback) == null) {
                HiLog.error(LABEL, "releaseSensorDataCallback callback is invalid", new Object[0]);
                return false;
            }
            SensorBean convertCategoryToCore = this.adapter.convertCategoryToCore(categoryMotion);
            HiTraceId begin = HiTrace.begin("releaseSensorDataCallback", 1);
            boolean releaseSensorDataCallback = SensorCore.getInstance().releaseSensorDataCallback(CALLBACK_MAP.get(iCategoryMotionDataCallback), convertCategoryToCore);
            HiTrace.end(begin);
            if (!releaseSensorDataCallback) {
                HiLog.error(LABEL, "releaseSensorDataCallback release the motion sensorData callback failed", new Object[0]);
                return false;
            }
            List<Integer> list = CALLBACK_MOTION_SENSOR_ID.get(iCategoryMotionDataCallback);
            if (list != null && !list.isEmpty()) {
                list.remove(Integer.valueOf(categoryMotion.getSensorId()));
            }
            if (list == null || list.isEmpty()) {
                CALLBACK_MOTION_SENSOR_ID.remove(iCategoryMotionDataCallback);
                CALLBACK_MAP.remove(iCategoryMotionDataCallback);
            }
            return true;
        }
    }

    public boolean releaseSensorDataCallback(ICategoryMotionDataCallback iCategoryMotionDataCallback) {
        if (iCategoryMotionDataCallback == null) {
            HiLog.error(LABEL, "releaseSensorDataCallback callback cannot be null", new Object[0]);
            return false;
        } else if (CALLBACK_MAP.get(iCategoryMotionDataCallback) == null) {
            HiLog.error(LABEL, "releaseSensorDataCallback callback is invalid", new Object[0]);
            return false;
        } else {
            CALLBACK_MOTION_SENSOR_ID.remove(iCategoryMotionDataCallback);
            HiTraceId begin = HiTrace.begin("releaseSensorDataCallback", 1);
            boolean releaseSensorDataCallback = SensorCore.getInstance().releaseSensorDataCallback(CALLBACK_MAP.remove(iCategoryMotionDataCallback), null);
            HiTrace.end(begin);
            if (releaseSensorDataCallback) {
                return true;
            }
            HiLog.error(LABEL, "releaseSensorDataCallback dispatch release the motion sensorData callback failed", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static final class CoreMotionDataCallbackImpl implements CoreMotionDataCallback {
        private final CategoryMotionAdapter adapter;
        private ICategoryMotionDataCallback callback;

        public CoreMotionDataCallbackImpl(ICategoryMotionDataCallback iCategoryMotionDataCallback, CategoryMotionAdapter categoryMotionAdapter) {
            this.callback = iCategoryMotionDataCallback;
            this.adapter = categoryMotionAdapter;
        }

        public void onSensorDataModified(CoreMotionData coreMotionData) {
            this.callback.onSensorDataModified(this.adapter.convertCoreToCategoryData(coreMotionData));
        }

        public void onAccuracyDataModified(CoreMotion coreMotion, int i) {
            this.callback.onAccuracyDataModified(this.adapter.convertCoreToCategory(coreMotion), i);
        }

        public void onCommandCompleted(CoreMotion coreMotion) {
            this.callback.onCommandCompleted(this.adapter.convertCoreToCategory(coreMotion));
        }
    }

    /* access modifiers changed from: private */
    public static final class CategoryMotionAdapter {
        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private List<CategoryMotion> convertCoreToCategoryList(List<SensorBean> list) {
            ArrayList arrayList = new ArrayList();
            for (SensorBean sensorBean : list) {
                if (sensorBean instanceof CoreMotion) {
                    arrayList.add(convertCoreToCategory((CoreMotion) sensorBean));
                }
            }
            return arrayList;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryMotion convertCoreToCategory(SensorBean sensorBean) {
            if (sensorBean instanceof CoreMotion) {
                return convertCoreToCategory((CoreMotion) sensorBean);
            }
            return null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryMotion convertCoreToCategory(CoreMotion coreMotion) {
            if (coreMotion == null) {
                return null;
            }
            return new CategoryMotion(coreMotion.getSensorId(), coreMotion.getName(), coreMotion.getVendor(), coreMotion.getVersion(), coreMotion.getUpperRange(), coreMotion.getResolution(), coreMotion.getFlags(), coreMotion.getCacheMaxCount(), coreMotion.getMinInterval(), coreMotion.getMaxInterval());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private SensorBean convertCategoryToCore(CategoryMotion categoryMotion) {
            return new CoreMotion(categoryMotion.getSensorId(), categoryMotion.getName(), categoryMotion.getVendor(), categoryMotion.getVersion(), categoryMotion.getUpperRange(), categoryMotion.getResolution(), categoryMotion.getFlags(), categoryMotion.getCacheMaxCount(), categoryMotion.getMinInterval(), categoryMotion.getMaxInterval());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryMotionData convertCoreToCategoryData(CoreMotionData coreMotionData) {
            if (coreMotionData == null) {
                return null;
            }
            return new CategoryMotionData(convertCoreToCategory(coreMotionData.getSensor()), coreMotionData.getAccuracy(), coreMotionData.getTimestamp(), coreMotionData.getSensorDataDim(), coreMotionData.getValues());
        }
    }
}
