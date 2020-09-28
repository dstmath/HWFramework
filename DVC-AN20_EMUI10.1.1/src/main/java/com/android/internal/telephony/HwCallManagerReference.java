package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.AbstractCallManager;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.uicc.IccUtils;
import com.huawei.android.telephony.RlogEx;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public final class HwCallManagerReference extends Handler implements AbstractCallManager.CallManagerReference {
    private static final int EVENT_DISCONNECT = 102;
    private static final int EVENT_HUAWEI_BASE = 500;
    private static final int EVENT_HW_BUFFER_SOLICITED = 502;
    private static final int EVENT_HW_BUFFER_UNSOLICITED = 501;
    private static final int EVENT_PHONE_STATE_CHANGED = 101;
    private static final int EVENT_RESET_SPEECH_INFO_DB_DELAYED = 503;
    private static final int EVENT_UNSOl_SPEECH_INFO = 103;
    private static final boolean IS_AMR_WB_SHOW_HD = SystemProperties.getBoolean("ro.config.amr_wb_show_hd", false);
    private static final String LOG_TAG = "HwCallManagerReference";
    public static final String OPERATOR_CUSTOMER_WB_SHOW_HD = "wb_show_hd";
    private static final int RESET_WB_DB_TIME = 200;
    private static final boolean SUPPORT_ADJUST_SPEECH_CODEC = HwModemCapability.isCapabilitySupport(11);
    private static final boolean VDBG = true;
    private static boolean mIsLocalHangupSpeedUp = SystemProperties.get("ro.config.hw_hangup_speedup", "true").equals("true");
    private int mActiveSub = 0;
    protected final RegistrantList mActiveSubChangeRegistrants = new RegistrantList();
    private CallManager mCM;
    protected AbstractCallManager.disconnectCallback mCallbackCallNotifier;
    protected AbstractCallManager.disconnectCallback mCallbackInCallScreen;
    public final RegistrantList mEncryptCallRegistrants = new RegistrantList();

    public HwCallManagerReference(CallManager cm) {
        this.mCM = cm;
    }

    public void setInCallScreenDisconnectCallback(AbstractCallManager.disconnectCallback callback) {
        this.mCallbackInCallScreen = callback;
    }

    public void setCallNotifierDisconnectCallback(AbstractCallManager.disconnectCallback callback) {
        this.mCallbackCallNotifier = callback;
    }

    public void inCallScreenDisconnectNotify(AsyncResult r) {
        AbstractCallManager.disconnectCallback disconnectcallback = this.mCallbackInCallScreen;
        if (disconnectcallback != null) {
            disconnectcallback.disconnectNotify(r);
        }
    }

    public void calllNotifierDisconnectNotify(AsyncResult r) {
        AbstractCallManager.disconnectCallback disconnectcallback = this.mCallbackCallNotifier;
        if (disconnectcallback != null) {
            disconnectcallback.disconnectNotify(r);
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
        RlogEx.i(LOG_TAG, "resultForKMCRemoteCmd() cmd " + cmd + " reqdata " + reqData);
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

    public static class HWBuffer {
        public static final int BUFFER_SIZE = 120;
        public static final int BUFLEN_SIZE = 1;
        public static final int EVENT_SIZE = 4;
        private static final int INVALID_EVENT_ID = -1;
        public static final String LOG_TAG = "CM_HWBUFFER";
        public byte[] buffer = null;
        public int event;
        public Throwable exception = null;
        private boolean isAvailable = false;
        public byte length = 0;
        public Message reqMsg = null;

        public HWBuffer() {
        }

        public HWBuffer(int event2, byte[] buffer2) {
            int i = 0;
            this.event = event2;
            this.length = (byte) (buffer2 != null ? buffer2.length : i);
            byte b = this.length;
            if (b > 0 && b <= 120) {
                this.buffer = Arrays.copyOf(buffer2, buffer2.length);
            }
            if (event2 > -1) {
                this.isAvailable = true;
            }
        }

        public boolean isAvail() {
            return this.isAvailable;
        }

        public boolean isSolicited() {
            return this.reqMsg != null;
        }

        public boolean isException() {
            return this.exception != null;
        }

        public byte[] toArray() {
            if (!isAvail()) {
                RlogEx.e(LOG_TAG, "this(HWBuffer) is unavailable!!!");
                return new byte[0];
            }
            ByteBuffer buf = ByteBuffer.wrap(new byte[(this.length + 5)]);
            if (HuaweiTelephonyConfigs.isHisiPlatform()) {
                buf.order(ByteOrder.nativeOrder());
            }
            buf.putInt(this.event);
            buf.put(this.length);
            byte b = this.length;
            if (b > 0 && 120 >= b) {
                buf.put(this.buffer);
            }
            return buf.array();
        }

        public String toString() {
            return "HWBuffer>>>{ event:" + this.event + ", length:" + ((int) this.length) + ", buffer:" + IccUtils.bytesToHexString(this.buffer) + " }.";
        }

        public void sendRespToTarget(Object obj) {
            if (!isAvail() || !isSolicited() || this.reqMsg.getTarget() == null) {
                RlogEx.v(LOG_TAG, "sendRespToTarget() Failed, HWBuffer not available OR reqMsg not found Target(@Handle)!");
                return;
            }
            Message message = this.reqMsg;
            message.obj = obj;
            message.sendToTarget();
        }

        public static HWBuffer makeHWBuffer(byte[] data) {
            return makeHWBuffer(data, false);
        }

        static HWBuffer makeHWBuffer(byte[] data, boolean isOrder) {
            if (data == null) {
                return null;
            }
            byte[] buffer2 = null;
            try {
                ByteBuffer byteData = ByteBuffer.wrap(data);
                if (isOrder) {
                    byteData.order(ByteOrder.nativeOrder());
                }
                int event2 = byteData.getInt();
                int i = byteData.get();
                if (i > 0) {
                    buffer2 = new byte[i];
                    byteData.get(buffer2, 0, i);
                }
                return new HWBuffer(event2, buffer2);
            } catch (Exception e) {
                RlogEx.e(LOG_TAG, "data[] parse execption, please check data format>>>[event(4):buf_len(1):buffer(<120)] !!!");
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

    private void processSolicitedHWResponse(AsyncResult ar) {
        HWBuffer hwBuf = HWBuffer.makeHWBuffer(ar, true);
        RlogEx.v(LOG_TAG, "received SolicitedHWResponse hwBuf:" + hwBuf.toString());
        if (hwBuf.isAvail()) {
            int i = hwBuf.event;
            if (i == 0 || i == 1) {
                RlogEx.i(LOG_TAG, "[Solicited] HW_ENCRYPT_CALL or HW_KMC_REMOTE_COMMUNICATION.");
                hwBuf.sendRespToTarget(hwBuf.buffer);
                return;
            }
            RlogEx.w(LOG_TAG, "CallManager be untreated for event[" + hwBuf.event + "] in processSolicitedHWResponse()! please user handle");
            hwBuf.sendRespToTarget(hwBuf.buffer);
            return;
        }
        RlogEx.e(LOG_TAG, "processSolicitedHWResponse(ar) is unavailable!!!");
    }

    private void processUnSolicitedHWResponse(AsyncResult ar) {
        HWBuffer hwBuf = HWBuffer.makeHWBuffer(ar, true);
        RlogEx.v(LOG_TAG, "received UnSolicitedHWResponse hwBuf:" + hwBuf.toString());
        if (hwBuf.isAvail()) {
            int i = hwBuf.event;
            if (i == 0 || i == 1) {
                RlogEx.i(LOG_TAG, "[UnSolicited] HW_ENCRYPT_CALL or HW_KMC_REMOTE_COMMUNICATION.");
                this.mEncryptCallRegistrants.notifyRegistrants(new AsyncResult((Object) null, hwBuf.buffer, hwBuf.exception));
                return;
            }
            RlogEx.w(LOG_TAG, "not found event for processUnSolicitedHWResponse()");
            return;
        }
        RlogEx.e(LOG_TAG, "processUnSolicitedHWResponse(ar) is unavailable!!!");
    }

    private void sendHWSolicited(Phone phone, Message reqMsg, int event, byte[] reqData) {
        if (phone == null) {
            RlogEx.w(LOG_TAG, "sendHWSolicited() phone parameter is invalid");
            return;
        }
        phone.sendHWSolicited(obtainMessage(EVENT_HW_BUFFER_SOLICITED, reqMsg), event, reqData);
        RlogEx.i(LOG_TAG, "sendHWSolicited completed, phone: " + phone + ", event=" + event);
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        switch (i) {
            case EVENT_PHONE_STATE_CHANGED /*{ENCODED_INT: 101}*/:
                RlogEx.i(LOG_TAG, " handleMessage (EVENT_PHONE_STATE_CHANGED)");
                onPhoneStateChanged((AsyncResult) msg.obj);
                return;
            case EVENT_DISCONNECT /*{ENCODED_INT: 102}*/:
                RlogEx.i(LOG_TAG, " handleMessage (EVENT_DISCONNECT)");
                onEventDisconnect((AsyncResult) msg.obj);
                return;
            case EVENT_UNSOl_SPEECH_INFO /*{ENCODED_INT: 103}*/:
                RlogEx.i(LOG_TAG, " handleMessage (EVENT_UNSOl_SPEECH_INFO)");
                onUnsoSpeechInfo((AsyncResult) msg.obj);
                return;
            default:
                switch (i) {
                    case EVENT_HW_BUFFER_UNSOLICITED /*{ENCODED_INT: 501}*/:
                        RlogEx.i(LOG_TAG, "handleMessage (EVENT_HW_BUFFER_UNSOLICITED)");
                        processUnSolicitedHWResponse((AsyncResult) msg.obj);
                        return;
                    case EVENT_HW_BUFFER_SOLICITED /*{ENCODED_INT: 502}*/:
                        RlogEx.i(LOG_TAG, "handleMessage (EVENT_HW_BUFFER_SOLICITED)");
                        AsyncResult soAr = (AsyncResult) msg.obj;
                        if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
                            processSolicitedHWResponse(soAr);
                            return;
                        } else if (soAr.exception == null) {
                            RlogEx.i(LOG_TAG, "encrypted call SOLICITED res is ok");
                            return;
                        } else {
                            RlogEx.e(LOG_TAG, "encrypted call SOLICITED exception ");
                            return;
                        }
                    case EVENT_RESET_SPEECH_INFO_DB_DELAYED /*{ENCODED_INT: 503}*/:
                        Phone phone = (Phone) msg.obj;
                        RlogEx.i(LOG_TAG, "handleMessage (EVENT_RESET_SPEECH_INFO_DB_DELAYED)");
                        if (phone != null && phone.getContext() != null) {
                            Settings.System.putInt(phone.getContext().getContentResolver(), OPERATOR_CUSTOMER_WB_SHOW_HD, 1);
                            return;
                        }
                        return;
                    default:
                        return;
                }
        }
    }

    private void onEventDisconnect(AsyncResult ar) {
        Phone phone;
        if (ar != null && (phone = (Phone) ar.userObj) != null) {
            int otherSlotId = 1;
            if (phone.getState() == PhoneConstants.State.IDLE) {
                phone.setSpeechInfoCodec(1);
                if (IS_AMR_WB_SHOW_HD) {
                    sendMessageDelayed(obtainMessage(EVENT_RESET_SPEECH_INFO_DB_DELAYED, phone), 200);
                    RlogEx.i(LOG_TAG, "onEventDisconnect: voice call end, Reset speechCodec to NB after 200ms.");
                }
            }
            int disConnectSlotId = phone.getPhoneId();
            if (disConnectSlotId != 0) {
                otherSlotId = 0;
            }
            int otherSub = SubscriptionController.getInstance().getSubIdUsingPhoneId(otherSlotId);
            PhoneConstants.State otherState = this.mCM.getState(otherSub);
            RlogEx.i(LOG_TAG, "disSlot: " + disConnectSlotId + ", othSlot: " + otherSlotId + ", otherState:" + otherState);
            if (otherState == PhoneConstants.State.OFFHOOK) {
                setAudioParameters(getPhone((long) otherSub));
            }
        }
    }

    private void onUnsoSpeechInfo(AsyncResult ar) {
        Phone phone;
        if (ar != null && (phone = (Phone) ar.userObj) != null) {
            int subId = phone.getSubId();
            RlogEx.i(LOG_TAG, "subid : " + subId + ", mActiveSub : " + this.mActiveSub);
            int[] intResult = (int[]) ar.result;
            if (intResult != null && intResult.length != 0) {
                int speechCodec = intResult[0];
                if (IS_AMR_WB_SHOW_HD) {
                    boolean isSpeechCodecWB = false;
                    if (2 == speechCodec) {
                        isSpeechCodecWB = true;
                    }
                    RlogEx.i(LOG_TAG, "speechCodec : " + speechCodec + ", isSpeechCodecWB : " + isSpeechCodecWB + ", phoneId:" + phone.getPhoneId());
                    setSpeechCodec(speechCodec, isSpeechCodecWB, phone);
                }
                phone.setSpeechInfoCodec(speechCodec);
                if (subId == this.mActiveSub) {
                    setAudioParameters(phone);
                }
            } else if (IS_AMR_WB_SHOW_HD) {
                RlogEx.i(LOG_TAG, "intResult==null or intResult.length == 0, reset SpeechCodec to NB.");
                setSpeechCodec(1, false, phone);
            }
        }
    }

    public void setSpeechCodec(int speechCodec, boolean isSpeechCodecWb, Phone phone) {
        if (phone == null) {
            RlogEx.e(LOG_TAG, "setSpeechCode: phone is null , return .");
            return;
        }
        Settings.System.putInt(phone.getContext().getContentResolver(), OPERATOR_CUSTOMER_WB_SHOW_HD, speechCodec);
        Intent intent = new Intent("com.huawei.intent.action.SPEECH_CODEC_WB");
        intent.addFlags(536870912);
        intent.putExtra("speechCodecWb", speechCodec);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phone.getPhoneId());
        phone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void registerForSubscriptionChange(Handler h, int what, Object obj) {
        this.mActiveSubChangeRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSubscriptionChange(Handler h) {
        this.mActiveSubChangeRegistrants.remove(h);
    }

    public void setActiveSubscription(int subscription) {
        RlogEx.i(LOG_TAG, "setActiveSubscription existing:" + this.mActiveSub + "new = " + subscription);
        this.mActiveSub = subscription;
        this.mActiveSubChangeRegistrants.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(this.mActiveSub), (Throwable) null));
    }

    public int getActiveSubscription() {
        RlogEx.i(LOG_TAG, "getActiveSubscription  = " + this.mActiveSub);
        return this.mActiveSub;
    }

    public void registerForPhoneStates(Phone phone) {
        phone.registerForHWBuffer(this, (int) EVENT_HW_BUFFER_UNSOLICITED, (Object) null);
        if (SUPPORT_ADJUST_SPEECH_CODEC) {
            phone.registerForPreciseCallStateChanged(this, (int) EVENT_PHONE_STATE_CHANGED, (Object) null);
            phone.registerForDisconnect(this, (int) EVENT_DISCONNECT, phone);
            phone.registerForUnsolSpeechInfo(this, (int) EVENT_UNSOl_SPEECH_INFO, phone);
        }
    }

    public void unregisterForPhoneStates(Phone phone) {
        phone.unregisterForHWBuffer(this);
        if (SUPPORT_ADJUST_SPEECH_CODEC) {
            phone.unregisterForPreciseCallStateChanged(this);
            phone.unregisterForUnsolSpeechInfo(this);
            phone.unregisterForDisconnect(this);
        }
    }

    public void onSwitchToOtherActiveSub(Phone currentPhone) {
        if (SUPPORT_ADJUST_SPEECH_CODEC) {
            if (currentPhone == null) {
                RlogEx.i(LOG_TAG, "onSwitchToOtherActiveSub currentPhone is NULL! ");
                return;
            }
            int currentSub = currentPhone.getSubId();
            RlogEx.i(LOG_TAG, "onSwitchToOtherActiveSub currentSub = " + currentSub + " mActiveSub = " + this.mActiveSub);
            int i = this.mActiveSub;
            if (currentSub == i) {
                PhoneConstants.State state = this.mCM.getState(i);
                RlogEx.i(LOG_TAG, "onSwitchToOtherActiveSub currentSub = " + currentSub + ", state = " + state);
                if (state == PhoneConstants.State.OFFHOOK) {
                    setAudioParameters(currentPhone);
                }
            }
        }
    }

    private void setAudioParameters(Phone phone) {
        if (phone != null) {
            Context context = phone.getContext();
            String speechInfo = phone.getSpeechInfoCodec();
            RlogEx.i(LOG_TAG, "setAudioParameters speechInfo = " + speechInfo);
            if (!speechInfo.equals("")) {
                ((AudioManager) context.getSystemService("audio")).setParameters(speechInfo);
            }
        }
    }

    private void onPhoneStateChanged(AsyncResult r) {
        if (this.mCM.getState(this.mActiveSub) == PhoneConstants.State.OFFHOOK) {
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
