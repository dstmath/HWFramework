package com.android.org.bouncycastle.crypto.digests;

import com.android.org.bouncycastle.crypto.Digest;

public class AndroidDigestFactoryBouncyCastle implements AndroidDigestFactoryInterface {
    public Digest getMD5() {
        return new MD5Digest();
    }

    public Digest getSHA1() {
        return new SHA1Digest();
    }

    public Digest getSHA224() {
        return new SHA224Digest();
    }

    public Digest getSHA256() {
        return new SHA256Digest();
    }

    public Digest getSHA384() {
        return new SHA384Digest();
    }

    public Digest getSHA512() {
        return new SHA512Digest();
    }
}
