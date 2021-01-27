package ohos.sensor.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;
import ohos.sensor.bean.CategoryLight;
import ohos.sensor.bean.CoreLight;
import ohos.sensor.bean.SensorBean;
import ohos.sensor.data.CategoryLightData;
import ohos.sensor.data.CoreLightData;
import ohos.sensor.listener.CoreLightDataCallback;
import ohos.sensor.listener.ICategoryLightDataCallback;
import ohos.sensor.manager.SensorCore;

public class CategoryLightAgent extends SensorAgent<CategoryLight, CategoryLightData, ICategoryLightDataCallback> {
    private static final Map<ICategoryLightDataCallback, List<Integer>> CALLBACK_LIGHT_SENSOR_ID = new ConcurrentHashMap();
    private static final Map<ICategoryLightDataCallback, CoreLightDataCallback> CALLBACK_MAP = new ConcurrentHashMap();
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218113824, "CategoryLightAgent");
    private final CategoryLightAdapter adapter = new CategoryLightAdapter();

    @Override // ohos.sensor.agent.SensorAgent
    public List<CategoryLight> getAllSensors() {
        HiTraceId begin = HiTrace.begin("getAllSensors", 1);
        List<SensorBean> allSensors = SensorCore.getInstance().getAllSensors(3);
        HiTrace.end(begin);
        return this.adapter.convertCoreToCategoryList(allSensors);
    }

    @Override // ohos.sensor.agent.SensorAgent
    public List<CategoryLight> getAllSensors(int i) {
        HiTraceId begin = HiTrace.begin("getAllSensors", 1);
        List<SensorBean> allSensors = SensorCore.getInstance().getAllSensors(3, i);
        HiTrace.end(begin);
        return this.adapter.convertCoreToCategoryList(allSensors);
    }

    @Override // ohos.sensor.agent.SensorAgent
    public CategoryLight getSingleSensor(int i) {
        HiTraceId begin = HiTrace.begin("getSingleSensor", 1);
        SensorBean singleSensor = SensorCore.getInstance().getSingleSensor(3, i);
        HiTrace.end(begin);
        if (singleSensor != null) {
            return this.adapter.convertCoreToCategory(singleSensor);
        }
        return null;
    }

    public boolean setSensorDataCallback(ICategoryLightDataCallback iCategoryLightDataCallback, CategoryLight categoryLight, long j) {
        return setSensorDataCallback(iCategoryLightDataCallback, categoryLight, j, 0L);
    }

    public boolean setSensorDataCallback(ICategoryLightDataCallback iCategoryLightDataCallback, CategoryLight categoryLight, long j, long j2) {
        CoreLightDataCallback coreLightDataCallback;
        if (!subscribeParamsCheck(iCategoryLightDataCallback, categoryLight, j, j2)) {
            return false;
        }
        List<Integer> list = CALLBACK_LIGHT_SENSOR_ID.get(iCategoryLightDataCallback);
        if (list != null && list.contains(Integer.valueOf(categoryLight.getSensorId()))) {
            return true;
        }
        if (CALLBACK_MAP.get(iCategoryLightDataCallback) != null) {
            HiLog.debug(LABEL, "setSensorDataCallback get the core callback from cache", new Object[0]);
            coreLightDataCallback = CALLBACK_MAP.get(iCategoryLightDataCallback);
        } else {
            HiLog.debug(LABEL, "setSensorDataCallback new core callback", new Object[0]);
            coreLightDataCallback = new CoreLightDataCallbackImpl(iCategoryLightDataCallback, this.adapter);
        }
        SensorBean convertCategoryToCore = this.adapter.convertCategoryToCore(categoryLight);
        HiTraceId begin = HiTrace.begin("setSensorDataCallback", 1);
        boolean sensorDataCallback = SensorCore.getInstance().setSensorDataCallback(coreLightDataCallback, convertCategoryToCore, j, j2);
        HiTrace.end(begin);
        if (!sensorDataCallback) {
            HiLog.error(LABEL, "setSensorDataCallback set the light sensorData callback failed", new Object[0]);
            return false;
        }
        CALLBACK_MAP.putIfAbsent(iCategoryLightDataCallback, coreLightDataCallback);
        if (CALLBACK_LIGHT_SENSOR_ID.get(iCategoryLightDataCallback) == null) {
            CALLBACK_LIGHT_SENSOR_ID.put(iCategoryLightDataCallback, new ArrayList());
        }
        CALLBACK_LIGHT_SENSOR_ID.get(iCategoryLightDataCallback).add(Integer.valueOf(categoryLight.getSensorId()));
        return true;
    }

    public boolean setSensorDataCallback(ICategoryLightDataCallback iCategoryLightDataCallback, CategoryLight categoryLight, int i) {
        return setSensorDataCallback(iCategoryLightDataCallback, categoryLight, i, 0L);
    }

    public boolean setSensorDataCallback(ICategoryLightDataCallback iCategoryLightDataCallback, CategoryLight categoryLight, int i, long j) {
        return setSensorDataCallback(iCategoryLightDataCallback, categoryLight, getInterval(i), j);
    }

    public boolean releaseSensorDataCallback(ICategoryLightDataCallback iCategoryLightDataCallback, CategoryLight categoryLight) {
        if (iCategoryLightDataCallback == null) {
            HiLog.error(LABEL, "releaseSensorDataCallback callback cannot be null", new Object[0]);
            return false;
        } else if (categoryLight == null) {
            return releaseSensorDataCallback(iCategoryLightDataCallback);
        } else {
            if (CALLBACK_MAP.get(iCategoryLightDataCallback) == null) {
                HiLog.error(LABEL, "releaseSensorDataCallback callback is invalid", new Object[0]);
                return false;
            }
            SensorBean convertCategoryToCore = this.adapter.convertCategoryToCore(categoryLight);
            HiTraceId begin = HiTrace.begin("releaseSensorDataCallback", 1);
            boolean releaseSensorDataCallback = SensorCore.getInstance().releaseSensorDataCallback(CALLBACK_MAP.get(iCategoryLightDataCallback), convertCategoryToCore);
            HiTrace.end(begin);
            if (!releaseSensorDataCallback) {
                HiLog.error(LABEL, "releaseSensorDataCallback release the light sensorData callback is failed", new Object[0]);
                return false;
            }
            List<Integer> list = CALLBACK_LIGHT_SENSOR_ID.get(iCategoryLightDataCallback);
            if (list != null && !list.isEmpty()) {
                list.remove(Integer.valueOf(categoryLight.getSensorId()));
            }
            if (list == null || list.isEmpty()) {
                CALLBACK_LIGHT_SENSOR_ID.remove(iCategoryLightDataCallback);
                CALLBACK_MAP.remove(iCategoryLightDataCallback);
            }
            return true;
        }
    }

    public boolean releaseSensorDataCallback(ICategoryLightDataCallback iCategoryLightDataCallback) {
        if (iCategoryLightDataCallback == null) {
            HiLog.error(LABEL, "releaseSensorDataCallback callback cannot be null", new Object[0]);
            return false;
        } else if (CALLBACK_MAP.get(iCategoryLightDataCallback) == null) {
            HiLog.error(LABEL, "releaseSensorDataCallback callback is invalid", new Object[0]);
            return false;
        } else {
            CALLBACK_LIGHT_SENSOR_ID.remove(iCategoryLightDataCallback);
            HiTraceId begin = HiTrace.begin("releaseSensorDataCallback", 1);
            boolean releaseSensorDataCallback = SensorCore.getInstance().releaseSensorDataCallback(CALLBACK_MAP.remove(iCategoryLightDataCallback), null);
            HiTrace.end(begin);
            if (releaseSensorDataCallback) {
                return true;
            }
            HiLog.error(LABEL, "releaseSensorDataCallback dispatch release the light sensorData callback is failed", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static final class CoreLightDataCallbackImpl implements CoreLightDataCallback {
        private CategoryLightAdapter adapter;
        private ICategoryLightDataCallback callback;

        public CoreLightDataCallbackImpl(ICategoryLightDataCallback iCategoryLightDataCallback, CategoryLightAdapter categoryLightAdapter) {
            this.callback = iCategoryLightDataCallback;
            this.adapter = categoryLightAdapter;
        }

        public void onSensorDataModified(CoreLightData coreLightData) {
            this.callback.onSensorDataModified(this.adapter.convertCoreToCategoryData(coreLightData));
        }

        public void onAccuracyDataModified(CoreLight coreLight, int i) {
            this.callback.onAccuracyDataModified(this.adapter.convertCoreToCategory(coreLight), i);
        }

        public void onCommandCompleted(CoreLight coreLight) {
            this.callback.onCommandCompleted(this.adapter.convertCoreToCategory(coreLight));
        }
    }

    /* access modifiers changed from: private */
    public static final class CategoryLightAdapter {
        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private List<CategoryLight> convertCoreToCategoryList(List<SensorBean> list) {
            ArrayList arrayList = new ArrayList();
            for (SensorBean sensorBean : list) {
                if (sensorBean instanceof CoreLight) {
                    arrayList.add(convertCoreToCategory(sensorBean));
                }
            }
            return arrayList;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryLight convertCoreToCategory(SensorBean sensorBean) {
            if (sensorBean instanceof CoreLight) {
                return convertCoreToCategory((CoreLight) sensorBean);
            }
            return null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryLight convertCoreToCategory(CoreLight coreLight) {
            if (coreLight == null) {
                return null;
            }
            return new CategoryLight(coreLight.getSensorId(), coreLight.getName(), coreLight.getVendor(), coreLight.getVersion(), coreLight.getUpperRange(), coreLight.getResolution(), coreLight.getFlags(), coreLight.getCacheMaxCount(), coreLight.getMinInterval(), coreLight.getMaxInterval());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private SensorBean convertCategoryToCore(CategoryLight categoryLight) {
            return new CoreLight(categoryLight.getSensorId(), categoryLight.getName(), categoryLight.getVendor(), categoryLight.getVersion(), categoryLight.getUpperRange(), categoryLight.getResolution(), categoryLight.getFlags(), categoryLight.getCacheMaxCount(), categoryLight.getMinInterval(), categoryLight.getMaxInterval());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private CategoryLightData convertCoreToCategoryData(CoreLightData coreLightData) {
            if (coreLightData == null) {
                return null;
            }
            return new CategoryLightData(convertCoreToCategory(coreLightData.getSensor()), coreLightData.getAccuracy(), coreLightData.getTimestamp(), coreLightData.getSensorDataDim(), coreLightData.getValues());
        }
    }
}
