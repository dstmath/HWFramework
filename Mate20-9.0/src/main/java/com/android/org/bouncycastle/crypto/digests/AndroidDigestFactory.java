package com.android.org.bouncycastle.crypto.digests;

import com.android.org.bouncycastle.crypto.Digest;
import java.security.Security;
import java.util.Locale;

public final class AndroidDigestFactory {
    private static final AndroidDigestFactoryInterface BC = new AndroidDigestFactoryBouncyCastle();
    private static final AndroidDigestFactoryInterface CONSCRYPT;

    static {
        if (Security.getProvider("AndroidOpenSSL") != null) {
            CONSCRYPT = new AndroidDigestFactoryOpenSSL();
        } else if (!System.getProperty("java.vendor", "").toLowerCase(Locale.US).contains("android")) {
            CONSCRYPT = null;
        } else {
            throw new AssertionError("Provider AndroidOpenSSL must exist");
        }
    }

    public static Digest getMD5() {
        if (CONSCRYPT != null) {
            try {
                return CONSCRYPT.getMD5();
            } catch (Exception e) {
            }
        }
        return BC.getMD5();
    }

    public static Digest getSHA1() {
        if (CONSCRYPT != null) {
            try {
                return CONSCRYPT.getSHA1();
            } catch (Exception e) {
            }
        }
        return BC.getSHA1();
    }

    public static Digest getSHA224() {
        if (CONSCRYPT != null) {
            try {
                return CONSCRYPT.getSHA224();
            } catch (Exception e) {
            }
        }
        return BC.getSHA224();
    }

    public static Digest getSHA256() {
        if (CONSCRYPT != null) {
            try {
                return CONSCRYPT.getSHA256();
            } catch (Exception e) {
            }
        }
        return BC.getSHA256();
    }

    public static Digest getSHA384() {
        if (CONSCRYPT != null) {
            try {
                return CONSCRYPT.getSHA384();
            } catch (Exception e) {
            }
        }
        return BC.getSHA384();
    }

    public static Digest getSHA512() {
        if (CONSCRYPT != null) {
            try {
                return CONSCRYPT.getSHA512();
            } catch (Exception e) {
            }
        }
        return BC.getSHA512();
    }
}
