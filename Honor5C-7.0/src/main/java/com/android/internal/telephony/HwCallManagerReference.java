package com.android.internal.telephony;

import android.content.Context;
import android.media.AudioManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.RegistrantList;
import android.telephony.Rlog;
import com.android.internal.telephony.AbstractCallManager.CallManagerReference;
import com.android.internal.telephony.AbstractCallManager.disconnectCallback;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public final class HwCallManagerReference extends Handler implements CallManagerReference {
    private static final int EVENT_DISCONNECT = 102;
    private static final int EVENT_HUAWEI_BASE = 500;
    private static final int EVENT_HW_BUFFER_SOLICITED = 502;
    private static final int EVENT_HW_BUFFER_UNSOLICITED = 501;
    private static final int EVENT_PHONE_STATE_CHANGED = 101;
    private static final int EVENT_UNSOl_SPEECH_INFO = 103;
    private static final String LOG_TAG = "HwCallManagerReference";
    private static final boolean VDBG = true;
    private static boolean mIsLocalHangupSpeedUp;
    private static final boolean mSupportAdjustSpeechCodec = false;
    private int mActiveSub;
    protected final RegistrantList mActiveSubChangeRegistrants;
    private CallManager mCM;
    protected disconnectCallback mCallbackCallNotifier;
    protected disconnectCallback mCallbackInCallScreen;
    public final RegistrantList mEncryptCallRegistrants;

    public static class HWBuffer {
        public static final int BUFFER_SIZE = 120;
        public static final int BUFLEN_SIZE = 1;
        public static final int EVENT_SIZE = 4;
        public static final String LOG_TAG = "CM_HWBUFFER";
        public byte[] buffer;
        public int event;
        public Throwable exception;
        private boolean isAvailable;
        public byte length;
        public Message reqMsg;

        public HWBuffer() {
            this.length = (byte) 0;
            this.buffer = null;
            this.reqMsg = null;
            this.exception = null;
            this.isAvailable = false;
        }

        public HWBuffer(int event, byte[] buffer) {
            int i = 0;
            this.length = (byte) 0;
            this.buffer = null;
            this.reqMsg = null;
            this.exception = null;
            this.isAvailable = false;
            this.event = event;
            if (buffer != null) {
                i = buffer.length;
            }
            this.length = (byte) i;
            if (this.length > null && BUFFER_SIZE >= this.length) {
                this.buffer = Arrays.copyOf(buffer, buffer.length);
            }
            if (-1 < event) {
                this.isAvailable = HwCallManagerReference.VDBG;
            }
        }

        public boolean isAvail() {
            return this.isAvailable;
        }

        public boolean isSolicited() {
            return this.reqMsg != null ? HwCallManagerReference.VDBG : false;
        }

        public boolean isException() {
            return this.exception != null ? HwCallManagerReference.VDBG : false;
        }

        public byte[] toArray() {
            if (isAvail()) {
                ByteBuffer buf = ByteBuffer.wrap(new byte[(this.length + 5)]);
                if (HuaweiTelephonyConfigs.isHisiPlatform()) {
                    buf.order(ByteOrder.nativeOrder());
                }
                buf.putInt(this.event);
                buf.put(this.length);
                if (this.length > null && BUFFER_SIZE >= this.length) {
                    buf.put(this.buffer);
                }
                return buf.array();
            }
            Rlog.e(LOG_TAG, "this(HWBuffer) is unavailable!!!");
            return new byte[0];
        }

        public String toString() {
            return "HWBuffer>>>{ event:" + this.event + ", length:" + this.length + ", buffer:" + IccUtils.bytesToHexString(this.buffer) + " }.";
        }

        public void sendRespToTarget(Object obj) {
            if (isAvail() && isSolicited() && this.reqMsg.getTarget() != null) {
                this.reqMsg.obj = obj;
                this.reqMsg.sendToTarget();
                return;
            }
            Rlog.v(LOG_TAG, "sendRespToTarget() Failed, HWBuffer not available OR reqMsg not found Target(@Handle)!");
        }

        public static HWBuffer makeHWBuffer(byte[] data) {
            return makeHWBuffer(data, false);
        }

        static HWBuffer makeHWBuffer(byte[] data, boolean isOrder) {
            if (data == null) {
                return null;
            }
            byte[] bArr = null;
            try {
                ByteBuffer byteData = ByteBuffer.wrap(data);
                if (isOrder) {
                    byteData.order(ByteOrder.nativeOrder());
                }
                int _event = byteData.getInt();
                byte _length = byteData.get();
                if (_length > null) {
                    bArr = new byte[_length];
                    byteData.get(bArr, 0, _length);
                }
                return new HWBuffer(_event, bArr);
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "data[] parse execption, please check data format>>>[event(4):buf_len(1):buffer(<120)] !!!");
                HWBuffer hwBuf = new HWBuffer();
                hwBuf.exception = e;
                return hwBuf;
            }
        }

        public static HWBuffer makeHWBuffer(AsyncResult ar) {
            return makeHWBuffer(ar, false);
        }

        static HWBuffer makeHWBuffer(AsyncResult ar, boolean isOrder) {
            HWBuffer hwBuf = makeHWBuffer((byte[]) ar.result, isOrder);
            if (hwBuf == null) {
                hwBuf = new HWBuffer();
            }
            if (hwBuf.isAvail()) {
                if (ar.exception != null) {
                    hwBuf.exception = ar.exception;
                }
                if (ar.userObj != null) {
                    hwBuf.reqMsg = (Message) ar.userObj;
                }
            }
            return hwBuf;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwCallManagerReference.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwCallManagerReference.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwCallManagerReference.<clinit>():void");
    }

    public HwCallManagerReference(CallManager cm) {
        this.mActiveSub = 0;
        this.mActiveSubChangeRegistrants = new RegistrantList();
        this.mEncryptCallRegistrants = new RegistrantList();
        this.mCM = cm;
    }

    public void setInCallScreenDisconnectCallback(disconnectCallback callback) {
        this.mCallbackInCallScreen = callback;
    }

    public void setCallNotifierDisconnectCallback(disconnectCallback callback) {
        this.mCallbackCallNotifier = callback;
    }

    public void inCallScreenDisconnectNotify(AsyncResult r) {
        if (this.mCallbackInCallScreen != null) {
            this.mCallbackInCallScreen.disconnectNotify(r);
        }
    }

    public void calllNotifierDisconnectNotify(AsyncResult r) {
        if (this.mCallbackCallNotifier != null) {
            this.mCallbackCallNotifier.disconnectNotify(r);
        }
    }

    public void disconnectNotify(Message msg) {
        if (mIsLocalHangupSpeedUp) {
            inCallScreenDisconnectNotify((AsyncResult) msg.obj);
            calllNotifierDisconnectNotify((AsyncResult) msg.obj);
        }
    }

    public void registerForEncryptedCall(Handler h, int what, Object obj) {
        this.mEncryptCallRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForEncryptedCall(Handler h) {
        this.mEncryptCallRegistrants.remove(h);
    }

    public void cmdForEncryptedCall(Phone phone, int cmd, byte[] reqData) {
        cmdForEncryptedCall(phone, null, cmd, reqData);
    }

    private void cmdForEncryptedCall(Phone phone, Message reqMsg, int cmd, byte[] reqData) {
        sendHWSolicited(phone, reqMsg, 0, new HWBuffer(cmd, reqData).toArray());
    }

    public void resultForKMCRemoteCmd(Phone phone, int cmd, int reqData) {
        Rlog.d(LOG_TAG, "resultForKMCRemoteCmd() cmd " + cmd + " reqdata " + reqData);
        resultForKMCRemoteCmd(phone, null, cmd, new byte[]{(byte) reqData});
    }

    private void resultForKMCRemoteCmd(Phone phone, Message reqMsg, int cmd, byte[] reqData) {
        sendHWSolicited(phone, reqMsg, 1, new HWBuffer(cmd, reqData).toArray());
    }

    public void setConnEncryptCallByNumber(Phone phone, String number, boolean val) {
        if (phone.getPhoneType() == 2) {
            ((GsmCdmaCallTracker) ((GsmCdmaPhone) phone).getCallTracker()).setConnEncryptCallByNumber(number, val);
        }
    }

    private void processSolicitedHWResponse(AsyncResult ar) {
        HWBuffer hwBuf = HWBuffer.makeHWBuffer(ar, (boolean) VDBG);
        Rlog.v(LOG_TAG, "received SolicitedHWResponse hwBuf:" + hwBuf.toString());
        if (hwBuf.isAvail()) {
            switch (hwBuf.event) {
                case HwVSimUtilsInner.UE_OPERATION_MODE_VOICE_CENTRIC /*0*/:
                case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                    Rlog.d(LOG_TAG, "[Solicited] HW_ENCRYPT_CALL or HW_KMC_REMOTE_COMMUNICATION.");
                    hwBuf.sendRespToTarget(hwBuf.buffer);
                    return;
                default:
                    Rlog.w(LOG_TAG, "CallManager be untreated for event[" + hwBuf.event + "] in processSolicitedHWResponse()! please user handle");
                    hwBuf.sendRespToTarget(hwBuf.buffer);
                    return;
            }
        }
        Rlog.e(LOG_TAG, "processSolicitedHWResponse(ar) is unavailable!!!");
    }

    private void processUnSolicitedHWResponse(AsyncResult ar) {
        HWBuffer hwBuf = HWBuffer.makeHWBuffer(ar, (boolean) VDBG);
        Rlog.v(LOG_TAG, "received UnSolicitedHWResponse hwBuf:" + hwBuf.toString());
        if (hwBuf.isAvail()) {
            switch (hwBuf.event) {
                case HwVSimUtilsInner.UE_OPERATION_MODE_VOICE_CENTRIC /*0*/:
                case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                    Rlog.d(LOG_TAG, "[UnSolicited] HW_ENCRYPT_CALL or HW_KMC_REMOTE_COMMUNICATION.");
                    this.mEncryptCallRegistrants.notifyRegistrants(new AsyncResult(null, hwBuf.buffer, hwBuf.exception));
                    return;
                default:
                    Rlog.w(LOG_TAG, "not found event for processUnSolicitedHWResponse()");
                    return;
            }
        }
        Rlog.e(LOG_TAG, "processUnSolicitedHWResponse(ar) is unavailable!!!");
    }

    private void sendHWSolicited(Phone phone, Message reqMsg, int event, byte[] reqData) {
        if (phone == null) {
            Rlog.w(LOG_TAG, "sendHWSolicited() phone parameter is invalid");
            return;
        }
        phone.sendHWSolicited(obtainMessage(EVENT_HW_BUFFER_SOLICITED, reqMsg), event, reqData);
        Rlog.d(LOG_TAG, "sendHWSolicited completed, phone: " + phone + ", event=" + event);
    }

    public void handleMessage(Message msg) {
        Phone phone;
        switch (msg.what) {
            case EVENT_PHONE_STATE_CHANGED /*101*/:
                Rlog.d(LOG_TAG, " handleMessage (EVENT_PHONE_STATE_CHANGED)");
                onPhoneStateChanged((AsyncResult) msg.obj);
                break;
            case EVENT_DISCONNECT /*102*/:
                Rlog.d(LOG_TAG, " handleMessage (EVENT_DISCONNECT)");
                phone = ((AsyncResult) msg.obj).userObj;
                if (phone != null) {
                    if (phone.getState() == State.IDLE) {
                        phone.setSpeechInfoCodec(1);
                    }
                    int disConnectSub = phone.getSubId();
                    int otherSub = disConnectSub == 0 ? 1 : 0;
                    State otherState = this.mCM.getState(otherSub);
                    Rlog.d(LOG_TAG, "DisConnectSub : " + disConnectSub + ", otherSub : " + otherSub + ", otherState :" + otherState);
                    if (otherState == State.OFFHOOK) {
                        setAudioParameters(getPhone((long) otherSub));
                        break;
                    }
                }
                break;
            case EVENT_UNSOl_SPEECH_INFO /*103*/:
                Rlog.d(LOG_TAG, " handleMessage (EVENT_UNSOl_SPEECH_INFO)");
                phone = ((AsyncResult) msg.obj).userObj;
                if (phone != null) {
                    int subId = phone.getSubId();
                    Rlog.d(LOG_TAG, "subid : " + subId + ", mActiveSub : " + this.mActiveSub);
                    int[] intResult = ((AsyncResult) msg.obj).result;
                    if (intResult != null && intResult.length != 0) {
                        phone.setSpeechInfoCodec(intResult[0]);
                        if (subId == this.mActiveSub) {
                            setAudioParameters(phone);
                            break;
                        }
                    }
                    return;
                }
                break;
            case EVENT_HW_BUFFER_UNSOLICITED /*501*/:
                Rlog.d(LOG_TAG, "handleMessage (EVENT_HW_BUFFER_UNSOLICITED)");
                processUnSolicitedHWResponse(msg.obj);
                break;
            case EVENT_HW_BUFFER_SOLICITED /*502*/:
                Rlog.d(LOG_TAG, "handleMessage (EVENT_HW_BUFFER_SOLICITED)");
                AsyncResult soAr = msg.obj;
                if (HuaweiTelephonyConfigs.isHisiPlatform()) {
                    if (soAr.exception != null) {
                        Rlog.e(LOG_TAG, "encrypted call SOLICITED exception " + soAr.exception);
                        break;
                    } else {
                        Rlog.i(LOG_TAG, "encrypted call SOLICITED res is ok");
                        break;
                    }
                }
                processSolicitedHWResponse(soAr);
                break;
        }
    }

    public void registerForSubscriptionChange(Handler h, int what, Object obj) {
        this.mActiveSubChangeRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSubscriptionChange(Handler h) {
        this.mActiveSubChangeRegistrants.remove(h);
    }

    public void setActiveSubscription(int subscription) {
        Rlog.d(LOG_TAG, "setActiveSubscription existing:" + this.mActiveSub + "new = " + subscription);
        this.mActiveSub = subscription;
        this.mActiveSubChangeRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(this.mActiveSub), null));
    }

    public int getActiveSubscription() {
        Rlog.d(LOG_TAG, "getActiveSubscription  = " + this.mActiveSub);
        return this.mActiveSub;
    }

    public void registerForPhoneStates(Phone phone) {
        phone.registerForHWBuffer(this, EVENT_HW_BUFFER_UNSOLICITED, null);
        if (mSupportAdjustSpeechCodec) {
            phone.registerForPreciseCallStateChanged(this, EVENT_PHONE_STATE_CHANGED, null);
            phone.registerForDisconnect(this, EVENT_DISCONNECT, phone);
            phone.registerForUnsolSpeechInfo(this, EVENT_UNSOl_SPEECH_INFO, phone);
        }
    }

    public void unregisterForPhoneStates(Phone phone) {
        phone.unregisterForHWBuffer(this);
        if (mSupportAdjustSpeechCodec) {
            phone.unregisterForPreciseCallStateChanged(this);
            phone.unregisterForUnsolSpeechInfo(this);
            phone.unregisterForDisconnect(this);
        }
    }

    public void onSwitchToOtherActiveSub(Phone currentPhone) {
        if (!mSupportAdjustSpeechCodec) {
            return;
        }
        if (currentPhone == null) {
            Rlog.d(LOG_TAG, "onSwitchToOtherActiveSub currentPhone is NULL! ");
            return;
        }
        int currentSub = currentPhone.getSubId();
        Rlog.d(LOG_TAG, "onSwitchToOtherActiveSub currentSub = " + currentSub + " mActiveSub = " + this.mActiveSub);
        if (currentSub == this.mActiveSub) {
            State state = this.mCM.getState(this.mActiveSub);
            Rlog.d(LOG_TAG, "onSwitchToOtherActiveSub currentSub = " + currentSub + ", state = " + state);
            if (state == State.OFFHOOK) {
                setAudioParameters(currentPhone);
            }
        }
    }

    private void setAudioParameters(Phone phone) {
        if (phone != null) {
            Context context = phone.getContext();
            String speechInfo = phone.getSpeechInfoCodec();
            Rlog.d(LOG_TAG, "setAudioParameters speechInfo = " + speechInfo);
            if (!speechInfo.equals("")) {
                ((AudioManager) context.getSystemService("audio")).setParameters(speechInfo);
            }
        }
    }

    private void onPhoneStateChanged(AsyncResult r) {
        if (this.mCM.getState(this.mActiveSub) == State.OFFHOOK) {
            setAudioParameters(getPhone((long) this.mActiveSub));
        }
    }

    private Phone getPhone(long subId) {
        for (Phone phone : this.mCM.getAllPhones()) {
            if (((long) phone.getSubId()) == subId && !(phone instanceof ImsPhone)) {
                return phone;
            }
        }
        return null;
    }
}
