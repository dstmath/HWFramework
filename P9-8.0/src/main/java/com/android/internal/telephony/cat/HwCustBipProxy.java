package com.android.internal.telephony.cat;

import android.content.Context;
import com.android.internal.telephony.cat.CatCmdMessage.ChannelSettings;

public class HwCustBipProxy {
    public Context mContext;

    public HwCustBipProxy(Context context) {
        this.mContext = context;
    }

    public boolean kddiBipOtaEnable() {
        return false;
    }

    public String getApnString(ChannelSettings channelSettings, String channelInfoType) {
        return "";
    }
}
