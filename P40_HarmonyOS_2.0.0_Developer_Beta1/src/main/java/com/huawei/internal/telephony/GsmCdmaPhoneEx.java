package com.huawei.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.TelephonyComponentFactory;

public class GsmCdmaPhoneEx extends PhoneExt {
    public static void sendInvalidMessageBack(Message onComplete) {
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
            onComplete.sendToTarget();
        }
    }

    /* access modifiers changed from: protected */
    public void initGsmCdmaPhone(Context context, CommandsInterfaceEx ci, PhoneNotifierEx notifier, int phoneId, int precisePhoneType) {
        setPhone(new GsmCdmaPhone(context, ci.getCommandsInterface(), notifier.getPhoneNotifier(), phoneId, precisePhoneType, TelephonyComponentFactory.getInstance()));
        this.mPhoneId = phoneId;
    }
}
