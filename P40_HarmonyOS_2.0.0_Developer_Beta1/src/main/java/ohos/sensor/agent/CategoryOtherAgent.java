package ohos.sensor.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;
import ohos.sensor.bean.CategoryOther;
import ohos.sensor.bean.CoreOther;
import ohos.sensor.bean.SensorBean;
import ohos.sensor.data.CategoryOtherData;
import ohos.sensor.data.CoreOtherData;
import ohos.sensor.listener.CoreOtherDataCallback;
import ohos.sensor.listener.ICategoryOtherDataCallback;
import ohos.sensor.manager.SensorCore;

public class CategoryOtherAgent extends SensorAgent<CategoryOther, CategoryOtherData, ICategoryOtherDataCallback> {
    private static final Map<ICategoryOtherDataCallback, CoreOtherDataCallback> CALLBACK_MAP = new ConcurrentHashMap();
    private static final Map<ICategoryOtherDataCallback, List<Integer>> CALLBACK_OTHER_SENSOR_ID = new ConcurrentHashMap();
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218113824, "CategoryOtherAgent");
    private final CategoryOtherAdapter adapter = new CategoryOtherAdapter();

    @Override // ohos.sensor.agent.SensorAgent
    public List<CategoryOther> getAllSensors() {
        HiTraceId begin = HiTrace.begin("getAllSensors", 1);
        List<SensorBean> allSensors = SensorCore.getInstance().getAllSensors(4);
        HiTrace.end(begin);
        return this.adapter.convertCoreToCategoryList(allSensors);
    }

    @Override // ohos.sensor.agent.SensorAgent
    public List<CategoryOther> getAllSensors(int i) {
        HiTraceId begin = HiTrace.begin("getAllSensors", 1);
        List<SensorBean> allSensors = SensorCore.getInstance().getAllSensors(4, i);
        HiTrace.end(begin);
        return this.adapter.convertCoreToCategoryList(allSensors);
    }

    @Override // ohos.sensor.agent.SensorAgent
    public CategoryOther getSingleSensor(int i) {
        HiTraceId begin = HiTrace.begin("getSingleSensor", 1);
        SensorBean singleSensor = SensorCore.getInstance().getSingleSensor(4, i);
        HiTrace.end(begin);
        if (singleSensor != null) {
            return this.adapter.convertCoreToCategory(singleSensor);
        }
        return null;
    }

    public boolean setSensorDataCallback(ICategoryOtherDataCallback iCategoryOtherDataCallback, CategoryOther categoryOther, long j) {
        return setSensorDataCallback(iCategoryOtherDataCallback, categoryOther, j, 0L);
    }

    public boolean setSensorDataCallback(ICategoryOtherDataCallback iCategoryOtherDataCallback, CategoryOther categoryOther, long j, long j2) {
        CoreOtherDataCallback coreOtherDataCallback;
        if (!subscribeParamsCheck(iCategoryOtherDataCallback, categoryOther, j, j2)) {
            return false;
        }
        List<Integer> list = CALLBACK_OTHER_SENSOR_ID.get(iCategoryOtherDataCallback);
        if (list != null && list.contains(Integer.valueOf(categoryOther.getSensorId()))) {
            return true;
        }
        if (CALLBACK_MAP.get(iCategoryOtherDataCallback) != null) {
            HiLog.debug(LABEL, "setSensorDataCallback get the core callback from cache", new Object[0]);
            coreOtherDataCallback = CALLBACK_MAP.get(iCategoryOtherDataCallback);
        } else {
            HiLog.debug(LABEL, "setSensorDataCallback new core callback", new Object[0]);
            coreOtherDataCallback = new CoreOtherDataCallbackImpl(iCategoryOtherDataCallback, this.adapter);
        }
        SensorBean convertCategoryToCore = this.adapter.convertCategoryToCore(categoryOther);
        HiTraceId begin = HiTrace.begin("setSensorDataCallback", 1);
        boolean sensorDataCallback = SensorCore.getInstance().setSensorDataCallback(coreOtherDataCallback, convertCategoryToCore, j, j2);
        HiTrace.end(begin);
        if (!sensorDataCallback) {
            HiLog.error(LABEL, "setSensorDataCallback set the other sensorData callback is failed", new Object[0]);
            return false;
        }
        CALLBACK_MAP.putIfAbsent(iCategoryOtherDataCallback, coreOtherDataCallback);
        if (CALLBACK_OTHER_SENSOR_ID.get(iCategoryOtherDataCallback) == null) {
            CALLBACK_OTHER_SENSOR_ID.put(iCategoryOtherDataCallback, new ArrayList());
        }
        CALLBACK_OTHER_SENSOR_ID.get(iCategoryOtherDataCallback).add(Integer.valueOf(categoryOther.getSensorId()));
        return true;
    }

    public boolean setSensorDataCallback(ICategoryOtherDataCallback iCategoryOtherDataCallback, CategoryOther categoryOther, int i) {
        return setSensorDataCallback(iCategoryOtherDataCallback, categoryOther, i, 0L);
    }

    public boolean setSensorDataCallback(ICategoryOtherDataCallback iCategoryOtherDataCallback, CategoryOther categoryOther, int i, long j) {
        return setSensorDataCallback(iCategoryOtherDataCallback, categoryOther, getInterval(i), j);
    }

    public boolean releaseSensorDataCallback(ICategoryOtherDataCallback iCategoryOtherDataCallback, CategoryOther categoryOther) {
        if (iCategoryOtherDataCallback == null) {
            HiLog.error(LABEL, "releaseSensorDataCallback callback cannot be null", new Object[0]);
            return false;
        } else if (categoryOther == null) {
            return releaseSensorDataCallback(iCategoryOtherDataCallback);
        } else {
            if (CALLBACK_MAP.get(iCategoryOtherDataCallback) == null) {
                HiLog.error(LABEL, "releaseSensorDataCallback callback is invalid", new Object[0]);
                return false;
            }
            SensorBean convertCategoryToCore = this.adapter.convertCategoryToCore(categoryOther);
            HiTraceId begin = HiTrace.begin("releaseSensorDataCallback", 1);
            boolean releaseSensorDataCallback = SensorCore.getInstance().releaseSensorDataCallback(CALLBACK_MAP.get(iCategoryOtherDataCallback), convertCategoryToCore);
            HiTrace.end(begin);
            if (!releaseSensorDataCallback) {
                HiLog.error(LABEL, "releaseSensorDataCallback release the other sensorData callback failed", new Object[0]);
                return false;
            }
            List<Integer> list = CALLBACK_OTHER_SENSOR_ID.get(iCategoryOtherDataCallback);
            if (list != null && !list.isEmpty()) {
                list.remove(Integer.valueOf(categoryOther.getSensorId()));
            }
            if (list == null || list.isEmpty()) {
                CALLBACK_OTHER_SENSOR_ID.remove(iCategoryOtherDataCallback);
                CALLBACK_MAP.remove(iCategoryOtherDataCallback);
            }
            return true;
        }
    }

    public boolean releaseSensorDataCallback(ICategoryOtherDataCallback iCategoryOtherDataCallback) {
        if (iCategoryOtherDataCallback == null) {
            HiLog.error(LABEL, "releaseSensorDataCallback callback cannot be null", new Object[0]);
            return false;
        } else if (CALLBACK_MAP.get(iCategoryOtherDataCallback) == null) {
            HiLog.error(LABEL, "releaseSensorDataCallback callback is invalid", new Object[0]);
            return false;
        } else {
            CALLBACK_OTHER_SENSOR_ID.remove(iCategoryOtherDataCallback);
            HiTraceId begin = HiTrace.begin("releaseSensorDataCallback", 1);
            boolean releaseSensorDataCallback = SensorCore.getInstance().releaseSensorDataCallback(CALLBACK_MAP.remove(iCategoryOtherDataCallback), null);
            HiTrace.end(begin);
            if (releaseSensorDataCallback) {
                return true;
            }
            HiLog.error(LABEL, "releaseSensorDataCallback dispatch release the other sensorData callback is failed", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static final class CoreOtherDataCallbackImpl implements CoreOtherDataCallback {
        private CategoryOtherAdapter adapter;
        private ICategoryOtherDataCallback callback;

        public CoreOtherDataCallbackImpl(ICategoryOtherDataCallback iCategoryOtherDataCallback, CategoryOtherAdapter categoryOtherAdapter) {
            this.callback = iCategoryOtherDataCallback;
            this.adapter = categoryOtherAdapter;
        }

        public void onSensorDataModified(CoreOtherData coreOtherData) {
            this.callback.onSensorDataModified(this.adapter.convertCoreToCategoryData(coreOtherData));
        }

        public void onAccuracyDataModified(CoreOther coreOther, int i) {
            this.callback.onAccuracyDataModified(this.adapter.convertCoreToCategory(coreOther), i);
        }

        public void onCommandCompleted(CoreOther coreOther) {
            this.callback.onCommandCompleted(this.adapter.convertCoreToCategory(coreOther));
        }
    }

    /* access modifiers changed from: private */
    public static final class CategoryOtherAdapter {
        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private List<CategoryOther> convertCoreToCategoryList(List<SensorBean> list) {
            ArrayList arrayList = new ArrayList();
            for (SensorBean sensorBean : list) {
                if (sensorBean instanceof CoreOther) {
                    arrayList.add(convertCoreToCategory((CoreOther) sensorBean));
                }
            }
            return arrayList;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryOther convertCoreToCategory(SensorBean sensorBean) {
            if (sensorBean instanceof CoreOther) {
                return convertCoreToCategory((CoreOther) sensorBean);
            }
            return null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryOther convertCoreToCategory(CoreOther coreOther) {
            return new CategoryOther(coreOther.getSensorId(), coreOther.getName(), coreOther.getVendor(), coreOther.getVersion(), coreOther.getUpperRange(), coreOther.getResolution(), coreOther.getFlags(), coreOther.getCacheMaxCount(), coreOther.getMinInterval(), coreOther.getMaxInterval());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private SensorBean convertCategoryToCore(CategoryOther categoryOther) {
            if (categoryOther == null) {
                return null;
            }
            return new CoreOther(categoryOther.getSensorId(), categoryOther.getName(), categoryOther.getVendor(), categoryOther.getVersion(), categoryOther.getUpperRange(), categoryOther.getResolution(), categoryOther.getFlags(), categoryOther.getCacheMaxCount(), categoryOther.getMinInterval(), categoryOther.getMaxInterval());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryOtherData convertCoreToCategoryData(CoreOtherData coreOtherData) {
            if (coreOtherData == null) {
                return null;
            }
            return new CategoryOtherData(convertCoreToCategory(coreOtherData.getSensor()), coreOtherData.getAccuracy(), coreOtherData.getTimestamp(), coreOtherData.getSensorDataDim(), coreOtherData.getValues());
        }
    }
}
