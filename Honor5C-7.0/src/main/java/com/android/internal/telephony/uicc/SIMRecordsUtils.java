package com.android.internal.telephony.uicc;

import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.GetField;
import com.huawei.utils.reflect.annotation.InvokeMethod;

public class SIMRecordsUtils extends EasyInvokeUtils {
    FieldObject<Integer> EVENT_GET_ICCID_DONE;
    FieldObject<Integer> EVENT_GET_MBDN_DONE;
    MethodObject<Void> setVoiceMailByCountry;

    @GetField(fieldObject = "EVENT_GET_MBDN_DONE")
    public int getEventMbdnDone(SIMRecords simRecords) {
        return ((Integer) getField(this.EVENT_GET_MBDN_DONE, simRecords)).intValue();
    }

    @InvokeMethod(methodObject = "setVoiceMailByCountry")
    public void setVoiceMailByCountry(SIMRecords simRecords, String spn) {
        invokeMethod(this.setVoiceMailByCountry, simRecords, new Object[]{spn});
    }

    @GetField(fieldObject = "EVENT_GET_ICCID_DONE")
    public int getEventIccidDone(SIMRecords simRecords) {
        return ((Integer) getField(this.EVENT_GET_ICCID_DONE, simRecords)).intValue();
    }
}
