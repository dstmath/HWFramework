package java.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import sun.security.x509.X509CertImpl;

public final class SignedObject implements Serializable {
    private static final long serialVersionUID = 720502720485447167L;
    private byte[] content;
    private byte[] signature;
    private String thealgorithm;

    public SignedObject(Serializable object, PrivateKey signingKey, Signature signingEngine) throws IOException, InvalidKeyException, SignatureException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutput a = new ObjectOutputStream(b);
        a.writeObject(object);
        a.flush();
        a.close();
        this.content = b.toByteArray();
        b.close();
        sign(signingKey, signingEngine);
    }

    public Object getObject() throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(this.content);
        ObjectInput a = new ObjectInputStream(b);
        Object obj = a.readObject();
        b.close();
        a.close();
        return obj;
    }

    public byte[] getSignature() {
        return (byte[]) this.signature.clone();
    }

    public String getAlgorithm() {
        return this.thealgorithm;
    }

    public boolean verify(PublicKey verificationKey, Signature verificationEngine) throws InvalidKeyException, SignatureException {
        verificationEngine.initVerify(verificationKey);
        verificationEngine.update((byte[]) this.content.clone());
        return verificationEngine.verify((byte[]) this.signature.clone());
    }

    private void sign(PrivateKey signingKey, Signature signingEngine) throws InvalidKeyException, SignatureException {
        signingEngine.initSign(signingKey);
        signingEngine.update((byte[]) this.content.clone());
        this.signature = (byte[]) signingEngine.sign().clone();
        this.thealgorithm = signingEngine.getAlgorithm();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        GetField fields = s.readFields();
        this.content = (byte[]) ((byte[]) fields.get("content", null)).clone();
        this.signature = (byte[]) ((byte[]) fields.get(X509CertImpl.SIGNATURE, null)).clone();
        this.thealgorithm = (String) fields.get("thealgorithm", null);
    }
}
