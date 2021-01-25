package com.huawei.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import com.android.internal.telephony.InboundSmsHandler;

public class InboundSmsHandlerEx {
    InboundSmsHandler mInboundSmsHandler;

    public static int getBroadcastCompleteEventHw() {
        return InboundSmsHandler.getBroadcastCompleteEventHw();
    }

    public void setInboundSmsHandler(InboundSmsHandler inboundSmsHandler) {
        this.mInboundSmsHandler = inboundSmsHandler;
    }

    public void dispatchIntent(Intent intent, String permission, int appOp, Bundle opts, BroadcastReceiver resultReceiver, UserHandle user) {
        InboundSmsHandler inboundSmsHandler = this.mInboundSmsHandler;
        if (inboundSmsHandler != null) {
            inboundSmsHandler.dispatchIntent(intent, permission, appOp, opts, resultReceiver, user);
        }
    }

    public boolean isWapPushDeliverActionAndMmsMessage(Intent intent) {
        InboundSmsHandler inboundSmsHandler = this.mInboundSmsHandler;
        if (inboundSmsHandler != null) {
            return inboundSmsHandler.isWapPushDeliverActionAndMmsMessage(intent);
        }
        return false;
    }

    public String getNumberIfWapPushDeliverActionAndMmsMessage(Intent intent) {
        InboundSmsHandler inboundSmsHandler = this.mInboundSmsHandler;
        if (inboundSmsHandler != null) {
            return inboundSmsHandler.getNumberIfWapPushDeliverActionAndMmsMessage(intent);
        }
        return null;
    }

    public void sendMessage(int what) {
        InboundSmsHandler inboundSmsHandler = this.mInboundSmsHandler;
        if (inboundSmsHandler != null) {
            inboundSmsHandler.sendMessage(what);
        }
    }

    public PhoneExt getPhoneExt() {
        if (this.mInboundSmsHandler == null) {
            return null;
        }
        PhoneExt phoneExt = new PhoneExt();
        phoneExt.setPhone(this.mInboundSmsHandler.getPhone());
        return phoneExt;
    }
}
