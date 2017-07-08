package sun.security.ssl;

import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignatureSpi;

public final class RSASignature extends SignatureSpi {
    private boolean isReset;
    private MessageDigest md5;
    private final Signature rawRsa;
    private MessageDigest sha;

    public RSASignature() throws NoSuchAlgorithmException {
        this.rawRsa = JsseJce.getSignature("NONEwithRSA");
        this.isReset = true;
    }

    static Signature getInstance() throws NoSuchAlgorithmException {
        return JsseJce.getSignature("MD5andSHA1withRSA");
    }

    static Signature getInternalInstance() throws NoSuchAlgorithmException, NoSuchProviderException {
        return Signature.getInstance("MD5andSHA1withRSA", "SunJSSE");
    }

    static void setHashes(Signature sig, MessageDigest md5, MessageDigest sha) {
        sig.setParameter("hashes", new MessageDigest[]{md5, sha});
    }

    private void reset() {
        if (!this.isReset) {
            this.md5.reset();
            this.sha.reset();
            this.isReset = true;
        }
    }

    private static void checkNull(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("Key must not be null");
        }
    }

    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        checkNull(publicKey);
        reset();
        this.rawRsa.initVerify(publicKey);
    }

    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        engineInitSign(privateKey, null);
    }

    protected void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
        checkNull(privateKey);
        reset();
        this.rawRsa.initSign(privateKey, random);
    }

    private void initDigests() {
        if (this.md5 == null) {
            this.md5 = JsseJce.getMD5();
            this.sha = JsseJce.getSHA();
        }
    }

    protected void engineUpdate(byte b) {
        initDigests();
        this.isReset = false;
        this.md5.update(b);
        this.sha.update(b);
    }

    protected void engineUpdate(byte[] b, int off, int len) {
        initDigests();
        this.isReset = false;
        this.md5.update(b, off, len);
        this.sha.update(b, off, len);
    }

    private byte[] getDigest() throws SignatureException {
        try {
            initDigests();
            byte[] data = new byte[36];
            this.md5.digest(data, 0, 16);
            this.sha.digest(data, 16, 20);
            this.isReset = true;
            return data;
        } catch (Throwable e) {
            throw new SignatureException(e);
        }
    }

    protected byte[] engineSign() throws SignatureException {
        this.rawRsa.update(getDigest());
        return this.rawRsa.sign();
    }

    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
        return engineVerify(sigBytes, 0, sigBytes.length);
    }

    protected boolean engineVerify(byte[] sigBytes, int offset, int length) throws SignatureException {
        this.rawRsa.update(getDigest());
        return this.rawRsa.verify(sigBytes, offset, length);
    }

    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
        if (!param.equals("hashes")) {
            throw new InvalidParameterException("Parameter not supported: " + param);
        } else if (value instanceof MessageDigest[]) {
            MessageDigest[] digests = (MessageDigest[]) value;
            this.md5 = digests[0];
            this.sha = digests[1];
        } else {
            throw new InvalidParameterException("value must be MessageDigest[]");
        }
    }

    protected Object engineGetParameter(String param) throws InvalidParameterException {
        throw new InvalidParameterException("Parameters not supported");
    }
}
