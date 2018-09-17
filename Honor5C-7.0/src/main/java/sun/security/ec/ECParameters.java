package sun.security.ec;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.AlgorithmParametersSpi;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidParameterSpecException;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public final class ECParameters extends AlgorithmParametersSpi {
    private ECParameterSpec paramSpec;

    public static ECPoint decodePoint(byte[] data, EllipticCurve curve) throws IOException {
        if (data.length == 0 || data[0] != 4) {
            throw new IOException("Only uncompressed point format supported");
        }
        int n = (curve.getField().getFieldSize() + 7) >> 3;
        if (data.length != (n * 2) + 1) {
            throw new IOException("Point does not match field size");
        }
        byte[] xb = new byte[n];
        byte[] yb = new byte[n];
        System.arraycopy(data, 1, xb, 0, n);
        System.arraycopy(data, n + 1, yb, 0, n);
        return new ECPoint(new BigInteger(1, xb), new BigInteger(1, yb));
    }

    public static byte[] encodePoint(ECPoint point, EllipticCurve curve) {
        int n = (curve.getField().getFieldSize() + 7) >> 3;
        byte[] xb = trimZeroes(point.getAffineX().toByteArray());
        byte[] yb = trimZeroes(point.getAffineY().toByteArray());
        if (xb.length > n || yb.length > n) {
            throw new RuntimeException("Point coordinates do not match field size");
        }
        byte[] b = new byte[((n << 1) + 1)];
        b[0] = (byte) 4;
        System.arraycopy(xb, 0, b, (n - xb.length) + 1, xb.length);
        System.arraycopy(yb, 0, b, b.length - yb.length, yb.length);
        return b;
    }

    static byte[] trimZeroes(byte[] b) {
        int i = 0;
        while (i < b.length - 1 && b[i] == null) {
            i++;
        }
        if (i == 0) {
            return b;
        }
        byte[] t = new byte[(b.length - i)];
        System.arraycopy(b, i, t, 0, t.length);
        return t;
    }

    public static NamedCurve getNamedCurve(ECParameterSpec params) {
        if ((params instanceof NamedCurve) || params == null) {
            return (NamedCurve) params;
        }
        int fieldSize = params.getCurve().getField().getFieldSize();
        for (ECParameterSpec namedCurve : NamedCurve.knownECParameterSpecs()) {
            if (namedCurve.getCurve().getField().getFieldSize() == fieldSize && namedCurve.getCurve().equals(params.getCurve()) && namedCurve.getGenerator().equals(params.getGenerator()) && namedCurve.getOrder().equals(params.getOrder()) && namedCurve.getCofactor() == params.getCofactor()) {
                return (NamedCurve) namedCurve;
            }
        }
        return null;
    }

    public static String getCurveName(ECParameterSpec params) {
        NamedCurve curve = getNamedCurve(params);
        if (curve == null) {
            return null;
        }
        return curve.getObjectIdentifier().toString();
    }

    public static byte[] encodeParameters(ECParameterSpec params) {
        NamedCurve curve = getNamedCurve(params);
        if (curve != null) {
            return curve.getEncoded();
        }
        throw new RuntimeException("Not a known named curve: " + params);
    }

    public static ECParameterSpec decodeParameters(byte[] params) throws IOException {
        DerValue encodedParams = new DerValue(params);
        if (encodedParams.tag == 6) {
            Object oid = encodedParams.getOID();
            ECParameterSpec spec = NamedCurve.getECParameterSpec((ObjectIdentifier) oid);
            if (spec != null) {
                return spec;
            }
            throw new IOException("Unknown named curve: " + oid);
        }
        throw new IOException("Only named ECParameters supported");
    }

    static AlgorithmParameters getAlgorithmParameters(ECParameterSpec spec) throws InvalidKeyException {
        try {
            AlgorithmParameters params = AlgorithmParameters.getInstance("EC", ECKeyFactory.ecInternalProvider);
            params.init((AlgorithmParameterSpec) spec);
            return params;
        } catch (GeneralSecurityException e) {
            throw new InvalidKeyException("EC parameters error", e);
        }
    }

    protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
        if (paramSpec instanceof ECParameterSpec) {
            this.paramSpec = getNamedCurve((ECParameterSpec) paramSpec);
            if (this.paramSpec == null) {
                throw new InvalidParameterSpecException("Not a supported named curve: " + paramSpec);
            }
        } else if (paramSpec instanceof ECGenParameterSpec) {
            String name = ((ECGenParameterSpec) paramSpec).getName();
            ECParameterSpec spec = NamedCurve.getECParameterSpec(name);
            if (spec == null) {
                throw new InvalidParameterSpecException("Unknown curve: " + name);
            }
            this.paramSpec = spec;
        } else if (paramSpec == null) {
            throw new InvalidParameterSpecException("paramSpec must not be null");
        } else {
            throw new InvalidParameterSpecException("Only ECParameterSpec and ECGenParameterSpec supported");
        }
    }

    protected void engineInit(byte[] params) throws IOException {
        this.paramSpec = decodeParameters(params);
    }

    protected void engineInit(byte[] params, String decodingMethod) throws IOException {
        engineInit(params);
    }

    protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> spec) throws InvalidParameterSpecException {
        if (spec.isAssignableFrom(ECParameterSpec.class)) {
            return this.paramSpec;
        }
        if (spec.isAssignableFrom(ECGenParameterSpec.class)) {
            return new ECGenParameterSpec(getCurveName(this.paramSpec));
        }
        throw new InvalidParameterSpecException("Only ECParameterSpec and ECGenParameterSpec supported");
    }

    protected byte[] engineGetEncoded() throws IOException {
        return encodeParameters(this.paramSpec);
    }

    protected byte[] engineGetEncoded(String encodingMethod) throws IOException {
        return engineGetEncoded();
    }

    protected String engineToString() {
        return this.paramSpec.toString();
    }
}
