package org.bouncycastle.jce;

import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.math.ec.ECCurve;

public class ECPointUtil {
    /* JADX WARNING: type inference failed for: r0v6, types: [org.bouncycastle.math.ec.ECCurve$Fp] */
    /* JADX WARNING: Multi-variable type inference failed */
    public static ECPoint decodePoint(EllipticCurve ellipticCurve, byte[] bArr) {
        ECCurve.F2m f2m;
        if (ellipticCurve.getField() instanceof ECFieldFp) {
            f2m = new ECCurve.Fp(((ECFieldFp) ellipticCurve.getField()).getP(), ellipticCurve.getA(), ellipticCurve.getB());
        } else {
            int[] midTermsOfReductionPolynomial = ((ECFieldF2m) ellipticCurve.getField()).getMidTermsOfReductionPolynomial();
            f2m = midTermsOfReductionPolynomial.length == 3 ? new ECCurve.F2m(((ECFieldF2m) ellipticCurve.getField()).getM(), midTermsOfReductionPolynomial[2], midTermsOfReductionPolynomial[1], midTermsOfReductionPolynomial[0], ellipticCurve.getA(), ellipticCurve.getB()) : new ECCurve.F2m(((ECFieldF2m) ellipticCurve.getField()).getM(), midTermsOfReductionPolynomial[0], ellipticCurve.getA(), ellipticCurve.getB());
        }
        return EC5Util.convertPoint(f2m.decodePoint(bArr));
    }
}
