package com.android.org.conscrypt.ct;

import com.android.org.conscrypt.InternalUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class CTLogStoreImpl implements CTLogStore {
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final Charset US_ASCII = Charset.forName("US-ASCII");
    private static volatile CTLogInfo[] defaultFallbackLogs = null;
    private static final File defaultSystemLogDir;
    private static final File defaultUserLogDir;
    private CTLogInfo[] fallbackLogs;
    private HashMap<ByteBuffer, CTLogInfo> logCache;
    private Set<ByteBuffer> missingLogCache;
    private File systemLogDir;
    private File userLogDir;

    public static class InvalidLogFileException extends Exception {
        public InvalidLogFileException() {
        }

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

    public CTLogStoreImpl(File userLogDir2, File systemLogDir2, CTLogInfo[] fallbackLogs2) {
        this.logCache = new HashMap<>();
        this.missingLogCache = Collections.synchronizedSet(new HashSet());
        this.userLogDir = userLogDir2;
        this.systemLogDir = systemLogDir2;
        this.fallbackLogs = fallbackLogs2;
    }

    public CTLogInfo getKnownLog(byte[] logId) {
        ByteBuffer buf = ByteBuffer.wrap(logId);
        CTLogInfo log = this.logCache.get(buf);
        if (log != null) {
            return log;
        }
        if (this.missingLogCache.contains(buf)) {
            return null;
        }
        CTLogInfo log2 = findKnownLog(logId);
        if (log2 != null) {
            this.logCache.put(buf, log2);
        } else {
            this.missingLogCache.add(buf);
        }
        return log2;
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
        CTLogInfo[] createDefaultFallbackLogs = createDefaultFallbackLogs();
        CTLogInfo[] result2 = createDefaultFallbackLogs;
        defaultFallbackLogs = createDefaultFallbackLogs;
        return result2;
    }

    private static CTLogInfo[] createDefaultFallbackLogs() {
        CTLogInfo[] logs = new CTLogInfo[8];
        int i = 0;
        while (i < 8) {
            try {
                logs[i] = new CTLogInfo(InternalUtil.logKeyToPublicKey(KnownLogs.LOG_KEYS[i]), KnownLogs.LOG_DESCRIPTIONS[i], KnownLogs.LOG_URLS[i]);
                i++;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        defaultFallbackLogs = logs;
        return logs;
    }

    public static CTLogInfo loadLog(File file) throws FileNotFoundException, InvalidLogFileException {
        return loadLog((InputStream) new FileInputStream(file));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0065, code lost:
        if (r7.equals("description") != false) goto L_0x0069;
     */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x006d  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0070  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x001b A[SYNTHETIC] */
    public static CTLogInfo loadLog(InputStream input) throws InvalidLogFileException {
        Scanner scan = new Scanner(input, "UTF-8");
        scan.useDelimiter("\n");
        String description = null;
        String url = null;
        String key = null;
        try {
            if (!scan.hasNext()) {
                return null;
            }
            while (scan.hasNext()) {
                String[] parts = scan.next().split(":", 2);
                if (parts.length >= 2) {
                    boolean z = false;
                    String name = parts[0];
                    String value = parts[1];
                    int hashCode = name.hashCode();
                    if (hashCode != -1724546052) {
                        if (hashCode == 106079) {
                            if (name.equals("key")) {
                                z = true;
                                switch (z) {
                                    case false:
                                        break;
                                    case true:
                                        break;
                                    case true:
                                        break;
                                }
                            }
                        } else if (hashCode == 116079) {
                            if (name.equals("url")) {
                                z = true;
                                switch (z) {
                                    case false:
                                        description = value;
                                        break;
                                    case true:
                                        url = value;
                                        break;
                                    case true:
                                        key = value;
                                        break;
                                }
                            }
                        }
                    }
                    z = true;
                    switch (z) {
                        case false:
                            break;
                        case true:
                            break;
                        case true:
                            break;
                    }
                }
            }
            scan.close();
            if (description == null || url == null || key == null) {
                throw new InvalidLogFileException("Missing one of 'description', 'url' or 'key'");
            }
            try {
                return new CTLogInfo(InternalUtil.readPublicKeyPem(new ByteArrayInputStream(("-----BEGIN PUBLIC KEY-----\n" + key + "\n-----END PUBLIC KEY-----").getBytes(US_ASCII))), description, url);
            } catch (InvalidKeyException e) {
                throw new InvalidLogFileException((Throwable) e);
            } catch (NoSuchAlgorithmException e2) {
                throw new InvalidLogFileException((Throwable) e2);
            }
        } finally {
            scan.close();
        }
    }

    private static String hexEncode(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(HEX_DIGITS[(b >> 4) & 15]);
            sb.append(HEX_DIGITS[b & 15]);
        }
        return sb.toString();
    }
}
