package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.SmsCbMessage;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.metrics.TelephonyMetrics;

public class CellBroadcastHandler extends WakeLockStateMachine {
    private CellBroadcastHandler(Context context, Phone phone) {
        this("CellBroadcastHandler", context, phone);
    }

    protected CellBroadcastHandler(String debugTag, Context context, Phone phone) {
        super(debugTag, context, phone);
    }

    public static CellBroadcastHandler makeCellBroadcastHandler(Context context, Phone phone) {
        CellBroadcastHandler handler = new CellBroadcastHandler(context, phone);
        handler.start();
        return handler;
    }

    /* access modifiers changed from: protected */
    public boolean handleSmsMessage(Message message) {
        if (message.obj instanceof SmsCbMessage) {
            handleBroadcastSms((SmsCbMessage) message.obj);
            return true;
        }
        loge("handleMessage got object of type: " + message.obj.getClass().getName());
        return false;
    }

    /* access modifiers changed from: protected */
    public void handleBroadcastSms(SmsCbMessage message) {
        Intent intent;
        String receiverPermission;
        int i;
        SmsCbMessage smsCbMessage = message;
        TelephonyMetrics.getInstance().writeNewCBSms(this.mPhone.getPhoneId(), message.getMessageFormat(), message.getMessagePriority(), message.isCmasMessage(), message.isEtwsMessage(), message.getServiceCategory());
        if (message.isEmergencyMessage()) {
            log("Dispatching emergency SMS CB, SmsCbMessage is: " + smsCbMessage);
            intent = new Intent("android.provider.Telephony.SMS_EMERGENCY_CB_RECEIVED");
            intent.setPackage(this.mContext.getResources().getString(17039785));
            receiverPermission = "android.permission.RECEIVE_EMERGENCY_BROADCAST";
            i = 17;
        } else {
            log("Dispatching SMS CB, SmsCbMessage is: " + smsCbMessage);
            intent = new Intent("android.provider.Telephony.SMS_CB_RECEIVED");
            intent.addFlags(16777216);
            receiverPermission = "android.permission.RECEIVE_SMS";
            i = 16;
        }
        int appOp = i;
        intent.putExtra("message", smsCbMessage);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        if (Build.IS_DEBUGGABLE) {
            String additionalPackage = Settings.Secure.getString(this.mContext.getContentResolver(), "cmas_additional_broadcast_pkg");
            if (additionalPackage != null) {
                Intent additionalIntent = new Intent(intent);
                additionalIntent.setPackage(additionalPackage);
                this.mContext.sendOrderedBroadcastAsUser(additionalIntent, UserHandle.ALL, receiverPermission, appOp, null, getHandler(), -1, null, null);
            }
        }
        Intent intent2 = intent;
        this.mContext.sendOrderedBroadcastAsUser(intent2, UserHandle.ALL, receiverPermission, appOp, this.mReceiver, getHandler(), -1, null, null);
    }
}
