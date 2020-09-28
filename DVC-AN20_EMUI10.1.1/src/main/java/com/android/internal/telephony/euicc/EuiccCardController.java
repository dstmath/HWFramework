package com.android.internal.telephony.euicc;

import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ComponentInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.service.euicc.EuiccProfileInfo;
import android.telephony.euicc.EuiccNotification;
import android.telephony.euicc.EuiccRulesAuthTable;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.euicc.IEuiccCardController;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccSlot;
import com.android.internal.telephony.uicc.euicc.EuiccCard;
import com.android.internal.telephony.uicc.euicc.EuiccCardErrorException;
import com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class EuiccCardController extends IEuiccCardController.Stub {
    private static final String KEY_LAST_BOOT_COUNT = "last_boot_count";
    private static final String TAG = "EuiccCardController";
    private static EuiccCardController sInstance;
    private AppOpsManager mAppOps;
    private ComponentInfo mBestComponent;
    private String mCallingPackage;
    private final Context mContext;
    private EuiccController mEuiccController;
    private Handler mEuiccMainThreadHandler;
    private SimSlotStatusChangedBroadcastReceiver mSimSlotStatusChangeReceiver;
    private UiccController mUiccController;

    /* access modifiers changed from: private */
    public class SimSlotStatusChangedBroadcastReceiver extends BroadcastReceiver {
        private SimSlotStatusChangedBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.telephony.action.SIM_SLOT_STATUS_CHANGED".equals(intent.getAction()) && EuiccCardController.this.isEmbeddedCardPresent()) {
                if (EuiccCardController.this.isEmbeddedSlotActivated()) {
                    EuiccCardController.this.mEuiccController.startOtaUpdatingIfNecessary();
                }
                EuiccCardController.this.mContext.unregisterReceiver(EuiccCardController.this.mSimSlotStatusChangeReceiver);
            }
        }
    }

    public static EuiccCardController init(Context context) {
        synchronized (EuiccCardController.class) {
            if (sInstance == null) {
                sInstance = new EuiccCardController(context);
            } else {
                Log.wtf(TAG, "init() called multiple times! sInstance = " + sInstance);
            }
        }
        return sInstance;
    }

    public static EuiccCardController get() {
        if (sInstance == null) {
            synchronized (EuiccCardController.class) {
                if (sInstance == null) {
                    throw new IllegalStateException("get() called before init()");
                }
            }
        }
        return sInstance;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.internal.telephony.euicc.EuiccCardController */
    /* JADX WARN: Multi-variable type inference failed */
    private EuiccCardController(Context context) {
        this(context, new Handler(), EuiccController.get(), UiccController.getInstance());
        ServiceManager.addService("euicc_card_controller", this);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public EuiccCardController(Context context, Handler handler, EuiccController euiccController, UiccController uiccController) {
        this.mContext = context;
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mEuiccMainThreadHandler = handler;
        this.mUiccController = uiccController;
        this.mEuiccController = euiccController;
        if (isBootUp(this.mContext)) {
            this.mSimSlotStatusChangeReceiver = new SimSlotStatusChangedBroadcastReceiver();
            this.mContext.registerReceiver(this.mSimSlotStatusChangeReceiver, new IntentFilter("android.telephony.action.SIM_SLOT_STATUS_CHANGED"));
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public static boolean isBootUp(Context context) {
        int bootCount = Settings.Global.getInt(context.getContentResolver(), "boot_count", -1);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int lastBootCount = sp.getInt(KEY_LAST_BOOT_COUNT, -1);
        if (bootCount != -1 && lastBootCount != -1 && bootCount == lastBootCount) {
            return false;
        }
        sp.edit().putInt(KEY_LAST_BOOT_COUNT, bootCount).apply();
        return true;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public boolean isEmbeddedSlotActivated() {
        UiccSlot[] slots = this.mUiccController.getUiccSlots();
        if (slots == null) {
            return false;
        }
        for (UiccSlot slotInfo : slots) {
            if (!(slotInfo == null || slotInfo.isRemovable() || !slotInfo.isActive())) {
                return true;
            }
        }
        return false;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public boolean isEmbeddedCardPresent() {
        UiccSlot[] slots = this.mUiccController.getUiccSlots();
        if (slots == null) {
            return false;
        }
        for (UiccSlot slotInfo : slots) {
            if (!(slotInfo == null || slotInfo.isRemovable() || slotInfo.getCardState() == null || !slotInfo.getCardState().isCardPresent())) {
                return true;
            }
        }
        return false;
    }

    private void checkCallingPackage(String callingPackage) {
        this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
        this.mCallingPackage = callingPackage;
        this.mBestComponent = EuiccConnector.findBestComponent(this.mContext.getPackageManager());
        ComponentInfo componentInfo = this.mBestComponent;
        if (componentInfo == null || !TextUtils.equals(this.mCallingPackage, componentInfo.packageName)) {
            throw new SecurityException("The calling package can only be LPA.");
        }
    }

    private EuiccCard getEuiccCard(String cardId) {
        UiccController controller = UiccController.getInstance();
        int slotId = controller.getUiccSlotForCardId(cardId);
        if (slotId != -1 && controller.getUiccSlot(slotId).isEuicc()) {
            return (EuiccCard) controller.getUiccCardForSlot(slotId);
        }
        loge("EuiccCard is null. CardId : " + givePrintableCardId(cardId));
        return null;
    }

    private static String givePrintableCardId(String cardId) {
        if (cardId == null) {
            return null;
        }
        if (cardId.length() <= 9) {
            return cardId;
        }
        return cardId.substring(0, 9) + "XXXXXXXXXXXXXXXXXXXXXX";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getResultCode(Throwable e) {
        if (e instanceof EuiccCardErrorException) {
            return ((EuiccCardErrorException) e).getErrorCode();
        }
        return -1;
    }

    public void getAllProfiles(String callingPackage, String cardId, final IGetAllProfilesCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2, (EuiccProfileInfo[]) null);
                } catch (RemoteException exception) {
                    loge("getAllProfiles callback failure.", exception);
                }
            } else {
                card.getAllProfiles(new AsyncResultCallback<EuiccProfileInfo[]>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass1 */

                    public void onResult(EuiccProfileInfo[] result) {
                        try {
                            callback.onComplete(0, result);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("getAllProfiles callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("getAllProfiles callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e), (EuiccProfileInfo[]) null);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("getAllProfiles callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3, (EuiccProfileInfo[]) null);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void getProfile(String callingPackage, String cardId, String iccid, final IGetProfileCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2, (EuiccProfileInfo) null);
                } catch (RemoteException exception) {
                    loge("getProfile callback failure.", exception);
                }
            } else {
                card.getProfile(iccid, new AsyncResultCallback<EuiccProfileInfo>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass2 */

                    public void onResult(EuiccProfileInfo result) {
                        try {
                            callback.onComplete(0, result);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("getProfile callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("getProfile callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e), (EuiccProfileInfo) null);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("getProfile callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3, (EuiccProfileInfo) null);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void disableProfile(String callingPackage, String cardId, String iccid, boolean refresh, final IDisableProfileCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2);
                } catch (RemoteException exception) {
                    loge("disableProfile callback failure.", exception);
                }
            } else {
                card.disableProfile(iccid, refresh, new AsyncResultCallback<Void>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass3 */

                    public void onResult(Void result) {
                        try {
                            callback.onComplete(0);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("disableProfile callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("disableProfile callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e));
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("disableProfile callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void switchToProfile(String callingPackage, String cardId, final String iccid, final boolean refresh, final ISwitchToProfileCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            final EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2, (EuiccProfileInfo) null);
                } catch (RemoteException exception) {
                    loge("switchToProfile callback failure.", exception);
                }
            } else {
                card.getProfile(iccid, new AsyncResultCallback<EuiccProfileInfo>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass4 */

                    public void onResult(final EuiccProfileInfo profile) {
                        card.switchToProfile(iccid, refresh, new AsyncResultCallback<Void>() {
                            /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass4.AnonymousClass1 */

                            public void onResult(Void result) {
                                try {
                                    callback.onComplete(0, profile);
                                } catch (RemoteException exception) {
                                    EuiccCardController.loge("switchToProfile callback failure.", exception);
                                }
                            }

                            @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                            public void onException(Throwable e) {
                                try {
                                    EuiccCardController.loge("switchToProfile callback onException: ", e);
                                    callback.onComplete(EuiccCardController.this.getResultCode(e), profile);
                                } catch (RemoteException exception) {
                                    EuiccCardController.loge("switchToProfile callback failure.", exception);
                                }
                            }
                        }, EuiccCardController.this.mEuiccMainThreadHandler);
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("getProfile in switchToProfile callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e), (EuiccProfileInfo) null);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("switchToProfile callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3, (EuiccProfileInfo) null);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void setNickname(String callingPackage, String cardId, String iccid, String nickname, final ISetNicknameCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2);
                } catch (RemoteException exception) {
                    loge("setNickname callback failure.", exception);
                }
            } else {
                card.setNickname(iccid, nickname, new AsyncResultCallback<Void>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass5 */

                    public void onResult(Void result) {
                        try {
                            callback.onComplete(0);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("setNickname callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("setNickname callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e));
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("setNickname callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void deleteProfile(String callingPackage, final String cardId, String iccid, final IDeleteProfileCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2);
                } catch (RemoteException exception) {
                    loge("deleteProfile callback failure.", exception);
                }
            } else {
                card.deleteProfile(iccid, new AsyncResultCallback<Void>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass6 */

                    public void onResult(Void result) {
                        Log.i(EuiccCardController.TAG, "Request subscription info list refresh after delete.");
                        SubscriptionController.getInstance().requestEmbeddedSubscriptionInfoListRefresh(EuiccCardController.this.mUiccController.convertToPublicCardId(cardId));
                        try {
                            callback.onComplete(0);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("deleteProfile callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("deleteProfile callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e));
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("deleteProfile callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void resetMemory(String callingPackage, final String cardId, int options, final IResetMemoryCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2);
                } catch (RemoteException exception) {
                    loge("resetMemory callback failure.", exception);
                }
            } else {
                card.resetMemory(options, new AsyncResultCallback<Void>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass7 */

                    public void onResult(Void result) {
                        Log.i(EuiccCardController.TAG, "Request subscription info list refresh after reset memory.");
                        SubscriptionController.getInstance().requestEmbeddedSubscriptionInfoListRefresh(EuiccCardController.this.mUiccController.convertToPublicCardId(cardId));
                        try {
                            callback.onComplete(0);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("resetMemory callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("resetMemory callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e));
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("resetMemory callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void getDefaultSmdpAddress(String callingPackage, String cardId, final IGetDefaultSmdpAddressCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2, (String) null);
                } catch (RemoteException exception) {
                    loge("getDefaultSmdpAddress callback failure.", exception);
                }
            } else {
                card.getDefaultSmdpAddress(new AsyncResultCallback<String>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass8 */

                    public void onResult(String result) {
                        try {
                            callback.onComplete(0, result);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("getDefaultSmdpAddress callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("getDefaultSmdpAddress callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e), (String) null);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("getDefaultSmdpAddress callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3, (String) null);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void getSmdsAddress(String callingPackage, String cardId, final IGetSmdsAddressCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2, (String) null);
                } catch (RemoteException exception) {
                    loge("getSmdsAddress callback failure.", exception);
                }
            } else {
                card.getSmdsAddress(new AsyncResultCallback<String>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass9 */

                    public void onResult(String result) {
                        try {
                            callback.onComplete(0, result);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("getSmdsAddress callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("getSmdsAddress callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e), (String) null);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("getSmdsAddress callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3, (String) null);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void setDefaultSmdpAddress(String callingPackage, String cardId, String address, final ISetDefaultSmdpAddressCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2);
                } catch (RemoteException exception) {
                    loge("setDefaultSmdpAddress callback failure.", exception);
                }
            } else {
                card.setDefaultSmdpAddress(address, new AsyncResultCallback<Void>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass10 */

                    public void onResult(Void result) {
                        try {
                            callback.onComplete(0);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("setDefaultSmdpAddress callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("setDefaultSmdpAddress callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e));
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("setDefaultSmdpAddress callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void getRulesAuthTable(String callingPackage, String cardId, final IGetRulesAuthTableCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2, (EuiccRulesAuthTable) null);
                } catch (RemoteException exception) {
                    loge("getRulesAuthTable callback failure.", exception);
                }
            } else {
                card.getRulesAuthTable(new AsyncResultCallback<EuiccRulesAuthTable>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass11 */

                    public void onResult(EuiccRulesAuthTable result) {
                        try {
                            callback.onComplete(0, result);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("getRulesAuthTable callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("getRulesAuthTable callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e), (EuiccRulesAuthTable) null);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("getRulesAuthTable callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3, (EuiccRulesAuthTable) null);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void getEuiccChallenge(String callingPackage, String cardId, final IGetEuiccChallengeCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2, (byte[]) null);
                } catch (RemoteException exception) {
                    loge("getEuiccChallenge callback failure.", exception);
                }
            } else {
                card.getEuiccChallenge(new AsyncResultCallback<byte[]>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass12 */

                    public void onResult(byte[] result) {
                        try {
                            callback.onComplete(0, result);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("getEuiccChallenge callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("getEuiccChallenge callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e), (byte[]) null);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("getEuiccChallenge callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3, (byte[]) null);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void getEuiccInfo1(String callingPackage, String cardId, final IGetEuiccInfo1Callback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2, (byte[]) null);
                } catch (RemoteException exception) {
                    loge("getEuiccInfo1 callback failure.", exception);
                }
            } else {
                card.getEuiccInfo1(new AsyncResultCallback<byte[]>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass13 */

                    public void onResult(byte[] result) {
                        try {
                            callback.onComplete(0, result);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("getEuiccInfo1 callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("getEuiccInfo1 callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e), (byte[]) null);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("getEuiccInfo1 callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3, (byte[]) null);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void getEuiccInfo2(String callingPackage, String cardId, final IGetEuiccInfo2Callback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2, (byte[]) null);
                } catch (RemoteException exception) {
                    loge("getEuiccInfo2 callback failure.", exception);
                }
            } else {
                card.getEuiccInfo2(new AsyncResultCallback<byte[]>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass14 */

                    public void onResult(byte[] result) {
                        try {
                            callback.onComplete(0, result);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("getEuiccInfo2 callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("getEuiccInfo2 callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e), (byte[]) null);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("getEuiccInfo2 callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3, (byte[]) null);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void authenticateServer(String callingPackage, String cardId, String matchingId, byte[] serverSigned1, byte[] serverSignature1, byte[] euiccCiPkIdToBeUsed, byte[] serverCertificate, final IAuthenticateServerCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2, (byte[]) null);
                } catch (RemoteException exception) {
                    loge("authenticateServer callback failure.", exception);
                }
            } else {
                card.authenticateServer(matchingId, serverSigned1, serverSignature1, euiccCiPkIdToBeUsed, serverCertificate, new AsyncResultCallback<byte[]>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass15 */

                    public void onResult(byte[] result) {
                        try {
                            callback.onComplete(0, result);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("authenticateServer callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("authenticateServer callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e), (byte[]) null);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("authenticateServer callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3, (byte[]) null);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void prepareDownload(String callingPackage, String cardId, byte[] hashCc, byte[] smdpSigned2, byte[] smdpSignature2, byte[] smdpCertificate, final IPrepareDownloadCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2, (byte[]) null);
                } catch (RemoteException exception) {
                    loge("prepareDownload callback failure.", exception);
                }
            } else {
                card.prepareDownload(hashCc, smdpSigned2, smdpSignature2, smdpCertificate, new AsyncResultCallback<byte[]>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass16 */

                    public void onResult(byte[] result) {
                        try {
                            callback.onComplete(0, result);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("prepareDownload callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("prepareDownload callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e), (byte[]) null);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("prepareDownload callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3, (byte[]) null);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void loadBoundProfilePackage(String callingPackage, final String cardId, byte[] boundProfilePackage, final ILoadBoundProfilePackageCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2, (byte[]) null);
                } catch (RemoteException exception) {
                    loge("loadBoundProfilePackage callback failure.", exception);
                }
            } else {
                card.loadBoundProfilePackage(boundProfilePackage, new AsyncResultCallback<byte[]>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass17 */

                    public void onResult(byte[] result) {
                        Log.i(EuiccCardController.TAG, "Request subscription info list refresh after install.");
                        SubscriptionController.getInstance().requestEmbeddedSubscriptionInfoListRefresh(EuiccCardController.this.mUiccController.convertToPublicCardId(cardId));
                        try {
                            callback.onComplete(0, result);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("loadBoundProfilePackage callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("loadBoundProfilePackage callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e), (byte[]) null);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("loadBoundProfilePackage callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3, (byte[]) null);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void cancelSession(String callingPackage, String cardId, byte[] transactionId, int reason, final ICancelSessionCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2, (byte[]) null);
                } catch (RemoteException exception) {
                    loge("cancelSession callback failure.", exception);
                }
            } else {
                card.cancelSession(transactionId, reason, new AsyncResultCallback<byte[]>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass18 */

                    public void onResult(byte[] result) {
                        try {
                            callback.onComplete(0, result);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("cancelSession callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("cancelSession callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e), (byte[]) null);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("cancelSession callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3, (byte[]) null);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void listNotifications(String callingPackage, String cardId, int events, final IListNotificationsCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2, (EuiccNotification[]) null);
                } catch (RemoteException exception) {
                    loge("listNotifications callback failure.", exception);
                }
            } else {
                card.listNotifications(events, new AsyncResultCallback<EuiccNotification[]>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass19 */

                    public void onResult(EuiccNotification[] result) {
                        try {
                            callback.onComplete(0, result);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("listNotifications callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("listNotifications callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e), (EuiccNotification[]) null);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("listNotifications callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3, (EuiccNotification[]) null);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void retrieveNotificationList(String callingPackage, String cardId, int events, final IRetrieveNotificationListCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2, (EuiccNotification[]) null);
                } catch (RemoteException exception) {
                    loge("retrieveNotificationList callback failure.", exception);
                }
            } else {
                card.retrieveNotificationList(events, new AsyncResultCallback<EuiccNotification[]>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass20 */

                    public void onResult(EuiccNotification[] result) {
                        try {
                            callback.onComplete(0, result);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("retrieveNotificationList callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("retrieveNotificationList callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e), (EuiccNotification[]) null);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("retrieveNotificationList callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3, (EuiccNotification[]) null);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void retrieveNotification(String callingPackage, String cardId, int seqNumber, final IRetrieveNotificationCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2, (EuiccNotification) null);
                } catch (RemoteException exception) {
                    loge("retrieveNotification callback failure.", exception);
                }
            } else {
                card.retrieveNotification(seqNumber, new AsyncResultCallback<EuiccNotification>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass21 */

                    public void onResult(EuiccNotification result) {
                        try {
                            callback.onComplete(0, result);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("retrieveNotification callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("retrieveNotification callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e), (EuiccNotification) null);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("retrieveNotification callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3, (EuiccNotification) null);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void removeNotificationFromList(String callingPackage, String cardId, int seqNumber, final IRemoveNotificationFromListCallback callback) {
        try {
            checkCallingPackage(callingPackage);
            EuiccCard card = getEuiccCard(cardId);
            if (card == null) {
                try {
                    callback.onComplete(-2);
                } catch (RemoteException exception) {
                    loge("removeNotificationFromList callback failure.", exception);
                }
            } else {
                card.removeNotificationFromList(seqNumber, new AsyncResultCallback<Void>() {
                    /* class com.android.internal.telephony.euicc.EuiccCardController.AnonymousClass22 */

                    public void onResult(Void result) {
                        try {
                            callback.onComplete(0);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("removeNotificationFromList callback failure.", exception);
                        }
                    }

                    @Override // com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback
                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("removeNotificationFromList callback onException: ", e);
                            callback.onComplete(EuiccCardController.this.getResultCode(e));
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("removeNotificationFromList callback failure.", exception);
                        }
                    }
                }, this.mEuiccMainThreadHandler);
            }
        } catch (SecurityException e) {
            try {
                callback.onComplete(-3);
            } catch (RemoteException re) {
                loge("callback onComplete failure after checkCallingPackage.", re);
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", "Requires DUMP");
        long token = Binder.clearCallingIdentity();
        EuiccCardController.super.dump(fd, pw, args);
        pw.println("mCallingPackage=" + this.mCallingPackage);
        pw.println("mBestComponent=" + this.mBestComponent);
        Binder.restoreCallingIdentity(token);
    }

    private static void loge(String message) {
        Log.e(TAG, message);
    }

    /* access modifiers changed from: private */
    public static void loge(String message, Throwable tr) {
        Log.e(TAG, message, tr);
    }
}
