package ohos.msdp.devicevirtualization;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.Surface;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ohos.dmsdp.sdk.DMSDPAdapter;
import ohos.dmsdp.sdk.DMSDPConfig;
import ohos.dmsdp.sdk.DMSDPDevice;
import ohos.dmsdp.sdk.DMSDPDeviceService;
import ohos.dmsdp.sdk.DMSDPListener;
import ohos.dmsdp.sdk.DiscoverListener;
import ohos.dmsdp.sdk.HwLog;
import ohos.telephony.TelephonyUtils;

public class VirtualDeviceManager extends VirtualManager {
    private static final String API_NAME_SUBSCRIBE = "subscribe";
    private static final String API_NAME_UNSUBSCRIBE = "unsubscribe";
    private static final int COLLECTION_SIZE = 8;
    private static final Object DEVICE_MANAGER_LOCK = new Object();
    private static final int INT_TWO = 2;
    private static final String SENSITIVE_INFO = "****";
    private static final String TAG = VirtualDeviceManager.class.getSimpleName();
    private static final int UPGRADE_CODE = -1;
    private static final Object VIRTUALIZATION_LOCK = new Object();
    private Map<String, DMSDPDevice> allConnectedDevice = new ConcurrentHashMap(8);
    private Map<String, DMSDPDevice> allDevices = new ConcurrentHashMap(8);
    private Map<String, Integer> allReqeustServiceTypes = new ConcurrentHashMap(8);
    private Map<String, DMSDPDeviceService> allRunningServices = new ConcurrentHashMap(8);
    private Map<String, VirtualDevice> allVirtualDevices = new ConcurrentHashMap(8);
    private Map<String, String> cameraInfo = new ConcurrentHashMap(8);
    private IDiscoveryCallback discoveryCallback;
    private DMSDPListener dmsdpListener = new DMSDPListener() {
        /* class ohos.msdp.devicevirtualization.VirtualDeviceManager.AnonymousClass1 */

        public void onDeviceChange(DMSDPDevice dMSDPDevice, int i, Map<String, Object> map) {
            if (dMSDPDevice == null) {
                HwLog.e(VirtualDeviceManager.TAG, "onDeviceChange device is null");
                return;
            }
            String str = VirtualDeviceManager.TAG;
            HwLog.e(str, "onDeviceChange device change state is " + i);
            if (VirtualDeviceManager.this.virtualDeviceObserver != null) {
                VirtualDeviceManager.this.virtualDeviceObserver.onDeviceStateChange((VirtualDevice) VirtualDeviceManager.this.allVirtualDevices.get(dMSDPDevice.getDeviceId()), EventTypeConverter.convertEventType(i));
            } else {
                HwLog.e(VirtualDeviceManager.TAG, "onDeviceChange virtualDeviceObserver is null");
            }
            String deviceId = dMSDPDevice.getDeviceId();
            if (i == 101) {
                VirtualDeviceManager.this.allConnectedDevice.put(deviceId, dMSDPDevice);
                VirtualDeviceManager.this.mDMSDPAdapter.stopDiscover(4, 4, VirtualDeviceManager.this.mDiscoverListener);
                VirtualDeviceManager.this.mDMSDPAdapter.requestDeviceService(4, dMSDPDevice, ((Integer) VirtualDeviceManager.this.allReqeustServiceTypes.getOrDefault(deviceId, 0)).intValue());
                VirtualDeviceManager.this.reportDeviceEvent(deviceId, i);
            } else if (i == 102) {
                VirtualDeviceManager.this.allReqeustServiceTypes.remove(deviceId);
                VirtualDeviceManager.this.allConnectedDevice.remove(deviceId);
                VirtualDeviceManager.this.reportDeviceEvent(deviceId, i);
            } else if (i == 103 || i == 108) {
                VirtualDeviceManager.this.allReqeustServiceTypes.remove(deviceId);
                VirtualDeviceManager.this.reportDeviceEvent(deviceId, i);
            } else {
                HwLog.e(VirtualDeviceManager.TAG, "unknown state!");
            }
        }

        public void onDeviceServiceChange(DMSDPDeviceService dMSDPDeviceService, int i, Map<String, Object> map) {
            if (dMSDPDeviceService == null) {
                HwLog.e(VirtualDeviceManager.TAG, "deviceService device is null");
                return;
            }
            HwLog.e(VirtualDeviceManager.TAG, String.format(Locale.ENGLISH, "onDeviceServiceChange service type is %d state is %d", Integer.valueOf(dMSDPDeviceService.getServiceType()), Integer.valueOf(i)));
            String str = dMSDPDeviceService.getDeviceId() + "#" + dMSDPDeviceService.getServiceId();
            if (i == 201) {
                VirtualDeviceManager.this.enableDeviceService(dMSDPDeviceService);
            } else if (i == 204) {
                HwLog.i(VirtualDeviceManager.TAG, String.format(Locale.ENGLISH, "startDeviceService start success %s ", Integer.valueOf(dMSDPDeviceService.getServiceType())));
                VirtualDeviceManager.this.allRunningServices.put(str, dMSDPDeviceService);
                VirtualDeviceManager.this.reportDeviceServiceEvent(dMSDPDeviceService, i);
            } else if (i == 205) {
                VirtualDeviceManager.this.allRunningServices.remove(str);
                VirtualDeviceManager.this.reportDeviceServiceEvent(dMSDPDeviceService, i);
            } else if (i == 206) {
                VirtualDeviceManager.this.allRunningServices.remove(str);
                VirtualDeviceManager.this.reportDeviceServiceEvent(dMSDPDeviceService, i);
            } else {
                HwLog.e(VirtualDeviceManager.TAG, "unknown state!");
            }
        }
    };
    private Map<String, Object> hdmiInfo = new ConcurrentHashMap(8);
    private Context mContext;
    private DMSDPAdapter mDMSDPAdapter;
    private DiscoverListener mDiscoverListener;
    private Map<String, Boolean> openCameraInCurrntApp = new ConcurrentHashMap(8);
    private Map<String, Boolean> openDisplayInCurrntApp = new ConcurrentHashMap(8);
    private Map<String, Boolean> openMicInCurrntApp = new ConcurrentHashMap(8);
    private Map<String, Boolean> openNotificationInCurrntApp = new ConcurrentHashMap(8);
    private Map<String, Boolean> openSensorInCurrntApp = new ConcurrentHashMap(8);
    private Map<String, Boolean> openSpeakerInCurrntApp = new ConcurrentHashMap(8);
    private Map<String, Boolean> openVibrateInCurrntApp = new ConcurrentHashMap(8);
    private IVirtualDeviceObserver virtualDeviceObserver;

    public DMSDPDeviceService getDmsdpDeviceService() {
        for (Map.Entry<String, DMSDPDeviceService> entry : this.allRunningServices.entrySet()) {
            if (entry.getValue().getServiceType() == 1) {
                return entry.getValue();
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public static final class InstanceHolder {
        private static final VirtualDeviceManager INSTANCE = new VirtualDeviceManager();

        private InstanceHolder() {
        }
    }

    static VirtualDeviceManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.msdp.devicevirtualization.VirtualManager
    public void onConnect(VirtualService virtualService) {
        HwLog.i(TAG, "onConnect");
        this.mDMSDPAdapter = virtualService.getDMSDPAdapter();
        this.mDMSDPAdapter.registerDMSDPListener(4, this.dmsdpListener);
        this.mContext = virtualService.getContext();
    }

    public Context getContext() {
        return this.mContext;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.msdp.devicevirtualization.VirtualManager
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

    public int setVideoSurface(Surface surface) {
        HwLog.i(TAG, "enter set video surface");
        DMSDPAdapter dMSDPAdapter = this.mDMSDPAdapter;
        if (dMSDPAdapter != null) {
            return dMSDPAdapter.setVideoSurface(4, surface);
        }
        HwLog.e(TAG, "get msdp device agent error");
        return -1;
    }

    public int startDiscovery(IDiscoveryCallback iDiscoveryCallback) {
        synchronized (VIRTUALIZATION_LOCK) {
            if (this.mDMSDPAdapter != null) {
                if (iDiscoveryCallback != null) {
                    Object systemService = this.mContext.getSystemService("power");
                    if (!(systemService instanceof PowerManager)) {
                        return -1;
                    }
                    if (!((PowerManager) systemService).isInteractive()) {
                        HwLog.e(TAG, "screen is off state");
                        return -12;
                    }
                    this.discoveryCallback = iDiscoveryCallback;
                    DiscoverListener discoverListener = getDiscoverListener();
                    int startDiscover = this.mDMSDPAdapter.startDiscover(4, 255, 255, 511, discoverListener);
                    if (startDiscover == -5) {
                        for (DMSDPDevice dMSDPDevice : this.allDevices.values()) {
                            this.discoveryCallback.onFound(new VirtualDevice(dMSDPDevice), 0);
                        }
                        return 0;
                    } else if (startDiscover == 0) {
                        this.mDiscoverListener = discoverListener;
                        return 0;
                    } else {
                        return ReturnCodeConverter.convertReturnCode(startDiscover);
                    }
                }
            }
            HwLog.e(TAG, "mDMSDPAdapter is null");
            return -2;
        }
    }

    private DiscoverListener getDiscoverListener() {
        return new DiscoverListener() {
            /* class ohos.msdp.devicevirtualization.VirtualDeviceManager.AnonymousClass2 */

            public void onDeviceFound(DMSDPDevice dMSDPDevice) {
                HwLog.d(VirtualDeviceManager.TAG, "onDeviceFound");
                VirtualDeviceManager.this.allDevices.put(dMSDPDevice.getDeviceId(), dMSDPDevice);
                VirtualDevice virtualDevice = new VirtualDevice(dMSDPDevice);
                VirtualDeviceManager.this.allVirtualDevices.put(dMSDPDevice.getDeviceId(), virtualDevice);
                VirtualDeviceManager.this.discoveryCallback.onFound(virtualDevice, 0);
            }

            public void onDeviceLost(DMSDPDevice dMSDPDevice) {
                HwLog.d(VirtualDeviceManager.TAG, "onDeviceLost");
            }

            public void onDeviceUpdate(DMSDPDevice dMSDPDevice, int i) {
                HwLog.d(VirtualDeviceManager.TAG, "onDeviceUpdate");
            }

            public void onStateChanged(int i, Map<String, Object> map) {
                HwLog.d(VirtualDeviceManager.TAG, "onDeviceChanged");
            }
        };
    }

    public int cancelDiscovery(IDiscoveryCallback iDiscoveryCallback) {
        DMSDPAdapter dMSDPAdapter = this.mDMSDPAdapter;
        if (dMSDPAdapter == null || iDiscoveryCallback == null) {
            HwLog.e(TAG, "mDMSDPAdapter is null");
            return -2;
        }
        this.discoveryCallback = iDiscoveryCallback;
        return ReturnCodeConverter.convertReturnCode(dMSDPAdapter.stopDiscover(4, 255, this.mDiscoverListener));
    }

    public List<VirtualDevice> getVirtualDevicesList() {
        HwLog.d(TAG, "getVirtualDevicesList");
        long currentTimeMillis = System.currentTimeMillis();
        long elapsedRealtime = SystemClock.elapsedRealtime();
        ArrayList arrayList = new ArrayList();
        for (DMSDPDevice dMSDPDevice : this.allDevices.values()) {
            arrayList.add(new VirtualDevice(dMSDPDevice));
        }
        DMSDPAdapter dMSDPAdapter = this.mDMSDPAdapter;
        if (dMSDPAdapter != null) {
            dMSDPAdapter.reportData("getVirtualDevicesList", currentTimeMillis, elapsedRealtime, 0);
        }
        return arrayList;
    }

    public int enableVirtualDevice(String str, EnumSet<Capability> enumSet, Map<String, String> map) {
        int i;
        HwLog.e(TAG, "enableVirtualDevice capability is " + enumSet);
        synchronized (VIRTUALIZATION_LOCK) {
            if (str != null) {
                if (!str.isEmpty()) {
                    if (enumSet == null) {
                        HwLog.e(TAG, "parameter is invalid");
                        return -2;
                    } else if (this.mDMSDPAdapter == null) {
                        HwLog.e(TAG, "mDMSDPAdapter is null");
                        return -11;
                    } else {
                        int transfromCapability = transfromCapability(enumSet);
                        if (this.allReqeustServiceTypes.containsKey(str)) {
                            i = this.allReqeustServiceTypes.get(str).intValue();
                            this.allReqeustServiceTypes.remove(str);
                        } else {
                            i = 0;
                        }
                        this.allReqeustServiceTypes.put(str, Integer.valueOf(i | transfromCapability));
                        if (enumSet.contains(Capability.HDMI)) {
                            HwLog.i(TAG, "enter enable HDMI");
                            if (map.containsKey(Constants.HDMI_SOURCE_INDEX)) {
                                this.hdmiInfo.put(Constants.HDMI_SOURCE_INDEX, Integer.valueOf(map.get(Constants.HDMI_SOURCE_INDEX)));
                            }
                            int connectDevice = this.mDMSDPAdapter.connectDevice(4, 14, new DMSDPDevice(str, 3), (Map) null);
                            if (connectDevice != 0) {
                                return ReturnCodeConverter.convertReturnCode(connectDevice);
                            }
                            VirtualDevice virtualDevice = new VirtualDevice();
                            virtualDevice.setDeviceId(str);
                            this.allVirtualDevices.put(str, virtualDevice);
                            HwLog.i(TAG, "hdmiVirtualDevice is created, deviceid = " + str);
                            return 0;
                        }
                        DMSDPDevice dMSDPDevice = this.allDevices.get(str);
                        if (dMSDPDevice == null) {
                            HwLog.e(TAG, "dmsdpDevice is null");
                            return -2;
                        } else if (this.allVirtualDevices.get(str) != null && !this.allVirtualDevices.get(str).getDeviceCapability().containsAll(enumSet)) {
                            HwLog.e(TAG, "dmsdpDevice is null");
                            return -2;
                        } else if (isNeedUpgrade(dMSDPDevice)) {
                            return -13;
                        } else {
                            if (dealDeviceType(dMSDPDevice, str, enumSet, map) == 0) {
                                return 0;
                            }
                            if (!this.allConnectedDevice.containsKey(str)) {
                                int connectDevice2 = this.mDMSDPAdapter.connectDevice(4, getDeviceChannelType(dMSDPDevice), dMSDPDevice, (Map) null);
                                if (connectDevice2 != 0) {
                                    return ReturnCodeConverter.convertReturnCode(connectDevice2);
                                }
                            } else {
                                this.mDMSDPAdapter.requestDeviceService(4, dMSDPDevice, transfromCapability);
                            }
                            setServiceFlag(str, enumSet, map);
                            return 0;
                        }
                    }
                }
            }
            HwLog.e(TAG, "parameter is invalid");
            return -2;
        }
    }

    private int dealDeviceType(DMSDPDevice dMSDPDevice, String str, EnumSet<Capability> enumSet, Map<String, String> map) {
        int deviceType = dMSDPDevice.getDeviceType();
        if (deviceType == 2) {
            HwLog.d(TAG, "the deviceType is Pad");
            this.mDMSDPAdapter.requestDeviceService(3, dMSDPDevice, transfromCapability(enumSet));
            setServiceFlag(str, enumSet, map);
            return 0;
        } else if (deviceType == 4) {
            HwLog.d(TAG, "the deviceType is HICAR");
            this.mDMSDPAdapter.requestDeviceService(1, dMSDPDevice, transfromCapability(enumSet));
            setServiceFlag(str, enumSet, map);
            return 0;
        } else if (deviceType != 7) {
            return -1;
        } else {
            HwLog.d(TAG, "the deviceType is PC");
            this.mDMSDPAdapter.requestDeviceService(2, dMSDPDevice, transfromCapability(enumSet));
            setServiceFlag(str, enumSet, map);
            return 0;
        }
    }

    public int disableVirtualDevice(String str, EnumSet<Capability> enumSet) {
        synchronized (VIRTUALIZATION_LOCK) {
            HwLog.d(TAG, String.format(Locale.ENGLISH, "disable device %s service %s", transformSensitiveInfo(str), enumSet));
            if (str != null) {
                if (!str.isEmpty()) {
                    if (enumSet == null) {
                        HwLog.e(TAG, "parameter is invalid");
                        return -2;
                    } else if (this.mDMSDPAdapter == null) {
                        HwLog.e(TAG, "mDMSDPAdapter is null");
                        return -11;
                    } else if (enumSet.contains(Capability.HDMI)) {
                        stopSingleService(str, enumSet);
                        return 0;
                    } else {
                        DMSDPDevice dMSDPDevice = this.allDevices.get(str);
                        if (dMSDPDevice == null) {
                            HwLog.e(TAG, "dmsdpDevice is null");
                            return -2;
                        } else if (this.allVirtualDevices.get(str) != null && !this.allVirtualDevices.get(str).getDeviceCapability().containsAll(enumSet)) {
                            HwLog.e(TAG, "dmsdpDevice is null");
                            return -2;
                        } else if (!canDisable(str, enumSet)) {
                            HwLog.e(TAG, "some capability not enable in this app");
                            return -16;
                        } else {
                            clearCurAppEnable(str, enumSet);
                            clearRequestServiceType(str, enumSet);
                            if (!enumSet.contains(Capability.DISPLAY) || isCoapDevice(dMSDPDevice)) {
                                stopSingleService(str, enumSet);
                            } else {
                                for (Map.Entry<String, DMSDPDeviceService> entry : this.allRunningServices.entrySet()) {
                                    if (entry.getKey().startsWith(str)) {
                                        DMSDPDeviceService value = entry.getValue();
                                        if (value.getServiceType() == 1) {
                                            this.mDMSDPAdapter.stopDeviceService(4, value, 2);
                                        } else {
                                            this.mDMSDPAdapter.stopDeviceService(4, value, 0);
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

    private boolean isCoapDevice(DMSDPDevice dMSDPDevice) {
        Object properties = dMSDPDevice.getProperties((int) TelephonyUtils.MSG_HAS_OPERATOR_PRIVILEGES);
        if (properties == null) {
            return false;
        }
        String str = TAG;
        HwLog.e(str, "device protocol is:" + properties);
        return ((Integer) properties).intValue() == 64;
    }

    private boolean isBleCamera(DMSDPDevice dMSDPDevice) {
        Object properties;
        if (dMSDPDevice.getDeviceType() == 5 && (properties = dMSDPDevice.getProperties((int) TelephonyUtils.MSG_HAS_OPERATOR_PRIVILEGES)) != null) {
            String str = TAG;
            HwLog.e(str, "device protocol is:" + properties);
            if (((Integer) properties).intValue() == 1) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.msdp.devicevirtualization.VirtualDeviceManager$3  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$ohos$msdp$devicevirtualization$Capability = new int[Capability.values().length];

        static {
            try {
                $SwitchMap$ohos$msdp$devicevirtualization$Capability[Capability.MIC.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$msdp$devicevirtualization$Capability[Capability.SPEAKER.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$msdp$devicevirtualization$Capability[Capability.DISPLAY.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$msdp$devicevirtualization$Capability[Capability.CAMERA.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    private void clearCurAppEnable(String str, EnumSet<Capability> enumSet) {
        Iterator it = enumSet.iterator();
        while (it.hasNext()) {
            int i = AnonymousClass3.$SwitchMap$ohos$msdp$devicevirtualization$Capability[((Capability) it.next()).ordinal()];
            if (i == 1) {
                this.openMicInCurrntApp.put(str, false);
            } else if (i == 2) {
                this.openSpeakerInCurrntApp.put(str, false);
            } else if (i == 3) {
                this.openDisplayInCurrntApp.put(str, false);
            } else if (i == 4) {
                this.openCameraInCurrntApp.put(str, false);
            }
        }
    }

    private void clearRequestServiceType(String str, EnumSet<Capability> enumSet) {
        if (this.allReqeustServiceTypes.containsKey(str)) {
            int transfromCapability = transfromCapability(enumSet);
            int intValue = this.allReqeustServiceTypes.get(str).intValue();
            this.allReqeustServiceTypes.remove(str);
            this.allReqeustServiceTypes.put(str, Integer.valueOf((~transfromCapability) & intValue));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000b  */
    private boolean canDisable(String str, EnumSet<Capability> enumSet) {
        Iterator it = enumSet.iterator();
        while (it.hasNext()) {
            Map<String, Boolean> map = null;
            int i = AnonymousClass3.$SwitchMap$ohos$msdp$devicevirtualization$Capability[((Capability) it.next()).ordinal()];
            if (i == 1 || i == 2) {
                break;
            }
            if (i != 3) {
                if (i == 4) {
                    break;
                }
            } else {
                map = this.openDisplayInCurrntApp;
            }
            if (map == null || !map.containsKey(str) || !map.get(str).booleanValue()) {
                return false;
            }
            while (it.hasNext()) {
            }
        }
        return true;
    }

    public int subscribe(EnumSet<ObserverEventType> enumSet, IVirtualDeviceObserver iVirtualDeviceObserver) {
        HwLog.d(TAG, API_NAME_SUBSCRIBE);
        long currentTimeMillis = System.currentTimeMillis();
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if (iVirtualDeviceObserver == null || enumSet == null || !enumSet.contains(ObserverEventType.VIRTUALDEVICE)) {
            HwLog.e(TAG, "parameter is invalid");
            DMSDPAdapter dMSDPAdapter = this.mDMSDPAdapter;
            if (dMSDPAdapter == null) {
                return -2;
            }
            dMSDPAdapter.reportData(API_NAME_SUBSCRIBE, currentTimeMillis, elapsedRealtime, -2);
            return -2;
        }
        this.virtualDeviceObserver = iVirtualDeviceObserver;
        DMSDPAdapter dMSDPAdapter2 = this.mDMSDPAdapter;
        if (dMSDPAdapter2 == null) {
            return 0;
        }
        dMSDPAdapter2.reportData(API_NAME_SUBSCRIBE, currentTimeMillis, elapsedRealtime, 0);
        return 0;
    }

    public int unsubscribe(EnumSet<ObserverEventType> enumSet, IVirtualDeviceObserver iVirtualDeviceObserver) {
        HwLog.d(TAG, API_NAME_UNSUBSCRIBE);
        long currentTimeMillis = System.currentTimeMillis();
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if (iVirtualDeviceObserver == null || enumSet == null || !enumSet.contains(ObserverEventType.VIRTUALDEVICE)) {
            HwLog.e(TAG, "parameter is invalid");
            DMSDPAdapter dMSDPAdapter = this.mDMSDPAdapter;
            if (dMSDPAdapter == null) {
                return -2;
            }
            dMSDPAdapter.reportData(API_NAME_UNSUBSCRIBE, currentTimeMillis, elapsedRealtime, -2);
            return -2;
        }
        this.virtualDeviceObserver = null;
        DMSDPAdapter dMSDPAdapter2 = this.mDMSDPAdapter;
        if (dMSDPAdapter2 == null) {
            return 0;
        }
        dMSDPAdapter2.reportData(API_NAME_UNSUBSCRIBE, currentTimeMillis, elapsedRealtime, 0);
        return 0;
    }

    public List<String> getVirtualCameraId() {
        ArrayList arrayList;
        synchronized (VIRTUALIZATION_LOCK) {
            arrayList = new ArrayList();
            this.mDMSDPAdapter.getVirtualCameraList(4, arrayList);
        }
        return arrayList;
    }

    public int updateDeviceService(DMSDPDeviceService dMSDPDeviceService, int i, Map<String, Object> map) {
        int updateDeviceService;
        synchronized (VIRTUALIZATION_LOCK) {
            updateDeviceService = this.mDMSDPAdapter.updateDeviceService(4, dMSDPDeviceService, i, map);
        }
        return updateDeviceService;
    }

    private int getDeviceChannelType(DMSDPDevice dMSDPDevice) {
        HwLog.d(TAG, "getDeviceChannelType");
        int deviceType = dMSDPDevice.getDeviceType();
        int i = 6;
        if (deviceType == 3) {
            i = 7;
        } else if (deviceType == 9) {
            i = 11;
        } else if (deviceType == 5) {
            i = 8;
        } else if (deviceType != 6) {
            i = 0;
        }
        if (dMSDPDevice.getProperties(1012) != null) {
            HwLog.e(TAG, "device is hilink");
            i = 9;
        }
        if (isCoapDevice(dMSDPDevice)) {
            HwLog.d(TAG, "device is coap");
            i = 10;
        }
        if (!isBleCamera(dMSDPDevice)) {
            return i;
        }
        HwLog.d(TAG, "device is BleCamera");
        return 4;
    }

    private void stopSingleService(String str, EnumSet<Capability> enumSet) {
        for (Map.Entry<String, DMSDPDeviceService> entry : this.allRunningServices.entrySet()) {
            HwLog.i(TAG, String.format(Locale.ENGLISH, "allRunningServices %s ", Integer.valueOf(entry.getValue().getServiceType())));
            if (entry.getKey().startsWith(str)) {
                DMSDPDeviceService value = entry.getValue();
                if (enumSet.contains(Capability.CAMERA) && entry.getValue().getServiceType() == convertCapabilityToServiceType(Capability.CAMERA)) {
                    HwLog.i(TAG, String.format(Locale.ENGLISH, "stop service SERVICE_TYPE_CAMERA", new Object[0]));
                    this.mDMSDPAdapter.stopDeviceService(4, value, 2);
                } else if (enumSet.contains(Capability.SPEAKER) && entry.getValue().getServiceType() == convertCapabilityToServiceType(Capability.SPEAKER)) {
                    HwLog.i(TAG, String.format(Locale.ENGLISH, "stop service SERVICE_TYPE_SPEAKER", new Object[0]));
                    this.mDMSDPAdapter.stopDeviceService(4, value, 0);
                } else if (enumSet.contains(Capability.MIC) && entry.getValue().getServiceType() == convertCapabilityToServiceType(Capability.MIC)) {
                    HwLog.i(TAG, String.format(Locale.ENGLISH, "stop service SERVICE_TYPE_MIC", new Object[0]));
                    this.mDMSDPAdapter.stopDeviceService(4, value, 0);
                } else if (enumSet.contains(Capability.DISPLAY) && entry.getValue().getServiceType() == convertCapabilityToServiceType(Capability.DISPLAY)) {
                    HwLog.i(TAG, String.format(Locale.ENGLISH, "stop service SERVICE_TYPE_DISPLAY", new Object[0]));
                    this.mDMSDPAdapter.stopDeviceService(4, value, 0);
                } else if (!enumSet.contains(Capability.HDMI) || !(entry.getValue().getServiceType() == 131072 || entry.getValue().getServiceType() == 262144)) {
                    HwLog.e(TAG, "stopSingleService failed!");
                } else {
                    HwLog.i(TAG, String.format(Locale.ENGLISH, "stop service SERVICE_TYPE_HDMI", new Object[0]));
                    this.mDMSDPAdapter.stopDeviceService(4, value, 0);
                }
            }
        }
    }

    private int convertCapabilityToServiceType(Capability capability) {
        int i = AnonymousClass3.$SwitchMap$ohos$msdp$devicevirtualization$Capability[capability.ordinal()];
        if (i == 1) {
            return 2;
        }
        if (i == 2) {
            return 4;
        }
        if (i != 3) {
            return i != 4 ? 0 : 1;
        }
        return 8;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableDeviceService(DMSDPDeviceService dMSDPDeviceService) {
        String str = TAG;
        HwLog.i(str, "startDeviceService start service is " + dMSDPDeviceService.getServiceType());
        synchronized (VIRTUALIZATION_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
            } else if (!this.allReqeustServiceTypes.containsKey(dMSDPDeviceService.getDeviceId())) {
                String str2 = TAG;
                HwLog.e(str2, "Refuse enable this device:" + dMSDPDeviceService.getDeviceName());
            } else {
                if (dMSDPDeviceService.getServiceType() == 1) {
                    startCameraService(dMSDPDeviceService);
                } else if (dMSDPDeviceService.getServiceType() == 2) {
                    startMicService(dMSDPDeviceService);
                } else if (dMSDPDeviceService.getServiceType() == 4) {
                    startSpeakerService(dMSDPDeviceService);
                } else if (dMSDPDeviceService.getServiceType() == 8) {
                    startDisplayService(dMSDPDeviceService);
                } else {
                    if (dMSDPDeviceService.getServiceType() != 131072) {
                        if (dMSDPDeviceService.getServiceType() != 262144) {
                            HwLog.e(TAG, "enableDeviceService failed!");
                        }
                    }
                    startHdmiService(dMSDPDeviceService);
                }
            }
        }
    }

    private void startDisplayService(DMSDPDeviceService dMSDPDeviceService) {
        IVirtualDeviceObserver iVirtualDeviceObserver;
        int startDeviceService = this.mDMSDPAdapter.startDeviceService(4, dMSDPDeviceService, 0, (Map) null);
        String str = TAG;
        HwLog.i(str, "start display service startResult is " + startDeviceService);
        if (startDeviceService != 0 && (iVirtualDeviceObserver = this.virtualDeviceObserver) != null) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()), Capability.DISPLAY, EventTypeConverter.convertEventType(startDeviceService));
            String str2 = TAG;
            HwLog.i(str2, "onDeviceCapabilityStateChange DISPLAY  " + startDeviceService);
        }
    }

    private void startSpeakerService(DMSDPDeviceService dMSDPDeviceService) {
        IVirtualDeviceObserver iVirtualDeviceObserver;
        HwLog.i(TAG, "start speaker service ");
        int startDeviceService = this.mDMSDPAdapter.startDeviceService(4, dMSDPDeviceService, 0, (Map) null);
        if (startDeviceService != 0 && (iVirtualDeviceObserver = this.virtualDeviceObserver) != null) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()), Capability.SPEAKER, EventTypeConverter.convertEventType(startDeviceService));
        }
    }

    private void startMicService(DMSDPDeviceService dMSDPDeviceService) {
        IVirtualDeviceObserver iVirtualDeviceObserver;
        HwLog.i(TAG, "start mic service ");
        int startDeviceService = this.mDMSDPAdapter.startDeviceService(4, dMSDPDeviceService, 0, (Map) null);
        if (startDeviceService != 0 && (iVirtualDeviceObserver = this.virtualDeviceObserver) != null) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()), Capability.MIC, EventTypeConverter.convertEventType(startDeviceService));
        }
    }

    private void startCameraService(DMSDPDeviceService dMSDPDeviceService) {
        IVirtualDeviceObserver iVirtualDeviceObserver;
        HwLog.i(TAG, "start camera service ");
        int startDeviceService = this.mDMSDPAdapter.startDeviceService(4, dMSDPDeviceService, 2, (Map) null);
        if (!(startDeviceService == 0 || (iVirtualDeviceObserver = this.virtualDeviceObserver) == null)) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()), Capability.CAMERA, EventTypeConverter.convertEventType(startDeviceService));
        }
        if (this.cameraInfo != null) {
            Map<String, Object> dealCameraInfo = dealCameraInfo();
            if (dealCameraInfo != null) {
                this.mDMSDPAdapter.updateDeviceService(4, dMSDPDeviceService, (int) EventType.EVENT_DEVICE_CAPABILITY_BUSY, dealCameraInfo);
            }
            HwLog.w(TAG, "cameraParams is null");
        }
        HwLog.w(TAG, "cameraInfo is null");
    }

    private void startHdmiService(DMSDPDeviceService dMSDPDeviceService) {
        int i;
        HwLog.i(TAG, "start Hdmi service ");
        Map<String, Object> map = this.hdmiInfo;
        if (map != null) {
            i = this.mDMSDPAdapter.startDeviceService(4, dMSDPDeviceService, 0, map);
            if (this.hdmiInfo.get(Constants.HDMI_SOURCE_INDEX) != null) {
                String str = TAG;
                HwLog.i(str, "hdmi source id = " + this.hdmiInfo.get(Constants.HDMI_SOURCE_INDEX));
            }
        } else {
            i = -1;
        }
        if (i != 0 && this.virtualDeviceObserver != null) {
            VirtualDevice virtualDevice = new VirtualDevice();
            virtualDevice.setDeviceId(dMSDPDeviceService.getDeviceId());
            this.virtualDeviceObserver.onDeviceCapabilityStateChange(virtualDevice, Capability.HDMI, EventTypeConverter.convertEventType(i));
        }
    }

    private Map<String, Object> dealCameraInfo() {
        HashMap hashMap = new HashMap(8);
        if (this.cameraInfo.containsKey(Constants.AUTO_ORIENTATION)) {
            if (this.cameraInfo.get(Constants.AUTO_ORIENTATION).equals(Constants.AUTO_ORIENTATION_ENABLE)) {
                hashMap.put("VIRCAM_AUTO_ORIENTATION", true);
                hashMap.put("VIRCAM_TYPE", DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else if (this.cameraInfo.get(Constants.AUTO_ORIENTATION).equals(Constants.AUTO_ORIENTATION_DISABLE)) {
                hashMap.put("VIRCAM_AUTO_ORIENTATION", false);
                hashMap.put("VIRCAM_TYPE", DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else {
                HwLog.e(TAG, "the value of AUTO_ORIENTATION is invalid");
            }
        }
        if (this.cameraInfo.containsKey(Constants.DO_MIRROR)) {
            if (this.cameraInfo.get(Constants.DO_MIRROR).equals(Constants.DO_MIRROR_ENABLE)) {
                hashMap.put("VIRCAM_DO_MIRROR", true);
                hashMap.put("VIRCAM_TYPE", DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else if (this.cameraInfo.get(Constants.DO_MIRROR).equals(Constants.DO_MIRROR_DISABLE)) {
                hashMap.put("VIRCAM_DO_MIRROR", false);
                hashMap.put("VIRCAM_TYPE", DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else {
                HwLog.e(TAG, "the value for DO_MIRROR is invalid");
            }
        }
        if (this.cameraInfo.containsKey(Constants.PROCESS_STRATGY)) {
            if (this.cameraInfo.get(Constants.PROCESS_STRATGY).equals(Constants.PROCESS_STRATGY_FULL_CONTENT)) {
                hashMap.put("VIRCAM_PROCESS_STRATGY", DMSDPConfig.FrameProcessStratgy.FULL_CONTENT.name());
                hashMap.put("VIRCAM_TYPE", DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else if (this.cameraInfo.get(Constants.PROCESS_STRATGY).equals(Constants.PROCESS_STRATGY_MAX_SCREEN_FILL)) {
                hashMap.put("VIRCAM_PROCESS_STRATGY", DMSDPConfig.FrameProcessStratgy.MAX_SCREEN_FILL.name());
                hashMap.put("VIRCAM_TYPE", DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else {
                HwLog.e(TAG, "the value for PROCESS_STRATGY is invalid");
            }
        }
        return hashMap;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportDeviceEvent(String str, int i) {
        String str2 = TAG;
        HwLog.i(str2, "reportDeviceEvent, deviceId = " + str);
        if (this.allVirtualDevices.get(str) == null) {
            HwLog.w(TAG, "reportDeviceServiceEvent: device not exist");
        } else if (i == 101) {
            HwLog.d(TAG, "device connect");
        } else if (i == 102) {
            reportDeviceDisconnect(str);
        } else if (i == 103 || i == 108) {
            reportDeviceConnectFail(str);
        } else {
            HwLog.e(TAG, "reportDeviceEvent failed!");
        }
    }

    private void reportDeviceConnectFail(String str) {
        IVirtualDeviceObserver iVirtualDeviceObserver = this.virtualDeviceObserver;
        if (iVirtualDeviceObserver != null) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(str), Capability.DISPLAY, EventType.EVENT_DEVICE_CAPABILITY_ABNORMAL);
        }
    }

    private void reportDeviceDisconnect(String str) {
        HwLog.i(TAG, "reportDeviceDisconnect");
        for (Map.Entry<String, DMSDPDeviceService> entry : this.allRunningServices.entrySet()) {
            if (entry.getKey().startsWith(str)) {
                String str2 = TAG;
                HwLog.i(str2, "service start with" + entry.getKey());
                DMSDPDeviceService value = entry.getValue();
                if (value.getServiceType() == 1) {
                    IVirtualDeviceObserver iVirtualDeviceObserver = this.virtualDeviceObserver;
                    if (iVirtualDeviceObserver != null) {
                        iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(str), Capability.CAMERA, EventType.EVENT_DEVICE_CAPABILITY_DISABLE);
                    }
                } else if (value.getServiceType() == 2) {
                    IVirtualDeviceObserver iVirtualDeviceObserver2 = this.virtualDeviceObserver;
                    if (iVirtualDeviceObserver2 != null) {
                        iVirtualDeviceObserver2.onDeviceCapabilityStateChange(this.allVirtualDevices.get(str), Capability.MIC, EventType.EVENT_DEVICE_CAPABILITY_DISABLE);
                    }
                } else if (value.getServiceType() == 4) {
                    IVirtualDeviceObserver iVirtualDeviceObserver3 = this.virtualDeviceObserver;
                    if (iVirtualDeviceObserver3 != null) {
                        iVirtualDeviceObserver3.onDeviceCapabilityStateChange(this.allVirtualDevices.get(str), Capability.SPEAKER, EventType.EVENT_DEVICE_CAPABILITY_DISABLE);
                    }
                } else if (value.getServiceType() == 8) {
                    IVirtualDeviceObserver iVirtualDeviceObserver4 = this.virtualDeviceObserver;
                    if (iVirtualDeviceObserver4 != null) {
                        iVirtualDeviceObserver4.onDeviceCapabilityStateChange(this.allVirtualDevices.get(str), Capability.DISPLAY, EventType.EVENT_DEVICE_CAPABILITY_DISABLE);
                    }
                } else if (value.getServiceType() == 131072 || value.getServiceType() == 262144) {
                    IVirtualDeviceObserver iVirtualDeviceObserver5 = this.virtualDeviceObserver;
                    if (iVirtualDeviceObserver5 != null) {
                        iVirtualDeviceObserver5.onDeviceCapabilityStateChange(this.allVirtualDevices.get(str), Capability.HDMI, EventType.EVENT_DEVICE_CAPABILITY_DISABLE);
                    }
                } else {
                    HwLog.e(TAG, "reportDeviceDisconnect failed!");
                }
                this.allRunningServices.remove(entry.getKey());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportDeviceServiceEvent(DMSDPDeviceService dMSDPDeviceService, int i) {
        HwLog.i(TAG, "reportDeviceServiceEvent");
        synchronized (VIRTUALIZATION_LOCK) {
            if (dMSDPDeviceService.getServiceType() == 131072 || dMSDPDeviceService.getServiceType() == 262144) {
                reportHdmiEvent(dMSDPDeviceService, i);
            }
            if (this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()) == null) {
                HwLog.w(TAG, "reportDeviceServiceEvent: device not exist");
                return;
            }
            if (dMSDPDeviceService.getServiceType() == 2) {
                reportMicEvent(dMSDPDeviceService, i);
            } else if (dMSDPDeviceService.getServiceType() == 4) {
                reportSpeakerEvent(dMSDPDeviceService, i);
            } else if (dMSDPDeviceService.getServiceType() == 1) {
                reportCameraEvent(dMSDPDeviceService, i);
            } else if (dMSDPDeviceService.getServiceType() == 8) {
                reportDisplayEvent(dMSDPDeviceService, i);
            } else {
                HwLog.e(TAG, "reportDeviceServiceEvent failed!");
            }
        }
    }

    private void reportDisplayEvent(DMSDPDeviceService dMSDPDeviceService, int i) {
        IVirtualDeviceObserver iVirtualDeviceObserver = this.virtualDeviceObserver;
        if (iVirtualDeviceObserver == null) {
            return;
        }
        if (i == 204) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()), Capability.DISPLAY, EventType.EVENT_DEVICE_CAPABILITY_ENABLE);
        } else if (i == 205) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()), Capability.DISPLAY, EventType.EVENT_DEVICE_CAPABILITY_DISABLE);
        } else if (i == 206) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()), Capability.DISPLAY, EventType.EVENT_DEVICE_CAPABILITY_ABNORMAL);
        } else {
            HwLog.e(TAG, "reportDisplayEvent failed!");
        }
    }

    private void reportHdmiEvent(DMSDPDeviceService dMSDPDeviceService, int i) {
        VirtualDevice virtualDevice = new VirtualDevice();
        virtualDevice.setDeviceId(dMSDPDeviceService.getDeviceId());
        IVirtualDeviceObserver iVirtualDeviceObserver = this.virtualDeviceObserver;
        if (iVirtualDeviceObserver == null) {
            return;
        }
        if (i == 204) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(virtualDevice, Capability.HDMI, EventType.EVENT_DEVICE_CAPABILITY_ENABLE);
        } else if (i == 205) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(virtualDevice, Capability.HDMI, EventType.EVENT_DEVICE_CAPABILITY_DISABLE);
        } else if (i == 206) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(virtualDevice, Capability.HDMI, EventType.EVENT_DEVICE_CAPABILITY_ABNORMAL);
        } else {
            HwLog.e(TAG, "reportHdmiEvent failed!");
        }
    }

    private void reportMicEvent(DMSDPDeviceService dMSDPDeviceService, int i) {
        IVirtualDeviceObserver iVirtualDeviceObserver = this.virtualDeviceObserver;
        if (iVirtualDeviceObserver == null) {
            return;
        }
        if (i == 204) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()), Capability.MIC, EventType.EVENT_DEVICE_CAPABILITY_ENABLE);
        } else if (i == 205) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()), Capability.MIC, EventType.EVENT_DEVICE_CAPABILITY_DISABLE);
        } else if (i == 206) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()), Capability.MIC, EventType.EVENT_DEVICE_CAPABILITY_ABNORMAL);
        } else {
            HwLog.e(TAG, "reportMicEvent failed!");
        }
    }

    private void reportSpeakerEvent(DMSDPDeviceService dMSDPDeviceService, int i) {
        IVirtualDeviceObserver iVirtualDeviceObserver = this.virtualDeviceObserver;
        if (iVirtualDeviceObserver == null) {
            return;
        }
        if (i == 204) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()), Capability.SPEAKER, EventType.EVENT_DEVICE_CAPABILITY_ENABLE);
        } else if (i == 205) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()), Capability.SPEAKER, EventType.EVENT_DEVICE_CAPABILITY_DISABLE);
        } else if (i == 206) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()), Capability.SPEAKER, EventType.EVENT_DEVICE_CAPABILITY_ABNORMAL);
        } else {
            HwLog.e(TAG, "reportSpeakerEvent failed!");
        }
    }

    private void reportCameraEvent(DMSDPDeviceService dMSDPDeviceService, int i) {
        IVirtualDeviceObserver iVirtualDeviceObserver = this.virtualDeviceObserver;
        if (iVirtualDeviceObserver == null) {
            return;
        }
        if (i == 204) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()), Capability.CAMERA, EventType.EVENT_DEVICE_CAPABILITY_ENABLE);
        } else if (i == 205) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()), Capability.CAMERA, EventType.EVENT_DEVICE_CAPABILITY_DISABLE);
        } else if (i == 206) {
            iVirtualDeviceObserver.onDeviceCapabilityStateChange(this.allVirtualDevices.get(dMSDPDeviceService.getDeviceId()), Capability.CAMERA, EventType.EVENT_DEVICE_CAPABILITY_ABNORMAL);
        } else {
            HwLog.e(TAG, "reportCameraEvent failed!");
        }
    }

    private static String transformSensitiveInfo(String str) {
        return transformSensitiveInfo(str, 2, 2, true);
    }

    private static String transformSensitiveInfo(String str, int i, int i2, boolean z) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(1);
        int length = str.length();
        if ((length - i) - i2 > 0) {
            String substring = str.substring(0, i);
            String substring2 = str.substring(length - i2);
            sb.append(substring);
            sb.append(SENSITIVE_INFO);
            sb.append(substring2);
        } else if (z) {
            if (length <= i || i <= 0) {
                sb.append(str.substring(0, 1));
                sb.append(SENSITIVE_INFO);
            } else {
                sb.append(str.substring(0, i));
                sb.append(SENSITIVE_INFO);
            }
        } else if (length <= i2 || i2 <= 0) {
            sb.append(SENSITIVE_INFO);
            sb.append(str.substring(str.length() - 1));
        } else {
            sb.append(SENSITIVE_INFO);
            sb.append(str.substring(length - i2));
        }
        return sb.toString();
    }

    private boolean isNeedUpgrade(DMSDPDevice dMSDPDevice) {
        if (dMSDPDevice.getProperties(1012) == null) {
            HwLog.e(TAG, "device is not hilink");
            return false;
        }
        Object properties = dMSDPDevice.getProperties(1011);
        if (properties == null) {
            HwLog.e(TAG, "get device protocol error, proObj is null");
            return false;
        } else if (!(properties instanceof Integer)) {
            HwLog.e(TAG, "protocol is wrong type");
            return false;
        } else if (((Integer) properties).intValue() == -1) {
            return true;
        } else {
            return false;
        }
    }

    private int transfromCapability(EnumSet<Capability> enumSet) {
        int i = enumSet.contains(Capability.CAMERA) ? 1 : 0;
        if (enumSet.contains(Capability.MIC)) {
            i |= 2;
        }
        if (enumSet.contains(Capability.SPEAKER)) {
            i |= 4;
        }
        if (enumSet.contains(Capability.DISPLAY)) {
            i |= 8;
        }
        return enumSet.contains(Capability.HDMI) ? i | 131072 | 262144 : i;
    }

    private void setServiceFlag(String str, EnumSet<Capability> enumSet, Map<String, String> map) {
        String str2 = TAG;
        HwLog.d(str2, "capability is " + enumSet.toString());
        if (enumSet.contains(Capability.CAMERA)) {
            this.openCameraInCurrntApp.put(str, true);
            this.cameraInfo = map;
        }
        if (enumSet.contains(Capability.MIC)) {
            this.openMicInCurrntApp.put(str, true);
        }
        if (enumSet.contains(Capability.SPEAKER)) {
            this.openSpeakerInCurrntApp.put(str, true);
        }
        if (enumSet.contains(Capability.DISPLAY)) {
            this.openDisplayInCurrntApp.put(str, true);
        }
    }
}
