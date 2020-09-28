package com.android.ims;

import android.app.PendingIntent;
import android.os.Message;
import android.telephony.ims.ImsCallProfile;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsCallSessionListener;
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsRegistrationListener;
import com.android.ims.internal.IImsService;
import com.android.ims.internal.IImsUt;

public abstract class ImsServiceBase {
    private ImsServiceBinder mBinder;

    private final class ImsServiceBinder extends IImsService.Stub {
        private ImsServiceBinder() {
        }

        public int open(int phoneId, int serviceClass, PendingIntent incomingCallIntent, IImsRegistrationListener listener) {
            return ImsServiceBase.this.onOpen(phoneId, serviceClass, incomingCallIntent, listener);
        }

        public void close(int serviceId) {
            ImsServiceBase.this.onClose(serviceId);
        }

        public boolean isConnected(int serviceId, int serviceType, int callType) {
            return ImsServiceBase.this.onIsConnected(serviceId, serviceType, callType);
        }

        public boolean isOpened(int serviceId) {
            return ImsServiceBase.this.onIsOpened(serviceId);
        }

        public void setRegistrationListener(int serviceId, IImsRegistrationListener listener) {
            ImsServiceBase.this.onSetRegistrationListener(serviceId, listener);
        }

        public void addRegistrationListener(int serviceId, int serviceType, IImsRegistrationListener listener) {
            ImsServiceBase.this.onAddRegistrationListener(serviceId, serviceType, listener);
        }

        public ImsCallProfile createCallProfile(int serviceId, int serviceType, int callType) {
            return ImsServiceBase.this.onCreateCallProfile(serviceId, serviceType, callType);
        }

        public IImsCallSession createCallSession(int serviceId, ImsCallProfile profile, IImsCallSessionListener listener) {
            return ImsServiceBase.this.onCreateCallSession(serviceId, profile, listener);
        }

        public IImsCallSession getPendingCallSession(int serviceId, String callId) {
            return ImsServiceBase.this.onGetPendingCallSession(serviceId, callId);
        }

        public IImsUt getUtInterface(int serviceId) {
            return ImsServiceBase.this.onGetUtInterface(serviceId);
        }

        public IImsConfig getConfigInterface(int phoneId) {
            return ImsServiceBase.this.onGetConfigInterface(phoneId);
        }

        public void turnOnIms(int phoneId) {
            ImsServiceBase.this.onTurnOnIms(phoneId);
        }

        public void turnOffIms(int phoneId) {
            ImsServiceBase.this.onTurnOffIms(phoneId);
        }

        public IImsEcbm getEcbmInterface(int serviceId) {
            return ImsServiceBase.this.onGetEcbmInterface(serviceId);
        }

        public void setUiTTYMode(int serviceId, int uiTtyMode, Message onComplete) {
            ImsServiceBase.this.onSetUiTTYMode(serviceId, uiTtyMode, onComplete);
        }

        public IImsMultiEndpoint getMultiEndpointInterface(int serviceId) {
            return ImsServiceBase.this.onGetMultiEndpointInterface(serviceId);
        }
    }

    public ImsServiceBinder getBinder() {
        if (this.mBinder == null) {
            this.mBinder = new ImsServiceBinder();
        }
        return this.mBinder;
    }

    /* access modifiers changed from: protected */
    public int onOpen(int phoneId, int serviceClass, PendingIntent incomingCallIntent, IImsRegistrationListener listener) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void onClose(int serviceId) {
    }

    /* access modifiers changed from: protected */
    public boolean onIsConnected(int serviceId, int serviceType, int callType) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean onIsOpened(int serviceId) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void onSetRegistrationListener(int serviceId, IImsRegistrationListener listener) {
    }

    /* access modifiers changed from: protected */
    public void onAddRegistrationListener(int serviceId, int serviceType, IImsRegistrationListener listener) {
    }

    /* access modifiers changed from: protected */
    public ImsCallProfile onCreateCallProfile(int serviceId, int serviceType, int callType) {
        return null;
    }

    /* access modifiers changed from: protected */
    public IImsCallSession onCreateCallSession(int serviceId, ImsCallProfile profile, IImsCallSessionListener listener) {
        return null;
    }

    /* access modifiers changed from: protected */
    public IImsCallSession onGetPendingCallSession(int serviceId, String callId) {
        return null;
    }

    /* access modifiers changed from: protected */
    public IImsUt onGetUtInterface(int serviceId) {
        return null;
    }

    /* access modifiers changed from: protected */
    public IImsConfig onGetConfigInterface(int phoneId) {
        return null;
    }

    /* access modifiers changed from: protected */
    public void onTurnOnIms(int phoneId) {
    }

    /* access modifiers changed from: protected */
    public void onTurnOffIms(int phoneId) {
    }

    /* access modifiers changed from: protected */
    public IImsEcbm onGetEcbmInterface(int serviceId) {
        return null;
    }

    /* access modifiers changed from: protected */
    public void onSetUiTTYMode(int serviceId, int uiTtyMode, Message onComplete) {
    }

    /* access modifiers changed from: protected */
    public IImsMultiEndpoint onGetMultiEndpointInterface(int serviceId) {
        return null;
    }
}
