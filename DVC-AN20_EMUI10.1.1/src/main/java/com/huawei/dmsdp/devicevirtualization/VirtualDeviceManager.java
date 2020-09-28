package com.huawei.dmsdp.devicevirtualization;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import com.huawei.android.hwpartdevicevirtualization.BuildConfig;
import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.DMSDPConfig;
import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.DMSDPListener;
import com.huawei.dmsdpsdk2.DeviceParameterConst;
import com.huawei.dmsdpsdk2.DiscoverListener;
import com.huawei.dmsdpsdk2.HwLog;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualDeviceManager extends VirtualManager {
    private static final String API_NAME_SUBSCRIBE = "subscribe";
    private static final String API_NAME_UNSUBSCRIBE = "unsubscribe";
    private static final int INT_TWO = 2;
    private static final String SENSITIVE_INFO = "****";
    private static final String TAG = VirtualDeviceManager.class.getSimpleName();
    private static final int UPGRADE_CODE = -1;
    private static final Object VIRTUALIZATION_LOCK = new Object();
    private Map<String, DMSDPDevice> allConnectedDevice = new ConcurrentHashMap(0);
    private Map<String, DMSDPDevice> allDevices = new ConcurrentHashMap(0);
    private Map<String, Integer> allReqeustServiceTypes = new ConcurrentHashMap(0);
    private Map<String, DMSDPDeviceService> allRunningServices = new ConcurrentHashMap(0);
    private Map<String, VirtualDevice> allVirtualDevices = new ConcurrentHashMap(0);
    private Map<String, String> cameraInfo = new ConcurrentHashMap(0);
    private IDiscoveryCallback discoveryCallback;
    private DMSDPListener dmsdpListener = new DMSDPListener() {
        /* class com.huawei.dmsdp.devicevirtualization.VirtualDeviceManager.AnonymousClass1 */

        @Override // com.huawei.dmsdpsdk2.DMSDPListener
        public void onDeviceChange(DMSDPDevice device, int state, Map<String, Object> map) {
            if (device == null) {
                HwLog.e(VirtualDeviceManager.TAG, "onDeviceChange device is null");
                return;
            }
            String str = VirtualDeviceManager.TAG;
            HwLog.e(str, "onDeviceChange device change state is " + state);
            if (VirtualDeviceManager.this.virtualDeviceObserver != null) {
                VirtualDeviceManager.this.virtualDeviceObserver.onDeviceStateChange((VirtualDevice) VirtualDeviceManager.this.allVirtualDevices.get(device.getDeviceId()), EventTypeConverter.convertEventType(state));
            } else {
                HwLog.e(VirtualDeviceManager.TAG, "onDeviceChange virtualDeviceObserver is null");
            }
            String deviceId = device.getDeviceId();
            if (state == 101) {
                VirtualDeviceManager.this.allConnectedDevice.put(deviceId, device);
                VirtualDeviceManager.this.mDMSDPAdapter.stopDiscover(4, 4, VirtualDeviceManager.this.mDiscoverListener);
                VirtualDeviceManager.this.mDMSDPAdapter.requestDeviceService(4, device, ((Integer) VirtualDeviceManager.this.allReqeustServiceTypes.getOrDefault(deviceId, 0)).intValue());
                VirtualDeviceManager.this.reportDeviceEvent(deviceId, state);
            } else if (state == 102) {
                VirtualDeviceManager.this.allReqeustServiceTypes.remove(deviceId);
                VirtualDeviceManager.this.allConnectedDevice.remove(deviceId);
                VirtualDeviceManager.this.reportDeviceEvent(deviceId, state);
            } else if (state == 103 || state == 108) {
                VirtualDeviceManager.this.allReqeustServiceTypes.remove(deviceId);
                VirtualDeviceManager.this.reportDeviceEvent(deviceId, state);
            }
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPListener
        public void onDeviceServiceChange(DMSDPDeviceService dmsdpDeviceService, int state, Map<String, Object> map) {
            if (dmsdpDeviceService == null) {
                HwLog.e(VirtualDeviceManager.TAG, "deviceService device is null");
                return;
            }
            HwLog.e(VirtualDeviceManager.TAG, String.format(Locale.ENGLISH, "onDeviceServiceChange service type is %d state is %d", Integer.valueOf(dmsdpDeviceService.getServiceType()), Integer.valueOf(state)));
            String key = dmsdpDeviceService.getDeviceId() + DMSDPConfig.SPLIT + dmsdpDeviceService.getServiceId();
            if (state == 201) {
                try {
                    VirtualDeviceManager.this.enableDeviceService(dmsdpDeviceService);
                } catch (SecurityException e) {
                    HwLog.e(VirtualDeviceManager.TAG, "enableDeviceService err:" + e.getLocalizedMessage());
                }
            } else if (state == 204) {
                HwLog.i(VirtualDeviceManager.TAG, String.format(Locale.ENGLISH, "startDeviceService start success %s ", Integer.valueOf(dmsdpDeviceService.getServiceType())));
                VirtualDeviceManager.this.allRunningServices.put(key, dmsdpDeviceService);
                VirtualDeviceManager.this.reportDeviceServiceEvent(dmsdpDeviceService, state);
            } else if (state == 205) {
                VirtualDeviceManager.this.allRunningServices.remove(key);
                VirtualDeviceManager.this.reportDeviceServiceEvent(dmsdpDeviceService, state);
            } else if (state == 206) {
                VirtualDeviceManager.this.allRunningServices.remove(key);
                VirtualDeviceManager.this.reportDeviceServiceEvent(dmsdpDeviceService, state);
            }
        }
    };
    private Context mContext;
    private DMSDPAdapter mDMSDPAdapter;
    private DiscoverListener mDiscoverListener;
    private Map<String, Boolean> openCameraInCurrntApp = new ConcurrentHashMap();
    private Map<String, Boolean> openDisplayInCurrntApp = new ConcurrentHashMap();
    private Map<String, Boolean> openMicInCurrntApp = new ConcurrentHashMap();
    private Map<String, Boolean> openNotificationInCurrntApp = new ConcurrentHashMap();
    private Map<String, Boolean> openSensorInCurrntApp = new ConcurrentHashMap();
    private Map<String, Boolean> openSpeakerInCurrntApp = new ConcurrentHashMap();
    private Map<String, Boolean> openVibrateInCurrntApp = new ConcurrentHashMap();
    private IVirtualDeviceObserver virtualDeviceObserver;

    /* access modifiers changed from: private */
    public static final class InstanceHolder {
        private static final VirtualDeviceManager INSTANCE = new VirtualDeviceManager();

        private InstanceHolder() {
        }
    }

    public static VirtualDeviceManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.dmsdp.devicevirtualization.VirtualManager
    public void onConnect(VirtualService service) {
        HwLog.i(TAG, "onConnect");
        this.mDMSDPAdapter = service.getDMSDPAdapter();
        this.mDMSDPAdapter.registerDMSDPListener(4, this.dmsdpListener);
        this.mContext = service.getContext();
    }

    public Context getContext() {
        return this.mContext;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.dmsdp.devicevirtualization.VirtualManager
    public void onDisConnect() {
        HwLog.i(TAG, "onDisConnect");
        DMSDPAdapter dMSDPAdapter = this.mDMSDPAdapter;
        if (dMSDPAdapter != null) {
            dMSDPAdapter.unRegisterDMSDPListener(4, this.dmsdpListener);
        } else {
            HwLog.e(TAG, "dmsdpAdapter is null when unRegisterDMSDPListener dmsdpListener");
        }
        this.allDevices.clear();
        this.allConnectedDevice.clear();
        this.mDMSDPAdapter = null;
        if (this.virtualDeviceObserver != null) {
            this.virtualDeviceObserver = null;
        }
    }

    public int startDiscovery(IDiscoveryCallback listener) {
        synchronized (VIRTUALIZATION_LOCK) {
            if (this.mDMSDPAdapter != null) {
                if (listener != null) {
                    if (!((PowerManager) this.mContext.getSystemService("power")).isScreenOn()) {
                        HwLog.e(TAG, "screen is off state");
                        return -12;
                    }
                    this.discoveryCallback = listener;
                    DiscoverListener discoverListener = new DiscoverListener() {
                        /* class com.huawei.dmsdp.devicevirtualization.VirtualDeviceManager.AnonymousClass2 */

                        @Override // com.huawei.dmsdpsdk2.DiscoverListener
                        public void onDeviceFound(DMSDPDevice dmsdpDevice) {
                            HwLog.d(VirtualDeviceManager.TAG, "onDeviceFound");
                            VirtualDeviceManager.this.allDevices.put(dmsdpDevice.getDeviceId(), dmsdpDevice);
                            VirtualDevice virtualDevice = new VirtualDevice(dmsdpDevice);
                            VirtualDeviceManager.this.allVirtualDevices.put(dmsdpDevice.getDeviceId(), virtualDevice);
                            VirtualDeviceManager.this.discoveryCallback.onFound(virtualDevice, 0);
                        }

                        @Override // com.huawei.dmsdpsdk2.DiscoverListener
                        public void onDeviceLost(DMSDPDevice device) {
                            HwLog.d(VirtualDeviceManager.TAG, "onDeviceLost");
                        }

                        @Override // com.huawei.dmsdpsdk2.DiscoverListener
                        public void onDeviceUpdate(DMSDPDevice device, int action) {
                            HwLog.d(VirtualDeviceManager.TAG, "onDeviceUpdate");
                        }

                        @Override // com.huawei.dmsdpsdk2.DiscoverListener
                        public void onStateChanged(int state, Map<String, Object> map) {
                            HwLog.d(VirtualDeviceManager.TAG, "onDeviceChanged");
                        }
                    };
                    int dmsdpReturnCode = this.mDMSDPAdapter.startDiscover(4, 255, 255, DMSDPConfig.DISCOVER_SERVICE_FILTER_ALL, discoverListener);
                    if (dmsdpReturnCode == -5) {
                        for (DMSDPDevice device : this.allDevices.values()) {
                            this.discoveryCallback.onFound(new VirtualDevice(device), 0);
                        }
                        return 0;
                    } else if (dmsdpReturnCode == 0) {
                        this.mDiscoverListener = discoverListener;
                        return 0;
                    } else {
                        return ReturnCodeConverter.convertReturnCode(dmsdpReturnCode);
                    }
                }
            }
            HwLog.e(TAG, "mDMSDPAdapter is null");
            return -2;
        }
    }

    public int cancelDiscovery(IDiscoveryCallback callback) {
        DMSDPAdapter dMSDPAdapter = this.mDMSDPAdapter;
        if (dMSDPAdapter == null || callback == null) {
            HwLog.e(TAG, "mDMSDPAdapter is null");
            return -2;
        }
        this.discoveryCallback = callback;
        return ReturnCodeConverter.convertReturnCode(dMSDPAdapter.stopDiscover(4, 255, this.mDiscoverListener));
    }

    public List<VirtualDevice> getVirtualDevicesList() {
        HwLog.d(TAG, "getVirtualDevicesList");
        long callTime = System.currentTimeMillis();
        long startTime = SystemClock.elapsedRealtime();
        List<VirtualDevice> devices = new ArrayList<>();
        for (DMSDPDevice device : this.allDevices.values()) {
            devices.add(new VirtualDevice(device));
        }
        DMSDPAdapter dMSDPAdapter = this.mDMSDPAdapter;
        if (dMSDPAdapter != null) {
            dMSDPAdapter.reportData("getVirtualDevicesList", callTime, startTime, 0);
        }
        return devices;
    }

    public int enableVirtualDevice(String deviceId, EnumSet<Capability> capability, Map<String, String> info) {
        String str = TAG;
        HwLog.e(str, "enableVirtualDevice capability is " + capability);
        synchronized (VIRTUALIZATION_LOCK) {
            if (deviceId != null) {
                if (!deviceId.isEmpty()) {
                    if (capability == null) {
                        HwLog.e(TAG, "parameter is invalid");
                        return -2;
                    } else if (this.mDMSDPAdapter == null) {
                        HwLog.e(TAG, "mDMSDPAdapter is null");
                        return -11;
                    } else {
                        DMSDPDevice dmsdpDevice = this.allDevices.get(deviceId);
                        if (dmsdpDevice == null) {
                            HwLog.e(TAG, "dmsdpDevice is null");
                            return -2;
                        } else if (this.allVirtualDevices.get(deviceId) != null && !this.allVirtualDevices.get(deviceId).getDeviceCapability().containsAll(capability)) {
                            HwLog.e(TAG, "dmsdpDevice is null");
                            return -2;
                        } else if (isNeedUpgrade(dmsdpDevice)) {
                            return -13;
                        } else {
                            int serviceType = transfromCapability(capability);
                            int serviceTypes = 0;
                            if (this.allReqeustServiceTypes.containsKey(deviceId)) {
                                serviceTypes = this.allReqeustServiceTypes.get(deviceId).intValue();
                                this.allReqeustServiceTypes.remove(deviceId);
                            }
                            this.allReqeustServiceTypes.put(deviceId, Integer.valueOf(serviceTypes | serviceType));
                            if (dealDeviceType(dmsdpDevice, deviceId, capability, info) == 0) {
                                return 0;
                            }
                            if (!this.allConnectedDevice.containsKey(deviceId)) {
                                int dmsdpReturnCode = this.mDMSDPAdapter.connectDevice(4, getDeviceChannelType(dmsdpDevice), dmsdpDevice, null);
                                if (dmsdpReturnCode != 0) {
                                    return ReturnCodeConverter.convertReturnCode(dmsdpReturnCode);
                                }
                            } else {
                                this.mDMSDPAdapter.requestDeviceService(4, dmsdpDevice, serviceType);
                            }
                            setServiceFlag(deviceId, capability, info);
                            return 0;
                        }
                    }
                }
            }
            HwLog.e(TAG, "parameter is invalid");
            return -2;
        }
    }

    private int dealDeviceType(DMSDPDevice dmsdpDevice, String deviceId, EnumSet<Capability> capability, Map<String, String> info) {
        int deviceType = dmsdpDevice.getDeviceType();
        if (deviceType == 2) {
            HwLog.d(TAG, "the deviceType is Pad");
            this.mDMSDPAdapter.requestDeviceService(3, dmsdpDevice, transfromCapability(capability));
            setServiceFlag(deviceId, capability, info);
            return 0;
        } else if (deviceType == 4) {
            HwLog.d(TAG, "the deviceType is HICAR");
            this.mDMSDPAdapter.requestDeviceService(1, dmsdpDevice, transfromCapability(capability));
            setServiceFlag(deviceId, capability, info);
            return 0;
        } else if (deviceType != 7) {
            return -1;
        } else {
            HwLog.d(TAG, "the deviceType is PC");
            this.mDMSDPAdapter.requestDeviceService(2, dmsdpDevice, transfromCapability(capability));
            setServiceFlag(deviceId, capability, info);
            return 0;
        }
    }

    public int disableVirtualDevice(String deviceId, EnumSet<Capability> capability) {
        synchronized (VIRTUALIZATION_LOCK) {
            HwLog.d(TAG, String.format(Locale.ENGLISH, "disable device %s service %s", transformSensitiveInfo(deviceId), capability));
            if (deviceId != null) {
                if (!deviceId.isEmpty()) {
                    if (capability == null) {
                        HwLog.e(TAG, "parameter is invalid");
                        return -2;
                    } else if (this.mDMSDPAdapter == null) {
                        HwLog.e(TAG, "mDMSDPAdapter is null");
                        return -11;
                    } else {
                        DMSDPDevice dmsdpDevice = this.allDevices.get(deviceId);
                        if (dmsdpDevice == null) {
                            HwLog.e(TAG, "dmsdpDevice is null");
                            return -2;
                        } else if (this.allVirtualDevices.get(deviceId) != null && !this.allVirtualDevices.get(deviceId).getDeviceCapability().containsAll(capability)) {
                            HwLog.e(TAG, "dmsdpDevice is null");
                            return -2;
                        } else if (!canDisable(deviceId, capability)) {
                            HwLog.e(TAG, "some capability not enable in this app");
                            return -16;
                        } else {
                            clearCurAppEnable(deviceId, capability);
                            clearRequestServiceType(deviceId, capability);
                            if (!capability.contains(Capability.DISPLAY) || isCoapDevice(dmsdpDevice)) {
                                stopSingleService(deviceId, capability);
                            } else {
                                for (Map.Entry<String, DMSDPDeviceService> entry : this.allRunningServices.entrySet()) {
                                    if (entry.getKey().startsWith(deviceId)) {
                                        DMSDPDeviceService service = entry.getValue();
                                        if (service.getServiceType() == 1) {
                                            this.mDMSDPAdapter.stopDeviceService(4, service, 2);
                                        } else {
                                            this.mDMSDPAdapter.stopDeviceService(4, service, 0);
                                        }
                                    }
                                }
                            }
                            return 0;
                        }
                    }
                }
            }
            HwLog.e(TAG, "parameter is invalid");
            return -2;
        }
    }

    private boolean isCoapDevice(DMSDPDevice device) {
        Object protocol = device.getProperties(DeviceParameterConst.DEVICE_DISCOVER_PROTOCOL_INT);
        if (protocol == null) {
            return false;
        }
        String str = TAG;
        HwLog.e(str, "device protocol is:" + protocol);
        if (((Integer) protocol).intValue() == 64) {
            return true;
        }
        return false;
    }

    private boolean isBleCamera(DMSDPDevice device) {
        Object protocol;
        if (device.getDeviceType() == 5 && (protocol = device.getProperties(DeviceParameterConst.DEVICE_DISCOVER_PROTOCOL_INT)) != null) {
            String str = TAG;
            HwLog.e(str, "device protocol is:" + protocol);
            if (((Integer) protocol).intValue() == 1) {
                return true;
            }
        }
        return false;
    }

    private void clearCurAppEnable(String deviceId, EnumSet<Capability> capabilities) {
        Iterator it = capabilities.iterator();
        while (it.hasNext()) {
            switch ((Capability) it.next()) {
                case MIC:
                    this.openMicInCurrntApp.put(deviceId, false);
                    break;
                case SPEAKER:
                    this.openSpeakerInCurrntApp.put(deviceId, false);
                    break;
                case DISPLAY:
                    this.openDisplayInCurrntApp.put(deviceId, false);
                    break;
                case CAMERA:
                    this.openCameraInCurrntApp.put(deviceId, false);
                    break;
                case SENSOR:
                    this.openSensorInCurrntApp.put(deviceId, false);
                    break;
                case VIBRATE:
                    this.openVibrateInCurrntApp.put(deviceId, false);
                    break;
                case NOTIFICATION:
                    this.openNotificationInCurrntApp.put(deviceId, false);
                    break;
            }
        }
    }

    private void clearRequestServiceType(String deviceId, EnumSet<Capability> capabilities) {
        if (this.allReqeustServiceTypes.containsKey(deviceId)) {
            int serviceType = transfromCapability(capabilities);
            int serviceTypes = this.allReqeustServiceTypes.get(deviceId).intValue();
            this.allReqeustServiceTypes.remove(deviceId);
            this.allReqeustServiceTypes.put(deviceId, Integer.valueOf((~serviceType) & serviceTypes));
        }
    }

    private boolean canDisable(String deviceId, EnumSet<Capability> capabilities) {
        Iterator it = capabilities.iterator();
        while (it.hasNext()) {
            Map<String, Boolean> map = null;
            switch ((Capability) it.next()) {
                case MIC:
                case SPEAKER:
                case CAMERA:
                    return true;
                case DISPLAY:
                    map = this.openDisplayInCurrntApp;
                    break;
                case SENSOR:
                    map = this.openSensorInCurrntApp;
                    break;
                case VIBRATE:
                    map = this.openVibrateInCurrntApp;
                    break;
                case NOTIFICATION:
                    map = this.openNotificationInCurrntApp;
                    break;
            }
            if (map == null || !map.containsKey(deviceId) || !map.get(deviceId).booleanValue()) {
                return false;
            }
        }
        return true;
    }

    public int subscribe(EnumSet<ObserverEventType> typeFilter, IVirtualDeviceObserver callback) {
        HwLog.d(TAG, API_NAME_SUBSCRIBE);
        long callTime = System.currentTimeMillis();
        long startTime = SystemClock.elapsedRealtime();
        if (callback == null || typeFilter == null || !typeFilter.contains(ObserverEventType.VIRTUALDEVICE)) {
            HwLog.e(TAG, "parameter is invalid");
            DMSDPAdapter dMSDPAdapter = this.mDMSDPAdapter;
            if (dMSDPAdapter == null) {
                return -2;
            }
            dMSDPAdapter.reportData(API_NAME_SUBSCRIBE, callTime, startTime, -2);
            return -2;
        }
        this.virtualDeviceObserver = callback;
        DMSDPAdapter dMSDPAdapter2 = this.mDMSDPAdapter;
        if (dMSDPAdapter2 == null) {
            return 0;
        }
        dMSDPAdapter2.reportData(API_NAME_SUBSCRIBE, callTime, startTime, 0);
        return 0;
    }

    public int unsubscribe(EnumSet<ObserverEventType> typeFilter, IVirtualDeviceObserver callback) {
        HwLog.d(TAG, API_NAME_UNSUBSCRIBE);
        long callTime = System.currentTimeMillis();
        long startTime = SystemClock.elapsedRealtime();
        if (callback == null || typeFilter == null || !typeFilter.contains(ObserverEventType.VIRTUALDEVICE)) {
            HwLog.e(TAG, "parameter is invalid");
            DMSDPAdapter dMSDPAdapter = this.mDMSDPAdapter;
            if (dMSDPAdapter == null) {
                return -2;
            }
            dMSDPAdapter.reportData(API_NAME_UNSUBSCRIBE, callTime, startTime, -2);
            return -2;
        }
        this.virtualDeviceObserver = null;
        DMSDPAdapter dMSDPAdapter2 = this.mDMSDPAdapter;
        if (dMSDPAdapter2 == null) {
            return 0;
        }
        dMSDPAdapter2.reportData(API_NAME_UNSUBSCRIBE, callTime, startTime, 0);
        return 0;
    }

    public List<String> getVirtualCameraId() {
        List<String> cameraIds;
        synchronized (VIRTUALIZATION_LOCK) {
            cameraIds = new ArrayList<>();
            this.mDMSDPAdapter.getVirtualCameraList(4, cameraIds);
        }
        return cameraIds;
    }

    private int getDeviceChannelType(DMSDPDevice device) {
        int channelType;
        HwLog.d(TAG, "getDeviceChannelType");
        int deviceType = device.getDeviceType();
        if (deviceType == 3) {
            channelType = 7;
        } else if (deviceType == 9) {
            channelType = 11;
        } else if (deviceType == 5) {
            channelType = 8;
        } else if (deviceType != 6) {
            channelType = 0;
        } else {
            channelType = 6;
        }
        if (device.getProperties(DeviceParameterConst.HILINK_DEVICE) != null) {
            HwLog.e(TAG, "device is hilink");
            channelType = 9;
        }
        if (isCoapDevice(device)) {
            HwLog.d(TAG, "device is coap");
            channelType = 10;
        }
        if (!isBleCamera(device)) {
            return channelType;
        }
        HwLog.d(TAG, "device is BleCamera");
        return 4;
    }

    private void stopSingleService(String deviceId, EnumSet<Capability> capability) {
        for (Map.Entry<String, DMSDPDeviceService> entry : this.allRunningServices.entrySet()) {
            HwLog.i(TAG, String.format(Locale.ENGLISH, "allRunningServices %s ", Integer.valueOf(entry.getValue().getServiceType())));
            if (entry.getKey().startsWith(deviceId)) {
                DMSDPDeviceService service = entry.getValue();
                if (capability.contains(Capability.CAMERA) && entry.getValue().getServiceType() == convertCapabilityToServiceType(Capability.CAMERA)) {
                    HwLog.i(TAG, String.format("stop service SERVICE_TYPE_CAMERA", new Object[0]));
                    this.mDMSDPAdapter.stopDeviceService(4, service, 2);
                } else if (capability.contains(Capability.SPEAKER) && entry.getValue().getServiceType() == convertCapabilityToServiceType(Capability.SPEAKER)) {
                    HwLog.i(TAG, String.format("stop service SERVICE_TYPE_SPEAKER", new Object[0]));
                    this.mDMSDPAdapter.stopDeviceService(4, service, 0);
                } else if (capability.contains(Capability.MIC) && entry.getValue().getServiceType() == convertCapabilityToServiceType(Capability.MIC)) {
                    HwLog.i(TAG, String.format("stop service SERVICE_TYPE_MIC", new Object[0]));
                    this.mDMSDPAdapter.stopDeviceService(4, service, 0);
                } else if (capability.contains(Capability.DISPLAY) && entry.getValue().getServiceType() == convertCapabilityToServiceType(Capability.DISPLAY)) {
                    HwLog.i(TAG, String.format("stop service SERVICE_TYPE_DISPLAY", new Object[0]));
                    this.mDMSDPAdapter.stopDeviceService(4, service, 0);
                } else if (capability.contains(Capability.SENSOR) && entry.getValue().getServiceType() == convertCapabilityToServiceType(Capability.SENSOR)) {
                    HwLog.i(TAG, String.format("stop service SERVICE_TYPE_SENSOR", new Object[0]));
                    this.mDMSDPAdapter.stopDeviceService(4, service, 0);
                } else if (capability.contains(Capability.VIBRATE) && entry.getValue().getServiceType() == convertCapabilityToServiceType(Capability.VIBRATE)) {
                    HwLog.i(TAG, String.format("stop service SERVICE_TYPE_VIBRATE", new Object[0]));
                    this.mDMSDPAdapter.stopDeviceService(4, service, 0);
                } else if (!capability.contains(Capability.NOTIFICATION) || entry.getValue().getServiceType() != convertCapabilityToServiceType(Capability.NOTIFICATION)) {
                    HwLog.w(TAG, "stop service default case");
                } else {
                    HwLog.i(TAG, "stop service DEVICE_SERVICE_TYPE_NOTIFICATION");
                    this.mDMSDPAdapter.stopDeviceService(4, service, 0);
                }
            }
        }
    }

    private int convertCapabilityToServiceType(Capability capability) {
        switch (capability) {
            case MIC:
                return 2;
            case SPEAKER:
                return 4;
            case DISPLAY:
                return 8;
            case CAMERA:
                return 1;
            case SENSOR:
                return DMSDPConfig.DEVICE_SERVICE_TYPE_SENSOR;
            case VIBRATE:
                return DMSDPConfig.DEVICE_SERVICE_TYPE_VIBRATE;
            case NOTIFICATION:
                return DMSDPConfig.DEVICE_SERVICE_TYPE_NOTIFICATION;
            default:
                return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableDeviceService(DMSDPDeviceService service) {
        String str = TAG;
        HwLog.i(str, "startDeviceService start service is " + service.getServiceType());
        synchronized (VIRTUALIZATION_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
            } else if (!this.allReqeustServiceTypes.containsKey(service.getDeviceId())) {
                String str2 = TAG;
                HwLog.e(str2, "Refuse enable this device:" + service.getDeviceName());
            } else {
                if (service.getServiceType() == 1) {
                    startCameraService(service);
                } else if (service.getServiceType() == 2) {
                    startMicService(service);
                } else if (service.getServiceType() == 4) {
                    startSpeakerService(service);
                } else if (service.getServiceType() == 8) {
                    startDisplayService(service);
                } else if (service.getServiceType() == 2048) {
                    startSensorService(service);
                } else if (service.getServiceType() == 4096) {
                    startVibrateService(service);
                } else if (service.getServiceType() == 8192) {
                    startNotificationService(service);
                } else {
                    HwLog.w(TAG, "enableDeviceService default case");
                }
            }
        }
    }

    private void startNotificationService(DMSDPDeviceService service) {
        IVirtualDeviceObserver iVirtualDeviceObserver;
        int startResult = this.mDMSDPAdapter.startDeviceService(4, service, 0, null);
        String str = TAG;
        HwLog.i(str, "start notification service startResult is " + startResult);
        if (startResult != 0 && (iVirtualDeviceObserver = this.virtualDeviceObserver) != null) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.NOTIFICATION, EventTypeConverter.convertEventType(startResult));
            String str2 = TAG;
            HwLog.i(str2, "onDeviceCapabilityStateChange NOTIFICATION  " + startResult);
        }
    }

    private void startSensorService(DMSDPDeviceService service) {
        IVirtualDeviceObserver iVirtualDeviceObserver;
        int startResult = this.mDMSDPAdapter.startDeviceService(4, service, 0, null);
        String str = TAG;
        HwLog.i(str, "start display service startResult is " + startResult);
        if (startResult != 0 && (iVirtualDeviceObserver = this.virtualDeviceObserver) != null) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.SENSOR, EventTypeConverter.convertEventType(startResult));
            String str2 = TAG;
            HwLog.i(str2, "onDeviceCapabilityStateChange SENSOR  " + startResult);
        }
    }

    private void startVibrateService(DMSDPDeviceService service) {
        IVirtualDeviceObserver iVirtualDeviceObserver;
        int startResult = this.mDMSDPAdapter.startDeviceService(4, service, 0, null);
        String str = TAG;
        HwLog.i(str, "start display service startResult is " + startResult);
        if (startResult != 0 && (iVirtualDeviceObserver = this.virtualDeviceObserver) != null) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.VIBRATE, EventTypeConverter.convertEventType(startResult));
            String str2 = TAG;
            HwLog.i(str2, "onDeviceCapabilityStateChange VIBRATE  " + startResult);
        }
    }

    private void startDisplayService(DMSDPDeviceService service) {
        IVirtualDeviceObserver iVirtualDeviceObserver;
        int startResult = this.mDMSDPAdapter.startDeviceService(4, service, 0, null);
        String str = TAG;
        HwLog.i(str, "start display service startResult is " + startResult);
        if (startResult != 0 && (iVirtualDeviceObserver = this.virtualDeviceObserver) != null) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.DISPLAY, EventTypeConverter.convertEventType(startResult));
            String str2 = TAG;
            HwLog.i(str2, "onDeviceCapabilityStateChange DISPLAY  " + startResult);
        }
    }

    private void startSpeakerService(DMSDPDeviceService service) {
        IVirtualDeviceObserver iVirtualDeviceObserver;
        HwLog.i(TAG, "start speaker service ");
        int startResult = this.mDMSDPAdapter.startDeviceService(4, service, 0, null);
        if (startResult != 0 && (iVirtualDeviceObserver = this.virtualDeviceObserver) != null) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.SPEAKER, EventTypeConverter.convertEventType(startResult));
        }
    }

    private void startMicService(DMSDPDeviceService service) {
        IVirtualDeviceObserver iVirtualDeviceObserver;
        HwLog.i(TAG, "start mic service ");
        int startResult = this.mDMSDPAdapter.startDeviceService(4, service, 0, null);
        if (startResult != 0 && (iVirtualDeviceObserver = this.virtualDeviceObserver) != null) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.MIC, EventTypeConverter.convertEventType(startResult));
        }
    }

    private void startCameraService(DMSDPDeviceService service) {
        IVirtualDeviceObserver iVirtualDeviceObserver;
        HwLog.i(TAG, "start camera service ");
        int startResult = this.mDMSDPAdapter.startDeviceService(4, service, 2, null);
        if (!(startResult == 0 || (iVirtualDeviceObserver = this.virtualDeviceObserver) == null)) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.CAMERA, EventTypeConverter.convertEventType(startResult));
        }
        if (this.cameraInfo != null) {
            Map<String, Object> cameraParams = dealCameraInfo();
            if (cameraParams != null) {
                this.mDMSDPAdapter.updateDeviceService(4, service, 207, cameraParams);
            }
            HwLog.w(TAG, "cameraParams is null");
        }
        HwLog.w(TAG, "cameraInfo is null");
    }

    private Map<String, Object> dealCameraInfo() {
        Map<String, Object> cameraParams = new HashMap<>(0);
        if (this.cameraInfo.containsKey(Constants.AUTO_ORIENTATION)) {
            if (this.cameraInfo.get(Constants.AUTO_ORIENTATION).equals(Constants.AUTO_ORIENTATION_ENABLE)) {
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_AUTO_ORIENTATION, true);
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_TYPE, DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else if (this.cameraInfo.get(Constants.AUTO_ORIENTATION).equals(Constants.AUTO_ORIENTATION_DISABLE)) {
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_AUTO_ORIENTATION, false);
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_TYPE, DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else {
                HwLog.e(TAG, "the value of AUTO_ORIENTATION is invalid");
            }
        }
        if (this.cameraInfo.containsKey(Constants.DO_MIRROR)) {
            if (this.cameraInfo.get(Constants.DO_MIRROR).equals(Constants.DO_MIRROR_ENABLE)) {
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_DO_MIRROR, true);
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_TYPE, DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else if (this.cameraInfo.get(Constants.DO_MIRROR).equals(Constants.DO_MIRROR_DISABLE)) {
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_DO_MIRROR, false);
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_TYPE, DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else {
                HwLog.e(TAG, "the value for DO_MIRROR is invalid");
            }
        }
        if (this.cameraInfo.containsKey(Constants.PROCESS_STRATGY)) {
            if (this.cameraInfo.get(Constants.PROCESS_STRATGY).equals(Constants.PROCESS_STRATGY_FULL_CONTENT)) {
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_PROCESS_STRATGY, DMSDPConfig.FrameProcessStratgy.FULL_CONTENT.name());
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_TYPE, DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else if (this.cameraInfo.get(Constants.PROCESS_STRATGY).equals(Constants.PROCESS_STRATGY_MAX_SCREEN_FILL)) {
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_PROCESS_STRATGY, DMSDPConfig.FrameProcessStratgy.MAX_SCREEN_FILL.name());
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_TYPE, DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else {
                HwLog.e(TAG, "the value for PROCESS_STRATGY is invalid");
            }
        }
        return cameraParams;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportDeviceEvent(String deviceId, int event) {
        HwLog.i(TAG, "reportDeviceEvent");
        if (this.allVirtualDevices.get(deviceId) == null) {
            HwLog.w(TAG, "reportDeviceServiceEvent: device not exist");
        } else if (event == 101) {
            HwLog.d(TAG, "device connect");
        } else if (event == 102) {
            reportDeviceDisconnect(deviceId);
        } else if (event == 103 || event == 108) {
            reportDeviceConnectFail(deviceId);
        }
    }

    private void reportDeviceConnectFail(String deviceId) {
        IVirtualDeviceObserver iVirtualDeviceObserver = this.virtualDeviceObserver;
        if (iVirtualDeviceObserver != null) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(deviceId), Capability.DISPLAY, 206);
        }
    }

    private void reportDeviceDisconnect(String deviceId) {
        HwLog.i(TAG, "reportDeviceDisconnect");
        for (Map.Entry<String, DMSDPDeviceService> entry : this.allRunningServices.entrySet()) {
            if (entry.getKey().startsWith(deviceId)) {
                String str = TAG;
                HwLog.i(str, "service start with" + entry.getKey());
                DMSDPDeviceService service = entry.getValue();
                if (service.getServiceType() == 1) {
                    IVirtualDeviceObserver iVirtualDeviceObserver = this.virtualDeviceObserver;
                    if (iVirtualDeviceObserver != null) {
                        iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(deviceId), Capability.CAMERA, 205);
                    }
                } else if (service.getServiceType() == 2) {
                    IVirtualDeviceObserver iVirtualDeviceObserver2 = this.virtualDeviceObserver;
                    if (iVirtualDeviceObserver2 != null) {
                        iVirtualDeviceObserver2.onDeviceCapabilityStateChange(this.allVirtualDevices.get(deviceId), Capability.MIC, 205);
                    }
                } else if (service.getServiceType() == 4) {
                    IVirtualDeviceObserver iVirtualDeviceObserver3 = this.virtualDeviceObserver;
                    if (iVirtualDeviceObserver3 != null) {
                        iVirtualDeviceObserver3.onDeviceCapabilityStateChange(this.allVirtualDevices.get(deviceId), Capability.SPEAKER, 205);
                    }
                } else if (service.getServiceType() == 8) {
                    IVirtualDeviceObserver iVirtualDeviceObserver4 = this.virtualDeviceObserver;
                    if (iVirtualDeviceObserver4 != null) {
                        iVirtualDeviceObserver4.onDeviceCapabilityStateChange(this.allVirtualDevices.get(deviceId), Capability.DISPLAY, 205);
                    }
                } else if (service.getServiceType() == 2048) {
                    IVirtualDeviceObserver iVirtualDeviceObserver5 = this.virtualDeviceObserver;
                    if (iVirtualDeviceObserver5 != null) {
                        iVirtualDeviceObserver5.onDeviceCapabilityStateChange(this.allVirtualDevices.get(deviceId), Capability.SENSOR, 205);
                    }
                } else if (service.getServiceType() == 4096) {
                    IVirtualDeviceObserver iVirtualDeviceObserver6 = this.virtualDeviceObserver;
                    if (iVirtualDeviceObserver6 != null) {
                        iVirtualDeviceObserver6.onDeviceCapabilityStateChange(this.allVirtualDevices.get(deviceId), Capability.VIBRATE, 205);
                    }
                } else if (service.getServiceType() == 8192) {
                    IVirtualDeviceObserver iVirtualDeviceObserver7 = this.virtualDeviceObserver;
                    if (iVirtualDeviceObserver7 != null) {
                        iVirtualDeviceObserver7.onDeviceCapabilityStateChange(this.allVirtualDevices.get(deviceId), Capability.NOTIFICATION, 205);
                    }
                } else {
                    HwLog.w(TAG, "reportDeviceDisconnect default case");
                }
                this.allRunningServices.remove(entry.getKey());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportDeviceServiceEvent(DMSDPDeviceService service, int event) {
        HwLog.i(TAG, "reportDeviceServiceEvent");
        synchronized (VIRTUALIZATION_LOCK) {
            if (this.allVirtualDevices.get(service.getDeviceId()) == null) {
                HwLog.w(TAG, "reportDeviceServiceEvent: device not exist");
                return;
            }
            if (service.getServiceType() == 2) {
                reportMicEvent(service, event);
            } else if (service.getServiceType() == 4) {
                reportSpeakerEvent(service, event);
            } else if (service.getServiceType() == 1) {
                reportCameraEvent(service, event);
            } else if (service.getServiceType() == 8) {
                reportDisplayEvent(service, event);
            } else if (service.getServiceType() == 2048) {
                reportSensorEvent(service, event);
            } else if (service.getServiceType() == 4096) {
                reportVibrateEvent(service, event);
            } else if (service.getServiceType() == 8192) {
                reportNotificationEvent(service, event);
            } else {
                HwLog.w(TAG, "reportDeviceServiceEvent default case");
            }
        }
    }

    private void reportNotificationEvent(DMSDPDeviceService service, int event) {
        IVirtualDeviceObserver iVirtualDeviceObserver = this.virtualDeviceObserver;
        if (iVirtualDeviceObserver == null) {
            return;
        }
        if (event == 204) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.NOTIFICATION, 204);
        } else if (event == 205) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.NOTIFICATION, 205);
        } else if (event == 206) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.NOTIFICATION, 206);
        } else {
            HwLog.w(TAG, "reportNotificationEvent default case");
        }
    }

    private void reportSensorEvent(DMSDPDeviceService service, int event) {
        IVirtualDeviceObserver iVirtualDeviceObserver = this.virtualDeviceObserver;
        if (iVirtualDeviceObserver == null) {
            return;
        }
        if (event == 204) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.SENSOR, 204);
        } else if (event == 205) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.SENSOR, 205);
        } else if (event == 206) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.SENSOR, 206);
        }
    }

    private void reportVibrateEvent(DMSDPDeviceService service, int event) {
        IVirtualDeviceObserver iVirtualDeviceObserver = this.virtualDeviceObserver;
        if (iVirtualDeviceObserver == null) {
            return;
        }
        if (event == 204) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.VIBRATE, 204);
        } else if (event == 205) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.VIBRATE, 205);
        } else if (event == 206) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.VIBRATE, 206);
        }
    }

    private void reportDisplayEvent(DMSDPDeviceService service, int event) {
        IVirtualDeviceObserver iVirtualDeviceObserver = this.virtualDeviceObserver;
        if (iVirtualDeviceObserver == null) {
            return;
        }
        if (event == 204) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.DISPLAY, 204);
        } else if (event == 205) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.DISPLAY, 205);
        } else if (event == 206) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.DISPLAY, 206);
        }
    }

    private void reportMicEvent(DMSDPDeviceService service, int event) {
        IVirtualDeviceObserver iVirtualDeviceObserver = this.virtualDeviceObserver;
        if (iVirtualDeviceObserver == null) {
            return;
        }
        if (event == 204) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.MIC, 204);
        } else if (event == 205) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.MIC, 205);
        } else if (event == 206) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.MIC, 206);
        }
    }

    private void reportSpeakerEvent(DMSDPDeviceService service, int event) {
        IVirtualDeviceObserver iVirtualDeviceObserver = this.virtualDeviceObserver;
        if (iVirtualDeviceObserver == null) {
            return;
        }
        if (event == 204) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.SPEAKER, 204);
        } else if (event == 205) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.SPEAKER, 205);
        } else if (event == 206) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.SPEAKER, 206);
        }
    }

    private void reportCameraEvent(DMSDPDeviceService service, int event) {
        IVirtualDeviceObserver iVirtualDeviceObserver = this.virtualDeviceObserver;
        if (iVirtualDeviceObserver == null) {
            return;
        }
        if (event == 204) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.CAMERA, 204);
        } else if (event == 205) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.CAMERA, 205);
        } else if (event == 206) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(service.getDeviceId()), Capability.CAMERA, 206);
        }
    }

    private static String transformSensitiveInfo(String info) {
        return transformSensitiveInfo(info, 2, 2, true);
    }

    private static String transformSensitiveInfo(String info, int left, int right, boolean isBasedOnLeft) {
        if (info == null || info.isEmpty()) {
            return BuildConfig.FLAVOR;
        }
        StringBuilder sbText = new StringBuilder(1);
        int length = info.length();
        if ((length - left) - right > 0) {
            String prefix = info.substring(0, left);
            String suffix = info.substring(length - right);
            sbText.append(prefix);
            sbText.append(SENSITIVE_INFO);
            sbText.append(suffix);
        } else if (isBasedOnLeft) {
            if (length <= left || left <= 0) {
                sbText.append(info.substring(0, 1));
                sbText.append(SENSITIVE_INFO);
            } else {
                sbText.append(info.substring(0, left));
                sbText.append(SENSITIVE_INFO);
            }
        } else if (length <= right || right <= 0) {
            sbText.append(SENSITIVE_INFO);
            sbText.append(info.substring(info.length() - 1));
        } else {
            sbText.append(SENSITIVE_INFO);
            sbText.append(info.substring(length - right));
        }
        return sbText.toString();
    }

    private boolean isNeedUpgrade(DMSDPDevice device) {
        if (device.getProperties(DeviceParameterConst.HILINK_DEVICE) == null) {
            HwLog.e(TAG, "device is not hilink");
            return false;
        }
        Object proObj = device.getProperties(DeviceParameterConst.PROTOCOL);
        if (proObj == null) {
            HwLog.e(TAG, "get device protocol error, proObj is null");
            return false;
        } else if (!(proObj instanceof Integer)) {
            HwLog.e(TAG, "protocol is wrong type");
            return false;
        } else if (((Integer) proObj).intValue() == -1) {
            return true;
        } else {
            return false;
        }
    }

    private int transfromCapability(EnumSet<Capability> capability) {
        int res = 0;
        if (capability.contains(Capability.CAMERA)) {
            res = 0 | 1;
        }
        if (capability.contains(Capability.MIC)) {
            res |= 2;
        }
        if (capability.contains(Capability.SPEAKER)) {
            res |= 4;
        }
        if (capability.contains(Capability.DISPLAY)) {
            res |= 8;
        }
        if (capability.contains(Capability.SENSOR)) {
            res |= DMSDPConfig.DEVICE_SERVICE_TYPE_SENSOR;
        }
        if (capability.contains(Capability.VIBRATE)) {
            res |= DMSDPConfig.DEVICE_SERVICE_TYPE_VIBRATE;
        }
        if (capability.contains(Capability.NOTIFICATION)) {
            return res | DMSDPConfig.DEVICE_SERVICE_TYPE_NOTIFICATION;
        }
        return res;
    }

    private void setServiceFlag(String deviceId, EnumSet<Capability> capability, Map<String, String> info) {
        String str = TAG;
        HwLog.d(str, "capability is " + capability.toString());
        if (capability.contains(Capability.CAMERA)) {
            this.openCameraInCurrntApp.put(deviceId, true);
            this.cameraInfo = info;
        }
        if (capability.contains(Capability.MIC)) {
            this.openMicInCurrntApp.put(deviceId, true);
        }
        if (capability.contains(Capability.SPEAKER)) {
            this.openSpeakerInCurrntApp.put(deviceId, true);
        }
        if (capability.contains(Capability.DISPLAY)) {
            this.openDisplayInCurrntApp.put(deviceId, true);
        }
        if (capability.contains(Capability.SENSOR)) {
            this.openSensorInCurrntApp.put(deviceId, true);
        }
        if (capability.contains(Capability.VIBRATE)) {
            this.openVibrateInCurrntApp.put(deviceId, true);
        }
        if (capability.contains(Capability.NOTIFICATION)) {
            this.openNotificationInCurrntApp.put(deviceId, true);
        }
    }
}
