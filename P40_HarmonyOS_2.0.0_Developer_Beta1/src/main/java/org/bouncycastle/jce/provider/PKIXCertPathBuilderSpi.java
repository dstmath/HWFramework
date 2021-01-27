package org.bouncycastle.jce.provider;

import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathBuilderSpi;
import java.security.cert.CertPathParameters;
import java.security.cert.CertificateParsingException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.jcajce.PKIXCertStore;
import org.bouncycastle.jcajce.PKIXExtendedBuilderParameters;
import org.bouncycastle.jcajce.PKIXExtendedParameters;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.x509.ExtendedPKIXBuilderParameters;
import org.bouncycastle.x509.ExtendedPKIXParameters;

public class PKIXCertPathBuilderSpi extends CertPathBuilderSpi {
    private Exception certPathException;
    private final boolean isForCRLCheck;

    public PKIXCertPathBuilderSpi() {
        this(false);
    }

    PKIXCertPathBuilderSpi(boolean z) {
        this.isForCRLCheck = z;
    }

    /* access modifiers changed from: protected */
    public CertPathBuilderResult build(X509Certificate x509Certificate, PKIXExtendedBuilderParameters pKIXExtendedBuilderParameters, List list) {
        CertPathBuilderResult certPathBuilderResult = null;
        if (list.contains(x509Certificate) || pKIXExtendedBuilderParameters.getExcludedCerts().contains(x509Certificate)) {
            return null;
        }
        if (pKIXExtendedBuilderParameters.getMaxPathLength() != -1 && list.size() - 1 > pKIXExtendedBuilderParameters.getMaxPathLength()) {
            return null;
        }
        list.add(x509Certificate);
        try {
            CertificateFactory certificateFactory = new CertificateFactory();
            PKIXCertPathValidatorSpi pKIXCertPathValidatorSpi = new PKIXCertPathValidatorSpi(this.isForCRLCheck);
            try {
                if (CertPathValidatorUtilities.isIssuerTrustAnchor(x509Certificate, pKIXExtendedBuilderParameters.getBaseParameters().getTrustAnchors(), pKIXExtendedBuilderParameters.getBaseParameters().getSigProvider())) {
                    try {
                        CertPath engineGenerateCertPath = certificateFactory.engineGenerateCertPath(list);
                        try {
                            PKIXCertPathValidatorResult pKIXCertPathValidatorResult = (PKIXCertPathValidatorResult) pKIXCertPathValidatorSpi.engineValidate(engineGenerateCertPath, pKIXExtendedBuilderParameters);
                            return new PKIXCertPathBuilderResult(engineGenerateCertPath, pKIXCertPathValidatorResult.getTrustAnchor(), pKIXCertPathValidatorResult.getPolicyTree(), pKIXCertPathValidatorResult.getPublicKey());
                        } catch (Exception e) {
                            throw new AnnotatedException("Certification path could not be validated.", e);
                        }
                    } catch (Exception e2) {
                        throw new AnnotatedException("Certification path could not be constructed from certificate list.", e2);
                    }
                } else {
                    ArrayList arrayList = new ArrayList();
                    arrayList.addAll(pKIXExtendedBuilderParameters.getBaseParameters().getCertificateStores());
                    try {
                        arrayList.addAll(CertPathValidatorUtilities.getAdditionalStoresFromAltNames(x509Certificate.getExtensionValue(Extension.issuerAlternativeName.getId()), pKIXExtendedBuilderParameters.getBaseParameters().getNamedCertificateStoreMap()));
                        HashSet hashSet = new HashSet();
                        try {
                            hashSet.addAll(CertPathValidatorUtilities.findIssuerCerts(x509Certificate, pKIXExtendedBuilderParameters.getBaseParameters().getCertStores(), arrayList));
                            if (!hashSet.isEmpty()) {
                                Iterator it = hashSet.iterator();
                                while (it.hasNext() && certPathBuilderResult == null) {
                                    certPathBuilderResult = build((X509Certificate) it.next(), pKIXExtendedBuilderParameters, list);
                                }
                                if (certPathBuilderResult == null) {
                                    list.remove(x509Certificate);
                                }
                                return certPathBuilderResult;
                            }
                            throw new AnnotatedException("No issuer certificate for certificate in certification path found.");
                        } catch (AnnotatedException e3) {
                            throw new AnnotatedException("Cannot find issuer certificate for certificate in certification path.", e3);
                        }
                    } catch (CertificateParsingException e4) {
                        throw new AnnotatedException("No additional X.509 stores can be added from certificate locations.", e4);
                    }
                }
            } catch (AnnotatedException e5) {
                this.certPathException = e5;
            }
        } catch (Exception e6) {
            throw new RuntimeException("Exception creating support classes.");
        }
    }

    @Override // java.security.cert.CertPathBuilderSpi
    public CertPathBuilderResult engineBuild(CertPathParameters certPathParameters) throws CertPathBuilderException, InvalidAlgorithmParameterException {
        PKIXExtendedBuilderParameters pKIXExtendedBuilderParameters;
        Exception exc;
        PKIXExtendedBuilderParameters.Builder builder;
        if (certPathParameters instanceof PKIXBuilderParameters) {
            PKIXBuilderParameters pKIXBuilderParameters = (PKIXBuilderParameters) certPathParameters;
            PKIXExtendedParameters.Builder builder2 = new PKIXExtendedParameters.Builder(pKIXBuilderParameters);
            if (certPathParameters instanceof ExtendedPKIXParameters) {
                ExtendedPKIXBuilderParameters extendedPKIXBuilderParameters = (ExtendedPKIXBuilderParameters) certPathParameters;
                for (PKIXCertStore pKIXCertStore : extendedPKIXBuilderParameters.getAdditionalStores()) {
                    builder2.addCertificateStore(pKIXCertStore);
                }
                PKIXExtendedBuilderParameters.Builder builder3 = new PKIXExtendedBuilderParameters.Builder(builder2.build());
                builder3.addExcludedCerts(extendedPKIXBuilderParameters.getExcludedCerts());
                builder3.setMaxPathLength(extendedPKIXBuilderParameters.getMaxPathLength());
                builder = builder3;
            } else {
                builder = new PKIXExtendedBuilderParameters.Builder(pKIXBuilderParameters);
            }
            pKIXExtendedBuilderParameters = builder.build();
        } else if (certPathParameters instanceof PKIXExtendedBuilderParameters) {
            pKIXExtendedBuilderParameters = (PKIXExtendedBuilderParameters) certPathParameters;
        } else {
            throw new InvalidAlgorithmParameterException("Parameters must be an instance of " + PKIXBuilderParameters.class.getName() + " or " + PKIXExtendedBuilderParameters.class.getName() + ".");
        }
        ArrayList arrayList = new ArrayList();
        CertPathBuilderResult certPathBuilderResult = null;
        Iterator it = CertPathValidatorUtilities.findTargets(pKIXExtendedBuilderParameters).iterator();
        while (it.hasNext() && certPathBuilderResult == null) {
            certPathBuilderResult = build((X509Certificate) it.next(), pKIXExtendedBuilderParameters, arrayList);
        }
        if (certPathBuilderResult != null || (exc = this.certPathException) == null) {
            if (!(certPathBuilderResult == null && this.certPathException == null)) {
                return certPathBuilderResult;
            }
            throw new CertPathBuilderException("Unable to find certificate chain.");
        } else if (exc instanceof AnnotatedException) {
            throw new CertPathBuilderException(exc.getMessage(), this.certPathException.getCause());
        } else {
            throw new CertPathBuilderException("Possible certificate chain could not be validated.", exc);
        }
    }
}
