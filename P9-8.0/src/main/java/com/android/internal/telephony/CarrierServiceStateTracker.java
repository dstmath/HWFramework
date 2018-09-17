package com.android.internal.telephony;

import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Global;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import com.android.internal.telephony.util.NotificationChannelController;

public class CarrierServiceStateTracker extends Handler {
    protected static final int CARRIER_EVENT_BASE = 100;
    protected static final int CARRIER_EVENT_DATA_DEREGISTRATION = 104;
    protected static final int CARRIER_EVENT_DATA_REGISTRATION = 103;
    protected static final int CARRIER_EVENT_VOICE_DEREGISTRATION = 102;
    protected static final int CARRIER_EVENT_VOICE_REGISTRATION = 101;
    private static final String LOG_TAG = "CSST";
    private static final int NOTIFICATION_ID = 1000;
    private static final int SHOW_NOTIFICATION = 200;
    private static final int UNINITIALIZED_DELAY_VALUE = -1;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            CarrierServiceStateTracker.this.mDelay = ((CarrierConfigManager) context.getSystemService("carrier_config")).getConfig().getInt("network_notification_delay_int");
            Rlog.i(CarrierServiceStateTracker.LOG_TAG, "reading time to delay notification: " + CarrierServiceStateTracker.this.mDelay);
            CarrierServiceStateTracker.this.handleConfigChanges();
        }
    };
    private int mDelay = -1;
    private boolean mIsPhoneRegistered = false;
    private Phone mPhone;
    private ServiceStateTracker mSST;

    public CarrierServiceStateTracker(Phone phone, ServiceStateTracker sst) {
        this.mPhone = phone;
        this.mSST = sst;
        phone.getContext().registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"));
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 101:
            case CARRIER_EVENT_DATA_REGISTRATION /*103*/:
                this.mIsPhoneRegistered = true;
                handleConfigChanges();
                return;
            case 102:
            case 104:
                if (!isGlobalModeOrRadioOffOrAirplaneMode()) {
                    this.mIsPhoneRegistered = false;
                    handleConfigChanges();
                    return;
                }
                return;
            case 200:
                sendNotification();
                return;
            default:
                return;
        }
    }

    private boolean isGlobalModeOrRadioOffOrAirplaneMode() {
        boolean z = true;
        Context context = this.mPhone.getContext();
        try {
            int preferredNetworkSetting = Global.getInt(context.getContentResolver(), "preferred_network_mode" + this.mPhone.getSubId(), Phone.PREFERRED_NT_MODE);
            int airplaneMode = Global.getInt(context.getContentResolver(), "airplane_mode_on", 0);
            if (preferredNetworkSetting != 10 && (this.mSST.isRadioOn() ^ 1) == 0 && airplaneMode == 0) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Unable to get PREFERRED_NETWORK_MODE.");
            return true;
        }
    }

    private void handleConfigChanges() {
        if (this.mDelay == -1) {
            cancelNotification();
            return;
        }
        if (this.mIsPhoneRegistered) {
            cancelNotification();
            Rlog.i(LOG_TAG, "canceling all notifications. ");
        } else {
            Message notificationMsg = obtainMessage(200, null);
            Rlog.i(LOG_TAG, "starting timer for notifications. ");
            sendMessageDelayed(notificationMsg, (long) this.mDelay);
        }
    }

    private void sendNotification() {
        Context context = this.mPhone.getContext();
        Rlog.i(LOG_TAG, "w/values: ," + this.mIsPhoneRegistered + "," + this.mDelay + "," + isGlobalModeOrRadioOffOrAirplaneMode() + "," + this.mSST.isRadioOn());
        if (!isGlobalModeOrRadioOffOrAirplaneMode() && !this.mIsPhoneRegistered) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
            PendingIntent settingsIntent = PendingIntent.getActivity(context, 0, new Intent("android.settings.DATA_ROAMING_SETTINGS"), 1073741824);
            CharSequence title = context.getText(17039452);
            CharSequence details = context.getText(17039451);
            notificationManager.notify(1000, new Builder(context).setWhen(System.currentTimeMillis()).setAutoCancel(true).setSmallIcon(17301642).setContentTitle(title).setColor(context.getResources().getColor(17170769)).setStyle(new BigTextStyle().bigText(details)).setContentText(details).setContentIntent(settingsIntent).setChannel(NotificationChannelController.CHANNEL_ID_ALERT).build());
        }
    }

    private void cancelNotification() {
        Context context = this.mPhone.getContext();
        this.mIsPhoneRegistered = true;
        ((NotificationManager) context.getSystemService("notification")).cancel(1000);
    }
}
