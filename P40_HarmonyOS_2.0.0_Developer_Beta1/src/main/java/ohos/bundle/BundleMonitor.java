package ohos.bundle;

import ohos.aafwk.content.Intent;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.LauncherService;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.event.commonevent.CommonEventSupport;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public class BundleMonitor extends CommonEventSubscriber {
    private static final String BUNDLE_NAME = "bundleName";
    private static final int DEFAULT_USER_ID = -1;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218108160, "LauncherService");
    private static final String USER_ID = "userId";
    private LauncherService.BundleStatusCallback callback;

    public BundleMonitor(CommonEventSubscribeInfo commonEventSubscribeInfo) {
        super(commonEventSubscribeInfo);
        AppLog.i(TAG, "BundleMonitor constructor with subscribeInfo", new Object[0]);
    }

    public void subscribe(LauncherService.BundleStatusCallback bundleStatusCallback) {
        AppLog.d(TAG, "subscribe called", new Object[0]);
        this.callback = bundleStatusCallback;
        try {
            CommonEventManager.subscribeCommonEvent(this);
        } catch (RemoteException unused) {
            AppLog.e(TAG, "subscribeCommonEvent occur exception.", new Object[0]);
        }
    }

    public void unsubscribe() {
        AppLog.d(TAG, "unsubscribe called", new Object[0]);
        try {
            CommonEventManager.unsubscribeCommonEvent(this);
        } catch (RemoteException unused) {
            AppLog.e(TAG, "unsubscribeCommonEvent occur exception.", new Object[0]);
        }
    }

    @Override // ohos.event.commonevent.CommonEventSubscriber
    public void onReceiveEvent(CommonEventData commonEventData) {
        LauncherService.BundleStatusCallback bundleStatusCallback;
        LauncherService.BundleStatusCallback bundleStatusCallback2;
        LauncherService.BundleStatusCallback bundleStatusCallback3;
        AppLog.i(TAG, "onReceiveEvent common event onReceiveEvent called", new Object[0]);
        if (commonEventData == null) {
            AppLog.e(TAG, "onReceiveEvent common event data is null", new Object[0]);
            return;
        }
        Intent intent = commonEventData.getIntent();
        if (intent == null) {
            AppLog.e(TAG, "onReceiveEvent intent is null", new Object[0]);
            return;
        }
        String action = intent.getAction();
        String stringParam = intent.getStringParam("bundleName");
        int intParam = intent.getIntParam(USER_ID, -1);
        AppLog.i(TAG, "onReceiveEvent action = %{public}s, bundle = %{public}s", action, stringParam);
        if (action.equals(CommonEventSupport.COMMON_EVENT_ABILITY_ADDED) && (bundleStatusCallback3 = this.callback) != null) {
            bundleStatusCallback3.onBundleAdded(stringParam, intParam);
        } else if (action.equals(CommonEventSupport.COMMON_EVENT_ABILITY_UPDATED) && (bundleStatusCallback2 = this.callback) != null) {
            bundleStatusCallback2.onBundleUpdated(stringParam, intParam);
        } else if (!action.equals(CommonEventSupport.COMMON_EVENT_ABILITY_REMOVED) || (bundleStatusCallback = this.callback) == null) {
            AppLog.w(TAG, "onReceiveEvent action = %{public}s not support", action);
        } else {
            bundleStatusCallback.onBundleRemoved(stringParam, intParam);
        }
    }
}
