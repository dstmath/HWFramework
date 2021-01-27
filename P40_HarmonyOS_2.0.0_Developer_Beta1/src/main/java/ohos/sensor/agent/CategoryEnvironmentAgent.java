package ohos.sensor.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;
import ohos.sensor.bean.CategoryEnvironment;
import ohos.sensor.bean.CoreEnvironment;
import ohos.sensor.bean.SensorBean;
import ohos.sensor.data.CategoryEnvironmentData;
import ohos.sensor.data.CoreEnvironmentData;
import ohos.sensor.listener.CoreEnvironmentDataCallback;
import ohos.sensor.listener.ICategoryEnvironmentDataCallback;
import ohos.sensor.manager.SensorCore;

public class CategoryEnvironmentAgent extends SensorAgent<CategoryEnvironment, CategoryEnvironmentData, ICategoryEnvironmentDataCallback> {
    private static final Map<ICategoryEnvironmentDataCallback, List<Integer>> CALLBACK_ENVIRONMENT_SENSOR_ID = new ConcurrentHashMap();
    private static final Map<ICategoryEnvironmentDataCallback, CoreEnvironmentDataCallback> CALLBACK_MAP = new ConcurrentHashMap();
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218113824, "CategoryEnvironmentAgent");
    private final CategoryEnvironmentAdapter adapter = new CategoryEnvironmentAdapter();

    @Override // ohos.sensor.agent.SensorAgent
    public List<CategoryEnvironment> getAllSensors() {
        HiTraceId begin = HiTrace.begin("getAllSensors", 1);
        List<SensorBean> allSensors = SensorCore.getInstance().getAllSensors(1);
        HiTrace.end(begin);
        return this.adapter.convertCoreToCategoryList(allSensors);
    }

    @Override // ohos.sensor.agent.SensorAgent
    public List<CategoryEnvironment> getAllSensors(int i) {
        HiTraceId begin = HiTrace.begin("getAllSensors", 1);
        List<SensorBean> allSensors = SensorCore.getInstance().getAllSensors(1, i);
        HiTrace.end(begin);
        return this.adapter.convertCoreToCategoryList(allSensors);
    }

    @Override // ohos.sensor.agent.SensorAgent
    public CategoryEnvironment getSingleSensor(int i) {
        HiTraceId begin = HiTrace.begin("getSingleSensor", 1);
        SensorBean singleSensor = SensorCore.getInstance().getSingleSensor(1, i);
        HiTrace.end(begin);
        if (singleSensor != null) {
            return this.adapter.convertCoreToCategory(singleSensor);
        }
        return null;
    }

    public boolean setSensorDataCallback(ICategoryEnvironmentDataCallback iCategoryEnvironmentDataCallback, CategoryEnvironment categoryEnvironment, long j) {
        return setSensorDataCallback(iCategoryEnvironmentDataCallback, categoryEnvironment, j, 0L);
    }

    public boolean setSensorDataCallback(ICategoryEnvironmentDataCallback iCategoryEnvironmentDataCallback, CategoryEnvironment categoryEnvironment, long j, long j2) {
        CoreEnvironmentDataCallback coreEnvironmentDataCallback;
        if (!subscribeParamsCheck(iCategoryEnvironmentDataCallback, categoryEnvironment, j, j2)) {
            return false;
        }
        List<Integer> list = CALLBACK_ENVIRONMENT_SENSOR_ID.get(iCategoryEnvironmentDataCallback);
        if (list != null && list.contains(Integer.valueOf(categoryEnvironment.getSensorId()))) {
            return true;
        }
        if (CALLBACK_MAP.get(iCategoryEnvironmentDataCallback) != null) {
            HiLog.debug(LABEL, "setSensorDataCallback get the core callback from cache", new Object[0]);
            coreEnvironmentDataCallback = CALLBACK_MAP.get(iCategoryEnvironmentDataCallback);
        } else {
            HiLog.debug(LABEL, "setSensorDataCallback new core callback", new Object[0]);
            coreEnvironmentDataCallback = new CoreEnvironmentDataCallbackImpl(iCategoryEnvironmentDataCallback, this.adapter);
        }
        SensorBean convertCategoryToCore = this.adapter.convertCategoryToCore(categoryEnvironment);
        HiTraceId begin = HiTrace.begin("setSensorDataCallback", 1);
        boolean sensorDataCallback = SensorCore.getInstance().setSensorDataCallback(coreEnvironmentDataCallback, convertCategoryToCore, j, j2);
        HiTrace.end(begin);
        if (!sensorDataCallback) {
            HiLog.error(LABEL, "setSensorDataCallback set the environment sensorData callback failed", new Object[0]);
            return false;
        }
        CALLBACK_MAP.putIfAbsent(iCategoryEnvironmentDataCallback, coreEnvironmentDataCallback);
        if (CALLBACK_ENVIRONMENT_SENSOR_ID.get(iCategoryEnvironmentDataCallback) == null) {
            CALLBACK_ENVIRONMENT_SENSOR_ID.put(iCategoryEnvironmentDataCallback, new ArrayList());
        }
        CALLBACK_ENVIRONMENT_SENSOR_ID.get(iCategoryEnvironmentDataCallback).add(Integer.valueOf(categoryEnvironment.getSensorId()));
        return true;
    }

    public boolean setSensorDataCallback(ICategoryEnvironmentDataCallback iCategoryEnvironmentDataCallback, CategoryEnvironment categoryEnvironment, int i) {
        return setSensorDataCallback(iCategoryEnvironmentDataCallback, categoryEnvironment, i, 0L);
    }

    public boolean setSensorDataCallback(ICategoryEnvironmentDataCallback iCategoryEnvironmentDataCallback, CategoryEnvironment categoryEnvironment, int i, long j) {
        return setSensorDataCallback(iCategoryEnvironmentDataCallback, categoryEnvironment, getInterval(i), j);
    }

    public boolean releaseSensorDataCallback(ICategoryEnvironmentDataCallback iCategoryEnvironmentDataCallback, CategoryEnvironment categoryEnvironment) {
        if (iCategoryEnvironmentDataCallback == null) {
            HiLog.error(LABEL, "releaseSensorDataCallback callback cannot be null", new Object[0]);
            return false;
        } else if (categoryEnvironment == null) {
            return releaseSensorDataCallback(iCategoryEnvironmentDataCallback);
        } else {
            if (CALLBACK_MAP.get(iCategoryEnvironmentDataCallback) == null) {
                HiLog.error(LABEL, "callback is invalid releaseSensorDataCallback failed", new Object[0]);
                return false;
            }
            SensorBean convertCategoryToCore = this.adapter.convertCategoryToCore(categoryEnvironment);
            HiTraceId begin = HiTrace.begin("releaseSensorDataCallback", 1);
            boolean releaseSensorDataCallback = SensorCore.getInstance().releaseSensorDataCallback(CALLBACK_MAP.get(iCategoryEnvironmentDataCallback), convertCategoryToCore);
            HiTrace.end(begin);
            if (!releaseSensorDataCallback) {
                return false;
            }
            List<Integer> list = CALLBACK_ENVIRONMENT_SENSOR_ID.get(iCategoryEnvironmentDataCallback);
            if (list != null && !list.isEmpty()) {
                list.remove(Integer.valueOf(categoryEnvironment.getSensorId()));
            }
            if (list == null || list.isEmpty()) {
                CALLBACK_ENVIRONMENT_SENSOR_ID.remove(iCategoryEnvironmentDataCallback);
                CALLBACK_MAP.remove(iCategoryEnvironmentDataCallback);
            }
            return true;
        }
    }

    public boolean releaseSensorDataCallback(ICategoryEnvironmentDataCallback iCategoryEnvironmentDataCallback) {
        if (iCategoryEnvironmentDataCallback == null) {
            HiLog.error(LABEL, "releaseSensorDataCallback callback cannot be null", new Object[0]);
            return false;
        } else if (CALLBACK_MAP.get(iCategoryEnvironmentDataCallback) == null) {
            HiLog.error(LABEL, "callback is invalid releaseSensorDataCallback failed", new Object[0]);
            return false;
        } else {
            CALLBACK_ENVIRONMENT_SENSOR_ID.remove(iCategoryEnvironmentDataCallback);
            HiTraceId begin = HiTrace.begin("releaseSensorDataCallback", 1);
            boolean releaseSensorDataCallback = SensorCore.getInstance().releaseSensorDataCallback(CALLBACK_MAP.remove(iCategoryEnvironmentDataCallback), null);
            HiTrace.end(begin);
            if (releaseSensorDataCallback) {
                return true;
            }
            HiLog.error(LABEL, "Dispatch release the environment sensorData callback is failed", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static final class CoreEnvironmentDataCallbackImpl implements CoreEnvironmentDataCallback {
        private CategoryEnvironmentAdapter adapter;
        private ICategoryEnvironmentDataCallback callback;

        public CoreEnvironmentDataCallbackImpl(ICategoryEnvironmentDataCallback iCategoryEnvironmentDataCallback, CategoryEnvironmentAdapter categoryEnvironmentAdapter) {
            this.callback = iCategoryEnvironmentDataCallback;
            this.adapter = categoryEnvironmentAdapter;
        }

        public void onSensorDataModified(CoreEnvironmentData coreEnvironmentData) {
            this.callback.onSensorDataModified(this.adapter.convertCoreToCategoryData(coreEnvironmentData));
        }

        public void onAccuracyDataModified(CoreEnvironment coreEnvironment, int i) {
            this.callback.onAccuracyDataModified(this.adapter.convertCoreToCategory(coreEnvironment), i);
        }

        public void onCommandCompleted(CoreEnvironment coreEnvironment) {
            this.callback.onCommandCompleted(this.adapter.convertCoreToCategory(coreEnvironment));
        }
    }

    /* access modifiers changed from: private */
    public static final class CategoryEnvironmentAdapter {
        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private List<CategoryEnvironment> convertCoreToCategoryList(List<SensorBean> list) {
            ArrayList arrayList = new ArrayList();
            for (SensorBean sensorBean : list) {
                if (sensorBean instanceof CoreEnvironment) {
                    arrayList.add(convertCoreToCategory(sensorBean));
                }
            }
            return arrayList;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryEnvironment convertCoreToCategory(SensorBean sensorBean) {
            if (sensorBean instanceof CoreEnvironment) {
                return convertCoreToCategory((CoreEnvironment) sensorBean);
            }
            return null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryEnvironment convertCoreToCategory(CoreEnvironment coreEnvironment) {
            if (coreEnvironment == null) {
                return null;
            }
            return new CategoryEnvironment(coreEnvironment.getSensorId(), coreEnvironment.getName(), coreEnvironment.getVendor(), coreEnvironment.getVersion(), coreEnvironment.getUpperRange(), coreEnvironment.getResolution(), coreEnvironment.getFlags(), coreEnvironment.getCacheMaxCount(), coreEnvironment.getMinInterval(), coreEnvironment.getMaxInterval());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private SensorBean convertCategoryToCore(CategoryEnvironment categoryEnvironment) {
            return new CoreEnvironment(categoryEnvironment.getSensorId(), categoryEnvironment.getName(), categoryEnvironment.getVendor(), categoryEnvironment.getVersion(), categoryEnvironment.getUpperRange(), categoryEnvironment.getResolution(), categoryEnvironment.getFlags(), categoryEnvironment.getCacheMaxCount(), categoryEnvironment.getMinInterval(), categoryEnvironment.getMaxInterval());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryEnvironmentData convertCoreToCategoryData(CoreEnvironmentData coreEnvironmentData) {
            if (coreEnvironmentData == null) {
                return null;
            }
            return new CategoryEnvironmentData(convertCoreToCategory(coreEnvironmentData.getSensor()), coreEnvironmentData.getAccuracy(), coreEnvironmentData.getTimestamp(), coreEnvironmentData.getSensorDataDim(), coreEnvironmentData.getValues());
        }
    }
}
