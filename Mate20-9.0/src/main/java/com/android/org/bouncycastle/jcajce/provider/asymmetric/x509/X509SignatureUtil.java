package com.android.org.bouncycastle.jcajce.provider.asymmetric.x509;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Null;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.jcajce.util.MessageDigestUtils;
import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.PSSParameterSpec;

class X509SignatureUtil {
    private static final ASN1Null derNull = DERNull.INSTANCE;

    X509SignatureUtil() {
    }

    static void setSignatureParameters(Signature signature, ASN1Encodable params) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        if (params != null && !derNull.equals(params)) {
            AlgorithmParameters sigParams = AlgorithmParameters.getInstance(signature.getAlgorithm(), signature.getProvider());
            try {
                sigParams.init(params.toASN1Primitive().getEncoded());
                if (signature.getAlgorithm().endsWith("MGF1")) {
                    try {
                        signature.setParameter(sigParams.getParameterSpec(PSSParameterSpec.class));
                    } catch (GeneralSecurityException e) {
                        throw new SignatureException("Exception extracting parameters: " + e.getMessage());
                    }
                }
            } catch (IOException e2) {
                throw new SignatureException("IOException decoding parameters: " + e2.getMessage());
            }
        }
    }

    static String getSignatureName(AlgorithmIdentifier sigAlgId) {
        ASN1Encodable params = sigAlgId.getParameters();
        if (params != null && !derNull.equals(params)) {
            if (sigAlgId.getAlgorithm().equals(PKCSObjectIdentifiers.id_RSASSA_PSS)) {
                RSASSAPSSparams rsaParams = RSASSAPSSparams.getInstance(params);
                return getDigestAlgName(rsaParams.getHashAlgorithm().getAlgorithm()) + "withRSAandMGF1";
            } else if (sigAlgId.getAlgorithm().equals(X9ObjectIdentifiers.ecdsa_with_SHA2)) {
                ASN1Sequence ecDsaParams = ASN1Sequence.getInstance(params);
                return getDigestAlgName((ASN1ObjectIdentifier) ecDsaParams.getObjectAt(0)) + "withECDSA";
            }
        }
        Provider prov = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (prov != null) {
            String algName = prov.getProperty("Alg.Alias.Signature." + sigAlgId.getAlgorithm().getId());
            if (algName != null) {
                return algName;
            }
        }
        Provider[] provs = Security.getProviders();
        for (int i = 0; i != provs.length; i++) {
            Provider provider = provs[i];
            String algName2 = provider.getProperty("Alg.Alias.Signature." + sigAlgId.getAlgorithm().getId());
            if (algName2 != null) {
                return algName2;
            }
        }
        return sigAlgId.getAlgorithm().getId();
    }

    private static String getDigestAlgName(ASN1ObjectIdentifier digestAlgOID) {
        String name = MessageDigestUtils.getDigestName(digestAlgOID);
        int dIndex = name.indexOf(45);
        if (dIndex <= 0 || name.startsWith("SHA3")) {
            return MessageDigestUtils.getDigestName(digestAlgOID);
        }
        return name.substring(0, dIndex) + name.substring(dIndex + 1);
    }
}
