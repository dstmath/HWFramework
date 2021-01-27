package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.app.BroadcastOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.telephony.UiccCardInfo;
import android.text.TextUtils;
import android.util.LocalLog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwPartTelephonyFactory;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.RadioConfig;
import com.android.internal.telephony.SubscriptionInfoUpdater;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccSlotStatus;
import com.android.internal.telephony.uicc.euicc.EuiccCard;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.dataconnection.ApnSettingHelper;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccSlotEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

public class UiccController extends Handler implements IUiccControllerInner {
    private static final String AID_ISIM = "A0000000871004";
    public static final int APP_FAM_3GPP = 1;
    public static final int APP_FAM_3GPP2 = 2;
    public static final int APP_FAM_IMS = 3;
    private static final String CARD_STRINGS = "card_strings";
    private static final boolean DBG = true;
    private static final String DEFAULT_CARD = "default_card";
    private static final int EID_LENGTH = 32;
    private static final int EVENT_BALONG_MODEM_RESET = 100001;
    private static final int EVENT_EID_READY = 9;
    private static final int EVENT_GET_ICC_STATUS_DONE = 3;
    private static final int EVENT_GET_SLOT_STATUS_DONE = 4;
    private static final int EVENT_HW_BASE = 100000;
    private static final int EVENT_ICC_STATUS_CHANGED = 1;
    private static final int EVENT_RADIO_AVAILABLE = 6;
    private static final int EVENT_RADIO_ON = 5;
    private static final int EVENT_RADIO_UNAVAILABLE = 7;
    private static final int EVENT_SIM_REFRESH = 8;
    private static final int EVENT_SLOT_STATUS_CHANGED = 2;
    public static final int INVALID_SLOT_ID = -1;
    public static final boolean IS_QUICK_BROADCAST_STATUS = SystemProperties.getBoolean("ro.quick_broadcast_cardstatus", false);
    private static final String LOG_TAG = "UiccController";
    private static final int TEMPORARILY_UNSUPPORTED_CARD_ID = -3;
    private static final boolean VDBG = false;
    @UnsupportedAppUsage
    private static UiccController mInstance;
    private static UiccStateChangedLauncher mLauncher;
    @UnsupportedAppUsage
    private static final Object mLock = new Object();
    private static ArrayList<IccSlotStatus> sLastSlotStatus;
    static LocalLog sLocalLog = new LocalLog(10);
    private ArrayList<String> mCardStrings;
    @UnsupportedAppUsage
    private CommandsInterface[] mCis;
    @UnsupportedAppUsage
    @VisibleForTesting
    public Context mContext;
    private int mDefaultEuiccCardId;
    private IHwUiccControllerEx mHwUiccControllerEx;
    protected RegistrantList mIccChangedRegistrants = new RegistrantList();
    private boolean mIsSlotStatusSupported = true;
    private int[] mPhoneIdToSlotId;
    private RadioConfig mRadioConfig;
    @VisibleForTesting
    public UiccSlot[] mUiccSlots;

    public static UiccController make(Context c, CommandsInterface[] ci) {
        UiccController uiccController;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new UiccController(c, ci);
                int commandsInterfacesArraysLegnth = ci != null ? ci.length : 0;
                CommandsInterfaceEx[] cis = new CommandsInterfaceEx[commandsInterfacesArraysLegnth];
                for (int i = 0; i < commandsInterfacesArraysLegnth; i++) {
                    cis[i] = new CommandsInterfaceEx();
                    cis[i].setCommandsInterface(ci[i]);
                }
                HwTelephonyFactory.getHwUiccManager().initHwAllInOneController(c, cis);
                HwTelephonyFactory.getHwUiccManager().initHwCarrierConfigCardManager(c);
                mLauncher = new UiccStateChangedLauncher(c, mInstance);
                uiccController = mInstance;
            } else {
                throw new RuntimeException("UiccController.make() should only be called once");
            }
        }
        return uiccController;
    }

    private UiccController(Context c, CommandsInterface[] ci) {
        log("Creating UiccController");
        this.mContext = c;
        this.mCis = ci;
        String logStr = "config_num_physical_slots = " + c.getResources().getInteger(17694873);
        log(logStr);
        sLocalLog.log(logStr);
        int numPhysicalSlots = c.getResources().getInteger(17694873);
        CommandsInterface[] commandsInterfaceArr = this.mCis;
        this.mUiccSlots = new UiccSlot[(numPhysicalSlots < commandsInterfaceArr.length ? commandsInterfaceArr.length : numPhysicalSlots)];
        this.mPhoneIdToSlotId = new int[ci.length];
        Arrays.fill(this.mPhoneIdToSlotId, -1);
        this.mRadioConfig = RadioConfig.getInstance(this.mContext);
        this.mHwUiccControllerEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwUiccControllerEx(this);
        this.mRadioConfig.registerForSimSlotStatusChanged(this, 2, null);
        int i = 0;
        while (true) {
            CommandsInterface[] commandsInterfaceArr2 = this.mCis;
            if (i < commandsInterfaceArr2.length) {
                commandsInterfaceArr2[i].registerForIccStatusChanged(this, 1, Integer.valueOf(i));
                if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
                    this.mCis[i].registerForOn(this, 1, Integer.valueOf(i));
                }
                this.mCis[i].registerForAvailable(this, 1, Integer.valueOf(i));
                this.mCis[i].registerForNotAvailable(this, 7, Integer.valueOf(i));
                this.mCis[i].registerForIccRefresh(this, 8, Integer.valueOf(i));
                if (IS_QUICK_BROADCAST_STATUS) {
                    this.mCis[i].registerForIccidChanged(this, 1, Integer.valueOf(i));
                }
                this.mCis[i].registerForUnsolBalongModemReset(this, EVENT_BALONG_MODEM_RESET, Integer.valueOf(i));
                UiccProfile.broadcastIccStateChangedIntent(IccCardConstantsEx.INTENT_VALUE_ICC_NOT_READY, null, i);
                i++;
            } else {
                this.mCardStrings = loadCardStrings();
                this.mDefaultEuiccCardId = -2;
                return;
            }
        }
    }

    public int getPhoneIdFromSlotId(int slotId) {
        int i = 0;
        while (true) {
            int[] iArr = this.mPhoneIdToSlotId;
            if (i >= iArr.length) {
                return -1;
            }
            if (iArr[i] == slotId) {
                return i;
            }
            i++;
        }
    }

    public int getSlotIdFromPhoneId(int phoneId) {
        try {
            return this.mPhoneIdToSlotId[phoneId];
        } catch (ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }

    @UnsupportedAppUsage
    public static UiccController getInstance() {
        UiccController uiccController;
        synchronized (mLock) {
            if (mInstance != null) {
                uiccController = mInstance;
            } else {
                throw new RuntimeException("UiccController.getInstance can't be called before make()");
            }
        }
        return uiccController;
    }

    @UnsupportedAppUsage
    public UiccCard getUiccCard(int phoneId) {
        UiccCard uiccCardForPhone;
        synchronized (mLock) {
            uiccCardForPhone = getUiccCardForPhone(phoneId);
        }
        return uiccCardForPhone;
    }

    public UiccCard getUiccCardForSlot(int slotId) {
        synchronized (mLock) {
            UiccSlot uiccSlot = getUiccSlot(slotId);
            if (uiccSlot == null) {
                return null;
            }
            return uiccSlot.getUiccCard();
        }
    }

    public UiccCard getUiccCardForPhone(int phoneId) {
        UiccSlot uiccSlot;
        synchronized (mLock) {
            if (!isValidPhoneIndex(phoneId) || (uiccSlot = getUiccSlotForPhone(phoneId)) == null) {
                return null;
            }
            return uiccSlot.getUiccCard();
        }
    }

    public UiccProfile getUiccProfileForPhone(int phoneId) {
        synchronized (mLock) {
            UiccProfile uiccProfile = null;
            if (!isValidPhoneIndex(phoneId)) {
                return null;
            }
            UiccCard uiccCard = getUiccCardForPhone(phoneId);
            if (uiccCard != null) {
                uiccProfile = uiccCard.getUiccProfile();
            }
            return uiccProfile;
        }
    }

    public UiccSlot[] getUiccSlots() {
        UiccSlot[] uiccSlotArr;
        synchronized (mLock) {
            uiccSlotArr = this.mUiccSlots;
        }
        return uiccSlotArr;
    }

    public void switchSlots(int[] physicalSlots, Message response) {
        this.mRadioConfig.setSimSlotsMapping(physicalSlots, response);
    }

    public UiccSlot getUiccSlot(int slotId) {
        synchronized (mLock) {
            if (!isValidSlotIndex(slotId)) {
                return null;
            }
            return this.mUiccSlots[slotId];
        }
    }

    public UiccSlot getUiccSlotForPhone(int phoneId) {
        synchronized (mLock) {
            if (isValidPhoneIndex(phoneId)) {
                int slotId = getSlotIdFromPhoneId(phoneId);
                if (isValidSlotIndex(slotId)) {
                    return this.mUiccSlots[slotId];
                }
            }
            return null;
        }
    }

    public int getUiccSlotForCardId(String cardId) {
        UiccCard uiccCard;
        synchronized (mLock) {
            if (cardId == null) {
                try {
                    return -1;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                for (int idx = 0; idx < this.mUiccSlots.length; idx++) {
                    if (!(this.mUiccSlots[idx] == null || (uiccCard = this.mUiccSlots[idx].getUiccCard()) == null || !cardId.equals(uiccCard.getCardId()))) {
                        return idx;
                    }
                }
                for (int idx2 = 0; idx2 < this.mUiccSlots.length; idx2++) {
                    if (this.mUiccSlots[idx2] != null && cardId.equals(this.mUiccSlots[idx2].getIccId())) {
                        return idx2;
                    }
                }
                return -1;
            }
        }
    }

    @UnsupportedAppUsage
    public IccRecords getIccRecords(int phoneId, int family) {
        synchronized (mLock) {
            UiccCardApplication app = getUiccCardApplication(phoneId, family);
            if (app == null) {
                return null;
            }
            return app.getIccRecords();
        }
    }

    @UnsupportedAppUsage
    public IccFileHandler getIccFileHandler(int phoneId, int family) {
        synchronized (mLock) {
            UiccCardApplication app = getUiccCardApplication(phoneId, family);
            if (app == null) {
                return null;
            }
            return app.getIccFileHandler();
        }
    }

    @UnsupportedAppUsage
    public void registerForIccChanged(Handler h, int what, Object obj) {
        synchronized (mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mIccChangedRegistrants.add(r);
            r.notifyRegistrant();
        }
        HwTelephonyFactory.getHwUiccManager().registerForIccChanged(h, what, obj);
    }

    public void unregisterForIccChanged(Handler h) {
        synchronized (mLock) {
            this.mIccChangedRegistrants.remove(h);
        }
        HwTelephonyFactory.getHwUiccManager().unregisterForIccChanged(h);
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        synchronized (mLock) {
            Integer phoneId = getCiIndex(msg);
            if (phoneId.intValue() >= 0) {
                if (phoneId.intValue() < this.mCis.length) {
                    LocalLog localLog = sLocalLog;
                    localLog.log("handleMessage: Received " + msg.what + " for phoneId " + phoneId);
                    AsyncResult ar = (AsyncResult) msg.obj;
                    int i = msg.what;
                    if (i != EVENT_BALONG_MODEM_RESET) {
                        switch (i) {
                            case 1:
                                log("Received EVENT_ICC_STATUS_CHANGED, calling getIccCardStatus");
                                this.mCis[phoneId.intValue()].getIccCardStatus(obtainMessage(3, phoneId));
                                break;
                            case 2:
                            case 4:
                                log("Received EVENT_SLOT_STATUS_CHANGED or EVENT_GET_SLOT_STATUS_DONE");
                                onGetSlotStatusDone(ar);
                                break;
                            case 3:
                                log("Received EVENT_GET_ICC_STATUS_DONE");
                                onGetIccCardStatusDone(ar, phoneId);
                                break;
                            case 5:
                            case 6:
                                log("Received EVENT_RADIO_AVAILABLE/EVENT_RADIO_ON, calling getIccCardStatus");
                                this.mCis[phoneId.intValue()].getIccCardStatus(obtainMessage(3, phoneId));
                                if (phoneId.intValue() == 0) {
                                    log("Received EVENT_RADIO_AVAILABLE/EVENT_RADIO_ON for phoneId 0, calling getIccSlotsStatus");
                                    this.mRadioConfig.getSimSlotsStatus(obtainMessage(4, phoneId));
                                    break;
                                }
                                break;
                            case 7:
                                log("EVENT_RADIO_UNAVAILABLE, dispose card");
                                UiccSlot uiccSlot = getUiccSlotForPhone(phoneId.intValue());
                                if (uiccSlot != null) {
                                    uiccSlot.onRadioStateUnavailable();
                                }
                                this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, phoneId, (Throwable) null));
                                break;
                            case 8:
                                log("Received EVENT_SIM_REFRESH");
                                onSimRefresh(ar, phoneId);
                                break;
                            case 9:
                                log("Received EVENT_EID_READY");
                                onEidReady(ar, phoneId);
                                break;
                            default:
                                Rlog.e(LOG_TAG, " Unknown Event " + msg.what);
                                break;
                        }
                    } else {
                        log("EVENT_BALONG_MODEM_RESET, dispose all cards.");
                        for (int card_index = 0; card_index < this.mCis.length; card_index++) {
                            UiccSlot uiccSlot2 = getUiccSlotForPhone(card_index);
                            if (uiccSlot2 != null) {
                                uiccSlot2.onRadioStateUnavailable();
                            }
                            this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(card_index), (Throwable) null));
                        }
                    }
                    return;
                }
            }
            Rlog.e(LOG_TAG, "Invalid phoneId : " + phoneId + " received with event " + msg.what);
        }
    }

    private Integer getCiIndex(Message msg) {
        Integer index = new Integer(0);
        if (msg == null) {
            return index;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return (Integer) msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return index;
        }
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return index;
        }
        return (Integer) ar.userObj;
    }

    @UnsupportedAppUsage
    public UiccCardApplication getUiccCardApplication(int phoneId, int family) {
        synchronized (mLock) {
            UiccCard uiccCard = getUiccCardForPhone(phoneId);
            if (uiccCard == null) {
                return null;
            }
            return uiccCard.getApplication(family);
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.uicc.UiccController$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$IccCardConstants$State = new int[IccCardConstants.State.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.ABSENT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.PIN_REQUIRED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.PUK_REQUIRED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.NETWORK_LOCKED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.READY.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.NOT_READY.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.PERM_DISABLED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.CARD_IO_ERROR.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.CARD_RESTRICTED.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.LOADED.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
        }
    }

    static String getIccStateIntentString(IccCardConstants.State state) {
        switch (AnonymousClass1.$SwitchMap$com$android$internal$telephony$IccCardConstants$State[state.ordinal()]) {
            case 1:
                return IccCardConstantsEx.INTENT_VALUE_ICC_ABSENT;
            case 2:
                return IccCardConstantsEx.INTENT_VALUE_ICC_LOCKED;
            case 3:
                return IccCardConstantsEx.INTENT_VALUE_ICC_LOCKED;
            case 4:
                return IccCardConstantsEx.INTENT_VALUE_ICC_LOCKED;
            case 5:
                return IccCardConstantsEx.INTENT_VALUE_ICC_READY;
            case 6:
                return IccCardConstantsEx.INTENT_VALUE_ICC_NOT_READY;
            case 7:
                return IccCardConstantsEx.INTENT_VALUE_ICC_LOCKED;
            case 8:
                return IccCardConstantsEx.INTENT_VALUE_ICC_CARD_IO_ERROR;
            case 9:
                return IccCardConstantsEx.INTENT_VALUE_ICC_CARD_RESTRICTED;
            case 10:
                return IccCardConstantsEx.INTENT_VALUE_ICC_LOADED;
            default:
                return IccCardConstantsEx.INTENT_VALUE_ICC_UNKNOWN;
        }
    }

    static void updateInternalIccState(Context context, IccCardConstants.State state, String reason, int phoneId) {
        updateInternalIccState(context, state, reason, phoneId, false);
    }

    static void updateInternalIccState(Context context, IccCardConstants.State state, String reason, int phoneId, boolean absentAndInactive) {
        ((TelephonyManager) context.getSystemService("phone")).setSimStateForPhone(phoneId, state.toString());
        SubscriptionInfoUpdater subInfoUpdator = PhoneFactory.getSubscriptionInfoUpdater();
        if (subInfoUpdator != null) {
            subInfoUpdator.updateInternalIccState(getIccStateIntentString(state), reason, phoneId, absentAndInactive);
        } else {
            Rlog.e(LOG_TAG, "subInfoUpdate is null.");
        }
    }

    private synchronized void onGetIccCardStatusDone(AsyncResult ar, Integer index) {
        String cardString;
        if (ar.exception != null) {
            Rlog.e(LOG_TAG, "Error getting ICC status. RIL_REQUEST_GET_ICC_STATUS should never return an error", ar.exception);
        } else if (!isValidPhoneIndex(index.intValue())) {
            Rlog.e(LOG_TAG, "onGetIccCardStatusDone: invalid index : " + index);
        } else {
            IccCardStatus status = (IccCardStatus) ar.result;
            IccCardStatusExt iccCardStatusExt = new IccCardStatusExt();
            iccCardStatusExt.setIccCardStatus(status);
            LocalLog localLog = sLocalLog;
            localLog.log("onGetIccCardStatusDone: phoneId " + index + " IccCardStatus: " + status);
            int slotId = status.physicalSlotIndex;
            if (!isValidSlotIndex(slotId)) {
                log("invalid slotId changed from " + slotId + " to " + index);
                slotId = index.intValue();
            }
            if (eidIsNotSupported(status)) {
                log("eid is not supported");
                this.mDefaultEuiccCardId = -1;
            }
            this.mPhoneIdToSlotId[index.intValue()] = slotId;
            if (status.mCardState == IccCardStatus.CardState.CARDSTATE_ABSENT) {
                TelephonyManager.getDefault();
                TelephonyManager.setTelephonyProperty(index.intValue(), IccRecords.PROPERTY_MCC_MATCHING_FYROM, PhoneConfigurationManager.SSSS);
            }
            if (this.mUiccSlots[slotId] == null) {
                if (2 == this.mCis[index.intValue()].getRadioState()) {
                    Rlog.e(LOG_TAG, "Current RadioState is RADIO_UNAVAILABLE,return immediatly");
                    return;
                }
                this.mUiccSlots[slotId] = new UiccSlot(this.mContext, true);
                UiccSlotEx uiccSlotEx = new UiccSlotEx();
                uiccSlotEx.setUiccSlot(this.mUiccSlots[slotId]);
                HwTelephonyFactory.getHwUiccManager().initUiccCard(uiccSlotEx, iccCardStatusExt, index);
            }
            this.mUiccSlots[slotId].update(this.mCis[index.intValue()], status, index.intValue(), slotId);
            UiccCardExt uiccCardExt = new UiccCardExt();
            uiccCardExt.setUiccCard(getUiccCardForPhone(index.intValue()));
            HwTelephonyFactory.getHwUiccManager().updateUiccCard(uiccCardExt, iccCardStatusExt, index);
            HwTelephonyFactory.getHwUiccManager().onGetIccStatusDone(ar, index);
            UiccCard card = this.mUiccSlots[slotId].getUiccCard();
            if (card == null) {
                log("mUiccSlots[" + slotId + "] has no card. Notifying IccChangedRegistrants");
                this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, index, (Throwable) null));
                return;
            }
            boolean isEuicc = this.mUiccSlots[slotId].isEuicc();
            if (isEuicc) {
                cardString = ((EuiccCard) card).getEid();
            } else {
                cardString = card.getIccId();
            }
            if (isEuicc && cardString == null && this.mDefaultEuiccCardId != -1) {
                ((EuiccCard) card).registerForEidReady(this, 9, index);
            }
            if (cardString != null) {
                addCardId(cardString);
                if (this.mDefaultEuiccCardId == -2 && !TextUtils.isEmpty(cardString)) {
                    this.mDefaultEuiccCardId = convertToPublicCardId(cardString);
                }
            }
            log("Notifying IccChangedRegistrants");
            this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, index, (Throwable) null));
            this.mHwUiccControllerEx.processRadioPowerDownIfNoCard();
        }
    }

    private boolean eidIsNotSupported(IccCardStatus status) {
        return status.physicalSlotIndex == -1;
    }

    private void addCardId(String cardString) {
        if (!TextUtils.isEmpty(cardString)) {
            if (cardString.length() < 32) {
                cardString = IccUtils.stripTrailingFs(cardString);
            }
            if (!this.mCardStrings.contains(cardString)) {
                this.mCardStrings.add(cardString);
                saveCardStrings();
            }
        }
    }

    public int convertToPublicCardId(String cardString) {
        if (this.mDefaultEuiccCardId == -1) {
            return -1;
        }
        if (TextUtils.isEmpty(cardString)) {
            return -2;
        }
        if (cardString.length() < 32) {
            cardString = IccUtils.stripTrailingFs(cardString);
        }
        int id = this.mCardStrings.indexOf(cardString);
        if (id == -1) {
            return -2;
        }
        return id;
    }

    public ArrayList<UiccCardInfo> getAllUiccCardInfos() {
        String iccid;
        String eid;
        int cardId;
        ArrayList<UiccCardInfo> infos = new ArrayList<>();
        int slotIndex = 0;
        while (true) {
            UiccSlot[] uiccSlotArr = this.mUiccSlots;
            if (slotIndex >= uiccSlotArr.length) {
                return infos;
            }
            UiccSlot slot = uiccSlotArr[slotIndex];
            if (slot != null) {
                boolean isEuicc = slot.isEuicc();
                UiccCard card = slot.getUiccCard();
                boolean isRemovable = slot.isRemovable();
                if (card != null) {
                    String iccid2 = card.getIccId();
                    if (isEuicc) {
                        String eid2 = ((EuiccCard) card).getEid();
                        eid = eid2;
                        iccid = iccid2;
                        cardId = convertToPublicCardId(eid2);
                    } else {
                        eid = null;
                        iccid = iccid2;
                        cardId = convertToPublicCardId(iccid2);
                    }
                } else {
                    String iccid3 = slot.getIccId();
                    if (isEuicc || TextUtils.isEmpty(iccid3)) {
                        eid = null;
                        iccid = iccid3;
                        cardId = -2;
                    } else {
                        eid = null;
                        iccid = iccid3;
                        cardId = convertToPublicCardId(iccid3);
                    }
                }
                infos.add(new UiccCardInfo(isEuicc, cardId, eid, IccUtils.stripTrailingFs(iccid), slotIndex, isRemovable));
            }
            slotIndex++;
        }
    }

    public int getCardIdForDefaultEuicc() {
        int i = this.mDefaultEuiccCardId;
        if (i == -3) {
            return -1;
        }
        return i;
    }

    private ArrayList<String> loadCardStrings() {
        String cardStrings = PreferenceManager.getDefaultSharedPreferences(this.mContext).getString(CARD_STRINGS, PhoneConfigurationManager.SSSS);
        if (TextUtils.isEmpty(cardStrings)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(cardStrings.split(",")));
    }

    private void saveCardStrings() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        editor.putString(CARD_STRINGS, TextUtils.join(",", this.mCardStrings));
        editor.commit();
    }

    private synchronized void onGetSlotStatusDone(AsyncResult ar) {
        if (this.mIsSlotStatusSupported) {
            Throwable e = ar.exception;
            if (e != null) {
                if (!(e instanceof CommandException) || ((CommandException) e).getCommandError() != CommandException.Error.REQUEST_NOT_SUPPORTED) {
                    String logStr = "Unexpected error getting slot status: " + ar.exception;
                    Rlog.e(LOG_TAG, logStr);
                    sLocalLog.log(logStr);
                } else {
                    log("onGetSlotStatusDone: request not supported; marking mIsSlotStatusSupported to false");
                    sLocalLog.log("onGetSlotStatusDone: request not supported; marking mIsSlotStatusSupported to false");
                    this.mIsSlotStatusSupported = false;
                }
                return;
            }
            ArrayList<IccSlotStatus> status = (ArrayList) ar.result;
            if (!slotStatusChanged(status)) {
                log("onGetSlotStatusDone: No change in slot status");
                return;
            }
            sLastSlotStatus = status;
            int numActiveSlots = 0;
            boolean isDefaultEuiccCardIdSet = false;
            boolean anyEuiccIsActive = false;
            boolean hasEuicc = false;
            int i = 0;
            while (true) {
                boolean isActive = true;
                if (i >= status.size()) {
                    break;
                }
                IccSlotStatus iss = status.get(i);
                if (iss.slotState != IccSlotStatus.SlotState.SLOTSTATE_ACTIVE) {
                    isActive = false;
                }
                if (isActive) {
                    numActiveSlots++;
                    if (!isValidPhoneIndex(iss.logicalSlotIndex)) {
                        Rlog.e(LOG_TAG, "Skipping slot " + i + " as phone " + iss.logicalSlotIndex + " is not available to communicate with this slot");
                    } else {
                        this.mPhoneIdToSlotId[iss.logicalSlotIndex] = i;
                    }
                }
                if (this.mUiccSlots[i] == null) {
                    this.mUiccSlots[i] = new UiccSlot(this.mContext, isActive);
                }
                CommandsInterface commandsInterface = null;
                if (!isValidPhoneIndex(iss.logicalSlotIndex)) {
                    this.mUiccSlots[i].update(null, iss, i);
                } else {
                    UiccSlot uiccSlot = this.mUiccSlots[i];
                    if (isActive) {
                        commandsInterface = this.mCis[iss.logicalSlotIndex];
                    }
                    uiccSlot.update(commandsInterface, iss, i);
                }
                if (this.mUiccSlots[i].isEuicc()) {
                    hasEuicc = true;
                    if (isActive) {
                        anyEuiccIsActive = true;
                    }
                    String eid = iss.eid;
                    if (!TextUtils.isEmpty(eid)) {
                        addCardId(eid);
                        if (!isDefaultEuiccCardIdSet) {
                            isDefaultEuiccCardIdSet = true;
                            this.mDefaultEuiccCardId = convertToPublicCardId(eid);
                            log("Using eid=" + eid + " in slot=" + i + " to set mDefaultEuiccCardId=" + this.mDefaultEuiccCardId);
                        }
                    }
                }
                i++;
            }
            if (hasEuicc && !anyEuiccIsActive && !isDefaultEuiccCardIdSet) {
                log("onGetSlotStatusDone: setting TEMPORARILY_UNSUPPORTED_CARD_ID");
                this.mDefaultEuiccCardId = -3;
            }
            if (numActiveSlots != this.mPhoneIdToSlotId.length) {
                Rlog.e(LOG_TAG, "Number of active slots " + numActiveSlots + " does not match the number of Phones" + this.mPhoneIdToSlotId.length);
            }
            Set<Integer> slotIds = new HashSet<>();
            int[] iArr = this.mPhoneIdToSlotId;
            for (int slotId : iArr) {
                if (!slotIds.contains(Integer.valueOf(slotId))) {
                    slotIds.add(Integer.valueOf(slotId));
                } else {
                    throw new RuntimeException("slotId " + slotId + " mapped to multiple phoneIds");
                }
            }
            BroadcastOptions options = BroadcastOptions.makeBasic();
            options.setBackgroundActivityStartsAllowed(true);
            Intent intent = new Intent("android.telephony.action.SIM_SLOT_STATUS_CHANGED");
            intent.addFlags(67108864);
            intent.addFlags(ApnSettingHelper.TYPE_WIFI_MMS);
            this.mContext.sendBroadcast(intent, "android.permission.READ_PRIVILEGED_PHONE_STATE", options.toBundle());
        }
    }

    private boolean slotStatusChanged(ArrayList<IccSlotStatus> slotStatusList) {
        ArrayList<IccSlotStatus> arrayList = sLastSlotStatus;
        if (arrayList == null || arrayList.size() != slotStatusList.size()) {
            return true;
        }
        Iterator<IccSlotStatus> it = slotStatusList.iterator();
        while (it.hasNext()) {
            if (!sLastSlotStatus.contains(it.next())) {
                return true;
            }
        }
        return false;
    }

    private void logPhoneIdToSlotIdMapping() {
        log("mPhoneIdToSlotId mapping:");
        for (int i = 0; i < this.mPhoneIdToSlotId.length; i++) {
            log("    phoneId " + i + " slotId " + this.mPhoneIdToSlotId[i]);
        }
    }

    private void onSimRefresh(AsyncResult ar, Integer index) {
        boolean changed;
        if (ar.exception != null) {
            Rlog.e(LOG_TAG, "onSimRefresh: Sim REFRESH with exception: " + ar.exception);
        } else if (!isValidPhoneIndex(index.intValue())) {
            Rlog.e(LOG_TAG, "onSimRefresh: invalid index : " + index);
        } else {
            IccRefreshResponse resp = (IccRefreshResponse) ar.result;
            log("onSimRefresh: " + resp);
            LocalLog localLog = sLocalLog;
            localLog.log("onSimRefresh: " + resp);
            if (resp == null) {
                Rlog.e(LOG_TAG, "onSimRefresh: received without input");
                return;
            }
            UiccCard uiccCard = getUiccCardForPhone(index.intValue());
            if (uiccCard == null) {
                Rlog.e(LOG_TAG, "onSimRefresh: refresh on null card : " + index);
                return;
            }
            int i = resp.refreshResult;
            if (i != 0) {
                if (i == 1) {
                    changed = uiccCard.resetAppWithAid(resp.aid, false);
                } else if (i == 2) {
                    changed = uiccCard.resetAppWithAid(resp.aid, true);
                } else {
                    return;
                }
            } else if (IccRecords.EFID_SET.contains(Integer.valueOf(resp.efId))) {
                log("FIEL_UPDATE resetAppWithAid");
                changed = uiccCard.resetAppWithAid(resp.aid, true);
            } else {
                return;
            }
            if (changed && resp.refreshResult == 2) {
                if (TextUtils.isEmpty(resp.aid) || !resp.aid.toUpperCase(Locale.ENGLISH).startsWith(AID_ISIM)) {
                    ((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).updateConfigForPhoneId(index.intValue(), IccCardConstantsEx.INTENT_VALUE_ICC_UNKNOWN);
                }
                if (this.mContext.getResources().getBoolean(17891501)) {
                    this.mCis[index.intValue()].setRadioPower(false, null);
                }
            }
            this.mCis[index.intValue()].getIccCardStatus(obtainMessage(3, index));
        }
    }

    private void onEidReady(AsyncResult ar, Integer index) {
        if (ar.exception != null) {
            Rlog.e(LOG_TAG, "onEidReady: exception: " + ar.exception);
        } else if (!isValidPhoneIndex(index.intValue())) {
            Rlog.e(LOG_TAG, "onEidReady: invalid index: " + index);
        } else {
            int slotId = this.mPhoneIdToSlotId[index.intValue()];
            UiccCard card = this.mUiccSlots[slotId].getUiccCard();
            if (card == null) {
                Rlog.e(LOG_TAG, "onEidReady: UiccCard in slot " + slotId + " is null");
                return;
            }
            String eid = ((EuiccCard) card).getEid();
            addCardId(eid);
            int i = this.mDefaultEuiccCardId;
            if (i == -2 || i == -3) {
                this.mDefaultEuiccCardId = convertToPublicCardId(eid);
                log("onEidReady: eid=" + eid + " slot=" + slotId + " mDefaultEuiccCardId=" + this.mDefaultEuiccCardId);
            }
            ((EuiccCard) card).unregisterForEidReady(this);
        }
    }

    public static boolean isCdmaSupported(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.telephony.cdma");
    }

    private boolean isValidPhoneIndex(int index) {
        return index >= 0 && index < TelephonyManager.getDefault().getPhoneCount();
    }

    private boolean isValidSlotIndex(int index) {
        return index >= 0 && index < this.mUiccSlots.length;
    }

    @UnsupportedAppUsage
    private void log(String string) {
        Rlog.i(LOG_TAG, string);
    }

    public void addCardLog(String data) {
        sLocalLog.log(data);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("UiccController: " + this);
        pw.println(" mContext=" + this.mContext);
        pw.println(" mInstance=" + mInstance);
        pw.println(" mIccChangedRegistrants: size=" + this.mIccChangedRegistrants.size());
        for (int i = 0; i < this.mIccChangedRegistrants.size(); i++) {
            pw.println("  mIccChangedRegistrants[" + i + "]=" + ((Registrant) this.mIccChangedRegistrants.get(i)).getHandler());
        }
        pw.println();
        pw.flush();
        pw.println(" mIsCdmaSupported=" + isCdmaSupported(this.mContext));
        pw.println(" mUiccSlots: size=" + this.mUiccSlots.length);
        pw.println(" mCardStrings=*");
        pw.println(" mDefaultEuiccCardId=" + this.mDefaultEuiccCardId);
        int i2 = 0;
        while (true) {
            UiccSlot[] uiccSlotArr = this.mUiccSlots;
            if (i2 < uiccSlotArr.length) {
                if (uiccSlotArr[i2] == null) {
                    pw.println("  mUiccSlots[" + i2 + "]=null");
                } else {
                    pw.println("  mUiccSlots[" + i2 + "]=" + this.mUiccSlots[i2]);
                    this.mUiccSlots[i2].dump(fd, pw, args);
                }
                i2++;
            } else {
                pw.println(" sLocalLog= ");
                sLocalLog.dump(fd, pw, args);
                return;
            }
        }
    }

    @Override // com.android.internal.telephony.uicc.IUiccControllerInner
    public UiccCardExt[] getUiccCards() {
        if (getUiccSlots() == null) {
            log("haven't get all UiccCards done, please wait!");
            return null;
        }
        UiccSlot[] uiccSlots = getUiccSlots();
        UiccCard[] uiccCards = new UiccCard[uiccSlots.length];
        UiccCardExt[] uiccCardExts = new UiccCardExt[uiccSlots.length];
        int index = 0;
        for (UiccSlot uiccSlot : uiccSlots) {
            if (uiccSlot != null) {
                uiccCards[index] = uiccSlot.getUiccCard();
                uiccCardExts[index] = new UiccCardExt();
                uiccCardExts[index].setUiccCard(uiccCards[index]);
            }
            index++;
        }
        return uiccCardExts;
    }

    @Override // com.android.internal.telephony.uicc.IUiccControllerInner
    public CommandsInterfaceEx[] getCis() {
        CommandsInterface[] commandsInterfaces = (CommandsInterface[]) this.mCis.clone();
        CommandsInterfaceEx[] commandsInterfaceExes = new CommandsInterfaceEx[commandsInterfaces.length];
        int index = 0;
        for (CommandsInterface commandsInterface : commandsInterfaces) {
            commandsInterfaceExes[index] = new CommandsInterfaceEx();
            commandsInterfaceExes[index].setCommandsInterface(commandsInterface);
            index++;
        }
        return commandsInterfaceExes;
    }

    @Override // com.android.internal.telephony.uicc.IUiccControllerInner
    public void disposeCard(int index) {
        synchronized (mLock) {
            UiccSlot uiccSlot = getUiccSlotForPhone(index);
            if (uiccSlot != null) {
                log("Disposing card " + index);
                uiccSlot.onRadioStateUnavailable();
            }
        }
    }

    @Override // com.android.internal.telephony.uicc.IUiccControllerInner
    public void onRefresh(int slotId, int[] fileList) {
        IccRecords iccRecords;
        boolean fileChanged = fileList != null && fileList.length > 0;
        log("onRefresh: fileChanged = " + fileChanged + "  slotId = " + slotId);
        UiccCardApplication uiccCardApplication = getUiccProfileForPhone(slotId) != null ? getUiccProfileForPhone(slotId).getApplicationIndex(slotId) : null;
        if (fileChanged && isValidPhoneIndex(slotId) && uiccCardApplication != null && (iccRecords = uiccCardApplication.getIccRecords()) != null) {
            iccRecords.onRefresh(fileChanged, fileList);
        }
    }

    @Override // com.android.internal.telephony.uicc.IUiccControllerInner
    public void notifyFdnStatusChange() {
        this.mHwUiccControllerEx.notifyFdnStatusChange();
    }

    @Override // com.android.internal.telephony.uicc.IUiccControllerInner
    public void getUiccCardStatus(Message result, int slotId) {
        this.mHwUiccControllerEx.getUiccCardStatus(result, slotId);
    }

    @Override // com.android.internal.telephony.uicc.IUiccControllerInner
    public void registerForFdnStatusChange(Handler h, int what, Object obj) {
        this.mHwUiccControllerEx.registerForFdnStatusChange(h, what, obj);
    }

    @Override // com.android.internal.telephony.uicc.IUiccControllerInner
    public void unregisterForFdnStatusChange(Handler h) {
        this.mHwUiccControllerEx.unregisterForFdnStatusChange(h);
    }
}
