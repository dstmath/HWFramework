package com.android.internal.telephony.dataconnection;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.telephony.AccessNetworkConstants;
import android.telephony.Rlog;
import android.telephony.data.ApnSetting;
import android.util.LocalLog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.dataconnection.AccessNetworksManager;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TransportManager extends Handler {
    private static final Map<Integer, Integer> ACCESS_NETWORK_TRANSPORT_TYPE_MAP = new HashMap();
    private static final int EVENT_QUALIFIED_NETWORKS_CHANGED = 1;
    private static final int EVENT_UPDATE_AVAILABLE_NETWORKS = 2;
    public static final String IWLAN_OPERATION_MODE_AP_ASSISTED = "AP-assisted";
    public static final String IWLAN_OPERATION_MODE_DEFAULT = "default";
    public static final String IWLAN_OPERATION_MODE_LEGACY = "legacy";
    public static final String SYSTEM_PROPERTIES_IWLAN_OPERATION_MODE = "ro.telephony.iwlan_operation_mode";
    private static final String TAG = TransportManager.class.getSimpleName();
    private AccessNetworksManager mAccessNetworksManager;
    private final LinkedList<List<AccessNetworksManager.QualifiedNetworks>> mAvailableNetworksList;
    private final int[] mAvailableTransports;
    private final SparseArray<int[]> mCurrentAvailableNetworks;
    private final Map<Integer, Integer> mCurrentTransports;
    private final RegistrantList mHandoverNeededEventRegistrants;
    private final LocalLog mLocalLog = new LocalLog(10);
    private final SparseIntArray mPendingHandoverApns;
    private final Phone mPhone;

    @Retention(RetentionPolicy.SOURCE)
    public @interface IwlanOperationMode {
    }

    static {
        ACCESS_NETWORK_TRANSPORT_TYPE_MAP.put(1, 1);
        ACCESS_NETWORK_TRANSPORT_TYPE_MAP.put(2, 1);
        ACCESS_NETWORK_TRANSPORT_TYPE_MAP.put(3, 1);
        ACCESS_NETWORK_TRANSPORT_TYPE_MAP.put(4, 1);
        ACCESS_NETWORK_TRANSPORT_TYPE_MAP.put(5, 2);
    }

    @VisibleForTesting
    public static final class HandoverParams {
        public final int apnType;
        public final HandoverCallback callback;
        public final int targetTransport;

        public interface HandoverCallback {
            void onCompleted(boolean z);
        }

        HandoverParams(int apnType2, int targetTransport2, HandoverCallback callback2) {
            this.apnType = apnType2;
            this.targetTransport = targetTransport2;
            this.callback = callback2;
        }
    }

    public TransportManager(Phone phone) {
        this.mPhone = phone;
        this.mCurrentAvailableNetworks = new SparseArray<>();
        this.mCurrentTransports = new ConcurrentHashMap();
        this.mPendingHandoverApns = new SparseIntArray();
        this.mHandoverNeededEventRegistrants = new RegistrantList();
        this.mAvailableNetworksList = new LinkedList<>();
        if (isInLegacyMode()) {
            log("operates in legacy mode.");
            this.mAvailableTransports = new int[]{1};
            return;
        }
        log("operates in AP-assisted mode.");
        this.mAccessNetworksManager = new AccessNetworksManager(phone);
        this.mAccessNetworksManager.registerForQualifiedNetworksChanged(this, 1);
        this.mAvailableTransports = new int[]{1, 2};
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 1) {
            this.mAvailableNetworksList.add((List) ((AsyncResult) msg.obj).result);
            sendEmptyMessage(2);
        } else if (i != 2) {
            loge("Unexpected event " + msg.what);
        } else {
            updateAvailableNetworks();
        }
    }

    private boolean isHandoverNeeded(AccessNetworksManager.QualifiedNetworks newNetworks) {
        int apnType = newNetworks.apnType;
        int[] newNetworkList = newNetworks.qualifiedNetworks;
        int[] currentNetworkList = this.mCurrentAvailableNetworks.get(apnType);
        if (ArrayUtils.isEmpty(currentNetworkList) && ACCESS_NETWORK_TRANSPORT_TYPE_MAP.get(Integer.valueOf(newNetworkList[0])).intValue() == 2) {
            return true;
        }
        if (ArrayUtils.isEmpty(newNetworkList) || ArrayUtils.isEmpty(currentNetworkList)) {
            return false;
        }
        if (this.mPendingHandoverApns.get(newNetworks.apnType) != ACCESS_NETWORK_TRANSPORT_TYPE_MAP.get(Integer.valueOf(newNetworkList[0])).intValue()) {
            return !ACCESS_NETWORK_TRANSPORT_TYPE_MAP.get(Integer.valueOf(newNetworkList[0])).equals(Integer.valueOf(getCurrentTransport(newNetworks.apnType)));
        }
        log("Handover not needed. There is already an ongoing handover.");
        return false;
    }

    private static boolean areNetworksValid(AccessNetworksManager.QualifiedNetworks networks) {
        if (networks.qualifiedNetworks == null) {
            return false;
        }
        for (int network : networks.qualifiedNetworks) {
            if (!ACCESS_NETWORK_TRANSPORT_TYPE_MAP.containsKey(Integer.valueOf(network))) {
                return false;
            }
        }
        return true;
    }

    private synchronized void setCurrentTransport(int apnType, int transport) {
        this.mCurrentTransports.put(Integer.valueOf(apnType), Integer.valueOf(transport));
        logl("setCurrentTransport: apnType=" + ApnSetting.getApnTypeString(apnType) + ", transport=" + AccessNetworkConstants.transportTypeToString(transport));
    }

    private boolean isHandoverPending() {
        return this.mPendingHandoverApns.size() > 0;
    }

    private void updateAvailableNetworks() {
        if (isHandoverPending()) {
            log("There's ongoing handover. Will update networks once handover completed.");
        } else if (this.mAvailableNetworksList.size() == 0) {
            log("Nothing in the available network list queue.");
        } else {
            List<AccessNetworksManager.QualifiedNetworks> networksList = this.mAvailableNetworksList.remove();
            logl("updateAvailableNetworks: " + networksList);
            for (AccessNetworksManager.QualifiedNetworks networks : networksList) {
                if (areNetworksValid(networks)) {
                    if (isHandoverNeeded(networks)) {
                        int targetTransport = ACCESS_NETWORK_TRANSPORT_TYPE_MAP.get(Integer.valueOf(networks.qualifiedNetworks[0])).intValue();
                        logl("Handover needed for APN type: " + ApnSetting.getApnTypeString(networks.apnType) + ", target transport: " + AccessNetworkConstants.transportTypeToString(targetTransport));
                        this.mPendingHandoverApns.put(networks.apnType, targetTransport);
                        this.mHandoverNeededEventRegistrants.notifyResult(new HandoverParams(networks.apnType, targetTransport, new HandoverParams.HandoverCallback(networks, targetTransport) {
                            /* class com.android.internal.telephony.dataconnection.$$Lambda$TransportManager$Dk_40vYVbwoK0GbaXsHT3AecbY */
                            private final /* synthetic */ AccessNetworksManager.QualifiedNetworks f$1;
                            private final /* synthetic */ int f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            @Override // com.android.internal.telephony.dataconnection.TransportManager.HandoverParams.HandoverCallback
                            public final void onCompleted(boolean z) {
                                TransportManager.this.lambda$updateAvailableNetworks$0$TransportManager(this.f$1, this.f$2, z);
                            }
                        }));
                    }
                    this.mCurrentAvailableNetworks.put(networks.apnType, networks.qualifiedNetworks);
                } else {
                    loge("Invalid networks received: " + networks);
                }
            }
            if (this.mAvailableNetworksList.size() > 0) {
                sendEmptyMessage(2);
            }
        }
    }

    public /* synthetic */ void lambda$updateAvailableNetworks$0$TransportManager(AccessNetworksManager.QualifiedNetworks networks, int targetTransport, boolean success) {
        if (success) {
            logl("Handover succeeded.");
        } else {
            logl("APN type " + ApnSetting.getApnTypeString(networks.apnType) + " handover to " + AccessNetworkConstants.transportTypeToString(targetTransport) + " failed.");
        }
        setCurrentTransport(networks.apnType, targetTransport);
        this.mPendingHandoverApns.delete(networks.apnType);
        if (this.mAvailableNetworksList.size() > 0) {
            sendEmptyMessage(2);
        }
    }

    public synchronized int[] getAvailableTransports() {
        return this.mAvailableTransports;
    }

    public boolean isInLegacyMode() {
        return IWLAN_OPERATION_MODE_LEGACY.equals(SystemProperties.get(SYSTEM_PROPERTIES_IWLAN_OPERATION_MODE)) || this.mPhone.getHalVersion().less(RIL.RADIO_HAL_VERSION_1_4);
    }

    public int getCurrentTransport(int apnType) {
        if (!isInLegacyMode() && this.mCurrentTransports.get(Integer.valueOf(apnType)) != null) {
            return this.mCurrentTransports.get(Integer.valueOf(apnType)).intValue();
        }
        return 1;
    }

    public boolean isAnyApnPreferredOnIwlan() {
        for (int i = 0; i < this.mCurrentAvailableNetworks.size(); i++) {
            int[] networkList = this.mCurrentAvailableNetworks.valueAt(i);
            if (networkList.length > 0 && networkList[0] == 5) {
                return true;
            }
        }
        return false;
    }

    public void registerForHandoverNeededEvent(Handler h, int what) {
        if (h != null) {
            this.mHandoverNeededEventRegistrants.addUnique(h, what, (Object) null);
        }
    }

    public void unregisterForHandoverNeededEvent(Handler h) {
        this.mHandoverNeededEventRegistrants.remove(h);
    }

    public void dump(FileDescriptor fd, PrintWriter printwriter, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(printwriter, "  ");
        pw.println("TransportManager:");
        pw.increaseIndent();
        pw.println("mAvailableTransports=[" + ((String) Arrays.stream(this.mAvailableTransports).mapToObj($$Lambda$TransportManager$vVwfnOC5CydwmAdimpil6w6F3zk.INSTANCE).collect(Collectors.joining(","))) + "]");
        StringBuilder sb = new StringBuilder();
        sb.append("mCurrentAvailableNetworks=");
        sb.append(this.mCurrentAvailableNetworks);
        pw.println(sb.toString());
        pw.println("mAvailableNetworksList=" + this.mAvailableNetworksList);
        pw.println("mPendingHandoverApns=" + this.mPendingHandoverApns);
        pw.println("mCurrentTransports=" + this.mCurrentTransports);
        pw.println("isInLegacy=" + isInLegacyMode());
        pw.println("IWLAN operation mode=" + SystemProperties.get(SYSTEM_PROPERTIES_IWLAN_OPERATION_MODE));
        AccessNetworksManager accessNetworksManager = this.mAccessNetworksManager;
        if (accessNetworksManager != null) {
            accessNetworksManager.dump(fd, pw, args);
        }
        pw.println("Local logs=");
        pw.increaseIndent();
        this.mLocalLog.dump(fd, pw, args);
        pw.decreaseIndent();
        pw.decreaseIndent();
        pw.flush();
    }

    private void logl(String s) {
        log(s);
        this.mLocalLog.log(s);
    }

    private void log(String s) {
        Rlog.i(TAG, s);
    }

    private void loge(String s) {
        Rlog.e(TAG, s);
    }
}
