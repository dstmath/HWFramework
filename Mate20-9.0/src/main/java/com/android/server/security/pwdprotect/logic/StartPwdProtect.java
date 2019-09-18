package com.android.server.security.pwdprotect.logic;

import android.text.TextUtils;
import android.util.Log;
import com.android.server.security.pwdprotect.model.PasswordIvsCache;
import com.android.server.security.pwdprotect.utils.DeviceEncryptUtils;
import com.android.server.security.pwdprotect.utils.EncryptUtils;
import com.android.server.security.pwdprotect.utils.FileUtils;
import com.android.server.security.pwdprotect.utils.HashUtils;
import com.android.server.security.pwdprotect.utils.StringUtils;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class StartPwdProtect {
    private static final byte[] HMACKEY = "HMACKEY".getBytes(Charset.forName("UTF-8"));
    private static final String TAG = "PwdProtectService";
    private byte[] mPin2Iv;
    private PrivateKey mPrivateKey;
    private byte[] mSalt;
    private byte[] mSecretKeyIV;

    private void init() {
        this.mSecretKeyIV = StringUtils.createRandomIvBytes();
        this.mPin2Iv = StringUtils.createRandomIvBytes();
        this.mSalt = StringUtils.createRandomIvBytes();
        DeviceEncryptUtils.createKey();
        DeviceEncryptUtils.createHmacKey();
    }

    public boolean turnOnPwdProtect(String privSpacePw, String pwQuestion, String pwQuestionAnswer, String mainSpacePin) {
        if (TextUtils.isEmpty(privSpacePw)) {
            return false;
        }
        init();
        KeyPair keyPair = EncryptUtils.generateRSAKeyPair(2048);
        PublicKey publicKey = keyPair.getPublic();
        this.mPrivateKey = keyPair.getPrivate();
        byte[] enPrivSpaceValues = EncryptUtils.encryptData(privSpacePw.getBytes(Charset.forName("UTF-8")), publicKey);
        if (enPrivSpaceValues.length == 0) {
            Log.e(TAG, "RSA encrypt PrivSpacePwd failed");
            return false;
        } else if (!savePrivSpaceEncryFile(enPrivSpaceValues, publicKey.getEncoded())) {
            Log.e(TAG, "save PrivSpaceEncryFile failed");
            return false;
        } else {
            Log.i(TAG, "begin savePwQ success");
            if (!savePwQ(pwQuestion)) {
                Log.e(TAG, "save savePwQ failed");
                return false;
            }
            Log.i(TAG, "begin savePwdQAnswer success");
            if (!savePwdQAnswer(pwQuestionAnswer)) {
                Log.e(TAG, "save savePwdQAnswer failed");
                return false;
            }
            Log.i(TAG, "begin saveMainSpaceEncryFile success");
            if (saveMainSpaceEncryFile(mainSpacePin, pwQuestionAnswer)) {
                return true;
            }
            Log.e(TAG, "save saveMainSpaceEncryFile failed");
            return false;
        }
    }

    private byte[] calPwQuestionHash(String pwQuestionAnswer) {
        return HashUtils.calHash256(pwQuestionAnswer.getBytes(Charset.forName("UTF-8")));
    }

    private byte[] encrySecretKey(String pwQuestionAnswer) {
        byte[] k1 = HashUtils.encryHmacSha256(pwQuestionAnswer.getBytes(Charset.forName("UTF-8")), HMACKEY);
        byte[] E_SK1 = new byte[0];
        if (this.mPrivateKey != null) {
            E_SK1 = EncryptUtils.aesCbcEncode(this.mPrivateKey.getEncoded(), k1, this.mSecretKeyIV);
        }
        if (E_SK1.length == 0) {
            Log.e(TAG, "AES encrypt SecretKey failed");
        }
        return E_SK1;
    }

    private byte[] encryMainSpacePin(String mainSpacePin, String pwQuestionAnswer) {
        byte[] E_SK2_value = EncryptUtils.aesCbcEncode(encrySecretKey(pwQuestionAnswer), HashUtils.pkdfEncry(mainSpacePin, this.mSalt), this.mPin2Iv);
        if (E_SK2_value.length != 0) {
            return E_SK2_value;
        }
        Log.e(TAG, "AES encrypt E_SK1 failed");
        return new byte[0];
    }

    private boolean saveMainSpaceEncryFile(String mainSpacePin, String pwQuestionAnswer) {
        byte[] eSk2Encrypt = DeviceEncryptUtils.deviceEncode(encryMainSpacePin(mainSpacePin, pwQuestionAnswer));
        if (eSk2Encrypt.length == 0) {
            Log.e(TAG, "Device encrypt E_SK2 failed");
            return false;
        }
        byte[] IvKeyVales = StringUtils.byteMerger(this.mSecretKeyIV, this.mPin2Iv, this.mSalt, eSk2Encrypt);
        FileUtils.writeFile(StringUtils.byteMerger(IvKeyVales, DeviceEncryptUtils.hmacSign(IvKeyVales)), PasswordIvsCache.FILE_E_SK2);
        return true;
    }

    private boolean savePrivSpaceEncryFile(byte[] ePrivSpaceValues, byte[] publicKey) {
        byte[] ePrivSpaceValues2 = DeviceEncryptUtils.deviceEncode(ePrivSpaceValues);
        if (ePrivSpaceValues2.length == 0) {
            Log.e(TAG, "Device encrypt E_PIN2 failed");
            return false;
        }
        byte[] IvKeyVales = StringUtils.byteMerger(ePrivSpaceValues2, publicKey);
        FileUtils.writeFile(StringUtils.byteMerger(IvKeyVales, DeviceEncryptUtils.hmacSign(IvKeyVales)), PasswordIvsCache.FILE_E_PIN2);
        return true;
    }

    private boolean savePwdQAnswer(String pwQuestionAnswer) {
        byte[] pwdQAnswerHashEncry = DeviceEncryptUtils.deviceEncode(calPwQuestionHash(pwQuestionAnswer));
        if (pwdQAnswerHashEncry.length == 0) {
            Log.e(TAG, "Device encrypt PwdQAnswer failed");
            return false;
        }
        FileUtils.writeFile(StringUtils.byteMerger(pwdQAnswerHashEncry, DeviceEncryptUtils.hmacSign(pwdQAnswerHashEncry)), PasswordIvsCache.FILE_E_PWDQANSWER);
        return true;
    }

    private boolean savePwQ(String pwQuestion) {
        byte[] pwdQHashEncry = DeviceEncryptUtils.deviceEncode(pwQuestion.getBytes(Charset.forName("UTF-8")));
        if (pwdQHashEncry.length == 0) {
            Log.e(TAG, "Device encrypt PwQ failed");
            return false;
        }
        FileUtils.writeFile(StringUtils.byteMerger(pwdQHashEncry, DeviceEncryptUtils.hmacSign(pwdQHashEncry)), PasswordIvsCache.FILE_E_PWDQ);
        return true;
    }
}
