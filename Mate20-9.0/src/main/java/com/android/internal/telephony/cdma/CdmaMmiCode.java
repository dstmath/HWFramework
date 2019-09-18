package com.android.internal.telephony.cdma;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
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
    static Pattern sPatternSuppService = Pattern.compile("((\\*|#|\\*#|\\*\\*|##)(\\d{2,3})(\\*([^*#]*)(\\*([^*#]*)(\\*([^*#]*)(\\*([^*#]*))?)?)?)?#)(.*)");
    String LOG_TAG = "CdmaMmiCode";
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
    MmiCode.State mState = MmiCode.State.PENDING;
    UiccCardApplication mUiccApplication;

    public static CdmaMmiCode newFromDialString(String dialString, GsmCdmaPhone phone, UiccCardApplication app) {
        CdmaMmiCode ret = null;
        if (dialString == null) {
            return null;
        }
        Matcher m = sPatternSuppService.matcher(dialString);
        if (m.matches()) {
            ret = new CdmaMmiCode(phone, app);
            ret.mPoundString = makeEmptyNull(m.group(1));
            ret.mAction = makeEmptyNull(m.group(2));
            ret.mSc = makeEmptyNull(m.group(3));
            ret.mSia = makeEmptyNull(m.group(5));
            ret.mSib = makeEmptyNull(m.group(7));
            ret.mSic = makeEmptyNull(m.group(9));
            ret.mPwd = makeEmptyNull(m.group(11));
            ret.mDialingNumber = makeEmptyNull(m.group(12));
        }
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
        this.mPhone = phone;
        this.LOG_TAG += "[SUB" + phone.getPhoneId() + "]";
        this.mContext = phone.getContext();
        this.mUiccApplication = app;
    }

    public MmiCode.State getState() {
        return this.mState;
    }

    public CharSequence getMessage() {
        return this.mMessage;
    }

    public Phone getPhone() {
        return this.mPhone;
    }

    public void cancel() {
        if (this.mState != MmiCode.State.COMPLETE && this.mState != MmiCode.State.FAILED) {
            this.mState = MmiCode.State.CANCELLED;
            this.mPhone.onMMIDone(this);
        }
    }

    public boolean isCancelable() {
        return false;
    }

    public boolean isPinPukCommand() {
        return this.mSc != null && (this.mSc.equals(SC_PIN) || this.mSc.equals(SC_PIN2) || this.mSc.equals(SC_PUK) || this.mSc.equals(SC_PUK2));
    }

    /* access modifiers changed from: package-private */
    public boolean isRegister() {
        return this.mAction != null && this.mAction.equals(ACTION_REGISTER);
    }

    public boolean isUssdRequest() {
        Rlog.w(this.LOG_TAG, "isUssdRequest is not implemented in CdmaMmiCode");
        return false;
    }

    public String getDialString() {
        return null;
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
                    handlePasswordError(17040521);
                } else {
                    if (pinLen >= 4) {
                        if (pinLen <= 8) {
                            if (this.mSc.equals(SC_PIN) && this.mUiccApplication != null && this.mUiccApplication.getState() == IccCardApplicationStatus.AppState.APPSTATE_PUK) {
                                handlePasswordError(17040543);
                                return;
                            } else if (this.mUiccApplication != null) {
                                String str = this.LOG_TAG;
                                Rlog.d(str, "process mmi service code using UiccApp sc=" + this.mSc);
                                if (this.mSc.equals(SC_PIN)) {
                                    this.mUiccApplication.changeIccLockPassword(oldPinOrPuk, newPinOrPuk, obtainMessage(1, this));
                                    return;
                                } else if (this.mSc.equals(SC_PIN2)) {
                                    this.mUiccApplication.changeIccFdnPassword(oldPinOrPuk, newPinOrPuk, obtainMessage(1, this));
                                    return;
                                } else if (this.mSc.equals(SC_PUK)) {
                                    this.mUiccApplication.supplyPuk(oldPinOrPuk, newPinOrPuk, obtainMessage(1, this));
                                    return;
                                } else if (this.mSc.equals(SC_PUK2)) {
                                    this.mUiccApplication.supplyPuk2(oldPinOrPuk, newPinOrPuk, obtainMessage(1, this));
                                    return;
                                } else {
                                    throw new RuntimeException("Unsupported service code=" + this.mSc);
                                }
                            } else {
                                throw new RuntimeException("No application mUiccApplicaiton is null");
                            }
                        }
                    }
                    handlePasswordError(17040227);
                }
            }
        } catch (RuntimeException e) {
            this.mState = MmiCode.State.FAILED;
            this.mMessage = this.mContext.getText(17040531);
            this.mPhone.onMMIDone(this);
        }
    }

    private void handlePasswordError(int res) {
        this.mState = MmiCode.State.FAILED;
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        sb.append(this.mContext.getText(res));
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    public void handleMessage(Message msg) {
        if (msg.what == 1) {
            onSetComplete(msg, (AsyncResult) msg.obj);
        } else {
            Rlog.e(this.LOG_TAG, "Unexpected reply");
        }
    }

    private CharSequence getScString() {
        if (this.mSc == null || !isPinPukCommand()) {
            return "";
        }
        return this.mContext.getText(17039457);
    }

    private void onSetComplete(Message msg, AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = MmiCode.State.FAILED;
            if (ar.exception instanceof CommandException) {
                CommandException.Error err = ((CommandException) ar.exception).getCommandError();
                if (err == CommandException.Error.PASSWORD_INCORRECT) {
                    if (isPinPukCommand()) {
                        if (this.mSc.equals(SC_PUK) || this.mSc.equals(SC_PUK2)) {
                            sb.append(this.mContext.getText(17039689));
                        } else {
                            sb.append(this.mContext.getText(17039688));
                        }
                        int attemptsRemaining = msg.arg1;
                        if (attemptsRemaining <= 0) {
                            Rlog.d(this.LOG_TAG, "onSetComplete: PUK locked, cancel as lock screen will handle this");
                            this.mState = MmiCode.State.CANCELLED;
                        } else if (attemptsRemaining > 0) {
                            String str = this.LOG_TAG;
                            Rlog.d(str, "onSetComplete: attemptsRemaining=" + attemptsRemaining);
                            sb.append(this.mContext.getResources().getQuantityString(18153497, attemptsRemaining, new Object[]{Integer.valueOf(attemptsRemaining)}));
                        }
                    } else {
                        sb.append(this.mContext.getText(17040631));
                    }
                } else if (err == CommandException.Error.SIM_PUK2) {
                    sb.append(this.mContext.getText(17039688));
                    sb.append("\n");
                    sb.append(this.mContext.getText(17040544));
                } else if (err != CommandException.Error.REQUEST_NOT_SUPPORTED) {
                    sb.append(this.mContext.getText(17040531));
                } else if (this.mSc.equals(SC_PIN)) {
                    sb.append(this.mContext.getText(17039988));
                }
            } else {
                sb.append(this.mContext.getText(17040531));
            }
        } else if (isRegister()) {
            this.mState = MmiCode.State.COMPLETE;
            sb.append(this.mContext.getText(17041083));
        } else {
            this.mState = MmiCode.State.FAILED;
            sb.append(this.mContext.getText(17040531));
        }
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    public ResultReceiver getUssdCallbackReceiver() {
        return null;
    }
}
