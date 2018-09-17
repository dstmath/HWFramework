package com.android.server.wifi.aware;

import android.net.wifi.aware.IWifiAwareDiscoverySessionCallback;
import android.net.wifi.aware.PublishConfig;
import android.net.wifi.aware.SubscribeConfig;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import libcore.util.HexEncoding;

public class WifiAwareDiscoverySessionState {
    private static final boolean DBG = false;
    private static final String TAG = "WifiAwareDiscSessState";
    private static final boolean VDBG = false;
    private IWifiAwareDiscoverySessionCallback mCallback;
    private boolean mIsPublishSession;
    private final SparseArray<String> mMacByRequestorInstanceId = new SparseArray();
    private int mPubSubId;
    private int mSessionId;
    private final WifiAwareNativeApi mWifiAwareNativeApi;

    public WifiAwareDiscoverySessionState(WifiAwareNativeApi wifiAwareNativeApi, int sessionId, int pubSubId, IWifiAwareDiscoverySessionCallback callback, boolean isPublishSession) {
        this.mWifiAwareNativeApi = wifiAwareNativeApi;
        this.mSessionId = sessionId;
        this.mPubSubId = pubSubId;
        this.mCallback = callback;
        this.mIsPublishSession = isPublishSession;
    }

    public int getSessionId() {
        return this.mSessionId;
    }

    public int getPubSubId() {
        return this.mPubSubId;
    }

    public boolean isPublishSession() {
        return this.mIsPublishSession;
    }

    public IWifiAwareDiscoverySessionCallback getCallback() {
        return this.mCallback;
    }

    public String getMac(int peerId, String sep) {
        String mac = (String) this.mMacByRequestorInstanceId.get(peerId);
        if (mac == null || sep == null || (sep.isEmpty() ^ 1) == 0) {
            return mac;
        }
        return 10 + 8 + 6 + 4 + 2;
    }

    public void terminate() {
        this.mCallback = null;
        if (this.mIsPublishSession) {
            this.mWifiAwareNativeApi.stopPublish((short) 0, this.mPubSubId);
        } else {
            this.mWifiAwareNativeApi.stopSubscribe((short) 0, this.mPubSubId);
        }
    }

    public boolean isPubSubIdSession(int pubSubId) {
        return this.mPubSubId == pubSubId;
    }

    public boolean updatePublish(short transactionId, PublishConfig config) {
        if (this.mIsPublishSession) {
            boolean success = this.mWifiAwareNativeApi.publish(transactionId, this.mPubSubId, config);
            if (!success) {
                try {
                    this.mCallback.onSessionConfigFail(1);
                } catch (RemoteException e) {
                    Log.w(TAG, "updatePublish onSessionConfigFail(): RemoteException (FYI): " + e);
                }
            }
            return success;
        }
        Log.e(TAG, "A SUBSCRIBE session is being used to publish");
        try {
            this.mCallback.onSessionConfigFail(1);
        } catch (RemoteException e2) {
            Log.e(TAG, "updatePublish: RemoteException=" + e2);
        }
        return false;
    }

    public boolean updateSubscribe(short transactionId, SubscribeConfig config) {
        if (this.mIsPublishSession) {
            Log.e(TAG, "A PUBLISH session is being used to subscribe");
            try {
                this.mCallback.onSessionConfigFail(1);
            } catch (RemoteException e) {
                Log.e(TAG, "updateSubscribe: RemoteException=" + e);
            }
            return false;
        }
        boolean success = this.mWifiAwareNativeApi.subscribe(transactionId, this.mPubSubId, config);
        if (!success) {
            try {
                this.mCallback.onSessionConfigFail(1);
            } catch (RemoteException e2) {
                Log.w(TAG, "updateSubscribe onSessionConfigFail(): RemoteException (FYI): " + e2);
            }
        }
        return success;
    }

    public boolean sendMessage(short transactionId, int peerId, byte[] message, int messageId) {
        String peerMacStr = (String) this.mMacByRequestorInstanceId.get(peerId);
        if (peerMacStr == null) {
            Log.e(TAG, "sendMessage: attempting to send a message to an address which didn't match/contact us");
            try {
                this.mCallback.onMessageSendFail(messageId, 1);
            } catch (RemoteException e) {
                Log.e(TAG, "sendMessage: RemoteException=" + e);
            }
            return false;
        }
        short s = transactionId;
        boolean success = this.mWifiAwareNativeApi.sendMessage(s, this.mPubSubId, peerId, HexEncoding.decode(peerMacStr.toCharArray(), false), message, messageId);
        if (success) {
            return success;
        }
        try {
            this.mCallback.onMessageSendFail(messageId, 1);
        } catch (RemoteException e2) {
            Log.e(TAG, "sendMessage: RemoteException=" + e2);
        }
        return false;
    }

    public void onMatch(int requestorInstanceId, byte[] peerMac, byte[] serviceSpecificInfo, byte[] matchFilter) {
        String prevMac = (String) this.mMacByRequestorInstanceId.get(requestorInstanceId);
        this.mMacByRequestorInstanceId.put(requestorInstanceId, new String(HexEncoding.encode(peerMac)));
        try {
            this.mCallback.onMatch(requestorInstanceId, serviceSpecificInfo, matchFilter);
        } catch (RemoteException e) {
            Log.w(TAG, "onMatch: RemoteException (FYI): " + e);
        }
    }

    public void onMessageReceived(int requestorInstanceId, byte[] peerMac, byte[] message) {
        String prevMac = (String) this.mMacByRequestorInstanceId.get(requestorInstanceId);
        this.mMacByRequestorInstanceId.put(requestorInstanceId, new String(HexEncoding.encode(peerMac)));
        try {
            this.mCallback.onMessageReceived(requestorInstanceId, message);
        } catch (RemoteException e) {
            Log.w(TAG, "onMessageReceived: RemoteException (FYI): " + e);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("AwareSessionState:");
        pw.println("  mSessionId: " + this.mSessionId);
        pw.println("  mIsPublishSession: " + this.mIsPublishSession);
        pw.println("  mPubSubId: " + this.mPubSubId);
        pw.println("  mMacByRequestorInstanceId: [" + this.mMacByRequestorInstanceId + "]");
    }
}
