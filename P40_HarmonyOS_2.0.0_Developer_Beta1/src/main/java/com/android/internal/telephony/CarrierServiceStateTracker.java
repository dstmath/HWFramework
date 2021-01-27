package com.android.internal.telephony;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.util.NotificationChannelController;
import java.util.HashMap;
import java.util.Map;

public class CarrierServiceStateTracker extends Handler {
    protected static final int CARRIER_EVENT_BASE = 100;
    protected static final int CARRIER_EVENT_DATA_DEREGISTRATION = 104;
    protected static final int CARRIER_EVENT_DATA_REGISTRATION = 103;
    protected static final int CARRIER_EVENT_IMS_CAPABILITIES_CHANGED = 105;
    protected static final int CARRIER_EVENT_VOICE_DEREGISTRATION = 102;
    protected static final int CARRIER_EVENT_VOICE_REGISTRATION = 101;
    private static final String LOG_TAG = "CSST";
    public static final int NOTIFICATION_EMERGENCY_NETWORK = 1001;
    public static final int NOTIFICATION_PREF_NETWORK = 1000;
    private static final int UNINITIALIZED_DELAY_VALUE = -1;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.CarrierServiceStateTracker.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            PersistableBundle b = ((CarrierConfigManager) context.getSystemService("carrier_config")).getConfigForSubId(CarrierServiceStateTracker.this.mPhone.getSubId());
            for (Map.Entry<Integer, NotificationType> entry : CarrierServiceStateTracker.this.mNotificationTypeMap.entrySet()) {
                entry.getValue().setDelay(b);
            }
            CarrierServiceStateTracker.this.handleConfigChanges();
        }
    };
    private final Map<Integer, NotificationType> mNotificationTypeMap = new HashMap();
    private Phone mPhone;
    private ContentObserver mPrefNetworkModeObserver = new ContentObserver(this) {
        /* class com.android.internal.telephony.CarrierServiceStateTracker.AnonymousClass2 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            CarrierServiceStateTracker.this.handlePrefNetworkModeChanged();
        }
    };
    private int mPreviousSubId = -1;
    private ServiceStateTracker mSST;

    public interface NotificationType {
        int getDelay();

        Notification.Builder getNotificationBuilder();

        int getTypeId();

        boolean sendMessage();

        void setDelay(PersistableBundle persistableBundle);
    }

    public CarrierServiceStateTracker(Phone phone, ServiceStateTracker sst) {
        this.mPhone = phone;
        this.mSST = sst;
        phone.getContext().registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"));
        SubscriptionManager.from(this.mPhone.getContext()).addOnSubscriptionsChangedListener(new SubscriptionManager.OnSubscriptionsChangedListener(getLooper()) {
            /* class com.android.internal.telephony.CarrierServiceStateTracker.AnonymousClass1 */

            @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
            public void onSubscriptionsChanged() {
                int subId = CarrierServiceStateTracker.this.mPhone.getSubId();
                if (CarrierServiceStateTracker.this.mPreviousSubId != subId) {
                    CarrierServiceStateTracker.this.mPreviousSubId = subId;
                    CarrierServiceStateTracker.this.registerPrefNetworkModeObserver();
                }
            }
        });
        registerNotificationTypes();
        registerPrefNetworkModeObserver();
    }

    @VisibleForTesting
    public ContentObserver getContentObserver() {
        return this.mPrefNetworkModeObserver;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerPrefNetworkModeObserver() {
        int slotId = this.mPhone.getPhoneId();
        unregisterPrefNetworkModeObserver();
        if (SubscriptionManager.isValidSlotIndex(slotId)) {
            ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
            contentResolver.registerContentObserver(Settings.Global.getUriFor("preferred_network_mode" + slotId), true, this.mPrefNetworkModeObserver);
        }
    }

    private void unregisterPrefNetworkModeObserver() {
        this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mPrefNetworkModeObserver);
    }

    @VisibleForTesting
    public Map<Integer, NotificationType> getNotificationTypeMap() {
        return this.mNotificationTypeMap;
    }

    private void registerNotificationTypes() {
        this.mNotificationTypeMap.put(1000, new PrefNetworkNotification(1000));
        this.mNotificationTypeMap.put(1001, new EmergencyNetworkNotification(1001));
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 1000 || i == 1001) {
            Rlog.d(LOG_TAG, "sending notification after delay: " + msg.what);
            NotificationType notificationType = this.mNotificationTypeMap.get(Integer.valueOf(msg.what));
            if (notificationType != null) {
                sendNotification(notificationType);
                return;
            }
            return;
        }
        switch (i) {
            case 101:
            case 102:
            case CARRIER_EVENT_DATA_REGISTRATION /* 103 */:
            case 104:
                handleConfigChanges();
                return;
            case 105:
                handleImsCapabilitiesChanged();
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPhoneStillRegistered() {
        if (this.mSST.mSS == null || this.mSST.mSS.getVoiceRegState() == 0 || this.mSST.mSS.getDataRegState() == 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPhoneVoiceRegistered() {
        if (this.mSST.mSS == null || this.mSST.mSS.getVoiceRegState() == 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPhoneRegisteredForWifiCalling() {
        Rlog.d(LOG_TAG, "isPhoneRegisteredForWifiCalling: " + this.mPhone.isWifiCallingEnabled());
        return this.mPhone.isWifiCallingEnabled();
    }

    @VisibleForTesting
    public boolean isRadioOffOrAirplaneMode() {
        try {
            return !this.mSST.isRadioOn() || Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "airplane_mode_on", 0) != 0;
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Unable to get AIRPLACE_MODE_ON.");
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isGlobalMode() {
        try {
            ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
            if (Settings.Global.getInt(contentResolver, "preferred_network_mode" + this.mPhone.getPhoneId(), Phone.PREFERRED_NT_MODE) == 10) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Unable to get PREFERRED_NETWORK_MODE.");
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConfigChanges() {
        for (Map.Entry<Integer, NotificationType> entry : this.mNotificationTypeMap.entrySet()) {
            evaluateSendingMessageOrCancelNotification(entry.getValue());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePrefNetworkModeChanged() {
        NotificationType notificationType = this.mNotificationTypeMap.get(1000);
        if (notificationType != null) {
            evaluateSendingMessageOrCancelNotification(notificationType);
        }
    }

    private void handleImsCapabilitiesChanged() {
        NotificationType notificationType = this.mNotificationTypeMap.get(1001);
        if (notificationType != null) {
            evaluateSendingMessageOrCancelNotification(notificationType);
        }
    }

    private void evaluateSendingMessageOrCancelNotification(NotificationType notificationType) {
        if (evaluateSendingMessage(notificationType)) {
            Message notificationMsg = obtainMessage(notificationType.getTypeId(), null);
            Rlog.i(LOG_TAG, "starting timer for notifications." + notificationType.getTypeId());
            sendMessageDelayed(notificationMsg, (long) getDelay(notificationType));
            return;
        }
        cancelNotification(notificationType.getTypeId());
        Rlog.i(LOG_TAG, "canceling notifications: " + notificationType.getTypeId());
    }

    @VisibleForTesting
    public boolean evaluateSendingMessage(NotificationType notificationType) {
        return notificationType.sendMessage();
    }

    @VisibleForTesting
    public int getDelay(NotificationType notificationType) {
        return notificationType.getDelay();
    }

    @VisibleForTesting
    public Notification.Builder getNotificationBuilder(NotificationType notificationType) {
        return notificationType.getNotificationBuilder();
    }

    @VisibleForTesting
    public NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService("notification");
    }

    @VisibleForTesting
    public void sendNotification(NotificationType notificationType) {
        if (evaluateSendingMessage(notificationType)) {
            Context context = this.mPhone.getContext();
            Notification.Builder builder = getNotificationBuilder(notificationType);
            builder.setWhen(System.currentTimeMillis()).setAutoCancel(true).setSmallIcon(17301642).setColor(context.getResources().getColor(17170460));
            getNotificationManager(context).notify(notificationType.getTypeId(), builder.build());
        }
    }

    public void cancelNotification(int notificationId) {
        Context context = this.mPhone.getContext();
        removeMessages(notificationId);
        getNotificationManager(context).cancel(notificationId);
    }

    public void dispose() {
        unregisterPrefNetworkModeObserver();
    }

    public class PrefNetworkNotification implements NotificationType {
        private int mDelay = -1;
        private final int mTypeId;

        PrefNetworkNotification(int typeId) {
            this.mTypeId = typeId;
        }

        @Override // com.android.internal.telephony.CarrierServiceStateTracker.NotificationType
        public void setDelay(PersistableBundle bundle) {
            if (bundle == null) {
                Rlog.e(CarrierServiceStateTracker.LOG_TAG, "bundle is null");
                return;
            }
            this.mDelay = bundle.getInt("network_notification_delay_int");
            Rlog.i(CarrierServiceStateTracker.LOG_TAG, "reading time to delay notification pref network: " + this.mDelay);
        }

        @Override // com.android.internal.telephony.CarrierServiceStateTracker.NotificationType
        public int getDelay() {
            return this.mDelay;
        }

        @Override // com.android.internal.telephony.CarrierServiceStateTracker.NotificationType
        public int getTypeId() {
            return this.mTypeId;
        }

        @Override // com.android.internal.telephony.CarrierServiceStateTracker.NotificationType
        public boolean sendMessage() {
            Rlog.i(CarrierServiceStateTracker.LOG_TAG, "PrefNetworkNotification: sendMessage() w/values: ," + CarrierServiceStateTracker.this.isPhoneStillRegistered() + "," + this.mDelay + "," + CarrierServiceStateTracker.this.isGlobalMode() + "," + CarrierServiceStateTracker.this.mSST.isRadioOn());
            if (this.mDelay == -1 || CarrierServiceStateTracker.this.isPhoneStillRegistered() || CarrierServiceStateTracker.this.isGlobalMode() || CarrierServiceStateTracker.this.isRadioOffOrAirplaneMode()) {
                return false;
            }
            return true;
        }

        @Override // com.android.internal.telephony.CarrierServiceStateTracker.NotificationType
        public Notification.Builder getNotificationBuilder() {
            Context context = CarrierServiceStateTracker.this.mPhone.getContext();
            Intent notificationIntent = new Intent("android.settings.DATA_ROAMING_SETTINGS");
            notificationIntent.putExtra("expandable", true);
            PendingIntent settingsIntent = PendingIntent.getActivity(context, 0, notificationIntent, 1140850688);
            CharSequence title = context.getText(17039465);
            CharSequence details = context.getText(17039464);
            return new Notification.Builder(context).setContentTitle(title).setStyle(new Notification.BigTextStyle().bigText(details)).setContentText(details).setChannel(NotificationChannelController.CHANNEL_ID_ALERT).setContentIntent(settingsIntent);
        }
    }

    public class EmergencyNetworkNotification implements NotificationType {
        private int mDelay = -1;
        private final int mTypeId;

        EmergencyNetworkNotification(int typeId) {
            this.mTypeId = typeId;
        }

        @Override // com.android.internal.telephony.CarrierServiceStateTracker.NotificationType
        public void setDelay(PersistableBundle bundle) {
            if (bundle == null) {
                Rlog.e(CarrierServiceStateTracker.LOG_TAG, "bundle is null");
                return;
            }
            this.mDelay = bundle.getInt("emergency_notification_delay_int");
            Rlog.i(CarrierServiceStateTracker.LOG_TAG, "reading time to delay notification emergency: " + this.mDelay);
        }

        @Override // com.android.internal.telephony.CarrierServiceStateTracker.NotificationType
        public int getDelay() {
            return this.mDelay;
        }

        @Override // com.android.internal.telephony.CarrierServiceStateTracker.NotificationType
        public int getTypeId() {
            return this.mTypeId;
        }

        @Override // com.android.internal.telephony.CarrierServiceStateTracker.NotificationType
        public boolean sendMessage() {
            Rlog.i(CarrierServiceStateTracker.LOG_TAG, "EmergencyNetworkNotification: sendMessage() w/values: ," + CarrierServiceStateTracker.this.isPhoneVoiceRegistered() + "," + this.mDelay + "," + CarrierServiceStateTracker.this.isPhoneRegisteredForWifiCalling() + "," + CarrierServiceStateTracker.this.mSST.isRadioOn());
            if (this.mDelay == -1 || CarrierServiceStateTracker.this.isPhoneVoiceRegistered() || !CarrierServiceStateTracker.this.isPhoneRegisteredForWifiCalling()) {
                return false;
            }
            return true;
        }

        @Override // com.android.internal.telephony.CarrierServiceStateTracker.NotificationType
        public Notification.Builder getNotificationBuilder() {
            Context context = CarrierServiceStateTracker.this.mPhone.getContext();
            CharSequence title = context.getText(17039418);
            CharSequence details = context.getText(17039417);
            return new Notification.Builder(context).setContentTitle(title).setStyle(new Notification.BigTextStyle().bigText(details)).setContentText(details).setChannel(NotificationChannelController.CHANNEL_ID_WFC);
        }
    }
}
