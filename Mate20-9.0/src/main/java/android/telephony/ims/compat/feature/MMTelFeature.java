package android.telephony.ims.compat.feature;

import android.app.PendingIntent;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.stub.ImsEcbmImplBase;
import android.telephony.ims.stub.ImsMultiEndpointImplBase;
import android.telephony.ims.stub.ImsUtImplBase;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsCallSessionListener;
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMMTelFeature;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsRegistrationListener;
import com.android.ims.internal.IImsUt;

public class MMTelFeature extends ImsFeature {
    private final IImsMMTelFeature mImsMMTelBinder = new IImsMMTelFeature.Stub() {
        public int startSession(PendingIntent incomingCallIntent, IImsRegistrationListener listener) throws RemoteException {
            int startSession;
            synchronized (MMTelFeature.this.mLock) {
                startSession = MMTelFeature.this.startSession(incomingCallIntent, listener);
            }
            return startSession;
        }

        public void endSession(int sessionId) throws RemoteException {
            synchronized (MMTelFeature.this.mLock) {
                MMTelFeature.this.endSession(sessionId);
            }
        }

        public boolean isConnected(int callSessionType, int callType) throws RemoteException {
            boolean isConnected;
            synchronized (MMTelFeature.this.mLock) {
                isConnected = MMTelFeature.this.isConnected(callSessionType, callType);
            }
            return isConnected;
        }

        public boolean isOpened() throws RemoteException {
            boolean isOpened;
            synchronized (MMTelFeature.this.mLock) {
                isOpened = MMTelFeature.this.isOpened();
            }
            return isOpened;
        }

        public int getFeatureStatus() throws RemoteException {
            int featureState;
            synchronized (MMTelFeature.this.mLock) {
                featureState = MMTelFeature.this.getFeatureState();
            }
            return featureState;
        }

        public void addRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
            synchronized (MMTelFeature.this.mLock) {
                MMTelFeature.this.addRegistrationListener(listener);
            }
        }

        public void removeRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
            synchronized (MMTelFeature.this.mLock) {
                MMTelFeature.this.removeRegistrationListener(listener);
            }
        }

        public ImsCallProfile createCallProfile(int sessionId, int callSessionType, int callType) throws RemoteException {
            ImsCallProfile createCallProfile;
            synchronized (MMTelFeature.this.mLock) {
                createCallProfile = MMTelFeature.this.createCallProfile(sessionId, callSessionType, callType);
            }
            return createCallProfile;
        }

        public IImsCallSession createCallSession(int sessionId, ImsCallProfile profile) throws RemoteException {
            IImsCallSession createCallSession;
            synchronized (MMTelFeature.this.mLock) {
                createCallSession = MMTelFeature.this.createCallSession(sessionId, profile, null);
            }
            return createCallSession;
        }

        public IImsCallSession getPendingCallSession(int sessionId, String callId) throws RemoteException {
            IImsCallSession pendingCallSession;
            synchronized (MMTelFeature.this.mLock) {
                pendingCallSession = MMTelFeature.this.getPendingCallSession(sessionId, callId);
            }
            return pendingCallSession;
        }

        public IImsUt getUtInterface() throws RemoteException {
            IImsUt iImsUt;
            synchronized (MMTelFeature.this.mLock) {
                ImsUtImplBase implBase = MMTelFeature.this.getUtInterface();
                iImsUt = implBase != null ? implBase.getInterface() : null;
            }
            return iImsUt;
        }

        public IImsConfig getConfigInterface() throws RemoteException {
            IImsConfig configInterface;
            synchronized (MMTelFeature.this.mLock) {
                configInterface = MMTelFeature.this.getConfigInterface();
            }
            return configInterface;
        }

        public void turnOnIms() throws RemoteException {
            synchronized (MMTelFeature.this.mLock) {
                MMTelFeature.this.turnOnIms();
            }
        }

        public void turnOffIms() throws RemoteException {
            synchronized (MMTelFeature.this.mLock) {
                MMTelFeature.this.turnOffIms();
            }
        }

        public IImsEcbm getEcbmInterface() throws RemoteException {
            IImsEcbm imsEcbm;
            synchronized (MMTelFeature.this.mLock) {
                ImsEcbmImplBase implBase = MMTelFeature.this.getEcbmInterface();
                imsEcbm = implBase != null ? implBase.getImsEcbm() : null;
            }
            return imsEcbm;
        }

        public void setUiTTYMode(int uiTtyMode, Message onComplete) throws RemoteException {
            synchronized (MMTelFeature.this.mLock) {
                MMTelFeature.this.setUiTTYMode(uiTtyMode, onComplete);
            }
        }

        public IImsMultiEndpoint getMultiEndpointInterface() throws RemoteException {
            IImsMultiEndpoint iImsMultiEndpoint;
            synchronized (MMTelFeature.this.mLock) {
                ImsMultiEndpointImplBase implBase = MMTelFeature.this.getMultiEndpointInterface();
                iImsMultiEndpoint = implBase != null ? implBase.getIImsMultiEndpoint() : null;
            }
            return iImsMultiEndpoint;
        }
    };
    /* access modifiers changed from: private */
    public final Object mLock = new Object();

    public final IImsMMTelFeature getBinder() {
        return this.mImsMMTelBinder;
    }

    public int startSession(PendingIntent incomingCallIntent, IImsRegistrationListener listener) {
        return 0;
    }

    public void endSession(int sessionId) {
    }

    public boolean isConnected(int callSessionType, int callType) {
        return false;
    }

    public boolean isOpened() {
        return false;
    }

    public void addRegistrationListener(IImsRegistrationListener listener) {
    }

    public void removeRegistrationListener(IImsRegistrationListener listener) {
    }

    public ImsCallProfile createCallProfile(int sessionId, int callSessionType, int callType) {
        return null;
    }

    public IImsCallSession createCallSession(int sessionId, ImsCallProfile profile, IImsCallSessionListener listener) {
        return null;
    }

    public IImsCallSession getPendingCallSession(int sessionId, String callId) {
        return null;
    }

    public ImsUtImplBase getUtInterface() {
        return null;
    }

    public IImsConfig getConfigInterface() {
        return null;
    }

    public void turnOnIms() {
    }

    public void turnOffIms() {
    }

    public ImsEcbmImplBase getEcbmInterface() {
        return null;
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
    }

    public ImsMultiEndpointImplBase getMultiEndpointInterface() {
        return null;
    }

    public void onFeatureReady() {
    }

    public void onFeatureRemoved() {
    }

    public int getLastCallType() {
        return -1;
    }
}
