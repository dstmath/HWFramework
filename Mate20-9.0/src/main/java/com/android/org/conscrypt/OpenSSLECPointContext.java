package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeRef;
import java.math.BigInteger;
import java.security.spec.ECPoint;

final class OpenSSLECPointContext {
    private final OpenSSLECGroupContext group;
    private final NativeRef.EC_POINT pointCtx;

    OpenSSLECPointContext(OpenSSLECGroupContext group2, NativeRef.EC_POINT pointCtx2) {
        this.group = group2;
        this.pointCtx = pointCtx2;
    }

    public boolean equals(Object o) {
        throw new IllegalArgumentException("OpenSSLECPointContext.equals is not defined.");
    }

    /* access modifiers changed from: package-private */
    public ECPoint getECPoint() {
        byte[][] generatorCoords = NativeCrypto.EC_POINT_get_affine_coordinates(this.group.getNativeRef(), this.pointCtx);
        return new ECPoint(new BigInteger(generatorCoords[0]), new BigInteger(generatorCoords[1]));
    }

    public int hashCode() {
        return super.hashCode();
    }

    /* access modifiers changed from: package-private */
    public NativeRef.EC_POINT getNativeRef() {
        return this.pointCtx;
    }

    static OpenSSLECPointContext getInstance(OpenSSLECGroupContext group2, ECPoint javaPoint) {
        OpenSSLECPointContext point = new OpenSSLECPointContext(group2, new NativeRef.EC_POINT(NativeCrypto.EC_POINT_new(group2.getNativeRef())));
        NativeCrypto.EC_POINT_set_affine_coordinates(group2.getNativeRef(), point.getNativeRef(), javaPoint.getAffineX().toByteArray(), javaPoint.getAffineY().toByteArray());
        return point;
    }
}
