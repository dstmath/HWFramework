package com.huawei.wallet.sdk.common.apdu.manager;

import android.text.TextUtils;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.crypto.AES;

public abstract class BaseAesManager {
    public static final int CURRENT_VERSION = 2;
    protected String mPersistentAesKey;
    protected String mTempAesIv;
    protected String mTempAesKey;

    private static class TempKeyIvKeeper {
        private String mTempAesIv;
        private String mTempAesKey;

        private static class Singletone {
            public static final TempKeyIvKeeper INSTANCE = new TempKeyIvKeeper();

            private Singletone() {
            }
        }

        public String getTempAesKey() {
            return this.mTempAesKey;
        }

        public String getTempAesIv() {
            return this.mTempAesIv;
        }

        private TempKeyIvKeeper() {
            this.mTempAesKey = null;
            this.mTempAesIv = null;
            this.mTempAesKey = AES.getaeskey();
            this.mTempAesIv = AES.getAesIV();
        }

        public static TempKeyIvKeeper getInstance() {
            return Singletone.INSTANCE;
        }
    }

    /* access modifiers changed from: protected */
    public abstract String getPersistentAesKey();

    protected BaseAesManager() {
        this.mTempAesKey = null;
        this.mTempAesIv = null;
        this.mPersistentAesKey = null;
        this.mTempAesKey = TempKeyIvKeeper.getInstance().getTempAesKey();
        this.mTempAesIv = TempKeyIvKeeper.getInstance().getTempAesIv();
        this.mPersistentAesKey = getPersistentAesKey();
    }

    /* access modifiers changed from: protected */
    public String recycleLeftMoveBit(String sourceStr, int moveIndex) {
        if (sourceStr == null || moveIndex >= 8) {
            return sourceStr;
        }
        byte[] sourceBytes = AES.asBin(sourceStr);
        int length = sourceBytes.length;
        byte[] tagetBytes = new byte[length];
        for (int i = 0; i < length; i++) {
            tagetBytes[i] = (byte) (((sourceBytes[i] & 255) << moveIndex) | ((sourceBytes[i] & 255) >>> (8 - moveIndex)));
        }
        return AES.asHex(tagetBytes);
    }

    public String encryptInAppLifeCycle(String content) {
        return AES.encryptToBase64WithIv(content, this.mTempAesKey, this.mTempAesIv);
    }

    public String descryptInAppLifeCycle(String content) {
        return AES.decryptFromBase64WithIv(content, this.mTempAesKey, this.mTempAesIv);
    }

    public String encryptPersistent(String content) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        String aesIv = AES.getAesIV();
        String encryptContent = AES.encryptToBase64WithIv(content, this.mPersistentAesKey, aesIv);
        if (TextUtils.isEmpty(encryptContent)) {
            return null;
        }
        return encryptContent + ":" + aesIv;
    }

    public String descryptPersistent(String content) {
        if (TextUtils.isEmpty(content)) {
            LogC.e("BaseAesManager descryptPersistent, content is null", false);
            return null;
        }
        String[] contentArray = content.split(":");
        if (contentArray.length <= 1) {
            return null;
        }
        return AES.decryptFromBase64WithIv(contentArray[0], this.mPersistentAesKey, contentArray[1]);
    }
}
