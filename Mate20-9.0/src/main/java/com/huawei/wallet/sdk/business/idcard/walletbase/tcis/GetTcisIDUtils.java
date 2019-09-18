package com.huawei.wallet.sdk.business.idcard.walletbase.tcis;

import android.os.Bundle;
import android.text.TextUtils;
import android.trustcircle.TrustCircleManager;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;

public class GetTcisIDUtils {
    public static boolean canGetTcisID() {
        try {
            Bundle bundle = TrustCircleManager.getInstance().getTcisInfo();
            if (bundle == null) {
                LogX.d("tcisId get fail", false);
                return false;
            } else if (!TextUtils.isEmpty(bundle.getString("tcisID"))) {
                return true;
            } else {
                LogX.d("tcisId get fail null", false);
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
