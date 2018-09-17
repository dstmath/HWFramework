package com.android.org.bouncycastle.jce.provider;

import com.android.org.bouncycastle.asn1.x509.Extension;
import com.android.org.bouncycastle.jcajce.PKIXCertStore;
import com.android.org.bouncycastle.jcajce.PKIXCertStoreSelector;
import com.android.org.bouncycastle.jcajce.PKIXExtendedBuilderParameters;
import com.android.org.bouncycastle.jcajce.PKIXExtendedBuilderParameters.Builder;
import com.android.org.bouncycastle.jcajce.PKIXExtendedParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import com.android.org.bouncycastle.jce.exception.ExtCertPathBuilderException;
import com.android.org.bouncycastle.x509.ExtendedPKIXBuilderParameters;
import com.android.org.bouncycastle.x509.ExtendedPKIXParameters;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class PKIXCertPathBuilderSpi extends CertPathBuilderSpi {
    private Exception certPathException;

    public CertPathBuilderResult engineBuild(CertPathParameters params) throws CertPathBuilderException, InvalidAlgorithmParameterException {
        PKIXExtendedBuilderParameters paramsPKIX;
        if (params instanceof PKIXBuilderParameters) {
            Builder paramsBldrPKIXBldr;
            PKIXExtendedParameters.Builder paramsPKIXBldr = new PKIXExtendedParameters.Builder((PKIXBuilderParameters) params);
            if (params instanceof ExtendedPKIXParameters) {
                ExtendedPKIXBuilderParameters extPKIX = (ExtendedPKIXBuilderParameters) params;
                for (PKIXCertStore addCertificateStore : extPKIX.getAdditionalStores()) {
                    paramsPKIXBldr.addCertificateStore(addCertificateStore);
                }
                paramsBldrPKIXBldr = new Builder(paramsPKIXBldr.build());
                paramsBldrPKIXBldr.addExcludedCerts(extPKIX.getExcludedCerts());
                paramsBldrPKIXBldr.setMaxPathLength(extPKIX.getMaxPathLength());
            } else {
                paramsBldrPKIXBldr = new Builder((PKIXBuilderParameters) params);
            }
            paramsPKIX = paramsBldrPKIXBldr.build();
        } else if (params instanceof PKIXExtendedBuilderParameters) {
            paramsPKIX = (PKIXExtendedBuilderParameters) params;
        } else {
            throw new InvalidAlgorithmParameterException("Parameters must be an instance of " + PKIXBuilderParameters.class.getName() + " or " + PKIXExtendedBuilderParameters.class.getName() + ".");
        }
        List certPathList = new ArrayList();
        PKIXCertStoreSelector certSelect = paramsPKIX.getBaseParameters().getTargetConstraints();
        try {
            Collection targets = CertPathValidatorUtilities.findCertificates(certSelect, paramsPKIX.getBaseParameters().getCertificateStores());
            targets.addAll(CertPathValidatorUtilities.findCertificates(certSelect, paramsPKIX.getBaseParameters().getCertStores()));
            if (targets.isEmpty()) {
                throw new CertPathBuilderException("No certificate found matching targetContraints.");
            }
            CertPathBuilderResult result = null;
            Iterator targetIter = targets.iterator();
            while (targetIter.hasNext() && result == null) {
                result = build((X509Certificate) targetIter.next(), paramsPKIX, certPathList);
            }
            if (result != null || this.certPathException == null) {
                if (result != null || this.certPathException != null) {
                    return result;
                }
                throw new CertPathBuilderException("Unable to find certificate chain.");
            } else if (this.certPathException instanceof AnnotatedException) {
                throw new CertPathBuilderException(this.certPathException.getMessage(), this.certPathException.getCause());
            } else {
                throw new CertPathBuilderException("Possible certificate chain could not be validated.", this.certPathException);
            }
        } catch (AnnotatedException e) {
            throw new ExtCertPathBuilderException("Error finding target certificate.", e);
        }
    }

    protected CertPathBuilderResult build(X509Certificate tbvCert, PKIXExtendedBuilderParameters pkixParams, List tbvPath) {
        if (tbvPath.contains(tbvCert)) {
            return null;
        }
        if (pkixParams.getExcludedCerts().contains(tbvCert)) {
            return null;
        }
        if (pkixParams.getMaxPathLength() != -1 && tbvPath.size() - 1 > pkixParams.getMaxPathLength()) {
            return null;
        }
        tbvPath.add(tbvCert);
        CertPathBuilderResult builderResult = null;
        try {
            CertificateFactory cFact = new CertificateFactory();
            PKIXCertPathValidatorSpi validator = new PKIXCertPathValidatorSpi();
            try {
                if (CertPathValidatorUtilities.findTrustAnchor(tbvCert, pkixParams.getBaseParameters().getTrustAnchors(), pkixParams.getBaseParameters().getSigProvider()) != null) {
                    CertPath certPath = cFact.engineGenerateCertPath(tbvPath);
                    PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) validator.engineValidate(certPath, pkixParams);
                    return new PKIXCertPathBuilderResult(certPath, result.getTrustAnchor(), result.getPolicyTree(), result.getPublicKey());
                }
                List stores = new ArrayList();
                stores.addAll(pkixParams.getBaseParameters().getCertificateStores());
                stores.addAll(CertPathValidatorUtilities.getAdditionalStoresFromAltNames(tbvCert.getExtensionValue(Extension.issuerAlternativeName.getId()), pkixParams.getBaseParameters().getNamedCertificateStoreMap()));
                Collection issuers = new HashSet();
                issuers.addAll(CertPathValidatorUtilities.findIssuerCerts(tbvCert, pkixParams.getBaseParameters().getCertStores(), stores));
                if (issuers.isEmpty()) {
                    throw new AnnotatedException("No issuer certificate for certificate in certification path found.");
                }
                Iterator it = issuers.iterator();
                while (it.hasNext() && builderResult == null) {
                    builderResult = build((X509Certificate) it.next(), pkixParams, tbvPath);
                }
                if (builderResult == null) {
                    tbvPath.remove(tbvCert);
                }
                return builderResult;
            } catch (AnnotatedException e) {
                throw new AnnotatedException("Cannot find issuer certificate for certificate in certification path.", e);
            } catch (CertificateParsingException e2) {
                throw new AnnotatedException("No additional X.509 stores can be added from certificate locations.", e2);
            } catch (Exception e3) {
                throw new AnnotatedException("Certification path could not be validated.", e3);
            } catch (Exception e32) {
                throw new AnnotatedException("Certification path could not be constructed from certificate list.", e32);
            } catch (AnnotatedException e4) {
                this.certPathException = e4;
            }
        } catch (Exception e5) {
            throw new RuntimeException("Exception creating support classes.");
        }
    }
}
