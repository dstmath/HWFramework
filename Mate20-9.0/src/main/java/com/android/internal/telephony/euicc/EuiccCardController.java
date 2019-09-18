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
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public EuiccController mEuiccController;
    /* access modifiers changed from: private */
    public Handler mEuiccMainThreadHandler;
    /* access modifiers changed from: private */
    public SimSlotStatusChangedBroadcastReceiver mSimSlotStatusChangeReceiver;
    private UiccController mUiccController;

    private class SimSlotStatusChangedBroadcastReceiver extends BroadcastReceiver {
        private SimSlotStatusChangedBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.telephony.action.SIM_SLOT_STATUS_CHANGED".equals(intent.getAction())) {
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

    /* JADX WARNING: type inference failed for: r3v0, types: [com.android.internal.telephony.euicc.EuiccCardController, android.os.IBinder] */
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
            if (slotInfo.isEuicc() && slotInfo.isActive()) {
                return true;
            }
        }
        return false;
    }

    private void checkCallingPackage(String callingPackage) {
        this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
        this.mCallingPackage = callingPackage;
        this.mBestComponent = EuiccConnector.findBestComponent(this.mContext.getPackageManager());
        if (this.mBestComponent == null || !TextUtils.equals(this.mCallingPackage, this.mBestComponent.packageName)) {
            throw new SecurityException("The calling package can only be LPA.");
        }
    }

    private EuiccCard getEuiccCard(String cardId) {
        UiccController controller = UiccController.getInstance();
        int slotId = controller.getUiccSlotForCardId(cardId);
        if (slotId != -1 && controller.getUiccSlot(slotId).isEuicc()) {
            return (EuiccCard) controller.getUiccCardForSlot(slotId);
        }
        loge("EuiccCard is null. CardId : " + cardId);
        return null;
    }

    /* access modifiers changed from: private */
    public int getResultCode(Throwable e) {
        if (e instanceof EuiccCardErrorException) {
            return ((EuiccCardErrorException) e).getErrorCode();
        }
        return -1;
    }

    public void getAllProfiles(String callingPackage, String cardId, final IGetAllProfilesCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2, null);
            } catch (RemoteException exception) {
                loge("getAllProfiles callback failure.", exception);
            }
            return;
        }
        card.getAllProfiles(new AsyncResultCallback<EuiccProfileInfo[]>() {
            public void onResult(EuiccProfileInfo[] result) {
                try {
                    callback.onComplete(0, result);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("getAllProfiles callback failure.", exception);
                }
            }

            public void onException(Throwable e) {
                try {
                    EuiccCardController.loge("getAllProfiles callback onException: ", e);
                    callback.onComplete(EuiccCardController.this.getResultCode(e), null);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("getAllProfiles callback failure.", exception);
                }
            }
        }, this.mEuiccMainThreadHandler);
    }

    public void getProfile(String callingPackage, String cardId, String iccid, final IGetProfileCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2, null);
            } catch (RemoteException exception) {
                loge("getProfile callback failure.", exception);
            }
            return;
        }
        card.getProfile(iccid, new AsyncResultCallback<EuiccProfileInfo>() {
            public void onResult(EuiccProfileInfo result) {
                try {
                    callback.onComplete(0, result);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("getProfile callback failure.", exception);
                }
            }

            public void onException(Throwable e) {
                try {
                    EuiccCardController.loge("getProfile callback onException: ", e);
                    callback.onComplete(EuiccCardController.this.getResultCode(e), null);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("getProfile callback failure.", exception);
                }
            }
        }, this.mEuiccMainThreadHandler);
    }

    public void disableProfile(String callingPackage, String cardId, String iccid, boolean refresh, final IDisableProfileCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2);
            } catch (RemoteException exception) {
                loge("disableProfile callback failure.", exception);
            }
            return;
        }
        card.disableProfile(iccid, refresh, new AsyncResultCallback<Void>() {
            public void onResult(Void result) {
                try {
                    callback.onComplete(0);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("disableProfile callback failure.", exception);
                }
            }

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

    public void switchToProfile(String callingPackage, String cardId, String iccid, boolean refresh, ISwitchToProfileCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2, null);
            } catch (RemoteException exception) {
                loge("switchToProfile callback failure.", exception);
            }
            return;
        }
        final ISwitchToProfileCallback iSwitchToProfileCallback = callback;
        final EuiccCard euiccCard = card;
        final String str = iccid;
        final boolean z = refresh;
        AnonymousClass4 r0 = new AsyncResultCallback<EuiccProfileInfo>() {
            public void onResult(final EuiccProfileInfo profile) {
                euiccCard.switchToProfile(str, z, new AsyncResultCallback<Void>() {
                    public void onResult(Void result) {
                        try {
                            iSwitchToProfileCallback.onComplete(0, profile);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("switchToProfile callback failure.", exception);
                        }
                    }

                    public void onException(Throwable e) {
                        try {
                            EuiccCardController.loge("switchToProfile callback onException: ", e);
                            iSwitchToProfileCallback.onComplete(EuiccCardController.this.getResultCode(e), profile);
                        } catch (RemoteException exception) {
                            EuiccCardController.loge("switchToProfile callback failure.", exception);
                        }
                    }
                }, EuiccCardController.this.mEuiccMainThreadHandler);
            }

            public void onException(Throwable e) {
                try {
                    EuiccCardController.loge("getProfile in switchToProfile callback onException: ", e);
                    iSwitchToProfileCallback.onComplete(EuiccCardController.this.getResultCode(e), null);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("switchToProfile callback failure.", exception);
                }
            }
        };
        card.getProfile(iccid, r0, this.mEuiccMainThreadHandler);
    }

    public void setNickname(String callingPackage, String cardId, String iccid, String nickname, final ISetNicknameCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2);
            } catch (RemoteException exception) {
                loge("setNickname callback failure.", exception);
            }
            return;
        }
        card.setNickname(iccid, nickname, new AsyncResultCallback<Void>() {
            public void onResult(Void result) {
                try {
                    callback.onComplete(0);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("setNickname callback failure.", exception);
                }
            }

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

    public void deleteProfile(String callingPackage, String cardId, String iccid, final IDeleteProfileCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2);
            } catch (RemoteException exception) {
                loge("deleteProfile callback failure.", exception);
            }
            return;
        }
        card.deleteProfile(iccid, new AsyncResultCallback<Void>() {
            public void onResult(Void result) {
                Log.i(EuiccCardController.TAG, "Request subscription info list refresh after delete.");
                SubscriptionController.getInstance().requestEmbeddedSubscriptionInfoListRefresh();
                try {
                    callback.onComplete(0);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("deleteProfile callback failure.", exception);
                }
            }

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

    public void resetMemory(String callingPackage, String cardId, int options, final IResetMemoryCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2);
            } catch (RemoteException exception) {
                loge("resetMemory callback failure.", exception);
            }
            return;
        }
        card.resetMemory(options, new AsyncResultCallback<Void>() {
            public void onResult(Void result) {
                Log.i(EuiccCardController.TAG, "Request subscription info list refresh after reset memory.");
                SubscriptionController.getInstance().requestEmbeddedSubscriptionInfoListRefresh();
                try {
                    callback.onComplete(0);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("resetMemory callback failure.", exception);
                }
            }

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

    public void getDefaultSmdpAddress(String callingPackage, String cardId, final IGetDefaultSmdpAddressCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2, null);
            } catch (RemoteException exception) {
                loge("getDefaultSmdpAddress callback failure.", exception);
            }
            return;
        }
        card.getDefaultSmdpAddress(new AsyncResultCallback<String>() {
            public void onResult(String result) {
                try {
                    callback.onComplete(0, result);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("getDefaultSmdpAddress callback failure.", exception);
                }
            }

            public void onException(Throwable e) {
                try {
                    EuiccCardController.loge("getDefaultSmdpAddress callback onException: ", e);
                    callback.onComplete(EuiccCardController.this.getResultCode(e), null);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("getDefaultSmdpAddress callback failure.", exception);
                }
            }
        }, this.mEuiccMainThreadHandler);
    }

    public void getSmdsAddress(String callingPackage, String cardId, final IGetSmdsAddressCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2, null);
            } catch (RemoteException exception) {
                loge("getSmdsAddress callback failure.", exception);
            }
            return;
        }
        card.getSmdsAddress(new AsyncResultCallback<String>() {
            public void onResult(String result) {
                try {
                    callback.onComplete(0, result);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("getSmdsAddress callback failure.", exception);
                }
            }

            public void onException(Throwable e) {
                try {
                    EuiccCardController.loge("getSmdsAddress callback onException: ", e);
                    callback.onComplete(EuiccCardController.this.getResultCode(e), null);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("getSmdsAddress callback failure.", exception);
                }
            }
        }, this.mEuiccMainThreadHandler);
    }

    public void setDefaultSmdpAddress(String callingPackage, String cardId, String address, final ISetDefaultSmdpAddressCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2);
            } catch (RemoteException exception) {
                loge("setDefaultSmdpAddress callback failure.", exception);
            }
            return;
        }
        card.setDefaultSmdpAddress(address, new AsyncResultCallback<Void>() {
            public void onResult(Void result) {
                try {
                    callback.onComplete(0);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("setDefaultSmdpAddress callback failure.", exception);
                }
            }

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

    public void getRulesAuthTable(String callingPackage, String cardId, final IGetRulesAuthTableCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2, null);
            } catch (RemoteException exception) {
                loge("getRulesAuthTable callback failure.", exception);
            }
            return;
        }
        card.getRulesAuthTable(new AsyncResultCallback<EuiccRulesAuthTable>() {
            public void onResult(EuiccRulesAuthTable result) {
                try {
                    callback.onComplete(0, result);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("getRulesAuthTable callback failure.", exception);
                }
            }

            public void onException(Throwable e) {
                try {
                    EuiccCardController.loge("getRulesAuthTable callback onException: ", e);
                    callback.onComplete(EuiccCardController.this.getResultCode(e), null);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("getRulesAuthTable callback failure.", exception);
                }
            }
        }, this.mEuiccMainThreadHandler);
    }

    public void getEuiccChallenge(String callingPackage, String cardId, final IGetEuiccChallengeCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2, null);
            } catch (RemoteException exception) {
                loge("getEuiccChallenge callback failure.", exception);
            }
            return;
        }
        card.getEuiccChallenge(new AsyncResultCallback<byte[]>() {
            public void onResult(byte[] result) {
                try {
                    callback.onComplete(0, result);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("getEuiccChallenge callback failure.", exception);
                }
            }

            public void onException(Throwable e) {
                try {
                    EuiccCardController.loge("getEuiccChallenge callback onException: ", e);
                    callback.onComplete(EuiccCardController.this.getResultCode(e), null);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("getEuiccChallenge callback failure.", exception);
                }
            }
        }, this.mEuiccMainThreadHandler);
    }

    public void getEuiccInfo1(String callingPackage, String cardId, final IGetEuiccInfo1Callback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2, null);
            } catch (RemoteException exception) {
                loge("getEuiccInfo1 callback failure.", exception);
            }
            return;
        }
        card.getEuiccInfo1(new AsyncResultCallback<byte[]>() {
            public void onResult(byte[] result) {
                try {
                    callback.onComplete(0, result);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("getEuiccInfo1 callback failure.", exception);
                }
            }

            public void onException(Throwable e) {
                try {
                    EuiccCardController.loge("getEuiccInfo1 callback onException: ", e);
                    callback.onComplete(EuiccCardController.this.getResultCode(e), null);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("getEuiccInfo1 callback failure.", exception);
                }
            }
        }, this.mEuiccMainThreadHandler);
    }

    public void getEuiccInfo2(String callingPackage, String cardId, final IGetEuiccInfo2Callback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2, null);
            } catch (RemoteException exception) {
                loge("getEuiccInfo2 callback failure.", exception);
            }
            return;
        }
        card.getEuiccInfo2(new AsyncResultCallback<byte[]>() {
            public void onResult(byte[] result) {
                try {
                    callback.onComplete(0, result);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("getEuiccInfo2 callback failure.", exception);
                }
            }

            public void onException(Throwable e) {
                try {
                    EuiccCardController.loge("getEuiccInfo2 callback onException: ", e);
                    callback.onComplete(EuiccCardController.this.getResultCode(e), null);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("getEuiccInfo2 callback failure.", exception);
                }
            }
        }, this.mEuiccMainThreadHandler);
    }

    public void authenticateServer(String callingPackage, String cardId, String matchingId, byte[] serverSigned1, byte[] serverSignature1, byte[] euiccCiPkIdToBeUsed, byte[] serverCertificate, IAuthenticateServerCallback callback) {
        final IAuthenticateServerCallback iAuthenticateServerCallback = callback;
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                iAuthenticateServerCallback.onComplete(-2, null);
            } catch (RemoteException exception) {
                RemoteException remoteException = exception;
                loge("authenticateServer callback failure.", exception);
            }
            return;
        }
        card.authenticateServer(matchingId, serverSigned1, serverSignature1, euiccCiPkIdToBeUsed, serverCertificate, new AsyncResultCallback<byte[]>() {
            public void onResult(byte[] result) {
                try {
                    iAuthenticateServerCallback.onComplete(0, result);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("authenticateServer callback failure.", exception);
                }
            }

            public void onException(Throwable e) {
                try {
                    EuiccCardController.loge("authenticateServer callback onException: ", e);
                    iAuthenticateServerCallback.onComplete(EuiccCardController.this.getResultCode(e), null);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("authenticateServer callback failure.", exception);
                }
            }
        }, this.mEuiccMainThreadHandler);
    }

    public void prepareDownload(String callingPackage, String cardId, byte[] hashCc, byte[] smdpSigned2, byte[] smdpSignature2, byte[] smdpCertificate, final IPrepareDownloadCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2, null);
            } catch (RemoteException exception) {
                loge("prepareDownload callback failure.", exception);
            }
            return;
        }
        card.prepareDownload(hashCc, smdpSigned2, smdpSignature2, smdpCertificate, new AsyncResultCallback<byte[]>() {
            public void onResult(byte[] result) {
                try {
                    callback.onComplete(0, result);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("prepareDownload callback failure.", exception);
                }
            }

            public void onException(Throwable e) {
                try {
                    EuiccCardController.loge("prepareDownload callback onException: ", e);
                    callback.onComplete(EuiccCardController.this.getResultCode(e), null);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("prepareDownload callback failure.", exception);
                }
            }
        }, this.mEuiccMainThreadHandler);
    }

    public void loadBoundProfilePackage(String callingPackage, String cardId, byte[] boundProfilePackage, final ILoadBoundProfilePackageCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2, null);
            } catch (RemoteException exception) {
                loge("loadBoundProfilePackage callback failure.", exception);
            }
            return;
        }
        card.loadBoundProfilePackage(boundProfilePackage, new AsyncResultCallback<byte[]>() {
            public void onResult(byte[] result) {
                Log.i(EuiccCardController.TAG, "Request subscription info list refresh after install.");
                SubscriptionController.getInstance().requestEmbeddedSubscriptionInfoListRefresh();
                try {
                    callback.onComplete(0, result);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("loadBoundProfilePackage callback failure.", exception);
                }
            }

            public void onException(Throwable e) {
                try {
                    EuiccCardController.loge("loadBoundProfilePackage callback onException: ", e);
                    callback.onComplete(EuiccCardController.this.getResultCode(e), null);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("loadBoundProfilePackage callback failure.", exception);
                }
            }
        }, this.mEuiccMainThreadHandler);
    }

    public void cancelSession(String callingPackage, String cardId, byte[] transactionId, int reason, final ICancelSessionCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2, null);
            } catch (RemoteException exception) {
                loge("cancelSession callback failure.", exception);
            }
            return;
        }
        card.cancelSession(transactionId, reason, new AsyncResultCallback<byte[]>() {
            public void onResult(byte[] result) {
                try {
                    callback.onComplete(0, result);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("cancelSession callback failure.", exception);
                }
            }

            public void onException(Throwable e) {
                try {
                    EuiccCardController.loge("cancelSession callback onException: ", e);
                    callback.onComplete(EuiccCardController.this.getResultCode(e), null);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("cancelSession callback failure.", exception);
                }
            }
        }, this.mEuiccMainThreadHandler);
    }

    public void listNotifications(String callingPackage, String cardId, int events, final IListNotificationsCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2, null);
            } catch (RemoteException exception) {
                loge("listNotifications callback failure.", exception);
            }
            return;
        }
        card.listNotifications(events, new AsyncResultCallback<EuiccNotification[]>() {
            public void onResult(EuiccNotification[] result) {
                try {
                    callback.onComplete(0, result);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("listNotifications callback failure.", exception);
                }
            }

            public void onException(Throwable e) {
                try {
                    EuiccCardController.loge("listNotifications callback onException: ", e);
                    callback.onComplete(EuiccCardController.this.getResultCode(e), null);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("listNotifications callback failure.", exception);
                }
            }
        }, this.mEuiccMainThreadHandler);
    }

    public void retrieveNotificationList(String callingPackage, String cardId, int events, final IRetrieveNotificationListCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2, null);
            } catch (RemoteException exception) {
                loge("retrieveNotificationList callback failure.", exception);
            }
            return;
        }
        card.retrieveNotificationList(events, new AsyncResultCallback<EuiccNotification[]>() {
            public void onResult(EuiccNotification[] result) {
                try {
                    callback.onComplete(0, result);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("retrieveNotificationList callback failure.", exception);
                }
            }

            public void onException(Throwable e) {
                try {
                    EuiccCardController.loge("retrieveNotificationList callback onException: ", e);
                    callback.onComplete(EuiccCardController.this.getResultCode(e), null);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("retrieveNotificationList callback failure.", exception);
                }
            }
        }, this.mEuiccMainThreadHandler);
    }

    public void retrieveNotification(String callingPackage, String cardId, int seqNumber, final IRetrieveNotificationCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2, null);
            } catch (RemoteException exception) {
                loge("retrieveNotification callback failure.", exception);
            }
            return;
        }
        card.retrieveNotification(seqNumber, new AsyncResultCallback<EuiccNotification>() {
            public void onResult(EuiccNotification result) {
                try {
                    callback.onComplete(0, result);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("retrieveNotification callback failure.", exception);
                }
            }

            public void onException(Throwable e) {
                try {
                    EuiccCardController.loge("retrieveNotification callback onException: ", e);
                    callback.onComplete(EuiccCardController.this.getResultCode(e), null);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("retrieveNotification callback failure.", exception);
                }
            }
        }, this.mEuiccMainThreadHandler);
    }

    public void removeNotificationFromList(String callingPackage, String cardId, int seqNumber, final IRemoveNotificationFromListCallback callback) {
        checkCallingPackage(callingPackage);
        EuiccCard card = getEuiccCard(cardId);
        if (card == null) {
            try {
                callback.onComplete(-2);
            } catch (RemoteException exception) {
                loge("removeNotificationFromList callback failure.", exception);
            }
            return;
        }
        card.removeNotificationFromList(seqNumber, new AsyncResultCallback<Void>() {
            public void onResult(Void result) {
                try {
                    callback.onComplete(0);
                } catch (RemoteException exception) {
                    EuiccCardController.loge("removeNotificationFromList callback failure.", exception);
                }
            }

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
