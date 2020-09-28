package com.huawei.dmsdpsdk2.videocall;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.android.hwpartdevicevirtualization.BuildConfig;
import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.DMSDPAdapterCallback;
import com.huawei.dmsdpsdk2.DMSDPConfig;
import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.DMSDPListener;
import com.huawei.dmsdpsdk2.DeviceParameterConst;
import com.huawei.dmsdpsdk2.DiscoverListener;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.IDMSDPAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VideoCallAdapter {
    private static final int COLLECTION_SIZE = 8;
    public static final int CONNECTED = 1;
    public static final int CONNECTING = 0;
    public static final int DISCONNECT = -1;
    public static final int DISCONNECTING = -2;
    public static final int ERROR_CODE_ALREADY_CONNECTING = -1;
    public static final int ERROR_CODE_ALREADY_DISCONNECT = -2;
    public static final int ERROR_CODE_ALREADY_DISCONNECTING = -3;
    public static final int ERROR_CODE_ALREADY_ENABLE = -4;
    public static final int ERROR_CODE_CONNECTED_FAILED = -6;
    public static final int ERROR_CODE_SERVICE_NOT_ENABLE = -5;
    private static final int INT_TWO = 2;
    private static final int PIN_EVENT = 3;
    private static final int SERVICE_FAIL = 2;
    private static final int SERVICE_SUCCESS = 1;
    private static final String TAG = VideoCallAdapter.class.getSimpleName();
    private static final Object VIDEO_CALL_LOCK = new Object();
    private static final int WAIT_TIME1000 = 1000;
    private static final int WAIT_TIME2000 = 2000;
    private static VideoCallAdapter videoCallAdapter;
    private static VideoCallAdapterCallback videoCallAdapterCallback;
    private ConcurrentHashMap<String, VideoCallDevice> allDevices;
    private ConcurrentHashMap<String, Integer> deviceServiceState;
    private ConcurrentHashMap<String, Integer> deviceState;
    private DiscoverListener discoverListener;
    private DMSDPListener dmsdpListener;
    private DMSDPAdapter mDMSDPAdapter;
    private DeviceServiceHandler mHandler;
    private ConcurrentHashMap<Integer, VideoCallDevice> usedServiceDevices;
    private ConcurrentHashMap<String, DMSDPDeviceService> usedServices;
    private VideoCallListener videoCallListener;

    public interface VideoCallAdapterCallback {
        void onAdapterGet(VideoCallAdapter videoCallAdapter);

        void onBinderDied();
    }

    private VideoCallAdapter(DMSDPAdapter adapter) {
        this.allDevices = new ConcurrentHashMap<>(8);
        this.usedServiceDevices = new ConcurrentHashMap<>(8);
        this.usedServices = new ConcurrentHashMap<>(8);
        this.deviceState = new ConcurrentHashMap<>(8);
        this.deviceServiceState = new ConcurrentHashMap<>(8);
        this.mHandler = new DeviceServiceHandler();
        this.discoverListener = new DiscoverListener() {
            /* class com.huawei.dmsdpsdk2.videocall.VideoCallAdapter.AnonymousClass1 */

            @Override // com.huawei.dmsdpsdk2.DiscoverListener
            public void onDeviceFound(DMSDPDevice device) {
                if (VideoCallAdapter.this.videoCallListener != null) {
                    HwLog.i(VideoCallAdapter.TAG, "VideoCallAdapter onDeviceFound");
                    VideoCallDevice videoCallDevice = new VideoCallDevice(device);
                    VideoCallAdapter.this.allDevices.put(device.getDeviceId(), videoCallDevice);
                    VideoCallAdapter.this.videoCallListener.onDeviceFound(videoCallDevice);
                }
            }

            @Override // com.huawei.dmsdpsdk2.DiscoverListener
            public void onDeviceLost(DMSDPDevice device) {
                HwLog.i(VideoCallAdapter.TAG, "VideoCallAdapter onDeviceLost");
            }

            @Override // com.huawei.dmsdpsdk2.DiscoverListener
            public void onDeviceUpdate(DMSDPDevice device, int action) {
            }

            @Override // com.huawei.dmsdpsdk2.DiscoverListener
            public void onStateChanged(int state, Map<String, Object> map) {
            }
        };
        this.dmsdpListener = new DMSDPListener() {
            /* class com.huawei.dmsdpsdk2.videocall.VideoCallAdapter.AnonymousClass2 */

            @Override // com.huawei.dmsdpsdk2.DMSDPListener
            public void onDeviceChange(DMSDPDevice device, int state, Map<String, Object> map) {
                if (device == null) {
                    HwLog.i(VideoCallAdapter.TAG, "onDeviceChange device is null");
                    return;
                }
                String str = VideoCallAdapter.TAG;
                HwLog.i(str, "onDeviceChange device: " + VideoCallAdapter.transformSensitiveInfo(device.getDeviceName()) + " state: " + state);
                if (!VideoCallAdapter.this.allDevices.containsKey(device.getDeviceId())) {
                    String str2 = VideoCallAdapter.TAG;
                    HwLog.i(str2, "allDevice get null " + VideoCallAdapter.transformSensitiveInfo(device.getDeviceId()));
                    return;
                }
                DMSDPDevice tmpDmsdpDevice = ((VideoCallDevice) VideoCallAdapter.this.allDevices.get(device.getDeviceId())).getDmsdpDevice();
                if (tmpDmsdpDevice != null) {
                    if (state == 101) {
                        VideoCallAdapter.this.handleDmsdpConnect(tmpDmsdpDevice);
                    } else if (state == 102) {
                        VideoCallAdapter.this.handleDmsdpDisconnect(tmpDmsdpDevice);
                    } else if (state == 103) {
                        VideoCallAdapter.this.handleDmsdpConnectFailed(tmpDmsdpDevice);
                    } else if (state == 107) {
                        VideoCallAdapter.this.handleDmsdpPinEvent(tmpDmsdpDevice, 107);
                    } else if (state == 108) {
                        VideoCallAdapter.this.handleDmsdpPinEvent(tmpDmsdpDevice, 108);
                    } else if (state == 109) {
                        VideoCallAdapter.this.handleDmsdpPinEvent(tmpDmsdpDevice, DMSDPConfig.EVENT_DEVICE_PIN_SUCCESS);
                    }
                }
            }

            @Override // com.huawei.dmsdpsdk2.DMSDPListener
            public void onDeviceServiceChange(DMSDPDeviceService deviceService, int state, Map<String, Object> map) {
                HwLog.i(VideoCallAdapter.TAG, String.format(Locale.ENGLISH, "onDeviceServiceUpdate name %s %s type:%d", VideoCallAdapter.transformSensitiveInfo(deviceService.getDeviceName()), VideoCallAdapter.transformSensitiveInfo(deviceService.getDeviceId()), Integer.valueOf(deviceService.getServiceType())));
                if (state != 201) {
                    switch (state) {
                        case 204:
                            HwLog.d(VideoCallAdapter.TAG, String.format("start service success device is %s service is %d", VideoCallAdapter.transformSensitiveInfo(deviceService.getDeviceId()), Integer.valueOf(deviceService.getServiceType())));
                            ConcurrentHashMap concurrentHashMap = VideoCallAdapter.this.deviceServiceState;
                            concurrentHashMap.put(deviceService.getDeviceId() + ":" + deviceService.getServiceType(), 1);
                            VideoCallAdapter.this.mHandler.sendMessage(VideoCallAdapter.this.mHandler.obtainMessage(1, deviceService.getServiceType(), 204, VideoCallAdapter.this.allDevices.get(deviceService.getDeviceId())));
                            return;
                        case 205:
                            VideoCallAdapter.this.handleServiceStop(deviceService);
                            return;
                        case 206:
                            HwLog.i(VideoCallAdapter.TAG, String.format("start service failed device is %s service is %d", VideoCallAdapter.transformSensitiveInfo(deviceService.getDeviceId()), Integer.valueOf(deviceService.getServiceType())));
                            ConcurrentHashMap concurrentHashMap2 = VideoCallAdapter.this.deviceServiceState;
                            concurrentHashMap2.remove(deviceService.getDeviceId() + ":" + deviceService.getServiceType());
                            VideoCallAdapter.this.usedServiceDevices.remove(Integer.valueOf(deviceService.getServiceType()));
                            VideoCallAdapter.this.mHandler.sendMessage(VideoCallAdapter.this.mHandler.obtainMessage(2, deviceService.getServiceType(), 206, VideoCallAdapter.this.allDevices.get(deviceService.getDeviceId())));
                            return;
                        default:
                            return;
                    }
                } else {
                    VideoCallAdapter.this.enableDeviceService(deviceService);
                }
            }
        };
        synchronized (VIDEO_CALL_LOCK) {
            this.mDMSDPAdapter = adapter;
            IDMSDPAdapter dmsdpService = adapter.getDMSDPService();
            if (dmsdpService != null) {
                try {
                    dmsdpService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        /* class com.huawei.dmsdpsdk2.videocall.VideoCallAdapter.AnonymousClass3 */

                        public void binderDied() {
                            HwLog.i(VideoCallAdapter.TAG, "VideoCallAdapter onBinderDied");
                            VideoCallAdapter.videoCallAdapterCallback.onBinderDied();
                            VideoCallAdapter.this.releaseInstance();
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
    private void handleServiceStop(DMSDPDeviceService deviceService) {
        HwLog.i(TAG, String.format("stop service success device is %s service is %d", transformSensitiveInfo(deviceService.getDeviceId()), Integer.valueOf(deviceService.getServiceType())));
        ConcurrentHashMap<String, Integer> concurrentHashMap = this.deviceServiceState;
        concurrentHashMap.remove(deviceService.getDeviceId() + ":" + deviceService.getDeviceId());
        this.usedServices.remove(deviceService.getDeviceId());
        this.usedServiceDevices.remove(Integer.valueOf(deviceService.getServiceType()));
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, deviceService.getServiceType(), 205, this.allDevices.get(deviceService.getDeviceId())));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void handleDmsdpConnect(DMSDPDevice tmpDmsdpDevice) {
        this.deviceState.put(tmpDmsdpDevice.getDeviceId(), 1);
        ConcurrentHashMap<String, Integer> concurrentHashMap = this.deviceServiceState;
        if (concurrentHashMap.get(tmpDmsdpDevice.getDeviceId() + ":" + 8) != null) {
            ConcurrentHashMap<String, DMSDPDeviceService> concurrentHashMap2 = this.usedServices;
            concurrentHashMap2.put(tmpDmsdpDevice.getDeviceId() + ":" + 8, new DMSDPDeviceService(tmpDmsdpDevice.getDeviceId(), tmpDmsdpDevice.getDeviceName(), null, 8));
            ConcurrentHashMap<String, Integer> concurrentHashMap3 = this.deviceServiceState;
            concurrentHashMap3.put(tmpDmsdpDevice.getDeviceId() + ":" + 8, 1);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, 8, 204, this.allDevices.get(tmpDmsdpDevice.getDeviceId())));
        }
        if (tmpDmsdpDevice.getDeviceType() == 3) {
            if (this.usedServiceDevices.isEmpty()) {
                HwLog.e(TAG, "usedServiceDevices.isEmpty()");
            }
            if (tmpDmsdpDevice.getProperties(DeviceParameterConst.DEVICE_ISHUAWEITV_BOOLEAN) == null) {
                HwLog.e(TAG, "DEVICE_ISHUAWEITV_BOOLEAN is null");
                return;
            }
            Object isHuaweiTv = tmpDmsdpDevice.getProperties(DeviceParameterConst.DEVICE_ISHUAWEITV_BOOLEAN);
            if ((isHuaweiTv instanceof Boolean) && ((Boolean) isHuaweiTv).booleanValue()) {
                for (Map.Entry<Integer, VideoCallDevice> deviceEntry : this.usedServiceDevices.entrySet()) {
                    if (deviceEntry.getValue().getDmsdpDevice().getDeviceId().equals(tmpDmsdpDevice.getDeviceId())) {
                        requestDeviceService(tmpDmsdpDevice, deviceEntry.getKey().intValue());
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void handleDmsdpDisconnect(DMSDPDevice device) {
        this.deviceState.remove(device.getDeviceId());
        ConcurrentHashMap<String, Integer> concurrentHashMap = this.deviceServiceState;
        if (concurrentHashMap.get(device.getDeviceId() + ":" + 8) != null) {
            ConcurrentHashMap<String, Integer> concurrentHashMap2 = this.deviceServiceState;
            concurrentHashMap2.put(device.getDeviceId() + ":" + 8, -1);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, 8, 205, this.allDevices.get(device.getDeviceId())));
        }
        Iterator<Map.Entry<String, Integer>> iterator = this.deviceServiceState.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getKey().contains(device.getDeviceId())) {
                iterator.remove();
            }
        }
        Iterator<Map.Entry<Integer, VideoCallDevice>> iterUserServiceDev = this.usedServiceDevices.entrySet().iterator();
        while (iterUserServiceDev.hasNext()) {
            if (iterUserServiceDev.next().getValue().getDmsdpDevice().getDeviceId().equals(device.getDeviceId())) {
                iterUserServiceDev.remove();
            }
        }
        Iterator<Map.Entry<String, DMSDPDeviceService>> iterUserServics = this.usedServices.entrySet().iterator();
        while (iterUserServics.hasNext()) {
            if (iterUserServics.next().getKey().contains(device.getDeviceId())) {
                iterUserServics.remove();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDmsdpConnectFailed(DMSDPDevice tmpDmsdpDevice) {
        HwLog.e(TAG, "EVENT_DEVICE_CONNECT_FALIED");
        this.deviceState.remove(tmpDmsdpDevice.getDeviceId());
        Iterator<Map.Entry<String, Integer>> iterator = this.deviceServiceState.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> next = iterator.next();
            if (next.getKey().contains(tmpDmsdpDevice.getDeviceId())) {
                String[] string = next.getKey().split(":");
                this.mHandler.sendMessage(this.mHandler.obtainMessage(2, Integer.parseInt(string[string.length - 1]), -6, this.allDevices.get(tmpDmsdpDevice.getDeviceId())));
                iterator.remove();
            }
        }
        Iterator<Map.Entry<Integer, VideoCallDevice>> iterUserServiceDev = this.usedServiceDevices.entrySet().iterator();
        while (iterUserServiceDev.hasNext()) {
            if (iterUserServiceDev.next().getValue().getDmsdpDevice().getDeviceId().equals(tmpDmsdpDevice.getDeviceId())) {
                iterUserServiceDev.remove();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDmsdpPinEvent(DMSDPDevice tmpDmsdpDevice, int event) {
        HwLog.i(TAG, "EVENT_DEVICE_SHOW_PIN_INPUT");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(3, tmpDmsdpDevice.getDeviceType(), event, this.allDevices.get(tmpDmsdpDevice.getDeviceId())));
    }

    public static void createInstance(Context context, VideoCallAdapterCallback callback) {
        synchronized (VIDEO_CALL_LOCK) {
            HwLog.i(TAG, "VideoCallAdapter createInstance");
            if (callback != null) {
                videoCallAdapterCallback = callback;
                if (videoCallAdapter != null) {
                    HwLog.d(TAG, "createInstance callback has been exist");
                    callback.onAdapterGet(videoCallAdapter);
                    return;
                }
                DMSDPAdapter.createInstance(context, new DMSDPAdapterCallback() {
                    /* class com.huawei.dmsdpsdk2.videocall.VideoCallAdapter.AnonymousClass4 */

                    @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
                    public void onAdapterGet(DMSDPAdapter adapter) {
                        synchronized (VideoCallAdapter.VIDEO_CALL_LOCK) {
                            String str = VideoCallAdapter.TAG;
                            HwLog.w(str, "VideoCallAdapter onAdapterGet " + adapter);
                            if (adapter == null) {
                                VideoCallAdapter.videoCallAdapterCallback.onBinderDied();
                                DMSDPAdapter.releaseInstance();
                                return;
                            }
                            VideoCallAdapter unused = VideoCallAdapter.videoCallAdapter = new VideoCallAdapter(adapter);
                            VideoCallAdapter.videoCallAdapterCallback.onAdapterGet(VideoCallAdapter.videoCallAdapter);
                        }
                    }

                    @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
                    public void onBinderDied() {
                    }
                });
                return;
            }
            HwLog.e(TAG, "createInstance callback null");
            throw new IllegalArgumentException("createInstance callback null");
        }
    }

    private void clear() {
        this.allDevices.clear();
        this.usedServiceDevices.clear();
        this.usedServices.clear();
        this.deviceState.clear();
        this.deviceServiceState.clear();
    }

    public void releaseInstance() {
        synchronized (VIDEO_CALL_LOCK) {
            HwLog.w(TAG, "VideoCallAdapter releaseInstance");
            unRegisterVideoCallListener(this.videoCallListener);
            DMSDPAdapter.releaseInstance();
            this.mDMSDPAdapter = null;
            clearVideoCallAdapter();
            clear();
        }
    }

    private static void clearVideoCallAdapter() {
        videoCallAdapter = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableDeviceService(DMSDPDeviceService dmsdpDeviceService) {
        HwLog.i(TAG, "enableDeviceService");
        ConcurrentHashMap<String, DMSDPDeviceService> concurrentHashMap = this.usedServices;
        concurrentHashMap.put(dmsdpDeviceService.getDeviceId() + ":" + dmsdpDeviceService.getServiceType(), dmsdpDeviceService);
        if (dmsdpDeviceService.getServiceType() != 8) {
            startDeviceService(dmsdpDeviceService);
        }
    }

    public int registerDMSDPListener(DMSDPListener listener) {
        synchronized (VIDEO_CALL_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.registerDMSDPListener(4, listener);
        }
    }

    public int unRegisterDMSDPListener(DMSDPListener listener) {
        synchronized (VIDEO_CALL_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.unRegisterDMSDPListener(4, listener);
        }
    }

    public int registerVideoCallListener(VideoCallListener listener) {
        HwLog.i(TAG, "registerVideoCallListener");
        synchronized (VIDEO_CALL_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            this.videoCallListener = listener;
            registerDMSDPListener(this.dmsdpListener);
            return 0;
        }
    }

    public int unRegisterVideoCallListener(VideoCallListener listener) {
        synchronized (VIDEO_CALL_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            this.videoCallListener = null;
            unRegisterDMSDPListener(this.dmsdpListener);
            return 0;
        }
    }

    public int startDiscover(int protocol, int deviceFilter, int serviceFilter) {
        synchronized (VIDEO_CALL_LOCK) {
            if (this.mDMSDPAdapter == null) {
                return -2;
            }
            if (this.discoverListener == null) {
                return -2;
            }
            return this.mDMSDPAdapter.startDiscover(4, protocol, deviceFilter, serviceFilter, this.discoverListener);
        }
    }

    public int startDiscover(int protocol, int deviceFilter, int serviceFilter, DiscoverListener listener) {
        synchronized (VIDEO_CALL_LOCK) {
            if (this.mDMSDPAdapter == null) {
                return -2;
            }
            return this.mDMSDPAdapter.startDiscover(4, protocol, deviceFilter, serviceFilter, listener);
        }
    }

    public int stopDiscover(int protocol) {
        synchronized (VIDEO_CALL_LOCK) {
            if (this.mDMSDPAdapter == null) {
                return -2;
            }
            if (this.discoverListener == null) {
                return -2;
            }
            return this.mDMSDPAdapter.stopDiscover(4, protocol, this.discoverListener);
        }
    }

    public int stopDiscover(int protocol, DiscoverListener listener) {
        synchronized (VIDEO_CALL_LOCK) {
            if (this.mDMSDPAdapter == null) {
                return -2;
            }
            return this.mDMSDPAdapter.stopDiscover(4, protocol, listener);
        }
    }

    public int connectDevice(int channelType, DMSDPDevice device) {
        synchronized (VIDEO_CALL_LOCK) {
            HwLog.d(TAG, "connectDevice");
            if (this.mDMSDPAdapter == null) {
                return -2;
            }
            return this.mDMSDPAdapter.connectDevice(4, channelType, device, null);
        }
    }

    public int getDevicesState(String deviceId) {
        HwLog.d(TAG, "getDevicesState");
        if (this.deviceState.containsKey(deviceId)) {
            return this.deviceState.get(deviceId).intValue();
        }
        HwLog.d(TAG, "DISCONNECT");
        return -1;
    }

    public int getDevicesServiceState(String deviceId, int serviceType) {
        HwLog.d(TAG, "getDevicesServiceState");
        ConcurrentHashMap<String, Integer> concurrentHashMap = this.deviceServiceState;
        if (!concurrentHashMap.containsKey(deviceId + ":" + serviceType)) {
            return -1;
        }
        ConcurrentHashMap<String, Integer> concurrentHashMap2 = this.deviceServiceState;
        return concurrentHashMap2.get(deviceId + ":" + serviceType).intValue();
    }

    public int connectDeviceService(VideoCallDevice device, List<Integer> deviceServiceType, int property) {
        HwLog.d(TAG, "connectDevice deviceServiceType");
        synchronized (VIDEO_CALL_LOCK) {
            if (device == null) {
                return -2;
            }
            VideoCallDevice tmpDevice = this.allDevices.get(device.getDmsdpDevice().getDeviceId());
            if (tmpDevice == null) {
                HwLog.e(TAG, "get device is null");
                return -2;
            } else if (getDevicesState(tmpDevice.getDmsdpDevice().getDeviceId()) == 0) {
                HwLog.e(TAG, "ERROR_CODE_ALREADY_CONNECTING");
                return -1;
            } else {
                for (Integer num : deviceServiceType) {
                    int serviceType = num.intValue();
                    if (checkService(tmpDevice, serviceType)) {
                        if (this.usedServiceDevices.putIfAbsent(Integer.valueOf(serviceType), tmpDevice) != null) {
                            String str = TAG;
                            HwLog.e(str, "onServiceFailed ERROR_CODE_ALREADY_ENABLE: " + serviceType);
                            this.mHandler.sendMessage(this.mHandler.obtainMessage(2, serviceType, -4, tmpDevice));
                        } else {
                            if (getDevicesState(tmpDevice.getDmsdpDevice().getDeviceId()) == 1) {
                                requestDeviceService(tmpDevice.getDmsdpDevice(), serviceType);
                            }
                            ConcurrentHashMap<String, Integer> concurrentHashMap = this.deviceServiceState;
                            concurrentHashMap.put(tmpDevice.getDmsdpDevice().getDeviceId() + ":" + serviceType, 0);
                        }
                    }
                }
                if (getDevicesState(tmpDevice.getDmsdpDevice().getDeviceId()) == -1) {
                    int ret = connectDevice(getDeviceChannelType(tmpDevice.getDmsdpDevice()), tmpDevice.getDmsdpDevice());
                    handleConnectRet(tmpDevice, ret);
                    if (ret != 0) {
                        return -1;
                    }
                    this.deviceState.put(tmpDevice.getDmsdpDevice().getDeviceId(), 0);
                }
                return 0;
            }
        }
    }

    private boolean checkService(VideoCallDevice device, int service) {
        HwLog.d(TAG, "checkService");
        ArrayList<Integer> supportServices = device.getSupportServices();
        int camera = service & 1;
        if (camera == 1 && supportServices.contains(Integer.valueOf(camera))) {
            return true;
        }
        int mic = service & 2;
        if (mic == 2 && supportServices.contains(Integer.valueOf(mic))) {
            return true;
        }
        int speaker = service & 4;
        if (speaker == 4 && supportServices.contains(Integer.valueOf(speaker))) {
            return true;
        }
        int display = service & 8;
        if (display == 8 && supportServices.contains(Integer.valueOf(display))) {
            return true;
        }
        int hfp = service & 64;
        if (hfp == 64 && supportServices.contains(Integer.valueOf(hfp))) {
            return true;
        }
        int a2dp = service & 128;
        if (a2dp != 128 || !supportServices.contains(Integer.valueOf(a2dp))) {
            return false;
        }
        return true;
    }

    private int getDeviceChannelType(DMSDPDevice device) {
        HwLog.d(TAG, "getDeviceChannelType");
        int deviceType = device.getDeviceType();
        if (deviceType == 3) {
            return 7;
        }
        if (deviceType == 5) {
            return 8;
        }
        if (deviceType != 6) {
            return 0;
        }
        return 6;
    }

    private void handleConnectRet(VideoCallDevice device, int ret) {
        if (ret != 0) {
            handleConnectError(device, ret);
        }
    }

    private void handleConnectError(VideoCallDevice device, int ret) {
        HwLog.d(TAG, String.format("connectDevice device %s failed", transformSensitiveInfo(device.getDmsdpDevice().getDeviceId())));
        Iterator<Map.Entry<Integer, VideoCallDevice>> iterator = this.usedServiceDevices.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().getDmsdpDevice().getDeviceId().equals(device.getDmsdpDevice().getDeviceId())) {
                iterator.remove();
            }
        }
        Iterator<Map.Entry<String, Integer>> iteratorDeviceServiceState = this.deviceServiceState.entrySet().iterator();
        while (iteratorDeviceServiceState.hasNext()) {
            if (iteratorDeviceServiceState.next().getKey().equals(device.getDmsdpDevice().getDeviceId())) {
                iteratorDeviceServiceState.remove();
            }
        }
        Iterator<Map.Entry<String, Integer>> iteratorDeviceState = this.deviceState.entrySet().iterator();
        while (iteratorDeviceState.hasNext()) {
            if (iteratorDeviceState.next().getKey().equals(device.getDmsdpDevice().getDeviceId())) {
                iteratorDeviceState.remove();
            }
        }
    }

    public int disconnectDevice(int channelType, DMSDPDevice device) {
        synchronized (VIDEO_CALL_LOCK) {
            if (this.mDMSDPAdapter == null) {
                return -2;
            }
            return this.mDMSDPAdapter.disconnectDevice(4, channelType, device);
        }
    }

    public int disconnectDeviceService(VideoCallDevice device, List<Integer> deviceServiceType) {
        synchronized (VIDEO_CALL_LOCK) {
            HwLog.d(TAG, "disconnectDeviceService");
            VideoCallDevice tmpDevice = this.allDevices.get(device.getDmsdpDevice().getDeviceId());
            if (tmpDevice == null) {
                HwLog.e(TAG, "get device is null");
                return -2;
            } else if (getDevicesState(tmpDevice.getDmsdpDevice().getDeviceId()) == 0) {
                HwLog.e(TAG, "ERROR_CODE_ALREADY_CONNECTING");
                return -1;
            } else if (getDevicesState(tmpDevice.getDmsdpDevice().getDeviceId()) == -2) {
                HwLog.e(TAG, "ERROR_CODE_ALREADY_DISCONNECTING");
                return -3;
            } else if (getDevicesState(tmpDevice.getDmsdpDevice().getDeviceId()) == -1) {
                HwLog.e(TAG, "ERROR_CODE_ALREADY_DISCONNECT");
                return -2;
            } else {
                for (Integer num : deviceServiceType) {
                    int serviceType = num.intValue();
                    ConcurrentHashMap<String, DMSDPDeviceService> concurrentHashMap = this.usedServices;
                    DMSDPDeviceService dmsdpDeviceService = concurrentHashMap.get(tmpDevice.getDmsdpDevice().getDeviceId() + ":" + serviceType);
                    if (dmsdpDeviceService == null) {
                        String str = TAG;
                        HwLog.e(str, "onServiceFailed ERROR_CODE_SERVICE_NOT_ENABLE: " + serviceType);
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(2, serviceType, -5, tmpDevice));
                    } else {
                        int ret = stopDeviceService(dmsdpDeviceService);
                        if (dmsdpDeviceService.getServiceType() == 8) {
                            HwLog.e(TAG, "disconnect DEVICE_SERVICE_TYPE_DISPLAY");
                            if (this.mDMSDPAdapter != null) {
                                this.mDMSDPAdapter.disconnectDevice(4, getDeviceChannelType(tmpDevice.getDmsdpDevice()), tmpDevice.getDmsdpDevice());
                                this.deviceState.put(tmpDevice.getDmsdpDevice().getDeviceId(), -2);
                            }
                        }
                        String str2 = TAG;
                        HwLog.e(str2, "dmsdpDeviceService result " + ret);
                    }
                }
                return 0;
            }
        }
    }

    public int requestDeviceService(DMSDPDevice device, int type) {
        synchronized (VIDEO_CALL_LOCK) {
            HwLog.i(TAG, "requestDeviceService start");
            if (this.mDMSDPAdapter == null) {
                return -2;
            }
            return this.mDMSDPAdapter.requestDeviceService(4, device, type);
        }
    }

    public int startDeviceService(DMSDPDeviceService service) {
        synchronized (VIDEO_CALL_LOCK) {
            HwLog.i(TAG, "startDeviceService start");
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            } else if (service.getServiceType() == 1) {
                return this.mDMSDPAdapter.startDeviceService(4, service, 1, null);
            } else {
                return this.mDMSDPAdapter.startDeviceService(4, service, 0, null);
            }
        }
    }

    public int stopDeviceService(DMSDPDeviceService service) {
        synchronized (VIDEO_CALL_LOCK) {
            HwLog.i(TAG, "stopDeviceService start");
            if (this.mDMSDPAdapter == null) {
                return -2;
            }
            if (service.getServiceType() == 1) {
                return this.mDMSDPAdapter.stopDeviceService(4, service, 1);
            }
            return this.mDMSDPAdapter.stopDeviceService(4, service, 0);
        }
    }

    public int updateDeviceService(DMSDPDeviceService service, int action) {
        synchronized (VIDEO_CALL_LOCK) {
            if (this.mDMSDPAdapter == null) {
                return -2;
            }
            return this.mDMSDPAdapter.updateDeviceService(4, service, action, null);
        }
    }

    public int updateDeviceService(DMSDPDeviceService service, int action, Map<String, Object> params) {
        synchronized (VIDEO_CALL_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.updateDeviceService(4, service, action, params);
        }
    }

    public int queryAuthDevice(int businessId, List<DMSDPDevice> deviceList) {
        synchronized (VIDEO_CALL_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            } else if (deviceList == null) {
                HwLog.e(TAG, "deviceList is null");
                return -2;
            } else {
                return this.mDMSDPAdapter.queryAuthDevice(businessId, deviceList);
            }
        }
    }

    public int delAuthDevice(int businessId) {
        synchronized (VIDEO_CALL_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.delAuthDevice(businessId);
        }
    }

    /* access modifiers changed from: private */
    public static String transformSensitiveInfo(String info) {
        return transformSensitiveInfo(info, 2, 2, true);
    }

    private static String transformSensitiveInfo(String info, int left, int right, boolean basedOnLeft) {
        if (info == null || info.isEmpty()) {
            return BuildConfig.FLAVOR;
        }
        StringBuilder sbText = new StringBuilder(1);
        int length = info.length();
        if ((length - left) - right > 0) {
            String prefix = info.substring(0, left);
            String suffix = info.substring(length - right);
            sbText.append(prefix);
            sbText.append("****");
            sbText.append(suffix);
        } else if (basedOnLeft) {
            if (length <= left || left <= 0) {
                sbText.append(info.substring(0, 1));
                sbText.append("****");
            } else {
                sbText.append(info.substring(0, left));
                sbText.append("****");
            }
        } else if (length <= right || right <= 0) {
            sbText.append("****");
            sbText.append(info.substring(info.length() - 1));
        } else {
            sbText.append("****");
            sbText.append(info.substring(length - right));
        }
        return sbText.toString();
    }

    public int getVirtualCameraList(int businessId, List<String> cameraIdList) {
        synchronized (VIDEO_CALL_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.getVirtualCameraList(businessId, cameraIdList);
        }
    }

    public int setVirtualDevicePolicy(int module, int policy) {
        synchronized (VIDEO_CALL_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.setVirtualDevicePolicy(4, module, policy);
        }
    }

    /* access modifiers changed from: private */
    public class DeviceServiceHandler extends Handler {
        private DeviceServiceHandler() {
        }

        public void handleMessage(Message msg) {
            if (VideoCallAdapter.this.videoCallListener == null) {
                HwLog.e(VideoCallAdapter.TAG, "videoCallListener is null");
            } else if (msg.obj instanceof VideoCallDevice) {
                VideoCallDevice videoCallDevice = (VideoCallDevice) msg.obj;
                int i = msg.what;
                if (i == 1) {
                    VideoCallAdapter.this.videoCallListener.onServiceSuccess(videoCallDevice, msg.arg1, msg.arg2);
                } else if (i == 2) {
                    VideoCallAdapter.this.videoCallListener.onServiceFailed(videoCallDevice, msg.arg1, msg.arg2);
                } else if (i == 3) {
                    VideoCallAdapter.this.videoCallListener.onPinEvent(videoCallDevice, msg.arg2);
                }
            } else {
                HwLog.e(VideoCallAdapter.TAG, "not VideoCallDevice instance");
            }
        }
    }
}
