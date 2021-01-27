package com.android.internal.telephony.cdma;

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
import com.huawei.internal.telephony.PhoneConstantsEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;

public class HwCdmaSignalStrengthManager extends HwBaseSignalStrengthManager {
    private static final int CDMA_STRENGTH_POOR_STD = SystemPropertiesEx.getInt("ro.cdma.poorstd", (int) C_STRENGTH_POOR_STD);
    private static final int C_STRENGTH_POOR_STD = -112;
    private static final int EVENT_BASE = 3000;
    private static final int EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH = 3001;
    private static final int EVENT_DELAY_UPDATE_REGISTER_STATE_DONE = 3002;
    private static final boolean IS_CMCC_4G_DSDX_ENABLE = SystemPropertiesEx.getBoolean("ro.hwpp.cmcc_4G_dsdx_enable", false);
    private static final String LOG_TAG = "HwCdmaSignalStrengthManager";
    private HwCdmaDoubleSignalStrength mDoubleSignalStrength;
    private HwSignalStrength mHwSigStr = HwSignalStrength.getInstance(this.mPhoneId, this.mContext);
    private SignalStrength mModemSignalStrength;
    private HwCdmaDoubleSignalStrength mOldDoubleSignalStrength;

    public HwCdmaSignalStrengthManager(IServiceStateTrackerInner serviceStateTracker, PhoneExt phoneExt, HwServiceStateTrackerEx hwServiceStateTrackerEx) {
        super(serviceStateTracker, phoneExt, hwServiceStateTrackerEx);
        this.mTag = "HwCdmaSignalStrengthManager[" + this.mPhoneId + "]";
    }

    @Override // com.android.internal.telephony.HwBaseSignalStrengthManager, android.os.Handler
    public void handleMessage(Message msg) {
        if (msg.what != EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH) {
            super.handleMessage(msg);
        } else {
            handleDelayUpdateGsmSingalStrength();
        }
    }

    private void handleDelayUpdateGsmSingalStrength() {
        logd("event update cdma&lte signal strength");
        this.mDoubleSignalStrength.proccessAlaphFilter(this.mServiceStateTracker.getSignalStrength(), this.mModemSignalStrength);
        this.mPhone.notifySignalStrength();
    }

    public boolean proccessCdmaLteDelayUpdateRegisterStateDone(ServiceState oldSs, ServiceState newSs) {
        int delayedTime;
        if (HwModemCapability.isCapabilitySupport(6)) {
            return false;
        }
        if (this.mHwServiceStateTrackerEx.needDelayForRatChanged(oldSs, newSs)) {
            return true;
        }
        boolean isNetworkOutofService = ((ServiceStateEx.getVoiceRegState(oldSs) != 0 && ServiceStateEx.getDataState(oldSs) != 0) || ServiceStateEx.getVoiceRegState(newSs) == 0 || ServiceStateEx.getDataState(newSs) == 0) ? false : true;
        int newMainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        boolean isInService = ServiceStateEx.getDataState(newSs) == 0 || (ServiceStateEx.getVoiceRegState(newSs) == 0 && HwFullNetworkManager.getInstance().isCMCCDsdxEnable());
        PhoneExt phoneExt = PhoneFactoryExt.getPhone(this.mPhone.getPhoneId());
        int callState = phoneExt == null ? PhoneConstantsEx.StateEx.IDLE.ordinal() : phoneExt.getState();
        logd("process delay update register state isLostNetwork : " + isNetworkOutofService + ", callState :" + callState);
        if (isInService || needCancelDelay() || ServiceStateEx.getDataState(newSs) == 3 || callState != PhoneConstantsEx.StateEx.IDLE.ordinal()) {
            this.mMainSlot = newMainSlot;
            cancelDeregisterStateDelayTimer();
        } else if (hasMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE)) {
            return true;
        } else {
            if (isNetworkOutofService && !this.mIsRefreshState && (delayedTime = getSendDeregisterStateDelayedTime(oldSs, newSs)) > 0) {
                delaySendDeregisterStateChange(delayedTime);
                return true;
            }
        }
        this.mIsRefreshState = false;
        return false;
    }

    private int getSendDeregisterStateDelayedTime(ServiceState oldSs, ServiceState newSs) {
        boolean isCsLostNetwork = true;
        boolean isPsLostNetwork = ServiceStateEx.getDataState(oldSs) == 0 && ServiceStateEx.getDataState(newSs) != 0;
        int delayedTime = 0;
        int networkClass = TelephonyManagerEx.getNetworkClass(getNetworkType(oldSs));
        if (!isPsLostNetwork) {
            if (ServiceStateEx.getVoiceRegState(oldSs) != 0 || ServiceStateEx.getVoiceRegState(newSs) == 0) {
                isCsLostNetwork = false;
            }
            if (isCsLostNetwork) {
                delayedTime = this.delayedTimeDefaultValue * 1000;
            } else {
                logd("use default delay time.");
            }
        } else if (networkClass == 3) {
            delayedTime = this.delayedTimeNetworkstatusPs4G;
        } else {
            delayedTime = this.delayedTimeDefaultValue * 1000;
        }
        logd("delay time = " + delayedTime);
        return delayedTime;
    }

    public boolean notifySignalStrength(SignalStrength oldSs, SignalStrength newSs) {
        boolean isNotified;
        if (hasMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE) || this.mHwServiceStateTrackerEx.hasRatChangedDelayMessage()) {
            logd("In delay update register state process, no notify signal");
            if (hasMessages(EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH)) {
                removeMessages(EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH);
            }
            return false;
        }
        this.mModemSignalStrength = SignalStrengthEx.newSignalStrength(newSs);
        logd("Process notify signal strenght! ver.02");
        if (isNetworkTypeChanged(oldSs, newSs)) {
            logd("Network is changed immediately!");
            if (hasMessages(EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH)) {
                removeMessages(EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH);
            }
            this.mDoubleSignalStrength = new HwCdmaDoubleSignalStrength(newSs);
            SignalStrengthEx.setPhoneId(newSs, this.mPhoneId);
            SignalStrengthEx.updateLevel(newSs, this.mServiceStateTracker.getCarrierConfigHw(), this.mServiceStateTracker.getmSSHw());
            this.mServiceStateTracker.setSignalStrength(newSs);
            isNotified = true;
        } else if (hasMessages(EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH)) {
            logd("has delay update message, don't proccess alpha filter immediately!");
            isNotified = false;
        } else {
            this.mOldDoubleSignalStrength = this.mDoubleSignalStrength;
            this.mDoubleSignalStrength = new HwCdmaDoubleSignalStrength(newSs);
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
        if (this.mIsSimStateChange) {
            isChanged = true;
            this.mIsSimStateChange = false;
        } else if (oldSs.isGsm() != newSs.isGsm()) {
            isChanged = true;
        } else {
            if (newSs.getCdmaDbm() < -1) {
                newState = 0 | 8;
            }
            if (oldSs.getCdmaDbm() < -1) {
                oldState = 0 | 8;
            }
            if (newSs.isGsm()) {
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
            } else {
                if (newSs.getEvdoDbm() < -1) {
                    newState |= 16;
                }
                if (oldSs.getEvdoDbm() < -1) {
                    oldState |= 16;
                }
            }
            if (SignalStrengthEx.getHwNrRsrp(oldSs) == -140 && SignalStrengthEx.getHwNrRsrp(newSs) != -140) {
                isChanged = true;
            }
            if (!(newState == 0 || newState == oldState)) {
                isChanged = true;
            }
        }
        logd("isNetworkTypeChanged: " + isChanged);
        return isChanged;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMessageDelayUpdateSingalStrength(int time) {
        logd("sendMessageDelayUpdateSingalStrength, time: " + time);
        Message msg = obtainMessage();
        msg.what = EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH;
        if (time == 0) {
            sendMessageDelayed(msg, (long) DELAY_DURING_TIME);
        } else {
            sendMessageDelayed(msg, (long) time);
        }
    }

    public class HwCdmaDoubleSignalStrength extends HwBaseSignalStrengthManager.HwBaseDoubleSignalStrength {
        private double mDoubleCdmaDbm;
        private double mDoubleCdmaEcio;
        private double mDoubleEvdoDbm;
        private double mDoubleEvdoEcio;
        private double mDoubleEvdoSnr;
        private double mOldDoubleCdmaDbm;
        private double mOldDoubleCdmaEcio;
        private double mOldDoubleEvdoDbm;
        private double mOldDoubleEvdoEcio;
        private double mOldDoubleEvdoSnr;

        HwCdmaDoubleSignalStrength(SignalStrength ss) {
            super(ss);
            this.mDoubleCdmaDbm = (double) ss.getCdmaDbm();
            this.mDoubleCdmaEcio = (double) ss.getCdmaEcio();
            this.mDoubleEvdoDbm = (double) ss.getEvdoDbm();
            this.mDoubleEvdoEcio = (double) ss.getEvdoEcio();
            this.mDoubleEvdoSnr = (double) ss.getEvdoSnr();
            this.mTechState = 0;
            if (ss.getCdmaDbm() < -1) {
                this.mTechState |= 8;
            }
            if (ss.isGsm()) {
                if (SignalStrengthEx.getLteRsrp(ss) < -1) {
                    this.mTechState |= 4;
                }
                if (SignalStrengthEx.getNrRsrp(ss) < -1) {
                    this.mTechState |= 32;
                }
            } else if (ss.getEvdoDbm() < -1) {
                this.mTechState |= 16;
            }
            this.mOldDoubleCdmaDbm = this.mDoubleCdmaDbm;
            this.mOldDoubleCdmaEcio = this.mDoubleCdmaEcio;
            this.mOldDoubleEvdoDbm = this.mDoubleEvdoDbm;
            this.mOldDoubleEvdoEcio = this.mDoubleEvdoEcio;
            this.mOldDoubleEvdoSnr = this.mDoubleEvdoSnr;
        }

        public HwCdmaDoubleSignalStrength(HwCdmaDoubleSignalStrength doubleSs) {
            super();
            this.mDoubleNrRsrp = doubleSs.mDoubleNrRsrp;
            this.mDoubleNrRssnr = doubleSs.mDoubleNrRssnr;
            this.mDoubleLteRsrp = doubleSs.mDoubleLteRsrp;
            this.mDoubleLteRssnr = doubleSs.mDoubleLteRssnr;
            this.mDoubleCdmaDbm = doubleSs.mDoubleCdmaDbm;
            this.mDoubleCdmaEcio = doubleSs.mDoubleCdmaEcio;
            this.mDoubleEvdoDbm = doubleSs.mDoubleEvdoDbm;
            this.mDoubleEvdoEcio = doubleSs.mDoubleEvdoEcio;
            this.mDoubleEvdoSnr = doubleSs.mDoubleEvdoSnr;
            this.mTechState = doubleSs.mTechState;
            this.mOldDoubleNrRsrp = doubleSs.mDoubleNrRsrp;
            this.mOldDoubleNrRssnr = doubleSs.mDoubleNrRssnr;
            this.mOldDoubleLteRsrp = doubleSs.mOldDoubleLteRsrp;
            this.mOldDoubleLteRssnr = doubleSs.mOldDoubleLteRssnr;
            this.mOldDoubleCdmaDbm = doubleSs.mOldDoubleCdmaDbm;
            this.mOldDoubleCdmaEcio = doubleSs.mOldDoubleCdmaEcio;
            this.mOldDoubleEvdoDbm = doubleSs.mOldDoubleEvdoDbm;
            this.mOldDoubleEvdoEcio = doubleSs.mOldDoubleEvdoEcio;
            this.mOldDoubleEvdoSnr = doubleSs.mOldDoubleEvdoSnr;
            this.mDelayTime = doubleSs.mDelayTime;
        }

        /* access modifiers changed from: package-private */
        public double getDoubleCdmaDbm() {
            return this.mDoubleCdmaDbm;
        }

        /* access modifiers changed from: package-private */
        public double getDoubleCdmaEcio() {
            return this.mDoubleCdmaEcio;
        }

        /* access modifiers changed from: package-private */
        public double getDoubleEvdoDbm() {
            return this.mDoubleEvdoDbm;
        }

        /* access modifiers changed from: package-private */
        public double getDoubleEvdoEcio() {
            return this.mDoubleEvdoEcio;
        }

        /* access modifiers changed from: package-private */
        public double getDoubleEvdoSnr() {
            return this.mDoubleEvdoSnr;
        }

        /* access modifiers changed from: package-private */
        public double getOldDoubleCdmaDbm() {
            return this.mOldDoubleCdmaDbm;
        }

        /* access modifiers changed from: package-private */
        public double getOldDoubleCdmaEcio() {
            return this.mOldDoubleCdmaEcio;
        }

        /* access modifiers changed from: package-private */
        public double getOldDoubleEvdoDbm() {
            return this.mOldDoubleEvdoDbm;
        }

        /* access modifiers changed from: package-private */
        public double getOldDoubleEvdoEcio() {
            return this.mOldDoubleEvdoEcio;
        }

        /* access modifiers changed from: package-private */
        public double getOldDoubleEvdoSnr() {
            return this.mOldDoubleEvdoSnr;
        }

        /* access modifiers changed from: package-private */
        public boolean processCdmaDbmAlaphFilter(HwCdmaDoubleSignalStrength oldDoubleSs, SignalStrength newSs, SignalStrength modemSs, boolean isNeedProcessDescend) {
            double oldCdmaDbm = oldDoubleSs.getDoubleCdmaDbm();
            double modemCdmaDbm = (double) modemSs.getCdmaDbm();
            this.mOldDoubleCdmaDbm = oldCdmaDbm;
            HwCdmaSignalStrengthManager hwCdmaSignalStrengthManager = HwCdmaSignalStrengthManager.this;
            hwCdmaSignalStrengthManager.logd("Before processCdmaDbmAlaphFilter -- old : " + oldCdmaDbm + "; instant new : " + modemCdmaDbm);
            if (modemCdmaDbm > -1.0d) {
                modemCdmaDbm = (double) HwCdmaSignalStrengthManager.CDMA_STRENGTH_POOR_STD;
            }
            if (oldCdmaDbm <= modemCdmaDbm) {
                this.mDoubleCdmaDbm = modemCdmaDbm;
            } else if (isNeedProcessDescend) {
                this.mDoubleCdmaDbm = ((7.0d * oldCdmaDbm) + (5.0d * modemCdmaDbm)) / 12.0d;
            } else {
                this.mDoubleCdmaDbm = oldCdmaDbm;
            }
            HwCdmaSignalStrengthManager hwCdmaSignalStrengthManager2 = HwCdmaSignalStrengthManager.this;
            hwCdmaSignalStrengthManager2.logd("CdmaDbmAlaphFilter modem : " + modemCdmaDbm + "; old : " + oldCdmaDbm + "; new : " + this.mDoubleCdmaDbm);
            double d = this.mDoubleCdmaDbm;
            if (d - modemCdmaDbm <= -1.0d || d - modemCdmaDbm >= 1.0d) {
                SignalStrengthEx.setCdmaDbm(newSs, (int) this.mDoubleCdmaDbm);
                return true;
            }
            this.mDoubleCdmaDbm = modemCdmaDbm;
            SignalStrengthEx.setCdmaDbm(newSs, (int) this.mDoubleCdmaDbm);
            return false;
        }

        private boolean processCdmaEcioAlaphFilter(HwCdmaDoubleSignalStrength oldDoubleSs, SignalStrength newSs, SignalStrength modemSs, boolean isNeedProcessDescend) {
            double oldCdmaEcio = oldDoubleSs.getDoubleCdmaEcio();
            double modemCdmaEcio = (double) modemSs.getCdmaEcio();
            this.mOldDoubleCdmaEcio = oldCdmaEcio;
            HwCdmaSignalStrengthManager hwCdmaSignalStrengthManager = HwCdmaSignalStrengthManager.this;
            hwCdmaSignalStrengthManager.logd("Before processCdmaEcioAlaphFilter -- old : " + oldCdmaEcio + "; instant new : " + modemCdmaEcio);
            if (oldCdmaEcio <= modemCdmaEcio) {
                this.mDoubleCdmaEcio = modemCdmaEcio;
            } else if (isNeedProcessDescend) {
                this.mDoubleCdmaEcio = (0.85d * oldCdmaEcio) + (0.15d * modemCdmaEcio);
            } else {
                this.mDoubleCdmaEcio = oldCdmaEcio;
            }
            HwCdmaSignalStrengthManager hwCdmaSignalStrengthManager2 = HwCdmaSignalStrengthManager.this;
            hwCdmaSignalStrengthManager2.logd("CdmaEcioAlaphFilter modem : " + modemCdmaEcio + "; old : " + oldCdmaEcio + "; new : " + this.mDoubleCdmaEcio);
            double d = this.mDoubleCdmaEcio;
            if (d - modemCdmaEcio <= -1.0d || d - modemCdmaEcio >= 1.0d) {
                SignalStrengthEx.setCdmaEcio(newSs, (int) this.mDoubleCdmaEcio);
                return true;
            }
            this.mDoubleCdmaEcio = modemCdmaEcio;
            SignalStrengthEx.setCdmaEcio(newSs, (int) this.mDoubleCdmaEcio);
            return false;
        }

        private boolean processEvdoDbmAlaphFilter(HwCdmaDoubleSignalStrength oldDoubleSs, SignalStrength newSs, SignalStrength modemSs, boolean isNeedProcessDescend) {
            double oldEvdoDbm = oldDoubleSs.getDoubleEvdoDbm();
            double modemEvdoDbm = (double) modemSs.getEvdoDbm();
            this.mOldDoubleEvdoDbm = oldEvdoDbm;
            HwCdmaSignalStrengthManager hwCdmaSignalStrengthManager = HwCdmaSignalStrengthManager.this;
            hwCdmaSignalStrengthManager.logd("Before processEvdoDbmAlaphFilter -- old : " + oldEvdoDbm + "; instant new : " + modemEvdoDbm);
            if (modemEvdoDbm > -1.0d) {
                modemEvdoDbm = (double) HwCdmaSignalStrengthManager.CDMA_STRENGTH_POOR_STD;
            }
            if (oldEvdoDbm <= modemEvdoDbm) {
                this.mDoubleEvdoDbm = modemEvdoDbm;
            } else if (isNeedProcessDescend) {
                this.mDoubleEvdoDbm = ((7.0d * oldEvdoDbm) + (5.0d * modemEvdoDbm)) / 12.0d;
            } else {
                this.mDoubleEvdoDbm = oldEvdoDbm;
            }
            HwCdmaSignalStrengthManager hwCdmaSignalStrengthManager2 = HwCdmaSignalStrengthManager.this;
            hwCdmaSignalStrengthManager2.logd("EvdoDbmAlaphFilter modem : " + modemEvdoDbm + "; old : " + oldEvdoDbm + "; new : " + this.mDoubleEvdoDbm);
            double d = this.mDoubleEvdoDbm;
            if (d - modemEvdoDbm <= -1.0d || d - modemEvdoDbm >= 1.0d) {
                SignalStrengthEx.setEvdoDbm(newSs, (int) this.mDoubleEvdoDbm);
                return true;
            }
            this.mDoubleEvdoDbm = modemEvdoDbm;
            SignalStrengthEx.setEvdoDbm(newSs, (int) this.mDoubleEvdoDbm);
            return false;
        }

        private boolean processEvdoEcioAlaphFilter(HwCdmaDoubleSignalStrength oldDoubleSs, SignalStrength newSs, SignalStrength modemSs, boolean isNeedProcessDescend) {
            double oldEvdoEcio = oldDoubleSs.getDoubleEvdoEcio();
            double modemEvdoEcio = (double) modemSs.getEvdoEcio();
            this.mOldDoubleEvdoEcio = oldEvdoEcio;
            HwCdmaSignalStrengthManager hwCdmaSignalStrengthManager = HwCdmaSignalStrengthManager.this;
            hwCdmaSignalStrengthManager.logd("Before processEvdoEcioAlaphFilter -- old : " + oldEvdoEcio + "; instant new : " + modemEvdoEcio);
            if (oldEvdoEcio <= modemEvdoEcio) {
                this.mDoubleEvdoEcio = modemEvdoEcio;
            } else if (isNeedProcessDescend) {
                this.mDoubleEvdoEcio = (0.85d * oldEvdoEcio) + (0.15d * modemEvdoEcio);
            } else {
                this.mDoubleEvdoEcio = oldEvdoEcio;
            }
            HwCdmaSignalStrengthManager hwCdmaSignalStrengthManager2 = HwCdmaSignalStrengthManager.this;
            hwCdmaSignalStrengthManager2.logd("EvdoEcioAlaphFilter modem : " + modemEvdoEcio + "; old : " + oldEvdoEcio + "; new : " + this.mDoubleEvdoEcio);
            double d = this.mDoubleEvdoEcio;
            if (d - modemEvdoEcio <= -1.0d || d - modemEvdoEcio >= 1.0d) {
                SignalStrengthEx.setEvdoEcio(newSs, (int) this.mDoubleEvdoEcio);
                return true;
            }
            this.mDoubleEvdoEcio = modemEvdoEcio;
            SignalStrengthEx.setEvdoEcio(newSs, (int) this.mDoubleEvdoEcio);
            return false;
        }

        private boolean processEvdoSnrAlaphFilter(HwCdmaDoubleSignalStrength oldDoubleSs, SignalStrength newSs, SignalStrength modemSs, boolean isNeedProcessDescend) {
            double oldEvdoSnr = oldDoubleSs.getDoubleEvdoSnr();
            double modemEvdoSnr = (double) modemSs.getEvdoSnr();
            this.mOldDoubleEvdoSnr = oldEvdoSnr;
            HwCdmaSignalStrengthManager hwCdmaSignalStrengthManager = HwCdmaSignalStrengthManager.this;
            hwCdmaSignalStrengthManager.logd("Before processEvdoSnrAlaphFilter -- old : " + oldEvdoSnr + "; instant new : " + modemEvdoSnr);
            if (oldEvdoSnr <= modemEvdoSnr) {
                this.mDoubleEvdoSnr = modemEvdoSnr;
            } else if (isNeedProcessDescend) {
                this.mDoubleEvdoSnr = (0.85d * oldEvdoSnr) + (0.15d * modemEvdoSnr);
            } else {
                this.mDoubleEvdoSnr = oldEvdoSnr;
            }
            HwCdmaSignalStrengthManager hwCdmaSignalStrengthManager2 = HwCdmaSignalStrengthManager.this;
            hwCdmaSignalStrengthManager2.logd("EvdoSnrAlaphFilter modem : " + modemEvdoSnr + "; old : " + oldEvdoSnr + "; new : " + this.mDoubleEvdoSnr);
            double d = this.mDoubleEvdoSnr;
            if (d - modemEvdoSnr <= -1.0d || d - modemEvdoSnr >= 1.0d) {
                SignalStrengthEx.setEvdoSnr(newSs, (int) this.mDoubleEvdoSnr);
                return true;
            }
            this.mDoubleEvdoSnr = modemEvdoSnr;
            SignalStrengthEx.setEvdoSnr(newSs, (int) this.mDoubleEvdoSnr);
            return false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void proccessAlaphFilter(SignalStrength newSs, SignalStrength modemSs) {
            proccessAlaphFilter(this, newSs, modemSs, true);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void proccessAlaphFilter(HwCdmaDoubleSignalStrength oldDoubleSs, SignalStrength newSs, SignalStrength modemSs, boolean isNeedProcessDescend) {
            boolean isNeedUpdate = false;
            if (oldDoubleSs == null) {
                HwCdmaSignalStrengthManager.this.logd("proccess oldDoubleSs is null");
                return;
            }
            if (newSs.isGsm()) {
                if ((this.mTechState & 32) != 0) {
                    isNeedUpdate = false | processNrRsrpAlaphFilter(oldDoubleSs, newSs, modemSs, isNeedProcessDescend);
                    if (HwCdmaSignalStrengthManager.IS_FEATURE_SIGNAL_DUALPARAM) {
                        isNeedUpdate |= processNrRssnrAlaphFilter(oldDoubleSs, newSs, modemSs, isNeedProcessDescend);
                    }
                }
                if ((this.mTechState & 4) != 0) {
                    isNeedUpdate |= processLteRsrpAlaphFilter(oldDoubleSs, newSs, modemSs, isNeedProcessDescend);
                    if (HwCdmaSignalStrengthManager.IS_FEATURE_SIGNAL_DUALPARAM) {
                        isNeedUpdate |= processLteRssnrAlaphFilter(oldDoubleSs, newSs, modemSs, isNeedProcessDescend);
                    }
                }
            } else if ((this.mTechState & 16) != 0) {
                isNeedUpdate = false | processEvdoDbmAlaphFilter(oldDoubleSs, newSs, modemSs, isNeedProcessDescend);
                if (HwCdmaSignalStrengthManager.IS_FEATURE_SIGNAL_DUALPARAM) {
                    isNeedUpdate = isNeedUpdate | processEvdoEcioAlaphFilter(oldDoubleSs, newSs, modemSs, isNeedProcessDescend) | processEvdoSnrAlaphFilter(oldDoubleSs, newSs, modemSs, isNeedProcessDescend);
                }
            }
            if ((this.mTechState & 8) != 0) {
                isNeedUpdate |= processCdmaDbmAlaphFilter(oldDoubleSs, newSs, modemSs, isNeedProcessDescend);
                if (HwCdmaSignalStrengthManager.IS_FEATURE_SIGNAL_DUALPARAM) {
                    isNeedUpdate |= processCdmaEcioAlaphFilter(oldDoubleSs, newSs, modemSs, isNeedProcessDescend);
                }
            }
            int phoneType = TelephonyManagerEx.getCurrentPhoneTypeForSlot(HwCdmaSignalStrengthManager.this.mPhoneId);
            if (phoneType == 2 || phoneType == 6) {
                setFakeSignalStrengthForSlowDescend(this, newSs);
                SignalStrengthEx.setPhoneId(newSs, HwCdmaSignalStrengthManager.this.mPhoneId);
                HwCdmaSignalStrengthManager.this.clearNrSignalStrength(isNeedProcessDescend, newSs);
                SignalStrengthEx.updateLevel(newSs, HwCdmaSignalStrengthManager.this.mServiceStateTracker.getCarrierConfigHw(), HwCdmaSignalStrengthManager.this.mServiceStateTracker.getmSSHw());
                HwCdmaSignalStrengthManager.this.mServiceStateTracker.setSignalStrength(newSs);
                if (isNeedUpdate) {
                    HwCdmaSignalStrengthManager.this.sendMessageDelayUpdateSingalStrength(this.mDelayTime);
                }
            } else if (HwCdmaSignalStrengthManager.this.hasMessages(HwCdmaSignalStrengthManager.EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH)) {
                HwCdmaSignalStrengthManager.this.removeMessages(HwCdmaSignalStrengthManager.EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH);
            }
        }

        private void setFakeSignalStrengthForSlowDescend(HwCdmaDoubleSignalStrength oldDoubleSs, SignalStrength newSs) {
            this.mDelayTime = 0;
            if (newSs.isGsm()) {
                processNrFakeSignalStrengthForSlowDescend(HwCdmaSignalStrengthManager.this.mHwSigStr, oldDoubleSs, newSs);
                processLteFakeSignalStrengthForSlowDescend(HwCdmaSignalStrengthManager.this.mHwSigStr, oldDoubleSs, newSs, false);
            } else {
                processEvdoFakeSignalStrengthForSlowDescend(oldDoubleSs, newSs);
            }
            processCdmaFakeSignalStrengthForSlowDescend(oldDoubleSs, newSs);
        }

        private void processEvdoFakeSignalStrengthForSlowDescend(HwCdmaDoubleSignalStrength oldDoubleSs, SignalStrength newSs) {
            HwSignalStrength.SignalThreshold signalThreshold;
            int evdoSnr;
            if ((this.mTechState & 16) != 0) {
                int oldLevel = HwCdmaSignalStrengthManager.this.mHwSigStr.getLevel(HwSignalStrength.SignalType.EVDO, (int) oldDoubleSs.getOldDoubleEvdoDbm(), (int) oldDoubleSs.getOldDoubleEvdoSnr());
                int newLevel = HwCdmaSignalStrengthManager.this.mHwSigStr.getLevel(HwSignalStrength.SignalType.EVDO, newSs.getEvdoDbm(), newSs.getEvdoSnr());
                int diffLevel = oldLevel - newLevel;
                HwCdmaSignalStrengthManager.this.logd("EVDO oldLevel: " + oldLevel + ", newLevel: " + newLevel);
                if (diffLevel > 1 && (signalThreshold = HwCdmaSignalStrengthManager.this.mHwSigStr.getSignalThreshold(HwSignalStrength.SignalType.EVDO)) != null) {
                    int lowerLevel = oldLevel - 1;
                    int evdoDbm = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, false);
                    if (evdoDbm != -1) {
                        this.mDoubleEvdoDbm = (double) evdoDbm;
                        SignalStrengthEx.setEvdoDbm(newSs, evdoDbm);
                        this.mDelayTime = 6000 / diffLevel;
                    }
                    HwCdmaSignalStrengthManager.this.logd("EVDO lowerLevel: " + lowerLevel + ", evdoDbm: " + evdoDbm);
                    if (HwCdmaSignalStrengthManager.IS_FEATURE_SIGNAL_DUALPARAM && (evdoSnr = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, true)) != -1) {
                        this.mDoubleEvdoSnr = (double) evdoSnr;
                        SignalStrengthEx.setEvdoSnr(newSs, evdoSnr);
                        this.mDelayTime = 6000 / diffLevel;
                    }
                }
            }
        }

        private void processCdmaFakeSignalStrengthForSlowDescend(HwCdmaDoubleSignalStrength oldDoubleSs, SignalStrength newSs) {
            HwSignalStrength.SignalThreshold signalThreshold;
            int cdmaEcio;
            if ((this.mTechState & 8) != 0) {
                int oldLevel = HwCdmaSignalStrengthManager.this.mHwSigStr.getLevel(HwSignalStrength.SignalType.CDMA, (int) oldDoubleSs.getOldDoubleCdmaDbm(), (int) oldDoubleSs.getOldDoubleCdmaEcio());
                int newLevel = HwCdmaSignalStrengthManager.this.mHwSigStr.getLevel(HwSignalStrength.SignalType.CDMA, newSs.getCdmaDbm(), newSs.getCdmaEcio());
                int diffLevel = oldLevel - newLevel;
                HwCdmaSignalStrengthManager.this.logd("CDMA oldLevel: " + oldLevel + ", newLevel: " + newLevel);
                if (diffLevel > 1 && (signalThreshold = HwCdmaSignalStrengthManager.this.mHwSigStr.getSignalThreshold(HwSignalStrength.SignalType.CDMA)) != null) {
                    int lowerLevel = oldLevel - 1;
                    int cdmaDbm = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, false);
                    if (cdmaDbm != -1) {
                        this.mDoubleCdmaDbm = (double) cdmaDbm;
                        SignalStrengthEx.setCdmaDbm(newSs, cdmaDbm);
                        this.mDelayTime = 6000 / diffLevel;
                    }
                    HwCdmaSignalStrengthManager.this.logd("CDMA lowerLevel: " + lowerLevel + ", cdmaDbm: " + cdmaDbm);
                    if (HwCdmaSignalStrengthManager.IS_FEATURE_SIGNAL_DUALPARAM && (cdmaEcio = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, true)) != -1) {
                        this.mDoubleCdmaEcio = (double) cdmaEcio;
                        SignalStrengthEx.setCdmaEcio(newSs, cdmaEcio);
                        this.mDelayTime = 6000 / diffLevel;
                    }
                }
            }
        }
    }
}
