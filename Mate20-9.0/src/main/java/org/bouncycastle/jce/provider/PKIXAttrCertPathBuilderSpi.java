package org.bouncycastle.jce.provider;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Principal;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathBuilderSpi;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidator;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.jcajce.PKIXCertStoreSelector;
import org.bouncycastle.jcajce.PKIXExtendedBuilderParameters;
import org.bouncycastle.jce.exception.ExtCertPathBuilderException;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.StoreException;
import org.bouncycastle.x509.ExtendedPKIXBuilderParameters;
import org.bouncycastle.x509.ExtendedPKIXParameters;
import org.bouncycastle.x509.X509AttributeCertStoreSelector;
import org.bouncycastle.x509.X509AttributeCertificate;
import org.bouncycastle.x509.X509CertStoreSelector;

public class PKIXAttrCertPathBuilderSpi extends CertPathBuilderSpi {
    private Exception certPathException;

    private CertPathBuilderResult build(X509AttributeCertificate x509AttributeCertificate, X509Certificate x509Certificate, PKIXExtendedBuilderParameters pKIXExtendedBuilderParameters, List list) {
        CertPathBuilderResult certPathBuilderResult = null;
        if (list.contains(x509Certificate) || pKIXExtendedBuilderParameters.getExcludedCerts().contains(x509Certificate)) {
            return null;
        }
        if (pKIXExtendedBuilderParameters.getMaxPathLength() != -1 && list.size() - 1 > pKIXExtendedBuilderParameters.getMaxPathLength()) {
            return null;
        }
        list.add(x509Certificate);
        try {
            CertificateFactory instance = CertificateFactory.getInstance("X.509", "BC");
            CertPathValidator instance2 = CertPathValidator.getInstance("RFC3281", "BC");
            try {
                if (CertPathValidatorUtilities.isIssuerTrustAnchor(x509Certificate, pKIXExtendedBuilderParameters.getBaseParameters().getTrustAnchors(), pKIXExtendedBuilderParameters.getBaseParameters().getSigProvider())) {
                    CertPath generateCertPath = instance.generateCertPath(list);
                    PKIXCertPathValidatorResult pKIXCertPathValidatorResult = (PKIXCertPathValidatorResult) instance2.validate(generateCertPath, pKIXExtendedBuilderParameters);
                    return new PKIXCertPathBuilderResult(generateCertPath, pKIXCertPathValidatorResult.getTrustAnchor(), pKIXCertPathValidatorResult.getPolicyTree(), pKIXCertPathValidatorResult.getPublicKey());
                }
                ArrayList arrayList = new ArrayList();
                arrayList.addAll(pKIXExtendedBuilderParameters.getBaseParameters().getCertificateStores());
                arrayList.addAll(CertPathValidatorUtilities.getAdditionalStoresFromAltNames(x509Certificate.getExtensionValue(Extension.issuerAlternativeName.getId()), pKIXExtendedBuilderParameters.getBaseParameters().getNamedCertificateStoreMap()));
                HashSet hashSet = new HashSet();
                hashSet.addAll(CertPathValidatorUtilities.findIssuerCerts(x509Certificate, pKIXExtendedBuilderParameters.getBaseParameters().getCertStores(), arrayList));
                if (!hashSet.isEmpty()) {
                    Iterator it = hashSet.iterator();
                    while (it.hasNext() && certPathBuilderResult == null) {
                        X509Certificate x509Certificate2 = (X509Certificate) it.next();
                        if (!x509Certificate2.getIssuerX500Principal().equals(x509Certificate2.getSubjectX500Principal())) {
                            certPathBuilderResult = build(x509AttributeCertificate, x509Certificate2, pKIXExtendedBuilderParameters, list);
                        }
                    }
                    if (certPathBuilderResult == null) {
                        list.remove(x509Certificate);
                    }
                    return certPathBuilderResult;
                }
                throw new AnnotatedException("No issuer certificate for certificate in certification path found.");
            } catch (CertificateParsingException e) {
                throw new AnnotatedException("No additional X.509 stores can be added from certificate locations.", e);
            } catch (AnnotatedException e2) {
                throw new AnnotatedException("Cannot find issuer certificate for certificate in certification path.", e2);
            } catch (Exception e3) {
                throw new AnnotatedException("Certification path could not be constructed from certificate list.", e3);
            } catch (Exception e4) {
                throw new AnnotatedException("Certification path could not be validated.", e4);
            } catch (AnnotatedException e5) {
                this.certPathException = new AnnotatedException("No valid certification path could be build.", e5);
            }
        } catch (Exception e6) {
            throw new RuntimeException("Exception creating support classes.");
        }
    }

    protected static Collection findCertificates(X509AttributeCertStoreSelector x509AttributeCertStoreSelector, List list) throws AnnotatedException {
        HashSet hashSet = new HashSet();
        for (Object next : list) {
            if (next instanceof Store) {
                try {
                    hashSet.addAll(((Store) next).getMatches(x509AttributeCertStoreSelector));
                } catch (StoreException e) {
                    throw new AnnotatedException("Problem while picking certificates from X.509 store.", e);
                }
            }
        }
        return hashSet;
    }

    public CertPathBuilderResult engineBuild(CertPathParameters certPathParameters) throws CertPathBuilderException, InvalidAlgorithmParameterException {
        PKIXExtendedBuilderParameters pKIXExtendedBuilderParameters;
        boolean z = certPathParameters instanceof PKIXBuilderParameters;
        if (z || (certPathParameters instanceof ExtendedPKIXBuilderParameters) || (certPathParameters instanceof PKIXExtendedBuilderParameters)) {
            List arrayList = new ArrayList();
            if (z) {
                PKIXExtendedBuilderParameters.Builder builder = new PKIXExtendedBuilderParameters.Builder((PKIXBuilderParameters) certPathParameters);
                if (certPathParameters instanceof ExtendedPKIXParameters) {
                    ExtendedPKIXBuilderParameters extendedPKIXBuilderParameters = (ExtendedPKIXBuilderParameters) certPathParameters;
                    builder.addExcludedCerts(extendedPKIXBuilderParameters.getExcludedCerts());
                    builder.setMaxPathLength(extendedPKIXBuilderParameters.getMaxPathLength());
                    arrayList = extendedPKIXBuilderParameters.getStores();
                }
                pKIXExtendedBuilderParameters = builder.build();
            } else {
                pKIXExtendedBuilderParameters = (PKIXExtendedBuilderParameters) certPathParameters;
            }
            ArrayList arrayList2 = new ArrayList();
            PKIXCertStoreSelector targetConstraints = pKIXExtendedBuilderParameters.getBaseParameters().getTargetConstraints();
            if (targetConstraints instanceof X509AttributeCertStoreSelector) {
                try {
                    Collection findCertificates = findCertificates((X509AttributeCertStoreSelector) targetConstraints, arrayList);
                    if (!findCertificates.isEmpty()) {
                        CertPathBuilderResult certPathBuilderResult = null;
                        Iterator it = findCertificates.iterator();
                        while (it.hasNext() && certPathBuilderResult == null) {
                            X509AttributeCertificate x509AttributeCertificate = (X509AttributeCertificate) it.next();
                            X509CertStoreSelector x509CertStoreSelector = new X509CertStoreSelector();
                            Principal[] principals = x509AttributeCertificate.getIssuer().getPrincipals();
                            HashSet hashSet = new HashSet();
                            int i = 0;
                            while (i < principals.length) {
                                try {
                                    if (principals[i] instanceof X500Principal) {
                                        x509CertStoreSelector.setSubject(((X500Principal) principals[i]).getEncoded());
                                    }
                                    PKIXCertStoreSelector<? extends Certificate> build = new PKIXCertStoreSelector.Builder(x509CertStoreSelector).build();
                                    hashSet.addAll(CertPathValidatorUtilities.findCertificates(build, pKIXExtendedBuilderParameters.getBaseParameters().getCertStores()));
                                    hashSet.addAll(CertPathValidatorUtilities.findCertificates(build, pKIXExtendedBuilderParameters.getBaseParameters().getCertificateStores()));
                                    i++;
                                } catch (AnnotatedException e) {
                                    throw new ExtCertPathBuilderException("Public key certificate for attribute certificate cannot be searched.", e);
                                } catch (IOException e2) {
                                    throw new ExtCertPathBuilderException("cannot encode X500Principal.", e2);
                                }
                            }
                            if (!hashSet.isEmpty()) {
                                Iterator it2 = hashSet.iterator();
                                while (it2.hasNext() && certPathBuilderResult == null) {
                                    certPathBuilderResult = build(x509AttributeCertificate, (X509Certificate) it2.next(), pKIXExtendedBuilderParameters, arrayList2);
                                }
                            } else {
                                throw new CertPathBuilderException("Public key certificate for attribute certificate cannot be found.");
                            }
                        }
                        if (certPathBuilderResult == null && this.certPathException != null) {
                            throw new ExtCertPathBuilderException("Possible certificate chain could not be validated.", this.certPathException);
                        } else if (certPathBuilderResult != null || this.certPathException != null) {
                            return certPathBuilderResult;
                        } else {
                            throw new CertPathBuilderException("Unable to find certificate chain.");
                        }
                    } else {
                        throw new CertPathBuilderException("No attribute certificate found matching targetContraints.");
                    }
                } catch (AnnotatedException e3) {
                    throw new ExtCertPathBuilderException("Error finding target attribute certificate.", e3);
                }
            } else {
                throw new CertPathBuilderException("TargetConstraints must be an instance of " + X509AttributeCertStoreSelector.class.getName() + " for " + getClass().getName() + " class.");
            }
        } else {
            throw new InvalidAlgorithmParameterException("Parameters must be an instance of " + PKIXBuilderParameters.class.getName() + " or " + PKIXExtendedBuilderParameters.class.getName() + ".");
        }
    }
}
