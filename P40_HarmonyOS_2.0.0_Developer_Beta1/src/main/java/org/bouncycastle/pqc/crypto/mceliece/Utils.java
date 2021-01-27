package org.bouncycastle.pqc.crypto.mceliece;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA224Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.pqc.jcajce.spec.McElieceCCA2KeyGenParameterSpec;

class Utils {
    Utils() {
    }

    static Digest getDigest(String str) {
        if (str.equals(McElieceCCA2KeyGenParameterSpec.SHA1)) {
            return new SHA1Digest();
        }
        if (str.equals(McElieceCCA2KeyGenParameterSpec.SHA224)) {
            return new SHA224Digest();
        }
        if (str.equals("SHA-256")) {
            return new SHA256Digest();
        }
        if (str.equals(McElieceCCA2KeyGenParameterSpec.SHA384)) {
            return new SHA384Digest();
        }
        if (str.equals("SHA-512")) {
            return new SHA512Digest();
        }
        throw new IllegalArgumentException("unrecognised digest algorithm: " + str);
    }
}
