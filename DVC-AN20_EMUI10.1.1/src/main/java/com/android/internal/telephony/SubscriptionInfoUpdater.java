package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.UserSwitchObserver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.IPackageManager;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.PreferenceManager;
import android.service.carrier.CarrierIdentifier;
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
import com.android.internal.telephony.SubscriptionInfoUpdater;
import com.android.internal.telephony.euicc.EuiccController;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccSlot;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.IccCardConstantsEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SubscriptionInfoUpdater extends Handler implements ISubscriptionInfoUpdaterInner {
    public static final String CURR_SUBID = "curr_subid";
    private static final boolean DBG = true;
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
    @UnsupportedAppUsage
    private static final int PROJECT_SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static final ParcelUuid REMOVE_GROUP_UUID = ParcelUuid.fromString("00000000-0000-0000-0000-000000000000");
    @UnsupportedAppUsage
    private static Context mContext = null;
    @UnsupportedAppUsage
    private static String[] mIccId;
    @UnsupportedAppUsage
    private static Phone[] mPhone;
    private static boolean sIsSubInfoInitialized = false;
    private static int[] sSimApplicationState;
    private static int[] sSimCardState;
    private Handler mBackgroundHandler;
    private CarrierServiceBindHelper mCarrierServiceBindHelper;
    @UnsupportedAppUsage
    private int mCurrentlyActiveUserId;
    private EuiccManager mEuiccManager;
    IHwSubscriptionInfoUpdaterEx mHwSubscriptionInfoUpdaterEx;
    @UnsupportedAppUsage
    private IPackageManager mPackageManager;
    private SubscriptionManager mSubscriptionManager;

    /* access modifiers changed from: package-private */
    public interface UpdateEmbeddedSubsCallback {
        void run(boolean z);
    }

    static {
        int i = PROJECT_SIM_NUM;
        mIccId = new String[i];
        sSimCardState = new int[i];
        sSimApplicationState = new int[i];
    }

    public SubscriptionInfoUpdater(Looper looper, Context context, Phone[] phone, CommandsInterface[] ci) {
        this(looper, context, phone, ci, IPackageManager.Stub.asInterface(ServiceManager.getService("package")));
    }

    @VisibleForTesting
    public SubscriptionInfoUpdater(Looper looper, Context context, Phone[] phone, CommandsInterface[] ci, IPackageManager packageMgr) {
        this.mSubscriptionManager = null;
        logd("Constructor invoked");
        this.mBackgroundHandler = new Handler(looper);
        mContext = context;
        mPhone = phone;
        this.mSubscriptionManager = SubscriptionManager.from(mContext);
        this.mEuiccManager = (EuiccManager) mContext.getSystemService("euicc");
        this.mPackageManager = packageMgr;
        CommandsInterfaceEx[] commandsInterfaceEx = new CommandsInterfaceEx[ci.length];
        int index = 0;
        CommandsInterface[] commandsInterfaceArr = (CommandsInterface[]) ci.clone();
        int length = commandsInterfaceArr.length;
        int i = 0;
        while (i < length) {
            CommandsInterface commandsInterface = commandsInterfaceArr[i];
            commandsInterfaceEx[index] = new CommandsInterfaceEx();
            commandsInterfaceEx[index].setCommandsInterface(commandsInterface);
            i++;
            index++;
        }
        this.mHwSubscriptionInfoUpdaterEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwSubscriptionInfoUpdaterEx(this, context, commandsInterfaceEx);
        new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
        this.mCarrierServiceBindHelper = new CarrierServiceBindHelper(mContext);
        initializeCarrierApps();
    }

    private void initializeCarrierApps() {
        this.mCurrentlyActiveUserId = 0;
        try {
            ActivityManager.getService().registerUserSwitchObserver(new UserSwitchObserver() {
                /* class com.android.internal.telephony.SubscriptionInfoUpdater.AnonymousClass1 */

                public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
                    SubscriptionInfoUpdater.this.mCurrentlyActiveUserId = newUserId;
                    CarrierAppUtils.disableCarrierAppsUntilPrivileged(SubscriptionInfoUpdater.mContext.getOpPackageName(), SubscriptionInfoUpdater.this.mPackageManager, TelephonyManager.getDefault(), SubscriptionInfoUpdater.mContext.getContentResolver(), SubscriptionInfoUpdater.this.mCurrentlyActiveUserId);
                    if (reply != null) {
                        try {
                            reply.sendResult((Bundle) null);
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

    public void updateInternalIccState(String simStatus, String reason, int slotId, boolean absentAndInactive) {
        logd("updateInternalIccState to simStatus " + simStatus + " reason " + reason + " slotId " + slotId);
        if (!SubscriptionManager.isValidSlotIndex(slotId)) {
            logd("updateInternalIccState contains invalid slotIndex: " + slotId);
            return;
        }
        int message = internalIccStateToMessage(simStatus);
        if (message != -1) {
            sendMessage(obtainMessage(message, slotId, absentAndInactive ? 1 : 0, reason));
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private int internalIccStateToMessage(String simStatus) {
        char c;
        switch (simStatus.hashCode()) {
            case -2044189691:
                if (simStatus.equals(IccCardConstantsEx.INTENT_VALUE_ICC_LOADED)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -2044123382:
                if (simStatus.equals(IccCardConstantsEx.INTENT_VALUE_ICC_LOCKED)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -1830845986:
                if (simStatus.equals(IccCardConstantsEx.INTENT_VALUE_ICC_CARD_IO_ERROR)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 2251386:
                if (simStatus.equals(IccCardConstantsEx.INTENT_VALUE_ICC_IMSI)) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 77848963:
                if (simStatus.equals(IccCardConstantsEx.INTENT_VALUE_ICC_READY)) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 433141802:
                if (simStatus.equals(IccCardConstantsEx.INTENT_VALUE_ICC_UNKNOWN)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1034051831:
                if (simStatus.equals(IccCardConstantsEx.INTENT_VALUE_ICC_NOT_READY)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 1599753450:
                if (simStatus.equals(IccCardConstantsEx.INTENT_VALUE_ICC_CARD_RESTRICTED)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1924388665:
                if (simStatus.equals(IccCardConstantsEx.INTENT_VALUE_ICC_ABSENT)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
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
            case '\b':
                return 11;
            default:
                logd("Ignoring simStatus: " + simStatus);
                return -1;
        }
    }

    @UnsupportedAppUsage
    private boolean isAllIccIdQueryDone() {
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            if (VSimUtilsInner.isPlatformTwoModems() && !VSimUtilsInner.isRadioAvailable(i)) {
                logd("[2Cards]Ignore pending sub" + i);
                mIccId[i] = "";
            }
            UiccSlot slot = UiccController.getInstance().getUiccSlotForPhone(i);
            int slotId = UiccController.getInstance().getSlotIdFromPhoneId(i);
            if (mIccId[i] == null || slot == null || !slot.isActive()) {
                if (mIccId[i] == null) {
                    logd("Wait for SIM " + i + " Iccid");
                } else {
                    logd(String.format("Wait for slot corresponding to phone %d to be active, slotId is %d", Integer.valueOf(i), Integer.valueOf(slotId)));
                }
                return false;
            }
        }
        logd("All IccIds query complete");
        return true;
    }

    public void handleMessage(Message msg) {
        List<Integer> cardIds = new ArrayList<>();
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
                handleSimAbsent(msg.arg1, msg.arg2);
                return;
            case 5:
                handleSimLocked(msg.arg1, (String) msg.obj);
                return;
            case 6:
                handleSimError(msg.arg1);
                return;
            case 7:
                broadcastSimStateChanged(msg.arg1, IccCardConstantsEx.INTENT_VALUE_ICC_UNKNOWN, null);
                broadcastSimCardStateChanged(msg.arg1, 0);
                broadcastSimApplicationStateChanged(msg.arg1, 0);
                updateSubscriptionCarrierId(msg.arg1, IccCardConstantsEx.INTENT_VALUE_ICC_UNKNOWN);
                updateCarrierServices(msg.arg1, IccCardConstantsEx.INTENT_VALUE_ICC_UNKNOWN);
                return;
            case 8:
                broadcastSimStateChanged(msg.arg1, IccCardConstantsEx.INTENT_VALUE_ICC_CARD_RESTRICTED, IccCardConstantsEx.INTENT_VALUE_ICC_CARD_RESTRICTED);
                broadcastSimCardStateChanged(msg.arg1, 9);
                broadcastSimApplicationStateChanged(msg.arg1, 6);
                updateSubscriptionCarrierId(msg.arg1, IccCardConstantsEx.INTENT_VALUE_ICC_CARD_RESTRICTED);
                updateCarrierServices(msg.arg1, IccCardConstantsEx.INTENT_VALUE_ICC_CARD_RESTRICTED);
                return;
            case 9:
                cardIds.add(Integer.valueOf(getCardIdFromPhoneId(msg.arg1)));
                updateEmbeddedSubscriptions(cardIds, $$Lambda$SubscriptionInfoUpdater$tLUuQ7lYu8EjRd038qzQlDmCtA.INSTANCE);
                handleSimNotReady(msg.arg1);
                return;
            case 10:
                cardIds.add(Integer.valueOf(getCardIdFromPhoneId(msg.arg1)));
                updateEmbeddedSubscriptions(cardIds, $$Lambda$SubscriptionInfoUpdater$DY4i_CG7hrAeejGLeh3hMUZySnw.INSTANCE);
                broadcastSimStateChanged(msg.arg1, IccCardConstantsEx.INTENT_VALUE_ICC_READY, null);
                broadcastSimCardStateChanged(msg.arg1, 11);
                broadcastSimApplicationStateChanged(msg.arg1, 6);
                return;
            case 11:
                broadcastSimStateChanged(msg.arg1, IccCardConstantsEx.INTENT_VALUE_ICC_IMSI, null);
                return;
            case 12:
                cardIds.add(Integer.valueOf(msg.arg1));
                updateEmbeddedSubscriptions(cardIds, new UpdateEmbeddedSubsCallback((Runnable) msg.obj) {
                    /* class com.android.internal.telephony.$$Lambda$SubscriptionInfoUpdater$UFyB0ValfLD0rdGDibCjTnGFkeo */
                    private final /* synthetic */ Runnable f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // com.android.internal.telephony.SubscriptionInfoUpdater.UpdateEmbeddedSubsCallback
                    public final void run(boolean z) {
                        SubscriptionInfoUpdater.lambda$handleMessage$2(this.f$0, z);
                    }
                });
                return;
            default:
                super.handleMessage(msg);
                return;
        }
    }

    static /* synthetic */ void lambda$handleMessage$0(boolean hasChanges) {
        if (hasChanges) {
            SubscriptionController.getInstance().notifySubscriptionInfoChanged();
        }
    }

    static /* synthetic */ void lambda$handleMessage$1(boolean hasChanges) {
        if (hasChanges) {
            SubscriptionController.getInstance().notifySubscriptionInfoChanged();
        }
    }

    static /* synthetic */ void lambda$handleMessage$2(Runnable r, boolean hasChanges) {
        if (hasChanges) {
            SubscriptionController.getInstance().notifySubscriptionInfoChanged();
        }
        if (r != null) {
            r.run();
        }
    }

    private int getCardIdFromPhoneId(int phoneId) {
        UiccController uiccController = UiccController.getInstance();
        UiccCard card = uiccController.getUiccCardForPhone(phoneId);
        if (card != null) {
            return uiccController.convertToPublicCardId(card.getCardId());
        }
        return -2;
    }

    /* access modifiers changed from: package-private */
    public void requestEmbeddedSubscriptionInfoListRefresh(int cardId, Runnable callback) {
        sendMessage(obtainMessage(12, cardId, 0, callback));
    }

    private void handleSimLocked(int slotId, String reason) {
        String[] strArr = mIccId;
        if (strArr[slotId] != null && strArr[slotId].equals("")) {
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
        broadcastSimStateChanged(slotId, IccCardConstantsEx.INTENT_VALUE_ICC_LOCKED, reason);
        broadcastSimCardStateChanged(slotId, 11);
        broadcastSimApplicationStateChanged(slotId, getSimStateFromLockedReason(reason));
        updateSubscriptionCarrierId(slotId, IccCardConstantsEx.INTENT_VALUE_ICC_LOCKED);
        updateCarrierServices(slotId, IccCardConstantsEx.INTENT_VALUE_ICC_LOCKED);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getSimStateFromLockedReason(String lockedReason) {
        char c;
        switch (lockedReason.hashCode()) {
            case -1733499378:
                if (lockedReason.equals(IccCardConstantsEx.INTENT_VALUE_LOCKED_NETWORK)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 79221:
                if (lockedReason.equals(IccCardConstantsEx.INTENT_VALUE_LOCKED_ON_PIN)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 79590:
                if (lockedReason.equals(IccCardConstantsEx.INTENT_VALUE_LOCKED_ON_PUK)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 190660331:
                if (lockedReason.equals(IccCardConstantsEx.INTENT_VALUE_ABSENT_ON_PERM_DISABLED)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            return 2;
        }
        if (c == 1) {
            return 3;
        }
        if (c == 2) {
            return 4;
        }
        if (c == 3) {
            return 7;
        }
        Rlog.e(LOG_TAG, "Unexpected SIM locked reason " + lockedReason);
        return 0;
    }

    private void handleSimNotReady(int slotId) {
        logd("handleSimNotReady: slotId: " + slotId);
        if (mPhone[slotId].getIccCard().isEmptyProfile()) {
            mIccId[slotId] = "";
        }
        broadcastSimStateChanged(slotId, IccCardConstantsEx.INTENT_VALUE_ICC_NOT_READY, null);
        broadcastSimCardStateChanged(slotId, 11);
        broadcastSimApplicationStateChanged(slotId, 6);
    }

    private void handleSimLoaded(int slotId) {
        IccCard iccCard;
        List<SubscriptionInfo> subscriptionInfos;
        String nameToSet;
        logd("handleSimLoaded: slotId: " + slotId);
        IccCard iccCard2 = mPhone[slotId].getIccCard();
        if (iccCard2 == null) {
            logd("handleSimLoaded: IccCard null");
            return;
        }
        IccRecords records = iccCard2.getIccRecords();
        if (records == null) {
            logd("handleSimLoaded: IccRecords null");
        } else if (IccUtils.stripTrailingFs(records.getFullIccId()) == null) {
            logd("handleSimLoaded: IccID null");
        } else {
            mIccId[slotId] = IccUtils.stripTrailingFs(records.getFullIccId());
            if (VSimUtilsInner.needBlockUnReservedForVsim(slotId)) {
                logd("handleSimStateLoadedInternal: block Unreserved subId, don't set mIccId[" + slotId + "] from records");
            } else {
                mIccId[slotId] = TextUtils.isEmpty(records.getIccId()) ? "emptyiccid" + slotId : records.getIccId();
            }
            List<SubscriptionInfo> subscriptionInfos2 = SubscriptionController.getInstance().getSubInfoUsingSlotIndexPrivileged(slotId);
            if (subscriptionInfos2 != null) {
                if (!subscriptionInfos2.isEmpty()) {
                    for (Iterator<SubscriptionInfo> it = subscriptionInfos2.iterator(); it.hasNext(); it = it) {
                        int subId = it.next().getSubscriptionId();
                        TelephonyManager tm = (TelephonyManager) mContext.getSystemService("phone");
                        String operator = tm.getSimOperatorNumeric(subId);
                        if (!TextUtils.isEmpty(operator)) {
                            if (subId == SubscriptionController.getInstance().getDefaultSubId()) {
                                MccTable.updateMccMncConfiguration(mContext, operator);
                            }
                            SubscriptionController.getInstance().setMccMnc(operator, subId);
                        } else {
                            logd("EVENT_RECORDS_LOADED Operator name is null");
                        }
                        String iso = tm.getSimCountryIsoForPhone(slotId);
                        if (!TextUtils.isEmpty(iso)) {
                            SubscriptionController.getInstance().setCountryIso(iso, subId);
                        } else {
                            logd("EVENT_RECORDS_LOADED sim country iso is null");
                        }
                        String msisdn = tm.getLine1Number(subId);
                        if (msisdn != null) {
                            SubscriptionController.getInstance().setDisplayNumber(msisdn, subId);
                        }
                        String imsi = tm.createForSubscriptionId(subId).getSubscriberId();
                        if (imsi != null) {
                            SubscriptionController.getInstance().setImsi(imsi, subId);
                        }
                        String[] ehplmns = records.getEhplmns();
                        String[] hplmns = records.getPlmnsFromHplmnActRecord();
                        if (ehplmns == null && hplmns == null) {
                            iccCard = iccCard2;
                        } else {
                            iccCard = iccCard2;
                            SubscriptionController.getInstance().setAssociatedPlmns(ehplmns, hplmns, subId);
                        }
                        SubscriptionInfo subInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(subId);
                        String simCarrierName = tm.getSimOperatorName(subId);
                        if (subInfo != null) {
                            subscriptionInfos = subscriptionInfos2;
                            if (subInfo.getNameSource() != 2) {
                                if (!TextUtils.isEmpty(simCarrierName)) {
                                    nameToSet = simCarrierName;
                                } else if (tm.isMultiSimEnabled()) {
                                    nameToSet = "CARD " + Integer.toString(slotId + 1);
                                } else {
                                    nameToSet = "CARD";
                                }
                                logd("sim name = " + nameToSet);
                                SubscriptionController.getInstance().setDisplayNameUsingSrc(nameToSet, subId, 0);
                            }
                        } else {
                            subscriptionInfos = subscriptionInfos2;
                        }
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                        if (sp.getInt(CURR_SUBID + slotId, -1) != subId) {
                            mPhone[slotId].getNetworkSelectionMode(obtainMessage(2, new Integer(slotId)));
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putInt(CURR_SUBID + slotId, subId);
                            editor.apply();
                        }
                        iccCard2 = iccCard;
                        records = records;
                        subscriptionInfos2 = subscriptionInfos;
                    }
                    CarrierAppUtils.disableCarrierAppsUntilPrivileged(mContext.getOpPackageName(), this.mPackageManager, TelephonyManager.getDefault(), mContext.getContentResolver(), this.mCurrentlyActiveUserId);
                    broadcastSimStateChanged(slotId, IccCardConstantsEx.INTENT_VALUE_ICC_LOADED, null);
                    broadcastSimCardStateChanged(slotId, 11);
                    broadcastSimApplicationStateChanged(slotId, 10);
                    updateSubscriptionCarrierId(slotId, IccCardConstantsEx.INTENT_VALUE_ICC_LOADED);
                    updateCarrierServices(slotId, IccCardConstantsEx.INTENT_VALUE_ICC_LOADED);
                }
            }
            loge("empty subinfo for slotId: " + slotId + "could not update ContentResolver");
            CarrierAppUtils.disableCarrierAppsUntilPrivileged(mContext.getOpPackageName(), this.mPackageManager, TelephonyManager.getDefault(), mContext.getContentResolver(), this.mCurrentlyActiveUserId);
            broadcastSimStateChanged(slotId, IccCardConstantsEx.INTENT_VALUE_ICC_LOADED, null);
            broadcastSimCardStateChanged(slotId, 11);
            broadcastSimApplicationStateChanged(slotId, 10);
            updateSubscriptionCarrierId(slotId, IccCardConstantsEx.INTENT_VALUE_ICC_LOADED);
            updateCarrierServices(slotId, IccCardConstantsEx.INTENT_VALUE_ICC_LOADED);
        }
    }

    private void updateCarrierServices(int slotId, String simState) {
        ((CarrierConfigManager) mContext.getSystemService("carrier_config")).updateConfigForPhoneId(slotId, simState);
        this.mCarrierServiceBindHelper.updateForPhoneId(slotId, simState);
    }

    private void updateSubscriptionCarrierId(int slotId, String simState) {
        Phone[] phoneArr = mPhone;
        if (phoneArr != null && phoneArr[slotId] != null) {
            phoneArr[slotId].resolveSubscriptionCarrierId(simState);
        }
    }

    private void handleSimAbsent(int slotId, int absentAndInactive) {
        String[] strArr = mIccId;
        if (strArr[slotId] != null && !strArr[slotId].equals("")) {
            logd("SIM" + (slotId + 1) + " hot plug out, absentAndInactive=" + absentAndInactive);
        }
        if (absentAndInactive == 0) {
            broadcastSimStateChanged(slotId, IccCardConstantsEx.INTENT_VALUE_ICC_ABSENT, null);
            broadcastSimCardStateChanged(slotId, 1);
            broadcastSimApplicationStateChanged(slotId, 0);
            updateCarrierServices(slotId, IccCardConstantsEx.INTENT_VALUE_ICC_ABSENT);
        }
    }

    private void handleSimError(int slotId) {
        String[] strArr = mIccId;
        if (strArr[slotId] != null && !strArr[slotId].equals("")) {
            logd("SIM" + (slotId + 1) + " Error ");
        }
        broadcastSimStateChanged(slotId, IccCardConstantsEx.INTENT_VALUE_ICC_CARD_IO_ERROR, IccCardConstantsEx.INTENT_VALUE_ICC_CARD_IO_ERROR);
        broadcastSimCardStateChanged(slotId, 8);
        broadcastSimApplicationStateChanged(slotId, 6);
        updateSubscriptionCarrierId(slotId, IccCardConstantsEx.INTENT_VALUE_ICC_CARD_IO_ERROR);
        updateCarrierServices(slotId, IccCardConstantsEx.INTENT_VALUE_ICC_CARD_IO_ERROR);
    }

    private synchronized void updateSubscriptionInfoByIccId(int slotIndex, boolean updateEmbeddedSubs) {
        logd("updateSubscriptionInfoByIccId:+ Start");
        if (!SubscriptionManager.isValidSlotIndex(slotIndex)) {
            loge("[updateSubscriptionInfoByIccId]- invalid slotIndex=" + slotIndex);
            return;
        }
        this.mHwSubscriptionInfoUpdaterEx.recordSimStateBySlotId(slotIndex);
        logd("updateSubscriptionInfoByIccId: removing subscription info record: slotIndex " + slotIndex);
        SubscriptionController.getInstance().clearSubInfoRecord(slotIndex);
        if (!"".equals(mIccId[slotIndex])) {
            logd("updateSubscriptionInfoByIccId: adding subscription info record: iccid: " + SubscriptionInfo.givePrintableIccid(mIccId[slotIndex]) + "slot: " + slotIndex);
            this.mSubscriptionManager.addSubscriptionInfoRecord(mIccId[slotIndex], slotIndex);
        }
        if (PROJECT_SIM_NUM > 1) {
            this.mHwSubscriptionInfoUpdaterEx.updateSubActivation(slotIndex);
        }
        List<SubscriptionInfo> subInfos = SubscriptionController.getInstance().getSubInfoUsingSlotIndexPrivileged(slotIndex);
        if (subInfos != null) {
            boolean changed = false;
            for (int i = 0; i < subInfos.size(); i++) {
                SubscriptionInfo temp = subInfos.get(i);
                ContentValues value = new ContentValues(1);
                String msisdn = TelephonyManager.getDefault().getLine1Number(temp.getSubscriptionId());
                if (!TextUtils.equals(msisdn, temp.getNumber())) {
                    value.put("number", msisdn);
                    mContext.getContentResolver().update(SubscriptionManager.getUriForSubscriptionId(temp.getSubscriptionId()), value, null, null);
                    changed = true;
                }
            }
            if (changed) {
                SubscriptionController.getInstance().refreshCachedActiveSubscriptionInfoList();
            }
        }
        if (isAllIccIdQueryDone()) {
            this.mHwSubscriptionInfoUpdaterEx.broadcastSubinfoRecordUpdated(mIccId);
            setSubInfoInitialized();
        }
        UiccController uiccController = UiccController.getInstance();
        UiccSlot[] uiccSlots = uiccController.getUiccSlots();
        if (uiccSlots != null && updateEmbeddedSubs) {
            List<Integer> cardIds = new ArrayList<>();
            for (UiccSlot uiccSlot : uiccSlots) {
                if (!(uiccSlot == null || uiccSlot.getUiccCard() == null)) {
                    cardIds.add(Integer.valueOf(uiccController.convertToPublicCardId(uiccSlot.getUiccCard().getCardId())));
                }
            }
            updateEmbeddedSubscriptions(cardIds, $$Lambda$SubscriptionInfoUpdater$ecTEeMEIjOEa2z5W3wjqiicibbY.INSTANCE);
        }
        SubscriptionController.getInstance().notifySubscriptionInfoChanged();
        logd("updateSubscriptionInfoByIccId: SubscriptionInfo update complete");
    }

    static /* synthetic */ void lambda$updateSubscriptionInfoByIccId$3(boolean hasChanges) {
        if (hasChanges) {
            SubscriptionController.getInstance().notifySubscriptionInfoChanged();
        }
        logd("updateSubscriptionInfoByIccId: SubscriptionInfo update complete");
    }

    private static void setSubInfoInitialized() {
        if (!sIsSubInfoInitialized) {
            Rlog.d(LOG_TAG, "SubInfo Initialized");
            sIsSubInfoInitialized = true;
        }
    }

    public static boolean isSubInfoInitialized() {
        return sIsSubInfoInitialized;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void updateEmbeddedSubscriptions(List<Integer> cardIds, UpdateEmbeddedSubsCallback callback) {
        if (!this.mEuiccManager.isEnabled()) {
            callback.run(false);
        } else {
            this.mBackgroundHandler.post(new Runnable(cardIds, callback) {
                /* class com.android.internal.telephony.$$Lambda$SubscriptionInfoUpdater$qyDxq2AWyReUxdc6HttVGQeDD3Y */
                private final /* synthetic */ List f$1;
                private final /* synthetic */ SubscriptionInfoUpdater.UpdateEmbeddedSubsCallback f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    SubscriptionInfoUpdater.this.lambda$updateEmbeddedSubscriptions$5$SubscriptionInfoUpdater(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$updateEmbeddedSubscriptions$5$SubscriptionInfoUpdater(List cardIds, UpdateEmbeddedSubsCallback callback) {
        List<GetEuiccProfileInfoListResult> results = new ArrayList<>();
        Iterator it = cardIds.iterator();
        while (it.hasNext()) {
            int cardId = ((Integer) it.next()).intValue();
            GetEuiccProfileInfoListResult result = EuiccController.get().blockingGetEuiccProfileInfoList(cardId);
            logd("blockingGetEuiccProfileInfoList cardId " + cardId);
            results.add(result);
        }
        post(new Runnable(results, callback) {
            /* class com.android.internal.telephony.$$Lambda$SubscriptionInfoUpdater$Y5woGfEDKrozRViLH7WF93qPEno */
            private final /* synthetic */ List f$1;
            private final /* synthetic */ SubscriptionInfoUpdater.UpdateEmbeddedSubsCallback f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                SubscriptionInfoUpdater.this.lambda$updateEmbeddedSubscriptions$4$SubscriptionInfoUpdater(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$updateEmbeddedSubscriptions$4$SubscriptionInfoUpdater(List results, UpdateEmbeddedSubsCallback callback) {
        boolean hasChanges = false;
        Iterator it = results.iterator();
        while (it.hasNext()) {
            if (updateEmbeddedSubscriptionsCache((GetEuiccProfileInfoListResult) it.next())) {
                hasChanges = true;
            }
        }
        if (callback != null) {
            callback.run(hasChanges);
        }
    }

    private boolean updateEmbeddedSubscriptionsCache(GetEuiccProfileInfoListResult result) {
        EuiccProfileInfo[] embeddedProfiles;
        List<EuiccProfileInfo> list;
        SubscriptionInfoUpdater subscriptionInfoUpdater = this;
        logd("updateEmbeddedSubscriptionsCache");
        if (result == null) {
            return false;
        }
        List<EuiccProfileInfo> list2 = result.getProfiles();
        if (result.getResult() != 0 || list2 == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("blockingGetEuiccProfileInfoList returns an error. Result code=");
            sb.append(result.getResult());
            sb.append(". Null profile list=");
            sb.append(result.getProfiles() == null);
            logd(sb.toString());
            return false;
        }
        EuiccProfileInfo[] embeddedProfiles2 = (EuiccProfileInfo[]) list2.toArray(new EuiccProfileInfo[list2.size()]);
        logd("blockingGetEuiccProfileInfoList: got " + result.getProfiles().size() + " profiles");
        boolean isRemovable = result.getIsRemovable();
        String[] embeddedIccids = new String[embeddedProfiles2.length];
        for (int i = 0; i < embeddedProfiles2.length; i++) {
            embeddedIccids[i] = subscriptionInfoUpdater.mHwSubscriptionInfoUpdaterEx.padTrailingFs(embeddedProfiles2[i].getIccid());
        }
        logd("Get eUICC profile list of size " + embeddedProfiles2.length);
        List<SubscriptionInfo> existingSubscriptions = SubscriptionController.getInstance().getSubscriptionInfoListForEmbeddedSubscriptionUpdate(embeddedIccids, isRemovable);
        ContentResolver contentResolver = mContext.getContentResolver();
        int length = embeddedProfiles2.length;
        boolean hasChanges = false;
        int i2 = 0;
        while (i2 < length) {
            EuiccProfileInfo embeddedProfile = embeddedProfiles2[i2];
            int index = findSubscriptionInfoForIccid(existingSubscriptions, subscriptionInfoUpdater.mHwSubscriptionInfoUpdaterEx.padTrailingFs(embeddedProfile.getIccid()));
            int prevCarrierId = -1;
            int nameSource = 0;
            if (index < 0) {
                list = list2;
                embeddedProfiles = embeddedProfiles2;
                SubscriptionController.getInstance().insertEmptySubInfoRecord(subscriptionInfoUpdater.mHwSubscriptionInfoUpdaterEx.padTrailingFs(embeddedProfile.getIccid()), -1);
            } else {
                list = list2;
                embeddedProfiles = embeddedProfiles2;
                nameSource = existingSubscriptions.get(index).getNameSource();
                prevCarrierId = existingSubscriptions.get(index).getCarrierId();
                existingSubscriptions.remove(index);
            }
            StringBuilder sb2 = new StringBuilder();
            sb2.append("embeddedProfile ");
            sb2.append(embeddedProfile);
            sb2.append(" existing record ");
            sb2.append(index < 0 ? "not found" : "found");
            logd(sb2.toString());
            ContentValues values = new ContentValues();
            values.put("is_embedded", (Integer) 1);
            List<UiccAccessRule> ruleList = embeddedProfile.getUiccAccessRules();
            boolean isRuleListEmpty = false;
            if (ruleList == null || ruleList.size() == 0) {
                isRuleListEmpty = true;
            }
            values.put("access_rules", isRuleListEmpty ? null : UiccAccessRule.encodeRules((UiccAccessRule[]) ruleList.toArray(new UiccAccessRule[ruleList.size()])));
            values.put("is_removable", Boolean.valueOf(isRemovable));
            if (SubscriptionController.getNameSourcePriority(nameSource) <= SubscriptionController.getNameSourcePriority(3)) {
                values.put("display_name", embeddedProfile.getNickname());
                values.put("name_source", (Integer) 3);
            }
            values.put("profile_class", Integer.valueOf(embeddedProfile.getProfileClass()));
            CarrierIdentifier cid = embeddedProfile.getCarrierIdentifier();
            if (cid != null) {
                if (prevCarrierId == -1) {
                    values.put("carrier_id", Integer.valueOf(CarrierResolver.getCarrierIdFromIdentifier(mContext, cid)));
                }
                String mcc = cid.getMcc();
                String mnc = cid.getMnc();
                values.put("mcc_string", mcc);
                values.put("mcc", mcc);
                values.put("mnc_string", mnc);
                values.put("mnc", mnc);
            }
            subscriptionInfoUpdater.mHwSubscriptionInfoUpdaterEx.putExtraValueForEuicc(values, embeddedProfile.getServiceProviderName(), embeddedProfile.getState());
            hasChanges = true;
            contentResolver.update(SubscriptionManager.CONTENT_URI, values, "icc_id=\"" + subscriptionInfoUpdater.mHwSubscriptionInfoUpdaterEx.padTrailingFs(embeddedProfile.getIccid()) + "\"", null);
            SubscriptionController.getInstance().refreshCachedActiveSubscriptionInfoList();
            i2++;
            subscriptionInfoUpdater = this;
            list2 = list;
            embeddedProfiles2 = embeddedProfiles;
        }
        if (!existingSubscriptions.isEmpty()) {
            logd("Removing existing embedded subscriptions of size" + existingSubscriptions.size());
            List<String> iccidsToRemove = new ArrayList<>();
            for (int i3 = 0; i3 < existingSubscriptions.size(); i3++) {
                SubscriptionInfo info = existingSubscriptions.get(i3);
                if (info.isEmbedded()) {
                    logd("Removing embedded subscription of IccId " + SubscriptionInfo.givePrintableIccid(info.getIccId()));
                    iccidsToRemove.add("\"" + info.getIccId() + "\"");
                }
            }
            ContentValues values2 = new ContentValues();
            values2.put("is_embedded", (Integer) 0);
            hasChanges = true;
            contentResolver.update(SubscriptionManager.CONTENT_URI, values2, "icc_id IN (" + TextUtils.join(",", iccidsToRemove) + ")", null);
            SubscriptionController.getInstance().refreshCachedActiveSubscriptionInfoList();
        }
        logd("updateEmbeddedSubscriptions done hasChanges=" + hasChanges);
        return hasChanges;
    }

    public void updateSubscriptionByCarrierConfigAndNotifyComplete(int phoneId, String configPackageName, PersistableBundle config, Message onComplete) {
        post(new Runnable(phoneId, configPackageName, config, onComplete) {
            /* class com.android.internal.telephony.$$Lambda$SubscriptionInfoUpdater$ZTY4uxKw17CHcHQzbBUF7mdNE */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ String f$2;
            private final /* synthetic */ PersistableBundle f$3;
            private final /* synthetic */ Message f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                SubscriptionInfoUpdater.this.lambda$updateSubscriptionByCarrierConfigAndNotifyComplete$6$SubscriptionInfoUpdater(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    public /* synthetic */ void lambda$updateSubscriptionByCarrierConfigAndNotifyComplete$6$SubscriptionInfoUpdater(int phoneId, String configPackageName, PersistableBundle config, Message onComplete) {
        updateSubscriptionByCarrierConfig(phoneId, configPackageName, config);
        onComplete.sendToTarget();
    }

    private String getDefaultCarrierServicePackageName() {
        return ((CarrierConfigManager) mContext.getSystemService("carrier_config")).getDefaultCarrierServicePackageName();
    }

    private boolean isCarrierServicePackage(int phoneId, String pkgName) {
        if (pkgName.equals(getDefaultCarrierServicePackageName())) {
            return false;
        }
        List<String> carrierPackageNames = TelephonyManager.from(mContext).getCarrierPackageNamesForIntentAndPhone(new Intent("android.service.carrier.CarrierService"), phoneId);
        logd("Carrier Packages For Subscription = " + carrierPackageNames);
        if (carrierPackageNames == null || !carrierPackageNames.contains(pkgName)) {
            return false;
        }
        return true;
    }

    @VisibleForTesting
    public void updateSubscriptionByCarrierConfig(int phoneId, String configPackageName, PersistableBundle config) {
        if (!SubscriptionManager.isValidPhoneId(phoneId) || TextUtils.isEmpty(configPackageName) || config == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("In updateSubscriptionByCarrierConfig(): phoneId=");
            sb.append(phoneId);
            sb.append(" configPackageName=");
            sb.append(configPackageName);
            sb.append(" config=");
            sb.append(config == null ? "null" : Integer.valueOf(config.hashCode()));
            logd(sb.toString());
            return;
        }
        SubscriptionController sc = SubscriptionController.getInstance();
        if (sc == null) {
            loge("SubscriptionController was null");
            return;
        }
        int currentSubId = sc.getSubIdUsingPhoneId(phoneId);
        if (!SubscriptionManager.isValidSubscriptionId(currentSubId) || currentSubId == Integer.MAX_VALUE) {
            logd("No subscription is active for phone being updated");
            return;
        }
        SubscriptionInfo currentSubInfo = sc.getSubscriptionInfo(currentSubId);
        if (currentSubInfo == null) {
            loge("Couldn't retrieve subscription info for current subscription");
        } else if (!isCarrierServicePackage(phoneId, configPackageName)) {
            loge("Cannot manage subId=" + currentSubId + ", carrierPackage=" + configPackageName);
        } else {
            ContentValues cv = new ContentValues();
            boolean isOpportunistic = config.getBoolean("is_opportunistic_subscription_bool", false);
            if (currentSubInfo.isOpportunistic() != isOpportunistic) {
                logd("Set SubId=" + currentSubId + " isOpportunistic=" + isOpportunistic);
                cv.put("is_opportunistic", isOpportunistic ? ProxyController.MODEM_1 : ProxyController.MODEM_0);
            }
            String groupUuidString = config.getString("subscription_group_uuid_string", "");
            ParcelUuid groupUuid = null;
            if (!TextUtils.isEmpty(groupUuidString)) {
                try {
                    groupUuid = ParcelUuid.fromString(groupUuidString);
                    if (groupUuid.equals(REMOVE_GROUP_UUID) && currentSubInfo.getGroupUuid() != null) {
                        cv.put("group_uuid", (String) null);
                        logd("Group Removed for" + currentSubId);
                    } else if (SubscriptionController.getInstance().canPackageManageGroup(groupUuid, configPackageName)) {
                        cv.put("group_uuid", groupUuid.toString());
                        cv.put("group_owner", configPackageName);
                        logd("Group Added for" + currentSubId);
                    } else {
                        loge("configPackageName " + configPackageName + " doesn't own grouUuid " + groupUuid);
                    }
                } catch (IllegalArgumentException e) {
                    loge("Invalid Group UUID=" + groupUuidString);
                }
            }
            if (cv.size() > 0 && mContext.getContentResolver().update(SubscriptionManager.getUriForSubscriptionId(currentSubId), cv, null, null) > 0) {
                sc.refreshCachedActiveSubscriptionInfoList();
                sc.notifySubscriptionInfoChanged();
                MultiSimSettingController.getInstance().notifySubscriptionGroupChanged(groupUuid);
            }
        }
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

    @UnsupportedAppUsage
    private void broadcastSimStateChanged(int slotId, String state, String reason) {
    }

    private void broadcastSimCardStateChanged(int phoneId, int state) {
        int[] iArr = sSimCardState;
        if (state != iArr[phoneId]) {
            iArr[phoneId] = state;
            Intent i = new Intent("android.telephony.action.SIM_CARD_STATE_CHANGED");
            i.addFlags(67108864);
            i.addFlags(16777216);
            i.putExtra("android.telephony.extra.SIM_STATE", state);
            SubscriptionManager.putPhoneIdAndSubIdExtra(i, phoneId);
            int slotId = UiccController.getInstance().getSlotIdFromPhoneId(phoneId);
            i.putExtra("slot", slotId);
            logd("Broadcasting intent ACTION_SIM_CARD_STATE_CHANGED " + simStateString(state) + " for phone: " + phoneId + " slot: " + slotId);
            mContext.sendBroadcast(i, "android.permission.READ_PRIVILEGED_PHONE_STATE");
            TelephonyMetrics.getInstance().updateSimState(phoneId, state);
        }
    }

    private void broadcastSimApplicationStateChanged(int phoneId, int state) {
        int[] iArr = sSimApplicationState;
        if (state == iArr[phoneId]) {
            return;
        }
        if (state != 6 || iArr[phoneId] != 0) {
            sSimApplicationState[phoneId] = state;
            Intent i = new Intent("android.telephony.action.SIM_APPLICATION_STATE_CHANGED");
            i.addFlags(16777216);
            i.addFlags(67108864);
            i.putExtra("android.telephony.extra.SIM_STATE", state);
            SubscriptionManager.putPhoneIdAndSubIdExtra(i, phoneId);
            int slotId = UiccController.getInstance().getSlotIdFromPhoneId(phoneId);
            i.putExtra("slot", slotId);
            logd("Broadcasting intent ACTION_SIM_APPLICATION_STATE_CHANGED " + simStateString(state) + " for phone: " + phoneId + " slot: " + slotId);
            mContext.sendBroadcast(i, "android.permission.READ_PRIVILEGED_PHONE_STATE");
            TelephonyMetrics.getInstance().updateSimState(phoneId, state);
        }
    }

    private static String simStateString(int state) {
        switch (state) {
            case 0:
                return IccCardConstantsEx.INTENT_VALUE_ICC_UNKNOWN;
            case 1:
                return IccCardConstantsEx.INTENT_VALUE_ICC_ABSENT;
            case 2:
                return "PIN_REQUIRED";
            case 3:
                return "PUK_REQUIRED";
            case 4:
                return "NETWORK_LOCKED";
            case 5:
                return IccCardConstantsEx.INTENT_VALUE_ICC_READY;
            case 6:
                return IccCardConstantsEx.INTENT_VALUE_ICC_NOT_READY;
            case 7:
                return IccCardConstantsEx.INTENT_VALUE_ABSENT_ON_PERM_DISABLED;
            case 8:
                return IccCardConstantsEx.INTENT_VALUE_ICC_CARD_IO_ERROR;
            case 9:
                return IccCardConstantsEx.INTENT_VALUE_ICC_CARD_RESTRICTED;
            case 10:
                return IccCardConstantsEx.INTENT_VALUE_ICC_LOADED;
            case 11:
                return "PRESENT";
            default:
                return "INVALID";
        }
    }

    @UnsupportedAppUsage
    private static void logd(String message) {
        Rlog.i(LOG_TAG, message);
    }

    private static void loge(String message) {
        Rlog.e(LOG_TAG, message);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("SubscriptionInfoUpdater:");
        this.mCarrierServiceBindHelper.dump(fd, pw, args);
    }

    @Override // com.android.internal.telephony.ISubscriptionInfoUpdaterInner
    public String[] getIccIdHw() {
        return mIccId;
    }

    @Override // com.android.internal.telephony.ISubscriptionInfoUpdaterInner
    public void resetIccid(int slotId) {
        if (slotId < 0 || slotId >= mIccId.length) {
            logd("resetIccid: invaild slotid =" + slotId);
            return;
        }
        logd("resetIccid: set iccid is null");
        mIccId[slotId] = null;
    }

    @Override // com.android.internal.telephony.ISubscriptionInfoUpdaterInner
    public boolean isAllIccIdQueryDoneHw() {
        return isAllIccIdQueryDone();
    }

    @Override // com.android.internal.telephony.ISubscriptionInfoUpdaterInner
    public void updateSubscriptionInfoByIccIdHw(int slotIndex, boolean isUpdateEmbeddedSubs) {
        updateSubscriptionInfoByIccId(slotIndex, isUpdateEmbeddedSubs);
    }

    public void cleanIccids() {
        for (int i = 0; i < mIccId.length; i++) {
            logd("clean iccids i=" + i);
            mIccId[i] = null;
        }
    }

    @Override // com.android.internal.telephony.ISubscriptionInfoUpdaterInner
    public void updateSubIdForNV(int slotId) {
        this.mHwSubscriptionInfoUpdaterEx.updateSubIdForNV(slotId);
    }

    @Override // com.android.internal.telephony.ISubscriptionInfoUpdaterInner
    public void updateEmbeddedSubscriptionsHw(List<Integer> cardIds) {
        updateEmbeddedSubscriptions(cardIds, $$Lambda$SubscriptionInfoUpdater$BOjocxHJGW4sp50CCGeQZ4s7Z8Y.INSTANCE);
    }

    static /* synthetic */ void lambda$updateEmbeddedSubscriptionsHw$7(boolean hasChanges) {
        if (hasChanges) {
            SubscriptionController.getInstance().notifySubscriptionInfoChanged();
        }
    }
}
