package ohos.sensor.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;
import ohos.sensor.bean.CategoryOrientation;
import ohos.sensor.bean.CoreOrientation;
import ohos.sensor.bean.SensorBean;
import ohos.sensor.data.CategoryOrientationData;
import ohos.sensor.data.CoreOrientationData;
import ohos.sensor.listener.CoreOrientationDataCallback;
import ohos.sensor.listener.ICategoryOrientationDataCallback;
import ohos.sensor.manager.SensorCore;

public class CategoryOrientationAgent extends SensorAgent<CategoryOrientation, CategoryOrientationData, ICategoryOrientationDataCallback> {
    private static final Map<ICategoryOrientationDataCallback, CoreOrientationDataCallback> CALLBACK_MAP = new ConcurrentHashMap();
    private static final Map<ICategoryOrientationDataCallback, List<Integer>> CALLBACK_ORIENTATION_SENSOR_ID = new ConcurrentHashMap();
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218113824, "CategoryOrientationAgent");
    private final CategoryOrientationAdapter adapter = new CategoryOrientationAdapter();

    @Override // ohos.sensor.agent.SensorAgent
    public List<CategoryOrientation> getAllSensors() {
        HiTraceId begin = HiTrace.begin("getAllSensors", 1);
        List<SensorBean> allSensors = SensorCore.getInstance().getAllSensors(2);
        HiTrace.end(begin);
        return this.adapter.convertCoreToCategoryList(allSensors);
    }

    @Override // ohos.sensor.agent.SensorAgent
    public List<CategoryOrientation> getAllSensors(int i) {
        HiTraceId begin = HiTrace.begin("getAllSensors", 1);
        List<SensorBean> allSensors = SensorCore.getInstance().getAllSensors(2, i);
        HiTrace.end(begin);
        return this.adapter.convertCoreToCategoryList(allSensors);
    }

    @Override // ohos.sensor.agent.SensorAgent
    public CategoryOrientation getSingleSensor(int i) {
        HiTraceId begin = HiTrace.begin("getSingleSensor", 1);
        SensorBean singleSensor = SensorCore.getInstance().getSingleSensor(2, i);
        HiTrace.end(begin);
        if (singleSensor != null) {
            return this.adapter.convertCoreToCategory(singleSensor);
        }
        return null;
    }

    public boolean setSensorDataCallback(ICategoryOrientationDataCallback iCategoryOrientationDataCallback, CategoryOrientation categoryOrientation, long j) {
        return setSensorDataCallback(iCategoryOrientationDataCallback, categoryOrientation, j, 0L);
    }

    public boolean setSensorDataCallback(ICategoryOrientationDataCallback iCategoryOrientationDataCallback, CategoryOrientation categoryOrientation, long j, long j2) {
        CoreOrientationDataCallback coreOrientationDataCallback;
        if (!subscribeParamsCheck(iCategoryOrientationDataCallback, categoryOrientation, j, j2)) {
            return false;
        }
        List<Integer> list = CALLBACK_ORIENTATION_SENSOR_ID.get(iCategoryOrientationDataCallback);
        if (list != null && list.contains(Integer.valueOf(categoryOrientation.getSensorId()))) {
            return true;
        }
        if (CALLBACK_MAP.get(iCategoryOrientationDataCallback) != null) {
            HiLog.info(LABEL, "setSensorDataCallback get the core callback from cache", new Object[0]);
            coreOrientationDataCallback = CALLBACK_MAP.get(iCategoryOrientationDataCallback);
        } else {
            HiLog.info(LABEL, "setSensorDataCallback new core callback", new Object[0]);
            coreOrientationDataCallback = new CoreOrientationDataCallbackImpl(iCategoryOrientationDataCallback, this.adapter);
        }
        SensorBean convertCategoryToCore = this.adapter.convertCategoryToCore(categoryOrientation);
        HiTraceId begin = HiTrace.begin("setSensorDataCallback", 1);
        boolean sensorDataCallback = SensorCore.getInstance().setSensorDataCallback(coreOrientationDataCallback, convertCategoryToCore, j, j2);
        HiTrace.end(begin);
        if (!sensorDataCallback) {
            HiLog.error(LABEL, "Set the orientation sensorData callback is failed", new Object[0]);
            return false;
        }
        CALLBACK_MAP.putIfAbsent(iCategoryOrientationDataCallback, coreOrientationDataCallback);
        if (CALLBACK_ORIENTATION_SENSOR_ID.get(iCategoryOrientationDataCallback) == null) {
            CALLBACK_ORIENTATION_SENSOR_ID.put(iCategoryOrientationDataCallback, new ArrayList());
        }
        CALLBACK_ORIENTATION_SENSOR_ID.get(iCategoryOrientationDataCallback).add(Integer.valueOf(categoryOrientation.getSensorId()));
        return true;
    }

    public boolean releaseSensorDataCallback(ICategoryOrientationDataCallback iCategoryOrientationDataCallback, CategoryOrientation categoryOrientation) {
        if (iCategoryOrientationDataCallback == null) {
            HiLog.error(LABEL, "releaseSensorDataCallback callback cannot be null", new Object[0]);
            return false;
        } else if (categoryOrientation == null) {
            return releaseSensorDataCallback(iCategoryOrientationDataCallback);
        } else {
            if (CALLBACK_MAP.get(iCategoryOrientationDataCallback) == null) {
                HiLog.error(LABEL, "callback is invalid releaseSensorDataCallback failed", new Object[0]);
                return false;
            }
            SensorBean convertCategoryToCore = this.adapter.convertCategoryToCore(categoryOrientation);
            HiTraceId begin = HiTrace.begin("releaseSensorDataCallback", 1);
            boolean releaseSensorDataCallback = SensorCore.getInstance().releaseSensorDataCallback(CALLBACK_MAP.get(iCategoryOrientationDataCallback), convertCategoryToCore);
            HiTrace.end(begin);
            if (!releaseSensorDataCallback) {
                HiLog.error(LABEL, "Release the orientation sensorData callback is failed", new Object[0]);
                return false;
            }
            List<Integer> list = CALLBACK_ORIENTATION_SENSOR_ID.get(iCategoryOrientationDataCallback);
            if (list != null && !list.isEmpty()) {
                list.remove(Integer.valueOf(categoryOrientation.getSensorId()));
            }
            if (list == null || list.isEmpty()) {
                CALLBACK_ORIENTATION_SENSOR_ID.remove(iCategoryOrientationDataCallback);
                CALLBACK_MAP.remove(iCategoryOrientationDataCallback);
            }
            return true;
        }
    }

    public boolean releaseSensorDataCallback(ICategoryOrientationDataCallback iCategoryOrientationDataCallback) {
        if (iCategoryOrientationDataCallback == null) {
            HiLog.error(LABEL, "releaseSensorDataCallback callback cannot be null", new Object[0]);
            return false;
        } else if (CALLBACK_MAP.get(iCategoryOrientationDataCallback) == null) {
            HiLog.error(LABEL, "callback is invalid releaseSensorDataCallback failed", new Object[0]);
            return false;
        } else {
            CALLBACK_ORIENTATION_SENSOR_ID.remove(iCategoryOrientationDataCallback);
            HiTraceId begin = HiTrace.begin("releaseSensorDataCallback", 1);
            boolean releaseSensorDataCallback = SensorCore.getInstance().releaseSensorDataCallback(CALLBACK_MAP.remove(iCategoryOrientationDataCallback), null);
            HiTrace.end(begin);
            if (releaseSensorDataCallback) {
                return true;
            }
            HiLog.error(LABEL, "Dispatch release the orientation sensorData callback is failed", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static final class CoreOrientationDataCallbackImpl implements CoreOrientationDataCallback {
        private CategoryOrientationAdapter adapter;
        private ICategoryOrientationDataCallback callback;

        public CoreOrientationDataCallbackImpl(ICategoryOrientationDataCallback iCategoryOrientationDataCallback, CategoryOrientationAdapter categoryOrientationAdapter) {
            this.callback = iCategoryOrientationDataCallback;
            this.adapter = categoryOrientationAdapter;
        }

        public void onSensorDataModified(CoreOrientationData coreOrientationData) {
            this.callback.onSensorDataModified(this.adapter.convertCoreToCategoryData(coreOrientationData));
        }

        public void onAccuracyDataModified(CoreOrientation coreOrientation, int i) {
            this.callback.onAccuracyDataModified(this.adapter.convertCoreToCategory(coreOrientation), i);
        }

        public void onCommandCompleted(CoreOrientation coreOrientation) {
            this.callback.onCommandCompleted(this.adapter.convertCoreToCategory(coreOrientation));
        }
    }

    /* access modifiers changed from: private */
    public static final class CategoryOrientationAdapter {
        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private List<CategoryOrientation> convertCoreToCategoryList(List<SensorBean> list) {
            ArrayList arrayList = new ArrayList();
            for (SensorBean sensorBean : list) {
                if (sensorBean instanceof CoreOrientation) {
                    arrayList.add(convertCoreToCategory((CoreOrientation) sensorBean));
                }
            }
            return arrayList;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryOrientation convertCoreToCategory(SensorBean sensorBean) {
            if (sensorBean instanceof CoreOrientation) {
                return convertCoreToCategory((CoreOrientation) sensorBean);
            }
            return null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryOrientation convertCoreToCategory(CoreOrientation coreOrientation) {
            if (coreOrientation == null) {
                return null;
            }
            return new CategoryOrientation(coreOrientation.getSensorId(), coreOrientation.getName(), coreOrientation.getVendor(), coreOrientation.getVersion(), coreOrientation.getUpperRange(), coreOrientation.getResolution(), coreOrientation.getFlags(), coreOrientation.getCacheMaxCount(), coreOrientation.getMinInterval(), coreOrientation.getMaxInterval());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private SensorBean convertCategoryToCore(CategoryOrientation categoryOrientation) {
            return new CoreOrientation(categoryOrientation.getSensorId(), categoryOrientation.getName(), categoryOrientation.getVendor(), categoryOrientation.getVersion(), categoryOrientation.getUpperRange(), categoryOrientation.getResolution(), categoryOrientation.getFlags(), categoryOrientation.getCacheMaxCount(), categoryOrientation.getMinInterval(), categoryOrientation.getMaxInterval());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryOrientationData convertCoreToCategoryData(CoreOrientationData coreOrientationData) {
            if (coreOrientationData == null) {
                return null;
            }
            return new CategoryOrientationData(convertCoreToCategory(coreOrientationData.getSensor()), coreOrientationData.getAccuracy(), coreOrientationData.getTimestamp(), coreOrientationData.getSensorDataDim(), coreOrientationData.getValues());
        }
    }
}
