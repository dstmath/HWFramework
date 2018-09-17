package android.net.wifi.aware;

import android.content.Context;
import android.net.NetworkSpecifier;
import android.net.wifi.RttManager.ParcelableRttParams;
import android.net.wifi.RttManager.ParcelableRttResults;
import android.net.wifi.RttManager.RttListener;
import android.net.wifi.RttManager.RttParams;
import android.net.wifi.aware.IWifiAwareDiscoverySessionCallback.Stub;
import android.net.wifi.aware.TlvBufferUtils.TlvIterable;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import java.lang.ref.WeakReference;
import java.nio.BufferOverflowException;
import java.util.List;
import libcore.util.HexEncoding;

public class WifiAwareManager {
    public static final String ACTION_WIFI_AWARE_STATE_CHANGED = "android.net.wifi.aware.action.WIFI_AWARE_STATE_CHANGED";
    private static final boolean DBG = false;
    private static final String TAG = "WifiAwareManager";
    private static final boolean VDBG = false;
    public static final int WIFI_AWARE_DATA_PATH_ROLE_INITIATOR = 0;
    public static final int WIFI_AWARE_DATA_PATH_ROLE_RESPONDER = 1;
    private final Context mContext;
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private SparseArray<RttListener> mRangingListeners = new SparseArray();
    private final IWifiAwareManager mService;

    private static class WifiAwareDiscoverySessionCallbackProxy extends Stub {
        private static final int CALLBACK_MATCH = 4;
        private static final int CALLBACK_MESSAGE_RECEIVED = 7;
        private static final int CALLBACK_MESSAGE_SEND_FAIL = 6;
        private static final int CALLBACK_MESSAGE_SEND_SUCCESS = 5;
        private static final int CALLBACK_SESSION_CONFIG_FAIL = 2;
        private static final int CALLBACK_SESSION_CONFIG_SUCCESS = 1;
        private static final int CALLBACK_SESSION_STARTED = 0;
        private static final int CALLBACK_SESSION_TERMINATED = 3;
        private static final String MESSAGE_BUNDLE_KEY_MESSAGE = "message";
        private static final String MESSAGE_BUNDLE_KEY_MESSAGE2 = "message2";
        private final WeakReference<WifiAwareManager> mAwareManager;
        private final int mClientId;
        private final Handler mHandler;
        private final boolean mIsPublish;
        private final DiscoverySessionCallback mOriginalCallback;
        private DiscoverySession mSession;

        WifiAwareDiscoverySessionCallbackProxy(WifiAwareManager mgr, Looper looper, boolean isPublish, DiscoverySessionCallback originalCallback, int clientId) {
            this.mAwareManager = new WeakReference(mgr);
            this.mIsPublish = isPublish;
            this.mOriginalCallback = originalCallback;
            this.mClientId = clientId;
            this.mHandler = new Handler(looper) {
                public void handleMessage(Message msg) {
                    if (WifiAwareDiscoverySessionCallbackProxy.this.mAwareManager.get() == null) {
                        Log.w(WifiAwareManager.TAG, "WifiAwareDiscoverySessionCallbackProxy: handleMessage post GC");
                        return;
                    }
                    switch (msg.what) {
                        case 0:
                            WifiAwareDiscoverySessionCallbackProxy.this.onProxySessionStarted(msg.arg1);
                            break;
                        case 1:
                            WifiAwareDiscoverySessionCallbackProxy.this.mOriginalCallback.onSessionConfigUpdated();
                            break;
                        case 2:
                            WifiAwareDiscoverySessionCallbackProxy.this.mOriginalCallback.onSessionConfigFailed();
                            if (WifiAwareDiscoverySessionCallbackProxy.this.mSession == null) {
                                WifiAwareDiscoverySessionCallbackProxy.this.mAwareManager.clear();
                                break;
                            }
                            break;
                        case 3:
                            WifiAwareDiscoverySessionCallbackProxy.this.onProxySessionTerminated(msg.arg1);
                            break;
                        case 4:
                            List matchFilter;
                            byte[] arg = msg.getData().getByteArray(WifiAwareDiscoverySessionCallbackProxy.MESSAGE_BUNDLE_KEY_MESSAGE2);
                            try {
                                matchFilter = new TlvIterable(0, 1, arg).toList();
                            } catch (BufferOverflowException e) {
                                matchFilter = null;
                                Log.e(WifiAwareManager.TAG, "onServiceDiscovered: invalid match filter byte array '" + new String(HexEncoding.encode(arg)) + "' - cannot be parsed: e=" + e);
                            }
                            WifiAwareDiscoverySessionCallbackProxy.this.mOriginalCallback.onServiceDiscovered(new PeerHandle(msg.arg1), msg.getData().getByteArray(WifiAwareDiscoverySessionCallbackProxy.MESSAGE_BUNDLE_KEY_MESSAGE), matchFilter);
                            break;
                        case 5:
                            WifiAwareDiscoverySessionCallbackProxy.this.mOriginalCallback.onMessageSendSucceeded(msg.arg1);
                            break;
                        case 6:
                            WifiAwareDiscoverySessionCallbackProxy.this.mOriginalCallback.onMessageSendFailed(msg.arg1);
                            break;
                        case 7:
                            WifiAwareDiscoverySessionCallbackProxy.this.mOriginalCallback.onMessageReceived(new PeerHandle(msg.arg1), (byte[]) msg.obj);
                            break;
                    }
                }
            };
        }

        public void onSessionStarted(int sessionId) {
            Message msg = this.mHandler.obtainMessage(0);
            msg.arg1 = sessionId;
            this.mHandler.sendMessage(msg);
        }

        public void onSessionConfigSuccess() {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
        }

        public void onSessionConfigFail(int reason) {
            Message msg = this.mHandler.obtainMessage(2);
            msg.arg1 = reason;
            this.mHandler.sendMessage(msg);
        }

        public void onSessionTerminated(int reason) {
            Message msg = this.mHandler.obtainMessage(3);
            msg.arg1 = reason;
            this.mHandler.sendMessage(msg);
        }

        public void onMatch(int peerId, byte[] serviceSpecificInfo, byte[] matchFilter) {
            Bundle data = new Bundle();
            data.putByteArray(MESSAGE_BUNDLE_KEY_MESSAGE, serviceSpecificInfo);
            data.putByteArray(MESSAGE_BUNDLE_KEY_MESSAGE2, matchFilter);
            Message msg = this.mHandler.obtainMessage(4);
            msg.arg1 = peerId;
            msg.setData(data);
            this.mHandler.sendMessage(msg);
        }

        public void onMessageSendSuccess(int messageId) {
            Message msg = this.mHandler.obtainMessage(5);
            msg.arg1 = messageId;
            this.mHandler.sendMessage(msg);
        }

        public void onMessageSendFail(int messageId, int reason) {
            Message msg = this.mHandler.obtainMessage(6);
            msg.arg1 = messageId;
            msg.arg2 = reason;
            this.mHandler.sendMessage(msg);
        }

        public void onMessageReceived(int peerId, byte[] message) {
            Message msg = this.mHandler.obtainMessage(7);
            msg.arg1 = peerId;
            msg.obj = message;
            this.mHandler.sendMessage(msg);
        }

        public void onProxySessionStarted(int sessionId) {
            if (this.mSession != null) {
                Log.e(WifiAwareManager.TAG, "onSessionStarted: sessionId=" + sessionId + ": session already created!?");
                throw new IllegalStateException("onSessionStarted: sessionId=" + sessionId + ": session already created!?");
            }
            WifiAwareManager mgr = (WifiAwareManager) this.mAwareManager.get();
            if (mgr == null) {
                Log.w(WifiAwareManager.TAG, "onProxySessionStarted: mgr GC'd");
                return;
            }
            if (this.mIsPublish) {
                PublishDiscoverySession session = new PublishDiscoverySession(mgr, this.mClientId, sessionId);
                this.mSession = session;
                this.mOriginalCallback.onPublishStarted(session);
            } else {
                SubscribeDiscoverySession session2 = new SubscribeDiscoverySession(mgr, this.mClientId, sessionId);
                this.mSession = session2;
                this.mOriginalCallback.onSubscribeStarted(session2);
            }
        }

        public void onProxySessionTerminated(int reason) {
            if (this.mSession != null) {
                this.mSession.setTerminated();
                this.mSession = null;
            } else {
                Log.w(WifiAwareManager.TAG, "Proxy: onSessionTerminated called but mSession is null!?");
            }
            this.mAwareManager.clear();
            this.mOriginalCallback.onSessionTerminated();
        }
    }

    private static class WifiAwareEventCallbackProxy extends IWifiAwareEventCallback.Stub {
        private static final int CALLBACK_CONNECT_FAIL = 1;
        private static final int CALLBACK_CONNECT_SUCCESS = 0;
        private static final int CALLBACK_IDENTITY_CHANGED = 2;
        private static final int CALLBACK_RANGING_ABORTED = 5;
        private static final int CALLBACK_RANGING_FAILURE = 4;
        private static final int CALLBACK_RANGING_SUCCESS = 3;
        private final WeakReference<WifiAwareManager> mAwareManager;
        private final Binder mBinder;
        private final Handler mHandler;
        private final Looper mLooper;

        RttListener getAndRemoveRangingListener(int rangingId) {
            WifiAwareManager mgr = (WifiAwareManager) this.mAwareManager.get();
            if (mgr == null) {
                Log.w(WifiAwareManager.TAG, "getAndRemoveRangingListener: called post GC");
                return null;
            }
            RttListener listener;
            synchronized (mgr.mLock) {
                listener = (RttListener) mgr.mRangingListeners.get(rangingId);
                mgr.mRangingListeners.delete(rangingId);
            }
            return listener;
        }

        WifiAwareEventCallbackProxy(WifiAwareManager mgr, Looper looper, Binder binder, final AttachCallback attachCallback, final IdentityChangedListener identityChangedListener) {
            this.mAwareManager = new WeakReference(mgr);
            this.mLooper = looper;
            this.mBinder = binder;
            this.mHandler = new Handler(looper) {
                public void handleMessage(Message msg) {
                    WifiAwareManager mgr = (WifiAwareManager) WifiAwareEventCallbackProxy.this.mAwareManager.get();
                    if (mgr == null) {
                        Log.w(WifiAwareManager.TAG, "WifiAwareEventCallbackProxy: handleMessage post GC");
                        return;
                    }
                    RttListener listener;
                    switch (msg.what) {
                        case 0:
                            attachCallback.onAttached(new WifiAwareSession(mgr, WifiAwareEventCallbackProxy.this.mBinder, msg.arg1));
                            break;
                        case 1:
                            WifiAwareEventCallbackProxy.this.mAwareManager.clear();
                            attachCallback.onAttachFailed();
                            break;
                        case 2:
                            if (identityChangedListener != null) {
                                identityChangedListener.onIdentityChanged((byte[]) msg.obj);
                                break;
                            } else {
                                Log.e(WifiAwareManager.TAG, "CALLBACK_IDENTITY_CHANGED: null listener.");
                                break;
                            }
                        case 3:
                            listener = WifiAwareEventCallbackProxy.this.getAndRemoveRangingListener(msg.arg1);
                            if (listener != null) {
                                listener.onSuccess(((ParcelableRttResults) msg.obj).mResults);
                                break;
                            } else {
                                Log.e(WifiAwareManager.TAG, "CALLBACK_RANGING_SUCCESS rangingId=" + msg.arg1 + ": no listener registered (anymore)");
                                break;
                            }
                        case 4:
                            listener = WifiAwareEventCallbackProxy.this.getAndRemoveRangingListener(msg.arg1);
                            if (listener != null) {
                                listener.onFailure(msg.arg2, (String) msg.obj);
                                break;
                            } else {
                                Log.e(WifiAwareManager.TAG, "CALLBACK_RANGING_SUCCESS rangingId=" + msg.arg1 + ": no listener registered (anymore)");
                                break;
                            }
                        case 5:
                            listener = WifiAwareEventCallbackProxy.this.getAndRemoveRangingListener(msg.arg1);
                            if (listener != null) {
                                listener.onAborted();
                                break;
                            } else {
                                Log.e(WifiAwareManager.TAG, "CALLBACK_RANGING_SUCCESS rangingId=" + msg.arg1 + ": no listener registered (anymore)");
                                break;
                            }
                    }
                }
            };
        }

        public void onConnectSuccess(int clientId) {
            Message msg = this.mHandler.obtainMessage(0);
            msg.arg1 = clientId;
            this.mHandler.sendMessage(msg);
        }

        public void onConnectFail(int reason) {
            Message msg = this.mHandler.obtainMessage(1);
            msg.arg1 = reason;
            this.mHandler.sendMessage(msg);
        }

        public void onIdentityChanged(byte[] mac) {
            Message msg = this.mHandler.obtainMessage(2);
            msg.obj = mac;
            this.mHandler.sendMessage(msg);
        }

        public void onRangingSuccess(int rangingId, ParcelableRttResults results) {
            Message msg = this.mHandler.obtainMessage(3);
            msg.arg1 = rangingId;
            msg.obj = results;
            this.mHandler.sendMessage(msg);
        }

        public void onRangingFailure(int rangingId, int reason, String description) {
            Message msg = this.mHandler.obtainMessage(4);
            msg.arg1 = rangingId;
            msg.arg2 = reason;
            msg.obj = description;
            this.mHandler.sendMessage(msg);
        }

        public void onRangingAborted(int rangingId) {
            Message msg = this.mHandler.obtainMessage(5);
            msg.arg1 = rangingId;
            this.mHandler.sendMessage(msg);
        }
    }

    public WifiAwareManager(Context context, IWifiAwareManager service) {
        this.mContext = context;
        this.mService = service;
    }

    public boolean isAvailable() {
        try {
            return this.mService.isUsageEnabled();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Characteristics getCharacteristics() {
        try {
            return this.mService.getCharacteristics();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void attach(AttachCallback attachCallback, Handler handler) {
        attach(handler, null, attachCallback, null);
    }

    public void attach(AttachCallback attachCallback, IdentityChangedListener identityChangedListener, Handler handler) {
        attach(handler, null, attachCallback, identityChangedListener);
    }

    public void attach(Handler handler, ConfigRequest configRequest, AttachCallback attachCallback, IdentityChangedListener identityChangedListener) {
        synchronized (this.mLock) {
            Looper looper = handler == null ? Looper.getMainLooper() : handler.getLooper();
            try {
                Binder binder = new Binder();
                this.mService.connect(binder, this.mContext.getOpPackageName(), new WifiAwareEventCallbackProxy(this, looper, binder, attachCallback, identityChangedListener), configRequest, identityChangedListener != null);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void disconnect(int clientId, Binder binder) {
        try {
            this.mService.disconnect(clientId, binder);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void publish(int clientId, Looper looper, PublishConfig publishConfig, DiscoverySessionCallback callback) {
        try {
            this.mService.publish(clientId, publishConfig, new WifiAwareDiscoverySessionCallbackProxy(this, looper, true, callback, clientId));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void updatePublish(int clientId, int sessionId, PublishConfig publishConfig) {
        try {
            this.mService.updatePublish(clientId, sessionId, publishConfig);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void subscribe(int clientId, Looper looper, SubscribeConfig subscribeConfig, DiscoverySessionCallback callback) {
        try {
            this.mService.subscribe(clientId, subscribeConfig, new WifiAwareDiscoverySessionCallbackProxy(this, looper, false, callback, clientId));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void updateSubscribe(int clientId, int sessionId, SubscribeConfig subscribeConfig) {
        try {
            this.mService.updateSubscribe(clientId, sessionId, subscribeConfig);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void terminateSession(int clientId, int sessionId) {
        try {
            this.mService.terminateSession(clientId, sessionId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void sendMessage(int clientId, int sessionId, PeerHandle peerHandle, byte[] message, int messageId, int retryCount) {
        if (peerHandle == null) {
            throw new IllegalArgumentException("sendMessage: invalid peerHandle - must be non-null");
        }
        try {
            this.mService.sendMessage(clientId, sessionId, peerHandle.peerId, message, messageId, retryCount);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startRanging(int clientId, int sessionId, RttParams[] params, RttListener listener) {
        try {
            int rangingKey = this.mService.startRanging(clientId, sessionId, new ParcelableRttParams(params));
            synchronized (this.mLock) {
                this.mRangingListeners.put(rangingKey, listener);
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public NetworkSpecifier createNetworkSpecifier(int clientId, int role, int sessionId, PeerHandle peerHandle, byte[] pmk, String passphrase) {
        int i = 1;
        int i2 = 0;
        if (role != 0 && role != 1) {
            throw new IllegalArgumentException("createNetworkSpecifier: Invalid 'role' argument when creating a network specifier");
        } else if (role == 0 && peerHandle == null) {
            throw new IllegalArgumentException("createNetworkSpecifier: Invalid peer handle (value of null) - not permitted on INITIATOR");
        } else {
            if (peerHandle != null) {
                i = 0;
            }
            if (peerHandle != null) {
                i2 = peerHandle.peerId;
            }
            return new WifiAwareNetworkSpecifier(i, role, clientId, sessionId, i2, null, pmk, passphrase);
        }
    }

    public NetworkSpecifier createNetworkSpecifier(int clientId, int role, byte[] peer, byte[] pmk, String passphrase) {
        if (role != 0 && role != 1) {
            throw new IllegalArgumentException("createNetworkSpecifier: Invalid 'role' argument when creating a network specifier");
        } else if (role == 0 && peer == null) {
            throw new IllegalArgumentException("createNetworkSpecifier: Invalid peer MAC address - null not permitted on INITIATOR");
        } else if (peer == null || peer.length == 6) {
            int i;
            if (peer == null) {
                i = 3;
            } else {
                i = 2;
            }
            return new WifiAwareNetworkSpecifier(i, role, clientId, 0, 0, peer, pmk, passphrase);
        } else {
            throw new IllegalArgumentException("createNetworkSpecifier: Invalid peer MAC address");
        }
    }
}
