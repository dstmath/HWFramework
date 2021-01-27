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
import java.security.PublicKey;
import java.util.Optional;

public class ModifyPassWord {
    private static final int IV_FIRST_E_SK2 = 0;
    private static final int IV_FOUR_E_SK2 = 3;
    private static final int IV_SECOND_E_SK2 = 1;
    private static final int IV_THIRD_E_SK2 = 2;
    private static final String TAG = "PwdProtectService";

    public static boolean modifyMainSpacePwd(String originalPassword, String newPassword) {
        if (TextUtils.isEmpty(originalPassword) || TextUtils.isEmpty(newPassword)) {
            Log.e(TAG, "modifyMainSpacePwd: Invalid input!");
            return false;
        }
        byte[] decryptedSecretKey = getSecretKey(originalPassword);
        if (decryptedSecretKey.length == 0) {
            Log.e(TAG, "modifyMainSpacePwd: ModifyMainSpacePwd, decryptedSecretKey decode error!");
            return false;
        }
        byte[] encodedKey = encryptSecretKey(decryptedSecretKey, newPassword);
        if (encodedKey.length == 0) {
            Log.e(TAG, "modifyMainSpacePwd: ModifyMainSpacePwd, encodedKey encode error!");
            return false;
        }
        byte[] ivKeyValues = StringUtils.concatByteArrays(FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 0), FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 1), FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 2), encodedKey);
        FileUtils.write(StringUtils.concatByteArrays(ivKeyValues, DeviceEncryptUtils.signWithHmac(ivKeyValues)), PasswordIvsCache.FILE_E_SK2);
        return true;
    }

    public static boolean modifyPrivateSpacePwd(String newPassword) {
        if (TextUtils.isEmpty(newPassword)) {
            Log.e(TAG, "modifyPrivateSpacePwd: NewPassword is null or empty!");
            return false;
        }
        byte[] publicKey = FileUtils.readPublicKey();
        if (publicKey.length == 0) {
            Log.e(TAG, "modifyPrivateSpacePwd: Read publicKey failed!");
            return false;
        }
        Optional<PublicKey> enPrivateSpaceValuesPublicKey = EncryptUtils.getPublicKey(publicKey);
        if (!enPrivateSpaceValuesPublicKey.isPresent()) {
            Log.e(TAG, "modifyPrivateSpacePwd: Get publicKey failed!");
            return false;
        }
        byte[] encryptedPrivateSpaceValues = EncryptUtils.encryptData(newPassword.getBytes(StandardCharsets.UTF_8), enPrivateSpaceValuesPublicKey.get());
        if (encryptedPrivateSpaceValues.length == 0) {
            Log.e(TAG, "modifyPrivateSpacePwd: Encrypt the password failed by rsa");
            return false;
        }
        byte[] encodedPrivateSpaceValues = DeviceEncryptUtils.encodeWithDeviceKey(encryptedPrivateSpaceValues);
        if (encodedPrivateSpaceValues.length == 0) {
            Log.e(TAG, "modifyPrivateSpacePwd: Device encrypts the password failed by aes");
            return false;
        }
        byte[] ivKeyValues = StringUtils.concatByteArrays(encodedPrivateSpaceValues, publicKey);
        FileUtils.write(StringUtils.concatByteArrays(ivKeyValues, DeviceEncryptUtils.signWithHmac(ivKeyValues)), PasswordIvsCache.FILE_E_PIN2);
        return true;
    }

    private static byte[] getSecretKey(String originalPassword) {
        byte[] passwordHash = HashUtils.calculateHashWithPbkdf(originalPassword, FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 2));
        byte[] decodedSecretKey = DeviceEncryptUtils.decodeWithDeviceKey(FileUtils.readKeys(PasswordIvsCache.FILE_E_SK2), FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 3));
        if (decodedSecretKey.length != 0) {
            return EncryptUtils.decodeWithAes(decodedSecretKey, passwordHash, FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 1));
        }
        Log.e(TAG, "getSecretKey: ModifyMainSpacePwd, decodedSecretKey decode error!");
        return new byte[0];
    }

    private static byte[] encryptSecretKey(byte[] key, String newPassword) {
        byte[] encodedSecretKey = EncryptUtils.encodeWithAes(key, HashUtils.calculateHashWithPbkdf(newPassword, FileUtils.readFile(PasswordIvsCache.FILE_E_SK2, 2)), FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 1));
        if (encodedSecretKey.length != 0) {
            return DeviceEncryptUtils.encodeWithDeviceKey(encodedSecretKey);
        }
        Log.e(TAG, "encryptSecretKey: ModifyMainSpacePwd, encodedSecretKey encode error!");
        return new byte[0];
    }
}
