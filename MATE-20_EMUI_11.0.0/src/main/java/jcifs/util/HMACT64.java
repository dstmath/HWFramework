package jcifs.util;

import java.security.MessageDigest;

public class HMACT64 extends MessageDigest implements Cloneable {
    private static final int BLOCK_LENGTH = 64;
    private static final byte IPAD = 54;
    private static final byte OPAD = 92;
    private byte[] ipad;
    private MessageDigest md5;
    private byte[] opad;

    public HMACT64(byte[] key) {
        super("HMACT64");
        this.ipad = new byte[64];
        this.opad = new byte[64];
        int length = Math.min(key.length, 64);
        for (int i = 0; i < length; i++) {
            this.ipad[i] = (byte) (key[i] ^ IPAD);
            this.opad[i] = (byte) (key[i] ^ OPAD);
        }
        for (int i2 = length; i2 < 64; i2++) {
            this.ipad[i2] = IPAD;
            this.opad[i2] = OPAD;
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
        this.ipad = new byte[64];
        this.opad = new byte[64];
        this.ipad = hmac.ipad;
        this.opad = hmac.opad;
        this.md5 = (MessageDigest) hmac.md5.clone();
    }

    @Override // java.security.MessageDigest, java.security.MessageDigestSpi, java.lang.Object
    public Object clone() {
        try {
            return new HMACT64(this);
        } catch (CloneNotSupportedException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.security.MessageDigestSpi
    public byte[] engineDigest() {
        byte[] digest = this.md5.digest();
        this.md5.update(this.opad);
        return this.md5.digest(digest);
    }

    /* access modifiers changed from: protected */
    @Override // java.security.MessageDigestSpi
    public int engineDigest(byte[] buf, int offset, int len) {
        byte[] digest = this.md5.digest();
        this.md5.update(this.opad);
        this.md5.update(digest);
        try {
            return this.md5.digest(buf, offset, len);
        } catch (Exception e) {
            throw new IllegalStateException();
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.security.MessageDigestSpi
    public int engineGetDigestLength() {
        return this.md5.getDigestLength();
    }

    /* access modifiers changed from: protected */
    @Override // java.security.MessageDigestSpi
    public void engineReset() {
        this.md5.reset();
        this.md5.update(this.ipad);
    }

    /* access modifiers changed from: protected */
    @Override // java.security.MessageDigestSpi
    public void engineUpdate(byte b) {
        this.md5.update(b);
    }

    /* access modifiers changed from: protected */
    @Override // java.security.MessageDigestSpi
    public void engineUpdate(byte[] input, int offset, int len) {
        this.md5.update(input, offset, len);
    }
}
