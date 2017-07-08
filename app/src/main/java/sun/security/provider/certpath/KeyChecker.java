package sun.security.provider.certpath;

import java.security.cert.CertPathValidatorException;
import java.security.cert.CertSelector;
import java.security.cert.Certificate;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import sun.security.util.Debug;
import sun.security.x509.PKIXExtensions;

class KeyChecker extends PKIXCertPathChecker {
    private static final int KEY_CERT_SIGN = 5;
    private static final Debug debug = null;
    private final int certPathLen;
    private int remainingCerts;
    private Set<String> supportedExts;
    private final CertSelector targetConstraints;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.certpath.KeyChecker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.certpath.KeyChecker.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.KeyChecker.<clinit>():void");
    }

    KeyChecker(int certPathLen, CertSelector targetCertSel) {
        this.certPathLen = certPathLen;
        this.targetConstraints = targetCertSel;
    }

    public void init(boolean forward) throws CertPathValidatorException {
        if (forward) {
            throw new CertPathValidatorException("forward checking not supported");
        }
        this.remainingCerts = this.certPathLen;
    }

    public boolean isForwardCheckingSupported() {
        return false;
    }

    public Set<String> getSupportedExtensions() {
        if (this.supportedExts == null) {
            this.supportedExts = new HashSet(3);
            this.supportedExts.add(PKIXExtensions.KeyUsage_Id.toString());
            this.supportedExts.add(PKIXExtensions.ExtendedKeyUsage_Id.toString());
            this.supportedExts.add(PKIXExtensions.SubjectAlternativeName_Id.toString());
            this.supportedExts = Collections.unmodifiableSet(this.supportedExts);
        }
        return this.supportedExts;
    }

    public void check(Certificate cert, Collection<String> unresCritExts) throws CertPathValidatorException {
        X509Certificate currCert = (X509Certificate) cert;
        this.remainingCerts--;
        if (this.remainingCerts != 0) {
            verifyCAKeyUsage(currCert);
        } else if (!(this.targetConstraints == null || this.targetConstraints.match(currCert))) {
            throw new CertPathValidatorException("target certificate constraints check failed");
        }
        if (unresCritExts != null && !unresCritExts.isEmpty()) {
            unresCritExts.remove(PKIXExtensions.KeyUsage_Id.toString());
            unresCritExts.remove(PKIXExtensions.ExtendedKeyUsage_Id.toString());
            unresCritExts.remove(PKIXExtensions.SubjectAlternativeName_Id.toString());
        }
    }

    static void verifyCAKeyUsage(X509Certificate cert) throws CertPathValidatorException {
        String msg = "CA key usage";
        if (debug != null) {
            debug.println("KeyChecker.verifyCAKeyUsage() ---checking " + msg + "...");
        }
        boolean[] keyUsageBits = cert.getKeyUsage();
        if (keyUsageBits != null) {
            if (keyUsageBits[KEY_CERT_SIGN]) {
                if (debug != null) {
                    debug.println("KeyChecker.verifyCAKeyUsage() " + msg + " verified.");
                }
                return;
            }
            throw new CertPathValidatorException(msg + " check failed: keyCertSign bit is not set", null, null, -1, PKIXReason.INVALID_KEY_USAGE);
        }
    }
}
