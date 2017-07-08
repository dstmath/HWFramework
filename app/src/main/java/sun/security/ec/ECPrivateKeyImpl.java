package sun.security.ec;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import sun.security.pkcs.PKCS8Key;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;

public final class ECPrivateKeyImpl extends PKCS8Key implements ECPrivateKey {
    private static final long serialVersionUID = 88695385615075129L;
    private ECParameterSpec params;
    private BigInteger s;

    public ECPrivateKeyImpl(byte[] encoded) throws InvalidKeyException {
        decode(encoded);
    }

    public ECPrivateKeyImpl(BigInteger s, ECParameterSpec params) throws InvalidKeyException {
        this.s = s;
        this.params = params;
        this.algid = new AlgorithmId(AlgorithmId.EC_oid, ECParameters.getAlgorithmParameters(params));
        try {
            DerOutputStream out = new DerOutputStream();
            out.putInteger(1);
            out.putOctetString(ECParameters.trimZeroes(s.toByteArray()));
            this.key = new DerValue((byte) DerValue.tag_SequenceOf, out.toByteArray()).toByteArray();
        } catch (Throwable exc) {
            throw new InvalidKeyException(exc);
        }
    }

    public String getAlgorithm() {
        return "EC";
    }

    public BigInteger getS() {
        return this.s;
    }

    public ECParameterSpec getParams() {
        return this.params;
    }

    protected void parseKeyBits() throws InvalidKeyException {
        try {
            DerValue derValue = new DerInputStream(this.key).getDerValue();
            if (derValue.tag != 48) {
                throw new IOException("Not a SEQUENCE");
            }
            DerInputStream data = derValue.data;
            if (data.getInteger() != 1) {
                throw new IOException("Version must be 1");
            }
            this.s = new BigInteger(1, data.getOctetString());
            while (data.available() != 0) {
                Object value = data.getDerValue();
                if (!value.isContextSpecific((byte) 0) && !value.isContextSpecific((byte) 1)) {
                    throw new InvalidKeyException("Unexpected value: " + value);
                }
            }
            AlgorithmParameters algParams = this.algid.getParameters();
            if (algParams == null) {
                throw new InvalidKeyException("EC domain parameters must be encoded in the algorithm identifier");
            }
            this.params = (ECParameterSpec) algParams.getParameterSpec(ECParameterSpec.class);
        } catch (IOException e) {
            throw new InvalidKeyException("Invalid EC private key", e);
        } catch (InvalidParameterSpecException e2) {
            throw new InvalidKeyException("Invalid EC private key", e2);
        }
    }

    public String toString() {
        return "Sun EC private key, " + this.params.getCurve().getField().getFieldSize() + " bits\n  private value:  " + this.s + "\n  parameters: " + this.params;
    }
}
