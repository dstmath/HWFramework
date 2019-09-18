package com.android.server.wifi.hotspot2;

import android.content.Context;
import android.net.Network;
import android.net.wifi.hotspot2.IProvisioningCallback;
import android.net.wifi.hotspot2.OsuProvider;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.wifi.hotspot2.OsuNetworkConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PasspointProvisioner {
    private static final int PROVISIONING_FAILURE = 1;
    private static final int PROVISIONING_STATUS = 0;
    private static final String TAG = "PasspointProvisioner";
    private static final String TLS_VERSION = "TLSv1";
    private int mCallingUid;
    private final Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentSessionId = 0;
    private final PasspointObjectFactory mObjectFactory;
    /* access modifiers changed from: private */
    public final OsuNetworkCallbacks mOsuNetworkCallbacks;
    /* access modifiers changed from: private */
    public final OsuNetworkConnection mOsuNetworkConnection;
    /* access modifiers changed from: private */
    public final OsuServerConnection mOsuServerConnection;
    /* access modifiers changed from: private */
    public final ProvisioningStateMachine mProvisioningStateMachine;
    /* access modifiers changed from: private */
    public boolean mVerboseLoggingEnabled = false;
    private final WfaKeyStore mWfaKeyStore;

    class OsuNetworkCallbacks implements OsuNetworkConnection.Callbacks {
        OsuNetworkCallbacks() {
        }

        public void onConnected(Network network) {
            if (PasspointProvisioner.this.mVerboseLoggingEnabled) {
                Log.v(PasspointProvisioner.TAG, "onConnected to " + network);
            }
            if (network == null) {
                PasspointProvisioner.this.mProvisioningStateMachine.handleDisconnect();
            } else {
                PasspointProvisioner.this.mProvisioningStateMachine.handleConnectedEvent(network);
            }
        }

        public void onDisconnected() {
            if (PasspointProvisioner.this.mVerboseLoggingEnabled) {
                Log.v(PasspointProvisioner.TAG, "onDisconnected");
            }
            PasspointProvisioner.this.mProvisioningStateMachine.handleDisconnect();
        }

        public void onTimeOut() {
            if (PasspointProvisioner.this.mVerboseLoggingEnabled) {
                Log.v(PasspointProvisioner.TAG, "Timed out waiting for connection to OSU AP");
            }
            PasspointProvisioner.this.mProvisioningStateMachine.handleDisconnect();
        }

        public void onWifiEnabled() {
            if (PasspointProvisioner.this.mVerboseLoggingEnabled) {
                Log.v(PasspointProvisioner.TAG, "onWifiEnabled");
            }
        }

        public void onWifiDisabled() {
            if (PasspointProvisioner.this.mVerboseLoggingEnabled) {
                Log.v(PasspointProvisioner.TAG, "onWifiDisabled");
            }
            PasspointProvisioner.this.mProvisioningStateMachine.handleWifiDisabled();
        }
    }

    public class OsuServerCallbacks {
        private final int mSessionId;

        OsuServerCallbacks(int sessionId) {
            this.mSessionId = sessionId;
        }

        public int getSessionId() {
            return this.mSessionId;
        }

        public void onServerValidationStatus(int sessionId, boolean succeeded) {
            if (PasspointProvisioner.this.mVerboseLoggingEnabled) {
                Log.v(PasspointProvisioner.TAG, "OSU Server Validation status=" + succeeded + " sessionId=" + sessionId);
            }
            if (succeeded) {
                PasspointProvisioner.this.mProvisioningStateMachine.getHandler().post(new Runnable(sessionId) {
                    private final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        PasspointProvisioner.this.mProvisioningStateMachine.handleServerValidationSuccess(this.f$1);
                    }
                });
            } else {
                PasspointProvisioner.this.mProvisioningStateMachine.getHandler().post(new Runnable(sessionId) {
                    private final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        PasspointProvisioner.this.mProvisioningStateMachine.handleServerValidationFailure(this.f$1);
                    }
                });
            }
        }
    }

    class ProvisioningStateMachine {
        private static final int INITIAL_STATE = 1;
        private static final int OSU_AP_CONNECTED = 3;
        private static final int OSU_PROVIDER_VERIFIED = 6;
        private static final int OSU_SERVER_CONNECTED = 4;
        private static final int OSU_SERVER_VALIDATED = 5;
        private static final String TAG = "ProvisioningStateMachine";
        private static final int WAITING_TO_CONNECT = 2;
        private Handler mHandler;
        private OsuProvider mOsuProvider;
        private IProvisioningCallback mProvisioningCallback;
        private URL mServerUrl;
        private int mState = 1;

        ProvisioningStateMachine() {
        }

        public void start(Handler handler) {
            this.mHandler = handler;
        }

        public Handler getHandler() {
            return this.mHandler;
        }

        public void startProvisioning(OsuProvider provider, IProvisioningCallback callback) {
            if (PasspointProvisioner.this.mVerboseLoggingEnabled) {
                Log.v(TAG, "startProvisioning received in state=" + this.mState);
            }
            if (this.mState != 1) {
                if (PasspointProvisioner.this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "State Machine needs to be reset before starting provisioning");
                }
                resetStateMachine(6);
            }
            if (!PasspointProvisioner.this.mOsuServerConnection.canValidateServer()) {
                Log.w(TAG, "Provisioning is not possible");
                this.mProvisioningCallback = callback;
                resetStateMachine(7);
                return;
            }
            try {
                this.mServerUrl = new URL(provider.getServerUri().toString());
                this.mProvisioningCallback = callback;
                this.mOsuProvider = provider;
                PasspointProvisioner.this.mOsuNetworkConnection.setEventCallback(PasspointProvisioner.this.mOsuNetworkCallbacks);
                PasspointProvisioner.this.mOsuServerConnection.setEventCallback(new OsuServerCallbacks(PasspointProvisioner.access$404(PasspointProvisioner.this)));
                if (!PasspointProvisioner.this.mOsuNetworkConnection.connect(this.mOsuProvider.getOsuSsid(), this.mOsuProvider.getNetworkAccessIdentifier())) {
                    resetStateMachine(1);
                    return;
                }
                invokeProvisioningCallback(0, 1);
                changeState(2);
            } catch (MalformedURLException e) {
                Log.e(TAG, "Invalid Server URL");
                this.mProvisioningCallback = callback;
                resetStateMachine(2);
            }
        }

        public void handleWifiDisabled() {
            if (PasspointProvisioner.this.mVerboseLoggingEnabled) {
                Log.v(TAG, "Wifi Disabled in state=" + this.mState);
            }
            if (this.mState == 1) {
                Log.w(TAG, "Wifi Disable unhandled in state=" + this.mState);
                return;
            }
            resetStateMachine(1);
        }

        public void handleServerValidationFailure(int sessionId) {
            if (PasspointProvisioner.this.mVerboseLoggingEnabled) {
                Log.v(TAG, "Server Validation failure received in " + this.mState);
            }
            if (sessionId != PasspointProvisioner.this.mCurrentSessionId) {
                Log.w(TAG, "Expected server validation callback for currentSessionId=" + PasspointProvisioner.this.mCurrentSessionId);
            } else if (this.mState != 4) {
                Log.wtf(TAG, "Server Validation Failure unhandled in mState=" + this.mState);
            } else {
                resetStateMachine(4);
            }
        }

        public void handleServerValidationSuccess(int sessionId) {
            if (PasspointProvisioner.this.mVerboseLoggingEnabled) {
                Log.v(TAG, "Server Validation Success received in " + this.mState);
            }
            if (sessionId != PasspointProvisioner.this.mCurrentSessionId) {
                Log.w(TAG, "Expected server validation callback for currentSessionId=" + PasspointProvisioner.this.mCurrentSessionId);
            } else if (this.mState != 4) {
                Log.wtf(TAG, "Server validation success event unhandled in state=" + this.mState);
            } else {
                changeState(5);
                invokeProvisioningCallback(0, 4);
                validateProvider();
            }
        }

        private void validateProvider() {
            if (PasspointProvisioner.this.mVerboseLoggingEnabled) {
                Log.v(TAG, "Validating provider in state=" + this.mState);
            }
            if (!PasspointProvisioner.this.mOsuServerConnection.validateProvider(this.mOsuProvider.getFriendlyName())) {
                resetStateMachine(5);
                return;
            }
            changeState(6);
            invokeProvisioningCallback(0, 5);
        }

        public void handleConnectedEvent(Network network) {
            if (PasspointProvisioner.this.mVerboseLoggingEnabled) {
                Log.v(TAG, "Connected event received in state=" + this.mState);
            }
            if (this.mState != 2) {
                Log.wtf(TAG, "Connection event unhandled in state=" + this.mState);
                return;
            }
            invokeProvisioningCallback(0, 2);
            changeState(3);
            initiateServerConnection(network);
        }

        private void initiateServerConnection(Network network) {
            if (PasspointProvisioner.this.mVerboseLoggingEnabled) {
                Log.v(TAG, "Initiating server connection in state=" + this.mState);
            }
            if (this.mState != 3) {
                Log.wtf(TAG, "Initiating server connection aborted in invalid state=" + this.mState);
            } else if (!PasspointProvisioner.this.mOsuServerConnection.connect(this.mServerUrl, network)) {
                resetStateMachine(3);
            } else {
                changeState(4);
                invokeProvisioningCallback(0, 3);
            }
        }

        public void handleDisconnect() {
            if (PasspointProvisioner.this.mVerboseLoggingEnabled) {
                Log.v(TAG, "Connection failed in state=" + this.mState);
            }
            if (this.mState == 1) {
                Log.w(TAG, "Disconnect event unhandled in state=" + this.mState);
                return;
            }
            resetStateMachine(1);
        }

        private void invokeProvisioningCallback(int callbackType, int status) {
            if (this.mProvisioningCallback == null) {
                Log.e(TAG, "Provisioning callback " + callbackType + " with status " + status + " not invoked");
                return;
            }
            if (callbackType == 0) {
                try {
                    this.mProvisioningCallback.onProvisioningStatus(status);
                } catch (RemoteException e) {
                    Log.e(TAG, "Remote Exception while posting callback type=" + callbackType + " status=" + status);
                }
            } else {
                this.mProvisioningCallback.onProvisioningFailure(status);
            }
        }

        private void changeState(int nextState) {
            if (nextState != this.mState) {
                if (PasspointProvisioner.this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "Changing state from " + this.mState + " -> " + nextState);
                }
                this.mState = nextState;
            }
        }

        private void resetStateMachine(int failureCode) {
            invokeProvisioningCallback(1, failureCode);
            PasspointProvisioner.this.mOsuNetworkConnection.setEventCallback(null);
            PasspointProvisioner.this.mOsuNetworkConnection.disconnectIfNeeded();
            PasspointProvisioner.this.mOsuServerConnection.setEventCallback(null);
            PasspointProvisioner.this.mOsuServerConnection.cleanup();
            changeState(1);
        }
    }

    static /* synthetic */ int access$404(PasspointProvisioner x0) {
        int i = x0.mCurrentSessionId + 1;
        x0.mCurrentSessionId = i;
        return i;
    }

    PasspointProvisioner(Context context, PasspointObjectFactory objectFactory) {
        this.mContext = context;
        this.mOsuNetworkConnection = objectFactory.makeOsuNetworkConnection(context);
        this.mProvisioningStateMachine = new ProvisioningStateMachine();
        this.mOsuNetworkCallbacks = new OsuNetworkCallbacks();
        this.mOsuServerConnection = objectFactory.makeOsuServerConnection();
        this.mWfaKeyStore = objectFactory.makeWfaKeyStore();
        this.mObjectFactory = objectFactory;
    }

    public void init(Looper looper) {
        this.mProvisioningStateMachine.start(new Handler(looper));
        this.mOsuNetworkConnection.init(this.mProvisioningStateMachine.getHandler());
        this.mProvisioningStateMachine.getHandler().post(new Runnable() {
            public final void run() {
                PasspointProvisioner.lambda$init$0(PasspointProvisioner.this);
            }
        });
    }

    public static /* synthetic */ void lambda$init$0(PasspointProvisioner passpointProvisioner) {
        passpointProvisioner.mWfaKeyStore.load();
        passpointProvisioner.mOsuServerConnection.init(passpointProvisioner.mObjectFactory.getSSLContext(TLS_VERSION), passpointProvisioner.mObjectFactory.getTrustManagerImpl(passpointProvisioner.mWfaKeyStore.get()));
    }

    public void enableVerboseLogging(int level) {
        this.mVerboseLoggingEnabled = level > 0;
        this.mOsuNetworkConnection.enableVerboseLogging(level);
        this.mOsuServerConnection.enableVerboseLogging(level);
    }

    public boolean startSubscriptionProvisioning(int callingUid, OsuProvider provider, IProvisioningCallback callback) {
        this.mCallingUid = callingUid;
        Log.v(TAG, "Provisioning started with " + provider.toString());
        this.mProvisioningStateMachine.getHandler().post(new Runnable(provider, callback) {
            private final /* synthetic */ OsuProvider f$1;
            private final /* synthetic */ IProvisioningCallback f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                PasspointProvisioner.this.mProvisioningStateMachine.startProvisioning(this.f$1, this.f$2);
            }
        });
        return true;
    }
}
