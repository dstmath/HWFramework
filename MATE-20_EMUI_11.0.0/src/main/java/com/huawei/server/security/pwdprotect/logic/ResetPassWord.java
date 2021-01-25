package com.huawei.server.security.pwdprotect.logic;

import android.text.TextUtils;
import android.util.Log;
import com.huawei.hwpartsecurityservices.BuildConfig;
import com.huawei.server.security.pwdprotect.model.PasswordIvsCache;
import com.huawei.server.security.pwdprotect.utils.DeviceEncryptUtils;
import com.huawei.server.security.pwdprotect.utils.EncryptUtils;
import com.huawei.server.security.pwdprotect.utils.FileUtils;
import com.huawei.server.security.pwdprotect.utils.HashUtils;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ResetPassWord {
    private static final byte[] HMACKEY = "HMACKEY".getBytes(Charset.forName("UTF-8"));
    private static final String TAG = "PwdProtectService";

    public static String decodeCurrentPwd(String mainSpacePin, String pwdQuestionAnswer) {
        if (TextUtils.isEmpty(mainSpacePin) || TextUtils.isEmpty(pwdQuestionAnswer) || !FileUtils.verifyFile().booleanValue()) {
            Log.e(TAG, "verifyFile failed ");
            return null;
        }
        byte[] E_SK1 = decodeE_SK2(mainSpacePin);
        if (E_SK1.length == 0) {
            Log.e(TAG, "decode E_SK2 failed");
            return null;
        }
        byte[] delZeroSk = decodeE_SK1(E_SK1, pwdQuestionAnswer);
        if (delZeroSk.length == 0) {
            Log.e(TAG, "decode E_SK1 failed");
            return null;
        }
        byte[] origPwd = decodeE_PIN2(delZeroSk);
        if (origPwd.length == 0) {
            Log.e(TAG, "decode E_PIN2 failed");
            return null;
        }
        Log.i(TAG, "decode origPd success");
        try {
            return new String(origPwd, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException");
            return BuildConfig.FLAVOR;
        }
    }

    public static Boolean pwdQAnswerVertify(byte[] pwQuestion) {
        byte[] pwdQAnswerHash = HashUtils.calHash256(pwQuestion);
        byte[] pwdAnswerHashDecode = DeviceEncryptUtils.deviceDecode(FileUtils.readKeys(PasswordIvsCache.FILE_E_PWDQANSWER), FileUtils.readIvs(PasswordIvsCache.FILE_E_PWDQANSWER, 0));
        if (pwdAnswerHashDecode.length != 0) {
            return Boolean.valueOf(Arrays.equals(pwdAnswerHashDecode, pwdQAnswerHash));
        }
        Log.e(TAG, "deviceDecode pwdAnswerHashDecode failed");
        return false;
    }

    public static String getPwdQuestion() {
        try {
            byte[] pwdQuestionDecode = DeviceEncryptUtils.deviceDecode(FileUtils.readKeys(PasswordIvsCache.FILE_E_PWDQ), FileUtils.readIvs(PasswordIvsCache.FILE_E_PWDQ, 0));
            if (pwdQuestionDecode.length != 0) {
                return new String(pwdQuestionDecode, "utf-8");
            }
            Log.e(TAG, "getPwdQuestion failed pwdQuestionDecode is null");
            return null;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "getPwdQuestion failed  UnsupportedEncodingException");
            return null;
        }
    }

    public static byte[] decodeE_SK2(String mainSpacePin) {
        byte[] mainSpacePinPkdf = HashUtils.pkdfEncry(mainSpacePin, FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 2));
        if (mainSpacePinPkdf.length == 0) {
            Log.e(TAG, "decodeE_SK2 failed mainSpacePinPkdf is null ");
            return new byte[0];
        }
        byte[] E_SK2_Decode = DeviceEncryptUtils.deviceDecode(FileUtils.readKeys(PasswordIvsCache.FILE_E_SK2), FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 3));
        if (E_SK2_Decode.length != 0) {
            return EncryptUtils.aesCbcDecode(E_SK2_Decode, mainSpacePinPkdf, FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 1));
        }
        Log.e(TAG, "decodeE_SK2 device encrypt failed");
        return new byte[0];
    }

    public static byte[] decodeE_SK1(byte[] E_SK1, String pwdQuestionAnswer) {
        byte[] k1 = HashUtils.encryHmacSha256(pwdQuestionAnswer.getBytes(Charset.forName("UTF-8")), HMACKEY);
        if (k1.length != 0) {
            return EncryptUtils.aesCbcDecode(E_SK1, k1, FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 0));
        }
        Log.e(TAG, "decodeE_SK1 HmacSha256 failed ");
        return new byte[0];
    }

    private static byte[] decodeE_PIN2(byte[] delZeroSk) {
        byte[] E_PIN2_decode = DeviceEncryptUtils.deviceDecode(FileUtils.readKeys(PasswordIvsCache.FILE_E_PIN2), FileUtils.readIvs(PasswordIvsCache.FILE_E_PIN2, 0));
        if (E_PIN2_decode.length != 0) {
            return EncryptUtils.decryptData(E_PIN2_decode, EncryptUtils.getPrivateKey(delZeroSk));
        }
        Log.e(TAG, "decodeE_PIN2 deviceDecode failed ");
        return new byte[0];
    }
}
