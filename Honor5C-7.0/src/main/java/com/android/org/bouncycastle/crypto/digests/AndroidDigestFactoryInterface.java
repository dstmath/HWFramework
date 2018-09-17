package com.android.org.bouncycastle.crypto.digests;

import com.android.org.bouncycastle.crypto.Digest;

interface AndroidDigestFactoryInterface {
    Digest getMD5();

    Digest getSHA1();

    Digest getSHA224();

    Digest getSHA256();

    Digest getSHA384();

    Digest getSHA512();
}
