package com.android.server.intellicom.networkslice.css;

import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.server.intellicom.common.HwAppStateObserver;
import com.android.server.intellicom.networkslice.HwNetworkSliceManager;
import com.huawei.android.os.AsyncResultEx;

public class NetworkSlicesHandler extends Handler {
    private static final int APP_APPEARS_FOREGROUND = 2001;
    private static final int APP_GONE = 2002;
    private static final int APP_REMOVED = 2003;
    private static final int APP_STATE_BASE = 2000;
    private static final String CALLBACK_IP = "ip";
    private static final String CALLBACK_PROTOCOLID = "protocolId";
    private static final String CALLBACK_REMOTE_PORT = "remotePort";
    private static final String CALLBACK_UID = "uid";
    public static final int FQDN_REPORT = 1;
    private static final String HOSTNAME = "hostName";
    private static final String IPADDRESSES = "ipList";
    private static final String IPADDRESSES_COUNT = "ipCount";
    public static final int IP_REPORT = 2;
    public static final String MSG_NETID = "netId";
    public static final String MSG_NETWORK_REQUEST = "networkRequest";
    public static final String MSG_UID = "uid";
    private static final int NETWORKSLICE_CALLBACK_BASE = 1000;
    private static final int NETWORKSLICE_REPORT_DATA_BASE = 0;
    public static final int ON_NETWORK_AVAILABLE = 1001;
    public static final int ON_NETWORK_LOST = 1002;
    public static final int ON_NETWORK_UNAVAILABLE = 1003;
    private static final String TAG_LOG = "NetworkSlicesHandler";
    private static final String UID = "uid";
    public static final int URSP_REPORT = 3;

    public NetworkSlicesHandler(Looper looper) {
        super(looper);
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        if (msg == null) {
            log("msg is null, return");
            return;
        }
        log("recieve msg = " + msg.what);
        Bundle data = msg.getData();
        int i = msg.what;
        if (i == 1) {
            handleFqdnReport(data);
        } else if (i == 2) {
            handleIpReport(data);
        } else if (i != 3) {
            switch (i) {
                case 1001:
                    handleOnNetworkAvailable(data);
                    return;
                case 1002:
                    handleOnNetworkLost(data);
                    return;
                case 1003:
                    handleOnUnAvailable(data);
                    return;
                default:
                    switch (i) {
                        case 2001:
                            handleAppAppearsForeground(msg);
                            return;
                        case 2002:
                            handleAppGone(msg);
                            return;
                        case 2003:
                            handleAppRemoved(msg);
                            return;
                        default:
                            log("handleMessage : " + msg.what);
                            return;
                    }
            }
        } else {
            HwNetworkSliceManager.getInstance().handleUrspChanged(data);
        }
    }

    public void registerForAppStateObserver() {
        HwAppStateObserver.getInstance().registerForAppAppearsForeground(this, 2001, null);
        HwAppStateObserver.getInstance().registerForAppRemoved(this, 2003, null);
        HwAppStateObserver.getInstance().registerForAppGone(this, 2002, null);
    }

    private void handleAppAppearsForeground(Message msg) {
        if (msg != null && msg.obj != null) {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar.getException() == null) {
                HwNetworkSliceManager.getInstance().requestNetworkSliceForPackageName(((Integer) ar.getResult()).intValue());
            }
        }
    }

    private void handleAppGone(Message msg) {
        if (msg != null && msg.obj != null) {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar.getException() == null) {
                HwNetworkSliceManager.getInstance().handleUidGone(((Integer) ar.getResult()).intValue());
            }
        }
    }

    private void handleAppRemoved(Message msg) {
        log("Receive ACTION_PACKAGE_REMOVED");
        if (msg != null && msg.obj != null) {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar.getException() == null) {
                HwNetworkSliceManager.getInstance().handleUidRemoved((String) ar.getResult());
            }
        }
    }

    private void handleFqdnReport(Bundle data) {
        if (data == null) {
            log("FQDN_REPORT data is null, return");
            return;
        }
        String hostname = data.getString("hostName");
        HwNetworkSliceManager.getInstance().requestNetworkSliceForFqdn(data.getInt("uid"), hostname, data.getStringArrayList("ipList"), data.getInt("ipCount"));
    }

    private void handleIpReport(Bundle data) {
        if (data == null) {
            log("IP_REPORT data is null, return");
            return;
        }
        HwNetworkSliceManager.getInstance().requestNetworkSliceForIp(data.getInt("uid"), data.getByteArray(CALLBACK_IP), data.getString(CALLBACK_PROTOCOLID), data.getString(CALLBACK_REMOTE_PORT));
    }

    private void handleOnNetworkAvailable(Bundle data) {
        if (data == null) {
            log("ON_NETWORK_AVAILABLE data is null, return");
            return;
        }
        int uid = data.getInt("uid");
        int netId = data.getInt(MSG_NETID);
        NetworkRequest request = (NetworkRequest) data.getParcelable(MSG_NETWORK_REQUEST);
        log("ON_NETWORK_AVAILABLE, uid = " + uid + " ,netId = " + netId + " ,request = " + request);
        HwNetworkSliceManager.getInstance().onNetworkAvailable(uid, netId, request);
    }

    private void handleOnNetworkLost(Bundle data) {
        if (data == null) {
            log("ON_NETWORK_LOST data is null, return");
            return;
        }
        NetworkRequest request = (NetworkRequest) data.getParcelable(MSG_NETWORK_REQUEST);
        HwNetworkSliceManager.getInstance().onNetworkLost(request);
        log("ON_NETWORK_LOST, request = " + request);
    }

    private void handleOnUnAvailable(Bundle data) {
        if (data == null) {
            log("ON_NETWORK_UNAVAILABLE data is null, return");
            return;
        }
        int uid = data.getInt("uid");
        NetworkRequest request = (NetworkRequest) data.getParcelable(MSG_NETWORK_REQUEST);
        HwNetworkSliceManager.getInstance().onNetworkUnAvailable(uid, request);
        log("ON_NETWORK_UNAVAILABLE, uid = " + uid + " ,request = " + request);
    }

    private void log(String msg) {
        Log.i(TAG_LOG, msg);
    }
}
