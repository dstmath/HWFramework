package ohos.sensor.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;
import ohos.sensor.bean.CategoryBody;
import ohos.sensor.bean.CoreBody;
import ohos.sensor.bean.SensorBean;
import ohos.sensor.data.CategoryBodyData;
import ohos.sensor.data.CoreBodyData;
import ohos.sensor.listener.CoreBodyDataCallback;
import ohos.sensor.listener.ICategoryBodyDataCallback;
import ohos.sensor.manager.SensorCore;

public class CategoryBodyAgent extends SensorAgent<CategoryBody, CategoryBodyData, ICategoryBodyDataCallback> {
    private static final Map<ICategoryBodyDataCallback, List<Integer>> CALLBACK_BODY_SENSOR_ID = new ConcurrentHashMap();
    private static final Map<ICategoryBodyDataCallback, CoreBodyDataCallback> CALLBACK_MAP = new ConcurrentHashMap();
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218113824, "CategoryBodyAgent");
    private final CategoryBodyAdapter adapter = new CategoryBodyAdapter();

    @Override // ohos.sensor.agent.SensorAgent
    public List<CategoryBody> getAllSensors() {
        HiTraceId begin = HiTrace.begin("getAllSensors", 1);
        List<SensorBean> allSensors = SensorCore.getInstance().getAllSensors(5);
        HiTrace.end(begin);
        return this.adapter.convertCoreToCategoryList(allSensors);
    }

    @Override // ohos.sensor.agent.SensorAgent
    public List<CategoryBody> getAllSensors(int i) {
        HiTraceId begin = HiTrace.begin("getAllSensors", 1);
        List<SensorBean> allSensors = SensorCore.getInstance().getAllSensors(5, i);
        HiTrace.end(begin);
        return this.adapter.convertCoreToCategoryList(allSensors);
    }

    @Override // ohos.sensor.agent.SensorAgent
    public CategoryBody getSingleSensor(int i) {
        HiTraceId begin = HiTrace.begin("getSingleSensor", 1);
        SensorBean singleSensor = SensorCore.getInstance().getSingleSensor(5, i);
        HiTrace.end(begin);
        if (singleSensor != null) {
            return this.adapter.convertCoreToCategory(singleSensor);
        }
        return null;
    }

    public boolean setSensorDataCallback(ICategoryBodyDataCallback iCategoryBodyDataCallback, CategoryBody categoryBody, long j) {
        return setSensorDataCallback(iCategoryBodyDataCallback, categoryBody, j, 0L);
    }

    public boolean setSensorDataCallback(ICategoryBodyDataCallback iCategoryBodyDataCallback, CategoryBody categoryBody, long j, long j2) {
        CoreBodyDataCallback coreBodyDataCallback;
        if (!subscribeParamsCheck(iCategoryBodyDataCallback, categoryBody, j, j2)) {
            return false;
        }
        List<Integer> list = CALLBACK_BODY_SENSOR_ID.get(iCategoryBodyDataCallback);
        if (list != null && list.contains(Integer.valueOf(categoryBody.getSensorId()))) {
            return true;
        }
        if (CALLBACK_MAP.get(iCategoryBodyDataCallback) != null) {
            HiLog.info(LABEL, "setSensorDataCallback get the core callback from cache", new Object[0]);
            coreBodyDataCallback = CALLBACK_MAP.get(iCategoryBodyDataCallback);
        } else {
            HiLog.info(LABEL, "setSensorDataCallback new core callback", new Object[0]);
            coreBodyDataCallback = new CoreBodyDataCallbackImpl(iCategoryBodyDataCallback, this.adapter);
        }
        SensorBean convertCategoryToCore = this.adapter.convertCategoryToCore(categoryBody);
        HiTraceId begin = HiTrace.begin("setSensorDataCallback", 1);
        boolean sensorDataCallback = SensorCore.getInstance().setSensorDataCallback(coreBodyDataCallback, convertCategoryToCore, j, j2);
        HiTrace.end(begin);
        if (!sensorDataCallback) {
            HiLog.error(LABEL, "Set the body sensorData callback is failed", new Object[0]);
            return false;
        }
        CALLBACK_MAP.putIfAbsent(iCategoryBodyDataCallback, coreBodyDataCallback);
        if (CALLBACK_BODY_SENSOR_ID.get(iCategoryBodyDataCallback) == null) {
            CALLBACK_BODY_SENSOR_ID.put(iCategoryBodyDataCallback, new ArrayList());
        }
        CALLBACK_BODY_SENSOR_ID.get(iCategoryBodyDataCallback).add(Integer.valueOf(categoryBody.getSensorId()));
        return true;
    }

    public boolean releaseSensorDataCallback(ICategoryBodyDataCallback iCategoryBodyDataCallback, CategoryBody categoryBody) {
        if (iCategoryBodyDataCallback == null) {
            HiLog.error(LABEL, "releaseSensorDataCallback callback cannot be null", new Object[0]);
            return false;
        } else if (categoryBody == null) {
            return releaseSensorDataCallback(iCategoryBodyDataCallback);
        } else {
            if (CALLBACK_MAP.get(iCategoryBodyDataCallback) == null) {
                HiLog.error(LABEL, "callback is invalid releaseSensorDataCallback failed", new Object[0]);
                return false;
            }
            SensorBean convertCategoryToCore = this.adapter.convertCategoryToCore(categoryBody);
            HiTraceId begin = HiTrace.begin("releaseSensorDataCallback", 1);
            boolean releaseSensorDataCallback = SensorCore.getInstance().releaseSensorDataCallback(CALLBACK_MAP.get(iCategoryBodyDataCallback), convertCategoryToCore);
            HiTrace.end(begin);
            if (!releaseSensorDataCallback) {
                HiLog.error(LABEL, "Release the body sensorData callback is failed", new Object[0]);
                return false;
            }
            List<Integer> list = CALLBACK_BODY_SENSOR_ID.get(iCategoryBodyDataCallback);
            if (list != null && !list.isEmpty()) {
                list.remove(Integer.valueOf(categoryBody.getSensorId()));
            }
            if (list == null || list.isEmpty()) {
                CALLBACK_BODY_SENSOR_ID.remove(iCategoryBodyDataCallback);
                CALLBACK_MAP.remove(iCategoryBodyDataCallback);
            }
            return true;
        }
    }

    public boolean releaseSensorDataCallback(ICategoryBodyDataCallback iCategoryBodyDataCallback) {
        if (iCategoryBodyDataCallback == null) {
            HiLog.error(LABEL, "releaseSensorDataCallback callback cannot be null", new Object[0]);
            return false;
        } else if (CALLBACK_MAP.get(iCategoryBodyDataCallback) == null) {
            HiLog.error(LABEL, "callback is invalid releaseSensorDataCallback failed", new Object[0]);
            return false;
        } else {
            CALLBACK_BODY_SENSOR_ID.remove(iCategoryBodyDataCallback);
            HiTraceId begin = HiTrace.begin("releaseSensorDataCallback", 1);
            boolean releaseSensorDataCallback = SensorCore.getInstance().releaseSensorDataCallback(CALLBACK_MAP.remove(iCategoryBodyDataCallback), null);
            HiTrace.end(begin);
            if (releaseSensorDataCallback) {
                return true;
            }
            HiLog.error(LABEL, "Dispatch release the body sensorData callback is failed", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static final class CoreBodyDataCallbackImpl implements CoreBodyDataCallback {
        private CategoryBodyAdapter adapter;
        private ICategoryBodyDataCallback callback;

        public CoreBodyDataCallbackImpl(ICategoryBodyDataCallback iCategoryBodyDataCallback, CategoryBodyAdapter categoryBodyAdapter) {
            this.callback = iCategoryBodyDataCallback;
            this.adapter = categoryBodyAdapter;
        }

        public void onSensorDataModified(CoreBodyData coreBodyData) {
            this.callback.onSensorDataModified(this.adapter.convertCoreToCategoryData(coreBodyData));
        }

        public void onAccuracyDataModified(CoreBody coreBody, int i) {
            this.callback.onAccuracyDataModified(this.adapter.convertCoreToCategory(coreBody), i);
        }

        public void onCommandCompleted(CoreBody coreBody) {
            this.callback.onCommandCompleted(this.adapter.convertCoreToCategory(coreBody));
        }
    }

    /* access modifiers changed from: private */
    public static final class CategoryBodyAdapter {
        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private List<CategoryBody> convertCoreToCategoryList(List<SensorBean> list) {
            ArrayList arrayList = new ArrayList();
            for (SensorBean sensorBean : list) {
                if (sensorBean instanceof CoreBody) {
                    arrayList.add(convertCoreToCategory(sensorBean));
                }
            }
            return arrayList;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryBody convertCoreToCategory(SensorBean sensorBean) {
            if (sensorBean instanceof CoreBody) {
                return convertCoreToCategory((CoreBody) sensorBean);
            }
            return null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryBody convertCoreToCategory(CoreBody coreBody) {
            if (coreBody == null) {
                return null;
            }
            return new CategoryBody(coreBody.getSensorId(), coreBody.getName(), coreBody.getVendor(), coreBody.getVersion(), coreBody.getUpperRange(), coreBody.getResolution(), coreBody.getFlags(), coreBody.getCacheMaxCount(), coreBody.getMinInterval(), coreBody.getMaxInterval());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private SensorBean convertCategoryToCore(CategoryBody categoryBody) {
            return new CoreBody(categoryBody.getSensorId(), categoryBody.getName(), categoryBody.getVendor(), categoryBody.getVersion(), categoryBody.getUpperRange(), categoryBody.getResolution(), categoryBody.getFlags(), categoryBody.getCacheMaxCount(), categoryBody.getMinInterval(), categoryBody.getMaxInterval());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryBodyData convertCoreToCategoryData(CoreBodyData coreBodyData) {
            if (coreBodyData == null) {
                return null;
            }
            return new CategoryBodyData(convertCoreToCategory(coreBodyData.getSensor()), coreBodyData.getAccuracy(), coreBodyData.getTimestamp(), coreBodyData.getSensorDataDim(), coreBodyData.getValues());
        }
    }
}
