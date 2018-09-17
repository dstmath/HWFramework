package javax.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class SealedObject implements Serializable {
    static final long serialVersionUID = 4482838265551344752L;
    protected byte[] encodedParams;
    private byte[] encryptedContent;
    private String paramsAlg;
    private String sealAlg;

    public SealedObject(Serializable object, Cipher c) throws IOException, IllegalBlockSizeException {
        this.encryptedContent = null;
        this.sealAlg = null;
        this.paramsAlg = null;
        this.encodedParams = null;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutput a = new ObjectOutputStream(b);
        try {
            a.writeObject(object);
            a.flush();
            byte[] content = b.toByteArray();
            try {
                this.encryptedContent = c.doFinal(content);
            } catch (BadPaddingException e) {
            }
            if (c.getParameters() != null) {
                this.encodedParams = c.getParameters().getEncoded();
                this.paramsAlg = c.getParameters().getAlgorithm();
            }
            this.sealAlg = c.getAlgorithm();
        } finally {
            a.close();
        }
    }

    protected SealedObject(SealedObject so) {
        this.encryptedContent = null;
        this.sealAlg = null;
        this.paramsAlg = null;
        this.encodedParams = null;
        this.encryptedContent = (byte[]) so.encryptedContent.clone();
        this.sealAlg = so.sealAlg;
        this.paramsAlg = so.paramsAlg;
        if (so.encodedParams != null) {
            this.encodedParams = (byte[]) so.encodedParams.clone();
        } else {
            this.encodedParams = null;
        }
    }

    public final String getAlgorithm() {
        return this.sealAlg;
    }

    public final Object getObject(Key key) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeyException {
        if (key == null) {
            throw new NullPointerException("key is null");
        }
        try {
            return unseal(key, null);
        } catch (NoSuchProviderException e) {
            throw new NoSuchAlgorithmException("algorithm not found");
        } catch (IllegalBlockSizeException ibse) {
            throw new InvalidKeyException(ibse.getMessage());
        } catch (BadPaddingException bpe) {
            throw new InvalidKeyException(bpe.getMessage());
        }
    }

    public final Object getObject(Cipher c) throws IOException, ClassNotFoundException, IllegalBlockSizeException, BadPaddingException {
        ObjectInput a = new extObjectInputStream(new ByteArrayInputStream(c.doFinal(this.encryptedContent)));
        try {
            Object obj = a.readObject();
            return obj;
        } finally {
            a.close();
        }
    }

    public final Object getObject(Key key, String provider) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        if (key == null) {
            throw new NullPointerException("key is null");
        } else if (provider == null || provider.length() == 0) {
            throw new IllegalArgumentException("missing provider");
        } else {
            try {
                return unseal(key, provider);
            } catch (IllegalBlockSizeException ibse) {
                throw new InvalidKeyException(ibse.getMessage());
            } catch (BadPaddingException bpe) {
                throw new InvalidKeyException(bpe.getMessage());
            }
        }
    }

    private Object unseal(Key key, String provider) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher c;
        AlgorithmParameters params = null;
        if (this.encodedParams != null) {
            if (provider != null) {
                try {
                    params = AlgorithmParameters.getInstance(this.paramsAlg, provider);
                } catch (NoSuchProviderException nspe) {
                    if (provider == null) {
                        throw new NoSuchAlgorithmException(this.paramsAlg + " not found");
                    }
                    throw new NoSuchProviderException(nspe.getMessage());
                }
            }
            params = AlgorithmParameters.getInstance(this.paramsAlg);
            params.init(this.encodedParams);
        }
        if (provider != null) {
            try {
                c = Cipher.getInstance(this.sealAlg, provider);
            } catch (NoSuchPaddingException e) {
                throw new NoSuchAlgorithmException("Padding that was used in sealing operation not available");
            } catch (NoSuchProviderException nspe2) {
                if (provider == null) {
                    throw new NoSuchAlgorithmException(this.sealAlg + " not found");
                }
                throw new NoSuchProviderException(nspe2.getMessage());
            }
        }
        c = Cipher.getInstance(this.sealAlg);
        if (params != null) {
            try {
                c.init(2, key, params);
            } catch (InvalidAlgorithmParameterException iape) {
                throw new RuntimeException(iape.getMessage());
            }
        }
        c.init(2, key);
        ObjectInput a = new extObjectInputStream(new ByteArrayInputStream(c.doFinal(this.encryptedContent)));
        try {
            Object obj = a.readObject();
            return obj;
        } finally {
            a.close();
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (this.encryptedContent != null) {
            this.encryptedContent = (byte[]) this.encryptedContent.clone();
        }
        if (this.encodedParams != null) {
            this.encodedParams = (byte[]) this.encodedParams.clone();
        }
    }
}
