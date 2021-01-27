package ohos.msdp.devicevirtualization;

import android.app.Application;
import android.content.Context;
import com.huawei.android.app.ActivityThreadEx;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ohos.dmsdp.sdk.HwLog;

public class VirtualDeviceCreator {
    private static final int API_LEVEL = 1;
    private static final int COLLECTION_SIZE = 8;
    private static final String TAG = "VirtualDeviceCreator";
    public static final String VIRTUAL_AGENT_DEVICE_CLASS = "VirtualAgentDeviceManager";
    public static final String VIRTUAL_DEVICE_CLASS = "VirtualDeviceManager";
    private static VirtualDeviceCreator sVirtualDeviceCreator;
    private IDmsdpServiceCallback mDmsdpServiceCallback;
    private IConnectCallback mIConnectCallback;
    private IConnectCallback mIDeviceConnectCallback;
    private Map<String, VirtualManager> mKitServices = new ConcurrentHashMap(8);
    private VirtualService mVirtualService;

    static int getAPILevel() {
        return 1;
    }

    private VirtualDeviceCreator() {
    }

    public static VirtualDeviceCreator getInstance() {
        VirtualDeviceCreator virtualDeviceCreator;
        synchronized (VirtualDeviceCreator.class) {
            if (sVirtualDeviceCreator == null) {
                sVirtualDeviceCreator = new VirtualDeviceCreator();
            }
            virtualDeviceCreator = sVirtualDeviceCreator;
        }
        return virtualDeviceCreator;
    }

    public int connect(IConnectCallback iConnectCallback) {
        HwLog.i(TAG, "start connect");
        if (iConnectCallback == null) {
            HwLog.e(TAG, "param is invalid");
            return -2;
        }
        Application currentApplication = ActivityThreadEx.currentApplication();
        if (currentApplication == null) {
            return -2;
        }
        Context applicationContext = currentApplication.getApplicationContext();
        HwLog.i(TAG, "start connect context: " + applicationContext);
        if (applicationContext != null) {
            HwLog.i(TAG, "start connect getPackageName: " + applicationContext.getPackageName());
            HwLog.i(TAG, "start connect getPackageResourcePath: " + applicationContext.getPackageResourcePath());
        }
        this.mIConnectCallback = iConnectCallback;
        createServiceCallback();
        VirtualService.createInstance(applicationContext, this.mDmsdpServiceCallback);
        return 0;
    }

    public int connectDevice(IConnectCallback iConnectCallback, boolean z) {
        HwLog.i(TAG, "start connect device");
        if (iConnectCallback == null) {
            HwLog.e(TAG, "param is invalid");
            return -2;
        }
        Application currentApplication = ActivityThreadEx.currentApplication();
        if (currentApplication == null) {
            return -2;
        }
        Context applicationContext = currentApplication.getApplicationContext();
        HwLog.i(TAG, "start connect device context: " + applicationContext);
        if (applicationContext != null) {
            HwLog.i(TAG, "start connect device getPackageName: " + applicationContext.getPackageName());
            HwLog.i(TAG, "start connect device getPackageResourcePath: " + applicationContext.getPackageResourcePath());
        }
        this.mIDeviceConnectCallback = iConnectCallback;
        createServiceCallback();
        VirtualService.createDeviceInstance(applicationContext, this.mDmsdpServiceCallback, z);
        return 0;
    }

    private void createServiceCallback() {
        this.mDmsdpServiceCallback = new IDmsdpServiceCallback() {
            /* class ohos.msdp.devicevirtualization.VirtualDeviceCreator.AnonymousClass1 */

            @Override // ohos.msdp.devicevirtualization.IDmsdpServiceCallback
            public void onAdapterGet(VirtualService virtualService) {
                HwLog.i(VirtualDeviceCreator.TAG, "onAdapterGet:" + virtualService);
                if (virtualService != null) {
                    VirtualDeviceCreator.this.mVirtualService = virtualService;
                    if (VirtualDeviceCreator.this.mIConnectCallback != null) {
                        VirtualDeviceCreator.this.mIConnectCallback.onConnect(0);
                    }
                    if (VirtualDeviceCreator.this.mIDeviceConnectCallback != null) {
                        VirtualDeviceCreator.this.mIDeviceConnectCallback.onConnect(0);
                        return;
                    }
                    return;
                }
                HwLog.e(VirtualDeviceCreator.TAG, "VirtualService is null");
                if (VirtualDeviceCreator.this.mIConnectCallback != null) {
                    VirtualDeviceCreator.this.mIConnectCallback.onConnect(1);
                    VirtualDeviceCreator.this.mIConnectCallback = null;
                }
                if (VirtualDeviceCreator.this.mIDeviceConnectCallback != null) {
                    VirtualDeviceCreator.this.mIDeviceConnectCallback.onConnect(1);
                    VirtualDeviceCreator.this.mIDeviceConnectCallback = null;
                }
            }

            @Override // ohos.msdp.devicevirtualization.IDmsdpServiceCallback
            public void onBinderDied() {
                HwLog.i(VirtualDeviceCreator.TAG, "start disconnect");
                if (VirtualDeviceCreator.this.mIConnectCallback != null) {
                    VirtualDeviceCreator.this.mIConnectCallback.onDisconnect();
                    VirtualDeviceCreator.this.mIConnectCallback = null;
                }
                if (VirtualDeviceCreator.this.mIDeviceConnectCallback != null) {
                    HwLog.i(VirtualDeviceCreator.TAG, "start disconnect dmsdpdevice");
                    VirtualDeviceCreator.this.mIDeviceConnectCallback.onDisconnect();
                    VirtualDeviceCreator.this.mIDeviceConnectCallback = null;
                }
                VirtualDeviceCreator.this.mDmsdpServiceCallback = null;
                if (VirtualDeviceCreator.this.mVirtualService != null) {
                    for (Map.Entry entry : VirtualDeviceCreator.this.mKitServices.entrySet()) {
                        ((VirtualManager) entry.getValue()).onDisConnect();
                    }
                    VirtualService.releaseInstance();
                    HwLog.d(VirtualDeviceCreator.TAG, "releaseInstance");
                    VirtualDeviceCreator.this.mVirtualService = null;
                }
                VirtualDeviceCreator.this.mKitServices.clear();
            }
        };
    }

    public void disConnect() {
        HwLog.i(TAG, "disConnect");
        this.mIConnectCallback = null;
        this.mIDeviceConnectCallback = null;
        this.mDmsdpServiceCallback = null;
        for (Map.Entry<String, VirtualManager> entry : this.mKitServices.entrySet()) {
            entry.getValue().onDisConnect();
        }
        this.mKitServices.clear();
        if (this.mVirtualService != null) {
            VirtualService.releaseInstance();
            HwLog.d(TAG, "releaseInstance");
            this.mVirtualService = null;
        }
    }

    public VirtualManager getService(String str) {
        HwLog.i(TAG, "getService");
        if (str == null || str.length() == 0 || this.mVirtualService == null) {
            return null;
        }
        if (this.mKitServices.containsKey(str)) {
            HwLog.i(TAG, "mKitServiceManager has contains serviceClass");
            return this.mKitServices.get(str);
        } else if (VIRTUAL_DEVICE_CLASS.equals(str)) {
            HwLog.i(TAG, "mVirtualService is not null");
            VirtualDeviceManager instance = VirtualDeviceManager.getInstance();
            instance.onConnect(this.mVirtualService);
            this.mKitServices.put(str, instance);
            return instance;
        } else {
            if (VIRTUAL_AGENT_DEVICE_CLASS.equals(str)) {
                HwLog.i(TAG, "mVirtualService is not null");
                VirtualDeviceManager instance2 = VirtualDeviceManager.getInstance();
                instance2.onConnect(this.mVirtualService);
                this.mKitServices.put(str, instance2);
                return instance2;
            }
            return null;
        }
    }

    public static String getVersion() {
        return Version.getVersion();
    }
}
