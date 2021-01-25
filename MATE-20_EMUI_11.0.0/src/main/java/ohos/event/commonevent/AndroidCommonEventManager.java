package ohos.event.commonevent;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import java.util.Optional;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.tools.Bytrace;

public class AndroidCommonEventManager {
    private static final String KEY_PUBLISH_PERMISSION = "key_publish_permission";
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.COMMON_EVENT_DOMAIN, TAG);
    private static final String TAG = "AndroidCommonEventManager";
    private static final int USER_CURRENT = -2;
    private Context aospContext;

    private static class Holder {
        private static final AndroidCommonEventManager INSTANCE = new AndroidCommonEventManager();

        private Holder() {
        }
    }

    private AndroidCommonEventManager() {
        this.aospContext = null;
    }

    public static AndroidCommonEventManager getInstance() {
        return Holder.INSTANCE;
    }

    public void subscribeCommonEvent(CommonEventSubscriberHost commonEventSubscriberHost, CommonEventSubscribeInfo commonEventSubscribeInfo) {
        if (!checkSubscriberValid(commonEventSubscriberHost, commonEventSubscribeInfo)) {
            HiLog.warn(LABEL, "AndroidCommonEventManager::subscribeCommonEvent The subscriber or the subscriberInfo is not valid", new Object[0]);
            return;
        }
        Object readObject = commonEventSubscriberHost.readObject();
        if (readObject == null) {
            try {
                readObject = commonEventSubscriberHost.writeObject(new AdapterReceiver(commonEventSubscriberHost));
            } catch (IllegalArgumentException unused) {
                HiLog.warn(LABEL, "AndroidCommonEventManager::subscribeCommonEvent The argument is illegal", new Object[0]);
                return;
            }
        }
        if (!(readObject instanceof AdapterReceiver)) {
            HiLog.warn(LABEL, "AndroidCommonEventManager::subscribeCommonEvent The object is not a receiver or object null", new Object[0]);
            return;
        }
        try {
            registerReceiver((AdapterReceiver) readObject, commonEventSubscribeInfo);
        } catch (IllegalStateException unused2) {
            HiLog.warn(LABEL, "AndroidCommonEventManager::subscribeCommonEvent failed.", new Object[0]);
        }
    }

    public void unsubscribeCommonEvent(CommonEventSubscriberHost commonEventSubscriberHost) {
        if (commonEventSubscriberHost == null) {
            HiLog.warn(LABEL, "AndroidCommonEventManager::unsubscribeCommonEvent Fail to register: The subscriber is null", new Object[0]);
            return;
        }
        Object readObject = commonEventSubscriberHost.readObject();
        if (!(readObject instanceof AdapterReceiver)) {
            HiLog.warn(LABEL, "AndroidCommonEventManager::unsubscribeCommonEvent The object is not a receiver or object null", new Object[0]);
            return;
        }
        try {
            unregisterSubscriber((AdapterReceiver) readObject);
        } catch (IllegalStateException unused) {
            HiLog.warn(LABEL, "AndroidCommonEventManager::unsubscribeCommonEvent failed.", new Object[0]);
        }
    }

    public void publishCommonEvent(CommonEventData commonEventData, CommonEventPublishInfo commonEventPublishInfo, ICommonEventSubscriber iCommonEventSubscriber) {
        if (commonEventData == null) {
            HiLog.warn(LABEL, "AndroidCommonEventManager::publishCommonEvent common event data is null", new Object[0]);
            return;
        }
        Context aospContext2 = getAospContext();
        if (aospContext2 == null) {
            HiLog.warn(LABEL, "AndroidCommonEventManager::publishCommonEvent get android context is null", new Object[0]);
            return;
        }
        Optional<Intent> createAndroidIntent = DataConverter.createAndroidIntent(false, commonEventData.getIntent());
        if (!createAndroidIntent.isPresent()) {
            HiLog.warn(LABEL, "AndroidCommonEventManager::publishCommonEvent intent is null", new Object[0]);
            return;
        }
        Intent intent = createAndroidIntent.get();
        if (intent.getAction() == null || intent.getAction().isEmpty()) {
            HiLog.error(LABEL, "AndroidCommonEventManager::publishCommonEvent action is empty.", new Object[0]);
            return;
        }
        Bytrace.startTrace(2, "EmuiPubCE");
        if (commonEventPublishInfo == null) {
            aospContext2.sendBroadcast(intent);
            Bytrace.finishTrace(2, "EmuiPubCE");
        } else if (commonEventPublishInfo.isSticky()) {
            sendStickyBroadcast(aospContext2, intent, commonEventData, commonEventPublishInfo, iCommonEventSubscriber);
            Bytrace.finishTrace(2, "EmuiPubCE");
        } else {
            String str = null;
            String[] subscriberPermissions = commonEventPublishInfo.getSubscriberPermissions();
            if (subscriberPermissions.length > 0) {
                str = subscriberPermissions[0];
                intent = intent.putExtra(KEY_PUBLISH_PERMISSION, new String[]{str});
            }
            if (commonEventPublishInfo.isOrdered()) {
                sendOrderedBroadcast(aospContext2, intent, commonEventData, str, iCommonEventSubscriber);
                Bytrace.finishTrace(2, "EmuiPubCE");
                return;
            }
            aospContext2.sendBroadcast(intent, str);
            Bytrace.finishTrace(2, "EmuiPubCE");
        }
    }

    private void sendOrderedBroadcast(Context context, Intent intent, CommonEventData commonEventData, String str, ICommonEventSubscriber iCommonEventSubscriber) {
        AdapterReceiver adapterReceiver;
        if (iCommonEventSubscriber != null) {
            try {
                adapterReceiver = new AdapterReceiver(iCommonEventSubscriber);
            } catch (IllegalArgumentException unused) {
                HiLog.warn(LABEL, "AndroidCommonEventManager::subscribeCommonEvent The argument is illegal", new Object[0]);
                return;
            }
        } else {
            adapterReceiver = null;
        }
        context.sendOrderedBroadcast(intent, str, adapterReceiver, null, commonEventData.getCode(), commonEventData.getData(), null);
    }

    private void sendStickyBroadcast(Context context, Intent intent, CommonEventData commonEventData, CommonEventPublishInfo commonEventPublishInfo, ICommonEventSubscriber iCommonEventSubscriber) {
        if (!commonEventPublishInfo.isOrdered()) {
            context.sendStickyBroadcast(intent);
            return;
        }
        AdapterReceiver adapterReceiver = null;
        if (iCommonEventSubscriber != null) {
            try {
                adapterReceiver = new AdapterReceiver(iCommonEventSubscriber);
            } catch (IllegalArgumentException unused) {
                HiLog.warn(LABEL, "AndroidCommonEventManager::sendStickyBroadcast The argument is illegal", new Object[0]);
                return;
            }
        }
        context.sendStickyOrderedBroadcast(intent, adapterReceiver, null, commonEventData.getCode(), commonEventData.getData(), null);
    }

    private void registerReceiver(AdapterReceiver adapterReceiver, CommonEventSubscribeInfo commonEventSubscribeInfo) {
        Context aospContext2 = getAospContext();
        if (aospContext2 != null) {
            Optional<IntentFilter> createAndroidIntentFilter = DataConverter.createAndroidIntentFilter(false, commonEventSubscribeInfo.getPriority(), commonEventSubscribeInfo.getIntentFilter());
            int userId = commonEventSubscribeInfo.getUserId();
            if (userId == -2) {
                aospContext2.registerReceiver(adapterReceiver, createAndroidIntentFilter.orElse(null), commonEventSubscribeInfo.getPermission(), null);
            } else {
                aospContext2.registerReceiverAsUser(adapterReceiver, UserHandle.of(userId), createAndroidIntentFilter.orElse(null), commonEventSubscribeInfo.getPermission(), null);
            }
        } else {
            throw new IllegalStateException("AndroidCommonEventManager::registerReceiver Fail to register: get android context is null.");
        }
    }

    private void unregisterSubscriber(AdapterReceiver adapterReceiver) {
        Context aospContext2 = getAospContext();
        if (aospContext2 != null) {
            aospContext2.unregisterReceiver(adapterReceiver);
            return;
        }
        throw new IllegalStateException("AndroidCommonEventManager::unregisterReceiver Fail to register: get android context is null.");
    }

    private boolean checkSubscriberValid(ICommonEventSubscriber iCommonEventSubscriber, CommonEventSubscribeInfo commonEventSubscribeInfo) {
        return (iCommonEventSubscriber == null || commonEventSubscribeInfo == null || commonEventSubscribeInfo.getIntentFilter() == null) ? false : true;
    }

    private Context getAospContext() {
        Application currentApplication;
        if (this.aospContext == null && (currentApplication = ActivityThread.currentApplication()) != null) {
            this.aospContext = currentApplication.getApplicationContext();
        }
        return this.aospContext;
    }
}
