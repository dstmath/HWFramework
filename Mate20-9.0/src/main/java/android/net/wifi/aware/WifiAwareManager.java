package android.net.wifi.aware;

import android.content.Context;
import android.net.NetworkSpecifier;
import android.net.wifi.aware.IWifiAwareDiscoverySessionCallback;
import android.net.wifi.aware.IWifiAwareEventCallback;
import android.net.wifi.aware.TlvBufferUtils;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
    private final IWifiAwareManager mService;

    @Retention(RetentionPolicy.SOURCE)
    public @interface DataPathRole {
    }

    private static class WifiAwareDiscoverySessionCallbackProxy extends IWifiAwareDiscoverySessionCallback.Stub {
        private static final int CALLBACK_MATCH = 4;
        private static final int CALLBACK_MATCH_WITH_DISTANCE = 8;
        private static final int CALLBACK_MESSAGE_RECEIVED = 7;
        private static final int CALLBACK_MESSAGE_SEND_FAIL = 6;
        private static final int CALLBACK_MESSAGE_SEND_SUCCESS = 5;
        private static final int CALLBACK_SESSION_CONFIG_FAIL = 2;
        private static final int CALLBACK_SESSION_CONFIG_SUCCESS = 1;
        private static final int CALLBACK_SESSION_STARTED = 0;
        private static final int CALLBACK_SESSION_TERMINATED = 3;
        private static final String MESSAGE_BUNDLE_KEY_MESSAGE = "message";
        private static final String MESSAGE_BUNDLE_KEY_MESSAGE2 = "message2";
        /* access modifiers changed from: private */
        public final WeakReference<WifiAwareManager> mAwareManager;
        private final int mClientId;
        private final Handler mHandler;
        private final boolean mIsPublish;
        /* access modifiers changed from: private */
        public final DiscoverySessionCallback mOriginalCallback;
        /* access modifiers changed from: private */
        public DiscoverySession mSession;

        WifiAwareDiscoverySessionCallbackProxy(WifiAwareManager mgr, Looper looper, boolean isPublish, DiscoverySessionCallback originalCallback, int clientId) {
            this.mAwareManager = new WeakReference<>(mgr);
            this.mIsPublish = isPublish;
            this.mOriginalCallback = originalCallback;
            this.mClientId = clientId;
            this.mHandler = new Handler(looper) {
                public void handleMessage(Message msg) {
                    List<byte[]> matchFilter;
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
                        case 8:
                            byte[] arg = msg.getData().getByteArray(WifiAwareDiscoverySessionCallbackProxy.MESSAGE_BUNDLE_KEY_MESSAGE2);
                            try {
                                matchFilter = new TlvBufferUtils.TlvIterable(0, 1, arg).toList();
                            } catch (BufferOverflowException e) {
                                matchFilter = null;
                                Log.e(WifiAwareManager.TAG, "onServiceDiscovered: invalid match filter byte array '" + new String(HexEncoding.encode(arg)) + "' - cannot be parsed: e=" + e);
                            }
                            if (msg.what != 4) {
                                WifiAwareDiscoverySessionCallbackProxy.this.mOriginalCallback.onServiceDiscoveredWithinRange(new PeerHandle(msg.arg1), msg.getData().getByteArray(WifiAwareDiscoverySessionCallbackProxy.MESSAGE_BUNDLE_KEY_MESSAGE), matchFilter, msg.arg2);
                                break;
                            } else {
                                WifiAwareDiscoverySessionCallbackProxy.this.mOriginalCallback.onServiceDiscovered(new PeerHandle(msg.arg1), msg.getData().getByteArray(WifiAwareDiscoverySessionCallbackProxy.MESSAGE_BUNDLE_KEY_MESSAGE), matchFilter);
                                break;
                            }
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

        private void onMatchCommon(int messageType, int peerId, byte[] serviceSpecificInfo, byte[] matchFilter, int distanceMm) {
            Bundle data = new Bundle();
            data.putByteArray(MESSAGE_BUNDLE_KEY_MESSAGE, serviceSpecificInfo);
            data.putByteArray(MESSAGE_BUNDLE_KEY_MESSAGE2, matchFilter);
            Message msg = this.mHandler.obtainMessage(messageType);
            msg.arg1 = peerId;
            msg.arg2 = distanceMm;
            msg.setData(data);
            this.mHandler.sendMessage(msg);
        }

        public void onMatch(int peerId, byte[] serviceSpecificInfo, byte[] matchFilter) {
            onMatchCommon(4, peerId, serviceSpecificInfo, matchFilter, 0);
        }

        public void onMatchWithDistance(int peerId, byte[] serviceSpecificInfo, byte[] matchFilter, int distanceMm) {
            onMatchCommon(8, peerId, serviceSpecificInfo, matchFilter, distanceMm);
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
            if (this.mSession == null) {
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
                return;
            }
            Log.e(WifiAwareManager.TAG, "onSessionStarted: sessionId=" + sessionId + ": session already created!?");
            throw new IllegalStateException("onSessionStarted: sessionId=" + sessionId + ": session already created!?");
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
        /* access modifiers changed from: private */
        public final WeakReference<WifiAwareManager> mAwareManager;
        /* access modifiers changed from: private */
        public final Binder mBinder;
        private final Handler mHandler;
        private final Looper mLooper;

        WifiAwareEventCallbackProxy(WifiAwareManager mgr, Looper looper, Binder binder, final AttachCallback attachCallback, final IdentityChangedListener identityChangedListener) {
            this.mAwareManager = new WeakReference<>(mgr);
            this.mLooper = looper;
            this.mBinder = binder;
            this.mHandler = new Handler(looper) {
                public void handleMessage(Message msg) {
                    WifiAwareManager mgr = (WifiAwareManager) WifiAwareEventCallbackProxy.this.mAwareManager.get();
                    if (mgr == null) {
                        Log.w(WifiAwareManager.TAG, "WifiAwareEventCallbackProxy: handleMessage post GC");
                        return;
                    }
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
        Looper looper;
        if (attachCallback != null) {
            synchronized (this.mLock) {
                if (handler == null) {
                    try {
                        looper = Looper.getMainLooper();
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    looper = handler.getLooper();
                }
                Looper looper2 = looper;
                Binder binder = new Binder();
                IWifiAwareManager iWifiAwareManager = this.mService;
                String opPackageName = this.mContext.getOpPackageName();
                WifiAwareEventCallbackProxy wifiAwareEventCallbackProxy = new WifiAwareEventCallbackProxy(this, looper2, binder, attachCallback, identityChangedListener);
                iWifiAwareManager.connect(binder, opPackageName, wifiAwareEventCallbackProxy, configRequest, identityChangedListener != null);
            }
            return;
        }
        throw new IllegalArgumentException("Null callback provided");
    }

    public void disconnect(int clientId, Binder binder) {
        try {
            this.mService.disconnect(clientId, binder);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void publish(int clientId, Looper looper, PublishConfig publishConfig, DiscoverySessionCallback callback) {
        if (callback != null) {
            try {
                IWifiAwareManager iWifiAwareManager = this.mService;
                String opPackageName = this.mContext.getOpPackageName();
                WifiAwareDiscoverySessionCallbackProxy wifiAwareDiscoverySessionCallbackProxy = new WifiAwareDiscoverySessionCallbackProxy(this, looper, true, callback, clientId);
                iWifiAwareManager.publish(opPackageName, clientId, publishConfig, wifiAwareDiscoverySessionCallbackProxy);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("Null callback provided");
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
        if (callback != null) {
            try {
                IWifiAwareManager iWifiAwareManager = this.mService;
                String opPackageName = this.mContext.getOpPackageName();
                WifiAwareDiscoverySessionCallbackProxy wifiAwareDiscoverySessionCallbackProxy = new WifiAwareDiscoverySessionCallbackProxy(this, looper, false, callback, clientId);
                iWifiAwareManager.subscribe(opPackageName, clientId, subscribeConfig, wifiAwareDiscoverySessionCallbackProxy);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("Null callback provided");
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
        if (peerHandle != null) {
            try {
                this.mService.sendMessage(clientId, sessionId, peerHandle.peerId, message, messageId, retryCount);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("sendMessage: invalid peerHandle - must be non-null");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:7:0x001d, code lost:
        if (android.net.wifi.aware.WifiAwareUtils.isLegacyVersion(r14.mContext, 28) == false) goto L_0x0021;
     */
    public NetworkSpecifier createNetworkSpecifier(int clientId, int role, int sessionId, PeerHandle peerHandle, byte[] pmk, String passphrase) {
        int i = role;
        PeerHandle peerHandle2 = peerHandle;
        if (i == 0 || i == 1) {
            if (i != 0) {
            }
            if (peerHandle2 == null) {
                throw new IllegalArgumentException("createNetworkSpecifier: Invalid peer handle - cannot be null");
            }
            WifiAwareNetworkSpecifier wifiAwareNetworkSpecifier = new WifiAwareNetworkSpecifier(peerHandle2 == null ? 1 : 0, i, clientId, sessionId, peerHandle2 != null ? peerHandle2.peerId : 0, null, pmk, passphrase, Process.myUid());
            return wifiAwareNetworkSpecifier;
        }
        throw new IllegalArgumentException("createNetworkSpecifier: Invalid 'role' argument when creating a network specifier");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001d, code lost:
        if (android.net.wifi.aware.WifiAwareUtils.isLegacyVersion(r14.mContext, 28) == false) goto L_0x0021;
     */
    public NetworkSpecifier createNetworkSpecifier(int clientId, int role, byte[] peer, byte[] pmk, String passphrase) {
        int i;
        int i2 = role;
        byte[] bArr = peer;
        if (i2 == 0 || i2 == 1) {
            if (i2 != 0) {
            }
            if (bArr == null) {
                throw new IllegalArgumentException("createNetworkSpecifier: Invalid peer MAC - cannot be null");
            }
            if (bArr == null || bArr.length == 6) {
                if (bArr == null) {
                    i = 3;
                } else {
                    i = 2;
                }
                WifiAwareNetworkSpecifier wifiAwareNetworkSpecifier = new WifiAwareNetworkSpecifier(i, i2, clientId, 0, 0, bArr, pmk, passphrase, Process.myUid());
                return wifiAwareNetworkSpecifier;
            }
            throw new IllegalArgumentException("createNetworkSpecifier: Invalid peer MAC address");
        }
        throw new IllegalArgumentException("createNetworkSpecifier: Invalid 'role' argument when creating a network specifier");
    }
}
