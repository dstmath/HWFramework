package com.android.internal.telephony.cdma;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.MmiCode.State;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.UiccCardApplication;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CdmaMmiCode extends Handler implements MmiCode {
    static final String ACTION_REGISTER = "**";
    static final int EVENT_SET_COMPLETE = 1;
    static final int MATCH_GROUP_ACTION = 2;
    static final int MATCH_GROUP_DIALING_NUMBER = 12;
    static final int MATCH_GROUP_POUND_STRING = 1;
    static final int MATCH_GROUP_PWD_CONFIRM = 11;
    static final int MATCH_GROUP_SERVICE_CODE = 3;
    static final int MATCH_GROUP_SIA = 5;
    static final int MATCH_GROUP_SIB = 7;
    static final int MATCH_GROUP_SIC = 9;
    static final String SC_PIN = "04";
    static final String SC_PIN2 = "042";
    static final String SC_PUK = "05";
    static final String SC_PUK2 = "052";
    static Pattern sPatternSuppService;
    String LOG_TAG;
    String mAction;
    Context mContext;
    String mDialingNumber;
    CharSequence mMessage;
    GsmCdmaPhone mPhone;
    String mPoundString;
    String mPwd;
    String mSc;
    String mSia;
    String mSib;
    String mSic;
    State mState;
    UiccCardApplication mUiccApplication;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cdma.CdmaMmiCode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cdma.CdmaMmiCode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.CdmaMmiCode.<clinit>():void");
    }

    public static CdmaMmiCode newFromDialString(String dialString, GsmCdmaPhone phone, UiccCardApplication app) {
        Matcher m = sPatternSuppService.matcher(dialString);
        if (!m.matches()) {
            return null;
        }
        CdmaMmiCode ret = new CdmaMmiCode(phone, app);
        ret.mPoundString = makeEmptyNull(m.group(MATCH_GROUP_POUND_STRING));
        ret.mAction = makeEmptyNull(m.group(MATCH_GROUP_ACTION));
        ret.mSc = makeEmptyNull(m.group(MATCH_GROUP_SERVICE_CODE));
        ret.mSia = makeEmptyNull(m.group(MATCH_GROUP_SIA));
        ret.mSib = makeEmptyNull(m.group(MATCH_GROUP_SIB));
        ret.mSic = makeEmptyNull(m.group(MATCH_GROUP_SIC));
        ret.mPwd = makeEmptyNull(m.group(MATCH_GROUP_PWD_CONFIRM));
        ret.mDialingNumber = makeEmptyNull(m.group(MATCH_GROUP_DIALING_NUMBER));
        return ret;
    }

    private static String makeEmptyNull(String s) {
        if (s == null || s.length() != 0) {
            return s;
        }
        return null;
    }

    CdmaMmiCode(GsmCdmaPhone phone, UiccCardApplication app) {
        super(phone.getHandler().getLooper());
        this.LOG_TAG = "CdmaMmiCode";
        this.mState = State.PENDING;
        this.mPhone = phone;
        this.LOG_TAG += "[SUB" + phone.getPhoneId() + "]";
        this.mContext = phone.getContext();
        this.mUiccApplication = app;
    }

    public State getState() {
        return this.mState;
    }

    public CharSequence getMessage() {
        return this.mMessage;
    }

    public Phone getPhone() {
        return this.mPhone;
    }

    public void cancel() {
        if (this.mState != State.COMPLETE && this.mState != State.FAILED) {
            this.mState = State.CANCELLED;
            this.mPhone.onMMIDone(this);
        }
    }

    public boolean isCancelable() {
        return false;
    }

    public boolean isPinPukCommand() {
        if (this.mSc == null) {
            return false;
        }
        if (this.mSc.equals(SC_PIN) || this.mSc.equals(SC_PIN2) || this.mSc.equals(SC_PUK)) {
            return true;
        }
        return this.mSc.equals(SC_PUK2);
    }

    boolean isRegister() {
        return this.mAction != null ? this.mAction.equals(ACTION_REGISTER) : false;
    }

    public boolean isUssdRequest() {
        Rlog.w(this.LOG_TAG, "isUssdRequest is not implemented in CdmaMmiCode");
        return false;
    }

    public void processCode() {
        try {
            if (isPinPukCommand()) {
                String oldPinOrPuk = this.mSia;
                String newPinOrPuk = this.mSib;
                int pinLen = newPinOrPuk.length();
                if (!isRegister()) {
                    throw new RuntimeException("Ivalid register/action=" + this.mAction);
                } else if (!newPinOrPuk.equals(this.mSic)) {
                    handlePasswordError(17039527);
                } else if (pinLen < 4 || pinLen > 8) {
                    handlePasswordError(17039528);
                } else if (this.mSc.equals(SC_PIN) && this.mUiccApplication != null && this.mUiccApplication.getState() == AppState.APPSTATE_PUK) {
                    handlePasswordError(17039530);
                } else if (this.mUiccApplication != null) {
                    Rlog.d(this.LOG_TAG, "process mmi service code using UiccApp sc=" + this.mSc);
                    if (this.mSc.equals(SC_PIN)) {
                        this.mUiccApplication.changeIccLockPassword(oldPinOrPuk, newPinOrPuk, obtainMessage(MATCH_GROUP_POUND_STRING, this));
                    } else if (this.mSc.equals(SC_PIN2)) {
                        this.mUiccApplication.changeIccFdnPassword(oldPinOrPuk, newPinOrPuk, obtainMessage(MATCH_GROUP_POUND_STRING, this));
                    } else if (this.mSc.equals(SC_PUK)) {
                        this.mUiccApplication.supplyPuk(oldPinOrPuk, newPinOrPuk, obtainMessage(MATCH_GROUP_POUND_STRING, this));
                    } else if (this.mSc.equals(SC_PUK2)) {
                        this.mUiccApplication.supplyPuk2(oldPinOrPuk, newPinOrPuk, obtainMessage(MATCH_GROUP_POUND_STRING, this));
                    } else {
                        throw new RuntimeException("Unsupported service code=" + this.mSc);
                    }
                } else {
                    throw new RuntimeException("No application mUiccApplicaiton is null");
                }
            }
        } catch (RuntimeException e) {
            this.mState = State.FAILED;
            this.mMessage = this.mContext.getText(17039516);
            this.mPhone.onMMIDone(this);
        }
    }

    private void handlePasswordError(int res) {
        this.mState = State.FAILED;
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        sb.append(this.mContext.getText(res));
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    public void handleMessage(Message msg) {
        if (msg.what == MATCH_GROUP_POUND_STRING) {
            onSetComplete(msg, msg.obj);
        } else {
            Rlog.e(this.LOG_TAG, "Unexpected reply");
        }
    }

    private CharSequence getScString() {
        if (this.mSc == null || !isPinPukCommand()) {
            return "";
        }
        return this.mContext.getText(17039543);
    }

    private void onSetComplete(Message msg, AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = State.FAILED;
            if (ar.exception instanceof CommandException) {
                Error err = ((CommandException) ar.exception).getCommandError();
                if (err == Error.PASSWORD_INCORRECT) {
                    if (isPinPukCommand()) {
                        if (this.mSc.equals(SC_PUK) || this.mSc.equals(SC_PUK2)) {
                            sb.append(this.mContext.getText(17039526));
                        } else {
                            sb.append(this.mContext.getText(17039525));
                        }
                        int attemptsRemaining = msg.arg1;
                        if (attemptsRemaining <= 0) {
                            Rlog.d(this.LOG_TAG, "onSetComplete: PUK locked, cancel as lock screen will handle this");
                            this.mState = State.CANCELLED;
                        } else if (attemptsRemaining > 0) {
                            Rlog.d(this.LOG_TAG, "onSetComplete: attemptsRemaining=" + attemptsRemaining);
                            Resources resources = this.mContext.getResources();
                            Object[] objArr = new Object[MATCH_GROUP_POUND_STRING];
                            objArr[0] = Integer.valueOf(attemptsRemaining);
                            sb.append(resources.getQuantityString(18087936, attemptsRemaining, objArr));
                        }
                    } else {
                        sb.append(this.mContext.getText(17039523));
                    }
                } else if (err == Error.SIM_PUK2) {
                    sb.append(this.mContext.getText(17039525));
                    sb.append("\n");
                    sb.append(this.mContext.getText(17039531));
                } else if (err != Error.REQUEST_NOT_SUPPORTED) {
                    sb.append(this.mContext.getText(17039516));
                } else if (this.mSc.equals(SC_PIN)) {
                    sb.append(this.mContext.getText(17039532));
                }
            } else {
                sb.append(this.mContext.getText(17039516));
            }
        } else if (isRegister()) {
            this.mState = State.COMPLETE;
            sb.append(this.mContext.getText(17039521));
        } else {
            this.mState = State.FAILED;
            sb.append(this.mContext.getText(17039516));
        }
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }
}
