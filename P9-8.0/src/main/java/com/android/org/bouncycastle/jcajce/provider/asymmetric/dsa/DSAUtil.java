package com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import com.android.org.bouncycastle.crypto.params.DSAParameters;
import com.android.org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;

public class DSAUtil {
    public static final ASN1ObjectIdentifier[] dsaOids = new ASN1ObjectIdentifier[]{X9ObjectIdentifiers.id_dsa, X9ObjectIdentifiers.id_dsa_with_sha1, OIWObjectIdentifiers.dsaWithSHA1};

    public static boolean isDsaOid(ASN1ObjectIdentifier algOid) {
        for (int i = 0; i != dsaOids.length; i++) {
            if (algOid.equals(dsaOids[i])) {
                return true;
            }
        }
        return false;
    }

    static DSAParameters toDSAParameters(DSAParams spec) {
        if (spec != null) {
            return new DSAParameters(spec.getP(), spec.getQ(), spec.getG());
        }
        return null;
    }

    public static AsymmetricKeyParameter generatePublicKeyParameter(PublicKey key) throws InvalidKeyException {
        if (key instanceof BCDSAPublicKey) {
            return ((BCDSAPublicKey) key).engineGetKeyParameters();
        }
        if (key instanceof DSAPublicKey) {
            return new BCDSAPublicKey((DSAPublicKey) key).engineGetKeyParameters();
        }
        try {
            return new BCDSAPublicKey(SubjectPublicKeyInfo.getInstance(key.getEncoded())).engineGetKeyParameters();
        } catch (Exception e) {
            throw new InvalidKeyException("can't identify DSA public key: " + key.getClass().getName());
        }
    }

    public static AsymmetricKeyParameter generatePrivateKeyParameter(PrivateKey key) throws InvalidKeyException {
        if (key instanceof DSAPrivateKey) {
            DSAPrivateKey k = (DSAPrivateKey) key;
            return new DSAPrivateKeyParameters(k.getX(), new DSAParameters(k.getParams().getP(), k.getParams().getQ(), k.getParams().getG()));
        }
        throw new InvalidKeyException("can't identify DSA private key.");
    }
}
