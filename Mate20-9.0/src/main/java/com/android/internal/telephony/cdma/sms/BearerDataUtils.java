package com.android.internal.telephony.cdma.sms;

import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.GetField;
import com.huawei.utils.reflect.annotation.InvokeMethod;

public class BearerDataUtils extends EasyInvokeUtils {
    FieldObject<Byte> SUBPARAM_MESSAGE_CENTER_TIME_STAMP;
    MethodObject<Integer> countAsciiSeptets;

    @GetField(fieldObject = "SUBPARAM_MESSAGE_CENTER_TIME_STAMP")
    public byte getSubparamMsgCenterTimeStamp(BearerData bearerData) {
        return ((Byte) getField(this.SUBPARAM_MESSAGE_CENTER_TIME_STAMP, bearerData)).byteValue();
    }

    @InvokeMethod(methodObject = "countAsciiSeptets")
    public int countAsciiSeptets(BearerData bearerData, CharSequence msg, boolean force) {
        return ((Integer) invokeMethod(this.countAsciiSeptets, bearerData, msg, Boolean.valueOf(force))).intValue();
    }
}
