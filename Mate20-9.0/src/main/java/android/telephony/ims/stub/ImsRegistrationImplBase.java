package android.telephony.ims.stub;

import android.annotation.SystemApi;
import android.net.Uri;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.aidl.IImsRegistration;
import android.telephony.ims.aidl.IImsRegistrationCallback;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.function.Consumer;

@SystemApi
public class ImsRegistrationImplBase {
    private static final String LOG_TAG = "ImsRegistrationImplBase";
    private static final int REGISTRATION_STATE_NOT_REGISTERED = 0;
    private static final int REGISTRATION_STATE_REGISTERED = 2;
    private static final int REGISTRATION_STATE_REGISTERING = 1;
    private static final int REGISTRATION_STATE_UNKNOWN = -1;
    public static final int REGISTRATION_TECH_IWLAN = 1;
    public static final int REGISTRATION_TECH_LTE = 0;
    public static final int REGISTRATION_TECH_NONE = -1;
    private final IImsRegistration mBinder = new IImsRegistration.Stub() {
        public int getRegistrationTechnology() throws RemoteException {
            return ImsRegistrationImplBase.this.getConnectionType();
        }

        public void addRegistrationCallback(IImsRegistrationCallback c) throws RemoteException {
            ImsRegistrationImplBase.this.addRegistrationCallback(c);
        }

        public void removeRegistrationCallback(IImsRegistrationCallback c) throws RemoteException {
            ImsRegistrationImplBase.this.removeRegistrationCallback(c);
        }
    };
    private final RemoteCallbackList<IImsRegistrationCallback> mCallbacks = new RemoteCallbackList<>();
    private int mConnectionType = -1;
    private ImsReasonInfo mLastDisconnectCause = new ImsReasonInfo();
    private final Object mLock = new Object();
    private int mRegistrationState = -1;

    public static class Callback {
        public void onRegistered(int imsRadioTech) {
        }

        public void onRegistering(int imsRadioTech) {
        }

        public void onDeregistered(ImsReasonInfo info) {
        }

        public void onTechnologyChangeFailed(int imsRadioTech, ImsReasonInfo info) {
        }

        public void onSubscriberAssociatedUriChanged(Uri[] uris) {
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ImsRegistrationTech {
    }

    public final IImsRegistration getBinder() {
        return this.mBinder;
    }

    /* access modifiers changed from: private */
    public void addRegistrationCallback(IImsRegistrationCallback c) throws RemoteException {
        this.mCallbacks.register(c);
        updateNewCallbackWithState(c);
    }

    /* access modifiers changed from: private */
    public void removeRegistrationCallback(IImsRegistrationCallback c) {
        this.mCallbacks.unregister(c);
    }

    public final void onRegistered(int imsRadioTech) {
        updateToState(imsRadioTech, 2);
        this.mCallbacks.broadcast(new Consumer(imsRadioTech) {
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ImsRegistrationImplBase.lambda$onRegistered$0(this.f$0, (IImsRegistrationCallback) obj);
            }
        });
    }

    static /* synthetic */ void lambda$onRegistered$0(int imsRadioTech, IImsRegistrationCallback c) {
        try {
            c.onRegistered(imsRadioTech);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, e + " onRegistrationConnected() - Skipping callback.");
        }
    }

    public final void onRegistering(int imsRadioTech) {
        updateToState(imsRadioTech, 1);
        this.mCallbacks.broadcast(new Consumer(imsRadioTech) {
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ImsRegistrationImplBase.lambda$onRegistering$1(this.f$0, (IImsRegistrationCallback) obj);
            }
        });
    }

    static /* synthetic */ void lambda$onRegistering$1(int imsRadioTech, IImsRegistrationCallback c) {
        try {
            c.onRegistering(imsRadioTech);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, e + " onRegistrationProcessing() - Skipping callback.");
        }
    }

    public final void onDeregistered(ImsReasonInfo info) {
        updateToDisconnectedState(info);
        this.mCallbacks.broadcast(new Consumer() {
            public final void accept(Object obj) {
                ImsRegistrationImplBase.lambda$onDeregistered$2(ImsReasonInfo.this, (IImsRegistrationCallback) obj);
            }
        });
    }

    static /* synthetic */ void lambda$onDeregistered$2(ImsReasonInfo info, IImsRegistrationCallback c) {
        try {
            c.onDeregistered(info);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, e + " onRegistrationDisconnected() - Skipping callback.");
        }
    }

    public final void onTechnologyChangeFailed(int imsRadioTech, ImsReasonInfo info) {
        this.mCallbacks.broadcast(new Consumer(imsRadioTech, info) {
            private final /* synthetic */ int f$0;
            private final /* synthetic */ ImsReasonInfo f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                ImsRegistrationImplBase.lambda$onTechnologyChangeFailed$3(this.f$0, this.f$1, (IImsRegistrationCallback) obj);
            }
        });
    }

    static /* synthetic */ void lambda$onTechnologyChangeFailed$3(int imsRadioTech, ImsReasonInfo info, IImsRegistrationCallback c) {
        try {
            c.onTechnologyChangeFailed(imsRadioTech, info);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, e + " onRegistrationChangeFailed() - Skipping callback.");
        }
    }

    public final void onSubscriberAssociatedUriChanged(Uri[] uris) {
        this.mCallbacks.broadcast(new Consumer(uris) {
            private final /* synthetic */ Uri[] f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ImsRegistrationImplBase.lambda$onSubscriberAssociatedUriChanged$4(this.f$0, (IImsRegistrationCallback) obj);
            }
        });
    }

    static /* synthetic */ void lambda$onSubscriberAssociatedUriChanged$4(Uri[] uris, IImsRegistrationCallback c) {
        try {
            c.onSubscriberAssociatedUriChanged(uris);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, e + " onSubscriberAssociatedUriChanged() - Skipping callback.");
        }
    }

    private void updateToState(int connType, int newState) {
        synchronized (this.mLock) {
            this.mConnectionType = connType;
            this.mRegistrationState = newState;
            this.mLastDisconnectCause = null;
        }
    }

    private void updateToDisconnectedState(ImsReasonInfo info) {
        synchronized (this.mLock) {
            updateToState(-1, 0);
            if (info != null) {
                this.mLastDisconnectCause = info;
            } else {
                Log.w(LOG_TAG, "updateToDisconnectedState: no ImsReasonInfo provided.");
                this.mLastDisconnectCause = new ImsReasonInfo();
            }
        }
    }

    @VisibleForTesting
    public final int getConnectionType() {
        int i;
        synchronized (this.mLock) {
            i = this.mConnectionType;
        }
        return i;
    }

    private void updateNewCallbackWithState(IImsRegistrationCallback c) throws RemoteException {
        int state;
        ImsReasonInfo disconnectInfo;
        synchronized (this.mLock) {
            state = this.mRegistrationState;
            disconnectInfo = this.mLastDisconnectCause;
        }
        switch (state) {
            case 0:
                c.onDeregistered(disconnectInfo);
                return;
            case 1:
                c.onRegistering(getConnectionType());
                return;
            case 2:
                c.onRegistered(getConnectionType());
                return;
            default:
                return;
        }
    }
}
