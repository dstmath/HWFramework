package com.android.org.bouncycastle.crypto.digests;

import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.digests.OpenSSLDigest;

public class AndroidDigestFactoryOpenSSL implements AndroidDigestFactoryInterface {
    public Digest getMD5() {
        return new OpenSSLDigest.MD5();
    }

    public Digest getSHA1() {
        return new OpenSSLDigest.SHA1();
    }

    public Digest getSHA224() {
        return new OpenSSLDigest.SHA224();
    }

    public Digest getSHA256() {
        return new OpenSSLDigest.SHA256();
    }

    public Digest getSHA384() {
        return new OpenSSLDigest.SHA384();
    }

    public Digest getSHA512() {
        return new OpenSSLDigest.SHA512();
    }
}
