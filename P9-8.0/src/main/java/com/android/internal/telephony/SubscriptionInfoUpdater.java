package com.android.internal.telephony;

import android.app.ActivityManager;
import android.app.UserSwitchObserver;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.os.AsyncResult;
import android.os.IRemoteCallback;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccException;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SubscriptionInfoUpdater extends AbstractSubscriptionInfoUpdater {
    public static final String CURR_SUBID = "curr_subid";
    private static final int EVENT_GET_NETWORK_SELECTION_MODE_DONE = 2;
    private static final int EVENT_SIM_ABSENT = 4;
    private static final int EVENT_SIM_IO_ERROR = 6;
    private static final int EVENT_SIM_LOADED = 3;
    private static final int EVENT_SIM_LOCKED = 5;
    private static final int EVENT_SIM_LOCKED_QUERY_ICCID_DONE = 1;
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
    private static Context mContext = null;
    private static String[] mIccId = new String[PROJECT_SIM_NUM];
    private static int[] mInsertSimState = new int[PROJECT_SIM_NUM];
    private static Phone[] mPhone;
    private CarrierServiceBindHelper mCarrierServiceBindHelper;
    private int mCurrentlyActiveUserId;
    private IPackageManager mPackageManager;
    private SubscriptionManager mSubscriptionManager = null;
    private UserManager mUserManager;
    private Map<Integer, Intent> rebroadcastIntentsOnUnlock = new HashMap();
    private final BroadcastReceiver sReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            SubscriptionInfoUpdater.this.logd("[Receiver]+");
            String action = intent.getAction();
            SubscriptionInfoUpdater.this.logd("Action: " + action);
            if (action.equals("android.intent.action.USER_UNLOCKED")) {
                Iterator iterator = SubscriptionInfoUpdater.this.rebroadcastIntentsOnUnlock.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry pair = (Entry) iterator.next();
                    Intent i = (Intent) pair.getValue();
                    i.putExtra("rebroadcastOnUnlock", true);
                    iterator.remove();
                    SubscriptionInfoUpdater.this.logd("Broadcasting intent ACTION_SIM_STATE_CHANGED for mCardIndex: " + pair.getKey());
                    ActivityManager.broadcastStickyIntent(i, -1);
                }
                SubscriptionInfoUpdater.this.logd("[Receiver]-");
            } else if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                int slotIndex = intent.getIntExtra("phone", -1);
                SubscriptionInfoUpdater.this.logd("slotIndex: " + slotIndex);
                if (SubscriptionManager.isValidSlotIndex(slotIndex)) {
                    String simStatus = intent.getStringExtra("ss");
                    SubscriptionInfoUpdater.this.logd("simStatus: " + simStatus);
                    if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                        if ("ABSENT".equals(simStatus)) {
                            SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(4, slotIndex, -1));
                        } else if ("UNKNOWN".equals(simStatus)) {
                            SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(7, slotIndex, -1));
                        } else if ("CARD_IO_ERROR".equals(simStatus)) {
                            SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(6, slotIndex, -1));
                        } else if ("CARD_RESTRICTED".equals(simStatus)) {
                            SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(8, slotIndex, -1));
                        } else if ("LOCKED".equals(simStatus)) {
                            SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(5, slotIndex, -1));
                        } else if ("LOADED".equals(simStatus)) {
                            SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(3, slotIndex, -1));
                        } else {
                            SubscriptionInfoUpdater.this.logd("Ignoring simStatus: " + simStatus);
                        }
                    }
                    SubscriptionInfoUpdater.this.logd("[Receiver]-");
                    return;
                }
                SubscriptionInfoUpdater.this.logd("ACTION_SIM_STATE_CHANGED contains invalid slotIndex: " + slotIndex);
            }
        }
    };

    private static class QueryIccIdUserObj {
        public int slotId;

        QueryIccIdUserObj(int slotId) {
            this.slotId = slotId;
        }
    }

    public SubscriptionInfoUpdater(Context context, Phone[] phone, CommandsInterface[] ci) {
        logd("Constructor invoked");
        mContext = context;
        mPhone = phone;
        this.mSubscriptionManager = SubscriptionManager.from(mContext);
        this.mPackageManager = Stub.asInterface(ServiceManager.getService("package"));
        subscriptionInfoInit(this, context, ci);
        this.mUserManager = (UserManager) mContext.getSystemService("user");
        IntentFilter intentFilter = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        mContext.registerReceiver(this.sReceiver, intentFilter);
        this.mCarrierServiceBindHelper = new CarrierServiceBindHelper(mContext);
        initializeCarrierApps();
    }

    private void initializeCarrierApps() {
        this.mCurrentlyActiveUserId = 0;
        try {
            ActivityManager.getService().registerUserSwitchObserver(new UserSwitchObserver() {
                public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
                    SubscriptionInfoUpdater.this.mCurrentlyActiveUserId = newUserId;
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

    private boolean isAllIccIdQueryDone() {
        int i = 0;
        while (i < PROJECT_SIM_NUM) {
            if (VSimUtilsInner.isPlatformTwoModems() && !VSimUtilsInner.isRadioAvailable(i)) {
                logd("[2Cards]Ignore pending sub" + i);
                mIccId[i] = ICCID_STRING_FOR_NO_SIM;
            }
            if (mIccId[i] == null) {
                logd("Wait for SIM" + (i + 1) + " IccId");
                return false;
            }
            i++;
        }
        logd("All IccIds query complete");
        return true;
    }

    public void setDisplayNameForNewSub(String newSubName, int subId, int newNameSource) {
        SubscriptionInfo subInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(subId);
        if (subInfo != null) {
            int oldNameSource = subInfo.getNameSource();
            CharSequence oldSubName = subInfo.getDisplayName();
            logd("[setDisplayNameForNewSub] subId = " + subInfo.getSubscriptionId() + ", oldSimName = " + oldSubName + ", oldNameSource = " + oldNameSource + ", newSubName = " + newSubName + ", newNameSource = " + newNameSource);
            if (oldSubName == null || ((oldNameSource == 0 && newSubName != null) || !(oldNameSource != 1 || newSubName == null || (newSubName.equals(oldSubName) ^ 1) == 0))) {
                this.mSubscriptionManager.setDisplayName(newSubName, subInfo.getSubscriptionId(), (long) newNameSource);
                return;
            }
            return;
        }
        logd("SUB" + (subId + 1) + " SubInfo not created yet");
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case 1:
                ar = msg.obj;
                int slotId = ar.userObj.slotId;
                logd("handleMessage : <EVENT_SIM_LOCKED_QUERY_ICCID_DONE> SIM" + (slotId + 1));
                if (ar.exception == null) {
                    if (ar.result != null) {
                        byte[] data = ar.result;
                        mIccId[slotId] = HwTelephonyFactory.getHwUiccManager().bcdIccidToString(data, 0, data.length);
                        if (mIccId[slotId] != null && mIccId[slotId].trim().length() == 0) {
                            mIccId[slotId] = "emptyiccid" + slotId;
                        }
                    } else {
                        logd("Null ar");
                        mIccId[slotId] = ICCID_STRING_FOR_NO_SIM;
                    }
                } else if (((ar.exception instanceof CommandException) && (((CommandException) ar.exception).getCommandError() == Error.RADIO_NOT_AVAILABLE || ((CommandException) ar.exception).getCommandError() == Error.GENERIC_FAILURE)) || (ar.exception instanceof IccException)) {
                    logd("Do Nothing.");
                } else {
                    mIccId[slotId] = ICCID_STRING_FOR_NO_SIM;
                    logd("Query IccId fail: " + ar.exception);
                }
                logd("sIccId[" + slotId + "] = " + SubscriptionInfo.givePrintableIccid(mIccId[slotId]));
                if (isAllIccIdQueryDone() && mNeedUpdate) {
                    updateSubscriptionInfoByIccId();
                }
                if (!ICCID_STRING_FOR_NO_SIM.equals(mIccId[slotId])) {
                    updateCarrierServices(slotId, "LOCKED");
                    return;
                }
                return;
            case 2:
                ar = (AsyncResult) msg.obj;
                Integer slotId2 = ar.userObj;
                if (ar.exception != null || ar.result == null) {
                    logd("EVENT_GET_NETWORK_SELECTION_MODE_DONE: error getting network mode.");
                    return;
                } else if (ar.result[0] == 1) {
                    mPhone[slotId2.intValue()].setNetworkSelectionModeAutomatic(null);
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
                handleSimLocked(msg.arg1);
                return;
            case 6:
                handleSimError(msg.arg1);
                return;
            case 7:
                updateCarrierServices(msg.arg1, "UNKNOWN");
                return;
            case 8:
                updateCarrierServices(msg.arg1, "CARD_RESTRICTED");
                return;
            default:
                logd("Unknown msg:" + msg.what);
                handleMessageExtend(msg);
                return;
        }
    }

    private void handleSimLocked(int slotId) {
        IccFileHandler fileHandler;
        if (mIccId[slotId] != null && mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM)) {
            logd("SIM" + (slotId + 1) + " hot plug in");
            mIccId[slotId] = null;
        }
        if (mPhone[slotId].getIccCard() == null) {
            fileHandler = null;
        } else {
            fileHandler = mPhone[slotId].getIccCard().getIccFileHandler();
        }
        if (fileHandler != null) {
            String iccId = mIccId[slotId];
            if (iccId == null) {
                logd("Querying IccId");
                fileHandler.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(1, new QueryIccIdUserObj(slotId)));
                return;
            }
            logd("NOT Querying IccId its already set sIccid[" + slotId + "]=" + SubscriptionInfo.givePrintableIccid(iccId));
            updateCarrierServices(slotId, "LOCKED");
            return;
        }
        logd("sFh[" + slotId + "] is null, ignore");
    }

    private void handleSimLoaded(int slotId) {
        logd("handleSimStateLoadedInternal: slotId: " + slotId);
        IccRecords records = mPhone[slotId].getIccCard().getIccRecords();
        if (records == null) {
            logd("onRecieve: IccRecords null");
        } else if (records.getIccId() == null) {
            logd("onRecieve: IccID null");
        } else {
            if (VSimUtilsInner.needBlockUnReservedForVsim(slotId)) {
                logd("handleSimStateLoadedInternal: block Unreserved subId, don't set mIccId[" + slotId + "] from records");
            } else {
                mIccId[slotId] = records.getIccId().trim().length() > 0 ? records.getIccId() : "emptyiccid" + slotId;
            }
            int subId = Integer.MAX_VALUE;
            int[] subIds = SubscriptionController.getInstance().getSubId(slotId);
            if (subIds != null) {
                subId = subIds[0];
            }
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                TelephonyManager tm = TelephonyManager.getDefault();
                String operator = tm.getSimOperatorNumeric(subId);
                slotId = SubscriptionController.getInstance().getPhoneId(subId);
                if (TextUtils.isEmpty(operator)) {
                    logd("EVENT_RECORDS_LOADED Operator name is null");
                } else {
                    if (subId == SubscriptionController.getInstance().getDefaultSubId()) {
                        MccTable.updateMccMncConfiguration(mContext, operator, false);
                    }
                    SubscriptionController.getInstance().setMccMnc(operator, subId);
                }
                String msisdn = tm.getLine1Number(subId);
                ContentResolver contentResolver = mContext.getContentResolver();
                if (msisdn != null) {
                    ContentValues number = new ContentValues(1);
                    number.put("number", msisdn);
                    contentResolver.update(SubscriptionManager.CONTENT_URI, number, "sim_id=" + Long.toString((long) subId), null);
                }
                SubscriptionInfo subInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(subId);
                String simCarrierName = tm.getSimOperatorName(subId);
                ContentValues name = new ContentValues(1);
                if (!(subInfo == null || subInfo.getNameSource() == 2)) {
                    String nameToSet;
                    if (!TextUtils.isEmpty(simCarrierName)) {
                        nameToSet = simCarrierName;
                    } else if (tm.isMultiSimEnabled()) {
                        nameToSet = "CARD " + Integer.toString(slotId + 1);
                    } else {
                        nameToSet = "CARD";
                    }
                    name.put("display_name", nameToSet);
                    logd("sim name = " + nameToSet);
                    contentResolver.update(SubscriptionManager.CONTENT_URI, name, "sim_id=" + Long.toString((long) subId), null);
                }
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                if (sp.getInt(CURR_SUBID + slotId, -1) != subId) {
                    mPhone[slotId].getNetworkSelectionMode(obtainMessage(2, new Integer(slotId)));
                    Editor editor = sp.edit();
                    editor.putInt(CURR_SUBID + slotId, subId);
                    editor.apply();
                }
            } else {
                logd("Invalid subId, could not update ContentResolver");
            }
            CarrierAppUtils.disableCarrierAppsUntilPrivileged(mContext.getOpPackageName(), this.mPackageManager, TelephonyManager.getDefault(), mContext.getContentResolver(), this.mCurrentlyActiveUserId);
            updateCarrierServices(slotId, "LOADED");
        }
    }

    private void updateCarrierServices(int slotId, String simState) {
        ((CarrierConfigManager) mContext.getSystemService("carrier_config")).updateConfigForPhoneId(slotId, simState);
        this.mCarrierServiceBindHelper.updateForPhoneId(slotId, simState);
    }

    private void handleSimAbsent(int slotId) {
        if (!(mIccId[slotId] == null || (mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM) ^ 1) == 0)) {
            logd("SIM" + (slotId + 1) + " hot plug out");
        }
        updateCarrierServices(slotId, "ABSENT");
    }

    private void handleSimError(int slotId) {
        if (!(mIccId[slotId] == null || (mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM) ^ 1) == 0)) {
            logd("SIM" + (slotId + 1) + " Error ");
        }
        mIccId[slotId] = ICCID_STRING_FOR_NO_SIM;
        if (isAllIccIdQueryDone()) {
            updateSubscriptionInfoByIccId();
        }
        updateCarrierServices(slotId, "CARD_IO_ERROR");
    }

    private synchronized void updateSubscriptionInfoByIccId() {
        int i;
        ContentValues contentValues;
        logd("updateSubscriptionInfoByIccId:+ Start");
        mNeedUpdate = false;
        for (i = 0; i < PROJECT_SIM_NUM; i++) {
            mInsertSimState[i] = 0;
        }
        int insertedSimCount = PROJECT_SIM_NUM;
        for (i = 0; i < PROJECT_SIM_NUM; i++) {
            if (ICCID_STRING_FOR_NO_SIM.equals(mIccId[i])) {
                insertedSimCount--;
                mInsertSimState[i] = -99;
            }
        }
        logd("insertedSimCount = " + insertedSimCount);
        i = 0;
        while (i < PROJECT_SIM_NUM) {
            if (mInsertSimState[i] != -99) {
                int index = 2;
                int j = i + 1;
                while (j < PROJECT_SIM_NUM) {
                    if (mInsertSimState[j] == 0 && mIccId[i].equals(mIccId[j])) {
                        mInsertSimState[i] = 1;
                        mInsertSimState[j] = index;
                        index++;
                    }
                    j++;
                }
            }
            i++;
        }
        ContentResolver contentResolver = mContext.getContentResolver();
        String[] oldIccId = new String[PROJECT_SIM_NUM];
        i = 0;
        while (i < PROJECT_SIM_NUM) {
            oldIccId[i] = null;
            List<SubscriptionInfo> oldSubInfo = SubscriptionController.getInstance().getSubInfoUsingSlotIndexWithCheck(i, false, mContext.getOpPackageName());
            if (oldSubInfo == null || oldSubInfo.size() <= 0) {
                if (mInsertSimState[i] == 0) {
                    mInsertSimState[i] = -1;
                }
                oldIccId[i] = ICCID_STRING_FOR_NO_SIM;
                logd("updateSubscriptionInfoByIccId: No SIM in slot " + i + " last time");
            } else {
                oldIccId[i] = ((SubscriptionInfo) oldSubInfo.get(0)).getIccId();
                logd("updateSubscriptionInfoByIccId: oldSubId = " + ((SubscriptionInfo) oldSubInfo.get(0)).getSubscriptionId());
                if (mInsertSimState[i] == 0 && (mIccId[i].equals(oldIccId[i]) ^ 1) != 0) {
                    mInsertSimState[i] = -1;
                }
                if (mInsertSimState[i] != 0) {
                    contentValues = new ContentValues(1);
                    contentValues.put("sim_id", Integer.valueOf(-1));
                    contentResolver.update(SubscriptionManager.CONTENT_URI, contentValues, "sim_id=" + Integer.toString(((SubscriptionInfo) oldSubInfo.get(0)).getSubscriptionId()), null);
                }
            }
            i++;
        }
        for (i = 0; i < PROJECT_SIM_NUM; i++) {
            logd("updateSubscriptionInfoByIccId: oldIccId[" + i + "] = " + SubscriptionInfo.givePrintableIccid(oldIccId[i]) + ", sIccId[" + i + "] = " + SubscriptionInfo.givePrintableIccid(mIccId[i]));
        }
        int nNewCardCount = 0;
        int nNewSimStatus = 0;
        i = 0;
        while (i < PROJECT_SIM_NUM) {
            if (mInsertSimState[i] == -99) {
                logd("updateSubscriptionInfoByIccId: No SIM inserted in slot " + i + " this time");
                if (PROJECT_SIM_NUM == 1) {
                    HwTelephonyFactory.getHwUiccManager().updateUserPreferences(false);
                }
            } else {
                if (mInsertSimState[i] > 0) {
                    this.mSubscriptionManager.addSubscriptionInfoRecord(mIccId[i] + Integer.toString(mInsertSimState[i]), i);
                    logd("SUB" + (i + 1) + " has invalid IccId");
                } else {
                    this.mSubscriptionManager.addSubscriptionInfoRecord(mIccId[i], i);
                }
                if (mInsertSimState[i] == i + 1 && oldIccId[i] != null && oldIccId[i].equals(mIccId[i] + Integer.toString(mInsertSimState[i]))) {
                    logd("same iccid not change index = " + i);
                    mInsertSimState[i] = 0;
                } else {
                    if (isNewSim(mIccId[i], oldIccId)) {
                        nNewCardCount++;
                        switch (i) {
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
                        mInsertSimState[i] = -2;
                    }
                }
            }
            i++;
        }
        for (i = 0; i < PROJECT_SIM_NUM; i++) {
            if (mInsertSimState[i] == -1) {
                mInsertSimState[i] = -3;
            }
            logd("updateSubscriptionInfoByIccId: sInsertSimState[" + i + "] = " + mInsertSimState[i]);
        }
        if (PROJECT_SIM_NUM > 1) {
            updateSubActivation(mInsertSimState, false);
        }
        List<SubscriptionInfo> subInfos = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        int nSubCount = subInfos == null ? 0 : subInfos.size();
        logd("updateSubscriptionInfoByIccId: nSubCount = " + nSubCount);
        for (i = 0; i < nSubCount; i++) {
            SubscriptionInfo temp = (SubscriptionInfo) subInfos.get(i);
            String msisdn = TelephonyManager.getDefault().getLine1Number(temp.getSubscriptionId());
            if (msisdn != null) {
                contentValues = new ContentValues(1);
                contentValues.put("number", msisdn);
                contentResolver.update(SubscriptionManager.CONTENT_URI, contentValues, "sim_id=" + Integer.toString(temp.getSubscriptionId()), null);
            }
        }
        broadcastSubinfoRecordUpdated(mIccId, oldIccId, nNewCardCount, nSubCount, nNewSimStatus);
        SubscriptionController.getInstance().notifySubscriptionInfoChanged();
        logd("updateSubscriptionInfoByIccId:- SsubscriptionInfo update complete");
    }

    private boolean isNewSim(String iccId, String[] oldIccId) {
        boolean newSim = true;
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            if (iccId.equals(oldIccId[i])) {
                newSim = false;
                break;
            }
        }
        logd("newSim = " + newSim);
        return newSim;
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

    public void dispose() {
        logd("[dispose]");
        mContext.unregisterReceiver(this.sReceiver);
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
