package com.android.org.conscrypt.ct;

import com.android.org.conscrypt.NativeCrypto;
import com.android.org.conscrypt.OpenSSLKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class CTLogStoreImpl implements CTLogStore {
    private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static volatile CTLogInfo[] defaultFallbackLogs = null;
    private static final File defaultSystemLogDir;
    private static final File defaultUserLogDir;
    private CTLogInfo[] fallbackLogs;
    private HashMap<ByteBuffer, CTLogInfo> logCache;
    private Set<ByteBuffer> missingLogCache;
    private File systemLogDir;
    private File userLogDir;

    public static class InvalidLogFileException extends Exception {
        public InvalidLogFileException(String message) {
            super(message);
        }

        public InvalidLogFileException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidLogFileException(Throwable cause) {
            super(cause);
        }
    }

    static {
        String ANDROID_DATA = System.getenv("ANDROID_DATA");
        String ANDROID_ROOT = System.getenv("ANDROID_ROOT");
        defaultUserLogDir = new File(ANDROID_DATA + "/misc/keychain/trusted_ct_logs/current/");
        defaultSystemLogDir = new File(ANDROID_ROOT + "/etc/security/ct_known_logs/");
    }

    public CTLogStoreImpl() {
        this(defaultUserLogDir, defaultSystemLogDir, getDefaultFallbackLogs());
    }

    public CTLogStoreImpl(File userLogDir, File systemLogDir, CTLogInfo[] fallbackLogs) {
        this.logCache = new HashMap();
        this.missingLogCache = Collections.synchronizedSet(new HashSet());
        this.userLogDir = userLogDir;
        this.systemLogDir = systemLogDir;
        this.fallbackLogs = fallbackLogs;
    }

    public CTLogInfo getKnownLog(byte[] logId) {
        ByteBuffer buf = ByteBuffer.wrap(logId);
        CTLogInfo log = (CTLogInfo) this.logCache.get(buf);
        if (log != null) {
            return log;
        }
        if (this.missingLogCache.contains(buf)) {
            return null;
        }
        log = findKnownLog(logId);
        if (log != null) {
            this.logCache.put(buf, log);
        } else {
            this.missingLogCache.add(buf);
        }
        return log;
    }

    private CTLogInfo findKnownLog(byte[] logId) {
        String filename = hexEncode(logId);
        try {
            return loadLog(new File(this.userLogDir, filename));
        } catch (InvalidLogFileException e) {
            return null;
        } catch (FileNotFoundException e2) {
            try {
                return loadLog(new File(this.systemLogDir, filename));
            } catch (InvalidLogFileException e3) {
                return null;
            } catch (FileNotFoundException e4) {
                if (!this.userLogDir.exists()) {
                    for (CTLogInfo log : this.fallbackLogs) {
                        if (Arrays.equals(logId, log.getID())) {
                            return log;
                        }
                    }
                }
                return null;
            }
        }
    }

    public static CTLogInfo[] getDefaultFallbackLogs() {
        CTLogInfo[] result = defaultFallbackLogs;
        if (result != null) {
            return result;
        }
        result = createDefaultFallbackLogs();
        defaultFallbackLogs = result;
        return result;
    }

    private static CTLogInfo[] createDefaultFallbackLogs() {
        CTLogInfo[] logs = new CTLogInfo[8];
        int i = 0;
        while (i < 8) {
            try {
                logs[i] = new CTLogInfo(new OpenSSLKey(NativeCrypto.d2i_PUBKEY(KnownLogs.LOG_KEYS[i])).getPublicKey(), KnownLogs.LOG_DESCRIPTIONS[i], KnownLogs.LOG_URLS[i]);
                i++;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        defaultFallbackLogs = logs;
        return logs;
    }

    public static CTLogInfo loadLog(File file) throws FileNotFoundException, InvalidLogFileException {
        return loadLog(new FileInputStream(file));
    }

    public static CTLogInfo loadLog(InputStream input) throws InvalidLogFileException {
        CTLogInfo cTLogInfo = null;
        Scanner scan = new Scanner(input, "UTF-8");
        scan.useDelimiter("\n");
        String description = null;
        String url = null;
        String key = null;
        try {
            if (!scan.hasNext()) {
                return cTLogInfo;
            }
            while (scan.hasNext()) {
                cTLogInfo = ":";
                String[] parts = scan.next().split(cTLogInfo, 2);
                if (parts.length >= 2) {
                    String name = parts[0];
                    String value = parts[1];
                    if (name.equals("description")) {
                        description = value;
                    } else if (name.equals("url")) {
                        url = value;
                    } else if (name.equals("key")) {
                        key = value;
                    }
                }
            }
            scan.close();
            if (description == null || url == null || key == null) {
                throw new InvalidLogFileException("Missing one of 'description', 'url' or 'key'");
            }
            try {
                return new CTLogInfo(OpenSSLKey.fromPublicKeyPemInputStream(new StringBufferInputStream("-----BEGIN PUBLIC KEY-----\n" + key + "\n" + "-----END PUBLIC KEY-----")).getPublicKey(), description, url);
            } catch (Throwable e) {
                throw new InvalidLogFileException(e);
            } catch (Throwable e2) {
                throw new InvalidLogFileException(e2);
            }
        } finally {
            scan.close();
        }
    }

    private static String hexEncode(byte[] data) {
        StringBuffer sb = new StringBuffer(data.length * 2);
        for (byte b : data) {
            sb.append(HEX_DIGITS[(b >> 4) & 15]);
            sb.append(HEX_DIGITS[b & 15]);
        }
        return sb.toString();
    }
}
