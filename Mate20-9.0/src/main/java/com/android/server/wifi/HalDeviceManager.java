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
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HalDeviceManager {
    @VisibleForTesting
    public static final String HAL_INSTANCE_NAME = "default";
    /* access modifiers changed from: private */
    public static final int[] IFACE_TYPES_BY_PRIORITY = {1, 0, 2, 3};
    private static final int MAX_SLEEP_RETRY_TIMES = 40;
    private static final int SLEEP_TIME_RETRY = 50;
    private static final int START_HAL_RETRY_INTERVAL_MS = 20;
    @VisibleForTesting
    public static final int START_HAL_RETRY_TIMES = 3;
    private static final String TAG = "HalDevMgr";
    private static final boolean VDBG = false;
    private final Clock mClock;
    private boolean mDbg = false;
    private final SparseArray<IWifiChipEventCallback.Stub> mDebugCallbacks = new SparseArray<>();
    private final IHwBinder.DeathRecipient mIWifiDeathRecipient = new IHwBinder.DeathRecipient() {
        public final void serviceDied(long j) {
            HalDeviceManager.lambda$new$3(HalDeviceManager.this, j);
        }
    };
    private final SparseArray<Map<InterfaceAvailableForRequestListenerProxy, Boolean>> mInterfaceAvailableForRequestListeners = new SparseArray<>();
    private final Map<Pair<String, Integer>, InterfaceCacheEntry> mInterfaceInfoCache = new HashMap();
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final Set<ManagerStatusListenerProxy> mManagerStatusListeners = new HashSet();
    private IServiceManager mServiceManager;
    private final IHwBinder.DeathRecipient mServiceManagerDeathRecipient = new IHwBinder.DeathRecipient() {
        public final void serviceDied(long j) {
            HalDeviceManager.lambda$new$2(HalDeviceManager.this, j);
        }
    };
    private final IServiceNotification mServiceNotificationCallback = new IServiceNotification.Stub() {
        public void onRegistration(String fqName, String name, boolean preexisting) {
            Log.d(HalDeviceManager.TAG, "IWifi registration notification: fqName=" + fqName + ", name=" + name + ", preexisting=" + preexisting);
            synchronized (HalDeviceManager.this.mLock) {
                HalDeviceManager.this.initIWifiIfNecessary();
            }
        }
    };
    private IWifi mWifi;
    private final WifiEventCallback mWifiEventCallback = new WifiEventCallback();

    private class IfaceCreationData {
        public WifiChipInfo chipInfo;
        public int chipModeId;
        public List<WifiIfaceInfo> interfacesToBeRemovedFirst;

        private IfaceCreationData() {
        }

        public String toString() {
            return "{chipInfo=" + this.chipInfo + ", chipModeId=" + this.chipModeId + ", interfacesToBeRemovedFirst=" + this.interfacesToBeRemovedFirst + ")";
        }
    }

    public interface InterfaceAvailableForRequestListener {
        void onAvailabilityChanged(boolean z);
    }

    private class InterfaceAvailableForRequestListenerProxy extends ListenerProxy<InterfaceAvailableForRequestListener> {
        InterfaceAvailableForRequestListenerProxy(InterfaceAvailableForRequestListener destroyedListener, Handler handler) {
            super(destroyedListener, handler, "InterfaceAvailableForRequestListenerProxy");
        }

        /* access modifiers changed from: protected */
        public void actionWithArg(boolean isAvailable) {
            ((InterfaceAvailableForRequestListener) this.mListener).onAvailabilityChanged(isAvailable);
        }
    }

    private class InterfaceCacheEntry {
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

    public interface InterfaceDestroyedListener {
        void onDestroyed(String str);
    }

    private class InterfaceDestroyedListenerProxy extends ListenerProxy<InterfaceDestroyedListener> {
        private final String mIfaceName;

        InterfaceDestroyedListenerProxy(String ifaceName, InterfaceDestroyedListener destroyedListener, Handler handler) {
            super(destroyedListener, handler, "InterfaceDestroyedListenerProxy");
            this.mIfaceName = ifaceName;
        }

        /* access modifiers changed from: protected */
        public void action() {
            ((InterfaceDestroyedListener) this.mListener).onDestroyed(this.mIfaceName);
        }
    }

    private abstract class ListenerProxy<LISTENER> {
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
            if (this.mHandler != null) {
                this.mHandler.post(new Runnable() {
                    public final void run() {
                        HalDeviceManager.ListenerProxy.this.action();
                    }
                });
            } else {
                action();
            }
        }

        /* access modifiers changed from: package-private */
        public void triggerWithArg(boolean arg) {
            if (this.mHandler != null) {
                this.mHandler.post(new Runnable(arg) {
                    private final /* synthetic */ boolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        HalDeviceManager.ListenerProxy.this.actionWithArg(this.f$1);
                    }
                });
            } else {
                actionWithArg(arg);
            }
        }

        /* access modifiers changed from: protected */
        public void action() {
        }

        /* access modifiers changed from: protected */
        public void actionWithArg(boolean arg) {
        }

        ListenerProxy(LISTENER listener, Handler handler, String tag) {
            this.mListener = listener;
            this.mHandler = handler;
        }
    }

    public interface ManagerStatusListener {
        void onStatusChanged();
    }

    private class ManagerStatusListenerProxy extends ListenerProxy<ManagerStatusListener> {
        ManagerStatusListenerProxy(ManagerStatusListener statusListener, Handler handler) {
            super(statusListener, handler, "ManagerStatusListenerProxy");
        }

        /* access modifiers changed from: protected */
        public void action() {
            ((ManagerStatusListener) this.mListener).onStatusChanged();
        }
    }

    private class WifiChipInfo {
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
            for (int type : HalDeviceManager.IFACE_TYPES_BY_PRIORITY) {
                sb.append(", ifaces[" + type + "].length=");
                sb.append(this.ifaces[type].length);
            }
            sb.append(")");
            return sb.toString();
        }
    }

    private class WifiEventCallback extends IWifiEventCallback.Stub {
        private WifiEventCallback() {
        }

        public void onStart() throws RemoteException {
        }

        public void onStop() throws RemoteException {
        }

        public void onFailure(WifiStatus status) throws RemoteException {
            Log.e(HalDeviceManager.TAG, "IWifiEventCallback.onFailure: " + HalDeviceManager.statusString(status));
            HalDeviceManager.this.teardownInternal();
        }
    }

    private class WifiIfaceInfo {
        public IWifiIface iface;
        public String name;

        private WifiIfaceInfo() {
        }
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

    public void registerStatusListener(ManagerStatusListener listener, Handler handler) {
        synchronized (this.mLock) {
            if (!this.mManagerStatusListeners.add(new ManagerStatusListenerProxy(listener, handler))) {
                Log.w(TAG, "registerStatusListener: duplicate registration ignored");
            }
        }
    }

    public boolean isSupported() {
        return isSupportedInternal();
    }

    public boolean isReady() {
        return this.mWifi != null;
    }

    public boolean isStarted() {
        return isWifiStarted();
    }

    public boolean start() {
        return startWifi();
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
            IWifiChip iWifiChip = cacheEntry.chip;
            return iWifiChip;
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
            boolean add = cacheEntry.destroyedListeners.add(new InterfaceDestroyedListenerProxy(name, destroyedListener, handler));
            return add;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0029, code lost:
        if (r0 != null) goto L_0x0033;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002b, code lost:
        android.util.Log.e(TAG, "registerInterfaceAvailableForRequestListener: no chip info found - but possibly registered pre-started - ignoring");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0032, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0033, code lost:
        dispatchAvailableForRequestListenersForType(r5, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0036, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0025, code lost:
        r0 = getAllChipInfo();
     */
    public void registerInterfaceAvailableForRequestListener(int ifaceType, InterfaceAvailableForRequestListener listener, Handler handler) {
        synchronized (this.mLock) {
            InterfaceAvailableForRequestListenerProxy proxy = new InterfaceAvailableForRequestListenerProxy(listener, handler);
            if (!this.mInterfaceAvailableForRequestListeners.get(ifaceType).containsKey(proxy)) {
                this.mInterfaceAvailableForRequestListeners.get(ifaceType).put(proxy, null);
            }
        }
    }

    public void unregisterInterfaceAvailableForRequestListener(int ifaceType, InterfaceAvailableForRequestListener listener) {
        synchronized (this.mLock) {
            this.mInterfaceAvailableForRequestListeners.get(ifaceType).remove(new InterfaceAvailableForRequestListenerProxy(listener, null));
        }
    }

    public static String getName(IWifiIface iface) {
        if (iface == null) {
            return "<null>";
        }
        HidlSupport.Mutable<String> nameResp = new HidlSupport.Mutable<>();
        try {
            iface.getName(new IWifiIface.getNameCallback() {
                public final void onValues(WifiStatus wifiStatus, String str) {
                    HalDeviceManager.lambda$getName$0(HidlSupport.Mutable.this, wifiStatus, str);
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

    public IWifiRttController createRttController() {
        synchronized (this.mLock) {
            if (this.mWifi == null) {
                Log.e(TAG, "createRttController: null IWifi");
                return null;
            }
            WifiChipInfo[] chipInfos = getAllChipInfo();
            if (chipInfos == null) {
                Log.e(TAG, "createRttController: no chip info found");
                stopWifi();
                return null;
            }
            for (WifiChipInfo chipInfo : chipInfos) {
                HidlSupport.Mutable<IWifiRttController> rttResp = new HidlSupport.Mutable<>();
                try {
                    chipInfo.chip.createRttController(null, new IWifiChip.createRttControllerCallback() {
                        public final void onValues(WifiStatus wifiStatus, IWifiRttController iWifiRttController) {
                            HalDeviceManager.lambda$createRttController$1(HidlSupport.Mutable.this, wifiStatus, iWifiRttController);
                        }
                    });
                } catch (RemoteException e) {
                    Log.e(TAG, "IWifiChip.createRttController exception: " + e);
                }
                if (rttResp.value != null) {
                    IWifiRttController iWifiRttController = (IWifiRttController) rttResp.value;
                    return iWifiRttController;
                }
            }
            Log.e(TAG, "createRttController: not available from any of the chips");
            return null;
        }
    }

    static /* synthetic */ void lambda$createRttController$1(HidlSupport.Mutable rttResp, WifiStatus status, IWifiRttController rtt) {
        if (status.code == 0) {
            rttResp.value = rtt;
            return;
        }
        Log.e(TAG, "IWifiChip.createRttController failed: " + statusString(status));
    }

    /* access modifiers changed from: protected */
    public IWifi getWifiServiceMockable() {
        try {
            return IWifi.getService();
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
        if (isSupportedInternal()) {
            initIWifiIfNecessary();
        }
    }

    /* access modifiers changed from: private */
    public void teardownInternal() {
        managerStatusListenerDispatch();
        dispatchAllDestroyedListeners();
        this.mInterfaceAvailableForRequestListeners.get(0).clear();
        this.mInterfaceAvailableForRequestListeners.get(1).clear();
        this.mInterfaceAvailableForRequestListeners.get(2).clear();
        this.mInterfaceAvailableForRequestListeners.get(3).clear();
    }

    public static /* synthetic */ void lambda$new$2(HalDeviceManager halDeviceManager, long cookie) {
        Log.wtf(TAG, "IServiceManager died: cookie=" + cookie);
        synchronized (halDeviceManager.mLock) {
            halDeviceManager.mServiceManager = null;
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
                        } else if (!this.mServiceManager.registerForNotifications(IWifi.kInterfaceName, "", this.mServiceNotificationCallback)) {
                            Log.wtf(TAG, "Failed to register a listener for IWifi service");
                            this.mServiceManager = null;
                        }
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
                if (this.mServiceManager.getTransport(IWifi.kInterfaceName, HAL_INSTANCE_NAME) != 0) {
                    z = true;
                }
                return z;
            } catch (RemoteException e) {
                Log.wtf(TAG, "Exception while operating on IServiceManager: " + e);
                return false;
            }
        }
    }

    public static /* synthetic */ void lambda$new$3(HalDeviceManager halDeviceManager, long cookie) {
        Log.e(TAG, "IWifi HAL service died! Have a listener for it ... cookie=" + cookie);
        synchronized (halDeviceManager.mLock) {
            halDeviceManager.mWifi = null;
            halDeviceManager.teardownInternal();
        }
    }

    /* access modifiers changed from: private */
    public void initIWifiIfNecessary() {
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
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Exception while operating on IWifi: " + e);
                }
            }
        }
    }

    private void initIWifiChipDebugListeners() {
    }

    private static /* synthetic */ void lambda$initIWifiChipDebugListeners$4(MutableBoolean statusOk, HidlSupport.Mutable chipIdsResp, WifiStatus status, ArrayList chipIds) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            chipIdsResp.value = chipIds;
            return;
        }
        Log.e(TAG, "getChipIds failed: " + statusString(status));
    }

    private static /* synthetic */ void lambda$initIWifiChipDebugListeners$5(MutableBoolean statusOk, HidlSupport.Mutable chipResp, WifiStatus status, IWifiChip chip) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            chipResp.value = chip;
            return;
        }
        Log.e(TAG, "getChip failed: " + statusString(status));
    }

    private WifiChipInfo[] getAllChipInfo() {
        synchronized (this.mLock) {
            WifiChipInfo[] wifiChipInfoArr = null;
            if (this.mWifi == null) {
                Log.e(TAG, "getAllChipInfo: called but mWifi is null!?");
                return null;
            }
            try {
                boolean z = false;
                MutableBoolean statusOk = new MutableBoolean(false);
                HidlSupport.Mutable<ArrayList<Integer>> chipIdsResp = new HidlSupport.Mutable<>();
                this.mWifi.getChipIds(new IWifi.getChipIdsCallback(statusOk, chipIdsResp) {
                    private final /* synthetic */ MutableBoolean f$0;
                    private final /* synthetic */ HidlSupport.Mutable f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                        HalDeviceManager.lambda$getAllChipInfo$6(this.f$0, this.f$1, wifiStatus, arrayList);
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
                Iterator it = ((ArrayList) chipIdsResp.value).iterator();
                int chipInfoIndex = 0;
                while (it.hasNext() != 0) {
                    Integer chipId = (Integer) it.next();
                    this.mWifi.getChip(chipId.intValue(), new IWifi.getChipCallback(statusOk, chipResp) {
                        private final /* synthetic */ MutableBoolean f$0;
                        private final /* synthetic */ HidlSupport.Mutable f$1;

                        {
                            this.f$0 = r1;
                            this.f$1 = r2;
                        }

                        public final void onValues(WifiStatus wifiStatus, IWifiChip iWifiChip) {
                            HalDeviceManager.lambda$getAllChipInfo$7(this.f$0, this.f$1, wifiStatus, iWifiChip);
                        }
                    });
                    if (!statusOk.value) {
                        Integer num = chipId;
                        HidlSupport.Mutable<ArrayList<Integer>> mutable = chipIdsResp;
                        WifiChipInfo[] wifiChipInfoArr2 = chipsInfo;
                        int i = chipInfoIndex;
                    } else if (chipResp.value == null) {
                        Integer num2 = chipId;
                        HidlSupport.Mutable<ArrayList<Integer>> mutable2 = chipIdsResp;
                        WifiChipInfo[] wifiChipInfoArr3 = chipsInfo;
                        int i2 = chipInfoIndex;
                    } else {
                        HidlSupport.Mutable<ArrayList<IWifiChip.ChipMode>> availableModesResp = new HidlSupport.Mutable<>();
                        ((IWifiChip) chipResp.value).getAvailableModes(new IWifiChip.getAvailableModesCallback(statusOk, availableModesResp) {
                            private final /* synthetic */ MutableBoolean f$0;
                            private final /* synthetic */ HidlSupport.Mutable f$1;

                            {
                                this.f$0 = r1;
                                this.f$1 = r2;
                            }

                            public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                                HalDeviceManager.lambda$getAllChipInfo$8(this.f$0, this.f$1, wifiStatus, arrayList);
                            }
                        });
                        if (!statusOk.value) {
                            return wifiChipInfoArr;
                        }
                        MutableBoolean currentModeValidResp = new MutableBoolean(z);
                        MutableInt currentModeResp = new MutableInt(z ? 1 : 0);
                        ((IWifiChip) chipResp.value).getMode(new IWifiChip.getModeCallback(statusOk, currentModeValidResp, currentModeResp) {
                            private final /* synthetic */ MutableBoolean f$0;
                            private final /* synthetic */ MutableBoolean f$1;
                            private final /* synthetic */ MutableInt f$2;

                            {
                                this.f$0 = r1;
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            public final void onValues(WifiStatus wifiStatus, int i) {
                                HalDeviceManager.lambda$getAllChipInfo$9(this.f$0, this.f$1, this.f$2, wifiStatus, i);
                            }
                        });
                        if (!statusOk.value) {
                            return wifiChipInfoArr;
                        }
                        HidlSupport.Mutable<ArrayList<String>> ifaceNamesResp = new HidlSupport.Mutable<>();
                        MutableInt ifaceIndex = new MutableInt(z);
                        ((IWifiChip) chipResp.value).getStaIfaceNames(new IWifiChip.getStaIfaceNamesCallback(statusOk, ifaceNamesResp) {
                            private final /* synthetic */ MutableBoolean f$0;
                            private final /* synthetic */ HidlSupport.Mutable f$1;

                            {
                                this.f$0 = r1;
                                this.f$1 = r2;
                            }

                            public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                                HalDeviceManager.lambda$getAllChipInfo$10(this.f$0, this.f$1, wifiStatus, arrayList);
                            }
                        });
                        if (!statusOk.value) {
                            return null;
                        }
                        WifiIfaceInfo[] staIfaces = new WifiIfaceInfo[((ArrayList) ifaceNamesResp.value).size()];
                        Iterator it2 = ((ArrayList) ifaceNamesResp.value).iterator();
                        while (it2.hasNext()) {
                            String ifaceName = (String) it2.next();
                            Iterator it3 = it2;
                            HidlSupport.Mutable<ArrayList<Integer>> chipIdsResp2 = chipIdsResp;
                            MutableInt ifaceIndex2 = ifaceIndex;
                            IWifiChip iWifiChip = (IWifiChip) chipResp.value;
                            Iterator it4 = it;
                            HidlSupport.Mutable<ArrayList<String>> ifaceNamesResp2 = ifaceNamesResp;
                            MutableInt currentModeResp2 = currentModeResp;
                            MutableBoolean currentModeValidResp2 = currentModeValidResp;
                            HidlSupport.Mutable<ArrayList<IWifiChip.ChipMode>> availableModesResp2 = availableModesResp;
                            WifiIfaceInfo[] wifiIfaceInfoArr = staIfaces;
                            WifiIfaceInfo[] staIfaces2 = staIfaces;
                            Integer chipId2 = chipId;
                            $$Lambda$HalDeviceManager$HLPmFjXA6r19Ma_sML3KIFjYXI8 r1 = new IWifiChip.getStaIfaceCallback(statusOk, ifaceName, wifiIfaceInfoArr, ifaceIndex2) {
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

                                public final void onValues(WifiStatus wifiStatus, IWifiStaIface iWifiStaIface) {
                                    HalDeviceManager.lambda$getAllChipInfo$11(HalDeviceManager.this, this.f$1, this.f$2, this.f$3, this.f$4, wifiStatus, iWifiStaIface);
                                }
                            };
                            iWifiChip.getStaIface(ifaceName, r1);
                            if (!statusOk.value) {
                                return null;
                            }
                            chipId = chipId2;
                            ifaceNamesResp = ifaceNamesResp2;
                            it2 = it3;
                            ifaceIndex = ifaceIndex2;
                            chipIdsResp = chipIdsResp2;
                            it = it4;
                            currentModeResp = currentModeResp2;
                            currentModeValidResp = currentModeValidResp2;
                            availableModesResp = availableModesResp2;
                            staIfaces = staIfaces2;
                        }
                        MutableInt currentModeResp3 = currentModeResp;
                        MutableBoolean currentModeValidResp3 = currentModeValidResp;
                        HidlSupport.Mutable<ArrayList<IWifiChip.ChipMode>> availableModesResp3 = availableModesResp;
                        WifiIfaceInfo[] staIfaces3 = staIfaces;
                        HidlSupport.Mutable<ArrayList<Integer>> chipIdsResp3 = chipIdsResp;
                        Iterator it5 = it;
                        HidlSupport.Mutable<ArrayList<String>> ifaceNamesResp3 = ifaceNamesResp;
                        Integer chipId3 = chipId;
                        MutableInt ifaceIndex3 = ifaceIndex;
                        ifaceIndex3.value = 0;
                        ((IWifiChip) chipResp.value).getApIfaceNames(new IWifiChip.getApIfaceNamesCallback(statusOk, ifaceNamesResp3) {
                            private final /* synthetic */ MutableBoolean f$0;
                            private final /* synthetic */ HidlSupport.Mutable f$1;

                            {
                                this.f$0 = r1;
                                this.f$1 = r2;
                            }

                            public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                                HalDeviceManager.lambda$getAllChipInfo$12(this.f$0, this.f$1, wifiStatus, arrayList);
                            }
                        });
                        if (!statusOk.value) {
                            return null;
                        }
                        WifiIfaceInfo[] apIfaces = new WifiIfaceInfo[((ArrayList) ifaceNamesResp3.value).size()];
                        Iterator it6 = ((ArrayList) ifaceNamesResp3.value).iterator();
                        while (it6.hasNext()) {
                            String ifaceName2 = (String) it6.next();
                            Integer chipId4 = chipId3;
                            $$Lambda$HalDeviceManager$LisNucJKN8TgUZ4F_hMe1s79mng r9 = r1;
                            WifiChipInfo[] chipsInfo2 = chipsInfo;
                            int chipInfoIndex2 = chipInfoIndex;
                            Iterator it7 = it6;
                            $$Lambda$HalDeviceManager$LisNucJKN8TgUZ4F_hMe1s79mng r12 = new IWifiChip.getApIfaceCallback(statusOk, ifaceName2, apIfaces, ifaceIndex3) {
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

                                public final void onValues(WifiStatus wifiStatus, IWifiApIface iWifiApIface) {
                                    HalDeviceManager.lambda$getAllChipInfo$13(HalDeviceManager.this, this.f$1, this.f$2, this.f$3, this.f$4, wifiStatus, iWifiApIface);
                                }
                            };
                            ((IWifiChip) chipResp.value).getApIface(ifaceName2, r9);
                            if (!statusOk.value) {
                                return null;
                            }
                            it6 = it7;
                            chipId3 = chipId4;
                            chipsInfo = chipsInfo2;
                            chipInfoIndex = chipInfoIndex2;
                        }
                        Integer chipId5 = chipId3;
                        WifiChipInfo[] chipsInfo3 = chipsInfo;
                        int chipInfoIndex3 = chipInfoIndex;
                        ifaceIndex3.value = 0;
                        ((IWifiChip) chipResp.value).getP2pIfaceNames(new IWifiChip.getP2pIfaceNamesCallback(statusOk, ifaceNamesResp3) {
                            private final /* synthetic */ MutableBoolean f$0;
                            private final /* synthetic */ HidlSupport.Mutable f$1;

                            {
                                this.f$0 = r1;
                                this.f$1 = r2;
                            }

                            public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                                HalDeviceManager.lambda$getAllChipInfo$14(this.f$0, this.f$1, wifiStatus, arrayList);
                            }
                        });
                        if (!statusOk.value) {
                            return null;
                        }
                        WifiIfaceInfo[] p2pIfaces = new WifiIfaceInfo[((ArrayList) ifaceNamesResp3.value).size()];
                        Iterator it8 = ((ArrayList) ifaceNamesResp3.value).iterator();
                        while (it8.hasNext()) {
                            String ifaceName3 = (String) it8.next();
                            Iterator it9 = it8;
                            $$Lambda$HalDeviceManager$ynHs4R12k_5_9Qxr5asWSHdsuE4 r122 = r1;
                            WifiIfaceInfo[] wifiIfaceInfoArr2 = p2pIfaces;
                            WifiIfaceInfo[] p2pIfaces2 = p2pIfaces;
                            IWifiChip iWifiChip2 = (IWifiChip) chipResp.value;
                            $$Lambda$HalDeviceManager$ynHs4R12k_5_9Qxr5asWSHdsuE4 r13 = new IWifiChip.getP2pIfaceCallback(statusOk, ifaceName3, wifiIfaceInfoArr2, ifaceIndex3) {
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

                                public final void onValues(WifiStatus wifiStatus, IWifiP2pIface iWifiP2pIface) {
                                    HalDeviceManager.lambda$getAllChipInfo$15(HalDeviceManager.this, this.f$1, this.f$2, this.f$3, this.f$4, wifiStatus, iWifiP2pIface);
                                }
                            };
                            iWifiChip2.getP2pIface(ifaceName3, r122);
                            if (!statusOk.value) {
                                return null;
                            }
                            it8 = it9;
                            p2pIfaces = p2pIfaces2;
                        }
                        WifiIfaceInfo[] p2pIfaces3 = p2pIfaces;
                        ifaceIndex3.value = 0;
                        ((IWifiChip) chipResp.value).getNanIfaceNames(new IWifiChip.getNanIfaceNamesCallback(statusOk, ifaceNamesResp3) {
                            private final /* synthetic */ MutableBoolean f$0;
                            private final /* synthetic */ HidlSupport.Mutable f$1;

                            {
                                this.f$0 = r1;
                                this.f$1 = r2;
                            }

                            public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
                                HalDeviceManager.lambda$getAllChipInfo$16(this.f$0, this.f$1, wifiStatus, arrayList);
                            }
                        });
                        if (!statusOk.value) {
                            return null;
                        }
                        WifiIfaceInfo[] nanIfaces = new WifiIfaceInfo[((ArrayList) ifaceNamesResp3.value).size()];
                        Iterator it10 = ((ArrayList) ifaceNamesResp3.value).iterator();
                        while (it10.hasNext()) {
                            String ifaceName4 = (String) it10.next();
                            Iterator it11 = it10;
                            $$Lambda$HalDeviceManager$OTxRCq8TAZZlX8UFhmqaHcpXJYQ r123 = r1;
                            HidlSupport.Mutable<ArrayList<String>> ifaceNamesResp4 = ifaceNamesResp3;
                            IWifiChip iWifiChip3 = (IWifiChip) chipResp.value;
                            $$Lambda$HalDeviceManager$OTxRCq8TAZZlX8UFhmqaHcpXJYQ r14 = new IWifiChip.getNanIfaceCallback(statusOk, ifaceName4, nanIfaces, ifaceIndex3) {
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

                                public final void onValues(WifiStatus wifiStatus, IWifiNanIface iWifiNanIface) {
                                    HalDeviceManager.lambda$getAllChipInfo$17(HalDeviceManager.this, this.f$1, this.f$2, this.f$3, this.f$4, wifiStatus, iWifiNanIface);
                                }
                            };
                            iWifiChip3.getNanIface(ifaceName4, r123);
                            if (!statusOk.value) {
                                return null;
                            }
                            it10 = it11;
                            ifaceNamesResp3 = ifaceNamesResp4;
                        }
                        WifiChipInfo chipInfo = new WifiChipInfo();
                        chipInfoIndex = chipInfoIndex3 + 1;
                        chipsInfo3[chipInfoIndex3] = chipInfo;
                        chipInfo.chip = (IWifiChip) chipResp.value;
                        chipInfo.chipId = chipId5.intValue();
                        chipInfo.availableModes = (ArrayList) availableModesResp3.value;
                        chipInfo.currentModeIdValid = currentModeValidResp3.value;
                        chipInfo.currentModeId = currentModeResp3.value;
                        chipInfo.ifaces[0] = staIfaces3;
                        chipInfo.ifaces[1] = apIfaces;
                        chipInfo.ifaces[2] = p2pIfaces3;
                        chipInfo.ifaces[3] = nanIfaces;
                        z = false;
                        chipIdsResp = chipIdsResp3;
                        it = it5;
                        chipsInfo = chipsInfo3;
                        wifiChipInfoArr = null;
                    }
                    return null;
                }
                WifiChipInfo[] chipsInfo4 = chipsInfo;
                int i3 = chipInfoIndex;
                return chipsInfo4;
            } catch (RemoteException e) {
                Log.e(TAG, "getAllChipInfoAndValidateCache exception: " + e);
                return null;
            } catch (NullPointerException ex) {
                Log.e(TAG, "getAllChipInfoAndValidateCache NullPointerException ", ex);
                return null;
            }
        }
    }

    static /* synthetic */ void lambda$getAllChipInfo$6(MutableBoolean statusOk, HidlSupport.Mutable chipIdsResp, WifiStatus status, ArrayList chipIds) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            chipIdsResp.value = chipIds;
            return;
        }
        Log.e(TAG, "getChipIds failed: " + statusString(status));
    }

    static /* synthetic */ void lambda$getAllChipInfo$7(MutableBoolean statusOk, HidlSupport.Mutable chipResp, WifiStatus status, IWifiChip chip) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            chipResp.value = chip;
            return;
        }
        Log.e(TAG, "getChip failed: " + statusString(status));
    }

    static /* synthetic */ void lambda$getAllChipInfo$8(MutableBoolean statusOk, HidlSupport.Mutable availableModesResp, WifiStatus status, ArrayList modes) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            availableModesResp.value = modes;
            return;
        }
        Log.e(TAG, "getAvailableModes failed: " + statusString(status));
    }

    static /* synthetic */ void lambda$getAllChipInfo$9(MutableBoolean statusOk, MutableBoolean currentModeValidResp, MutableInt currentModeResp, WifiStatus status, int modeId) {
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

    static /* synthetic */ void lambda$getAllChipInfo$10(MutableBoolean statusOk, HidlSupport.Mutable ifaceNamesResp, WifiStatus status, ArrayList ifnames) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            ifaceNamesResp.value = ifnames;
            return;
        }
        Log.e(TAG, "getStaIfaceNames failed: " + statusString(status));
    }

    public static /* synthetic */ void lambda$getAllChipInfo$11(HalDeviceManager halDeviceManager, MutableBoolean statusOk, String ifaceName, WifiIfaceInfo[] staIfaces, MutableInt ifaceIndex, WifiStatus status, IWifiStaIface iface) {
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

    static /* synthetic */ void lambda$getAllChipInfo$12(MutableBoolean statusOk, HidlSupport.Mutable ifaceNamesResp, WifiStatus status, ArrayList ifnames) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            ifaceNamesResp.value = ifnames;
            return;
        }
        Log.e(TAG, "getApIfaceNames failed: " + statusString(status));
    }

    public static /* synthetic */ void lambda$getAllChipInfo$13(HalDeviceManager halDeviceManager, MutableBoolean statusOk, String ifaceName, WifiIfaceInfo[] apIfaces, MutableInt ifaceIndex, WifiStatus status, IWifiApIface iface) {
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

    static /* synthetic */ void lambda$getAllChipInfo$14(MutableBoolean statusOk, HidlSupport.Mutable ifaceNamesResp, WifiStatus status, ArrayList ifnames) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            ifaceNamesResp.value = ifnames;
            return;
        }
        Log.e(TAG, "getP2pIfaceNames failed: " + statusString(status));
    }

    public static /* synthetic */ void lambda$getAllChipInfo$15(HalDeviceManager halDeviceManager, MutableBoolean statusOk, String ifaceName, WifiIfaceInfo[] p2pIfaces, MutableInt ifaceIndex, WifiStatus status, IWifiP2pIface iface) {
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

    static /* synthetic */ void lambda$getAllChipInfo$16(MutableBoolean statusOk, HidlSupport.Mutable ifaceNamesResp, WifiStatus status, ArrayList ifnames) {
        statusOk.value = status.code == 0;
        if (statusOk.value) {
            ifaceNamesResp.value = ifnames;
            return;
        }
        Log.e(TAG, "getNanIfaceNames failed: " + statusString(status));
    }

    public static /* synthetic */ void lambda$getAllChipInfo$17(HalDeviceManager halDeviceManager, MutableBoolean statusOk, String ifaceName, WifiIfaceInfo[] nanIfaces, MutableInt ifaceIndex, WifiStatus status, IWifiNanIface iface) {
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
                boolean isStarted = this.mWifi.isStarted();
                return isStarted;
            } catch (RemoteException e) {
                Log.e(TAG, "isWifiStarted exception: " + e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private void ensureIWifiLocked() {
        int retries = 0;
        while (this.mWifi == null && retries < 40) {
            try {
                Log.e(TAG, "ensureIWifiLocked: sleep 50 ms");
                Thread.sleep(50);
                retries++;
            } catch (InterruptedException e) {
                Log.e(TAG, "ensureIWifiLocked: got an InterruptedException");
                return;
            }
        }
    }

    private boolean startWifi() {
        ensureIWifiLocked();
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
            } catch (Throwable th) {
                throw th;
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

    private void managerStatusListenerDispatch() {
        synchronized (this.mLock) {
            for (ManagerStatusListenerProxy cb : this.mManagerStatusListeners) {
                cb.trigger();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Set<Integer> getSupportedIfaceTypesInternal(IWifiChip chip) {
        IWifiChip iWifiChip = chip;
        Set<Integer> results = new HashSet<>();
        WifiChipInfo[] chipInfos = getAllChipInfo();
        if (chipInfos == null) {
            Log.e(TAG, "getSupportedIfaceTypesInternal: no chip info found");
            return results;
        }
        MutableInt chipIdIfProvided = new MutableInt(0);
        if (iWifiChip != null) {
            MutableBoolean statusOk = new MutableBoolean(false);
            try {
                iWifiChip.getId(new IWifiChip.getIdCallback(chipIdIfProvided, statusOk) {
                    private final /* synthetic */ MutableInt f$0;
                    private final /* synthetic */ MutableBoolean f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    public final void onValues(WifiStatus wifiStatus, int i) {
                        HalDeviceManager.lambda$getSupportedIfaceTypesInternal$18(this.f$0, this.f$1, wifiStatus, i);
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
            if (iWifiChip == null || wci.chipId == chipIdIfProvided.value) {
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

    static /* synthetic */ void lambda$getSupportedIfaceTypesInternal$18(MutableInt chipIdIfProvided, MutableBoolean statusOk, WifiStatus status, int id) {
        if (status.code == 0) {
            chipIdIfProvided.value = id;
            statusOk.value = true;
            return;
        }
        Log.e(TAG, "getSupportedIfaceTypesInternal: IWifiChip.getId() error: " + statusString(status));
        statusOk.value = false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005f, code lost:
        return r2;
     */
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
                if (iface != null && !dispatchAvailableForRequestListeners()) {
                    return null;
                }
            }
        }
    }

    private IWifiIface createIfaceIfPossible(WifiChipInfo[] chipInfos, int ifaceType, boolean lowPriority, InterfaceDestroyedListener destroyedListener, Handler handler) {
        WifiChipInfo[] wifiChipInfoArr = chipInfos;
        int i = ifaceType;
        InterfaceDestroyedListener interfaceDestroyedListener = destroyedListener;
        synchronized (this.mLock) {
            try {
                IfaceCreationData bestIfaceCreationProposal = null;
                for (WifiChipInfo chipInfo : wifiChipInfoArr) {
                    Iterator<IWifiChip.ChipMode> it = chipInfo.availableModes.iterator();
                    while (it.hasNext()) {
                        IWifiChip.ChipMode chipMode = it.next();
                        Iterator<IWifiChip.ChipIfaceCombination> it2 = chipMode.availableCombinations.iterator();
                        while (it2.hasNext()) {
                            IWifiChip.ChipIfaceCombination chipIfaceCombo = it2.next();
                            int[][] expandedIfaceCombos = expandIfaceCombos(chipIfaceCombo);
                            int length = expandedIfaceCombos.length;
                            IfaceCreationData bestIfaceCreationProposal2 = bestIfaceCreationProposal;
                            int i2 = 0;
                            while (i2 < length) {
                                int i3 = length;
                                int i4 = i2;
                                int[][] expandedIfaceCombos2 = expandedIfaceCombos;
                                IWifiChip.ChipIfaceCombination chipIfaceCombo2 = chipIfaceCombo;
                                Iterator<IWifiChip.ChipIfaceCombination> it3 = it2;
                                IWifiChip.ChipMode chipMode2 = chipMode;
                                IfaceCreationData currentProposal = canIfaceComboSupportRequest(chipInfo, chipMode, expandedIfaceCombos[i2], i, lowPriority);
                                if (compareIfaceCreationData(currentProposal, bestIfaceCreationProposal2)) {
                                    bestIfaceCreationProposal2 = currentProposal;
                                }
                                i2 = i4 + 1;
                                length = i3;
                                expandedIfaceCombos = expandedIfaceCombos2;
                                chipIfaceCombo = chipIfaceCombo2;
                                it2 = it3;
                                chipMode = chipMode2;
                            }
                            Iterator<IWifiChip.ChipIfaceCombination> it4 = it2;
                            IWifiChip.ChipMode chipMode3 = chipMode;
                            bestIfaceCreationProposal = bestIfaceCreationProposal2;
                        }
                    }
                }
                if (bestIfaceCreationProposal != null) {
                    IWifiIface iface = executeChipReconfiguration(bestIfaceCreationProposal, i);
                    if (iface != null) {
                        InterfaceCacheEntry cacheEntry = new InterfaceCacheEntry();
                        cacheEntry.chip = bestIfaceCreationProposal.chipInfo.chip;
                        cacheEntry.chipId = bestIfaceCreationProposal.chipInfo.chipId;
                        cacheEntry.name = getName(iface);
                        cacheEntry.type = i;
                        if (interfaceDestroyedListener != null) {
                            try {
                                cacheEntry.destroyedListeners.add(new InterfaceDestroyedListenerProxy(cacheEntry.name, interfaceDestroyedListener, handler));
                            } catch (Throwable th) {
                                th = th;
                                boolean z = lowPriority;
                                throw th;
                            }
                        } else {
                            Handler handler2 = handler;
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
                boolean z2 = lowPriority;
                Handler handler3 = handler;
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
                    while (true) {
                        if (i < length) {
                            int i2 = i;
                            int i3 = length;
                            int[][] expandedIfaceCombos2 = expandedIfaceCombos;
                            if (canIfaceComboSupportRequest(chipInfo, chipMode, expandedIfaceCombos[i], ifaceType, false) != null) {
                                return true;
                            }
                            i = i2 + 1;
                            length = i3;
                            expandedIfaceCombos = expandedIfaceCombos2;
                        }
                    }
                }
            }
        }
        return false;
    }

    private int[][] expandIfaceCombos(IWifiChip.ChipIfaceCombination chipIfaceCombo) {
        int numOfCombos = 1;
        Iterator<IWifiChip.ChipIfaceCombinationLimit> it = chipIfaceCombo.limits.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            IWifiChip.ChipIfaceCombinationLimit limit = it.next();
            for (int i = 0; i < limit.maxIfaces; i++) {
                numOfCombos *= limit.types.size();
            }
        }
        int[][] expandedIfaceCombos = (int[][]) Array.newInstance(int.class, new int[]{numOfCombos, IFACE_TYPES_BY_PRIORITY.length});
        int span = numOfCombos;
        Iterator<IWifiChip.ChipIfaceCombinationLimit> it2 = chipIfaceCombo.limits.iterator();
        while (it2.hasNext()) {
            IWifiChip.ChipIfaceCombinationLimit limit2 = it2.next();
            int span2 = span;
            for (int i2 = 0; i2 < limit2.maxIfaces; i2++) {
                span2 /= limit2.types.size();
                for (int k = 0; k < numOfCombos; k++) {
                    int[] iArr = expandedIfaceCombos[k];
                    int intValue = limit2.types.get((k / span2) % limit2.types.size()).intValue();
                    iArr[intValue] = iArr[intValue] + 1;
                }
            }
            span = span2;
        }
        return expandedIfaceCombos;
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
        for (int type : IFACE_TYPES_BY_PRIORITY) {
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
        boolean z = true;
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
        if (requestedIfaceType != 2) {
            return true;
        }
        if (existingIfaceType != 3) {
            z = false;
        }
        return z;
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

    /* JADX WARNING: Removed duplicated region for block: B:16:0x003d A[Catch:{ RemoteException -> 0x012d, all -> 0x012b }] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0055 A[Catch:{ RemoteException -> 0x012d, all -> 0x012b }] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x009b A[SYNTHETIC, Splitter:B:30:0x009b] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00c0 A[Catch:{ RemoteException -> 0x012d, all -> 0x012b }] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00c1 A[Catch:{ RemoteException -> 0x012d, all -> 0x012b }] */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00ce A[Catch:{ RemoteException -> 0x012d, all -> 0x012b }] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00db A[Catch:{ RemoteException -> 0x012d, all -> 0x012b }] */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00e8 A[Catch:{ RemoteException -> 0x012d, all -> 0x012b }] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00fd A[Catch:{ RemoteException -> 0x012d, all -> 0x012b }] */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0125 A[SYNTHETIC, Splitter:B:48:0x0125] */
    private IWifiIface executeChipReconfiguration(IfaceCreationData ifaceCreationData, int ifaceType) {
        boolean isModeConfigNeeded;
        HidlSupport.Mutable<WifiStatus> statusResp;
        if (this.mDbg) {
            Log.d(TAG, "executeChipReconfiguration: ifaceCreationData=" + ifaceCreationData + ", ifaceType=" + ifaceType);
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
                            for (WifiIfaceInfo[] ifaceInfos : ifaceCreationData.chipInfo.ifaces) {
                                for (WifiIfaceInfo ifaceInfo : r4[r6]) {
                                    removeIfaceInternal(ifaceInfo.iface);
                                }
                            }
                            if (ifaceCreationData.chipInfo.chip.configureChip(ifaceCreationData.chipModeId).code != 0) {
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
                        switch (ifaceType) {
                            case 0:
                                ifaceCreationData.chipInfo.chip.createStaIface(new IWifiChip.createStaIfaceCallback(ifaceResp) {
                                    private final /* synthetic */ HidlSupport.Mutable f$1;

                                    {
                                        this.f$1 = r2;
                                    }

                                    public final void onValues(WifiStatus wifiStatus, IWifiStaIface iWifiStaIface) {
                                        HalDeviceManager.lambda$executeChipReconfiguration$19(HidlSupport.Mutable.this, this.f$1, wifiStatus, iWifiStaIface);
                                    }
                                });
                                break;
                            case 1:
                                ifaceCreationData.chipInfo.chip.createApIface(new IWifiChip.createApIfaceCallback(ifaceResp) {
                                    private final /* synthetic */ HidlSupport.Mutable f$1;

                                    {
                                        this.f$1 = r2;
                                    }

                                    public final void onValues(WifiStatus wifiStatus, IWifiApIface iWifiApIface) {
                                        HalDeviceManager.lambda$executeChipReconfiguration$20(HidlSupport.Mutable.this, this.f$1, wifiStatus, iWifiApIface);
                                    }
                                });
                                break;
                            case 2:
                                ifaceCreationData.chipInfo.chip.createP2pIface(new IWifiChip.createP2pIfaceCallback(ifaceResp) {
                                    private final /* synthetic */ HidlSupport.Mutable f$1;

                                    {
                                        this.f$1 = r2;
                                    }

                                    public final void onValues(WifiStatus wifiStatus, IWifiP2pIface iWifiP2pIface) {
                                        HalDeviceManager.lambda$executeChipReconfiguration$21(HidlSupport.Mutable.this, this.f$1, wifiStatus, iWifiP2pIface);
                                    }
                                });
                                break;
                            case 3:
                                ifaceCreationData.chipInfo.chip.createNanIface(new IWifiChip.createNanIfaceCallback(ifaceResp) {
                                    private final /* synthetic */ HidlSupport.Mutable f$1;

                                    {
                                        this.f$1 = r2;
                                    }

                                    public final void onValues(WifiStatus wifiStatus, IWifiNanIface iWifiNanIface) {
                                        HalDeviceManager.lambda$executeChipReconfiguration$22(HidlSupport.Mutable.this, this.f$1, wifiStatus, iWifiNanIface);
                                    }
                                });
                                break;
                        }
                        if (((WifiStatus) statusResp.value).code == 0) {
                            Log.e(TAG, "executeChipReconfiguration: failed to create interface ifaceType=" + ifaceType + ": " + statusString((WifiStatus) statusResp.value));
                            return null;
                        }
                        IWifiIface iWifiIface = (IWifiIface) ifaceResp.value;
                        return iWifiIface;
                    }
                }
                isModeConfigNeeded = true;
                if (this.mDbg) {
                }
                if (!isModeConfigNeeded) {
                }
                statusResp = new HidlSupport.Mutable<>();
                HidlSupport.Mutable<IWifiIface> ifaceResp2 = new HidlSupport.Mutable<>();
                switch (ifaceType) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
                if (((WifiStatus) statusResp.value).code == 0) {
                }
            } catch (RemoteException e) {
                Log.e(TAG, "executeChipReconfiguration exception: " + e);
                return null;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    static /* synthetic */ void lambda$executeChipReconfiguration$19(HidlSupport.Mutable statusResp, HidlSupport.Mutable ifaceResp, WifiStatus status, IWifiStaIface iface) {
        statusResp.value = status;
        ifaceResp.value = iface;
    }

    static /* synthetic */ void lambda$executeChipReconfiguration$20(HidlSupport.Mutable statusResp, HidlSupport.Mutable ifaceResp, WifiStatus status, IWifiApIface iface) {
        statusResp.value = status;
        ifaceResp.value = iface;
    }

    static /* synthetic */ void lambda$executeChipReconfiguration$21(HidlSupport.Mutable statusResp, HidlSupport.Mutable ifaceResp, WifiStatus status, IWifiP2pIface iface) {
        statusResp.value = status;
        ifaceResp.value = iface;
    }

    static /* synthetic */ void lambda$executeChipReconfiguration$22(HidlSupport.Mutable statusResp, HidlSupport.Mutable ifaceResp, WifiStatus status, IWifiNanIface iface) {
        statusResp.value = status;
        ifaceResp.value = iface;
    }

    private boolean removeIfaceInternal(IWifiIface iface) {
        String name = getName(iface);
        int type = getType(iface);
        if (this.mDbg) {
            Log.d(TAG, "removeIfaceInternal: iface(name)=" + name + ", type=" + type);
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
                switch (type) {
                    case 0:
                        status = chip.removeStaIface(name);
                        break;
                    case 1:
                        status = chip.removeApIface(name);
                        break;
                    case 2:
                        status = chip.removeP2pIface(name);
                        break;
                    case 3:
                        try {
                            status = chip.removeNanIface(name);
                            break;
                        } catch (RemoteException e) {
                            Log.e(TAG, "IWifiChip.removeXxxIface exception: " + e);
                            break;
                        }
                    default:
                        Log.wtf(TAG, "removeIfaceInternal: invalid type=" + type);
                        return false;
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
                InterfaceCacheEntry entry = (InterfaceCacheEntry) it.next().getValue();
                for (InterfaceDestroyedListenerProxy listener : entry.destroyedListeners) {
                    listener.trigger();
                }
                entry.destroyedListeners.clear();
                it.remove();
            }
        }
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
                private final /* synthetic */ MutableInt f$0;

                {
                    this.f$0 = r1;
                }

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
