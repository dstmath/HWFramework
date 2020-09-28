package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.os.Parcelable;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.SmsCbMessage;
import android.telephony.SubscriptionManager;
import android.util.LocalLog;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class CellBroadcastHandler extends WakeLockStateMachine {
    private static final String EXTRA_MESSAGE = "message";
    private final LocalLog mLocalLog;

    private CellBroadcastHandler(Context context, Phone phone) {
        this("CellBroadcastHandler", context, phone);
    }

    protected CellBroadcastHandler(String debugTag, Context context, Phone phone) {
        super(debugTag, context, phone);
        this.mLocalLog = new LocalLog(5);
    }

    public static CellBroadcastHandler makeCellBroadcastHandler(Context context, Phone phone) {
        CellBroadcastHandler handler = new CellBroadcastHandler(context, phone);
        handler.start();
        return handler;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.WakeLockStateMachine
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
        String additionalPackage;
        TelephonyMetrics.getInstance().writeNewCBSms(this.mPhone.getPhoneId(), message.getMessageFormat(), message.getMessagePriority(), message.isCmasMessage(), message.isEtwsMessage(), message.getServiceCategory(), message.getSerialNumber(), System.currentTimeMillis());
        if (message.isEmergencyMessage()) {
            String msg = "Dispatching emergency SMS CB, SmsCbMessage is: " + message;
            log(msg);
            this.mLocalLog.log(msg);
            Intent intent = new Intent("android.provider.Telephony.SMS_EMERGENCY_CB_RECEIVED");
            intent.addFlags(268435456);
            intent.putExtra(EXTRA_MESSAGE, (Parcelable) message);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            if (Build.IS_DEBUGGABLE && (additionalPackage = Settings.Secure.getString(this.mContext.getContentResolver(), "cmas_additional_broadcast_pkg")) != null) {
                Intent additionalIntent = new Intent(intent);
                additionalIntent.setPackage(additionalPackage);
                this.mContext.sendOrderedBroadcastAsUser(additionalIntent, UserHandle.ALL, "android.permission.RECEIVE_EMERGENCY_BROADCAST", 17, null, getHandler(), -1, null, null);
            }
            String[] pkgs = this.mContext.getResources().getStringArray(17236002);
            this.mReceiverCount.addAndGet(pkgs.length);
            for (String pkg : pkgs) {
                intent.setPackage(pkg);
                this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.ALL, "android.permission.RECEIVE_EMERGENCY_BROADCAST", 17, this.mReceiver, getHandler(), -1, null, null);
            }
            return;
        }
        String msg2 = "Dispatching SMS CB, SmsCbMessage is: " + message;
        log(msg2);
        this.mLocalLog.log(msg2);
        Intent intent2 = new Intent("android.provider.Telephony.SMS_CB_RECEIVED");
        intent2.addFlags(16777216);
        intent2.putExtra(EXTRA_MESSAGE, (Parcelable) message);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent2, this.mPhone.getPhoneId());
        this.mReceiverCount.incrementAndGet();
        this.mContext.sendOrderedBroadcastAsUser(intent2, UserHandle.ALL, "android.permission.RECEIVE_SMS", 16, this.mReceiver, getHandler(), -1, null, null);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("CellBroadcastHandler:");
        this.mLocalLog.dump(fd, pw, args);
        pw.flush();
    }
}
