package android.security;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Process;
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
    private static SystemKeyStore mInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.security.SystemKeyStore.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.security.SystemKeyStore.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.SystemKeyStore.<clinit>():void");
    }

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
            String hexStr = Integer.toString(b & Process.PROC_TERM_MASK, 16);
            if (hexStr.length() == 1) {
                hexStr = WifiEnterpriseConfig.ENGINE_DISABLE + hexStr;
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
                FileUtils.setPermissions(keyFile.getName(), 384, -1, -1);
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
