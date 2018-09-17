package java.security;

import java.nio.ByteBuffer;
import sun.security.pkcs.PKCS9Attribute;

public abstract class MessageDigest extends MessageDigestSpi {
    private static final int INITIAL = 0;
    private static final int IN_PROGRESS = 1;
    private String algorithm;
    private Provider provider;
    private int state;

    static class Delegate extends MessageDigest {
        private MessageDigestSpi digestSpi;

        public Delegate(MessageDigestSpi digestSpi, String algorithm) {
            super(algorithm);
            this.digestSpi = digestSpi;
        }

        public Object clone() throws CloneNotSupportedException {
            if (this.digestSpi instanceof Cloneable) {
                MessageDigest that = new Delegate((MessageDigestSpi) this.digestSpi.clone(), this.algorithm);
                that.provider = this.provider;
                that.state = this.state;
                return that;
            }
            throw new CloneNotSupportedException();
        }

        protected int engineGetDigestLength() {
            return this.digestSpi.engineGetDigestLength();
        }

        protected void engineUpdate(byte input) {
            this.digestSpi.engineUpdate(input);
        }

        protected void engineUpdate(byte[] input, int offset, int len) {
            this.digestSpi.engineUpdate(input, offset, len);
        }

        protected void engineUpdate(ByteBuffer input) {
            this.digestSpi.engineUpdate(input);
        }

        protected byte[] engineDigest() {
            return this.digestSpi.engineDigest();
        }

        protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
            return this.digestSpi.engineDigest(buf, offset, len);
        }

        protected void engineReset() {
            this.digestSpi.engineReset();
        }
    }

    protected MessageDigest(String algorithm) {
        this.state = INITIAL;
        this.algorithm = algorithm;
    }

    public static MessageDigest getInstance(String algorithm) throws NoSuchAlgorithmException {
        try {
            Object[] objs = Security.getImpl(algorithm, PKCS9Attribute.MESSAGE_DIGEST_STR, (String) null);
            if (objs[INITIAL] instanceof MessageDigest) {
                MessageDigest md = objs[INITIAL];
                md.provider = (Provider) objs[IN_PROGRESS];
                return md;
            }
            MessageDigest delegate = new Delegate((MessageDigestSpi) objs[INITIAL], algorithm);
            delegate.provider = (Provider) objs[IN_PROGRESS];
            return delegate;
        } catch (NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(algorithm + " not found");
        }
    }

    public static MessageDigest getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        if (provider == null || provider.length() == 0) {
            throw new IllegalArgumentException("missing provider");
        }
        Object[] objs = Security.getImpl(algorithm, PKCS9Attribute.MESSAGE_DIGEST_STR, provider);
        if (objs[INITIAL] instanceof MessageDigest) {
            MessageDigest md = objs[INITIAL];
            md.provider = (Provider) objs[IN_PROGRESS];
            return md;
        }
        MessageDigest delegate = new Delegate((MessageDigestSpi) objs[INITIAL], algorithm);
        delegate.provider = (Provider) objs[IN_PROGRESS];
        return delegate;
    }

    public static MessageDigest getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException("missing provider");
        }
        Object[] objs = Security.getImpl(algorithm, PKCS9Attribute.MESSAGE_DIGEST_STR, provider);
        if (objs[INITIAL] instanceof MessageDigest) {
            MessageDigest md = objs[INITIAL];
            md.provider = (Provider) objs[IN_PROGRESS];
            return md;
        }
        MessageDigest delegate = new Delegate((MessageDigestSpi) objs[INITIAL], algorithm);
        delegate.provider = (Provider) objs[IN_PROGRESS];
        return delegate;
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public void update(byte input) {
        engineUpdate(input);
        this.state = IN_PROGRESS;
    }

    public void update(byte[] input, int offset, int len) {
        if (input == null) {
            throw new IllegalArgumentException("No input buffer given");
        } else if (input.length - offset < len) {
            throw new IllegalArgumentException("Input buffer too short");
        } else {
            engineUpdate(input, offset, len);
            this.state = IN_PROGRESS;
        }
    }

    public void update(byte[] input) {
        engineUpdate(input, INITIAL, input.length);
        this.state = IN_PROGRESS;
    }

    public final void update(ByteBuffer input) {
        if (input == null) {
            throw new NullPointerException();
        }
        engineUpdate(input);
        this.state = IN_PROGRESS;
    }

    public byte[] digest() {
        byte[] result = engineDigest();
        this.state = INITIAL;
        return result;
    }

    public int digest(byte[] buf, int offset, int len) throws DigestException {
        if (buf == null) {
            throw new IllegalArgumentException("No output buffer given");
        } else if (buf.length - offset < len) {
            throw new IllegalArgumentException("Output buffer too small for specified offset and length");
        } else {
            int numBytes = engineDigest(buf, offset, len);
            this.state = INITIAL;
            return numBytes;
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
            case INITIAL /*0*/:
                builder.append("<initialized>");
                break;
            case IN_PROGRESS /*1*/:
                builder.append("<in progress>");
                break;
        }
        return builder.toString();
    }

    public static boolean isEqual(byte[] digesta, byte[] digestb) {
        boolean z = false;
        if (digesta.length != digestb.length) {
            return false;
        }
        int result = INITIAL;
        for (int i = INITIAL; i < digesta.length; i += IN_PROGRESS) {
            result |= digesta[i] ^ digestb[i];
        }
        if (result == 0) {
            z = true;
        }
        return z;
    }

    public void reset() {
        engineReset();
        this.state = INITIAL;
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
