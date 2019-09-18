package com.android.internal.telephony;

import android.app.ActivityManager;
import android.app.UserSwitchObserver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.IPackageManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.PreferenceManager;
import android.service.euicc.EuiccProfileInfo;
import android.service.euicc.GetEuiccProfileInfoListResult;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UiccAccessRule;
import android.telephony.euicc.EuiccManager;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.euicc.EuiccController;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionInfoUpdater extends AbstractSubscriptionInfoUpdater {
    public static final String CURR_SUBID = "curr_subid";
    private static final int EVENT_GET_NETWORK_SELECTION_MODE_DONE = 2;
    private static final int EVENT_INVALID = -1;
    private static final int EVENT_REFRESH_EMBEDDED_SUBSCRIPTIONS = 12;
    private static final int EVENT_SIM_ABSENT = 4;
    private static final int EVENT_SIM_IMSI = 11;
    private static final int EVENT_SIM_IO_ERROR = 6;
    private static final int EVENT_SIM_LOADED = 3;
    private static final int EVENT_SIM_LOCKED = 5;
    private static final int EVENT_SIM_NOT_READY = 9;
    private static final int EVENT_SIM_READY = 10;
    private static final int EVENT_SIM_RESTRICTED = 8;
    private static final int EVENT_SIM_UNKNOWN = 7;
    private static final String ICCID_STRING_FOR_NO_SIM = "";
    private static final String LOG_TAG = "SubscriptionInfoUpdater";
    private static final int PROJECT_SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    public static final int SIM_CHANGED = -1;
    public static final int SIM_NEW = -2;
    public static final int SIM_NOT_CHANGE = 0;
    public static final int SIM_NOT_INSERT = -99;
    public static final int SIM_REPOSITION = -3;
    public static final int STATUS_NO_SIM_INSERTED = 0;
    public static final int STATUS_SIM1_INSERTED = 1;
    public static final int STATUS_SIM2_INSERTED = 2;
    public static final int STATUS_SIM3_INSERTED = 4;
    public static final int STATUS_SIM4_INSERTED = 8;
    /* access modifiers changed from: private */
    public static Context mContext = null;
    private static String[] mIccId = new String[PROJECT_SIM_NUM];
    private static int[] mInsertSimState = new int[PROJECT_SIM_NUM];
    private static Phone[] mPhone;
    private static int[] sSimApplicationState = new int[PROJECT_SIM_NUM];
    private static int[] sSimCardState = new int[PROJECT_SIM_NUM];
    private CarrierServiceBindHelper mCarrierServiceBindHelper;
    /* access modifiers changed from: private */
    public int mCurrentlyActiveUserId;
    private EuiccManager mEuiccManager;
    /* access modifiers changed from: private */
    public IPackageManager mPackageManager;
    private SubscriptionManager mSubscriptionManager = null;

    public SubscriptionInfoUpdater(Looper looper, Context context, Phone[] phone, CommandsInterface[] ci) {
        super(looper);
        logd("Constructor invoked");
        mContext = context;
        mPhone = phone;
        this.mSubscriptionManager = SubscriptionManager.from(mContext);
        this.mEuiccManager = (EuiccManager) mContext.getSystemService("euicc");
        this.mPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        subscriptionInfoInit(this, context, ci);
        new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
        this.mCarrierServiceBindHelper = new CarrierServiceBindHelper(mContext);
        initializeCarrierApps();
    }

    private void initializeCarrierApps() {
        this.mCurrentlyActiveUserId = 0;
        try {
            ActivityManager.getService().registerUserSwitchObserver(new UserSwitchObserver() {
                public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
                    int unused = SubscriptionInfoUpdater.this.mCurrentlyActiveUserId = newUserId;
                    CarrierAppUtils.disableCarrierAppsUntilPrivileged(SubscriptionInfoUpdater.mContext.getOpPackageName(), SubscriptionInfoUpdater.this.mPackageManager, TelephonyManager.getDefault(), SubscriptionInfoUpdater.mContext.getContentResolver(), SubscriptionInfoUpdater.this.mCurrentlyActiveUserId);
                    if (reply != null) {
                        try {
                            reply.sendResult(null);
                        } catch (RemoteException e) {
                        }
                    }
                }
            }, LOG_TAG);
            this.mCurrentlyActiveUserId = ActivityManager.getService().getCurrentUser().id;
        } catch (RemoteException e) {
            logd("Couldn't get current user ID; guessing it's 0: " + e.getMessage());
        }
        CarrierAppUtils.disableCarrierAppsUntilPrivileged(mContext.getOpPackageName(), this.mPackageManager, TelephonyManager.getDefault(), mContext.getContentResolver(), this.mCurrentlyActiveUserId);
    }

    public void updateInternalIccState(String simStatus, String reason, int slotId) {
        logd("updateInternalIccState to simStatus " + simStatus + " reason " + reason + " slotId " + slotId);
        if (!SubscriptionManager.isValidSlotIndex(slotId)) {
            logd("updateInternalIccState contains invalid slotIndex: " + slotId);
            return;
        }
        int message = internalIccStateToMessage(simStatus);
        if (message != -1) {
            sendMessage(obtainMessage(message, slotId, -1, reason));
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private int internalIccStateToMessage(String simStatus) {
        char c;
        switch (simStatus.hashCode()) {
            case -2044189691:
                if (simStatus.equals("LOADED")) {
                    c = 6;
                    break;
                }
            case -2044123382:
                if (simStatus.equals("LOCKED")) {
                    c = 5;
                    break;
                }
            case -1830845986:
                if (simStatus.equals("CARD_IO_ERROR")) {
                    c = 2;
                    break;
                }
            case 2251386:
                if (simStatus.equals("IMSI")) {
                    c = 8;
                    break;
                }
            case 77848963:
                if (simStatus.equals("READY")) {
                    c = 7;
                    break;
                }
            case 433141802:
                if (simStatus.equals("UNKNOWN")) {
                    c = 1;
                    break;
                }
            case 1034051831:
                if (simStatus.equals("NOT_READY")) {
                    c = 4;
                    break;
                }
            case 1599753450:
                if (simStatus.equals("CARD_RESTRICTED")) {
                    c = 3;
                    break;
                }
            case 1924388665:
                if (simStatus.equals("ABSENT")) {
                    c = 0;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 4;
            case 1:
                return 7;
            case 2:
                return 6;
            case 3:
                return 8;
            case 4:
                return 9;
            case 5:
                return 5;
            case 6:
                return 3;
            case 7:
                return 10;
            case 8:
                return 11;
            default:
                logd("Ignoring simStatus: " + simStatus);
                return -1;
        }
    }

    private boolean isAllIccIdQueryDone() {
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            if (VSimUtilsInner.isPlatformTwoModems() && !VSimUtilsInner.isRadioAvailable(i)) {
                logd("[2Cards]Ignore pending sub" + i);
                mIccId[i] = ICCID_STRING_FOR_NO_SIM;
            }
            if (mIccId[i] == null) {
                logd("Wait for SIM" + (i + 1) + " IccId");
                return false;
            }
        }
        logd("All IccIds query complete");
        return true;
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 2:
                AsyncResult ar = (AsyncResult) msg.obj;
                Integer slotId = (Integer) ar.userObj;
                if (ar.exception != null || ar.result == null) {
                    logd("EVENT_GET_NETWORK_SELECTION_MODE_DONE: error getting network mode.");
                    return;
                } else if (((int[]) ar.result)[0] == 1) {
                    mPhone[slotId.intValue()].setNetworkSelectionModeAutomatic(null);
                    return;
                } else {
                    return;
                }
            case 3:
                handleSimLoaded(msg.arg1);
                return;
            case 4:
                handleSimAbsent(msg.arg1);
                return;
            case 5:
                handleSimLocked(msg.arg1, (String) msg.obj);
                return;
            case 6:
                handleSimError(msg.arg1);
                return;
            case 7:
                updateCarrierServices(msg.arg1, "UNKNOWN");
                broadcastSimStateChanged(msg.arg1, "UNKNOWN", null);
                broadcastSimCardStateChanged(msg.arg1, 0);
                broadcastSimApplicationStateChanged(msg.arg1, 0);
                return;
            case 8:
                updateCarrierServices(msg.arg1, "CARD_RESTRICTED");
                broadcastSimStateChanged(msg.arg1, "CARD_RESTRICTED", "CARD_RESTRICTED");
                broadcastSimCardStateChanged(msg.arg1, 9);
                broadcastSimApplicationStateChanged(msg.arg1, 6);
                return;
            case 9:
                broadcastSimStateChanged(msg.arg1, "NOT_READY", null);
                broadcastSimCardStateChanged(msg.arg1, 11);
                broadcastSimApplicationStateChanged(msg.arg1, 6);
                break;
            case 10:
                broadcastSimStateChanged(msg.arg1, "READY", null);
                broadcastSimCardStateChanged(msg.arg1, 11);
                broadcastSimApplicationStateChanged(msg.arg1, 6);
                return;
            case 11:
                broadcastSimStateChanged(msg.arg1, "IMSI", null);
                return;
            case 12:
                break;
            default:
                logd("Unknown msg:" + msg.what);
                handleMessageExtend(msg);
                return;
        }
        if (updateEmbeddedSubscriptions()) {
            SubscriptionController.getInstance().notifySubscriptionInfoChanged();
        }
        if (msg.obj != null) {
            ((Runnable) msg.obj).run();
        }
    }

    /* access modifiers changed from: package-private */
    public void requestEmbeddedSubscriptionInfoListRefresh(Runnable callback) {
        sendMessage(obtainMessage(12, callback));
    }

    private void handleSimLocked(int slotId, String reason) {
        if (mIccId[slotId] != null && mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM)) {
            logd("SIM" + (slotId + 1) + " hot plug in");
            mIccId[slotId] = null;
        }
        String iccId = mIccId[slotId];
        if (iccId == null) {
            IccCard iccCard = mPhone[slotId].getIccCard();
            if (iccCard == null) {
                logd("handleSimLocked: IccCard null");
                return;
            }
            IccRecords records = iccCard.getIccRecords();
            if (records == null) {
                logd("handleSimLocked: IccRecords null");
                return;
            } else if (IccUtils.stripTrailingFs(records.getFullIccId()) == null) {
                logd("handleSimLocked: IccID null");
                return;
            } else {
                mIccId[slotId] = IccUtils.stripTrailingFs(records.getFullIccId());
            }
        } else {
            logd("NOT Querying IccId its already set sIccid[" + slotId + "]=" + SubscriptionInfo.givePrintableIccid(iccId));
        }
        updateCarrierServices(slotId, "LOCKED");
        broadcastSimStateChanged(slotId, "LOCKED", reason);
        broadcastSimCardStateChanged(slotId, 11);
        broadcastSimApplicationStateChanged(slotId, getSimStateFromLockedReason(reason));
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0048  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x005f  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0061  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0063 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0064 A[RETURN] */
    private static int getSimStateFromLockedReason(String lockedReason) {
        char c;
        int hashCode = lockedReason.hashCode();
        if (hashCode == -1733499378) {
            if (lockedReason.equals("NETWORK")) {
                c = 2;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
            }
        } else if (hashCode == 79221) {
            if (lockedReason.equals("PIN")) {
                c = 0;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
            }
        } else if (hashCode == 79590) {
            if (lockedReason.equals("PUK")) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
            }
        } else if (hashCode == 190660331 && lockedReason.equals("PERM_DISABLED")) {
            c = 3;
            switch (c) {
                case 0:
                    return 2;
                case 1:
                    return 3;
                case 2:
                    return 4;
                case 3:
                    return 7;
                default:
                    Rlog.e(LOG_TAG, "Unexpected SIM locked reason " + lockedReason);
                    return 0;
            }
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
        }
    }

    private void handleSimLoaded(int slotId) {
        IccRecords records;
        IccCard iccCard;
        int[] subIds;
        String nameToSet;
        String str;
        int i = slotId;
        logd("handleSimLoaded: slotId: " + i);
        int loadedSlotId = i;
        IccCard iccCard2 = mPhone[i].getIccCard();
        if (iccCard2 == null) {
            logd("handleSimLoaded: IccCard null");
            return;
        }
        IccRecords records2 = iccCard2.getIccRecords();
        if (records2 == null) {
            logd("handleSimLoaded: IccRecords null");
        } else if (IccUtils.stripTrailingFs(records2.getFullIccId()) == null) {
            logd("handleSimLoaded: IccID null");
        } else {
            mIccId[i] = IccUtils.stripTrailingFs(records2.getFullIccId());
            if (VSimUtilsInner.needBlockUnReservedForVsim(slotId)) {
                logd("handleSimStateLoadedInternal: block Unreserved subId, don't set mIccId[" + i + "] from records");
            } else {
                String[] strArr = mIccId;
                if (records2.getIccId().trim().length() > 0) {
                    str = records2.getIccId();
                } else {
                    str = "emptyiccid" + i;
                }
                strArr[i] = str;
            }
            int[] subIds2 = this.mSubscriptionManager.getActiveSubscriptionIdList();
            int length = subIds2.length;
            boolean z = false;
            int i2 = i;
            int i3 = 0;
            while (i3 < length) {
                int subId = subIds2[i3];
                TelephonyManager tm = TelephonyManager.getDefault();
                String operator = tm.getSimOperatorNumeric(subId);
                int slotId2 = SubscriptionController.getInstance().getPhoneId(subId);
                if (!TextUtils.isEmpty(operator)) {
                    if (subId == SubscriptionController.getInstance().getDefaultSubId()) {
                        MccTable.updateMccMncConfiguration(mContext, operator, z);
                    }
                    SubscriptionController.getInstance().setMccMnc(operator, subId);
                } else {
                    logd("EVENT_RECORDS_LOADED Operator name is null");
                }
                String msisdn = tm.getLine1Number(subId);
                ContentResolver contentResolver = mContext.getContentResolver();
                if (msisdn != null) {
                    SubscriptionController.getInstance().setDisplayNumber(msisdn, subId);
                }
                SubscriptionInfo subInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(subId);
                String simCarrierName = tm.getSimOperatorName(subId);
                if (subInfo != null) {
                    iccCard = iccCard2;
                    if (subInfo.getNameSource() != 2) {
                        if (!TextUtils.isEmpty(simCarrierName)) {
                            nameToSet = simCarrierName;
                        } else if (tm.isMultiSimEnabled()) {
                            nameToSet = "CARD " + Integer.toString(slotId2 + 1);
                        } else {
                            nameToSet = "CARD";
                        }
                        StringBuilder sb = new StringBuilder();
                        records = records2;
                        sb.append("sim name = ");
                        sb.append(nameToSet);
                        logd(sb.toString());
                        SubscriptionController.getInstance().setDisplayName(nameToSet, subId);
                    } else {
                        records = records2;
                    }
                } else {
                    iccCard = iccCard2;
                    records = records2;
                }
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                int storedSubId = sp.getInt(CURR_SUBID + slotId2, -1);
                if (storedSubId != subId) {
                    int i4 = storedSubId;
                    subIds = subIds2;
                    mPhone[slotId2].getNetworkSelectionMode(obtainMessage(2, new Integer(slotId2)));
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt(CURR_SUBID + slotId2, subId);
                    editor.apply();
                } else {
                    subIds = subIds2;
                }
                i3++;
                iccCard2 = iccCard;
                records2 = records;
                subIds2 = subIds;
                z = false;
            }
            IccRecords iccRecords = records2;
            int[] iArr = subIds2;
            CarrierAppUtils.disableCarrierAppsUntilPrivileged(mContext.getOpPackageName(), this.mPackageManager, TelephonyManager.getDefault(), mContext.getContentResolver(), this.mCurrentlyActiveUserId);
            broadcastSimStateChanged(loadedSlotId, "LOADED", null);
            broadcastSimCardStateChanged(loadedSlotId, 11);
            broadcastSimApplicationStateChanged(loadedSlotId, 10);
            updateCarrierServices(loadedSlotId, "LOADED");
        }
    }

    private void updateCarrierServices(int slotId, String simState) {
        ((CarrierConfigManager) mContext.getSystemService("carrier_config")).updateConfigForPhoneId(slotId, simState);
        this.mCarrierServiceBindHelper.updateForPhoneId(slotId, simState);
    }

    private void handleSimAbsent(int slotId) {
        if (mIccId[slotId] != null && !mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM)) {
            logd("SIM" + (slotId + 1) + " hot plug out");
        }
        updateCarrierServices(slotId, "ABSENT");
        broadcastSimStateChanged(slotId, "ABSENT", null);
        broadcastSimCardStateChanged(slotId, 1);
        broadcastSimApplicationStateChanged(slotId, 6);
    }

    private void handleSimError(int slotId) {
        if (mIccId[slotId] != null && !mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM)) {
            logd("SIM" + (slotId + 1) + " Error ");
        }
        updateCarrierServices(slotId, "CARD_IO_ERROR");
        broadcastSimStateChanged(slotId, "CARD_IO_ERROR", "CARD_IO_ERROR");
        broadcastSimCardStateChanged(slotId, 8);
        broadcastSimApplicationStateChanged(slotId, 6);
    }

    private synchronized void updateSubscriptionInfoByIccId() {
        int i;
        int i2;
        synchronized (this) {
            logd("updateSubscriptionInfoByIccId:+ Start");
            int i3 = 0;
            mNeedUpdate = false;
            for (int i4 = 0; i4 < PROJECT_SIM_NUM; i4++) {
                mInsertSimState[i4] = 0;
            }
            int insertedSimCount = PROJECT_SIM_NUM;
            int i5 = 0;
            while (true) {
                i = -99;
                if (i5 >= PROJECT_SIM_NUM) {
                    break;
                }
                if (ICCID_STRING_FOR_NO_SIM.equals(mIccId[i5])) {
                    insertedSimCount--;
                    mInsertSimState[i5] = -99;
                }
                i5++;
            }
            logd("insertedSimCount = " + insertedSimCount);
            if (SubscriptionController.getInstance().getActiveSubIdList().length > insertedSimCount) {
                SubscriptionController.getInstance().clearSubInfo();
            }
            int i6 = 0;
            while (true) {
                i2 = 1;
                if (i6 >= PROJECT_SIM_NUM) {
                    break;
                }
                if (mInsertSimState[i6] != -99) {
                    int index = 2;
                    for (int j = i6 + 1; j < PROJECT_SIM_NUM; j++) {
                        if (mInsertSimState[j] == 0 && mIccId[i6] != null && mIccId[i6].equals(mIccId[j])) {
                            mInsertSimState[i6] = 1;
                            mInsertSimState[j] = index;
                            index++;
                        }
                    }
                    int i7 = index;
                }
                i6++;
            }
            ContentResolver contentResolver = mContext.getContentResolver();
            String[] oldIccId = new String[PROJECT_SIM_NUM];
            String[] decIccId = new String[PROJECT_SIM_NUM];
            for (int i8 = 0; i8 < PROJECT_SIM_NUM; i8++) {
                oldIccId[i8] = null;
                List<SubscriptionInfo> oldSubInfo = SubscriptionController.getInstance().getSubInfoUsingSlotIndexPrivileged(i8, false);
                decIccId[i8] = IccUtils.getDecimalSubstring(mIccId[i8]);
                if (oldSubInfo == null || oldSubInfo.size() <= 0) {
                    if (mInsertSimState[i8] == 0) {
                        mInsertSimState[i8] = -1;
                    }
                    oldIccId[i8] = ICCID_STRING_FOR_NO_SIM;
                    logd("updateSubscriptionInfoByIccId: No SIM in slot " + i8 + " last time");
                } else {
                    oldIccId[i8] = oldSubInfo.get(0).getIccId();
                    logd("updateSubscriptionInfoByIccId: oldSubId = " + oldSubInfo.get(0).getSubscriptionId());
                    if (mInsertSimState[i8] == 0 && mIccId[i8] != null && !mIccId[i8].equals(oldIccId[i8]) && (decIccId[i8] == null || !decIccId[i8].equals(oldIccId[i8]))) {
                        mInsertSimState[i8] = -1;
                    }
                    if (mInsertSimState[i8] != 0) {
                        ContentValues value = new ContentValues(1);
                        value.put("sim_id", -1);
                        contentResolver.update(SubscriptionManager.CONTENT_URI, value, "sim_id=" + Integer.toString(oldSubInfo.get(0).getSubscriptionId()), null);
                        SubscriptionController.getInstance().refreshCachedActiveSubscriptionInfoList();
                    }
                }
            }
            for (int i9 = 0; i9 < PROJECT_SIM_NUM; i9++) {
                logd("updateSubscriptionInfoByIccId: oldIccId[" + i9 + "] = " + SubscriptionInfo.givePrintableIccid(oldIccId[i9]) + ", sIccId[" + i9 + "] = " + SubscriptionInfo.givePrintableIccid(mIccId[i9]));
            }
            int nNewCardCount = 0;
            int nNewSimStatus = 0;
            int i10 = 0;
            while (i10 < PROJECT_SIM_NUM) {
                if (mInsertSimState[i10] == i) {
                    logd("updateSubscriptionInfoByIccId: No SIM inserted in slot " + i10 + " this time");
                    if (PROJECT_SIM_NUM == 1) {
                        HwTelephonyFactory.getHwUiccManager().updateUserPreferences(false);
                    }
                } else {
                    if (mInsertSimState[i10] > 0) {
                        this.mSubscriptionManager.addSubscriptionInfoRecord(mIccId[i10] + Integer.toString(mInsertSimState[i10]), i10);
                        logd("SUB" + (i10 + 1) + " has invalid IccId");
                    } else {
                        logd("updateSubscriptionInfoByIccId: adding subscription info record: iccid: " + SubscriptionInfo.givePrintableIccid(mIccId[i10]) + "slot: " + i10);
                        this.mSubscriptionManager.addSubscriptionInfoRecord(mIccId[i10], i10);
                    }
                    if (mInsertSimState[i10] == i10 + 1 && oldIccId[i10] != null) {
                        if (oldIccId[i10].equals(mIccId[i10] + Integer.toString(mInsertSimState[i10]))) {
                            logd("same iccid not change index = " + i10);
                            mInsertSimState[i10] = 0;
                        }
                    }
                    if (isNewSim(mIccId[i10], decIccId[i10], oldIccId)) {
                        nNewCardCount++;
                        switch (i10) {
                            case 0:
                                nNewSimStatus |= 1;
                                break;
                            case 1:
                                nNewSimStatus |= 2;
                                break;
                            case 2:
                                nNewSimStatus |= 4;
                                break;
                        }
                        mInsertSimState[i10] = -2;
                    }
                }
                i10++;
                i = -99;
            }
            for (int i11 = 0; i11 < PROJECT_SIM_NUM; i11++) {
                if (mInsertSimState[i11] == -1) {
                    mInsertSimState[i11] = -3;
                }
                logd("updateSubscriptionInfoByIccId: sInsertSimState[" + i11 + "] = " + mInsertSimState[i11]);
            }
            if (PROJECT_SIM_NUM > 1) {
                updateSubActivation(mInsertSimState, false);
            }
            List<SubscriptionInfo> subInfos = this.mSubscriptionManager.getActiveSubscriptionInfoList();
            int nSubCount = subInfos == null ? 0 : subInfos.size();
            logd("updateSubscriptionInfoByIccId: nSubCount = " + nSubCount);
            while (i3 < nSubCount) {
                SubscriptionInfo temp = subInfos.get(i3);
                String msisdn = TelephonyManager.getDefault().getLine1Number(temp.getSubscriptionId());
                if (msisdn != null) {
                    ContentValues value2 = new ContentValues(i2);
                    value2.put("number", msisdn);
                    Uri uri = SubscriptionManager.CONTENT_URI;
                    StringBuilder sb = new StringBuilder();
                    String str = msisdn;
                    sb.append("sim_id=");
                    sb.append(Integer.toString(temp.getSubscriptionId()));
                    contentResolver.update(uri, value2, sb.toString(), null);
                    SubscriptionController.getInstance().refreshCachedActiveSubscriptionInfoList();
                }
                i3++;
                i2 = 1;
            }
            int i12 = nSubCount;
            broadcastSubinfoRecordUpdated(mIccId, oldIccId, nNewCardCount, nSubCount, nNewSimStatus);
            updateEmbeddedSubscriptions();
            SubscriptionController.getInstance().notifySubscriptionInfoChanged();
            logd("updateSubscriptionInfoByIccId:- SubscriptionInfo update complete");
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public boolean updateEmbeddedSubscriptions() {
        EuiccProfileInfo[] embeddedProfiles;
        byte[] bArr;
        EuiccProfileInfo[] embeddedProfiles2;
        if (!this.mEuiccManager.isEnabled()) {
            return false;
        }
        GetEuiccProfileInfoListResult result = EuiccController.get().blockingGetEuiccProfileInfoList();
        if (result == null) {
            return false;
        }
        if (result.getResult() == 0) {
            List<EuiccProfileInfo> list = result.getProfiles();
            if (list == null || list.size() == 0) {
                embeddedProfiles2 = new EuiccProfileInfo[0];
            } else {
                embeddedProfiles2 = (EuiccProfileInfo[]) list.toArray(new EuiccProfileInfo[list.size()]);
            }
            embeddedProfiles = embeddedProfiles2;
        } else {
            logd("updatedEmbeddedSubscriptions: error " + result.getResult() + " listing profiles");
            embeddedProfiles = new EuiccProfileInfo[0];
        }
        boolean isRemovable = result.getIsRemovable();
        String[] embeddedIccids = new String[embeddedProfiles.length];
        for (int i = 0; i < embeddedProfiles.length; i++) {
            embeddedIccids[i] = embeddedProfiles[i].getIccid();
        }
        List<SubscriptionInfo> existingSubscriptions = SubscriptionController.getInstance().getSubscriptionInfoListForEmbeddedSubscriptionUpdate(embeddedIccids, isRemovable);
        ContentResolver contentResolver = mContext.getContentResolver();
        int length = embeddedProfiles.length;
        boolean hasChanges = false;
        int i2 = 0;
        while (i2 < length) {
            EuiccProfileInfo embeddedProfile = embeddedProfiles[i2];
            int index = findSubscriptionInfoForIccid(existingSubscriptions, embeddedProfile.getIccid());
            if (index < 0) {
                SubscriptionController.getInstance().insertEmptySubInfoRecord(embeddedProfile.getIccid(), -1);
            } else {
                existingSubscriptions.remove(index);
            }
            ContentValues values = new ContentValues();
            values.put("is_embedded", 1);
            List<UiccAccessRule> ruleList = embeddedProfile.getUiccAccessRules();
            boolean isRuleListEmpty = false;
            if (ruleList == null || ruleList.size() == 0) {
                isRuleListEmpty = true;
            }
            if (isRuleListEmpty) {
                bArr = null;
            } else {
                bArr = UiccAccessRule.encodeRules((UiccAccessRule[]) ruleList.toArray(new UiccAccessRule[ruleList.size()]));
            }
            values.put("access_rules", bArr);
            values.put("is_removable", Boolean.valueOf(isRemovable));
            values.put("display_name", embeddedProfile.getNickname());
            values.put("name_source", 2);
            hasChanges = true;
            contentResolver.update(SubscriptionManager.CONTENT_URI, values, "icc_id=\"" + embeddedProfile.getIccid() + "\"", null);
            SubscriptionController.getInstance().refreshCachedActiveSubscriptionInfoList();
            i2++;
            result = result;
        }
        if (!existingSubscriptions.isEmpty()) {
            List<String> iccidsToRemove = new ArrayList<>();
            for (int i3 = 0; i3 < existingSubscriptions.size(); i3++) {
                if (existingSubscriptions.get(i3).isEmbedded()) {
                    iccidsToRemove.add("\"" + info.getIccId() + "\"");
                }
            }
            ContentValues values2 = new ContentValues();
            values2.put("is_embedded", 0);
            hasChanges = true;
            contentResolver.update(SubscriptionManager.CONTENT_URI, values2, "icc_id IN (" + TextUtils.join(",", iccidsToRemove) + ")", null);
            SubscriptionController.getInstance().refreshCachedActiveSubscriptionInfoList();
        }
        return hasChanges;
    }

    private static int findSubscriptionInfoForIccid(List<SubscriptionInfo> list, String iccid) {
        for (int i = 0; i < list.size(); i++) {
            if (TextUtils.equals(iccid, list.get(i).getIccId())) {
                return i;
            }
        }
        return -1;
    }

    private boolean isNewSim(String iccId, String decIccId, String[] oldIccId) {
        boolean newSim = true;
        int i = 0;
        while (true) {
            if (i < PROJECT_SIM_NUM) {
                if (iccId == null || !iccId.equals(oldIccId[i])) {
                    if (decIccId != null && decIccId.equals(oldIccId[i])) {
                        newSim = false;
                        break;
                    }
                    i++;
                } else {
                    newSim = false;
                    break;
                }
            } else {
                break;
            }
        }
        logd("newSim = " + newSim);
        return newSim;
    }

    private void broadcastSimStateChanged(int slotId, String state, String reason) {
    }

    public synchronized void resetInsertSimState() {
        logd("[resetInsertSimState]: reset the sInsertSimState to not change");
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            mInsertSimState[i] = 0;
        }
    }

    public void cleanIccids() {
        for (int i = 0; i < mIccId.length; i++) {
            logd("clean iccids i=" + i);
            mIccId[i] = null;
        }
    }

    private void broadcastSimCardStateChanged(int phoneId, int state) {
        if (state != sSimCardState[phoneId]) {
            sSimCardState[phoneId] = state;
            Intent i = new Intent("android.telephony.action.SIM_CARD_STATE_CHANGED");
            i.addFlags(67108864);
            i.addFlags(16777216);
            i.putExtra("android.telephony.extra.SIM_STATE", state);
            SubscriptionManager.putPhoneIdAndSubIdExtra(i, phoneId);
            logd("Broadcasting intent ACTION_SIM_CARD_STATE_CHANGED " + simStateString(state) + " for phone: " + phoneId);
            mContext.sendBroadcast(i, "android.permission.READ_PRIVILEGED_PHONE_STATE");
        }
    }

    private void broadcastSimApplicationStateChanged(int phoneId, int state) {
        if (state == sSimApplicationState[phoneId]) {
            return;
        }
        if (state != 6 || sSimApplicationState[phoneId] != 0) {
            sSimApplicationState[phoneId] = state;
            Intent i = new Intent("android.telephony.action.SIM_APPLICATION_STATE_CHANGED");
            i.addFlags(16777216);
            i.addFlags(67108864);
            i.putExtra("android.telephony.extra.SIM_STATE", state);
            SubscriptionManager.putPhoneIdAndSubIdExtra(i, phoneId);
            logd("Broadcasting intent ACTION_SIM_APPLICATION_STATE_CHANGED " + simStateString(state) + " for phone: " + phoneId);
            mContext.sendBroadcast(i, "android.permission.READ_PRIVILEGED_PHONE_STATE");
        }
    }

    private static String simStateString(int state) {
        switch (state) {
            case 0:
                return "UNKNOWN";
            case 1:
                return "ABSENT";
            case 2:
                return "PIN_REQUIRED";
            case 3:
                return "PUK_REQUIRED";
            case 4:
                return "NETWORK_LOCKED";
            case 5:
                return "READY";
            case 6:
                return "NOT_READY";
            case 7:
                return "PERM_DISABLED";
            case 8:
                return "CARD_IO_ERROR";
            case 9:
                return "CARD_RESTRICTED";
            case 10:
                return "LOADED";
            case 11:
                return "PRESENT";
            default:
                return "INVALID";
        }
    }

    private void logd(String message) {
        Rlog.d(LOG_TAG, message);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("SubscriptionInfoUpdater:");
        this.mCarrierServiceBindHelper.dump(fd, pw, args);
    }

    public String[] getIccIdHw() {
        return mIccId;
    }

    public int[] getInsertSimStateHw() {
        return mInsertSimState;
    }

    public boolean isAllIccIdQueryDoneHw() {
        return isAllIccIdQueryDone();
    }

    public void updateSubscriptionInfoByIccIdHw() {
        updateSubscriptionInfoByIccId();
    }
}
