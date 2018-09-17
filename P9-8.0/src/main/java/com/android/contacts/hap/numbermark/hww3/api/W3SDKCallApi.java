package com.android.contacts.hap.numbermark.hww3.api;

import android.content.Context;
import com.android.contacts.hap.numbermark.CapabilityInfo;
import com.android.contacts.hap.numbermark.base.ISDKCallApi;
import com.android.contacts.hap.service.NumberMarkInfo;
import java.util.List;

public class W3SDKCallApi implements ISDKCallApi {
    private static final String SUPPLIER_W3_SERVER_OPTION = "server 3";
    private static volatile W3SDKCallApi callApi;
    private static W3ApiManager mW3ApiManager;

    public static W3SDKCallApi getInstance(Context context) {
        if (callApi == null) {
            callApi = new W3SDKCallApi();
        }
        mW3ApiManager = W3ApiManager.getInstance(context);
        return callApi;
    }

    public NumberMarkInfo getInfoByNum(String num, String callType) {
        return mW3ApiManager.getMarkInfoFromW3Server(num);
    }

    public String correction(NumberMarkInfo info) {
        return null;
    }

    public List<CapabilityInfo> getExtraInfoByNum(String num) {
        return null;
    }

    public List<NumberMarkInfo> getInfoByName(String name) {
        return null;
    }

    public NumberMarkInfo getInfoFromPresetDB(String num) {
        return null;
    }

    public String toString() {
        return SUPPLIER_W3_SERVER_OPTION;
    }
}
