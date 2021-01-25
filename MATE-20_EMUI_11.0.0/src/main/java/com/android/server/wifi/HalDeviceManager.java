package com.android.server.wifi;

import android.hardware.wifi.V1_0.IWifi;
import android.hardware.wifi.V1_0.IWifiApIface;
import android.hardware.wifi.V1_0.IWifiChip;
import android.hardware.wifi.V1_0.IWifiChipEventCallback;
import android.hardware.wifi.V1_0.IWifiEventCallback;
import android.hardware.wifi.V1_0.IWifiIface;
import android.hardware.wifi.V1_0.IWifiNanIface;
import android.hardware.wifi.V1_0.IWifiP2pIface;
import android.hardware.wifi.V1_0.IWifiRttController;
import android.hardware.wifi.V1_0.IWifiStaIface;
import android.hardware.wifi.V1_0.WifiStatus;
import android.hidl.manager.V1_0.IServiceNotification;
import android.hidl.manager.V1_2.IServiceManager;
import android.os.Handler;
import android.os.HidlSupport;
import android.os.IHwBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.MutableBoolean;
import android.util.MutableInt;
import android.util.Pair;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.HalDeviceManager;
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

public class HalDeviceManager {
    private static final int[] IFACE_TYPES_BY_PRIORITY = {1, 0, 2, 3};
    private static final int START_HAL_RETRY_INTERVAL_MS = 20;
    @VisibleForTesting
    public static final int START_HAL_RETRY_TIMES = 3;
    private static final String TAG = "HalDevMgr";
    private static final boolean VDBG = false;
    private final Clock mClock;
    private boolean mDbg = false;
    private final SparseArray<IWifiChipEventCallback.Stub> mDebugCallbacks = new SparseArray<>();
    private final IHwBinder.DeathRecipient mIWifiDeathRecipient = new IHwBinder.DeathRecipient() {
        /* class com.android.server.wifi.$$Lambda$HalDeviceManager$jNAzj5YlVhwJm5NjZ6HiKskQStI */

        public final void serviceDied(long j) {
            HalDeviceManager.this.lambda$new$2$HalDeviceManager(j);
        }
    };
    private IWifiRttController mIWifiRttController;
    private final SparseArray<Map<InterfaceAvailableForRequestListenerProxy, Boolean>> mInterfaceAvailableForRequestListeners = new SparseArray<>();
    private final Map<Pair<String, Integer>, InterfaceCacheEntry> mInterfaceInfoCache = new HashMap();
    private boolean mIsReady;
    private boolean mIsVendorHalSupported = false;
    private final Object mLock = new Object();
    private final Set<ManagerStatusListenerProxy> mManagerStatusListeners = new HashSet();
    private final Set<InterfaceRttControllerLifecycleCallbackProxy> mRttControllerLifecycleCallbacks = new HashSet();
    private IServiceManager mServiceManager;
    private final IHwBinder.DeathRecipient mServiceManagerDeathRecipient = new IHwBinder.DeathRecipient() {
        /* class com.android.server.wifi.$$Lambda$HalDeviceManager$SeJCUxQL5U06WtkK8XwQet85g */

        public final void serviceDied(long j) {
            HalDeviceManager.this.lambda$new$1$HalDeviceManager(j);
        }
    };
    private final IServiceNotification mServiceNotificationCallback = new IServiceNotification.Stub() {
        /* class com.android.server.wifi.HalDeviceManager.AnonymousClass1 */

        public void onRegistration(String fqName, String name, boolean preexisting) {
            Log.d(HalDeviceManager.TAG, "IWifi registration notification: fqName=" + fqName + ", name=" + name + ", preexisting=" + preexisting);
            synchronized (HalDeviceManager.this.mLock) {
                HalDeviceManager.this.initIWifiIfNecessary();
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

    public interface InterfaceRttControllerLifecycleCallback {
        void onNewRttController(IWifiRttController iWifiRttController);

        void onRttControllerDestroyed();
    }

    public interface ManagerStatusListener {
        void onStatusChanged();
    }

    public HalDeviceManager(Clock clock) {
        this.mClock = clock;
        this.mInterfaceAvailableForRequestListeners.put(0, new HashMap());
        this.mInterfaceAvailableForRequestListeners.put(1, new HashMap());
        this.mInterfaceAvailableForRequestListeners.put(2, new HashMap());
        this.mInterfaceAvailableForRequestListeners.put(3, new HashMap());
    }

    /* access modifiers changed from: package-private */
    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mDbg = true;
        } else {
            this.mDbg = false;
        }
    }

    public void initialize() {
        initializeInternal();
    }

    public void reset() {
        synchronized (this.mLock) {
            this.mWifi = null;
            this.mServiceManager = null;
            this.mIsReady = false;
            Log.w(TAG, "IWifi service reset");
        }
    }

    public void registerStatusListener(ManagerStatusListener listener, Handler handler) {
        synchronized (this.mLock) {
            if (!this.mManagerStatusListeners.add(new ManagerStatusListenerProxy(listener, handler))) {
                Log.w(TAG, "registerStatusListener: duplicate registration ignored");
            }
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

    public boolean isStarted() {
        return isWifiStarted();
    }

    public boolean start() {
        return startWifi();
    }

    public void stop() {
        stopWifi();
        this.mWifi = null;
    }

    public Set<Integer> getSupportedIfaceTypes() {
        return getSupportedIfaceTypesInternal(null);
    }

    public Set<Integer> getSupportedIfaceTypes(IWifiChip chip) {
        return getSupportedIfaceTypesInternal(chip);
    }

    public IWifiStaIface createStaIface(boolean lowPrioritySta, InterfaceDestroyedListener destroyedListener, Handler handler) {
        return (IWifiStaIface) createIface(0, lowPrioritySta, destroyedListener, handler);
    }

    public IWifiApIface createApIface(InterfaceDestroyedListener destroyedListener, Handler handler) {
        return (IWifiApIface) createIface(1, false, destroyedListener, handler);
    }

    public IWifiP2pIface createP2pIface(InterfaceDestroyedListener destroyedListener, Handler handler) {
        return (IWifiP2pIface) createIface(2, false, destroyedListener, handler);
    }

    public IWifiNanIface createNanIface(InterfaceDestroyedListener destroyedListener, Handler handler) {
        return (IWifiNanIface) createIface(3, false, destroyedListener, handler);
    }

    public boolean removeIface(IWifiIface iface) {
        boolean success = removeIfaceInternal(iface);
        dispatchAvailableForRequestListeners();
        return success;
    }

    public IWifiChip getChip(IWifiIface iface) {
        String name = getName(iface);
        int type = getType(iface);
        synchronized (this.mLock) {
            InterfaceCacheEntry cacheEntry = this.mInterfaceInfoCache.get(Pair.create(name, Integer.valueOf(type)));
            if (cacheEntry == null) {
                Log.e(TAG, "getChip: no entry for iface(name)=" + name);
                return null;
            }
            return cacheEntry.chip;
        }
    }

    public boolean registerDestroyedListener(IWifiIface iface, InterfaceDestroyedListener destroyedListener, Handler handler) {
        String name = getName(iface);
        int type = getType(iface);
        synchronized (this.mLock) {
            InterfaceCacheEntry cacheEntry = this.mInterfaceInfoCache.get(Pair.create(name, Integer.valueOf(type)));
            if (cacheEntry == null) {
                Log.e(TAG, "registerDestroyedListener: no entry for iface(name)=" + name);
                return false;
            }
            return cacheEntry.destroyedListeners.add(new InterfaceDestroyedListenerProxy(name, destroyedListener, handler));
        }
    }

    public void registerInterfaceAvailableForRequestListener(int ifaceType, InterfaceAvailableForRequestListener listener, Handler handler) {
        synchronized (this.mLock) {
            InterfaceAvailableForRequestListenerProxy proxy = new InterfaceAvailableForRequestListenerProxy(listener, handler);
            if (!this.mInterfaceAvailableForRequestListeners.get(ifaceType).containsKey(proxy)) {
                this.mInterfaceAvailableForRequestListeners.get(ifaceType).put(proxy, null);
            } else {
                return;
            }
        }
        WifiChipInfo[] chipInfos = getAllChipInfo();
        if (chipInfos == null) {
            Log.e(TAG, "registerInterfaceAvailableForRequestListener: no chip info found - but possibly registered pre-started - ignoring");
        } else {
            dispatchAvailableForRequestListenersForType(ifaceType, chipInfos);
        }
    }

    public void unregisterInterfaceAvailableForRequestListener(int ifaceType, InterfaceAvailableForRequestListener listener) {
        synchronized (this.mLock) {
            this.mInterfaceAvailableForRequestListeners.get(ifaceType).remove(new InterfaceAvailableForRequestListenerProxy(listener, null));
        }
    }

    public void registerRttControllerLifecycleCallback(InterfaceRttControllerLifecycleCallback callback, Handler handler) {
        if (callback == null || handler == null) {
            Log.wtf(TAG, "registerRttControllerLifecycleCallback with nulls!? callback=" + callback + ", handler=" + handler);
            return;
        }
        synchronized (this.mLock) {
            InterfaceRttControllerLifecycleCallbackProxy proxy = new InterfaceRttControllerLifecycleCallbackProxy(callback, handler);
            if (!this.mRttControllerLifecycleCallbacks.add(proxy)) {
                Log.d(TAG, "registerRttControllerLifecycleCallback: registering an existing callback=" + callback);
                return;
            }
            if (this.mIWifiRttController == null) {
                this.mIWifiRttController = createRttControllerIfPossible();
            }
            if (this.mIWifiRttController != null) {
                proxy.onNewRttController(this.mIWifiRttController);
            }
        }
    }

    public static String getName(IWifiIface iface) {
        if (iface == null) {
            return "<null>";
        }
        HidlSupport.Mutable<String> nameResp = new HidlSupport.Mutable<>();
        try {
            iface.getName(new IWifiIface.getNameCallback(nameResp) {
                /* class com.android.server.wifi.$$Lambda$HalDeviceManager$bTmsDoAj9faJCBOTeT1Q3Ww5yNM */
                private final /* synthetic */ HidlSupport.Mutable f$0;

                {
                    this.f$0 = r1;
                }

                @Override // android.hardware.wifi.V1_0.IWifiIface.getNameCallback
                public final void onValues(WifiStatus wifiStatus, String str) {
                    HalDeviceManager.lambda$getName$0(this.f$0, wifiStatus, str);
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Exception on getName: " + e);
        }
        return (String) nameResp.value;
    }

    static /* synthetic */ void lambda$getName$0(HidlSupport.Mutable nameResp, WifiStatus status, String name) {
        if (status.code == 0) {
            nameResp.value = name;
            return;
        }
        Log.e(TAG, "Error on getName: " + statusString(status));
    }

    /* access modifiers changed from: private */
    public class InterfaceCacheEntry {
        public IWifiChip chip;
        public int chipId;
        public long creationTime;
        public Set<InterfaceDestroyedListenerProxy> destroyedListeners;
        public boolean isLowPriority;
        public String name;
        public int type;

        private InterfaceCacheEntry() {
            this.destroyedListeners = new HashSet();
        }

        public String toString() {
            return "{name=" + this.name + ", type=" + this.type + ", destroyedListeners.size()=" + this.destroyedListeners.size() + ", creationTime=" + this.creationTime + ", isLowPriority=" + this.isLowPriority + "}";
        }
    }

    /* access modifiers changed from: private */
    public class WifiIfaceInfo {
        public IWifiIface iface;
        public String name;

        private WifiIfaceInfo() {
        }
    }

    /* access modifiers changed from: private */
    public class WifiChipInfo {
        public ArrayList<IWifiChip.ChipMode> availableModes;
        public IWifiChip chip;
        public int chipId;
        public int currentModeId;
        public boolean currentModeIdValid;
        public WifiIfaceInfo[][] ifaces;

        private WifiChipInfo() {
            this.ifaces = new WifiIfaceInfo[HalDeviceManager.IFACE_TYPES_BY_PRIORITY.length][];
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{chipId=");
            sb.append(this.chipId);
            sb.append(", availableModes=");
            sb.append(this.availableModes);
            sb.append(", currentModeIdValid=");
            sb.append(this.currentModeIdValid);
            sb.append(", currentModeId=");
            sb.append(this.currentModeId);
            int[] iArr = HalDeviceManager.IFACE_TYPES_BY_PRIORITY;
            for (int type : iArr) {
                sb.append(", ifaces[" + type + "].length=");
                sb.append(this.ifaces[type].length);
            }
            sb.append(")");
            return sb.toString();
        }
    }

    /* access modifiers changed from: protected */
    public IWifi getWifiServiceMockable() {
        try {
            return IWifi.getService(true);
        } catch (RemoteException e) {
            Log.e(TAG, "Exception getting IWifi service: " + e);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public IServiceManager getServiceManagerMockable() {
        try {
            return IServiceManager.getService();
        } catch (RemoteException e) {
            Log.e(TAG, "Exception getting IServiceManager: " + e);
            return null;
        }
    }

    private void initializeInternal() {
        initIServiceManagerIfNecessary();
        if (this.mIsVendorHalSupported) {
            initIWifiIfNecessary();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void teardownInternal() {
        managerStatusListenerDispatch();
        dispatchAllDestroyedListeners();
        this.mInterfaceAvailableForRequestListeners.get(0).clear();
        this.mInterfaceAvailableForRequestListeners.get(1).clear();
        this.mInterfaceAvailableForRequestListeners.get(2).clear();
        this.mInterfaceAvailableForRequestListeners.get(3).clear();
        this.mIWifiRttController = null;
        dispatchRttControllerLifecycleOnDestroyed();
        this.mRttControllerLifecycleCallbacks.clear();
    }

    public /* synthetic */ void lambda$new$1$HalDeviceManager(long cookie) {
        Log.wtf(TAG, "IServiceManager died: cookie=" + cookie);
        synchronized (this.mLock) {
            this.mServiceManager = null;
        }
    }

    private void initIServiceManagerIfNecessary() {
        if (this.mDbg) {
            Log.d(TAG, "initIServiceManagerIfNecessary");
        }
        synchronized (this.mLock) {
            if (this.mServiceManager == null) {
                this.mServiceManager = getServiceManagerMockable();
                if (this.mServiceManager == null) {
                    Log.wtf(TAG, "Failed to get IServiceManager instance");
                } else {
                    try {
                        if (!this.mServiceManager.linkToDeath(this.mServiceManagerDeathRecipient, 0)) {
                            Log.wtf(TAG, "Error on linkToDeath on IServiceManager");
                            this.mServiceManager = null;
                            return;
                        }
                        if (!this.mServiceManager.registerForNotifications(IWifi.kInterfaceName, "", this.mServiceNotificationCallback)) {
                            Log.wtf(TAG, "Failed to register a listener for IWifi service");
                            this.mServiceManager = null;
                        }
                        this.mIsVendorHalSupported = isSupportedInternal();
                    } catch (RemoteException e) {
                        Log.wtf(TAG, "Exception while operating on IServiceManager: " + e);
                        this.mServiceManager = null;
                    }
                }
            }
        }
    }

    private boolean isSupportedInternal() {
        synchronized (this.mLock) {
            boolean z = false;
            if (this.mServiceManager == null) {
                Log.e(TAG, "isSupported: called but mServiceManager is null!?");
                return false;
            }
            try {
                if (!this.mServiceManager.listManifestByInterface(IWifi.kInterfaceName).isEmpty()) {
                    z = true;
                }
                return z;
            } catch (RemoteException e) {
                Log.wtf(TAG, "Exception while operating on IServiceManager: " + e);
                return false;
            }
        }
    }

    public /* synthetic */ void lambda$new$2$HalDeviceManager(long cookie) {
        Log.e(TAG, "IWifi HAL service died! Have a listener for it ... cookie=" + cookie);
        synchronized (this.mLock) {
            this.mWifi = null;
            this.mIsReady = false;
            teardownInternal();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initIWifiIfNecessary() {
        if (this.mDbg) {
            Log.d(TAG, "initIWifiIfNecessary");
        }
        synchronized (this.mLock) {
            if (this.mWifi == null) {
                try {
                    this.mWifi = getWifiServiceMockable();
                    if (this.mWifi == null) {
                        Log.e(TAG, "IWifi not (yet) available - but have a listener for it ...");
                    } else if (!this.mWifi.linkToDeath(this.mIWifiDeathRecipient, 0)) {
                        Log.e(TAG, "Error on linkToDeath on IWifi - will retry later");
                    } else {
                        WifiStatus status = this.mWifi.registerEventCallback(this.mWifiEventCallback);
                        if (status.code != 0) {
                            Log.e(TAG, "IWifi.registerEventCallback failed: " + statusString(status));
                            this.mWifi = null;
                            return;
                        }
                        stopWifi();
                        this.mIsReady = true;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Exception while operating on IWifi: " + e);
                }
            }
        }
    }

    private void initIWifiChipDebugListeners() {
    }

    private static /* synthetic */ void lambda$initIWifiChipDebugListeners$3(MutableBoolean statusOk, HidlSupport.Mutable chipIdsResp, WifiStatus status, ArrayList chipIds) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            chipIdsResp.value = chipIds;
            return;
        }
        Log.e(TAG, "getChipIds failed: " + statusString(status));
    }

    private static /* synthetic */ void lambda$initIWifiChipDebugListeners$4(MutableBoolean statusOk, HidlSupport.Mutable chipResp, WifiStatus status, IWifiChip chip) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            chipResp.value = chip;
            return;
        }
        Log.e(TAG, "getChip failed: " + statusString(status));
    }

    /* JADX WARN: Type inference failed for: r10v0 */
    /* JADX WARN: Type inference failed for: r10v1, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r10v10 */
    /* JADX WARNING: Unknown variable types count: 1 */
    private WifiChipInfo[] getAllChipInfo() {
        synchronized (this.mLock) {
            WifiChipInfo[] wifiChipInfoArr = null;
            if (this.mWifi == null) {
                Log.e(TAG, "getAllChipInfo: called but mWifi is null!?");
                return null;
            }
            try {
                ?? r10 = 0;
                MutableBoolean statusOk = new MutableBoolean(false);
                HidlSupport.Mutable<ArrayList<Integer>> chipIdsResp = new HidlSupport.Mutable<>();
                this.mWifi.getChipIds(new IWifi.getChipIdsCallback(statusOk, chipIdsResp) {
                    /* class com.android.server.wifi.$$Lambda$HalDeviceManager$J6ItBAiUMCzTjCuPD7lYSuJSIGU */
                    private final /* synthetic */ MutableBoolean f$0;
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.wifi.V1_0.IWifi.getChipIdsCallback
                    public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                        HalDeviceManager.lambda$getAllChipInfo$5(this.f$0, this.f$1, wifiStatus, arrayList);
                    }
                });
                if (!statusOk.value) {
                    return null;
                }
                if (((ArrayList) chipIdsResp.value).size() == 0) {
                    Log.e(TAG, "Should have at least 1 chip!");
                    return null;
                }
                WifiChipInfo[] chipsInfo = new WifiChipInfo[((ArrayList) chipIdsResp.value).size()];
                HidlSupport.Mutable<IWifiChip> chipResp = new HidlSupport.Mutable<>();
                HidlSupport.Mutable<ArrayList<String>> ifaceNamesResp = ((ArrayList) chipIdsResp.value).iterator();
                int chipInfoIndex = 0;
                while (ifaceNamesResp.hasNext()) {
                    Integer chipId = (Integer) ifaceNamesResp.next();
                    this.mWifi.getChip(chipId.intValue(), new IWifi.getChipCallback(statusOk, chipResp) {
                        /* class com.android.server.wifi.$$Lambda$HalDeviceManager$J_rq9pD25U5x_bgsE9o4bIVv6Rs */
                        private final /* synthetic */ MutableBoolean f$0;
                        private final /* synthetic */ HidlSupport.Mutable f$1;

                        {
                            this.f$0 = r1;
                            this.f$1 = r2;
                        }

                        @Override // android.hardware.wifi.V1_0.IWifi.getChipCallback
                        public final void onValues(WifiStatus wifiStatus, IWifiChip iWifiChip) {
                            HalDeviceManager.lambda$getAllChipInfo$6(this.f$0, this.f$1, wifiStatus, iWifiChip);
                        }
                    });
                    if (statusOk.value) {
                        if (chipResp.value != null) {
                            HidlSupport.Mutable<ArrayList<IWifiChip.ChipMode>> availableModesResp = new HidlSupport.Mutable<>();
                            ((IWifiChip) chipResp.value).getAvailableModes(new IWifiChip.getAvailableModesCallback(statusOk, availableModesResp) {
                                /* class com.android.server.wifi.$$Lambda$HalDeviceManager$GV5vgwCMeGctE92pSijMSntP7M */
                                private final /* synthetic */ MutableBoolean f$0;
                                private final /* synthetic */ HidlSupport.Mutable f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                @Override // android.hardware.wifi.V1_0.IWifiChip.getAvailableModesCallback
                                public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                                    HalDeviceManager.lambda$getAllChipInfo$7(this.f$0, this.f$1, wifiStatus, arrayList);
                                }
                            });
                            if (!statusOk.value) {
                                return wifiChipInfoArr;
                            }
                            MutableBoolean currentModeValidResp = new MutableBoolean(r10);
                            int i = r10 == true ? 1 : 0;
                            int i2 = r10 == true ? 1 : 0;
                            int i3 = r10 == true ? 1 : 0;
                            MutableInt currentModeResp = new MutableInt(i);
                            ((IWifiChip) chipResp.value).getMode(new IWifiChip.getModeCallback(statusOk, currentModeValidResp, currentModeResp) {
                                /* class com.android.server.wifi.$$Lambda$HalDeviceManager$kKpeJmcjHk7E6pKqNwVTgOU76EA */
                                private final /* synthetic */ MutableBoolean f$0;
                                private final /* synthetic */ MutableBoolean f$1;
                                private final /* synthetic */ MutableInt f$2;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                    this.f$2 = r3;
                                }

                                @Override // android.hardware.wifi.V1_0.IWifiChip.getModeCallback
                                public final void onValues(WifiStatus wifiStatus, int i) {
                                    HalDeviceManager.lambda$getAllChipInfo$8(this.f$0, this.f$1, this.f$2, wifiStatus, i);
                                }
                            });
                            if (!statusOk.value) {
                                return wifiChipInfoArr;
                            }
                            HidlSupport.Mutable<ArrayList<String>> ifaceNamesResp2 = new HidlSupport.Mutable<>();
                            MutableInt ifaceIndex = new MutableInt(r10);
                            ((IWifiChip) chipResp.value).getStaIfaceNames(new IWifiChip.getStaIfaceNamesCallback(statusOk, ifaceNamesResp2) {
                                /* class com.android.server.wifi.$$Lambda$HalDeviceManager$WPvAGl6UabBR2Zo5mjPN7Pljlo */
                                private final /* synthetic */ MutableBoolean f$0;
                                private final /* synthetic */ HidlSupport.Mutable f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                @Override // android.hardware.wifi.V1_0.IWifiChip.getStaIfaceNamesCallback
                                public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                                    HalDeviceManager.lambda$getAllChipInfo$9(this.f$0, this.f$1, wifiStatus, arrayList);
                                }
                            });
                            if (!statusOk.value) {
                                return wifiChipInfoArr;
                            }
                            WifiIfaceInfo[] staIfaces = new WifiIfaceInfo[((ArrayList) ifaceNamesResp2.value).size()];
                            Iterator it = ((ArrayList) ifaceNamesResp2.value).iterator();
                            while (it.hasNext()) {
                                String ifaceName = (String) it.next();
                                ((IWifiChip) chipResp.value).getStaIface(ifaceName, new IWifiChip.getStaIfaceCallback(statusOk, ifaceName, staIfaces, ifaceIndex) {
                                    /* class com.android.server.wifi.$$Lambda$HalDeviceManager$NbsSOlQ2rIfa_ahKmLVOeqJ0sk */
                                    private final /* synthetic */ MutableBoolean f$1;
                                    private final /* synthetic */ String f$2;
                                    private final /* synthetic */ HalDeviceManager.WifiIfaceInfo[] f$3;
                                    private final /* synthetic */ MutableInt f$4;

                                    {
                                        this.f$1 = r2;
                                        this.f$2 = r3;
                                        this.f$3 = r4;
                                        this.f$4 = r5;
                                    }

                                    @Override // android.hardware.wifi.V1_0.IWifiChip.getStaIfaceCallback
                                    public final void onValues(WifiStatus wifiStatus, IWifiStaIface iWifiStaIface) {
                                        HalDeviceManager.this.lambda$getAllChipInfo$10$HalDeviceManager(this.f$1, this.f$2, this.f$3, this.f$4, wifiStatus, iWifiStaIface);
                                    }
                                });
                                if (!statusOk.value) {
                                    return null;
                                }
                                availableModesResp = availableModesResp;
                                ifaceNamesResp2 = ifaceNamesResp2;
                                chipIdsResp = chipIdsResp;
                                ifaceIndex = ifaceIndex;
                                ifaceNamesResp = ifaceNamesResp;
                                currentModeResp = currentModeResp;
                                currentModeValidResp = currentModeValidResp;
                                staIfaces = staIfaces;
                            }
                            HidlSupport.Mutable<ArrayList<String>> ifaceNamesResp3 = ifaceNamesResp2;
                            HidlSupport.Mutable<ArrayList<IWifiChip.ChipMode>> availableModesResp2 = availableModesResp;
                            ifaceIndex.value = 0;
                            ((IWifiChip) chipResp.value).getApIfaceNames(new IWifiChip.getApIfaceNamesCallback(statusOk, ifaceNamesResp3) {
                                /* class com.android.server.wifi.$$Lambda$HalDeviceManager$G_a1DJorXSzwXeeGcyXnUw44LU */
                                private final /* synthetic */ MutableBoolean f$0;
                                private final /* synthetic */ HidlSupport.Mutable f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                @Override // android.hardware.wifi.V1_0.IWifiChip.getApIfaceNamesCallback
                                public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                                    HalDeviceManager.lambda$getAllChipInfo$11(this.f$0, this.f$1, wifiStatus, arrayList);
                                }
                            });
                            if (!statusOk.value) {
                                return null;
                            }
                            WifiIfaceInfo[] apIfaces = new WifiIfaceInfo[((ArrayList) ifaceNamesResp3.value).size()];
                            Iterator it2 = ((ArrayList) ifaceNamesResp3.value).iterator();
                            while (it2.hasNext()) {
                                String ifaceName2 = (String) it2.next();
                                ((IWifiChip) chipResp.value).getApIface(ifaceName2, new IWifiChip.getApIfaceCallback(statusOk, ifaceName2, apIfaces, ifaceIndex) {
                                    /* class com.android.server.wifi.$$Lambda$HalDeviceManager$jZreS4fq6SFMLm2Ky9h8uCsiqO8 */
                                    private final /* synthetic */ MutableBoolean f$1;
                                    private final /* synthetic */ String f$2;
                                    private final /* synthetic */ HalDeviceManager.WifiIfaceInfo[] f$3;
                                    private final /* synthetic */ MutableInt f$4;

                                    {
                                        this.f$1 = r2;
                                        this.f$2 = r3;
                                        this.f$3 = r4;
                                        this.f$4 = r5;
                                    }

                                    @Override // android.hardware.wifi.V1_0.IWifiChip.getApIfaceCallback
                                    public final void onValues(WifiStatus wifiStatus, IWifiApIface iWifiApIface) {
                                        HalDeviceManager.this.lambda$getAllChipInfo$12$HalDeviceManager(this.f$1, this.f$2, this.f$3, this.f$4, wifiStatus, iWifiApIface);
                                    }
                                });
                                if (!statusOk.value) {
                                    return null;
                                }
                                availableModesResp2 = availableModesResp2;
                                chipsInfo = chipsInfo;
                                apIfaces = apIfaces;
                            }
                            ifaceIndex.value = 0;
                            ((IWifiChip) chipResp.value).getP2pIfaceNames(new IWifiChip.getP2pIfaceNamesCallback(statusOk, ifaceNamesResp3) {
                                /* class com.android.server.wifi.$$Lambda$HalDeviceManager$eCjBY_x53LlceEqam19pM9GP8Hg */
                                private final /* synthetic */ MutableBoolean f$0;
                                private final /* synthetic */ HidlSupport.Mutable f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                @Override // android.hardware.wifi.V1_0.IWifiChip.getP2pIfaceNamesCallback
                                public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                                    HalDeviceManager.lambda$getAllChipInfo$13(this.f$0, this.f$1, wifiStatus, arrayList);
                                }
                            });
                            if (!statusOk.value) {
                                return null;
                            }
                            WifiIfaceInfo[] p2pIfaces = new WifiIfaceInfo[((ArrayList) ifaceNamesResp3.value).size()];
                            Iterator it3 = ((ArrayList) ifaceNamesResp3.value).iterator();
                            while (it3.hasNext()) {
                                String ifaceName3 = (String) it3.next();
                                ((IWifiChip) chipResp.value).getP2pIface(ifaceName3, new IWifiChip.getP2pIfaceCallback(statusOk, ifaceName3, p2pIfaces, ifaceIndex) {
                                    /* class com.android.server.wifi.$$Lambda$HalDeviceManager$Y4lM61kLmbzKhU2PVXYtGOePWWM */
                                    private final /* synthetic */ MutableBoolean f$1;
                                    private final /* synthetic */ String f$2;
                                    private final /* synthetic */ HalDeviceManager.WifiIfaceInfo[] f$3;
                                    private final /* synthetic */ MutableInt f$4;

                                    {
                                        this.f$1 = r2;
                                        this.f$2 = r3;
                                        this.f$3 = r4;
                                        this.f$4 = r5;
                                    }

                                    @Override // android.hardware.wifi.V1_0.IWifiChip.getP2pIfaceCallback
                                    public final void onValues(WifiStatus wifiStatus, IWifiP2pIface iWifiP2pIface) {
                                        HalDeviceManager.this.lambda$getAllChipInfo$14$HalDeviceManager(this.f$1, this.f$2, this.f$3, this.f$4, wifiStatus, iWifiP2pIface);
                                    }
                                });
                                if (!statusOk.value) {
                                    return null;
                                }
                                it3 = it3;
                                p2pIfaces = p2pIfaces;
                            }
                            ifaceIndex.value = 0;
                            ((IWifiChip) chipResp.value).getNanIfaceNames(new IWifiChip.getNanIfaceNamesCallback(statusOk, ifaceNamesResp3) {
                                /* class com.android.server.wifi.$$Lambda$HalDeviceManager$yPcf5jFsIHD8FLJVWQLrQ1Z9fSc */
                                private final /* synthetic */ MutableBoolean f$0;
                                private final /* synthetic */ HidlSupport.Mutable f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                @Override // android.hardware.wifi.V1_0.IWifiChip.getNanIfaceNamesCallback
                                public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                                    HalDeviceManager.lambda$getAllChipInfo$15(this.f$0, this.f$1, wifiStatus, arrayList);
                                }
                            });
                            if (!statusOk.value) {
                                return null;
                            }
                            WifiIfaceInfo[] nanIfaces = new WifiIfaceInfo[((ArrayList) ifaceNamesResp3.value).size()];
                            Iterator it4 = ((ArrayList) ifaceNamesResp3.value).iterator();
                            while (it4.hasNext()) {
                                String ifaceName4 = (String) it4.next();
                                ((IWifiChip) chipResp.value).getNanIface(ifaceName4, new IWifiChip.getNanIfaceCallback(statusOk, ifaceName4, nanIfaces, ifaceIndex) {
                                    /* class com.android.server.wifi.$$Lambda$HalDeviceManager$hzrxXx9RDE1QCGSaFElLzJYP5ag */
                                    private final /* synthetic */ MutableBoolean f$1;
                                    private final /* synthetic */ String f$2;
                                    private final /* synthetic */ HalDeviceManager.WifiIfaceInfo[] f$3;
                                    private final /* synthetic */ MutableInt f$4;

                                    {
                                        this.f$1 = r2;
                                        this.f$2 = r3;
                                        this.f$3 = r4;
                                        this.f$4 = r5;
                                    }

                                    @Override // android.hardware.wifi.V1_0.IWifiChip.getNanIfaceCallback
                                    public final void onValues(WifiStatus wifiStatus, IWifiNanIface iWifiNanIface) {
                                        HalDeviceManager.this.lambda$getAllChipInfo$16$HalDeviceManager(this.f$1, this.f$2, this.f$3, this.f$4, wifiStatus, iWifiNanIface);
                                    }
                                });
                                if (!statusOk.value) {
                                    return null;
                                }
                                it4 = it4;
                                ifaceNamesResp3 = ifaceNamesResp3;
                            }
                            WifiChipInfo chipInfo = new WifiChipInfo();
                            chipsInfo[chipInfoIndex] = chipInfo;
                            chipInfo.chip = (IWifiChip) chipResp.value;
                            chipInfo.chipId = chipId.intValue();
                            chipInfo.availableModes = (ArrayList) availableModesResp2.value;
                            chipInfo.currentModeIdValid = currentModeValidResp.value;
                            chipInfo.currentModeId = currentModeResp.value;
                            chipInfo.ifaces[0] = staIfaces;
                            chipInfo.ifaces[1] = apIfaces;
                            chipInfo.ifaces[2] = p2pIfaces;
                            chipInfo.ifaces[3] = nanIfaces;
                            chipInfoIndex++;
                            r10 = 0;
                            chipIdsResp = chipIdsResp;
                            chipsInfo = chipsInfo;
                            ifaceNamesResp = ifaceNamesResp;
                            wifiChipInfoArr = null;
                        }
                    }
                    return null;
                }
                return chipsInfo;
            } catch (RemoteException e) {
                Log.e(TAG, "getAllChipInfoAndValidateCache exception: " + e);
                return null;
            } catch (NullPointerException ex) {
                Log.e(TAG, "getAllChipInfoAndValidateCache NullPointerException ", ex);
                return null;
            }
        }
    }

    static /* synthetic */ void lambda$getAllChipInfo$5(MutableBoolean statusOk, HidlSupport.Mutable chipIdsResp, WifiStatus status, ArrayList chipIds) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            chipIdsResp.value = chipIds;
            return;
        }
        Log.e(TAG, "getChipIds failed: " + statusString(status));
    }

    static /* synthetic */ void lambda$getAllChipInfo$6(MutableBoolean statusOk, HidlSupport.Mutable chipResp, WifiStatus status, IWifiChip chip) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            chipResp.value = chip;
            return;
        }
        Log.e(TAG, "getChip failed: " + statusString(status));
    }

    static /* synthetic */ void lambda$getAllChipInfo$7(MutableBoolean statusOk, HidlSupport.Mutable availableModesResp, WifiStatus status, ArrayList modes) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            availableModesResp.value = modes;
            return;
        }
        Log.e(TAG, "getAvailableModes failed: " + statusString(status));
    }

    static /* synthetic */ void lambda$getAllChipInfo$8(MutableBoolean statusOk, MutableBoolean currentModeValidResp, MutableInt currentModeResp, WifiStatus status, int modeId) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            currentModeValidResp.value = true;
            currentModeResp.value = modeId;
        } else if (status.code == 5) {
            statusOk.value = true;
        } else {
            Log.e(TAG, "getMode failed: " + statusString(status));
        }
    }

    static /* synthetic */ void lambda$getAllChipInfo$9(MutableBoolean statusOk, HidlSupport.Mutable ifaceNamesResp, WifiStatus status, ArrayList ifnames) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            ifaceNamesResp.value = ifnames;
            return;
        }
        Log.e(TAG, "getStaIfaceNames failed: " + statusString(status));
    }

    public /* synthetic */ void lambda$getAllChipInfo$10$HalDeviceManager(MutableBoolean statusOk, String ifaceName, WifiIfaceInfo[] staIfaces, MutableInt ifaceIndex, WifiStatus status, IWifiStaIface iface) {
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
        Log.e(TAG, "getStaIface failed: " + statusString(status));
    }

    static /* synthetic */ void lambda$getAllChipInfo$11(MutableBoolean statusOk, HidlSupport.Mutable ifaceNamesResp, WifiStatus status, ArrayList ifnames) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            ifaceNamesResp.value = ifnames;
            return;
        }
        Log.e(TAG, "getApIfaceNames failed: " + statusString(status));
    }

    public /* synthetic */ void lambda$getAllChipInfo$12$HalDeviceManager(MutableBoolean statusOk, String ifaceName, WifiIfaceInfo[] apIfaces, MutableInt ifaceIndex, WifiStatus status, IWifiApIface iface) {
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
        Log.e(TAG, "getApIface failed: " + statusString(status));
    }

    static /* synthetic */ void lambda$getAllChipInfo$13(MutableBoolean statusOk, HidlSupport.Mutable ifaceNamesResp, WifiStatus status, ArrayList ifnames) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            ifaceNamesResp.value = ifnames;
            return;
        }
        Log.e(TAG, "getP2pIfaceNames failed: " + statusString(status));
    }

    public /* synthetic */ void lambda$getAllChipInfo$14$HalDeviceManager(MutableBoolean statusOk, String ifaceName, WifiIfaceInfo[] p2pIfaces, MutableInt ifaceIndex, WifiStatus status, IWifiP2pIface iface) {
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
        Log.e(TAG, "getP2pIface failed: " + statusString(status));
    }

    static /* synthetic */ void lambda$getAllChipInfo$15(MutableBoolean statusOk, HidlSupport.Mutable ifaceNamesResp, WifiStatus status, ArrayList ifnames) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            ifaceNamesResp.value = ifnames;
            return;
        }
        Log.e(TAG, "getNanIfaceNames failed: " + statusString(status));
    }

    public /* synthetic */ void lambda$getAllChipInfo$16$HalDeviceManager(MutableBoolean statusOk, String ifaceName, WifiIfaceInfo[] nanIfaces, MutableInt ifaceIndex, WifiStatus status, IWifiNanIface iface) {
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
        Log.e(TAG, "getNanIface failed: " + statusString(status));
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
                    Log.e(TAG, "validateInterfaceCache: no chip found for " + entry);
                    return false;
                }
                WifiIfaceInfo[] ifaceInfoList = matchingChipInfo.ifaces[entry.type];
                if (ifaceInfoList == null) {
                    Log.e(TAG, "validateInterfaceCache: invalid type on entry " + entry);
                    return false;
                }
                boolean matchFound = false;
                int length2 = ifaceInfoList.length;
                int i2 = 0;
                while (true) {
                    if (i2 >= length2) {
                        break;
                    } else if (ifaceInfoList[i2].name.equals(entry.name)) {
                        matchFound = true;
                        break;
                    } else {
                        i2++;
                    }
                }
                if (!matchFound) {
                    Log.e(TAG, "validateInterfaceCache: no interface found for " + entry);
                    return false;
                }
            }
            return true;
        }
    }

    private boolean isWifiStarted() {
        synchronized (this.mLock) {
            try {
                if (this.mWifi == null) {
                    Log.w(TAG, "isWifiStarted called but mWifi is null!?");
                    return false;
                }
                return this.mWifi.isStarted();
            } catch (RemoteException e) {
                Log.e(TAG, "isWifiStarted exception: " + e);
                return false;
            }
        }
    }

    private boolean startWifi() {
        initIWifiIfNecessary();
        synchronized (this.mLock) {
            try {
                if (this.mWifi == null) {
                    Log.w(TAG, "startWifi called but mWifi is null!?");
                    return false;
                }
                int triedCount = 0;
                while (triedCount <= 3) {
                    WifiStatus status = this.mWifi.start();
                    if (status.code == 0) {
                        initIWifiChipDebugListeners();
                        managerStatusListenerDispatch();
                        if (triedCount != 0) {
                            Log.d(TAG, "start IWifi succeeded after trying " + triedCount + " times");
                        }
                        return true;
                    } else if (status.code == 5) {
                        Log.e(TAG, "Cannot start IWifi: " + statusString(status) + ", Retrying...");
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                        }
                        triedCount++;
                    } else {
                        Log.e(TAG, "Cannot start IWifi: " + statusString(status));
                        return false;
                    }
                }
                Log.e(TAG, "Cannot start IWifi after trying " + triedCount + " times");
                return false;
            } catch (RemoteException e2) {
                Log.e(TAG, "startWifi exception: " + e2);
                return false;
            }
        }
    }

    private void stopWifi() {
        synchronized (this.mLock) {
            try {
                if (this.mWifi == null) {
                    Log.w(TAG, "stopWifi called but mWifi is null!?");
                } else {
                    WifiStatus status = this.mWifi.stop();
                    if (status.code != 0) {
                        Log.e(TAG, "Cannot stop IWifi: " + statusString(status));
                    }
                    teardownInternal();
                }
            } catch (RemoteException e) {
                Log.e(TAG, "stopWifi exception: " + e);
            }
        }
    }

    /* access modifiers changed from: private */
    public class WifiEventCallback extends IWifiEventCallback.Stub {
        private WifiEventCallback() {
        }

        @Override // android.hardware.wifi.V1_0.IWifiEventCallback
        public void onStart() throws RemoteException {
        }

        @Override // android.hardware.wifi.V1_0.IWifiEventCallback
        public void onStop() throws RemoteException {
        }

        @Override // android.hardware.wifi.V1_0.IWifiEventCallback
        public void onFailure(WifiStatus status) throws RemoteException {
            Log.e(HalDeviceManager.TAG, "IWifiEventCallback.onFailure: " + HalDeviceManager.statusString(status));
            HalDeviceManager.this.teardownInternal();
        }
    }

    private void managerStatusListenerDispatch() {
        synchronized (this.mLock) {
            for (ManagerStatusListenerProxy cb : this.mManagerStatusListeners) {
                cb.trigger();
            }
        }
    }

    /* access modifiers changed from: private */
    public class ManagerStatusListenerProxy extends ListenerProxy<ManagerStatusListener> {
        ManagerStatusListenerProxy(ManagerStatusListener statusListener, Handler handler) {
            super(statusListener, handler, "ManagerStatusListenerProxy");
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.wifi.HalDeviceManager.ListenerProxy
        public void action() {
            ((ManagerStatusListener) this.mListener).onStatusChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public Set<Integer> getSupportedIfaceTypesInternal(IWifiChip chip) {
        Set<Integer> results = new HashSet<>();
        WifiChipInfo[] chipInfos = getAllChipInfo();
        if (chipInfos == null) {
            Log.e(TAG, "getSupportedIfaceTypesInternal: no chip info found");
            return results;
        }
        MutableInt chipIdIfProvided = new MutableInt(0);
        if (chip != null) {
            MutableBoolean statusOk = new MutableBoolean(false);
            try {
                chip.getId(new IWifiChip.getIdCallback(chipIdIfProvided, statusOk) {
                    /* class com.android.server.wifi.$$Lambda$HalDeviceManager$kMV_gE_EG84ftYS0Yp4P7U_NM */
                    private final /* synthetic */ MutableInt f$0;
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    @Override // android.hardware.wifi.V1_0.IWifiChip.getIdCallback
                    public final void onValues(WifiStatus wifiStatus, int i) {
                        HalDeviceManager.lambda$getSupportedIfaceTypesInternal$17(this.f$0, this.f$1, wifiStatus, i);
                    }
                });
                if (!statusOk.value) {
                    return results;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "getSupportedIfaceTypesInternal IWifiChip.getId() exception: " + e);
                return results;
            }
        }
        for (WifiChipInfo wci : chipInfos) {
            if (chip == null || wci.chipId == chipIdIfProvided.value) {
                Iterator<IWifiChip.ChipMode> it = wci.availableModes.iterator();
                while (it.hasNext()) {
                    Iterator<IWifiChip.ChipIfaceCombination> it2 = it.next().availableCombinations.iterator();
                    while (it2.hasNext()) {
                        Iterator<IWifiChip.ChipIfaceCombinationLimit> it3 = it2.next().limits.iterator();
                        while (it3.hasNext()) {
                            Iterator<Integer> it4 = it3.next().types.iterator();
                            while (it4.hasNext()) {
                                results.add(Integer.valueOf(it4.next().intValue()));
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    static /* synthetic */ void lambda$getSupportedIfaceTypesInternal$17(MutableInt chipIdIfProvided, MutableBoolean statusOk, WifiStatus status, int id) {
        if (status.code == 0) {
            chipIdIfProvided.value = id;
            statusOk.value = true;
            return;
        }
        Log.e(TAG, "getSupportedIfaceTypesInternal: IWifiChip.getId() error: " + statusString(status));
        statusOk.value = false;
    }

    private IWifiIface createIface(int ifaceType, boolean lowPriority, InterfaceDestroyedListener destroyedListener, Handler handler) {
        if (this.mDbg) {
            Log.d(TAG, "createIface: ifaceType=" + ifaceType + ", lowPriority=" + lowPriority);
        }
        synchronized (this.mLock) {
            WifiChipInfo[] chipInfos = getAllChipInfo();
            if (chipInfos == null) {
                Log.e(TAG, "createIface: no chip info found");
                stopWifi();
                return null;
            } else if (!validateInterfaceCache(chipInfos)) {
                Log.e(TAG, "createIface: local cache is invalid!");
                stopWifi();
                return null;
            } else {
                IWifiIface iface = createIfaceIfPossible(chipInfos, ifaceType, lowPriority, destroyedListener, handler);
                if (iface == null || dispatchAvailableForRequestListeners()) {
                    return iface;
                }
                return null;
            }
        }
    }

    private IWifiIface createIfaceIfPossible(WifiChipInfo[] chipInfos, int ifaceType, boolean lowPriority, InterfaceDestroyedListener destroyedListener, Handler handler) {
        synchronized (this.mLock) {
            try {
                IfaceCreationData bestIfaceCreationProposal = null;
                for (WifiChipInfo chipInfo : chipInfos) {
                    Iterator<IWifiChip.ChipMode> it = chipInfo.availableModes.iterator();
                    while (it.hasNext()) {
                        IWifiChip.ChipMode chipMode = it.next();
                        Iterator<IWifiChip.ChipIfaceCombination> it2 = chipMode.availableCombinations.iterator();
                        while (it2.hasNext()) {
                            IWifiChip.ChipIfaceCombination chipIfaceCombo = it2.next();
                            int[][] expandedIfaceCombos = expandIfaceCombos(chipIfaceCombo);
                            int length = expandedIfaceCombos.length;
                            IfaceCreationData bestIfaceCreationProposal2 = bestIfaceCreationProposal;
                            int i = 0;
                            while (i < length) {
                                IfaceCreationData currentProposal = canIfaceComboSupportRequest(chipInfo, chipMode, expandedIfaceCombos[i], ifaceType, lowPriority);
                                if (compareIfaceCreationData(currentProposal, bestIfaceCreationProposal2)) {
                                    bestIfaceCreationProposal2 = currentProposal;
                                } else {
                                    bestIfaceCreationProposal2 = bestIfaceCreationProposal2;
                                }
                                i++;
                                length = length;
                                expandedIfaceCombos = expandedIfaceCombos;
                                chipIfaceCombo = chipIfaceCombo;
                                chipMode = chipMode;
                            }
                            bestIfaceCreationProposal = bestIfaceCreationProposal2;
                        }
                    }
                }
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
                        cacheEntry.isLowPriority = lowPriority;
                        if (this.mDbg) {
                            Log.d(TAG, "createIfaceIfPossible: added cacheEntry=" + cacheEntry);
                        }
                        this.mInterfaceInfoCache.put(Pair.create(cacheEntry.name, Integer.valueOf(cacheEntry.type)), cacheEntry);
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

    private boolean isItPossibleToCreateIface(WifiChipInfo[] chipInfos, int ifaceType) {
        for (WifiChipInfo chipInfo : chipInfos) {
            Iterator<IWifiChip.ChipMode> it = chipInfo.availableModes.iterator();
            while (it.hasNext()) {
                IWifiChip.ChipMode chipMode = it.next();
                Iterator<IWifiChip.ChipIfaceCombination> it2 = chipMode.availableCombinations.iterator();
                while (it2.hasNext()) {
                    int[][] expandedIfaceCombos = expandIfaceCombos(it2.next());
                    int length = expandedIfaceCombos.length;
                    int i = 0;
                    while (i < length) {
                        if (canIfaceComboSupportRequest(chipInfo, chipMode, expandedIfaceCombos[i], ifaceType, false) != null) {
                            return true;
                        }
                        i++;
                        length = length;
                        expandedIfaceCombos = expandedIfaceCombos;
                    }
                }
            }
        }
        return false;
    }

    private int[][] expandIfaceCombos(IWifiChip.ChipIfaceCombination chipIfaceCombo) {
        int numOfCombos = 1;
        Iterator<IWifiChip.ChipIfaceCombinationLimit> it = chipIfaceCombo.limits.iterator();
        while (it.hasNext()) {
            IWifiChip.ChipIfaceCombinationLimit limit = it.next();
            for (int i = 0; i < limit.maxIfaces; i++) {
                numOfCombos *= limit.types.size();
            }
        }
        int[][] expandedIfaceCombos = (int[][]) Array.newInstance(int.class, numOfCombos, IFACE_TYPES_BY_PRIORITY.length);
        int span = numOfCombos;
        Iterator<IWifiChip.ChipIfaceCombinationLimit> it2 = chipIfaceCombo.limits.iterator();
        while (it2.hasNext()) {
            IWifiChip.ChipIfaceCombinationLimit limit2 = it2.next();
            for (int i2 = 0; i2 < limit2.maxIfaces; i2++) {
                span /= limit2.types.size();
                for (int k = 0; k < numOfCombos; k++) {
                    int[] iArr = expandedIfaceCombos[k];
                    int intValue = limit2.types.get((k / span) % limit2.types.size()).intValue();
                    iArr[intValue] = iArr[intValue] + 1;
                }
            }
        }
        return expandedIfaceCombos;
    }

    /* access modifiers changed from: private */
    public class IfaceCreationData {
        public WifiChipInfo chipInfo;
        public int chipModeId;
        public List<WifiIfaceInfo> interfacesToBeRemovedFirst;

        private IfaceCreationData() {
        }

        public String toString() {
            return "{chipInfo=" + this.chipInfo + ", chipModeId=" + this.chipModeId + ", interfacesToBeRemovedFirst=" + this.interfacesToBeRemovedFirst + ")";
        }
    }

    private IfaceCreationData canIfaceComboSupportRequest(WifiChipInfo chipInfo, IWifiChip.ChipMode chipMode, int[] chipIfaceCombo, int ifaceType, boolean lowPriority) {
        if (chipIfaceCombo[ifaceType] == 0) {
            return null;
        }
        int i = 0;
        if (chipInfo.currentModeIdValid && chipInfo.currentModeId != chipMode.id) {
            int[] iArr = IFACE_TYPES_BY_PRIORITY;
            int length = iArr.length;
            while (i < length) {
                int type = iArr[i];
                if (chipInfo.ifaces[type].length != 0 && (lowPriority || !allowedToDeleteIfaceTypeForRequestedType(type, ifaceType, chipInfo.ifaces, chipInfo.ifaces[type].length))) {
                    return null;
                }
                i++;
            }
            IfaceCreationData ifaceCreationData = new IfaceCreationData();
            ifaceCreationData.chipInfo = chipInfo;
            ifaceCreationData.chipModeId = chipMode.id;
            return ifaceCreationData;
        }
        List<WifiIfaceInfo> interfacesToBeRemovedFirst = new ArrayList<>();
        int[] iArr2 = IFACE_TYPES_BY_PRIORITY;
        int length2 = iArr2.length;
        while (i < length2) {
            int type2 = iArr2[i];
            int tooManyInterfaces = chipInfo.ifaces[type2].length - chipIfaceCombo[type2];
            if (type2 == ifaceType) {
                tooManyInterfaces++;
            }
            if (tooManyInterfaces > 0) {
                if (lowPriority || !allowedToDeleteIfaceTypeForRequestedType(type2, ifaceType, chipInfo.ifaces, tooManyInterfaces)) {
                    return null;
                }
                interfacesToBeRemovedFirst = selectInterfacesToDelete(tooManyInterfaces, chipInfo.ifaces[type2]);
            }
            i++;
        }
        IfaceCreationData ifaceCreationData2 = new IfaceCreationData();
        ifaceCreationData2.chipInfo = chipInfo;
        ifaceCreationData2.chipModeId = chipMode.id;
        ifaceCreationData2.interfacesToBeRemovedFirst = interfacesToBeRemovedFirst;
        return ifaceCreationData2;
    }

    private boolean compareIfaceCreationData(IfaceCreationData val1, IfaceCreationData val2) {
        int numIfacesToDelete1;
        int numIfacesToDelete2;
        if (val1 == null) {
            return false;
        }
        if (val2 == null) {
            return true;
        }
        int[] iArr = IFACE_TYPES_BY_PRIORITY;
        for (int type : iArr) {
            if (!val1.chipInfo.currentModeIdValid || val1.chipInfo.currentModeId == val1.chipModeId) {
                numIfacesToDelete1 = val1.interfacesToBeRemovedFirst.size();
            } else {
                numIfacesToDelete1 = val1.chipInfo.ifaces[type].length;
            }
            if (!val2.chipInfo.currentModeIdValid || val2.chipInfo.currentModeId == val2.chipModeId) {
                numIfacesToDelete2 = val2.interfacesToBeRemovedFirst.size();
            } else {
                numIfacesToDelete2 = val2.chipInfo.ifaces[type].length;
            }
            if (numIfacesToDelete1 < numIfacesToDelete2) {
                return true;
            }
        }
        return false;
    }

    private boolean allowedToDeleteIfaceTypeForRequestedType(int existingIfaceType, int requestedIfaceType, WifiIfaceInfo[][] currentIfaces, int numNecessaryInterfaces) {
        int numAvailableLowPriorityInterfaces = 0;
        for (InterfaceCacheEntry entry : this.mInterfaceInfoCache.values()) {
            if (entry.type == existingIfaceType && entry.isLowPriority) {
                numAvailableLowPriorityInterfaces++;
            }
        }
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
        boolean lookupError = false;
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
                Log.e(TAG, "selectInterfacesToDelete: can't find cache entry with name=" + info.name);
                lookupError = true;
                break;
            }
            if (cacheEntry.isLowPriority) {
                orderedListLowPriority.append(cacheEntry.creationTime, info);
            } else {
                orderedList.append(cacheEntry.creationTime, info);
            }
            i++;
        }
        if (lookupError) {
            Log.e(TAG, "selectInterfacesToDelete: falling back to arbitrary selection");
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

    /* JADX WARNING: Removed duplicated region for block: B:16:0x003e A[Catch:{ RemoteException -> 0x013a }] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0056 A[Catch:{ RemoteException -> 0x013a }] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00a0  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00c5  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00f5  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x010a  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0132  */
    private IWifiIface executeChipReconfiguration(IfaceCreationData ifaceCreationData, int ifaceType) {
        boolean isModeConfigNeeded;
        HidlSupport.Mutable<WifiStatus> statusResp;
        if (this.mDbg) {
            Log.i(TAG, "executeChipReconfiguration: ifaceCreationData=" + ifaceCreationData + ", ifaceType=" + ifaceType);
        }
        synchronized (this.mLock) {
            try {
                if (ifaceCreationData.chipInfo.currentModeIdValid) {
                    if (ifaceCreationData.chipInfo.currentModeId == ifaceCreationData.chipModeId) {
                        isModeConfigNeeded = false;
                        if (this.mDbg) {
                            Log.d(TAG, "isModeConfigNeeded=" + isModeConfigNeeded);
                        }
                        if (!isModeConfigNeeded) {
                            WifiIfaceInfo[][] wifiIfaceInfoArr = ifaceCreationData.chipInfo.ifaces;
                            for (WifiIfaceInfo[] ifaceInfos : wifiIfaceInfoArr) {
                                for (WifiIfaceInfo ifaceInfo : ifaceInfos) {
                                    removeIfaceInternal(ifaceInfo.iface);
                                }
                            }
                            WifiStatus status = ifaceCreationData.chipInfo.chip.configureChip(ifaceCreationData.chipModeId);
                            updateRttControllerOnModeChange();
                            if (status.code != 0) {
                                Log.e(TAG, "executeChipReconfiguration: configureChip error: " + statusString(status));
                                return null;
                            }
                        } else {
                            for (WifiIfaceInfo ifaceInfo2 : ifaceCreationData.interfacesToBeRemovedFirst) {
                                removeIfaceInternal(ifaceInfo2.iface);
                            }
                        }
                        statusResp = new HidlSupport.Mutable<>();
                        HidlSupport.Mutable<IWifiIface> ifaceResp = new HidlSupport.Mutable<>();
                        if (ifaceType != 0) {
                            ifaceCreationData.chipInfo.chip.createStaIface(new IWifiChip.createStaIfaceCallback(statusResp, ifaceResp) {
                                /* class com.android.server.wifi.$$Lambda$HalDeviceManager$0fExlzrQXvHvboqrhwLsuIEN8sQ */
                                private final /* synthetic */ HidlSupport.Mutable f$0;
                                private final /* synthetic */ HidlSupport.Mutable f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                @Override // android.hardware.wifi.V1_0.IWifiChip.createStaIfaceCallback
                                public final void onValues(WifiStatus wifiStatus, IWifiStaIface iWifiStaIface) {
                                    HalDeviceManager.lambda$executeChipReconfiguration$18(this.f$0, this.f$1, wifiStatus, iWifiStaIface);
                                }
                            });
                        } else if (ifaceType == 1) {
                            ifaceCreationData.chipInfo.chip.createApIface(new IWifiChip.createApIfaceCallback(statusResp, ifaceResp) {
                                /* class com.android.server.wifi.$$Lambda$HalDeviceManager$OnsWneK7WJWdtu1Yc97G_SlWc5w */
                                private final /* synthetic */ HidlSupport.Mutable f$0;
                                private final /* synthetic */ HidlSupport.Mutable f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                @Override // android.hardware.wifi.V1_0.IWifiChip.createApIfaceCallback
                                public final void onValues(WifiStatus wifiStatus, IWifiApIface iWifiApIface) {
                                    HalDeviceManager.lambda$executeChipReconfiguration$19(this.f$0, this.f$1, wifiStatus, iWifiApIface);
                                }
                            });
                        } else if (ifaceType == 2) {
                            ifaceCreationData.chipInfo.chip.createP2pIface(new IWifiChip.createP2pIfaceCallback(statusResp, ifaceResp) {
                                /* class com.android.server.wifi.$$Lambda$HalDeviceManager$MPjapT5h9jFZYNnOBjLSEdwh6tg */
                                private final /* synthetic */ HidlSupport.Mutable f$0;
                                private final /* synthetic */ HidlSupport.Mutable f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                @Override // android.hardware.wifi.V1_0.IWifiChip.createP2pIfaceCallback
                                public final void onValues(WifiStatus wifiStatus, IWifiP2pIface iWifiP2pIface) {
                                    HalDeviceManager.lambda$executeChipReconfiguration$20(this.f$0, this.f$1, wifiStatus, iWifiP2pIface);
                                }
                            });
                        } else if (ifaceType == 3) {
                            ifaceCreationData.chipInfo.chip.createNanIface(new IWifiChip.createNanIfaceCallback(statusResp, ifaceResp) {
                                /* class com.android.server.wifi.$$Lambda$HalDeviceManager$BkJu2mnM7l_bsdJ9qDxHZJRcVM */
                                private final /* synthetic */ HidlSupport.Mutable f$0;
                                private final /* synthetic */ HidlSupport.Mutable f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                @Override // android.hardware.wifi.V1_0.IWifiChip.createNanIfaceCallback
                                public final void onValues(WifiStatus wifiStatus, IWifiNanIface iWifiNanIface) {
                                    HalDeviceManager.lambda$executeChipReconfiguration$21(this.f$0, this.f$1, wifiStatus, iWifiNanIface);
                                }
                            });
                        }
                        if (((WifiStatus) statusResp.value).code == 0) {
                            Log.e(TAG, "executeChipReconfiguration: failed to create interface ifaceType=" + ifaceType + ": " + statusString((WifiStatus) statusResp.value));
                            return null;
                        }
                        return (IWifiIface) ifaceResp.value;
                    }
                }
                isModeConfigNeeded = true;
                if (this.mDbg) {
                }
                if (!isModeConfigNeeded) {
                }
                statusResp = new HidlSupport.Mutable<>();
                HidlSupport.Mutable<IWifiIface> ifaceResp2 = new HidlSupport.Mutable<>();
                if (ifaceType != 0) {
                }
                if (((WifiStatus) statusResp.value).code == 0) {
                }
            } catch (RemoteException e) {
                Log.e(TAG, "executeChipReconfiguration exception: " + e);
                return null;
            }
        }
    }

    static /* synthetic */ void lambda$executeChipReconfiguration$18(HidlSupport.Mutable statusResp, HidlSupport.Mutable ifaceResp, WifiStatus status, IWifiStaIface iface) {
        statusResp.value = status;
        ifaceResp.value = iface;
    }

    static /* synthetic */ void lambda$executeChipReconfiguration$19(HidlSupport.Mutable statusResp, HidlSupport.Mutable ifaceResp, WifiStatus status, IWifiApIface iface) {
        statusResp.value = status;
        ifaceResp.value = iface;
    }

    static /* synthetic */ void lambda$executeChipReconfiguration$20(HidlSupport.Mutable statusResp, HidlSupport.Mutable ifaceResp, WifiStatus status, IWifiP2pIface iface) {
        statusResp.value = status;
        ifaceResp.value = iface;
    }

    static /* synthetic */ void lambda$executeChipReconfiguration$21(HidlSupport.Mutable statusResp, HidlSupport.Mutable ifaceResp, WifiStatus status, IWifiNanIface iface) {
        statusResp.value = status;
        ifaceResp.value = iface;
    }

    private boolean removeIfaceInternal(IWifiIface iface) {
        String name = getName(iface);
        int type = getType(iface);
        if (this.mDbg) {
            Log.i(TAG, "removeIfaceInternal: iface(name)=" + name + ", type=" + type);
        }
        if (type == -1) {
            Log.e(TAG, "removeIfaceInternal: can't get type -- iface(name)=" + name);
            return false;
        }
        synchronized (this.mLock) {
            if (this.mWifi == null) {
                Log.e(TAG, "removeIfaceInternal: null IWifi -- iface(name)=" + name);
                return false;
            }
            IWifiChip chip = getChip(iface);
            if (chip == null) {
                Log.e(TAG, "removeIfaceInternal: null IWifiChip -- iface(name)=" + name);
                return false;
            } else if (name == null) {
                Log.e(TAG, "removeIfaceInternal: can't get name");
                return false;
            } else {
                WifiStatus status = null;
                if (type == 0) {
                    status = chip.removeStaIface(name);
                } else if (type == 1) {
                    status = chip.removeApIface(name);
                } else if (type == 2) {
                    status = chip.removeP2pIface(name);
                } else if (type != 3) {
                    try {
                        Log.wtf(TAG, "removeIfaceInternal: invalid type=" + type);
                        return false;
                    } catch (RemoteException e) {
                        Log.e(TAG, "IWifiChip.removeXxxIface exception: " + e);
                    }
                } else {
                    status = chip.removeNanIface(name);
                }
                dispatchDestroyedListeners(name, type);
                if (status != null && status.code == 0) {
                    return true;
                }
                Log.e(TAG, "IWifiChip.removeXxxIface failed: " + statusString(status));
                return false;
            }
        }
    }

    private boolean dispatchAvailableForRequestListeners() {
        synchronized (this.mLock) {
            WifiChipInfo[] chipInfos = getAllChipInfo();
            if (chipInfos == null) {
                Log.e(TAG, "dispatchAvailableForRequestListeners: no chip info found");
                stopWifi();
                return false;
            }
            for (int ifaceType : IFACE_TYPES_BY_PRIORITY) {
                dispatchAvailableForRequestListenersForType(ifaceType, chipInfos);
            }
            return true;
        }
    }

    private void dispatchAvailableForRequestListenersForType(int ifaceType, WifiChipInfo[] chipInfos) {
        synchronized (this.mLock) {
            Map<InterfaceAvailableForRequestListenerProxy, Boolean> listeners = this.mInterfaceAvailableForRequestListeners.get(ifaceType);
            if (listeners.size() != 0) {
                boolean isAvailable = isItPossibleToCreateIface(chipInfos, ifaceType);
                for (Map.Entry<InterfaceAvailableForRequestListenerProxy, Boolean> listenerEntry : listeners.entrySet()) {
                    if (listenerEntry.getValue() == null || listenerEntry.getValue().booleanValue() != isAvailable) {
                        listenerEntry.getKey().triggerWithArg(isAvailable);
                    }
                    listenerEntry.setValue(Boolean.valueOf(isAvailable));
                }
            }
        }
    }

    private void dispatchDestroyedListeners(String name, int type) {
        synchronized (this.mLock) {
            InterfaceCacheEntry entry = this.mInterfaceInfoCache.get(Pair.create(name, Integer.valueOf(type)));
            if (entry == null) {
                Log.e(TAG, "dispatchDestroyedListeners: no cache entry for iface(name)=" + name);
                return;
            }
            for (InterfaceDestroyedListenerProxy listener : entry.destroyedListeners) {
                listener.trigger();
            }
            entry.destroyedListeners.clear();
            this.mInterfaceInfoCache.remove(Pair.create(name, Integer.valueOf(type)));
        }
    }

    private void dispatchAllDestroyedListeners() {
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
                    Log.e(TAG, "dispatchAllDestroyedListeners: clear destroyedListeners error");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public abstract class ListenerProxy<LISTENER> {
        private Handler mHandler;
        protected LISTENER mListener;

        public boolean equals(Object obj) {
            return this.mListener == ((ListenerProxy) obj).mListener;
        }

        public int hashCode() {
            return this.mListener.hashCode();
        }

        /* access modifiers changed from: package-private */
        public void trigger() {
            Handler handler = this.mHandler;
            if (handler != null) {
                handler.post(new Runnable() {
                    /* class com.android.server.wifi.$$Lambda$HalDeviceManager$ListenerProxy$EUZ7m5GXHY27oKauEW_8pihGjbw */

                    @Override // java.lang.Runnable
                    public final void run() {
                        HalDeviceManager.ListenerProxy.this.lambda$trigger$0$HalDeviceManager$ListenerProxy();
                    }
                });
            } else {
                lambda$trigger$0$HalDeviceManager$ListenerProxy();
            }
        }

        /* access modifiers changed from: package-private */
        public void triggerWithArg(boolean arg) {
            Handler handler = this.mHandler;
            if (handler != null) {
                handler.post(new Runnable(arg) {
                    /* class com.android.server.wifi.$$Lambda$HalDeviceManager$ListenerProxy$YGLSZf58sxTORRCaSB1wOY_oquo */
                    private final /* synthetic */ boolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        HalDeviceManager.ListenerProxy.this.lambda$triggerWithArg$1$HalDeviceManager$ListenerProxy(this.f$1);
                    }
                });
            } else {
                lambda$triggerWithArg$1$HalDeviceManager$ListenerProxy(arg);
            }
        }

        /* access modifiers changed from: protected */
        /* renamed from: action */
        public void lambda$trigger$0$HalDeviceManager$ListenerProxy() {
        }

        /* access modifiers changed from: protected */
        /* renamed from: actionWithArg */
        public void lambda$triggerWithArg$1$HalDeviceManager$ListenerProxy(boolean arg) {
        }

        ListenerProxy(LISTENER listener, Handler handler, String tag) {
            this.mListener = listener;
            this.mHandler = handler;
        }
    }

    /* access modifiers changed from: private */
    public class InterfaceDestroyedListenerProxy extends ListenerProxy<InterfaceDestroyedListener> {
        private final String mIfaceName;

        InterfaceDestroyedListenerProxy(String ifaceName, InterfaceDestroyedListener destroyedListener, Handler handler) {
            super(destroyedListener, handler, "InterfaceDestroyedListenerProxy");
            this.mIfaceName = ifaceName;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.wifi.HalDeviceManager.ListenerProxy
        public void action() {
            ((InterfaceDestroyedListener) this.mListener).onDestroyed(this.mIfaceName);
        }
    }

    /* access modifiers changed from: private */
    public class InterfaceAvailableForRequestListenerProxy extends ListenerProxy<InterfaceAvailableForRequestListener> {
        InterfaceAvailableForRequestListenerProxy(InterfaceAvailableForRequestListener destroyedListener, Handler handler) {
            super(destroyedListener, handler, "InterfaceAvailableForRequestListenerProxy");
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.wifi.HalDeviceManager.ListenerProxy
        public void actionWithArg(boolean isAvailable) {
            ((InterfaceAvailableForRequestListener) this.mListener).onAvailabilityChanged(isAvailable);
        }
    }

    /* access modifiers changed from: private */
    public class InterfaceRttControllerLifecycleCallbackProxy implements InterfaceRttControllerLifecycleCallback {
        private InterfaceRttControllerLifecycleCallback mCallback;
        private Handler mHandler;

        InterfaceRttControllerLifecycleCallbackProxy(InterfaceRttControllerLifecycleCallback callback, Handler handler) {
            this.mCallback = callback;
            this.mHandler = handler;
        }

        public boolean equals(Object obj) {
            if (obj == null || this.mCallback != ((InterfaceRttControllerLifecycleCallbackProxy) obj).mCallback) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return this.mCallback.hashCode();
        }

        public /* synthetic */ void lambda$onNewRttController$0$HalDeviceManager$InterfaceRttControllerLifecycleCallbackProxy(IWifiRttController controller) {
            this.mCallback.onNewRttController(controller);
        }

        @Override // com.android.server.wifi.HalDeviceManager.InterfaceRttControllerLifecycleCallback
        public void onNewRttController(IWifiRttController controller) {
            this.mHandler.post(new Runnable(controller) {
                /* class com.android.server.wifi.$$Lambda$HalDeviceManager$InterfaceRttControllerLifecycleCallbackProxy$Vt8Gvz01jOxC1TqVEIeBuJ45xAg */
                private final /* synthetic */ IWifiRttController f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HalDeviceManager.InterfaceRttControllerLifecycleCallbackProxy.this.lambda$onNewRttController$0$HalDeviceManager$InterfaceRttControllerLifecycleCallbackProxy(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onRttControllerDestroyed$1$HalDeviceManager$InterfaceRttControllerLifecycleCallbackProxy() {
            this.mCallback.onRttControllerDestroyed();
        }

        @Override // com.android.server.wifi.HalDeviceManager.InterfaceRttControllerLifecycleCallback
        public void onRttControllerDestroyed() {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.wifi.$$Lambda$HalDeviceManager$InterfaceRttControllerLifecycleCallbackProxy$Jq3jiK0PF_ihQmDGnOqjWGZKR74 */

                @Override // java.lang.Runnable
                public final void run() {
                    HalDeviceManager.InterfaceRttControllerLifecycleCallbackProxy.this.lambda$onRttControllerDestroyed$1$HalDeviceManager$InterfaceRttControllerLifecycleCallbackProxy();
                }
            });
        }
    }

    private void dispatchRttControllerLifecycleOnNew() {
        for (InterfaceRttControllerLifecycleCallbackProxy cbp : this.mRttControllerLifecycleCallbacks) {
            cbp.onNewRttController(this.mIWifiRttController);
        }
    }

    private void dispatchRttControllerLifecycleOnDestroyed() {
        for (InterfaceRttControllerLifecycleCallbackProxy cbp : this.mRttControllerLifecycleCallbacks) {
            cbp.onRttControllerDestroyed();
        }
    }

    private void updateRttControllerOnModeChange() {
        synchronized (this.mLock) {
            boolean controllerDestroyed = this.mIWifiRttController != null;
            this.mIWifiRttController = null;
            if (this.mRttControllerLifecycleCallbacks.size() == 0) {
                Log.i(TAG, "updateRttController: no one is interested in RTT controllers");
                return;
            }
            IWifiRttController newRttController = createRttControllerIfPossible();
            if (newRttController != null) {
                this.mIWifiRttController = newRttController;
                dispatchRttControllerLifecycleOnNew();
            } else if (controllerDestroyed) {
                dispatchRttControllerLifecycleOnDestroyed();
            }
        }
    }

    private IWifiRttController createRttControllerIfPossible() {
        synchronized (this.mLock) {
            if (!isWifiStarted()) {
                Log.w(TAG, "createRttControllerIfPossible: Wifi is not started");
                return null;
            }
            WifiChipInfo[] chipInfos = getAllChipInfo();
            if (chipInfos == null) {
                Log.d(TAG, "createRttControllerIfPossible: no chip info found - most likely chip not up yet");
                return null;
            }
            for (WifiChipInfo chipInfo : chipInfos) {
                if (chipInfo.currentModeIdValid) {
                    HidlSupport.Mutable<IWifiRttController> rttResp = new HidlSupport.Mutable<>();
                    try {
                        chipInfo.chip.createRttController(null, new IWifiChip.createRttControllerCallback(rttResp) {
                            /* class com.android.server.wifi.$$Lambda$HalDeviceManager$X9vx2J4m1oo365x_oO3URsq_nqo */
                            private final /* synthetic */ HidlSupport.Mutable f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // android.hardware.wifi.V1_0.IWifiChip.createRttControllerCallback
                            public final void onValues(WifiStatus wifiStatus, IWifiRttController iWifiRttController) {
                                HalDeviceManager.lambda$createRttControllerIfPossible$22(this.f$0, wifiStatus, iWifiRttController);
                            }
                        });
                    } catch (RemoteException e) {
                        Log.e(TAG, "IWifiChip.createRttController exception: " + e);
                    }
                    if (rttResp.value != null) {
                        return (IWifiRttController) rttResp.value;
                    }
                }
            }
            Log.w(TAG, "createRttControllerIfPossible: not available from any of the chips");
            return null;
        }
    }

    static /* synthetic */ void lambda$createRttControllerIfPossible$22(HidlSupport.Mutable rttResp, WifiStatus status, IWifiRttController rtt) {
        if (status.code == 0) {
            rttResp.value = rtt;
            return;
        }
        Log.e(TAG, "IWifiChip.createRttController failed: " + statusString(status));
    }

    /* access modifiers changed from: private */
    public static String statusString(WifiStatus status) {
        if (status == null) {
            return "status=null";
        }
        return status.code + " (" + status.description + ")";
    }

    private static int getType(IWifiIface iface) {
        MutableInt typeResp = new MutableInt(-1);
        try {
            iface.getType(new IWifiIface.getTypeCallback(typeResp) {
                /* class com.android.server.wifi.$$Lambda$HalDeviceManager$ErxCpEghr4yhQpGHX1NQPumvouc */
                private final /* synthetic */ MutableInt f$0;

                {
                    this.f$0 = r1;
                }

                @Override // android.hardware.wifi.V1_0.IWifiIface.getTypeCallback
                public final void onValues(WifiStatus wifiStatus, int i) {
                    HalDeviceManager.lambda$getType$23(this.f$0, wifiStatus, i);
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Exception on getType: " + e);
        }
        return typeResp.value;
    }

    static /* synthetic */ void lambda$getType$23(MutableInt typeResp, WifiStatus status, int type) {
        if (status.code == 0) {
            typeResp.value = type;
            return;
        }
        Log.e(TAG, "Error on getType: " + statusString(status));
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("HalDeviceManager:");
        pw.println("  mServiceManager: " + this.mServiceManager);
        pw.println("  mWifi: " + this.mWifi);
        pw.println("  mManagerStatusListeners: " + this.mManagerStatusListeners);
        pw.println("  mInterfaceAvailableForRequestListeners: " + this.mInterfaceAvailableForRequestListeners);
        pw.println("  mInterfaceInfoCache: " + this.mInterfaceInfoCache);
    }
}
