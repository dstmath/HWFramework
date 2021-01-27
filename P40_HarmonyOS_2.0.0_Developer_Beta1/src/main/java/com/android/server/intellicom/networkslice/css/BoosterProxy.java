package com.android.server.intellicom.networkslice.css;

import android.content.Context;
import android.net.booster.IHwCommBoosterCallback;
import android.net.booster.IHwCommBoosterServiceManager;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.intellicom.networkslice.HwNetworkSliceManager;
import com.android.server.intellicom.networkslice.model.FqdnIps;
import com.android.server.intellicom.networkslice.model.TrafficDescriptors;
import com.huawei.internal.telephony.HwTelephonyBoosterUtils;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BoosterProxy {
    private static final int BIND_PROCESS_TO_NETWORK_SLICE = 803;
    private static final String BIND_ROUTE_IPV4_ADDRANDMASK = "ipv4AddrAndMask";
    private static final String BIND_ROUTE_IPV4_NUM = "ipv4Num";
    private static final String BIND_ROUTE_IPV6_ADDRANDPREFIX = "ipv6AddrAndPrefix";
    private static final String BIND_ROUTE_IPV6_NUM = "ipv6Num";
    private static final String BIND_ROUTE_NETID = "netId";
    private static final String BIND_ROUTE_PROTOCOL_IDS = "protocolIds";
    private static final String BIND_ROUTE_REMOTE_PORTS = "remotePorts";
    private static final String BIND_ROUTE_UID = "uids";
    private static final String BIND_ROUTE_URSP_PRECEDENCE = "urspPrecedence";
    private static final String CALLBACK_IP = "ip";
    private static final int CALLBACK_IP_REPORT = 14;
    private static final String CALLBACK_PROTOCOLID = "protocolId";
    private static final String CALLBACK_REMOTE_PORT = "remotePort";
    private static final String CALLBACK_UID = "uid";
    private static final int CALLBACK_URSP_CHANGED = 13;
    public static final int ERROR_INVALID_PARAM = -3;
    public static final int ERROR_NO_SERVICE = -1;
    private static final int GET_NETWORK_SLICE = 802;
    private static final String PACKAGE_NAME = "com.android.server.intellicom.networkslice";
    private static final String REQUEST_NETWORK_SLICE_APPID = "appId";
    private static final String REQUEST_NETWORK_SLICE_CONNECTION_CAPABILITY = "connectionCapability";
    private static final String REQUEST_NETWORK_SLICE_DNN = "dnn";
    private static final String REQUEST_NETWORK_SLICE_FQDN = "fqdn";
    private static final String REQUEST_NETWORK_SLICE_IP = "ip";
    private static final String REQUEST_NETWORK_SLICE_PROTOCOL = "protocolId";
    private static final String REQUEST_NETWORK_SLICE_REMOTE_PORT = "remotePort";
    private static final String SEPARATOR = ",";
    public static final int SUCCESS = 0;
    private static final String TAG = "BoosterProxy";
    private static final int UNBIND_PROCESS_TO_NETWORK_SLICE = 804;
    private static final String UNBIND_ROUTE_TYPE = "type";
    private static final int UNBIND_TYPE_ALL = 0;
    private static final int UNBIND_TYPE_NETID = 1;
    private static final int UNBIND_TYPE_UIDS = 2;
    private IHwCommBoosterServiceManager mBoosterServiceManager;
    private IHwCommBoosterCallback mIHwCommBoosterCallback = new IHwCommBoosterCallback.Stub() {
        /* class com.android.server.intellicom.networkslice.css.BoosterProxy.AnonymousClass1 */

        public void callBack(int type, Bundle b) throws RemoteException {
            BoosterProxy boosterProxy = BoosterProxy.this;
            boosterProxy.log("receive booster callback type " + type);
            if (b == null) {
                BoosterProxy.this.log("data is null");
                return;
            }
            Message msg = null;
            NetworkSlicesHandler handler = HwNetworkSliceManager.getInstance().getHandler();
            if (handler == null) {
                BoosterProxy.this.log("handler is null");
                return;
            }
            if (type == 13) {
                BoosterProxy.this.log("TrafficDescriptors CHANGED");
                msg = handler.obtainMessage(3);
            } else if (type != 14) {
                Log.w(BoosterProxy.TAG, "Wrong type = " + type);
            } else {
                msg = handler.obtainMessage(2);
            }
            if (msg != null) {
                msg.setData(b);
                msg.sendToTarget();
            }
        }
    };

    public static BoosterProxy getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public Bundle getNetworkSlice(TrafficDescriptors td, Context context) {
        return getBoosterPara(802, fillNetworkSliceRequest(td, context));
    }

    public static void fillBindParas(Set<Integer> uids, Bundle bindParas) {
        if (!Stream.of(uids, bindParas).anyMatch($$Lambda$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            bindParas.putString(BIND_ROUTE_UID, (String) uids.stream().map($$Lambda$BoosterProxy$NDS5Za3EF29TgoKf_MqCovVYqvI.INSTANCE).collect(Collectors.joining(",")));
        }
    }

    public static void fillBindParas(int netId, byte urspPrecedence, Bundle bindParas) {
        bindParas.putInt("netId", netId);
        bindParas.putByte(BIND_ROUTE_URSP_PRECEDENCE, urspPrecedence);
    }

    public int unbindAllRoute() {
        Bundle input = new Bundle();
        input.putInt("netId", 0);
        input.putInt("type", 0);
        return reportBoosterPara(UNBIND_PROCESS_TO_NETWORK_SLICE, input);
    }

    public int unbindSingleNetId(int netId) {
        Bundle input = new Bundle();
        input.putInt("netId", netId);
        input.putInt("type", 1);
        return reportBoosterPara(UNBIND_PROCESS_TO_NETWORK_SLICE, input);
    }

    public int unbindUids(int netId, String uids, byte urspPrecedence) {
        Bundle input = new Bundle();
        input.putInt("netId", netId);
        input.putString(BIND_ROUTE_UID, uids);
        input.putString(BIND_ROUTE_URSP_PRECEDENCE, String.valueOf((int) urspPrecedence));
        input.putInt("type", 2);
        return reportBoosterPara(UNBIND_PROCESS_TO_NETWORK_SLICE, input);
    }

    public void registerBoosterCallback() {
        log("registerBoosterCallback enter");
        if (getBoosterServiceManager() != null) {
            int ret = this.mBoosterServiceManager.registerCallBack(PACKAGE_NAME, this.mIHwCommBoosterCallback);
            if (ret != 0) {
                log("registerBoosterCallback:registerCallBack failed, ret=" + ret);
                return;
            }
            return;
        }
        log("registerBoosterCallback:null HwCommBoosterServiceManager");
    }

    public int unbindProcessToNetwork(String uid, int netId) {
        Bundle input = new Bundle();
        input.putInt("netId", netId);
        input.putString(BIND_ROUTE_UID, uid);
        return reportBoosterPara(UNBIND_PROCESS_TO_NETWORK_SLICE, input);
    }

    public int bindProcessToNetwork(Bundle data) {
        return reportBoosterPara(BIND_PROCESS_TO_NETWORK_SLICE, data);
    }

    public static void fillIpBindParasForFqdn(Bundle bindParas, FqdnIps fqdnIps) {
        bindParas.putByte(BIND_ROUTE_IPV4_NUM, fqdnIps.getIpv4Num());
        bindParas.putByteArray(BIND_ROUTE_IPV4_ADDRANDMASK, fqdnIps.getIpv4AddrAndMask());
        bindParas.putByte(BIND_ROUTE_IPV6_NUM, fqdnIps.getIpv6Num());
        bindParas.putByteArray(BIND_ROUTE_IPV6_ADDRANDPREFIX, fqdnIps.getIpv6AddrAndPrefix());
        bindParas.putString(BIND_ROUTE_PROTOCOL_IDS, "");
        bindParas.putString(BIND_ROUTE_REMOTE_PORTS, "");
    }

    public static void fillIpBindParasForIpTriad(Bundle bindParas, TrafficDescriptors tds) {
        bindParas.putByte(BIND_ROUTE_IPV4_NUM, tds.getIpv4Num());
        bindParas.putByteArray(BIND_ROUTE_IPV4_ADDRANDMASK, tds.getIpv4AddrAndMask());
        bindParas.putByte(BIND_ROUTE_IPV6_NUM, tds.getIpv6Num());
        bindParas.putByteArray(BIND_ROUTE_IPV6_ADDRANDPREFIX, tds.getIpv6AddrAndPrefix());
        bindParas.putString(BIND_ROUTE_PROTOCOL_IDS, tds.getProtocolIds());
        bindParas.putString(BIND_ROUTE_REMOTE_PORTS, tds.getRemotePorts());
    }

    /* access modifiers changed from: private */
    public static class SingletonInstance {
        private static final BoosterProxy INSTANCE = new BoosterProxy();

        private SingletonInstance() {
        }
    }

    private Bundle getBoosterPara(int dataType, Bundle data) {
        if (getBoosterServiceManager() != null) {
            return this.mBoosterServiceManager.getBoosterPara(PACKAGE_NAME, dataType, data);
        }
        return null;
    }

    private IHwCommBoosterServiceManager getBoosterServiceManager() {
        if (this.mBoosterServiceManager == null) {
            this.mBoosterServiceManager = HwTelephonyBoosterUtils.getHwCommBoosterServiceManager();
            if (this.mBoosterServiceManager == null) {
                log("Can not get BoosterServiceManager");
                return null;
            }
        }
        return this.mBoosterServiceManager;
    }

    private int reportBoosterPara(int dataType, Bundle data) {
        if (getBoosterServiceManager() != null) {
            return this.mBoosterServiceManager.reportBoosterPara(PACKAGE_NAME, dataType, data);
        }
        return -1;
    }

    private static Bundle fillNetworkSliceRequest(TrafficDescriptors td, Context context) {
        String appId = HwNetworkSliceManager.OS_ID + context.getPackageManager().getNameForUid(td.getUid());
        byte[] ip = td.getIp() == null ? null : td.getIp().getAddress();
        Bundle bundle = new Bundle();
        bundle.putString(REQUEST_NETWORK_SLICE_APPID, appId);
        bundle.putString(REQUEST_NETWORK_SLICE_DNN, td.getDnn());
        bundle.putString(REQUEST_NETWORK_SLICE_FQDN, td.getFqdn());
        bundle.putByteArray("ip", ip);
        bundle.putString("protocolId", td.getProtocolId());
        bundle.putString("remotePort", td.getRemotePort());
        bundle.putInt(REQUEST_NETWORK_SLICE_CONNECTION_CAPABILITY, td.getCct());
        return bundle;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String msg) {
        Log.i(TAG, msg);
    }
}
