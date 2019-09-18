package com.android.internal.telephony.uicc;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.util.LocalLog;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.RadioConfig;
import com.android.internal.telephony.SubscriptionInfoUpdater;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccSlotStatus;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class UiccController extends AbstractUiccController {
    public static final int APP_FAM_3GPP = 1;
    public static final int APP_FAM_3GPP2 = 2;
    public static final int APP_FAM_IMS = 3;
    private static final boolean DBG = true;
    private static final int EVENT_BALONG_MODEM_RESET = 9;
    private static final int EVENT_GET_ICC_STATUS_DONE = 3;
    private static final int EVENT_GET_SLOT_STATUS_DONE = 4;
    private static final int EVENT_ICC_STATUS_CHANGED = 1;
    private static final int EVENT_RADIO_AVAILABLE = 6;
    private static final int EVENT_RADIO_ON = 5;
    private static final int EVENT_RADIO_UNAVAILABLE = 7;
    private static final int EVENT_SIM_REFRESH = 8;
    private static final int EVENT_SLOT_STATUS_CHANGED = 2;
    public static final int INVALID_SLOT_ID = -1;
    public static final boolean IS_QUICK_BROADCAST_STATUS = SystemProperties.getBoolean("ro.quick_broadcast_cardstatus", false);
    private static final String LOG_TAG = "UiccController";
    private static final boolean VDBG = false;
    private static UiccController mInstance;
    private static UiccStateChangedLauncher mLauncher;
    private static final Object mLock = new Object();
    private static ArrayList<IccSlotStatus> sLastSlotStatus;
    static LocalLog sLocalLog = new LocalLog(100);
    private CommandsInterface[] mCis;
    private Context mContext;
    protected RegistrantList mIccChangedRegistrants = new RegistrantList();
    private boolean mIsSlotStatusSupported = true;
    private int[] mPhoneIdToSlotId;
    private RadioConfig mRadioConfig;
    private UiccSlot[] mUiccSlots;

    public static UiccController make(Context c, CommandsInterface[] ci) {
        UiccController uiccController;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new UiccController(c, ci);
                HwTelephonyFactory.getHwUiccManager().initHwDsdsController(c, ci);
                HwTelephonyFactory.getHwUiccManager().initHwAllInOneController(c, ci);
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
        String logStr = "config_num_physical_slots = " + c.getResources().getInteger(17694847);
        log(logStr);
        sLocalLog.log(logStr);
        this.mUiccSlots = new UiccSlot[(c.getResources().getInteger(17694847) < this.mCis.length ? this.mCis.length : c.getResources().getInteger(17694847))];
        this.mPhoneIdToSlotId = new int[ci.length];
        Arrays.fill(this.mPhoneIdToSlotId, -1);
        this.mRadioConfig = RadioConfig.getInstance(this.mContext);
        this.mRadioConfig.registerForSimSlotStatusChanged(this, 2, null);
        for (int i = 0; i < this.mCis.length; i++) {
            this.mCis[i].registerForIccStatusChanged(this, 1, Integer.valueOf(i));
            if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
                this.mCis[i].registerForOn(this, 1, Integer.valueOf(i));
            }
            this.mCis[i].registerForAvailable(this, 1, Integer.valueOf(i));
            this.mCis[i].registerForNotAvailable(this, 7, Integer.valueOf(i));
            this.mCis[i].registerForIccRefresh(this, 8, Integer.valueOf(i));
            if (IS_QUICK_BROADCAST_STATUS) {
                this.mCis[i].registerForIccidChanged(this, 1, Integer.valueOf(i));
            }
            this.mCis[i].registerForUnsolBalongModemReset(this, 9, Integer.valueOf(i));
            UiccProfile.broadcastIccStateChangedIntent("NOT_READY", null, i);
        }
    }

    private int getSlotIdFromPhoneId(int phoneId) {
        return this.mPhoneIdToSlotId[phoneId];
    }

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
            UiccCard uiccCard = uiccSlot.getUiccCard();
            return uiccCard;
        }
    }

    public UiccCard getUiccCardForPhone(int phoneId) {
        synchronized (mLock) {
            if (isValidPhoneIndex(phoneId)) {
                UiccSlot uiccSlot = getUiccSlotForPhone(phoneId);
                if (uiccSlot != null) {
                    UiccCard uiccCard = uiccSlot.getUiccCard();
                    return uiccCard;
                }
            }
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0016, code lost:
        return r2;
     */
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
            UiccSlot uiccSlot = this.mUiccSlots[slotId];
            return uiccSlot;
        }
    }

    public UiccSlot getUiccSlotForPhone(int phoneId) {
        synchronized (mLock) {
            if (isValidPhoneIndex(phoneId)) {
                int slotId = getSlotIdFromPhoneId(phoneId);
                if (isValidSlotIndex(slotId)) {
                    UiccSlot uiccSlot = this.mUiccSlots[slotId];
                    return uiccSlot;
                }
            }
            return null;
        }
    }

    public int getUiccSlotForCardId(String cardId) {
        synchronized (mLock) {
            for (int idx = 0; idx < this.mUiccSlots.length; idx++) {
                if (this.mUiccSlots[idx] != null) {
                    UiccCard uiccCard = this.mUiccSlots[idx].getUiccCard();
                    if (uiccCard != null && cardId.equals(uiccCard.getCardId())) {
                        return idx;
                    }
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

    public IccRecords getIccRecords(int phoneId, int family) {
        synchronized (mLock) {
            UiccCardApplication app = getUiccCardApplication(phoneId, family);
            if (app == null) {
                return null;
            }
            IccRecords iccRecords = app.getIccRecords();
            return iccRecords;
        }
    }

    public IccFileHandler getIccFileHandler(int phoneId, int family) {
        synchronized (mLock) {
            UiccCardApplication app = getUiccCardApplication(phoneId, family);
            if (app == null) {
                return null;
            }
            IccFileHandler iccFileHandler = app.getIccFileHandler();
            return iccFileHandler;
        }
    }

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

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x011e, code lost:
        return;
     */
    public void handleMessage(Message msg) {
        synchronized (mLock) {
            Integer phoneId = getCiIndex(msg);
            if (phoneId.intValue() >= 0) {
                if (phoneId.intValue() < this.mCis.length) {
                    LocalLog localLog = sLocalLog;
                    localLog.log("handleMessage: Received " + msg.what + " for phoneId " + phoneId);
                    AsyncResult ar = (AsyncResult) msg.obj;
                    switch (msg.what) {
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
                            HwTelephonyFactory.getHwPhoneManager().saveUiccCardsToVirtualNet(getUiccCards());
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
                            this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, phoneId, null));
                            HwTelephonyFactory.getHwPhoneManager().saveUiccCardsToVirtualNet(getUiccCards());
                            break;
                        case 8:
                            log("Received EVENT_SIM_REFRESH");
                            onSimRefresh(ar, phoneId);
                            break;
                        case 9:
                            log("EVENT_BALONG_MODEM_RESET, dispose all cards.");
                            for (int card_index = 0; card_index < this.mCis.length; card_index++) {
                                UiccSlot uiccSlot2 = getUiccSlotForPhone(card_index);
                                if (uiccSlot2 != null) {
                                    uiccSlot2.onRadioStateUnavailable();
                                }
                                this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(card_index), null));
                            }
                            HwTelephonyFactory.getHwPhoneManager().saveUiccCardsToVirtualNet(getUiccCards());
                            break;
                        default:
                            Rlog.e(LOG_TAG, " Unknown Event " + msg.what);
                            break;
                    }
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

    public UiccCardApplication getUiccCardApplication(int phoneId, int family) {
        synchronized (mLock) {
            UiccCard uiccCard = getUiccCardForPhone(phoneId);
            if (uiccCard == null) {
                return null;
            }
            UiccCardApplication application = uiccCard.getApplication(family);
            return application;
        }
    }

    static void updateInternalIccState(String value, String reason, int phoneId) {
        SubscriptionInfoUpdater subInfoUpdator = PhoneFactory.getSubscriptionInfoUpdater();
        if (subInfoUpdator != null) {
            subInfoUpdator.updateInternalIccState(value, reason, phoneId);
        } else {
            Rlog.e(LOG_TAG, "subInfoUpdate is null.");
        }
    }

    private synchronized void onGetIccCardStatusDone(AsyncResult ar, Integer index) {
        if (ar.exception != null) {
            Rlog.e(LOG_TAG, "Error getting ICC status. RIL_REQUEST_GET_ICC_STATUS should never return an error", ar.exception);
        } else if (!isValidPhoneIndex(index.intValue())) {
            Rlog.e(LOG_TAG, "onGetIccCardStatusDone: invalid index : " + index);
        } else {
            IccCardStatus status = (IccCardStatus) ar.result;
            LocalLog localLog = sLocalLog;
            localLog.log("onGetIccCardStatusDone: phoneId " + index + " IccCardStatus: " + status);
            int slotId = status.physicalSlotIndex;
            if (slotId == -1) {
                slotId = index.intValue();
            }
            this.mPhoneIdToSlotId[index.intValue()] = slotId;
            if (status.mCardState == IccCardStatus.CardState.CARDSTATE_ABSENT) {
                TelephonyManager.getDefault();
                TelephonyManager.setTelephonyProperty(index.intValue(), IccRecords.PROPERTY_MCC_MATCHING_FYROM, "");
            }
            if (this.mUiccSlots[slotId] == null) {
                if (CommandsInterface.RadioState.RADIO_UNAVAILABLE == this.mCis[index.intValue()].getRadioState()) {
                    Rlog.e(LOG_TAG, "Current RadioState is RADIO_UNAVAILABLE,return immediatly");
                    return;
                } else {
                    this.mUiccSlots[slotId] = new UiccSlot(this.mContext, true);
                    HwTelephonyFactory.getHwUiccManager().initUiccCard(this.mUiccSlots[slotId], status, index);
                }
            }
            this.mUiccSlots[slotId].update(this.mCis[index.intValue()], status, index.intValue());
            HwTelephonyFactory.getHwUiccManager().updateUiccCard(getUiccCardForPhone(index.intValue()), status, index);
            HwTelephonyFactory.getHwUiccManager().onGetIccStatusDone(ar, index);
            log("Notifying IccChangedRegistrants");
            this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, index, null));
            processRadioPowerDownIfNoCard(getUiccCards());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0048, code lost:
        return;
     */
    private synchronized void onGetSlotStatusDone(AsyncResult ar) {
        if (this.mIsSlotStatusSupported) {
            Throwable e = ar.exception;
            int i = 0;
            if (e != null) {
                if (e instanceof CommandException) {
                    if (((CommandException) e).getCommandError() == CommandException.Error.REQUEST_NOT_SUPPORTED) {
                        log("onGetSlotStatusDone: request not supported; marking mIsSlotStatusSupported to false");
                        sLocalLog.log("onGetSlotStatusDone: request not supported; marking mIsSlotStatusSupported to false");
                        this.mIsSlotStatusSupported = false;
                    }
                }
                String logStr = "Unexpected error getting slot status: " + ar.exception;
                Rlog.e(LOG_TAG, logStr);
                sLocalLog.log(logStr);
            } else {
                ArrayList<IccSlotStatus> status = (ArrayList) ar.result;
                if (!slotStatusChanged(status)) {
                    log("onGetSlotStatusDone: No change in slot status");
                    return;
                }
                sLastSlotStatus = status;
                int numActiveSlots = 0;
                for (int i2 = 0; i2 < status.size(); i2++) {
                    IccSlotStatus iss = status.get(i2);
                    boolean isActive = iss.slotState == IccSlotStatus.SlotState.SLOTSTATE_ACTIVE;
                    if (isActive) {
                        numActiveSlots++;
                        if (isValidPhoneIndex(iss.logicalSlotIndex)) {
                            this.mPhoneIdToSlotId[iss.logicalSlotIndex] = i2;
                        } else {
                            throw new RuntimeException("Logical slot index " + iss.logicalSlotIndex + " invalid for physical slot " + i2);
                        }
                    }
                    if (this.mUiccSlots[i2] == null) {
                        this.mUiccSlots[i2] = new UiccSlot(this.mContext, isActive);
                    }
                    this.mUiccSlots[i2].update(isActive ? this.mCis[iss.logicalSlotIndex] : null, iss);
                }
                if (numActiveSlots == this.mPhoneIdToSlotId.length) {
                    Set<Integer> slotIds = new HashSet<>();
                    int[] iArr = this.mPhoneIdToSlotId;
                    int length = iArr.length;
                    while (i < length) {
                        int slotId = iArr[i];
                        if (!slotIds.contains(Integer.valueOf(slotId))) {
                            slotIds.add(Integer.valueOf(slotId));
                            i++;
                        } else {
                            throw new RuntimeException("slotId " + slotId + " mapped to multiple phoneIds");
                        }
                    }
                    Intent intent = new Intent("android.telephony.action.SIM_SLOT_STATUS_CHANGED");
                    intent.addFlags(67108864);
                    intent.addFlags(16777216);
                    this.mContext.sendBroadcast(intent, "android.permission.READ_PRIVILEGED_PHONE_STATE");
                    return;
                }
                throw new RuntimeException("Number of active slots " + numActiveSlots + " does not match the expected value " + this.mPhoneIdToSlotId.length);
            }
        }
    }

    private boolean slotStatusChanged(ArrayList<IccSlotStatus> slotStatusList) {
        if (sLastSlotStatus == null || sLastSlotStatus.size() != slotStatusList.size()) {
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
            switch (resp.refreshResult) {
                case 0:
                    if (IccRecords.EFID_SET.contains(Integer.valueOf(resp.efId))) {
                        log("FIEL_UPDATE resetAppWithAid");
                        changed = uiccCard.resetAppWithAid(resp.aid, false);
                        break;
                    } else {
                        return;
                    }
                case 1:
                    changed = uiccCard.resetAppWithAid(resp.aid, false);
                    break;
                case 2:
                    changed = uiccCard.resetAppWithAid(resp.aid, true);
                    break;
                default:
                    return;
            }
            if (changed && resp.refreshResult == 2) {
                ((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).updateConfigForPhoneId(index.intValue(), "UNKNOWN");
                if (this.mContext.getResources().getBoolean(17957007)) {
                    this.mCis[index.intValue()].setRadioPower(false, null);
                }
            }
            this.mCis[index.intValue()].getIccCardStatus(obtainMessage(3, index));
        }
    }

    private boolean isValidPhoneIndex(int index) {
        return index >= 0 && index < TelephonyManager.getDefault().getPhoneCount();
    }

    private boolean isValidSlotIndex(int index) {
        return index >= 0 && index < this.mUiccSlots.length;
    }

    private void log(String string) {
        Rlog.d(LOG_TAG, string);
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
        pw.println(" mUiccSlots: size=" + this.mUiccSlots.length);
        for (int i2 = 0; i2 < this.mUiccSlots.length; i2++) {
            if (this.mUiccSlots[i2] == null) {
                pw.println("  mUiccSlots[" + i2 + "]=null");
            } else {
                pw.println("  mUiccSlots[" + i2 + "]=" + this.mUiccSlots[i2]);
                this.mUiccSlots[i2].dump(fd, pw, args);
            }
        }
        pw.println(" sLocalLog= ");
        sLocalLog.dump(fd, pw, args);
    }

    public UiccCard[] getUiccCards() {
        if (getUiccSlots() == null) {
            log("haven't get all UiccCards done, please wait!");
            return null;
        }
        UiccSlot[] us = getUiccSlots();
        UiccCard[] uc = new UiccCard[us.length];
        for (int i = 0; i < us.length; i++) {
            if (us[i] != null) {
                uc[i] = us[i].getUiccCard();
            }
        }
        return uc;
    }

    public CommandsInterface[] getmCis() {
        return (CommandsInterface[]) this.mCis.clone();
    }

    public void disposeCard(int index) {
        synchronized (mLock) {
            UiccSlot uiccSlot = getUiccSlotForPhone(index);
            if (uiccSlot != null) {
                Rlog.d(LOG_TAG, "Disposing card " + index);
                uiccSlot.onRadioStateUnavailable();
                HwTelephonyFactory.getHwPhoneManager().saveUiccCardsToVirtualNet(getUiccCards());
            }
        }
    }

    public void onRefresh(int slotId, int[] fileList) {
        boolean fileChanged = fileList != null && fileList.length > 0;
        log("onRefresh: fileChanged = " + fileChanged + "  slotId = " + slotId);
        if (fileChanged && isValidPhoneIndex(slotId) && getUiccCardForPhone(slotId) != null && getUiccCardForPhone(slotId).getApplicationIndex(slotId) != null) {
            IccRecords iccRecords = getUiccCardForPhone(slotId).getApplicationIndex(slotId).getIccRecords();
            if (iccRecords != null) {
                iccRecords.onRefresh(fileChanged, fileList);
            }
        }
    }
}
