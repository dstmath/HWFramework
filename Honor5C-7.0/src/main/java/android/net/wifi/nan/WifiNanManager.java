package android.net.wifi.nan;

import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

public class WifiNanManager {
    private static final boolean DBG = false;
    private static final String TAG = "WifiNanManager";
    private static final boolean VDBG = false;
    private IBinder mBinder;
    private IWifiNanManager mService;

    public WifiNanManager(IWifiNanManager service) {
        this.mService = service;
    }

    public void connect(WifiNanEventListener listener, int events) {
        if (listener == null) {
            try {
                throw new IllegalArgumentException("Invalid listener - must not be null");
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        if (this.mBinder == null) {
            this.mBinder = new Binder();
        }
        this.mService.connect(this.mBinder, listener.callback, events);
    }

    public void disconnect() {
        try {
            this.mService.disconnect(this.mBinder);
            this.mBinder = null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void requestConfig(ConfigRequest configRequest) {
        try {
            this.mService.requestConfig(configRequest);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public WifiNanPublishSession publish(PublishData publishData, PublishSettings publishSettings, WifiNanSessionListener listener, int events) {
        return publishRaw(publishData, publishSettings, listener, events | WifiNanSessionListener.LISTEN_HIDDEN_FLAGS);
    }

    public WifiNanPublishSession publishRaw(PublishData publishData, PublishSettings publishSettings, WifiNanSessionListener listener, int events) {
        if (publishSettings.mPublishType == 0 && publishData.mRxFilterLength != 0) {
            throw new IllegalArgumentException("Invalid publish data & settings: UNSOLICITED publishes (active) can't have an Rx filter");
        } else if (publishSettings.mPublishType == 1 && publishData.mTxFilterLength != 0) {
            throw new IllegalArgumentException("Invalid publish data & settings: SOLICITED publishes (passive) can't have a Tx filter");
        } else if (listener == null) {
            throw new IllegalArgumentException("Invalid listener - must not be null");
        } else {
            try {
                int sessionId = this.mService.createSession(listener.callback, events);
                this.mService.publish(sessionId, publishData, publishSettings);
                return new WifiNanPublishSession(this, sessionId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void publish(int sessionId, PublishData publishData, PublishSettings publishSettings) {
        if (publishSettings.mPublishType == 0 && publishData.mRxFilterLength != 0) {
            throw new IllegalArgumentException("Invalid publish data & settings: UNSOLICITED publishes (active) can't have an Rx filter");
        } else if (publishSettings.mPublishType != 1 || publishData.mTxFilterLength == 0) {
            try {
                this.mService.publish(sessionId, publishData, publishSettings);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("Invalid publish data & settings: SOLICITED publishes (passive) can't have a Tx filter");
        }
    }

    public WifiNanSubscribeSession subscribe(SubscribeData subscribeData, SubscribeSettings subscribeSettings, WifiNanSessionListener listener, int events) {
        return subscribeRaw(subscribeData, subscribeSettings, listener, events | WifiNanSessionListener.LISTEN_HIDDEN_FLAGS);
    }

    public WifiNanSubscribeSession subscribeRaw(SubscribeData subscribeData, SubscribeSettings subscribeSettings, WifiNanSessionListener listener, int events) {
        if (subscribeSettings.mSubscribeType == 1 && subscribeData.mRxFilterLength != 0) {
            throw new IllegalArgumentException("Invalid subscribe data & settings: ACTIVE subscribes can't have an Rx filter");
        } else if (subscribeSettings.mSubscribeType != 0 || subscribeData.mTxFilterLength == 0) {
            try {
                int sessionId = this.mService.createSession(listener.callback, events);
                this.mService.subscribe(sessionId, subscribeData, subscribeSettings);
                return new WifiNanSubscribeSession(this, sessionId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("Invalid subscribe data & settings: PASSIVE subscribes can't have a Tx filter");
        }
    }

    public void subscribe(int sessionId, SubscribeData subscribeData, SubscribeSettings subscribeSettings) {
        if (subscribeSettings.mSubscribeType == 1 && subscribeData.mRxFilterLength != 0) {
            throw new IllegalArgumentException("Invalid subscribe data & settings: ACTIVE subscribes can't have an Rx filter");
        } else if (subscribeSettings.mSubscribeType != 0 || subscribeData.mTxFilterLength == 0) {
            try {
                this.mService.subscribe(sessionId, subscribeData, subscribeSettings);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("Invalid subscribe data & settings: PASSIVE subscribes can't have a Tx filter");
        }
    }

    public void stopSession(int sessionId) {
        try {
            this.mService.stopSession(sessionId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void destroySession(int sessionId) {
        try {
            this.mService.destroySession(sessionId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void sendMessage(int sessionId, int peerId, byte[] message, int messageLength, int messageId) {
        try {
            this.mService.sendMessage(sessionId, peerId, message, messageLength, messageId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
