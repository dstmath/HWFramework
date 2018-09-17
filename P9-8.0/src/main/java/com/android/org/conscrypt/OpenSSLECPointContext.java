package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeRef.EC_POINT;
import java.math.BigInteger;
import java.security.spec.ECPoint;

final class OpenSSLECPointContext {
    private final OpenSSLECGroupContext group;
    private final EC_POINT pointCtx;

    OpenSSLECPointContext(OpenSSLECGroupContext group, EC_POINT pointCtx) {
        this.group = group;
        this.pointCtx = pointCtx;
    }

    public boolean equals(Object o) {
        throw new IllegalArgumentException("OpenSSLECPointContext.equals is not defined.");
    }

    public ECPoint getECPoint() {
        byte[][] generatorCoords = NativeCrypto.EC_POINT_get_affine_coordinates(this.group.getNativeRef(), this.pointCtx);
        return new ECPoint(new BigInteger(generatorCoords[0]), new BigInteger(generatorCoords[1]));
    }

    public int hashCode() {
        return super.hashCode();
    }

    public EC_POINT getNativeRef() {
        return this.pointCtx;
    }

    public static OpenSSLECPointContext getInstance(OpenSSLECGroupContext group, ECPoint javaPoint) {
        OpenSSLECPointContext point = new OpenSSLECPointContext(group, new EC_POINT(NativeCrypto.EC_POINT_new(group.getNativeRef())));
        NativeCrypto.EC_POINT_set_affine_coordinates(group.getNativeRef(), point.getNativeRef(), javaPoint.getAffineX().toByteArray(), javaPoint.getAffineY().toByteArray());
        return point;
    }
}
