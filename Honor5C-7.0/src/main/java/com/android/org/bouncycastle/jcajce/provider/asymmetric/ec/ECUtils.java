package com.android.org.bouncycastle.jcajce.provider.asymmetric.ec;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.x9.X9ECParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import java.security.spec.ECGenParameterSpec;

class ECUtils {
    ECUtils() {
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
}
