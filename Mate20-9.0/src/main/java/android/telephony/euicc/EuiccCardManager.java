package android.telephony.euicc;

import android.annotation.SystemApi;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.euicc.EuiccProfileInfo;
import android.telephony.euicc.EuiccCardManager;
import android.util.Log;
import com.android.internal.telephony.euicc.IAuthenticateServerCallback;
import com.android.internal.telephony.euicc.ICancelSessionCallback;
import com.android.internal.telephony.euicc.IDeleteProfileCallback;
import com.android.internal.telephony.euicc.IDisableProfileCallback;
import com.android.internal.telephony.euicc.IEuiccCardController;
import com.android.internal.telephony.euicc.IGetAllProfilesCallback;
import com.android.internal.telephony.euicc.IGetDefaultSmdpAddressCallback;
import com.android.internal.telephony.euicc.IGetEuiccChallengeCallback;
import com.android.internal.telephony.euicc.IGetEuiccInfo1Callback;
import com.android.internal.telephony.euicc.IGetEuiccInfo2Callback;
import com.android.internal.telephony.euicc.IGetProfileCallback;
import com.android.internal.telephony.euicc.IGetRulesAuthTableCallback;
import com.android.internal.telephony.euicc.IGetSmdsAddressCallback;
import com.android.internal.telephony.euicc.IListNotificationsCallback;
import com.android.internal.telephony.euicc.ILoadBoundProfilePackageCallback;
import com.android.internal.telephony.euicc.IPrepareDownloadCallback;
import com.android.internal.telephony.euicc.IRemoveNotificationFromListCallback;
import com.android.internal.telephony.euicc.IResetMemoryCallback;
import com.android.internal.telephony.euicc.IRetrieveNotificationCallback;
import com.android.internal.telephony.euicc.IRetrieveNotificationListCallback;
import com.android.internal.telephony.euicc.ISetDefaultSmdpAddressCallback;
import com.android.internal.telephony.euicc.ISetNicknameCallback;
import com.android.internal.telephony.euicc.ISwitchToProfileCallback;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.Executor;

@SystemApi
public class EuiccCardManager {
    public static final int CANCEL_REASON_END_USER_REJECTED = 0;
    public static final int CANCEL_REASON_POSTPONED = 1;
    public static final int CANCEL_REASON_PPR_NOT_ALLOWED = 3;
    public static final int CANCEL_REASON_TIMEOUT = 2;
    public static final int RESET_OPTION_DELETE_FIELD_LOADED_TEST_PROFILES = 2;
    public static final int RESET_OPTION_DELETE_OPERATIONAL_PROFILES = 1;
    public static final int RESET_OPTION_RESET_DEFAULT_SMDP_ADDRESS = 4;
    public static final int RESULT_EUICC_NOT_FOUND = -2;
    public static final int RESULT_OK = 0;
    public static final int RESULT_UNKNOWN_ERROR = -1;
    private static final String TAG = "EuiccCardManager";
    private final Context mContext;

    @Retention(RetentionPolicy.SOURCE)
    public @interface CancelReason {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ResetOption {
    }

    public interface ResultCallback<T> {
        void onComplete(int i, T t);
    }

    public EuiccCardManager(Context context) {
        this.mContext = context;
    }

    private IEuiccCardController getIEuiccCardController() {
        return IEuiccCardController.Stub.asInterface(ServiceManager.getService("euicc_card_controller"));
    }

    public void requestAllProfiles(String cardId, final Executor executor, final ResultCallback<EuiccProfileInfo[]> callback) {
        try {
            getIEuiccCardController().getAllProfiles(this.mContext.getOpPackageName(), cardId, new IGetAllProfilesCallback.Stub() {
                public void onComplete(int resultCode, EuiccProfileInfo[] profiles) {
                    executor.execute(new Runnable(resultCode, profiles) {
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ EuiccProfileInfo[] f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, this.f$2);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling getAllProfiles", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void requestProfile(String cardId, String iccid, final Executor executor, final ResultCallback<EuiccProfileInfo> callback) {
        try {
            getIEuiccCardController().getProfile(this.mContext.getOpPackageName(), cardId, iccid, new IGetProfileCallback.Stub() {
                public void onComplete(int resultCode, EuiccProfileInfo profile) {
                    executor.execute(new Runnable(resultCode, profile) {
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ EuiccProfileInfo f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, this.f$2);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling getProfile", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void disableProfile(String cardId, String iccid, boolean refresh, final Executor executor, final ResultCallback<Void> callback) {
        try {
            getIEuiccCardController().disableProfile(this.mContext.getOpPackageName(), cardId, iccid, refresh, new IDisableProfileCallback.Stub() {
                public void onComplete(int resultCode) {
                    executor.execute(new Runnable(resultCode) {
                        private final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, null);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling disableProfile", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void switchToProfile(String cardId, String iccid, boolean refresh, final Executor executor, final ResultCallback<EuiccProfileInfo> callback) {
        try {
            getIEuiccCardController().switchToProfile(this.mContext.getOpPackageName(), cardId, iccid, refresh, new ISwitchToProfileCallback.Stub() {
                public void onComplete(int resultCode, EuiccProfileInfo profile) {
                    executor.execute(new Runnable(resultCode, profile) {
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ EuiccProfileInfo f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, this.f$2);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling switchToProfile", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void setNickname(String cardId, String iccid, String nickname, final Executor executor, final ResultCallback<Void> callback) {
        try {
            getIEuiccCardController().setNickname(this.mContext.getOpPackageName(), cardId, iccid, nickname, new ISetNicknameCallback.Stub() {
                public void onComplete(int resultCode) {
                    executor.execute(new Runnable(resultCode) {
                        private final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, null);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling setNickname", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void deleteProfile(String cardId, String iccid, final Executor executor, final ResultCallback<Void> callback) {
        try {
            getIEuiccCardController().deleteProfile(this.mContext.getOpPackageName(), cardId, iccid, new IDeleteProfileCallback.Stub() {
                public void onComplete(int resultCode) {
                    executor.execute(new Runnable(resultCode) {
                        private final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, null);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling deleteProfile", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void resetMemory(String cardId, int options, final Executor executor, final ResultCallback<Void> callback) {
        try {
            getIEuiccCardController().resetMemory(this.mContext.getOpPackageName(), cardId, options, new IResetMemoryCallback.Stub() {
                public void onComplete(int resultCode) {
                    executor.execute(new Runnable(resultCode) {
                        private final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, null);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling resetMemory", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void requestDefaultSmdpAddress(String cardId, final Executor executor, final ResultCallback<String> callback) {
        try {
            getIEuiccCardController().getDefaultSmdpAddress(this.mContext.getOpPackageName(), cardId, new IGetDefaultSmdpAddressCallback.Stub() {
                public void onComplete(int resultCode, String address) {
                    executor.execute(new Runnable(resultCode, address) {
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ String f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, this.f$2);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling getDefaultSmdpAddress", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void requestSmdsAddress(String cardId, final Executor executor, final ResultCallback<String> callback) {
        try {
            getIEuiccCardController().getSmdsAddress(this.mContext.getOpPackageName(), cardId, new IGetSmdsAddressCallback.Stub() {
                public void onComplete(int resultCode, String address) {
                    executor.execute(new Runnable(resultCode, address) {
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ String f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, this.f$2);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling getSmdsAddress", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void setDefaultSmdpAddress(String cardId, String defaultSmdpAddress, final Executor executor, final ResultCallback<Void> callback) {
        try {
            getIEuiccCardController().setDefaultSmdpAddress(this.mContext.getOpPackageName(), cardId, defaultSmdpAddress, new ISetDefaultSmdpAddressCallback.Stub() {
                public void onComplete(int resultCode) {
                    executor.execute(new Runnable(resultCode) {
                        private final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, null);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling setDefaultSmdpAddress", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void requestRulesAuthTable(String cardId, final Executor executor, final ResultCallback<EuiccRulesAuthTable> callback) {
        try {
            getIEuiccCardController().getRulesAuthTable(this.mContext.getOpPackageName(), cardId, new IGetRulesAuthTableCallback.Stub() {
                public void onComplete(int resultCode, EuiccRulesAuthTable rat) {
                    executor.execute(new Runnable(resultCode, rat) {
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ EuiccRulesAuthTable f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, this.f$2);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling getRulesAuthTable", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void requestEuiccChallenge(String cardId, final Executor executor, final ResultCallback<byte[]> callback) {
        try {
            getIEuiccCardController().getEuiccChallenge(this.mContext.getOpPackageName(), cardId, new IGetEuiccChallengeCallback.Stub() {
                public void onComplete(int resultCode, byte[] challenge) {
                    executor.execute(new Runnable(resultCode, challenge) {
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ byte[] f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, this.f$2);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling getEuiccChallenge", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void requestEuiccInfo1(String cardId, final Executor executor, final ResultCallback<byte[]> callback) {
        try {
            getIEuiccCardController().getEuiccInfo1(this.mContext.getOpPackageName(), cardId, new IGetEuiccInfo1Callback.Stub() {
                public void onComplete(int resultCode, byte[] info) {
                    executor.execute(new Runnable(resultCode, info) {
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ byte[] f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, this.f$2);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling getEuiccInfo1", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void requestEuiccInfo2(String cardId, final Executor executor, final ResultCallback<byte[]> callback) {
        try {
            getIEuiccCardController().getEuiccInfo2(this.mContext.getOpPackageName(), cardId, new IGetEuiccInfo2Callback.Stub() {
                public void onComplete(int resultCode, byte[] info) {
                    executor.execute(new Runnable(resultCode, info) {
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ byte[] f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, this.f$2);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling getEuiccInfo2", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void authenticateServer(String cardId, String matchingId, byte[] serverSigned1, byte[] serverSignature1, byte[] euiccCiPkIdToBeUsed, byte[] serverCertificate, Executor executor, ResultCallback<byte[]> callback) {
        try {
            final Executor executor2 = executor;
            final ResultCallback<byte[]> resultCallback = callback;
            try {
                getIEuiccCardController().authenticateServer(this.mContext.getOpPackageName(), cardId, matchingId, serverSigned1, serverSignature1, euiccCiPkIdToBeUsed, serverCertificate, new IAuthenticateServerCallback.Stub() {
                    public void onComplete(int resultCode, byte[] response) {
                        executor2.execute(new Runnable(resultCode, response) {
                            private final /* synthetic */ int f$1;
                            private final /* synthetic */ byte[] f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            public final void run() {
                                EuiccCardManager.ResultCallback.this.onComplete(this.f$1, this.f$2);
                            }
                        });
                    }
                });
            } catch (RemoteException e) {
                e = e;
            }
        } catch (RemoteException e2) {
            e = e2;
            Executor executor3 = executor;
            ResultCallback<byte[]> resultCallback2 = callback;
            Log.e(TAG, "Error calling authenticateServer", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void prepareDownload(String cardId, byte[] hashCc, byte[] smdpSigned2, byte[] smdpSignature2, byte[] smdpCertificate, final Executor executor, final ResultCallback<byte[]> callback) {
        try {
            getIEuiccCardController().prepareDownload(this.mContext.getOpPackageName(), cardId, hashCc, smdpSigned2, smdpSignature2, smdpCertificate, new IPrepareDownloadCallback.Stub() {
                public void onComplete(int resultCode, byte[] response) {
                    executor.execute(new Runnable(resultCode, response) {
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ byte[] f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, this.f$2);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling prepareDownload", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void loadBoundProfilePackage(String cardId, byte[] boundProfilePackage, final Executor executor, final ResultCallback<byte[]> callback) {
        try {
            getIEuiccCardController().loadBoundProfilePackage(this.mContext.getOpPackageName(), cardId, boundProfilePackage, new ILoadBoundProfilePackageCallback.Stub() {
                public void onComplete(int resultCode, byte[] response) {
                    executor.execute(new Runnable(resultCode, response) {
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ byte[] f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, this.f$2);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling loadBoundProfilePackage", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void cancelSession(String cardId, byte[] transactionId, int reason, final Executor executor, final ResultCallback<byte[]> callback) {
        try {
            getIEuiccCardController().cancelSession(this.mContext.getOpPackageName(), cardId, transactionId, reason, new ICancelSessionCallback.Stub() {
                public void onComplete(int resultCode, byte[] response) {
                    executor.execute(new Runnable(resultCode, response) {
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ byte[] f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, this.f$2);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling cancelSession", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void listNotifications(String cardId, int events, final Executor executor, final ResultCallback<EuiccNotification[]> callback) {
        try {
            getIEuiccCardController().listNotifications(this.mContext.getOpPackageName(), cardId, events, new IListNotificationsCallback.Stub() {
                public void onComplete(int resultCode, EuiccNotification[] notifications) {
                    executor.execute(new Runnable(resultCode, notifications) {
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ EuiccNotification[] f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, this.f$2);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling listNotifications", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void retrieveNotificationList(String cardId, int events, final Executor executor, final ResultCallback<EuiccNotification[]> callback) {
        try {
            getIEuiccCardController().retrieveNotificationList(this.mContext.getOpPackageName(), cardId, events, new IRetrieveNotificationListCallback.Stub() {
                public void onComplete(int resultCode, EuiccNotification[] notifications) {
                    executor.execute(new Runnable(resultCode, notifications) {
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ EuiccNotification[] f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, this.f$2);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling retrieveNotificationList", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void retrieveNotification(String cardId, int seqNumber, final Executor executor, final ResultCallback<EuiccNotification> callback) {
        try {
            getIEuiccCardController().retrieveNotification(this.mContext.getOpPackageName(), cardId, seqNumber, new IRetrieveNotificationCallback.Stub() {
                public void onComplete(int resultCode, EuiccNotification notification) {
                    executor.execute(new Runnable(resultCode, notification) {
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ EuiccNotification f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, this.f$2);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling retrieveNotification", e);
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeNotificationFromList(String cardId, int seqNumber, final Executor executor, final ResultCallback<Void> callback) {
        try {
            getIEuiccCardController().removeNotificationFromList(this.mContext.getOpPackageName(), cardId, seqNumber, new IRemoveNotificationFromListCallback.Stub() {
                public void onComplete(int resultCode) {
                    executor.execute(new Runnable(resultCode) {
                        private final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            EuiccCardManager.ResultCallback.this.onComplete(this.f$1, null);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling removeNotificationFromList", e);
            throw e.rethrowFromSystemServer();
        }
    }
}
