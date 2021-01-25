package com.android.server.intellicom.networkslice;

import android.common.HwPartBaseTelephonyFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.os.Binder;
import android.os.Bundle;
import android.os.Looper;
import android.os.Messenger;
import android.util.Log;
import com.android.server.intellicom.common.IntellicomUtils;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.intellicom.networkslice.css.BoosterProxy;
import com.android.server.intellicom.networkslice.css.HwNetworkSliceSettingsObserver;
import com.android.server.intellicom.networkslice.css.NetworkSliceCallback;
import com.android.server.intellicom.networkslice.css.NetworkSlicesHandler;
import com.android.server.intellicom.networkslice.model.FqdnIps;
import com.android.server.intellicom.networkslice.model.NetworkSliceInfo;
import com.android.server.intellicom.networkslice.model.OsAppId;
import com.android.server.intellicom.networkslice.model.RouteSelectionDescriptor;
import com.android.server.intellicom.networkslice.model.SliceRouteInfo;
import com.android.server.intellicom.networkslice.model.TrafficDescriptors;
import com.huawei.android.content.pm.UserInfoExt;
import com.huawei.android.net.NetworkCapabilitiesEx;
import com.huawei.android.net.StringNetworkSpecifierEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserManagerExt;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.internal.telephony.PackageManagerExt;
import huawei.android.net.slice.INetworkSliceStateListener;
import huawei.android.net.slice.TrafficDescriptor;
import huawei.com.android.server.policy.HwGlobalActionsData;
import huawei.net.NetworkRequestExt;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HwNetworkSliceManager {
    private static final String ACTION_MAKE_DEFAULT_PHONE_DONE = "com.huawei.intent.action.MAKE_DEFAULT_PHONE_DONE";
    private static final String ACTION_NETWORK_SLICE_LOST = "com.huawei.intent.action.NETWORK_SLICE_LOST";
    private static final String ACTION_RIL_CONNECTED = "com.huawei.intent.action.RIL_CONNECTED";
    private static final int CHANGE_TYPE_DECREASE = -1;
    private static final int CHANGE_TYPE_INCREASE = 1;
    private static final int CHANGE_TYPE_ZEROING = 0;
    private static final boolean DBG = SystemPropertiesEx.getBoolean("ro.debuggable", false);
    private static final String HW_SYSTEM_SERVER_START = "com.huawei.systemserver.START";
    private static final int INVALID_UID = -1;
    private static final int IPV4_LEN = 4;
    private static final int IPV6_LEN = 16;
    private static final boolean IS_NR_SLICES_SUPPORTED = HwPartBaseTelephonyFactory.loadFactory().createHwInnerTelephonyManager().isNrSlicesSupported();
    private static final int MAX_NETWORK_SLICE = 6;
    private static final String NETWORK_ON_LOST_DNN = "dnn";
    private static final String NETWORK_ON_LOST_PDU_SESSION_TYPE = "pduSessionType";
    private static final String NETWORK_ON_LOST_SNSSAI = "sNssai";
    private static final String NETWORK_ON_LOST_SSCMODE = "sscMode";
    public static final String OS_ID = "01020304050607080102030405060708#";
    private static final int REQUEST_NETWORK_TIMEOUT = 10000;
    private static final String SEPARATOR_FOR_NORMAL_DATA = ",";
    private static final String SINGLE_INDENT = "  ";
    private static final String TAG = "HwNetworkSliceManager";
    private BoosterProxy mBoosterProxy;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private NetworkSlicesHandler mHandler;
    private AtomicBoolean mHasMatchAllSlice;
    private HwNetworkSliceSettingsObserver mHwNetworkSliceSettingsObserver;
    private boolean mIsFirstPhoneMakeDone;
    private AtomicBoolean mIsMatchAllRequsted;
    private boolean mIsMatchRequesting;
    private AtomicBoolean mIsReady;
    private boolean mIsScreenOn;
    private boolean mIsUrspAvailable;
    private boolean mIsWifiConnect;
    private Looper mLooper;
    private AtomicInteger mNetworkRequestCountor;
    private AtomicInteger mNetworkSliceCounter;
    private List<NetworkSliceInfo> mNetworkSliceInfos;

    private HwNetworkSliceManager() {
        this.mIsFirstPhoneMakeDone = true;
        this.mNetworkSliceInfos = new ArrayList();
        this.mNetworkSliceCounter = new AtomicInteger(0);
        this.mIsReady = new AtomicBoolean(false);
        this.mIsMatchAllRequsted = new AtomicBoolean(false);
        this.mHasMatchAllSlice = new AtomicBoolean(false);
        this.mIsMatchRequesting = false;
        this.mNetworkRequestCountor = new AtomicInteger(1);
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.server.intellicom.networkslice.HwNetworkSliceManager.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) {
                    HwNetworkSliceManager.logw("intent or intent.getAction is null.");
                    return;
                }
                String action = intent.getAction();
                char c = 65535;
                switch (action.hashCode()) {
                    case -1094345150:
                        if (action.equals(HwNetworkSliceManager.ACTION_NETWORK_SLICE_LOST)) {
                            c = 4;
                            break;
                        }
                        break;
                    case -374985024:
                        if (action.equals("com.huawei.systemserver.START")) {
                            c = 0;
                            break;
                        }
                        break;
                    case -343630553:
                        if (action.equals(SmartDualCardConsts.SYSTEM_STATE_NAME_WIFI_NETWORK_STATE_CHANGED)) {
                            c = 3;
                            break;
                        }
                        break;
                    case 568726786:
                        if (action.equals(HwNetworkSliceManager.ACTION_MAKE_DEFAULT_PHONE_DONE)) {
                            c = 1;
                            break;
                        }
                        break;
                    case 1849863807:
                        if (action.equals(HwNetworkSliceManager.ACTION_RIL_CONNECTED)) {
                            c = 2;
                            break;
                        }
                        break;
                }
                if (c == 0) {
                    HwNetworkSliceManager.this.mBoosterProxy.registerBoosterCallback();
                    HwNetworkSliceManager.this.mHwNetworkSliceSettingsObserver.initWhitelist(HwNetworkSliceManager.this.mContext, HwNetworkSliceManager.this.mLooper);
                    HwNetworkSliceManager.log("register booster callback.");
                } else if (c == 1) {
                    HwNetworkSliceManager.log("Receive ACTION_MAKE_DEFAULT_PHONE_DONE");
                    HwNetworkSliceManager.this.handleMakeDefaultPhoneDone();
                } else if (c == 2) {
                    HwNetworkSliceManager.log("Receive ACTION_RIL_CONNECTED");
                } else if (c == 3) {
                    HwNetworkSliceManager.log("Receive NETWORK_STATE_CHANGED_ACTION");
                    HwNetworkSliceManager.this.onWifiNetworkStateChanged(intent);
                } else if (c != 4) {
                    HwNetworkSliceManager.log("BroadcastReceiver error: " + action);
                } else {
                    HwNetworkSliceManager.log("Receive ACTION_NETWORK_SLICE_LOST");
                    HwNetworkSliceManager.this.handleNetworkSliceLost(intent);
                }
            }
        };
    }

    public static HwNetworkSliceManager getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public void init(Context context, Looper looper) {
        if (Stream.of(context, looper).anyMatch($$Lambda$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            log("context or looper is null, fail to init HwNetworkSliceManager.");
            return;
        }
        log("construct HwNetworkSliceManager");
        this.mHandler = new NetworkSlicesHandler(looper);
        this.mContext = context;
        this.mLooper = looper;
        this.mHwNetworkSliceSettingsObserver = HwNetworkSliceSettingsObserver.getInstance();
        this.mHwNetworkSliceSettingsObserver.init(context, looper);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.systemserver.START");
        filter.addAction(ACTION_MAKE_DEFAULT_PHONE_DONE);
        filter.addAction(ACTION_RIL_CONNECTED);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_WIFI_NETWORK_STATE_CHANGED);
        filter.addAction(ACTION_NETWORK_SLICE_LOST);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        this.mBoosterProxy = BoosterProxy.getInstance();
        initNetworkSliceInfos();
        this.mHandler.registerForAppStateObserver();
        this.mIsReady.getAndSet(true);
    }

    public void requestNetworkSliceForPackageName(int uid) {
        if (!isCanMatchNetworkSlices()) {
            log("requestNetworkSlice, current environment cannot match slices");
            return;
        }
        String packageName = this.mContext.getPackageManager().getNameForUid(uid);
        log("requestNetworkSliceForPackageName, uid = " + uid);
        if (!this.mHwNetworkSliceSettingsObserver.isNeedToRequestSliceForAppIdAuto(packageName)) {
            log("No need to requestNetwork for uid = " + uid);
            return;
        }
        requestNetwork(uid, requestNetworkSlice(new TrafficDescriptors.Builder().setUid(uid).build()));
    }

    public void requestNetworkSliceForFqdn(int uid, String fqdn, List<String> ipAddresses, int ipAddressesCount) {
        if (!isCanMatchNetworkSlices()) {
            log("requestNetworkSliceForFqdn, current environment cannot match slices");
        } else if (ipAddresses == null) {
            logd("requestNetworkSliceForFqdn ipAddresses is null.");
        } else {
            logd("requestNetworkSliceForFqdn for uid = " + uid);
            if (!this.mHwNetworkSliceSettingsObserver.isNeedToRequestSliceForFqdnAuto(fqdn, uid)) {
                logd("requestNetworkSliceForFqdn No need to requestNetwork for uid = " + uid);
                return;
            }
            Set<InetAddress> ipv4 = new HashSet<>();
            Set<InetAddress> ipv6 = new HashSet<>();
            for (String address : ipAddresses) {
                try {
                    InetAddress ip = InetAddress.getByName(address);
                    if (ip != null) {
                        int length = ip.getAddress().length;
                        if (length == 4) {
                            ipv4.add(ip);
                        } else if (length != 16) {
                            logw("ip length wrong, len = " + ip.getAddress().length);
                        } else {
                            ipv6.add(ip);
                        }
                    }
                } catch (UnknownHostException e) {
                    logw("RequestNetworkSliceForFqdn has UnknownHostException");
                }
            }
            requestNetwork(uid, requestNetworkSlice(new TrafficDescriptors.Builder().setUid(uid).setFqdn(fqdn).setFqdnIps(new FqdnIps.Builder().setIpv4Addr(ipv4).setIpv6Addr(ipv6).build()).build()));
        }
    }

    public void handleUidGone(int uid) {
        if (IS_NR_SLICES_SUPPORTED) {
            for (NetworkSliceInfo nsi : this.mNetworkSliceInfos) {
                unbindProcessToNetworkForSingleUid(uid, nsi, false);
            }
        }
    }

    public NetworkSliceInfo getNetworkSliceInfoByPara(Object o, NetworkSliceInfo.ParaType type) {
        for (NetworkSliceInfo sliceInfo : this.mNetworkSliceInfos) {
            if (sliceInfo.isRightNetworkSlice(o, type)) {
                log("getNetworkSliceInfoByPara, sliceInfo = " + sliceInfo);
                return sliceInfo;
            }
        }
        log("getNetworkSliceInfoByPara, return null");
        return null;
    }

    public void handleUidRemoved(String packageName) {
        Set<Integer> removedUids;
        if (IS_NR_SLICES_SUPPORTED && (removedUids = lambda$getUidsFromAppIds$0$HwNetworkSliceManager(packageName)) != null) {
            for (Integer num : removedUids) {
                int uid = num.intValue();
                for (NetworkSliceInfo nsi : this.mNetworkSliceInfos) {
                    unbindProcessToNetworkForSingleUid(uid, nsi, true);
                }
            }
        }
    }

    public void handleUrspChanged(Bundle data) {
        if (!IS_NR_SLICES_SUPPORTED) {
            logd("requestNetworkSlice, current environment cannot match slices");
        } else if (data == null) {
            logw("handleUrspChanged, data is null");
        } else {
            this.mIsReady.getAndSet(false);
            this.mIsUrspAvailable = true;
            cleanEnvironment();
            RouteSelectionDescriptor rsd = RouteSelectionDescriptor.makeRouteSelectionDescriptor(data);
            this.mIsReady.getAndSet(true);
            tryToActivateSliceForForegroundApp();
            if (!rsd.isMatchAll()) {
                this.mHasMatchAllSlice.getAndSet(false);
                return;
            }
            this.mHasMatchAllSlice.getAndSet(true);
            NetworkSliceInfo networkSliceInfo = getNetworkSliceInfoByPara(null, NetworkSliceInfo.ParaType.ROUTE_SELECTION_DESCRIPTOR);
            if (networkSliceInfo != null) {
                log("networkSliceInfo != null is true, setRouteSelectionDescriptor = " + rsd);
                networkSliceInfo.setRouteSelectionDescriptor(rsd);
                networkSliceInfo.setTempTrafficDescriptors(new TrafficDescriptors.Builder().setRouteBitmap(data.getByte(TrafficDescriptors.TDS_ROUTE_BITMAP, (byte) 0).byteValue()).build());
                changeNetworkSliceCounter(1);
            }
            log("match all slice start to active when ursp changed, networkSliceInfo= " + networkSliceInfo);
            if (!isCanRequestNetwork()) {
                log("handleUrspChanged can not request network");
                return;
            }
            this.mIsMatchRequesting = true;
            requestNetwork(-1, networkSliceInfo);
        }
    }

    public void restoreSliceEnvironment() {
        requestMatchAllSlice();
        tryToActivateSliceForForegroundApp();
    }

    private void tryToActivateSliceForForegroundApp() {
        int uid = IntellicomUtils.getForegroundAppUid(this.mContext);
        if (-1 != uid) {
            requestNetworkSliceForPackageName(uid);
        }
    }

    public void requestMatchAllSlice() {
        if (this.mHasMatchAllSlice.get() && !this.mIsMatchAllRequsted.get() && !this.mIsMatchRequesting && isCanRequestNetwork()) {
            this.mIsMatchRequesting = true;
            for (NetworkSliceInfo nsi : this.mNetworkSliceInfos) {
                if (nsi.isMatchAll()) {
                    requestNetwork(-1, nsi);
                    return;
                }
            }
        }
    }

    public NetworkRequest requestNetworkSliceForNetworkCap(NetworkCapabilities nc, Messenger messenger, int uid) {
        if (nc == null || messenger == null) {
            return null;
        }
        if (!isCanMatchNetworkSlices()) {
            log("requestNetworkSliceForNetworkCap, current environment cannot match slices");
            return null;
        }
        int apnType = IntellicomUtils.getApnTypeFromNetworkCapabilities(nc);
        String apnName = getApnName(apnType);
        int cct = getCct(apnType);
        log("request network slice for nc=" + nc + ",apnType= " + apnType + ",apnName= " + apnName + ",cct= " + cct);
        if (!isNeedToRequestForNetworkCap(cct, apnName, apnType, uid)) {
            logd("No need to requestNetwork for network capability, dnn and cct");
            return null;
        }
        TrafficDescriptors td = new TrafficDescriptors.Builder().setDnn(apnName).setUid(uid).setMessenger(messenger).setNeedToCreateRequest(true).setCct(cct).build();
        NetworkSliceInfo networkSliceInfo = requestNetworkSlice(td);
        if (networkSliceInfo == null) {
            return null;
        }
        NetworkCapabilitiesEx newNcEx = new NetworkCapabilitiesEx(nc);
        newNcEx.addCapability(networkSliceInfo.getNetworkCapability());
        newNcEx.addTransportType(0);
        NetworkRequest request = new NetworkRequestExt(newNcEx.getNetworkCapabilities(), -1, getNextRequestId()).getNetworkRequest();
        tryRequestNetwork(uid, networkSliceInfo, td, request, 10000);
        return request;
    }

    public void requestNetworkSliceForIp(int uid, byte[] ip, String protocolId, String remotePort) {
        if (!isCanMatchNetworkSlices()) {
            logd("requestNetworkSliceForIp, current environment cannot match slices");
            return;
        }
        try {
            requestNetwork(uid, requestNetworkSlice(new TrafficDescriptors.Builder().setUid(uid).setIp(InetAddress.getByAddress(ip)).setProtocolId(protocolId).setRemotePort(remotePort).build()));
        } catch (UnknownHostException e) {
            logw("addr is of illegal length");
        }
    }

    public NetworkRequest requestNetworkSliceForSignedApp(int uid, TrafficDescriptor trafficDescriptor, Messenger messenger, int timeoutMs) {
        String packageName;
        if (!isCanMatchNetworkSlicesForSignedApp()) {
            log("requestNetworkSliceForSignedApp, current environment cannot match slices");
            return null;
        } else if (!this.mHwNetworkSliceSettingsObserver.isCooperativeApp(uid)) {
            logd("No need to requestNetwork for signed apk");
            return null;
        } else {
            String appId = trafficDescriptor.getAppId();
            Context context = this.mContext;
            if (context != null) {
                packageName = context.getPackageManager().getNameForUid(uid);
            } else {
                packageName = null;
            }
            if (appId == null || appId.equals(packageName)) {
                TrafficDescriptors td = new TrafficDescriptors.Builder().setUid(uid).setIp(trafficDescriptor.getIp()).setDnn(trafficDescriptor.getDnn()).setFqdn(trafficDescriptor.getFqdn()).setProtocolId(String.valueOf(trafficDescriptor.getProtocolId())).setRemotePort(trafficDescriptor.getRemotePort()).setCct(trafficDescriptor.getConnectionCapability()).setMessenger(messenger).setNeedToCreateRequest(true).build();
                NetworkSliceInfo networkSliceInfo = requestNetworkSlice(td);
                if (networkSliceInfo == null) {
                    return null;
                }
                NetworkCapabilities nc = new NetworkCapabilities();
                nc.addCapability(networkSliceInfo.getNetworkCapability());
                nc.addTransportType(0);
                NetworkRequest request = new NetworkRequest(nc, -1, getNextRequestId(), NetworkRequest.Type.REQUEST);
                tryRequestNetwork(uid, networkSliceInfo, td, request, timeoutMs);
                return request;
            }
            loge("requestNetworkSliceForSignedApp invalid input for appid");
            return null;
        }
    }

    public boolean releaseNetworkSliceBySignedApp(int uid, int requestId) {
        if (!IS_NR_SLICES_SUPPORTED) {
            return false;
        }
        NetworkSliceInfo networkSliceInfo = getNetworkSliceInfoByPara(Integer.valueOf(requestId), NetworkSliceInfo.ParaType.NETWORK_REQUEST_ID);
        if (networkSliceInfo == null) {
            logd("networkSliceInfo is null");
            return false;
        }
        unbindProcessToNetworkForSingleUid(uid, networkSliceInfo, true);
        return true;
    }

    public void onNetworkAvailable(int uid, int netId, NetworkRequest request) {
        log("onNetworkAvailable request = " + request + ". netId = " + netId + ", uid = " + uid);
        NetworkSliceInfo networkSliceInfo = getNetworkSliceInfoByPara(request, NetworkSliceInfo.ParaType.NETWORK_REQUEST);
        if (networkSliceInfo == null) {
            log("onNetworkAvailable - networkSliceInfo is null");
            return;
        }
        networkSliceInfo.setNetId(netId);
        if (networkSliceInfo.isMatchAll()) {
            log("match_all do not need to bind route");
            this.mIsMatchAllRequsted.getAndSet(true);
            this.mIsMatchRequesting = false;
            return;
        }
        Set<Integer> triggerActivationUids = new HashSet<>();
        triggerActivationUids.addAll(networkSliceInfo.getNetworkCallback().getRequestUids());
        for (TrafficDescriptors tds : networkSliceInfo.getSliceRouteInfos().keySet()) {
            bindNetworkSliceProcessToNetwork(uid, triggerActivationUids, networkSliceInfo, networkSliceInfo.getFqdnIps(tds), tds);
            SliceRouteInfo sri = networkSliceInfo.getSliceRouteInfo(tds);
            if (sri != null) {
                for (FqdnIps fqdnIps : sri.getWaittingFqdnIps()) {
                    bindNetworkSliceProcessToNetwork(uid, triggerActivationUids, networkSliceInfo, fqdnIps, tds);
                }
                networkSliceInfo.clearWaittingFqdnIps(tds);
                log("bind success networkSliceInfo = " + networkSliceInfo);
            }
        }
    }

    public void onNetworkLost(NetworkRequest request) {
        log("onNetworkLost request = " + request);
        if (request != null) {
            NetworkSliceInfo networkSliceInfo = getNetworkSliceInfoByPara(request, NetworkSliceInfo.ParaType.NETWORK_REQUEST);
            if (networkSliceInfo == null) {
                log("onNetworkLost - networkSliceInfo = null");
            } else if (networkSliceInfo.isMatchAll()) {
                this.mIsMatchAllRequsted.getAndSet(false);
                this.mIsMatchRequesting = false;
            } else {
                networkSliceInfo.clearUsedUids();
                int result = this.mBoosterProxy.unbindSingleNetId(networkSliceInfo.getNetId());
                log("unbind uid to network slice result = " + result);
            }
        }
    }

    public void onNetworkUnAvailable(int uid, NetworkRequest request) {
        NetworkSliceInfo nsi = getNetworkSliceInfoByPara(request, NetworkSliceInfo.ParaType.NETWORK_REQUEST);
        log("onNetworkUnAvailable request= " + request + " ,nsi= " + nsi);
        if (nsi != null) {
            if (nsi.isMatchAll()) {
                this.mIsMatchRequesting = false;
                this.mIsMatchAllRequsted.getAndSet(false);
            }
            recoveryNetworkSlice(nsi);
        }
    }

    public boolean releaseNetworkSliceByApp(NetworkRequest networkRequest, int uid) {
        if (networkRequest == null || !IS_NR_SLICES_SUPPORTED) {
            return false;
        }
        NetworkRequestExt networkRequestExt = new NetworkRequestExt();
        networkRequestExt.setNetworkRequest(networkRequest);
        if (networkRequestExt.getNetCapability5GSliceType() == -1) {
            return false;
        }
        NetworkSliceInfo networkSliceInfo = getNetworkSliceInfoByPara(Integer.valueOf(networkRequestExt.getRequestId()), NetworkSliceInfo.ParaType.NETWORK_REQUEST_ID);
        if (networkSliceInfo == null) {
            logd("networkSliceInfo is null");
            return false;
        }
        unbindProcessToNetworkForSingleUid(uid, networkSliceInfo, true);
        return true;
    }

    public NetworkSlicesHandler getHandler() {
        return this.mHandler;
    }

    public boolean registerListener(INetworkSliceStateListener networkSliceStateListener) {
        return HwNetworkSliceSettingsObserver.getInstance().registerListener(networkSliceStateListener);
    }

    public boolean unregisterListener(INetworkSliceStateListener networkSliceStateListener) {
        return HwNetworkSliceSettingsObserver.getInstance().unregisterListener(networkSliceStateListener);
    }

    public boolean isUpToToplimit() {
        return this.mNetworkSliceCounter.get() >= 6;
    }

    private void initNetworkSliceInfos() {
        this.mNetworkSliceInfos = new ArrayList();
        for (int nc = 33; nc <= 38; nc++) {
            NetworkRequest request = new NetworkRequest.Builder().addCapability(nc).addTransportType(0).build();
            NetworkSliceInfo networkSliceInfo = new NetworkSliceInfo();
            networkSliceInfo.setNetworkRequest(request);
            networkSliceInfo.setNetworkCapability(nc);
            this.mNetworkSliceInfos.add(networkSliceInfo);
        }
        changeNetworkSliceCounter(0);
    }

    private void tryRequestNetwork(int uid, NetworkSliceInfo networkSliceInfo, TrafficDescriptors td, NetworkRequest request, int timeoutMs) {
        if (Stream.of(networkSliceInfo, td, request).anyMatch($$Lambda$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            loge("tryRequestNetwork failed, input invalid");
            return;
        }
        networkSliceInfo.putCreatedNetworkRequest(request);
        NetworkRequestExt networkRequestExt = new NetworkRequestExt();
        networkRequestExt.setNetworkRequest(request);
        int requestId = networkRequestExt.getRequestId();
        networkSliceInfo.addMessenger(requestId, td.getMessenger());
        if (!td.isRequestAgain()) {
            requestNetwork(uid, networkSliceInfo, requestId, timeoutMs);
        } else if (networkSliceInfo.getNetId() != -1) {
            networkSliceInfo.getNetworkCallback().cacheRequestUid(uid);
            networkSliceInfo.getNetworkCallback().onAvailable(requestId);
        } else {
            logd("NetId is invalid, waitting for pdu session activation");
        }
    }

    private NetworkSliceInfo requestNetworkSlice(TrafficDescriptors td) {
        if (td == null) {
            logw("requestNetworkSlice, the TrafficDescriptor is null");
            return null;
        }
        Bundle result = this.mBoosterProxy.getNetworkSlice(td, this.mContext);
        if (result == null) {
            logw("can't get network slice");
            return null;
        }
        RouteSelectionDescriptor rsd = RouteSelectionDescriptor.makeRouteSelectionDescriptor(result);
        TrafficDescriptors tds = TrafficDescriptors.makeTrafficDescriptors(result);
        NetworkSliceInfo requestAgain = getNetworkSliceInfoByPara(rsd, NetworkSliceInfo.ParaType.ROUTE_SELECTION_DESCRIPTOR);
        log("requestAgain = " + requestAgain);
        if (requestAgain != null) {
            return handleRsdRequestAgain(requestAgain, td, tds);
        }
        if (isUpToToplimit()) {
            log("already has 6 network slices, do not request again. uid = " + td.getUid());
            return null;
        }
        NetworkSliceInfo networkSliceInfo = getNetworkSliceInfoByPara(null, NetworkSliceInfo.ParaType.ROUTE_SELECTION_DESCRIPTOR);
        if (networkSliceInfo != null) {
            networkSliceInfo.setRouteSelectionDescriptor(rsd);
            networkSliceInfo.cacheTrafficDescriptors(tds);
            networkSliceInfo.setTempTrafficDescriptors(tds);
            changeNetworkSliceCounter(1);
            log("Slice network has binded, networkSliceInfo = " + networkSliceInfo);
        }
        return networkSliceInfo;
    }

    private NetworkSliceInfo handleRsdRequestAgain(NetworkSliceInfo requestAgain, TrafficDescriptors requestTd, TrafficDescriptors tdsInUrsp) {
        if (requestAgain == null || requestTd == null) {
            return null;
        }
        SliceRouteInfo sri = requestAgain.getSliceRouteInfo(tdsInUrsp);
        if (sri == null) {
            return handleMultipleUrspFirstBind(requestAgain, tdsInUrsp, requestTd);
        }
        boolean isBinded = requestAgain.isBindCompleted(requestTd.getUid(), requestTd.getFqdnIps(), tdsInUrsp);
        if (tdsInUrsp.isIpTriad() || isBinded) {
            if (tdsInUrsp.isUidRouteBindType()) {
                sri.getUsedUids().add(Integer.valueOf(requestTd.getUid()));
            }
            log("networkSlice has allready binded uid:" + requestTd.getUid() + ",networkSliceInfo = " + requestAgain);
            if (!requestTd.isNeedToCreateRequest()) {
                return null;
            }
            requestTd.setRequestAgain(true);
            return requestAgain;
        } else if (requestAgain.getNetId() == -1) {
            return handleInvalidNetwork(requestAgain, tdsInUrsp, requestTd);
        } else {
            int bindResult = bindNetworkSliceProcessToNetworkForRequestAgain(requestTd.getUid(), requestAgain, requestTd.getFqdnIps(), tdsInUrsp);
            if (bindResult == 0 && tdsInUrsp.isUidRouteBindType()) {
                requestAgain.addUid(requestTd.getUid(), tdsInUrsp);
                requestAgain.addUsedUid(requestTd.getUid(), tdsInUrsp);
                tryAddSignedUid(requestTd.getUid(), tdsInUrsp, requestAgain);
            }
            log("no need to request this slice again. uid = " + requestTd.getUid() + " and bind result = " + bindResult);
            if (!requestTd.isNeedToCreateRequest()) {
                return null;
            }
            requestTd.setRequestAgain(true);
            return requestAgain;
        }
    }

    private NetworkSliceInfo handleMultipleUrspFirstBind(NetworkSliceInfo requestAgain, TrafficDescriptors tdsInUrsp, TrafficDescriptors requestTd) {
        if (requestAgain == null || tdsInUrsp == null || requestTd == null) {
            return null;
        }
        requestAgain.cacheTrafficDescriptors(tdsInUrsp);
        Set<Integer> triggerActivationUids = new HashSet<>();
        triggerActivationUids.add(Integer.valueOf(requestTd.getUid()));
        tryAddSignedUid(requestTd.getUid(), tdsInUrsp, requestAgain);
        bindNetworkSliceProcessToNetwork(requestTd.getUid(), triggerActivationUids, requestAgain, requestAgain.getFqdnIps(tdsInUrsp), tdsInUrsp);
        if (!requestTd.isNeedToCreateRequest()) {
            return null;
        }
        requestTd.setRequestAgain(true);
        return requestAgain;
    }

    private void tryAddSignedUid(int uid, TrafficDescriptors tds, NetworkSliceInfo nsi) {
        if (!Stream.of(tds, nsi, this.mContext).anyMatch($$Lambda$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE) && tds.isAtiveTriggeringApp(this.mContext.getPackageManager().getNameForUid(uid))) {
            nsi.addSignedUid(uid, tds);
        }
    }

    private NetworkSliceInfo handleInvalidNetwork(NetworkSliceInfo requestAgain, TrafficDescriptors tdsInUrsp, TrafficDescriptors requestTd) {
        if (requestAgain == null || tdsInUrsp == null || requestTd == null) {
            return null;
        }
        log("networkSlice doesn't finish activity, bind later networkSliceInfo = " + requestAgain);
        Set<FqdnIps> waittingFqdnIps = requestAgain.getWaittingFqdnIps(tdsInUrsp);
        if (waittingFqdnIps == null) {
            waittingFqdnIps = new HashSet();
            requestAgain.setWaittingFqdnIps(waittingFqdnIps, tdsInUrsp);
        }
        if (requestAgain.getFqdnIps(tdsInUrsp) != null) {
            waittingFqdnIps.add(requestAgain.getFqdnIps(tdsInUrsp).getNewFqdnIps(requestTd.getFqdnIps()));
        }
        if (!requestTd.isNeedToCreateRequest()) {
            return null;
        }
        requestTd.setRequestAgain(true);
        return requestAgain;
    }

    private int getNextRequestId() {
        return this.mNetworkRequestCountor.getAndIncrement();
    }

    private int bindNetworkSliceProcessToNetworkForRequestAgain(int uid, NetworkSliceInfo nsi, FqdnIps fqdnIps, TrafficDescriptors tds) {
        log("bindNetworkSliceProcessToNetworkForRequestAgain: uid = " + uid + " nsi = " + nsi + " fqdnIps = " + fqdnIps);
        if (nsi == null || tds == null) {
            return -3;
        }
        if (nsi.isMatchAll()) {
            return 0;
        }
        Bundle bindParas = new Bundle();
        BoosterProxy.fillBindParas(nsi.getNetId(), tds.getUrspPrecedence(), bindParas);
        int i = AnonymousClass2.$SwitchMap$com$android$server$intellicom$networkslice$model$TrafficDescriptors$RouteBindType[tds.getRouteBindType().ordinal()];
        if (i == 1) {
            fillUidBindParasForRequestAgain(bindParas, uid, nsi, tds);
        } else if (i == 2) {
            fillIpBindParas(bindParas, tds, fqdnIps, nsi);
        } else if (i != 3) {
            logw("Can not bind invalid tds");
        } else {
            fillUidBindParasForRequestAgain(bindParas, uid, nsi, tds);
            fillIpBindParas(bindParas, tds, fqdnIps, nsi);
        }
        return this.mBoosterProxy.bindProcessToNetwork(bindParas);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.intellicom.networkslice.HwNetworkSliceManager$2  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$intellicom$networkslice$model$TrafficDescriptors$RouteBindType = new int[TrafficDescriptors.RouteBindType.values().length];

        static {
            try {
                $SwitchMap$com$android$server$intellicom$networkslice$model$TrafficDescriptors$RouteBindType[TrafficDescriptors.RouteBindType.UID_TDS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$intellicom$networkslice$model$TrafficDescriptors$RouteBindType[TrafficDescriptors.RouteBindType.IP_TDS.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$intellicom$networkslice$model$TrafficDescriptors$RouteBindType[TrafficDescriptors.RouteBindType.UID_IP_TDS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$server$intellicom$networkslice$model$TrafficDescriptors$RouteBindType[TrafficDescriptors.RouteBindType.INVALID_TDS.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private int bindNetworkSliceProcessToNetwork(int uid, Set<Integer> triggerActivationUids, NetworkSliceInfo nsi, FqdnIps fqdnIps, TrafficDescriptors tds) {
        log("bindNetworkSliceProcessToNetwork: triggerActivationUids = " + triggerActivationUids + " nsi = " + nsi + " fqdnIps = " + fqdnIps);
        if (nsi == null || tds == null) {
            return -3;
        }
        Bundle bindParas = new Bundle();
        BoosterProxy.fillBindParas(nsi.getNetId(), tds.getUrspPrecedence(), bindParas);
        int i = AnonymousClass2.$SwitchMap$com$android$server$intellicom$networkslice$model$TrafficDescriptors$RouteBindType[tds.getRouteBindType().ordinal()];
        if (i == 1) {
            fillUidBindParas(bindParas, tds, triggerActivationUids, nsi, uid);
        } else if (i == 2) {
            fillIpBindParas(bindParas, tds, fqdnIps, nsi);
        } else if (i != 3) {
            logw("Can not bind invalid tds");
        } else {
            fillUidBindParas(bindParas, tds, triggerActivationUids, nsi, uid);
            fillIpBindParas(bindParas, tds, fqdnIps, nsi);
        }
        return this.mBoosterProxy.bindProcessToNetwork(bindParas);
    }

    private void fillUidBindParasForRequestAgain(Bundle bindParas, int uid, NetworkSliceInfo nsi, TrafficDescriptors tds) {
        Set<Integer> triggerActivationUids = new HashSet<>();
        triggerActivationUids.add(Integer.valueOf(uid));
        BoosterProxy.fillBindParas(triggerActivationUids, bindParas);
        nsi.addUid(uid, tds);
        nsi.addUsedUid(uid, tds);
    }

    private void fillUidBindParas(Bundle bindParas, TrafficDescriptors tds, Set<Integer> triggerActivationUids, NetworkSliceInfo nsi, int uid) {
        if (tds.isMatchNetworkCap()) {
            BoosterProxy.fillBindParas(triggerActivationUids, bindParas);
            nsi.addUids(triggerActivationUids, tds);
        } else {
            Set<Integer> allUids = new HashSet<>();
            Set<Integer> autoUids = getAutoUids(tds);
            allUids.addAll(autoUids);
            allUids.addAll(nsi.getSignedUids(tds));
            BoosterProxy.fillBindParas(allUids, bindParas);
            nsi.replaceUids(tds, autoUids);
        }
        if (tds.isMatchNetworkCap()) {
            nsi.addUsedUids(triggerActivationUids, tds);
        } else if (uid != -1) {
            nsi.addUsedUid(uid, tds);
        }
    }

    private Set<Integer> getAutoUids(TrafficDescriptors tds) {
        if (tds == null) {
            return new HashSet();
        }
        String uidsStr = getUidsFromAppIds(tds.getAppIds());
        if (uidsStr == null) {
            return new HashSet();
        }
        String[] uidStrs = uidsStr.split(",");
        if (uidStrs == null) {
            return new HashSet();
        }
        Set<Integer> tempUids = new HashSet<>();
        for (String uidStr : uidStrs) {
            try {
                tempUids.add(Integer.valueOf(uidStr));
            } catch (NumberFormatException e) {
                loge("wrong uid string");
            }
        }
        log("bindNetworkSliceProcessToNetwork getUidsFromAppIds=" + uidsStr);
        return tempUids;
    }

    private void fillIpBindParas(Bundle bindParas, TrafficDescriptors tds, FqdnIps fqdnIps, NetworkSliceInfo nsi) {
        if (tds.isMatchFqdn()) {
            FqdnIps newFqdnIps = fqdnIps;
            FqdnIps nsiFqdnIps = nsi.getFqdnIps(tds);
            if (nsiFqdnIps == null) {
                nsi.setFqdnIps(newFqdnIps, tds);
            } else {
                newFqdnIps = nsiFqdnIps.getNewFqdnIps(fqdnIps);
                nsi.mergeFqdnIps(fqdnIps, tds);
            }
            BoosterProxy.fillIpBindParasForFqdn(bindParas, newFqdnIps);
            return;
        }
        BoosterProxy.fillIpBindParasForIpTriad(bindParas, tds);
    }

    private String getUidsFromAppIds(String originAppIds) {
        String result;
        Set<String> appIds = getAppIdsWithoutOsId(originAppIds);
        if (appIds == null) {
            result = null;
        } else {
            result = (String) appIds.stream().map(new Function() {
                /* class com.android.server.intellicom.networkslice.$$Lambda$HwNetworkSliceManager$LUne0BElZASNasNlQ5XLhRBt_Q */

                @Override // java.util.function.Function
                public final Object apply(Object obj) {
                    return HwNetworkSliceManager.this.lambda$getUidsFromAppIds$0$HwNetworkSliceManager((String) obj);
                }
            }).flatMap($$Lambda$AMboGYEhuVfighAr4ZxypJn5wlE.INSTANCE).filter($$Lambda$HwNetworkSliceManager$J8PQoEjhjgm45kvrbzUJf1heKA.INSTANCE).map($$Lambda$HwNetworkSliceManager$pMG1Z11RzEEdIpBcuuWB1PujQfQ.INSTANCE).collect(Collectors.joining(","));
        }
        log("getUidsFromAppIds, uids = " + result);
        return result;
    }

    static /* synthetic */ boolean lambda$getUidsFromAppIds$1(Integer uids) {
        return uids != null;
    }

    private Set<String> getAppIdsWithoutOsId(String originAppIds) {
        String[] orgins = originAppIds.split(",");
        if (orgins == null || orgins.length == 0) {
            loge("getAppIdsWithoutOsId orgins == null, should not run here.");
            return null;
        }
        Set<String> appIds = new HashSet<>();
        for (String osIdAppId : orgins) {
            OsAppId osAppId = OsAppId.create(osIdAppId);
            if (osAppId != null) {
                appIds.add(osAppId.getAppId());
            }
        }
        return appIds;
    }

    private void cleanEnvironment() {
        int result = this.mBoosterProxy.unbindAllRoute();
        StringBuilder sb = new StringBuilder();
        sb.append("unbind all route, result = ");
        sb.append(result == 0);
        log(sb.toString());
        for (NetworkSliceInfo nsi : this.mNetworkSliceInfos) {
            unregisterNetworkCallback(nsi);
        }
        initNetworkSliceInfos();
        log("Clean enveronment done");
    }

    private void unregisterNetworkCallbackRemoteInitiated(RouteSelectionDescriptor rsd) {
        NetworkSliceInfo nsi = getNetworkSliceInfoByPara(rsd, NetworkSliceInfo.ParaType.ROUTE_SELECTION_DESCRIPTOR);
        if (nsi != null) {
            unregisterNetworkCallback(nsi);
            cleanRouteSelectionDescriptor(nsi);
        }
    }

    private void releaseNetworkSlice(NetworkSliceInfo nsi) {
        this.mBoosterProxy.unbindSingleNetId(nsi.getNetId());
        unregisterNetworkCallback(nsi);
        cleanRouteSelectionDescriptor(nsi);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v2, resolved type: android.net.ConnectivityManager */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v1, types: [android.net.ConnectivityManager$NetworkCallback, com.android.server.intellicom.networkslice.css.NetworkSliceCallback] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void unregisterNetworkCallback(NetworkSliceInfo nsi) {
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (cm == 0) {
            log("Can not get ConnectivityManager in onNetworkLost.");
            return;
        }
        ?? networkCallback = nsi.getNetworkCallback();
        if (networkCallback != 0) {
            log("unregisterNetworkCallback nsi = " + nsi);
            cm.unregisterNetworkCallback((ConnectivityManager.NetworkCallback) networkCallback);
            if (networkCallback.getNetwork() != null) {
                networkCallback.onLostForNetworkSlice(nsi.getNetworkCallback().getNetwork(), false);
            }
        }
        nsi.clear();
    }

    private void requestNetwork(int uid, NetworkSliceInfo networkSliceInfo) {
        if (networkSliceInfo == null || networkSliceInfo.getNetworkRequest() == null) {
            logw("networkSliceInfo is null with no request id");
            return;
        }
        NetworkRequestExt networkRequestExt = new NetworkRequestExt();
        networkRequestExt.setNetworkRequest(networkSliceInfo.getNetworkRequest());
        requestNetwork(uid, networkSliceInfo, networkRequestExt.getRequestId(), 10000);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.intellicom.networkslice.model.NetworkSliceInfo */
    /* JADX DEBUG: Multi-variable search result rejected for r3v2, resolved type: android.net.ConnectivityManager */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v3, types: [android.net.ConnectivityManager$NetworkCallback, com.android.server.intellicom.networkslice.css.NetworkSliceCallback] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void requestNetwork(int uid, NetworkSliceInfo networkSliceInfo, int requestId, int timeoutMs) {
        if (networkSliceInfo == 0) {
            logw("requestNetwork networkSliceInfo is null");
            return;
        }
        RouteSelectionDescriptor rsd = networkSliceInfo.getRouteSelectionDescriptor();
        TrafficDescriptors tds = networkSliceInfo.getTempTrafficDescriptors();
        if (rsd == null || tds == null) {
            logw("requestNetwork rsd is null or tds is null");
            return;
        }
        NetworkRequest request = networkSliceInfo.getNetworkRequest();
        if (request == null) {
            logw("Can not get request by capability:" + networkSliceInfo.getNetworkCapability());
            cleanRouteSelectionDescriptor(networkSliceInfo);
            return;
        }
        fillRsdIntoNetworkRquest(request, rsd, tds);
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (cm == 0) {
            log("Can not get ConnectivityManager");
            cleanRouteSelectionDescriptor(networkSliceInfo);
            return;
        }
        log("For " + uid + " start to request:" + request);
        ?? networkSliceCallback = new NetworkSliceCallback(uid, requestId, this.mHandler);
        cm.requestNetwork(request, (ConnectivityManager.NetworkCallback) networkSliceCallback, timeoutMs);
        networkSliceInfo.setNetworkCallback(networkSliceCallback);
    }

    private void fillRsdIntoNetworkRquest(NetworkRequest request, RouteSelectionDescriptor rsd, TrafficDescriptors tds) {
        NetworkRequestExt networkRequestExt = new NetworkRequestExt();
        networkRequestExt.setNetworkRequest(request);
        networkRequestExt.setDnn(rsd.getDnn());
        networkRequestExt.setSnssai(rsd.getSnssai());
        networkRequestExt.setSscMode(rsd.getSscMode());
        networkRequestExt.setPduSessionType(rsd.getPduSessionType());
        networkRequestExt.setRouteBitmap(tds.getRouteBitmap());
    }

    private void cleanRouteSelectionDescriptor(NetworkSliceInfo networkSliceInfo) {
        if (networkSliceInfo != null) {
            log("cleanRouteSelectionDescriptor:" + networkSliceInfo);
            clearRequest(networkSliceInfo.getNetworkRequest());
            networkSliceInfo.setRouteSelectionDescriptor(null);
            changeNetworkSliceCounter(-1);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v2, resolved type: android.net.ConnectivityManager */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v1, types: [android.net.ConnectivityManager$NetworkCallback, com.android.server.intellicom.networkslice.css.NetworkSliceCallback] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void recoveryNetworkSlice(NetworkSliceInfo networkSliceInfo) {
        if (networkSliceInfo != null) {
            this.mBoosterProxy.unbindSingleNetId(networkSliceInfo.getNetId());
            networkSliceInfo.setNetId(-1);
            networkSliceInfo.clearUids();
            networkSliceInfo.clearUsedUids();
            networkSliceInfo.setRouteSelectionDescriptor(null);
            networkSliceInfo.clearSliceRouteInfos();
            ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            ?? networkCallback = networkSliceInfo.getNetworkCallback();
            if (!(cm == 0 || networkCallback == 0)) {
                cm.unregisterNetworkCallback((ConnectivityManager.NetworkCallback) networkCallback);
            }
            networkSliceInfo.setNetworkCallback(null);
            clearRequest(networkSliceInfo.getNetworkRequest());
            changeNetworkSliceCounter(-1);
        }
    }

    private void changeNetworkSliceCounter(int changeType) {
        if (changeType == -1) {
            this.mNetworkSliceCounter.getAndDecrement();
        } else if (changeType == 0) {
            this.mNetworkSliceCounter = new AtomicInteger(0);
        } else if (changeType != 1) {
            logw("wrong type of network slice counter:" + changeType);
        } else {
            this.mNetworkSliceCounter.getAndIncrement();
        }
        this.mHwNetworkSliceSettingsObserver.notifyNetworkSliceStateChanged();
    }

    /* access modifiers changed from: private */
    public static void log(String msg) {
        Log.i(TAG, msg);
    }

    private static void logd(String msg) {
        if (DBG) {
            Log.d(TAG, msg);
        }
    }

    /* access modifiers changed from: private */
    public static void logw(String msg) {
        Log.w(TAG, msg);
    }

    private static void loge(String msg) {
        Log.e(TAG, msg);
    }

    private boolean isValidCapability(int capability) {
        return 33 <= capability && capability <= 38;
    }

    private void clearRequest(NetworkRequest request) {
        if (request != null) {
            log("clearRequest:" + request);
            NetworkRequestExt networkRequestExt = new NetworkRequestExt();
            networkRequestExt.setNetworkRequest(request);
            networkRequestExt.setDnn("");
            networkRequestExt.setSnssai("");
            networkRequestExt.setSscMode((byte) 0);
            networkRequestExt.setPduSessionType(0);
            networkRequestExt.setRouteBitmap((byte) 0);
        }
    }

    private boolean isCanRequestNetwork() {
        if (!isMobileDataClose() && !isAirplaneModeOn() && !isWifiConnected() && isSaState() && isDefaultDataOnMainCard() && !isVpnOn()) {
            return true;
        }
        return false;
    }

    private boolean isCanMatchNetworkSlices() {
        if (isEnvironmentReady() && isCanRequestNetwork()) {
            return true;
        }
        return false;
    }

    private boolean isEnvironmentReady() {
        if (IS_NR_SLICES_SUPPORTED && this.mIsReady.get() && isUrspAvailable()) {
            return true;
        }
        return false;
    }

    private boolean isCanMatchNetworkSlicesForSignedApp() {
        if (isEnvironmentReady() && isCanRequestNetworkForSignedApp()) {
            return true;
        }
        return false;
    }

    private boolean isCanRequestNetworkForSignedApp() {
        if (!isMobileDataClose() && !isAirplaneModeOn() && isSaState() && isDefaultDataOnMainCard()) {
            return true;
        }
        return false;
    }

    private boolean isMobileDataClose() {
        return !this.mHwNetworkSliceSettingsObserver.isMobileDataEnabled();
    }

    private boolean isSaState() {
        return this.mHwNetworkSliceSettingsObserver.isNrSa();
    }

    private boolean isWifiConnected() {
        return this.mIsWifiConnect;
    }

    private boolean isScreenOn() {
        return this.mIsScreenOn;
    }

    private boolean isAirplaneModeOn() {
        return this.mHwNetworkSliceSettingsObserver.isAirplaneModeOn();
    }

    private boolean isVpnOn() {
        return this.mHwNetworkSliceSettingsObserver.isVpnOn();
    }

    private boolean isDefaultDataOnMainCard() {
        return this.mHwNetworkSliceSettingsObserver.isDefaultDataOnMainCard();
    }

    private boolean isUrspAvailable() {
        return this.mIsUrspAvailable;
    }

    private void setUrspAvailable(boolean urspAvailable) {
        this.mIsUrspAvailable = urspAvailable;
    }

    private boolean hasApnType(int apnTypeBitmask, int type) {
        return (apnTypeBitmask & type) == type;
    }

    private int getPhoneIdFromNetworkCapabilities(NetworkCapabilities networkCapabilities) {
        int subId = -1;
        int phoneId = -1;
        NetworkSpecifier networkSpecifier = new NetworkCapabilitiesEx(networkCapabilities).getNetworkSpecifier();
        if (networkSpecifier != null) {
            try {
                subId = Integer.parseInt(new StringNetworkSpecifierEx(networkSpecifier).toString());
            } catch (NumberFormatException e) {
                Log.e(TAG, "getPhoneIdFromNetworkCapabilities exceptio");
            }
        }
        if (subId != -1) {
            phoneId = SubscriptionManagerEx.getPhoneId(subId);
        }
        log("getPhoneIdFromNetworkCapabilities, subId = " + subId + " phoneId:" + phoneId);
        return phoneId;
    }

    private boolean inApnTypeWhiteListForDnn(int apnType) {
        switch (apnType) {
            case 0:
            case 17:
            case 64:
            case 32768:
            case 65536:
            case 131072:
            case 262144:
            case 524288:
            case 1048576:
            case 2097152:
            case HwGlobalActionsData.FLAG_KEYCOMBINATION_INIT_STATE /* 4194304 */:
            case 8388608:
            case 16777216:
            case 33554432:
                return true;
            default:
                return false;
        }
    }

    private boolean isNeedToRequestForNetworkCap(int cct, String apnName, int apnType, int uid) {
        return this.mHwNetworkSliceSettingsObserver.isNeedToRequestSliceForCctAuto(String.valueOf(cct), uid) || (this.mHwNetworkSliceSettingsObserver.isNeedToRequestSliceForDnnAuto(apnName, uid) && !inApnTypeWhiteListForDnn(apnType));
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    /* renamed from: getUidsFromAppId */
    public Set<Integer> lambda$getUidsFromAppIds$0$HwNetworkSliceManager(String appid) {
        long identity = Binder.clearCallingIdentity();
        try {
            int userCount = UserManagerExt.get(this.mContext).getUserCount();
            List<UserInfoExt> users = UserManagerExt.getUsers(UserManagerExt.get(this.mContext), false);
            Binder.restoreCallingIdentity(identity);
            PackageManagerExt pm = new PackageManagerExt(this.mContext);
            Set<Integer> uids = new HashSet<>();
            if (users == null) {
                return uids;
            }
            for (int n = 0; n < userCount; n++) {
                int uid = pm.getPackageUidAsUser(appid, users.get(n).getUserId());
                log("getUidsFromAppIds uid " + uid);
                if (uid != -1) {
                    uids.add(Integer.valueOf(uid));
                }
            }
            return uids;
        } catch (Throwable pm2) {
            Binder.restoreCallingIdentity(identity);
            throw pm2;
        }
    }

    private String getApnName(int apnType) {
        for (HwNetworkSliceSettingsObserver.ApnObject apnObject : this.mHwNetworkSliceSettingsObserver.getApnObjects()) {
            if (apnType != 0 && hasApnType(apnObject.getApnTypesBitmask(), apnType)) {
                log("match dnn for apnType=" + apnType + ", apnNmae = " + apnObject.getApnName());
                return apnObject.getApnName();
            }
        }
        return "";
    }

    private int getCct(int apnType) {
        if (apnType == 2) {
            return 2;
        }
        if (apnType == 4) {
            return 4;
        }
        if (apnType != 64) {
            return -1;
        }
        return 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onWifiNetworkStateChanged(Intent intent) {
        if (intent == null) {
            logw("intent from wifi broadcast is null");
            return;
        }
        boolean isWifiConnected = false;
        boolean isChanged = false;
        NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        if (netInfo != null) {
            if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                isWifiConnected = false;
            }
            if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
                isWifiConnected = true;
            }
            if (this.mIsWifiConnect != isWifiConnected) {
                logd("onWifiNetworkStateChanged wifi is from " + this.mIsWifiConnect + " to " + isWifiConnected);
                isChanged = true;
                this.mIsWifiConnect = isWifiConnected;
            }
            if (isChanged) {
                if (this.mIsWifiConnect) {
                    unbindAllProccessToNetwork();
                } else {
                    restoreSliceEnvironment();
                    bindAllProccessToNetwork();
                }
            }
        }
        log("onWifiNetworkStateChanged mIsWifiConnect = " + this.mIsWifiConnect);
    }

    public void unbindAllProccessToNetwork() {
        SliceRouteInfo sri;
        for (NetworkSliceInfo nsi : this.mNetworkSliceInfos) {
            if (!nsi.isMatchAll() && nsi.getNetId() != -1) {
                for (TrafficDescriptors tds : nsi.getSliceRouteInfos().keySet()) {
                    if (!tds.isMatchNetworkCap() && (sri = nsi.getSliceRouteInfo(tds)) != null) {
                        this.mBoosterProxy.unbindUids(nsi.getNetId(), sri.getUidsStr(), tds.getUrspPrecedence());
                    }
                }
            }
        }
    }

    public void bindAllProccessToNetwork() {
        for (NetworkSliceInfo nsi : this.mNetworkSliceInfos) {
            if (!nsi.isMatchAll()) {
                nsi.clearUsedUids();
                NetworkSliceCallback nsc = nsi.getNetworkCallback();
                for (TrafficDescriptors tds : nsi.getSliceRouteInfos().keySet()) {
                    if (!tds.isMatchNetworkCap()) {
                        if (!(nsc == null || nsi.getNetId() == -1)) {
                            bindNetworkSliceProcessToNetwork(-1, nsc.getRequestUids(), nsi, nsi.getFqdnIps(tds), tds);
                        }
                        log("bindAllProccessToNetwork, bind uid to network slice result = -3 nsi = " + nsi);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkSliceLost(Intent intent) {
        if (intent != null) {
            byte sscMode = intent.getByteExtra(NETWORK_ON_LOST_SSCMODE, (byte) 0);
            int pduSessionType = intent.getIntExtra(NETWORK_ON_LOST_PDU_SESSION_TYPE, -1);
            String snssai = "";
            String dnn = intent.getStringExtra(NETWORK_ON_LOST_DNN) != null ? intent.getStringExtra(NETWORK_ON_LOST_DNN) : snssai;
            if (intent.getStringExtra(NETWORK_ON_LOST_SNSSAI) != null) {
                snssai = intent.getStringExtra(NETWORK_ON_LOST_SNSSAI);
            }
            RouteSelectionDescriptor rsd = new RouteSelectionDescriptor.Builder().setDnn(dnn).setPduSessionType(pduSessionType).setSnssai(snssai).setSscMode(sscMode).build();
            log("ACTION_NETWORK_SLICE_LOST : " + rsd);
            unregisterNetworkCallbackRemoteInitiated(rsd);
        }
    }

    private void unbindProcessToNetworkForSingleUid(int uid, NetworkSliceInfo nsi, boolean isNeedToRemoveUid) {
        if (nsi != null && nsi.getNetId() != -1) {
            Iterator<Map.Entry<TrafficDescriptors, SliceRouteInfo>> entries = nsi.getSliceRouteInfos().entrySet().iterator();
            while (entries.hasNext()) {
                TrafficDescriptors tds = entries.next().getKey();
                if (tds != null) {
                    nsi.getUsedUids(tds);
                    if (nsi.isUidRouteBindType(tds) && nsi.isInUsedUids(uid, tds)) {
                        if (isNeedToRemoveUid) {
                            this.mBoosterProxy.unbindUids(nsi.getNetId(), String.valueOf(uid), tds.getUrspPrecedence());
                            log("unbindProcessToNetworkForSingleUid usedUids = " + nsi.getUsedUids(tds) + " uids = " + nsi.getUids(tds) + " uid = " + uid);
                            nsi.removeUid(uid, tds);
                        }
                        nsi.removeUsedUid(uid, tds);
                        nsi.removeSignedUid(uid, tds);
                        nsi.getNetworkCallback().removeRequestUid(uid);
                        if (nsi.isUsedUidEmpty(tds)) {
                            entries.remove();
                        }
                    }
                }
            }
            if (nsi.getSliceRouteInfos().isEmpty()) {
                releaseNetworkSlice(nsi);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMakeDefaultPhoneDone() {
        if (!this.mIsFirstPhoneMakeDone) {
            cleanEnvironment();
        } else {
            this.mIsFirstPhoneMakeDone = false;
        }
    }

    /* access modifiers changed from: private */
    public static class SingletonInstance {
        private static final HwNetworkSliceManager INSTANCE = new HwNetworkSliceManager();

        private SingletonInstance() {
        }
    }
}
