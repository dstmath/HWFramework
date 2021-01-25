package com.android.internal.telephony.cat;

import android.content.Context;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.cat.CatCmdMessage;

public class HwCustBipProxy {
    public Context mContext;

    public HwCustBipProxy(Context context) {
        this.mContext = context;
    }

    public boolean kddiBipOtaEnable() {
        return false;
    }

    public String getApnString(CatCmdMessage.ChannelSettings channelSettings, String channelInfoType) {
        return PhoneConfigurationManager.SSSS;
    }

    public boolean isBipOtaEnable(int slotId) {
        return false;
    }
}
