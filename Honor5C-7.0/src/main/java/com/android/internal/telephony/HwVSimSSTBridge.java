package com.android.internal.telephony;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Message;
import android.os.UserHandle;
import android.provider.HwTelephony.VirtualNets;
import android.telephony.CellLocation;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import java.io.FileDescriptor;
import java.io.PrintWriter;

abstract class HwVSimSSTBridge extends ServiceStateTracker {
    private static final /* synthetic */ int[] -com-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues = null;
    private String LOG_TAG;
    private String mCurDataSpn;
    private String mCurPlmn;
    private boolean mCurShowPlmn;
    private boolean mCurShowSpn;
    private String mCurSpn;
    private CellLocation mNewCellLoc;
    private GsmCdmaPhone mPhone;

    private static /* synthetic */ int[] -getcom-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues() {
        if (-com-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues != null) {
            return -com-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues;
        }
        int[] iArr = new int[RadioState.values().length];
        try {
            iArr[RadioState.RADIO_OFF.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[RadioState.RADIO_ON.ordinal()] = 3;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[RadioState.RADIO_UNAVAILABLE.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        -com-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues = iArr;
        return iArr;
    }

    protected abstract Intent createSpnIntent();

    protected abstract String getNetworkOperatorForPhone(TelephonyManager telephonyManager, int i);

    protected abstract UiccCard getUiccCard();

    protected abstract UiccCardApplication getVSimUiccCardApplication();

    protected abstract void initUiccController();

    protected abstract boolean isUiccControllerValid();

    protected abstract void putPhoneIdAndSubIdExtra(Intent intent, int i);

    protected abstract void registerForIccChanged();

    protected abstract void setNetworkCountryIsoForPhone(TelephonyManager telephonyManager, int i, String str);

    protected abstract void setNetworkOperatorNumericForPhone(TelephonyManager telephonyManager, int i, String str);

    protected abstract void unregisterForIccChanged();

    protected abstract void updateVSimOperatorProp();

    public HwVSimSSTBridge(GsmCdmaPhone phone, CommandsInterface ci) {
        super(phone, ci);
        this.LOG_TAG = "VSimSSTBridge";
        this.mCurSpn = null;
        this.mCurDataSpn = null;
        this.mCurPlmn = null;
        this.mCurShowPlmn = false;
        this.mCurShowSpn = false;
        initOnce(phone, ci);
    }

    private void initOnce(GsmCdmaPhone phone, CommandsInterface ci) {
        this.mPhone = phone;
        this.LOG_TAG += "[SUB" + this.mPhone.getPhoneId() + "]";
        initUiccController();
        registerForIccChanged();
        this.mNewCellLoc = new GsmCellLocation();
    }

    public void dispose() {
        unregisterForIccChanged();
        super.dispose();
    }

    public void handleMessage(Message msg) {
        if (this.mPhone.isPhoneTypeGsm()) {
            int i = msg.what;
            super.handleMessage(msg);
            return;
        }
        log("not gsm phone, not handle message: " + msg.what);
    }

    protected void log(String s) {
        Rlog.d(this.LOG_TAG, s);
    }

    protected void loge(String s) {
        Rlog.e(this.LOG_TAG, s);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("HwVSimSSTBridge extends:");
        super.dump(fd, pw, args);
        pw.println(" mPhone=" + this.mPhone);
        pw.println(" mCurSpn=" + this.mCurSpn);
        pw.println(" mCurDataSpn=" + this.mCurDataSpn);
        pw.println(" mCurShowSpn=" + this.mCurShowSpn);
        pw.println(" mCurPlmn=" + this.mCurPlmn);
        pw.println(" mCurShowPlmn=" + this.mCurShowPlmn);
        pw.println(" mNewCellLoc=" + this.mNewCellLoc);
    }

    public void updateSpnDisplay() {
        boolean showPlmn;
        String plmn;
        IccRecords iccRecords = this.mIccRecords;
        int rule = iccRecords != null ? iccRecords.getDisplayRule(this.mSS.getOperatorNumeric()) : 0;
        if (this.mSS.getVoiceRegState() == 1 || this.mSS.getVoiceRegState() == 2) {
            showPlmn = true;
            plmn = Resources.getSystem().getText(17040012).toString();
            log("updateSpnDisplay: radio is on but out of service, set plmn='" + plmn + "'");
        } else if (this.mSS.getVoiceRegState() == 0) {
            plmn = this.mSS.getOperatorAlphaLong();
            showPlmn = !TextUtils.isEmpty(plmn) ? (rule & 2) == 2 : false;
        } else {
            showPlmn = true;
            plmn = Resources.getSystem().getText(17040012).toString();
            log("updateSpnDisplay: radio is off w/ showPlmn=" + true + " plmn=" + plmn);
        }
        String serviceProviderName = iccRecords != null ? iccRecords.getServiceProviderName() : "";
        String dataSpn = serviceProviderName;
        boolean showSpn = !TextUtils.isEmpty(serviceProviderName) ? (rule & 1) == 1 : false;
        if (this.mSS.getVoiceRegState() == 3 || (showPlmn && TextUtils.equals(serviceProviderName, plmn))) {
            serviceProviderName = null;
            showSpn = false;
        }
        if (showPlmn != this.mCurShowPlmn || showSpn != this.mCurShowSpn || !TextUtils.equals(serviceProviderName, this.mCurSpn) || !TextUtils.equals(dataSpn, this.mCurDataSpn) || !TextUtils.equals(plmn, this.mCurPlmn)) {
            log(String.format("updateSpnDisplay: changed sending intent rule=" + rule + " showPlmn='%b' plmn='%s' showSpn='%b' spn='%s' dataSpn='%s'", new Object[]{Boolean.valueOf(showPlmn), plmn, Boolean.valueOf(showSpn), serviceProviderName, dataSpn}));
            updateVSimOperatorProp();
            Intent intent = createSpnIntent();
            intent.addFlags(536870912);
            intent.putExtra("showSpn", showSpn);
            intent.putExtra(VirtualNets.SPN, serviceProviderName);
            intent.putExtra("spnData", dataSpn);
            intent.putExtra("showPlmn", showPlmn);
            intent.putExtra("plmn", plmn);
            putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
        this.mCurShowSpn = showSpn;
        this.mCurShowPlmn = showPlmn;
        this.mCurSpn = serviceProviderName;
        this.mCurDataSpn = dataSpn;
        this.mCurPlmn = plmn;
    }

    protected void onUpdateIccAvailability() {
        if (isUiccControllerValid()) {
            UiccCardApplication newUiccApplication = getVSimUiccCardApplication();
            if (this.mUiccApplcation != newUiccApplication) {
                if (this.mUiccApplcation != null) {
                    log("Removing stale icc objects.");
                    this.mUiccApplcation.unregisterForReady(this);
                    this.mUiccApplcation.unregisterForGetAdDone(this);
                    if (this.mIccRecords != null) {
                        this.mIccRecords.unregisterForRecordsLoaded(this);
                    }
                    this.mIccRecords = null;
                    this.mUiccApplcation = null;
                }
                if (newUiccApplication != null) {
                    log("New card found");
                    this.mUiccApplcation = newUiccApplication;
                    this.mIccRecords = this.mUiccApplcation.getIccRecords();
                    if (this.mPhone.isPhoneTypeGsm()) {
                        this.mUiccApplcation.registerForReady(this, 17, null);
                        this.mUiccApplcation.registerForGetAdDone(this, 1002, null);
                        if (this.mIccRecords != null) {
                            this.mIccRecords.registerForRecordsLoaded(this, 16, null);
                        }
                    }
                }
            }
        }
    }

    protected void handlePollStateResult(int what, AsyncResult ar) {
        if (ar.userObj == this.mPollingContext) {
            if (ar.exception != null) {
                Error err = null;
                if (ar.exception instanceof CommandException) {
                    err = ((CommandException) ar.exception).getCommandError();
                }
                if (err == Error.RADIO_NOT_AVAILABLE) {
                    cancelPollState();
                    return;
                } else if (err != Error.OP_NOT_ALLOWED_BEFORE_REG_NW) {
                    loge("RIL implementation has returned an error where it must succeed" + ar.exception);
                }
            } else {
                try {
                    handlePollStateResultMessage(what, ar);
                } catch (RuntimeException ex) {
                    loge("Exception while polling service state. Probably malformed RIL response." + ex);
                }
            }
            int[] iArr = this.mPollingContext;
            iArr[0] = iArr[0] - 1;
            if (this.mPollingContext[0] == 0) {
                updateRoamingState();
                pollStateDone();
            }
        }
    }

    public void pollState(boolean modemTriggered) {
        this.mPollingContext = new int[1];
        this.mPollingContext[0] = 0;
        switch (-getcom-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues()[this.mCi.getRadioState().ordinal()]) {
            case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                this.mNewSS.setStateOff();
                this.mNewCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                if (!(modemTriggered || 18 == this.mSS.getRilDataRadioTechnology())) {
                    pollStateDone();
                    return;
                }
            case HwVSimUtilsInner.STATE_EB /*2*/:
                this.mNewSS.setStateOutOfService();
                this.mNewCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                pollStateDone();
                return;
        }
        int[] iArr = this.mPollingContext;
        iArr[0] = iArr[0] + 1;
        this.mCi.getOperator(obtainMessage(6, this.mPollingContext));
        iArr = this.mPollingContext;
        iArr[0] = iArr[0] + 1;
        this.mCi.getDataRegistrationState(obtainMessage(5, this.mPollingContext));
        iArr = this.mPollingContext;
        iArr[0] = iArr[0] + 1;
        this.mCi.getVoiceRegistrationState(obtainMessage(4, this.mPollingContext));
        if (this.mPhone.isPhoneTypeGsm()) {
            iArr = this.mPollingContext;
            iArr[0] = iArr[0] + 1;
            this.mCi.getNetworkSelectionMode(obtainMessage(14, this.mPollingContext));
        }
    }

    private void pollStateDone() {
        if (this.mPhone.isPhoneTypeGsm()) {
            pollStateDoneGsm();
        }
    }

    private void pollStateDoneGsm() {
        boolean hasGprsAttached;
        boolean hasGprsDetached;
        useDataRegStateForDataOnlyDevices();
        log("Poll ServiceState done:  oldSS=[" + this.mSS + "] newSS=[" + this.mNewSS + "]");
        if (this.mSS.getDataRegState() != 0) {
            hasGprsAttached = this.mNewSS.getDataRegState() == 0;
        } else {
            hasGprsAttached = false;
        }
        if (this.mSS.getDataRegState() == 0) {
            hasGprsDetached = this.mNewSS.getDataRegState() != 0;
        } else {
            hasGprsDetached = false;
        }
        boolean hasDataRegStateChanged = this.mSS.getDataRegState() != this.mNewSS.getDataRegState();
        boolean hasRilDataRadioTechnologyChanged = this.mSS.getRilDataRadioTechnology() != this.mNewSS.getRilDataRadioTechnology();
        boolean hasChanged = !this.mNewSS.equals(this.mSS);
        boolean hasLocationChanged = !this.mNewCellLoc.equals(this.mCellLoc);
        boolean needNotifyData = this.mSS.getCssIndicator() != this.mNewSS.getCssIndicator();
        boolean hasLacChanged = ((GsmCellLocation) this.mNewCellLoc).getLac() != ((GsmCellLocation) this.mCellLoc).getLac();
        TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        if (hasGprsAttached) {
            log("service state hasRegistered , poll signal strength at once");
            sendMessage(obtainMessage(10));
        }
        ServiceState tss = this.mSS;
        this.mSS = this.mNewSS;
        this.mNewSS = tss;
        this.mNewSS.setStateOutOfService();
        CellLocation tcl = (GsmCellLocation) this.mCellLoc;
        this.mCellLoc = this.mNewCellLoc;
        this.mNewCellLoc = tcl;
        if (hasRilDataRadioTechnologyChanged) {
            tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
        }
        if (hasChanged) {
            updateSpnDisplay();
            tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
            updateVSimOperatorProp();
            String prevOperatorNumeric = getNetworkOperatorForPhone(tm, this.mPhone.getPhoneId());
            String operatorNumeric = this.mSS.getOperatorNumeric();
            setNetworkOperatorNumericForPhone(tm, this.mPhone.getPhoneId(), operatorNumeric);
            updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
            if (TextUtils.isEmpty(operatorNumeric)) {
                log("operatorNumeric is null");
                setNetworkCountryIsoForPhone(tm, this.mPhone.getPhoneId(), "");
            } else {
                String iso = "";
                String mcc = "";
                try {
                    iso = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(0, 3)));
                } catch (NumberFormatException ex) {
                    loge("pollStateDone: countryCodeForMcc error" + ex);
                } catch (StringIndexOutOfBoundsException ex2) {
                    loge("pollStateDone: countryCodeForMcc error" + ex2);
                }
                setNetworkCountryIsoForPhone(tm, this.mPhone.getPhoneId(), iso);
            }
            tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming());
            setRoamingType(this.mSS);
            log("Broadcasting ServiceState : " + this.mSS);
            this.mPhone.notifyServiceStateChanged(this.mSS);
        }
        if (hasGprsAttached) {
            this.mAttachedRegistrants.notifyRegistrants();
        }
        if (hasGprsDetached) {
            this.mDetachedRegistrants.notifyRegistrants();
        }
        if (hasDataRegStateChanged || hasRilDataRadioTechnologyChanged) {
            notifyDataRegStateRilRadioTechnologyChanged();
            needNotifyData = true;
        }
        if (needNotifyData) {
            this.mPhone.notifyDataConnection(null);
        }
        if (hasLocationChanged) {
            this.mPhone.notifyLocationChanged();
        }
        if (hasLacChanged) {
            Rlog.i(this.LOG_TAG, "LAC changed, update operator name display");
            updateSpnDisplay();
        }
    }

    private void setSignalStrengthDefaultValues() {
        this.mSignalStrength = new SignalStrength(true);
    }
}
