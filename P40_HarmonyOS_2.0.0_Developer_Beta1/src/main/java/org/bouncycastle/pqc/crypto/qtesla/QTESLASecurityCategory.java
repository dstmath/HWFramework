package org.bouncycastle.pqc.crypto.qtesla;

public class QTESLASecurityCategory {
    public static final int PROVABLY_SECURE_I = 5;
    public static final int PROVABLY_SECURE_III = 6;

    private QTESLASecurityCategory() {
    }

    public static String getName(int i) {
        if (i == 5) {
            return "qTESLA-p-I";
        }
        if (i == 6) {
            return "qTESLA-p-III";
        }
        throw new IllegalArgumentException("unknown security category: " + i);
    }

    static int getPrivateSize(int i) {
        if (i == 5) {
            return 5224;
        }
        if (i == 6) {
            return 12392;
        }
        throw new IllegalArgumentException("unknown security category: " + i);
    }

    static int getPublicSize(int i) {
        if (i == 5) {
            return 14880;
        }
        if (i == 6) {
            return 38432;
        }
        throw new IllegalArgumentException("unknown security category: " + i);
    }

    static int getSignatureSize(int i) {
        if (i == 5) {
            return 2592;
        }
        if (i == 6) {
            return 5664;
        }
        throw new IllegalArgumentException("unknown security category: " + i);
    }

    static void validate(int i) {
        if (i != 5 && i != 6) {
            throw new IllegalArgumentException("unknown security category: " + i);
        }
    }
}
