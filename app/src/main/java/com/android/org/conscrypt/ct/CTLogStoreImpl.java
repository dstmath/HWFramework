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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class CTLogStoreImpl implements CTLogStore {
    private static final char[] HEX_DIGITS = null;
    private static volatile CTLogInfo[] defaultFallbackLogs;
    private static final File defaultSystemLogDir = null;
    private static final File defaultUserLogDir = null;
    private CTLogInfo[] fallbackLogs;
    private Map<ByteBuffer, CTLogInfo> logCache;
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.ct.CTLogStoreImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.ct.CTLogStoreImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.ct.CTLogStoreImpl.<clinit>():void");
    }

    public CTLogStoreImpl() {
        this(defaultUserLogDir, defaultSystemLogDir, getDefaultFallbackLogs());
    }

    public CTLogStoreImpl(File userLogDir, File systemLogDir, CTLogInfo[] fallbackLogs) {
        this.logCache = new Hashtable();
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
                for (CTLogInfo log : this.fallbackLogs) {
                    if (Arrays.equals(logId, log.getID())) {
                        return log;
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
        Scanner scan = new Scanner(input).useDelimiter(",");
        if (!scan.hasNext()) {
            return null;
        }
        String description = null;
        String url = null;
        String str = null;
        while (scan.hasNext()) {
            String[] parts = scan.next().split(":", 2);
            if (parts.length >= 2) {
                String name = parts[0];
                String value = parts[1];
                if (name.equals("description")) {
                    description = value;
                } else if (name.equals("url")) {
                    url = value;
                } else if (name.equals("key")) {
                    str = value;
                }
            }
        }
        if (description == null || url == null || str == null) {
            throw new InvalidLogFileException("Missing one of 'description', 'url' or 'key'");
        }
        try {
            return new CTLogInfo(OpenSSLKey.fromPublicKeyPemInputStream(new StringBufferInputStream("-----BEGIN PUBLIC KEY-----\n" + str + "\n" + "-----END PUBLIC KEY-----")).getPublicKey(), description, url);
        } catch (Throwable e) {
            throw new InvalidLogFileException(e);
        } catch (Throwable e2) {
            throw new InvalidLogFileException(e2);
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
