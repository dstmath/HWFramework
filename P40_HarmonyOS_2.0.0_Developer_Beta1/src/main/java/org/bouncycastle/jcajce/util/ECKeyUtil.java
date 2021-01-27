package org.bouncycastle.jcajce.util;

import java.io.IOException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ECPoint;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECCurve;

public class ECKeyUtil {

    private static class ECPublicKeyWithCompression implements ECPublicKey {
        private final ECPublicKey ecPublicKey;

        public ECPublicKeyWithCompression(ECPublicKey eCPublicKey) {
            this.ecPublicKey = eCPublicKey;
        }

        @Override // java.security.Key
        public String getAlgorithm() {
            return this.ecPublicKey.getAlgorithm();
        }

        @Override // java.security.Key
        public byte[] getEncoded() {
            ECCurve eCCurve;
            SubjectPublicKeyInfo instance = SubjectPublicKeyInfo.getInstance(this.ecPublicKey.getEncoded());
            X962Parameters instance2 = X962Parameters.getInstance(instance.getAlgorithm().getParameters());
            if (instance2.isNamedCurve()) {
                ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier) instance2.getParameters();
                X9ECParameters byOID = CustomNamedCurves.getByOID(aSN1ObjectIdentifier);
                if (byOID == null) {
                    byOID = ECNamedCurveTable.getByOID(aSN1ObjectIdentifier);
                }
                eCCurve = byOID.getCurve();
            } else if (!instance2.isImplicitlyCA()) {
                eCCurve = X9ECParameters.getInstance(instance2.getParameters()).getCurve();
            } else {
                throw new IllegalStateException("unable to identify implictlyCA");
            }
            try {
                return new SubjectPublicKeyInfo(instance.getAlgorithm(), ASN1OctetString.getInstance(new X9ECPoint(eCCurve.decodePoint(instance.getPublicKeyData().getOctets()), true).toASN1Primitive()).getOctets()).getEncoded();
            } catch (IOException e) {
                throw new IllegalStateException("unable to encode EC public key: " + e.getMessage());
            }
        }

        @Override // java.security.Key
        public String getFormat() {
            return this.ecPublicKey.getFormat();
        }

        @Override // java.security.interfaces.ECKey
        public ECParameterSpec getParams() {
            return this.ecPublicKey.getParams();
        }

        @Override // java.security.interfaces.ECPublicKey
        public ECPoint getW() {
            return this.ecPublicKey.getW();
        }
    }

    public static ECPublicKey createKeyWithCompression(ECPublicKey eCPublicKey) {
        return new ECPublicKeyWithCompression(eCPublicKey);
    }
}
