package sun.security.validator;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class EndEntityChecker {
    private static final int KU_KEY_AGREEMENT = 4;
    private static final int KU_KEY_ENCIPHERMENT = 2;
    private static final Collection<String> KU_SERVER_ENCRYPTION = null;
    private static final Collection<String> KU_SERVER_KEY_AGREEMENT = null;
    private static final Collection<String> KU_SERVER_SIGNATURE = null;
    private static final int KU_SIGNATURE = 0;
    private static final String NSCT_CODE_SIGNING = "object_signing";
    private static final String NSCT_SSL_CLIENT = "ssl_client";
    private static final String NSCT_SSL_SERVER = "ssl_server";
    private static final String OID_EKU_ANY_USAGE = "2.5.29.37.0";
    private static final String OID_EKU_CODE_SIGNING = "1.3.6.1.5.5.7.3.3";
    private static final String OID_EKU_MS_SGC = "1.3.6.1.4.1.311.10.3.3";
    private static final String OID_EKU_NS_SGC = "2.16.840.1.113730.4.1";
    private static final String OID_EKU_TIME_STAMPING = "1.3.6.1.5.5.7.3.8";
    private static final String OID_EKU_TLS_CLIENT = "1.3.6.1.5.5.7.3.2";
    private static final String OID_EKU_TLS_SERVER = "1.3.6.1.5.5.7.3.1";
    private static final String OID_EXTENDED_KEY_USAGE = "2.5.29.37";
    private static final String OID_SUBJECT_ALT_NAME = "2.5.29.17";
    private final String type;
    private final String variant;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.validator.EndEntityChecker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.validator.EndEntityChecker.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.validator.EndEntityChecker.<clinit>():void");
    }

    private EndEntityChecker(String type, String variant) {
        this.type = type;
        this.variant = variant;
    }

    static EndEntityChecker getInstance(String type, String variant) {
        return new EndEntityChecker(type, variant);
    }

    void check(X509Certificate cert, Object parameter) throws CertificateException {
        if (!this.variant.equals(Validator.VAR_GENERIC)) {
            if (this.variant.equals(Validator.VAR_TLS_SERVER)) {
                checkTLSServer(cert, (String) parameter);
            } else if (this.variant.equals(Validator.VAR_TLS_CLIENT)) {
                checkTLSClient(cert);
            } else if (this.variant.equals(Validator.VAR_CODE_SIGNING)) {
                checkCodeSigning(cert);
            } else if (this.variant.equals(Validator.VAR_JCE_SIGNING)) {
                checkCodeSigning(cert);
            } else if (this.variant.equals(Validator.VAR_PLUGIN_CODE_SIGNING)) {
                checkCodeSigning(cert);
            } else if (this.variant.equals(Validator.VAR_TSA_SERVER)) {
                checkTSAServer(cert);
            } else {
                throw new CertificateException("Unknown variant: " + this.variant);
            }
        }
    }

    private Set<String> getCriticalExtensions(X509Certificate cert) {
        Set<String> exts = cert.getCriticalExtensionOIDs();
        if (exts == null) {
            return Collections.emptySet();
        }
        return exts;
    }

    private void checkRemainingExtensions(Set<String> exts) throws CertificateException {
        exts.remove("2.5.29.19");
        exts.remove(OID_SUBJECT_ALT_NAME);
        if (!exts.isEmpty()) {
            throw new CertificateException("Certificate contains unsupported critical extensions: " + exts);
        }
    }

    private boolean checkEKU(X509Certificate cert, Set<String> set, String expectedEKU) throws CertificateException {
        boolean z = true;
        List<String> eku = cert.getExtendedKeyUsage();
        if (eku == null) {
            return true;
        }
        if (!eku.contains(expectedEKU)) {
            z = eku.contains(OID_EKU_ANY_USAGE);
        }
        return z;
    }

    private boolean checkKeyUsage(X509Certificate cert, int bit) throws CertificateException {
        boolean[] keyUsage = cert.getKeyUsage();
        if (keyUsage == null) {
            return true;
        }
        return keyUsage.length > bit ? keyUsage[bit] : false;
    }

    private void checkTLSClient(X509Certificate cert) throws CertificateException {
        Set<String> exts = getCriticalExtensions(cert);
        if (!checkKeyUsage(cert, KU_SIGNATURE)) {
            throw new ValidatorException("KeyUsage does not allow digital signatures", ValidatorException.T_EE_EXTENSIONS, cert);
        } else if (!checkEKU(cert, exts, OID_EKU_TLS_CLIENT)) {
            throw new ValidatorException("Extended key usage does not permit use for TLS client authentication", ValidatorException.T_EE_EXTENSIONS, cert);
        } else if (SimpleValidator.getNetscapeCertTypeBit(cert, NSCT_SSL_CLIENT)) {
            exts.remove("2.5.29.15");
            exts.remove(OID_EXTENDED_KEY_USAGE);
            exts.remove("2.16.840.1.113730.1.1");
            checkRemainingExtensions(exts);
        } else {
            throw new ValidatorException("Netscape cert type does not permit use for SSL client", ValidatorException.T_EE_EXTENSIONS, cert);
        }
    }

    private void checkTLSServer(X509Certificate cert, String parameter) throws CertificateException {
        Set<String> exts = getCriticalExtensions(cert);
        if (KU_SERVER_ENCRYPTION.contains(parameter)) {
            if (!checkKeyUsage(cert, KU_KEY_ENCIPHERMENT)) {
                throw new ValidatorException("KeyUsage does not allow key encipherment", ValidatorException.T_EE_EXTENSIONS, cert);
            }
        } else if (KU_SERVER_SIGNATURE.contains(parameter)) {
            if (!checkKeyUsage(cert, KU_SIGNATURE)) {
                throw new ValidatorException("KeyUsage does not allow digital signatures", ValidatorException.T_EE_EXTENSIONS, cert);
            }
        } else if (!KU_SERVER_KEY_AGREEMENT.contains(parameter)) {
            throw new CertificateException("Unknown authType: " + parameter);
        } else if (!checkKeyUsage(cert, KU_KEY_AGREEMENT)) {
            throw new ValidatorException("KeyUsage does not allow key agreement", ValidatorException.T_EE_EXTENSIONS, cert);
        }
        if (!checkEKU(cert, exts, OID_EKU_TLS_SERVER) && !checkEKU(cert, exts, OID_EKU_MS_SGC) && !checkEKU(cert, exts, OID_EKU_NS_SGC)) {
            throw new ValidatorException("Extended key usage does not permit use for TLS server authentication", ValidatorException.T_EE_EXTENSIONS, cert);
        } else if (SimpleValidator.getNetscapeCertTypeBit(cert, NSCT_SSL_SERVER)) {
            exts.remove("2.5.29.15");
            exts.remove(OID_EXTENDED_KEY_USAGE);
            exts.remove("2.16.840.1.113730.1.1");
            checkRemainingExtensions(exts);
        } else {
            throw new ValidatorException("Netscape cert type does not permit use for SSL server", ValidatorException.T_EE_EXTENSIONS, cert);
        }
    }

    private void checkCodeSigning(X509Certificate cert) throws CertificateException {
        Set<String> exts = getCriticalExtensions(cert);
        if (!checkKeyUsage(cert, KU_SIGNATURE)) {
            throw new ValidatorException("KeyUsage does not allow digital signatures", ValidatorException.T_EE_EXTENSIONS, cert);
        } else if (checkEKU(cert, exts, OID_EKU_CODE_SIGNING)) {
            if (!this.variant.equals(Validator.VAR_JCE_SIGNING)) {
                if (SimpleValidator.getNetscapeCertTypeBit(cert, NSCT_CODE_SIGNING)) {
                    exts.remove("2.16.840.1.113730.1.1");
                } else {
                    throw new ValidatorException("Netscape cert type does not permit use for code signing", ValidatorException.T_EE_EXTENSIONS, cert);
                }
            }
            exts.remove("2.5.29.15");
            exts.remove(OID_EXTENDED_KEY_USAGE);
            checkRemainingExtensions(exts);
        } else {
            throw new ValidatorException("Extended key usage does not permit use for code signing", ValidatorException.T_EE_EXTENSIONS, cert);
        }
    }

    private void checkTSAServer(X509Certificate cert) throws CertificateException {
        Set<String> exts = getCriticalExtensions(cert);
        if (!checkKeyUsage(cert, KU_SIGNATURE)) {
            throw new ValidatorException("KeyUsage does not allow digital signatures", ValidatorException.T_EE_EXTENSIONS, cert);
        } else if (cert.getExtendedKeyUsage() == null) {
            throw new ValidatorException("Certificate does not contain an extended key usage extension required for a TSA server", ValidatorException.T_EE_EXTENSIONS, cert);
        } else if (checkEKU(cert, exts, OID_EKU_TIME_STAMPING)) {
            exts.remove("2.5.29.15");
            exts.remove(OID_EXTENDED_KEY_USAGE);
            checkRemainingExtensions(exts);
        } else {
            throw new ValidatorException("Extended key usage does not permit use for TSA server", ValidatorException.T_EE_EXTENSIONS, cert);
        }
    }
}
