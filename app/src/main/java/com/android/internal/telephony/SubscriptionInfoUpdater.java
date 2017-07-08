package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.app.IUserSwitchObserver;
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
import android.provider.Telephony.Carriers;
import android.provider.Telephony.Sms.Intents;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.uicc.IccConstants;
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
    private static final int EVENT_SIM_UNKNOWN = 7;
    private static final String ICCID_STRING_FOR_NO_SIM = "";
    private static final String LOG_TAG = "SubscriptionInfoUpdater";
    private static final int PROJECT_SIM_NUM = 0;
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
    private static Context mContext;
    private static String[] mIccId;
    private static int[] mInsertSimState;
    private static Phone[] mPhone;
    private CarrierServiceBindHelper mCarrierServiceBindHelper;
    private int mCurrentlyActiveUserId;
    private IPackageManager mPackageManager;
    private SubscriptionManager mSubscriptionManager;
    private UserManager mUserManager;
    private Map<Integer, Intent> rebroadcastIntentsOnUnlock;
    private final BroadcastReceiver sReceiver;

    private static class QueryIccIdUserObj {
        public int slotId;

        QueryIccIdUserObj(int slotId) {
            this.slotId = slotId;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.SubscriptionInfoUpdater.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.SubscriptionInfoUpdater.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionInfoUpdater.<clinit>():void");
    }

    public SubscriptionInfoUpdater(Context context, Phone[] phone, CommandsInterface[] ci) {
        this.mSubscriptionManager = null;
        this.rebroadcastIntentsOnUnlock = new HashMap();
        this.sReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                SubscriptionInfoUpdater.this.logd("[Receiver]+");
                String action = intent.getAction();
                SubscriptionInfoUpdater.this.logd("Action: " + action);
                if (action.equals("android.intent.action.USER_UNLOCKED")) {
                    Iterator iterator = SubscriptionInfoUpdater.this.rebroadcastIntentsOnUnlock.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Entry pair = (Entry) iterator.next();
                        Intent i = (Intent) pair.getValue();
                        iterator.remove();
                        SubscriptionInfoUpdater.this.logd("Broadcasting intent ACTION_SIM_STATE_CHANGED for mCardIndex: " + pair.getKey());
                        ActivityManagerNative.broadcastStickyIntent(i, "android.permission.READ_PHONE_STATE", SubscriptionInfoUpdater.SIM_CHANGED);
                    }
                    SubscriptionInfoUpdater.this.rebroadcastIntentsOnUnlock = null;
                    SubscriptionInfoUpdater.this.logd("[Receiver]-");
                } else if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                    int slotId = intent.getIntExtra("phone", SubscriptionInfoUpdater.SIM_CHANGED);
                    SubscriptionInfoUpdater.this.logd("slotId: " + slotId);
                    if (slotId != SubscriptionInfoUpdater.SIM_CHANGED) {
                        String simStatus = intent.getStringExtra("ss");
                        SubscriptionInfoUpdater.this.logd("simStatus: " + simStatus);
                        if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                            if ("ABSENT".equals(simStatus)) {
                                SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(SubscriptionInfoUpdater.STATUS_SIM3_INSERTED, slotId, SubscriptionInfoUpdater.SIM_CHANGED));
                            } else if ("UNKNOWN".equals(simStatus)) {
                                SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(SubscriptionInfoUpdater.EVENT_SIM_UNKNOWN, slotId, SubscriptionInfoUpdater.SIM_CHANGED));
                            } else if ("CARD_IO_ERROR".equals(simStatus)) {
                                SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(SubscriptionInfoUpdater.EVENT_SIM_IO_ERROR, slotId, SubscriptionInfoUpdater.SIM_CHANGED));
                            } else if ("LOCKED".equals(simStatus)) {
                                SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(SubscriptionInfoUpdater.EVENT_SIM_LOCKED, slotId, SubscriptionInfoUpdater.SIM_CHANGED));
                            } else if ("LOADED".equals(simStatus)) {
                                SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(SubscriptionInfoUpdater.EVENT_SIM_LOADED, slotId, SubscriptionInfoUpdater.SIM_CHANGED));
                            } else {
                                SubscriptionInfoUpdater.this.logd("Ignoring simStatus: " + simStatus);
                            }
                        }
                        SubscriptionInfoUpdater.this.logd("[Receiver]-");
                    }
                }
            }
        };
        logd("Constructor invoked");
        mContext = context;
        mPhone = phone;
        this.mSubscriptionManager = SubscriptionManager.from(mContext);
        this.mPackageManager = Stub.asInterface(ServiceManager.getService(Intents.EXTRA_PACKAGE_NAME));
        subscriptionInfoInit(this, context, ci);
        this.mUserManager = (UserManager) mContext.getSystemService(Carriers.USER);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        mContext.registerReceiver(this.sReceiver, intentFilter);
        this.mCarrierServiceBindHelper = new CarrierServiceBindHelper(mContext);
        initializeCarrierApps();
    }

    private void initializeCarrierApps() {
        this.mCurrentlyActiveUserId = STATUS_NO_SIM_INSERTED;
        try {
            ActivityManagerNative.getDefault().registerUserSwitchObserver(new IUserSwitchObserver.Stub() {
                public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
                    SubscriptionInfoUpdater.this.mCurrentlyActiveUserId = newUserId;
                    CarrierAppUtils.disableCarrierAppsUntilPrivileged(SubscriptionInfoUpdater.mContext.getOpPackageName(), SubscriptionInfoUpdater.this.mPackageManager, TelephonyManager.getDefault(), SubscriptionInfoUpdater.this.mCurrentlyActiveUserId);
                    if (reply != null) {
                        try {
                            reply.sendResult(null);
                        } catch (RemoteException e) {
                        }
                    }
                }

                public void onUserSwitchComplete(int newUserId) {
                }

                public void onForegroundProfileSwitch(int newProfileId) throws RemoteException {
                }
            });
            this.mCurrentlyActiveUserId = ActivityManagerNative.getDefault().getCurrentUser().id;
        } catch (RemoteException e) {
            logd("Couldn't get current user ID; guessing it's 0: " + e.getMessage());
        }
        CarrierAppUtils.disableCarrierAppsUntilPrivileged(mContext.getOpPackageName(), this.mPackageManager, TelephonyManager.getDefault(), this.mCurrentlyActiveUserId);
    }

    private boolean isAllIccIdQueryDone() {
        int i = STATUS_NO_SIM_INSERTED;
        while (i < PROJECT_SIM_NUM) {
            if (VSimUtilsInner.isPlatformTwoModems() && !VSimUtilsInner.isRadioAvailable(i)) {
                logd("[2Cards]Ignore pending sub" + i);
                mIccId[i] = ICCID_STRING_FOR_NO_SIM;
            }
            if (mIccId[i] == null) {
                logd("Wait for SIM" + (i + STATUS_SIM1_INSERTED) + " IccId");
                return false;
            }
            i += STATUS_SIM1_INSERTED;
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
            if (oldSubName != null && (oldNameSource != 0 || newSubName == null)) {
                if (oldNameSource == STATUS_SIM1_INSERTED && newSubName != null) {
                    if (newSubName.equals(oldSubName)) {
                        return;
                    }
                }
                return;
            }
            this.mSubscriptionManager.setDisplayName(newSubName, subInfo.getSubscriptionId(), (long) newNameSource);
            return;
        }
        logd("SUB" + (subId + STATUS_SIM1_INSERTED) + " SubInfo not created yet");
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case STATUS_SIM1_INSERTED /*1*/:
                ar = msg.obj;
                int slotId = ar.userObj.slotId;
                logd("handleMessage : <EVENT_SIM_LOCKED_QUERY_ICCID_DONE> SIM" + (slotId + STATUS_SIM1_INSERTED));
                if (ar.exception != null) {
                    mIccId[slotId] = ICCID_STRING_FOR_NO_SIM;
                    logd("Query IccId fail: " + ar.exception);
                } else if (ar.result != null) {
                    byte[] data = ar.result;
                    mIccId[slotId] = HwTelephonyFactory.getHwUiccManager().bcdIccidToString(data, STATUS_NO_SIM_INSERTED, data.length);
                    if (mIccId[slotId] != null && mIccId[slotId].trim().length() == 0) {
                        mIccId[slotId] = "emptyiccid" + slotId;
                    }
                } else {
                    logd("Null ar");
                    mIccId[slotId] = ICCID_STRING_FOR_NO_SIM;
                }
                logd("sIccId[" + slotId + "] = " + mIccId[slotId]);
                if (isAllIccIdQueryDone() && mNeedUpdate) {
                    updateSubscriptionInfoByIccId();
                }
                if (!ICCID_STRING_FOR_NO_SIM.equals(mIccId[slotId])) {
                    updateCarrierServices(slotId, "LOCKED");
                }
            case STATUS_SIM2_INSERTED /*2*/:
                ar = (AsyncResult) msg.obj;
                Integer slotId2 = ar.userObj;
                if (ar.exception != null || ar.result == null) {
                    logd("EVENT_GET_NETWORK_SELECTION_MODE_DONE: error getting network mode.");
                } else if (ar.result[STATUS_NO_SIM_INSERTED] == STATUS_SIM1_INSERTED) {
                    mPhone[slotId2.intValue()].setNetworkSelectionModeAutomatic(null);
                }
            case EVENT_SIM_LOADED /*3*/:
                handleSimLoaded(msg.arg1);
            case STATUS_SIM3_INSERTED /*4*/:
                handleSimAbsent(msg.arg1);
            case EVENT_SIM_LOCKED /*5*/:
                handleSimLocked(msg.arg1);
            case EVENT_SIM_IO_ERROR /*6*/:
                updateCarrierServices(msg.arg1, "CARD_IO_ERROR");
            case EVENT_SIM_UNKNOWN /*7*/:
                updateCarrierServices(msg.arg1, "UNKNOWN");
            default:
                logd("Unknown msg:" + msg.what);
                handleMessageExtend(msg);
        }
    }

    private void handleSimLocked(int slotId) {
        IccFileHandler fileHandler = null;
        if (mIccId[slotId] != null && mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM)) {
            logd("SIM" + (slotId + STATUS_SIM1_INSERTED) + " hot plug in");
            mIccId[slotId] = null;
        }
        if (mPhone[slotId].getIccCard() != null) {
            fileHandler = mPhone[slotId].getIccCard().getIccFileHandler();
        }
        if (fileHandler != null) {
            String iccId = mIccId[slotId];
            if (iccId == null) {
                logd("Querying IccId");
                fileHandler.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(STATUS_SIM1_INSERTED, new QueryIccIdUserObj(slotId)));
                return;
            }
            logd("NOT Querying IccId its already set sIccid[" + slotId + "]=" + iccId);
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
            String iccId;
            String[] strArr = mIccId;
            if (records.getIccId().trim().length() > 0) {
                iccId = records.getIccId();
            } else {
                iccId = "emptyiccid" + slotId;
            }
            strArr[slotId] = iccId;
            int subId = Integer.MAX_VALUE;
            int[] subIds = SubscriptionController.getInstance().getSubId(slotId);
            if (subIds != null) {
                subId = subIds[STATUS_NO_SIM_INSERTED];
            }
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                TelephonyManager tm = TelephonyManager.getDefault();
                String operator = tm.getSimOperatorNumericForPhone(slotId);
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
                    ContentValues number = new ContentValues(STATUS_SIM1_INSERTED);
                    number.put("number", msisdn);
                    contentResolver.update(SubscriptionManager.CONTENT_URI, number, "sim_id=" + Long.toString((long) subId), null);
                }
                SubscriptionInfo subInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(subId);
                String simCarrierName = tm.getSimOperatorName(subId);
                ContentValues name = new ContentValues(STATUS_SIM1_INSERTED);
                if (!(subInfo == null || subInfo.getNameSource() == STATUS_SIM2_INSERTED)) {
                    String nameToSet;
                    if (!TextUtils.isEmpty(simCarrierName)) {
                        nameToSet = simCarrierName;
                    } else if (tm.isMultiSimEnabled()) {
                        nameToSet = "CARD " + Integer.toString(slotId + STATUS_SIM1_INSERTED);
                    } else {
                        nameToSet = "CARD";
                    }
                    name.put("display_name", nameToSet);
                    logd("sim name = " + nameToSet);
                    contentResolver.update(SubscriptionManager.CONTENT_URI, name, "sim_id=" + Long.toString((long) subId), null);
                }
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                if (sp.getInt(CURR_SUBID + slotId, SIM_CHANGED) != subId) {
                    mPhone[slotId].getNetworkSelectionMode(obtainMessage(STATUS_SIM2_INSERTED, new Integer(slotId)));
                    Editor editor = sp.edit();
                    editor.putInt(CURR_SUBID + slotId, subId);
                    editor.apply();
                }
            } else {
                logd("Invalid subId, could not update ContentResolver");
            }
            CarrierAppUtils.disableCarrierAppsUntilPrivileged(mContext.getOpPackageName(), this.mPackageManager, TelephonyManager.getDefault(), this.mCurrentlyActiveUserId);
            updateCarrierServices(slotId, "LOADED");
        }
    }

    private void updateCarrierServices(int slotId, String simState) {
        ((CarrierConfigManager) mContext.getSystemService("carrier_config")).updateConfigForPhoneId(slotId, simState);
        this.mCarrierServiceBindHelper.updateForPhoneId(slotId, simState);
    }

    private void handleSimAbsent(int slotId) {
        if (!(mIccId[slotId] == null || mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM))) {
            logd("SIM" + (slotId + STATUS_SIM1_INSERTED) + " hot plug out");
        }
        updateCarrierServices(slotId, "ABSENT");
    }

    private synchronized void updateSubscriptionInfoByIccId() {
        int i;
        logd("updateSubscriptionInfoByIccId:+ Start");
        mNeedUpdate = false;
        for (i = STATUS_NO_SIM_INSERTED; i < PROJECT_SIM_NUM; i += STATUS_SIM1_INSERTED) {
            mInsertSimState[i] = STATUS_NO_SIM_INSERTED;
        }
        int insertedSimCount = PROJECT_SIM_NUM;
        for (i = STATUS_NO_SIM_INSERTED; i < PROJECT_SIM_NUM; i += STATUS_SIM1_INSERTED) {
            if (ICCID_STRING_FOR_NO_SIM.equals(mIccId[i])) {
                insertedSimCount += SIM_CHANGED;
                mInsertSimState[i] = SIM_NOT_INSERT;
            }
        }
        logd("insertedSimCount = " + insertedSimCount);
        i = STATUS_NO_SIM_INSERTED;
        while (i < PROJECT_SIM_NUM) {
            if (mInsertSimState[i] != SIM_NOT_INSERT) {
                int index = STATUS_SIM2_INSERTED;
                int j = i + STATUS_SIM1_INSERTED;
                while (j < PROJECT_SIM_NUM) {
                    if (mInsertSimState[j] == 0 && mIccId[i].equals(mIccId[j])) {
                        mInsertSimState[i] = STATUS_SIM1_INSERTED;
                        mInsertSimState[j] = index;
                        index += STATUS_SIM1_INSERTED;
                    }
                    j += STATUS_SIM1_INSERTED;
                }
            }
            i += STATUS_SIM1_INSERTED;
        }
        ContentResolver contentResolver = mContext.getContentResolver();
        String[] oldIccId = new String[PROJECT_SIM_NUM];
        i = STATUS_NO_SIM_INSERTED;
        while (i < PROJECT_SIM_NUM) {
            oldIccId[i] = null;
            List<SubscriptionInfo> oldSubInfo = SubscriptionController.getInstance().getSubInfoUsingSlotIdWithCheck(i, false, mContext.getOpPackageName());
            if (oldSubInfo != null) {
                oldIccId[i] = ((SubscriptionInfo) oldSubInfo.get(STATUS_NO_SIM_INSERTED)).getIccId();
                logd("updateSubscriptionInfoByIccId: oldSubId = " + ((SubscriptionInfo) oldSubInfo.get(STATUS_NO_SIM_INSERTED)).getSubscriptionId());
                if (mInsertSimState[i] == 0 && !mIccId[i].equals(oldIccId[i])) {
                    mInsertSimState[i] = SIM_CHANGED;
                }
                if (mInsertSimState[i] != 0) {
                    ContentValues contentValues = new ContentValues(STATUS_SIM1_INSERTED);
                    contentValues.put("sim_id", Integer.valueOf(SIM_CHANGED));
                    contentValues = contentValues;
                    contentResolver.update(SubscriptionManager.CONTENT_URI, contentValues, "sim_id=" + Integer.toString(((SubscriptionInfo) oldSubInfo.get(STATUS_NO_SIM_INSERTED)).getSubscriptionId()), null);
                }
            } else {
                if (mInsertSimState[i] == 0) {
                    mInsertSimState[i] = SIM_CHANGED;
                }
                oldIccId[i] = ICCID_STRING_FOR_NO_SIM;
                logd("updateSubscriptionInfoByIccId: No SIM in slot " + i + " last time");
            }
            i += STATUS_SIM1_INSERTED;
        }
        for (i = STATUS_NO_SIM_INSERTED; i < PROJECT_SIM_NUM; i += STATUS_SIM1_INSERTED) {
            logd("updateSubscriptionInfoByIccId: oldIccId[" + i + "] = " + oldIccId[i] + ", sIccId[" + i + "] = " + mIccId[i]);
        }
        int nNewCardCount = STATUS_NO_SIM_INSERTED;
        int nNewSimStatus = STATUS_NO_SIM_INSERTED;
        i = STATUS_NO_SIM_INSERTED;
        while (i < PROJECT_SIM_NUM) {
            if (mInsertSimState[i] == SIM_NOT_INSERT) {
                logd("updateSubscriptionInfoByIccId: No SIM inserted in slot " + i + " this time");
                if (PROJECT_SIM_NUM == STATUS_SIM1_INSERTED) {
                    HwTelephonyFactory.getHwUiccManager().updateUserPreferences(false);
                }
            } else {
                if (mInsertSimState[i] > 0) {
                    this.mSubscriptionManager.addSubscriptionInfoRecord(mIccId[i] + Integer.toString(mInsertSimState[i]), i);
                    logd("SUB" + (i + STATUS_SIM1_INSERTED) + " has invalid IccId");
                } else {
                    this.mSubscriptionManager.addSubscriptionInfoRecord(mIccId[i], i);
                }
                if (mInsertSimState[i] == i + STATUS_SIM1_INSERTED && oldIccId[i] != null) {
                    if (oldIccId[i].equals(mIccId[i] + Integer.toString(mInsertSimState[i]))) {
                        logd("same iccid not change index = " + i);
                        mInsertSimState[i] = STATUS_NO_SIM_INSERTED;
                    }
                }
                if (isNewSim(mIccId[i], oldIccId)) {
                    nNewCardCount += STATUS_SIM1_INSERTED;
                    switch (i) {
                        case STATUS_NO_SIM_INSERTED /*0*/:
                            nNewSimStatus |= STATUS_SIM1_INSERTED;
                            break;
                        case STATUS_SIM1_INSERTED /*1*/:
                            nNewSimStatus |= STATUS_SIM2_INSERTED;
                            break;
                        case STATUS_SIM2_INSERTED /*2*/:
                            nNewSimStatus |= STATUS_SIM3_INSERTED;
                            break;
                    }
                    mInsertSimState[i] = SIM_NEW;
                }
            }
            i += STATUS_SIM1_INSERTED;
        }
        for (i = STATUS_NO_SIM_INSERTED; i < PROJECT_SIM_NUM; i += STATUS_SIM1_INSERTED) {
            if (mInsertSimState[i] == SIM_CHANGED) {
                mInsertSimState[i] = SIM_REPOSITION;
            }
            logd("updateSubscriptionInfoByIccId: sInsertSimState[" + i + "] = " + mInsertSimState[i]);
        }
        if (PROJECT_SIM_NUM > STATUS_SIM1_INSERTED) {
            updateSubActivation(mInsertSimState, false);
        }
        List<SubscriptionInfo> subInfos = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        int nSubCount = subInfos == null ? STATUS_NO_SIM_INSERTED : subInfos.size();
        logd("updateSubscriptionInfoByIccId: nSubCount = " + nSubCount);
        for (i = STATUS_NO_SIM_INSERTED; i < nSubCount; i += STATUS_SIM1_INSERTED) {
            SubscriptionInfo temp = (SubscriptionInfo) subInfos.get(i);
            String msisdn = TelephonyManager.getDefault().getLine1Number(temp.getSubscriptionId());
            if (msisdn != null) {
                contentValues = new ContentValues(STATUS_SIM1_INSERTED);
                contentValues.put("number", msisdn);
                contentValues = contentValues;
                contentResolver.update(SubscriptionManager.CONTENT_URI, contentValues, "sim_id=" + Integer.toString(temp.getSubscriptionId()), null);
            }
        }
        broadcastSubinfoRecordUpdated(mIccId, oldIccId, nNewCardCount, nSubCount, nNewSimStatus);
        SubscriptionController.getInstance().notifySubscriptionInfoChanged();
        logd("updateSubscriptionInfoByIccId:- SsubscriptionInfo update complete");
    }

    private boolean isNewSim(String iccId, String[] oldIccId) {
        boolean newSim = true;
        for (int i = STATUS_NO_SIM_INSERTED; i < PROJECT_SIM_NUM; i += STATUS_SIM1_INSERTED) {
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
        for (int i = STATUS_NO_SIM_INSERTED; i < PROJECT_SIM_NUM; i += STATUS_SIM1_INSERTED) {
            mInsertSimState[i] = STATUS_NO_SIM_INSERTED;
        }
    }

    public void cleanIccids() {
        for (int i = STATUS_NO_SIM_INSERTED; i < mIccId.length; i += STATUS_SIM1_INSERTED) {
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
