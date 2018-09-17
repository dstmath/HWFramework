package com.android.org.bouncycastle.jcajce.provider.asymmetric.ec;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.x9.X962Parameters;
import com.android.org.bouncycastle.asn1.x9.X9ECParameters;
import com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import com.android.org.bouncycastle.jce.spec.ECNamedCurveSpec;
import com.android.org.bouncycastle.math.ec.ECCurve;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;

class ECUtils {
    ECUtils() {
    }

    static AsymmetricKeyParameter generatePublicKeyParameter(PublicKey key) throws InvalidKeyException {
        return key instanceof BCECPublicKey ? ((BCECPublicKey) key).engineGetKeyParameters() : ECUtil.generatePublicKeyParameter(key);
    }

    static X9ECParameters getDomainParametersFromGenSpec(ECGenParameterSpec genSpec) {
        return getDomainParametersFromName(genSpec.getName());
    }

    static X9ECParameters getDomainParametersFromName(String curveName) {
        try {
            if (curveName.charAt(0) >= '0' && curveName.charAt(0) <= '2') {
                return ECUtil.getNamedCurveByOid(new ASN1ObjectIdentifier(curveName));
            }
            if (curveName.indexOf(32) > 0) {
                return ECUtil.getNamedCurveByName(curveName.substring(curveName.indexOf(32) + 1));
            }
            return ECUtil.getNamedCurveByName(curveName);
        } catch (IllegalArgumentException e) {
            return ECUtil.getNamedCurveByName(curveName);
        }
    }

    static X962Parameters getDomainParametersFromName(ECParameterSpec ecSpec, boolean withCompression) {
        if (ecSpec instanceof ECNamedCurveSpec) {
            ASN1ObjectIdentifier curveOid = ECUtil.getNamedCurveOid(((ECNamedCurveSpec) ecSpec).getName());
            if (curveOid == null) {
                curveOid = new ASN1ObjectIdentifier(((ECNamedCurveSpec) ecSpec).getName());
            }
            return new X962Parameters(curveOid);
        } else if (ecSpec == null) {
            return new X962Parameters(DERNull.INSTANCE);
        } else {
            ECCurve curve = EC5Util.convertCurve(ecSpec.getCurve());
            return new X962Parameters(new X9ECParameters(curve, EC5Util.convertPoint(curve, ecSpec.getGenerator(), withCompression), ecSpec.getOrder(), BigInteger.valueOf((long) ecSpec.getCofactor()), ecSpec.getCurve().getSeed()));
        }
    }
}
