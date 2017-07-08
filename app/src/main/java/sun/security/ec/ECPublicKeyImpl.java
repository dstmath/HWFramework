package sun.security.ec;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.security.InvalidKeyException;
import java.security.KeyRep;
import java.security.KeyRep.Type;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.InvalidParameterSpecException;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X509Key;

public final class ECPublicKeyImpl extends X509Key implements ECPublicKey {
    private static final long serialVersionUID = -2462037275160462289L;
    private ECParameterSpec params;
    private ECPoint w;

    public ECPublicKeyImpl(ECPoint w, ECParameterSpec params) throws InvalidKeyException {
        this.w = w;
        this.params = params;
        this.algid = new AlgorithmId(AlgorithmId.EC_oid, ECParameters.getAlgorithmParameters(params));
        this.key = ECParameters.encodePoint(w, params.getCurve());
    }

    public ECPublicKeyImpl(byte[] encoded) throws InvalidKeyException {
        decode(encoded);
    }

    public String getAlgorithm() {
        return "EC";
    }

    public ECPoint getW() {
        return this.w;
    }

    public ECParameterSpec getParams() {
        return this.params;
    }

    public byte[] getEncodedPublicValue() {
        return (byte[]) this.key.clone();
    }

    protected void parseKeyBits() throws InvalidKeyException {
        try {
            this.params = (ECParameterSpec) this.algid.getParameters().getParameterSpec(ECParameterSpec.class);
            this.w = ECParameters.decodePoint(this.key, this.params.getCurve());
        } catch (IOException e) {
            throw new InvalidKeyException("Invalid EC key", e);
        } catch (InvalidParameterSpecException e2) {
            throw new InvalidKeyException("Invalid EC key", e2);
        }
    }

    public String toString() {
        return "Sun EC public key, " + this.params.getCurve().getField().getFieldSize() + " bits\n  public x coord: " + this.w.getAffineX() + "\n  public y coord: " + this.w.getAffineY() + "\n  parameters: " + this.params;
    }

    protected Object writeReplace() throws ObjectStreamException {
        return new KeyRep(Type.PUBLIC, getAlgorithm(), getFormat(), getEncoded());
    }
}
