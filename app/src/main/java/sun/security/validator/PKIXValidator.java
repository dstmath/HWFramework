package sun.security.validator;

import java.security.AlgorithmConstraints;
import java.security.InvalidAlgorithmParameterException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathValidator;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.provider.certpath.AlgorithmChecker;

public final class PKIXValidator extends Validator {
    private static final boolean TRY_VALIDATOR = true;
    private static final boolean checkTLSRevocation = false;
    private int certPathLength;
    private final CertificateFactory factory;
    private final PKIXBuilderParameters parameterTemplate;
    private final boolean plugin;
    private final Set<X509Certificate> trustedCerts;
    private final Map<X500Principal, List<PublicKey>> trustedSubjects;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.validator.PKIXValidator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.validator.PKIXValidator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.validator.PKIXValidator.<clinit>():void");
    }

    PKIXValidator(String variant, Collection<X509Certificate> trustedCerts) {
        super(Validator.TYPE_PKIX, variant);
        this.certPathLength = -1;
        if (trustedCerts instanceof Set) {
            this.trustedCerts = (Set) trustedCerts;
        } else {
            this.trustedCerts = new HashSet((Collection) trustedCerts);
        }
        Set trustAnchors = new HashSet();
        for (X509Certificate cert : trustedCerts) {
            trustAnchors.add(new TrustAnchor(cert, null));
        }
        try {
            this.parameterTemplate = new PKIXBuilderParameters(trustAnchors, null);
            setDefaultParameters(variant);
            this.trustedSubjects = new HashMap();
            for (X509Certificate cert2 : trustedCerts) {
                List<PublicKey> keys;
                X500Principal dn = cert2.getSubjectX500Principal();
                if (this.trustedSubjects.containsKey(dn)) {
                    keys = (List) this.trustedSubjects.get(dn);
                } else {
                    keys = new ArrayList();
                    this.trustedSubjects.put(dn, keys);
                }
                keys.add(cert2.getPublicKey());
            }
            try {
                this.factory = CertificateFactory.getInstance("X.509");
                this.plugin = variant.equals(Validator.VAR_PLUGIN_CODE_SIGNING);
            } catch (CertificateException e) {
                throw new RuntimeException("Internal error", e);
            }
        } catch (InvalidAlgorithmParameterException e2) {
            throw new RuntimeException("Unexpected error: " + e2.toString(), e2);
        }
    }

    PKIXValidator(String variant, PKIXBuilderParameters params) {
        super(Validator.TYPE_PKIX, variant);
        this.certPathLength = -1;
        this.trustedCerts = new HashSet();
        for (TrustAnchor anchor : params.getTrustAnchors()) {
            X509Certificate cert = anchor.getTrustedCert();
            if (cert != null) {
                this.trustedCerts.add(cert);
            }
        }
        this.parameterTemplate = params;
        this.trustedSubjects = new HashMap();
        for (X509Certificate cert2 : this.trustedCerts) {
            List<PublicKey> keys;
            X500Principal dn = cert2.getSubjectX500Principal();
            if (this.trustedSubjects.containsKey(dn)) {
                keys = (List) this.trustedSubjects.get(dn);
            } else {
                keys = new ArrayList();
                this.trustedSubjects.put(dn, keys);
            }
            keys.add(cert2.getPublicKey());
        }
        try {
            this.factory = CertificateFactory.getInstance("X.509");
            this.plugin = variant.equals(Validator.VAR_PLUGIN_CODE_SIGNING);
        } catch (CertificateException e) {
            throw new RuntimeException("Internal error", e);
        }
    }

    public Collection<X509Certificate> getTrustedCertificates() {
        return this.trustedCerts;
    }

    public int getCertPathLength() {
        return this.certPathLength;
    }

    private void setDefaultParameters(String variant) {
        if (variant == Validator.VAR_TLS_SERVER || variant == Validator.VAR_TLS_CLIENT) {
            this.parameterTemplate.setRevocationEnabled(checkTLSRevocation);
        } else {
            this.parameterTemplate.setRevocationEnabled(false);
        }
    }

    public PKIXBuilderParameters getParameters() {
        return this.parameterTemplate;
    }

    X509Certificate[] engineValidate(X509Certificate[] chain, Collection<X509Certificate> otherCerts, AlgorithmConstraints constraints, Object parameter) throws CertificateException {
        if (chain == null || chain.length == 0) {
            throw new CertificateException("null or zero-length certificate chain");
        }
        PKIXBuilderParameters pkixParameters = (PKIXBuilderParameters) this.parameterTemplate.clone();
        if (constraints != null) {
            pkixParameters.addCertPathChecker(new AlgorithmChecker(constraints));
        }
        Object prevIssuer = null;
        int i = 0;
        while (i < chain.length) {
            X509Certificate cert = chain[i];
            X500Principal dn = cert.getSubjectX500Principal();
            if (i != 0 && !dn.equals(r12)) {
                return doBuild(chain, otherCerts, pkixParameters);
            }
            if (!this.trustedCerts.contains(cert) && (!this.trustedSubjects.containsKey(dn) || !((List) this.trustedSubjects.get(dn)).contains(cert.getPublicKey()))) {
                prevIssuer = cert.getIssuerX500Principal();
                i++;
            } else if (i == 0) {
                return new X509Certificate[]{chain[0]};
            } else {
                Object newChain = new X509Certificate[i];
                System.arraycopy((Object) chain, 0, newChain, 0, i);
                return doValidate(newChain, pkixParameters);
            }
        }
        X509Certificate last = chain[chain.length - 1];
        X500Principal issuer = last.getIssuerX500Principal();
        X500Principal subject = last.getSubjectX500Principal();
        if (this.trustedSubjects.containsKey(issuer) && isSignatureValid((List) this.trustedSubjects.get(issuer), last)) {
            return doValidate(chain, pkixParameters);
        }
        if (!this.plugin) {
            return doBuild(chain, otherCerts, pkixParameters);
        }
        if (chain.length > 1) {
            newChain = new X509Certificate[(chain.length - 1)];
            System.arraycopy((Object) chain, 0, newChain, 0, newChain.length);
            try {
                pkixParameters.setTrustAnchors(Collections.singleton(new TrustAnchor(chain[chain.length - 1], null)));
                doValidate(newChain, pkixParameters);
            } catch (Throwable iape) {
                throw new CertificateException(iape);
            }
        }
        throw new ValidatorException(ValidatorException.T_NO_TRUST_ANCHOR);
    }

    private boolean isSignatureValid(List<PublicKey> keys, X509Certificate sub) {
        if (!this.plugin) {
            return TRY_VALIDATOR;
        }
        for (PublicKey key : keys) {
            try {
                sub.verify(key);
                return TRY_VALIDATOR;
            } catch (Exception e) {
            }
        }
        return false;
    }

    private static X509Certificate[] toArray(CertPath path, TrustAnchor anchor) throws CertificateException {
        List<? extends Certificate> list = path.getCertificates();
        X509Certificate[] chain = new X509Certificate[(list.size() + 1)];
        list.toArray(chain);
        X509Certificate trustedCert = anchor.getTrustedCert();
        if (trustedCert == null) {
            throw new ValidatorException("TrustAnchor must be specified as certificate");
        }
        chain[chain.length - 1] = trustedCert;
        return chain;
    }

    private void setDate(PKIXBuilderParameters params) {
        Date date = this.validationDate;
        if (date != null) {
            params.setDate(date);
        }
    }

    private X509Certificate[] doValidate(X509Certificate[] chain, PKIXBuilderParameters params) throws CertificateException {
        try {
            setDate(params);
            CertPathValidator validator = CertPathValidator.getInstance(Validator.TYPE_PKIX);
            CertPath path = this.factory.generateCertPath(Arrays.asList(chain));
            this.certPathLength = chain.length;
            return toArray(path, ((PKIXCertPathValidatorResult) validator.validate(path, params)).getTrustAnchor());
        } catch (Throwable e) {
            throw new ValidatorException("PKIX path validation failed: " + e.toString(), e);
        }
    }

    private X509Certificate[] doBuild(X509Certificate[] chain, Collection<X509Certificate> otherCerts, PKIXBuilderParameters params) throws CertificateException {
        try {
            setDate(params);
            X509CertSelector selector = new X509CertSelector();
            selector.setCertificate(chain[0]);
            params.setTargetCertConstraints(selector);
            Collection<X509Certificate> certs = new ArrayList();
            certs.addAll(Arrays.asList(chain));
            if (otherCerts != null) {
                certs.addAll(otherCerts);
            }
            params.addCertStore(CertStore.getInstance("Collection", new CollectionCertStoreParameters(certs)));
            PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) CertPathBuilder.getInstance(Validator.TYPE_PKIX).build(params);
            return toArray(result.getCertPath(), result.getTrustAnchor());
        } catch (Throwable e) {
            throw new ValidatorException("PKIX path building failed: " + e.toString(), e);
        }
    }
}
