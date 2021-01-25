package com.android.server.intellicom.networkslice.css;

import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.intellicom.networkslice.HwNetworkSliceManager;
import com.android.server.intellicom.networkslice.model.NetworkSliceInfo;
import com.huawei.android.net.ConnectivityManagerEx;
import com.huawei.android.net.LinkPropertiesEx;
import com.huawei.android.net.NetworkCallbackEx;
import com.huawei.android.net.NetworkEx;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class NetworkSliceCallback extends NetworkCallbackEx {
    private static final int ARG1_BLOCKED = 1;
    private static final int ARG1_INVALID = -1;
    private static final int ARG1_NOT_BLOCKED = 0;
    private static final String BUNDLE_ARG1 = "arg1";
    private static final String BUNDLE_NOTIFY_TYPE = "notificationType";
    private static final String BUNDLE_REQUEST_ID = "requestId";
    private static final int INVALID_NOTIFICATION_TYPE = -1;
    private static final int INVALID_REQUEST_ID = -1;
    private static final String TAG = "NetworkSliceCallback";
    private boolean mBlocked;
    private NetworkSlicesHandler mHandler;
    private LinkProperties mLinkProperties;
    private Network mNetwork;
    private NetworkCapabilities mNetworkCapabilities;
    private int mRequestId;
    private Set<Integer> mRequestUids = new HashSet();
    private int mUid;

    public NetworkSliceCallback(int uid, int requestId, NetworkSlicesHandler handler) {
        this.mRequestId = requestId;
        this.mHandler = handler;
        this.mUid = uid;
        this.mRequestUids.add(Integer.valueOf(uid));
    }

    public void onPreCheck(Network network) {
        callCallbackForRequest(524289, network, -1, null, null);
    }

    public void onAvailable(Network network, NetworkCapabilities networkCapabilities, LinkProperties linkProperties, boolean blocked) {
        this.mNetwork = network;
        this.mNetworkCapabilities = networkCapabilities;
        this.mLinkProperties = linkProperties;
        this.mBlocked = blocked;
        onAvailable(network);
        if (!networkCapabilities.hasCapability(21)) {
            onNetworkSuspended(network);
        }
        onCapabilitiesChanged(network, networkCapabilities);
        onLinkPropertiesChanged(network, linkProperties);
        onBlockedStatusChanged(network, blocked);
        callCallbackForRequest(524290, network, blocked ? 1 : 0, networkCapabilities, linkProperties);
    }

    public void onAvailable(int requestId) {
        NetworkCapabilities networkCapabilities;
        if (this.mNetwork != null && (networkCapabilities = this.mNetworkCapabilities) != null) {
            if (!networkCapabilities.hasCapability(21)) {
                onNetworkSuspended(this.mNetwork);
            }
            this.mRequestId = requestId;
            onCapabilitiesChanged(this.mNetwork, this.mNetworkCapabilities);
            onLinkPropertiesChanged(this.mNetwork, this.mLinkProperties);
            onBlockedStatusChanged(this.mNetwork, this.mBlocked);
            boolean z = this.mBlocked;
            Bundle extraData = new Bundle();
            extraData.putInt(BUNDLE_NOTIFY_TYPE, 524290);
            extraData.putInt(BUNDLE_ARG1, z ? 1 : 0);
            extraData.putInt(BUNDLE_REQUEST_ID, requestId);
            callCallbackForRequest(this.mNetwork, this.mNetworkCapabilities, this.mLinkProperties, extraData);
        }
    }

    public void onAvailable(Network network) {
        Message msg = this.mHandler.obtainMessage(1001);
        Bundle bundle = msg.getData();
        bundle.putInt("uid", this.mUid);
        bundle.putInt(NetworkSlicesHandler.MSG_NETID, new NetworkEx(network).getNetId());
        NetworkSliceInfo networkSliceInfo = HwNetworkSliceManager.getInstance().getNetworkSliceInfoByPara(this, NetworkSliceInfo.ParaType.NETWORK_CALLBACK);
        log("onAvailable - networkSliceInfo = " + networkSliceInfo);
        if (networkSliceInfo == null || networkSliceInfo.getNetworkRequest() == null) {
            log("networkSliceInfo is null so return.");
            return;
        }
        bundle.putParcelable(NetworkSlicesHandler.MSG_NETWORK_REQUEST, networkSliceInfo.getNetworkRequest());
        msg.sendToTarget();
    }

    public void onLosing(Network network, int maxMsToLive) {
        callCallbackForRequest(524291, network, maxMsToLive, null, null);
    }

    public void onLost(Network network) {
        log("callback - onLost :" + network);
        onLostForNetworkSlice(network, true);
    }

    public void onLostForNetworkSlice(Network network, boolean isNeedToNotifyNetworkSliceManager) {
        if (isNeedToNotifyNetworkSliceManager) {
            Message msg = this.mHandler.obtainMessage(1002);
            Bundle bundle = msg.getData();
            bundle.putInt(NetworkSlicesHandler.MSG_NETID, new NetworkEx(network).getNetId());
            NetworkSliceInfo networkSliceInfo = HwNetworkSliceManager.getInstance().getNetworkSliceInfoByPara(this, NetworkSliceInfo.ParaType.NETWORK_CALLBACK);
            if (networkSliceInfo == null) {
                log("onLostForNetworkSlice - networkSliceInfo = null");
                return;
            } else {
                bundle.putParcelable(NetworkSlicesHandler.MSG_NETWORK_REQUEST, networkSliceInfo.getNetworkRequest());
                msg.sendToTarget();
            }
        }
        callCallbackForRequest(524292, network, -1, null, null);
    }

    public void onUnavailable() {
        Message msg = this.mHandler.obtainMessage(1003);
        Bundle bundle = msg.getData();
        bundle.putInt("uid", this.mUid);
        NetworkSliceInfo networkSliceInfo = HwNetworkSliceManager.getInstance().getNetworkSliceInfoByPara(this, NetworkSliceInfo.ParaType.NETWORK_CALLBACK);
        if (networkSliceInfo != null) {
            bundle.putParcelable(NetworkSlicesHandler.MSG_NETWORK_REQUEST, networkSliceInfo.getNetworkRequest());
            msg.sendToTarget();
            callCallbackForRequest(524293, null, -1, null, null);
        }
    }

    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
        callCallbackForRequest(524294, network, -1, networkCapabilities, null);
    }

    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
        callCallbackForRequest(524295, network, -1, null, linkProperties);
    }

    public void onNetworkSuspended(Network network) {
        callCallbackForRequest(524297, network, -1, null, null);
    }

    public void onNetworkResumed(Network network) {
        callCallbackForRequest(524298, network, -1, null, null);
    }

    public void onBlockedStatusChanged(Network network, boolean blocked) {
        callCallbackForRequest(524299, network, blocked ? 1 : 0, null, null);
    }

    public void cacheRequestUid(int uid) {
        this.mRequestUids.add(Integer.valueOf(uid));
    }

    public void removeRequestUid(int uid) {
        this.mRequestUids.remove(Integer.valueOf(uid));
    }

    public Set<Integer> getRequestUids() {
        return Collections.unmodifiableSet(this.mRequestUids);
    }

    public int getRequestId() {
        return this.mRequestId;
    }

    public void setRequestId(int requestId) {
        this.mRequestId = requestId;
    }

    private void callCallbackForRequest(int notificationType, Network network, int arg1, NetworkCapabilities networkCapabilities, LinkProperties linkProperties) {
        Bundle extraData = new Bundle();
        extraData.putInt(BUNDLE_NOTIFY_TYPE, notificationType);
        extraData.putInt(BUNDLE_ARG1, arg1);
        extraData.putInt(BUNDLE_REQUEST_ID, -1);
        callCallbackForRequest(network, networkCapabilities, linkProperties, extraData);
    }

    private void callCallbackForRequest(Network network, NetworkCapabilities networkCapabilities, LinkProperties linkProperties, Bundle extraData) {
        int requestId = -1;
        int notificationType = extraData != null ? extraData.getInt(BUNDLE_NOTIFY_TYPE) : -1;
        int arg1 = extraData != null ? extraData.getInt(BUNDLE_ARG1) : -1;
        if (extraData != null) {
            requestId = extraData.getInt(BUNDLE_REQUEST_ID);
        }
        log("callCallbackForRequest notificationType=" + ConnectivityManagerEx.getCallbackName(notificationType) + ",network=" + network + ",arg1=" + arg1 + ",networkCapabilities=" + networkCapabilities);
        NetworkSliceInfo nsi = HwNetworkSliceManager.getInstance().getNetworkSliceInfoByPara(this, NetworkSliceInfo.ParaType.NETWORK_CALLBACK);
        if (nsi != null && isNeedToSendMsgToApp(nsi, requestId)) {
            Bundle bundle = new Bundle();
            Message msg = Message.obtain();
            if (notificationType != 524293) {
                putParcelable(bundle, network);
            }
            switch (notificationType) {
                case 524290:
                    putParcelable(bundle, networkCapabilities);
                    putParcelable(bundle, LinkPropertiesEx.creatLinkProperties(linkProperties));
                    msg.arg1 = arg1;
                    break;
                case 524291:
                    msg.arg1 = arg1;
                    break;
                case 524294:
                    putParcelable(bundle, networkCapabilities);
                    break;
                case 524295:
                    putParcelable(bundle, linkProperties);
                    break;
                case 524299:
                    msg.arg1 = arg1;
                    break;
                default:
                    log("wrong type:" + notificationType);
                    break;
            }
            msg.what = notificationType;
            msg.setData(bundle);
            notifyConnectivityManager(nsi, notificationType, msg, requestId);
        }
    }

    private void notifyConnectivityManager(NetworkSliceInfo nsi, int notificationType, Message msg, int requestId) {
        if (msg != null) {
            if ((524290 == notificationType || 524293 == notificationType) && requestId != -1) {
                try {
                    Messenger messenger = nsi.getMessenger(requestId);
                    if (messenger != null) {
                        putParcelable(msg.getData(), nsi.getNetworkRequestByRequestId(requestId));
                        messenger.send(msg);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException caught trying to send a callback msg");
                }
            } else if (nsi.getMessengers() != null) {
                for (Integer num : nsi.getMessengers().keySet()) {
                    int reqId = num.intValue();
                    Messenger messenger2 = nsi.getMessenger(reqId);
                    if (messenger2 != null) {
                        Message message = Message.obtain();
                        message.copyFrom(msg);
                        putParcelable(message.getData(), nsi.getNetworkRequestByRequestId(reqId));
                        messenger2.send(message);
                    }
                }
            }
        }
    }

    private static <T extends Parcelable> void putParcelable(Bundle bundle, T t) {
        bundle.putParcelable(t.getClass().getSimpleName(), t);
    }

    private boolean isNeedToSendMsgToApp(NetworkSliceInfo nsi, int requestId) {
        if (nsi == null) {
            return false;
        }
        if (nsi.getMessenger(requestId) != null) {
            return true;
        }
        if (requestId != -1 || nsi.getMessenger(this.mRequestId) == null) {
            return false;
        }
        return true;
    }

    public Network getNetwork() {
        return this.mNetwork;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NetworkSliceCallback that = (NetworkSliceCallback) o;
        if (this.mRequestId != that.mRequestId || this.mUid != that.mUid || this.mBlocked != that.mBlocked || !Objects.equals(this.mRequestUids, that.mRequestUids) || !Objects.equals(this.mHandler, that.mHandler) || !Objects.equals(this.mNetwork, that.mNetwork) || !Objects.equals(this.mNetworkCapabilities, that.mNetworkCapabilities) || !Objects.equals(this.mLinkProperties, that.mLinkProperties)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.mRequestUids, Integer.valueOf(this.mRequestId), Integer.valueOf(this.mUid), this.mHandler, this.mNetwork, Boolean.valueOf(this.mBlocked), this.mNetworkCapabilities, this.mLinkProperties);
    }

    private void log(String msg) {
        Log.i(TAG, msg);
    }
}
