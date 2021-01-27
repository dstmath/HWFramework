package com.huawei.server.security.pwdprotect.logic;

import android.text.TextUtils;
import android.util.Log;
import com.huawei.server.security.pwdprotect.model.PasswordIvsCache;
import com.huawei.server.security.pwdprotect.utils.DeviceEncryptUtils;
import com.huawei.server.security.pwdprotect.utils.EncryptUtils;
import com.huawei.server.security.pwdprotect.utils.FileUtils;
import com.huawei.server.security.pwdprotect.utils.HashUtils;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Optional;

public class ResetPassWord {
    private static final String EMPTY_STRING = "";
    private static final byte[] HMAC_KEY = "HMACKEY".getBytes(StandardCharsets.UTF_8);
    private static final int IV_FIRST_E_PIN2 = 0;
    private static final int IV_FIRST_E_PWD_QUESTION = 0;
    private static final int IV_FIRST_E_PWD_QUESTION_ANSWER = 0;
    private static final int IV_FIRST_E_SK2 = 0;
    private static final int IV_FOUR_E_SK2 = 3;
    private static final int IV_SECOND_E_SK2 = 1;
    private static final int IV_THIRD_E_SK2 = 2;
    private static final String TAG = "PwdProtectService";

    public static String decodeCurrentPwd(String mainSpacePin, String pwdQuestionAnswer) {
        if (TextUtils.isEmpty(mainSpacePin) || TextUtils.isEmpty(pwdQuestionAnswer)) {
            Log.e(TAG, "decodeCurrentPwd: Invalid input!");
            return null;
        } else if (!FileUtils.verifyFile()) {
            Log.e(TAG, "decodeCurrentPwd: VerifyFile failed!");
            return null;
        } else {
            byte[] decodedSecretKey = decodeEncryptedSecKey(mainSpacePin);
            if (decodedSecretKey.length == 0) {
                Log.e(TAG, "decodeCurrentPwd: Decode E_SK2 failed!");
                return null;
            }
            byte[] delZeroSk = decodeEncryptedAnswer(decodedSecretKey, pwdQuestionAnswer);
            if (delZeroSk.length == 0) {
                Log.e(TAG, "decodeCurrentPwd: Decode secretKey failed!");
                return null;
            }
            byte[] originalPwd = decodeEncryptedPin(delZeroSk);
            if (originalPwd.length == 0) {
                Log.e(TAG, "decodeCurrentPwd: Decode E_PIN2 failed!");
                return null;
            }
            Log.i(TAG, "decodeCurrentPwd: Decode originalPwd success!");
            return new String(originalPwd, StandardCharsets.UTF_8);
        }
    }

    public static boolean verifyPwdQuestionAnswer(byte[] questionAnswer) {
        if (questionAnswer == null || questionAnswer.length == 0) {
            Log.e(TAG, "verifyPwdQuestionAnswer: questionAnswer is null!");
            return false;
        }
        byte[] pwdQuestionAnswerHash = HashUtils.calculateHashWithSha256(questionAnswer);
        byte[] pwdAnswerHashDecode = DeviceEncryptUtils.decodeWithDeviceKey(FileUtils.readKeys(PasswordIvsCache.FILE_E_PWDQANSWER), FileUtils.readIvs(PasswordIvsCache.FILE_E_PWDQANSWER, 0));
        if (pwdAnswerHashDecode.length != 0) {
            return Arrays.equals(pwdAnswerHashDecode, pwdQuestionAnswerHash);
        }
        Log.e(TAG, "verifyPwdQuestionAnswer: DeviceDecode pwdAnswerHashDecode failed!");
        return false;
    }

    public static String getPwdQuestion() {
        byte[] pwdQuestionDecode = DeviceEncryptUtils.decodeWithDeviceKey(FileUtils.readKeys(PasswordIvsCache.FILE_E_PWDQ), FileUtils.readIvs(PasswordIvsCache.FILE_E_PWDQ, 0));
        if (pwdQuestionDecode.length != 0) {
            return new String(pwdQuestionDecode, StandardCharsets.UTF_8);
        }
        Log.e(TAG, "getPwdQuestion: Failed, failed to decode the password question!");
        return "";
    }

    private static byte[] decodeEncryptedSecKey(String mainSpacePin) {
        if (TextUtils.isEmpty(mainSpacePin)) {
            Log.e(TAG, "decodeEncryptedSecKey: DecodeEncryptedSecKey failed, input is invalid!");
            return new byte[0];
        }
        byte[] mainSpacePinHash = HashUtils.calculateHashWithPbkdf(mainSpacePin, FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 2));
        if (mainSpacePinHash.length == 0) {
            Log.e(TAG, "decodeEncryptedSecKey: DecodeEncryptedSecKey failed, mainSpacePinHash is null!");
            return new byte[0];
        }
        byte[] secretKey = DeviceEncryptUtils.decodeWithDeviceKey(FileUtils.readKeys(PasswordIvsCache.FILE_E_SK2), FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 3));
        if (secretKey.length != 0) {
            return EncryptUtils.decodeWithAes(secretKey, mainSpacePinHash, FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 1));
        }
        Log.e(TAG, "decodeEncryptedSecKey: DecodeEncryptedSecKey device encrypt failed!");
        return new byte[0];
    }

    private static byte[] decodeEncryptedAnswer(byte[] secKey, String pwdQuestionAnswer) {
        byte[] hash = HashUtils.calculateHashWithHmacSha256(pwdQuestionAnswer.getBytes(StandardCharsets.UTF_8), HMAC_KEY);
        if (hash.length != 0) {
            return EncryptUtils.decodeWithAes(secKey, hash, FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 0));
        }
        Log.e(TAG, "decodeEncryptedAnswer: DecodeEncryptedAnswer HmacSha256 failed!");
        return new byte[0];
    }

    private static byte[] decodeEncryptedPin(byte[] delZeroSk) {
        byte[] decodedPin2 = DeviceEncryptUtils.decodeWithDeviceKey(FileUtils.readKeys(PasswordIvsCache.FILE_E_PIN2), FileUtils.readIvs(PasswordIvsCache.FILE_E_PIN2, 0));
        if (decodedPin2.length == 0) {
            Log.e(TAG, "decodeEncryptedPin: DecodeEncryptedPin deviceDecode failed!");
            return new byte[0];
        }
        Optional<PrivateKey> privateKey = EncryptUtils.getPrivateKey(delZeroSk);
        if (privateKey.isPresent()) {
            return EncryptUtils.decryptData(decodedPin2, privateKey.get());
        }
        Log.e(TAG, "decodeEncryptedPin: DecodeEncryptedPin privateKey is null!");
        return new byte[0];
    }
}
