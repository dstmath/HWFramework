package sun.security.validator;

import java.io.IOException;
import java.security.AlgorithmConstraints;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.provider.certpath.AlgorithmChecker;
import sun.security.provider.certpath.UntrustedChecker;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.NetscapeCertTypeExtension;
import sun.security.x509.X509CertImpl;

public final class SimpleValidator extends Validator {
    private static final String NSCT_CODE_SIGNING_CA = "object_signing_ca";
    private static final String NSCT_SSL_CA = "ssl_ca";
    static final ObjectIdentifier OBJID_NETSCAPE_CERT_TYPE = null;
    static final String OID_BASIC_CONSTRAINTS = "2.5.29.19";
    static final String OID_EKU_ANY_USAGE = "2.5.29.37.0";
    static final String OID_EXTENDED_KEY_USAGE = "2.5.29.37";
    static final String OID_KEY_USAGE = "2.5.29.15";
    static final String OID_NETSCAPE_CERT_TYPE = "2.16.840.1.113730.1.1";
    private final Collection<X509Certificate> trustedCerts;
    private final Map<X500Principal, List<X509Certificate>> trustedX500Principals;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.validator.SimpleValidator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.validator.SimpleValidator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.validator.SimpleValidator.<clinit>():void");
    }

    SimpleValidator(String variant, Collection<X509Certificate> trustedCerts) {
        super(Validator.TYPE_SIMPLE, variant);
        this.trustedCerts = trustedCerts;
        this.trustedX500Principals = new HashMap();
        for (X509Certificate cert : trustedCerts) {
            X500Principal principal = cert.getSubjectX500Principal();
            List<X509Certificate> list = (List) this.trustedX500Principals.get(principal);
            if (list == null) {
                list = new ArrayList(2);
                this.trustedX500Principals.put(principal, list);
            }
            list.add(cert);
        }
    }

    public Collection<X509Certificate> getTrustedCertificates() {
        return this.trustedCerts;
    }

    X509Certificate[] engineValidate(X509Certificate[] chain, Collection<X509Certificate> collection, AlgorithmConstraints constraints, Object parameter) throws CertificateException {
        if (chain == null || chain.length == 0) {
            throw new CertificateException("null or zero-length certificate chain");
        }
        chain = buildTrustedChain(chain);
        Date date = this.validationDate;
        if (date == null) {
            date = new Date();
        }
        UntrustedChecker untrustedChecker = new UntrustedChecker();
        TrustAnchor anchor = new TrustAnchor(chain[chain.length - 1], null);
        AlgorithmChecker defaultAlgChecker = new AlgorithmChecker(anchor);
        AlgorithmChecker algorithmChecker = null;
        if (constraints != null) {
            algorithmChecker = new AlgorithmChecker(anchor, constraints);
        }
        int maxPathLength = chain.length - 1;
        int i = chain.length - 2;
        while (i >= 0) {
            X509Certificate issuerCert = chain[i + 1];
            X509Certificate cert = chain[i];
            try {
                untrustedChecker.check(cert, Collections.emptySet());
                try {
                    defaultAlgChecker.check((Certificate) cert, Collections.emptySet());
                    if (algorithmChecker != null) {
                        algorithmChecker.check((Certificate) cert, Collections.emptySet());
                    }
                    if (!(this.variant.equals(Validator.VAR_CODE_SIGNING) || this.variant.equals(Validator.VAR_JCE_SIGNING))) {
                        cert.checkValidity(date);
                    }
                    if (cert.getIssuerX500Principal().equals(issuerCert.getSubjectX500Principal())) {
                        try {
                            cert.verify(issuerCert.getPublicKey());
                            if (i != 0) {
                                maxPathLength = checkExtensions(cert, maxPathLength);
                            }
                            i--;
                        } catch (Throwable e) {
                            throw new ValidatorException(ValidatorException.T_SIGNATURE_ERROR, cert, e);
                        }
                    }
                    throw new ValidatorException(ValidatorException.T_NAME_CHAINING, cert);
                } catch (Throwable cpve) {
                    throw new ValidatorException(ValidatorException.T_ALGORITHM_DISABLED, cert, cpve);
                }
            } catch (CertPathValidatorException cpve2) {
                throw new ValidatorException("Untrusted certificate: " + cert.getSubjectX500Principal(), ValidatorException.T_UNTRUSTED_CERT, cert, cpve2);
            }
        }
        return chain;
    }

    private int checkExtensions(X509Certificate cert, int maxPathLen) throws CertificateException {
        Object critSet = cert.getCriticalExtensionOIDs();
        if (critSet == null) {
            critSet = Collections.emptySet();
        }
        int pathLenConstraint = checkBasicConstraints(cert, critSet, maxPathLen);
        checkKeyUsage(cert, critSet);
        checkNetscapeCertType(cert, critSet);
        if (critSet.isEmpty()) {
            return pathLenConstraint;
        }
        throw new ValidatorException("Certificate contains unknown critical extensions: " + critSet, ValidatorException.T_CA_EXTENSIONS, cert);
    }

    private void checkNetscapeCertType(X509Certificate cert, Set<String> critSet) throws CertificateException {
        if (!this.variant.equals(Validator.VAR_GENERIC)) {
            if (this.variant.equals(Validator.VAR_TLS_CLIENT) || this.variant.equals(Validator.VAR_TLS_SERVER)) {
                if (getNetscapeCertTypeBit(cert, NSCT_SSL_CA)) {
                    critSet.remove(OID_NETSCAPE_CERT_TYPE);
                    return;
                }
                throw new ValidatorException("Invalid Netscape CertType extension for SSL CA certificate", ValidatorException.T_CA_EXTENSIONS, cert);
            } else if (!this.variant.equals(Validator.VAR_CODE_SIGNING) && !this.variant.equals(Validator.VAR_JCE_SIGNING)) {
                throw new CertificateException("Unknown variant " + this.variant);
            } else if (getNetscapeCertTypeBit(cert, NSCT_CODE_SIGNING_CA)) {
                critSet.remove(OID_NETSCAPE_CERT_TYPE);
            } else {
                throw new ValidatorException("Invalid Netscape CertType extension for code signing CA certificate", ValidatorException.T_CA_EXTENSIONS, cert);
            }
        }
    }

    static boolean getNetscapeCertTypeBit(X509Certificate cert, String type) {
        try {
            NetscapeCertTypeExtension ext;
            if (cert instanceof X509CertImpl) {
                ext = (NetscapeCertTypeExtension) ((X509CertImpl) cert).getExtension(OBJID_NETSCAPE_CERT_TYPE);
                if (ext == null) {
                    return true;
                }
            }
            byte[] extVal = cert.getExtensionValue(OID_NETSCAPE_CERT_TYPE);
            if (extVal == null) {
                return true;
            }
            ext = new NetscapeCertTypeExtension(new DerValue(new DerInputStream(extVal).getOctetString()).getUnalignedBitString().toByteArray());
            return ((Boolean) ext.get(type)).booleanValue();
        } catch (IOException e) {
            return false;
        }
    }

    private int checkBasicConstraints(X509Certificate cert, Set<String> critSet, int maxPathLen) throws CertificateException {
        critSet.remove(OID_BASIC_CONSTRAINTS);
        int constraints = cert.getBasicConstraints();
        if (constraints < 0) {
            throw new ValidatorException("End user tried to act as a CA", ValidatorException.T_CA_EXTENSIONS, cert);
        }
        if (!X509CertImpl.isSelfIssued(cert)) {
            if (maxPathLen <= 0) {
                throw new ValidatorException("Violated path length constraints", ValidatorException.T_CA_EXTENSIONS, cert);
            }
            maxPathLen--;
        }
        if (maxPathLen > constraints) {
            return constraints;
        }
        return maxPathLen;
    }

    private void checkKeyUsage(X509Certificate cert, Set<String> critSet) throws CertificateException {
        critSet.remove(OID_KEY_USAGE);
        critSet.remove(OID_EXTENDED_KEY_USAGE);
        boolean[] keyUsageInfo = cert.getKeyUsage();
        if (keyUsageInfo == null) {
            return;
        }
        if (keyUsageInfo.length < 6 || !keyUsageInfo[5]) {
            throw new ValidatorException("Wrong key usage: expected keyCertSign", ValidatorException.T_CA_EXTENSIONS, cert);
        }
    }

    private X509Certificate[] buildTrustedChain(X509Certificate[] chain) throws CertificateException {
        List<X509Certificate> c = new ArrayList(chain.length);
        for (X509Certificate cert : chain) {
            X509Certificate trustedCert = getTrustedCertificate(cert);
            if (trustedCert != null) {
                c.add(trustedCert);
                return (X509Certificate[]) c.toArray(CHAIN0);
            }
            c.add(cert);
        }
        X509Certificate cert2 = chain[chain.length - 1];
        X500Principal subject = cert2.getSubjectX500Principal();
        List<X509Certificate> list = (List) this.trustedX500Principals.get(cert2.getIssuerX500Principal());
        if (list != null) {
            c.add((X509Certificate) list.iterator().next());
            return (X509Certificate[]) c.toArray(CHAIN0);
        }
        throw new ValidatorException(ValidatorException.T_NO_TRUST_ANCHOR);
    }

    private X509Certificate getTrustedCertificate(X509Certificate cert) {
        List<X509Certificate> list = (List) this.trustedX500Principals.get(cert.getSubjectX500Principal());
        if (list == null) {
            return null;
        }
        Principal certIssuerName = cert.getIssuerX500Principal();
        PublicKey certPublicKey = cert.getPublicKey();
        for (X509Certificate mycert : list) {
            if (mycert.equals(cert)) {
                return cert;
            }
            if (mycert.getIssuerX500Principal().equals(certIssuerName) && mycert.getPublicKey().equals(certPublicKey)) {
                return mycert;
            }
        }
        return null;
    }
}
