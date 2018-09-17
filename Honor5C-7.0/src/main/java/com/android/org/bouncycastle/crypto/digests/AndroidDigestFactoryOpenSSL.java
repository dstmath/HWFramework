package com.android.org.bouncycastle.crypto.digests;

import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.digests.OpenSSLDigest.MD5;
import com.android.org.bouncycastle.crypto.digests.OpenSSLDigest.SHA1;
import com.android.org.bouncycastle.crypto.digests.OpenSSLDigest.SHA224;
import com.android.org.bouncycastle.crypto.digests.OpenSSLDigest.SHA256;
import com.android.org.bouncycastle.crypto.digests.OpenSSLDigest.SHA384;
import com.android.org.bouncycastle.crypto.digests.OpenSSLDigest.SHA512;

public class AndroidDigestFactoryOpenSSL implements AndroidDigestFactoryInterface {
    public Digest getMD5() {
        return new MD5();
    }

    public Digest getSHA1() {
        return new SHA1();
    }

    public Digest getSHA224() {
        return new SHA224();
    }

    public Digest getSHA256() {
        return new SHA256();
    }

    public Digest getSHA384() {
        return new SHA384();
    }

    public Digest getSHA512() {
        return new SHA512();
    }
}
