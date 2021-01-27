package com.huawei.server.security.pwdprotect.logic;

import android.text.TextUtils;
import android.util.Log;
import com.huawei.server.security.pwdprotect.model.PasswordIvsCache;
import com.huawei.server.security.pwdprotect.utils.DeviceEncryptUtils;
import com.huawei.server.security.pwdprotect.utils.EncryptUtils;
import com.huawei.server.security.pwdprotect.utils.FileUtils;
import com.huawei.server.security.pwdprotect.utils.HashUtils;
import com.huawei.server.security.pwdprotect.utils.StringUtils;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Optional;

public class StartPwdProtect {
    private static final byte[] HMAC_KEY = "HMACKEY".getBytes(StandardCharsets.UTF_8);
    private static final int KEY_LENGTH = 2048;
    private static final String TAG = "PwdProtectService";
    private byte[] mPinIv;
    private PrivateKey mPrivateKey;
    private byte[] mSalt;
    private byte[] mSecretKeyIv;

    public boolean turnOnPwdProtect(String privateSpacePwd, String pwdQuestion, String pwdQuestionAnswer, String mainSpacePin) {
        if (TextUtils.isEmpty(privateSpacePwd) || TextUtils.isEmpty(pwdQuestion) || TextUtils.isEmpty(pwdQuestionAnswer) || TextUtils.isEmpty(mainSpacePin)) {
            Log.e(TAG, "turnOnPwdProtect: TurnOnPwdProtect failed, invalid input!");
            return false;
        }
        init();
        Optional<KeyPair> keyPair = EncryptUtils.generateRsaKeyPair(KEY_LENGTH);
        if (!keyPair.isPresent()) {
            Log.e(TAG, "turnOnPwdProtect: TurnOnPwdProtect failed, keyPair is null!");
            return false;
        }
        PublicKey publicKey = keyPair.get().getPublic();
        this.mPrivateKey = keyPair.get().getPrivate();
        byte[] encryptedPrivateSpaceValues = EncryptUtils.encryptData(privateSpacePwd.getBytes(StandardCharsets.UTF_8), publicKey);
        if (encryptedPrivateSpaceValues.length == 0) {
            Log.e(TAG, "turnOnPwdProtect: RSA encrypts private space password failed!");
            return false;
        } else if (!encryptPrivateSpacePwd(encryptedPrivateSpaceValues, publicKey.getEncoded())) {
            Log.e(TAG, "turnOnPwdProtect: Save private space entry file failed!");
            return false;
        } else if (!encryptPwdQuestion(pwdQuestion)) {
            Log.e(TAG, "turnOnPwdProtect: Save password question failed");
            return false;
        } else if (!encryptPwdQuestionAnswer(pwdQuestionAnswer)) {
            Log.e(TAG, "turnOnPwdProtect: Save password question answer failed");
            return false;
        } else if (!encryptWithDevice(mainSpacePin, pwdQuestionAnswer)) {
            Log.e(TAG, "turnOnPwdProtect: Save main space entry file failed!");
            return false;
        } else {
            Log.i(TAG, "turnOnPwdProtect: Turn on password protect successfully.");
            return true;
        }
    }

    private void init() {
        this.mSecretKeyIv = StringUtils.createRandomIvBytes();
        this.mPinIv = StringUtils.createRandomIvBytes();
        this.mSalt = StringUtils.createRandomIvBytes();
        DeviceEncryptUtils.generateSecretKey();
        DeviceEncryptUtils.generateHmacKey();
    }

    private byte[] calPwdQuestionHash(String pwQuestionAnswer) {
        return HashUtils.calculateHashWithSha256(pwQuestionAnswer.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] encryptSecretKey(String pwQuestionAnswer) {
        byte[] secretKey = HashUtils.calculateHashWithHmacSha256(pwQuestionAnswer.getBytes(StandardCharsets.UTF_8), HMAC_KEY);
        byte[] encryptSecretKey = new byte[0];
        PrivateKey privateKey = this.mPrivateKey;
        if (privateKey != null) {
            encryptSecretKey = EncryptUtils.encodeWithAes(privateKey.getEncoded(), secretKey, this.mSecretKeyIv);
        }
        if (encryptSecretKey.length == 0) {
            Log.e(TAG, "encryptSecretKey: AES encrypts secretKey failed!");
        }
        return encryptSecretKey;
    }

    private byte[] encryptMainSpacePin(String mainSpacePin, String pwQuestionAnswer) {
        byte[] encryptSecretKey = EncryptUtils.encodeWithAes(encryptSecretKey(pwQuestionAnswer), HashUtils.calculateHashWithPbkdf(mainSpacePin, this.mSalt), this.mPinIv);
        if (encryptSecretKey.length != 0) {
            return encryptSecretKey;
        }
        Log.e(TAG, "encryptMainSpacePin: AES encrypts secret key failed!");
        return new byte[0];
    }

    private boolean encryptWithDevice(String mainSpacePin, String pwdQuestionAnswer) {
        byte[] encryptedSecretKey = DeviceEncryptUtils.encodeWithDeviceKey(encryptMainSpacePin(mainSpacePin, pwdQuestionAnswer));
        if (encryptedSecretKey.length == 0) {
            Log.e(TAG, "encryptWithDevice: Device encrypts E_SK2 failed!");
            return false;
        }
        byte[] ivKeyValues = StringUtils.concatByteArrays(this.mSecretKeyIv, this.mPinIv, this.mSalt, encryptedSecretKey);
        FileUtils.write(StringUtils.concatByteArrays(ivKeyValues, DeviceEncryptUtils.signWithHmac(ivKeyValues)), PasswordIvsCache.FILE_E_SK2);
        return true;
    }

    private boolean encryptPrivateSpacePwd(byte[] encryptedPrivateSpaceValues, byte[] publicKey) {
        byte[] privateSpaceValues = DeviceEncryptUtils.encodeWithDeviceKey(encryptedPrivateSpaceValues);
        if (privateSpaceValues.length == 0) {
            Log.e(TAG, "encryptPrivateSpacePwd: Device encrypts E_PIN2 failed!");
            return false;
        }
        byte[] ivKeyValues = StringUtils.concatByteArrays(privateSpaceValues, publicKey);
        FileUtils.write(StringUtils.concatByteArrays(ivKeyValues, DeviceEncryptUtils.signWithHmac(ivKeyValues)), PasswordIvsCache.FILE_E_PIN2);
        return true;
    }

    private boolean encryptPwdQuestionAnswer(String pwdQuestionAnswer) {
        byte[] pwdQuestionAnswerHashEntry = DeviceEncryptUtils.encodeWithDeviceKey(calPwdQuestionHash(pwdQuestionAnswer));
        if (pwdQuestionAnswerHashEntry.length == 0) {
            Log.e(TAG, "encryptPwdQuestionAnswer: Device encrypts password question answer failed!");
            return false;
        }
        FileUtils.write(StringUtils.concatByteArrays(pwdQuestionAnswerHashEntry, DeviceEncryptUtils.signWithHmac(pwdQuestionAnswerHashEntry)), PasswordIvsCache.FILE_E_PWDQANSWER);
        return true;
    }

    private boolean encryptPwdQuestion(String pwdQuestion) {
        byte[] pwdQuestionHashEntry = DeviceEncryptUtils.encodeWithDeviceKey(pwdQuestion.getBytes(StandardCharsets.UTF_8));
        if (pwdQuestionHashEntry.length == 0) {
            Log.e(TAG, "encryptPwdQuestion: Device encrypts password question failed!");
            return false;
        }
        FileUtils.write(StringUtils.concatByteArrays(pwdQuestionHashEntry, DeviceEncryptUtils.signWithHmac(pwdQuestionHashEntry)), PasswordIvsCache.FILE_E_PWDQ);
        return true;
    }
}
