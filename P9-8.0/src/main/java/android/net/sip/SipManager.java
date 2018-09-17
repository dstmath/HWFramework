package android.net.sip;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.sip.ISipService.Stub;
import android.net.sip.SipAudioCall.Listener;
import android.net.sip.SipProfile.Builder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import java.text.ParseException;

public class SipManager {
    public static final String ACTION_SIP_ADD_PHONE = "com.android.phone.SIP_ADD_PHONE";
    public static final String ACTION_SIP_CALL_OPTION_CHANGED = "com.android.phone.SIP_CALL_OPTION_CHANGED";
    public static final String ACTION_SIP_INCOMING_CALL = "com.android.phone.SIP_INCOMING_CALL";
    public static final String ACTION_SIP_REMOVE_PHONE = "com.android.phone.SIP_REMOVE_PHONE";
    public static final String ACTION_SIP_SERVICE_UP = "android.net.sip.SIP_SERVICE_UP";
    public static final String EXTRA_CALL_ID = "android:sipCallID";
    public static final String EXTRA_LOCAL_URI = "android:localSipUri";
    public static final String EXTRA_OFFER_SD = "android:sipOfferSD";
    public static final int INCOMING_CALL_RESULT_CODE = 101;
    private static final String TAG = "SipManager";
    private Context mContext;
    private ISipService mSipService;

    private static class ListenerRelay extends SipSessionAdapter {
        private SipRegistrationListener mListener;
        private String mUri;

        public ListenerRelay(SipRegistrationListener listener, String uri) {
            this.mListener = listener;
            this.mUri = uri;
        }

        private String getUri(ISipSession session) {
            String str;
            if (session == null) {
                try {
                    str = this.mUri;
                } catch (Throwable e) {
                    Rlog.e(SipManager.TAG, "getUri(): ", e);
                    return null;
                }
            }
            str = session.getLocalProfile().getUriString();
            return str;
        }

        public void onRegistering(ISipSession session) {
            this.mListener.onRegistering(getUri(session));
        }

        public void onRegistrationDone(ISipSession session, int duration) {
            long expiryTime = (long) duration;
            if (duration > 0) {
                expiryTime += System.currentTimeMillis();
            }
            this.mListener.onRegistrationDone(getUri(session), expiryTime);
        }

        public void onRegistrationFailed(ISipSession session, int errorCode, String message) {
            this.mListener.onRegistrationFailed(getUri(session), errorCode, message);
        }

        public void onRegistrationTimeout(ISipSession session) {
            this.mListener.onRegistrationFailed(getUri(session), -5, "registration timed out");
        }
    }

    public static SipManager newInstance(Context context) {
        return isApiSupported(context) ? new SipManager(context) : null;
    }

    public static boolean isApiSupported(Context context) {
        return context.getPackageManager().hasSystemFeature("android.software.sip");
    }

    public static boolean isVoipSupported(Context context) {
        if (context.getPackageManager().hasSystemFeature("android.software.sip.voip")) {
            return isApiSupported(context);
        }
        return false;
    }

    public static boolean isSipWifiOnly(Context context) {
        return context.getResources().getBoolean(17957007);
    }

    private SipManager(Context context) {
        this.mContext = context;
        createSipService();
    }

    private void createSipService() {
        if (this.mSipService == null) {
            this.mSipService = Stub.asInterface(ServiceManager.getService("sip"));
        }
    }

    private void checkSipServiceConnection() throws SipException {
        createSipService();
        if (this.mSipService == null) {
            throw new SipException("SipService is dead and is restarting...", new Exception());
        }
    }

    public void open(SipProfile localProfile) throws SipException {
        try {
            checkSipServiceConnection();
            this.mSipService.open(localProfile, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw new SipException("open()", e);
        }
    }

    public void open(SipProfile localProfile, PendingIntent incomingCallPendingIntent, SipRegistrationListener listener) throws SipException {
        if (incomingCallPendingIntent == null) {
            throw new NullPointerException("incomingCallPendingIntent cannot be null");
        }
        try {
            checkSipServiceConnection();
            this.mSipService.open3(localProfile, incomingCallPendingIntent, createRelay(listener, localProfile.getUriString()), this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw new SipException("open()", e);
        }
    }

    public void setRegistrationListener(String localProfileUri, SipRegistrationListener listener) throws SipException {
        try {
            checkSipServiceConnection();
            this.mSipService.setRegistrationListener(localProfileUri, createRelay(listener, localProfileUri), this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw new SipException("setRegistrationListener()", e);
        }
    }

    public void close(String localProfileUri) throws SipException {
        try {
            checkSipServiceConnection();
            this.mSipService.close(localProfileUri, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw new SipException("close()", e);
        }
    }

    public boolean isOpened(String localProfileUri) throws SipException {
        try {
            checkSipServiceConnection();
            return this.mSipService.isOpened(localProfileUri, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw new SipException("isOpened()", e);
        }
    }

    public boolean isRegistered(String localProfileUri) throws SipException {
        try {
            checkSipServiceConnection();
            return this.mSipService.isRegistered(localProfileUri, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw new SipException("isRegistered()", e);
        }
    }

    public SipAudioCall makeAudioCall(SipProfile localProfile, SipProfile peerProfile, Listener listener, int timeout) throws SipException {
        if (isVoipSupported(this.mContext)) {
            SipAudioCall call = new SipAudioCall(this.mContext, localProfile);
            call.setListener(listener);
            call.makeCall(peerProfile, createSipSession(localProfile, null), timeout);
            return call;
        }
        throw new SipException("VOIP API is not supported");
    }

    public SipAudioCall makeAudioCall(String localProfileUri, String peerProfileUri, Listener listener, int timeout) throws SipException {
        if (isVoipSupported(this.mContext)) {
            try {
                return makeAudioCall(new Builder(localProfileUri).build(), new Builder(peerProfileUri).build(), listener, timeout);
            } catch (ParseException e) {
                throw new SipException("build SipProfile", e);
            }
        }
        throw new SipException("VOIP API is not supported");
    }

    public SipAudioCall takeAudioCall(Intent incomingCallIntent, Listener listener) throws SipException {
        if (incomingCallIntent == null) {
            throw new SipException("Cannot retrieve session with null intent");
        }
        String callId = getCallId(incomingCallIntent);
        if (callId == null) {
            throw new SipException("Call ID missing in incoming call intent");
        }
        String offerSd = getOfferSessionDescription(incomingCallIntent);
        if (offerSd == null) {
            throw new SipException("Session description missing in incoming call intent");
        }
        try {
            checkSipServiceConnection();
            ISipSession session = this.mSipService.getPendingSession(callId, this.mContext.getOpPackageName());
            if (session == null) {
                throw new SipException("No pending session for the call");
            }
            SipAudioCall call = new SipAudioCall(this.mContext, session.getLocalProfile());
            call.attachCall(new SipSession(session), offerSd);
            call.setListener(listener);
            return call;
        } catch (Throwable t) {
            SipException sipException = new SipException("takeAudioCall()", t);
        }
    }

    public static boolean isIncomingCallIntent(Intent intent) {
        boolean z = false;
        if (intent == null) {
            return false;
        }
        String callId = getCallId(intent);
        String offerSd = getOfferSessionDescription(intent);
        if (!(callId == null || offerSd == null)) {
            z = true;
        }
        return z;
    }

    public static String getCallId(Intent incomingCallIntent) {
        return incomingCallIntent.getStringExtra(EXTRA_CALL_ID);
    }

    public static String getOfferSessionDescription(Intent incomingCallIntent) {
        return incomingCallIntent.getStringExtra(EXTRA_OFFER_SD);
    }

    public static Intent createIncomingCallBroadcast(String callId, String sessionDescription) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CALL_ID, callId);
        intent.putExtra(EXTRA_OFFER_SD, sessionDescription);
        return intent;
    }

    public void register(SipProfile localProfile, int expiryTime, SipRegistrationListener listener) throws SipException {
        try {
            checkSipServiceConnection();
            ISipSession session = this.mSipService.createSession(localProfile, createRelay(listener, localProfile.getUriString()), this.mContext.getOpPackageName());
            if (session == null) {
                throw new SipException("SipService.createSession() returns null");
            }
            session.register(expiryTime);
        } catch (RemoteException e) {
            throw new SipException("register()", e);
        }
    }

    public void unregister(SipProfile localProfile, SipRegistrationListener listener) throws SipException {
        try {
            checkSipServiceConnection();
            ISipSession session = this.mSipService.createSession(localProfile, createRelay(listener, localProfile.getUriString()), this.mContext.getOpPackageName());
            if (session == null) {
                throw new SipException("SipService.createSession() returns null");
            }
            session.unregister();
        } catch (RemoteException e) {
            throw new SipException("unregister()", e);
        }
    }

    public SipSession getSessionFor(Intent incomingCallIntent) throws SipException {
        try {
            checkSipServiceConnection();
            ISipSession s = this.mSipService.getPendingSession(getCallId(incomingCallIntent), this.mContext.getOpPackageName());
            if (s == null) {
                return null;
            }
            return new SipSession(s);
        } catch (RemoteException e) {
            throw new SipException("getSessionFor()", e);
        }
    }

    private static ISipSessionListener createRelay(SipRegistrationListener listener, String uri) {
        return listener == null ? null : new ListenerRelay(listener, uri);
    }

    public SipSession createSipSession(SipProfile localProfile, SipSession.Listener listener) throws SipException {
        try {
            checkSipServiceConnection();
            ISipSession s = this.mSipService.createSession(localProfile, null, this.mContext.getOpPackageName());
            if (s != null) {
                return new SipSession(s, listener);
            }
            throw new SipException("Failed to create SipSession; network unavailable?");
        } catch (RemoteException e) {
            throw new SipException("createSipSession()", e);
        }
    }

    public SipProfile[] getListOfProfiles() throws SipException {
        try {
            checkSipServiceConnection();
            return this.mSipService.getListOfProfiles(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            return new SipProfile[0];
        }
    }
}
