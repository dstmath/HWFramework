package com.huawei.dmsdpsdk2.devicevirtualization;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;
import com.huawei.android.hwpartdevicevirtualization.BuildConfig;
import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.DMSDPAdapterAgent;
import com.huawei.dmsdpsdk2.DMSDPAdapterCallback;
import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.DMSDPListener;
import com.huawei.dmsdpsdk2.DeviceInfo;
import com.huawei.dmsdpsdk2.DeviceParameterConst;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.ISecureFileListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class VirtualizationAdapter {
    private static final int DEFAULT_CAPACITY = 10;
    private static final int DEFAULT_DISPLAY_VIDEO_HEIGHT_INT = 1080;
    private static final int DEFAULT_DISPLAY_VIDEO_WIDTH_INT = 1920;
    private static final String DMSDP_PACKAGE_NAME = "com.huawei.dmsdpdevice";
    private static final String PIN_MAP_KEY = "pin_code";
    private static final String TAG = "VirtualizationAdapter";
    private static IInitCallback sInitCallback;
    private static AtomicBoolean sInitSuccess = new AtomicBoolean(false);
    private static volatile VirtualizationAdapter sTvAdapter;
    private volatile DMSDPDevice mConnectedDevice;
    private DMSDPAdapter mDMSDPAdapter;
    private DMSDPListener mDMSDPListener;
    private DeviceInfo mDeviceInfo;
    private volatile DMSDPDeviceService mDisplayService;
    private String mRemoteDeviceName;
    private IVirtualizationCallback mVirtualizationCallback;

    private VirtualizationAdapter(DMSDPAdapter adapter) {
        this.mDMSDPAdapter = adapter;
        registerDiedListener(adapter);
    }

    public static int initInstance(Context context, DeviceInfo deviceInfo, IInitCallback callback) {
        HwLog.i(TAG, "initInstance");
        if (context == null || callback == null || isInvalidDeviceInfo(deviceInfo)) {
            HwLog.e(TAG, "initInstance invalid argument");
            sInitSuccess.set(false);
            DMSDPAdapterAgent.releaseInstance();
            return -2;
        } else if (sInitSuccess.getAndSet(true)) {
            HwLog.i(TAG, "initInstance already init success");
            return 0;
        } else {
            sInitCallback = callback;
            Context applicationContext = context.getApplicationContext();
            if (!isServiceExist(applicationContext)) {
                HwLog.e(TAG, "initInstance service is not found");
                sInitCallback.onInitFail(-9);
                sInitSuccess.set(false);
                DMSDPAdapterAgent.releaseInstance();
                return 0;
            } else if (sTvAdapter != null) {
                HwLog.i(TAG, "initInstance already created");
                return 0;
            } else {
                createAdapterInstance(applicationContext, deviceInfo);
                return 0;
            }
        }
    }

    private static boolean isInvalidDeviceInfo(DeviceInfo deviceInfo) {
        return deviceInfo == null || deviceInfo.getDeviceName() == null;
    }

    private static boolean isServiceExist(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(DMSDP_PACKAGE_NAME, 0).packageName.equals(DMSDP_PACKAGE_NAME);
        } catch (PackageManager.NameNotFoundException e) {
            HwLog.e(TAG, "com.huawei.dmsdpdevice not found");
            return false;
        }
    }

    private static void createAdapterInstance(final Context context, final DeviceInfo deviceInfo) {
        DMSDPAdapterAgent.createInstance(context, new DMSDPAdapterCallback() {
            /* class com.huawei.dmsdpsdk2.devicevirtualization.VirtualizationAdapter.AnonymousClass1 */

            @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
            public void onAdapterGet(DMSDPAdapter adapter) {
                if (adapter == null || !(adapter instanceof DMSDPAdapterAgent)) {
                    HwLog.e(VirtualizationAdapter.TAG, "createInstance adapter is null");
                    VirtualizationAdapter.sInitCallback.onInitFail(-10);
                    VirtualizationAdapter.sInitSuccess.set(false);
                    DMSDPAdapterAgent.releaseInstance();
                    return;
                }
                VirtualizationAdapter unused = VirtualizationAdapter.sTvAdapter = new VirtualizationAdapter(adapter);
                VirtualizationAdapter.sTvAdapter.registerServiceListener();
                VirtualizationAdapter.sTvAdapter.setSecureFileListener();
                HwLog.i(VirtualizationAdapter.TAG, "createInstance initDevice");
                if (VirtualizationAdapter.sTvAdapter.initDevice(deviceInfo, context) == 0) {
                    HwLog.d(VirtualizationAdapter.TAG, "createInstance onInitSuccess");
                    VirtualizationAdapter.sInitCallback.onInitSuccess(VirtualizationAdapter.sTvAdapter);
                    return;
                }
                HwLog.e(VirtualizationAdapter.TAG, "createInstance onInitFail initDevice");
                VirtualizationAdapter.sInitCallback.onInitFail(-15);
                VirtualizationAdapter.releaseInstance();
            }

            @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
            public void onBinderDied() {
                HwLog.w(VirtualizationAdapter.TAG, "DMSDP service onBinderDied");
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSecureFileListener() {
        setSecureFileListener(new SecureFileListener());
    }

    private void setSecureFileListener(ISecureFileListener listener) {
        if (!isAdapterNull()) {
            this.mDMSDPAdapter.setSecureFileListener(4, listener);
        }
    }

    public static void releaseInstance() {
        HwLog.i(TAG, "releaseInstance");
        sInitSuccess.set(false);
        resetReceiver();
        DMSDPAdapterAgent.releaseInstance();
    }

    private static void resetReceiver() {
        if (sTvAdapter != null) {
            int unRegisterServiceResult = sTvAdapter.unRegisterServiceListener(sTvAdapter.mDMSDPListener);
            HwLog.d(TAG, "resetReceiver unRegisterServiceResult:" + unRegisterServiceResult);
            sTvAdapter.setSecureFileListener(null);
            sTvAdapter.mVirtualizationCallback = null;
            sTvAdapter = null;
        }
        sInitCallback = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerServiceListener() {
        this.mDMSDPListener = new DMSDPListener() {
            /* class com.huawei.dmsdpsdk2.devicevirtualization.VirtualizationAdapter.AnonymousClass2 */

            @Override // com.huawei.dmsdpsdk2.DMSDPListener
            public void onDeviceChange(DMSDPDevice dmsdpDevice, int deviceState, Map<String, Object> info) {
                if (dmsdpDevice == null) {
                    HwLog.e(VirtualizationAdapter.TAG, "onDeviceChange dmsdpDevice is null");
                } else if (VirtualizationAdapter.this.mVirtualizationCallback == null) {
                    HwLog.e(VirtualizationAdapter.TAG, "onDeviceChange mVirtualizationCallback is null");
                } else {
                    HwLog.i(VirtualizationAdapter.TAG, "onDeviceChange state:" + deviceState);
                    if (!VirtualizationAdapter.this.authConnection(dmsdpDevice, deviceState, info)) {
                        int deviceType = dmsdpDevice.getDeviceType();
                        HwLog.i(VirtualizationAdapter.TAG, "onDeviceChange deviceType:" + deviceType);
                        VirtualizationAdapter.this.updateDeviceService(dmsdpDevice, deviceState);
                        String deviceName = dmsdpDevice.getDeviceName();
                        VirtualizationAdapter.this.mRemoteDeviceName = deviceName;
                        VirtualizationAdapter.this.mVirtualizationCallback.onDeviceChange(deviceName, deviceState);
                    }
                }
            }

            @Override // com.huawei.dmsdpsdk2.DMSDPListener
            public void onDeviceServiceChange(DMSDPDeviceService dmsdpDeviceService, int state, Map<String, Object> map) {
                if (dmsdpDeviceService == null) {
                    HwLog.e(VirtualizationAdapter.TAG, "onDeviceServiceChange device service is null");
                    return;
                }
                HwLog.i(VirtualizationAdapter.TAG, "onDeviceServiceChange serviceId:" + dmsdpDeviceService.getServiceId() + ",state: " + state);
                if (VirtualizationAdapter.this.mConnectedDevice == null) {
                    HwLog.i(VirtualizationAdapter.TAG, "onDeviceServiceChange no device is connected");
                } else if (dmsdpDeviceService.getServiceType() == 8) {
                    VirtualizationAdapter.this.onDeviceChange(dmsdpDeviceService, state);
                }
            }
        };
        int registerResult = registerServiceListener(this.mDMSDPListener);
        if (registerResult != 0) {
            HwLog.e(TAG, "registerServiceListener failed,registerResult:" + registerResult);
        }
    }

    private int registerServiceListener(DMSDPListener listener) {
        if (isAdapterNull()) {
            return -10;
        }
        return this.mDMSDPAdapter.registerDMSDPListener(4, listener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean authConnection(DMSDPDevice dmsdpDevice, int deviceState, Map<String, Object> info) {
        if (deviceState == 113 && info != null) {
            HwLog.d(TAG, "onDeviceChange authConnection show pin code");
            if (info.get(PIN_MAP_KEY) instanceof String) {
                this.mVirtualizationCallback.onPinCode(dmsdpDevice.getDeviceName(), (String) info.get(PIN_MAP_KEY));
                return true;
            }
        }
        if (deviceState != 117) {
            return false;
        }
        HwLog.d(TAG, "onDeviceChange authConnection device request connection");
        this.mVirtualizationCallback.onDeviceChange(dmsdpDevice.getDeviceName(), 112);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDeviceChange(DMSDPDeviceService dmsdpDeviceService, int state) {
        if (state == 204) {
            HwLog.i(TAG, "onDeviceChange projection device connected");
            this.mDisplayService = dmsdpDeviceService;
            IVirtualizationCallback iVirtualizationCallback = this.mVirtualizationCallback;
            if (iVirtualizationCallback == null) {
                HwLog.e(TAG, "onDeviceChange callback is null");
            } else {
                iVirtualizationCallback.onDeviceChange(this.mRemoteDeviceName, state);
            }
        } else if (state == 205) {
            HwLog.i(TAG, "onDeviceChange projection device disConnected");
            if (this.mDisplayService != null) {
                updateDeviceService(dmsdpDeviceService, 205);
                IVirtualizationCallback iVirtualizationCallback2 = this.mVirtualizationCallback;
                if (iVirtualizationCallback2 == null) {
                    HwLog.e(TAG, "onDeviceChange stop callback is null");
                    return;
                }
                iVirtualizationCallback2.onDeviceChange(this.mRemoteDeviceName, state);
                this.mDisplayService = null;
            }
        } else {
            HwLog.i(TAG, "onDeviceChange state:" + state);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDeviceService(DMSDPDevice dmsdpDevice, int deviceState) {
        if (deviceState == 101) {
            this.mConnectedDevice = dmsdpDevice;
        }
        if (deviceState == 102) {
            this.mConnectedDevice = null;
            if (this.mDisplayService != null) {
                sTvAdapter.updateDeviceService(this.mDisplayService, 205);
                this.mDisplayService = null;
            }
        }
    }

    private int updateDeviceService(DMSDPDeviceService service, int action) {
        if (isAdapterNull()) {
            return -10;
        }
        return this.mDMSDPAdapter.updateDeviceService(4, service, action, null);
    }

    public int updateDeviceName(String deviceName) {
        if (TextUtils.isEmpty(deviceName)) {
            HwLog.i(TAG, "updateDeviceName deviceName is empty");
            return -2;
        }
        DeviceInfo deviceInfo = new DeviceInfo(BuildConfig.FLAVOR);
        deviceInfo.setDeviceName(deviceName);
        int setResult = setDeviceInfo(deviceInfo);
        if (setResult == 0) {
            return 0;
        }
        HwLog.i(TAG, "updateDeviceName set device info failed, setResult:" + setResult);
        return setResult;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int initDevice(DeviceInfo info, Context context) {
        this.mDeviceInfo = info;
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        int screenWidth = DEFAULT_DISPLAY_VIDEO_WIDTH_INT;
        int screenHeight = DEFAULT_DISPLAY_VIDEO_HEIGHT_INT;
        if (windowManager != null) {
            HwLog.d(TAG, "windowManager is not null");
            DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
            screenWidth = displayMetrics.widthPixels;
            screenHeight = displayMetrics.heightPixels;
        }
        HwLog.d(TAG, "screenWidth:" + screenWidth + ",screenHeight:" + screenHeight);
        DeviceInfo deviceInfo = new DeviceInfo(BuildConfig.FLAVOR);
        deviceInfo.setDeviceName(info.getDeviceName());
        deviceInfo.setDeviceType(info.getDeviceType());
        deviceInfo.addProperties(DeviceParameterConst.DISPLAY_VIDEO_WIDTH_INT, Integer.valueOf(screenWidth));
        deviceInfo.addProperties(DeviceParameterConst.DISPLAY_VIDEO_HEIGHT_INT, Integer.valueOf(screenHeight));
        if (info.isIgnoreAndroidCamera()) {
            HwLog.i(TAG, "ignore android camera");
            deviceInfo.addProperties(DeviceParameterConst.IGNORE_ANDROID_CAMERA, true);
        }
        AudioCapabilities micCapabilities = info.getMicCapabilities();
        if (micCapabilities != null) {
            deviceInfo.addProperties(DeviceParameterConst.AUDIO_MIC_CAP_STRING, micCapabilities.toString());
        }
        AudioCapabilities speakerCapabilities = info.getSpeakerCapabilities();
        if (speakerCapabilities != null) {
            deviceInfo.addProperties(DeviceParameterConst.AUDIO_SPEAKER_CAP_STRING, speakerCapabilities.toString());
        }
        return setDeviceInfo(deviceInfo);
    }

    private int setDeviceInfo(DeviceInfo deviceInfo) {
        if (isAdapterNull()) {
            return -10;
        }
        if (deviceInfo != null) {
            return this.mDMSDPAdapter.setDeviceInfo(4, deviceInfo);
        }
        HwLog.e(TAG, "device info is null");
        return -2;
    }

    public int setVirtualizationCallback(IVirtualizationCallback callback) {
        if (callback == null) {
            HwLog.i(TAG, "setVirtualizationCallback callback is null");
            return -2;
        }
        this.mVirtualizationCallback = callback;
        return 0;
    }

    public int startAdv() {
        HwLog.d(TAG, "startAdv start");
        if (isAdapterNull()) {
            return -10;
        }
        return this.mDMSDPAdapter.startScan(4, 64);
    }

    public int stopAdv() {
        HwLog.d(TAG, "stopAdv start");
        if (isAdapterNull()) {
            return -10;
        }
        return this.mDMSDPAdapter.stopScan(4, 64);
    }

    public int startProjection(Surface surface, int width, int height) {
        if (this.mConnectedDevice != null) {
            HwLog.i(TAG, "start projection");
            if (surface == null || !surface.isValid()) {
                HwLog.e(TAG, "surface is not valid");
                return -2;
            }
            HwLog.i(TAG, "set surface");
            DeviceInfo info = new DeviceInfo(BuildConfig.FLAVOR);
            info.setSurface(surface);
            info.addProperties(DeviceParameterConst.DISPLAY_VITUALWIDTH_INT, Integer.valueOf(width));
            info.addProperties(DeviceParameterConst.DISPLAY_VITUALHEIGHT_INT, Integer.valueOf(height));
            int result = setDeviceInfo(info);
            if (result != 0) {
                HwLog.e(TAG, "startProjection set device info failed,result:" + result);
                return -11;
            } else if (updateDeviceService(this.mDisplayService, 204) == 0) {
                return 0;
            } else {
                HwLog.e(TAG, "startProjection failed");
                return -12;
            }
        } else {
            HwLog.i(TAG, "device is not connected");
            return -14;
        }
    }

    public int stopProjection() {
        if (this.mConnectedDevice == null || this.mDisplayService == null) {
            HwLog.i(TAG, "projection is not start");
            return 0;
        }
        HwLog.i(TAG, "stop projection");
        int updateResult = updateDeviceService(this.mDisplayService, 205);
        if (updateResult == 0) {
            return 0;
        }
        HwLog.e(TAG, "stop projection failed, updateResult:" + updateResult);
        return -13;
    }

    public int disconnectDevice() {
        if (isAdapterNull()) {
            return -10;
        }
        if (this.mDeviceInfo == null) {
            HwLog.i(TAG, "disconnectDevice device init failed");
            return -15;
        } else if (this.mConnectedDevice == null) {
            HwLog.i(TAG, "disconnectDevice device is not connected");
            return -14;
        } else {
            return this.mDMSDPAdapter.disconnectDevice(4, 10, new DMSDPDevice(BuildConfig.FLAVOR, this.mDeviceInfo.getDeviceType()));
        }
    }

    private int unRegisterServiceListener(DMSDPListener listener) {
        if (isAdapterNull()) {
            return -10;
        }
        return this.mDMSDPAdapter.unRegisterDMSDPListener(4, listener);
    }

    private boolean isAdapterNull() {
        if (this.mDMSDPAdapter != null) {
            return false;
        }
        HwLog.e(TAG, "mDMSDPAdapter is null");
        return true;
    }

    public int getTrustDeviceList(List<RemoteDevice> devices) {
        HwLog.d(TAG, "getTrustDeviceList start");
        if (isAdapterNull()) {
            return -10;
        }
        if (devices == null) {
            HwLog.i(TAG, "getTrustDeviceList devices is null");
            return -2;
        }
        List<DMSDPDevice> deviceList = new ArrayList<>(10);
        int errorCode = this.mDMSDPAdapter.getTrustDeviceList(4, deviceList);
        if (errorCode == 0) {
            devices.clear();
            for (DMSDPDevice dmsdpDevice : deviceList) {
                RemoteDevice remoteDevice = new RemoteDevice(dmsdpDevice.getDeviceId());
                remoteDevice.setDeviceName(dmsdpDevice.getDeviceName());
                devices.add(remoteDevice);
            }
        }
        return errorCode;
    }

    public int deleteTrustDevice(String deviceId) {
        if (isAdapterNull()) {
            return -10;
        }
        return this.mDMSDPAdapter.deleteTrustDevice(4, deviceId);
    }

    public int acceptConnection(boolean isPermanent) {
        if (isAdapterNull()) {
            return -10;
        }
        DMSDPDevice currentDevice = new DMSDPDevice(BuildConfig.FLAVOR, this.mDeviceInfo.getDeviceType());
        if (isPermanent) {
            currentDevice.addProperties(DeviceParameterConst.DEVICE_PERMISSION_TYPE, 1);
        } else {
            currentDevice.addProperties(DeviceParameterConst.DEVICE_PERMISSION_TYPE, 0);
        }
        return this.mDMSDPAdapter.connectDevice(4, 10, currentDevice, null);
    }

    public static String getVersion() {
        HwLog.d(TAG, "getVersion start");
        return BuildConfig.VERSION_NAME;
    }

    private void registerDiedListener(DMSDPAdapter adapter) {
        IInterface dmsdpService = adapter.getDMSDPService();
        if (dmsdpService != null) {
            try {
                dmsdpService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                    /* class com.huawei.dmsdpsdk2.devicevirtualization.VirtualizationAdapter.AnonymousClass3 */

                    @Override // android.os.IBinder.DeathRecipient
                    public void binderDied() {
                        HwLog.i(VirtualizationAdapter.TAG, "onBinderDied");
                        onBinderDied();
                    }

                    private void onBinderDied() {
                        VirtualizationAdapter.this.handleDiedMessage();
                    }
                }, 0);
            } catch (RemoteException e) {
                HwLog.e(TAG, "call service linkToDeath RemoteException");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDiedMessage() {
        IInitCallback iInitCallback = sInitCallback;
        if (iInitCallback != null) {
            iInitCallback.onBinderDied();
        }
    }

    /* access modifiers changed from: package-private */
    public class SecureFileListener extends ISecureFileListener.Stub {
        SecureFileListener() {
        }

        @Override // com.huawei.dmsdpsdk2.ISecureFileListener
        public long getSecureFileSize(String fileName) throws RemoteException {
            if (TextUtils.isEmpty(fileName)) {
                HwLog.i(VirtualizationAdapter.TAG, "getSecureFileSize fileName is empty");
                return 0;
            } else if (VirtualizationAdapter.this.mVirtualizationCallback == null) {
                HwLog.e(VirtualizationAdapter.TAG, "getSecureFileSize callback is null");
                return 0;
            } else {
                long secureFileSize = VirtualizationAdapter.this.mVirtualizationCallback.getSecureFileSize(fileName);
                HwLog.i(VirtualizationAdapter.TAG, "getSecureFileSize secureFileSize:" + secureFileSize);
                return secureFileSize;
            }
        }

        @Override // com.huawei.dmsdpsdk2.ISecureFileListener
        public byte[] readSecureFile(String fileName) throws RemoteException {
            if (TextUtils.isEmpty(fileName)) {
                HwLog.i(VirtualizationAdapter.TAG, "readSecureFile fileName is empty");
                return new byte[0];
            } else if (VirtualizationAdapter.this.mVirtualizationCallback != null) {
                return VirtualizationAdapter.this.mVirtualizationCallback.readSecureFile(fileName);
            } else {
                HwLog.e(VirtualizationAdapter.TAG, "readSecureFile callback is null");
                return new byte[0];
            }
        }

        @Override // com.huawei.dmsdpsdk2.ISecureFileListener
        public boolean writeSecureFile(String fileName, byte[] bytes) throws RemoteException {
            if (TextUtils.isEmpty(fileName)) {
                HwLog.i(VirtualizationAdapter.TAG, "writeSecureFile fileName is empty");
                return false;
            } else if (bytes == null || bytes.length == 0) {
                HwLog.i(VirtualizationAdapter.TAG, "writeSecureFile bytes is empty");
                return false;
            } else {
                HwLog.d(VirtualizationAdapter.TAG, "writeSecureFile fileName size:" + bytes.length);
                if (VirtualizationAdapter.this.mVirtualizationCallback != null) {
                    return VirtualizationAdapter.this.mVirtualizationCallback.writeSecureFile(fileName, bytes);
                }
                HwLog.e(VirtualizationAdapter.TAG, "writeSecureFile callback is null");
                return false;
            }
        }
    }
}
