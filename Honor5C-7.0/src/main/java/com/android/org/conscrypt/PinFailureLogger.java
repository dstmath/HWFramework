package com.android.org.conscrypt;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import libcore.io.Base64;
import libcore.io.DropBox;

public class PinFailureLogger {
    private static final long LOG_INTERVAL_NANOS = 817405952;
    private static long lastLoggedNanos;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.PinFailureLogger.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.PinFailureLogger.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.PinFailureLogger.<clinit>():void");
    }

    public static synchronized void log(String cn, boolean chainContainsUserCert, boolean pinIsEnforcing, List<X509Certificate> chain) {
        synchronized (PinFailureLogger.class) {
            if (timeToLog()) {
                writeToLog(cn, chainContainsUserCert, pinIsEnforcing, chain);
                lastLoggedNanos = System.nanoTime();
                return;
            }
        }
    }

    protected static synchronized void writeToLog(String cn, boolean chainContainsUserCert, boolean pinIsEnforcing, List<X509Certificate> chain) {
        synchronized (PinFailureLogger.class) {
            StringBuilder sb = new StringBuilder();
            sb.append(cn);
            sb.append("|");
            sb.append(chainContainsUserCert);
            sb.append("|");
            sb.append(pinIsEnforcing);
            sb.append("|");
            for (X509Certificate cert : chain) {
                try {
                    sb.append(Base64.encode(cert.getEncoded()));
                } catch (CertificateEncodingException e) {
                    sb.append("Error: could not encode certificate");
                }
                sb.append("|");
            }
            DropBox.addText("exp_det_cert_pin_failure", sb.toString());
        }
    }

    protected static boolean timeToLog() {
        return System.nanoTime() - lastLoggedNanos > LOG_INTERVAL_NANOS;
    }
}
