package java.security;

import java.nio.ByteBuffer;
import sun.security.jca.Providers;
import sun.security.pkcs.PKCS9Attribute;

public abstract class MessageDigest extends MessageDigestSpi {
    private static final int INITIAL = 0;
    private static final int IN_PROGRESS = 1;
    /* access modifiers changed from: private */
    public String algorithm;
    /* access modifiers changed from: private */
    public Provider provider;
    /* access modifiers changed from: private */
    public int state = 0;

    static class Delegate extends MessageDigest {
        private MessageDigestSpi digestSpi;

        public Delegate(MessageDigestSpi digestSpi2, String algorithm) {
            super(algorithm);
            this.digestSpi = digestSpi2;
        }

        public Object clone() throws CloneNotSupportedException {
            if (this.digestSpi instanceof Cloneable) {
                MessageDigest that = new Delegate((MessageDigestSpi) this.digestSpi.clone(), this.algorithm);
                Provider unused = that.provider = this.provider;
                int unused2 = that.state = this.state;
                return that;
            }
            throw new CloneNotSupportedException();
        }

        /* access modifiers changed from: protected */
        public int engineGetDigestLength() {
            return this.digestSpi.engineGetDigestLength();
        }

        /* access modifiers changed from: protected */
        public void engineUpdate(byte input) {
            this.digestSpi.engineUpdate(input);
        }

        /* access modifiers changed from: protected */
        public void engineUpdate(byte[] input, int offset, int len) {
            this.digestSpi.engineUpdate(input, offset, len);
        }

        /* access modifiers changed from: protected */
        public void engineUpdate(ByteBuffer input) {
            this.digestSpi.engineUpdate(input);
        }

        /* access modifiers changed from: protected */
        public byte[] engineDigest() {
            return this.digestSpi.engineDigest();
        }

        /* access modifiers changed from: protected */
        public int engineDigest(byte[] buf, int offset, int len) throws DigestException {
            return this.digestSpi.engineDigest(buf, offset, len);
        }

        /* access modifiers changed from: protected */
        public void engineReset() {
            this.digestSpi.engineReset();
        }
    }

    protected MessageDigest(String algorithm2) {
        this.algorithm = algorithm2;
    }

    public static MessageDigest getInstance(String algorithm2) throws NoSuchAlgorithmException {
        MessageDigest md;
        try {
            Object[] objs = Security.getImpl(algorithm2, PKCS9Attribute.MESSAGE_DIGEST_STR, (String) null);
            if (objs[0] instanceof MessageDigest) {
                md = (MessageDigest) objs[0];
            } else {
                md = new Delegate((MessageDigestSpi) objs[0], algorithm2);
            }
            md.provider = (Provider) objs[1];
            return md;
        } catch (NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(algorithm2 + " not found");
        }
    }

    public static MessageDigest getInstance(String algorithm2, String provider2) throws NoSuchAlgorithmException, NoSuchProviderException {
        if (provider2 == null || provider2.length() == 0) {
            throw new IllegalArgumentException("missing provider");
        }
        Providers.checkBouncyCastleDeprecation(provider2, PKCS9Attribute.MESSAGE_DIGEST_STR, algorithm2);
        Object[] objs = Security.getImpl(algorithm2, PKCS9Attribute.MESSAGE_DIGEST_STR, provider2);
        if (objs[0] instanceof MessageDigest) {
            MessageDigest md = (MessageDigest) objs[0];
            md.provider = (Provider) objs[1];
            return md;
        }
        MessageDigest delegate = new Delegate((MessageDigestSpi) objs[0], algorithm2);
        delegate.provider = (Provider) objs[1];
        return delegate;
    }

    public static MessageDigest getInstance(String algorithm2, Provider provider2) throws NoSuchAlgorithmException {
        if (provider2 != null) {
            Providers.checkBouncyCastleDeprecation(provider2, PKCS9Attribute.MESSAGE_DIGEST_STR, algorithm2);
            Object[] objs = Security.getImpl(algorithm2, PKCS9Attribute.MESSAGE_DIGEST_STR, provider2);
            if (objs[0] instanceof MessageDigest) {
                MessageDigest md = (MessageDigest) objs[0];
                md.provider = (Provider) objs[1];
                return md;
            }
            MessageDigest delegate = new Delegate((MessageDigestSpi) objs[0], algorithm2);
            delegate.provider = (Provider) objs[1];
            return delegate;
        }
        throw new IllegalArgumentException("missing provider");
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public void update(byte input) {
        engineUpdate(input);
        this.state = 1;
    }

    public void update(byte[] input, int offset, int len) {
        if (input == null) {
            throw new IllegalArgumentException("No input buffer given");
        } else if (input.length - offset >= len) {
            engineUpdate(input, offset, len);
            this.state = 1;
        } else {
            throw new IllegalArgumentException("Input buffer too short");
        }
    }

    public void update(byte[] input) {
        engineUpdate(input, 0, input.length);
        this.state = 1;
    }

    public final void update(ByteBuffer input) {
        if (input != null) {
            engineUpdate(input);
            this.state = 1;
            return;
        }
        throw new NullPointerException();
    }

    public byte[] digest() {
        byte[] result = engineDigest();
        this.state = 0;
        return result;
    }

    public int digest(byte[] buf, int offset, int len) throws DigestException {
        if (buf == null) {
            throw new IllegalArgumentException("No output buffer given");
        } else if (buf.length - offset >= len) {
            int numBytes = engineDigest(buf, offset, len);
            this.state = 0;
            return numBytes;
        } else {
            throw new IllegalArgumentException("Output buffer too small for specified offset and length");
        }
    }

    public byte[] digest(byte[] input) {
        update(input);
        return digest();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.algorithm);
        builder.append(" Message Digest from ");
        builder.append(this.provider.getName());
        builder.append(", ");
        switch (this.state) {
            case 0:
                builder.append("<initialized>");
                break;
            case 1:
                builder.append("<in progress>");
                break;
        }
        return builder.toString();
    }

    public static boolean isEqual(byte[] digesta, byte[] digestb) {
        boolean z = true;
        if (digesta == digestb) {
            return true;
        }
        if (digesta == null || digestb == null || digesta.length != digestb.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < digesta.length; i++) {
            result |= digesta[i] ^ digestb[i];
        }
        if (result != 0) {
            z = false;
        }
        return z;
    }

    public void reset() {
        engineReset();
        this.state = 0;
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public final int getDigestLength() {
        int digestLen = engineGetDigestLength();
        if (digestLen != 0) {
            return digestLen;
        }
        try {
            return ((MessageDigest) clone()).digest().length;
        } catch (CloneNotSupportedException e) {
            return digestLen;
        }
    }

    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        }
        throw new CloneNotSupportedException();
    }
}
