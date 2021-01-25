package android.net.sip;

import android.content.Context;
import android.media.AudioManager;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.sip.SimpleSessionDescription;
import android.net.sip.SipSession;
import android.net.wifi.WifiManager;
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
    private WifiManager.WifiLock mWifiHighPerfLock;
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
        if (listener != null && callbackImmediately) {
            try {
                if (this.mErrorCode != 0) {
                    listener.onError(this, this.mErrorCode, this.mErrorMessage);
                } else if (!this.mInCall) {
                    int state = getState();
                    if (state == 0) {
                        listener.onReadyToCall(this);
                    } else if (state == 3) {
                        listener.onRinging(this, getPeerProfile());
                    } else if (state == 5) {
                        listener.onCalling(this);
                    } else if (state == 6) {
                        listener.onRingingBack(this);
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        SipProfile peerProfile;
        synchronized (this) {
            peerProfile = this.mSipSession == null ? null : this.mSipSession.getPeerProfile();
        }
        return peerProfile;
    }

    public int getState() {
        synchronized (this) {
            if (this.mSipSession == null) {
                return 0;
            }
            return this.mSipSession.getState();
        }
    }

    public SipSession getSipSession() {
        SipSession sipSession;
        synchronized (this) {
            sipSession = this.mSipSession;
        }
        return sipSession;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        }
    }

    private SipSession.Listener createListener() {
        return new SipSession.Listener() {
            /* class android.net.sip.SipAudioCall.AnonymousClass1 */

            @Override // android.net.sip.SipSession.Listener
            public void onCalling(SipSession session) {
                SipAudioCall sipAudioCall = SipAudioCall.this;
                sipAudioCall.log("onCalling: session=" + session);
                Listener listener = SipAudioCall.this.mListener;
                if (listener != null) {
                    try {
                        listener.onCalling(SipAudioCall.this);
                    } catch (Throwable t) {
                        SipAudioCall.this.loge("onCalling():", t);
                    }
                }
            }

            @Override // android.net.sip.SipSession.Listener
            public void onRingingBack(SipSession session) {
                SipAudioCall sipAudioCall = SipAudioCall.this;
                sipAudioCall.log("onRingingBackk: " + session);
                Listener listener = SipAudioCall.this.mListener;
                if (listener != null) {
                    try {
                        listener.onRingingBack(SipAudioCall.this);
                    } catch (Throwable t) {
                        SipAudioCall.this.loge("onRingingBack():", t);
                    }
                }
            }

            @Override // android.net.sip.SipSession.Listener
            public void onRinging(SipSession session, SipProfile peerProfile, String sessionDescription) {
                synchronized (SipAudioCall.this) {
                    if (SipAudioCall.this.mSipSession == null || !SipAudioCall.this.mInCall || !session.getCallId().equals(SipAudioCall.this.mSipSession.getCallId())) {
                        session.endCall();
                        return;
                    }
                    try {
                        SipAudioCall.this.mSipSession.answerCall(SipAudioCall.this.createAnswer(sessionDescription).encode(), 5);
                    } catch (Throwable e) {
                        SipAudioCall.this.loge("onRinging():", e);
                        session.endCall();
                    }
                }
            }

            @Override // android.net.sip.SipSession.Listener
            public void onCallEstablished(SipSession session, String sessionDescription) {
                SipAudioCall.this.mPeerSd = sessionDescription;
                SipAudioCall sipAudioCall = SipAudioCall.this;
                sipAudioCall.log("onCallEstablished(): " + SipAudioCall.this.mPeerSd);
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
                } else {
                    SipAudioCall.this.transferToNewSession();
                }
            }

            @Override // android.net.sip.SipSession.Listener
            public void onCallEnded(SipSession session) {
                SipAudioCall sipAudioCall = SipAudioCall.this;
                sipAudioCall.log("onCallEnded: " + session + " mSipSession:" + SipAudioCall.this.mSipSession);
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

            @Override // android.net.sip.SipSession.Listener
            public void onCallBusy(SipSession session) {
                SipAudioCall sipAudioCall = SipAudioCall.this;
                sipAudioCall.log("onCallBusy: " + session);
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

            @Override // android.net.sip.SipSession.Listener
            public void onCallChangeFailed(SipSession session, int errorCode, String message) {
                SipAudioCall sipAudioCall = SipAudioCall.this;
                sipAudioCall.log("onCallChangedFailed: " + message);
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

            @Override // android.net.sip.SipSession.Listener
            public void onError(SipSession session, int errorCode, String message) {
                SipAudioCall.this.onError(errorCode, message);
            }

            @Override // android.net.sip.SipSession.Listener
            public void onRegistering(SipSession session) {
            }

            @Override // android.net.sip.SipSession.Listener
            public void onRegistrationTimeout(SipSession session) {
            }

            @Override // android.net.sip.SipSession.Listener
            public void onRegistrationFailed(SipSession session, int errorCode, String message) {
            }

            @Override // android.net.sip.SipSession.Listener
            public void onRegistrationDone(SipSession session, int duration) {
            }

            @Override // android.net.sip.SipSession.Listener
            public void onCallTransferring(SipSession newSession, String sessionDescription) {
                SipAudioCall sipAudioCall = SipAudioCall.this;
                sipAudioCall.log("onCallTransferring: mSipSession=" + SipAudioCall.this.mSipSession + " newSession=" + newSession);
                SipAudioCall.this.mTransferringSession = newSession;
                if (sessionDescription == null) {
                    try {
                        newSession.makeCall(newSession.getPeerProfile(), SipAudioCall.this.createOffer().encode(), SipAudioCall.TRANSFER_TIMEOUT);
                    } catch (Throwable e) {
                        SipAudioCall.this.loge("onCallTransferring()", e);
                        newSession.endCall();
                    }
                } else {
                    newSession.answerCall(SipAudioCall.this.createAnswer(sessionDescription).encode(), 5);
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x003b, code lost:
        if (isInCall() == false) goto L_0x003d;
     */
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
            if (!this.mHold) {
                if (this.mSipSession != null) {
                    this.mSipSession.changeCall(createHoldOffer().encode(), timeout);
                    this.mHold = true;
                    setAudioGroupMode();
                    return;
                }
                loge("holdCall:");
                throw new SipException("Not in a call to hold call");
            }
        }
    }

    public void answerCall(int timeout) throws SipException {
        log("answerCall: mSipSession" + this.mSipSession + " timeout=" + timeout);
        synchronized (this) {
            if (this.mSipSession != null) {
                try {
                    this.mAudioStream = new AudioStream(InetAddress.getByName(getLocalIp()));
                    this.mSipSession.answerCall(createAnswer(this.mPeerSd).encode(), timeout);
                } catch (IOException e) {
                    loge("answerCall:", e);
                    throw new SipException("answerCall()", e);
                }
            } else {
                throw new SipException("No call to answer");
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
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private SimpleSessionDescription createOffer() {
        SimpleSessionDescription offer = new SimpleSessionDescription(this.mSessionId, getLocalIp());
        AudioCodec.getCodecs();
        SimpleSessionDescription.Media media = offer.newMedia("audio", this.mAudioStream.getLocalPort(), 1, "RTP/AVP");
        AudioCodec[] codecs = AudioCodec.getCodecs();
        for (AudioCodec codec : codecs) {
            media.setRtpPayload(codec.type, codec.rtpmap, codec.fmtp);
        }
        media.setRtpPayload(127, "telephone-event/8000", "0-15");
        log("createOffer: offer=" + offer);
        return offer;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private SimpleSessionDescription createAnswer(String offerSd) {
        if (TextUtils.isEmpty(offerSd)) {
            return createOffer();
        }
        SimpleSessionDescription offer = new SimpleSessionDescription(offerSd);
        SimpleSessionDescription answer = new SimpleSessionDescription(this.mSessionId, getLocalIp());
        SimpleSessionDescription.Media[] media = offer.getMedia();
        AudioCodec codec = null;
        for (SimpleSessionDescription.Media media2 : media) {
            if (codec == null && media2.getPort() > 0 && "audio".equals(media2.getType()) && "RTP/AVP".equals(media2.getProtocol())) {
                int[] rtpPayloadTypes = media2.getRtpPayloadTypes();
                int length = rtpPayloadTypes.length;
                AudioCodec codec2 = codec;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        codec = codec2;
                        break;
                    }
                    int type = rtpPayloadTypes[i];
                    codec2 = AudioCodec.getCodec(type, media2.getRtpmap(type), media2.getFmtp(type));
                    if (codec2 != null) {
                        codec = codec2;
                        break;
                    }
                    i++;
                }
                if (codec != null) {
                    SimpleSessionDescription.Media reply = answer.newMedia("audio", this.mAudioStream.getLocalPort(), 1, "RTP/AVP");
                    reply.setRtpPayload(codec.type, codec.rtpmap, codec.fmtp);
                    int[] rtpPayloadTypes2 = media2.getRtpPayloadTypes();
                    for (int type2 : rtpPayloadTypes2) {
                        String rtpmap = media2.getRtpmap(type2);
                        if (!(type2 == codec.type || rtpmap == null || !rtpmap.startsWith("telephone-event"))) {
                            reply.setRtpPayload(type2, rtpmap, media2.getFmtp(type2));
                        }
                    }
                    if (media2.getAttribute("recvonly") != null) {
                        answer.setAttribute("sendonly", "");
                    } else if (media2.getAttribute("sendonly") != null) {
                        answer.setAttribute("recvonly", "");
                    } else if (offer.getAttribute("recvonly") != null) {
                        answer.setAttribute("sendonly", "");
                    } else if (offer.getAttribute("sendonly") != null) {
                        answer.setAttribute("recvonly", "");
                    }
                }
            }
            SimpleSessionDescription.Media reply2 = answer.newMedia(media2.getType(), 0, 1, media2.getProtocol());
            for (String format : media2.getFormats()) {
                reply2.setFormat(format, null);
            }
        }
        if (codec != null) {
            log("createAnswer: answer=" + answer);
            return answer;
        }
        loge("createAnswer: no suitable codes");
        throw new IllegalStateException("Reject SDP: no suitable codecs");
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
        SimpleSessionDescription.Media media = offer.newMedia("audio", this.mAudioStream.getLocalPort(), 1, "RTP/AVP");
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
        if (this.mWm.getConnectionInfo().getBSSID() == null) {
            return DONT_RELEASE_SOCKET;
        }
        return true;
    }

    public void toggleMute() {
        synchronized (this) {
            this.mMuted = !this.mMuted ? true : DONT_RELEASE_SOCKET;
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

    public AudioGroup getAudioGroup() {
        synchronized (this) {
            if (this.mAudioGroup != null) {
                return this.mAudioGroup;
            }
            return this.mAudioStream == null ? null : this.mAudioStream.getGroup();
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

    /* JADX INFO: Multiple debug info for r4v2 int: [D('address' java.lang.String), D('codec' android.net.rtp.AudioCodec)] */
    private synchronized void startAudioInternal() throws UnknownHostException {
        loge("startAudioInternal: mPeerSd=" + this.mPeerSd);
        if (this.mPeerSd != null) {
            stopCall(DONT_RELEASE_SOCKET);
            this.mInCall = true;
            SimpleSessionDescription offer = new SimpleSessionDescription(this.mPeerSd);
            AudioStream stream = this.mAudioStream;
            SimpleSessionDescription.Media[] media = offer.getMedia();
            int length = media.length;
            AudioCodec codec = null;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                SimpleSessionDescription.Media media2 = media[i];
                if (codec == null && media2.getPort() > 0 && "audio".equals(media2.getType()) && "RTP/AVP".equals(media2.getProtocol())) {
                    int[] rtpPayloadTypes = media2.getRtpPayloadTypes();
                    int length2 = rtpPayloadTypes.length;
                    AudioCodec codec2 = codec;
                    int i2 = 0;
                    while (true) {
                        if (i2 >= length2) {
                            codec = codec2;
                            break;
                        }
                        int type = rtpPayloadTypes[i2];
                        codec2 = AudioCodec.getCodec(type, media2.getRtpmap(type), media2.getFmtp(type));
                        if (codec2 != null) {
                            codec = codec2;
                            break;
                        }
                        i2++;
                    }
                    if (codec != null) {
                        String address = media2.getAddress();
                        if (address == null) {
                            address = offer.getAddress();
                        }
                        stream.associate(InetAddress.getByName(address), media2.getPort());
                        stream.setDtmfType(-1);
                        stream.setCodec(codec);
                        int[] rtpPayloadTypes2 = media2.getRtpPayloadTypes();
                        for (int type2 : rtpPayloadTypes2) {
                            String rtpmap = media2.getRtpmap(type2);
                            if (!(type2 == codec.type || rtpmap == null || !rtpmap.startsWith("telephone-event"))) {
                                stream.setDtmfType(type2);
                            }
                        }
                        if (this.mHold) {
                            stream.setMode(0);
                        } else if (media2.getAttribute("recvonly") != null) {
                            stream.setMode(1);
                        } else if (media2.getAttribute("sendonly") != null) {
                            stream.setMode(2);
                        } else if (offer.getAttribute("recvonly") != null) {
                            stream.setMode(1);
                        } else if (offer.getAttribute("sendonly") != null) {
                            stream.setMode(2);
                        } else {
                            stream.setMode(0);
                        }
                    }
                }
                i++;
            }
            if (codec != null) {
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
            } else {
                throw new IllegalStateException("Reject SDP: no suitable codecs");
            }
        } else {
            throw new IllegalStateException("mPeerSd = null");
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
        AudioStream audioStream = this.mAudioStream;
        if (audioStream != null) {
            audioStream.join(null);
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String s, Throwable t) {
        Rlog.e(LOG_TAG, s, t);
    }
}
