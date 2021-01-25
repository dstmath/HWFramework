package com.huawei.internal.telephony;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.ims.ImsSsInfo;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.IHwGsmCdmaPhoneInner;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneMmiCode;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;

public class MmiCodeExt {
    public static final int EVENT_USSD_COMPLETE = 4;
    private static final String LOG = "MmiCodeExt";
    private static final int NOT_REGISTERED = -1;
    private MmiCode mMmiCode;

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.internal.telephony.MmiCodeExt$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$internal$telephony$MmiCodeExt$StateExt = new int[StateExt.values().length];

        static {
            try {
                $SwitchMap$com$huawei$internal$telephony$MmiCodeExt$StateExt[StateExt.PENDING.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$MmiCodeExt$StateExt[StateExt.CANCELLED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$MmiCodeExt$StateExt[StateExt.COMPLETE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$MmiCodeExt$StateExt[StateExt.FAILED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public static MmiCode.State getMmiCodeStateFromStateEx(StateExt stateExt) {
        int i = AnonymousClass1.$SwitchMap$com$huawei$internal$telephony$MmiCodeExt$StateExt[stateExt.ordinal()];
        if (i == 1) {
            return MmiCode.State.PENDING;
        }
        if (i == 2) {
            return MmiCode.State.CANCELLED;
        }
        if (i == 3) {
            return MmiCode.State.COMPLETE;
        }
        if (i != 4) {
            return null;
        }
        return MmiCode.State.FAILED;
    }

    public void setGsmMmiCode(String dialString, IHwGsmCdmaPhoneInner phone, UiccCardApplicationEx app) {
        this.mMmiCode = GsmMmiCode.newFromDialString(dialString, phone, app);
    }

    public boolean isNull() {
        return this.mMmiCode == null;
    }

    public void setGsmMmiCode(MmiCode mmiCode) {
        this.mMmiCode = mmiCode;
    }

    public MmiCode getMmiCode() {
        return this.mMmiCode;
    }

    public String getSc() {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            return ((GsmMmiCode) mmiCode).getmSC();
        }
        return null;
    }

    public boolean isServiceCodeCallForwarding(String serviceCode) {
        if (this.mMmiCode instanceof GsmMmiCode) {
            return GsmMmiCode.isServiceCodeCallForwarding(serviceCode);
        }
        return false;
    }

    public void setImsPhone(PhoneExt phone) {
        if ((this.mMmiCode instanceof GsmMmiCode) && (phone.getPhone() instanceof ImsPhone)) {
            ((GsmMmiCode) this.mMmiCode).setImsPhone(phone.getPhone());
        }
    }

    public void processCode() {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode != null) {
            try {
                mmiCode.processCode();
            } catch (CallStateException e) {
                Rlog.e(LOG, "processCode error.");
            }
        }
    }

    public boolean isInterrogate() {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            return ((GsmMmiCode) mmiCode).isInterrogateHw();
        }
        return false;
    }

    public boolean isActivate() {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            return ((GsmMmiCode) mmiCode).isActivateHw();
        }
        return false;
    }

    public boolean isDeactivate() {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            return ((GsmMmiCode) mmiCode).isDeactivateHw();
        }
        return false;
    }

    public boolean isRegister() {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            return ((GsmMmiCode) mmiCode).isRegisterHw();
        }
        return false;
    }

    public boolean isErasure() {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            return ((GsmMmiCode) mmiCode).isErasureHw();
        }
        return false;
    }

    public Message obtainMessage(int event, MmiCodeExt mmiCodeExt) {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            return ((GsmMmiCode) mmiCode).obtainMessage(event, mmiCodeExt);
        }
        return null;
    }

    public Message obtainMessage(int event, int isSettingUnconditionalVoice, int isEnableDesired, MmiCodeExt mmiCodeExt) {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            return ((GsmMmiCode) mmiCode).obtainMessage(event, isSettingUnconditionalVoice, isEnableDesired, mmiCodeExt);
        }
        return null;
    }

    public String getSia() {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            return ((GsmMmiCode) mmiCode).getSia();
        }
        return null;
    }

    public String getSib() {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            return ((GsmMmiCode) mmiCode).getSib();
        }
        return null;
    }

    public String getSic() {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            return ((GsmMmiCode) mmiCode).getSic();
        }
        return null;
    }

    public int scToCallForwardReasonEx(String sc) {
        if (this.mMmiCode instanceof GsmMmiCode) {
            return GsmMmiCode.scToCallForwardReasonEx(sc);
        }
        throw new RuntimeException("invalid call forward sc");
    }

    public int siToTimeEx(String si) {
        if (this.mMmiCode instanceof GsmMmiCode) {
            return GsmMmiCode.siToTimeEx(si);
        }
        return 0;
    }

    public void setHwCallFwgReg(boolean isCallFwdReg) {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            ((GsmMmiCode) mmiCode).setHwCallFwgReg(isCallFwdReg);
        }
    }

    public String scToBarringFacility(String sc) {
        if (this.mMmiCode instanceof GsmMmiCode) {
            return GsmMmiCode.scToBarringFacilityHw(sc);
        }
        return null;
    }

    public String getDialingNumber() {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            return ((GsmMmiCode) mmiCode).getDialingNumber();
        }
        return null;
    }

    public void setVoiceCallForwardingFlag(int line, boolean enable, String number) {
        MmiCode mmiCode = this.mMmiCode;
        if ((mmiCode instanceof GsmMmiCode) && ((GsmMmiCode) mmiCode).getIccRcords() != null) {
            ((GsmMmiCode) this.mMmiCode).getIccRcords().setVoiceCallForwardingFlag(line, enable, number);
        }
    }

    public boolean isServiceCodeCallBarring(String serviceCode) {
        if (this.mMmiCode instanceof GsmMmiCode) {
            return GsmMmiCode.isServiceCodeCallBarringHw(serviceCode);
        }
        return false;
    }

    public Context getContext() {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            return ((GsmMmiCode) mmiCode).getContext();
        }
        return null;
    }

    public PhoneExt getPhone() {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            return ((GsmMmiCode) mmiCode).getPhoneExt();
        }
        return null;
    }

    public void setState(StateExt state) {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            ((GsmMmiCode) mmiCode).setState(getMmiCodeStateFromStateEx(state));
        }
    }

    public void setMessage(StringBuilder sb) {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            ((GsmMmiCode) mmiCode).setMessage(sb);
        }
    }

    public boolean getHwCallFwdReg() {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            return ((GsmMmiCode) mmiCode).getHwCallFwdReg();
        }
        return false;
    }

    public CharSequence createQueryCallWaitingResultMessageEx(int serviceClass) {
        MmiCode mmiCode = this.mMmiCode;
        if (mmiCode instanceof GsmMmiCode) {
            return ((GsmMmiCode) mmiCode).createQueryCallWaitingResultMessageEx(serviceClass);
        }
        return null;
    }

    public CharSequence makeCFQueryResultMessageEx(CallForwardInfoExt info, int serviceClassMask) {
        if (!(this.mMmiCode instanceof GsmMmiCode) || info == null || !(info.getCallForwardInfo() instanceof CallForwardInfo)) {
            return null;
        }
        return ((GsmMmiCode) this.mMmiCode).makeCFQueryResultMessageEx(info.getCallForwardInfo(), serviceClassMask);
    }

    public int getStatus(Bundle ssInfoResp) {
        ImsSsInfo imsSsInfo = null;
        if (ssInfoResp != null) {
            imsSsInfo = ssInfoResp.getParcelable(ImsPhoneMmiCode.UT_BUNDLE_KEY_SSINFO);
        }
        if (imsSsInfo instanceof ImsSsInfo) {
            return imsSsInfo.getStatus();
        }
        return -1;
    }

    public enum StateExt {
        PENDING(MmiCode.State.PENDING),
        CANCELLED(MmiCode.State.CANCELLED),
        COMPLETE(MmiCode.State.COMPLETE),
        FAILED(MmiCode.State.FAILED);
        
        private final MmiCode.State state;

        private StateExt(MmiCode.State state2) {
            this.state = state2;
        }
    }
}
