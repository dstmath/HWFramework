package jcifs.util;

import java.security.MessageDigest;

public class HMACT64 extends MessageDigest implements Cloneable {
    private static final int BLOCK_LENGTH = 64;
    private static final byte IPAD = (byte) 54;
    private static final byte OPAD = (byte) 92;
    private byte[] ipad;
    private MessageDigest md5;
    private byte[] opad;

    public HMACT64(byte[] key) {
        int i;
        super("HMACT64");
        this.ipad = new byte[BLOCK_LENGTH];
        this.opad = new byte[BLOCK_LENGTH];
        int length = Math.min(key.length, BLOCK_LENGTH);
        for (i = 0; i < length; i++) {
            this.ipad[i] = (byte) (key[i] ^ 54);
            this.opad[i] = (byte) (key[i] ^ 92);
        }
        for (i = length; i < BLOCK_LENGTH; i++) {
            this.ipad[i] = IPAD;
            this.opad[i] = OPAD;
        }
        try {
            this.md5 = MessageDigest.getInstance("MD5");
            engineReset();
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    private HMACT64(HMACT64 hmac) throws CloneNotSupportedException {
        super("HMACT64");
        this.ipad = new byte[BLOCK_LENGTH];
        this.opad = new byte[BLOCK_LENGTH];
        this.ipad = hmac.ipad;
        this.opad = hmac.opad;
        this.md5 = (MessageDigest) hmac.md5.clone();
    }

    public Object clone() {
        try {
            return new HMACT64(this);
        } catch (CloneNotSupportedException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    protected byte[] engineDigest() {
        byte[] digest = this.md5.digest();
        this.md5.update(this.opad);
        return this.md5.digest(digest);
    }

    protected int engineDigest(byte[] buf, int offset, int len) {
        byte[] digest = this.md5.digest();
        this.md5.update(this.opad);
        this.md5.update(digest);
        try {
            return this.md5.digest(buf, offset, len);
        } catch (Exception e) {
            throw new IllegalStateException();
        }
    }

    protected int engineGetDigestLength() {
        return this.md5.getDigestLength();
    }

    protected void engineReset() {
        this.md5.reset();
        this.md5.update(this.ipad);
    }

    protected void engineUpdate(byte b) {
        this.md5.update(b);
    }

    protected void engineUpdate(byte[] input, int offset, int len) {
        this.md5.update(input, offset, len);
    }
}
