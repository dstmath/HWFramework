package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.RegistrantListEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.internal.telephony.GsmCdmaCallTrackerEx;
import com.huawei.internal.telephony.PhoneConstantsExt;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.uicc.IccUtilsEx;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class HwCallManagerEx extends DefaultHwCallManagerEx {
    private static final int EVENT_DISCONNECT = 102;
    private static final int EVENT_HUAWEI_BASE = 500;
    private static final int EVENT_HW_BUFFER_SOLICITED = 502;
    private static final int EVENT_HW_BUFFER_UNSOLICITED = 501;
    private static final int EVENT_PHONE_STATE_CHANGED = 101;
    private static final int EVENT_RESET_SPEECH_INFO_DB_DELAYED = 503;
    private static final int EVENT_UNSOl_SPEECH_INFO = 103;
    private static final int HW_ENCRYPT_CALL = 0;
    private static final int HW_KMC_REMOTE_COMMUNICATION = 1;
    private static final boolean IS_AMR_WB_SHOW_HD = SystemPropertiesEx.getBoolean("ro.config.amr_wb_show_hd", false);
    private static final boolean IS_WATCH = "watch".equals(SystemPropertiesEx.get("ro.build.characteristics", "default"));
    private static final String LOG_TAG = "HwCallManagerEx";
    private static final String OPERATOR_CUSTOMER_WB_SHOW_HD = "wb_show_hd";
    private static final int RESET_WB_DB_TIME = 200;
    private static final boolean SUPPORT_ADJUST_SPEECH_CODEC = HwModemCapability.isCapabilitySupport(11);
    private static final boolean VDBG = true;
    private int mActiveSub = 0;
    private final RegistrantListEx mActiveSubChangeRegistrants = new RegistrantListEx();
    private ICallManagerInner mCM;
    private final RegistrantListEx mEncryptCallRegistrants = new RegistrantListEx();
    Handler mHandler = new Handler(Looper.getMainLooper()) {
        /* class com.android.internal.telephony.HwCallManagerEx.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            switch (i) {
                case HwCallManagerEx.EVENT_PHONE_STATE_CHANGED /* 101 */:
                    RlogEx.i(HwCallManagerEx.LOG_TAG, " handleMessage (EVENT_PHONE_STATE_CHANGED)");
                    HwCallManagerEx.this.onPhoneStateChanged(AsyncResultEx.from(msg.obj));
                    return;
                case HwCallManagerEx.EVENT_DISCONNECT /* 102 */:
                    RlogEx.i(HwCallManagerEx.LOG_TAG, " handleMessage (EVENT_DISCONNECT)");
                    HwCallManagerEx.this.onEventDisconnect(AsyncResultEx.from(msg.obj));
                    return;
                case HwCallManagerEx.EVENT_UNSOl_SPEECH_INFO /* 103 */:
                    RlogEx.i(HwCallManagerEx.LOG_TAG, " handleMessage (EVENT_UNSOl_SPEECH_INFO)");
                    HwCallManagerEx.this.onUnsoSpeechInfo(AsyncResultEx.from(msg.obj));
                    return;
                default:
                    switch (i) {
                        case HwCallManagerEx.EVENT_HW_BUFFER_UNSOLICITED /* 501 */:
                            RlogEx.i(HwCallManagerEx.LOG_TAG, "handleMessage (EVENT_HW_BUFFER_UNSOLICITED)");
                            HwCallManagerEx.this.processUnSolicitedHWResponse(AsyncResultEx.from(msg.obj));
                            return;
                        case HwCallManagerEx.EVENT_HW_BUFFER_SOLICITED /* 502 */:
                            RlogEx.i(HwCallManagerEx.LOG_TAG, "handleMessage (EVENT_HW_BUFFER_SOLICITED)");
                            AsyncResultEx soAr = AsyncResultEx.from(msg.obj);
                            if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
                                HwCallManagerEx.this.processSolicitedHWResponse(soAr);
                                return;
                            } else if (soAr.getException() == null) {
                                RlogEx.i(HwCallManagerEx.LOG_TAG, "encrypted call SOLICITED res is ok");
                                return;
                            } else {
                                RlogEx.e(HwCallManagerEx.LOG_TAG, "encrypted call SOLICITED exception ");
                                return;
                            }
                        case HwCallManagerEx.EVENT_RESET_SPEECH_INFO_DB_DELAYED /* 503 */:
                            PhoneExt phone = null;
                            if (msg.obj instanceof PhoneExt) {
                                phone = (PhoneExt) msg.obj;
                            }
                            RlogEx.i(HwCallManagerEx.LOG_TAG, "handleMessage (EVENT_RESET_SPEECH_INFO_DB_DELAYED)");
                            if (phone != null && phone.getContext() != null) {
                                Settings.System.putInt(phone.getContext().getContentResolver(), HwCallManagerEx.OPERATOR_CUSTOMER_WB_SHOW_HD, 1);
                                return;
                            }
                            return;
                        default:
                            return;
                    }
            }
        }
    };

    public HwCallManagerEx(ICallManagerInner callManager) {
        this.mCM = callManager;
    }

    public void registerForEncryptedCall(Handler h, int what, Object obj) {
        this.mEncryptCallRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForEncryptedCall(Handler h) {
        this.mEncryptCallRegistrants.remove(h);
    }

    public void cmdForEncryptedCall(PhoneExt phone, int cmd, byte[] reqData) {
        cmdForEncryptedCall(phone, null, cmd, reqData);
    }

    private void cmdForEncryptedCall(PhoneExt phone, Message reqMsg, int cmd, byte[] reqData) {
        sendHWSolicited(phone, reqMsg, 0, new HWBuffer(cmd, reqData).toArray());
    }

    public void resultForKMCRemoteCmd(PhoneExt phone, int cmd, int reqData) {
        RlogEx.i(LOG_TAG, "resultForKMCRemoteCmd() cmd " + cmd + " reqdata " + reqData);
        resultForKMCRemoteCmd(phone, null, cmd, new byte[]{(byte) reqData});
    }

    private void resultForKMCRemoteCmd(PhoneExt phone, Message reqMsg, int cmd, byte[] reqData) {
        sendHWSolicited(phone, reqMsg, 1, new HWBuffer(cmd, reqData).toArray());
    }

    public void setConnEncryptCallByNumber(PhoneExt phone, String number, boolean val) {
        GsmCdmaCallTrackerEx gsmCdmaCallTrackerEx;
        if (phone != null && number != null && phone.getPhoneType() == 2 && (gsmCdmaCallTrackerEx = phone.getGsmCdmaCallTrackerEx()) != null) {
            gsmCdmaCallTrackerEx.setConnEncryptCallByNumber(number, val);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processSolicitedHWResponse(AsyncResultEx ar) {
        if (ar != null) {
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
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processUnSolicitedHWResponse(AsyncResultEx ar) {
        HWBuffer hwBuf = HWBuffer.makeHWBuffer(ar, true);
        RlogEx.v(LOG_TAG, "received UnSolicitedHWResponse hwBuf:" + hwBuf.toString());
        if (hwBuf.isAvail()) {
            int i = hwBuf.event;
            if (i == 0 || i == 1) {
                RlogEx.i(LOG_TAG, "[UnSolicited] HW_ENCRYPT_CALL or HW_KMC_REMOTE_COMMUNICATION.");
                this.mEncryptCallRegistrants.notifyRegistrants((Object) null, hwBuf.buffer, hwBuf.exception);
                return;
            }
            RlogEx.w(LOG_TAG, "not found event for processUnSolicitedHWResponse()");
            return;
        }
        RlogEx.e(LOG_TAG, "processUnSolicitedHWResponse(ar) is unavailable!!!");
    }

    private void sendHWSolicited(PhoneExt phone, Message reqMsg, int event, byte[] reqData) {
        if (phone == null) {
            RlogEx.w(LOG_TAG, "sendHWSolicited() phone parameter is invalid");
            return;
        }
        phone.requestForECInfo(this.mHandler.obtainMessage(EVENT_HW_BUFFER_SOLICITED, reqMsg), event, reqData);
        RlogEx.i(LOG_TAG, "sendHWSolicited completed, phone: " + phone + ", event=" + event);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onEventDisconnect(AsyncResultEx ar) {
        if (ar != null) {
            PhoneExt phone = null;
            if (ar.getUserObj() instanceof PhoneExt) {
                phone = (PhoneExt) ar.getUserObj();
            }
            if (phone != null) {
                int otherSlotId = 1;
                if (phone.getStateEx() == PhoneConstantsExt.StateEx.IDLE) {
                    phone.setSpeechInfoCodec(1);
                    if (IS_AMR_WB_SHOW_HD) {
                        Handler handler = this.mHandler;
                        handler.sendMessageDelayed(handler.obtainMessage(EVENT_RESET_SPEECH_INFO_DB_DELAYED, phone), 200);
                        RlogEx.i(LOG_TAG, "onEventDisconnect: voice call end, Reset speechCodec to NB after 200ms.");
                    }
                }
                int disConnectSlotId = phone.getPhoneId();
                if (disConnectSlotId != 0) {
                    otherSlotId = 0;
                }
                int otherSub = SubscriptionControllerEx.getInstance().getSubIdUsingPhoneId(otherSlotId);
                PhoneConstantsExt.StateEx otherState = this.mCM.getStateEx(otherSub);
                RlogEx.i(LOG_TAG, "disSlot: " + disConnectSlotId + ", othSlot: " + otherSlotId + ", otherState:" + otherState);
                if (otherState == PhoneConstantsExt.StateEx.OFFHOOK) {
                    setAudioParameters(this.mCM.getPhoneHw(otherSub));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUnsoSpeechInfo(AsyncResultEx ar) {
        if (ar != null) {
            PhoneExt phone = null;
            if (ar.getUserObj() instanceof PhoneExt) {
                phone = (PhoneExt) ar.getUserObj();
            }
            if (phone != null) {
                int subId = phone.getSubId();
                RlogEx.i(LOG_TAG, "subid : " + subId + ", mActiveSub : " + this.mActiveSub);
                int[] intResult = (int[]) ar.getResult();
                if (intResult != null && intResult.length != 0) {
                    int speechCodec = intResult[0];
                    if (IS_AMR_WB_SHOW_HD) {
                        boolean isSpeechCodecWB = false;
                        if (speechCodec == 2) {
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
    }

    public void setSpeechCodec(int speechCodec, boolean isSpeechCodecWb, PhoneExt phone) {
        if (phone == null || phone.getContext() == null) {
            RlogEx.e(LOG_TAG, "setSpeechCode: phone is null , return .");
            return;
        }
        Settings.System.putInt(phone.getContext().getContentResolver(), OPERATOR_CUSTOMER_WB_SHOW_HD, speechCodec);
        Intent intent = new Intent("com.huawei.intent.action.SPEECH_CODEC_WB");
        intent.addFlags(536870912);
        intent.putExtra("speechCodecWb", speechCodec);
        SubscriptionManagerEx.putPhoneIdAndSubIdExtra(intent, phone.getPhoneId());
        phone.getContext().sendStickyBroadcastAsUser(intent, UserHandleEx.ALL);
    }

    public void registerForSubscriptionChange(Handler h, int what, Object obj) {
        this.mActiveSubChangeRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSubscriptionChange(Handler h) {
        this.mActiveSubChangeRegistrants.remove(h);
    }

    public int getActiveSubscription() {
        RlogEx.i(LOG_TAG, "getActiveSubscription  = " + this.mActiveSub);
        return this.mActiveSub;
    }

    public void setActiveSubscription(int subscription) {
        RlogEx.i(LOG_TAG, "setActiveSubscription existing:" + this.mActiveSub + "new = " + subscription);
        this.mActiveSub = subscription;
        this.mActiveSubChangeRegistrants.notifyRegistrants((Object) null, Integer.valueOf(this.mActiveSub), (Throwable) null);
    }

    public void registerForPhoneStatesHw(PhoneExt phone) {
        if (phone != null) {
            phone.registerForHWBuffer(this.mHandler, (int) EVENT_HW_BUFFER_UNSOLICITED, (Object) null);
            if (SUPPORT_ADJUST_SPEECH_CODEC) {
                phone.registerForPreciseCallStateChanged(this.mHandler, (int) EVENT_PHONE_STATE_CHANGED, (Object) null);
                phone.registerForDisconnect(this.mHandler, (int) EVENT_DISCONNECT, phone);
                if (!IS_WATCH) {
                    phone.registerForUnsolSpeechInfo(this.mHandler, (int) EVENT_UNSOl_SPEECH_INFO, phone);
                }
            }
        }
    }

    public void unregisterForPhoneStatesHw(PhoneExt phone) {
        if (phone != null) {
            phone.unregisterForHWBuffer(this.mHandler);
            if (SUPPORT_ADJUST_SPEECH_CODEC) {
                phone.unregisterForPreciseCallStateChanged(this.mHandler);
                if (!IS_WATCH) {
                    phone.unregisterForUnsolSpeechInfo(this.mHandler);
                }
                phone.unregisterForDisconnect(this.mHandler);
            }
        }
    }

    public void onSwitchToOtherActiveSub(PhoneExt phone) {
        if (SUPPORT_ADJUST_SPEECH_CODEC) {
            if (phone == null) {
                RlogEx.i(LOG_TAG, "onSwitchToOtherActiveSub phone is NULL! ");
                return;
            }
            int currentSub = phone.getSubId();
            RlogEx.i(LOG_TAG, "onSwitchToOtherActiveSub currentSub = " + currentSub + " mActiveSub = " + this.mActiveSub);
            int i = this.mActiveSub;
            if (currentSub == i) {
                PhoneConstantsExt.StateEx state = this.mCM.getStateEx(i);
                RlogEx.i(LOG_TAG, "onSwitchToOtherActiveSub currentSub = " + currentSub + ", state = " + state);
                if (state == PhoneConstantsExt.StateEx.OFFHOOK) {
                    setAudioParameters(phone);
                }
            }
        }
    }

    private void setAudioParameters(PhoneExt phone) {
        if (phone != null) {
            Context context = phone.getContext();
            String speechInfo = phone.getSpeechInfoCodec();
            RlogEx.i(LOG_TAG, "setAudioParameters speechInfo = " + speechInfo);
            if (!TextUtils.isEmpty(speechInfo)) {
                ((AudioManager) context.getSystemService("audio")).setParameters(speechInfo);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPhoneStateChanged(AsyncResultEx r) {
        if (this.mCM.getStateEx(this.mActiveSub) == PhoneConstantsExt.StateEx.OFFHOOK) {
            setAudioParameters(this.mCM.getPhoneHw(this.mActiveSub));
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

        public static HWBuffer makeHWBuffer(byte[] data) {
            return makeHWBuffer(data, false);
        }

        static HWBuffer makeHWBuffer(byte[] data, boolean isOrder) {
            HWBuffer hwBuf = null;
            if (data == null) {
                return null;
            }
            int event2 = -1;
            byte[] buffer2 = null;
            boolean success = false;
            try {
                ByteBuffer byteData = ByteBuffer.wrap(data);
                if (isOrder) {
                    byteData.order(ByteOrder.nativeOrder());
                }
                event2 = byteData.getInt();
                int i = byteData.get();
                if (i > 0) {
                    buffer2 = new byte[i];
                    byteData.get(buffer2, 0, i);
                }
                success = true;
            } catch (Exception e) {
                RlogEx.e(LOG_TAG, "data[] parse execption, please check data format >>>[event(4):buf_len(1):buffer(<120)] !!!");
                hwBuf = new HWBuffer();
                hwBuf.exception = e;
            }
            if (success) {
                return new HWBuffer(event2, buffer2);
            }
            return hwBuf;
        }

        public static HWBuffer makeHWBuffer(AsyncResultEx ar) {
            return makeHWBuffer(ar, false);
        }

        static HWBuffer makeHWBuffer(AsyncResultEx ar, boolean isOrder) {
            HWBuffer hwBuf = makeHWBuffer((byte[]) ar.getResult(), isOrder);
            if (hwBuf == null) {
                hwBuf = new HWBuffer();
            }
            if (hwBuf.isAvail()) {
                if (ar.getException() != null) {
                    hwBuf.exception = ar.getException();
                }
                if (ar.getUserObj() != null) {
                    hwBuf.reqMsg = (Message) ar.getUserObj();
                }
            }
            return hwBuf;
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
            return "HWBuffer>>>{ event:" + this.event + ", length:" + ((int) this.length) + ", buffer:" + IccUtilsEx.bytesToHexString(this.buffer) + " }.";
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
    }
}
