package com.android.server.wifi.p2p;

import android.hardware.wifi.supplicant.V1_0.ISupplicant;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pNetwork;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatusCode;
import android.hardware.wifi.supplicant.V1_0.WpsConfigMethods;
import android.hardware.wifi.supplicant.V1_1.ISupplicant;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.os.IHwBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.android.server.wifi.ScoringParams;
import com.android.server.wifi.p2p.SupplicantP2pIfaceHal;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.StringUtil;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantP2pIface;
import vendor.huawei.hardware.wifi.supplicant.V2_1.ISupplicantP2pIfaceCallback;

public class SupplicantP2pIfaceHal {
    private static final boolean DBG = true;
    private static final int DEFAULT_GROUP_OWNER_INTENT = 6;
    private static final int DEFAULT_OPERATING_CLASS = 81;
    private static final int HIDL_V2_0 = 200;
    private static final int HIDL_V2_1 = 201;
    private static final int RESULT_NOT_VALID = -1;
    private static final String TAG = "SupplicantP2pIfaceHal";
    private static final Pattern WPS_DEVICE_TYPE_PATTERN = Pattern.compile("^(\\d{1,2})-([0-9a-fA-F]{8})-(\\d{1,2})$");
    private SupplicantP2pIfaceCallback mCallback = null;
    private ISupplicantIface mHidlSupplicantIface = null;
    private int mHidlVersion = HIDL_V2_1;
    private IServiceManager mIServiceManager = null;
    private ISupplicant mISupplicant = null;
    private ISupplicantP2pIface mISupplicantP2pIface = null;
    /* access modifiers changed from: private */
    public Object mLock = new Object();
    private final WifiP2pMonitor mMonitor;
    private final IHwBinder.DeathRecipient mServiceManagerDeathRecipient = new IHwBinder.DeathRecipient() {
        public final void serviceDied(long j) {
            SupplicantP2pIfaceHal.lambda$new$0(SupplicantP2pIfaceHal.this, j);
        }
    };
    private final IServiceNotification mServiceNotificationCallback = new IServiceNotification.Stub() {
        public void onRegistration(String fqName, String name, boolean preexisting) {
            synchronized (SupplicantP2pIfaceHal.this.mLock) {
                Log.i(SupplicantP2pIfaceHal.TAG, "IServiceNotification.onRegistration for: " + fqName + ", " + name + " preexisting=" + preexisting);
                if (!SupplicantP2pIfaceHal.this.initSupplicantService()) {
                    Log.e(SupplicantP2pIfaceHal.TAG, "initalizing ISupplicant failed.");
                    SupplicantP2pIfaceHal.this.supplicantServiceDiedHandler();
                } else {
                    Log.i(SupplicantP2pIfaceHal.TAG, "Completed initialization of ISupplicant interfaces.");
                }
            }
        }
    };
    private final IHwBinder.DeathRecipient mSupplicantDeathRecipient = new IHwBinder.DeathRecipient() {
        public final void serviceDied(long j) {
            SupplicantP2pIfaceHal.lambda$new$1(SupplicantP2pIfaceHal.this, j);
        }
    };

    private static class SupplicantResult<E> {
        private boolean mHasRecordHalTime = false;
        private String mMethodName;
        private long mP2pHalCallStartTime;
        private SupplicantStatus mStatus;
        private E mValue;

        SupplicantResult(String methodName) {
            this.mMethodName = methodName;
            this.mStatus = null;
            this.mValue = null;
            SupplicantP2pIfaceHal.logd("entering " + this.mMethodName);
            this.mP2pHalCallStartTime = SystemClock.uptimeMillis();
            this.mHasRecordHalTime = false;
        }

        public void setResult(SupplicantStatus status, E value) {
            SupplicantP2pIfaceHal.logCompletion(this.mMethodName, status);
            if (value == null || !(value instanceof String) || !((String) value).matches("([A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2}")) {
                SupplicantP2pIfaceHal.logd("leaving " + this.mMethodName + " with result = " + value);
            } else {
                SupplicantP2pIfaceHal.logd("leaving " + this.mMethodName + " with result = " + StringUtil.safeDisplayBssid((String) value));
            }
            this.mStatus = status;
            this.mValue = value;
            checkHalCallThresholdMs();
        }

        public void setResult(SupplicantStatus status) {
            SupplicantP2pIfaceHal.logCompletion(this.mMethodName, status);
            SupplicantP2pIfaceHal.logd("leaving " + this.mMethodName);
            this.mStatus = status;
            checkHalCallThresholdMs();
        }

        public boolean isSuccess() {
            checkHalCallThresholdMs();
            return this.mStatus != null && (this.mStatus.code == 0 || this.mStatus.code == 5);
        }

        public E getResult() {
            checkHalCallThresholdMs();
            if (isSuccess()) {
                return this.mValue;
            }
            return null;
        }

        private void checkHalCallThresholdMs() {
            if (!this.mHasRecordHalTime) {
                long mP2pHalCallEndTime = SystemClock.uptimeMillis();
                int statusCode = -1;
                if (this.mStatus != null) {
                    statusCode = this.mStatus.code;
                }
                if (mP2pHalCallEndTime - this.mP2pHalCallStartTime > 300) {
                    Log.w(SupplicantP2pIfaceHal.TAG, "Hal call took " + (mP2pHalCallEndTime - this.mP2pHalCallStartTime) + "ms on " + this.mMethodName + ", status.code:" + SupplicantStatusCode.toString(statusCode), new Exception());
                }
                this.mHasRecordHalTime = true;
            }
        }
    }

    private class VendorSupplicantP2pIfaceHalCallbackV2_1 extends ISupplicantP2pIfaceCallback.Stub {
        private static final String TAG = "SupplicantP2pIfaceCallback";
        SupplicantP2pIfaceCallback mCallback;
        private final String mInterface;
        private final WifiP2pMonitor mMonitor;

        VendorSupplicantP2pIfaceHalCallbackV2_1(String iface, WifiP2pMonitor monitor, SupplicantP2pIfaceCallback callback) {
            this.mInterface = iface;
            this.mMonitor = monitor;
            this.mCallback = callback;
        }

        public void onNetworkAdded(int networkId) {
            this.mCallback.onNetworkAdded(networkId);
        }

        public void onNetworkRemoved(int networkId) {
            this.mCallback.onNetworkRemoved(networkId);
        }

        public void onDeviceFound(byte[] srcAddress, byte[] p2pDeviceAddress, byte[] primaryDeviceType, String deviceName, short configMethods, byte deviceCapabilities, int groupCapabilities, byte[] wfdDeviceInfo) {
            this.mCallback.onDeviceFound(srcAddress, p2pDeviceAddress, primaryDeviceType, deviceName, configMethods, deviceCapabilities, groupCapabilities, wfdDeviceInfo);
        }

        public void onDeviceLost(byte[] p2pDeviceAddress) {
            this.mCallback.onDeviceLost(p2pDeviceAddress);
        }

        public void onFindStopped() {
            this.mCallback.onFindStopped();
        }

        public void onGoNegotiationRequest(byte[] srcAddress, short passwordId) {
            this.mCallback.onGoNegotiationRequest(srcAddress, passwordId);
        }

        public void onGoNegotiationCompleted(int status) {
            this.mCallback.onGoNegotiationCompleted(status);
        }

        public void onGroupFormationSuccess() {
            this.mCallback.onGroupFormationSuccess();
        }

        public void onGroupFormationFailure(String failureReason) {
            this.mCallback.onGroupFormationFailure(failureReason);
        }

        public void onGroupStarted(String groupIfName, boolean isGo, ArrayList<Byte> ssid, int frequency, byte[] psk, String passphrase, byte[] goDeviceAddress, boolean isPersistent) {
            this.mCallback.onGroupStarted(groupIfName, isGo, ssid, frequency, psk, passphrase, goDeviceAddress, isPersistent);
        }

        public void onGroupRemoved(String groupIfName, boolean isGo) {
            this.mCallback.onGroupRemoved(groupIfName, isGo);
        }

        public void onInvitationReceived(byte[] srcAddress, byte[] goDeviceAddress, byte[] bssid, int persistentNetworkId, int operatingFrequency) {
            this.mCallback.onInvitationReceived(srcAddress, goDeviceAddress, bssid, persistentNetworkId, operatingFrequency);
        }

        public void onInvitationResult(byte[] bssid, int status) {
            this.mCallback.onInvitationResult(bssid, status);
        }

        public void onProvisionDiscoveryCompleted(byte[] p2pDeviceAddress, boolean isRequest, byte status, short configMethods, String generatedPin) {
            this.mCallback.onProvisionDiscoveryCompleted(p2pDeviceAddress, isRequest, status, configMethods, generatedPin);
        }

        public void onServiceDiscoveryResponse(byte[] srcAddress, short updateIndicator, ArrayList<Byte> tlvs) {
            this.mCallback.onServiceDiscoveryResponse(srcAddress, updateIndicator, tlvs);
        }

        public void onStaAuthorized(byte[] srcAddress, byte[] p2pDeviceAddress) {
            this.mCallback.onStaAuthorized(srcAddress, p2pDeviceAddress);
        }

        public void onStaDeauthorized(byte[] srcAddress, byte[] p2pDeviceAddress) {
            this.mCallback.onStaDeauthorized(srcAddress, p2pDeviceAddress);
        }

        public void onP2pInterfaceCreated(String dataString) {
            Log.d(TAG, "onP2pInterfaceCreated " + dataString);
            if (dataString != null) {
                String[] tokens = dataString.split(" ");
                if (tokens.length >= 3) {
                    if (tokens[1].startsWith("GO")) {
                        this.mMonitor.broadcastP2pGoInterfaceCreated(this.mInterface, tokens[2]);
                    } else {
                        this.mMonitor.broadcastP2pGcInterfaceCreated(this.mInterface, tokens[2]);
                    }
                }
            }
        }

        public void onGroupRemoveAndReform(String iface) {
            this.mMonitor.broadcastP2pGroupRemoveAndReform(this.mInterface);
        }

        public void onHwDeviceFound(byte[] srcAddress, byte[] p2pDeviceAddress, byte[] primaryDeviceType, String deviceName, short configMethods, byte deviceCapabilities, int groupCapabilities, byte[] wfdDeviceInfo) {
            byte[] bArr = srcAddress;
            byte[] bArr2 = p2pDeviceAddress;
            byte[] bArr3 = primaryDeviceType;
            String str = deviceName;
            short s = configMethods;
            byte b = deviceCapabilities;
            int i = groupCapabilities;
            byte[] bArr4 = wfdDeviceInfo;
            this.mCallback.getHwSupplicantP2pIfaceCallbackExt().onHwDeviceFound(bArr, bArr2, bArr3, str, s, b, i, bArr4);
            this.mCallback.onDeviceFound(bArr, bArr2, bArr3, str, s, b, i, bArr4);
            SupplicantP2pIfaceHal.logd("HwDevice discovered");
        }
    }

    public static /* synthetic */ void lambda$new$0(SupplicantP2pIfaceHal supplicantP2pIfaceHal, long cookie) {
        Log.w(TAG, "IServiceManager died: cookie=" + cookie);
        synchronized (supplicantP2pIfaceHal.mLock) {
            supplicantP2pIfaceHal.supplicantServiceDiedHandler();
            supplicantP2pIfaceHal.mIServiceManager = null;
        }
    }

    public static /* synthetic */ void lambda$new$1(SupplicantP2pIfaceHal supplicantP2pIfaceHal, long cookie) {
        Log.w(TAG, "ISupplicant/ISupplicantStaIface died: cookie=" + cookie);
        synchronized (supplicantP2pIfaceHal.mLock) {
            supplicantP2pIfaceHal.supplicantServiceDiedHandler();
        }
    }

    public SupplicantP2pIfaceHal(WifiP2pMonitor monitor) {
        this.mMonitor = monitor;
    }

    private boolean linkToServiceManagerDeath() {
        if (this.mIServiceManager == null) {
            return false;
        }
        try {
            if (this.mIServiceManager.linkToDeath(this.mServiceManagerDeathRecipient, 0)) {
                return true;
            }
            Log.wtf(TAG, "Error on linkToDeath on IServiceManager");
            supplicantServiceDiedHandler();
            this.mIServiceManager = null;
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "IServiceManager.linkToDeath exception", e);
            return false;
        }
    }

    public boolean initialize() {
        Log.i(TAG, "Registering ISupplicant service ready callback.");
        synchronized (this.mLock) {
            if (this.mIServiceManager != null) {
                Log.i(TAG, "Supplicant HAL already initialized.");
                return true;
            }
            this.mISupplicant = null;
            this.mISupplicantP2pIface = null;
            try {
                this.mIServiceManager = getServiceManagerMockable();
                if (this.mIServiceManager == null) {
                    Log.e(TAG, "Failed to get HIDL Service Manager");
                    return false;
                } else if (!linkToServiceManagerDeath()) {
                    return false;
                } else {
                    if (this.mIServiceManager.registerForNotifications(ISupplicant.kInterfaceName, "", this.mServiceNotificationCallback)) {
                        return true;
                    }
                    Log.e(TAG, "Failed to register for notifications to android.hardware.wifi.supplicant@1.0::ISupplicant");
                    this.mIServiceManager = null;
                    return false;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Exception while trying to register a listener for ISupplicant service: " + e);
                supplicantServiceDiedHandler();
                return false;
            }
        }
    }

    private boolean linkToSupplicantDeath() {
        if (this.mISupplicant == null) {
            return false;
        }
        try {
            if (this.mISupplicant.linkToDeath(this.mSupplicantDeathRecipient, 0)) {
                return true;
            }
            Log.wtf(TAG, "Error on linkToDeath on ISupplicant");
            supplicantServiceDiedHandler();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "ISupplicant.linkToDeath exception", e);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public boolean initSupplicantService() {
        synchronized (this.mLock) {
            try {
                this.mISupplicant = getSupplicantMockable();
                if (this.mISupplicant == null) {
                    Log.e(TAG, "Got null ISupplicant service. Stopping supplicant HIDL startup");
                    return false;
                } else if (!linkToSupplicantDeath()) {
                    return false;
                } else {
                    return true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicant.getService exception: " + e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private boolean linkToSupplicantP2pIfaceDeath() {
        if (this.mISupplicantP2pIface == null) {
            return false;
        }
        try {
            if (this.mISupplicantP2pIface.linkToDeath(this.mSupplicantDeathRecipient, 0)) {
                return true;
            }
            Log.wtf(TAG, "Error on linkToDeath on ISupplicantP2pIface");
            supplicantServiceDiedHandler();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "ISupplicantP2pIface.linkToDeath exception", e);
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0068, code lost:
        return true;
     */
    public boolean setupIface(String ifaceName) {
        ISupplicantIface ifaceHwBinder;
        if (ifaceName == null) {
            Log.e(TAG, "Got null ifaceName when setting up p2p Iface");
            return false;
        }
        synchronized (this.mLock) {
            if (this.mISupplicantP2pIface != null) {
                return false;
            }
            if (isV1_1()) {
                ifaceHwBinder = addIfaceV1_1(ifaceName);
            } else {
                ifaceHwBinder = getIfaceV1_0(ifaceName);
            }
            if (ifaceHwBinder == null) {
                Log.e(TAG, "initSupplicantP2pIface got null iface");
                return false;
            } else if (trySetupForVendor(ifaceHwBinder, ifaceName)) {
                return true;
            } else {
                this.mISupplicantP2pIface = getP2pIfaceMockable(ifaceHwBinder);
                if (!linkToSupplicantP2pIfaceDeath()) {
                    return false;
                }
                if (!(this.mISupplicantP2pIface == null || this.mMonitor == null)) {
                    this.mCallback = new SupplicantP2pIfaceCallback(ifaceName, this.mMonitor);
                    if (!registerCallback(this.mCallback)) {
                        Log.e(TAG, "Callback registration failed. Initialization incomplete.");
                        return false;
                    }
                }
            }
        }
    }

    private ISupplicantIface getIfaceV1_0(String ifaceName) {
        ArrayList<ISupplicant.IfaceInfo> supplicantIfaces = new ArrayList<>();
        try {
            this.mISupplicant.listInterfaces(new ISupplicant.listInterfacesCallback(supplicantIfaces) {
                private final /* synthetic */ ArrayList f$0;

                {
                    this.f$0 = r1;
                }

                public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                    SupplicantP2pIfaceHal.lambda$getIfaceV1_0$2(this.f$0, supplicantStatus, arrayList);
                }
            });
            if (supplicantIfaces.size() == 0) {
                Log.e(TAG, "Got zero HIDL supplicant ifaces. Stopping supplicant HIDL startup.");
                supplicantServiceDiedHandler();
                return null;
            }
            SupplicantResult<ISupplicantIface> supplicantIface = new SupplicantResult<>("getInterface()");
            Iterator<ISupplicant.IfaceInfo> it = supplicantIfaces.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ISupplicant.IfaceInfo ifaceInfo = it.next();
                if (ifaceInfo.type == 1 && ifaceName.equals(ifaceInfo.name)) {
                    try {
                        this.mISupplicant.getInterface(ifaceInfo, new ISupplicant.getInterfaceCallback() {
                            public final void onValues(SupplicantStatus supplicantStatus, ISupplicantIface iSupplicantIface) {
                                SupplicantP2pIfaceHal.lambda$getIfaceV1_0$3(SupplicantP2pIfaceHal.SupplicantResult.this, supplicantStatus, iSupplicantIface);
                            }
                        });
                        break;
                    } catch (RemoteException e) {
                        Log.e(TAG, "ISupplicant.getInterface exception: " + e);
                        supplicantServiceDiedHandler();
                        return null;
                    }
                }
            }
            return supplicantIface.getResult();
        } catch (RemoteException e2) {
            Log.e(TAG, "ISupplicant.listInterfaces exception: " + e2);
            return null;
        }
    }

    static /* synthetic */ void lambda$getIfaceV1_0$2(ArrayList supplicantIfaces, SupplicantStatus status, ArrayList ifaces) {
        if (status.code != 0) {
            Log.e(TAG, "Getting Supplicant Interfaces failed: " + status.code);
            return;
        }
        supplicantIfaces.addAll(ifaces);
    }

    static /* synthetic */ void lambda$getIfaceV1_0$3(SupplicantResult supplicantIface, SupplicantStatus status, ISupplicantIface iface) {
        if (status.code != 0) {
            Log.e(TAG, "Failed to get ISupplicantIface " + status.code);
            return;
        }
        supplicantIface.setResult(status, iface);
    }

    private ISupplicantIface addIfaceV1_1(String ifaceName) {
        synchronized (this.mLock) {
            ISupplicant.IfaceInfo ifaceInfo = new ISupplicant.IfaceInfo();
            ifaceInfo.name = ifaceName;
            ifaceInfo.type = 1;
            SupplicantResult<ISupplicantIface> supplicantIface = new SupplicantResult<>("addInterface(" + ifaceInfo + ")");
            try {
                android.hardware.wifi.supplicant.V1_1.ISupplicant supplicant_v1_1 = getSupplicantMockableV1_1();
                if (supplicant_v1_1 == null) {
                    Log.e(TAG, "Can't call addIface: ISupplicantP2pIface is null");
                    return null;
                }
                supplicant_v1_1.addInterface(ifaceInfo, new ISupplicant.addInterfaceCallback() {
                    public final void onValues(SupplicantStatus supplicantStatus, ISupplicantIface iSupplicantIface) {
                        SupplicantP2pIfaceHal.lambda$addIfaceV1_1$4(SupplicantP2pIfaceHal.SupplicantResult.this, supplicantStatus, iSupplicantIface);
                    }
                });
                ISupplicantIface result = supplicantIface.getResult();
                return result;
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicant.addInterface exception: " + e);
                supplicantServiceDiedHandler();
                return null;
            }
        }
    }

    static /* synthetic */ void lambda$addIfaceV1_1$4(SupplicantResult supplicantIface, SupplicantStatus status, ISupplicantIface iface) {
        if (status.code == 0 || status.code == 5) {
            supplicantIface.setResult(status, iface);
            return;
        }
        Log.e(TAG, "Failed to get ISupplicantIface " + status.code);
    }

    public boolean teardownIface(String ifaceName) {
        synchronized (this.mLock) {
            if (this.mISupplicantP2pIface == null) {
                return false;
            }
            if (!isV1_1()) {
                return true;
            }
            boolean removeIfaceV1_1 = removeIfaceV1_1(ifaceName);
            return removeIfaceV1_1;
        }
    }

    private boolean removeIfaceV1_1(String ifaceName) {
        synchronized (this.mLock) {
            try {
                android.hardware.wifi.supplicant.V1_1.ISupplicant supplicant_v1_1 = getSupplicantMockableV1_1();
                if (supplicant_v1_1 == null) {
                    Log.e(TAG, "Can't call removeIface: ISupplicantP2pIface is null");
                    return false;
                }
                ISupplicant.IfaceInfo ifaceInfo = new ISupplicant.IfaceInfo();
                ifaceInfo.name = ifaceName;
                ifaceInfo.type = 1;
                SupplicantStatus status = supplicant_v1_1.removeInterface(ifaceInfo);
                if (status.code != 0) {
                    Log.e(TAG, "Failed to remove iface " + status.code);
                    return false;
                }
                this.mCallback = null;
                this.mISupplicantP2pIface = null;
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicant.removeInterface exception: " + e);
                supplicantServiceDiedHandler();
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public void supplicantServiceDiedHandler() {
        synchronized (this.mLock) {
            this.mISupplicant = null;
            this.mISupplicantP2pIface = null;
        }
    }

    public boolean isInitializationStarted() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mIServiceManager != null;
        }
        return z;
    }

    public boolean isInitializationComplete() {
        return this.mISupplicant != null;
    }

    /* access modifiers changed from: protected */
    public IServiceManager getServiceManagerMockable() throws RemoteException {
        return IServiceManager.getService();
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.supplicant.V1_0.ISupplicant getSupplicantMockable() throws RemoteException {
        try {
            return android.hardware.wifi.supplicant.V1_0.ISupplicant.getService();
        } catch (NoSuchElementException e) {
            Log.e(TAG, "Failed to get ISupplicant", e);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public android.hardware.wifi.supplicant.V1_1.ISupplicant getSupplicantMockableV1_1() throws RemoteException {
        android.hardware.wifi.supplicant.V1_1.ISupplicant castFrom;
        synchronized (this.mLock) {
            try {
                castFrom = android.hardware.wifi.supplicant.V1_1.ISupplicant.castFrom(android.hardware.wifi.supplicant.V1_0.ISupplicant.getService());
            } catch (NoSuchElementException e) {
                Log.e(TAG, "Failed to get ISupplicant", e);
                return null;
            } catch (Throwable th) {
                throw th;
            }
        }
        return castFrom;
    }

    /* access modifiers changed from: protected */
    public ISupplicantP2pIface getP2pIfaceMockable(ISupplicantIface iface) {
        return ISupplicantP2pIface.asInterface(iface.asBinder());
    }

    /* access modifiers changed from: protected */
    public ISupplicantP2pNetwork getP2pNetworkMockable(ISupplicantNetwork network) {
        return ISupplicantP2pNetwork.asInterface(network.asBinder());
    }

    private boolean isV1_1() {
        boolean z;
        synchronized (this.mLock) {
            z = false;
            try {
                if (getSupplicantMockableV1_1() != null) {
                    z = true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicant.getService exception: " + e);
                supplicantServiceDiedHandler();
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return z;
    }

    protected static void logd(String s) {
        Log.d(TAG, s);
    }

    protected static void logCompletion(String operation, SupplicantStatus status) {
        if (status == null) {
            Log.w(TAG, operation + " failed: no status code returned.");
        } else if (status.code == 0) {
            logd(operation + " completed successfully.");
        } else {
            Log.w(TAG, operation + " failed: " + status.code + " (" + status.debugMessage + ")");
        }
    }

    private boolean checkSupplicantP2pIfaceAndLogFailure(String method) {
        if (this.mISupplicantP2pIface != null) {
            return true;
        }
        Log.e(TAG, "Can't call " + method + ": ISupplicantP2pIface is null");
        return false;
    }

    private int wpsInfoToConfigMethod(int info) {
        switch (info) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
            case 3:
                return 2;
            default:
                Log.e(TAG, "Unsupported WPS provision method: " + info);
                return -1;
        }
    }

    public String getName() {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("getName")) {
                return null;
            }
            SupplicantResult<String> result = new SupplicantResult<>("getName()");
            try {
                this.mISupplicantP2pIface.getName(new ISupplicantIface.getNameCallback() {
                    public final void onValues(SupplicantStatus supplicantStatus, String str) {
                        SupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, str);
                    }
                });
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            String result2 = result.getResult();
            return result2;
        }
    }

    public boolean registerCallback(android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIfaceCallback receiver) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("registerCallback")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("registerCallback()");
            try {
                result.setResult(this.mISupplicantP2pIface.registerCallback(receiver));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean find(int timeout) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("find")) {
                return false;
            }
            if (timeout < 0) {
                Log.e(TAG, "Invalid timeout value: " + timeout);
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("find(" + timeout + ")");
            try {
                result.setResult(this.mISupplicantP2pIface.find(timeout));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean stopFind() {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("stopFind")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("stopFind()");
            try {
                result.setResult(this.mISupplicantP2pIface.stopFind());
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean flush() {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("flush")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("flush()");
            try {
                result.setResult(this.mISupplicantP2pIface.flush());
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean serviceFlush() {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("serviceFlush")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("serviceFlush()");
            try {
                result.setResult(this.mISupplicantP2pIface.flushServices());
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean setPowerSave(String groupIfName, boolean enable) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("setPowerSave")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("setPowerSave(" + groupIfName + ", " + enable + ")");
            try {
                result.setResult(this.mISupplicantP2pIface.setPowerSave(groupIfName, enable));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean setGroupIdle(String groupIfName, int timeoutInSec) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("setGroupIdle")) {
                return false;
            }
            if (timeoutInSec < 0) {
                Log.e(TAG, "Invalid group timeout value " + timeoutInSec);
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("setGroupIdle(" + groupIfName + ", " + timeoutInSec + ")");
            try {
                result.setResult(this.mISupplicantP2pIface.setGroupIdle(groupIfName, timeoutInSec));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean setSsidPostfix(String postfix) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("setSsidPostfix")) {
                return false;
            }
            if (postfix == null) {
                Log.e(TAG, "Invalid SSID postfix value (null).");
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("setSsidPostfix(" + postfix + ")");
            try {
                ISupplicantP2pIface iSupplicantP2pIface = this.mISupplicantP2pIface;
                result.setResult(iSupplicantP2pIface.setSsidPostfix(NativeUtil.decodeSsid("\"" + postfix + "\"")));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            } catch (IllegalArgumentException e2) {
                Log.e(TAG, "Could not decode SSID.", e2);
                return false;
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public String connect(WifiP2pConfig config, boolean joinExistingGroup) {
        if (config == null) {
            return null;
        }
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("setSsidPostfix")) {
                return null;
            }
            if (config.deviceAddress == null) {
                Log.e(TAG, "Could not parse null mac address.");
                return null;
            } else if (config.wps.setup != 0 || TextUtils.isEmpty(config.wps.pin)) {
                try {
                    byte[] peerAddress = NativeUtil.macAddressToByteArray(config.deviceAddress);
                    int provisionMethod = wpsInfoToConfigMethod(config.wps.setup);
                    if (provisionMethod == -1) {
                        Log.e(TAG, "Invalid WPS config method: " + config.wps.setup);
                        return null;
                    }
                    String preSelectedPin = TextUtils.isEmpty(config.wps.pin) ? "" : config.wps.pin;
                    boolean persistent = config.netId == -2;
                    int goIntent = 0;
                    if (!joinExistingGroup) {
                        int groupOwnerIntent = config.groupOwnerIntent;
                        if (groupOwnerIntent < 0 || groupOwnerIntent > 15) {
                            groupOwnerIntent = 6;
                        }
                        goIntent = groupOwnerIntent;
                    }
                    SupplicantResult<String> result = new SupplicantResult<>("connect(" + StringUtil.safeDisplayBssid(config.deviceAddress) + ")");
                    try {
                        this.mISupplicantP2pIface.connect(peerAddress, provisionMethod, preSelectedPin, joinExistingGroup, persistent, goIntent, new ISupplicantP2pIface.connectCallback() {
                            public final void onValues(SupplicantStatus supplicantStatus, String str) {
                                SupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, str);
                            }
                        });
                    } catch (RemoteException e) {
                        Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                        supplicantServiceDiedHandler();
                    }
                    String result2 = result.getResult();
                    return result2;
                } catch (Exception e2) {
                    Log.e(TAG, "Could not parse peer mac address.", e2);
                    return null;
                }
            } else {
                Log.e(TAG, "Expected empty pin for PBC.");
                return null;
            }
        }
    }

    public boolean cancelConnect() {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("cancelConnect")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("cancelConnect()");
            try {
                result.setResult(this.mISupplicantP2pIface.cancelConnect());
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean provisionDiscovery(WifiP2pConfig config) {
        if (config == null) {
            return false;
        }
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("provisionDiscovery")) {
                return false;
            }
            int targetMethod = wpsInfoToConfigMethod(config.wps.setup);
            if (targetMethod == -1) {
                Log.e(TAG, "Unrecognized WPS configuration method: " + config.wps.setup);
                return false;
            }
            if (targetMethod == 1) {
                targetMethod = 2;
            } else if (targetMethod == 2) {
                targetMethod = 1;
            }
            if (config.deviceAddress == null) {
                Log.e(TAG, "Cannot parse null mac address.");
                return false;
            }
            try {
                byte[] macAddress = NativeUtil.macAddressToByteArray(config.deviceAddress);
                SupplicantResult<Void> result = new SupplicantResult<>("provisionDiscovery(" + config.deviceAddress + ", " + config.wps.setup + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.provisionDiscovery(macAddress, targetMethod));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            } catch (Exception e2) {
                Log.e(TAG, "Could not parse peer mac address.", e2);
                return false;
            }
        }
    }

    public boolean invite(WifiP2pGroup group, String peerAddress) {
        if (TextUtils.isEmpty(peerAddress)) {
            return false;
        }
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("invite")) {
                return false;
            }
            if (group == null) {
                Log.e(TAG, "Cannot invite to null group.");
                return false;
            } else if (group.getOwner() == null) {
                Log.e(TAG, "Cannot invite to group with null owner.");
                return false;
            } else if (group.getOwner().deviceAddress == null) {
                Log.e(TAG, "Group owner has no mac address.");
                return false;
            } else {
                try {
                    byte[] ownerMacAddress = NativeUtil.macAddressToByteArray(group.getOwner().deviceAddress);
                    if (peerAddress == null) {
                        Log.e(TAG, "Cannot parse peer mac address.");
                        return false;
                    }
                    try {
                        byte[] peerMacAddress = NativeUtil.macAddressToByteArray(peerAddress);
                        SupplicantResult<Void> result = new SupplicantResult<>("invite(" + group.getInterface() + ", " + group.getOwner().deviceAddress + ", " + peerAddress + ")");
                        try {
                            result.setResult(this.mISupplicantP2pIface.invite(group.getInterface(), ownerMacAddress, peerMacAddress));
                        } catch (RemoteException e) {
                            Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                            supplicantServiceDiedHandler();
                        }
                        boolean isSuccess = result.isSuccess();
                        return isSuccess;
                    } catch (Exception e2) {
                        Log.e(TAG, "Peer mac address parse error.", e2);
                        return false;
                    }
                } catch (Exception e3) {
                    Log.e(TAG, "Group owner mac address parse error.", e3);
                    return false;
                }
            }
        }
    }

    public boolean reject(String peerAddress) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("reject")) {
                return false;
            }
            if (peerAddress == null) {
                Log.e(TAG, "Cannot parse rejected peer's mac address.");
                return false;
            }
            try {
                byte[] macAddress = NativeUtil.macAddressToByteArray(peerAddress);
                SupplicantResult<Void> result = new SupplicantResult<>("reject(" + peerAddress + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.reject(macAddress));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            } catch (IllegalArgumentException e2) {
                Log.e(TAG, "IllegalArgumentException Could not parse peer mac address.", e2);
                return false;
            } catch (Exception e3) {
                Log.e(TAG, "Could not parse peer mac address.", e3);
                return false;
            }
        }
    }

    public String getDeviceAddress() {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("getDeviceAddress")) {
                return null;
            }
            SupplicantResult<String> result = new SupplicantResult<>("getDeviceAddress()");
            try {
                this.mISupplicantP2pIface.getDeviceAddress(new ISupplicantP2pIface.getDeviceAddressCallback() {
                    public final void onValues(SupplicantStatus supplicantStatus, byte[] bArr) {
                        SupplicantP2pIfaceHal.lambda$getDeviceAddress$7(SupplicantP2pIfaceHal.SupplicantResult.this, supplicantStatus, bArr);
                    }
                });
                String result2 = result.getResult();
                return result2;
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
                return null;
            }
        }
    }

    static /* synthetic */ void lambda$getDeviceAddress$7(SupplicantResult result, SupplicantStatus status, byte[] address) {
        String parsedAddress = null;
        try {
            parsedAddress = NativeUtil.macAddressFromByteArray(address);
        } catch (Exception e) {
            Log.e(TAG, "Could not process reported address.", e);
        }
        result.setResult(status, parsedAddress);
    }

    public String getSsid(String address) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("getSsid")) {
                return null;
            }
            if (address == null) {
                Log.e(TAG, "Cannot parse peer mac address.");
                return null;
            }
            try {
                byte[] macAddress = NativeUtil.macAddressToByteArray(address);
                SupplicantResult<String> result = new SupplicantResult<>("getSsid(" + address + ")");
                try {
                    this.mISupplicantP2pIface.getSsid(macAddress, new ISupplicantP2pIface.getSsidCallback() {
                        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                            SupplicantP2pIfaceHal.lambda$getSsid$8(SupplicantP2pIfaceHal.SupplicantResult.this, supplicantStatus, arrayList);
                        }
                    });
                    String result2 = result.getResult();
                    return result2;
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                    return null;
                }
            } catch (Exception e2) {
                Log.e(TAG, "Could not parse mac address.", e2);
                return null;
            }
        }
    }

    static /* synthetic */ void lambda$getSsid$8(SupplicantResult result, SupplicantStatus status, ArrayList ssid) {
        String ssidString = null;
        if (ssid != null) {
            try {
                ssidString = NativeUtil.removeEnclosingQuotes(NativeUtil.encodeSsid(ssid));
            } catch (Exception e) {
                Log.e(TAG, "Could not encode SSID.", e);
            }
        }
        result.setResult(status, ssidString);
    }

    public boolean reinvoke(int networkId, String peerAddress) {
        if (TextUtils.isEmpty(peerAddress) || networkId < 0) {
            return false;
        }
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("reinvoke")) {
                return false;
            }
            if (peerAddress == null) {
                Log.e(TAG, "Cannot parse peer mac address.");
                return false;
            }
            try {
                byte[] macAddress = NativeUtil.macAddressToByteArray(peerAddress);
                SupplicantResult<Void> result = new SupplicantResult<>("reinvoke(" + networkId + ", " + peerAddress + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.reinvoke(networkId, macAddress));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            } catch (Exception e2) {
                Log.e(TAG, "Could not parse mac address.", e2);
                return false;
            }
        }
    }

    public boolean groupAdd(int networkId, boolean isPersistent) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("groupAdd")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("groupAdd(" + networkId + ", " + isPersistent + ")");
            try {
                result.setResult(this.mISupplicantP2pIface.addGroup(isPersistent, networkId));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean groupAdd(boolean isPersistent) {
        return groupAdd(-1, isPersistent);
    }

    public boolean groupRemove(String groupName) {
        if (TextUtils.isEmpty(groupName)) {
            return false;
        }
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("groupRemove")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("groupRemove(" + groupName + ")");
            try {
                result.setResult(this.mISupplicantP2pIface.removeGroup(groupName));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public int getGroupCapability(String peerAddress) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("getGroupCapability")) {
                return -1;
            }
            if (peerAddress == null) {
                Log.e(TAG, "Cannot parse peer mac address.");
                return -1;
            }
            try {
                byte[] macAddress = NativeUtil.macAddressToByteArray(peerAddress);
                SupplicantResult<Integer> capability = new SupplicantResult<>("getGroupCapability(" + peerAddress + ")");
                try {
                    this.mISupplicantP2pIface.getGroupCapability(macAddress, new ISupplicantP2pIface.getGroupCapabilityCallback() {
                        public final void onValues(SupplicantStatus supplicantStatus, int i) {
                            SupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, Integer.valueOf(i));
                        }
                    });
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                if (!capability.isSuccess()) {
                    return -1;
                }
                int intValue = capability.getResult().intValue();
                return intValue;
            } catch (IllegalArgumentException e2) {
                Log.e(TAG, "IllegalArgumentException Could not parse group address.", e2);
                return -1;
            } catch (Exception e3) {
                Log.e(TAG, "Could not parse group address.", e3);
                return -1;
            }
        }
    }

    public boolean configureExtListen(boolean enable, int periodInMillis, int intervalInMillis) {
        if (enable && intervalInMillis < periodInMillis) {
            return false;
        }
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("configureExtListen")) {
                return false;
            }
            if (!enable) {
                periodInMillis = 0;
                intervalInMillis = 0;
            }
            if (periodInMillis >= 0) {
                if (intervalInMillis >= 0) {
                    SupplicantResult<Void> result = new SupplicantResult<>("configureExtListen(" + periodInMillis + ", " + intervalInMillis + ")");
                    try {
                        result.setResult(this.mISupplicantP2pIface.configureExtListen(periodInMillis, intervalInMillis));
                    } catch (RemoteException e) {
                        Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                        supplicantServiceDiedHandler();
                    }
                    boolean isSuccess = result.isSuccess();
                    return isSuccess;
                }
            }
            Log.e(TAG, "Invalid parameters supplied to configureExtListen: " + periodInMillis + ", " + intervalInMillis);
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00ec, code lost:
        return false;
     */
    public boolean setListenChannel(int listenChannel, int operatingChannel) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("setListenChannel")) {
                return false;
            }
            if (listenChannel >= 1 && listenChannel <= 11) {
                SupplicantResult<Void> result = new SupplicantResult<>("setListenChannel(" + listenChannel + ", " + 81 + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.setListenChannel(listenChannel, 81));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                if (!result.isSuccess()) {
                    return false;
                }
            } else if (listenChannel != 0) {
                return false;
            }
            if (operatingChannel >= 0 && operatingChannel <= 165) {
                ArrayList<ISupplicantP2pIface.FreqRange> ranges = new ArrayList<>();
                if (operatingChannel >= 1 && operatingChannel <= 165) {
                    int freq = (operatingChannel <= 14 ? 2407 : ScoringParams.BAND5) + (operatingChannel * 5);
                    ISupplicantP2pIface.FreqRange range1 = new ISupplicantP2pIface.FreqRange();
                    range1.min = 1000;
                    range1.max = freq - 5;
                    ISupplicantP2pIface.FreqRange range2 = new ISupplicantP2pIface.FreqRange();
                    range2.min = freq + 5;
                    range2.max = 6000;
                    ranges.add(range1);
                    ranges.add(range2);
                }
                SupplicantResult<Void> result2 = new SupplicantResult<>("setDisallowedFrequencies(" + ranges + ")");
                try {
                    result2.setResult(this.mISupplicantP2pIface.setDisallowedFrequencies(ranges));
                } catch (RemoteException e2) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e2);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result2.isSuccess();
                return isSuccess;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:76:0x015a, code lost:
        return false;
     */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0150 A[Catch:{ RemoteException -> 0x0134 }] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x0159 A[SYNTHETIC] */
    public boolean serviceAdd(WifiP2pServiceInfo servInfo) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("serviceAdd")) {
                return false;
            }
            if (servInfo == null) {
                Log.e(TAG, "Null service info passed.");
                return false;
            }
            for (String s : servInfo.getSupplicantQueryList()) {
                if (s == null) {
                    Log.e(TAG, "Invalid service description (null).");
                    return false;
                }
                String[] data = s.split(" ");
                if (data.length < 3) {
                    Log.e(TAG, "Service specification invalid: " + s);
                    return false;
                }
                ArrayList<Byte> response = null;
                SupplicantResult<Void> result = null;
                try {
                    if ("upnp".equals(data[0])) {
                        try {
                            int version = Integer.parseInt(data[1], 16);
                            result = new SupplicantResult<>("addUpnpService(" + data[1] + ", " + data[2] + ")");
                            result.setResult(this.mISupplicantP2pIface.addUpnpService(version, data[2]));
                            if (result != null) {
                                if (!result.isSuccess()) {
                                }
                            }
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "UPnP Service specification invalid: " + s, e);
                            return false;
                        }
                    } else if (!"bonjour".equals(data[0])) {
                        return false;
                    } else {
                        if (!(data[1] == null || data[2] == null)) {
                            ArrayList<Byte> request = null;
                            try {
                                request = NativeUtil.byteArrayToArrayList(NativeUtil.hexStringToByteArray(data[1]));
                                response = NativeUtil.byteArrayToArrayList(NativeUtil.hexStringToByteArray(data[2]));
                            } catch (IllegalArgumentException e2) {
                                Log.e(TAG, "IllegalArgumentException Invalid argument.");
                            } catch (Exception e3) {
                                Log.e(TAG, "Invalid bonjour service description.");
                                return false;
                            }
                            result = new SupplicantResult<>("addBonjourService(" + data[1] + ", " + data[2] + ")");
                            result.setResult(this.mISupplicantP2pIface.addBonjourService(request, response));
                        }
                        if (result != null) {
                        }
                    }
                } catch (RemoteException e4) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e4);
                    supplicantServiceDiedHandler();
                }
            }
            return true;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0158, code lost:
        return false;
     */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x014e A[Catch:{ RemoteException -> 0x0132 }] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0157 A[SYNTHETIC] */
    public boolean serviceRemove(WifiP2pServiceInfo servInfo) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("serviceRemove")) {
                return false;
            }
            if (servInfo == null) {
                Log.e(TAG, "Null service info passed.");
                return false;
            }
            for (String s : servInfo.getSupplicantQueryList()) {
                if (s == null) {
                    Log.e(TAG, "Invalid service description (null).");
                    return false;
                }
                String[] data = s.split(" ");
                if (data.length < 3) {
                    Log.e(TAG, "Service specification invalid: " + s);
                    return false;
                }
                SupplicantResult<Void> result = null;
                try {
                    if ("upnp".equals(data[0])) {
                        try {
                            int version = Integer.parseInt(data[1], 16);
                            result = new SupplicantResult<>("removeUpnpService(" + data[1] + ", " + data[2] + ")");
                            result.setResult(this.mISupplicantP2pIface.removeUpnpService(version, data[2]));
                            if (result != null) {
                                if (!result.isSuccess()) {
                                }
                            }
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "UPnP Service specification invalid: " + s, e);
                            return false;
                        }
                    } else if ("bonjour".equals(data[0])) {
                        if (data[1] != null) {
                            try {
                                ArrayList<Byte> request = NativeUtil.byteArrayToArrayList(NativeUtil.hexStringToByteArray(data[1]));
                                result = new SupplicantResult<>("removeBonjourService(" + data[1] + ")");
                                result.setResult(this.mISupplicantP2pIface.removeBonjourService(request));
                            } catch (IllegalArgumentException e2) {
                                Log.e(TAG, "IllegalArgumentException occur when byteArrayToArrayList");
                                return false;
                            } catch (Exception e3) {
                                Log.e(TAG, "Invalid bonjour service description.");
                                return false;
                            }
                        }
                        if (result != null) {
                        }
                    } else {
                        Log.e(TAG, "Unknown / unsupported P2P service requested: " + data[0]);
                        return false;
                    }
                } catch (RemoteException e4) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e4);
                    supplicantServiceDiedHandler();
                }
            }
            return true;
        }
    }

    public String requestServiceDiscovery(String peerAddress, String query) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("requestServiceDiscovery")) {
                return null;
            }
            if (peerAddress == null) {
                Log.e(TAG, "Cannot parse peer mac address.");
                return null;
            }
            try {
                byte[] macAddress = NativeUtil.macAddressToByteArray(peerAddress);
                if (query == null) {
                    Log.e(TAG, "Cannot parse service discovery query: " + query);
                    return null;
                }
                try {
                    ArrayList<Byte> binQuery = NativeUtil.byteArrayToArrayList(NativeUtil.hexStringToByteArray(query));
                    SupplicantResult<Long> result = new SupplicantResult<>("requestServiceDiscovery(" + peerAddress + ", " + query + ")");
                    try {
                        this.mISupplicantP2pIface.requestServiceDiscovery(macAddress, binQuery, new ISupplicantP2pIface.requestServiceDiscoveryCallback() {
                            public final void onValues(SupplicantStatus supplicantStatus, long j) {
                                SupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, new Long(j));
                            }
                        });
                    } catch (RemoteException e) {
                        Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                        supplicantServiceDiedHandler();
                    }
                    Long value = result.getResult();
                    if (value == null) {
                        return null;
                    }
                    String l = value.toString();
                    return l;
                } catch (Exception e2) {
                    Log.e(TAG, "Could not parse service query.", e2);
                    return null;
                }
            } catch (IllegalArgumentException e3) {
                Log.e(TAG, "IllegalArgumentException Could not process peer MAC address.", e3);
                return null;
            } catch (Exception e4) {
                Log.e(TAG, "Could not process peer MAC address.", e4);
                return null;
            }
        }
    }

    public boolean cancelServiceDiscovery(String identifier) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("cancelServiceDiscovery")) {
                return false;
            }
            if (identifier == null) {
                Log.e(TAG, "cancelServiceDiscovery requires a valid tag.");
                return false;
            }
            try {
                long id = Long.parseLong(identifier);
                SupplicantResult<Void> result = new SupplicantResult<>("cancelServiceDiscovery(" + identifier + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.cancelServiceDiscovery(id));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            } catch (NumberFormatException e2) {
                Log.e(TAG, "Service discovery identifier invalid: " + identifier, e2);
                return false;
            }
        }
    }

    public boolean setMiracastMode(int mode) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("setMiracastMode")) {
                return false;
            }
            byte targetMode = 0;
            switch (mode) {
                case 1:
                    targetMode = 1;
                    break;
                case 2:
                    targetMode = 2;
                    break;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("setMiracastMode(" + mode + ")");
            try {
                result.setResult(this.mISupplicantP2pIface.setMiracastMode(targetMode));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean startWpsPbc(String groupIfName, String bssid) {
        if (TextUtils.isEmpty(groupIfName)) {
            Log.e(TAG, "Group name required when requesting WPS PBC. Got (" + groupIfName + ")");
            return false;
        }
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("startWpsPbc")) {
                return false;
            }
            byte[] bArr = {0, 0, 0, 0, 0, 0};
            try {
                byte[] macAddress = NativeUtil.macAddressToByteArray(bssid);
                SupplicantResult<Void> result = new SupplicantResult<>("startWpsPbc(" + groupIfName + ", " + bssid + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.startWpsPbc(groupIfName, macAddress));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            } catch (Exception e2) {
                Log.e(TAG, "Could not parse BSSID.", e2);
                return false;
            }
        }
    }

    public boolean startWpsPinKeypad(String groupIfName, String pin) {
        if (TextUtils.isEmpty(groupIfName) || TextUtils.isEmpty(pin)) {
            return false;
        }
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("startWpsPinKeypad")) {
                return false;
            }
            if (groupIfName == null) {
                Log.e(TAG, "Group name required when requesting WPS KEYPAD.");
                return false;
            } else if (pin == null) {
                Log.e(TAG, "PIN required when requesting WPS KEYPAD.");
                return false;
            } else {
                SupplicantResult<Void> result = new SupplicantResult<>("startWpsPinKeypad(" + groupIfName + ", " + pin + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.startWpsPinKeypad(groupIfName, pin));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
        }
    }

    public String startWpsPinDisplay(String groupIfName, String bssid) {
        if (TextUtils.isEmpty(groupIfName)) {
            return null;
        }
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("startWpsPinDisplay")) {
                return null;
            }
            if (groupIfName == null) {
                Log.e(TAG, "Group name required when requesting WPS KEYPAD.");
                return null;
            }
            byte[] bArr = {0, 0, 0, 0, 0, 0};
            try {
                byte[] macAddress = NativeUtil.macAddressToByteArray(bssid);
                SupplicantResult<String> result = new SupplicantResult<>("startWpsPinDisplay(" + groupIfName + ", " + bssid + ")");
                try {
                    this.mISupplicantP2pIface.startWpsPinDisplay(groupIfName, macAddress, new ISupplicantP2pIface.startWpsPinDisplayCallback() {
                        public final void onValues(SupplicantStatus supplicantStatus, String str) {
                            SupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, str);
                        }
                    });
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                String result2 = result.getResult();
                return result2;
            } catch (Exception e2) {
                Log.e(TAG, "Could not parse BSSID.", e2);
                return null;
            }
        }
    }

    public boolean cancelWps(String groupIfName) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("cancelWps")) {
                return false;
            }
            if (groupIfName == null) {
                Log.e(TAG, "Group name required when requesting WPS KEYPAD.");
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("cancelWps(" + groupIfName + ")");
            try {
                result.setResult(this.mISupplicantP2pIface.cancelWps(groupIfName));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean enableWfd(boolean enable) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("enableWfd")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("enableWfd(" + enable + ")");
            try {
                result.setResult(this.mISupplicantP2pIface.enableWfd(enable));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean setWfdDeviceInfo(String info) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("setWfdDeviceInfo")) {
                return false;
            }
            if (info == null) {
                Log.e(TAG, "Cannot parse null WFD info string.");
                return false;
            }
            try {
                byte[] wfdInfo = NativeUtil.hexStringToByteArray(info);
                SupplicantResult<Void> result = new SupplicantResult<>("setWfdDeviceInfo(" + info + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.setWfdDeviceInfo(wfdInfo));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            } catch (Exception e2) {
                Log.e(TAG, "Could not parse WFD Device Info string.");
                return false;
            }
        }
    }

    public boolean removeNetwork(int networkId) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("removeNetwork")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("removeNetwork(" + networkId + ")");
            try {
                result.setResult(this.mISupplicantP2pIface.removeNetwork(networkId));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    private List<Integer> listNetworks() {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("listNetworks")) {
                return null;
            }
            SupplicantResult<ArrayList> result = new SupplicantResult<>("listNetworks()");
            try {
                this.mISupplicantP2pIface.listNetworks(new ISupplicantIface.listNetworksCallback() {
                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        SupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, arrayList);
                    }
                });
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            List<Integer> result2 = result.getResult();
            return result2;
        }
    }

    private ISupplicantP2pNetwork getNetwork(int networkId) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("getNetwork")) {
                return null;
            }
            SupplicantResult<ISupplicantNetwork> result = new SupplicantResult<>("getNetwork(" + networkId + ")");
            try {
                this.mISupplicantP2pIface.getNetwork(networkId, new ISupplicantIface.getNetworkCallback() {
                    public final void onValues(SupplicantStatus supplicantStatus, ISupplicantNetwork iSupplicantNetwork) {
                        SupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, iSupplicantNetwork);
                    }
                });
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            if (result.getResult() == null) {
                Log.e(TAG, "getNetwork got null network");
                return null;
            }
            ISupplicantP2pNetwork p2pNetworkMockable = getP2pNetworkMockable(result.getResult());
            return p2pNetworkMockable;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:73:0x01db, code lost:
        return false;
     */
    public boolean loadGroups(WifiP2pGroupList groups) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("loadGroups")) {
                return false;
            }
            List<Integer> networkIds = listNetworks();
            if (networkIds != null) {
                if (!networkIds.isEmpty()) {
                    for (Integer networkId : networkIds) {
                        ISupplicantP2pNetwork network = getNetwork(networkId.intValue());
                        if (network == null) {
                            Log.e(TAG, "Failed to retrieve network object for " + networkId);
                        } else {
                            SupplicantResult<Boolean> resultIsCurrent = new SupplicantResult<>("isCurrent(" + networkId + ")");
                            try {
                                network.isCurrent(new ISupplicantP2pNetwork.isCurrentCallback() {
                                    public final void onValues(SupplicantStatus supplicantStatus, boolean z) {
                                        SupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, Boolean.valueOf(z));
                                    }
                                });
                            } catch (RemoteException e) {
                                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                                supplicantServiceDiedHandler();
                            }
                            if (resultIsCurrent.isSuccess()) {
                                if (!resultIsCurrent.getResult().booleanValue()) {
                                    WifiP2pGroup group = new WifiP2pGroup();
                                    group.setNetworkId(networkId.intValue());
                                    SupplicantResult<ArrayList> resultSsid = new SupplicantResult<>("getSsid(" + networkId + ")");
                                    try {
                                        network.getSsid(new ISupplicantP2pNetwork.getSsidCallback() {
                                            public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                                                SupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, arrayList);
                                            }
                                        });
                                    } catch (RemoteException e2) {
                                        Log.e(TAG, "ISupplicantP2pIface exception: " + e2);
                                        supplicantServiceDiedHandler();
                                    }
                                    if (resultSsid.isSuccess() && resultSsid.getResult() != null && !resultSsid.getResult().isEmpty()) {
                                        group.setNetworkName(NativeUtil.removeEnclosingQuotes(NativeUtil.encodeSsid(resultSsid.getResult())));
                                    }
                                    SupplicantResult<byte[]> resultBssid = new SupplicantResult<>("getBssid(" + networkId + ")");
                                    try {
                                        network.getBssid(new ISupplicantP2pNetwork.getBssidCallback() {
                                            public final void onValues(SupplicantStatus supplicantStatus, byte[] bArr) {
                                                SupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, bArr);
                                            }
                                        });
                                    } catch (RemoteException e3) {
                                        Log.e(TAG, "ISupplicantP2pIface exception: " + e3);
                                        supplicantServiceDiedHandler();
                                    }
                                    if (resultBssid.isSuccess() && !ArrayUtils.isEmpty(resultBssid.getResult())) {
                                        WifiP2pDevice device = new WifiP2pDevice();
                                        device.deviceAddress = NativeUtil.macAddressFromByteArray(resultBssid.getResult());
                                        group.setOwner(device);
                                    }
                                    SupplicantResult<Boolean> resultIsGo = new SupplicantResult<>("isGo(" + networkId + ")");
                                    try {
                                        network.isGo(new ISupplicantP2pNetwork.isGoCallback() {
                                            public final void onValues(SupplicantStatus supplicantStatus, boolean z) {
                                                SupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, Boolean.valueOf(z));
                                            }
                                        });
                                    } catch (RemoteException e4) {
                                        Log.e(TAG, "ISupplicantP2pIface exception: " + e4);
                                        supplicantServiceDiedHandler();
                                    }
                                    if (resultIsGo.isSuccess()) {
                                        group.setIsGroupOwner(resultIsGo.getResult().booleanValue());
                                    }
                                    groups.add(group);
                                }
                            }
                            Log.i(TAG, "Skipping current network");
                        }
                    }
                    return true;
                }
            }
        }
    }

    public boolean setWpsDeviceName(String name) {
        if (name == null) {
            return false;
        }
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("setWpsDeviceName")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("setWpsDeviceName(" + name + ")");
            try {
                result.setResult(this.mISupplicantP2pIface.setWpsDeviceName(name));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean setWpsDeviceType(String typeStr) {
        try {
            Matcher match = WPS_DEVICE_TYPE_PATTERN.matcher(typeStr);
            if (match.find()) {
                if (match.groupCount() == 3) {
                    short categ = Short.parseShort(match.group(1));
                    byte[] oui = NativeUtil.hexStringToByteArray(match.group(2));
                    short subCateg = Short.parseShort(match.group(3));
                    byte[] bytes = new byte[8];
                    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
                    byteBuffer.putShort(categ);
                    byteBuffer.put(oui);
                    byteBuffer.putShort(subCateg);
                    synchronized (this.mLock) {
                        if (!checkSupplicantP2pIfaceAndLogFailure("setWpsDeviceType")) {
                            return false;
                        }
                        SupplicantResult<Void> result = new SupplicantResult<>("setWpsDeviceType(" + typeStr + ")");
                        try {
                            result.setResult(this.mISupplicantP2pIface.setWpsDeviceType(bytes));
                        } catch (RemoteException e) {
                            Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                            supplicantServiceDiedHandler();
                        }
                        boolean isSuccess = result.isSuccess();
                        return isSuccess;
                    }
                }
            }
            Log.e(TAG, "Malformed WPS device type " + typeStr);
            return false;
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "Illegal argument " + typeStr, e2);
            return false;
        }
    }

    public boolean setWpsConfigMethods(String configMethodsStr) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("setWpsConfigMethods")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("setWpsConfigMethods(" + configMethodsStr + ")");
            short configMethodsMask = 0;
            String[] configMethodsStrArr = configMethodsStr.split("\\s+");
            for (String stringToWpsConfigMethod : configMethodsStrArr) {
                configMethodsMask = (short) (stringToWpsConfigMethod(stringToWpsConfigMethod) | configMethodsMask);
            }
            try {
                result.setResult(this.mISupplicantP2pIface.setWpsConfigMethods(configMethodsMask));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public String getNfcHandoverRequest() {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("getNfcHandoverRequest")) {
                return null;
            }
            SupplicantResult<ArrayList> result = new SupplicantResult<>("getNfcHandoverRequest()");
            try {
                this.mISupplicantP2pIface.createNfcHandoverRequestMessage(new ISupplicantP2pIface.createNfcHandoverRequestMessageCallback() {
                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        SupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, arrayList);
                    }
                });
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            if (!result.isSuccess()) {
                return null;
            }
            String hexStringFromByteArray = NativeUtil.hexStringFromByteArray(NativeUtil.byteArrayFromArrayList(result.getResult()));
            return hexStringFromByteArray;
        }
    }

    public String getNfcHandoverSelect() {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("getNfcHandoverSelect")) {
                return null;
            }
            SupplicantResult<ArrayList> result = new SupplicantResult<>("getNfcHandoverSelect()");
            try {
                this.mISupplicantP2pIface.createNfcHandoverSelectMessage(new ISupplicantP2pIface.createNfcHandoverSelectMessageCallback() {
                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        SupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, arrayList);
                    }
                });
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            if (!result.isSuccess()) {
                return null;
            }
            String hexStringFromByteArray = NativeUtil.hexStringFromByteArray(NativeUtil.byteArrayFromArrayList(result.getResult()));
            return hexStringFromByteArray;
        }
    }

    public boolean initiatorReportNfcHandover(String selectMessage) {
        if (selectMessage == null) {
            return false;
        }
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("initiatorReportNfcHandover")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("initiatorReportNfcHandover(" + selectMessage + ")");
            try {
                result.setResult(this.mISupplicantP2pIface.reportNfcHandoverInitiation(NativeUtil.byteArrayToArrayList(NativeUtil.hexStringToByteArray(selectMessage))));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            } catch (IllegalArgumentException e2) {
                Log.e(TAG, "Illegal argument " + selectMessage, e2);
                return false;
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean responderReportNfcHandover(String requestMessage) {
        if (requestMessage == null) {
            return false;
        }
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("responderReportNfcHandover")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("responderReportNfcHandover(" + requestMessage + ")");
            try {
                result.setResult(this.mISupplicantP2pIface.reportNfcHandoverResponse(NativeUtil.byteArrayToArrayList(NativeUtil.hexStringToByteArray(requestMessage))));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            } catch (IllegalArgumentException e2) {
                Log.e(TAG, "Illegal argument " + requestMessage, e2);
                return false;
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean setClientList(int networkId, String clientListStr) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("setClientList")) {
                return false;
            }
            if (TextUtils.isEmpty(clientListStr)) {
                Log.e(TAG, "Invalid client list");
                return false;
            }
            ISupplicantP2pNetwork network = getNetwork(networkId);
            if (network == null) {
                Log.e(TAG, "Invalid network id ");
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("setClientList(" + networkId + ", " + clientListStr + ")");
            try {
                ArrayList<byte[]> clients = new ArrayList<>();
                for (String clientStr : Arrays.asList(clientListStr.split("\\s+"))) {
                    clients.add(NativeUtil.macAddressToByteArray(clientStr));
                }
                result.setResult(network.setClientList(clients));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            } catch (IllegalArgumentException e2) {
                Log.e(TAG, "Illegal argument " + clientListStr, e2);
                return false;
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public String getClientList(int networkId) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("getClientList")) {
                return null;
            }
            ISupplicantP2pNetwork network = getNetwork(networkId);
            if (network == null) {
                Log.e(TAG, "Invalid network id ");
                return null;
            }
            SupplicantResult<ArrayList> result = new SupplicantResult<>("getClientList(" + networkId + ")");
            try {
                network.getClientList(new ISupplicantP2pNetwork.getClientListCallback() {
                    public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
                        SupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, arrayList);
                    }
                });
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            if (!result.isSuccess()) {
                return null;
            }
            String str = (String) result.getResult().stream().map($$Lambda$22Qhg7RQJlXihi83tqGgsfFMs.INSTANCE).collect(Collectors.joining(" "));
            return str;
        }
    }

    public boolean saveConfig() {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("saveConfig")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("saveConfig()");
            try {
                result.setResult(this.mISupplicantP2pIface.saveConfig());
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private static short stringToWpsConfigMethod(String configMethod) {
        char c;
        switch (configMethod.hashCode()) {
            case -1781962557:
                if (configMethod.equals("virtual_push_button")) {
                    c = 9;
                    break;
                }
            case -1419358249:
                if (configMethod.equals("ethernet")) {
                    c = 1;
                    break;
                }
            case -1134657068:
                if (configMethod.equals("keypad")) {
                    c = 8;
                    break;
                }
            case -614489202:
                if (configMethod.equals("virtual_display")) {
                    c = 12;
                    break;
                }
            case -522593958:
                if (configMethod.equals("physical_display")) {
                    c = 13;
                    break;
                }
            case -423872603:
                if (configMethod.equals("nfc_interface")) {
                    c = 6;
                    break;
                }
            case -416734217:
                if (configMethod.equals("push_button")) {
                    c = 7;
                    break;
                }
            case 3388229:
                if (configMethod.equals("p2ps")) {
                    c = 11;
                    break;
                }
            case 3599197:
                if (configMethod.equals("usba")) {
                    c = 0;
                    break;
                }
            case 102727412:
                if (configMethod.equals("label")) {
                    c = 2;
                    break;
                }
            case 179612103:
                if (configMethod.equals("ext_nfc_token")) {
                    c = 5;
                    break;
                }
            case 1146869903:
                if (configMethod.equals("physical_push_button")) {
                    c = 10;
                    break;
                }
            case 1671764162:
                if (configMethod.equals("display")) {
                    c = 3;
                    break;
                }
            case 2010140181:
                if (configMethod.equals("int_nfc_token")) {
                    c = 4;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 4;
            case 3:
                return 8;
            case 4:
                return 32;
            case 5:
                return 16;
            case 6:
                return 64;
            case 7:
                return WpsConfigMethods.PUSHBUTTON;
            case 8:
                return WpsConfigMethods.KEYPAD;
            case 9:
                return WpsConfigMethods.VIRT_PUSHBUTTON;
            case 10:
                return WpsConfigMethods.PHY_PUSHBUTTON;
            case 11:
                return WpsConfigMethods.P2PS;
            case 12:
                return WpsConfigMethods.VIRT_DISPLAY;
            case 13:
                return WpsConfigMethods.PHY_DISPLAY;
            default:
                throw new IllegalArgumentException("Invalid WPS config method: " + configMethod);
        }
    }

    private boolean trySetupForVendor(ISupplicantIface ifaceHwBinder, String ifaceName) {
        ISupplicantP2pIface supplicantP2pIface = vendor.huawei.hardware.wifi.supplicant.V2_1.ISupplicantP2pIface.castFrom(ifaceHwBinder);
        if (supplicantP2pIface != null) {
            this.mHidlVersion = HIDL_V2_1;
            Log.i(TAG, "Start to setup vendor ISupplicantP2pIface V2.1");
        } else {
            supplicantP2pIface = vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantP2pIface.castFrom(ifaceHwBinder);
            if (supplicantP2pIface == null) {
                return false;
            }
            this.mHidlVersion = 200;
            Log.i(TAG, "Start to setup vendor ISupplicantP2pIface V2.0");
        }
        this.mISupplicantP2pIface = supplicantP2pIface;
        if (!linkToSupplicantP2pIfaceDeath()) {
            return false;
        }
        if (this.mISupplicantP2pIface == null || this.mMonitor == null || hwP2pRegisterCallback(new VendorSupplicantP2pIfaceHalCallbackV2_1(ifaceName, this.mMonitor, new SupplicantP2pIfaceCallback(ifaceName, this.mMonitor)))) {
            Log.i(TAG, "Successfully setup vendor ISupplicantP2pIface");
            return true;
        }
        Log.e(TAG, "Vendor callback registration failed. Initialization incomplete.");
        return false;
    }

    private vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantP2pIface checkVendorSupplicantP2pIfaceAndLogFailure(String method) {
        if (this.mISupplicantP2pIface == null) {
            Log.e(TAG, "Can't call " + method + ": ISupplicantP2pIface is null");
            return null;
        }
        vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantP2pIface vendorP2pIface = null;
        if (this.mHidlVersion == 200) {
            vendorP2pIface = vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantP2pIface.castFrom(this.mISupplicantP2pIface);
        } else if (this.mHidlVersion == HIDL_V2_1) {
            vendorP2pIface = vendor.huawei.hardware.wifi.supplicant.V2_1.ISupplicantP2pIface.castFrom(this.mISupplicantP2pIface);
        }
        if (vendorP2pIface != null) {
            return vendorP2pIface;
        }
        Log.e(TAG, "Can't call " + method + ": fail to cast ISupplicantP2pIface to vendor " + this.mHidlVersion);
        return null;
    }

    private boolean hwP2pRegisterCallback(ISupplicantP2pIfaceCallback receiver) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantP2pIface vendorP2pIface = checkVendorSupplicantP2pIfaceAndLogFailure("hwP2pRegisterCallback");
            if (vendorP2pIface == null) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("hwP2pRegisterCallback()");
            try {
                if (this.mHidlVersion == 200) {
                    result.setResult(vendorP2pIface.hwP2pRegisterCallback(receiver));
                } else {
                    result.setResult(((vendor.huawei.hardware.wifi.supplicant.V2_1.ISupplicantP2pIface) vendorP2pIface).hwP2pRegisterCallback_2_1(receiver));
                }
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean groupAddWithFreq(int networkId, boolean isPersistent, String freq) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantP2pIface vendorP2pIface = checkVendorSupplicantP2pIfaceAndLogFailure("hwP2pRegisterCallback");
            if (vendorP2pIface == null) {
                return false;
            }
            if (freq == null) {
                Log.e(TAG, "freq try to create is null");
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("groupAddWithFreq(" + networkId + ", " + isPersistent + ", " + freq + ")");
            try {
                result.setResult(vendorP2pIface.addGroupWithFreq(isPersistent, networkId, freq));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean magiclinkConnect(String cmd) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantP2pIface vendorP2pIface = checkVendorSupplicantP2pIfaceAndLogFailure("hwP2pRegisterCallback");
            if (vendorP2pIface == null) {
                return false;
            }
            if (cmd == null) {
                Log.e(TAG, "cmd try to connect is null");
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("magiclinkConnect([secrecy parameters])");
            try {
                result.setResult(vendorP2pIface.magiclinkConnect(cmd));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public boolean addP2pRptGroup(String config) {
        synchronized (this.mLock) {
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantP2pIface vendorP2pIface = checkVendorSupplicantP2pIfaceAndLogFailure("hwP2pRegisterCallback");
            if (vendorP2pIface == null) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("rptP2pAddGroup()");
            try {
                result.setResult(vendorP2pIface.rptP2pAddGroup(config));
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public int getP2pLinkspeed(String ifaceName) {
        synchronized (this.mLock) {
            if (TextUtils.isEmpty(ifaceName)) {
                return -1;
            }
            vendor.huawei.hardware.wifi.supplicant.V2_0.ISupplicantP2pIface vendorP2pIface = checkVendorSupplicantP2pIfaceAndLogFailure("hwP2pRegisterCallback");
            if (vendorP2pIface == null) {
                return -1;
            }
            SupplicantResult<Integer> result = new SupplicantResult<>("getP2pLinkspeed()");
            try {
                vendorP2pIface.getP2pLinkspeed(ifaceName, new ISupplicantP2pIface.getP2pLinkspeedCallback() {
                    public final void onValues(SupplicantStatus supplicantStatus, int i) {
                        SupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, Integer.valueOf(i));
                    }
                });
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            if (!result.isSuccess()) {
                return -1;
            }
            int intValue = result.getResult().intValue();
            return intValue;
        }
    }
}
