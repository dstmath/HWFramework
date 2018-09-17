package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.internal.telephony.Call.SrvccState;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public abstract class CallTracker extends Handler {
    private static final boolean DBG_POLL = false;
    protected static final int EVENT_CALL_STATE_CHANGE = 2;
    protected static final int EVENT_CALL_WAITING_INFO_CDMA = 15;
    protected static final int EVENT_CONFERENCE_RESULT = 11;
    protected static final int EVENT_ECT_RESULT = 13;
    protected static final int EVENT_EXIT_ECM_RESPONSE_CDMA = 14;
    protected static final int EVENT_GET_LAST_CALL_FAIL_CAUSE = 5;
    protected static final int EVENT_OPERATION_COMPLETE = 4;
    protected static final int EVENT_POLL_CALLS_RESULT = 1;
    protected static final int EVENT_RADIO_AVAILABLE = 9;
    protected static final int EVENT_RADIO_NOT_AVAILABLE = 10;
    protected static final int EVENT_REPOLL_AFTER_DELAY = 3;
    protected static final int EVENT_RIL_RECOVERY = 201;
    protected static final int EVENT_RSRVCC_STATE_CHANGED = 50;
    protected static final int EVENT_SEPARATE_RESULT = 12;
    protected static final int EVENT_SWITCH_RESULT = 8;
    protected static final int EVENT_THREE_WAY_DIAL_BLANK_FLASH = 20;
    protected static final int EVENT_THREE_WAY_DIAL_L2_RESULT_CDMA = 16;
    public static final boolean IS_SUPPORT_RIL_RECOVERY = false;
    protected static final int MAX_END_CALL_DURATION = 35000;
    static final int POLL_DELAY_MSEC = 250;
    private final int VALID_COMPARE_LENGTH;
    public CommandsInterface mCi;
    protected ArrayList<Connection> mHandoverConnections;
    protected Message mLastRelevantPoll;
    protected boolean mNeedsPoll;
    protected boolean mNumberConverted;
    protected int mPendingOperations;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.CallTracker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.CallTracker.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.CallTracker.<clinit>():void");
    }

    public abstract State getState();

    public abstract void handleMessage(Message message);

    protected abstract void handlePollCalls(AsyncResult asyncResult);

    protected abstract void log(String str);

    public abstract void registerForVoiceCallEnded(Handler handler, int i, Object obj);

    public abstract void registerForVoiceCallStarted(Handler handler, int i, Object obj);

    public abstract void unregisterForVoiceCallEnded(Handler handler);

    public abstract void unregisterForVoiceCallStarted(Handler handler);

    public CallTracker() {
        this.mHandoverConnections = new ArrayList();
        this.mNumberConverted = IS_SUPPORT_RIL_RECOVERY;
        this.VALID_COMPARE_LENGTH = EVENT_REPOLL_AFTER_DELAY;
    }

    protected void pollCallsWhenSafe() {
        this.mNeedsPoll = true;
        if (checkNoOperationsPending()) {
            this.mLastRelevantPoll = obtainMessage(EVENT_POLL_CALLS_RESULT);
            this.mCi.getCurrentCalls(this.mLastRelevantPoll);
        }
    }

    protected void pollCallsAfterDelay() {
        Message msg = obtainMessage();
        msg.what = EVENT_REPOLL_AFTER_DELAY;
        sendMessageDelayed(msg, 250);
    }

    protected boolean isCommandExceptionRadioNotAvailable(Throwable e) {
        if (e != null && (e instanceof CommandException) && ((CommandException) e).getCommandError() == Error.RADIO_NOT_AVAILABLE) {
            return true;
        }
        return IS_SUPPORT_RIL_RECOVERY;
    }

    protected Connection getHoConnection(DriverCall dc) {
        for (Connection hoConn : this.mHandoverConnections) {
            log("getHoConnection - compare number: hoConn= " + hoConn.toString());
            if (hoConn.getAddress() != null && hoConn.getAddress().contains(dc.number)) {
                log("getHoConnection: Handover connection match found = " + hoConn.toString());
                return hoConn;
            }
        }
        for (Connection hoConn2 : this.mHandoverConnections) {
            log("getHoConnection: compare state hoConn= " + hoConn2.toString());
            if (hoConn2.getStateBeforeHandover() == Call.stateFromDCState(dc.state)) {
                log("getHoConnection: Handover connection match found = " + hoConn2.toString());
                return hoConn2;
            }
        }
        return null;
    }

    protected void notifySrvccState(SrvccState state, ArrayList<Connection> c) {
        if (state == SrvccState.STARTED && c != null) {
            this.mHandoverConnections.addAll(c);
        } else if (state != SrvccState.COMPLETED) {
            this.mHandoverConnections.clear();
        }
        log("notifySrvccState: mHandoverConnections= " + this.mHandoverConnections.toString());
    }

    protected void handleRadioAvailable() {
        pollCallsWhenSafe();
    }

    protected Message obtainNoPollCompleteMessage(int what) {
        this.mPendingOperations += EVENT_POLL_CALLS_RESULT;
        this.mLastRelevantPoll = null;
        return obtainMessage(what);
    }

    private boolean checkNoOperationsPending() {
        return this.mPendingOperations == 0 ? true : IS_SUPPORT_RIL_RECOVERY;
    }

    protected String checkForTestEmergencyNumber(String dialString) {
        String testEn = SystemProperties.get("ril.test.emergencynumber");
        if (TextUtils.isEmpty(testEn)) {
            return dialString;
        }
        String[] values = testEn.split(":");
        log("checkForTestEmergencyNumber: values.length=" + values.length);
        if (values.length != EVENT_CALL_STATE_CHANGE || !values[0].equals(PhoneNumberUtils.stripSeparators(dialString))) {
            return dialString;
        }
        if (this.mCi != null) {
            this.mCi.testingEmergencyCall();
        }
        log("checkForTestEmergencyNumber: remap " + dialString + " to " + values[EVENT_POLL_CALLS_RESULT]);
        return values[EVENT_POLL_CALLS_RESULT];
    }

    protected String convertNumberIfNecessary(Phone phone, String dialNumber) {
        if (dialNumber == null) {
            return dialNumber;
        }
        String[] convertMaps = phone.getContext().getResources().getStringArray(17236036);
        log("convertNumberIfNecessary Roaming convertMaps.length " + convertMaps.length + " dialNumber.length() " + dialNumber.length());
        if (convertMaps.length < EVENT_POLL_CALLS_RESULT || dialNumber.length() < EVENT_REPOLL_AFTER_DELAY) {
            return dialNumber;
        }
        String outNumber = "";
        boolean needConvert = IS_SUPPORT_RIL_RECOVERY;
        int length = convertMaps.length;
        for (int i = 0; i < length; i += EVENT_POLL_CALLS_RESULT) {
            String convertMap = convertMaps[i];
            log("convertNumberIfNecessary: " + convertMap);
            String[] entry = convertMap.split(":");
            if (entry.length > EVENT_POLL_CALLS_RESULT) {
                String[] tmpArray = entry[EVENT_POLL_CALLS_RESULT].split(",");
                if (!TextUtils.isEmpty(entry[0]) && dialNumber.equals(entry[0])) {
                    if (tmpArray.length < EVENT_CALL_STATE_CHANGE || TextUtils.isEmpty(tmpArray[EVENT_POLL_CALLS_RESULT])) {
                        if (outNumber.isEmpty()) {
                            needConvert = true;
                        }
                    } else if (compareGid1(phone, tmpArray[EVENT_POLL_CALLS_RESULT])) {
                        needConvert = true;
                    }
                    if (needConvert) {
                        if (TextUtils.isEmpty(tmpArray[0]) || !tmpArray[0].endsWith("MDN")) {
                            outNumber = tmpArray[0];
                        } else {
                            String mdn = phone.getLine1Number();
                            if (!TextUtils.isEmpty(mdn)) {
                                if (mdn.startsWith(HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX)) {
                                    outNumber = mdn;
                                } else {
                                    outNumber = tmpArray[0].substring(0, tmpArray[0].length() - 3) + mdn;
                                }
                            }
                        }
                        needConvert = IS_SUPPORT_RIL_RECOVERY;
                    }
                }
            }
        }
        if (TextUtils.isEmpty(outNumber)) {
            return dialNumber;
        }
        log("convertNumberIfNecessary: convert service number");
        this.mNumberConverted = true;
        return outNumber;
    }

    private boolean compareGid1(Phone phone, String serviceGid1) {
        int i = 0;
        String gid1 = phone.getGroupIdLevel1();
        int gid_length = serviceGid1.length();
        boolean ret = true;
        if (serviceGid1 == null || serviceGid1.equals("")) {
            log("compareGid1 serviceGid is empty, return " + true);
            return true;
        }
        if (gid1 != null && gid1.length() >= gid_length) {
            i = gid1.substring(0, gid_length).equalsIgnoreCase(serviceGid1);
        }
        if (i == 0) {
            log(" gid1 " + gid1 + " serviceGid1 " + serviceGid1);
            ret = IS_SUPPORT_RIL_RECOVERY;
        }
        log("compareGid1 is " + (ret ? "Same" : "Different"));
        return ret;
    }

    public void cleanRilRecovery() {
        if (IS_SUPPORT_RIL_RECOVERY && hasMessages(EVENT_RIL_RECOVERY)) {
            log("remove msg:EVENT_RIL_RECOVERY");
            removeMessages(EVENT_RIL_RECOVERY);
        }
    }

    public void delaySendRilRecoveryMsg(Call.State state) {
        if (IS_SUPPORT_RIL_RECOVERY && state != Call.State.DISCONNECTING && !hasMessages(EVENT_RIL_RECOVERY)) {
            log("delay send EVENT_RIL_RECOVERY");
            sendMessageDelayed(obtainMessage(EVENT_RIL_RECOVERY), 35000);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("CallTracker:");
        pw.println(" mPendingOperations=" + this.mPendingOperations);
        pw.println(" mNeedsPoll=" + this.mNeedsPoll);
        pw.println(" mLastRelevantPoll=" + this.mLastRelevantPoll);
    }
}
