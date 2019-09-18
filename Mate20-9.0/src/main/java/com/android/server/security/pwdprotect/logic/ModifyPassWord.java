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

public class ModifyPassWord {
    private static final String TAG = "PwdProtectService";

    public static boolean modifyMainSpacePw(String origPassword, String newPassword) {
        if (TextUtils.isEmpty(origPassword) || TextUtils.isEmpty(newPassword)) {
            Log.e(TAG, "origPd or newPd is null");
            return false;
        }
        byte[] pin1PkdfOrigPw = HashUtils.pkdfEncry(origPassword, FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 2));
        byte[] E_SK2_Decode = DeviceEncryptUtils.deviceDecode(FileUtils.readKeys(PasswordIvsCache.FILE_E_SK2), FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 3));
        if (E_SK2_Decode.length == 0) {
            Log.e(TAG, "E_SK2_Decode Decode Error");
            return false;
        }
        byte[] E_SK1 = EncryptUtils.aesCbcDecode(E_SK2_Decode, pin1PkdfOrigPw, FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 1));
        if (E_SK1.length == 0) {
            Log.e(TAG, "E_SK1 Decode Error");
            return false;
        }
        byte[] mE_SK2 = EncryptUtils.aesCbcEncode(E_SK1, HashUtils.pkdfEncry(newPassword, FileUtils.readFile(PasswordIvsCache.FILE_E_SK2, 2)), FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 1));
        if (mE_SK2.length == 0) {
            Log.e(TAG, "mE_SK2 Encode Error");
            return false;
        }
        byte[] mE_SK2_Encode = DeviceEncryptUtils.deviceEncode(mE_SK2);
        if (mE_SK2_Encode.length == 0) {
            Log.e(TAG, "mE_SK2_Encode Encode Error");
            return false;
        }
        byte[] IvKeyVales = StringUtils.byteMerger(FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 0), FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 1), FileUtils.readIvs(PasswordIvsCache.FILE_E_SK2, 2), mE_SK2_Encode);
        FileUtils.writeFile(StringUtils.byteMerger(IvKeyVales, DeviceEncryptUtils.hmacSign(IvKeyVales)), PasswordIvsCache.FILE_E_SK2);
        return true;
    }

    public static boolean modifyPrivSpacePw(String newPassword) {
        if (TextUtils.isEmpty(newPassword)) {
            Log.e(TAG, "newPd is null");
            return false;
        }
        byte[] publicKey = FileUtils.readPublicKey();
        if (publicKey.length == 0) {
            Log.e(TAG, "encryptE_PIN2 readPublicKey failed ");
            return false;
        }
        byte[] IvKeyVales = StringUtils.byteMerger(DeviceEncryptUtils.deviceEncode(EncryptUtils.encryptData(newPassword.getBytes(Charset.forName("UTF-8")), EncryptUtils.getPublicKey(publicKey))), publicKey);
        FileUtils.writeFile(StringUtils.byteMerger(IvKeyVales, DeviceEncryptUtils.hmacSign(IvKeyVales)), PasswordIvsCache.FILE_E_PIN2);
        return true;
    }
}
