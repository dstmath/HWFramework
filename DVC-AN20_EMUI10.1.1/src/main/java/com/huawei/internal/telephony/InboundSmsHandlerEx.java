package com.huawei.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import com.android.internal.telephony.InboundSmsHandler;

public class InboundSmsHandlerEx {
    InboundSmsHandler mInboundSmsHandler;

    public void setInboundSmsHandler(InboundSmsHandler inboundSmsHandler) {
        this.mInboundSmsHandler = inboundSmsHandler;
    }

    public void dispatchIntent(Intent intent, String permission, int appOp, Bundle opts, BroadcastReceiver resultReceiver, UserHandle user) {
        InboundSmsHandler inboundSmsHandler = this.mInboundSmsHandler;
        if (inboundSmsHandler != null) {
            inboundSmsHandler.dispatchIntent(intent, permission, appOp, opts, resultReceiver, user);
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
