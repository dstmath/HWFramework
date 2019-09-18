package com.huawei.wallet.sdk.business.idcard.walletbase.tcis;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.bankcard.util.PackageUtil;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.SessionKeyUtil;
import com.huawei.wallet.sdk.common.utils.crypto.HMACSHA256;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import org.json.JSONObject;

public class TCISParameterUtils {
    private static String TCIS_SERVICE = "com.huawei.trustcircle";
    private static AtomicBoolean isInitCheckTCISAtom = new AtomicBoolean(false);
    private static AtomicBoolean isInstallTCISAtom = new AtomicBoolean(false);

    public String reSignJsonData(JSONObject jsonData, Context mContext, boolean needCheckSign) {
        if (!isSupportTcis(mContext)) {
            return jsonData.toString();
        }
        JSONObject headerObject = jsonData.optJSONObject("header");
        if (headerObject == null) {
            return jsonData.toString();
        }
        String commandStrs = headerObject.optString("commander");
        if (needCheckSign && !SessionKeyUtil.needCheckSign(commandStrs)) {
            return jsonData.toString();
        }
        String tcisId = TcisManager.getInstance(mContext).getTcisID();
        if (!TextUtils.isEmpty(tcisId)) {
            try {
                headerObject.put("tcisid", tcisId);
                headerObject.put("sessionKeySign", HMACSHA256.hmac_256(SessionKeyUtil.buildSignData(jsonData.toString()), TcisManager.getInstance(mContext).getSeeionKeyByCloudServer(false)));
            } catch (JSONException e) {
                LogX.e("sessionKeySign get fail" + e.getMessage(), false);
            }
        }
        return jsonData.toString();
    }

    private static boolean isSupportTcis(Context context) {
        if (isInitCheckTCISAtom.get()) {
            return isInstallTCISAtom.get();
        }
        isInstallTCISAtom.set(Boolean.valueOf(Build.VERSION.SDK_INT > 26 && PackageUtil.isAppInstalled(context, TCIS_SERVICE) && GetTcisIDUtils.canGetTcisID()).booleanValue());
        isInitCheckTCISAtom.set(true);
        return isInstallTCISAtom.get();
    }
}
