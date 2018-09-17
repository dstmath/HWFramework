package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.telephony.SmsCbMessage;
import android.telephony.SubscriptionManager;

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

    protected boolean handleSmsMessage(Message message) {
        if (message.obj instanceof SmsCbMessage) {
            handleBroadcastSms((SmsCbMessage) message.obj);
            return true;
        }
        loge("handleMessage got object of type: " + message.obj.getClass().getName());
        return false;
    }

    protected void handleBroadcastSms(SmsCbMessage message) {
        String receiverPermission;
        int appOp;
        Intent intent;
        if (message.isEmergencyMessage()) {
            log("Dispatching emergency SMS CB, SmsCbMessage is: " + message);
            intent = new Intent("android.provider.Telephony.SMS_EMERGENCY_CB_RECEIVED");
            intent.setPackage(this.mContext.getResources().getString(17039767));
            receiverPermission = "android.permission.RECEIVE_EMERGENCY_BROADCAST";
            appOp = 17;
        } else {
            log("Dispatching SMS CB, SmsCbMessage is: " + message);
            intent = new Intent("android.provider.Telephony.SMS_CB_RECEIVED");
            intent.addFlags(16777216);
            receiverPermission = "android.permission.RECEIVE_SMS";
            appOp = 16;
        }
        intent.putExtra("message", message);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        if (Build.IS_DEBUGGABLE) {
            String additionalPackage = Secure.getString(this.mContext.getContentResolver(), "cmas_additional_broadcast_pkg");
            if (additionalPackage != null) {
                Intent additionalIntent = new Intent(intent);
                additionalIntent.setPackage(additionalPackage);
                this.mContext.sendOrderedBroadcastAsUser(additionalIntent, UserHandle.ALL, receiverPermission, appOp, null, getHandler(), -1, null, null);
            }
        }
        this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.ALL, receiverPermission, appOp, this.mReceiver, getHandler(), -1, null, null);
    }
}
