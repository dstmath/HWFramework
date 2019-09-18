package org.bouncycastle.jce.provider;

import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertPath;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertPathValidatorSpi;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import org.bouncycastle.jcajce.PKIXCertStoreSelector;
import org.bouncycastle.jcajce.PKIXExtendedParameters;
import org.bouncycastle.jcajce.util.BCJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jce.exception.ExtCertPathValidatorException;
import org.bouncycastle.x509.ExtendedPKIXParameters;
import org.bouncycastle.x509.X509AttributeCertStoreSelector;
import org.bouncycastle.x509.X509AttributeCertificate;

public class PKIXAttrCertPathValidatorSpi extends CertPathValidatorSpi {
    private final JcaJceHelper helper = new BCJcaJceHelper();

    public CertPathValidatorResult engineValidate(CertPath certPath, CertPathParameters certPathParameters) throws CertPathValidatorException, InvalidAlgorithmParameterException {
        PKIXExtendedParameters pKIXExtendedParameters;
        boolean z = certPathParameters instanceof ExtendedPKIXParameters;
        if (z || (certPathParameters instanceof PKIXExtendedParameters)) {
            Set hashSet = new HashSet();
            Set hashSet2 = new HashSet();
            Set hashSet3 = new HashSet();
            HashSet hashSet4 = new HashSet();
            if (certPathParameters instanceof PKIXParameters) {
                PKIXExtendedParameters.Builder builder = new PKIXExtendedParameters.Builder((PKIXParameters) certPathParameters);
                if (z) {
                    ExtendedPKIXParameters extendedPKIXParameters = (ExtendedPKIXParameters) certPathParameters;
                    builder.setUseDeltasEnabled(extendedPKIXParameters.isUseDeltasEnabled());
                    builder.setValidityModel(extendedPKIXParameters.getValidityModel());
                    Set attrCertCheckers = extendedPKIXParameters.getAttrCertCheckers();
                    Set prohibitedACAttributes = extendedPKIXParameters.getProhibitedACAttributes();
                    hashSet3 = extendedPKIXParameters.getNecessaryACAttributes();
                    hashSet2 = prohibitedACAttributes;
                    hashSet = attrCertCheckers;
                }
                pKIXExtendedParameters = builder.build();
            } else {
                pKIXExtendedParameters = (PKIXExtendedParameters) certPathParameters;
            }
            PKIXExtendedParameters pKIXExtendedParameters2 = pKIXExtendedParameters;
            PKIXCertStoreSelector targetConstraints = pKIXExtendedParameters2.getTargetConstraints();
            if (targetConstraints instanceof X509AttributeCertStoreSelector) {
                X509AttributeCertificate attributeCert = ((X509AttributeCertStoreSelector) targetConstraints).getAttributeCert();
                CertPath processAttrCert1 = RFC3281CertPathUtilities.processAttrCert1(attributeCert, pKIXExtendedParameters2);
                CertPathValidatorResult processAttrCert2 = RFC3281CertPathUtilities.processAttrCert2(certPath, pKIXExtendedParameters2);
                X509Certificate x509Certificate = (X509Certificate) certPath.getCertificates().get(0);
                RFC3281CertPathUtilities.processAttrCert3(x509Certificate, pKIXExtendedParameters2);
                RFC3281CertPathUtilities.processAttrCert4(x509Certificate, hashSet4);
                RFC3281CertPathUtilities.processAttrCert5(attributeCert, pKIXExtendedParameters2);
                RFC3281CertPathUtilities.processAttrCert7(attributeCert, certPath, processAttrCert1, pKIXExtendedParameters2, hashSet);
                RFC3281CertPathUtilities.additionalChecks(attributeCert, hashSet2, hashSet3);
                try {
                    RFC3281CertPathUtilities.checkCRLs(attributeCert, pKIXExtendedParameters2, x509Certificate, CertPathValidatorUtilities.getValidCertDateFromValidityModel(pKIXExtendedParameters2, null, -1), certPath.getCertificates(), this.helper);
                    return processAttrCert2;
                } catch (AnnotatedException e) {
                    throw new ExtCertPathValidatorException("Could not get validity date from attribute certificate.", e);
                }
            } else {
                throw new InvalidAlgorithmParameterException("TargetConstraints must be an instance of " + X509AttributeCertStoreSelector.class.getName() + " for " + getClass().getName() + " class.");
            }
        } else {
            throw new InvalidAlgorithmParameterException("Parameters must be a " + ExtendedPKIXParameters.class.getName() + " instance.");
        }
    }
}
