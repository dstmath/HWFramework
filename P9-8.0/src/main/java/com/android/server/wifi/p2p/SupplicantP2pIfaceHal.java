package com.android.server.wifi.p2p;

import android.hardware.wifi.supplicant.V1_0.ISupplicant;
import android.hardware.wifi.supplicant.V1_0.ISupplicant.IfaceInfo;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.FreqRange;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pNetwork;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.hardware.wifi.supplicant.V1_0.WpsConfigMethods;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.hidl.manager.V1_0.IServiceNotification.Stub;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.os.IHwBinder.DeathRecipient;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass10;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass11;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass12;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass13;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass14;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass15;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass16;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass17;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass18;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass19;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass2;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass20;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass3;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass4;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass5;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass6;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass7;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass8;
import com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass9;
import com.android.server.wifi.util.NativeUtil;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import vendor.huawei.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface;
import vendor.huawei.hardware.wifi.supplicant.V1_0.ISupplicantP2pIfaceCallback;

public class SupplicantP2pIfaceHal {
    private static final boolean DBG = true;
    private static final int DEFAULT_GROUP_OWNER_INTENT = 6;
    private static final int DEFAULT_OPERATING_CLASS = 81;
    private static final int RESULT_NOT_VALID = -1;
    private static final String TAG = "SupplicantP2pIfaceHal";
    private static final Pattern WPS_DEVICE_TYPE_PATTERN = Pattern.compile("^(\\d{1,2})-([0-9a-fA-F]{8})-(\\d{1,2})$");
    private SupplicantP2pIfaceCallback mCallback = null;
    private ISupplicantIface mHidlSupplicantIface = null;
    private IServiceManager mIServiceManager = null;
    private ISupplicant mISupplicant = null;
    private ISupplicantP2pIface mISupplicantP2pIface = null;
    private Object mLock = new Object();
    private final WifiP2pMonitor mMonitor;
    private final DeathRecipient mServiceManagerDeathRecipient = new AnonymousClass19(this);
    private final IServiceNotification mServiceNotificationCallback = new Stub() {
        public void onRegistration(String fqName, String name, boolean preexisting) {
            synchronized (SupplicantP2pIfaceHal.this.mLock) {
                Log.i(SupplicantP2pIfaceHal.TAG, "IServiceNotification.onRegistration for: " + fqName + ", " + name + " preexisting=" + preexisting);
                if (SupplicantP2pIfaceHal.this.initSupplicantService() && (SupplicantP2pIfaceHal.this.initSupplicantP2pIface() ^ 1) == 0) {
                    Log.i(SupplicantP2pIfaceHal.TAG, "Completed initialization of ISupplicant interfaces.");
                } else {
                    Log.e(SupplicantP2pIfaceHal.TAG, "initalizing ISupplicantIfaces failed.");
                    SupplicantP2pIfaceHal.this.supplicantServiceDiedHandler();
                }
            }
        }
    };
    private final DeathRecipient mSupplicantDeathRecipient = new AnonymousClass20(this);

    private static class SupplicantResult<E> {
        private String mMethodName;
        private SupplicantStatus mStatus = null;
        private E mValue = null;

        SupplicantResult(String methodName) {
            this.mMethodName = methodName;
            SupplicantP2pIfaceHal.logd("entering " + this.mMethodName);
        }

        /* renamed from: setResult */
        public void lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(SupplicantStatus status, E value) {
            SupplicantP2pIfaceHal.logCompletion(this.mMethodName, status);
            SupplicantP2pIfaceHal.logd("leaving " + this.mMethodName + " with result = " + value);
            this.mStatus = status;
            this.mValue = value;
        }

        public void setResult(SupplicantStatus status) {
            SupplicantP2pIfaceHal.logCompletion(this.mMethodName, status);
            SupplicantP2pIfaceHal.logd("leaving " + this.mMethodName);
            this.mStatus = status;
        }

        public boolean isSuccess() {
            return this.mStatus != null && this.mStatus.code == 0;
        }

        public E getResult() {
            return isSuccess() ? this.mValue : null;
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_4088(long cookie) {
        Log.w(TAG, "IServiceManager died: cookie=" + cookie);
        synchronized (this.mLock) {
            supplicantServiceDiedHandler();
            this.mIServiceManager = null;
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_4478(long cookie) {
        Log.w(TAG, "ISupplicant/ISupplicantStaIface died: cookie=" + cookie);
        synchronized (this.mLock) {
            supplicantServiceDiedHandler();
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
                } else if (this.mIServiceManager.registerForNotifications(ISupplicant.kInterfaceName, "", this.mServiceNotificationCallback)) {
                    return true;
                } else {
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

    private boolean initSupplicantService() {
        synchronized (this.mLock) {
            try {
                this.mISupplicant = getSupplicantMockable();
                if (this.mISupplicant == null) {
                    Log.e(TAG, "Got null ISupplicant service. Stopping supplicant HIDL startup");
                    return false;
                } else if (linkToSupplicantDeath()) {
                    return true;
                } else {
                    return false;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "ISupplicant.getService exception: " + e);
                return false;
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

    /* JADX WARNING: Missing block: B:43:0x00ad, code:
            if (r10.mISupplicantP2pIface == null) goto L_0x00d4;
     */
    /* JADX WARNING: Missing block: B:45:0x00b1, code:
            if (r10.mMonitor == null) goto L_0x00d4;
     */
    /* JADX WARNING: Missing block: B:46:0x00b3, code:
            r10.mCallback = new com.android.server.wifi.p2p.SupplicantP2pIfaceCallback("p2p0", r10.mMonitor);
     */
    /* JADX WARNING: Missing block: B:47:0x00c5, code:
            if (hwP2pRegisterCallback(r10.mCallback) != false) goto L_0x00d4;
     */
    /* JADX WARNING: Missing block: B:48:0x00c7, code:
            android.util.Log.e(TAG, "Callback registration failed. Initialization incomplete.");
     */
    /* JADX WARNING: Missing block: B:49:0x00d0, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:53:0x00d4, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean initSupplicantP2pIface() {
        synchronized (this.mLock) {
            ArrayList<IfaceInfo> supplicantIfaces = new ArrayList();
            try {
                this.mISupplicant.listInterfaces(new AnonymousClass2(supplicantIfaces));
                if (supplicantIfaces.size() == 0) {
                    Log.e(TAG, "Got zero HIDL supplicant ifaces. Stopping supplicant HIDL startup.");
                    return false;
                }
                SupplicantResult<ISupplicantIface> supplicantIface = new SupplicantResult("getInterface()");
                for (IfaceInfo ifaceInfo : supplicantIfaces) {
                    if (ifaceInfo.type == 1) {
                        try {
                            this.mISupplicant.getInterface(ifaceInfo, new com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo.AnonymousClass1(supplicantIface));
                            break;
                        } catch (RemoteException e) {
                            Log.e(TAG, "ISupplicant.getInterface exception: " + e);
                            return false;
                        }
                    }
                }
                if (supplicantIface.getResult() == null) {
                    Log.e(TAG, "initSupplicantP2pIface got null iface");
                    return false;
                }
                this.mISupplicantP2pIface = getP2pIfaceMockable((ISupplicantIface) supplicantIface.getResult());
                if (!linkToSupplicantP2pIfaceDeath()) {
                    return false;
                }
            } catch (RemoteException e2) {
                Log.e(TAG, "ISupplicant.listInterfaces exception: " + e2);
                return false;
            }
        }
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_9591(ArrayList supplicantIfaces, SupplicantStatus status, ArrayList ifaces) {
        if (status.code != 0) {
            Log.e(TAG, "Getting Supplicant Interfaces failed: " + status.code);
        } else {
            supplicantIfaces.addAll(ifaces);
        }
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_10697(SupplicantResult supplicantIface, SupplicantStatus status, ISupplicantIface iface) {
        if (status.code != 0) {
            Log.e(TAG, "Failed to get ISupplicantIface " + status.code);
        } else {
            supplicantIface.lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(status, iface);
        }
    }

    private void supplicantServiceDiedHandler() {
        synchronized (this.mLock) {
            this.mISupplicant = null;
            this.mISupplicantP2pIface = null;
        }
    }

    public boolean isInitializationStarted() {
        return this.mIServiceManager != null;
    }

    public boolean isInitializationComplete() {
        return this.mISupplicantP2pIface != null;
    }

    protected IServiceManager getServiceManagerMockable() throws RemoteException {
        return IServiceManager.getService();
    }

    protected ISupplicant getSupplicantMockable() throws RemoteException {
        return ISupplicant.getService();
    }

    protected ISupplicantP2pIface getP2pIfaceMockable(ISupplicantIface iface) {
        return ISupplicantP2pIface.asInterface(iface.asBinder());
    }

    protected ISupplicantP2pNetwork getP2pNetworkMockable(ISupplicantNetwork network) {
        return ISupplicantP2pNetwork.asInterface(network.asBinder());
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
            if (checkSupplicantP2pIfaceAndLogFailure("getName")) {
                SupplicantResult<String> result = new SupplicantResult("getName()");
                try {
                    this.mISupplicantP2pIface.getName(new AnonymousClass3(result));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                String str = (String) result.getResult();
                return str;
            }
            return null;
        }
    }

    public boolean hwP2pRegisterCallback(ISupplicantP2pIfaceCallback receiver) {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("hwP2pRegisterCallback")) {
                SupplicantResult<Void> result = new SupplicantResult("hwP2pRegisterCallback()");
                try {
                    result.setResult(this.mISupplicantP2pIface.hwP2pRegisterCallback(receiver));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
            return false;
        }
    }

    public boolean find(int timeout) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("find")) {
                return false;
            } else if (timeout < 0) {
                Log.e(TAG, "Invalid timeout value: " + timeout);
                return false;
            } else {
                SupplicantResult<Void> result = new SupplicantResult("find(" + timeout + ")");
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
    }

    public boolean stopFind() {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("stopFind")) {
                SupplicantResult<Void> result = new SupplicantResult("stopFind()");
                try {
                    result.setResult(this.mISupplicantP2pIface.stopFind());
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
            return false;
        }
    }

    public boolean flush() {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("flush")) {
                SupplicantResult<Void> result = new SupplicantResult("flush()");
                try {
                    result.setResult(this.mISupplicantP2pIface.flush());
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
            return false;
        }
    }

    public boolean serviceFlush() {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("serviceFlush")) {
                SupplicantResult<Void> result = new SupplicantResult("serviceFlush()");
                try {
                    result.setResult(this.mISupplicantP2pIface.flushServices());
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
            return false;
        }
    }

    public boolean setPowerSave(String groupIfName, boolean enable) {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("setPowerSave")) {
                SupplicantResult<Void> result = new SupplicantResult("setPowerSave(" + groupIfName + ", " + enable + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.setPowerSave(groupIfName, enable));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
            return false;
        }
    }

    public boolean setGroupIdle(String groupIfName, int timeoutInSec) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("setGroupIdle")) {
                return false;
            } else if (timeoutInSec < 0) {
                Log.e(TAG, "Invalid group timeout value " + timeoutInSec);
                return false;
            } else {
                SupplicantResult<Void> result = new SupplicantResult("setGroupIdle(" + groupIfName + ", " + timeoutInSec + ")");
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
    }

    public boolean setSsidPostfix(String postfix) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("setSsidPostfix")) {
                return false;
            } else if (postfix == null) {
                Log.e(TAG, "Invalid SSID postfix value (null).");
                return false;
            } else {
                SupplicantResult<Void> result = new SupplicantResult("setSsidPostfix(" + postfix + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.setSsidPostfix(NativeUtil.decodeSsid("\"" + postfix + "\"")));
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
    }

    public String connect(WifiP2pConfig config, boolean joinExistingGroup) {
        if (config == null) {
            return null;
        }
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("setSsidPostfix")) {
                return null;
            } else if (config.deviceAddress == null) {
                Log.e(TAG, "Could not parse null mac address.");
                return null;
            } else if (config.wps.setup != 0 || (TextUtils.isEmpty(config.wps.pin) ^ 1) == 0) {
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
                    SupplicantResult<String> result = new SupplicantResult("connect(" + config.deviceAddress + ")");
                    try {
                        this.mISupplicantP2pIface.connect(peerAddress, provisionMethod, preSelectedPin, joinExistingGroup, persistent, goIntent, new AnonymousClass6(result));
                    } catch (RemoteException e) {
                        Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                        supplicantServiceDiedHandler();
                    }
                    String str = (String) result.getResult();
                    return str;
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
            if (checkSupplicantP2pIfaceAndLogFailure("cancelConnect")) {
                SupplicantResult<Void> result = new SupplicantResult("cancelConnect()");
                try {
                    result.setResult(this.mISupplicantP2pIface.cancelConnect());
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
            return false;
        }
    }

    public boolean provisionDiscovery(WifiP2pConfig config) {
        if (config == null) {
            return false;
        }
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("provisionDiscovery")) {
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
                    SupplicantResult<Void> result = new SupplicantResult("provisionDiscovery(" + config.deviceAddress + ", " + config.wps.setup + ")");
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
            return false;
        }
    }

    public boolean invite(WifiP2pGroup group, String peerAddress) {
        if (TextUtils.isEmpty(peerAddress)) {
            return false;
        }
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("invite")) {
                return false;
            } else if (group == null) {
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
                        SupplicantResult<Void> result = new SupplicantResult("invite(" + group.getInterface() + ", " + group.getOwner().deviceAddress + ", " + peerAddress + ")");
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
                } catch (Exception e22) {
                    Log.e(TAG, "Group owner mac address parse error.", e22);
                    return false;
                }
            }
        }
    }

    public boolean reject(String peerAddress) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("reject")) {
                return false;
            } else if (peerAddress == null) {
                Log.e(TAG, "Cannot parse rejected peer's mac address.");
                return false;
            } else {
                try {
                    byte[] macAddress = NativeUtil.macAddressToByteArray(peerAddress);
                    SupplicantResult<Void> result = new SupplicantResult("reject(" + peerAddress + ")");
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
    }

    public String getDeviceAddress() {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("getDeviceAddress")) {
                SupplicantResult<String> result = new SupplicantResult("getDeviceAddress()");
                try {
                    this.mISupplicantP2pIface.getDeviceAddress(new AnonymousClass9(result));
                    String str = (String) result.getResult();
                    return str;
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                    return null;
                }
            }
            return null;
        }
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_35213(SupplicantResult result, SupplicantStatus status, byte[] address) {
        String parsedAddress = null;
        try {
            parsedAddress = NativeUtil.macAddressFromByteArray(address);
        } catch (Exception e) {
            Log.e(TAG, "Could not process reported address.", e);
        }
        result.lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(status, parsedAddress);
    }

    public String getSsid(String address) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("getSsid")) {
                return null;
            } else if (address == null) {
                Log.e(TAG, "Cannot parse peer mac address.");
                return null;
            } else {
                try {
                    byte[] macAddress = NativeUtil.macAddressToByteArray(address);
                    SupplicantResult<String> result = new SupplicantResult("getSsid(" + address + ")");
                    try {
                        this.mISupplicantP2pIface.getSsid(macAddress, new AnonymousClass11(result));
                        String str = (String) result.getResult();
                        return str;
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
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_36845(SupplicantResult result, SupplicantStatus status, ArrayList ssid) {
        String ssidString = null;
        if (ssid != null) {
            try {
                ssidString = NativeUtil.encodeSsid(ssid);
            } catch (Exception e) {
                Log.e(TAG, "Could not encode SSID.", e);
            }
        }
        result.lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_87800(status, ssidString);
    }

    public boolean reinvoke(int networkId, String peerAddress) {
        if (TextUtils.isEmpty(peerAddress) || networkId < 0) {
            return false;
        }
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("reinvoke")) {
                return false;
            } else if (peerAddress == null) {
                Log.e(TAG, "Cannot parse peer mac address.");
                return false;
            } else {
                try {
                    byte[] macAddress = NativeUtil.macAddressToByteArray(peerAddress);
                    SupplicantResult<Void> result = new SupplicantResult("reinvoke(" + networkId + ", " + peerAddress + ")");
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
    }

    public boolean groupAdd(int networkId, boolean isPersistent) {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("groupAdd")) {
                SupplicantResult<Void> result = new SupplicantResult("groupAdd(" + networkId + ", " + isPersistent + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.addGroup(isPersistent, networkId));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
            return false;
        }
    }

    public boolean groupAdd(boolean isPersistent) {
        return groupAdd(-1, isPersistent);
    }

    public boolean groupAddWithFreq(int networkId, boolean isPersistent, String freq) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("groupAddWithFreq")) {
                return false;
            } else if (freq == null) {
                Log.e(TAG, "freq try to create is null");
                return false;
            } else {
                SupplicantResult<Void> result = new SupplicantResult("groupAddWithFreq(" + networkId + ", " + isPersistent + ", " + freq + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.addGroupWithFreq(isPersistent, networkId, freq));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
        }
    }

    public boolean magiclinkConnect(String cmd) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("magiclinkConnect")) {
                return false;
            } else if (cmd == null) {
                Log.e(TAG, "cmd try to connect is null");
                return false;
            } else {
                SupplicantResult<Void> result = new SupplicantResult("magiclinkConnect([secrecy parameters])");
                try {
                    result.setResult(this.mISupplicantP2pIface.magiclinkConnect(cmd));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
        }
    }

    public boolean groupRemove(String groupName) {
        if (TextUtils.isEmpty(groupName)) {
            return false;
        }
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("groupRemove")) {
                SupplicantResult<Void> result = new SupplicantResult("groupRemove(" + groupName + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.removeGroup(groupName));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
            return false;
        }
    }

    public int getGroupCapability(String peerAddress) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("getGroupCapability")) {
                return -1;
            } else if (peerAddress == null) {
                Log.e(TAG, "Cannot parse peer mac address.");
                return -1;
            } else {
                try {
                    byte[] macAddress = NativeUtil.macAddressToByteArray(peerAddress);
                    SupplicantResult<Integer> capability = new SupplicantResult("getGroupCapability(" + peerAddress + ")");
                    try {
                        this.mISupplicantP2pIface.getGroupCapability(macAddress, new AnonymousClass10(capability));
                    } catch (RemoteException e) {
                        Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                        supplicantServiceDiedHandler();
                    }
                    if (capability.isSuccess()) {
                        int intValue = ((Integer) capability.getResult()).intValue();
                        return intValue;
                    }
                    return -1;
                } catch (IllegalArgumentException e2) {
                    Log.e(TAG, "IllegalArgumentException Could not parse group address.", e2);
                    return -1;
                } catch (Exception e3) {
                    Log.e(TAG, "Could not parse group address.", e3);
                    return -1;
                }
            }
        }
    }

    public boolean configureExtListen(boolean enable, int periodInMillis, int intervalInMillis) {
        if (enable && intervalInMillis < periodInMillis) {
            return false;
        }
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("configureExtListen")) {
                if (!enable) {
                    periodInMillis = 0;
                    intervalInMillis = 0;
                }
                if (periodInMillis < 0 || intervalInMillis < 0) {
                    Log.e(TAG, "Invalid parameters supplied to configureExtListen: " + periodInMillis + ", " + intervalInMillis);
                    return false;
                }
                SupplicantResult<Void> result = new SupplicantResult("configureExtListen(" + periodInMillis + ", " + intervalInMillis + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.configureExtListen(periodInMillis, intervalInMillis));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:50:0x0107, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setListenChannel(int listenChannel, int operatingChannel) {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("setListenChannel")) {
                SupplicantResult<Void> result;
                if (listenChannel >= 1 && listenChannel <= 11) {
                    result = new SupplicantResult("setListenChannel(" + listenChannel + ", " + 81 + ")");
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
                if (operatingChannel < 0 || operatingChannel > 165) {
                } else {
                    ArrayList<FreqRange> ranges = new ArrayList();
                    if (operatingChannel >= 1 && operatingChannel <= 165) {
                        int freq = (operatingChannel <= 14 ? 2407 : 5000) + (operatingChannel * 5);
                        FreqRange range1 = new FreqRange();
                        range1.min = 1000;
                        range1.max = freq - 5;
                        FreqRange range2 = new FreqRange();
                        range2.min = freq + 5;
                        range2.max = 6000;
                        ranges.add(range1);
                        ranges.add(range2);
                    }
                    result = new SupplicantResult("setDisallowedFrequencies(" + ranges + ")");
                    try {
                        result.setResult(this.mISupplicantP2pIface.setDisallowedFrequencies(ranges));
                    } catch (RemoteException e2) {
                        Log.e(TAG, "ISupplicantP2pIface exception: " + e2);
                        supplicantServiceDiedHandler();
                    }
                    boolean isSuccess = result.isSuccess();
                    return isSuccess;
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:101:0x00cc A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00c4 A:{SYNTHETIC, Splitter: B:44:0x00c4} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00c4 A:{SYNTHETIC, Splitter: B:44:0x00c4} */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x00cc A:{SYNTHETIC} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean serviceAdd(WifiP2pServiceInfo servInfo) {
        RemoteException e;
        SupplicantResult<Void> result;
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("serviceAdd")) {
                return false;
            } else if (servInfo == null) {
                Log.e(TAG, "Null service info passed.");
                return false;
            } else {
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
                    SupplicantResult result2 = null;
                    try {
                        SupplicantResult<Void> result3;
                        if ("upnp".equals(data[0])) {
                            try {
                                int version = Integer.parseInt(data[1], 16);
                                result3 = new SupplicantResult("addUpnpService(" + data[1] + ", " + data[2] + ")");
                                try {
                                    result3.setResult(this.mISupplicantP2pIface.addUpnpService(version, data[2]));
                                    result2 = result3;
                                } catch (RemoteException e2) {
                                    e = e2;
                                    result2 = result3;
                                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                                    supplicantServiceDiedHandler();
                                    if (result2 != null) {
                                    }
                                    return false;
                                }
                                if (result2 != null) {
                                    if ((result2.isSuccess() ^ 1) != 0) {
                                    }
                                }
                                return false;
                            } catch (NumberFormatException e3) {
                                Log.e(TAG, "UPnP Service specification invalid: " + s, e3);
                                return false;
                            }
                        } else if ("bonjour".equals(data[0])) {
                            if (!(data[1] == null || data[2] == null)) {
                                ArrayList arrayList = null;
                                ArrayList response = null;
                                try {
                                    arrayList = NativeUtil.byteArrayToArrayList(NativeUtil.hexStringToByteArray(data[1]));
                                    response = NativeUtil.byteArrayToArrayList(NativeUtil.hexStringToByteArray(data[2]));
                                } catch (IllegalArgumentException e4) {
                                    Log.e(TAG, "IllegalArgumentException Invalid argument.");
                                } catch (Exception e5) {
                                    Log.e(TAG, "Invalid bonjour service description.");
                                    return false;
                                }
                                result3 = new SupplicantResult("addBonjourService(" + data[1] + ", " + data[2] + ")");
                                result3.setResult(this.mISupplicantP2pIface.addBonjourService(arrayList, response));
                                result2 = result3;
                            }
                            if (result2 != null) {
                            }
                            return false;
                        } else {
                            return false;
                        }
                    } catch (RemoteException e6) {
                        e = e6;
                    }
                }
                return true;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:106:0x00c6 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00be A:{SYNTHETIC, Splitter: B:44:0x00be} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00be A:{SYNTHETIC, Splitter: B:44:0x00be} */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x00c6 A:{SYNTHETIC} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean serviceRemove(WifiP2pServiceInfo servInfo) {
        RemoteException e;
        SupplicantResult<Void> result;
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("serviceRemove")) {
                return false;
            } else if (servInfo == null) {
                Log.e(TAG, "Null service info passed.");
                return false;
            } else {
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
                    SupplicantResult result2 = null;
                    try {
                        SupplicantResult<Void> result3;
                        if ("upnp".equals(data[0])) {
                            try {
                                int version = Integer.parseInt(data[1], 16);
                                result3 = new SupplicantResult("removeUpnpService(" + data[1] + ", " + data[2] + ")");
                                try {
                                    result3.setResult(this.mISupplicantP2pIface.removeUpnpService(version, data[2]));
                                    result2 = result3;
                                } catch (RemoteException e2) {
                                    e = e2;
                                    result2 = result3;
                                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                                    supplicantServiceDiedHandler();
                                    if (result2 != null) {
                                    }
                                    return false;
                                }
                                if (result2 != null) {
                                    if ((result2.isSuccess() ^ 1) != 0) {
                                    }
                                }
                                return false;
                            } catch (NumberFormatException e3) {
                                Log.e(TAG, "UPnP Service specification invalid: " + s, e3);
                                return false;
                            }
                        } else if ("bonjour".equals(data[0])) {
                            if (data[1] != null) {
                                try {
                                    ArrayList<Byte> request = NativeUtil.byteArrayToArrayList(NativeUtil.hexStringToByteArray(data[1]));
                                    result3 = new SupplicantResult("removeBonjourService(" + data[1] + ")");
                                    result3.setResult(this.mISupplicantP2pIface.removeBonjourService(request));
                                    result2 = result3;
                                } catch (IllegalArgumentException e4) {
                                    Log.e(TAG, "IllegalArgumentException occur when byteArrayToArrayList");
                                    return false;
                                } catch (Exception e5) {
                                    Log.e(TAG, "Invalid bonjour service description.");
                                    return false;
                                }
                            }
                            if (result2 != null) {
                            }
                            return false;
                        } else {
                            Log.e(TAG, "Unknown / unsupported P2P service requested: " + data[0]);
                            return false;
                        }
                    } catch (RemoteException e6) {
                        e = e6;
                    }
                }
                return true;
            }
        }
    }

    public String requestServiceDiscovery(String peerAddress, String query) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("requestServiceDiscovery")) {
                return null;
            } else if (peerAddress == null) {
                Log.e(TAG, "Cannot parse peer mac address.");
                return null;
            } else {
                try {
                    byte[] macAddress = NativeUtil.macAddressToByteArray(peerAddress);
                    if (query == null) {
                        Log.e(TAG, "Cannot parse service discovery query: " + query);
                        return null;
                    }
                    try {
                        ArrayList<Byte> binQuery = NativeUtil.byteArrayToArrayList(NativeUtil.hexStringToByteArray(query));
                        SupplicantResult<Long> result = new SupplicantResult("requestServiceDiscovery(" + peerAddress + ", " + query + ")");
                        try {
                            this.mISupplicantP2pIface.requestServiceDiscovery(macAddress, binQuery, new AnonymousClass12(result));
                        } catch (RemoteException e) {
                            Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                            supplicantServiceDiedHandler();
                        }
                        Long value = (Long) result.getResult();
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
                } catch (Exception e22) {
                    Log.e(TAG, "Could not process peer MAC address.", e22);
                    return null;
                }
            }
        }
    }

    public boolean cancelServiceDiscovery(String identifier) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("cancelServiceDiscovery")) {
                return false;
            } else if (identifier == null) {
                Log.e(TAG, "cancelServiceDiscovery requires a valid tag.");
                return false;
            } else {
                try {
                    long id = Long.parseLong(identifier);
                    SupplicantResult<Void> result = new SupplicantResult("cancelServiceDiscovery(" + identifier + ")");
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
    }

    public boolean setMiracastMode(int mode) {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("setMiracastMode")) {
                byte targetMode = (byte) 0;
                switch (mode) {
                    case 1:
                        targetMode = (byte) 1;
                        break;
                    case 2:
                        targetMode = (byte) 2;
                        break;
                }
                SupplicantResult<Void> result = new SupplicantResult("setMiracastMode(" + mode + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.setMiracastMode(targetMode));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
            return false;
        }
    }

    public boolean startWpsPbc(String groupIfName, String bssid) {
        if (TextUtils.isEmpty(groupIfName)) {
            Log.e(TAG, "Group name required when requesting WPS PBC. Got (" + groupIfName + ")");
            return false;
        }
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("startWpsPbc")) {
                byte[] bArr = new byte[6];
                bArr = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
                try {
                    bArr = NativeUtil.macAddressToByteArray(bssid);
                    SupplicantResult<Void> result = new SupplicantResult("startWpsPbc(" + groupIfName + ", " + bssid + ")");
                    try {
                        result.setResult(this.mISupplicantP2pIface.startWpsPbc(groupIfName, bArr));
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
            return false;
        }
    }

    public boolean startWpsPinKeypad(String groupIfName, String pin) {
        if (TextUtils.isEmpty(groupIfName) || TextUtils.isEmpty(pin)) {
            return false;
        }
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("startWpsPinKeypad")) {
                return false;
            } else if (groupIfName == null) {
                Log.e(TAG, "Group name required when requesting WPS KEYPAD.");
                return false;
            } else if (pin == null) {
                Log.e(TAG, "PIN required when requesting WPS KEYPAD.");
                return false;
            } else {
                SupplicantResult<Void> result = new SupplicantResult("startWpsPinKeypad(" + groupIfName + ", " + pin + ")");
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
            } else if (groupIfName == null) {
                Log.e(TAG, "Group name required when requesting WPS KEYPAD.");
                return null;
            } else {
                byte[] bArr = new byte[6];
                bArr = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
                try {
                    bArr = NativeUtil.macAddressToByteArray(bssid);
                    SupplicantResult<String> result = new SupplicantResult("startWpsPinDisplay(" + groupIfName + ", " + bssid + ")");
                    try {
                        this.mISupplicantP2pIface.startWpsPinDisplay(groupIfName, bArr, new AnonymousClass13(result));
                    } catch (RemoteException e) {
                        Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                        supplicantServiceDiedHandler();
                    }
                    String str = (String) result.getResult();
                    return str;
                } catch (Exception e2) {
                    Log.e(TAG, "Could not parse BSSID.", e2);
                    return null;
                }
            }
        }
    }

    public boolean cancelWps(String groupIfName) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("cancelWps")) {
                return false;
            } else if (groupIfName == null) {
                Log.e(TAG, "Group name required when requesting WPS KEYPAD.");
                return false;
            } else {
                SupplicantResult<Void> result = new SupplicantResult("cancelWps(" + groupIfName + ")");
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
    }

    public boolean enableWfd(boolean enable) {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("enableWfd")) {
                SupplicantResult<Void> result = new SupplicantResult("enableWfd(" + enable + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.enableWfd(enable));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
            return false;
        }
    }

    public boolean setWfdDeviceInfo(String info) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("setWfdDeviceInfo")) {
                return false;
            } else if (info == null) {
                Log.e(TAG, "Cannot parse null WFD info string.");
                return false;
            } else {
                try {
                    byte[] wfdInfo = NativeUtil.hexStringToByteArray(info);
                    SupplicantResult<Void> result = new SupplicantResult("setWfdDeviceInfo(" + info + ")");
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
    }

    public boolean removeNetwork(int networkId) {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("removeNetwork")) {
                SupplicantResult<Void> result = new SupplicantResult("removeNetwork(" + networkId + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.removeNetwork(networkId));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
            return false;
        }
    }

    private List<Integer> listNetworks() {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("listNetworks")) {
                SupplicantResult<ArrayList> result = new SupplicantResult("listNetworks()");
                try {
                    this.mISupplicantP2pIface.listNetworks(new AnonymousClass5(result));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                List<Integer> list = (List) result.getResult();
                return list;
            }
            return null;
        }
    }

    private ISupplicantP2pNetwork getNetwork(int networkId) {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("getNetwork")) {
                SupplicantResult<ISupplicantNetwork> result = new SupplicantResult("getNetwork(" + networkId + ")");
                try {
                    this.mISupplicantP2pIface.getNetwork(networkId, new AnonymousClass4(result));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                if (result.getResult() == null) {
                    Log.e(TAG, "getNetwork got null network");
                    return null;
                }
                ISupplicantP2pNetwork p2pNetworkMockable = getP2pNetworkMockable((ISupplicantNetwork) result.getResult());
                return p2pNetworkMockable;
            }
            return null;
        }
    }

    public boolean loadGroups(WifiP2pGroupList groups) {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("loadGroups")) {
                List<Integer> networkIds = listNetworks();
                if (networkIds == null || networkIds.isEmpty()) {
                    return false;
                }
                for (Integer networkId : networkIds) {
                    ISupplicantP2pNetwork network = getNetwork(networkId.intValue());
                    if (network == null) {
                        Log.e(TAG, "Failed to retrieve network object for " + networkId);
                    } else {
                        SupplicantResult<Boolean> resultIsCurrent = new SupplicantResult("isCurrent(" + networkId + ")");
                        try {
                            network.isCurrent(new AnonymousClass17(resultIsCurrent));
                        } catch (RemoteException e) {
                            Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                            supplicantServiceDiedHandler();
                        }
                        if (!resultIsCurrent.isSuccess() || ((Boolean) resultIsCurrent.getResult()).booleanValue()) {
                            Log.i(TAG, "Skipping current network");
                        } else {
                            WifiP2pGroup group = new WifiP2pGroup();
                            group.setNetworkId(networkId.intValue());
                            SupplicantResult<ArrayList> resultSsid = new SupplicantResult("getSsid(" + networkId + ")");
                            try {
                                network.getSsid(new AnonymousClass16(resultSsid));
                            } catch (RemoteException e2) {
                                Log.e(TAG, "ISupplicantP2pIface exception: " + e2);
                                supplicantServiceDiedHandler();
                            }
                            if (!(!resultSsid.isSuccess() || resultSsid.getResult() == null || (((ArrayList) resultSsid.getResult()).isEmpty() ^ 1) == 0)) {
                                group.setNetworkName(NativeUtil.removeEnclosingQuotes(NativeUtil.encodeSsid((ArrayList) resultSsid.getResult())));
                            }
                            SupplicantResult<byte[]> resultBssid = new SupplicantResult("getBssid(" + networkId + ")");
                            try {
                                network.getBssid(new AnonymousClass14(resultBssid));
                            } catch (RemoteException e22) {
                                Log.e(TAG, "ISupplicantP2pIface exception: " + e22);
                                supplicantServiceDiedHandler();
                            }
                            if (resultBssid.isSuccess() && (ArrayUtils.isEmpty((byte[]) resultBssid.getResult()) ^ 1) != 0) {
                                WifiP2pDevice device = new WifiP2pDevice();
                                device.deviceAddress = NativeUtil.macAddressFromByteArray((byte[]) resultBssid.getResult());
                                group.setOwner(device);
                            }
                            SupplicantResult<Boolean> resultIsGo = new SupplicantResult("isGo(" + networkId + ")");
                            try {
                                network.isGo(new AnonymousClass18(resultIsGo));
                            } catch (RemoteException e222) {
                                Log.e(TAG, "ISupplicantP2pIface exception: " + e222);
                                supplicantServiceDiedHandler();
                            }
                            if (resultIsGo.isSuccess()) {
                                group.setIsGroupOwner(((Boolean) resultIsGo.getResult()).booleanValue());
                            }
                            groups.add(group);
                        }
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean setWpsDeviceName(String name) {
        if (name == null) {
            return false;
        }
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("setWpsDeviceName")) {
                SupplicantResult<Void> result = new SupplicantResult("setWpsDeviceName(" + name + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.setWpsDeviceName(name));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
            return false;
        }
    }

    public boolean setWpsDeviceType(String typeStr) {
        try {
            Matcher match = WPS_DEVICE_TYPE_PATTERN.matcher(typeStr);
            if (match.find() && match.groupCount() == 3) {
                short categ = Short.parseShort(match.group(1));
                byte[] oui = NativeUtil.hexStringToByteArray(match.group(2));
                short subCateg = Short.parseShort(match.group(3));
                byte[] bytes = new byte[8];
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
                byteBuffer.putShort(categ);
                byteBuffer.put(oui);
                byteBuffer.putShort(subCateg);
                synchronized (this.mLock) {
                    if (checkSupplicantP2pIfaceAndLogFailure("setWpsDeviceType")) {
                        SupplicantResult<Void> result = new SupplicantResult("setWpsDeviceType(" + typeStr + ")");
                        try {
                            result.setResult(this.mISupplicantP2pIface.setWpsDeviceType(bytes));
                        } catch (RemoteException e) {
                            Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                            supplicantServiceDiedHandler();
                        }
                        boolean isSuccess = result.isSuccess();
                        return isSuccess;
                    }
                    return false;
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
            if (checkSupplicantP2pIfaceAndLogFailure("setWpsConfigMethods")) {
                SupplicantResult<Void> result = new SupplicantResult("setWpsConfigMethods(" + configMethodsStr + ")");
                short configMethodsMask = (short) 0;
                for (String stringToWpsConfigMethod : configMethodsStr.split("\\s+")) {
                    configMethodsMask = (short) (stringToWpsConfigMethod(stringToWpsConfigMethod) | configMethodsMask);
                }
                try {
                    result.setResult(this.mISupplicantP2pIface.setWpsConfigMethods(configMethodsMask));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                return result.isSuccess();
            }
            return false;
        }
    }

    public String getNfcHandoverRequest() {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("getNfcHandoverRequest")) {
                SupplicantResult<ArrayList> result = new SupplicantResult("getNfcHandoverRequest()");
                try {
                    this.mISupplicantP2pIface.createNfcHandoverRequestMessage(new AnonymousClass7(result));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                if (result.isSuccess()) {
                    String hexStringFromByteArray = NativeUtil.hexStringFromByteArray(NativeUtil.byteArrayFromArrayList((ArrayList) result.getResult()));
                    return hexStringFromByteArray;
                }
                return null;
            }
            return null;
        }
    }

    public String getNfcHandoverSelect() {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("getNfcHandoverSelect")) {
                SupplicantResult<ArrayList> result = new SupplicantResult("getNfcHandoverSelect()");
                try {
                    this.mISupplicantP2pIface.createNfcHandoverSelectMessage(new AnonymousClass8(result));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                if (result.isSuccess()) {
                    String hexStringFromByteArray = NativeUtil.hexStringFromByteArray(NativeUtil.byteArrayFromArrayList((ArrayList) result.getResult()));
                    return hexStringFromByteArray;
                }
                return null;
            }
            return null;
        }
    }

    public boolean initiatorReportNfcHandover(String selectMessage) {
        if (selectMessage == null) {
            return false;
        }
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("initiatorReportNfcHandover")) {
                SupplicantResult<Void> result = new SupplicantResult("initiatorReportNfcHandover(" + selectMessage + ")");
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
            return false;
        }
    }

    public boolean responderReportNfcHandover(String requestMessage) {
        if (requestMessage == null) {
            return false;
        }
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("responderReportNfcHandover")) {
                SupplicantResult<Void> result = new SupplicantResult("responderReportNfcHandover(" + requestMessage + ")");
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
            return false;
        }
    }

    public boolean setClientList(int networkId, String clientListStr) {
        synchronized (this.mLock) {
            if (!checkSupplicantP2pIfaceAndLogFailure("setClientList")) {
                return false;
            } else if (TextUtils.isEmpty(clientListStr)) {
                Log.e(TAG, "Invalid client list");
                return false;
            } else {
                ISupplicantP2pNetwork network = getNetwork(networkId);
                if (network == null) {
                    Log.e(TAG, "Invalid network id ");
                    return false;
                }
                SupplicantResult<Void> result = new SupplicantResult("setClientList(" + networkId + ", " + clientListStr + ")");
                try {
                    ArrayList<byte[]> clients = new ArrayList();
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
    }

    public String getClientList(int networkId) {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("getClientList")) {
                ISupplicantP2pNetwork network = getNetwork(networkId);
                if (network == null) {
                    Log.e(TAG, "Invalid network id ");
                    return null;
                }
                SupplicantResult<ArrayList> result = new SupplicantResult("getClientList(" + networkId + ")");
                try {
                    network.getClientList(new AnonymousClass15(result));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                if (result.isSuccess()) {
                    String str = (String) ((ArrayList) result.getResult()).stream().map(new -$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo()).collect(Collectors.joining(" "));
                    return str;
                }
                return null;
            }
            return null;
        }
    }

    public boolean saveConfig() {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("saveConfig")) {
                SupplicantResult<Void> result = new SupplicantResult("saveConfig()");
                try {
                    result.setResult(this.mISupplicantP2pIface.saveConfig());
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
            return false;
        }
    }

    private static short stringToWpsConfigMethod(String configMethod) {
        if (configMethod.equals("usba")) {
            return (short) 1;
        }
        if (configMethod.equals("ethernet")) {
            return (short) 2;
        }
        if (configMethod.equals("label")) {
            return (short) 4;
        }
        if (configMethod.equals("display")) {
            return (short) 8;
        }
        if (configMethod.equals("int_nfc_token")) {
            return (short) 32;
        }
        if (configMethod.equals("ext_nfc_token")) {
            return (short) 16;
        }
        if (configMethod.equals("nfc_interface")) {
            return (short) 64;
        }
        if (configMethod.equals("push_button")) {
            return WpsConfigMethods.PUSHBUTTON;
        }
        if (configMethod.equals("keypad")) {
            return WpsConfigMethods.KEYPAD;
        }
        if (configMethod.equals("virtual_push_button")) {
            return WpsConfigMethods.VIRT_PUSHBUTTON;
        }
        if (configMethod.equals("physical_push_button")) {
            return WpsConfigMethods.PHY_PUSHBUTTON;
        }
        if (configMethod.equals("p2ps")) {
            return WpsConfigMethods.P2PS;
        }
        if (configMethod.equals("virtual_display")) {
            return WpsConfigMethods.VIRT_DISPLAY;
        }
        if (configMethod.equals("physical_display")) {
            return WpsConfigMethods.PHY_DISPLAY;
        }
        throw new IllegalArgumentException("Invalid WPS config method: " + configMethod);
    }

    public boolean addP2pRptGroup(String config) {
        synchronized (this.mLock) {
            if (checkSupplicantP2pIfaceAndLogFailure("rptP2pAddGroup")) {
                SupplicantResult<Void> result = new SupplicantResult("rptP2pAddGroup()");
                try {
                    result.setResult(this.mISupplicantP2pIface.rptP2pAddGroup(config));
                } catch (RemoteException e) {
                    Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result.isSuccess();
                return isSuccess;
            }
            return false;
        }
    }
}
