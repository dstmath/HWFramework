package ohos.security.keystore.provider;

import ohos.hiviewdfx.HiLogLabel;

public class KeyStoreLogger {
    private static final int SUB_DOMAIN_SECURITY_KEYSTORE = 218115846;

    private KeyStoreLogger() {
    }

    public static HiLogLabel getLabel(String str) {
        return new HiLogLabel(3, SUB_DOMAIN_SECURITY_KEYSTORE, str);
    }
}
