package com.android.internal.telephony.uicc;

import android.os.Message;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.GetField;
import com.huawei.utils.reflect.annotation.InvokeMethod;

public class AdnRecordCacheUtils extends EasyInvokeUtils {
    FieldObject<IccFileHandler> mFh;
    FieldObject<UsimPhoneBookManager> mUsimPhoneBookManager;
    MethodObject<Void> sendErrorResponse;

    @GetField(fieldObject = "mFh")
    public IccFileHandler getFh(AdnRecordCache adnRecordCache) {
        return (IccFileHandler) getField(this.mFh, adnRecordCache);
    }

    @GetField(fieldObject = "mUsimPhoneBookManager")
    public UsimPhoneBookManager getUsimPhoneBookManager(AdnRecordCache adnRecordCache) {
        return (UsimPhoneBookManager) getField(this.mUsimPhoneBookManager, adnRecordCache);
    }

    @InvokeMethod(methodObject = "sendErrorResponse")
    public void sendErrorResponse(AdnRecordCache adnRecordCache, Message response, String errString) {
        invokeMethod(this.sendErrorResponse, adnRecordCache, new Object[]{response, errString});
    }
}
