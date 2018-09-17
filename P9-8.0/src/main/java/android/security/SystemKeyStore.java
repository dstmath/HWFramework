package android.security;

import android.os.Environment;
import android.os.FileUtils;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
import libcore.io.IoUtils;

public class SystemKeyStore {
    private static final String KEY_FILE_EXTENSION = ".sks";
    private static final String SYSTEM_KEYSTORE_DIRECTORY = "misc/systemkeys";
    private static SystemKeyStore mInstance = new SystemKeyStore();

    private SystemKeyStore() {
    }

    public static SystemKeyStore getInstance() {
        return mInstance;
    }

    public static String toHexString(byte[] keyData) {
        if (keyData == null) {
            return null;
        }
        int keyLen = keyData.length;
        StringBuilder sb = new StringBuilder(keyData.length * 2);
        for (byte b : keyData) {
            String hexStr = Integer.toString(b & 255, 16);
            if (hexStr.length() == 1) {
                hexStr = "0" + hexStr;
            }
            sb.append(hexStr);
        }
        return sb.toString();
    }

    public String generateNewKeyHexString(int numBits, String algName, String keyName) throws NoSuchAlgorithmException {
        return toHexString(generateNewKey(numBits, algName, keyName));
    }

    public byte[] generateNewKey(int numBits, String algName, String keyName) throws NoSuchAlgorithmException {
        File keyFile = getKeyFile(keyName);
        if (keyFile.exists()) {
            throw new IllegalArgumentException();
        }
        KeyGenerator skg = KeyGenerator.getInstance(algName);
        skg.init(numBits, SecureRandom.getInstance("SHA1PRNG"));
        byte[] retKey = skg.generateKey().getEncoded();
        try {
            if (keyFile.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(keyFile);
                fos.write(retKey);
                fos.flush();
                FileUtils.sync(fos);
                fos.close();
                FileUtils.setPermissions(keyFile.getName(), MetricsEvent.ACTION_SHOW_SETTINGS_SUGGESTION, -1, -1);
                return retKey;
            }
            throw new IllegalArgumentException();
        } catch (IOException e) {
            return null;
        }
    }

    private File getKeyFile(String keyName) {
        return new File(new File(Environment.getDataDirectory(), SYSTEM_KEYSTORE_DIRECTORY), keyName + KEY_FILE_EXTENSION);
    }

    public String retrieveKeyHexString(String keyName) throws IOException {
        return toHexString(retrieveKey(keyName));
    }

    public byte[] retrieveKey(String keyName) throws IOException {
        File keyFile = getKeyFile(keyName);
        if (keyFile.exists()) {
            return IoUtils.readFileAsByteArray(keyFile.toString());
        }
        return null;
    }

    public void deleteKey(String keyName) {
        File keyFile = getKeyFile(keyName);
        if (keyFile.exists()) {
            keyFile.delete();
            return;
        }
        throw new IllegalArgumentException();
    }
}
