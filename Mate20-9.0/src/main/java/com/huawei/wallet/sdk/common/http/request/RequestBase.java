package com.huawei.wallet.sdk.common.http.request;

import com.huawei.wallet.sdk.common.AppConfig;
import com.huawei.wallet.sdk.common.utils.TimeUtil;
import java.security.SecureRandom;
import java.util.Date;

public class RequestBase {
    private String aesKey;
    private String huaweirsaIndex;
    private String merchantID = AppConfig.MERCHANT_ID;
    private int rsaKeyIndex = -1;
    private String srcTransactionID = generateSrcId();

    public static String generateSrcId() {
        int randomNumber = (new SecureRandom().nextInt(10000000) + 10000000) % 10000000;
        if (randomNumber < 1000000) {
            randomNumber += 1000000;
        }
        return TimeUtil.formatDate2String(new Date(System.currentTimeMillis()), TimeUtil.YEAR_TO_MSEL_NO_LINE) + randomNumber;
    }

    public String getMerchantID() {
        return this.merchantID;
    }

    public void setMerchantID(String merchantID2) {
        this.merchantID = merchantID2;
    }

    public int getRsaKeyIndex() {
        return this.rsaKeyIndex;
    }

    public void setRsaKeyIndex(int rsaKeyIndex2) {
        this.rsaKeyIndex = rsaKeyIndex2;
    }

    public String getHuaweirsaIndex() {
        return this.huaweirsaIndex;
    }

    public void setHuaweirsaIndex(String huaweirsaIndex2) {
        this.huaweirsaIndex = huaweirsaIndex2;
    }

    public String getAesKey() {
        return this.aesKey;
    }

    public void setAesKey(String aesKey2) {
        this.aesKey = aesKey2;
    }

    public String getSrcTransactionID() {
        return this.srcTransactionID;
    }

    public void setSrcTransactionID(String srcTransactionID2) {
        this.srcTransactionID = srcTransactionID2;
    }
}
