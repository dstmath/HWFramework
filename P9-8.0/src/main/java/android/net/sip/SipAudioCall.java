package android.net.sip;

import android.content.Context;
import android.media.AudioManager;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.sip.SimpleSessionDescription.Media;
import android.net.sip.SipSession.State;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Message;
import android.telephony.Rlog;
import android.text.TextUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SipAudioCall {
    private static final boolean DBG = true;
    private static final boolean DONT_RELEASE_SOCKET = false;
    private static final String LOG_TAG = SipAudioCall.class.getSimpleName();
    private static final boolean RELEASE_SOCKET = true;
    private static final int SESSION_TIMEOUT = 5;
    private static final int TRANSFER_TIMEOUT = 15;
    private AudioGroup mAudioGroup;
    private AudioStream mAudioStream;
    private Context mContext;
    private int mErrorCode = 0;
    private String mErrorMessage;
    private boolean mHold = DONT_RELEASE_SOCKET;
    private boolean mInCall = DONT_RELEASE_SOCKET;
    private Listener mListener;
    private SipProfile mLocalProfile;
    private boolean mMuted = DONT_RELEASE_SOCKET;
    private String mPeerSd;
    private long mSessionId = System.currentTimeMillis();
    private SipSession mSipSession;
    private SipSession mTransferringSession;
    private WifiLock mWifiHighPerfLock;
    private WifiManager mWm;

    public static class Listener {
        public void onReadyToCall(SipAudioCall call) {
            onChanged(call);
        }

        public void onCalling(SipAudioCall call) {
            onChanged(call);
        }

        public void onRinging(SipAudioCall call, SipProfile caller) {
            onChanged(call);
        }

        public void onRingingBack(SipAudioCall call) {
            onChanged(call);
        }

        public void onCallEstablished(SipAudioCall call) {
            onChanged(call);
        }

        public void onCallEnded(SipAudioCall call) {
            onChanged(call);
        }

        public void onCallBusy(SipAudioCall call) {
            onChanged(call);
        }

        public void onCallHeld(SipAudioCall call) {
            onChanged(call);
        }

        public void onError(SipAudioCall call, int errorCode, String errorMessage) {
        }

        public void onChanged(SipAudioCall call) {
        }
    }

    public SipAudioCall(Context context, SipProfile localProfile) {
        this.mContext = context;
        this.mLocalProfile = localProfile;
        this.mWm = (WifiManager) context.getSystemService("wifi");
    }

    public void setListener(Listener listener) {
        setListener(listener, DONT_RELEASE_SOCKET);
    }

    public void setListener(Listener listener, boolean callbackImmediately) {
        this.mListener = listener;
        if (listener != null && (callbackImmediately ^ 1) == 0) {
            try {
                if (this.mErrorCode != 0) {
                    listener.onError(this, this.mErrorCode, this.mErrorMessage);
                } else if (!this.mInCall) {
                    switch (getState()) {
                        case 0:
                            listener.onReadyToCall(this);
                            return;
                        case 3:
                            listener.onRinging(this, getPeerProfile());
                            return;
                        case 5:
                            listener.onCalling(this);
                            return;
                        case State.OUTGOING_CALL_RING_BACK /*6*/:
                            listener.onRingingBack(this);
                            return;
                        default:
                            return;
                    }
                } else if (this.mHold) {
                    listener.onCallHeld(this);
                } else {
                    listener.onCallEstablished(this);
                }
            } catch (Throwable t) {
                loge("setListener()", t);
            }
        }
    }

    public boolean isInCall() {
        boolean z;
        synchronized (this) {
            z = this.mInCall;
        }
        return z;
    }

    public boolean isOnHold() {
        boolean z;
        synchronized (this) {
            z = this.mHold;
        }
        return z;
    }

    public void close() {
        close(true);
    }

    private synchronized void close(boolean closeRtp) {
        if (closeRtp) {
            stopCall(true);
        }
        this.mInCall = DONT_RELEASE_SOCKET;
        this.mHold = DONT_RELEASE_SOCKET;
        this.mSessionId = System.currentTimeMillis();
        this.mErrorCode = 0;
        this.mErrorMessage = null;
        if (this.mSipSession != null) {
            this.mSipSession.setListener(null);
            this.mSipSession = null;
        }
    }

    public SipProfile getLocalProfile() {
        SipProfile sipProfile;
        synchronized (this) {
            sipProfile = this.mLocalProfile;
        }
        return sipProfile;
    }

    public SipProfile getPeerProfile() {
        SipProfile sipProfile = null;
        synchronized (this) {
            if (this.mSipSession != null) {
                sipProfile = this.mSipSession.getPeerProfile();
            }
        }
        return sipProfile;
    }

    public int getState() {
        synchronized (this) {
            if (this.mSipSession == null) {
                return 0;
            }
            int state = this.mSipSession.getState();
            return state;
        }
    }

    public SipSession getSipSession() {
        SipSession sipSession;
        synchronized (this) {
            sipSession = this.mSipSession;
        }
        return sipSession;
    }

    private synchronized void transferToNewSession() {
        if (this.mTransferringSession != null) {
            SipSession origin = this.mSipSession;
            this.mSipSession = this.mTransferringSession;
            this.mTransferringSession = null;
            if (this.mAudioStream != null) {
                this.mAudioStream.join(null);
            } else {
                try {
                    this.mAudioStream = new AudioStream(InetAddress.getByName(getLocalIp()));
                } catch (Throwable t) {
                    loge("transferToNewSession():", t);
                }
            }
            if (origin != null) {
                origin.endCall();
            }
            startAudio();
            return;
        }
        return;
    }

    private android.net.sip.SipSession.Listener createListener() {
        return new android.net.sip.SipSession.Listener() {
            public void onCalling(SipSession session) {
                SipAudioCall.this.log("onCalling: session=" + session);
                Listener listener = SipAudioCall.this.mListener;
                if (listener != null) {
                    try {
                        listener.onCalling(SipAudioCall.this);
                    } catch (Throwable t) {
                        SipAudioCall.this.loge("onCalling():", t);
                    }
                }
            }

            public void onRingingBack(SipSession session) {
                SipAudioCall.this.log("onRingingBackk: " + session);
                Listener listener = SipAudioCall.this.mListener;
                if (listener != null) {
                    try {
                        listener.onRingingBack(SipAudioCall.this);
                    } catch (Throwable t) {
                        SipAudioCall.this.loge("onRingingBack():", t);
                    }
                }
            }

            public void onRinging(SipSession session, SipProfile peerProfile, String sessionDescription) {
                synchronized (SipAudioCall.this) {
                    if (SipAudioCall.this.mSipSession != null && (SipAudioCall.this.mInCall ^ 1) == 0 && (session.getCallId().equals(SipAudioCall.this.mSipSession.getCallId()) ^ 1) == 0) {
                        try {
                            SipAudioCall.this.mSipSession.answerCall(SipAudioCall.this.createAnswer(sessionDescription).encode(), 5);
                        } catch (Throwable e) {
                            SipAudioCall.this.loge("onRinging():", e);
                            session.endCall();
                        }
                    } else {
                        session.endCall();
                        return;
                    }
                }
            }

            public void onCallEstablished(SipSession session, String sessionDescription) {
                SipAudioCall.this.mPeerSd = sessionDescription;
                SipAudioCall.this.log("onCallEstablished(): " + SipAudioCall.this.mPeerSd);
                if (SipAudioCall.this.mTransferringSession == null || session != SipAudioCall.this.mTransferringSession) {
                    Listener listener = SipAudioCall.this.mListener;
                    if (listener != null) {
                        try {
                            if (SipAudioCall.this.mHold) {
                                listener.onCallHeld(SipAudioCall.this);
                            } else {
                                listener.onCallEstablished(SipAudioCall.this);
                            }
                        } catch (Throwable t) {
                            SipAudioCall.this.loge("onCallEstablished(): ", t);
                        }
                    }
                    return;
                }
                SipAudioCall.this.transferToNewSession();
            }

            public void onCallEnded(SipSession session) {
                SipAudioCall.this.log("onCallEnded: " + session + " mSipSession:" + SipAudioCall.this.mSipSession);
                if (session == SipAudioCall.this.mTransferringSession) {
                    SipAudioCall.this.mTransferringSession = null;
                } else if (SipAudioCall.this.mTransferringSession == null && session == SipAudioCall.this.mSipSession) {
                    Listener listener = SipAudioCall.this.mListener;
                    if (listener != null) {
                        try {
                            listener.onCallEnded(SipAudioCall.this);
                        } catch (Throwable t) {
                            SipAudioCall.this.loge("onCallEnded(): ", t);
                        }
                    }
                    SipAudioCall.this.close();
                }
            }

            public void onCallBusy(SipSession session) {
                SipAudioCall.this.log("onCallBusy: " + session);
                Listener listener = SipAudioCall.this.mListener;
                if (listener != null) {
                    try {
                        listener.onCallBusy(SipAudioCall.this);
                    } catch (Throwable t) {
                        SipAudioCall.this.loge("onCallBusy(): ", t);
                    }
                }
                SipAudioCall.this.close(SipAudioCall.DONT_RELEASE_SOCKET);
            }

            public void onCallChangeFailed(SipSession session, int errorCode, String message) {
                SipAudioCall.this.log("onCallChangedFailed: " + message);
                SipAudioCall.this.mErrorCode = errorCode;
                SipAudioCall.this.mErrorMessage = message;
                Listener listener = SipAudioCall.this.mListener;
                if (listener != null) {
                    try {
                        listener.onError(SipAudioCall.this, SipAudioCall.this.mErrorCode, message);
                    } catch (Throwable t) {
                        SipAudioCall.this.loge("onCallBusy():", t);
                    }
                }
            }

            public void onError(SipSession session, int errorCode, String message) {
                SipAudioCall.this.onError(errorCode, message);
            }

            public void onRegistering(SipSession session) {
            }

            public void onRegistrationTimeout(SipSession session) {
            }

            public void onRegistrationFailed(SipSession session, int errorCode, String message) {
            }

            public void onRegistrationDone(SipSession session, int duration) {
            }

            public void onCallTransferring(SipSession newSession, String sessionDescription) {
                SipAudioCall.this.log("onCallTransferring: mSipSession=" + SipAudioCall.this.mSipSession + " newSession=" + newSession);
                SipAudioCall.this.mTransferringSession = newSession;
                if (sessionDescription == null) {
                    try {
                        newSession.makeCall(newSession.getPeerProfile(), SipAudioCall.this.createOffer().encode(), SipAudioCall.TRANSFER_TIMEOUT);
                        return;
                    } catch (Throwable e) {
                        SipAudioCall.this.loge("onCallTransferring()", e);
                        newSession.endCall();
                        return;
                    }
                }
                newSession.answerCall(SipAudioCall.this.createAnswer(sessionDescription).encode(), 5);
            }
        };
    }

    /* JADX WARNING: Missing block: B:9:0x003c, code:
            if ((isInCall() ^ 1) != 0) goto L_0x003e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onError(int errorCode, String message) {
        log("onError: " + SipErrorCode.toString(errorCode) + ": " + message);
        this.mErrorCode = errorCode;
        this.mErrorMessage = message;
        Listener listener = this.mListener;
        if (listener != null) {
            try {
                listener.onError(this, errorCode, message);
            } catch (Throwable t) {
                loge("onError():", t);
            }
        }
        synchronized (this) {
            if (errorCode != -10) {
            }
            close(true);
        }
    }

    public void attachCall(SipSession session, String sessionDescription) throws SipException {
        if (SipManager.isVoipSupported(this.mContext)) {
            synchronized (this) {
                this.mSipSession = session;
                this.mPeerSd = sessionDescription;
                log("attachCall(): " + this.mPeerSd);
                try {
                    session.setListener(createListener());
                } catch (Throwable e) {
                    loge("attachCall()", e);
                    throwSipException(e);
                }
            }
            return;
        }
        throw new SipException("VOIP API is not supported");
    }

    public void makeCall(SipProfile peerProfile, SipSession sipSession, int timeout) throws SipException {
        log("makeCall: " + peerProfile + " session=" + sipSession + " timeout=" + timeout);
        if (SipManager.isVoipSupported(this.mContext)) {
            synchronized (this) {
                this.mSipSession = sipSession;
                try {
                    this.mAudioStream = new AudioStream(InetAddress.getByName(getLocalIp()));
                    sipSession.setListener(createListener());
                    sipSession.makeCall(peerProfile, createOffer().encode(), timeout);
                } catch (IOException e) {
                    loge("makeCall:", e);
                    throw new SipException("makeCall()", e);
                }
            }
            return;
        }
        throw new SipException("VOIP API is not supported");
    }

    public void endCall() throws SipException {
        log("endCall: mSipSession" + this.mSipSession);
        synchronized (this) {
            stopCall(true);
            this.mInCall = DONT_RELEASE_SOCKET;
            if (this.mSipSession != null) {
                this.mSipSession.endCall();
            }
        }
    }

    public void holdCall(int timeout) throws SipException {
        log("holdCall: mSipSession" + this.mSipSession + " timeout=" + timeout);
        synchronized (this) {
            if (this.mHold) {
            } else if (this.mSipSession == null) {
                loge("holdCall:");
                throw new SipException("Not in a call to hold call");
            } else {
                this.mSipSession.changeCall(createHoldOffer().encode(), timeout);
                this.mHold = true;
                setAudioGroupMode();
            }
        }
    }

    public void answerCall(int timeout) throws SipException {
        log("answerCall: mSipSession" + this.mSipSession + " timeout=" + timeout);
        synchronized (this) {
            if (this.mSipSession == null) {
                throw new SipException("No call to answer");
            }
            try {
                this.mAudioStream = new AudioStream(InetAddress.getByName(getLocalIp()));
                this.mSipSession.answerCall(createAnswer(this.mPeerSd).encode(), timeout);
            } catch (IOException e) {
                loge("answerCall:", e);
                throw new SipException("answerCall()", e);
            }
        }
    }

    public void continueCall(int timeout) throws SipException {
        log("continueCall: mSipSession" + this.mSipSession + " timeout=" + timeout);
        synchronized (this) {
            if (this.mHold) {
                this.mSipSession.changeCall(createContinueOffer().encode(), timeout);
                this.mHold = DONT_RELEASE_SOCKET;
                setAudioGroupMode();
                return;
            }
        }
    }

    private SimpleSessionDescription createOffer() {
        SimpleSessionDescription offer = new SimpleSessionDescription(this.mSessionId, getLocalIp());
        AudioCodec[] codecs = AudioCodec.getCodecs();
        Media media = offer.newMedia("audio", this.mAudioStream.getLocalPort(), 1, "RTP/AVP");
        for (AudioCodec codec : AudioCodec.getCodecs()) {
            media.setRtpPayload(codec.type, codec.rtpmap, codec.fmtp);
        }
        media.setRtpPayload(127, "telephone-event/8000", "0-15");
        log("createOffer: offer=" + offer);
        return offer;
    }

    private SimpleSessionDescription createAnswer(String offerSd) {
        if (TextUtils.isEmpty(offerSd)) {
            return createOffer();
        }
        SimpleSessionDescription offer = new SimpleSessionDescription(offerSd);
        SimpleSessionDescription answer = new SimpleSessionDescription(this.mSessionId, getLocalIp());
        AudioCodec codec = null;
        for (Media media : offer.getMedia()) {
            Media reply;
            if (codec == null && media.getPort() > 0 && "audio".equals(media.getType()) && "RTP/AVP".equals(media.getProtocol())) {
                for (int type : media.getRtpPayloadTypes()) {
                    codec = AudioCodec.getCodec(type, media.getRtpmap(type), media.getFmtp(type));
                    if (codec != null) {
                        break;
                    }
                }
                if (codec != null) {
                    reply = answer.newMedia("audio", this.mAudioStream.getLocalPort(), 1, "RTP/AVP");
                    reply.setRtpPayload(codec.type, codec.rtpmap, codec.fmtp);
                    for (int type2 : media.getRtpPayloadTypes()) {
                        String rtpmap = media.getRtpmap(type2);
                        if (!(type2 == codec.type || rtpmap == null || !rtpmap.startsWith("telephone-event"))) {
                            reply.setRtpPayload(type2, rtpmap, media.getFmtp(type2));
                        }
                    }
                    if (media.getAttribute("recvonly") != null) {
                        answer.setAttribute("sendonly", "");
                    } else if (media.getAttribute("sendonly") != null) {
                        answer.setAttribute("recvonly", "");
                    } else if (offer.getAttribute("recvonly") != null) {
                        answer.setAttribute("sendonly", "");
                    } else if (offer.getAttribute("sendonly") != null) {
                        answer.setAttribute("recvonly", "");
                    }
                }
            }
            reply = answer.newMedia(media.getType(), 0, 1, media.getProtocol());
            for (String format : media.getFormats()) {
                reply.setFormat(format, null);
            }
        }
        if (codec == null) {
            loge("createAnswer: no suitable codes");
            throw new IllegalStateException("Reject SDP: no suitable codecs");
        }
        log("createAnswer: answer=" + answer);
        return answer;
    }

    private SimpleSessionDescription createHoldOffer() {
        SimpleSessionDescription offer = createContinueOffer();
        offer.setAttribute("sendonly", "");
        log("createHoldOffer: offer=" + offer);
        return offer;
    }

    private SimpleSessionDescription createContinueOffer() {
        log("createContinueOffer");
        SimpleSessionDescription offer = new SimpleSessionDescription(this.mSessionId, getLocalIp());
        Media media = offer.newMedia("audio", this.mAudioStream.getLocalPort(), 1, "RTP/AVP");
        AudioCodec codec = this.mAudioStream.getCodec();
        media.setRtpPayload(codec.type, codec.rtpmap, codec.fmtp);
        int dtmfType = this.mAudioStream.getDtmfType();
        if (dtmfType != -1) {
            media.setRtpPayload(dtmfType, "telephone-event/8000", "0-15");
        }
        return offer;
    }

    private void grabWifiHighPerfLock() {
        if (this.mWifiHighPerfLock == null) {
            log("grabWifiHighPerfLock:");
            this.mWifiHighPerfLock = ((WifiManager) this.mContext.getSystemService("wifi")).createWifiLock(3, LOG_TAG);
            this.mWifiHighPerfLock.acquire();
        }
    }

    private void releaseWifiHighPerfLock() {
        if (this.mWifiHighPerfLock != null) {
            log("releaseWifiHighPerfLock:");
            this.mWifiHighPerfLock.release();
            this.mWifiHighPerfLock = null;
        }
    }

    private boolean isWifiOn() {
        return this.mWm.getConnectionInfo().getBSSID() == null ? DONT_RELEASE_SOCKET : true;
    }

    public void toggleMute() {
        synchronized (this) {
            this.mMuted ^= 1;
            setAudioGroupMode();
        }
    }

    public boolean isMuted() {
        boolean z;
        synchronized (this) {
            z = this.mMuted;
        }
        return z;
    }

    public void setSpeakerMode(boolean speakerMode) {
        synchronized (this) {
            ((AudioManager) this.mContext.getSystemService("audio")).setSpeakerphoneOn(speakerMode);
            setAudioGroupMode();
        }
    }

    private boolean isSpeakerOn() {
        return ((AudioManager) this.mContext.getSystemService("audio")).isSpeakerphoneOn();
    }

    public void sendDtmf(int code) {
        sendDtmf(code, null);
    }

    public void sendDtmf(int code, Message result) {
        synchronized (this) {
            AudioGroup audioGroup = getAudioGroup();
            if (!(audioGroup == null || this.mSipSession == null || 8 != getState())) {
                log("sendDtmf: code=" + code + " result=" + result);
                audioGroup.sendDtmf(code);
            }
            if (result != null) {
                result.sendToTarget();
            }
        }
    }

    public AudioStream getAudioStream() {
        AudioStream audioStream;
        synchronized (this) {
            audioStream = this.mAudioStream;
        }
        return audioStream;
    }

    /* JADX WARNING: Missing block: B:12:0x000f, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public AudioGroup getAudioGroup() {
        AudioGroup audioGroup = null;
        synchronized (this) {
            if (this.mAudioGroup != null) {
                audioGroup = this.mAudioGroup;
                return audioGroup;
            } else if (this.mAudioStream != null) {
                audioGroup = this.mAudioStream.getGroup();
            }
        }
    }

    public void setAudioGroup(AudioGroup group) {
        synchronized (this) {
            log("setAudioGroup: group=" + group);
            if (!(this.mAudioStream == null || this.mAudioStream.getGroup() == null)) {
                this.mAudioStream.join(group);
            }
            this.mAudioGroup = group;
        }
    }

    public void startAudio() {
        try {
            startAudioInternal();
        } catch (UnknownHostException e) {
            onError(-7, e.getMessage());
        } catch (Throwable e2) {
            onError(-4, e2.getMessage());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:59:0x012b  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00e0  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void startAudioInternal() throws UnknownHostException {
        loge("startAudioInternal: mPeerSd=" + this.mPeerSd);
        if (this.mPeerSd == null) {
            throw new IllegalStateException("mPeerSd = null");
        }
        stopCall(DONT_RELEASE_SOCKET);
        this.mInCall = true;
        SimpleSessionDescription offer = new SimpleSessionDescription(this.mPeerSd);
        AudioStream stream = this.mAudioStream;
        AudioCodec codec = null;
        for (Media media : offer.getMedia()) {
            if (codec == null && media.getPort() > 0 && "audio".equals(media.getType()) && "RTP/AVP".equals(media.getProtocol())) {
                for (int type : media.getRtpPayloadTypes()) {
                    codec = AudioCodec.getCodec(type, media.getRtpmap(type), media.getFmtp(type));
                    if (codec != null) {
                        break;
                    }
                }
                if (codec != null) {
                    String address = media.getAddress();
                    if (address == null) {
                        address = offer.getAddress();
                    }
                    stream.associate(InetAddress.getByName(address), media.getPort());
                    stream.setDtmfType(-1);
                    stream.setCodec(codec);
                    for (int type2 : media.getRtpPayloadTypes()) {
                        String rtpmap = media.getRtpmap(type2);
                        if (!(type2 == codec.type || rtpmap == null || !rtpmap.startsWith("telephone-event"))) {
                            stream.setDtmfType(type2);
                        }
                    }
                    if (this.mHold) {
                        stream.setMode(0);
                    } else if (media.getAttribute("recvonly") != null) {
                        stream.setMode(1);
                    } else if (media.getAttribute("sendonly") != null) {
                        stream.setMode(2);
                    } else if (offer.getAttribute("recvonly") != null) {
                        stream.setMode(1);
                    } else if (offer.getAttribute("sendonly") != null) {
                        stream.setMode(2);
                    } else {
                        stream.setMode(0);
                    }
                    if (codec != null) {
                        throw new IllegalStateException("Reject SDP: no suitable codecs");
                    }
                    if (isWifiOn()) {
                        grabWifiHighPerfLock();
                    }
                    AudioGroup audioGroup = getAudioGroup();
                    if (!this.mHold) {
                        if (audioGroup == null) {
                            audioGroup = new AudioGroup();
                        }
                        stream.join(audioGroup);
                    }
                    setAudioGroupMode();
                }
            }
        }
        if (codec != null) {
        }
    }

    private void setAudioGroupMode() {
        AudioGroup audioGroup = getAudioGroup();
        log("setAudioGroupMode: audioGroup=" + audioGroup);
        if (audioGroup == null) {
            return;
        }
        if (this.mHold) {
            audioGroup.setMode(0);
        } else if (this.mMuted) {
            audioGroup.setMode(1);
        } else if (isSpeakerOn()) {
            audioGroup.setMode(3);
        } else {
            audioGroup.setMode(2);
        }
    }

    private void stopCall(boolean releaseSocket) {
        log("stopCall: releaseSocket=" + releaseSocket);
        releaseWifiHighPerfLock();
        if (this.mAudioStream != null) {
            this.mAudioStream.join(null);
            if (releaseSocket) {
                this.mAudioStream.release();
                this.mAudioStream = null;
            }
        }
    }

    private String getLocalIp() {
        return this.mSipSession.getLocalIp();
    }

    private void throwSipException(Throwable throwable) throws SipException {
        if (throwable instanceof SipException) {
            throw ((SipException) throwable);
        }
        throw new SipException("", throwable);
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }

    private void loge(String s, Throwable t) {
        Rlog.e(LOG_TAG, s, t);
    }
}
