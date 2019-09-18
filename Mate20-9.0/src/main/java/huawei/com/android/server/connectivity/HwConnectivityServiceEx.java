package huawei.com.android.server.connectivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.NetworkSpecifier;
import android.net.RouteInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.server.connectivity.IHwConnectivityServiceInner;
import com.android.server.connectivity.NetworkAgentInfo;
import com.huawei.server.connectivity.IHwConnectivityServiceEx;
import java.util.HashMap;
import java.util.Iterator;

public class HwConnectivityServiceEx implements IHwConnectivityServiceEx {
    private static final boolean DBG = true;
    private static final String TAG = HwConnectivityServiceEx.class.getSimpleName();
    private Context context;
    private IHwConnectivityServiceInner csi;

    public HwConnectivityServiceEx(IHwConnectivityServiceInner csi2, Context context2) {
        this.csi = csi2;
        this.context = context2;
    }

    private static void log(String s) {
        Slog.d(TAG, s);
    }

    private static void loge(String s) {
        Slog.e(TAG, s);
    }

    public void maybeHandleNetworkAgentMessageEx(Message msg, NetworkAgentInfo nai) {
        HashMap<Messenger, NetworkAgentInfo> mNetworkAgentInfos = this.csi.getNetworkAgentInfos();
        int i = msg.what;
        switch (i) {
            case 528486:
                log("CMD_UPDATE_WIFI_AP_TYPE :" + msg.arg1);
                nai.networkMisc.wifiApType = msg.arg1;
                return;
            case 528487:
                nai.networkMisc.connectToCellularAndWLAN = msg.arg1;
                nai.networkMisc.acceptUnvalidated = ((Boolean) msg.obj).booleanValue();
                log("update acceptUnvalidated :" + nai.networkMisc.acceptUnvalidated + ", connectToCellularAndWLAN:" + nai.networkMisc.connectToCellularAndWLAN);
                return;
            default:
                switch (i) {
                    case 528585:
                        setExplicitlyUnselected(mNetworkAgentInfos.get(msg.replyTo));
                        return;
                    case 528586:
                        updateNetworkConcurrently(mNetworkAgentInfos.get(msg.replyTo), (NetworkInfo) msg.obj);
                        return;
                    case 528587:
                        triggerRoamingNetworkMonitor(mNetworkAgentInfos.get(msg.replyTo));
                        return;
                    case 528588:
                        triggerInvalidlinkNetworkMonitor(mNetworkAgentInfos.get(msg.replyTo));
                        return;
                    default:
                        return;
                }
        }
    }

    public void removeLegacyRouteToHost(int netId, RouteInfo bestRoute, int uid) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("network_management");
        log("removeLegacyRouteToHost");
        if (b != null) {
            try {
                _data.writeInterfaceToken(this.csi.getDescriptor());
                _data.writeInt(netId);
                bestRoute.writeToParcel(_data, 0);
                _data.writeInt(uid);
                b.transact(this.csi.getCodeRemoveLegacyrouteToHost(), _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException e) {
                loge("Exception trying to remove a route : RemoteException");
            } catch (Exception e2) {
                loge("Exception trying to remove a route: ");
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public NetworkAgentInfo getIdenticalActiveNetworkAgentInfo(NetworkAgentInfo na) {
        if (HuaweiTelephonyConfigs.isHisiPlatform() || na == null || na.networkInfo.getState() != NetworkInfo.State.CONNECTED) {
            return null;
        }
        NetworkAgentInfo bestNetwork = null;
        Iterator it = this.csi.getNetworkAgentInfos().values().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            NetworkAgentInfo network = (NetworkAgentInfo) it.next();
            log("checking existed " + network.name());
            if (network == this.csi.getHwNetworkForType(network.networkInfo.getType())) {
                LinkProperties curNetworkLp = network.linkProperties;
                LinkProperties newNetworkLp = na.linkProperties;
                if (network.networkInfo.getState() == NetworkInfo.State.CONNECTED && curNetworkLp != null && !TextUtils.isEmpty(curNetworkLp.getInterfaceName())) {
                    boolean isLpIdentical = curNetworkLp.keyEquals(newNetworkLp);
                    log("LinkProperties Identical are " + isLpIdentical);
                    NetworkSpecifier ns = network.networkCapabilities.getNetworkSpecifier();
                    NetworkSpecifier ns2 = na.networkCapabilities.getNetworkSpecifier();
                    if (ns != null && ns.satisfiedBy(ns2) && isLpIdentical) {
                        log("apparently satisfied");
                        bestNetwork = network;
                        break;
                    }
                } else {
                    log("some key parameter is null, ignore");
                }
            } else {
                log("not recorded, ignore");
            }
        }
        return bestNetwork;
    }

    public void setupUniqueDeviceName() {
        String hostname = SystemProperties.get("net.hostname");
        if (TextUtils.isEmpty(hostname) || hostname.length() < 8) {
            String id = Settings.Secure.getString(this.context.getContentResolver(), "android_id");
            if (id != null && id.length() > 0) {
                if (TextUtils.isEmpty(hostname)) {
                    String hostname2 = SystemProperties.get("ro.config.marketing_name", "");
                    if (TextUtils.isEmpty(hostname2)) {
                        hostname = Build.MODEL.replace(" ", "_");
                        if (hostname != null && hostname.length() > 18) {
                            hostname = hostname.substring(0, 18);
                        }
                    } else {
                        hostname = hostname2.replace(" ", "_");
                    }
                }
                String hostname3 = hostname + "-" + id;
                if (hostname3 != null && hostname3.length() > 25) {
                    hostname3 = hostname3.substring(0, 25);
                }
                SystemProperties.set("net.hostname", hostname3);
            }
        }
    }

    private void setExplicitlyUnselected(NetworkAgentInfo nai) {
        if (nai != null) {
            nai.networkMisc.explicitlySelected = false;
            nai.networkMisc.acceptUnvalidated = false;
            if (nai.networkInfo != null && ConnectivityManager.getNetworkTypeName(1).equals(nai.networkInfo.getTypeName())) {
                log("setExplicitlyUnselected, WiFi+ switch from WiFi to Cellular, enableDefaultTypeAPN explicitly.");
                enableDefaultTypeAPN(true);
            }
        }
    }

    private void updateNetworkConcurrently(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
        NetworkInfo oldInfo;
        String str;
        NetworkInfo.State state = newInfo.getState();
        INetworkManagementService netd = this.csi.getNetd();
        synchronized (networkAgent) {
            oldInfo = networkAgent.networkInfo;
            networkAgent.networkInfo = newInfo;
        }
        if (oldInfo != null && oldInfo.getState() == state) {
            log("updateNetworkConcurrently, ignoring duplicate network state non-change");
        } else if (netd == null) {
            loge("updateNetworkConcurrently, invalid member, netd = null");
        } else {
            networkAgent.setCurrentScore(0);
            try {
                int i = networkAgent.network.netId;
                if (networkAgent.networkCapabilities.hasCapability(13)) {
                    str = null;
                } else {
                    str = "SYSTEM";
                }
                netd.createPhysicalNetwork(i, str);
                networkAgent.created = true;
                this.csi.hwUpdateLinkProperties(networkAgent, null);
                log("updateNetworkConcurrently, nai.networkInfo = " + networkAgent.networkInfo);
                networkAgent.asyncChannel.sendMessage(528391, 4, 0, null);
            } catch (Exception e) {
                loge("updateNetworkConcurrently, Error creating network " + networkAgent.network.netId + ": " + e.getMessage());
            }
        }
    }

    private void triggerRoamingNetworkMonitor(NetworkAgentInfo networkAgent) {
        if (networkAgent != null && networkAgent.networkMonitor != null) {
            log("triggerRoamingNetworkMonitor, nai.networkInfo = " + networkAgent.networkInfo);
            networkAgent.networkMonitor.sendMessage(532581);
        }
    }

    private void triggerInvalidlinkNetworkMonitor(NetworkAgentInfo networkAgent) {
        if (networkAgent != null && networkAgent.networkMonitor != null) {
            log("triggerInvalidlinkNetworkMonitor, nai.networkInfo = " + networkAgent.networkInfo);
            networkAgent.networkMonitor.sendMessage(532582);
        }
    }

    private void enableDefaultTypeAPN(boolean enabled) {
        String str = TAG;
        Log.d(str, "enableDefaultTypeAPN= " + enabled);
        String defaultMobileEnable = SystemProperties.get("sys.defaultapn.enabled", "true");
        String str2 = TAG;
        Log.d(str2, "DEFAULT_MOBILE_ENABLE before state is " + defaultMobileEnable);
        SystemProperties.set("sys.defaultapn.enabled", enabled ? "true" : "false");
        HwTelephonyManagerInner hwTm = HwTelephonyManagerInner.getDefault();
        if (hwTm != null) {
            hwTm.setDefaultMobileEnable(enabled);
        }
    }
}
