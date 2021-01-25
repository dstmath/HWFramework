package com.huawei.wifi2;

import android.hardware.wifi.V1_0.IWifi;
import android.hardware.wifi.V1_0.IWifiApIface;
import android.hardware.wifi.V1_0.IWifiChip;
import android.hardware.wifi.V1_0.IWifiEventCallback;
import android.hardware.wifi.V1_0.IWifiIface;
import android.hardware.wifi.V1_0.IWifiNanIface;
import android.hardware.wifi.V1_0.IWifiP2pIface;
import android.hardware.wifi.V1_0.IWifiStaIface;
import android.hardware.wifi.V1_0.WifiStatus;
import android.hidl.manager.V1_0.IServiceNotification;
import android.hidl.manager.V1_2.IServiceManager;
import android.os.Handler;
import android.os.HidlSupport;
import android.os.IHwBinder;
import android.os.RemoteException;
import android.util.LongSparseArray;
import android.util.MutableBoolean;
import android.util.MutableInt;
import android.util.Pair;
import android.util.wifi.HwHiLog;
import com.huawei.wifi2.HwWifi2HalDeviceManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HwWifi2HalDeviceManager {
    private static final int[] IFACE_TYPES_BY_PRIORITY = {1, 0, 2, 3};
    private static final int INVALID_VALUE = -1;
    private static final String TAG = "HwWifi2HalDeviceManager";
    private final HwWifi2Clock mClock = new HwWifi2Clock();
    private final IHwBinder.DeathRecipient mIWifiDeathRecipient = new IHwBinder.DeathRecipient() {
        /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$gOAOAftRT2H8aq_lxZ8aVOz8_rg */

        public final void serviceDied(long j) {
            HwWifi2HalDeviceManager.this.lambda$new$1$HwWifi2HalDeviceManager(j);
        }
    };
    private final Map<Pair<String, Integer>, InterfaceCacheEntry> mInterfaceInfoCache = new HashMap();
    private boolean mIsReady;
    private boolean mIsVendorHalSupported = false;
    private final Object mLock = new Object();
    private final Set<ManagerStatusListenerProxy> mManagerStatusListeners = new HashSet();
    private IServiceManager mServiceManager;
    private final IHwBinder.DeathRecipient mServiceManagerDeathRecipient = new IHwBinder.DeathRecipient() {
        /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$y16RBJjM53HyhVUlWgSCbiJwcW8 */

        public final void serviceDied(long j) {
            HwWifi2HalDeviceManager.this.lambda$new$0$HwWifi2HalDeviceManager(j);
        }
    };
    private final IServiceNotification mServiceNotificationCallback = new IServiceNotification.Stub() {
        /* class com.huawei.wifi2.HwWifi2HalDeviceManager.AnonymousClass1 */

        public void onRegistration(String fqName, String name, boolean isPreexisting) {
            synchronized (HwWifi2HalDeviceManager.this.mLock) {
                HwWifi2HalDeviceManager.this.initWifiIfNecessary();
            }
        }
    };
    private IWifi mWifi;
    private final WifiEventCallback mWifiEventCallback = new WifiEventCallback();

    public interface InterfaceAvailableForRequestListener {
        void onAvailabilityChanged(boolean z);
    }

    public interface InterfaceDestroyedListener {
        void onDestroyed(String str);
    }

    public interface ManagerStatusListener {
        void onStatusChanged();
    }

    public /* synthetic */ void lambda$new$0$HwWifi2HalDeviceManager(long cookie) {
        HwHiLog.i(TAG, false, "IServiceManager died: cookie = %{public}s", new Object[]{String.valueOf(cookie)});
        synchronized (this.mLock) {
            this.mServiceManager = null;
        }
    }

    public /* synthetic */ void lambda$new$1$HwWifi2HalDeviceManager(long cookie) {
        HwHiLog.i(TAG, false, "IWifi HAL service died! Have listener for it, cookie = %{public}s", new Object[]{String.valueOf(cookie)});
        synchronized (this.mLock) {
            this.mWifi = null;
            this.mIsReady = false;
        }
    }

    public void initialize() {
        initializeInternal();
    }

    private void initializeInternal() {
        HwHiLog.i(TAG, false, "initializeInternal Enter", new Object[0]);
        initServiceManagerIfNecessary();
        if (this.mIsVendorHalSupported) {
            synchronized (this.mLock) {
                initWifiIfNecessary();
            }
        }
    }

    private void initServiceManagerIfNecessary() {
        HwHiLog.i(TAG, false, "initServiceManagerIfNecessary enter", new Object[0]);
        synchronized (this.mLock) {
            if (this.mServiceManager == null) {
                this.mServiceManager = getServiceManagerMockable();
                if (this.mServiceManager == null) {
                    HwHiLog.e(TAG, false, "Failed to get IServiceManager instance", new Object[0]);
                } else {
                    try {
                        if (!this.mServiceManager.linkToDeath(this.mServiceManagerDeathRecipient, 0)) {
                            HwHiLog.e(TAG, false, "Error on linkToDeath on IServiceManager", new Object[0]);
                            this.mServiceManager = null;
                            return;
                        }
                        if (!this.mServiceManager.registerForNotifications("android.hardware.wifi@1.0::IWifi", "", this.mServiceNotificationCallback)) {
                            HwHiLog.e(TAG, false, "Failed to register listener for IWifi service", new Object[0]);
                            this.mServiceManager = null;
                        }
                        this.mIsVendorHalSupported = isSupportedInternal();
                        HwHiLog.i(TAG, false, "initServiceManagerIfNecessary finish, mIsVendorHalSupported = %{public}b", new Object[]{Boolean.valueOf(this.mIsVendorHalSupported)});
                    } catch (RemoteException e) {
                        HwHiLog.e(TAG, false, "Exception while operating on IServiceManager", new Object[0]);
                        this.mServiceManager = null;
                    }
                }
            }
        }
    }

    public IServiceManager getServiceManagerMockable() {
        try {
            return IServiceManager.getService();
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "Exception getting IServiceManager", new Object[0]);
            return null;
        }
    }

    private boolean isSupportedInternal() {
        boolean z = false;
        HwHiLog.i(TAG, false, "isSupportedInternal enter", new Object[0]);
        synchronized (this.mLock) {
            if (this.mServiceManager == null) {
                HwHiLog.e(TAG, false, "isSupported: called but mServiceManager is null!?", new Object[0]);
                return false;
            }
            try {
                if (!this.mServiceManager.listManifestByInterface("android.hardware.wifi@1.0::IWifi").isEmpty()) {
                    z = true;
                }
                return z;
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "Exception while operating on IServiceManager", new Object[0]);
                return false;
            }
        }
    }

    private void initWifiIfNecessary() {
        synchronized (this.mLock) {
            if (this.mWifi != null) {
                HwHiLog.e(TAG, false, "initWifiIfNecessary did already ", new Object[0]);
                return;
            }
            try {
                this.mWifi = getWifiServiceMockable();
                if (this.mWifi == null) {
                    HwHiLog.e(TAG, false, "IWifi not (yet) available - but have a listener for it", new Object[0]);
                    return;
                }
                HwHiLog.i(TAG, false, "getWifiServiceMockable success", new Object[0]);
                if (!this.mWifi.linkToDeath(this.mIWifiDeathRecipient, 0)) {
                    HwHiLog.e(TAG, false, "Error on linkToDeath on IWifi - will retry later", new Object[0]);
                    return;
                }
                WifiStatus status = this.mWifi.registerEventCallback(this.mWifiEventCallback);
                if (status.code != 0) {
                    HwHiLog.e(TAG, false, "IWifi.registerEventCallback failed: %{public}s", new Object[]{statusString(status)});
                    this.mWifi = null;
                    return;
                }
                this.mIsReady = true;
                HwHiLog.i(TAG, false, "initWifiIfNecessary success", new Object[0]);
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "Exception while operating on IWifi", new Object[0]);
            }
        }
    }

    public IWifi getWifiServiceMockable() {
        try {
            return IWifi.getService(true);
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "Exception getting IWifi service", new Object[0]);
            return null;
        }
    }

    public class WifiEventCallback extends IWifiEventCallback.Stub {
        private WifiEventCallback() {
            HwWifi2HalDeviceManager.this = r1;
        }

        public void onStart() {
            HwHiLog.i(HwWifi2HalDeviceManager.TAG, false, "IWifiEventCallback.onStart", new Object[0]);
        }

        public void onStop() {
            HwHiLog.w(HwWifi2HalDeviceManager.TAG, false, "IWifiEventCallback.onStop", new Object[0]);
        }

        public void onFailure(WifiStatus status) {
            HwHiLog.w(HwWifi2HalDeviceManager.TAG, false, "IWifiEventCallback.onFailure: %{public}s", new Object[]{HwWifi2HalDeviceManager.statusString(status)});
            HwWifi2HalDeviceManager.this.teardownInternal();
        }
    }

    private void teardownInternal() {
        HwHiLog.i(TAG, false, "teardownInternal Enter", new Object[0]);
        managerStatusListenerDispatch();
        dispatchAllDestroyedListeners();
    }

    private void managerStatusListenerDispatch() {
        HwHiLog.i(TAG, false, "managerStatusListenerDispatch enter", new Object[0]);
        synchronized (this.mLock) {
            for (ManagerStatusListenerProxy cb : this.mManagerStatusListeners) {
                cb.trigger();
            }
        }
    }

    public void dispatchAllDestroyedListeners() {
        HwHiLog.i(TAG, false, "dispatchAllDestroyedListeners enter", new Object[0]);
        synchronized (this.mLock) {
            Iterator<Map.Entry<Pair<String, Integer>, InterfaceCacheEntry>> it = this.mInterfaceInfoCache.entrySet().iterator();
            while (it.hasNext()) {
                try {
                    InterfaceCacheEntry entry = it.next().getValue();
                    for (InterfaceDestroyedListenerProxy listener : entry.destroyedListeners) {
                        listener.trigger();
                    }
                    entry.destroyedListeners.clear();
                    it.remove();
                } catch (ConcurrentModificationException e) {
                    HwHiLog.e(TAG, false, "dispatchAllDestroyedListeners: clear destroyedListeners error", new Object[0]);
                }
            }
        }
    }

    public void registerStatusListener(ManagerStatusListener listener, Handler handler) {
        synchronized (this.mLock) {
            if (!this.mManagerStatusListeners.add(new ManagerStatusListenerProxy(listener, handler))) {
                HwHiLog.i(TAG, false, "registerStatusListener: duplicate registration ignored", new Object[0]);
            }
        }
    }

    public class ManagerStatusListenerProxy extends ListenerProxy<ManagerStatusListener> {
        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        ManagerStatusListenerProxy(ManagerStatusListener statusListener, Handler handler) {
            super(statusListener, handler, "ManagerStatusListenerProxy");
            HwWifi2HalDeviceManager.this = r2;
        }

        @Override // com.huawei.wifi2.HwWifi2HalDeviceManager.ListenerProxy
        public void action() {
            ((ManagerStatusListener) this.mListener).onStatusChanged();
        }
    }

    public abstract class ListenerProxy<LISTENER> {
        private Handler mHandler;
        protected LISTENER mListener;

        ListenerProxy(LISTENER listener, Handler handler, String tag) {
            HwWifi2HalDeviceManager.this = r1;
            this.mListener = listener;
            this.mHandler = handler;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof ListenerProxy) || this.mListener != ((ListenerProxy) obj).mListener) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return this.mListener.hashCode();
        }

        public void trigger() {
            Handler handler = this.mHandler;
            if (handler != null) {
                handler.post(new Runnable() {
                    /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$ListenerProxy$WoM_q0SaHRBAnmqcpDCPmgKtPqw */

                    @Override // java.lang.Runnable
                    public final void run() {
                        HwWifi2HalDeviceManager.ListenerProxy.this.lambda$trigger$0$HwWifi2HalDeviceManager$ListenerProxy();
                    }
                });
            } else {
                lambda$trigger$0$HwWifi2HalDeviceManager$ListenerProxy();
            }
        }

        public void triggerWithArg(boolean isArgInclude) {
            Handler handler = this.mHandler;
            if (handler != null) {
                handler.post(new Runnable(isArgInclude) {
                    /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$ListenerProxy$Wk7syHsw6NFmsp8scd28Te46oE */
                    private final /* synthetic */ boolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        HwWifi2HalDeviceManager.ListenerProxy.this.lambda$triggerWithArg$1$HwWifi2HalDeviceManager$ListenerProxy(this.f$1);
                    }
                });
            } else {
                lambda$triggerWithArg$1$HwWifi2HalDeviceManager$ListenerProxy(isArgInclude);
            }
        }

        /* renamed from: action */
        public void lambda$trigger$0$HwWifi2HalDeviceManager$ListenerProxy() {
        }

        /* renamed from: actionWithArg */
        public void lambda$triggerWithArg$1$HwWifi2HalDeviceManager$ListenerProxy(boolean isArgInclude) {
        }
    }

    public boolean start() {
        return startWifi();
    }

    private boolean startWifi() {
        HwHiLog.i(TAG, false, "startWifi", new Object[0]);
        synchronized (this.mLock) {
            initWifiIfNecessary();
        }
        if (this.mWifi != null) {
            return isStarted();
        }
        HwHiLog.e(TAG, false, "startWifi called but mWifi is null", new Object[0]);
        return false;
    }

    public boolean isStarted() {
        synchronized (this.mLock) {
            try {
                if (this.mWifi == null) {
                    HwHiLog.w(TAG, false, "isWifiStarted called but mWifi is null", new Object[0]);
                    return false;
                }
                return this.mWifi.isStarted();
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "isWifiStarted exception", new Object[0]);
                return false;
            }
        }
    }

    public IWifiStaIface createStaIface(boolean isLowPrioritySta, InterfaceDestroyedListener destroyedListener, Handler handler) {
        IWifiIface iface = createIface(0, isLowPrioritySta, destroyedListener, handler);
        if (iface instanceof IWifiStaIface) {
            return (IWifiStaIface) iface;
        }
        return null;
    }

    private IWifiIface createIface(int ifaceType, boolean isLowPriority, InterfaceDestroyedListener destroyedListener, Handler handler) {
        HwHiLog.i(TAG, false, "createIface: ifaceType=%{public}d, isLowPriority=%{public}b", new Object[]{Integer.valueOf(ifaceType), Boolean.valueOf(isLowPriority)});
        synchronized (this.mLock) {
            WifiChipInfo[] chipInfos = getAllChipInfo();
            if (chipInfos == null) {
                HwHiLog.e(TAG, false, "createIface: no chip info found", new Object[0]);
                return null;
            } else if (!validateInterfaceCache(chipInfos)) {
                HwHiLog.e(TAG, false, "createIface: local cache is invalid!", new Object[0]);
                return null;
            } else {
                IWifiIface iface = createIfaceIfPossible(chipInfos, ifaceType, isLowPriority, destroyedListener, handler);
                if (iface == null) {
                    HwHiLog.e(TAG, false, "createStaIface failure", new Object[0]);
                }
                return iface;
            }
        }
    }

    private boolean getChipIds(HidlSupport.Mutable<ArrayList<Integer>> result) {
        MutableBoolean statusOk = new MutableBoolean(false);
        try {
            this.mWifi.getChipIds(new IWifi.getChipIdsCallback(statusOk, result) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$_YqV3m8z3cHFOWfOGbivueGoOlU */
                private final /* synthetic */ MutableBoolean f$0;
                private final /* synthetic */ HidlSupport.Mutable f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                    HwWifi2HalDeviceManager.lambda$getChipIds$2(this.f$0, this.f$1, wifiStatus, arrayList);
                }
            });
            if (!statusOk.value) {
                return false;
            }
            if (((ArrayList) result.value).size() != 0) {
                return true;
            }
            HwHiLog.e(TAG, false, "Should have at least 1 chip", new Object[0]);
            return false;
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "getChipIds exception", new Object[0]);
            return false;
        }
    }

    static /* synthetic */ void lambda$getChipIds$2(MutableBoolean statusOk, HidlSupport.Mutable result, WifiStatus status, ArrayList chipIds) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            result.value = chipIds;
        } else {
            HwHiLog.e(TAG, false, "getChipIds failed: %{public}s", new Object[]{statusString(status)});
        }
    }

    private boolean getChipForId(int chipId, HidlSupport.Mutable<IWifiChip> chipResp) {
        MutableBoolean statusOk = new MutableBoolean(false);
        try {
            this.mWifi.getChip(chipId, new IWifi.getChipCallback(statusOk, chipResp) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$PAYcDiGe3dXfde28JJJ865aINU */
                private final /* synthetic */ MutableBoolean f$0;
                private final /* synthetic */ HidlSupport.Mutable f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void onValues(WifiStatus wifiStatus, IWifiChip iWifiChip) {
                    HwWifi2HalDeviceManager.lambda$getChipForId$3(this.f$0, this.f$1, wifiStatus, iWifiChip);
                }
            });
            if (!statusOk.value || chipResp.value == null) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "getChipForId exception", new Object[0]);
            return false;
        }
    }

    static /* synthetic */ void lambda$getChipForId$3(MutableBoolean statusOk, HidlSupport.Mutable chipResp, WifiStatus status, IWifiChip chip) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            chipResp.value = chip;
        } else {
            HwHiLog.e(TAG, false, "getChip failed:%{public}s", new Object[]{statusString(status)});
        }
    }

    private boolean getAvailableModesForChip(HidlSupport.Mutable<IWifiChip> chipResp, HidlSupport.Mutable<ArrayList<IWifiChip.ChipMode>> availableModesResp) {
        MutableBoolean statusOk = new MutableBoolean(false);
        try {
            ((IWifiChip) chipResp.value).getAvailableModes(new IWifiChip.getAvailableModesCallback(statusOk, availableModesResp) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$yG9btzJplqKYHsMOs5QUeF_GiXU */
                private final /* synthetic */ MutableBoolean f$0;
                private final /* synthetic */ HidlSupport.Mutable f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                    HwWifi2HalDeviceManager.lambda$getAvailableModesForChip$4(this.f$0, this.f$1, wifiStatus, arrayList);
                }
            });
            if (!statusOk.value) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "getAvailableModesForChip exception", new Object[0]);
            return false;
        }
    }

    static /* synthetic */ void lambda$getAvailableModesForChip$4(MutableBoolean statusOk, HidlSupport.Mutable availableModesResp, WifiStatus status, ArrayList modes) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            availableModesResp.value = modes;
        } else {
            HwHiLog.e(TAG, false, "getAvailableModes failed: %{public}s", new Object[]{statusString(status)});
        }
    }

    private boolean getModeForChip(HidlSupport.Mutable<IWifiChip> chipResp, MutableBoolean currentModeValidResp, MutableInt currentModeResp) {
        MutableBoolean statusOk = new MutableBoolean(false);
        try {
            ((IWifiChip) chipResp.value).getMode(new IWifiChip.getModeCallback(statusOk, currentModeValidResp, currentModeResp) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$XeZGay_vek0E9MoAItr4gfcqPk */
                private final /* synthetic */ MutableBoolean f$0;
                private final /* synthetic */ MutableBoolean f$1;
                private final /* synthetic */ MutableInt f$2;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void onValues(WifiStatus wifiStatus, int i) {
                    HwWifi2HalDeviceManager.lambda$getModeForChip$5(this.f$0, this.f$1, this.f$2, wifiStatus, i);
                }
            });
            if (!statusOk.value) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "getModeForChip exception", new Object[0]);
            return false;
        }
    }

    static /* synthetic */ void lambda$getModeForChip$5(MutableBoolean statusOk, MutableBoolean currentModeValidResp, MutableInt currentModeResp, WifiStatus status, int modeId) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            currentModeValidResp.value = true;
            currentModeResp.value = modeId;
        } else if (status.code == 5) {
            statusOk.value = true;
            HwHiLog.i(TAG, false, "IWifiChip getMode result is ERROR_NOT_AVAILABLE", new Object[0]);
        } else {
            HwHiLog.e(TAG, false, "getMode failed: %{public}s", new Object[]{statusString(status)});
        }
    }

    private boolean getStaIfaceNamesForChip(HidlSupport.Mutable<IWifiChip> chipResp, HidlSupport.Mutable<ArrayList<String>> ifaceNamesResp) {
        MutableBoolean statusOk = new MutableBoolean(false);
        try {
            ((IWifiChip) chipResp.value).getStaIfaceNames(new IWifiChip.getStaIfaceNamesCallback(statusOk, ifaceNamesResp) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$sKqwJw6VvQCdYbm_vl6VIf4p9fk */
                private final /* synthetic */ MutableBoolean f$0;
                private final /* synthetic */ HidlSupport.Mutable f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                    HwWifi2HalDeviceManager.lambda$getStaIfaceNamesForChip$6(this.f$0, this.f$1, wifiStatus, arrayList);
                }
            });
            if (!statusOk.value) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "getStaIfaceNamesForChip exception", new Object[0]);
            return false;
        }
    }

    static /* synthetic */ void lambda$getStaIfaceNamesForChip$6(MutableBoolean statusOk, HidlSupport.Mutable ifaceNamesResp, WifiStatus status, ArrayList ifnames) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            ifaceNamesResp.value = ifnames;
            HwHiLog.e(TAG, false, "getStaIfaceNames success: ifnames = %{public}s", new Object[]{ifnames});
            return;
        }
        HwHiLog.e(TAG, false, "getStaIfaceNames failed: %{public}s", new Object[]{statusString(status)});
    }

    private boolean getStaIfaceForName(String ifaceName, HidlSupport.Mutable<IWifiChip> chipResp, WifiIfaceInfo[] staIfaces, MutableInt ifaceIndex) {
        MutableBoolean statusOk = new MutableBoolean(false);
        try {
            ((IWifiChip) chipResp.value).getStaIface(ifaceName, new IWifiChip.getStaIfaceCallback(statusOk, ifaceName, staIfaces, ifaceIndex) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$O2GDDFHD1eQhGUmuZ6tCtqaD2dg */
                private final /* synthetic */ MutableBoolean f$1;
                private final /* synthetic */ String f$2;
                private final /* synthetic */ HwWifi2HalDeviceManager.WifiIfaceInfo[] f$3;
                private final /* synthetic */ MutableInt f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void onValues(WifiStatus wifiStatus, IWifiStaIface iWifiStaIface) {
                    HwWifi2HalDeviceManager.this.lambda$getStaIfaceForName$7$HwWifi2HalDeviceManager(this.f$1, this.f$2, this.f$3, this.f$4, wifiStatus, iWifiStaIface);
                }
            });
            if (!statusOk.value) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "getStaIfaceForName exception", new Object[0]);
            return false;
        }
    }

    public /* synthetic */ void lambda$getStaIfaceForName$7$HwWifi2HalDeviceManager(MutableBoolean statusOk, String ifaceName, WifiIfaceInfo[] staIfaces, MutableInt ifaceIndex, WifiStatus status, IWifiStaIface iface) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            WifiIfaceInfo ifaceInfo = new WifiIfaceInfo();
            ifaceInfo.name = ifaceName;
            ifaceInfo.iface = iface;
            int i = ifaceIndex.value;
            ifaceIndex.value = i + 1;
            staIfaces[i] = ifaceInfo;
            return;
        }
        HwHiLog.e(TAG, false, "getStaIface failed", new Object[0]);
    }

    private boolean getApIfaceNamesForChip(HidlSupport.Mutable<IWifiChip> chipResp, HidlSupport.Mutable<ArrayList<String>> ifaceNamesResp) {
        MutableBoolean statusOk = new MutableBoolean(false);
        try {
            ((IWifiChip) chipResp.value).getApIfaceNames(new IWifiChip.getApIfaceNamesCallback(statusOk, ifaceNamesResp) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$riavEJZ02u1VvDDU1MY5y4_G9Es */
                private final /* synthetic */ MutableBoolean f$0;
                private final /* synthetic */ HidlSupport.Mutable f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                    HwWifi2HalDeviceManager.lambda$getApIfaceNamesForChip$8(this.f$0, this.f$1, wifiStatus, arrayList);
                }
            });
            if (!statusOk.value) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "getApIfaceNamesForChip exception", new Object[0]);
            return false;
        }
    }

    static /* synthetic */ void lambda$getApIfaceNamesForChip$8(MutableBoolean statusOk, HidlSupport.Mutable ifaceNamesResp, WifiStatus status, ArrayList ifnames) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            ifaceNamesResp.value = ifnames;
        } else {
            HwHiLog.e(TAG, false, "getApIfaceNamesForChip failed: %{public}s", new Object[]{statusString(status)});
        }
    }

    private boolean getApIfaceForName(String ifaceName, HidlSupport.Mutable<IWifiChip> chipResp, WifiIfaceInfo[] apIfaces, MutableInt ifaceIndex) {
        HwHiLog.i(TAG, false, "getApIfaceForName enter", new Object[0]);
        MutableBoolean statusOk = new MutableBoolean(false);
        try {
            ((IWifiChip) chipResp.value).getApIface(ifaceName, new IWifiChip.getApIfaceCallback(statusOk, ifaceName, apIfaces, ifaceIndex) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$CYnFeEuv_jepoL2xDfLdpi9WEjs */
                private final /* synthetic */ MutableBoolean f$1;
                private final /* synthetic */ String f$2;
                private final /* synthetic */ HwWifi2HalDeviceManager.WifiIfaceInfo[] f$3;
                private final /* synthetic */ MutableInt f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void onValues(WifiStatus wifiStatus, IWifiApIface iWifiApIface) {
                    HwWifi2HalDeviceManager.this.lambda$getApIfaceForName$9$HwWifi2HalDeviceManager(this.f$1, this.f$2, this.f$3, this.f$4, wifiStatus, iWifiApIface);
                }
            });
            if (!statusOk.value) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "getApIfaceForName exception", new Object[0]);
            return false;
        }
    }

    public /* synthetic */ void lambda$getApIfaceForName$9$HwWifi2HalDeviceManager(MutableBoolean statusOk, String ifaceName, WifiIfaceInfo[] apIfaces, MutableInt ifaceIndex, WifiStatus status, IWifiApIface iface) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            WifiIfaceInfo ifaceInfo = new WifiIfaceInfo();
            ifaceInfo.name = ifaceName;
            ifaceInfo.iface = iface;
            int i = ifaceIndex.value;
            ifaceIndex.value = i + 1;
            apIfaces[i] = ifaceInfo;
            return;
        }
        HwHiLog.e(TAG, false, "getApIface failed: %{public}s", new Object[]{statusString(status)});
    }

    private boolean getP2pIfaceNamesForChip(HidlSupport.Mutable<IWifiChip> chipResp, HidlSupport.Mutable<ArrayList<String>> ifaceNamesResp) {
        MutableBoolean statusOk = new MutableBoolean(false);
        try {
            ((IWifiChip) chipResp.value).getP2pIfaceNames(new IWifiChip.getP2pIfaceNamesCallback(statusOk, ifaceNamesResp) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$jJD7Xmulq8S7bRZVKg9gp2oS8bg */
                private final /* synthetic */ MutableBoolean f$0;
                private final /* synthetic */ HidlSupport.Mutable f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                    HwWifi2HalDeviceManager.lambda$getP2pIfaceNamesForChip$10(this.f$0, this.f$1, wifiStatus, arrayList);
                }
            });
            if (!statusOk.value) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "getP2pIfaceNamesForChip exception", new Object[0]);
            return false;
        }
    }

    static /* synthetic */ void lambda$getP2pIfaceNamesForChip$10(MutableBoolean statusOk, HidlSupport.Mutable ifaceNamesResp, WifiStatus status, ArrayList ifnames) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            ifaceNamesResp.value = ifnames;
        } else {
            HwHiLog.e(TAG, false, "getP2pIfaceNamesForChip failed: %{public}s", new Object[]{statusString(status)});
        }
    }

    private boolean getP2pIfaceForName(String ifaceName, HidlSupport.Mutable<IWifiChip> chipResp, WifiIfaceInfo[] p2pIfaces, MutableInt ifaceIndex) {
        MutableBoolean statusOk = new MutableBoolean(false);
        try {
            ((IWifiChip) chipResp.value).getP2pIface(ifaceName, new IWifiChip.getP2pIfaceCallback(statusOk, ifaceName, p2pIfaces, ifaceIndex) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$25FbaymKMV82rAOYAgXygR45oLs */
                private final /* synthetic */ MutableBoolean f$1;
                private final /* synthetic */ String f$2;
                private final /* synthetic */ HwWifi2HalDeviceManager.WifiIfaceInfo[] f$3;
                private final /* synthetic */ MutableInt f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void onValues(WifiStatus wifiStatus, IWifiP2pIface iWifiP2pIface) {
                    HwWifi2HalDeviceManager.this.lambda$getP2pIfaceForName$11$HwWifi2HalDeviceManager(this.f$1, this.f$2, this.f$3, this.f$4, wifiStatus, iWifiP2pIface);
                }
            });
            if (!statusOk.value) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "getP2pIfaceForName exception", new Object[0]);
            return false;
        }
    }

    public /* synthetic */ void lambda$getP2pIfaceForName$11$HwWifi2HalDeviceManager(MutableBoolean statusOk, String ifaceName, WifiIfaceInfo[] p2pIfaces, MutableInt ifaceIndex, WifiStatus status, IWifiP2pIface iface) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            WifiIfaceInfo ifaceInfo = new WifiIfaceInfo();
            ifaceInfo.name = ifaceName;
            ifaceInfo.iface = iface;
            int i = ifaceIndex.value;
            ifaceIndex.value = i + 1;
            p2pIfaces[i] = ifaceInfo;
            return;
        }
        HwHiLog.e(TAG, false, "getP2pIface failed: %{public}s", new Object[]{statusString(status)});
    }

    private boolean getNanIfaceNamesForChip(HidlSupport.Mutable<IWifiChip> chipResp, HidlSupport.Mutable<ArrayList<String>> ifaceNamesResp) {
        MutableBoolean statusOk = new MutableBoolean(false);
        try {
            ((IWifiChip) chipResp.value).getNanIfaceNames(new IWifiChip.getNanIfaceNamesCallback(statusOk, ifaceNamesResp) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$6TVXabgwzzuOj7IJVUrIztyR2Lw */
                private final /* synthetic */ MutableBoolean f$0;
                private final /* synthetic */ HidlSupport.Mutable f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                    HwWifi2HalDeviceManager.lambda$getNanIfaceNamesForChip$12(this.f$0, this.f$1, wifiStatus, arrayList);
                }
            });
            if (!statusOk.value) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "getNanIfaceNamesForChip exception", new Object[0]);
            return false;
        }
    }

    static /* synthetic */ void lambda$getNanIfaceNamesForChip$12(MutableBoolean statusOk, HidlSupport.Mutable ifaceNamesResp, WifiStatus status, ArrayList ifnames) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            ifaceNamesResp.value = ifnames;
        } else {
            HwHiLog.e(TAG, false, "getNanIfaceNamesForChip failed: %{public}s", new Object[]{statusString(status)});
        }
    }

    private boolean getNanIfaceForName(String ifaceName, HidlSupport.Mutable<IWifiChip> chipResp, WifiIfaceInfo[] nanIfaces, MutableInt ifaceIndex) {
        HwHiLog.i(TAG, false, "getNanIfaceForName enter", new Object[0]);
        MutableBoolean statusOk = new MutableBoolean(false);
        try {
            ((IWifiChip) chipResp.value).getNanIface(ifaceName, new IWifiChip.getNanIfaceCallback(statusOk, ifaceName, nanIfaces, ifaceIndex) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$srbDiZsIGKrXcdoQbNuxOxt8jvY */
                private final /* synthetic */ MutableBoolean f$1;
                private final /* synthetic */ String f$2;
                private final /* synthetic */ HwWifi2HalDeviceManager.WifiIfaceInfo[] f$3;
                private final /* synthetic */ MutableInt f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void onValues(WifiStatus wifiStatus, IWifiNanIface iWifiNanIface) {
                    HwWifi2HalDeviceManager.this.lambda$getNanIfaceForName$13$HwWifi2HalDeviceManager(this.f$1, this.f$2, this.f$3, this.f$4, wifiStatus, iWifiNanIface);
                }
            });
            if (!statusOk.value) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "getNanIfaceForName exception", new Object[0]);
            return false;
        }
    }

    public /* synthetic */ void lambda$getNanIfaceForName$13$HwWifi2HalDeviceManager(MutableBoolean statusOk, String ifaceName, WifiIfaceInfo[] nanIfaces, MutableInt ifaceIndex, WifiStatus status, IWifiNanIface iface) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            WifiIfaceInfo ifaceInfo = new WifiIfaceInfo();
            ifaceInfo.name = ifaceName;
            ifaceInfo.iface = iface;
            int i = ifaceIndex.value;
            ifaceIndex.value = i + 1;
            nanIfaces[i] = ifaceInfo;
            return;
        }
        HwHiLog.e(TAG, false, "getNanIface failed: %{public}s", new Object[]{statusString(status)});
    }

    private WifiChipInfo[] getAllChipInfo() {
        synchronized (this.mLock) {
            if (this.mWifi == null) {
                return null;
            }
            HidlSupport.Mutable<ArrayList<Integer>> chipIdsResp = new HidlSupport.Mutable<>();
            if (!getChipIds(chipIdsResp)) {
                return null;
            }
            WifiChipInfo[] chipsInfo = new WifiChipInfo[((ArrayList) chipIdsResp.value).size()];
            int chipInfoIndex = 0;
            Iterator it = ((ArrayList) chipIdsResp.value).iterator();
            while (it.hasNext()) {
                WifiChipInfo wifiChipInfo = getWifiChipInfo((Integer) it.next());
                if (wifiChipInfo == null) {
                    return null;
                }
                chipsInfo[chipInfoIndex] = wifiChipInfo;
                chipInfoIndex++;
            }
            return chipsInfo;
        }
    }

    private WifiChipInfo getWifiChipInfo(Integer chipId) {
        WifiIfaceInfo[] staIfaces;
        WifiIfaceInfo[] apIfaces;
        WifiIfaceInfo[] p2pIfaces;
        WifiIfaceInfo[] nanIfaces;
        HidlSupport.Mutable<IWifiChip> chipResp = new HidlSupport.Mutable<>();
        if (!getChipForId(chipId.intValue(), chipResp)) {
            return null;
        }
        HidlSupport.Mutable<ArrayList<IWifiChip.ChipMode>> availableModesResp = new HidlSupport.Mutable<>();
        if (!getAvailableModesForChip(chipResp, availableModesResp)) {
            return null;
        }
        MutableBoolean currentModeValidResp = new MutableBoolean(false);
        MutableInt currentModeResp = new MutableInt(0);
        if (!getModeForChip(chipResp, currentModeValidResp, currentModeResp) || (staIfaces = getStaIfacesInfo(chipResp)) == null || (apIfaces = getApIfacesInfo(chipResp)) == null || (p2pIfaces = getP2pIfacesInfo(chipResp)) == null || (nanIfaces = getNanIfacesInfo(chipResp)) == null) {
            return null;
        }
        WifiChipInfo chipInfo = new WifiChipInfo();
        chipInfo.chip = (IWifiChip) chipResp.value;
        chipInfo.chipId = chipId.intValue();
        chipInfo.availableModes = (ArrayList) availableModesResp.value;
        chipInfo.isCurrentModeIdValid = currentModeValidResp.value;
        chipInfo.currentModeId = currentModeResp.value;
        chipInfo.ifaces[0] = staIfaces;
        chipInfo.ifaces[1] = apIfaces;
        chipInfo.ifaces[2] = p2pIfaces;
        chipInfo.ifaces[3] = nanIfaces;
        return chipInfo;
    }

    private WifiIfaceInfo[] getStaIfacesInfo(HidlSupport.Mutable<IWifiChip> chipResp) {
        HidlSupport.Mutable<ArrayList<String>> ifaceNamesResp = new HidlSupport.Mutable<>();
        if (!getStaIfaceNamesForChip(chipResp, ifaceNamesResp)) {
            return null;
        }
        MutableInt ifaceIndex = new MutableInt(0);
        WifiIfaceInfo[] staIfaces = new WifiIfaceInfo[((ArrayList) ifaceNamesResp.value).size()];
        Iterator it = ((ArrayList) ifaceNamesResp.value).iterator();
        while (it.hasNext()) {
            if (!getStaIfaceForName((String) it.next(), chipResp, staIfaces, ifaceIndex)) {
                return null;
            }
        }
        return staIfaces;
    }

    private WifiIfaceInfo[] getApIfacesInfo(HidlSupport.Mutable<IWifiChip> chipResp) {
        HidlSupport.Mutable<ArrayList<String>> ifaceNamesResp = new HidlSupport.Mutable<>();
        if (!getApIfaceNamesForChip(chipResp, ifaceNamesResp)) {
            return null;
        }
        MutableInt ifaceIndex = new MutableInt(0);
        WifiIfaceInfo[] apIfaces = new WifiIfaceInfo[((ArrayList) ifaceNamesResp.value).size()];
        Iterator it = ((ArrayList) ifaceNamesResp.value).iterator();
        while (it.hasNext()) {
            if (!getApIfaceForName((String) it.next(), chipResp, apIfaces, ifaceIndex)) {
                return null;
            }
        }
        return apIfaces;
    }

    private WifiIfaceInfo[] getP2pIfacesInfo(HidlSupport.Mutable<IWifiChip> chipResp) {
        HidlSupport.Mutable<ArrayList<String>> ifaceNamesResp = new HidlSupport.Mutable<>();
        if (!getP2pIfaceNamesForChip(chipResp, ifaceNamesResp)) {
            return null;
        }
        MutableInt ifaceIndex = new MutableInt(0);
        WifiIfaceInfo[] p2pIfaces = new WifiIfaceInfo[((ArrayList) ifaceNamesResp.value).size()];
        Iterator it = ((ArrayList) ifaceNamesResp.value).iterator();
        while (it.hasNext()) {
            if (!getP2pIfaceForName((String) it.next(), chipResp, p2pIfaces, ifaceIndex)) {
                return null;
            }
        }
        return p2pIfaces;
    }

    private WifiIfaceInfo[] getNanIfacesInfo(HidlSupport.Mutable<IWifiChip> chipResp) {
        HidlSupport.Mutable<ArrayList<String>> ifaceNamesResp = new HidlSupport.Mutable<>();
        if (!getNanIfaceNamesForChip(chipResp, ifaceNamesResp)) {
            return null;
        }
        MutableInt ifaceIndex = new MutableInt(0);
        WifiIfaceInfo[] nanIfaces = new WifiIfaceInfo[((ArrayList) ifaceNamesResp.value).size()];
        Iterator it = ((ArrayList) ifaceNamesResp.value).iterator();
        while (it.hasNext()) {
            if (!getNanIfaceForName((String) it.next(), chipResp, nanIfaces, ifaceIndex)) {
                return null;
            }
        }
        return nanIfaces;
    }

    private boolean validateInterfaceCache(WifiChipInfo[] chipInfos) {
        synchronized (this.mLock) {
            for (InterfaceCacheEntry entry : this.mInterfaceInfoCache.values()) {
                WifiChipInfo matchingChipInfo = null;
                int length = chipInfos.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    WifiChipInfo ci = chipInfos[i];
                    if (ci.chipId == entry.chipId) {
                        matchingChipInfo = ci;
                        break;
                    }
                    i++;
                }
                if (matchingChipInfo == null) {
                    HwHiLog.e(TAG, false, "validateInterfaceCache: no chip found for %{public}s", new Object[]{entry});
                    return false;
                }
                WifiIfaceInfo[] ifaceInfoList = matchingChipInfo.ifaces[entry.type];
                if (ifaceInfoList == null) {
                    HwHiLog.e(TAG, false, "validateInterfaceCache: invalid type on entry %{public}s", new Object[]{entry});
                    return false;
                }
                boolean isMatchFound = false;
                int length2 = ifaceInfoList.length;
                int i2 = 0;
                while (true) {
                    if (i2 >= length2) {
                        break;
                    } else if (ifaceInfoList[i2].name.equals(entry.name)) {
                        isMatchFound = true;
                        break;
                    } else {
                        i2++;
                    }
                }
                if (!isMatchFound) {
                    HwHiLog.e(TAG, false, "validateInterfaceCache: no interface found for %{public}s", new Object[]{entry});
                    return false;
                }
            }
            return true;
        }
    }

    private IWifiIface createIfaceIfPossible(WifiChipInfo[] chipInfos, int ifaceType, boolean isLowPriority, InterfaceDestroyedListener destroyedListener, Handler handler) {
        HwHiLog.i(TAG, false, "createIfaceIfPossible: chipInfos=%{public}s, ifaceType = %{public}d, isLowPriorit = %{public}b", new Object[]{Arrays.deepToString(chipInfos), Integer.valueOf(ifaceType), Boolean.valueOf(isLowPriority)});
        synchronized (this.mLock) {
            try {
                int length = chipInfos.length;
                IfaceCreationData bestIfaceCreationProposal = null;
                int i = 0;
                while (i < length) {
                    WifiChipInfo chipInfo = chipInfos[i];
                    Iterator<IWifiChip.ChipMode> it = chipInfo.availableModes.iterator();
                    IfaceCreationData bestIfaceCreationProposal2 = bestIfaceCreationProposal;
                    while (it.hasNext()) {
                        bestIfaceCreationProposal2 = getIfaceCreationData(ifaceType, isLowPriority, bestIfaceCreationProposal2, chipInfo, it.next());
                    }
                    i++;
                    bestIfaceCreationProposal = bestIfaceCreationProposal2;
                }
                HwHiLog.i(TAG, false, "createIfaceIfPossible: bestIfaceCreationProposal to check", new Object[0]);
                if (bestIfaceCreationProposal != null) {
                    IWifiIface iface = executeChipReconfiguration(bestIfaceCreationProposal, ifaceType);
                    if (iface != null) {
                        InterfaceCacheEntry cacheEntry = new InterfaceCacheEntry();
                        cacheEntry.chip = bestIfaceCreationProposal.chipInfo.chip;
                        cacheEntry.chipId = bestIfaceCreationProposal.chipInfo.chipId;
                        cacheEntry.name = getName(iface);
                        cacheEntry.type = ifaceType;
                        if (destroyedListener != null) {
                            try {
                                cacheEntry.destroyedListeners.add(new InterfaceDestroyedListenerProxy(cacheEntry.name, destroyedListener, handler));
                            } catch (Throwable th) {
                                th = th;
                                throw th;
                            }
                        }
                        cacheEntry.creationTime = this.mClock.getUptimeSinceBootMillis();
                        cacheEntry.isLowPriority = isLowPriority;
                        this.mInterfaceInfoCache.put(Pair.create(cacheEntry.name, Integer.valueOf(cacheEntry.type)), cacheEntry);
                        HwHiLog.i(TAG, false, "createIfaceIfPossible: added cacheEntry=%{public}s", new Object[]{cacheEntry});
                        return iface;
                    }
                }
                return null;
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    private IfaceCreationData getIfaceCreationData(int ifaceType, boolean isLowPriority, IfaceCreationData bestIfaceCreationProposal, WifiChipInfo chipInfo, IWifiChip.ChipMode chipMode) {
        IfaceCreationData tempProposal = bestIfaceCreationProposal;
        Iterator it = chipMode.availableCombinations.iterator();
        while (it.hasNext()) {
            IfaceCreationData tempProposal2 = tempProposal;
            for (int[] expandedIfaceCombo : expandIfaceCombos((IWifiChip.ChipIfaceCombination) it.next())) {
                IfaceCreationData currentProposal = canIfaceComboSupportRequest(chipInfo, chipMode, expandedIfaceCombo, ifaceType, isLowPriority);
                if (compareIfaceCreationData(currentProposal, bestIfaceCreationProposal)) {
                    tempProposal2 = currentProposal;
                }
            }
            tempProposal = tempProposal2;
        }
        return tempProposal;
    }

    private int[][] expandIfaceCombos(IWifiChip.ChipIfaceCombination chipIfaceCombo) {
        int numOfCombos = 1;
        Iterator it = chipIfaceCombo.limits.iterator();
        while (it.hasNext()) {
            IWifiChip.ChipIfaceCombinationLimit limit = (IWifiChip.ChipIfaceCombinationLimit) it.next();
            for (int i = 0; i < limit.maxIfaces; i++) {
                numOfCombos *= limit.types.size();
            }
        }
        HwHiLog.i(TAG, false, "numOfCombos = %{public}d, IFACE_TYPES_BY_PRIORITY.length = %{public}d", new Object[]{Integer.valueOf(numOfCombos), Integer.valueOf(IFACE_TYPES_BY_PRIORITY.length)});
        int[][] expandedIfaceCombos = (int[][]) Array.newInstance(int.class, numOfCombos, IFACE_TYPES_BY_PRIORITY.length);
        int span = numOfCombos;
        Iterator it2 = chipIfaceCombo.limits.iterator();
        while (it2.hasNext()) {
            IWifiChip.ChipIfaceCombinationLimit limit2 = (IWifiChip.ChipIfaceCombinationLimit) it2.next();
            for (int i2 = 0; i2 < limit2.maxIfaces; i2++) {
                span /= limit2.types.size();
                for (int k = 0; k < numOfCombos; k++) {
                    int[] iArr = expandedIfaceCombos[k];
                    int intValue = ((Integer) limit2.types.get((k / span) % limit2.types.size())).intValue();
                    iArr[intValue] = iArr[intValue] + 1;
                }
            }
        }
        return expandedIfaceCombos;
    }

    public void reset() {
        synchronized (this.mLock) {
            this.mWifi = null;
            this.mServiceManager = null;
            this.mIsReady = false;
            HwHiLog.i(TAG, false, "IWifi service reset", new Object[0]);
        }
    }

    public boolean isSupported() {
        return this.mIsVendorHalSupported;
    }

    public boolean isReady() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mIsReady;
        }
        return z;
    }

    public void stop() {
        stopWifi();
    }

    public Set<Integer> getSupportedIfaceTypes() {
        return getSupportedIfaceTypesInternal(null);
    }

    public Set<Integer> getSupportedIfaceTypes(IWifiChip chip) {
        return getSupportedIfaceTypesInternal(chip);
    }

    private Set<Integer> getSupportedIfaceTypesInternal(IWifiChip chip) {
        HwHiLog.i(TAG, false, "getSupportedIfaceTypesInternal enter", new Object[0]);
        Set<Integer> results = new HashSet<>();
        WifiChipInfo[] chipInfos = getAllChipInfo();
        if (chipInfos == null) {
            HwHiLog.e(TAG, false, "getSupportedIfaceTypesInternal: no chip info found", new Object[0]);
            return results;
        }
        MutableInt chipIdIfProvided = new MutableInt(0);
        if (chip != null) {
            MutableBoolean statusOk = new MutableBoolean(false);
            try {
                chip.getId(new IWifiChip.getIdCallback(chipIdIfProvided, statusOk) {
                    /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$CtEOM2QkzGAquX_EHi8l5gIhFBg */
                    private final /* synthetic */ MutableInt f$0;
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, int i) {
                        HwWifi2HalDeviceManager.lambda$getSupportedIfaceTypesInternal$14(this.f$0, this.f$1, wifiStatus, i);
                    }
                });
                if (!statusOk.value) {
                    return results;
                }
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "getSupportedIfaceTypesInternal IWifiChip.getId() exception err", new Object[0]);
                return results;
            }
        } else {
            HwHiLog.e(TAG, false, "getSupportedIfaceTypesInternal: chip is null", new Object[0]);
        }
        for (WifiChipInfo wci : chipInfos) {
            if (chip == null || wci.chipId == chipIdIfProvided.value) {
                Iterator<IWifiChip.ChipMode> it = wci.availableModes.iterator();
                while (it.hasNext()) {
                    addResult(results, it.next());
                }
            }
        }
        return results;
    }

    static /* synthetic */ void lambda$getSupportedIfaceTypesInternal$14(MutableInt chipIdIfProvided, MutableBoolean statusOk, WifiStatus status, int id) {
        if (status.code == 0) {
            chipIdIfProvided.value = id;
            statusOk.value = true;
            return;
        }
        HwHiLog.e(TAG, false, "IWifiChip.getId() error: %{public}s", new Object[]{statusString(status)});
        statusOk.value = false;
    }

    private void addResult(Set<Integer> results, IWifiChip.ChipMode cm) {
        Iterator it = cm.availableCombinations.iterator();
        while (it.hasNext()) {
            Iterator it2 = ((IWifiChip.ChipIfaceCombination) it.next()).limits.iterator();
            while (it2.hasNext()) {
                Iterator it3 = ((IWifiChip.ChipIfaceCombinationLimit) it2.next()).types.iterator();
                while (it3.hasNext()) {
                    results.add(Integer.valueOf(((Integer) it3.next()).intValue()));
                }
            }
        }
    }

    public boolean removeIface(IWifiIface iface) {
        if (removeIfaceInternal(iface)) {
            return true;
        }
        dispatchAllDestroyedListeners();
        return true;
    }

    private boolean removeIfaceInternal(IWifiIface iface) {
        String name = getName(iface);
        int type = getType(iface);
        HwHiLog.i(TAG, false, "removeIfaceInternal: iface(name)=%{public}s, type=%{public}d", new Object[]{name, Integer.valueOf(type)});
        if (type == -1) {
            HwHiLog.e(TAG, false, "removeIfaceInternal: can't get type -- iface(name)=%{public}s", new Object[]{name});
            return false;
        }
        synchronized (this.mLock) {
            if (this.mWifi == null) {
                HwHiLog.e(TAG, false, "removeIfaceInternal: null IWifi -- iface(name)=%{public}s", new Object[]{name});
                return false;
            }
            IWifiChip chip = getChip(iface);
            if (chip == null) {
                HwHiLog.e(TAG, false, "removeIfaceInternal: null IWifiChip -- iface(name)=%{public}s", new Object[]{name});
                return false;
            } else if (name == null) {
                HwHiLog.e(TAG, false, "removeIfaceInternal: can't get name", new Object[0]);
                return false;
            } else {
                WifiStatus status = null;
                if (type != 0) {
                    try {
                        HwHiLog.i(TAG, false, "removeIfaceInternal: invalid type=%{public}d", new Object[]{Integer.valueOf(type)});
                        return false;
                    } catch (RemoteException e) {
                        HwHiLog.e(TAG, false, "IWifiChip.removeXxxIface exception", new Object[0]);
                    }
                } else {
                    status = chip.removeStaIface(name);
                    if (status != null && status.code == 0) {
                        HwHiLog.i(TAG, false, "removeStaIface success", new Object[0]);
                    }
                    dispatchDestroyedListeners(name, type);
                    if (status != null && status.code == 0) {
                        return true;
                    }
                    HwHiLog.e(TAG, false, "IWifiChip.removeXxIface failed:%{public}s", new Object[]{statusString(status)});
                    return false;
                }
            }
        }
    }

    public static String getName(IWifiIface iface) {
        if (iface == null) {
            HwHiLog.e(TAG, false, "iface is null ", new Object[0]);
            return "<null>";
        }
        HidlSupport.Mutable<String> nameResp = new HidlSupport.Mutable<>();
        try {
            iface.getName(new IWifiIface.getNameCallback(nameResp) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$f7eYonHDcU3OK6jvWZ_fbwBlfas */
                private final /* synthetic */ HidlSupport.Mutable f$0;

                {
                    this.f$0 = r1;
                }

                public final void onValues(WifiStatus wifiStatus, String str) {
                    HwWifi2HalDeviceManager.lambda$getName$15(this.f$0, wifiStatus, str);
                }
            });
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "Exception on getName", new Object[0]);
        }
        if (nameResp.value == null) {
            HwHiLog.e(TAG, false, "getName result is null ", new Object[0]);
        }
        return (String) nameResp.value;
    }

    static /* synthetic */ void lambda$getName$15(HidlSupport.Mutable nameResp, WifiStatus status, String name) {
        if (status.code == 0) {
            nameResp.value = name;
        } else {
            HwHiLog.e(TAG, false, "Error on getName: %{public}s", new Object[]{statusString(status)});
        }
    }

    private static int getType(IWifiIface iface) {
        MutableInt typeResp = new MutableInt(-1);
        try {
            iface.getType(new IWifiIface.getTypeCallback(typeResp) {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$48OEP4G0G7w6T7DONiMBVYOK57g */
                private final /* synthetic */ MutableInt f$0;

                {
                    this.f$0 = r1;
                }

                public final void onValues(WifiStatus wifiStatus, int i) {
                    HwWifi2HalDeviceManager.lambda$getType$16(this.f$0, wifiStatus, i);
                }
            });
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "Exception on getType", new Object[0]);
        }
        return typeResp.value;
    }

    static /* synthetic */ void lambda$getType$16(MutableInt typeResp, WifiStatus status, int type) {
        if (status.code == 0) {
            typeResp.value = type;
        } else {
            HwHiLog.e(TAG, false, "Error on getType: %{public}s", new Object[]{statusString(status)});
        }
    }

    public IWifiChip getChip(IWifiIface iface) {
        String name = getName(iface);
        int type = getType(iface);
        HwHiLog.i(TAG, false, "getChip: iface(name)= %{public}s, type = %{public}d", new Object[]{name, Integer.valueOf(type)});
        synchronized (this.mLock) {
            InterfaceCacheEntry cacheEntry = this.mInterfaceInfoCache.get(Pair.create(name, Integer.valueOf(type)));
            if (cacheEntry == null) {
                HwHiLog.e(TAG, false, "getChip: no entry for iface = %{public}s", new Object[]{name});
                return null;
            }
            return cacheEntry.chip;
        }
    }

    private void dispatchDestroyedListeners(String name, int type) {
        HwHiLog.i(TAG, false, "dispatchDestroyedListeners: iface(name)=%{public}s", new Object[]{name});
        synchronized (this.mLock) {
            InterfaceCacheEntry entry = this.mInterfaceInfoCache.get(Pair.create(name, Integer.valueOf(type)));
            if (entry == null) {
                HwHiLog.e(TAG, false, "dispatchDestroyedListeners: no cache entry for iface = %{public}s", new Object[]{name});
                return;
            }
            for (InterfaceDestroyedListenerProxy listener : entry.destroyedListeners) {
                listener.trigger();
            }
            entry.destroyedListeners.clear();
            this.mInterfaceInfoCache.remove(Pair.create(name, Integer.valueOf(type)));
        }
    }

    public class InterfaceDestroyedListenerProxy extends ListenerProxy<InterfaceDestroyedListener> {
        private final String mIfaceName;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        InterfaceDestroyedListenerProxy(String ifaceName, InterfaceDestroyedListener destroyedListener, Handler handler) {
            super(destroyedListener, handler, "InterfaceDestroyedListenerProxy");
            HwWifi2HalDeviceManager.this = r2;
            this.mIfaceName = ifaceName;
        }

        @Override // com.huawei.wifi2.HwWifi2HalDeviceManager.ListenerProxy
        public void action() {
            HwHiLog.i(HwWifi2HalDeviceManager.TAG, false, "InterfaceDestroyedListenerProxy: onDestroyed for mIfaceName = %{public}s", new Object[]{this.mIfaceName});
            ((InterfaceDestroyedListener) this.mListener).onDestroyed(this.mIfaceName);
        }
    }

    private class InterfaceAvailableForRequestListenerProxy extends ListenerProxy<InterfaceAvailableForRequestListener> {
        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        InterfaceAvailableForRequestListenerProxy(InterfaceAvailableForRequestListener destroyedListener, Handler handler) {
            super(destroyedListener, handler, "InterfaceAvailableForRequestListenerProxy");
            HwWifi2HalDeviceManager.this = r2;
        }

        @Override // com.huawei.wifi2.HwWifi2HalDeviceManager.ListenerProxy
        public void actionWithArg(boolean isAvailable) {
            ((InterfaceAvailableForRequestListener) this.mListener).onAvailabilityChanged(isAvailable);
        }
    }

    public class InterfaceCacheEntry {
        protected IWifiChip chip;
        protected int chipId;
        protected long creationTime;
        protected Set<InterfaceDestroyedListenerProxy> destroyedListeners;
        protected boolean isLowPriority;
        protected String name;
        protected int type;

        private InterfaceCacheEntry() {
            HwWifi2HalDeviceManager.this = r1;
            this.destroyedListeners = new HashSet();
        }

        public String toString() {
            return "{name=" + this.name + ", type=" + this.type + ", destroyedListeners.size()=" + this.destroyedListeners.size() + ", creationTime=" + this.creationTime + ", isLowPriority=" + this.isLowPriority + "}";
        }
    }

    public class WifiIfaceInfo {
        protected IWifiIface iface;
        protected String name;

        private WifiIfaceInfo() {
            HwWifi2HalDeviceManager.this = r1;
        }

        public String toString() {
            return "{name = " + this.name + "}, iface={" + this.iface + "}";
        }
    }

    public class WifiChipInfo {
        protected ArrayList<IWifiChip.ChipMode> availableModes;
        protected IWifiChip chip;
        protected int chipId;
        protected int currentModeId;
        protected WifiIfaceInfo[][] ifaces;
        protected boolean isCurrentModeIdValid;

        private WifiChipInfo() {
            HwWifi2HalDeviceManager.this = r1;
            this.ifaces = new WifiIfaceInfo[HwWifi2HalDeviceManager.IFACE_TYPES_BY_PRIORITY.length][];
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{chipId=");
            sb.append(this.chipId);
            sb.append(", availableModes=");
            sb.append(this.availableModes);
            sb.append(", isCurrentModeIdValid=");
            sb.append(this.isCurrentModeIdValid);
            sb.append(", currentModeId=");
            sb.append(this.currentModeId);
            int[] iArr = HwWifi2HalDeviceManager.IFACE_TYPES_BY_PRIORITY;
            for (int type : iArr) {
                sb.append(", ifaces[" + type + "].length=");
                sb.append(this.ifaces.length);
            }
            sb.append(")");
            return sb.toString();
        }
    }

    private void stopWifi() {
        if (this.mWifi == null) {
            HwHiLog.e(TAG, false, "stopWifi called but mWifi is null", new Object[0]);
        } else {
            HwHiLog.i(TAG, false, "wifi2 can not stop wifi", new Object[0]);
        }
    }

    public class IfaceCreationData {
        protected WifiChipInfo chipInfo;
        protected int chipModeId;
        protected List<WifiIfaceInfo> interfacesToBeRemovedFirst;

        private IfaceCreationData() {
            HwWifi2HalDeviceManager.this = r1;
        }

        public String toString() {
            return "{chipInfo = " + this.chipInfo + "}, chipModeId=" + this.chipModeId + ", interfacesToBeRemovedFirst= {" + this.interfacesToBeRemovedFirst + "}";
        }
    }

    private IfaceCreationData canIfaceComboSupportRequest(WifiChipInfo chipInfo, IWifiChip.ChipMode chipMode, int[] chipIfaceCombo, int ifaceType, boolean isLowPriority) {
        if (chipIfaceCombo[ifaceType] == 0) {
            HwHiLog.e(TAG, false, "Requested type not supported by combo", new Object[0]);
            return null;
        }
        if (chipInfo.isCurrentModeIdValid && chipInfo.currentModeId != chipMode.id) {
            for (int type : IFACE_TYPES_BY_PRIORITY) {
                if (handleChipInfo(chipInfo, ifaceType, isLowPriority, type)) {
                    return null;
                }
            }
            IfaceCreationData ifaceCreationData = new IfaceCreationData();
            ifaceCreationData.chipInfo = chipInfo;
            ifaceCreationData.chipModeId = chipMode.id;
            return ifaceCreationData;
        }
        List<WifiIfaceInfo> arrayList = new ArrayList<>();
        int[] iArr = IFACE_TYPES_BY_PRIORITY;
        List<WifiIfaceInfo> interfacesToBeRemovedFirst = arrayList;
        for (int type2 : iArr) {
            int tooManyInterfaces = chipInfo.ifaces[type2].length - chipIfaceCombo[type2];
            if (type2 == ifaceType) {
                tooManyInterfaces++;
            }
            if (tooManyInterfaces > 0) {
                if (isLowPriority) {
                    return null;
                }
                if (!allowedToDeleteIfaceTypeForRequestedType(type2, ifaceType, chipInfo.ifaces, tooManyInterfaces)) {
                    HwHiLog.e(TAG, false, "Would need to delete some higher priority interfaces", new Object[0]);
                    return null;
                }
                interfacesToBeRemovedFirst = selectInterfacesToDelete(tooManyInterfaces, chipInfo.ifaces[type2]);
            }
        }
        IfaceCreationData ifaceCreationData2 = new IfaceCreationData();
        ifaceCreationData2.chipInfo = chipInfo;
        ifaceCreationData2.chipModeId = chipMode.id;
        ifaceCreationData2.interfacesToBeRemovedFirst = interfacesToBeRemovedFirst;
        return ifaceCreationData2;
    }

    private boolean handleChipInfo(WifiChipInfo chipInfo, int ifaceType, boolean isLowPriority, int type) {
        if (chipInfo.ifaces[type].length != 0) {
            if (isLowPriority) {
                HwHiLog.e(TAG, false, "isLowPriority for request type = %{public}d", new Object[]{Integer.valueOf(type)});
                return true;
            } else if (!allowedToDeleteIfaceTypeForRequestedType(type, ifaceType, chipInfo.ifaces, chipInfo.ifaces[type].length)) {
                HwHiLog.e(TAG, false, "no need to delete request type = %{public}d", new Object[]{Integer.valueOf(type)});
                return true;
            }
        }
        return false;
    }

    private boolean compareIfaceCreationData(IfaceCreationData val1, IfaceCreationData val2) {
        int numIfacesToDelete1;
        int numIfacesToDelete2;
        HwHiLog.i(TAG, false, "compareIfaceCreationData: val1=%{public}s, val2=%{public}s", new Object[]{val1, val2});
        if (val1 == null) {
            return false;
        }
        if (val2 == null) {
            return true;
        }
        int[] iArr = IFACE_TYPES_BY_PRIORITY;
        if (iArr.length > 0) {
            int type = iArr[0];
            if (!val1.chipInfo.isCurrentModeIdValid || val1.chipInfo.currentModeId == val1.chipModeId) {
                numIfacesToDelete1 = val1.interfacesToBeRemovedFirst.size();
            } else {
                numIfacesToDelete1 = val1.chipInfo.ifaces[type].length;
            }
            if (!val2.chipInfo.isCurrentModeIdValid || val2.chipInfo.currentModeId == val2.chipModeId) {
                numIfacesToDelete2 = val2.interfacesToBeRemovedFirst.size();
            } else {
                numIfacesToDelete2 = val2.chipInfo.ifaces[type].length;
            }
            HwHiLog.i(TAG, false, "decision based on type=%{public}d, numIfacesToDelete1 =%{public}d, numIfacesToDelete2 = %{public}d", new Object[]{Integer.valueOf(type), Integer.valueOf(numIfacesToDelete1), Integer.valueOf(numIfacesToDelete2)});
            return true;
        }
        HwHiLog.i(TAG, false, "proposals identical - flip coin", new Object[0]);
        return false;
    }

    private boolean allowedToDeleteIfaceTypeForRequestedType(int existingIfaceType, int requestedIfaceType, WifiIfaceInfo[][] currentIfaces, int numNecessaryInterfaces) {
        HwHiLog.i(TAG, false, "allowedToDeleteIfaceTypeForRequestedType enter, numNecessaryInterfaces = %{public}d", new Object[]{Integer.valueOf(numNecessaryInterfaces)});
        int numAvailableLowPriorityInterfaces = 0;
        for (InterfaceCacheEntry entry : this.mInterfaceInfoCache.values()) {
            if (entry.type == existingIfaceType && entry.isLowPriority) {
                numAvailableLowPriorityInterfaces++;
            }
        }
        HwHiLog.i(TAG, false, "allowedToDeleteIfaceTypeForRequestedType, LowPriorityInterfaces num = %{public}d", new Object[]{Integer.valueOf(numAvailableLowPriorityInterfaces)});
        if (numAvailableLowPriorityInterfaces >= numNecessaryInterfaces) {
            return true;
        }
        if (existingIfaceType == requestedIfaceType || currentIfaces[requestedIfaceType].length != 0) {
            return false;
        }
        if (currentIfaces[existingIfaceType].length > 1) {
            return true;
        }
        if (requestedIfaceType == 3) {
            return false;
        }
        if (requestedIfaceType != 2 || existingIfaceType == 3) {
            return true;
        }
        return false;
    }

    private List<WifiIfaceInfo> selectInterfacesToDelete(int excessInterfaces, WifiIfaceInfo[] interfaces) {
        HwHiLog.i(TAG, false, "selectInterfacesToDelete: excessInterfaces=%{public}d, interfaces=%{public}s", new Object[]{Integer.valueOf(excessInterfaces), Arrays.toString(interfaces)});
        boolean isLookupError = false;
        LongSparseArray<WifiIfaceInfo> orderedListLowPriority = new LongSparseArray<>();
        LongSparseArray<WifiIfaceInfo> orderedList = new LongSparseArray<>();
        int length = interfaces.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            WifiIfaceInfo info = interfaces[i];
            InterfaceCacheEntry cacheEntry = this.mInterfaceInfoCache.get(Pair.create(info.name, Integer.valueOf(getType(info.iface))));
            if (cacheEntry == null) {
                HwHiLog.e(TAG, false, "selectInterfacesToDelete: can't find cache entry with name=%{public}s", new Object[]{info.name});
                isLookupError = true;
                break;
            }
            if (cacheEntry.isLowPriority) {
                orderedListLowPriority.append(cacheEntry.creationTime, info);
            } else {
                orderedList.append(cacheEntry.creationTime, info);
            }
            i++;
        }
        if (isLookupError) {
            HwHiLog.e(TAG, false, "selectInterfacesToDelete: falling back to arbitrary selection", new Object[0]);
            return Arrays.asList((WifiIfaceInfo[]) Arrays.copyOf(interfaces, excessInterfaces));
        }
        List<WifiIfaceInfo> result = new ArrayList<>(excessInterfaces);
        for (int i2 = 0; i2 < excessInterfaces; i2++) {
            int lowPriorityNextIndex = (orderedListLowPriority.size() - i2) - 1;
            if (lowPriorityNextIndex >= 0) {
                result.add(orderedListLowPriority.valueAt(lowPriorityNextIndex));
            } else {
                result.add(orderedList.valueAt(((orderedList.size() - i2) + orderedListLowPriority.size()) - 1));
            }
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0041 A[Catch:{ RemoteException -> 0x00c1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x007d  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00a3  */
    private IWifiIface executeChipReconfiguration(IfaceCreationData ifaceCreationData, int ifaceType) {
        boolean isModeConfigNeeded;
        HwHiLog.i(TAG, false, "ifaceCreationData=%{public}s, ifaceType=%{public}d", new Object[]{ifaceCreationData, Integer.valueOf(ifaceType)});
        synchronized (this.mLock) {
            try {
                if (ifaceCreationData.chipInfo.isCurrentModeIdValid) {
                    if (ifaceCreationData.chipInfo.currentModeId == ifaceCreationData.chipModeId) {
                        isModeConfigNeeded = false;
                        HwHiLog.i(TAG, false, "NeedModeConfig=%{public}b, faceType=%{public}d", new Object[]{Boolean.valueOf(isModeConfigNeeded), Integer.valueOf(ifaceType)});
                        if (!isModeConfigNeeded) {
                            WifiIfaceInfo[][] wifiIfaceInfoArr = ifaceCreationData.chipInfo.ifaces;
                            for (WifiIfaceInfo[] ifaceInfos : wifiIfaceInfoArr) {
                                for (WifiIfaceInfo ifaceInfo : ifaceInfos) {
                                    removeIfaceInternal(ifaceInfo.iface);
                                }
                            }
                            WifiStatus status = ifaceCreationData.chipInfo.chip.configureChip(ifaceCreationData.chipModeId);
                            if (status.code != 0) {
                                HwHiLog.e(TAG, false, "configureChip error: %{public}s", new Object[]{statusString(status)});
                                return null;
                            }
                        } else {
                            for (WifiIfaceInfo ifaceInfo2 : ifaceCreationData.interfacesToBeRemovedFirst) {
                                removeIfaceInternal(ifaceInfo2.iface);
                            }
                        }
                        HidlSupport.Mutable<WifiStatus> statusResp = new HidlSupport.Mutable<>();
                        HidlSupport.Mutable<IWifiIface> ifaceResp = new HidlSupport.Mutable<>();
                        if (ifaceType == 0) {
                            HwHiLog.e(TAG, false, "executeChipReconfiguration: start createStaIface", new Object[0]);
                            ifaceCreationData.chipInfo.chip.createStaIface(new IWifiChip.createStaIfaceCallback(statusResp, ifaceResp) {
                                /* class com.huawei.wifi2.$$Lambda$HwWifi2HalDeviceManager$g_ExLL59MMEQDUNcmGPN_GtUhCA */
                                private final /* synthetic */ HidlSupport.Mutable f$0;
                                private final /* synthetic */ HidlSupport.Mutable f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                public final void onValues(WifiStatus wifiStatus, IWifiStaIface iWifiStaIface) {
                                    HwWifi2HalDeviceManager.lambda$executeChipReconfiguration$17(this.f$0, this.f$1, wifiStatus, iWifiStaIface);
                                }
                            });
                        }
                        return handleStatusResp(ifaceType, statusResp, ifaceResp);
                    }
                }
                isModeConfigNeeded = true;
                HwHiLog.i(TAG, false, "NeedModeConfig=%{public}b, faceType=%{public}d", new Object[]{Boolean.valueOf(isModeConfigNeeded), Integer.valueOf(ifaceType)});
                if (!isModeConfigNeeded) {
                }
                HidlSupport.Mutable<WifiStatus> statusResp2 = new HidlSupport.Mutable<>();
                HidlSupport.Mutable<IWifiIface> ifaceResp2 = new HidlSupport.Mutable<>();
                if (ifaceType == 0) {
                }
                return handleStatusResp(ifaceType, statusResp2, ifaceResp2);
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "executeChipReconfiguration: exception", new Object[0]);
                return null;
            }
        }
    }

    static /* synthetic */ void lambda$executeChipReconfiguration$17(HidlSupport.Mutable statusResp, HidlSupport.Mutable ifaceResp, WifiStatus status, IWifiStaIface iface) {
        statusResp.value = status;
        ifaceResp.value = iface;
    }

    private IWifiIface handleStatusResp(int ifaceType, HidlSupport.Mutable<WifiStatus> statusResp, HidlSupport.Mutable<IWifiIface> ifaceResp) {
        if (((WifiStatus) statusResp.value).code != 0) {
            HwHiLog.e(TAG, false, "failed create interface ifaceType=%{public}d", new Object[]{Integer.valueOf(ifaceType)});
            return null;
        }
        HwHiLog.i(TAG, false, "executeChipReconfiguration: wifi2 create sta success", new Object[0]);
        return (IWifiIface) ifaceResp.value;
    }

    public static String statusString(WifiStatus status) {
        if (status == null) {
            return "status=null";
        }
        return status.code + " (" + status.description + ")";
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("HwWifi2HalDeviceManager:");
        pw.println("  mServiceManager: " + this.mServiceManager);
        pw.println("  mWifi: " + this.mWifi);
        pw.println("  mManagerStatusListeners: " + this.mManagerStatusListeners);
        pw.println("  mInterfaceInfoCache: " + this.mInterfaceInfoCache);
    }
}
