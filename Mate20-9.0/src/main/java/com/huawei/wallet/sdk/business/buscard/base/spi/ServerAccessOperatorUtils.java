package com.huawei.wallet.sdk.business.buscard.base.spi;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.AppletInfoApiFactory;
import com.huawei.wallet.sdk.business.buscard.base.model.CardInfo;
import com.huawei.wallet.sdk.business.buscard.model.TerminalInfo;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.request.BaseRequest;
import com.huawei.wallet.sdk.common.utils.EMUIBuildUtil;
import com.huawei.wallet.sdk.common.utils.TimeUtil;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import java.security.SecureRandom;

public class ServerAccessOperatorUtils {
    private static final String SIGN_KEY = "TDID";

    private static class Instance {
        /* access modifiers changed from: private */
        public static ServerAccessOperatorUtils mInstance = new ServerAccessOperatorUtils();

        private Instance() {
        }
    }

    private ServerAccessOperatorUtils() {
    }

    public static ServerAccessOperatorUtils getInstance() {
        return Instance.mInstance;
    }

    public String getOrderPayType(int type) {
        if (type == 3) {
            return BaseRequest.PAY_TYPE_HUAWEIPAY_UNION;
        }
        if (type == 2) {
            return BaseRequest.PAY_TYPE_WECHAT;
        }
        if (type == 4) {
            return BaseRequest.PAY_TYPE_HUAWEIPAY_WALLET;
        }
        return "Huaweipay";
    }

    public String encodeCardBalance(int amount, String udid1, String deviceId1, String secondeDeviceId1, String packageName) {
        String cardBalance = String.valueOf(amount);
        String number = String.format("%04d", new Object[]{Integer.valueOf(new SecureRandom().nextInt(EMUIBuildUtil.VERSION_CODES.CUR_DEVELOPMENT))});
        String salt = TimeUtil.getFormatTime(TimeUtil.YEAR_TO_MSEL_NO_LINE) + number + cardBalance;
        byte[] digest = TerminalInfo.getSign(SIGN_KEY, salt);
        if (digest == null || digest.length <= 0) {
            String str = packageName;
            LogX.i("digest null");
            return null;
        }
        String digestStr = Base64.encodeToString(digest, 10);
        StringBuilder sb = new StringBuilder();
        String udid = udid1;
        String sn = PhoneDeviceUtil.getSerialNumber();
        if (TextUtils.isEmpty(udid) || TextUtils.isEmpty(sn)) {
            String deviceId = deviceId1;
            if (!TextUtils.isEmpty(deviceId)) {
                String secondeDeviceId = secondeDeviceId1;
                if (TextUtils.isEmpty(secondeDeviceId)) {
                    secondeDeviceId = "";
                }
                sb.append(deviceId);
                sb.append("|");
                sb.append(secondeDeviceId);
                sb.append("|");
                LogX.i("encodeSpare");
            } else {
                String str2 = packageName;
                LogX.i("deviceId null");
                return null;
            }
        } else {
            sb.append(udid);
            sb.append("|");
            sb.append(sn);
            sb.append("|");
            LogX.i("encodeFirst");
        }
        sb.append(salt);
        sb.append("|");
        String emmccId = TerminalInfo.getEMMCID();
        if (TextUtils.isEmpty(emmccId)) {
            LogX.i("emccId null");
            return null;
        }
        sb.append(emmccId);
        sb.append("|");
        sb.append(digestStr);
        sb.append("|");
        sb.append(packageName);
        return sb.toString();
    }

    public static String getCardNum(int dataType, String aid, String productId, Context context) {
        boolean isLogicNum;
        String cardNum = "";
        if (TextUtils.isEmpty(aid) || TextUtils.isEmpty(productId) || context == null) {
            return cardNum;
        }
        if (dataType == 1) {
            isLogicNum = false;
        } else if (dataType != 64) {
            return cardNum;
        } else {
            isLogicNum = true;
        }
        CardInfo cardInfo = AppletInfoApiFactory.createAppletCardInfoReader(context).readTrafficCardInfo(aid, productId, dataType).getData();
        if (cardInfo == null) {
            LogX.i("cardNum error");
        } else if (isLogicNum) {
            cardNum = cardInfo.getLogicCardNum();
        } else {
            cardNum = cardInfo.getCardNum();
        }
        return cardNum;
    }
}
