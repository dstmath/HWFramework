package com.android.internal.telephony.gsm;

import android.os.Message;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import com.android.internal.telephony.HwBaseSignalStrengthManager;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwServiceStateTrackerEx;
import com.android.internal.telephony.HwSignalStrength;
import com.android.internal.telephony.IServiceStateTrackerInner;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.SignalStrengthEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import huawei.cust.HwCfgFilePolicy;

public class HwGsmSignalStrengthManager extends HwBaseSignalStrengthManager {
    private static final int EVENT_BASE = 2000;
    private static final int EVENT_DELAY_UPDATE_EMERGENCY_TO_NOSERVICE_DONE = 2001;
    private static final int EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH = 2002;
    private static final int GSM_STRENGTH_POOR_STD = -109;
    private static final String LOG_TAG = "HwGsmSignalStrengthManager";
    private static final int WCDMA_ECIO_NONE = 255;
    private static final int WCDMA_ECIO_POOR_STD = SystemPropertiesEx.getInt("ro.wcdma.eciopoorstd", (int) W_ECIO_POOR_STD);
    private static final int WCDMA_STRENGTH_POOR_STD = SystemPropertiesEx.getInt("ro.wcdma.poorstd", (int) W_STRENGTH_POOR_STD);
    private static final int W_ECIO_POOR_STD = -17;
    private static final int W_STRENGTH_POOR_STD = -112;
    private HwGsmDoubleSignalStrength mDoubleSignalStrength;
    private HwSignalStrength mHwSigStr = HwSignalStrength.getInstance(this.mPhoneId, this.mContext);
    private SignalStrength mModemSignalStrength;
    private HwGsmDoubleSignalStrength mOldDoubleSignalStrength;

    public HwGsmSignalStrengthManager(IServiceStateTrackerInner serviceStateTracker, PhoneExt phoneExt, HwServiceStateTrackerEx hwServiceStateTrackerEx) {
        super(serviceStateTracker, phoneExt, hwServiceStateTrackerEx);
        this.mTag = "HwGsmSignalStrengthManager[" + this.mPhoneId + "]";
    }

    @Override // com.android.internal.telephony.HwBaseSignalStrengthManager, android.os.Handler
    public void handleMessage(Message msg) {
        logd("handleMessage, msg.what = " + msg.what);
        int i = msg.what;
        if (i == EVENT_DELAY_UPDATE_EMERGENCY_TO_NOSERVICE_DONE) {
            handleDelayUpdateEmergencyToNoServiceDone();
        } else if (i != EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH) {
            super.handleMessage(msg);
        } else {
            handleDelayUpdateGsmSingalStrength();
        }
    }

    private void handleDelayUpdateEmergencyToNoServiceDone() {
        logd("Delay Timer expired");
        this.mIsRefreshStateEcc = true;
        this.mServiceStateTracker.pollState();
    }

    private void handleDelayUpdateGsmSingalStrength() {
        logd("event update gsm signal strength");
        this.mDoubleSignalStrength.proccessAlaphFilter(this.mServiceStateTracker.getSignalStrength(), this.mModemSignalStrength);
        this.mPhone.notifySignalStrength();
    }

    public boolean proccessGsmDelayUpdateRegisterStateDone(ServiceState oldSs, ServiceState newSs) {
        int delayedTime;
        if (HwModemCapability.isCapabilitySupport(6)) {
            return false;
        }
        if (delayUpdateGsmEcctoNoserviceState(oldSs, newSs) || this.mHwServiceStateTrackerEx.needDelayForRatChanged(oldSs, newSs)) {
            return true;
        }
        boolean lostNework = ((ServiceStateEx.getVoiceRegState(oldSs) != 0 && ServiceStateEx.getDataState(oldSs) != 0) || ServiceStateEx.getVoiceRegState(newSs) == 0 || ServiceStateEx.getDataState(newSs) == 0) ? false : true;
        int newMainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        boolean isInService = ServiceStateEx.getDataState(newSs) == 0 || (ServiceStateEx.getVoiceRegState(newSs) == 0 && HwFullNetworkManager.getInstance().isCMCCDsdxEnable());
        logd("lostNework : " + lostNework + ", newMainSlot : " + newMainSlot);
        if (isInService || needCancelDelay() || ServiceStateEx.getDataState(newSs) == 3) {
            this.mMainSlot = newMainSlot;
            cancelDeregisterStateDelayTimer();
        } else if (hasMessages(1001)) {
            return true;
        } else {
            if (lostNework && !this.mIsRefreshState && (delayedTime = getSendDeregisterStateDelayedTime(oldSs, newSs)) > 0) {
                delaySendDeregisterStateChange(delayedTime);
                return true;
            }
        }
        this.mIsRefreshState = false;
        return false;
    }

    private boolean delayUpdateGsmEcctoNoserviceState(ServiceState oldSs, ServiceState newSs) {
        int delayedTime = getEcctoNoserviceStateDelayedTime();
        if (delayedTime <= 0) {
            return false;
        }
        boolean isEcctoNoservice = ((ServiceStateEx.getVoiceRegState(oldSs) == 1 && ServiceStateEx.getDataState(oldSs) == 1) && ServiceStateEx.isEmergencyOnly(oldSs)) && ((ServiceStateEx.getVoiceRegState(newSs) == 1 && ServiceStateEx.getDataState(newSs) == 1) && !ServiceStateEx.isEmergencyOnly(newSs));
        int newMainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        logd("isEcctoNoservice : " + isEcctoNoservice);
        if (isNeedCancelEccDelayTimer(newSs)) {
            this.mMainSlotEcc = newMainSlot;
            cancelEcctoNoserviceStateDelayTimer();
        } else {
            logd("hasMessages EVENT_DELAY_UPDATE_EMERGENCY_TO_NOSERVICE_DONE");
            if (hasMessages(EVENT_DELAY_UPDATE_EMERGENCY_TO_NOSERVICE_DONE)) {
                return true;
            }
            if (isEcctoNoservice && !this.mIsRefreshStateEcc) {
                if (hasMessages(1001)) {
                    logd("cancelDeregisterStateDelayTimer");
                    cancelDeregisterStateDelayTimer();
                }
                logd("Ecc delay time = " + delayedTime);
                delayEcctoNoserviceStateChange(delayedTime);
                return true;
            }
        }
        this.mIsRefreshStateEcc = false;
        return false;
    }

    private int getSendDeregisterStateDelayedTime(ServiceState oldSs, ServiceState newSs) {
        boolean isPsLostNetwork = false;
        boolean isCsLostNetwork = (ServiceStateEx.getVoiceRegState(oldSs) != 0 || ServiceStateEx.getVoiceRegState(newSs) == 0 || ServiceStateEx.getDataState(newSs) == 0) ? false : true;
        if (!(ServiceStateEx.getDataState(oldSs) != 0 || ServiceStateEx.getVoiceRegState(newSs) == 0 || ServiceStateEx.getDataState(newSs) == 0)) {
            isPsLostNetwork = true;
        }
        try {
            int slotId = this.mPhone.getPhoneId();
            Integer defaultTime = (Integer) HwCfgFilePolicy.getValue("lostnetwork.default_timer", slotId, Integer.class);
            Integer delaytimerCs2G = (Integer) HwCfgFilePolicy.getValue("lostnetwork.delaytimer_cs2G", slotId, Integer.class);
            Integer delaytimerCs3G = (Integer) HwCfgFilePolicy.getValue("lostnetwork.delaytimer_cs3G", slotId, Integer.class);
            Integer delaytimerCs4G = (Integer) HwCfgFilePolicy.getValue("lostnetwork.delaytimer_cs4G", slotId, Integer.class);
            Integer delaytimerPs2G = (Integer) HwCfgFilePolicy.getValue("lostnetwork.delaytimer_ps2G", slotId, Integer.class);
            Integer delaytimerPs3G = (Integer) HwCfgFilePolicy.getValue("lostnetwork.delaytimer_ps3G", slotId, Integer.class);
            Integer delaytimerPs4G = (Integer) HwCfgFilePolicy.getValue("lostnetwork.delaytimer_ps4G", slotId, Integer.class);
            if (defaultTime != null) {
                this.delayedTimeDefaultValue = defaultTime.intValue();
            }
            if (delaytimerCs2G != null) {
                this.delayedTimeNetworkstatusCs2G = delaytimerCs2G.intValue() * 1000;
            }
            if (delaytimerCs3G != null) {
                this.delayedTimeNetworkstatusCs3G = delaytimerCs3G.intValue() * 1000;
            }
            if (delaytimerCs4G != null) {
                this.delayedTimeNetworkstatusCs4G = delaytimerCs4G.intValue() * 1000;
            }
            if (delaytimerPs2G != null) {
                this.delayedTimeNetworkstatusPs2G = delaytimerPs2G.intValue() * 1000;
            }
            if (delaytimerPs3G != null) {
                this.delayedTimeNetworkstatusPs3G = delaytimerPs3G.intValue() * 1000;
            }
            if (delaytimerPs4G != null) {
                this.delayedTimeNetworkstatusPs4G = delaytimerPs4G.intValue() * 1000;
            }
        } catch (Exception e) {
            loge("lostnetwork error!");
        }
        int delayedTime = 0;
        int networkClass = TelephonyManagerEx.getNetworkClass(getNetworkType(oldSs));
        if (isCsLostNetwork) {
            if (networkClass == 1) {
                delayedTime = this.delayedTimeNetworkstatusCs2G;
            } else if (networkClass == 2) {
                delayedTime = this.delayedTimeNetworkstatusCs3G;
            } else if (networkClass == 3) {
                delayedTime = this.delayedTimeNetworkstatusCs4G;
            } else {
                delayedTime = this.delayedTimeDefaultValue * 1000;
            }
        } else if (!isPsLostNetwork) {
            logd("use default delay time.");
        } else if (networkClass == 1) {
            delayedTime = this.delayedTimeNetworkstatusPs2G;
        } else if (networkClass == 2) {
            delayedTime = this.delayedTimeNetworkstatusPs3G;
        } else if (networkClass == 3) {
            delayedTime = this.delayedTimeNetworkstatusPs4G;
        } else {
            delayedTime = this.delayedTimeDefaultValue * 1000;
        }
        logd("delay time = " + delayedTime);
        return delayedTime;
    }

    private int getEcctoNoserviceStateDelayedTime() {
        return DELAYED_ECC_TO_NOSERVICE_VALUE * 1000;
    }

    private boolean isNeedCancelEccDelayTimer(ServiceState newSs) {
        boolean isSubDeactivated = SubscriptionControllerEx.getInstance().getSubState(this.mPhone.getPhoneId()) == 0;
        IccCardConstantsEx.StateEx externalState = this.mPhone.getIccCardState();
        logd("desiredPowerState : " + this.mServiceStateTracker.getDesiredPowerState() + ", radiostate : " + this.mPhone.getCi().getRadioState() + ", mRadioOffByDoRecovery : " + this.mServiceStateTracker.getDoRecoveryTriggerState() + ", isSubDeactivated : " + isSubDeactivated + ", phoneOOS : " + this.mPhone.getOOSFlag() + ", isUserPref4GSlot : " + HwFullNetworkManager.getInstance().isUserPref4GSlot(this.mMainSlotEcc) + ", externalState : " + externalState);
        if (ServiceStateEx.getDataState(newSs) != 1 || ServiceStateEx.getVoiceRegState(newSs) != 1) {
            return true;
        }
        return ((ServiceStateEx.getVoiceRegState(newSs) == 1 && ServiceStateEx.getDataState(newSs) == 1) && ServiceStateEx.isEmergencyOnly(newSs)) || !this.mServiceStateTracker.getDesiredPowerState() || this.mPhone.getCi().getRadioState() == 0 || this.mServiceStateTracker.getDoRecoveryTriggerState() || isCardInvalid(isSubDeactivated, this.mPhone.getPhoneId()) || !HwFullNetworkManager.getInstance().isUserPref4GSlot(this.mMainSlotEcc) || this.mPhone.getOOSFlag() || ServiceStateEx.getDataState(newSs) == 3 || externalState == IccCardConstantsEx.StateEx.PUK_REQUIRED;
    }

    private void cancelEcctoNoserviceStateDelayTimer() {
        if (hasMessages(EVENT_DELAY_UPDATE_EMERGENCY_TO_NOSERVICE_DONE)) {
            logd("cancelEcctoNoserviceStateDelayTimer");
            removeMessages(EVENT_DELAY_UPDATE_EMERGENCY_TO_NOSERVICE_DONE);
        }
    }

    private void delayEcctoNoserviceStateChange(int delayedTime) {
        if (!hasMessages(EVENT_DELAY_UPDATE_EMERGENCY_TO_NOSERVICE_DONE)) {
            Message msg = obtainMessage();
            msg.what = EVENT_DELAY_UPDATE_EMERGENCY_TO_NOSERVICE_DONE;
            sendMessageDelayed(msg, (long) delayedTime);
            logd("EccStateChange timer is running,do nothing");
        }
    }

    public boolean notifySignalStrength(SignalStrength oldSs, SignalStrength newSs) {
        boolean isNotified;
        if (hasMessages(1001) || this.mHwServiceStateTrackerEx.hasRatChangedDelayMessage()) {
            logd("no notify signal");
            if (hasMessages(EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH)) {
                removeMessages(EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH);
            }
            return false;
        }
        this.mModemSignalStrength = SignalStrengthEx.newSignalStrength(newSs);
        if (isNetworkTypeChanged(oldSs, newSs)) {
            logd("Network is changed immediately!");
            if (hasMessages(EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH)) {
                removeMessages(EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH);
            }
            this.mDoubleSignalStrength = new HwGsmDoubleSignalStrength(newSs);
            SignalStrengthEx.setPhoneId(newSs, this.mPhoneId);
            SignalStrengthEx.updateLevel(newSs, this.mServiceStateTracker.getCarrierConfigHw(), this.mServiceStateTracker.getmSSHw());
            this.mServiceStateTracker.setSignalStrength(newSs);
            isNotified = true;
        } else if (hasMessages(EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH)) {
            logd("has delay update msg");
            isNotified = false;
        } else {
            this.mOldDoubleSignalStrength = this.mDoubleSignalStrength;
            this.mDoubleSignalStrength = new HwGsmDoubleSignalStrength(newSs);
            this.mDoubleSignalStrength.proccessAlaphFilter(this.mOldDoubleSignalStrength, newSs, this.mModemSignalStrength, false);
            isNotified = true;
        }
        if (isNotified) {
            this.mPhone.notifySignalStrength();
        }
        return isNotified;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.HwBaseSignalStrengthManager
    public boolean isNetworkTypeChanged(SignalStrength oldSs, SignalStrength newSs) {
        if (oldSs == null || newSs == null) {
            return false;
        }
        int newState = 0;
        int oldState = 0;
        boolean isChanged = false;
        if (newSs.getGsmSignalStrength() < -1) {
            newState = 0 | 1;
        }
        if (oldSs.getGsmSignalStrength() < -1) {
            oldState = 0 | 1;
        }
        if (SignalStrengthEx.getWcdmaRscp(newSs) < -1) {
            newState |= 2;
        }
        if (SignalStrengthEx.getWcdmaRscp(oldSs) < -1) {
            oldState |= 2;
        }
        if (SignalStrengthEx.getLteRsrp(newSs) < -1) {
            newState |= 4;
        }
        if (SignalStrengthEx.getLteRsrp(oldSs) < -1) {
            oldState |= 4;
        }
        if (SignalStrengthEx.getHwNrRsrp(newSs) < -1) {
            newState |= 32;
        }
        if (SignalStrengthEx.getHwNrRsrp(oldSs) < -1) {
            oldState |= 32;
        }
        if (SignalStrengthEx.getHwNrRsrp(oldSs) == -140 && SignalStrengthEx.getHwNrRsrp(newSs) != -140) {
            isChanged = true;
        }
        if (newState == 0 || newState == oldState) {
            return isChanged;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMessageDelayUpdateSingalStrength(int time) {
        logd("sendMessageDelayUpdateSingalStrength, time: " + time);
        Message msg = obtainMessage();
        msg.what = EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH;
        if (time == 0) {
            sendMessageDelayed(msg, (long) DELAY_DURING_TIME);
        } else {
            sendMessageDelayed(msg, (long) time);
        }
    }

    public class HwGsmDoubleSignalStrength extends HwBaseSignalStrengthManager.HwBaseDoubleSignalStrength {
        private double mDoubleGsmSs;
        private double mDoubleWcdmaEcio;
        private double mDoubleWcdmaRscp;
        private double mOldDoubleGsmSs;
        private double mOldDoubleWcdmaEcio;
        private double mOldDoubleWcdmaRscp;

        HwGsmDoubleSignalStrength(SignalStrength ss) {
            super(ss);
            this.mDoubleWcdmaRscp = (double) SignalStrengthEx.getWcdmaRscp(ss);
            this.mDoubleWcdmaEcio = (double) SignalStrengthEx.getWcdmaEcio(ss);
            this.mDoubleGsmSs = (double) ss.getGsmSignalStrength();
            this.mTechState = 0;
            if (ss.getGsmSignalStrength() < -1) {
                this.mTechState |= 1;
            }
            if (SignalStrengthEx.getWcdmaRscp(ss) < -1) {
                this.mTechState |= 2;
            }
            if (SignalStrengthEx.getLteRsrp(ss) < -1) {
                this.mTechState |= 4;
            }
            if (SignalStrengthEx.getNrRsrp(ss) < -1) {
                this.mTechState |= 32;
            }
            this.mOldDoubleWcdmaRscp = this.mDoubleWcdmaRscp;
            this.mOldDoubleWcdmaEcio = this.mDoubleWcdmaEcio;
            this.mOldDoubleGsmSs = this.mDoubleGsmSs;
        }

        public HwGsmDoubleSignalStrength(HwGsmDoubleSignalStrength doubleSs) {
            super();
            this.mDoubleNrRsrp = doubleSs.mDoubleNrRsrp;
            this.mDoubleNrRssnr = doubleSs.mDoubleNrRssnr;
            this.mDoubleLteRsrp = doubleSs.mDoubleLteRsrp;
            this.mDoubleLteRssnr = doubleSs.mDoubleLteRssnr;
            this.mDoubleWcdmaRscp = doubleSs.mDoubleWcdmaRscp;
            this.mDoubleWcdmaEcio = doubleSs.mDoubleWcdmaEcio;
            this.mDoubleGsmSs = doubleSs.mDoubleGsmSs;
            this.mTechState = doubleSs.mTechState;
            this.mOldDoubleNrRsrp = doubleSs.mDoubleNrRsrp;
            this.mOldDoubleNrRssnr = doubleSs.mDoubleNrRssnr;
            this.mOldDoubleLteRsrp = doubleSs.mDoubleLteRsrp;
            this.mOldDoubleLteRssnr = doubleSs.mDoubleLteRssnr;
            this.mOldDoubleWcdmaRscp = doubleSs.mDoubleWcdmaRscp;
            this.mOldDoubleWcdmaEcio = doubleSs.mDoubleWcdmaEcio;
            this.mOldDoubleGsmSs = doubleSs.mDoubleGsmSs;
            this.mDelayTime = doubleSs.mDelayTime;
        }

        /* access modifiers changed from: package-private */
        public double getDoubleWcdmaRscp() {
            return this.mDoubleWcdmaRscp;
        }

        /* access modifiers changed from: package-private */
        public double getDoubleWcdmaEcio() {
            return this.mDoubleWcdmaEcio;
        }

        /* access modifiers changed from: package-private */
        public double getDoubleGsmSignalStrength() {
            return this.mDoubleGsmSs;
        }

        /* access modifiers changed from: package-private */
        public double getOldDoubleWcdmaRscp() {
            return this.mOldDoubleWcdmaRscp;
        }

        /* access modifiers changed from: package-private */
        public double getOldDoubleWcdmaEcio() {
            return this.mOldDoubleWcdmaEcio;
        }

        /* access modifiers changed from: package-private */
        public double getOldDoubleGsmSignalStrength() {
            return this.mOldDoubleGsmSs;
        }

        private boolean processWcdmaRscpAlaphFilter(HwGsmDoubleSignalStrength oldDoubleSs, SignalStrength newSs, SignalStrength modemSs, boolean isNeedProcessDescend) {
            double oldWcdmaRscp = oldDoubleSs.getDoubleWcdmaRscp();
            double modemWcdmaRscp = (double) SignalStrengthEx.getWcdmaRscp(modemSs);
            this.mOldDoubleWcdmaRscp = oldWcdmaRscp;
            HwGsmSignalStrengthManager hwGsmSignalStrengthManager = HwGsmSignalStrengthManager.this;
            hwGsmSignalStrengthManager.logd("Before processWcdmaRscpAlaphFilter -- old : " + oldWcdmaRscp + "; instant new : " + modemWcdmaRscp);
            if (modemWcdmaRscp >= -1.0d) {
                modemWcdmaRscp = (double) HwGsmSignalStrengthManager.WCDMA_STRENGTH_POOR_STD;
            }
            if (oldWcdmaRscp <= modemWcdmaRscp) {
                this.mDoubleWcdmaRscp = modemWcdmaRscp;
            } else if (isNeedProcessDescend) {
                this.mDoubleWcdmaRscp = ((7.0d * oldWcdmaRscp) + (5.0d * modemWcdmaRscp)) / 12.0d;
            } else {
                this.mDoubleWcdmaRscp = oldWcdmaRscp;
            }
            HwGsmSignalStrengthManager hwGsmSignalStrengthManager2 = HwGsmSignalStrengthManager.this;
            hwGsmSignalStrengthManager2.logd("WcdmaRscpAlaphFilter modem : " + modemWcdmaRscp + "; old : " + oldWcdmaRscp + "; new : " + this.mDoubleWcdmaRscp);
            double d = this.mDoubleWcdmaRscp;
            if (d - modemWcdmaRscp <= -1.0d || d - modemWcdmaRscp >= 1.0d) {
                SignalStrengthEx.setWcdmaRscp(newSs, (int) this.mDoubleWcdmaRscp);
                return true;
            }
            this.mDoubleWcdmaRscp = modemWcdmaRscp;
            SignalStrengthEx.setWcdmaRscp(newSs, (int) this.mDoubleWcdmaRscp);
            return false;
        }

        private boolean processWcdmaEcioAlaphFilter(HwGsmDoubleSignalStrength oldDoubleSs, SignalStrength newSs, SignalStrength modemSs, boolean isNeedProcessDescend) {
            double oldWcdmaEcio = oldDoubleSs.getDoubleWcdmaEcio();
            double modemWcdmaEcio = (double) SignalStrengthEx.getWcdmaEcio(modemSs);
            this.mOldDoubleWcdmaEcio = oldWcdmaEcio;
            HwGsmSignalStrengthManager hwGsmSignalStrengthManager = HwGsmSignalStrengthManager.this;
            hwGsmSignalStrengthManager.logd("Before processWcdmaEcioAlaphFilter -- old : " + oldWcdmaEcio + "; instant new : " + modemWcdmaEcio);
            if (oldWcdmaEcio <= modemWcdmaEcio) {
                this.mDoubleWcdmaEcio = modemWcdmaEcio;
            } else if (isNeedProcessDescend) {
                this.mDoubleWcdmaEcio = (0.85d * oldWcdmaEcio) + (0.15d * modemWcdmaEcio);
            } else {
                this.mDoubleWcdmaEcio = oldWcdmaEcio;
            }
            HwGsmSignalStrengthManager hwGsmSignalStrengthManager2 = HwGsmSignalStrengthManager.this;
            hwGsmSignalStrengthManager2.logd("WcdmaEcioAlaphFilter modem : " + modemWcdmaEcio + "; old : " + oldWcdmaEcio + "; new : " + this.mDoubleWcdmaEcio);
            double d = this.mDoubleWcdmaEcio;
            if (d - modemWcdmaEcio <= -1.0d || d - modemWcdmaEcio >= 1.0d) {
                SignalStrengthEx.setWcdmaEcio(newSs, (int) this.mDoubleWcdmaEcio);
                return true;
            }
            this.mDoubleWcdmaEcio = modemWcdmaEcio;
            SignalStrengthEx.setWcdmaEcio(newSs, (int) this.mDoubleWcdmaEcio);
            return false;
        }

        private boolean processGsmSignalStrengthAlaphFilter(HwGsmDoubleSignalStrength oldDoubleSs, SignalStrength newSs, SignalStrength modemSs, boolean isNeedProcessDescend) {
            double oldGsmSs = oldDoubleSs.getDoubleGsmSignalStrength();
            double modemGsmSs = (double) modemSs.getGsmSignalStrength();
            this.mOldDoubleGsmSs = oldGsmSs;
            HwGsmSignalStrengthManager hwGsmSignalStrengthManager = HwGsmSignalStrengthManager.this;
            hwGsmSignalStrengthManager.logd("Before>>old : " + oldGsmSs + "; instant new : " + modemGsmSs);
            if (modemGsmSs >= -1.0d) {
                modemGsmSs = -109.0d;
            }
            if (oldGsmSs <= modemGsmSs) {
                this.mDoubleGsmSs = modemGsmSs;
            } else if (isNeedProcessDescend) {
                this.mDoubleGsmSs = ((7.0d * oldGsmSs) + (5.0d * modemGsmSs)) / 12.0d;
            } else {
                this.mDoubleGsmSs = oldGsmSs;
            }
            HwGsmSignalStrengthManager hwGsmSignalStrengthManager2 = HwGsmSignalStrengthManager.this;
            hwGsmSignalStrengthManager2.logd("GsmSS AlaphFilter modem : " + modemGsmSs + "; old : " + oldGsmSs + "; new : " + this.mDoubleGsmSs);
            double d = this.mDoubleGsmSs;
            if (d - modemGsmSs <= -1.0d || d - modemGsmSs >= 1.0d) {
                SignalStrengthEx.setGsmSignalStrength(newSs, (int) this.mDoubleGsmSs);
                return true;
            }
            this.mDoubleGsmSs = modemGsmSs;
            SignalStrengthEx.setGsmSignalStrength(newSs, (int) this.mDoubleGsmSs);
            return false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void proccessAlaphFilter(SignalStrength newSs, SignalStrength modemSs) {
            proccessAlaphFilter(this, newSs, modemSs, true);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void proccessAlaphFilter(HwGsmDoubleSignalStrength oldDoubleSs, SignalStrength newSs, SignalStrength modemSs, boolean isNeedProcessDescend) {
            boolean isNeedUpdate = false;
            if (oldDoubleSs == null) {
                HwGsmSignalStrengthManager.this.logd("proccess oldDoubleSs is null");
                return;
            }
            if ((this.mTechState & 32) != 0) {
                isNeedUpdate = false | processNrRsrpAlaphFilter(oldDoubleSs, newSs, modemSs, isNeedProcessDescend);
                if (HwGsmSignalStrengthManager.IS_FEATURE_SIGNAL_DUALPARAM) {
                    isNeedUpdate |= processNrRssnrAlaphFilter(oldDoubleSs, newSs, modemSs, isNeedProcessDescend);
                }
            }
            if ((this.mTechState & 4) != 0) {
                isNeedUpdate |= processLteRsrpAlaphFilter(oldDoubleSs, newSs, modemSs, isNeedProcessDescend);
                if (HwGsmSignalStrengthManager.IS_FEATURE_SIGNAL_DUALPARAM) {
                    isNeedUpdate |= processLteRssnrAlaphFilter(oldDoubleSs, newSs, modemSs, isNeedProcessDescend);
                }
            }
            if ((this.mTechState & 2) != 0) {
                isNeedUpdate |= processWcdmaRscpAlaphFilter(oldDoubleSs, newSs, modemSs, isNeedProcessDescend);
                if (HwGsmSignalStrengthManager.IS_FEATURE_SIGNAL_DUALPARAM) {
                    isNeedUpdate |= processWcdmaEcioAlaphFilter(oldDoubleSs, newSs, modemSs, isNeedProcessDescend);
                }
            }
            boolean isGsmPhone = true;
            if ((this.mTechState & 1) != 0) {
                isNeedUpdate |= processGsmSignalStrengthAlaphFilter(oldDoubleSs, newSs, modemSs, isNeedProcessDescend);
            }
            if (TelephonyManagerEx.getCurrentPhoneTypeForSlot(HwGsmSignalStrengthManager.this.mPhoneId) != 1) {
                isGsmPhone = false;
            }
            if (isGsmPhone) {
                setFakeSignalStrengthForSlowDescend(this, newSs);
                SignalStrengthEx.setPhoneId(newSs, HwGsmSignalStrengthManager.this.mPhoneId);
                HwGsmSignalStrengthManager.this.clearNrSignalStrength(isNeedProcessDescend, newSs);
                SignalStrengthEx.updateLevel(newSs, HwGsmSignalStrengthManager.this.mServiceStateTracker.getCarrierConfigHw(), HwGsmSignalStrengthManager.this.mServiceStateTracker.getmSSHw());
                HwGsmSignalStrengthManager.this.mServiceStateTracker.setSignalStrength(newSs);
                if (isNeedUpdate) {
                    HwGsmSignalStrengthManager.this.sendMessageDelayUpdateSingalStrength(this.mDelayTime);
                }
            } else if (HwGsmSignalStrengthManager.this.hasMessages(HwGsmSignalStrengthManager.EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH)) {
                HwGsmSignalStrengthManager.this.removeMessages(HwGsmSignalStrengthManager.EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH);
            }
        }

        private void setFakeSignalStrengthForSlowDescend(HwGsmDoubleSignalStrength oldDoubleSs, SignalStrength newSs) {
            this.mDelayTime = 0;
            if (HwGsmSignalStrengthManager.this.mHwSigStr == null) {
                HwGsmSignalStrengthManager.this.loge("mHwSigStr is null");
                return;
            }
            processNrFakeSignalStrengthForSlowDescend(HwGsmSignalStrengthManager.this.mHwSigStr, oldDoubleSs, newSs);
            processLteFakeSignalStrengthForSlowDescend(HwGsmSignalStrengthManager.this.mHwSigStr, oldDoubleSs, newSs, true);
            processUmtsFakeSignalStrengthForSlowDescend(oldDoubleSs, newSs);
            processGsmFakeSignalStrengthForSlowDescend(oldDoubleSs, newSs);
        }

        private void processUmtsFakeSignalStrengthForSlowDescend(HwGsmDoubleSignalStrength oldDoubleSs, SignalStrength newSs) {
            HwSignalStrength.SignalThreshold signalThreshold;
            int wcdmaEcio;
            if ((this.mTechState & 2) != 0) {
                int oldLevel = HwGsmSignalStrengthManager.this.mHwSigStr.getLevel(HwSignalStrength.SignalType.UMTS, (int) oldDoubleSs.getOldDoubleWcdmaRscp(), (int) oldDoubleSs.getOldDoubleWcdmaEcio());
                int newLevel = HwGsmSignalStrengthManager.this.mHwSigStr.getLevel(HwSignalStrength.SignalType.UMTS, SignalStrengthEx.getWcdmaRscp(newSs), SignalStrengthEx.getWcdmaEcio(newSs));
                int diffLevel = oldLevel - newLevel;
                HwGsmSignalStrengthManager.this.logd("UMTS oldLevel: " + oldLevel + ", newLevel: " + newLevel);
                if (diffLevel > 1 && (signalThreshold = HwGsmSignalStrengthManager.this.mHwSigStr.getSignalThreshold(HwSignalStrength.SignalType.UMTS)) != null) {
                    int lowerLevel = oldLevel - 1;
                    int wcdmaRscp = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, false);
                    if (wcdmaRscp != -1) {
                        this.mDoubleWcdmaRscp = (double) wcdmaRscp;
                        SignalStrengthEx.setWcdmaRscp(newSs, wcdmaRscp);
                        this.mDelayTime = 6000 / diffLevel;
                    }
                    HwGsmSignalStrengthManager.this.logd("UMTS lowerLevel: " + lowerLevel + ", wcdmaRscp: " + wcdmaRscp);
                    if (HwGsmSignalStrengthManager.IS_FEATURE_SIGNAL_DUALPARAM && (wcdmaEcio = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, true)) != -1) {
                        this.mDoubleWcdmaEcio = (double) wcdmaEcio;
                        SignalStrengthEx.setWcdmaEcio(newSs, wcdmaEcio);
                        this.mDelayTime = 6000 / diffLevel;
                    }
                }
            }
        }

        private void processGsmFakeSignalStrengthForSlowDescend(HwGsmDoubleSignalStrength oldDoubleSs, SignalStrength newSs) {
            HwSignalStrength.SignalThreshold signalThreshold;
            if ((this.mTechState & 1) != 0) {
                int oldLevel = HwGsmSignalStrengthManager.this.mHwSigStr.getLevel(HwSignalStrength.SignalType.GSM, (int) oldDoubleSs.getOldDoubleGsmSignalStrength(), 255);
                int newLevel = HwGsmSignalStrengthManager.this.mHwSigStr.getLevel(HwSignalStrength.SignalType.GSM, newSs.getGsmSignalStrength(), 255);
                int diffLevel = oldLevel - newLevel;
                HwGsmSignalStrengthManager.this.logd("GSM oldLevel: " + oldLevel + ", newLevel: " + newLevel);
                if (diffLevel > 1 && (signalThreshold = HwGsmSignalStrengthManager.this.mHwSigStr.getSignalThreshold(HwSignalStrength.SignalType.GSM)) != null) {
                    int lowerLevel = oldLevel - 1;
                    int gsmSs = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, false);
                    if (gsmSs != -1) {
                        this.mDoubleGsmSs = (double) gsmSs;
                        SignalStrengthEx.setGsmSignalStrength(newSs, gsmSs);
                        this.mDelayTime = 6000 / diffLevel;
                    }
                    HwGsmSignalStrengthManager.this.logd("GSM lowerLevel: " + lowerLevel + ", gsmSs: " + gsmSs);
                }
            }
        }
    }
}
