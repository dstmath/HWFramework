package com.huawei.wallet.sdk.business.idcard.commonbase.util.log;

import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;

public class LogErrorConstant {
    public static final int DICS_QUERY_ERR = 907118175;
    private static final String ERR = "err";
    public static final int EXCEPTION_JSON_ERR = 907118110;
    public static final int EXCEPTION_NUMBERFORMAT_ERR = 907118102;
    public static final int GET_TCIS_SEESION_KEY_ERROR = 907125135;
    private static final int HWPAY_ERRORCODE_SEGMENT = 907118000;
    private static final String LOCATION = "loc";
    public static final int NET_RESPONSE_NULL_ERR = 907118056;
    public static final int NET_RESPONSE_OTHER_ERR = 907118057;
    public static final int NET_URI_FORMAT_ERR = 907118051;
    private static final int WALLET_ERRORCODE_SEGMENT = 907125000;

    public static Map<String, String> getLocalAndErrMap(String loc, String err) {
        Map<String, String> paramMap = null;
        if (!TextUtils.isEmpty(loc)) {
            paramMap = new HashMap<>();
            paramMap.put(LOCATION, loc);
        }
        if (!TextUtils.isEmpty(err)) {
            if (paramMap == null) {
                paramMap = new HashMap<>();
            }
            paramMap.put(ERR, err);
        }
        return paramMap;
    }
}
