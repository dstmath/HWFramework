package com.huawei.dmsdpsdk2.hiplay;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.text.TextUtils;
import com.huawei.android.hwpartdevicevirtualization.BuildConfig;
import com.huawei.dmsdp.devicevirtualization.Capability;
import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.DMSDPAdapterCallback;
import com.huawei.dmsdpsdk2.DMSDPAdapterProxy;
import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.DMSDPListener;
import com.huawei.dmsdpsdk2.DataListener;
import com.huawei.dmsdpsdk2.DeviceParameterConst;
import com.huawei.dmsdpsdk2.DiscoverListener;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.common.Utils;
import com.huawei.dmsdpsdk2.hiplay.AllConnectReporter;
import com.huawei.dmsdpsdk2.hiplay.HiPlayHelper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class HiPlayAdapter {
    private static final int COLLECTION_SIZE = 8;
    private static final String EVENT_KEY = "event";
    private static final int EVENT_MARK_HIPLAY_AFTER_SERVICE = 1;
    private static final int EVENT_MARK_HIPLAY_BEFORE_SERVICE = 2;
    private static final int EVENT_UNMARK_HIPLAY = 3;
    private static final Object HIPLAY_DISCOVERY_LOCK = new Object();
    private static final Object HIPLAY_LOCK = new Object();
    private static final Object HIPLAY_MSG_LOCK = new Object();
    private static final String TAG = HiPlayAdapter.class.getSimpleName();
    private static final int TYPE_INVALID = -1;
    private static final String VERSION_KEY = "version";
    private static final int VERSION_VALUE = 1;
    private static HiPlayAdapter hiPlayAdapter;
    private static HiPlayAdapterCallback hiPlayAdapterCallback;
    private static HiPlayHelper sHiPlayHelper;
    private ConcurrentHashMap<String, DMSDPDevice> allDMSDPDevices;
    private Map<String, HiPlayDevice> allHiPlayDevices;
    private ConcurrentHashMap<String, Integer> channelState;
    private DataListener dataListener;
    private ConcurrentHashMap<String, Integer> deviceServiceState;
    private ConcurrentHashMap<String, Integer> deviceState;
    private DiscoverListener discoverListener;
    private DMSDPListener dmsdpListener;
    private List<String> hiplayNeedDeviceList;
    private DMSDPAdapter mDMSDPAdapter;
    private List<HiPlayHelper.DevSvrId> speakerServiceList;
    private ConcurrentHashMap<String, DMSDPDeviceService> usedServices;

    public interface HiPlayAdapterCallback {
        void onAdapterGet(HiPlayAdapter hiPlayAdapter);

        void onBinderDied();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDeviceServiceFound(DMSDPDeviceService deviceService) {
        HwLog.i(TAG, String.format(Locale.ENGLISH, "found device service, device id: %s, service id: %s, service type: %d", HiPlayHelper.transSensiInfo(deviceService.getDeviceId()), HiPlayHelper.transSensiInfo(deviceService.getServiceId()), Integer.valueOf(deviceService.getServiceType())));
        if (sHiPlayHelper.hasEnableTaskRunning() && this.allHiPlayDevices.get(deviceService.getDeviceId()) != null) {
            HwLog.i(TAG, "HiPlay enable task found service, enable it");
            enableDeviceService(deviceService);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDeviceServiceStart(DMSDPDeviceService deviceService) {
        HwLog.i(TAG, String.format(Locale.ENGLISH, "start device service success, device id: %s, service id: %s, service type: %d", HiPlayHelper.transSensiInfo(deviceService.getDeviceId()), HiPlayHelper.transSensiInfo(deviceService.getServiceId()), Integer.valueOf(deviceService.getServiceType())));
        ConcurrentHashMap<String, Integer> concurrentHashMap = this.deviceServiceState;
        concurrentHashMap.put(deviceService.getDeviceId() + ":" + deviceService.getServiceType(), 1);
        ConcurrentHashMap<String, DMSDPDeviceService> concurrentHashMap2 = this.usedServices;
        concurrentHashMap2.put(deviceService.getDeviceId() + ":" + deviceService.getServiceType(), deviceService);
        this.speakerServiceList.add(new HiPlayHelper.DevSvrId(deviceService.getDeviceId(), deviceService.getServiceId(), deviceService.getServiceType()));
        checkAndUpdateDeviceConnState();
        if (sHiPlayHelper.hasEnableTaskRunning() && this.allHiPlayDevices.get(deviceService.getDeviceId()) != null) {
            sHiPlayHelper.sendMessageToHiPlayRetHandler(1, deviceService.getServiceType(), 204, this.allHiPlayDevices.get(deviceService.getDeviceId()));
        }
        DMSDPDevice dmsdpDevice = this.allDMSDPDevices.get(deviceService.getDeviceId());
        if (dmsdpDevice == null) {
            HwLog.e(TAG, String.format(Locale.ENGLISH, "can not find device by id: %s", HiPlayHelper.transSensiInfo(deviceService.getDeviceId())));
            return;
        }
        String str = TAG;
        HwLog.i(str, "get dmsdpdevice: " + dmsdpDevice.toString());
        int ret = sendDMSDPDeviceData(dmsdpDevice, 1);
        String str2 = TAG;
        HwLog.i(str2, "mDMSDPAdapter.sendData: " + ret);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDeviceServiceAbnormal(DMSDPDeviceService deviceService) {
        HwLog.i(TAG, String.format(Locale.ENGLISH, "device service abnormal, device id: %s, service id: %s, service type: %d", HiPlayHelper.transSensiInfo(deviceService.getDeviceId()), HiPlayHelper.transSensiInfo(deviceService.getServiceId()), Integer.valueOf(deviceService.getServiceType())));
        ConcurrentHashMap<String, Integer> concurrentHashMap = this.deviceServiceState;
        concurrentHashMap.remove(deviceService.getDeviceId() + ":" + deviceService.getServiceType());
        if (this.allHiPlayDevices.get(deviceService.getDeviceId()) != null) {
            sHiPlayHelper.sendMessageToHiPlayRetHandler(2, deviceService.getServiceType(), 206, this.allHiPlayDevices.get(deviceService.getDeviceId()));
            return;
        }
        HwLog.w(TAG, "not hiplay enable/disable service abnormal, just report it");
        sHiPlayHelper.reportHiPlayDeviceState(deviceService.getDeviceId(), HiPlayHelper.convertSericeTypeToCapability(deviceService.getServiceType()), DeviceState.NO_CONTINUTING);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleServiceStop(DMSDPDeviceService deviceService) {
        HwLog.i(TAG, String.format(Locale.ENGLISH, "stop service success, device id: %s, service id: %s, service type: %d", HiPlayHelper.transSensiInfo(deviceService.getDeviceId()), HiPlayHelper.transSensiInfo(deviceService.getServiceId()), Integer.valueOf(deviceService.getServiceType())));
        DMSDPDevice tmpDmsdpDevice = this.allDMSDPDevices.get(deviceService.getDeviceId());
        if (tmpDmsdpDevice != null) {
            HwLog.w(TAG, String.format(Locale.ENGLISH, "After stop service, disconnect device", new Object[0]));
            int channelType = this.channelState.get(tmpDmsdpDevice.getDeviceId()).intValue();
            String str = TAG;
            HwLog.i(str, "current channelType : " + channelType);
            this.mDMSDPAdapter.disconnectDevice(4, channelType, tmpDmsdpDevice);
            this.channelState.remove(tmpDmsdpDevice.getDeviceId());
        }
        ConcurrentHashMap<String, Integer> concurrentHashMap = this.deviceServiceState;
        concurrentHashMap.remove(deviceService.getDeviceId() + ":" + deviceService.getServiceType());
        ConcurrentHashMap<String, DMSDPDeviceService> concurrentHashMap2 = this.usedServices;
        concurrentHashMap2.remove(deviceService.getDeviceId() + ":" + deviceService.getServiceType());
    }

    private HiPlayAdapter(DMSDPAdapter adapter) {
        this.allDMSDPDevices = new ConcurrentHashMap<>(8);
        this.allHiPlayDevices = new HashMap(8);
        this.usedServices = new ConcurrentHashMap<>(8);
        this.deviceState = new ConcurrentHashMap<>(8);
        this.deviceServiceState = new ConcurrentHashMap<>(8);
        this.channelState = new ConcurrentHashMap<>(8);
        this.speakerServiceList = new ArrayList(8);
        this.hiplayNeedDeviceList = new ArrayList(8);
        this.discoverListener = new DiscoverListener() {
            /* class com.huawei.dmsdpsdk2.hiplay.HiPlayAdapter.AnonymousClass1 */

            @Override // com.huawei.dmsdpsdk2.DiscoverListener
            public void onDeviceFound(DMSDPDevice device) {
                HwLog.i(HiPlayAdapter.TAG, String.format(Locale.ENGLISH, "HiPlayAdapter find device: %s, type: %d, name: %s", HiPlayHelper.transSensiInfo(device.getDeviceId()), Integer.valueOf(device.getDeviceType()), HiPlayHelper.transSensiInfo(device.getDeviceName())));
                if (!HiPlayUtils.isHiPlayNeedDevice(device.getDeviceType())) {
                    HwLog.i(HiPlayAdapter.TAG, "Not HiPlay Need Device, skip it");
                } else if (HiPlayAdapter.sHiPlayHelper.getActiveOptParam() == null || !HiPlayAdapter.sHiPlayHelper.getActiveOptParam().getDeviceId().equals(device.getDeviceId())) {
                    HwLog.i(HiPlayAdapter.TAG, "connection params is null or found dev id not match the enable id, skip it");
                } else {
                    HwLog.i(HiPlayAdapter.TAG, "Found HiPlay enabled device");
                    HiPlayAdapter.sHiPlayHelper.notifyDeviceFound();
                    HiPlayAdapter.this.allDMSDPDevices.put(device.getDeviceId(), device);
                    HiPlayAdapter.this.allHiPlayDevices.put(device.getDeviceId(), new HiPlayDevice(device));
                    HiPlayAdapter.sHiPlayHelper.sendMessageToDeviceOptHandler(1, HiPlayAdapter.sHiPlayHelper.getActiveOptParam());
                }
            }

            @Override // com.huawei.dmsdpsdk2.DiscoverListener
            public void onDeviceLost(DMSDPDevice device) {
                if (!HiPlayUtils.isHiPlayNeedDevice(device.getDeviceType())) {
                    HwLog.i(HiPlayAdapter.TAG, "Not HiPlay Need Device, skip it");
                    return;
                }
                HiPlayAdapter.this.mDMSDPAdapter.unRegisterDataListener(4, device, 16);
                HiPlayAdapter.this.mDMSDPAdapter.unRegisterDataListener(4, device, 15);
                HiPlayAdapter.this.mDMSDPAdapter.unRegisterDataListener(4, device, 19);
                HiPlayAdapter.this.allDMSDPDevices.remove(device.getDeviceId());
                HiPlayAdapter.this.allHiPlayDevices.remove(device.getDeviceId());
                HiPlayAdapter.sHiPlayHelper.reportHiPlayDeviceState(device.getDeviceId(), DeviceState.NO_CONTINUTING);
                HwLog.i(HiPlayAdapter.TAG, String.format(Locale.ENGLISH, "HiPlayAdapter onDeviceLost, device id: %s", HiPlayHelper.transSensiInfo(device.getDeviceId())));
            }

            @Override // com.huawei.dmsdpsdk2.DiscoverListener
            public void onDeviceUpdate(DMSDPDevice device, int action) {
                HwLog.i(HiPlayAdapter.TAG, String.format(Locale.ENGLISH, "HiPlayAdapter onDeviceUpdate, device id: %s, type: %d, action: %d", HiPlayHelper.transSensiInfo(device.getDeviceId()), Integer.valueOf(device.getDeviceType()), Integer.valueOf(action)));
            }

            @Override // com.huawei.dmsdpsdk2.DiscoverListener
            public void onStateChanged(int state, Map<String, Object> map) {
                HwLog.i(HiPlayAdapter.TAG, String.format(Locale.ENGLISH, "HiPlayAdapter onStateChanged, state: %d", Integer.valueOf(state)));
            }
        };
        this.dmsdpListener = new DMSDPListener() {
            /* class com.huawei.dmsdpsdk2.hiplay.HiPlayAdapter.AnonymousClass2 */

            @Override // com.huawei.dmsdpsdk2.DMSDPListener
            public void onDeviceChange(DMSDPDevice device, int state, Map<String, Object> map) {
                if (device == null) {
                    HwLog.i(HiPlayAdapter.TAG, "onDeviceChange device is null");
                    return;
                }
                HwLog.i(HiPlayAdapter.TAG, String.format(Locale.ENGLISH, "onDeviceChange device name: %s, state: %d", HiPlayHelper.transSensiInfo(device.getDeviceName()), Integer.valueOf(state)));
                if (!HiPlayUtils.isHiPlayNeedDevice(device.getDeviceType())) {
                    HwLog.i(HiPlayAdapter.TAG, "Not HiPlay Need Device, skip it");
                    return;
                }
                if (!HiPlayAdapter.this.allHiPlayDevices.containsKey(device.getDeviceId())) {
                    String str = HiPlayAdapter.TAG;
                    HwLog.w(str, "can not find device id: " + HiPlayHelper.transSensiInfo(device.getDeviceId()) + ", cache it");
                    HiPlayAdapter.this.allHiPlayDevices.put(device.getDeviceId(), new HiPlayDevice(device));
                    HiPlayAdapter.this.allDMSDPDevices.put(device.getDeviceId(), device);
                }
                DMSDPDevice tmpDmsdpDevice = (DMSDPDevice) HiPlayAdapter.this.allDMSDPDevices.get(device.getDeviceId());
                if (state == 101) {
                    HiPlayAdapter.this.handleDmsdpConnect(tmpDmsdpDevice);
                } else if (state == 102) {
                    HiPlayAdapter.this.handleDmsdpDisconnect(tmpDmsdpDevice);
                } else if (state == 103) {
                    HiPlayAdapter.this.handleDmsdpConnectFailed(tmpDmsdpDevice);
                } else {
                    String str2 = HiPlayAdapter.TAG;
                    HwLog.i(str2, "do not deal event: " + state);
                }
            }

            @Override // com.huawei.dmsdpsdk2.DMSDPListener
            public void onDeviceServiceChange(DMSDPDeviceService deviceService, int state, Map<String, Object> map) {
                HwLog.i(HiPlayAdapter.TAG, String.format(Locale.ENGLISH, "onDeviceServiceUpdate id: %s, type: %d, stat: %d", HiPlayHelper.transSensiInfo(deviceService.getDeviceId()), Integer.valueOf(deviceService.getServiceType()), Integer.valueOf(state)));
                if (!HiPlayAdapter.this.allHiPlayDevices.containsKey(deviceService.getDeviceId())) {
                    HwLog.i(HiPlayAdapter.TAG, "device list is not contains this one");
                } else if (!HiPlayUtils.isHiPlayNeedService(deviceService.getServiceType())) {
                    HwLog.i(HiPlayAdapter.TAG, "Not HiPlay need service, ignor it");
                } else if (state != 201) {
                    switch (state) {
                        case 204:
                            HiPlayAdapter.this.onDeviceServiceStart(deviceService);
                            return;
                        case 205:
                            HiPlayAdapter.this.handleServiceStop(deviceService);
                            return;
                        case 206:
                            HiPlayAdapter.this.onDeviceServiceAbnormal(deviceService);
                            return;
                        default:
                            return;
                    }
                } else {
                    HiPlayAdapter.this.onDeviceServiceFound(deviceService);
                }
            }
        };
        this.dataListener = new DataListener() {
            /* class com.huawei.dmsdpsdk2.hiplay.HiPlayAdapter.AnonymousClass3 */

            /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00c0: APUT  
              (r2v1 java.lang.Object[])
              (2 ??[int, float, short, byte, char])
              (wrap: java.lang.Integer : 0x00bc: INVOKE  (r4v2 java.lang.Integer) = (r4v1 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
             */
            @Override // com.huawei.dmsdpsdk2.DataListener
            public void onDataReceive(DMSDPDevice device, int dataType, byte[] data) {
                if (dataType == 26) {
                    String str = HiPlayAdapter.TAG;
                    HwLog.i(str, "dataType is: " + dataType);
                    String dataStr = new String(data, StandardCharsets.UTF_8);
                    String str2 = HiPlayAdapter.TAG;
                    HwLog.i(str2, "dataStr is: " + dataStr);
                    if (TextUtils.isEmpty(dataStr)) {
                        HwLog.e(HiPlayAdapter.TAG, "can not analyze received data");
                        return;
                    }
                    List<String> deviceIdList = HiPlayAdapter.sHiPlayHelper.reportDeviceStatus(dataStr);
                    if (deviceIdList != null && deviceIdList.size() > 0) {
                        for (String deviceId : deviceIdList) {
                            if (HiPlayAdapter.this.getDevicesState(deviceId) == 0) {
                                String str3 = HiPlayAdapter.TAG;
                                HwLog.i(str3, "remove deviceid from devicesState: " + HiPlayHelper.transSensiInfo(deviceId));
                                HiPlayAdapter.this.deviceState.remove(deviceId);
                            }
                        }
                        return;
                    }
                    return;
                }
                String dataStr2 = HiPlayAdapter.TAG;
                Locale locale = Locale.ENGLISH;
                Object[] objArr = new Object[3];
                int i = 0;
                objArr[0] = HiPlayHelper.transSensiInfo(device.getDeviceId());
                objArr[1] = Integer.valueOf(dataType);
                if (data != null) {
                    i = data.length;
                }
                objArr[2] = Integer.valueOf(i);
                HwLog.i(dataStr2, String.format(locale, "Receive data, device id: %s, dataType: %d, datalen: %d", objArr));
                HiPlayDevice hiplayDevice = (HiPlayDevice) HiPlayAdapter.this.allHiPlayDevices.get(device.getDeviceId());
                if (hiplayDevice == null) {
                    HwLog.e(HiPlayAdapter.TAG, "Can not find hipaly device");
                } else if (dataType != 15 && dataType != 16 && dataType != 19) {
                    HwLog.w(HiPlayAdapter.TAG, "Not hiplay data, drop it");
                } else if (HiPlayAdapter.sHiPlayHelper.getHiPlayListener() != null) {
                    HiPlayAdapter.sHiPlayHelper.getHiPlayListener().onDataReceive(hiplayDevice, dataType, data);
                }
            }
        };
        synchronized (HIPLAY_LOCK) {
            this.mDMSDPAdapter = adapter;
            IInterface dmsdpService = adapter.getDMSDPService();
            if (dmsdpService != null) {
                try {
                    dmsdpService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        /* class com.huawei.dmsdpsdk2.hiplay.HiPlayAdapter.AnonymousClass4 */

                        @Override // android.os.IBinder.DeathRecipient
                        public void binderDied() {
                            HwLog.i(HiPlayAdapter.TAG, "HiPlay Adapter onBinderDied");
                            if (HiPlayAdapter.hiPlayAdapterCallback != null) {
                                HiPlayAdapter.hiPlayAdapterCallback.onBinderDied();
                            }
                            HiPlayAdapter.this.releaseInstance();
                            DMSDPAdapter.disableVirtualAudio();
                        }
                    }, 0);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "VideoCall service linkToDeath RemoteException");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void handleDmsdpConnect(DMSDPDevice tmpDmsdpDevice) {
        this.deviceState.put(tmpDmsdpDevice.getDeviceId(), 1);
        if (!this.hiplayNeedDeviceList.contains(tmpDmsdpDevice.getDeviceId())) {
            HwLog.i(TAG, "save this device id info");
            this.hiplayNeedDeviceList.add(tmpDmsdpDevice.getDeviceId());
            checkAndUpdateDeviceConnState();
        }
        if (sHiPlayHelper.getActiveOptParam() == null) {
            HwLog.i(TAG, "connection params is null, not hiplay enable");
            return;
        }
        for (Integer serviceType : sHiPlayHelper.getActiveOptParam().getServiceTypes()) {
            if (sHiPlayHelper.getActiveOptParam().getDeviceId().equals(tmpDmsdpDevice.getDeviceId())) {
                String str = TAG;
                HwLog.i(str, "request device service, type: " + serviceType);
                if (requestDeviceService(tmpDmsdpDevice, serviceType.intValue()) != 0) {
                    HwLog.d(TAG, "request device service failed");
                    sHiPlayHelper.reportEnableOptFailed();
                    return;
                }
                HwLog.i(TAG, "register data listener for hiplay");
                this.mDMSDPAdapter.registerDataListener(4, tmpDmsdpDevice, 16, this.dataListener);
                this.mDMSDPAdapter.registerDataListener(4, tmpDmsdpDevice, 15, this.dataListener);
            }
        }
    }

    private void checkAndUpdateDeviceConnState() {
        Set<String> removeDeviceId = new HashSet<>();
        Set<HiPlayHelper.DevSvrId> removeService = new HashSet<>();
        for (String devId : this.hiplayNeedDeviceList) {
            for (HiPlayHelper.DevSvrId devSvrId : this.speakerServiceList) {
                if (devId.equals(devSvrId.getDeviceId())) {
                    String str = TAG;
                    HwLog.i(str, "Found device with speaker service enable, report it, deviceId: " + HiPlayHelper.transSensiInfo(devId) + ", serviceId: " + HiPlayHelper.transSensiInfo(devSvrId.getServiceId()));
                    sHiPlayHelper.reportHiPlayDeviceState(devId, Capability.SPEAKER, DeviceState.CONTIUITING);
                    removeDeviceId.add(devId);
                    removeService.add(devSvrId);
                }
            }
        }
        this.hiplayNeedDeviceList.removeAll(removeDeviceId);
        this.speakerServiceList.removeAll(removeService);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void handleDmsdpDisconnect(DMSDPDevice device) {
        this.deviceState.remove(device.getDeviceId());
        Iterator<Map.Entry<String, Integer>> iterator = this.deviceServiceState.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getKey().contains(device.getDeviceId())) {
                iterator.remove();
            }
        }
        Iterator<Map.Entry<String, DMSDPDeviceService>> iterUserServics = this.usedServices.entrySet().iterator();
        while (iterUserServics.hasNext()) {
            if (iterUserServics.next().getKey().contains(device.getDeviceId())) {
                iterUserServics.remove();
            }
        }
        this.deviceState.put(device.getDeviceId(), -1);
        if (this.allHiPlayDevices.get(device.getDeviceId()) != null) {
            HwLog.w(TAG, "hiplay enable/disable service is not null, report disconnect status");
            HiPlayHelper.DeviceOperationParams params = sHiPlayHelper.getActiveOptParam();
            if (params == null) {
                HwLog.w(TAG, "params is null");
                return;
            }
            Set<Integer> serviceTypes = params.getServiceTypes();
            if (serviceTypes == null) {
                HwLog.w(TAG, "serviceTypes of conn params is null");
                return;
            }
            String str = TAG;
            HwLog.i(str, "serviceTypes is: " + serviceTypes);
            for (Integer serType : serviceTypes) {
                sHiPlayHelper.sendMessageToHiPlayRetHandler(1, serType.intValue(), 205, this.allHiPlayDevices.get(device.getDeviceId()));
            }
        } else {
            HwLog.w(TAG, "not hiplay enable/disable service stop, just report it");
            sHiPlayHelper.reportHiPlayDeviceState(device.getDeviceId(), HiPlayHelper.convertSericeTypeToCapability(device.getDeviceType()), DeviceState.NO_CONTINUTING);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDmsdpConnectFailed(DMSDPDevice tmpDmsdpDevice) {
        HwLog.e(TAG, "EVENT_DEVICE_CONNECT_FALIED");
        this.deviceState.put(tmpDmsdpDevice.getDeviceId(), -1);
        if (sHiPlayHelper.getActiveOptParam() == null) {
            HwLog.i(TAG, "not hiplay used");
        }
        sHiPlayHelper.reportHiPlayDeviceState(tmpDmsdpDevice.getDeviceId(), DeviceState.CONTINUTING_FAILED);
        Iterator<Map.Entry<String, Integer>> iterator = this.deviceServiceState.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> next = iterator.next();
            if (next.getKey().contains(tmpDmsdpDevice.getDeviceId())) {
                String[] string = next.getKey().split(":");
                sHiPlayHelper.sendMessageToHiPlayRetHandler(2, Integer.parseInt(string[string.length - 1]), -9, this.allHiPlayDevices.get(tmpDmsdpDevice.getDeviceId()));
                iterator.remove();
            }
        }
    }

    public static void createInstance(Context context, HiPlayAdapterCallback callback) {
        synchronized (HIPLAY_LOCK) {
            HwLog.i(TAG, "HiPlayAdapter createInstance");
            if (callback != null) {
                String str = TAG;
                HwLog.i(str, "Receive HiPlayAdapterCallback addr: " + callback);
                hiPlayAdapterCallback = callback;
                String str2 = TAG;
                HwLog.i(str2, "Saved HiPlayAdapterCallback addr: " + hiPlayAdapterCallback);
                if (hiPlayAdapter != null) {
                    HwLog.d(TAG, "createInstance callback has been exist");
                    synchronized (HIPLAY_MSG_LOCK) {
                        sHiPlayHelper = new HiPlayHelper();
                        sHiPlayHelper.registerHiPlayAdapter(hiPlayAdapter);
                    }
                    callback.onAdapterGet(hiPlayAdapter);
                    return;
                }
                DMSDPAdapter.createInstance(context, new DMSDPAdapterCallback() {
                    /* class com.huawei.dmsdpsdk2.hiplay.HiPlayAdapter.AnonymousClass5 */

                    @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
                    public void onAdapterGet(DMSDPAdapter adapter) {
                        synchronized (HiPlayAdapter.HIPLAY_LOCK) {
                            String str = HiPlayAdapter.TAG;
                            HwLog.w(str, "HiPlayAdapter onAdapterGet " + adapter);
                            if (adapter != null) {
                                if (adapter instanceof DMSDPAdapterProxy) {
                                    synchronized (HiPlayAdapter.HIPLAY_MSG_LOCK) {
                                        HiPlayAdapter unused = HiPlayAdapter.hiPlayAdapter = new HiPlayAdapter(adapter);
                                        HiPlayHelper unused2 = HiPlayAdapter.sHiPlayHelper = new HiPlayHelper();
                                        HiPlayAdapter.sHiPlayHelper.registerHiPlayAdapter(HiPlayAdapter.hiPlayAdapter);
                                    }
                                    String str2 = HiPlayAdapter.TAG;
                                    HwLog.i(str2, "Called HiPlayAdapterCallback addr: " + HiPlayAdapter.hiPlayAdapterCallback);
                                    if (HiPlayAdapter.hiPlayAdapterCallback != null) {
                                        HiPlayAdapter.hiPlayAdapterCallback.onAdapterGet(HiPlayAdapter.hiPlayAdapter);
                                    }
                                    return;
                                }
                            }
                            HwLog.i(HiPlayAdapter.TAG, "DMSDPAdapter is null");
                            HiPlayAdapter.hiPlayAdapterCallback.onBinderDied();
                            DMSDPAdapter.releaseInstance();
                        }
                    }

                    @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
                    public void onBinderDied() {
                        HwLog.e(HiPlayAdapter.TAG, "HiPlay get adapter failed, binder died");
                    }
                });
                return;
            }
            HwLog.e(TAG, "createInstance callback null");
            throw new IllegalArgumentException("createInstance callback null");
        }
    }

    public void releaseInstance() {
        synchronized (HIPLAY_LOCK) {
            HwLog.w(TAG, "HiPlayAdapter releaseInstance");
            DMSDPAdapter.releaseInstance();
            this.mDMSDPAdapter = null;
            sHiPlayHelper.stop();
            sHiPlayHelper.unregisterHiPlayAdapter(this);
            sHiPlayHelper.unRegisterHiPlayListener();
            clearHiPlayAdapter();
            clear();
        }
    }

    private void clearHiPlayAdapter() {
        HwLog.i(TAG, "clear DMSDP listener");
        if (this.mDMSDPAdapter != null) {
            for (Map.Entry<String, DMSDPDevice> entry : this.allDMSDPDevices.entrySet()) {
                this.mDMSDPAdapter.unRegisterDataListener(4, entry.getValue(), 16);
                this.mDMSDPAdapter.unRegisterDataListener(4, entry.getValue(), 15);
                this.mDMSDPAdapter.unRegisterDataListener(4, null, 26);
                this.mDMSDPAdapter.unRegisterDataListener(4, null, 19);
            }
            this.mDMSDPAdapter.unRegisterDMSDPListener(4, this.dmsdpListener);
        }
        sHiPlayHelper.clearHiPlayAdapter();
        clearHiPlayAdapterCb();
    }

    private static void clearHiPlayAdapterCallback() {
        hiPlayAdapterCallback = null;
        hiPlayAdapter = null;
    }

    private void clearHiPlayAdapterCb() {
        clearHiPlayAdapterCallback();
    }

    private void clear() {
        this.allDMSDPDevices.clear();
        this.allHiPlayDevices.clear();
        this.usedServices.clear();
        this.deviceState.clear();
        this.deviceServiceState.clear();
        this.channelState.clear();
    }

    public int registerHiPlayListener(HiPlayListener listener) {
        HwLog.i(TAG, "Register hiplay listener");
        synchronized (HIPLAY_LOCK) {
            if (this.mDMSDPAdapter != null) {
                if (sHiPlayHelper != null) {
                    sHiPlayHelper.registerHiPlayListener(listener);
                    return this.mDMSDPAdapter.registerDMSDPListener(4, this.dmsdpListener);
                }
            }
            HwLog.e(TAG, "mDMSDPAdapter or sHiPlayHelper is null");
            return -2;
        }
    }

    public int unRegisterHiPlayListener() {
        HwLog.i(TAG, "unRegisterDMSDPListener");
        synchronized (HIPLAY_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            sHiPlayHelper.unRegisterHiPlayListener();
            return this.mDMSDPAdapter.unRegisterDMSDPListener(4, this.dmsdpListener);
        }
    }

    public int sendData(HiPlayDevice device, int dataType, byte[] data) {
        String str = TAG;
        HwLog.d(str, "send hipaly data, dataType: " + dataType);
        synchronized (HIPLAY_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            DMSDPDevice dmsdpDevice = this.allDMSDPDevices.get(device.getDeviceId());
            if (dmsdpDevice == null) {
                HwLog.e(TAG, String.format(Locale.ENGLISH, "can not find device by id: %s", HiPlayHelper.transSensiInfo(device.getDeviceId())));
                return -2;
            }
            return this.mDMSDPAdapter.sendData(4, dmsdpDevice, dataType, data);
        }
    }

    public int enableVirtualDevice(HiPlayDevice device, EnumSet<Capability> capability, String connJson) {
        HwLog.i(TAG, "enableVirtualDevice");
        if (!validEnableParams(device, capability, connJson)) {
            HwLog.e(TAG, "enable virtual device param invalid");
            return -2;
        } else if (sHiPlayHelper == null) {
            HwLog.e(TAG, "HiPlay Helper is null, internal error");
            return -1;
        } else {
            reportAllConnectState(device, 1, 0);
            HwLog.i(TAG, String.format(Locale.ENGLISH, "receive enable virtual device task, device id: %s, capability: %s, conn json: %s", HiPlayHelper.transSensiInfo(device.getDeviceId()), Utils.getEnumSetString(capability), connJson));
            HiPlayHelper hiPlayHelper = sHiPlayHelper;
            hiPlayHelper.getClass();
            sHiPlayHelper.addTask(new HiPlayHelper.HiPlayTask(device, capability, connJson, HiPlayHelper.OptMode.ENABLE));
            return 0;
        }
    }

    public int disableVirtualDevice(HiPlayDevice device, EnumSet<Capability> capability) {
        if (!validDisableParams(device, capability)) {
            HwLog.e(TAG, "disable virtual device param invalid");
            return -2;
        } else if (sHiPlayHelper == null) {
            HwLog.e(TAG, "HiPlay Helper is null, internal error");
            return -1;
        } else {
            reportAllConnectState(device, 3, 0);
            HwLog.i(TAG, String.format(Locale.ENGLISH, "receive disable virtual device task, device id: %s, capability : %s", HiPlayHelper.transSensiInfo(device.getDeviceId()), Utils.getEnumSetString(capability)));
            HiPlayHelper hiPlayHelper = sHiPlayHelper;
            hiPlayHelper.getClass();
            sHiPlayHelper.addTask(new HiPlayHelper.HiPlayTask(device, capability, BuildConfig.FLAVOR, HiPlayHelper.OptMode.DISABLE));
            return 0;
        }
    }

    private void reportAllConnectState(HiPlayDevice hiPlayDevice, int state, int errorCode) {
        if (hiPlayDevice == null) {
            HwLog.e(TAG, "hiPlayDevice is invalid");
            return;
        }
        try {
            AllConnectReporter.doReport(this, new AllConnectReporter.Builder().setProcessState(state).setErrorCode(errorCode).setConnectSource(AllConnectReporter.CONNECT_SOURCE_HIPLAY).setCurrentTime(System.currentTimeMillis()).setHiPlayDevice(hiPlayDevice).build());
        } catch (JSONException e) {
            HwLog.e(TAG, "build json failed");
        }
    }

    private boolean validEnableParams(HiPlayDevice device, EnumSet<Capability> capability, String connJson) {
        if (!HiPlayUtils.isHiPlayNeedDevice(device.getDeviceType())) {
            String str = TAG;
            HwLog.i(str, "Device Type invalid, type: " + device.getDeviceType());
            return false;
        }
        Iterator it = capability.iterator();
        while (it.hasNext()) {
            if (!HiPlayUtils.isHiPlayNeedCapability((Capability) it.next())) {
                HwLog.e(TAG, "Not HiPlay Need capability");
                return false;
            }
        }
        if (!HiPlayUtils.checkJsonEmpty(connJson)) {
            return false;
        }
        if (!HiPlayUtils.isDiffAccount(connJson) && !HiPlayUtils.checkJsonHasKeys(connJson, HiPlayUtils.AUTH_MODE, HiPlayUtils.DISCOVER_MODE)) {
            return false;
        }
        return true;
    }

    private boolean validDisableParams(HiPlayDevice device, EnumSet<Capability> capability) {
        if (!HiPlayUtils.isHiPlayNeedDevice(device.getDeviceType())) {
            String str = TAG;
            HwLog.i(str, "Device Type invalid, type: " + device.getDeviceType());
            return false;
        }
        Iterator it = capability.iterator();
        while (it.hasNext()) {
            if (!HiPlayUtils.isHiPlayNeedCapability((Capability) it.next())) {
                HwLog.e(TAG, "Not HiPlay Need capability");
                return false;
            }
        }
        return true;
    }

    private void enableDeviceService(DMSDPDeviceService dmsdpDeviceService) {
        HwLog.i(TAG, String.format(Locale.ENGLISH, "enableDeviceService deviceId: %s, serviceId: %s, type: %d", HiPlayHelper.transSensiInfo(dmsdpDeviceService.getDeviceId()), HiPlayHelper.transSensiInfo(dmsdpDeviceService.getServiceId()), Integer.valueOf(dmsdpDeviceService.getServiceType())));
        ConcurrentHashMap<String, DMSDPDeviceService> concurrentHashMap = this.usedServices;
        concurrentHashMap.put(dmsdpDeviceService.getDeviceId() + ":" + dmsdpDeviceService.getServiceType(), dmsdpDeviceService);
        int result = startDeviceService(dmsdpDeviceService);
        if (result == 0) {
            HwLog.i(TAG, String.format(Locale.ENGLISH, "start service type: %d", Integer.valueOf(dmsdpDeviceService.getServiceType())));
        } else if (result == -5) {
            HwLog.i(TAG, "This device service has been enabled, no need enable again");
            sHiPlayHelper.sendMessageToHiPlayRetHandler(1, dmsdpDeviceService.getServiceType(), 204, this.allHiPlayDevices.get(dmsdpDeviceService.getDeviceId()));
        } else {
            HwLog.d(TAG, "enableDeviceService failed");
            sendDMSDPDeviceData(this.allDMSDPDevices.get(dmsdpDeviceService.getDeviceId()), 3);
            sHiPlayHelper.reportEnableOptFailed();
        }
    }

    /* access modifiers changed from: protected */
    public int startDiscover(int protocol, int deviceFilter, int serviceFilter) {
        synchronized (HIPLAY_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "MSDPAdapter is null");
                return -2;
            } else if (this.discoverListener == null) {
                HwLog.e(TAG, "discoverListener is null");
                return -2;
            } else {
                return this.mDMSDPAdapter.startDiscover(4, protocol, deviceFilter, serviceFilter, this.discoverListener);
            }
        }
    }

    /* access modifiers changed from: protected */
    public int stopDiscover(int protocol) {
        synchronized (HIPLAY_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "MSDPAdapter is null");
                return -2;
            } else if (this.discoverListener == null) {
                HwLog.e(TAG, "discoverListener is null");
                return -2;
            } else {
                return this.mDMSDPAdapter.stopDiscover(4, protocol, this.discoverListener);
            }
        }
    }

    private int connectDevice(int channelType, DMSDPDevice device) {
        synchronized (HIPLAY_LOCK) {
            String str = TAG;
            HwLog.d(str, "connectDevice, device id: " + HiPlayHelper.transSensiInfo(device.getDeviceId()));
            if (this.mDMSDPAdapter == null) {
                HwLog.d(TAG, "connectDevice failed, mDMSDPAdapter is null");
                return -2;
            }
            this.deviceState.put(device.getDeviceId(), 0);
            this.mDMSDPAdapter.registerDataListener(4, device, 19, this.dataListener);
            return this.mDMSDPAdapter.connectDevice(4, channelType, device, null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getDevicesState(String deviceId) {
        String str = TAG;
        HwLog.d(str, "getDevicesState, deviceId: " + HiPlayHelper.transSensiInfo(deviceId));
        if (this.deviceState.containsKey(deviceId)) {
            return this.deviceState.get(deviceId).intValue();
        }
        HwLog.d(TAG, "Can not find state by device id");
        return -1;
    }

    private int getDeviceChannelType(DMSDPDevice device, String authMode, int version) {
        int channelType;
        int deviceType = device.getDeviceType();
        if (deviceType == 3) {
            channelType = getChannelTypeByAuthMode(authMode, version);
        } else if (deviceType != 10) {
            channelType = 9;
        } else {
            channelType = 13;
        }
        HwLog.d(TAG, String.format(Locale.ENGLISH, "getDeviceChannelType, device id: %s, channel type: %d", HiPlayHelper.transSensiInfo(device.getDeviceId()), Integer.valueOf(channelType)));
        return channelType;
    }

    private int getChannelTypeByAuthMode(String authMode, int version) {
        if (TextUtils.isEmpty(authMode)) {
            HwLog.e(TAG, "the value of authMode is empty");
            return -1;
        } else if (authMode.equals(HiPlayUtils.AUTH_MODE_SAME_ACCOUNT_STR)) {
            if (version >= 2) {
                return 14;
            }
            return 10;
        } else if (authMode.equals(HiPlayUtils.AUTH_MODE_DIFF_ACCOUNT_STR)) {
            return 14;
        } else {
            return -1;
        }
    }

    private int disconnectDeviceService(HiPlayDevice device, List<Integer> deviceServiceType) {
        HwLog.i(TAG, String.format(Locale.ENGLISH, "disconnect device service, device id: %s, services: %s", HiPlayHelper.transSensiInfo(device.getDeviceId()), Utils.getListString(deviceServiceType)));
        HiPlayDevice tmpDevice = this.allHiPlayDevices.get(device.getDeviceId());
        if (tmpDevice == null) {
            HwLog.e(TAG, "can not find device info");
            return -2;
        }
        int state = getDevicesState(tmpDevice.getDeviceId());
        if (state == -2) {
            HwLog.e(TAG, "device is in disconnecting");
            return -14;
        } else if (state == -1) {
            HwLog.e(TAG, "device is already disconnected");
            return -9;
        } else if (state != 0) {
            boolean disableRet = true;
            for (Integer num : deviceServiceType) {
                int serviceType = num.intValue();
                ConcurrentHashMap<String, DMSDPDeviceService> concurrentHashMap = this.usedServices;
                DMSDPDeviceService dmsdpDeviceService = concurrentHashMap.get(tmpDevice.getDeviceId() + ":" + serviceType);
                if (dmsdpDeviceService == null) {
                    HwLog.e(TAG, String.format(Locale.ENGLISH, "can not find device service info, device id: %s, serviceType: %d", HiPlayHelper.transSensiInfo(tmpDevice.getDeviceId()), Integer.valueOf(serviceType)));
                    disableRet = false;
                } else {
                    HwLog.e(TAG, String.format(Locale.ENGLISH, "disable device service info, device id: %s, serviceType: %d", HiPlayHelper.transSensiInfo(tmpDevice.getDeviceId()), Integer.valueOf(serviceType)));
                    int ret = stopDeviceService(dmsdpDeviceService);
                    String str = TAG;
                    HwLog.i(str, "stop service result: " + ret);
                    if (ret != 0) {
                        HwLog.d(TAG, "stop service failed");
                        disableRet = false;
                    }
                }
            }
            if (disableRet) {
                return 0;
            }
            return -1;
        } else {
            HwLog.e(TAG, "device is in connecting");
            return -13;
        }
    }

    private int requestDeviceService(DMSDPDevice device, int type) {
        synchronized (HIPLAY_LOCK) {
            HwLog.i(TAG, "requestDeviceService start");
            if (this.mDMSDPAdapter == null) {
                return -2;
            }
            return this.mDMSDPAdapter.requestDeviceService(4, device, type);
        }
    }

    private int sendDMSDPDeviceData(DMSDPDevice device, int event) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(EVENT_KEY, event);
            jsonObject.put(VERSION_KEY, 1);
            int ret = this.mDMSDPAdapter.sendData(4, device, 29, jsonObject.toString().getBytes(StandardCharsets.UTF_8));
            String str = TAG;
            HwLog.i(str, "mDMSDPAdapter.sendData: " + ret);
            return ret;
        } catch (JSONException e) {
            HwLog.i(TAG, "get hiplayevent json failed: ");
            return -1;
        }
    }

    private int startDeviceService(DMSDPDeviceService service) {
        synchronized (HIPLAY_LOCK) {
            HwLog.i(TAG, String.format(Locale.ENGLISH, "startDeviceService start, device id:%s, service id: %s, service type: %d", HiPlayHelper.transSensiInfo(service.getDeviceId()), HiPlayHelper.transSensiInfo(service.getServiceId()), Integer.valueOf(service.getServiceType())));
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            sendDMSDPDeviceData(this.allDMSDPDevices.get(service.getDeviceId()), 2);
            return this.mDMSDPAdapter.startDeviceService(4, service, 0, null);
        }
    }

    private int stopDeviceService(DMSDPDeviceService service) {
        HwLog.i(TAG, "stopDeviceService");
        synchronized (HIPLAY_LOCK) {
            HwLog.i(TAG, String.format(Locale.ENGLISH, "stopDeviceService start, device id:%s, service id: %s, service type: %d", HiPlayHelper.transSensiInfo(service.getDeviceId()), HiPlayHelper.transSensiInfo(service.getServiceId()), Integer.valueOf(service.getServiceType())));
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            sendDMSDPDeviceData(this.allDMSDPDevices.get(service.getDeviceId()), 3);
            return this.mDMSDPAdapter.stopDeviceService(4, service, 0);
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void handleNewDevice(HiPlayHelper.DeviceOperationParams params) {
        this.mDMSDPAdapter.registerDataListener(4, null, 26, this.dataListener);
        if (params == null) {
            HwLog.w(TAG, "params is null");
            return;
        }
        DMSDPDevice tmpDmsdpDevice = this.allDMSDPDevices.get(params.getDeviceId());
        if (tmpDmsdpDevice == null) {
            HwLog.w(TAG, String.format(Locale.ENGLISH, "can not find device by id: %s", HiPlayHelper.transSensiInfo(params.getDeviceId())));
            sHiPlayHelper.reportEnableOptFailed();
            return;
        }
        String jsonData = params.getJsonString();
        tmpDmsdpDevice.addProperties(DeviceParameterConst.AUTH_MODE_JSON_STRING, jsonData);
        tmpDmsdpDevice.addProperties(DeviceParameterConst.DEVICE_DISCOVER_PROTOCOL_INT, Integer.valueOf(params.getDiscoverProc()));
        if (!sHiPlayHelper.isHaveRunningTask().booleanValue()) {
            HwLog.d(TAG, "there is no running task");
            return;
        }
        int channelType = getDeviceChannelType(tmpDmsdpDevice, HiPlayUtils.parseAuthMode(jsonData), HiPlayUtils.parseVersion(jsonData));
        if (channelType == -1) {
            sHiPlayHelper.reportEnableOptFailed();
            return;
        }
        this.channelState.put(tmpDmsdpDevice.getDeviceId(), Integer.valueOf(channelType));
        int ret = connectDevice(channelType, tmpDmsdpDevice);
        HwLog.i(TAG, String.format(Locale.ENGLISH, "hiplay connect device, id: %s, channelType: %d, ret: %d", HiPlayHelper.transSensiInfo(params.getDeviceId()), Integer.valueOf(channelType), Integer.valueOf(ret)));
        if (ret == -8) {
            HwLog.i(TAG, "Found this device has already connected");
            this.dmsdpListener.onDeviceChange(tmpDmsdpDevice, 101, null);
        } else if (ret != 0) {
            HwLog.d(TAG, "hiplay connect device failed");
            sHiPlayHelper.reportEnableOptFailed();
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0098, code lost:
        r1 = th;
     */
    public synchronized void handleDeviceEnable(HiPlayHelper.DeviceOperationParams params) {
        synchronized (HIPLAY_DISCOVERY_LOCK) {
            if (params.getDiscoverProc() == 32) {
                String str = TAG;
                HwLog.i(str, "access mode is " + params.getAccessVersion());
                if (params.getAccessVersion() == HiPlayHelper.AccessVersion.ACCESS_AP) {
                    DMSDPDevice device = new DMSDPDevice(params.getDeviceId(), 10);
                    device.setDeviceName(params.getDeviceName());
                    this.discoverListener.onDeviceFound(device);
                } else if (params.getAccessVersion() == HiPlayHelper.AccessVersion.ACCESS_ROUTER) {
                    sHiPlayHelper.startDiscover();
                } else {
                    sHiPlayHelper.reportEnableOptFailed();
                }
            }
            if (params.getDiscoverProc() == 64) {
                this.discoverListener.onDeviceFound(new DMSDPDevice(params.getDeviceId(), params.getDeviceType()));
            }
            if (params.getDiscoverProc() == 0 && params.getDeviceType() == 3) {
                this.discoverListener.onDeviceFound(new DMSDPDevice(params.getDeviceId(), params.getDeviceType()));
            }
        }
        return;
        while (true) {
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void handleDeviceDisable(HiPlayHelper.DeviceOperationParams params) {
        HwLog.i(TAG, "handleDeviceDisable");
        if (this.allHiPlayDevices.isEmpty()) {
            HwLog.w(TAG, "allHiPlayDevices is empty");
            sHiPlayHelper.reportDisableOptFailed();
            return;
        }
        HiPlayDevice tmpDmsdpDevice = this.allHiPlayDevices.get(params.getDeviceId());
        if (tmpDmsdpDevice == null) {
            HwLog.w(TAG, String.format(Locale.ENGLISH, "device disable, can not find dmsdp device id: %s", HiPlayHelper.transSensiInfo(params.getDeviceId())));
            sHiPlayHelper.reportDisableOptFailed();
            return;
        }
        sHiPlayHelper.stopDiscover();
        List<Integer> serviceList = new ArrayList<>();
        Iterator it = params.getCapability().iterator();
        while (it.hasNext()) {
            int serviceType = Utils.convertCapabilityToServiceType((Capability) it.next());
            if (serviceType != -1) {
                serviceList.add(Integer.valueOf(serviceType));
            }
        }
        int ret = disconnectDeviceService(tmpDmsdpDevice, serviceList);
        if (ret == 0) {
            HwLog.i(TAG, "start stop device service");
            this.deviceState.put(tmpDmsdpDevice.getDeviceId(), -2);
        } else if (ret == -9) {
            HwLog.w(TAG, "This device has been disconnected or being disconnecting");
            for (Integer num : params.getServiceTypes()) {
                sHiPlayHelper.sendMessageToHiPlayRetHandler(1, num.intValue(), 205, tmpDmsdpDevice);
            }
        } else if (ret == -14) {
            HwLog.w(TAG, "This Device is disconnecting, wait for disconnect");
        } else {
            HwLog.d(TAG, "disconnectDeviceService failed");
            HieventHiplayError.reportError(4);
            sHiPlayHelper.reportDisableOptFailed();
        }
    }

    /* access modifiers changed from: protected */
    public ConcurrentHashMap<String, DMSDPDevice> getAllDMSDPDevices() {
        return this.allDMSDPDevices;
    }

    /* access modifiers changed from: protected */
    public Map<String, HiPlayDevice> getAllHiPlayDevices() {
        return this.allHiPlayDevices;
    }
}
