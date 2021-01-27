package com.huawei.internal.telephony.gsm;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.os.Handler;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;

public class HwCustGsmInboundSmsHandler extends Handler {
    @UnsupportedAppUsage
    protected final Context mContext;
    @UnsupportedAppUsage
    protected Phone mPhone;

    public HwCustGsmInboundSmsHandler(Context context, Phone phone) {
        this.mContext = context;
        this.mPhone = phone;
    }

    public String getSubIdKey(int slotId) {
        return PhoneConfigurationManager.SSSS;
    }

    public void clearVmWhenImsiChange(Context context, int slotId) {
    }

    public void setMwiNumber(int mwiNumber) {
    }

    public void onSimRecordsLoaded() {
    }
}
