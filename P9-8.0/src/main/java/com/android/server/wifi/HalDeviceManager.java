package com.android.server.wifi;

import android.hardware.wifi.V1_0.IWifi;
import android.hardware.wifi.V1_0.IWifiApIface;
import android.hardware.wifi.V1_0.IWifiChip;
import android.hardware.wifi.V1_0.IWifiChip.ChipIfaceCombination;
import android.hardware.wifi.V1_0.IWifiChip.ChipIfaceCombinationLimit;
import android.hardware.wifi.V1_0.IWifiChip.ChipMode;
import android.hardware.wifi.V1_0.IWifiEventCallback;
import android.hardware.wifi.V1_0.IWifiIface;
import android.hardware.wifi.V1_0.IWifiNanIface;
import android.hardware.wifi.V1_0.IWifiP2pIface;
import android.hardware.wifi.V1_0.IWifiRttController;
import android.hardware.wifi.V1_0.IWifiStaIface;
import android.hardware.wifi.V1_0.WifiStatus;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.hidl.manager.V1_0.IServiceNotification.Stub;
import android.os.Handler;
import android.os.IHwBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.MutableBoolean;
import android.util.MutableInt;
import android.util.SparseArray;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass10;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass11;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass12;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass13;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass14;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass15;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass16;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass17;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass18;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass19;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass20;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass21;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass22;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass23;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass3;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass4;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass5;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass7;
import com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass9;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class HalDeviceManager {
    private static final boolean DBG = false;
    public static final String HAL_INSTANCE_NAME = "default";
    private static final int[] IFACE_TYPES_BY_PRIORITY = new int[]{1, 0, 2, 3};
    private static final int MAX_SLEEP_RETRY_TIMES = 40;
    private static final int SLEEP_TIME_RETRY = 50;
    private static final int START_HAL_RETRY_INTERVAL_MS = 20;
    public static final int START_HAL_RETRY_TIMES = 3;
    private static final String TAG = "HalDeviceManager";
    private final DeathRecipient mIWifiDeathRecipient = new AnonymousClass4(this);
    private final SparseArray<Set<InterfaceAvailableForRequestListenerProxy>> mInterfaceAvailableForRequestListeners = new SparseArray();
    private final Map<IWifiIface, InterfaceCacheEntry> mInterfaceInfoCache = new HashMap();
    private final Object mLock = new Object();
    private final Set<ManagerStatusListenerProxy> mManagerStatusListeners = new HashSet();
    private IServiceManager mServiceManager;
    private final DeathRecipient mServiceManagerDeathRecipient = new AnonymousClass3(this);
    private final IServiceNotification mServiceNotificationCallback = new Stub() {
        public void onRegistration(String fqName, String name, boolean preexisting) {
            Log.d(HalDeviceManager.TAG, "IWifi registration notification: fqName=" + fqName + ", name=" + name + ", preexisting=" + preexisting);
            synchronized (HalDeviceManager.this.mLock) {
                HalDeviceManager.this.mWifi = null;
                HalDeviceManager.this.initIWifiIfNecessary();
                HalDeviceManager.this.stopWifi();
            }
        }
    };
    private IWifi mWifi;
    private final WifiEventCallback mWifiEventCallback = new WifiEventCallback(this, null);

    private class IfaceCreationData {
        public WifiChipInfo chipInfo;
        public int chipModeId;
        public List<WifiIfaceInfo> interfacesToBeRemovedFirst;

        /* synthetic */ IfaceCreationData(HalDeviceManager this$0, IfaceCreationData -this1) {
            this();
        }

        private IfaceCreationData() {
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{chipInfo=").append(this.chipInfo).append(", chipModeId=").append(this.chipModeId).append(", interfacesToBeRemovedFirst=").append(this.interfacesToBeRemovedFirst).append(")");
            return sb.toString();
        }
    }

    public interface InterfaceAvailableForRequestListener {
        void onAvailableForRequest();
    }

    private abstract class ListenerProxy<LISTENER> {
        private static final int LISTENER_TRIGGERED = 0;
        private Handler mHandler;
        protected LISTENER mListener;

        protected abstract void action();

        public boolean equals(Object obj) {
            return this.mListener == ((ListenerProxy) obj).mListener;
        }

        public int hashCode() {
            return this.mListener.hashCode();
        }

        void trigger() {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(0));
        }

        ListenerProxy(LISTENER listener, Looper looper, final String tag) {
            this.mListener = listener;
            this.mHandler = new Handler(looper) {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 0:
                            ListenerProxy.this.action();
                            return;
                        default:
                            Log.e(tag, "ListenerProxy.handleMessage: unknown message what=" + msg.what);
                            return;
                    }
                }
            };
        }
    }

    private class InterfaceAvailableForRequestListenerProxy extends ListenerProxy<InterfaceAvailableForRequestListener> {
        InterfaceAvailableForRequestListenerProxy(InterfaceAvailableForRequestListener destroyedListener, Looper looper) {
            super(destroyedListener, looper, "InterfaceAvailableForRequestListenerProxy");
        }

        protected void action() {
            ((InterfaceAvailableForRequestListener) this.mListener).onAvailableForRequest();
        }
    }

    private class InterfaceCacheEntry {
        public IWifiChip chip;
        public int chipId;
        public Set<InterfaceDestroyedListenerProxy> destroyedListeners;
        public String name;
        public int type;

        /* synthetic */ InterfaceCacheEntry(HalDeviceManager this$0, InterfaceCacheEntry -this1) {
            this();
        }

        private InterfaceCacheEntry() {
            this.destroyedListeners = new HashSet();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{name=").append(this.name).append(", type=").append(this.type).append(", destroyedListeners.size()=").append(this.destroyedListeners.size()).append("}");
            return sb.toString();
        }
    }

    public interface InterfaceDestroyedListener {
        void onDestroyed();
    }

    private class InterfaceDestroyedListenerProxy extends ListenerProxy<InterfaceDestroyedListener> {
        InterfaceDestroyedListenerProxy(InterfaceDestroyedListener destroyedListener, Looper looper) {
            super(destroyedListener, looper, "InterfaceDestroyedListenerProxy");
        }

        protected void action() {
            ((InterfaceDestroyedListener) this.mListener).onDestroyed();
        }
    }

    public interface ManagerStatusListener {
        void onStatusChanged();
    }

    private class ManagerStatusListenerProxy extends ListenerProxy<ManagerStatusListener> {
        ManagerStatusListenerProxy(ManagerStatusListener statusListener, Looper looper) {
            super(statusListener, looper, "ManagerStatusListenerProxy");
        }

        protected void action() {
            ((ManagerStatusListener) this.mListener).onStatusChanged();
        }
    }

    private static class Mutable<E> {
        public E value;

        Mutable() {
            this.value = null;
        }

        Mutable(E value) {
            this.value = value;
        }
    }

    private class WifiChipInfo {
        public ArrayList<ChipMode> availableModes;
        public IWifiChip chip;
        public int chipId;
        public int currentModeId;
        public boolean currentModeIdValid;
        public WifiIfaceInfo[][] ifaces;

        /* synthetic */ WifiChipInfo(HalDeviceManager this$0, WifiChipInfo -this1) {
            this();
        }

        private WifiChipInfo() {
            this.ifaces = new WifiIfaceInfo[HalDeviceManager.IFACE_TYPES_BY_PRIORITY.length][];
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{chipId=").append(this.chipId).append(", availableModes=").append(this.availableModes).append(", currentModeIdValid=").append(this.currentModeIdValid).append(", currentModeId=").append(this.currentModeId);
            for (int type : HalDeviceManager.IFACE_TYPES_BY_PRIORITY) {
                sb.append(", ifaces[").append(type).append("].length=").append(this.ifaces[type].length);
            }
            sb.append(")");
            return sb.toString();
        }
    }

    private class WifiEventCallback extends IWifiEventCallback.Stub {
        /* synthetic */ WifiEventCallback(HalDeviceManager this$0, WifiEventCallback -this1) {
            this();
        }

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

        /* synthetic */ WifiIfaceInfo(HalDeviceManager this$0, WifiIfaceInfo -this1) {
            this();
        }

        private WifiIfaceInfo() {
        }
    }

    public HalDeviceManager() {
        this.mInterfaceAvailableForRequestListeners.put(0, new HashSet());
        this.mInterfaceAvailableForRequestListeners.put(1, new HashSet());
        this.mInterfaceAvailableForRequestListeners.put(2, new HashSet());
        this.mInterfaceAvailableForRequestListeners.put(3, new HashSet());
    }

    public void initialize() {
        initializeInternal();
    }

    public void registerStatusListener(ManagerStatusListener listener, Looper looper) {
        synchronized (this.mLock) {
            Set set = this.mManagerStatusListeners;
            if (looper == null) {
                looper = Looper.myLooper();
            }
            if (!set.add(new ManagerStatusListenerProxy(listener, looper))) {
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

    Set<Integer> getSupportedIfaceTypes() {
        return getSupportedIfaceTypesInternal(null);
    }

    Set<Integer> getSupportedIfaceTypes(IWifiChip chip) {
        return getSupportedIfaceTypesInternal(chip);
    }

    public IWifiStaIface createStaIface(InterfaceDestroyedListener destroyedListener, Looper looper) {
        return (IWifiStaIface) createIface(0, destroyedListener, looper);
    }

    public IWifiApIface createApIface(InterfaceDestroyedListener destroyedListener, Looper looper) {
        return (IWifiApIface) createIface(1, destroyedListener, looper);
    }

    public IWifiP2pIface createP2pIface(InterfaceDestroyedListener destroyedListener, Looper looper) {
        return (IWifiP2pIface) createIface(2, destroyedListener, looper);
    }

    public IWifiNanIface createNanIface(InterfaceDestroyedListener destroyedListener, Looper looper) {
        return (IWifiNanIface) createIface(3, destroyedListener, looper);
    }

    public boolean removeIface(IWifiIface iface) {
        boolean success = removeIfaceInternal(iface);
        dispatchAvailableForRequestListeners();
        return success;
    }

    public IWifiChip getChip(IWifiIface iface) {
        synchronized (this.mLock) {
            InterfaceCacheEntry cacheEntry = (InterfaceCacheEntry) this.mInterfaceInfoCache.get(iface);
            if (cacheEntry == null) {
                Log.e(TAG, "getChip: no entry for iface(name)=" + getName(iface));
                return null;
            }
            IWifiChip iWifiChip = cacheEntry.chip;
            return iWifiChip;
        }
    }

    public boolean registerDestroyedListener(IWifiIface iface, InterfaceDestroyedListener destroyedListener, Looper looper) {
        synchronized (this.mLock) {
            InterfaceCacheEntry cacheEntry = (InterfaceCacheEntry) this.mInterfaceInfoCache.get(iface);
            if (cacheEntry == null) {
                Log.e(TAG, "registerDestroyedListener: no entry for iface(name)=" + getName(iface));
                return false;
            }
            Set set = cacheEntry.destroyedListeners;
            if (looper == null) {
                looper = Looper.myLooper();
            }
            boolean add = set.add(new InterfaceDestroyedListenerProxy(destroyedListener, looper));
            return add;
        }
    }

    public void registerInterfaceAvailableForRequestListener(int ifaceType, InterfaceAvailableForRequestListener listener, Looper looper) {
        Set set = (Set) this.mInterfaceAvailableForRequestListeners.get(ifaceType);
        if (looper == null) {
            looper = Looper.myLooper();
        }
        set.add(new InterfaceAvailableForRequestListenerProxy(listener, looper));
        WifiChipInfo[] chipInfos = getAllChipInfo();
        if (chipInfos == null) {
            Log.e(TAG, "registerInterfaceAvailableForRequestListener: no chip info found - but possibly registered pre-started - ignoring");
        } else {
            dispatchAvailableForRequestListenersForType(ifaceType, chipInfos);
        }
    }

    public void unregisterInterfaceAvailableForRequestListener(int ifaceType, InterfaceAvailableForRequestListener listener) {
        Iterator<InterfaceAvailableForRequestListenerProxy> it = ((Set) this.mInterfaceAvailableForRequestListeners.get(ifaceType)).iterator();
        while (it.hasNext()) {
            if (((InterfaceAvailableForRequestListenerProxy) it.next()).mListener == listener) {
                it.remove();
                return;
            }
        }
    }

    public static String getName(IWifiIface iface) {
        if (iface == null) {
            return "<null>";
        }
        Mutable<String> nameResp = new Mutable();
        try {
            iface.getName(new com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass1(nameResp));
        } catch (RemoteException e) {
            Log.e(TAG, "Exception on getName: " + e);
        }
        return (String) nameResp.value;
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_13199(Mutable nameResp, WifiStatus status, String name) {
        if (status.code == 0) {
            nameResp.value = name;
        } else {
            Log.e(TAG, "Error on getName: " + statusString(status));
        }
    }

    public IWifiRttController createRttController(IWifiIface boundIface) {
        synchronized (this.mLock) {
            if (this.mWifi == null) {
                Log.e(TAG, "createRttController: null IWifi -- boundIface(name)=" + getName(boundIface));
                return null;
            }
            IWifiChip chip = getChip(boundIface);
            if (chip == null) {
                Log.e(TAG, "createRttController: null IWifiChip -- boundIface(name)=" + getName(boundIface));
                return null;
            }
            Mutable<IWifiRttController> rttResp = new Mutable();
            try {
                chip.createRttController(boundIface, new -$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k(rttResp));
            } catch (RemoteException e) {
                Log.e(TAG, "IWifiChip.createRttController exception: " + e);
            }
            IWifiRttController iWifiRttController = (IWifiRttController) rttResp.value;
            return iWifiRttController;
        }
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_15675(Mutable rttResp, WifiStatus status, IWifiRttController rtt) {
        if (status.code == 0) {
            rttResp.value = rtt;
        } else {
            Log.e(TAG, "IWifiChip.createRttController failed: " + statusString(status));
        }
    }

    protected IWifi getWifiServiceMockable() {
        try {
            return IWifi.getService();
        } catch (RemoteException e) {
            Log.e(TAG, "Exception getting IWifi service: " + e);
            return null;
        }
    }

    protected IServiceManager getServiceManagerMockable() {
        try {
            return IServiceManager.getService();
        } catch (RemoteException e) {
            Log.e(TAG, "Exception getting IServiceManager: " + e);
            return null;
        }
    }

    private void initializeInternal() {
        initIServiceManagerIfNecessary();
    }

    private void teardownInternal() {
        managerStatusListenerDispatch();
        dispatchAllDestroyedListeners();
        ((Set) this.mInterfaceAvailableForRequestListeners.get(0)).clear();
        ((Set) this.mInterfaceAvailableForRequestListeners.get(1)).clear();
        ((Set) this.mInterfaceAvailableForRequestListeners.get(2)).clear();
        ((Set) this.mInterfaceAvailableForRequestListeners.get(3)).clear();
    }

    /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_20381(long cookie) {
        Log.wtf(TAG, "IServiceManager died: cookie=" + cookie);
        synchronized (this.mLock) {
            this.mServiceManager = null;
        }
    }

    private void initIServiceManagerIfNecessary() {
        synchronized (this.mLock) {
            if (this.mServiceManager != null) {
                return;
            }
            this.mServiceManager = getServiceManagerMockable();
            if (this.mServiceManager == null) {
                Log.wtf(TAG, "Failed to get IServiceManager instance");
            } else {
                try {
                    if (!this.mServiceManager.linkToDeath(this.mServiceManagerDeathRecipient, 0)) {
                        Log.wtf(TAG, "Error on linkToDeath on IServiceManager");
                        this.mServiceManager = null;
                        return;
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

    /* JADX WARNING: Missing block: B:13:0x0023, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isSupportedInternal() {
        boolean z = false;
        synchronized (this.mLock) {
            if (this.mServiceManager == null) {
                Log.e(TAG, "isSupported: called but mServiceManager is null!?");
                return false;
            }
            try {
                if (this.mServiceManager.getTransport(IWifi.kInterfaceName, HAL_INSTANCE_NAME) != (byte) 0) {
                    z = true;
                }
            } catch (RemoteException e) {
                Log.wtf(TAG, "Exception while operating on IServiceManager: " + e);
                return false;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_23995(long cookie) {
        Log.e(TAG, "IWifi HAL service died! Have a listener for it ... cookie=" + cookie);
        synchronized (this.mLock) {
            this.mWifi = null;
            teardownInternal();
        }
    }

    private void initIWifiIfNecessary() {
        synchronized (this.mLock) {
            if (this.mWifi != null) {
                return;
            }
            try {
                this.mWifi = getWifiServiceMockable();
                if (this.mWifi == null) {
                    Log.e(TAG, "IWifi not (yet) available - but have a listener for it ...");
                    return;
                } else if (this.mWifi.linkToDeath(this.mIWifiDeathRecipient, 0)) {
                    WifiStatus status = this.mWifi.registerEventCallback(this.mWifiEventCallback);
                    if (status.code != 0) {
                        Log.e(TAG, "IWifi.registerEventCallback failed: " + statusString(status));
                        this.mWifi = null;
                        return;
                    }
                    managerStatusListenerDispatch();
                } else {
                    Log.e(TAG, "Error on linkToDeath on IWifi - will retry later");
                    return;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Exception while operating on IWifi: " + e);
            }
        }
    }

    private void initIWifiChipDebugListeners() {
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_26791(MutableBoolean statusOk, Mutable chipIdsResp, WifiStatus status, ArrayList chipIds) {
        boolean z = false;
        if (status.code == 0) {
            z = true;
        }
        statusOk.value = z;
        if (statusOk.value) {
            chipIdsResp.value = chipIds;
        } else {
            Log.e(TAG, "getChipIds failed: " + statusString(status));
        }
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_27706(MutableBoolean statusOk, Mutable chipResp, WifiStatus status, IWifiChip chip) {
        boolean z = false;
        if (status.code == 0) {
            z = true;
        }
        statusOk.value = z;
        if (statusOk.value) {
            chipResp.value = chip;
        } else {
            Log.e(TAG, "getChip failed: " + statusString(status));
        }
    }

    private WifiChipInfo[] getAllChipInfo() {
        synchronized (this.mLock) {
            if (this.mWifi == null) {
                Log.e(TAG, "getAllChipInfo: called but mWifi is null!?");
                return null;
            }
            try {
                MutableBoolean statusOk = new MutableBoolean(false);
                Mutable<ArrayList<Integer>> chipIdsResp = new Mutable();
                this.mWifi.getChipIds(new AnonymousClass7(statusOk, chipIdsResp));
                if (!statusOk.value) {
                    return null;
                } else if (((ArrayList) chipIdsResp.value).size() == 0) {
                    Log.e(TAG, "Should have at least 1 chip!");
                    return null;
                } else {
                    int chipInfoIndex = 0;
                    WifiChipInfo[] chipsInfo = new WifiChipInfo[((ArrayList) chipIdsResp.value).size()];
                    Mutable<IWifiChip> chipResp = new Mutable();
                    Iterator chipId$iterator = ((ArrayList) chipIdsResp.value).iterator();
                    while (true) {
                        int chipInfoIndex2 = chipInfoIndex;
                        if (chipId$iterator.hasNext()) {
                            Integer chipId = (Integer) chipId$iterator.next();
                            this.mWifi.getChip(chipId.intValue(), new AnonymousClass5(statusOk, chipResp));
                            if (statusOk.value) {
                                Mutable<ArrayList<ChipMode>> availableModesResp = new Mutable();
                                ((IWifiChip) chipResp.value).getAvailableModes(new AnonymousClass14(statusOk, availableModesResp));
                                if (statusOk.value) {
                                    MutableBoolean mutableBoolean = new MutableBoolean(false);
                                    MutableInt mutableInt = new MutableInt(0);
                                    ((IWifiChip) chipResp.value).getMode(new AnonymousClass19(statusOk, mutableBoolean, mutableInt));
                                    if (statusOk.value) {
                                        Mutable<ArrayList<String>> ifaceNamesResp = new Mutable();
                                        MutableInt ifaceIndex = new MutableInt(0);
                                        ((IWifiChip) chipResp.value).getStaIfaceNames(new AnonymousClass18(statusOk, ifaceNamesResp));
                                        if (statusOk.value) {
                                            WifiIfaceInfo[] staIfaces = new WifiIfaceInfo[((ArrayList) ifaceNamesResp.value).size()];
                                            for (String ifaceName : (ArrayList) ifaceNamesResp.value) {
                                                ((IWifiChip) chipResp.value).getStaIface(ifaceName, new AnonymousClass23(this, statusOk, ifaceName, staIfaces, ifaceIndex));
                                                if (!statusOk.value) {
                                                    return null;
                                                }
                                            }
                                            ifaceIndex.value = 0;
                                            ((IWifiChip) chipResp.value).getApIfaceNames(new AnonymousClass13(statusOk, ifaceNamesResp));
                                            if (statusOk.value) {
                                                WifiIfaceInfo[] apIfaces = new WifiIfaceInfo[((ArrayList) ifaceNamesResp.value).size()];
                                                for (String ifaceName2 : (ArrayList) ifaceNamesResp.value) {
                                                    ((IWifiChip) chipResp.value).getApIface(ifaceName2, new AnonymousClass20(this, statusOk, ifaceName2, apIfaces, ifaceIndex));
                                                    if (!statusOk.value) {
                                                        return null;
                                                    }
                                                }
                                                ifaceIndex.value = 0;
                                                ((IWifiChip) chipResp.value).getP2pIfaceNames(new AnonymousClass17(statusOk, ifaceNamesResp));
                                                if (statusOk.value) {
                                                    WifiIfaceInfo[] p2pIfaces = new WifiIfaceInfo[((ArrayList) ifaceNamesResp.value).size()];
                                                    for (String ifaceName22 : (ArrayList) ifaceNamesResp.value) {
                                                        ((IWifiChip) chipResp.value).getP2pIface(ifaceName22, new AnonymousClass22(this, statusOk, ifaceName22, p2pIfaces, ifaceIndex));
                                                        if (!statusOk.value) {
                                                            return null;
                                                        }
                                                    }
                                                    ifaceIndex.value = 0;
                                                    ((IWifiChip) chipResp.value).getNanIfaceNames(new AnonymousClass16(statusOk, ifaceNamesResp));
                                                    if (statusOk.value) {
                                                        WifiIfaceInfo[] nanIfaces = new WifiIfaceInfo[((ArrayList) ifaceNamesResp.value).size()];
                                                        for (String ifaceName222 : (ArrayList) ifaceNamesResp.value) {
                                                            ((IWifiChip) chipResp.value).getNanIface(ifaceName222, new AnonymousClass21(this, statusOk, ifaceName222, nanIfaces, ifaceIndex));
                                                            if (!statusOk.value) {
                                                                return null;
                                                            }
                                                        }
                                                        WifiChipInfo wifiChipInfo = new WifiChipInfo(this, null);
                                                        chipInfoIndex = chipInfoIndex2 + 1;
                                                        chipsInfo[chipInfoIndex2] = wifiChipInfo;
                                                        wifiChipInfo.chip = (IWifiChip) chipResp.value;
                                                        wifiChipInfo.chipId = chipId.intValue();
                                                        wifiChipInfo.availableModes = (ArrayList) availableModesResp.value;
                                                        wifiChipInfo.currentModeIdValid = mutableBoolean.value;
                                                        wifiChipInfo.currentModeId = mutableInt.value;
                                                        wifiChipInfo.ifaces[0] = staIfaces;
                                                        wifiChipInfo.ifaces[1] = apIfaces;
                                                        wifiChipInfo.ifaces[2] = p2pIfaces;
                                                        wifiChipInfo.ifaces[3] = nanIfaces;
                                                    } else {
                                                        return null;
                                                    }
                                                }
                                                return null;
                                            }
                                            return null;
                                        }
                                        return null;
                                    }
                                    return null;
                                }
                                return null;
                            }
                            return null;
                        }
                        return chipsInfo;
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, "getAllChipInfoAndValidateCache exception: " + e);
                return null;
            }
        }
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_31674(MutableBoolean statusOk, Mutable chipIdsResp, WifiStatus status, ArrayList chipIds) {
        boolean z = false;
        if (status.code == 0) {
            z = true;
        }
        statusOk.value = z;
        if (statusOk.value) {
            chipIdsResp.value = chipIds;
        } else {
            Log.e(TAG, "getChipIds failed: " + statusString(status));
        }
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_32673(MutableBoolean statusOk, Mutable chipResp, WifiStatus status, IWifiChip chip) {
        boolean z = false;
        if (status.code == 0) {
            z = true;
        }
        statusOk.value = z;
        if (statusOk.value) {
            chipResp.value = chip;
        } else {
            Log.e(TAG, "getChip failed: " + statusString(status));
        }
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_33339(MutableBoolean statusOk, Mutable availableModesResp, WifiStatus status, ArrayList modes) {
        boolean z = false;
        if (status.code == 0) {
            z = true;
        }
        statusOk.value = z;
        if (statusOk.value) {
            availableModesResp.value = modes;
        } else {
            Log.e(TAG, "getAvailableModes failed: " + statusString(status));
        }
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_34120(MutableBoolean statusOk, MutableBoolean currentModeValidResp, MutableInt currentModeResp, WifiStatus status, int modeId) {
        boolean z = false;
        if (status.code == 0) {
            z = true;
        }
        statusOk.value = z;
        if (statusOk.value) {
            currentModeValidResp.value = true;
            currentModeResp.value = modeId;
        } else if (status.code == 5) {
            statusOk.value = true;
        } else {
            Log.e(TAG, "getMode failed: " + statusString(status));
        }
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_35058(MutableBoolean statusOk, Mutable ifaceNamesResp, WifiStatus status, ArrayList ifnames) {
        boolean z = false;
        if (status.code == 0) {
            z = true;
        }
        statusOk.value = z;
        if (statusOk.value) {
            ifaceNamesResp.value = ifnames;
        } else {
            Log.e(TAG, "getStaIfaceNames failed: " + statusString(status));
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_35887(MutableBoolean statusOk, String ifaceName, WifiIfaceInfo[] staIfaces, MutableInt ifaceIndex, WifiStatus status, IWifiStaIface iface) {
        boolean z = false;
        if (status.code == 0) {
            z = true;
        }
        statusOk.value = z;
        if (statusOk.value) {
            WifiIfaceInfo ifaceInfo = new WifiIfaceInfo(this, null);
            ifaceInfo.name = ifaceName;
            ifaceInfo.iface = iface;
            int i = ifaceIndex.value;
            ifaceIndex.value = i + 1;
            staIfaces[i] = ifaceInfo;
            return;
        }
        Log.e(TAG, "getStaIface failed: " + statusString(status));
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_36863(MutableBoolean statusOk, Mutable ifaceNamesResp, WifiStatus status, ArrayList ifnames) {
        boolean z = false;
        if (status.code == 0) {
            z = true;
        }
        statusOk.value = z;
        if (statusOk.value) {
            ifaceNamesResp.value = ifnames;
        } else {
            Log.e(TAG, "getApIfaceNames failed: " + statusString(status));
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_37689(MutableBoolean statusOk, String ifaceName, WifiIfaceInfo[] apIfaces, MutableInt ifaceIndex, WifiStatus status, IWifiApIface iface) {
        boolean z = false;
        if (status.code == 0) {
            z = true;
        }
        statusOk.value = z;
        if (statusOk.value) {
            WifiIfaceInfo ifaceInfo = new WifiIfaceInfo(this, null);
            ifaceInfo.name = ifaceName;
            ifaceInfo.iface = iface;
            int i = ifaceIndex.value;
            ifaceIndex.value = i + 1;
            apIfaces[i] = ifaceInfo;
            return;
        }
        Log.e(TAG, "getApIface failed: " + statusString(status));
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_38663(MutableBoolean statusOk, Mutable ifaceNamesResp, WifiStatus status, ArrayList ifnames) {
        boolean z = false;
        if (status.code == 0) {
            z = true;
        }
        statusOk.value = z;
        if (statusOk.value) {
            ifaceNamesResp.value = ifnames;
        } else {
            Log.e(TAG, "getP2pIfaceNames failed: " + statusString(status));
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_39492(MutableBoolean statusOk, String ifaceName, WifiIfaceInfo[] p2pIfaces, MutableInt ifaceIndex, WifiStatus status, IWifiP2pIface iface) {
        boolean z = false;
        if (status.code == 0) {
            z = true;
        }
        statusOk.value = z;
        if (statusOk.value) {
            WifiIfaceInfo ifaceInfo = new WifiIfaceInfo(this, null);
            ifaceInfo.name = ifaceName;
            ifaceInfo.iface = iface;
            int i = ifaceIndex.value;
            ifaceIndex.value = i + 1;
            p2pIfaces[i] = ifaceInfo;
            return;
        }
        Log.e(TAG, "getP2pIface failed: " + statusString(status));
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_40469(MutableBoolean statusOk, Mutable ifaceNamesResp, WifiStatus status, ArrayList ifnames) {
        boolean z = false;
        if (status.code == 0) {
            z = true;
        }
        statusOk.value = z;
        if (statusOk.value) {
            ifaceNamesResp.value = ifnames;
        } else {
            Log.e(TAG, "getNanIfaceNames failed: " + statusString(status));
        }
    }

    /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_41298(MutableBoolean statusOk, String ifaceName, WifiIfaceInfo[] nanIfaces, MutableInt ifaceIndex, WifiStatus status, IWifiNanIface iface) {
        boolean z = false;
        if (status.code == 0) {
            z = true;
        }
        statusOk.value = z;
        if (statusOk.value) {
            WifiIfaceInfo ifaceInfo = new WifiIfaceInfo(this, null);
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
            for (Entry<IWifiIface, InterfaceCacheEntry> entry : this.mInterfaceInfoCache.entrySet()) {
                WifiChipInfo matchingChipInfo = null;
                for (WifiChipInfo ci : chipInfos) {
                    if (ci.chipId == ((InterfaceCacheEntry) entry.getValue()).chipId) {
                        matchingChipInfo = ci;
                        break;
                    }
                }
                if (matchingChipInfo == null) {
                    Log.e(TAG, "validateInterfaceCache: no chip found for " + entry.getValue());
                    return false;
                }
                WifiIfaceInfo[] ifaceInfoList = matchingChipInfo.ifaces[((InterfaceCacheEntry) entry.getValue()).type];
                if (ifaceInfoList == null) {
                    Log.e(TAG, "validateInterfaceCache: invalid type on entry " + entry.getValue());
                    return false;
                }
                boolean matchFound = false;
                for (WifiIfaceInfo ifaceInfo : ifaceInfoList) {
                    if (ifaceInfo.name.equals(((InterfaceCacheEntry) entry.getValue()).name)) {
                        matchFound = true;
                        continue;
                        break;
                    }
                }
                if (!matchFound) {
                    Log.e(TAG, "validateInterfaceCache: no interface found for " + entry.getValue());
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
        return;
    }

    private void managerStatusListenerDispatch() {
        synchronized (this.mLock) {
            for (ManagerStatusListenerProxy cb : this.mManagerStatusListeners) {
                cb.trigger();
            }
        }
    }

    Set<Integer> getSupportedIfaceTypesInternal(IWifiChip chip) {
        Set<Integer> results = new HashSet();
        WifiChipInfo[] chipInfos = getAllChipInfo();
        if (chipInfos == null) {
            Log.e(TAG, "getSupportedIfaceTypesInternal: no chip info found");
            return results;
        }
        MutableInt chipIdIfProvided = new MutableInt(0);
        if (chip != null) {
            MutableBoolean statusOk = new MutableBoolean(false);
            try {
                chip.getId(new AnonymousClass15(chipIdIfProvided, statusOk));
                if (!statusOk.value) {
                    return results;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "getSupportedIfaceTypesInternal IWifiChip.getId() exception: " + e);
                return results;
            }
        }
        int i = 0;
        int length = chipInfos.length;
        while (true) {
            int i2 = i;
            if (i2 >= length) {
                return results;
            }
            WifiChipInfo wci = chipInfos[i2];
            if (chip == null || wci.chipId == chipIdIfProvided.value) {
                for (ChipMode cm : wci.availableModes) {
                    for (ChipIfaceCombination cic : cm.availableCombinations) {
                        for (ChipIfaceCombinationLimit cicl : cic.limits) {
                            for (Integer intValue : cicl.types) {
                                results.add(Integer.valueOf(intValue.intValue()));
                            }
                        }
                    }
                }
            }
            i = i2 + 1;
        }
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_51432(MutableInt chipIdIfProvided, MutableBoolean statusOk, WifiStatus status, int id) {
        if (status.code == 0) {
            chipIdIfProvided.value = id;
            statusOk.value = true;
            return;
        }
        Log.e(TAG, "getSupportedIfaceTypesInternal: IWifiChip.getId() error: " + statusString(status));
        statusOk.value = false;
    }

    /* JADX WARNING: Missing block: B:22:0x003b, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private IWifiIface createIface(int ifaceType, InterfaceDestroyedListener destroyedListener, Looper looper) {
        synchronized (this.mLock) {
            WifiChipInfo[] chipInfos = getAllChipInfo();
            if (chipInfos == null) {
                Log.e(TAG, "createIface: no chip info found");
                stopWifi();
                return null;
            } else if (validateInterfaceCache(chipInfos)) {
                IWifiIface iface = createIfaceIfPossible(chipInfos, ifaceType, destroyedListener, looper);
                if (iface == null || dispatchAvailableForRequestListeners()) {
                } else {
                    return null;
                }
            } else {
                Log.e(TAG, "createIface: local cache is invalid!");
                stopWifi();
                return null;
            }
        }
    }

    /* JADX WARNING: Missing block: B:35:0x00ae, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private IWifiIface createIfaceIfPossible(WifiChipInfo[] chipInfos, int ifaceType, InterfaceDestroyedListener destroyedListener, Looper looper) {
        synchronized (this.mLock) {
            IfaceCreationData bestIfaceCreationProposal = null;
            int i = 0;
            int length = chipInfos.length;
            while (true) {
                int i2 = i;
                if (i2 >= length) {
                    break;
                }
                WifiChipInfo chipInfo = chipInfos[i2];
                for (ChipMode chipMode : chipInfo.availableModes) {
                    for (ChipIfaceCombination chipIfaceCombo : chipMode.availableCombinations) {
                        for (int[] expandedIfaceCombo : expandIfaceCombos(chipIfaceCombo)) {
                            IfaceCreationData currentProposal = canIfaceComboSupportRequest(chipInfo, chipMode, expandedIfaceCombo, ifaceType);
                            if (compareIfaceCreationData(currentProposal, bestIfaceCreationProposal)) {
                                bestIfaceCreationProposal = currentProposal;
                            }
                        }
                    }
                }
                i = i2 + 1;
            }
            if (bestIfaceCreationProposal != null) {
                IWifiIface iface = executeChipReconfiguration(bestIfaceCreationProposal, ifaceType);
                if (iface != null) {
                    InterfaceCacheEntry cacheEntry = new InterfaceCacheEntry(this, null);
                    cacheEntry.chip = bestIfaceCreationProposal.chipInfo.chip;
                    cacheEntry.chipId = bestIfaceCreationProposal.chipInfo.chipId;
                    cacheEntry.name = getName(iface);
                    cacheEntry.type = ifaceType;
                    if (destroyedListener != null) {
                        Set set = cacheEntry.destroyedListeners;
                        if (looper == null) {
                            looper = Looper.myLooper();
                        }
                        set.add(new InterfaceDestroyedListenerProxy(destroyedListener, looper));
                    }
                    this.mInterfaceInfoCache.put(iface, cacheEntry);
                    return iface;
                }
            }
        }
    }

    private boolean isItPossibleToCreateIface(WifiChipInfo[] chipInfos, int ifaceType) {
        for (WifiChipInfo chipInfo : chipInfos) {
            for (ChipMode chipMode : chipInfo.availableModes) {
                for (ChipIfaceCombination chipIfaceCombo : chipMode.availableCombinations) {
                    for (int[] expandedIfaceCombo : expandIfaceCombos(chipIfaceCombo)) {
                        if (canIfaceComboSupportRequest(chipInfo, chipMode, expandedIfaceCombo, ifaceType) != null) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private int[][] expandIfaceCombos(ChipIfaceCombination chipIfaceCombo) {
        int i;
        int numOfCombos = 1;
        for (ChipIfaceCombinationLimit limit : chipIfaceCombo.limits) {
            for (i = 0; i < limit.maxIfaces; i++) {
                numOfCombos *= limit.types.size();
            }
        }
        int[][] expandedIfaceCombos = (int[][]) Array.newInstance(Integer.TYPE, new int[]{numOfCombos, IFACE_TYPES_BY_PRIORITY.length});
        int span = numOfCombos;
        for (ChipIfaceCombinationLimit limit2 : chipIfaceCombo.limits) {
            for (i = 0; i < limit2.maxIfaces; i++) {
                span /= limit2.types.size();
                for (int k = 0; k < numOfCombos; k++) {
                    int[] iArr = expandedIfaceCombos[k];
                    Integer num = (Integer) limit2.types.get((k / span) % limit2.types.size());
                    iArr[num.intValue()] = iArr[num.intValue()] + 1;
                }
            }
        }
        return expandedIfaceCombos;
    }

    private IfaceCreationData canIfaceComboSupportRequest(WifiChipInfo chipInfo, ChipMode chipMode, int[] chipIfaceCombo, int ifaceType) {
        if (chipIfaceCombo[ifaceType] == 0) {
            return null;
        }
        boolean isChipModeChangeProposed = chipInfo.currentModeIdValid && chipInfo.currentModeId != chipMode.id;
        IfaceCreationData ifaceCreationData;
        if (isChipModeChangeProposed) {
            for (int type : IFACE_TYPES_BY_PRIORITY) {
                if (chipInfo.ifaces[type].length != 0 && !allowedToDeleteIfaceTypeForRequestedType(type, ifaceType)) {
                    return null;
                }
            }
            ifaceCreationData = new IfaceCreationData(this, null);
            ifaceCreationData.chipInfo = chipInfo;
            ifaceCreationData.chipModeId = chipMode.id;
            return ifaceCreationData;
        }
        List<WifiIfaceInfo> interfacesToBeRemovedFirst = new ArrayList();
        for (int type2 : IFACE_TYPES_BY_PRIORITY) {
            int tooManyInterfaces = chipInfo.ifaces[type2].length - chipIfaceCombo[type2];
            if (type2 == ifaceType) {
                tooManyInterfaces++;
            }
            if (tooManyInterfaces > 0) {
                if (!allowedToDeleteIfaceTypeForRequestedType(type2, ifaceType)) {
                    return null;
                }
                for (int i = 0; i < tooManyInterfaces; i++) {
                    interfacesToBeRemovedFirst.add(chipInfo.ifaces[type2][i]);
                }
            }
        }
        ifaceCreationData = new IfaceCreationData(this, null);
        ifaceCreationData.chipInfo = chipInfo;
        ifaceCreationData.chipModeId = chipMode.id;
        ifaceCreationData.interfacesToBeRemovedFirst = interfacesToBeRemovedFirst;
        return ifaceCreationData;
    }

    private boolean compareIfaceCreationData(IfaceCreationData val1, IfaceCreationData val2) {
        if (val1 == null) {
            return false;
        }
        if (val2 == null) {
            return true;
        }
        for (int type : IFACE_TYPES_BY_PRIORITY) {
            int numIfacesToDelete1;
            int numIfacesToDelete2;
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

    private boolean allowedToDeleteIfaceTypeForRequestedType(int existingIfaceType, int requestedIfaceType) {
        boolean z = true;
        if (existingIfaceType == requestedIfaceType || requestedIfaceType == 3) {
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

    private IWifiIface executeChipReconfiguration(IfaceCreationData ifaceCreationData, int ifaceType) {
        synchronized (this.mLock) {
            try {
                boolean isModeConfigNeeded = ifaceCreationData.chipInfo.currentModeIdValid ? ifaceCreationData.chipInfo.currentModeId != ifaceCreationData.chipModeId : true;
                if (isModeConfigNeeded) {
                    for (WifiIfaceInfo[] ifaceInfos : ifaceCreationData.chipInfo.ifaces) {
                        for (WifiIfaceInfo ifaceInfo : r12[r10]) {
                            removeIfaceInternal(ifaceInfo.iface);
                        }
                    }
                    WifiStatus status = ifaceCreationData.chipInfo.chip.configureChip(ifaceCreationData.chipModeId);
                    if (status.code != 0) {
                        Log.e(TAG, "executeChipReconfiguration: configureChip error: " + statusString(status));
                        return null;
                    }
                }
                for (WifiIfaceInfo ifaceInfo2 : ifaceCreationData.interfacesToBeRemovedFirst) {
                    removeIfaceInternal(ifaceInfo2.iface);
                }
                Mutable<WifiStatus> statusResp = new Mutable();
                Mutable<IWifiIface> ifaceResp = new Mutable();
                switch (ifaceType) {
                    case 0:
                        ifaceCreationData.chipInfo.chip.createStaIface(new AnonymousClass12(statusResp, ifaceResp));
                        break;
                    case 1:
                        ifaceCreationData.chipInfo.chip.createApIface(new AnonymousClass9(statusResp, ifaceResp));
                        break;
                    case 2:
                        ifaceCreationData.chipInfo.chip.createP2pIface(new AnonymousClass11(statusResp, ifaceResp));
                        break;
                    case 3:
                        ifaceCreationData.chipInfo.chip.createNanIface(new AnonymousClass10(statusResp, ifaceResp));
                        break;
                }
                if (((WifiStatus) statusResp.value).code != 0) {
                    Log.e(TAG, "executeChipReconfiguration: failed to create interface ifaceType=" + ifaceType + ": " + statusString((WifiStatus) statusResp.value));
                    return null;
                }
                return (IWifiIface) ifaceResp.value;
            } catch (RemoteException e) {
                Log.e(TAG, "executeChipReconfiguration exception: " + e);
                return null;
            }
        }
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_69282(Mutable statusResp, Mutable ifaceResp, WifiStatus status, IWifiStaIface iface) {
        statusResp.value = status;
        ifaceResp.value = iface;
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_69661(Mutable statusResp, Mutable ifaceResp, WifiStatus status, IWifiApIface iface) {
        statusResp.value = status;
        ifaceResp.value = iface;
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_70041(Mutable statusResp, Mutable ifaceResp, WifiStatus status, IWifiP2pIface iface) {
        statusResp.value = status;
        ifaceResp.value = iface;
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_70422(Mutable statusResp, Mutable ifaceResp, WifiStatus status, IWifiNanIface iface) {
        statusResp.value = status;
        ifaceResp.value = iface;
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean removeIfaceInternal(IWifiIface iface) {
        synchronized (this.mLock) {
            if (this.mWifi == null) {
                Log.e(TAG, "removeIfaceInternal: null IWifi -- iface(name)=" + getName(iface));
                return false;
            }
            IWifiChip chip = getChip(iface);
            if (chip == null) {
                Log.e(TAG, "removeIfaceInternal: null IWifiChip -- iface(name)=" + getName(iface));
                return false;
            }
            String name = getName(iface);
            if (name == null) {
                Log.e(TAG, "removeIfaceInternal: can't get name");
                return false;
            }
            int type = getType(iface);
            if (type == -1) {
                Log.e(TAG, "removeIfaceInternal: can't get type -- iface(name)=" + getName(iface));
                return false;
            }
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
                    status = chip.removeNanIface(name);
                    break;
                default:
                    try {
                        Log.wtf(TAG, "removeIfaceInternal: invalid type=" + type);
                        return false;
                    } catch (RemoteException e) {
                        Log.e(TAG, "IWifiChip.removeXxxIface exception: " + e);
                        break;
                    }
            }
            dispatchDestroyedListeners(iface);
            if (status == null || status.code != 0) {
                Log.e(TAG, "IWifiChip.removeXxxIface failed: " + statusString(status));
                return false;
            }
            return true;
        }
    }

    private boolean dispatchAvailableForRequestListeners() {
        int i = 0;
        synchronized (this.mLock) {
            WifiChipInfo[] chipInfos = getAllChipInfo();
            if (chipInfos == null) {
                Log.e(TAG, "dispatchAvailableForRequestListeners: no chip info found");
                stopWifi();
                return false;
            }
            int[] iArr = IFACE_TYPES_BY_PRIORITY;
            int length = iArr.length;
            while (i < length) {
                dispatchAvailableForRequestListenersForType(iArr[i], chipInfos);
                i++;
            }
            return true;
        }
    }

    private void dispatchAvailableForRequestListenersForType(int ifaceType, WifiChipInfo[] chipInfos) {
        Set<InterfaceAvailableForRequestListenerProxy> listeners = (Set) this.mInterfaceAvailableForRequestListeners.get(ifaceType);
        if (listeners.size() != 0 && isItPossibleToCreateIface(chipInfos, ifaceType)) {
            for (InterfaceAvailableForRequestListenerProxy listener : listeners) {
                listener.trigger();
            }
        }
    }

    private void dispatchDestroyedListeners(IWifiIface iface) {
        synchronized (this.mLock) {
            InterfaceCacheEntry entry = (InterfaceCacheEntry) this.mInterfaceInfoCache.get(iface);
            if (entry == null) {
                Log.e(TAG, "dispatchDestroyedListeners: no cache entry for iface(name)=" + getName(iface));
                return;
            }
            for (InterfaceDestroyedListenerProxy listener : entry.destroyedListeners) {
                listener.trigger();
            }
            entry.destroyedListeners.clear();
            this.mInterfaceInfoCache.remove(iface);
        }
    }

    private void dispatchAllDestroyedListeners() {
        synchronized (this.mLock) {
            Iterator<Entry<IWifiIface, InterfaceCacheEntry>> it = this.mInterfaceInfoCache.entrySet().iterator();
            while (it.hasNext()) {
                InterfaceCacheEntry entry = (InterfaceCacheEntry) ((Entry) it.next()).getValue();
                for (InterfaceDestroyedListenerProxy listener : entry.destroyedListeners) {
                    listener.trigger();
                }
                entry.destroyedListeners.clear();
                it.remove();
            }
        }
    }

    private static String statusString(WifiStatus status) {
        if (status == null) {
            return "status=null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(status.code).append(" (").append(status.description).append(")");
        return sb.toString();
    }

    private static int getType(IWifiIface iface) {
        MutableInt typeResp = new MutableInt(-1);
        try {
            iface.getType(new com.android.server.wifi.-$Lambda$zRsSIzbfvkJSErD1TWUPvfb3F7k.AnonymousClass2(typeResp));
        } catch (RemoteException e) {
            Log.e(TAG, "Exception on getType: " + e);
        }
        return typeResp.value;
    }

    static /* synthetic */ void lambda$-com_android_server_wifi_HalDeviceManager_79875(MutableInt typeResp, WifiStatus status, int type) {
        if (status.code == 0) {
            typeResp.value = type;
        } else {
            Log.e(TAG, "Error on getType: " + statusString(status));
        }
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
