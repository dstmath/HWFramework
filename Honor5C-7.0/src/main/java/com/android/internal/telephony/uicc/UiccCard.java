package com.android.internal.telephony.uicc;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.LocalLog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.TelephonyEventLog;
import com.android.internal.telephony.cat.CatService;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.IccCardStatus.PinState;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.google.android.mms.pdu.PduPersister;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class UiccCard extends AbstractUiccCard {
    protected static final boolean DBG = true;
    private static final int EVENT_CARD_ADDED = 14;
    private static final int EVENT_CARD_REMOVED = 13;
    private static final int EVENT_CARRIER_PRIVILIGES_LOADED = 20;
    private static final int EVENT_CLOSE_LOGICAL_CHANNEL_DONE = 16;
    private static final int EVENT_OPEN_LOGICAL_CHANNEL_DONE = 15;
    private static final int EVENT_SIM_IO_DONE = 19;
    private static final int EVENT_TRANSMIT_APDU_BASIC_CHANNEL_DONE = 18;
    private static final int EVENT_TRANSMIT_APDU_LOGICAL_CHANNEL_DONE = 17;
    public static final String EXTRA_ICC_CARD_ADDED = "com.android.internal.telephony.uicc.ICC_CARD_ADDED";
    protected static final String LOG_TAG = "UiccCard";
    private static final String OPERATOR_BRAND_OVERRIDE_PREFIX = "operator_branding_";
    private static final LocalLog mLocalLog = null;
    private RegistrantList mAbsentRegistrants;
    private CardState mCardState;
    private RegistrantList mCarrierPrivilegeRegistrants;
    private UiccCarrierPrivilegeRules mCarrierPrivilegeRules;
    private CatService mCatService;
    private int mCdmaSubscriptionAppIndex;
    private CommandsInterface mCi;
    private Context mContext;
    private int mGsmUmtsSubscriptionAppIndex;
    protected Handler mHandler;
    private int mImsSubscriptionAppIndex;
    private RadioState mLastRadioState;
    private final Object mLock;
    private int mPhoneId;
    private UiccCardApplication[] mUiccApplications;
    private PinState mUniversalPinState;

    private class ClickListener implements OnClickListener {
        String pkgName;

        public ClickListener(String pkgName) {
            this.pkgName = pkgName;
        }

        public void onClick(DialogInterface dialog, int which) {
            synchronized (UiccCard.this.mLock) {
                if (which == -1) {
                    Intent market = new Intent("android.intent.action.VIEW");
                    market.setData(Uri.parse("market://details?id=" + this.pkgName));
                    market.addFlags(268435456);
                    UiccCard.this.mContext.startActivity(market);
                } else if (which == -2) {
                    UiccCard.this.log("Not now clicked for carrier app dialog.");
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.uicc.UiccCard.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.uicc.UiccCard.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.UiccCard.<clinit>():void");
    }

    public UiccCard(Context c, CommandsInterface ci, IccCardStatus ics) {
        this.mLock = new Object();
        this.mUiccApplications = new UiccCardApplication[8];
        this.mLastRadioState = RadioState.RADIO_UNAVAILABLE;
        this.mAbsentRegistrants = new RegistrantList();
        this.mCarrierPrivilegeRegistrants = new RegistrantList();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UiccCard.EVENT_CARD_REMOVED /*13*/:
                        UiccCard.this.onIccSwap(false);
                    case UiccCard.EVENT_CARD_ADDED /*14*/:
                        UiccCard.this.onIccSwap(UiccCard.DBG);
                    case UiccCard.EVENT_OPEN_LOGICAL_CHANNEL_DONE /*15*/:
                    case UiccCard.EVENT_CLOSE_LOGICAL_CHANNEL_DONE /*16*/:
                    case UiccCard.EVENT_TRANSMIT_APDU_LOGICAL_CHANNEL_DONE /*17*/:
                    case UiccCard.EVENT_TRANSMIT_APDU_BASIC_CHANNEL_DONE /*18*/:
                    case UiccCard.EVENT_SIM_IO_DONE /*19*/:
                        AsyncResult ar = msg.obj;
                        if (ar.exception != null) {
                            UiccCard.this.loglocal("Exception: " + ar.exception);
                            UiccCard.this.log("Error in SIM access with exception" + ar.exception);
                        }
                        AsyncResult.forMessage((Message) ar.userObj, ar.result, ar.exception);
                        ((Message) ar.userObj).sendToTarget();
                    case UiccCard.EVENT_CARRIER_PRIVILIGES_LOADED /*20*/:
                        UiccCard.this.onCarrierPriviligesLoadedMessage();
                    case CallFailCause.STATUS_ENQUIRY /*30*/:
                        UiccCard.this.log("EVENT_CARD_UIM_LOCK");
                        UiccCard.this.bCardUimLocked = UiccCard.DBG;
                        UiccCard.this.displayUimTipDialog(UiccCard.this.mContext, 33685788);
                    default:
                        super.handleMessage(msg);
                }
            }
        };
        log("Creating");
        this.mCardState = ics.mCardState;
        update(c, ci, ics);
    }

    public UiccCard(Context c, CommandsInterface ci, IccCardStatus ics, int phoneId) {
        this.mLock = new Object();
        this.mUiccApplications = new UiccCardApplication[8];
        this.mLastRadioState = RadioState.RADIO_UNAVAILABLE;
        this.mAbsentRegistrants = new RegistrantList();
        this.mCarrierPrivilegeRegistrants = new RegistrantList();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UiccCard.EVENT_CARD_REMOVED /*13*/:
                        UiccCard.this.onIccSwap(false);
                    case UiccCard.EVENT_CARD_ADDED /*14*/:
                        UiccCard.this.onIccSwap(UiccCard.DBG);
                    case UiccCard.EVENT_OPEN_LOGICAL_CHANNEL_DONE /*15*/:
                    case UiccCard.EVENT_CLOSE_LOGICAL_CHANNEL_DONE /*16*/:
                    case UiccCard.EVENT_TRANSMIT_APDU_LOGICAL_CHANNEL_DONE /*17*/:
                    case UiccCard.EVENT_TRANSMIT_APDU_BASIC_CHANNEL_DONE /*18*/:
                    case UiccCard.EVENT_SIM_IO_DONE /*19*/:
                        AsyncResult ar = msg.obj;
                        if (ar.exception != null) {
                            UiccCard.this.loglocal("Exception: " + ar.exception);
                            UiccCard.this.log("Error in SIM access with exception" + ar.exception);
                        }
                        AsyncResult.forMessage((Message) ar.userObj, ar.result, ar.exception);
                        ((Message) ar.userObj).sendToTarget();
                    case UiccCard.EVENT_CARRIER_PRIVILIGES_LOADED /*20*/:
                        UiccCard.this.onCarrierPriviligesLoadedMessage();
                    case CallFailCause.STATUS_ENQUIRY /*30*/:
                        UiccCard.this.log("EVENT_CARD_UIM_LOCK");
                        UiccCard.this.bCardUimLocked = UiccCard.DBG;
                        UiccCard.this.displayUimTipDialog(UiccCard.this.mContext, 33685788);
                    default:
                        super.handleMessage(msg);
                }
            }
        };
        this.mCardState = ics.mCardState;
        this.mPhoneId = phoneId;
        update(c, ci, ics);
        if (this.mPhoneId == 0) {
            ci.registerForUimLockcard(this.mHandler, 30, Integer.valueOf(0));
            ci.getIccCardStatus(null);
        }
    }

    protected UiccCard() {
        this.mLock = new Object();
        this.mUiccApplications = new UiccCardApplication[8];
        this.mLastRadioState = RadioState.RADIO_UNAVAILABLE;
        this.mAbsentRegistrants = new RegistrantList();
        this.mCarrierPrivilegeRegistrants = new RegistrantList();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UiccCard.EVENT_CARD_REMOVED /*13*/:
                        UiccCard.this.onIccSwap(false);
                    case UiccCard.EVENT_CARD_ADDED /*14*/:
                        UiccCard.this.onIccSwap(UiccCard.DBG);
                    case UiccCard.EVENT_OPEN_LOGICAL_CHANNEL_DONE /*15*/:
                    case UiccCard.EVENT_CLOSE_LOGICAL_CHANNEL_DONE /*16*/:
                    case UiccCard.EVENT_TRANSMIT_APDU_LOGICAL_CHANNEL_DONE /*17*/:
                    case UiccCard.EVENT_TRANSMIT_APDU_BASIC_CHANNEL_DONE /*18*/:
                    case UiccCard.EVENT_SIM_IO_DONE /*19*/:
                        AsyncResult ar = msg.obj;
                        if (ar.exception != null) {
                            UiccCard.this.loglocal("Exception: " + ar.exception);
                            UiccCard.this.log("Error in SIM access with exception" + ar.exception);
                        }
                        AsyncResult.forMessage((Message) ar.userObj, ar.result, ar.exception);
                        ((Message) ar.userObj).sendToTarget();
                    case UiccCard.EVENT_CARRIER_PRIVILIGES_LOADED /*20*/:
                        UiccCard.this.onCarrierPriviligesLoadedMessage();
                    case CallFailCause.STATUS_ENQUIRY /*30*/:
                        UiccCard.this.log("EVENT_CARD_UIM_LOCK");
                        UiccCard.this.bCardUimLocked = UiccCard.DBG;
                        UiccCard.this.displayUimTipDialog(UiccCard.this.mContext, 33685788);
                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }

    public void dispose() {
        synchronized (this.mLock) {
            log("Disposing card");
            if (this.mCatService != null) {
                this.mCatService.dispose();
            }
            for (UiccCardApplication app : this.mUiccApplications) {
                if (app != null) {
                    app.dispose();
                }
            }
            this.mCatService = null;
            this.mUiccApplications = null;
            this.mCarrierPrivilegeRules = null;
            if (this.mPhoneId == 0) {
                this.mCi.unregisterForUimLockcard(this.mHandler);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void update(Context c, CommandsInterface ci, IccCardStatus ics) {
        synchronized (this.mLock) {
            CardState oldState = this.mCardState;
            this.mCardState = ics.mCardState;
            this.mUniversalPinState = ics.mUniversalPinState;
            this.mGsmUmtsSubscriptionAppIndex = ics.mGsmUmtsSubscriptionAppIndex;
            this.mCdmaSubscriptionAppIndex = ics.mCdmaSubscriptionAppIndex;
            this.mImsSubscriptionAppIndex = ics.mImsSubscriptionAppIndex;
            this.mContext = c;
            this.mCi = ci;
            log(ics.mApplications.length + " applications");
            for (int i = 0; i < this.mUiccApplications.length; i++) {
                if (this.mUiccApplications[i] == null) {
                    if (i < ics.mApplications.length) {
                        this.mUiccApplications[i] = new UiccCardApplication(this, ics.mApplications[i], this.mContext, this.mCi);
                    }
                } else if (i >= ics.mApplications.length) {
                    this.mUiccApplications[i].dispose();
                    this.mUiccApplications[i] = null;
                } else {
                    this.mUiccApplications[i].update(ics.mApplications[i], this.mContext, this.mCi);
                }
            }
            createAndUpdateCatService();
            log("Before privilege rules: " + this.mCarrierPrivilegeRules + " : " + this.mCardState);
            if (this.mCarrierPrivilegeRules == null && this.mCardState == CardState.CARDSTATE_PRESENT) {
                this.mCarrierPrivilegeRules = new UiccCarrierPrivilegeRules(this, this.mHandler.obtainMessage(EVENT_CARRIER_PRIVILIGES_LOADED));
            } else if (!(this.mCarrierPrivilegeRules == null || this.mCardState == CardState.CARDSTATE_PRESENT)) {
                this.mCarrierPrivilegeRules = null;
            }
            sanitizeApplicationIndexes();
            RadioState radioState = HwTelephonyFactory.getHwUiccManager().powerUpRadioIfhasCard(this.mContext, this.mPhoneId, this.mCi.getRadioState(), this.mLastRadioState, this.mCardState);
            log("update: radioState=" + radioState + " mLastRadioState=" + this.mLastRadioState);
            if (radioState == RadioState.RADIO_ON && this.mLastRadioState == RadioState.RADIO_ON) {
                if (oldState != CardState.CARDSTATE_ABSENT && this.mCardState == CardState.CARDSTATE_ABSENT) {
                    log("update: notify card removed");
                    this.mAbsentRegistrants.notifyRegistrants();
                    if (!this.bCardUimLocked) {
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_CARD_REMOVED, null));
                    }
                } else if (oldState == CardState.CARDSTATE_ABSENT && this.mCardState != CardState.CARDSTATE_ABSENT) {
                    log("update: notify card added");
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_CARD_ADDED, null));
                }
            }
            this.mLastRadioState = radioState;
        }
    }

    protected void createAndUpdateCatService() {
        if (VSimUtilsInner.isVSimSub(this.mPhoneId)) {
            log("createAndUpdateCatService, nothing for vsim sub " + this.mPhoneId);
            return;
        }
        if (this.mUiccApplications.length <= 0 || this.mUiccApplications[0] == null) {
            if (this.mCatService != null) {
                this.mCatService.dispose();
            }
            this.mCatService = null;
        } else if (this.mCatService == null) {
            this.mCatService = CatService.getInstance(this.mCi, this.mContext, this, this.mPhoneId);
        } else {
            this.mCatService.update(this.mCi, this.mContext, this);
        }
    }

    public CatService getCatService() {
        return this.mCatService;
    }

    protected void finalize() {
        log("UiccCard finalized");
    }

    private void sanitizeApplicationIndexes() {
        this.mGsmUmtsSubscriptionAppIndex = checkIndex(this.mGsmUmtsSubscriptionAppIndex, AppType.APPTYPE_SIM, AppType.APPTYPE_USIM);
        this.mCdmaSubscriptionAppIndex = checkIndex(this.mCdmaSubscriptionAppIndex, AppType.APPTYPE_RUIM, AppType.APPTYPE_CSIM);
        this.mImsSubscriptionAppIndex = checkIndex(this.mImsSubscriptionAppIndex, AppType.APPTYPE_ISIM, null);
    }

    private int checkIndex(int index, AppType expectedAppType, AppType altExpectedAppType) {
        if (this.mUiccApplications == null || index >= this.mUiccApplications.length) {
            loge("App index " + index + " is invalid since there are no applications");
            return -1;
        } else if (index < 0) {
            return -1;
        } else {
            if (this.mUiccApplications[index].getType() == expectedAppType || this.mUiccApplications[index].getType() == altExpectedAppType) {
                return index;
            }
            loge("App index " + index + " is invalid since it's not " + expectedAppType + " and not " + altExpectedAppType);
            return -1;
        }
    }

    public void registerForAbsent(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mAbsentRegistrants.add(r);
            if (this.mCardState == CardState.CARDSTATE_ABSENT) {
                r.notifyRegistrant();
            }
        }
    }

    public void unregisterForAbsent(Handler h) {
        synchronized (this.mLock) {
            this.mAbsentRegistrants.remove(h);
        }
    }

    public void registerForCarrierPrivilegeRulesLoaded(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mCarrierPrivilegeRegistrants.add(r);
            if (areCarrierPriviligeRulesLoaded()) {
                r.notifyRegistrant();
            }
        }
    }

    public void unregisterForCarrierPrivilegeRulesLoaded(Handler h) {
        synchronized (this.mLock) {
            this.mCarrierPrivilegeRegistrants.remove(h);
        }
    }

    private void onIccSwap(boolean isAdded) {
        if (this.mContext.getResources().getBoolean(17956938)) {
            log("onIccSwap: isHotSwapSupported is true, don't prompt for rebooting");
            return;
        }
        log("onIccSwap: isHotSwapSupported is false, prompt for rebooting");
        promptForRestart(isAdded);
    }

    private void promptForRestart(boolean isAdded) {
        if (VSimUtilsInner.isPlatformTwoModems()) {
            if (VSimUtilsInner.isVSimSub(this.mPhoneId) || VSimUtilsInner.isVSimInProcess() || VSimUtilsInner.isVSimReconnecting()) {
                log("[2Cards] mPhoneId=" + this.mPhoneId + " is VSIM sub or VSIM is on(" + VSimUtilsInner.isVSimInProcess() + ")!");
                return;
            } else if (VSimUtilsInner.isVSimOn()) {
                log("[2Cards] Vsim is on , not prompt!");
                return;
            } else if (VSimUtilsInner.isVSimCauseCardReload()) {
                log("[2Cards] Slot " + this.mPhoneId + " VsimCauseCard Reload , not prompt!");
                return;
            }
        }
        synchronized (this.mLock) {
            String dialogComponent = this.mContext.getResources().getString(17039420);
            if (dialogComponent != null) {
                try {
                    this.mContext.startActivity(new Intent().setComponent(ComponentName.unflattenFromString(dialogComponent)).addFlags(268435456).putExtra(EXTRA_ICC_CARD_ADDED, isAdded));
                    return;
                } catch (ActivityNotFoundException e) {
                    loge("Unable to find ICC hotswap prompt for restart activity: " + e);
                }
            }
            if (HwTelephonyFactory.getHwUiccManager().isHotswapSupported()) {
                return;
            }
            AlertDialog dialog = HwTelephonyFactory.getHwUiccManager().createSimAddDialog(this.mContext, isAdded, this.mPhoneId);
            dialog.getWindow().setType(TelephonyEventLog.TAG_IMS_CALL_RECEIVE);
            dialog.show();
        }
    }

    private boolean isPackageInstalled(String pkgName) {
        try {
            this.mContext.getPackageManager().getPackageInfo(pkgName, 1);
            log(pkgName + " is installed.");
            return DBG;
        } catch (NameNotFoundException e) {
            log(pkgName + " is not installed.");
            return false;
        }
    }

    private void promptInstallCarrierApp(String pkgName) {
        OnClickListener listener = new ClickListener(pkgName);
        Resources r = Resources.getSystem();
        String message = r.getString(17040370);
        AlertDialog dialog = new Builder(this.mContext).setMessage(message).setNegativeButton(r.getString(17040372), listener).setPositiveButton(r.getString(17040371), listener).create();
        dialog.getWindow().setType(TelephonyEventLog.TAG_IMS_CALL_RECEIVE);
        dialog.show();
    }

    private void onCarrierPriviligesLoadedMessage() {
        UsageStatsManager usm = (UsageStatsManager) this.mContext.getSystemService("usagestats");
        if (usm != null) {
            usm.onCarrierPrivilegedAppsChanged();
        }
        synchronized (this.mLock) {
            this.mCarrierPrivilegeRegistrants.notifyRegistrants();
            String whitelistSetting = Global.getString(this.mContext.getContentResolver(), "carrier_app_whitelist");
            if (TextUtils.isEmpty(whitelistSetting)) {
                return;
            }
            HashSet<String> carrierAppSet = new HashSet(Arrays.asList(whitelistSetting.split("\\s*;\\s*")));
            if (carrierAppSet.isEmpty()) {
                return;
            }
            for (String pkgName : this.mCarrierPrivilegeRules.getPackageNames()) {
                if (!(TextUtils.isEmpty(pkgName) || !carrierAppSet.contains(pkgName) || isPackageInstalled(pkgName))) {
                    promptInstallCarrierApp(pkgName);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isApplicationOnIcc(AppType type) {
        synchronized (this.mLock) {
            int i = 0;
            while (true) {
                if (i >= this.mUiccApplications.length) {
                    return false;
                } else if (this.mUiccApplications[i] == null || this.mUiccApplications[i].getType() != type) {
                    i++;
                } else {
                    return DBG;
                }
            }
        }
    }

    public CardState getCardState() {
        CardState cardState;
        synchronized (this.mLock) {
            cardState = this.mCardState;
        }
        return cardState;
    }

    public PinState getUniversalPinState() {
        PinState pinState;
        synchronized (this.mLock) {
            pinState = this.mUniversalPinState;
        }
        return pinState;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public UiccCardApplication getApplication(int family) {
        synchronized (this.mLock) {
            UiccCardApplication uiccCardApplication;
            int index = 8;
            switch (family) {
                case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                    index = this.mGsmUmtsSubscriptionAppIndex;
                    break;
                case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                    index = this.mCdmaSubscriptionAppIndex;
                    if (index >= 0) {
                        if (this.mUiccApplications != null && index < this.mUiccApplications.length) {
                            uiccCardApplication = this.mUiccApplications[index];
                            return uiccCardApplication;
                        }
                        break;
                    }
                    return null;
                case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                    index = this.mImsSubscriptionAppIndex;
                    if (index >= 0) {
                        uiccCardApplication = this.mUiccApplications[index];
                        return uiccCardApplication;
                    }
                    return null;
            }
            if (index >= 0) {
                uiccCardApplication = this.mUiccApplications[index];
                return uiccCardApplication;
            }
            return null;
        }
    }

    public UiccCardApplication getApplicationIndex(int index) {
        synchronized (this.mLock) {
            if (index >= 0) {
                if (index < this.mUiccApplications.length) {
                    UiccCardApplication uiccCardApplication = this.mUiccApplications[index];
                    return uiccCardApplication;
                }
            }
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public UiccCardApplication getApplicationByType(int type) {
        synchronized (this.mLock) {
            int i = 0;
            while (true) {
                if (i >= this.mUiccApplications.length) {
                    return null;
                } else if (this.mUiccApplications[i] == null || this.mUiccApplications[i].getType().ordinal() != type) {
                    i++;
                } else {
                    UiccCardApplication uiccCardApplication = this.mUiccApplications[i];
                    return uiccCardApplication;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean resetAppWithAid(String aid) {
        boolean changed;
        synchronized (this.mLock) {
            changed = false;
            int i = 0;
            while (true) {
                if (i < this.mUiccApplications.length) {
                    if (this.mUiccApplications[i] != null && (aid == null || aid.equals(this.mUiccApplications[i].getAid()))) {
                        this.mUiccApplications[i].dispose();
                        this.mUiccApplications[i] = null;
                        changed = DBG;
                    }
                    i++;
                }
            }
        }
        return changed;
    }

    public void iccOpenLogicalChannel(String AID, Message response) {
        loglocal("Open Logical Channel: " + AID + " by pid:" + Binder.getCallingPid() + " uid:" + Binder.getCallingUid());
        this.mCi.iccOpenLogicalChannel(AID, this.mHandler.obtainMessage(EVENT_OPEN_LOGICAL_CHANNEL_DONE, response));
    }

    public void iccCloseLogicalChannel(int channel, Message response) {
        loglocal("Close Logical Channel: " + channel);
        this.mCi.iccCloseLogicalChannel(channel, this.mHandler.obtainMessage(EVENT_CLOSE_LOGICAL_CHANNEL_DONE, response));
    }

    public void iccTransmitApduLogicalChannel(int channel, int cla, int command, int p1, int p2, int p3, String data, Message response) {
        this.mCi.iccTransmitApduLogicalChannel(channel, cla, command, p1, p2, p3, data, this.mHandler.obtainMessage(EVENT_TRANSMIT_APDU_LOGICAL_CHANNEL_DONE, response));
    }

    public void iccTransmitApduBasicChannel(int cla, int command, int p1, int p2, int p3, String data, Message response) {
        this.mCi.iccTransmitApduBasicChannel(cla, command, p1, p2, p3, data, this.mHandler.obtainMessage(EVENT_TRANSMIT_APDU_BASIC_CHANNEL_DONE, response));
    }

    public void iccExchangeSimIO(int fileID, int command, int p1, int p2, int p3, String pathID, Message response) {
        this.mCi.iccIO(command, fileID, pathID, p1, p2, p3, null, null, this.mHandler.obtainMessage(EVENT_SIM_IO_DONE, response));
    }

    public void sendEnvelopeWithStatus(String contents, Message response) {
        this.mCi.sendEnvelopeWithStatus(contents, response);
    }

    public int getNumApplications() {
        int count = 0;
        for (UiccCardApplication a : this.mUiccApplications) {
            if (a != null) {
                count++;
            }
        }
        return count;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public boolean areCarrierPriviligeRulesLoaded() {
        if (this.mCarrierPrivilegeRules != null) {
            return this.mCarrierPrivilegeRules.areCarrierPriviligeRulesLoaded();
        }
        return DBG;
    }

    public boolean hasCarrierPrivilegeRules() {
        if (this.mCarrierPrivilegeRules != null) {
            return this.mCarrierPrivilegeRules.hasCarrierPrivilegeRules();
        }
        return false;
    }

    public int getCarrierPrivilegeStatus(Signature signature, String packageName) {
        if (this.mCarrierPrivilegeRules == null) {
            return -1;
        }
        return this.mCarrierPrivilegeRules.getCarrierPrivilegeStatus(signature, packageName);
    }

    public int getCarrierPrivilegeStatus(PackageManager packageManager, String packageName) {
        if (this.mCarrierPrivilegeRules == null) {
            return -1;
        }
        return this.mCarrierPrivilegeRules.getCarrierPrivilegeStatus(packageManager, packageName);
    }

    public int getCarrierPrivilegeStatus(PackageInfo packageInfo) {
        if (this.mCarrierPrivilegeRules == null) {
            return -1;
        }
        return this.mCarrierPrivilegeRules.getCarrierPrivilegeStatus(packageInfo);
    }

    public int getCarrierPrivilegeStatusForCurrentTransaction(PackageManager packageManager) {
        if (this.mCarrierPrivilegeRules == null) {
            return -1;
        }
        return this.mCarrierPrivilegeRules.getCarrierPrivilegeStatusForCurrentTransaction(packageManager);
    }

    public List<String> getCarrierPackageNamesForIntent(PackageManager packageManager, Intent intent) {
        if (this.mCarrierPrivilegeRules == null) {
            return null;
        }
        return this.mCarrierPrivilegeRules.getCarrierPackageNamesForIntent(packageManager, intent);
    }

    public boolean setOperatorBrandOverride(String brand) {
        log("setOperatorBrandOverride: " + brand);
        log("current iccId: " + getIccId());
        String iccId = getIccId();
        if (TextUtils.isEmpty(iccId)) {
            return false;
        }
        Editor spEditor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        String key = OPERATOR_BRAND_OVERRIDE_PREFIX + iccId;
        if (brand == null) {
            spEditor.remove(key).commit();
        } else {
            spEditor.putString(key, brand).commit();
        }
        return DBG;
    }

    public String getOperatorBrandOverride() {
        String iccId = getIccId();
        if (TextUtils.isEmpty(iccId)) {
            return null;
        }
        return PreferenceManager.getDefaultSharedPreferences(this.mContext).getString(OPERATOR_BRAND_OVERRIDE_PREFIX + iccId, null);
    }

    public String getIccId() {
        for (UiccCardApplication app : this.mUiccApplications) {
            if (app != null) {
                IccRecords ir = app.getIccRecords();
                if (!(ir == null || ir.getIccId() == null)) {
                    return ir.getIccId();
                }
            }
        }
        return null;
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    private void loglocal(String msg) {
        mLocalLog.log(msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        pw.println("UiccCard:");
        pw.println(" mCi=" + this.mCi);
        pw.println(" mLastRadioState=" + this.mLastRadioState);
        pw.println(" mCatService=" + this.mCatService);
        pw.println(" mAbsentRegistrants: size=" + this.mAbsentRegistrants.size());
        for (i = 0; i < this.mAbsentRegistrants.size(); i++) {
            pw.println("  mAbsentRegistrants[" + i + "]=" + ((Registrant) this.mAbsentRegistrants.get(i)).getHandler());
        }
        for (i = 0; i < this.mCarrierPrivilegeRegistrants.size(); i++) {
            pw.println("  mCarrierPrivilegeRegistrants[" + i + "]=" + ((Registrant) this.mCarrierPrivilegeRegistrants.get(i)).getHandler());
        }
        pw.println(" mCardState=" + this.mCardState);
        pw.println(" mUniversalPinState=" + this.mUniversalPinState);
        pw.println(" mGsmUmtsSubscriptionAppIndex=" + this.mGsmUmtsSubscriptionAppIndex);
        pw.println(" mCdmaSubscriptionAppIndex=" + this.mCdmaSubscriptionAppIndex);
        pw.println(" mImsSubscriptionAppIndex=" + this.mImsSubscriptionAppIndex);
        pw.println(" mImsSubscriptionAppIndex=" + this.mImsSubscriptionAppIndex);
        pw.println(" mUiccApplications: length=" + this.mUiccApplications.length);
        for (i = 0; i < this.mUiccApplications.length; i++) {
            if (this.mUiccApplications[i] == null) {
                pw.println("  mUiccApplications[" + i + "]=" + null);
            } else {
                pw.println("  mUiccApplications[" + i + "]=" + this.mUiccApplications[i].getType() + " " + this.mUiccApplications[i]);
            }
        }
        pw.println();
        for (UiccCardApplication app : this.mUiccApplications) {
            if (app != null) {
                app.dump(fd, pw, args);
                pw.println();
            }
        }
        for (UiccCardApplication app2 : this.mUiccApplications) {
            if (app2 != null) {
                IccRecords ir = app2.getIccRecords();
                if (ir != null) {
                    ir.dump(fd, pw, args);
                    pw.println();
                }
            }
        }
        if (this.mCarrierPrivilegeRules == null) {
            pw.println(" mCarrierPrivilegeRules: null");
        } else {
            pw.println(" mCarrierPrivilegeRules: " + this.mCarrierPrivilegeRules);
            this.mCarrierPrivilegeRules.dump(fd, pw, args);
        }
        pw.println(" mCarrierPrivilegeRegistrants: size=" + this.mCarrierPrivilegeRegistrants.size());
        for (i = 0; i < this.mCarrierPrivilegeRegistrants.size(); i++) {
            pw.println("  mCarrierPrivilegeRegistrants[" + i + "]=" + ((Registrant) this.mCarrierPrivilegeRegistrants.get(i)).getHandler());
        }
        pw.flush();
        pw.println("mLocalLog:");
        mLocalLog.dump(fd, pw, args);
        pw.flush();
    }

    public int getGsmUmtsSubscriptionAppIndex() {
        return this.mGsmUmtsSubscriptionAppIndex;
    }

    public int getCdmaSubscriptionAppIndex() {
        return this.mCdmaSubscriptionAppIndex;
    }
}
