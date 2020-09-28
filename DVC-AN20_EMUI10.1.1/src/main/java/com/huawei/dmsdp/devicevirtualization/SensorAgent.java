package com.huawei.dmsdp.devicevirtualization;

import android.os.SystemClock;
import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.DMSDPListener;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.sensor.SensorData;
import com.huawei.dmsdpsdk2.sensor.SensorDataListener;
import com.huawei.dmsdpsdk2.sensor.VirtualSensor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SensorAgent extends VirtualManager {
    private static final String API_NAME_SUBSCRIBESENSOR = "subscribeSensorDataListener";
    private static final int FIVE_SECONDS = 5000;
    private static final Object SENSOR_LOCK = new Object();
    private static final String TAG = "VirtualSensorService";
    private static final int VIRTUAL_DEVICE_SIZE = 16;
    private static DMSDPListener mDmsdpListener = null;
    private DMSDPAdapter mDMSDPAdapter;
    private ConcurrentHashMap<String, VirtualSensorListenerInfo> mVirtualSensorDataListenerList;

    private SensorAgent() {
        this.mVirtualSensorDataListenerList = new ConcurrentHashMap<>(16);
    }

    /* access modifiers changed from: package-private */
    public static class VirtualSensorListenerInfo {
        private SensorDataListener mSensorDataListener;
        private IVirtualSensorDataListener mVirtualSensorDataListener;

        VirtualSensorListenerInfo(IVirtualSensorDataListener virtualSensorDataListener, SensorDataListener sensorDataListener) {
            this.mVirtualSensorDataListener = virtualSensorDataListener;
            this.mSensorDataListener = sensorDataListener;
        }

        public IVirtualSensorDataListener getVirtualSensorDataListener() {
            return this.mVirtualSensorDataListener;
        }

        public SensorDataListener getSensorDataListener() {
            return this.mSensorDataListener;
        }
    }

    private static final class InstanceHolder {
        private static final SensorAgent INSTANCE = new SensorAgent();

        private InstanceHolder() {
        }
    }

    public static SensorAgent getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public List<VirtualSensor> getSensorList(String deviceId, int sensorType) {
        synchronized (SENSOR_LOCK) {
            List<VirtualSensor> sensorList = new ArrayList<>();
            if (this.mDMSDPAdapter == null) {
                HwLog.e("VirtualSensorService", "getSensorList mDMSDPAdapter is null");
                return sensorList;
            } else if (deviceId == null) {
                HwLog.e("VirtualSensorService", "getSensorList deviceId is null");
                return sensorList;
            } else {
                List<VirtualSensor> getList = new ArrayList<>();
                int ret = this.mDMSDPAdapter.getSensorList(deviceId, sensorType, getList);
                if (ret != 0) {
                    HwLog.e("VirtualSensorService", "getSensorList err:" + ret);
                    return sensorList;
                }
                for (VirtualSensor tempSensor : getList) {
                    if (tempSensor != null) {
                        sensorList.add(new VirtualSensor(tempSensor.getDeviceId(), tempSensor.getSensorId(), sensorType));
                    }
                }
                return sensorList;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getListenerKey(String deviceId, int sensorId, int sensorType) {
        return deviceId + ":" + sensorId + ":" + sensorType;
    }

    /* access modifiers changed from: package-private */
    public SensorDataListener getKitSensorDataListener() {
        return new SensorDataListener() {
            /* class com.huawei.dmsdp.devicevirtualization.SensorAgent.AnonymousClass1 */

            @Override // com.huawei.dmsdpsdk2.sensor.SensorDataListener
            public void onSensorChanged(SensorData data) {
                if (data == null) {
                    HwLog.e("VirtualSensorService", "onSensorChanged data null");
                    return;
                }
                synchronized (SensorAgent.SENSOR_LOCK) {
                    if (data.getSensor() == null) {
                        HwLog.e("VirtualSensorService", "onSensorChanged getSensor null");
                        return;
                    }
                    VirtualSensor inputSensor = data.getSensor();
                    if (inputSensor.getDeviceId() == null) {
                        HwLog.e("VirtualSensorService", "onSensorChanged getDeviceId null");
                        return;
                    }
                    String listenerKey = SensorAgent.this.getListenerKey(inputSensor.getDeviceId(), inputSensor.getSensorId(), inputSensor.getSensorType());
                    VirtualSensorData virtualData = new VirtualSensorData(new VirtualSensor(inputSensor.getDeviceId(), inputSensor.getSensorId(), inputSensor.getSensorType()), data.getAccuracy(), data.getTimestamp(), data.getValues());
                    VirtualSensorListenerInfo sensorListenerInfo = (VirtualSensorListenerInfo) SensorAgent.this.mVirtualSensorDataListenerList.get(listenerKey);
                    if (sensorListenerInfo == null) {
                        HwLog.e("VirtualSensorService", "onSensorChanged sensorListenerInfo null");
                    } else if (sensorListenerInfo.getVirtualSensorDataListener() == null) {
                        HwLog.e("VirtualSensorService", "onSensorChanged getVirtualSensorDataListener null");
                    } else {
                        sensorListenerInfo.getVirtualSensorDataListener().onSensorChanged(virtualData);
                    }
                }
            }
        };
    }

    public int subscribeSensorDataListener(IVirtualSensorDataListener listener, VirtualSensor sensor) {
        synchronized (SENSOR_LOCK) {
            if (this.mDMSDPAdapter == null) {
                return -11;
            }
            long callTime = System.currentTimeMillis();
            long startTime = SystemClock.elapsedRealtime();
            if (listener != null) {
                if (sensor != null) {
                    if (sensor.getDeviceId() == null) {
                        this.mDMSDPAdapter.reportData(API_NAME_SUBSCRIBESENSOR, callTime, startTime, -2);
                        return -2;
                    }
                    String listenerKey = getListenerKey(sensor.getDeviceId(), sensor.getSensorId(), sensor.getSensorType());
                    SensorDataListener kitlistener = null;
                    VirtualSensorListenerInfo info = this.mVirtualSensorDataListenerList.get(listenerKey);
                    if (info != null) {
                        kitlistener = info.getSensorDataListener();
                    }
                    if (kitlistener == null) {
                        kitlistener = getKitSensorDataListener();
                    }
                    int ret = this.mDMSDPAdapter.subscribeSensorDataListener(kitlistener, new VirtualSensor(sensor.getDeviceId(), sensor.getSensorId(), sensor.getSensorType()), FIVE_SECONDS);
                    if (ret != 0) {
                        HwLog.i("VirtualSensorService", "subscribeSensorDataListener err:" + ret);
                        return ret;
                    }
                    this.mVirtualSensorDataListenerList.put(listenerKey, new VirtualSensorListenerInfo(listener, kitlistener));
                    return 0;
                }
            }
            this.mDMSDPAdapter.reportData(API_NAME_SUBSCRIBESENSOR, callTime, startTime, -2);
            return -2;
        }
    }

    public void unSubscribeSensorDataListener(IVirtualSensorDataListener listener) {
        synchronized (SENSOR_LOCK) {
            if (this.mDMSDPAdapter != null) {
                if (listener != null) {
                    List<String> deleteDeviceList = new ArrayList<>();
                    for (Map.Entry<String, VirtualSensorListenerInfo> entry : this.mVirtualSensorDataListenerList.entrySet()) {
                        VirtualSensorListenerInfo sensorListenerInfo = entry.getValue();
                        if (sensorListenerInfo != null) {
                            if (sensorListenerInfo.getVirtualSensorDataListener() == listener) {
                                this.mDMSDPAdapter.unSubscribeSensorDataListener(sensorListenerInfo.getSensorDataListener());
                                deleteDeviceList.add(entry.getKey());
                            }
                        }
                    }
                    for (String deleteDevice : deleteDeviceList) {
                        if (deleteDevice != null) {
                            this.mVirtualSensorDataListenerList.remove(deleteDevice);
                        }
                    }
                }
            }
        }
    }

    private void unSubscribeAllSensorDataListener() {
        synchronized (SENSOR_LOCK) {
            if (this.mDMSDPAdapter != null) {
                List<String> deleteDeviceList = new ArrayList<>();
                for (Map.Entry<String, VirtualSensorListenerInfo> entry : this.mVirtualSensorDataListenerList.entrySet()) {
                    VirtualSensorListenerInfo sensorListenerInfo = entry.getValue();
                    if (sensorListenerInfo != null) {
                        if (sensorListenerInfo.getSensorDataListener() != null) {
                            this.mDMSDPAdapter.unSubscribeSensorDataListener(sensorListenerInfo.getSensorDataListener());
                            deleteDeviceList.add(entry.getKey());
                        }
                    }
                }
                for (String deleteDevice : deleteDeviceList) {
                    if (deleteDevice != null) {
                        this.mVirtualSensorDataListenerList.remove(deleteDevice);
                    }
                }
            }
        }
    }

    private static DMSDPListener getDmsdpListener() {
        synchronized (SENSOR_LOCK) {
            if (mDmsdpListener != null) {
                return mDmsdpListener;
            }
            mDmsdpListener = new DMSDPListener() {
                /* class com.huawei.dmsdp.devicevirtualization.SensorAgent.AnonymousClass2 */

                @Override // com.huawei.dmsdpsdk2.DMSDPListener
                public void onDeviceChange(DMSDPDevice dmsdpDevice, int event, Map<String, Object> map) {
                }

                @Override // com.huawei.dmsdpsdk2.DMSDPListener
                public void onDeviceServiceChange(DMSDPDeviceService dmsdpDeviceService, int event, Map<String, Object> info) {
                    HwLog.i("VirtualSensorService", "onDeviceServiceChange event:" + event);
                    SensorAgent.onServiceChange(dmsdpDeviceService, event, info);
                }
            };
            return mDmsdpListener;
        }
    }

    /* access modifiers changed from: private */
    public static void onServiceChange(DMSDPDeviceService dmsdpDeviceService, int event, Map<String, Object> info) {
        if (dmsdpDeviceService != null) {
            HwLog.i("VirtualSensorService", "ServiceChange type:" + dmsdpDeviceService.getServiceType() + " event:" + Integer.toString(event));
        }
        deviceServiceChangeHandler(dmsdpDeviceService, event, info);
    }

    private static void deviceServiceChangeHandler(DMSDPDeviceService dmsdpDeviceService, int event, Map<String, Object> map) {
        if (dmsdpDeviceService == null) {
            HwLog.e("VirtualSensorService", "dmsdpDeviceService is null when deviceServiceChangeHandler");
        } else {
            if (dmsdpDeviceService.getServiceType() != 2048) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.dmsdp.devicevirtualization.VirtualManager
    public void onConnect(VirtualService dmsdpService) {
        synchronized (SENSOR_LOCK) {
            if (dmsdpService != null) {
                this.mDMSDPAdapter = dmsdpService.getDMSDPAdapter();
                if (this.mDMSDPAdapter != null) {
                    this.mDMSDPAdapter.registerDMSDPListener(5, getDmsdpListener());
                } else {
                    HwLog.e("VirtualSensorService", "dmsdpAdapter is null when register dmsdpListener");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.dmsdp.devicevirtualization.VirtualManager
    public void onDisConnect() {
        synchronized (SENSOR_LOCK) {
            if (this.mDMSDPAdapter != null) {
                unSubscribeAllSensorDataListener();
                this.mDMSDPAdapter.unRegisterDMSDPListener(5, getDmsdpListener());
            }
            this.mDMSDPAdapter = null;
        }
    }
}
