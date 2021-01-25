package com.huawei.okio;

import com.huawei.networkit.grs.utils.Encrypt;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.annotation.Nullable;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class HashingSink extends ForwardingSink {
    @Nullable
    private final Mac mac;
    @Nullable
    private final MessageDigest messageDigest;

    public static HashingSink md5(Sink sink) {
        return new HashingSink(sink, "MD5");
    }

    public static HashingSink sha1(Sink sink) {
        return new HashingSink(sink, "SHA-1");
    }

    public static HashingSink sha256(Sink sink) {
        return new HashingSink(sink, Encrypt.ALGORITHM_SHA256);
    }

    public static HashingSink sha512(Sink sink) {
        return new HashingSink(sink, "SHA-512");
    }

    public static HashingSink hmacSha1(Sink sink, ByteString key) {
        return new HashingSink(sink, key, "HmacSHA1");
    }

    public static HashingSink hmacSha256(Sink sink, ByteString key) {
        return new HashingSink(sink, key, "HmacSHA256");
    }

    public static HashingSink hmacSha512(Sink sink, ByteString key) {
        return new HashingSink(sink, key, "HmacSHA512");
    }

    private HashingSink(Sink sink, String algorithm) {
        super(sink);
        try {
            this.messageDigest = MessageDigest.getInstance(algorithm);
            this.mac = null;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }

    private HashingSink(Sink sink, ByteString key, String algorithm) {
        super(sink);
        try {
            this.mac = Mac.getInstance(algorithm);
            this.mac.init(new SecretKeySpec(key.toByteArray(), algorithm));
            this.messageDigest = null;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        } catch (InvalidKeyException e2) {
            throw new IllegalArgumentException(e2);
        }
    }

    @Override // com.huawei.okio.ForwardingSink, com.huawei.okio.Sink
    public void write(Buffer source, long byteCount) throws IOException {
        Util.checkOffsetAndCount(source.size, 0, byteCount);
        long hashedCount = 0;
        Segment s = source.head;
        while (hashedCount < byteCount) {
            int toHash = (int) Math.min(byteCount - hashedCount, (long) (s.limit - s.pos));
            MessageDigest messageDigest2 = this.messageDigest;
            if (messageDigest2 != null) {
                messageDigest2.update(s.data, s.pos, toHash);
            } else {
                this.mac.update(s.data, s.pos, toHash);
            }
            hashedCount += (long) toHash;
            s = s.next;
        }
        super.write(source, byteCount);
    }

    public final ByteString hash() {
        MessageDigest messageDigest2 = this.messageDigest;
        return ByteString.of(messageDigest2 != null ? messageDigest2.digest() : this.mac.doFinal());
    }
}
