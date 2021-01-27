package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import java.security.Provider;
import javax.crypto.Cipher;

class CipherHook {
    private static final String TAG = CipherHook.class.getSimpleName();

    CipherHook() {
    }

    @HookMethod(name = "createCipher", params = {String.class, Provider.class}, targetClass = Cipher.class)
    static Cipher createCipherHook(String transformation, Provider provider) {
        Log.e(TAG, "Call System Hook Method: Cipher createCipherHook()");
        return createCipherBackup(transformation, provider);
    }

    @BackupMethod(name = "createCipher", params = {String.class, Provider.class}, targetClass = Cipher.class)
    static Cipher createCipherBackup(String transformation, Provider provider) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: Cipher createCipherBackup().");
        return null;
    }
}
