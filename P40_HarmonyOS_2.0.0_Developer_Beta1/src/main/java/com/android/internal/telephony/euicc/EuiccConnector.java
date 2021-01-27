package com.android.internal.telephony.euicc;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.service.euicc.DownloadSubscriptionResult;
import android.service.euicc.GetDefaultDownloadableSubscriptionListResult;
import android.service.euicc.GetDownloadableSubscriptionMetadataResult;
import android.service.euicc.GetEuiccProfileInfoListResult;
import android.service.euicc.IDeleteSubscriptionCallback;
import android.service.euicc.IDownloadSubscriptionCallback;
import android.service.euicc.IEraseSubscriptionsCallback;
import android.service.euicc.IEuiccService;
import android.service.euicc.IGetDefaultDownloadableSubscriptionListCallback;
import android.service.euicc.IGetDownloadableSubscriptionMetadataCallback;
import android.service.euicc.IGetEidCallback;
import android.service.euicc.IGetEuiccInfoCallback;
import android.service.euicc.IGetEuiccProfileInfoListCallback;
import android.service.euicc.IGetOtaStatusCallback;
import android.service.euicc.IOtaStatusChangedCallback;
import android.service.euicc.IRetainSubscriptionsForFactoryResetCallback;
import android.service.euicc.ISwitchToSubscriptionCallback;
import android.service.euicc.IUpdateSubscriptionNicknameCallback;
import android.telephony.TelephonyManager;
import android.telephony.UiccCardInfo;
import android.telephony.euicc.DownloadableSubscription;
import android.telephony.euicc.EuiccInfo;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.content.PackageMonitor;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.euicc.EuiccConnector;
import com.android.internal.util.IState;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class EuiccConnector extends StateMachine implements ServiceConnection, IHwEuiccConnectorInner {
    private static final int BIND_TIMEOUT_MILLIS = 30000;
    private static final int CMD_COMMAND_COMPLETE = 6;
    static final int CMD_COMMAND_COMPLETE_EXT = 6;
    private static final int CMD_CONNECT_TIMEOUT = 2;
    private static final int CMD_DELETE_SUBSCRIPTION = 106;
    private static final int CMD_DOWNLOAD_SUBSCRIPTION = 102;
    private static final int CMD_ERASE_SUBSCRIPTIONS = 109;
    private static final int CMD_GET_DEFAULT_DOWNLOADABLE_SUBSCRIPTION_LIST = 104;
    private static final int CMD_GET_DOWNLOADABLE_SUBSCRIPTION_METADATA = 101;
    private static final int CMD_GET_EID = 100;
    private static final int CMD_GET_EUICC_INFO = 105;
    private static final int CMD_GET_EUICC_PROFILE_INFO_LIST = 103;
    private static final int CMD_GET_OTA_STATUS = 111;
    private static final int CMD_LINGER_TIMEOUT = 3;
    private static final int CMD_PACKAGE_CHANGE = 1;
    static final int CMD_PACKAGE_CHANGE_EXT = 1;
    private static final int CMD_RETAIN_SUBSCRIPTIONS = 110;
    private static final int CMD_SERVICE_CONNECTED = 4;
    private static final int CMD_SERVICE_DISCONNECTED = 5;
    private static final int CMD_START_OTA_IF_NECESSARY = 112;
    private static final int CMD_SWITCH_TO_SUBSCRIPTION = 107;
    private static final int CMD_UPDATE_SUBSCRIPTION_NICKNAME = 108;
    private static final int EUICC_QUERY_FLAGS = 269484096;
    @VisibleForTesting
    static final int LINGER_TIMEOUT_MILLIS = 60000;
    private static final String TAG = "EuiccConnector";
    private Set<BaseEuiccCommandCallback> mActiveCommandCallbacks = new ArraySet();
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public AvailableState mAvailableState;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public BindingState mBindingState;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public ConnectedState mConnectedState;
    private Context mContext;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public DisconnectedState mDisconnectedState;
    private IHwEuiccConnectorEx mEuiccConnectorEx;
    private IEuiccService mEuiccService;
    private final PackageMonitor mPackageMonitor = new EuiccPackageMonitor();
    private PackageManager mPm;
    private ServiceInfo mSelectedComponent;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public UnavailableState mUnavailableState;
    private final BroadcastReceiver mUserUnlockedReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.euicc.EuiccConnector.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                EuiccConnector.this.sendMessage(1);
            }
        }
    };

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public interface BaseEuiccCommandCallback {
        void onEuiccServiceUnavailable();
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public interface DeleteCommandCallback extends BaseEuiccCommandCallback {
        void onDeleteComplete(int i);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public interface DownloadCommandCallback extends BaseEuiccCommandCallback {
        void onDownloadComplete(DownloadSubscriptionResult downloadSubscriptionResult);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public interface EraseCommandCallback extends BaseEuiccCommandCallback {
        void onEraseComplete(int i);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public interface GetDefaultListCommandCallback extends BaseEuiccCommandCallback {
        void onGetDefaultListComplete(int i, GetDefaultDownloadableSubscriptionListResult getDefaultDownloadableSubscriptionListResult);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public interface GetEidCommandCallback extends BaseEuiccCommandCallback {
        void onGetEidComplete(String str);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public interface GetEuiccInfoCommandCallback extends BaseEuiccCommandCallback {
        void onGetEuiccInfoComplete(EuiccInfo euiccInfo);
    }

    /* access modifiers changed from: package-private */
    public interface GetEuiccProfileInfoListCommandCallback extends BaseEuiccCommandCallback {
        void onListComplete(GetEuiccProfileInfoListResult getEuiccProfileInfoListResult);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public interface GetMetadataCommandCallback extends BaseEuiccCommandCallback {
        void onGetMetadataComplete(int i, GetDownloadableSubscriptionMetadataResult getDownloadableSubscriptionMetadataResult);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public interface GetOtaStatusCommandCallback extends BaseEuiccCommandCallback {
        void onGetOtaStatusComplete(int i);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public interface OtaStatusChangedCallback extends BaseEuiccCommandCallback {
        void onOtaStatusChanged(int i);
    }

    public interface RequestDefaultSmdpAddressCommandCallback extends BaseEuiccCommandCallback {
        void onRequestDefaultSmdpAddressComplete(String str);
    }

    public interface ResetMemoryCommandCallback extends BaseEuiccCommandCallback {
        void onResetMemoryComplete(int i);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public interface RetainSubscriptionsCommandCallback extends BaseEuiccCommandCallback {
        void onRetainSubscriptionsComplete(int i);
    }

    public interface SetDefaultSmdpAddressCommandCallback extends BaseEuiccCommandCallback {
        void onSetDefaultSmdpAddressComplete(int i);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public interface SwitchCommandCallback extends BaseEuiccCommandCallback {
        void onSwitchComplete(int i);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public interface UpdateNicknameCommandCallback extends BaseEuiccCommandCallback {
        void onUpdateNicknameComplete(int i);
    }

    /* access modifiers changed from: private */
    public static boolean isEuiccCommand(int what) {
        return what >= 100;
    }

    public static ActivityInfo findBestActivity(PackageManager packageManager, Intent intent) {
        ActivityInfo bestComponent = (ActivityInfo) findBestComponent(packageManager, packageManager.queryIntentActivities(intent, EUICC_QUERY_FLAGS));
        if (bestComponent == null) {
            Log.w(TAG, "No valid component found for intent: " + intent);
        }
        return bestComponent;
    }

    public static ComponentInfo findBestComponent(PackageManager packageManager) {
        ComponentInfo bestComponent = findBestComponent(packageManager, packageManager.queryIntentServices(new Intent("android.service.euicc.EuiccService"), EUICC_QUERY_FLAGS));
        if (bestComponent == null) {
            Log.w(TAG, "No valid EuiccService implementation found");
        }
        return bestComponent;
    }

    /* access modifiers changed from: package-private */
    public static class GetMetadataRequest {
        GetMetadataCommandCallback mCallback;
        boolean mForceDeactivateSim;
        DownloadableSubscription mSubscription;

        GetMetadataRequest() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class DownloadRequest {
        DownloadCommandCallback mCallback;
        boolean mForceDeactivateSim;
        Bundle mResolvedBundle;
        DownloadableSubscription mSubscription;
        boolean mSwitchAfterDownload;

        DownloadRequest() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class GetDefaultListRequest {
        GetDefaultListCommandCallback mCallback;
        boolean mForceDeactivateSim;

        GetDefaultListRequest() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class DeleteRequest {
        DeleteCommandCallback mCallback;
        String mIccid;

        DeleteRequest() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class SwitchRequest {
        SwitchCommandCallback mCallback;
        boolean mForceDeactivateSim;
        String mIccid;

        SwitchRequest() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class UpdateNicknameRequest {
        UpdateNicknameCommandCallback mCallback;
        String mIccid;
        String mNickname;

        UpdateNicknameRequest() {
        }
    }

    EuiccConnector(Context context) {
        super(TAG);
        init(context);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public EuiccConnector(Context context, Looper looper) {
        super(TAG, looper);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        this.mPm = context.getPackageManager();
        this.mUnavailableState = new UnavailableState();
        addState(this.mUnavailableState);
        this.mAvailableState = new AvailableState();
        addState(this.mAvailableState, this.mUnavailableState);
        this.mBindingState = new BindingState();
        addState(this.mBindingState);
        this.mDisconnectedState = new DisconnectedState();
        addState(this.mDisconnectedState);
        this.mConnectedState = new ConnectedState();
        addState(this.mConnectedState, this.mDisconnectedState);
        this.mSelectedComponent = findBestComponent();
        setInitialState(this.mSelectedComponent != null ? this.mAvailableState : this.mUnavailableState);
        this.mPackageMonitor.register(this.mContext, (Looper) null, false);
        this.mContext.registerReceiver(this.mUserUnlockedReceiver, new IntentFilter("android.intent.action.USER_UNLOCKED"));
        this.mEuiccConnectorEx = HwTelephonyFactory.getHwEuiccConnectorEx(context, this);
        start();
    }

    public void onHalting() {
        this.mPackageMonitor.unregister();
        this.mContext.unregisterReceiver(this.mUserUnlockedReceiver);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void getEid(int cardId, GetEidCommandCallback callback) {
        sendMessage(100, cardId, 0, callback);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void getOtaStatus(int cardId, GetOtaStatusCommandCallback callback) {
        sendMessage(111, cardId, 0, callback);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void startOtaIfNecessary(int cardId, OtaStatusChangedCallback callback) {
        sendMessage(112, cardId, 0, callback);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void getDownloadableSubscriptionMetadata(int cardId, DownloadableSubscription subscription, boolean forceDeactivateSim, GetMetadataCommandCallback callback) {
        GetMetadataRequest request = new GetMetadataRequest();
        request.mSubscription = subscription;
        request.mForceDeactivateSim = forceDeactivateSim;
        request.mCallback = callback;
        sendMessage(101, cardId, 0, request);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void downloadSubscription(int cardId, DownloadableSubscription subscription, boolean switchAfterDownload, boolean forceDeactivateSim, Bundle resolvedBundle, DownloadCommandCallback callback) {
        DownloadRequest request = new DownloadRequest();
        request.mSubscription = subscription;
        request.mSwitchAfterDownload = switchAfterDownload;
        request.mForceDeactivateSim = forceDeactivateSim;
        request.mResolvedBundle = resolvedBundle;
        request.mCallback = callback;
        sendMessage(102, cardId, 0, request);
    }

    /* access modifiers changed from: package-private */
    public void getEuiccProfileInfoList(int cardId, GetEuiccProfileInfoListCommandCallback callback) {
        sendMessage(CMD_GET_EUICC_PROFILE_INFO_LIST, cardId, 0, callback);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void getDefaultDownloadableSubscriptionList(int cardId, boolean forceDeactivateSim, GetDefaultListCommandCallback callback) {
        GetDefaultListRequest request = new GetDefaultListRequest();
        request.mForceDeactivateSim = forceDeactivateSim;
        request.mCallback = callback;
        sendMessage(104, cardId, 0, request);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void getEuiccInfo(int cardId, GetEuiccInfoCommandCallback callback) {
        sendMessage(105, cardId, 0, callback);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void deleteSubscription(int cardId, String iccid, DeleteCommandCallback callback) {
        DeleteRequest request = new DeleteRequest();
        request.mIccid = iccid;
        request.mCallback = callback;
        sendMessage(106, cardId, 0, request);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void switchToSubscription(int cardId, String iccid, boolean forceDeactivateSim, SwitchCommandCallback callback) {
        SwitchRequest request = new SwitchRequest();
        request.mIccid = iccid;
        request.mForceDeactivateSim = forceDeactivateSim;
        request.mCallback = callback;
        sendMessage(CMD_SWITCH_TO_SUBSCRIPTION, cardId, 0, request);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void updateSubscriptionNickname(int cardId, String iccid, String nickname, UpdateNicknameCommandCallback callback) {
        UpdateNicknameRequest request = new UpdateNicknameRequest();
        request.mIccid = iccid;
        request.mNickname = nickname;
        request.mCallback = callback;
        sendMessage(108, cardId, 0, request);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void eraseSubscriptions(int cardId, EraseCommandCallback callback) {
        sendMessage(109, cardId, 0, callback);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void retainSubscriptions(int cardId, RetainSubscriptionsCommandCallback callback) {
        sendMessage(CMD_RETAIN_SUBSCRIPTIONS, cardId, 0, callback);
    }

    /* access modifiers changed from: private */
    public class UnavailableState extends State {
        private UnavailableState() {
        }

        public boolean processMessage(Message message) {
            if (message.what == 1) {
                EuiccConnector euiccConnector = EuiccConnector.this;
                euiccConnector.mSelectedComponent = euiccConnector.findBestComponent();
                if (EuiccConnector.this.mSelectedComponent != null) {
                    EuiccConnector euiccConnector2 = EuiccConnector.this;
                    euiccConnector2.transitionTo(euiccConnector2.mAvailableState);
                } else if (EuiccConnector.this.getCurrentState() != EuiccConnector.this.mUnavailableState) {
                    EuiccConnector euiccConnector3 = EuiccConnector.this;
                    euiccConnector3.transitionTo(euiccConnector3.mUnavailableState);
                }
                return true;
            } else if (!EuiccConnector.isEuiccCommand(message.what)) {
                return false;
            } else {
                EuiccConnector.getCallback(message).onEuiccServiceUnavailable();
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public class AvailableState extends State {
        private AvailableState() {
        }

        public boolean processMessage(Message message) {
            if (!EuiccConnector.isEuiccCommand(message.what)) {
                return false;
            }
            EuiccConnector.this.deferMessage(message);
            EuiccConnector euiccConnector = EuiccConnector.this;
            euiccConnector.transitionTo(euiccConnector.mBindingState);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class BindingState extends State {
        private BindingState() {
        }

        public void enter() {
            if (EuiccConnector.this.createBinding()) {
                EuiccConnector euiccConnector = EuiccConnector.this;
                euiccConnector.transitionTo(euiccConnector.mDisconnectedState);
                return;
            }
            EuiccConnector euiccConnector2 = EuiccConnector.this;
            euiccConnector2.transitionTo(euiccConnector2.mAvailableState);
        }

        public boolean processMessage(Message message) {
            EuiccConnector.this.deferMessage(message);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class DisconnectedState extends State {
        private DisconnectedState() {
        }

        public void enter() {
            EuiccConnector.this.sendMessageDelayed(2, 30000);
        }

        public boolean processMessage(Message message) {
            boolean isSameComponent;
            if (message.what == 4) {
                EuiccConnector.this.mEuiccService = (IEuiccService) message.obj;
                EuiccConnector euiccConnector = EuiccConnector.this;
                euiccConnector.transitionTo(euiccConnector.mConnectedState);
                return true;
            }
            boolean forceRebind = false;
            if (message.what == 1) {
                ServiceInfo bestComponent = EuiccConnector.this.findBestComponent();
                String affectedPackage = (String) message.obj;
                if (bestComponent == null) {
                    isSameComponent = EuiccConnector.this.mSelectedComponent != null;
                } else {
                    isSameComponent = EuiccConnector.this.mSelectedComponent == null || Objects.equals(bestComponent.getComponentName(), EuiccConnector.this.mSelectedComponent.getComponentName());
                }
                if (bestComponent != null && Objects.equals(bestComponent.packageName, affectedPackage)) {
                    forceRebind = true;
                }
                if (!isSameComponent || forceRebind) {
                    EuiccConnector.this.unbind();
                    EuiccConnector.this.mSelectedComponent = bestComponent;
                    if (EuiccConnector.this.mSelectedComponent == null) {
                        EuiccConnector euiccConnector2 = EuiccConnector.this;
                        euiccConnector2.transitionTo(euiccConnector2.mUnavailableState);
                    } else {
                        EuiccConnector euiccConnector3 = EuiccConnector.this;
                        euiccConnector3.transitionTo(euiccConnector3.mBindingState);
                    }
                }
                return true;
            } else if (message.what == 2) {
                EuiccConnector euiccConnector4 = EuiccConnector.this;
                euiccConnector4.transitionTo(euiccConnector4.mAvailableState);
                return true;
            } else if (!EuiccConnector.isEuiccCommand(message.what)) {
                return false;
            } else {
                EuiccConnector.this.deferMessage(message);
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public class ConnectedState extends State {
        private ConnectedState() {
        }

        public void enter() {
            EuiccConnector.this.removeMessages(2);
            EuiccConnector.this.sendMessageDelayed(3, 60000);
        }

        public boolean processMessage(Message message) {
            if (message.what == 5) {
                EuiccConnector.this.mEuiccService = null;
                EuiccConnector euiccConnector = EuiccConnector.this;
                euiccConnector.transitionTo(euiccConnector.mDisconnectedState);
                return true;
            } else if (message.what == 3) {
                EuiccConnector.this.unbind();
                EuiccConnector euiccConnector2 = EuiccConnector.this;
                euiccConnector2.transitionTo(euiccConnector2.mAvailableState);
                return true;
            } else if (message.what == 6) {
                ((Runnable) message.obj).run();
                return true;
            } else if (!EuiccConnector.isEuiccCommand(message.what)) {
                return false;
            } else {
                final BaseEuiccCommandCallback callback = EuiccConnector.getCallback(message);
                EuiccConnector.this.onCommandStart(callback);
                final int cardId = message.arg1;
                int slotId = EuiccConnector.this.getSlotIdFromCardId(cardId);
                try {
                    switch (message.what) {
                        case 100:
                            EuiccConnector.this.mEuiccService.getEid(slotId, new IGetEidCallback.Stub() {
                                /* class com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass1 */

                                public void onSuccess(String eid) {
                                    EuiccConnector.this.sendMessage(6, new Runnable(callback, eid) {
                                        /* class com.android.internal.telephony.euicc.$$Lambda$EuiccConnector$ConnectedState$1$wTkmDdVlxcrtbVPcCl3t7xD490o */
                                        private final /* synthetic */ EuiccConnector.BaseEuiccCommandCallback f$1;
                                        private final /* synthetic */ String f$2;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                        }

                                        @Override // java.lang.Runnable
                                        public final void run() {
                                            EuiccConnector.ConnectedState.AnonymousClass1.this.lambda$onSuccess$0$EuiccConnector$ConnectedState$1(this.f$1, this.f$2);
                                        }
                                    });
                                }

                                public /* synthetic */ void lambda$onSuccess$0$EuiccConnector$ConnectedState$1(BaseEuiccCommandCallback callback, String eid) {
                                    ((GetEidCommandCallback) callback).onGetEidComplete(eid);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 101:
                            GetMetadataRequest request = (GetMetadataRequest) message.obj;
                            EuiccConnector.this.mEuiccService.getDownloadableSubscriptionMetadata(slotId, request.mSubscription, request.mForceDeactivateSim, new IGetDownloadableSubscriptionMetadataCallback.Stub() {
                                /* class com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass2 */

                                public void onComplete(GetDownloadableSubscriptionMetadataResult result) {
                                    EuiccConnector.this.sendMessage(6, new Runnable(callback, cardId, result) {
                                        /* class com.android.internal.telephony.euicc.$$Lambda$EuiccConnector$ConnectedState$2$IvG3dLVC7AcOy5j0EwIqA8hP44Q */
                                        private final /* synthetic */ EuiccConnector.BaseEuiccCommandCallback f$1;
                                        private final /* synthetic */ int f$2;
                                        private final /* synthetic */ GetDownloadableSubscriptionMetadataResult f$3;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                            this.f$3 = r4;
                                        }

                                        @Override // java.lang.Runnable
                                        public final void run() {
                                            EuiccConnector.ConnectedState.AnonymousClass2.this.lambda$onComplete$0$EuiccConnector$ConnectedState$2(this.f$1, this.f$2, this.f$3);
                                        }
                                    });
                                }

                                public /* synthetic */ void lambda$onComplete$0$EuiccConnector$ConnectedState$2(BaseEuiccCommandCallback callback, int cardId, GetDownloadableSubscriptionMetadataResult result) {
                                    ((GetMetadataCommandCallback) callback).onGetMetadataComplete(cardId, result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 102:
                            DownloadRequest request2 = (DownloadRequest) message.obj;
                            EuiccConnector.this.mEuiccService.downloadSubscription(slotId, request2.mSubscription, request2.mSwitchAfterDownload, request2.mForceDeactivateSim, request2.mResolvedBundle, new IDownloadSubscriptionCallback.Stub() {
                                /* class com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass3 */

                                public void onComplete(DownloadSubscriptionResult result) {
                                    EuiccConnector.this.sendMessage(6, new Runnable(callback, result) {
                                        /* class com.android.internal.telephony.euicc.$$Lambda$EuiccConnector$ConnectedState$3$6FrGqACrFuV2Sxte2SudRMjR6s */
                                        private final /* synthetic */ EuiccConnector.BaseEuiccCommandCallback f$1;
                                        private final /* synthetic */ DownloadSubscriptionResult f$2;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                        }

                                        @Override // java.lang.Runnable
                                        public final void run() {
                                            EuiccConnector.ConnectedState.AnonymousClass3.this.lambda$onComplete$0$EuiccConnector$ConnectedState$3(this.f$1, this.f$2);
                                        }
                                    });
                                }

                                public /* synthetic */ void lambda$onComplete$0$EuiccConnector$ConnectedState$3(BaseEuiccCommandCallback callback, DownloadSubscriptionResult result) {
                                    ((DownloadCommandCallback) callback).onDownloadComplete(result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case EuiccConnector.CMD_GET_EUICC_PROFILE_INFO_LIST /* 103 */:
                            EuiccConnector.this.mEuiccService.getEuiccProfileInfoList(slotId, new IGetEuiccProfileInfoListCallback.Stub() {
                                /* class com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass4 */

                                public void onComplete(GetEuiccProfileInfoListResult result) {
                                    EuiccConnector.this.sendMessage(6, new Runnable(callback, result) {
                                        /* class com.android.internal.telephony.euicc.$$Lambda$EuiccConnector$ConnectedState$4$S52i3hpE3FGho807KZ1LR5rXQM */
                                        private final /* synthetic */ EuiccConnector.BaseEuiccCommandCallback f$1;
                                        private final /* synthetic */ GetEuiccProfileInfoListResult f$2;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                        }

                                        @Override // java.lang.Runnable
                                        public final void run() {
                                            EuiccConnector.ConnectedState.AnonymousClass4.this.lambda$onComplete$0$EuiccConnector$ConnectedState$4(this.f$1, this.f$2);
                                        }
                                    });
                                }

                                public /* synthetic */ void lambda$onComplete$0$EuiccConnector$ConnectedState$4(BaseEuiccCommandCallback callback, GetEuiccProfileInfoListResult result) {
                                    ((GetEuiccProfileInfoListCommandCallback) callback).onListComplete(result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 104:
                            EuiccConnector.this.mEuiccService.getDefaultDownloadableSubscriptionList(slotId, ((GetDefaultListRequest) message.obj).mForceDeactivateSim, new IGetDefaultDownloadableSubscriptionListCallback.Stub() {
                                /* class com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass5 */

                                public void onComplete(GetDefaultDownloadableSubscriptionListResult result) {
                                    EuiccConnector.this.sendMessage(6, new Runnable(callback, cardId, result) {
                                        /* class com.android.internal.telephony.euicc.$$Lambda$EuiccConnector$ConnectedState$5$zyynBcfeewfACr0Sg8S162JrG4 */
                                        private final /* synthetic */ EuiccConnector.BaseEuiccCommandCallback f$1;
                                        private final /* synthetic */ int f$2;
                                        private final /* synthetic */ GetDefaultDownloadableSubscriptionListResult f$3;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                            this.f$3 = r4;
                                        }

                                        @Override // java.lang.Runnable
                                        public final void run() {
                                            EuiccConnector.ConnectedState.AnonymousClass5.this.lambda$onComplete$0$EuiccConnector$ConnectedState$5(this.f$1, this.f$2, this.f$3);
                                        }
                                    });
                                }

                                public /* synthetic */ void lambda$onComplete$0$EuiccConnector$ConnectedState$5(BaseEuiccCommandCallback callback, int cardId, GetDefaultDownloadableSubscriptionListResult result) {
                                    ((GetDefaultListCommandCallback) callback).onGetDefaultListComplete(cardId, result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 105:
                            EuiccConnector.this.mEuiccService.getEuiccInfo(slotId, new IGetEuiccInfoCallback.Stub() {
                                /* class com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass6 */

                                public void onSuccess(EuiccInfo euiccInfo) {
                                    EuiccConnector.this.sendMessage(6, new Runnable(callback, euiccInfo) {
                                        /* class com.android.internal.telephony.euicc.$$Lambda$EuiccConnector$ConnectedState$6$RMNCT6pukGHYhU_7k7HVxbm5IWE */
                                        private final /* synthetic */ EuiccConnector.BaseEuiccCommandCallback f$1;
                                        private final /* synthetic */ EuiccInfo f$2;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                        }

                                        @Override // java.lang.Runnable
                                        public final void run() {
                                            EuiccConnector.ConnectedState.AnonymousClass6.this.lambda$onSuccess$0$EuiccConnector$ConnectedState$6(this.f$1, this.f$2);
                                        }
                                    });
                                }

                                public /* synthetic */ void lambda$onSuccess$0$EuiccConnector$ConnectedState$6(BaseEuiccCommandCallback callback, EuiccInfo euiccInfo) {
                                    ((GetEuiccInfoCommandCallback) callback).onGetEuiccInfoComplete(euiccInfo);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 106:
                            EuiccConnector.this.mEuiccService.deleteSubscription(slotId, ((DeleteRequest) message.obj).mIccid, new IDeleteSubscriptionCallback.Stub() {
                                /* class com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass7 */

                                public void onComplete(int result) {
                                    EuiccConnector.this.sendMessage(6, new Runnable(callback, result) {
                                        /* class com.android.internal.telephony.euicc.$$Lambda$EuiccConnector$ConnectedState$7$Ogvr7PIASwQa0kQAqAyfdEKAG4 */
                                        private final /* synthetic */ EuiccConnector.BaseEuiccCommandCallback f$1;
                                        private final /* synthetic */ int f$2;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                        }

                                        @Override // java.lang.Runnable
                                        public final void run() {
                                            EuiccConnector.ConnectedState.AnonymousClass7.this.lambda$onComplete$0$EuiccConnector$ConnectedState$7(this.f$1, this.f$2);
                                        }
                                    });
                                }

                                public /* synthetic */ void lambda$onComplete$0$EuiccConnector$ConnectedState$7(BaseEuiccCommandCallback callback, int result) {
                                    ((DeleteCommandCallback) callback).onDeleteComplete(result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case EuiccConnector.CMD_SWITCH_TO_SUBSCRIPTION /* 107 */:
                            SwitchRequest request3 = (SwitchRequest) message.obj;
                            EuiccConnector.this.mEuiccService.switchToSubscription(slotId, request3.mIccid, request3.mForceDeactivateSim, new ISwitchToSubscriptionCallback.Stub() {
                                /* class com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass8 */

                                public void onComplete(int result) {
                                    EuiccConnector.this.sendMessage(6, new Runnable(callback, result) {
                                        /* class com.android.internal.telephony.euicc.$$Lambda$EuiccConnector$ConnectedState$8$653ymvVUxXSmc5rF5YXkbNw3yw8 */
                                        private final /* synthetic */ EuiccConnector.BaseEuiccCommandCallback f$1;
                                        private final /* synthetic */ int f$2;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                        }

                                        @Override // java.lang.Runnable
                                        public final void run() {
                                            EuiccConnector.ConnectedState.AnonymousClass8.this.lambda$onComplete$0$EuiccConnector$ConnectedState$8(this.f$1, this.f$2);
                                        }
                                    });
                                }

                                public /* synthetic */ void lambda$onComplete$0$EuiccConnector$ConnectedState$8(BaseEuiccCommandCallback callback, int result) {
                                    ((SwitchCommandCallback) callback).onSwitchComplete(result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 108:
                            UpdateNicknameRequest request4 = (UpdateNicknameRequest) message.obj;
                            EuiccConnector.this.mEuiccService.updateSubscriptionNickname(slotId, request4.mIccid, request4.mNickname, new IUpdateSubscriptionNicknameCallback.Stub() {
                                /* class com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass9 */

                                public void onComplete(int result) {
                                    EuiccConnector.this.sendMessage(6, new Runnable(callback, result) {
                                        /* class com.android.internal.telephony.euicc.$$Lambda$EuiccConnector$ConnectedState$9$xm26YKGxl72UYoxSNyEMJslmmNk */
                                        private final /* synthetic */ EuiccConnector.BaseEuiccCommandCallback f$1;
                                        private final /* synthetic */ int f$2;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                        }

                                        @Override // java.lang.Runnable
                                        public final void run() {
                                            EuiccConnector.ConnectedState.AnonymousClass9.this.lambda$onComplete$0$EuiccConnector$ConnectedState$9(this.f$1, this.f$2);
                                        }
                                    });
                                }

                                public /* synthetic */ void lambda$onComplete$0$EuiccConnector$ConnectedState$9(BaseEuiccCommandCallback callback, int result) {
                                    ((UpdateNicknameCommandCallback) callback).onUpdateNicknameComplete(result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 109:
                            EuiccConnector.this.mEuiccService.eraseSubscriptions(slotId, new IEraseSubscriptionsCallback.Stub() {
                                /* class com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass10 */

                                public void onComplete(int result) {
                                    EuiccConnector.this.sendMessage(6, new Runnable(callback, result) {
                                        /* class com.android.internal.telephony.euicc.$$Lambda$EuiccConnector$ConnectedState$10$uMqDQsfFYIEEah_N7V76hMlEL94 */
                                        private final /* synthetic */ EuiccConnector.BaseEuiccCommandCallback f$1;
                                        private final /* synthetic */ int f$2;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                        }

                                        @Override // java.lang.Runnable
                                        public final void run() {
                                            EuiccConnector.ConnectedState.AnonymousClass10.this.lambda$onComplete$0$EuiccConnector$ConnectedState$10(this.f$1, this.f$2);
                                        }
                                    });
                                }

                                public /* synthetic */ void lambda$onComplete$0$EuiccConnector$ConnectedState$10(BaseEuiccCommandCallback callback, int result) {
                                    ((EraseCommandCallback) callback).onEraseComplete(result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case EuiccConnector.CMD_RETAIN_SUBSCRIPTIONS /* 110 */:
                            EuiccConnector.this.mEuiccService.retainSubscriptionsForFactoryReset(slotId, new IRetainSubscriptionsForFactoryResetCallback.Stub() {
                                /* class com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass11 */

                                public void onComplete(int result) {
                                    EuiccConnector.this.sendMessage(6, new Runnable(callback, result) {
                                        /* class com.android.internal.telephony.euicc.$$Lambda$EuiccConnector$ConnectedState$11$yvv0ylXs7V5vymCcYvu3RpgoeDw */
                                        private final /* synthetic */ EuiccConnector.BaseEuiccCommandCallback f$1;
                                        private final /* synthetic */ int f$2;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                        }

                                        @Override // java.lang.Runnable
                                        public final void run() {
                                            EuiccConnector.ConnectedState.AnonymousClass11.this.lambda$onComplete$0$EuiccConnector$ConnectedState$11(this.f$1, this.f$2);
                                        }
                                    });
                                }

                                public /* synthetic */ void lambda$onComplete$0$EuiccConnector$ConnectedState$11(BaseEuiccCommandCallback callback, int result) {
                                    ((RetainSubscriptionsCommandCallback) callback).onRetainSubscriptionsComplete(result);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 111:
                            EuiccConnector.this.mEuiccService.getOtaStatus(slotId, new IGetOtaStatusCallback.Stub() {
                                /* class com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass12 */

                                public void onSuccess(int status) {
                                    EuiccConnector.this.sendMessage(6, new Runnable(callback, status) {
                                        /* class com.android.internal.telephony.euicc.$$Lambda$EuiccConnector$ConnectedState$12$wYal9P4llN7g9YAk_zACL8m3nS0 */
                                        private final /* synthetic */ EuiccConnector.BaseEuiccCommandCallback f$1;
                                        private final /* synthetic */ int f$2;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                        }

                                        @Override // java.lang.Runnable
                                        public final void run() {
                                            EuiccConnector.ConnectedState.AnonymousClass12.this.lambda$onSuccess$0$EuiccConnector$ConnectedState$12(this.f$1, this.f$2);
                                        }
                                    });
                                }

                                public /* synthetic */ void lambda$onSuccess$0$EuiccConnector$ConnectedState$12(BaseEuiccCommandCallback callback, int status) {
                                    ((GetOtaStatusCommandCallback) callback).onGetOtaStatusComplete(status);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        case 112:
                            EuiccConnector.this.mEuiccService.startOtaIfNecessary(slotId, new IOtaStatusChangedCallback.Stub() {
                                /* class com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass13 */

                                public void onOtaStatusChanged(int status) throws RemoteException {
                                    if (status == 1) {
                                        EuiccConnector.this.sendMessage(6, new Runnable(status) {
                                            /* class com.android.internal.telephony.euicc.$$Lambda$EuiccConnector$ConnectedState$13$5nh8TOHvAdIIa_S3V0gwsRICKC4 */
                                            private final /* synthetic */ int f$1;

                                            {
                                                this.f$1 = r2;
                                            }

                                            @Override // java.lang.Runnable
                                            public final void run() {
                                                ((EuiccConnector.OtaStatusChangedCallback) EuiccConnector.BaseEuiccCommandCallback.this).onOtaStatusChanged(this.f$1);
                                            }
                                        });
                                    } else {
                                        EuiccConnector.this.sendMessage(6, new Runnable(callback, status) {
                                            /* class com.android.internal.telephony.euicc.$$Lambda$EuiccConnector$ConnectedState$13$REfW_lBcrAssQONSKwOlO3PX83k */
                                            private final /* synthetic */ EuiccConnector.BaseEuiccCommandCallback f$1;
                                            private final /* synthetic */ int f$2;

                                            {
                                                this.f$1 = r2;
                                                this.f$2 = r3;
                                            }

                                            @Override // java.lang.Runnable
                                            public final void run() {
                                                EuiccConnector.ConnectedState.AnonymousClass13.this.lambda$onOtaStatusChanged$1$EuiccConnector$ConnectedState$13(this.f$1, this.f$2);
                                            }
                                        });
                                    }
                                }

                                public /* synthetic */ void lambda$onOtaStatusChanged$1$EuiccConnector$ConnectedState$13(BaseEuiccCommandCallback callback, int status) {
                                    ((OtaStatusChangedCallback) callback).onOtaStatusChanged(status);
                                    EuiccConnector.this.onCommandEnd(callback);
                                }
                            });
                            break;
                        default:
                            if (!EuiccConnector.this.mEuiccConnectorEx.handleConnectedStateMessage(message)) {
                                Log.wtf(EuiccConnector.TAG, "Unimplemented eUICC command: " + message.what);
                                callback.onEuiccServiceUnavailable();
                                EuiccConnector.this.onCommandEnd(callback);
                            }
                            return true;
                    }
                } catch (Exception e) {
                    Log.w(EuiccConnector.TAG, "Exception making binder call to EuiccService", e);
                    callback.onEuiccServiceUnavailable();
                    EuiccConnector.this.onCommandEnd(callback);
                }
                return true;
            }
        }

        public void exit() {
            EuiccConnector.this.removeMessages(3);
            for (BaseEuiccCommandCallback callback : EuiccConnector.this.mActiveCommandCallbacks) {
                callback.onEuiccServiceUnavailable();
            }
            EuiccConnector.this.mActiveCommandCallbacks.clear();
        }
    }

    /* access modifiers changed from: private */
    public static BaseEuiccCommandCallback getCallback(Message message) {
        switch (message.what) {
            case 100:
            case CMD_GET_EUICC_PROFILE_INFO_LIST /* 103 */:
            case 105:
            case 109:
            case CMD_RETAIN_SUBSCRIPTIONS /* 110 */:
            case 111:
            case 112:
                return (BaseEuiccCommandCallback) message.obj;
            case 101:
                return ((GetMetadataRequest) message.obj).mCallback;
            case 102:
                return ((DownloadRequest) message.obj).mCallback;
            case 104:
                return ((GetDefaultListRequest) message.obj).mCallback;
            case 106:
                return ((DeleteRequest) message.obj).mCallback;
            case CMD_SWITCH_TO_SUBSCRIPTION /* 107 */:
                return ((SwitchRequest) message.obj).mCallback;
            case 108:
                return ((UpdateNicknameRequest) message.obj).mCallback;
            default:
                return HwTelephonyFactory.getHwTelephonyBaseManager().getEuiccConnectorCallback(message);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getSlotIdFromCardId(int cardId) {
        List<UiccCardInfo> infos;
        if (cardId == -1 || cardId == -2 || (infos = ((TelephonyManager) this.mContext.getSystemService("phone")).getUiccCardsInfo()) == null || infos.size() == 0) {
            return -1;
        }
        int slotId = -1;
        for (UiccCardInfo info : infos) {
            if (info.getCardId() == cardId) {
                slotId = info.getSlotIndex();
            }
        }
        return slotId;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCommandStart(BaseEuiccCommandCallback callback) {
        this.mActiveCommandCallbacks.add(callback);
        removeMessages(3);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCommandEnd(BaseEuiccCommandCallback callback) {
        if (!this.mActiveCommandCallbacks.remove(callback)) {
            Log.wtf(TAG, "Callback already removed from mActiveCommandCallbacks");
        }
        if (this.mActiveCommandCallbacks.isEmpty()) {
            sendMessageDelayed(3, 60000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ServiceInfo findBestComponent() {
        return (ServiceInfo) findBestComponent(this.mPm);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean createBinding() {
        if (this.mSelectedComponent == null) {
            Log.wtf(TAG, "Attempting to create binding but no component is selected");
            return false;
        }
        Intent intent = new Intent("android.service.euicc.EuiccService");
        intent.setComponent(this.mSelectedComponent.getComponentName());
        return this.mContext.bindService(intent, this, 67108865);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unbind() {
        this.mEuiccService = null;
        this.mContext.unbindService(this);
    }

    private static ComponentInfo findBestComponent(PackageManager packageManager, List<ResolveInfo> resolveInfoList) {
        int bestPriority = Integer.MIN_VALUE;
        ComponentInfo bestComponent = null;
        if (resolveInfoList != null) {
            for (ResolveInfo resolveInfo : resolveInfoList) {
                if (isValidEuiccComponent(packageManager, resolveInfo) && resolveInfo.filter.getPriority() > bestPriority) {
                    bestPriority = resolveInfo.filter.getPriority();
                    bestComponent = resolveInfo.getComponentInfo();
                }
            }
        }
        return bestComponent;
    }

    private static boolean isValidEuiccComponent(PackageManager packageManager, ResolveInfo resolveInfo) {
        String permission;
        ComponentInfo componentInfo = resolveInfo.getComponentInfo();
        String packageName = componentInfo.getComponentName().getPackageName();
        if (packageManager.checkPermission("android.permission.WRITE_EMBEDDED_SUBSCRIPTIONS", packageName) != 0) {
            Log.wtf(TAG, "Package " + packageName + " does not declare WRITE_EMBEDDED_SUBSCRIPTIONS");
            return false;
        }
        if (componentInfo instanceof ServiceInfo) {
            permission = ((ServiceInfo) componentInfo).permission;
        } else if (componentInfo instanceof ActivityInfo) {
            permission = ((ActivityInfo) componentInfo).permission;
        } else {
            throw new IllegalArgumentException("Can only verify services/activities");
        }
        if (!TextUtils.equals(permission, "android.permission.BIND_EUICC_SERVICE")) {
            Log.wtf(TAG, "Package " + packageName + " does not require the BIND_EUICC_SERVICE permission");
            return false;
        } else if (resolveInfo.filter != null && resolveInfo.filter.getPriority() != 0) {
            return true;
        } else {
            Log.wtf(TAG, "Package " + packageName + " does not specify a priority");
            return false;
        }
    }

    @Override // android.content.ServiceConnection
    public void onServiceConnected(ComponentName name, IBinder service) {
        sendMessage(4, IEuiccService.Stub.asInterface(service));
    }

    @Override // android.content.ServiceConnection
    public void onServiceDisconnected(ComponentName name) {
        sendMessage(5);
    }

    private class EuiccPackageMonitor extends PackageMonitor {
        private EuiccPackageMonitor() {
        }

        public void onPackageAdded(String packageName, int reason) {
            sendPackageChange(packageName, true);
        }

        public void onPackageRemoved(String packageName, int reason) {
            sendPackageChange(packageName, true);
        }

        public void onPackageUpdateFinished(String packageName, int uid) {
            sendPackageChange(packageName, true);
        }

        public void onPackageModified(String packageName) {
            sendPackageChange(packageName, false);
        }

        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            if (doit) {
                for (String packageName : packages) {
                    sendPackageChange(packageName, true);
                }
            }
            return EuiccConnector.super.onHandleForceStop(intent, packages, uid, doit);
        }

        private void sendPackageChange(String packageName, boolean forceUnbindForThisPackage) {
            EuiccConnector.this.sendMessage(1, forceUnbindForThisPackage ? packageName : null);
        }
    }

    /* access modifiers changed from: protected */
    public void unhandledMessage(Message msg) {
        IState state = getCurrentState();
        StringBuilder sb = new StringBuilder();
        sb.append("Unhandled message ");
        sb.append(msg.what);
        sb.append(" in state ");
        sb.append(state == null ? "null" : state.getName());
        Log.wtf(TAG, sb.toString());
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        EuiccConnector.super.dump(fd, pw, args);
        pw.println("mSelectedComponent=" + this.mSelectedComponent);
        pw.println("mEuiccService=" + this.mEuiccService);
        pw.println("mActiveCommandCount=" + this.mActiveCommandCallbacks.size());
    }

    @Override // com.android.internal.telephony.euicc.IHwEuiccConnectorInner
    public IHwEuiccConnectorEx getEuiccConnectorEx() {
        return this.mEuiccConnectorEx;
    }

    @Override // com.android.internal.telephony.euicc.IHwEuiccConnectorInner
    public void onCommandEndEx(BaseEuiccCommandCallback callback) {
        onCommandEnd(callback);
    }

    @Override // com.android.internal.telephony.euicc.IHwEuiccConnectorInner
    public IEuiccService getEuiccService() {
        return this.mEuiccService;
    }
}
