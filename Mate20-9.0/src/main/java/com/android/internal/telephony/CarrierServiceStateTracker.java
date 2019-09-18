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
    protected static final int CARRIER_EVENT_VOICE_DEREGISTRATION = 102;
    protected static final int CARRIER_EVENT_VOICE_REGISTRATION = 101;
    private static final String LOG_TAG = "CSST";
    public static final int NOTIFICATION_EMERGENCY_NETWORK = 1001;
    public static final int NOTIFICATION_PREF_NETWORK = 1000;
    private static final int UNINITIALIZED_DELAY_VALUE = -1;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            PersistableBundle b = ((CarrierConfigManager) context.getSystemService("carrier_config")).getConfigForSubId(CarrierServiceStateTracker.this.mPhone.getSubId());
            for (Map.Entry<Integer, NotificationType> entry : CarrierServiceStateTracker.this.mNotificationTypeMap.entrySet()) {
                entry.getValue().setDelay(b);
            }
            CarrierServiceStateTracker.this.handleConfigChanges();
        }
    };
    /* access modifiers changed from: private */
    public final Map<Integer, NotificationType> mNotificationTypeMap = new HashMap();
    /* access modifiers changed from: private */
    public Phone mPhone;
    private ContentObserver mPrefNetworkModeObserver = new ContentObserver(this) {
        public void onChange(boolean selfChange) {
            CarrierServiceStateTracker.this.handlePrefNetworkModeChanged();
        }
    };
    /* access modifiers changed from: private */
    public int mPreviousSubId = -1;
    /* access modifiers changed from: private */
    public ServiceStateTracker mSST;

    public class EmergencyNetworkNotification implements NotificationType {
        private int mDelay = -1;
        private final int mTypeId;

        EmergencyNetworkNotification(int typeId) {
            this.mTypeId = typeId;
        }

        public void setDelay(PersistableBundle bundle) {
            if (bundle == null) {
                Rlog.e(CarrierServiceStateTracker.LOG_TAG, "bundle is null");
                return;
            }
            this.mDelay = bundle.getInt("emergency_notification_delay_int");
            Rlog.i(CarrierServiceStateTracker.LOG_TAG, "reading time to delay notification emergency: " + this.mDelay);
        }

        public int getDelay() {
            return this.mDelay;
        }

        public int getTypeId() {
            return this.mTypeId;
        }

        public boolean sendMessage() {
            Rlog.i(CarrierServiceStateTracker.LOG_TAG, "EmergencyNetworkNotification: sendMessage() w/values: ," + CarrierServiceStateTracker.this.isPhoneVoiceRegistered() + "," + this.mDelay + "," + CarrierServiceStateTracker.this.isPhoneRegisteredForWifiCalling() + "," + CarrierServiceStateTracker.this.mSST.isRadioOn());
            if (this.mDelay == -1 || CarrierServiceStateTracker.this.isPhoneVoiceRegistered() || !CarrierServiceStateTracker.this.isPhoneRegisteredForWifiCalling()) {
                return false;
            }
            return true;
        }

        public Notification.Builder getNotificationBuilder() {
            Context context = CarrierServiceStateTracker.this.mPhone.getContext();
            CharSequence title = context.getText(17039408);
            CharSequence details = context.getText(17039407);
            return new Notification.Builder(context).setContentTitle(title).setStyle(new Notification.BigTextStyle().bigText(details)).setContentText(details).setChannel(NotificationChannelController.CHANNEL_ID_WFC);
        }
    }

    public interface NotificationType {
        int getDelay();

        Notification.Builder getNotificationBuilder();

        int getTypeId();

        boolean sendMessage();

        void setDelay(PersistableBundle persistableBundle);
    }

    public class PrefNetworkNotification implements NotificationType {
        private int mDelay = -1;
        private final int mTypeId;

        PrefNetworkNotification(int typeId) {
            this.mTypeId = typeId;
        }

        public void setDelay(PersistableBundle bundle) {
            if (bundle == null) {
                Rlog.e(CarrierServiceStateTracker.LOG_TAG, "bundle is null");
                return;
            }
            this.mDelay = bundle.getInt("network_notification_delay_int");
            Rlog.i(CarrierServiceStateTracker.LOG_TAG, "reading time to delay notification pref network: " + this.mDelay);
        }

        public int getDelay() {
            return this.mDelay;
        }

        public int getTypeId() {
            return this.mTypeId;
        }

        public boolean sendMessage() {
            Rlog.i(CarrierServiceStateTracker.LOG_TAG, "PrefNetworkNotification: sendMessage() w/values: ," + CarrierServiceStateTracker.this.isPhoneStillRegistered() + "," + this.mDelay + "," + CarrierServiceStateTracker.this.isGlobalMode() + "," + CarrierServiceStateTracker.this.mSST.isRadioOn());
            if (this.mDelay == -1 || CarrierServiceStateTracker.this.isPhoneStillRegistered() || CarrierServiceStateTracker.this.isGlobalMode() || CarrierServiceStateTracker.this.isRadioOffOrAirplaneMode()) {
                return false;
            }
            return true;
        }

        public Notification.Builder getNotificationBuilder() {
            Context context = CarrierServiceStateTracker.this.mPhone.getContext();
            Intent notificationIntent = new Intent("android.settings.DATA_ROAMING_SETTINGS");
            notificationIntent.putExtra("expandable", true);
            PendingIntent settingsIntent = PendingIntent.getActivity(context, 0, notificationIntent, 1073741824);
            CharSequence title = context.getText(17039455);
            CharSequence details = context.getText(17039454);
            return new Notification.Builder(context).setContentTitle(title).setStyle(new Notification.BigTextStyle().bigText(details)).setContentText(details).setChannel(NotificationChannelController.CHANNEL_ID_ALERT).setContentIntent(settingsIntent);
        }
    }

    public CarrierServiceStateTracker(Phone phone, ServiceStateTracker sst) {
        this.mPhone = phone;
        this.mSST = sst;
        phone.getContext().registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"));
        SubscriptionManager.from(this.mPhone.getContext()).addOnSubscriptionsChangedListener(new SubscriptionManager.OnSubscriptionsChangedListener(getLooper()) {
            public void onSubscriptionsChanged() {
                int subId = CarrierServiceStateTracker.this.mPhone.getSubId();
                if (CarrierServiceStateTracker.this.mPreviousSubId != subId) {
                    int unused = CarrierServiceStateTracker.this.mPreviousSubId = subId;
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
    public void registerPrefNetworkModeObserver() {
        int subId = this.mPhone.getSubId();
        unregisterPrefNetworkModeObserver();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
            contentResolver.registerContentObserver(Settings.Global.getUriFor("preferred_network_mode" + subId), true, this.mPrefNetworkModeObserver);
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

    public void handleMessage(Message msg) {
        int i = msg.what;
        switch (i) {
            case 101:
            case 102:
            case CARRIER_EVENT_DATA_REGISTRATION /*103*/:
            case 104:
                handleConfigChanges();
                return;
            default:
                switch (i) {
                    case 1000:
                    case 1001:
                        Rlog.d(LOG_TAG, "sending notification after delay: " + msg.what);
                        NotificationType notificationType = this.mNotificationTypeMap.get(Integer.valueOf(msg.what));
                        if (notificationType != null) {
                            sendNotification(notificationType);
                            return;
                        }
                        return;
                    default:
                        return;
                }
        }
    }

    /* access modifiers changed from: private */
    public boolean isPhoneStillRegistered() {
        boolean z = true;
        if (this.mSST.mSS == null) {
            return true;
        }
        if (!(this.mSST.mSS.getVoiceRegState() == 0 || this.mSST.mSS.getDataRegState() == 0)) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public boolean isPhoneVoiceRegistered() {
        boolean z = true;
        if (this.mSST.mSS == null) {
            return true;
        }
        if (this.mSST.mSS.getVoiceRegState() != 0) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public boolean isPhoneRegisteredForWifiCalling() {
        Rlog.d(LOG_TAG, "isPhoneRegisteredForWifiCalling: " + this.mPhone.isWifiCallingEnabled());
        return this.mPhone.isWifiCallingEnabled();
    }

    @VisibleForTesting
    public boolean isRadioOffOrAirplaneMode() {
        boolean z = true;
        try {
            int airplaneMode = Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "airplane_mode_on", 0);
            if (this.mSST.isRadioOn() && airplaneMode == 0) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Unable to get AIRPLACE_MODE_ON.");
            return true;
        }
    }

    /* access modifiers changed from: private */
    public boolean isGlobalMode() {
        boolean z = true;
        try {
            ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
            if (Settings.Global.getInt(contentResolver, "preferred_network_mode" + this.mPhone.getSubId(), Phone.PREFERRED_NT_MODE) != 10) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Unable to get PREFERRED_NETWORK_MODE.");
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void handleConfigChanges() {
        for (Map.Entry<Integer, NotificationType> entry : this.mNotificationTypeMap.entrySet()) {
            evaluateSendingMessageOrCancelNotification(entry.getValue());
        }
    }

    /* access modifiers changed from: private */
    public void handlePrefNetworkModeChanged() {
        NotificationType notificationType = this.mNotificationTypeMap.get(1000);
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
            builder.setWhen(System.currentTimeMillis()).setAutoCancel(true).setSmallIcon(17301642).setColor(context.getResources().getColor(17170784));
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
}
