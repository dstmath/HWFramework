package com.android.server.connectivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.IDnsResolver;
import android.net.INetd;
import android.net.INetworkPolicyListener;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkAgent;
import android.net.NetworkInfo;
import android.net.NetworkPolicyManager;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.util.NetdService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.server.ConnectivityService;
import com.android.server.HwBluetoothManagerServiceEx;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.intellicom.networkslice.HwNetworkSliceManager;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.server.connectivity.IHwConnectivityServiceEx;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HwConnectivityServiceEx implements IHwConnectivityServiceEx {
    private static final int BLOCK = 1;
    private static final String CTS_PACKAGE = "android.jobscheduler.cts";
    private static final boolean DBG = true;
    private static final String DISTRIBUTED_NET_FEATURE_KEY = "distributed_net_feature";
    private static final String DNS_RESOLVER_SERVICE = "dnsresolver";
    private static final int EVENT_DELETE_NETWORK_CONNECT_CACHE = 3;
    private static final int EVENT_FRESH_NETWORK_CONNECT_CACHE = 1;
    private static final int EVENT_NETWORK_TESTED = 41;
    private static final int EVENT_UID_RULES_CHANGED = 2;
    private static final int HOST_NAME_LONG = 25;
    private static final int HOST_NAME_MEDIUM = 18;
    private static final int HOST_NAME_SHORT = 8;
    private static final int INVAILD_RESULT = -1;
    private static final int INVALID_NETWORK_ID = -1;
    private static final int NETWORK_STATE_CACHE_FRESH_TIME = 10000;
    private static final int NETWORK_STATE_CACHE_LIFE_TIME = 2000;
    private static final int NETWORK_TESTED_RESULT_INVALID = 0;
    private static final int OFF = 0;
    private static final int ON = 1;
    private static final String REFLECTION_DISTRIBUTED_NET_MANAGER = "com.android.server.connectivity.DistributedNetworkManager";
    private static final String TAG = HwConnectivityServiceEx.class.getSimpleName();
    private static final boolean TV_DEVICE = ("tv".equals(SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT)) || "mobiletv".equals(SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT)));
    private static final int UNBLOCK = 0;
    private final Map<Integer, CacheNetworkState> mCacheNetworkStateMapByUid = new ConcurrentHashMap();
    private Context mContext;
    private IHwConnectivityServiceInner mCsi;
    private Object mDistributedNetManager;
    private InternalHandler mHandler;
    private final HandlerThread mHandlerThread;
    private final INetworkPolicyListener mPolicyListener = new NetworkPolicyManager.Listener() {
        /* class com.android.server.connectivity.HwConnectivityServiceEx.AnonymousClass1 */

        public void onUidRulesChanged(int uid, int uidRules) {
            HwConnectivityServiceEx.this.mHandler.sendMessage(HwConnectivityServiceEx.this.mHandler.obtainMessage(2, uid, uidRules));
        }

        public void onRestrictBackgroundChanged(boolean restrictBackground) {
            HwConnectivityServiceEx.this.mHandler.sendMessage(HwConnectivityServiceEx.this.mHandler.obtainMessage(3));
        }
    };

    public HwConnectivityServiceEx(IHwConnectivityServiceInner csi, Context context) {
        this.mCsi = csi;
        this.mContext = context;
        this.mHandlerThread = new HandlerThread("HwConnectivityServiceExThread");
        this.mHandlerThread.start();
        this.mHandler = new InternalHandler(this.mHandlerThread.getLooper());
        Object object = this.mContext.getSystemService("netpolicy");
        if (object instanceof NetworkPolicyManager) {
            ((NetworkPolicyManager) object).registerListener(this.mPolicyListener);
        }
        if (isDistributedNetSupport()) {
            initDistributedNetService();
        }
    }

    private static void log(String string) {
        Slog.d(TAG, string);
    }

    private static void loge(String string) {
        Slog.e(TAG, string);
    }

    public void maybeHandleNetworkAgentMessageEx(Message msg, NetworkAgentInfo nai, Handler handler) {
        HashMap<Messenger, NetworkAgentInfo> mNetworkAgentInfos = this.mCsi.getNetworkAgentInfos();
        int i = msg.what;
        switch (i) {
            case 528486:
                nai.networkMisc.wifiApType = msg.arg1;
                log("CMD_UPDATE_WIFI_AP_TYPE :" + nai.networkMisc.wifiApType);
                return;
            case 528487:
                nai.networkMisc.connectToCellularAndWLAN = msg.arg1;
                Object object = msg.obj;
                if (object instanceof Boolean) {
                    nai.networkMisc.acceptUnvalidated = ((Boolean) object).booleanValue();
                }
                log("update acceptUnvalidated is: " + nai.networkMisc.acceptUnvalidated + ", connectToCellularAndWLAN: " + nai.networkMisc.connectToCellularAndWLAN);
                return;
            default:
                switch (i) {
                    case 528585:
                        setExplicitlyUnselected(mNetworkAgentInfos.get(msg.replyTo));
                        return;
                    case 528586:
                        Object object2 = msg.obj;
                        if (object2 instanceof NetworkInfo) {
                            updateNetworkConcurrently(mNetworkAgentInfos.get(msg.replyTo), (NetworkInfo) object2);
                            return;
                        }
                        return;
                    case 528587:
                        triggerRoamingNetworkMonitor(mNetworkAgentInfos.get(msg.replyTo));
                        return;
                    case 528588:
                        triggerInvalidlinkNetworkMonitor(mNetworkAgentInfos.get(msg.replyTo), handler);
                        return;
                    default:
                        return;
                }
        }
    }

    public NetworkAgentInfo getIdenticalActiveNetworkAgentInfo(NetworkAgentInfo na) {
        if (HuaweiTelephonyConfigs.isHisiPlatform() || na == null || na.networkInfo.getState() != NetworkInfo.State.CONNECTED) {
            return null;
        }
        for (NetworkAgentInfo network : this.mCsi.getNetworkAgentInfos().values()) {
            log("checking existed " + network.name());
            if (network != this.mCsi.getHwNetworkForType(network.networkInfo.getType())) {
                log("not recorded, ignore");
            } else {
                LinkProperties curNetworkLp = network.linkProperties;
                LinkProperties newNetworkLp = na.linkProperties;
                if (network.networkInfo.getState() != NetworkInfo.State.CONNECTED || curNetworkLp == null || TextUtils.isEmpty(curNetworkLp.getInterfaceName())) {
                    log("some key parameter is null, ignore");
                } else {
                    boolean isLpIdentical = curNetworkLp.keyEquals(newNetworkLp);
                    log("LinkProperties Identical are " + isLpIdentical);
                    NetworkSpecifier ns = null;
                    Object nsObject = network.networkCapabilities.getNetworkSpecifier();
                    if (nsObject instanceof NetworkSpecifier) {
                        ns = (NetworkSpecifier) nsObject;
                    }
                    NetworkSpecifier ns2 = null;
                    Object ns2Object = na.networkCapabilities.getNetworkSpecifier();
                    if (ns2Object instanceof NetworkSpecifier) {
                        ns2 = (NetworkSpecifier) ns2Object;
                    }
                    if (ns != null && ns2 != null && ns.satisfiedBy(ns2) && isLpIdentical) {
                        log("apparently satisfied");
                        return network;
                    }
                }
            }
        }
        return null;
    }

    public void setupUniqueDeviceName() {
        String hostname;
        String hostname2 = SystemProperties.get("net.hostname");
        if (TextUtils.isEmpty(hostname2) || hostname2.length() < 8) {
            String id = Settings.Secure.getString(this.mContext.getContentResolver(), "android_id");
            if (!TextUtils.isEmpty(id)) {
                if (TextUtils.isEmpty(hostname2)) {
                    if (TV_DEVICE) {
                        hostname = SystemProperties.get("ro.product.model", "");
                    } else {
                        hostname = SystemProperties.get("ro.config.marketing_name", "");
                    }
                    if (TextUtils.isEmpty(hostname)) {
                        hostname2 = Build.MODEL.replace(" ", "_");
                        if (hostname2 != null && hostname2.length() > 18) {
                            hostname2 = hostname2.substring(0, 18);
                        }
                    } else {
                        hostname2 = hostname.replace(" ", "_");
                    }
                }
                String hostname3 = hostname2 + AwarenessInnerConstants.DASH_KEY + id;
                if (hostname3 != null && hostname3.length() > 25) {
                    hostname3 = hostname3.substring(0, 25);
                }
                SystemProperties.set("net.hostname", hostname3);
            }
        }
    }

    public boolean releaseNetworkSliceByApp(NetworkRequest networkRequest, int uid) {
        return HwNetworkSliceManager.getInstance().releaseNetworkSliceByApp(networkRequest, uid);
    }

    private void setExplicitlyUnselected(NetworkAgentInfo nai) {
        if (nai != null) {
            nai.networkMisc.explicitlySelected = false;
            nai.networkMisc.acceptUnvalidated = false;
            if (nai.networkInfo != null && ConnectivityManager.getNetworkTypeName(1).equals(nai.networkInfo.getTypeName())) {
                log("WiFi+ switch from WiFi to Cellular, enableDefaultTypeApn explicitly.");
                enableDefaultTypeApn(true);
            }
        }
    }

    private IDnsResolver getDnsResolver() {
        return IDnsResolver.Stub.asInterface(ServiceManager.getService(DNS_RESOLVER_SERVICE));
    }

    private void updateNetworkConcurrently(NetworkAgentInfo netAgent, NetworkInfo newInfo) {
        NetworkInfo oldInfo;
        int i;
        NetworkInfo.State netState = newInfo.getState();
        INetd netd = NetdService.getInstance();
        synchronized (netAgent) {
            oldInfo = netAgent.networkInfo;
            netAgent.networkInfo = newInfo;
        }
        if (oldInfo != null && oldInfo.getState() == netState) {
            log("updateNetworkConcurrently, ignoring duplicate network state non-change");
        } else if (netd == null) {
            loge("updateNetworkConcurrently, invalid member, netd = null");
        } else {
            netAgent.setCurrentScore(0);
            try {
                int i2 = netAgent.network.netId;
                if (netAgent.networkCapabilities.hasCapability(13)) {
                    i = 0;
                } else {
                    i = 2;
                }
                netd.networkCreatePhysical(i2, i);
                getDnsResolver().createNetworkCache(netAgent.network.netId);
                netAgent.created = true;
                this.mCsi.hwUpdateLinkProperties(netAgent, (LinkProperties) null);
                log("updateNetworkConcurrently, nai.networkInfo = " + netAgent.networkInfo);
                Bundle redirectUrlBundle = new Bundle();
                redirectUrlBundle.putString(NetworkAgent.REDIRECT_URL_KEY, "");
                netAgent.asyncChannel.sendMessage(528391, 4, 0, redirectUrlBundle);
            } catch (RemoteException | ServiceSpecificException e) {
                loge("updateNetworkConcurrently, Error creating network");
            }
        }
    }

    private void triggerRoamingNetworkMonitor(NetworkAgentInfo networkAgent) {
    }

    private int getCurrentNetworkId() {
        int networkId = -1;
        ConnectivityService cs = null;
        ConnectivityService cs2 = this.mCsi;
        if (cs2 instanceof ConnectivityService) {
            cs = cs2;
        }
        if (cs == null) {
            return -1;
        }
        Network network = cs.getNetworkForTypeWifi();
        if (network != null) {
            networkId = network.netId;
        }
        log("networkId= " + networkId);
        return networkId;
    }

    private void triggerInvalidlinkNetworkMonitor(NetworkAgentInfo networkAgent, Handler handler) {
        int netId = getCurrentNetworkId();
        if (netId == -1) {
            loge("netId is invalid");
        } else if (handler != null) {
            handler.sendMessage(handler.obtainMessage(41, 0, netId, ""));
        }
    }

    private void enableDefaultTypeApn(boolean isEnabled) {
        log("enableDefaultTypeApn= " + isEnabled);
        String str = AppActConstant.VALUE_TRUE;
        String defaultMobileEnable = SystemProperties.get("sys.defaultapn.enabled", str);
        log("DEFAULT_MOBILE_ENABLE before state is " + defaultMobileEnable);
        if (!isEnabled) {
            str = AppActConstant.VALUE_FALSE;
        }
        SystemProperties.set("sys.defaultapn.enabled", str);
        HwTelephonyManagerInner hwTm = HwTelephonyManagerInner.getDefault();
        if (hwTm != null) {
            hwTm.setDefaultMobileEnable(isEnabled);
        }
    }

    /* access modifiers changed from: private */
    public class InterfaceBlockInfo {
        int block;
        long timeStamp;

        InterfaceBlockInfo(int block2, long timeStamp2) {
            this.timeStamp = timeStamp2;
            this.block = block2;
        }
    }

    /* access modifiers changed from: private */
    public class CacheNetworkState {
        private Map<String, InterfaceBlockInfo> cacheNetworkStateMapByIntf = new ConcurrentHashMap();
        private long timeStamp = SystemClock.elapsedRealtime();

        CacheNetworkState() {
        }

        /* access modifiers changed from: package-private */
        public InterfaceBlockInfo getBlockByIntf(String intf) {
            return this.cacheNetworkStateMapByIntf.get(intf);
        }

        /* access modifiers changed from: package-private */
        public void setBlockByIntf(String intf, int blockValue, long timeStampValue) {
            this.cacheNetworkStateMapByIntf.put(intf, new InterfaceBlockInfo(blockValue, timeStampValue));
        }

        /* access modifiers changed from: package-private */
        public boolean isNeedDelete(long currentTime) {
            if (currentTime - this.timeStamp > 2000) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public class InternalHandler extends Handler {
        InternalHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                HwConnectivityServiceEx.this.handleFreshCache();
            } else if (i == 2) {
                synchronized (HwConnectivityServiceEx.this.mCacheNetworkStateMapByUid) {
                    int uid = msg.arg1;
                    if (HwConnectivityServiceEx.this.mCacheNetworkStateMapByUid.get(Integer.valueOf(uid)) != null) {
                        HwConnectivityServiceEx.this.mCacheNetworkStateMapByUid.remove(Integer.valueOf(uid));
                    }
                }
            } else if (i == 3) {
                synchronized (HwConnectivityServiceEx.this.mCacheNetworkStateMapByUid) {
                    HwConnectivityServiceEx.this.mCacheNetworkStateMapByUid.clear();
                }
            }
        }
    }

    public int getCacheNetworkState(int uid, String interfaceName) {
        CacheNetworkState cacheNetworkState;
        InterfaceBlockInfo interfaceBlockInfo;
        String ifaceName = TextUtils.isEmpty(interfaceName) ? HwBluetoothManagerServiceEx.DEFAULT_PACKAGE_NAME : interfaceName;
        synchronized (this.mCacheNetworkStateMapByUid) {
            cacheNetworkState = this.mCacheNetworkStateMapByUid.get(Integer.valueOf(uid));
        }
        if (cacheNetworkState == null || (interfaceBlockInfo = cacheNetworkState.getBlockByIntf(ifaceName)) == null || SystemClock.elapsedRealtime() - interfaceBlockInfo.timeStamp >= 2000) {
            return -1;
        }
        return interfaceBlockInfo.block;
    }

    private boolean isSystem(int uid) {
        return uid < 10000;
    }

    public void setCacheNetworkState(int uid, String interfaceName, boolean isBlock) {
        String ifaceName = TextUtils.isEmpty(interfaceName) ? HwBluetoothManagerServiceEx.DEFAULT_PACKAGE_NAME : interfaceName;
        if (!CTS_PACKAGE.equals(this.mContext.getPackageManager().getNameForUid(uid))) {
            if (!isSystem(Process.myUid())) {
                log("setCacheNetworkState fail , Process.myUid" + Process.myUid());
                return;
            }
            log("set " + uid + " " + ifaceName + " value " + isBlock);
            synchronized (this.mCacheNetworkStateMapByUid) {
                CacheNetworkState cacheNetworkState = this.mCacheNetworkStateMapByUid.get(Integer.valueOf(uid));
                if (cacheNetworkState == null) {
                    cacheNetworkState = new CacheNetworkState();
                    this.mCacheNetworkStateMapByUid.put(Integer.valueOf(uid), cacheNetworkState);
                }
                cacheNetworkState.setBlockByIntf(ifaceName, isBlock ? 1 : 0, SystemClock.elapsedRealtime());
                if (!this.mHandler.hasMessages(1)) {
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), 10000);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFreshCache() {
        long currentTime = SystemClock.elapsedRealtime();
        synchronized (this.mCacheNetworkStateMapByUid) {
            Iterator<Map.Entry<Integer, CacheNetworkState>> iter = this.mCacheNetworkStateMapByUid.entrySet().iterator();
            while (iter.hasNext()) {
                CacheNetworkState cacheNetworkState = null;
                Object object = iter.next().getValue();
                if (object instanceof CacheNetworkState) {
                    cacheNetworkState = (CacheNetworkState) object;
                }
                if (cacheNetworkState != null && cacheNetworkState.isNeedDelete(currentTime)) {
                    iter.remove();
                }
            }
            if (this.mCacheNetworkStateMapByUid.size() != 0 && !this.mHandler.hasMessages(1)) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), 10000);
            }
        }
    }

    private void initDistributedNetService() {
        this.mDistributedNetManager = loadClass(REFLECTION_DISTRIBUTED_NET_MANAGER, this.mContext);
    }

    private boolean isDistributedNetSupport() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch") || Settings.System.getInt(this.mContext.getContentResolver(), DISTRIBUTED_NET_FEATURE_KEY, 0) == 1;
    }

    private static Object loadClass(String className, Context context) {
        if (TextUtils.isEmpty(className) || context == null) {
            return null;
        }
        try {
            return Class.forName(className).getConstructor(Context.class).newInstance(context);
        } catch (ClassNotFoundException e) {
            loge("Class Not found Exception");
            return null;
        } catch (IllegalAccessException e2) {
            loge("Illegal access exception");
            return null;
        } catch (InstantiationException e3) {
            loge("Instantiation exception");
            return null;
        } catch (NoSuchMethodException e4) {
            loge("NoSuchMethodException exception");
            return null;
        } catch (InvocationTargetException e5) {
            loge("InvocationTargetException exception");
            return null;
        }
    }
}
