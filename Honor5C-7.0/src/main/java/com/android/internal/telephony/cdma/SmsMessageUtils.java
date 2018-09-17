package com.android.internal.telephony.cdma;

import com.android.internal.telephony.cdma.sms.SmsEnvelope;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.annotation.GetField;
import com.huawei.utils.reflect.annotation.SetField;

public class SmsMessageUtils extends EasyInvokeUtils {
    FieldObject<SmsEnvelope> mEnvelope;
    FieldObject<Integer> status;

    @SetField(fieldObject = "mEnvelope")
    public void setEnvelope(SmsMessage smsMessage, SmsEnvelope value) {
        setField(this.mEnvelope, smsMessage, value);
    }

    @SetField(fieldObject = "status")
    public void setStatus(SmsMessage smsMessage, int value) {
        setField(this.status, smsMessage, Integer.valueOf(value));
    }

    @GetField(fieldObject = "status")
    public int getStatus(SmsMessage smsMessage) {
        return ((Integer) getField(this.status, smsMessage)).intValue();
    }
}
