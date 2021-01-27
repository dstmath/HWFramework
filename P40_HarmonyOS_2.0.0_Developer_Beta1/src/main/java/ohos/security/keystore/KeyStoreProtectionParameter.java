package ohos.security.keystore;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

public class KeyStoreProtectionParameter implements KeyStore.ProtectionParameter {
    private int secKeyAccessibleAttr;
    private int secKeyAuthDuration;
    private Map<Integer, String[]> secKeyCryptoAttrs;
    private int secKeyUsagePurposes;

    private KeyStoreProtectionParameter(int i, Map<Integer, String[]> map, int i2, int i3) {
        this.secKeyUsagePurposes = i;
        this.secKeyCryptoAttrs = map;
        this.secKeyAccessibleAttr = i2;
        this.secKeyAuthDuration = i3;
    }

    public int getSecKeyUsagePurposes() {
        return this.secKeyUsagePurposes;
    }

    public String[] getSecKeyCryptoAttrs(int i) {
        return this.secKeyCryptoAttrs.get(Integer.valueOf(i));
    }

    public boolean isKeyAccessible(int i) {
        return (this.secKeyAccessibleAttr & i) != 0;
    }

    public int getSecKeyAuthDuration() {
        return this.secKeyAuthDuration;
    }

    public static class Builder {
        private static final int DEFAULT_SIZE = 3;
        private static final int DEFAULT_VALUE = -1;
        private int secKeyAccessibleAttr;
        private int secKeyAuthDuration = -1;
        private Map<Integer, String[]> secKeyCryptoAttrs = new HashMap(3);
        private int secKeyUsagePurposes;

        public Builder(int i) {
            this.secKeyUsagePurposes = i;
        }

        public Builder setSecKeyCryptoAttrs(int i, String... strArr) {
            this.secKeyCryptoAttrs.put(Integer.valueOf(i), strArr);
            return this;
        }

        public Builder setSecKeyAccessibleAttr(int i) {
            this.secKeyAccessibleAttr = i;
            return this;
        }

        public Builder setSecKeyAuthDuration(int i) {
            this.secKeyAuthDuration = i;
            return this;
        }

        public KeyStoreProtectionParameter createCustomProtection() {
            return new KeyStoreProtectionParameter(this.secKeyUsagePurposes, this.secKeyCryptoAttrs, this.secKeyAccessibleAttr, this.secKeyAuthDuration);
        }
    }
}
