package com.android.internal.telephony;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.CellLocation;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.vsim.HwVSimController;
import java.io.FileDescriptor;
import java.io.PrintWriter;

abstract class HwVSimSSTBridge extends ServiceStateTracker {
    private String LOG_TAG = "VSimSSTBridge";
    private String mCurDataSpn = null;
    private String mCurPlmn = null;
    private boolean mCurShowPlmn = false;
    private boolean mCurShowSpn = false;
    private String mCurSpn = null;
    private CellLocation mNewCellLoc;
    private GsmCdmaPhone mPhone;

    /* renamed from: com.android.internal.telephony.HwVSimSSTBridge$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$CommandsInterface$RadioState = new int[CommandsInterface.RadioState.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$CommandsInterface$RadioState[CommandsInterface.RadioState.RADIO_UNAVAILABLE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandsInterface$RadioState[CommandsInterface.RadioState.RADIO_OFF.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public abstract Intent createSpnIntent();

    /* access modifiers changed from: protected */
    public abstract String getNetworkOperatorForPhone(TelephonyManager telephonyManager, int i);

    /* access modifiers changed from: protected */
    public abstract UiccCard getUiccCard();

    /* access modifiers changed from: protected */
    public abstract UiccCardApplication getVSimUiccCardApplication();

    /* access modifiers changed from: protected */
    public abstract void initUiccController();

    /* access modifiers changed from: protected */
    public abstract boolean isUiccControllerValid();

    /* access modifiers changed from: protected */
    public abstract void putPhoneIdAndSubIdExtra(Intent intent, int i);

    /* access modifiers changed from: protected */
    public abstract void registerForIccChanged();

    /* access modifiers changed from: protected */
    public abstract void setNetworkCountryIsoForPhone(TelephonyManager telephonyManager, int i, String str);

    /* access modifiers changed from: protected */
    public abstract void setNetworkOperatorNumericForPhone(TelephonyManager telephonyManager, int i, String str);

    /* access modifiers changed from: protected */
    public abstract void unregisterForIccChanged();

    /* access modifiers changed from: protected */
    public abstract void updateVSimOperatorProp();

    public HwVSimSSTBridge(GsmCdmaPhone phone, CommandsInterface ci) {
        super(phone, ci);
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
        HwVSimSSTBridge.super.dispose();
    }

    public void handleMessage(Message msg) {
        if (!this.mPhone.isPhoneTypeGsm()) {
            log("not gsm phone, not handle message: " + msg.what);
            return;
        }
        int i = msg.what;
        HwVSimSSTBridge.super.handleMessage(msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("HwVSimSSTBridge extends:");
        HwVSimSSTBridge.super.dump(fd, pw, args);
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
        int rule = iccRecords != null ? iccRecords.getDisplayRule(this.mSS) : 0;
        int ruleFromApk = HwVSimController.getInstance().getRule();
        if (ruleFromApk != -1) {
            rule = ruleFromApk;
        }
        int combinedRegState = HwTelephonyFactory.getHwNetworkManager().getGsmCombinedRegState(this, this.mPhone, this.mSS);
        if (combinedRegState == 1 || combinedRegState == 2) {
            showPlmn = true;
            plmn = Resources.getSystem().getText(17040350).toString();
            log("updateSpnDisplay: radio is on but out of service, set plmn='" + plmn + "'");
        } else if (combinedRegState == 0) {
            plmn = this.mSS.getOperatorAlphaLong();
            showPlmn = !TextUtils.isEmpty(plmn) && (rule & 2) == 2;
        } else {
            showPlmn = true;
            plmn = Resources.getSystem().getText(17040350).toString();
            log("updateSpnDisplay: radio is off w/ showPlmn=" + true + " plmn=" + plmn);
        }
        String spn = iccRecords != null ? iccRecords.getServiceProviderName() : "";
        String spnFromApk = HwVSimController.getInstance().getSpn();
        if (!TextUtils.isEmpty(spnFromApk)) {
            spn = spnFromApk;
        }
        String dataSpn = spn;
        boolean showSpn = !TextUtils.isEmpty(spn) && (rule & 1) == 1;
        if (this.mSS.getVoiceRegState() == 3 || (showPlmn && TextUtils.equals(spn, plmn))) {
            spn = null;
            showSpn = false;
        }
        if (showPlmn != this.mCurShowPlmn || showSpn != this.mCurShowSpn || !TextUtils.equals(spn, this.mCurSpn) || !TextUtils.equals(dataSpn, this.mCurDataSpn) || !TextUtils.equals(plmn, this.mCurPlmn)) {
            log(String.format("updateSpnDisplay: changed sending intent rule=" + rule + " showPlmn='%b' plmn='%s' showSpn='%b' spn='%s' dataSpn='%s'", new Object[]{Boolean.valueOf(showPlmn), plmn, Boolean.valueOf(showSpn), spn, dataSpn}));
            updateVSimOperatorProp();
            Intent intent = createSpnIntent();
            intent.addFlags(536870912);
            intent.putExtra("showSpn", showSpn);
            intent.putExtra("spn", spn);
            intent.putExtra("spnData", dataSpn);
            intent.putExtra("showPlmn", showPlmn);
            intent.putExtra("plmn", plmn);
            putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
        this.mCurShowSpn = showSpn;
        this.mCurShowPlmn = showPlmn;
        this.mCurSpn = spn;
        this.mCurDataSpn = dataSpn;
        this.mCurPlmn = plmn;
    }

    /* JADX WARNING: type inference failed for: r4v0, types: [android.os.Handler, com.android.internal.telephony.HwVSimSSTBridge] */
    /* access modifiers changed from: protected */
    public void onUpdateIccAvailability() {
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
                        this.mUiccApplcation.registerForGetAdDone(this, HwFullNetworkConstants.EVENT_RADIO_UNAVAILABLE, null);
                        if (this.mIccRecords != null) {
                            this.mIccRecords.registerForRecordsLoaded(this, 16, null);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handlePollStateResult(int what, AsyncResult ar) {
        if (ar.userObj == this.mPollingContext) {
            if (ar.exception != null) {
                CommandException.Error err = null;
                if (ar.exception instanceof CommandException) {
                    err = ar.exception.getCommandError();
                }
                if (err == CommandException.Error.RADIO_NOT_AVAILABLE) {
                    cancelPollState();
                    return;
                } else if (err != CommandException.Error.OP_NOT_ALLOWED_BEFORE_REG_NW) {
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
        log("pollState: modemTriggered=" + modemTriggered);
        switch (AnonymousClass1.$SwitchMap$com$android$internal$telephony$CommandsInterface$RadioState[this.mCi.getRadioState().ordinal()]) {
            case 1:
                this.mNewSS.setStateOutOfService();
                this.mNewCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                pollStateDone();
                return;
            case 2:
                this.mNewSS.setStateOff();
                this.mNewCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                if (!modemTriggered && 18 != this.mSS.getRilDataRadioTechnology()) {
                    pollStateDone();
                    return;
                }
        }
        int[] iArr = this.mPollingContext;
        iArr[0] = iArr[0] + 1;
        this.mCi.getOperator(obtainMessage(6, this.mPollingContext));
        int[] iArr2 = this.mPollingContext;
        iArr2[0] = iArr2[0] + 1;
        ((NetworkRegistrationManager) getRegStateManagers().get(1)).getNetworkRegistrationState(2, obtainMessage(5, this.mPollingContext));
        int[] iArr3 = this.mPollingContext;
        iArr3[0] = iArr3[0] + 1;
        ((NetworkRegistrationManager) getRegStateManagers().get(1)).getNetworkRegistrationState(1, obtainMessage(4, this.mPollingContext));
        if (this.mPhone.isPhoneTypeGsm()) {
            int[] iArr4 = this.mPollingContext;
            iArr4[0] = iArr4[0] + 1;
            this.mCi.getNetworkSelectionMode(obtainMessage(14, this.mPollingContext));
        }
    }

    private void pollStateDone() {
        if (this.mPhone.isPhoneTypeGsm()) {
            pollStateDoneGsm();
        }
    }

    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
        */
    private void pollStateDoneGsm() {
        /*
            r31 = this;
            r1 = r31
            r31.useDataRegStateForDataOnlyDevices()
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "Poll ServiceState done:  oldSS=["
            r0.append(r2)
            android.telephony.ServiceState r2 = r1.mSS
            r0.append(r2)
            java.lang.String r2 = "] newSS=["
            r0.append(r2)
            android.telephony.ServiceState r2 = r1.mNewSS
            r0.append(r2)
            java.lang.String r2 = "]"
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            r1.log(r0)
            android.telephony.ServiceState r0 = r1.mSS
            int r0 = r0.getVoiceRegState()
            r3 = 1
            if (r0 == 0) goto L_0x003d
            android.telephony.ServiceState r0 = r1.mNewSS
            int r0 = r0.getVoiceRegState()
            if (r0 != 0) goto L_0x003d
            r0 = r3
            goto L_0x003e
        L_0x003d:
            r0 = 0
        L_0x003e:
            r4 = r0
            android.telephony.ServiceState r0 = r1.mSS
            int r0 = r0.getVoiceRegState()
            if (r0 != 0) goto L_0x0051
            android.telephony.ServiceState r0 = r1.mNewSS
            int r0 = r0.getVoiceRegState()
            if (r0 == 0) goto L_0x0051
            r0 = r3
            goto L_0x0052
        L_0x0051:
            r0 = 0
        L_0x0052:
            r5 = r0
            android.telephony.ServiceState r0 = r1.mSS
            int r0 = r0.getDataRegState()
            if (r0 == 0) goto L_0x0065
            android.telephony.ServiceState r0 = r1.mNewSS
            int r0 = r0.getDataRegState()
            if (r0 != 0) goto L_0x0065
            r0 = r3
            goto L_0x0066
        L_0x0065:
            r0 = 0
        L_0x0066:
            r6 = r0
            android.telephony.ServiceState r0 = r1.mSS
            int r0 = r0.getDataRegState()
            if (r0 != 0) goto L_0x0079
            android.telephony.ServiceState r0 = r1.mNewSS
            int r0 = r0.getDataRegState()
            if (r0 == 0) goto L_0x0079
            r0 = r3
            goto L_0x007a
        L_0x0079:
            r0 = 0
        L_0x007a:
            r7 = r0
            android.telephony.ServiceState r0 = r1.mSS
            int r0 = r0.getDataRegState()
            android.telephony.ServiceState r8 = r1.mNewSS
            int r8 = r8.getDataRegState()
            if (r0 == r8) goto L_0x008b
            r0 = r3
            goto L_0x008c
        L_0x008b:
            r0 = 0
        L_0x008c:
            r8 = r0
            r0 = 0
            android.telephony.ServiceState r9 = r1.mNewSS
            java.lang.String r9 = r9.getOperatorNumeric()
            if (r9 == 0) goto L_0x00a9
            android.telephony.ServiceState r9 = r1.mNewSS
            java.lang.String r9 = r9.getOperatorNumeric()
            android.telephony.ServiceState r10 = r1.mSS
            java.lang.String r10 = r10.getOperatorNumeric()
            boolean r9 = r9.equals(r10)
            r9 = r9 ^ r3
            r0 = r9
            goto L_0x00aa
        L_0x00a9:
            r9 = r0
        L_0x00aa:
            android.telephony.ServiceState r0 = r1.mSS
            int r0 = r0.getRilDataRadioTechnology()
            android.telephony.ServiceState r10 = r1.mNewSS
            int r10 = r10.getRilDataRadioTechnology()
            if (r0 == r10) goto L_0x00ba
            r0 = r3
            goto L_0x00bb
        L_0x00ba:
            r0 = 0
        L_0x00bb:
            r10 = r0
            android.telephony.ServiceState r0 = r1.mNewSS
            android.telephony.ServiceState r11 = r1.mSS
            boolean r0 = r0.equals(r11)
            r0 = r0 ^ r3
            r11 = r0
            android.telephony.CellLocation r0 = r1.mNewCellLoc
            android.telephony.CellLocation r12 = r1.mCellLoc
            boolean r0 = r0.equals(r12)
            r0 = r0 ^ r3
            r12 = r0
            android.telephony.ServiceState r0 = r1.mSS
            int r0 = r0.getCssIndicator()
            android.telephony.ServiceState r13 = r1.mNewSS
            int r13 = r13.getCssIndicator()
            if (r0 == r13) goto L_0x00e0
            r0 = r3
            goto L_0x00e1
        L_0x00e0:
            r0 = 0
        L_0x00e1:
            r13 = r0
            android.telephony.CellLocation r0 = r1.mNewCellLoc
            android.telephony.gsm.GsmCellLocation r0 = (android.telephony.gsm.GsmCellLocation) r0
            android.telephony.CellLocation r14 = r1.mCellLoc
            android.telephony.gsm.GsmCellLocation r14 = (android.telephony.gsm.GsmCellLocation) r14
            boolean r14 = r0.isNotLacEquals(r14)
            com.android.internal.telephony.GsmCdmaPhone r0 = r1.mPhone
            android.content.Context r0 = r0.getContext()
            java.lang.String r15 = "phone"
            java.lang.Object r0 = r0.getSystemService(r15)
            r15 = r0
            android.telephony.TelephonyManager r15 = (android.telephony.TelephonyManager) r15
            if (r6 == 0) goto L_0x010d
            java.lang.String r0 = "service state hasRegistered , poll signal strength at once"
            r1.log(r0)
            r0 = 10
            android.os.Message r0 = r1.obtainMessage(r0)
            r1.sendMessage(r0)
        L_0x010d:
            android.telephony.ServiceState r3 = r1.mSS
            android.telephony.ServiceState r0 = r1.mNewSS
            r1.mSS = r0
            r1.mNewSS = r3
            android.telephony.ServiceState r0 = r1.mNewSS
            r0.setStateOutOfService()
            android.telephony.CellLocation r0 = r1.mCellLoc
            r2 = r0
            android.telephony.gsm.GsmCellLocation r2 = (android.telephony.gsm.GsmCellLocation) r2
            android.telephony.CellLocation r0 = r1.mNewCellLoc
            r1.mCellLoc = r0
            r1.mNewCellLoc = r2
            if (r10 == 0) goto L_0x0139
            com.android.internal.telephony.GsmCdmaPhone r0 = r1.mPhone
            int r0 = r0.getPhoneId()
            r17 = r2
            android.telephony.ServiceState r2 = r1.mSS
            int r2 = r2.getRilDataRadioTechnology()
            r15.setDataNetworkTypeForPhone(r0, r2)
            goto L_0x013b
        L_0x0139:
            r17 = r2
        L_0x013b:
            if (r4 != 0) goto L_0x0143
            if (r9 == 0) goto L_0x0140
            goto L_0x0143
        L_0x0140:
            r20 = r3
            goto L_0x015a
        L_0x0143:
            long r18 = android.os.SystemClock.elapsedRealtime()
            r20 = r3
            long r2 = r1.mLastReceivedNITZReferenceTime
            long r18 = r18 - r2
            r2 = 5000(0x1388, double:2.4703E-320)
            int r0 = (r18 > r2 ? 1 : (r18 == r2 ? 0 : -1))
            if (r0 <= 0) goto L_0x015a
            com.android.internal.telephony.NitzStateMachine r0 = r31.getNitzState()
            r0.handleNetworkAvailable()
        L_0x015a:
            if (r5 == 0) goto L_0x0163
            com.android.internal.telephony.NitzStateMachine r0 = r31.getNitzState()
            r0.handleNetworkUnavailable()
        L_0x0163:
            if (r11 == 0) goto L_0x02ce
            r31.updateSpnDisplay()
            com.android.internal.telephony.GsmCdmaPhone r0 = r1.mPhone
            int r0 = r0.getPhoneId()
            android.telephony.ServiceState r2 = r1.mSS
            java.lang.String r2 = r2.getOperatorAlphaLong()
            r15.setNetworkOperatorNameForPhone(r0, r2)
            r31.updateVSimOperatorProp()
            com.android.internal.telephony.GsmCdmaPhone r0 = r1.mPhone
            int r0 = r0.getPhoneId()
            java.lang.String r2 = r1.getNetworkOperatorForPhone(r15, r0)
            java.lang.String r3 = r31.getNetworkCountryIsoForPhone()
            android.telephony.ServiceState r0 = r1.mSS
            r21 = r4
            java.lang.String r4 = r0.getOperatorNumeric()
            com.android.internal.telephony.GsmCdmaPhone r0 = r1.mPhone
            int r0 = r0.getPhoneId()
            r1.setNetworkOperatorNumericForPhone(r15, r0, r4)
            com.android.internal.telephony.GsmCdmaPhone r0 = r1.mPhone
            android.content.Context r0 = r0.getContext()
            r1.updateCarrierMccMncConfiguration(r4, r2, r0)
            boolean r0 = android.text.TextUtils.isEmpty(r4)
            if (r0 == 0) goto L_0x01cf
            java.lang.String r0 = "operatorNumeric is null"
            r1.log(r0)
            com.android.internal.telephony.GsmCdmaPhone r0 = r1.mPhone
            int r0 = r0.getPhoneId()
            r22 = r5
            java.lang.String r5 = ""
            r1.setNetworkCountryIsoForPhone(r15, r0, r5)
            com.android.internal.telephony.NitzStateMachine r0 = r31.getNitzState()
            r0.handleNetworkUnavailable()
            r24 = r9
            r30 = r10
            r28 = r11
            r29 = r12
            r26 = r13
            r27 = r14
            goto L_0x029c
        L_0x01cf:
            r22 = r5
            android.telephony.ServiceState r0 = r1.mSS
            int r0 = r0.getRilDataRadioTechnology()
            r5 = 18
            if (r0 == r5) goto L_0x0290
            java.lang.String r0 = ""
            r5 = r0
            r0 = 3
            r23 = r5
            r5 = 0
            java.lang.String r0 = r4.substring(r5, r0)     // Catch:{ NumberFormatException | StringIndexOutOfBoundsException -> 0x01f2 }
            int r5 = java.lang.Integer.parseInt(r0)     // Catch:{ NumberFormatException | StringIndexOutOfBoundsException -> 0x01f2 }
            java.lang.String r5 = com.android.internal.telephony.MccTable.countryCodeForMcc(r5)     // Catch:{ NumberFormatException | StringIndexOutOfBoundsException -> 0x01f2 }
            r24 = r9
            goto L_0x020b
        L_0x01f2:
            r0 = move-exception
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            r24 = r9
            java.lang.String r9 = "pollStateDone: countryCodeForMcc error: "
            r5.append(r9)
            r5.append(r0)
            java.lang.String r5 = r5.toString()
            r1.loge(r5)
            r5 = r23
        L_0x020b:
            com.android.internal.telephony.GsmCdmaPhone r0 = r1.mPhone
            int r0 = r0.getPhoneId()
            r1.setNetworkCountryIsoForPhone(r15, r0, r5)
            boolean r0 = r31.iccCardExists()
            boolean r9 = r1.networkCountryIsoChanged(r5, r3)
            if (r0 == 0) goto L_0x0224
            if (r9 == 0) goto L_0x0224
            r16 = 1
            goto L_0x0226
        L_0x0224:
            r16 = 0
        L_0x0226:
            r25 = r16
            r26 = r13
            r27 = r14
            long r13 = java.lang.System.currentTimeMillis()
            r28 = r11
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            r29 = r12
            java.lang.String r12 = "Before handleNetworkCountryCodeKnown: countryChanged="
            r11.append(r12)
            r12 = r25
            r11.append(r12)
            r30 = r10
            java.lang.String r10 = " iccCardExist="
            r11.append(r10)
            r11.append(r0)
            java.lang.String r10 = " countryIsoChanged="
            r11.append(r10)
            r11.append(r9)
            java.lang.String r10 = " operatorNumeric="
            r11.append(r10)
            r11.append(r4)
            java.lang.String r10 = " prevOperatorNumeric="
            r11.append(r10)
            r11.append(r2)
            java.lang.String r10 = " countryIsoCode="
            r11.append(r10)
            r11.append(r5)
            java.lang.String r10 = " prevCountryIsoCode="
            r11.append(r10)
            r11.append(r3)
            java.lang.String r10 = " ltod="
            r11.append(r10)
            java.lang.String r10 = android.util.TimeUtils.logTimeOfDay(r13)
            r11.append(r10)
            java.lang.String r10 = r11.toString()
            r1.log(r10)
            com.android.internal.telephony.NitzStateMachine r10 = r31.getNitzState()
            r10.handleNetworkCountryCodeSet(r12)
            goto L_0x029c
        L_0x0290:
            r24 = r9
            r30 = r10
            r28 = r11
            r29 = r12
            r26 = r13
            r27 = r14
        L_0x029c:
            com.android.internal.telephony.GsmCdmaPhone r0 = r1.mPhone
            int r0 = r0.getPhoneId()
            android.telephony.ServiceState r5 = r1.mSS
            boolean r5 = r5.getVoiceRoaming()
            r15.setNetworkRoamingForPhone(r0, r5)
            android.telephony.ServiceState r0 = r1.mSS
            r1.setRoamingType(r0)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r5 = "Broadcasting ServiceState : "
            r0.append(r5)
            android.telephony.ServiceState r5 = r1.mSS
            r0.append(r5)
            java.lang.String r0 = r0.toString()
            r1.log(r0)
            com.android.internal.telephony.GsmCdmaPhone r0 = r1.mPhone
            android.telephony.ServiceState r5 = r1.mSS
            r0.notifyServiceStateChanged(r5)
            goto L_0x02de
        L_0x02ce:
            r21 = r4
            r22 = r5
            r24 = r9
            r30 = r10
            r28 = r11
            r29 = r12
            r26 = r13
            r27 = r14
        L_0x02de:
            if (r6 == 0) goto L_0x02e5
            android.os.RegistrantList r0 = r1.mAttachedRegistrants
            r0.notifyRegistrants()
        L_0x02e5:
            if (r7 == 0) goto L_0x02ec
            android.os.RegistrantList r0 = r1.mDetachedRegistrants
            r0.notifyRegistrants()
        L_0x02ec:
            if (r8 != 0) goto L_0x02f4
            if (r30 == 0) goto L_0x02f1
            goto L_0x02f4
        L_0x02f1:
            r13 = r26
            goto L_0x02f8
        L_0x02f4:
            r31.notifyDataRegStateRilRadioTechnologyChanged()
            r13 = 1
        L_0x02f8:
            if (r13 == 0) goto L_0x0300
            com.android.internal.telephony.GsmCdmaPhone r0 = r1.mPhone
            r2 = 0
            r0.notifyDataConnection(r2)
        L_0x0300:
            if (r29 == 0) goto L_0x0307
            com.android.internal.telephony.GsmCdmaPhone r0 = r1.mPhone
            r0.notifyLocationChanged()
        L_0x0307:
            if (r27 == 0) goto L_0x0313
            java.lang.String r0 = r1.LOG_TAG
            java.lang.String r2 = "LAC changed, update operator name display"
            android.telephony.Rlog.i(r0, r2)
            r31.updateSpnDisplay()
        L_0x0313:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwVSimSSTBridge.pollStateDoneGsm():void");
    }

    private void setSignalStrengthDefaultValues() {
        this.mSignalStrength = new SignalStrength(true);
    }

    /* access modifiers changed from: protected */
    public String getNetworkCountryIsoForPhone() {
        return SystemProperties.get("gsm.operator.iso-country.vsim", "");
    }

    private boolean networkCountryIsoChanged(String newCountryIsoCode, String prevCountryIsoCode) {
        if (TextUtils.isEmpty(newCountryIsoCode)) {
            log("countryIsoChanged: no new country ISO code");
            return false;
        } else if (!TextUtils.isEmpty(prevCountryIsoCode)) {
            return !newCountryIsoCode.equals(prevCountryIsoCode);
        } else {
            log("countryIsoChanged: no previous country ISO code");
            return true;
        }
    }

    private boolean iccCardExists() {
        if (this.mUiccApplcation == null) {
            return false;
        }
        return this.mUiccApplcation.getState() != IccCardApplicationStatus.AppState.APPSTATE_UNKNOWN;
    }
}
