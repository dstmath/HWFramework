package com.android.contacts.hap.numbermark.hwtencent.api;

import android.content.Context;
import com.android.contacts.hap.numbermark.CapabilityInfo;
import com.android.contacts.hap.numbermark.base.ISDKCallApi;
import com.android.contacts.hap.service.NumberMarkInfo;
import java.util.List;

public class TencentSDKCallApi implements ISDKCallApi {
    private static final String SUPPLIER_TENCENT_SERVER_OPTION = "server 2";
    private static volatile TencentSDKCallApi callApi;
    private static TencentApiManager mTencentApiManager;

    public static TencentSDKCallApi getInstance(Context context) {
        if (callApi == null) {
            callApi = new TencentSDKCallApi();
        }
        mTencentApiManager = TencentApiManager.getInstance(context);
        return callApi;
    }

    public NumberMarkInfo getInfoByNum(String num, String callType) {
        return mTencentApiManager.cloudFetchNumberInfo(num, callType);
    }

    public NumberMarkInfo getInfoFromPresetDB(String num) {
        return mTencentApiManager.localFetchNumberInfo(num);
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

    public String toString() {
        return SUPPLIER_TENCENT_SERVER_OPTION;
    }
}
