package ohos.security.keystore;

import java.security.spec.AlgorithmParameterSpec;
import java.util.HashMap;
import java.util.Map;

public class KeyGenAlgorithmParaSpec implements AlgorithmParameterSpec {
    private int secKeyAccessibleAttr;
    private AlgorithmParameterSpec secKeyAlgorithmParameterSpec;
    private String secKeyAlias;
    private int secKeyAuthDuration;
    private Map<Integer, String[]> secKeyCryptoAttrs;
    private int secKeySizeInBits;
    private int secKeyUsagePurposes;

    private KeyGenAlgorithmParaSpec(Builder builder) {
        this.secKeyAlgorithmParameterSpec = builder.secKeyAlgorithm;
        this.secKeySizeInBits = builder.secKeySizeInBits;
        this.secKeyAlias = builder.secKeyAlias;
        this.secKeyUsagePurposes = builder.secKeyUsagePurposes;
        this.secKeyCryptoAttrs = builder.secKeyCryptoAttr;
        this.secKeyAuthDuration = builder.secKeyAuthDuration;
        this.secKeyAccessibleAttr = builder.secKeyAccessibleAttr;
    }

    public AlgorithmParameterSpec getSecKeyAlgorithmParameterSpec() {
        return this.secKeyAlgorithmParameterSpec;
    }

    public int getSecKeySizeInBits() {
        return this.secKeySizeInBits;
    }

    public String getSecKeyAlias() {
        return this.secKeyAlias;
    }

    public String[] getSecKeyCryptoAttr(int i) {
        return this.secKeyCryptoAttrs.get(Integer.valueOf(i));
    }

    public int getSecKeyAuthDuration() {
        return this.secKeyAuthDuration;
    }

    public int getSecKeyUsagePurposes() {
        return this.secKeyUsagePurposes;
    }

    public boolean isKeyAccessible(int i) {
        return (this.secKeyAccessibleAttr & i) != 0;
    }

    public static class Builder {
        private static final int DEFAULT_SIZE = 3;
        private static final int DEFAULT_VALUE = -1;
        private int secKeyAccessibleAttr;
        private AlgorithmParameterSpec secKeyAlgorithm;
        private String secKeyAlias;
        private int secKeyAuthDuration = -1;
        private Map<Integer, String[]> secKeyCryptoAttr = new HashMap(3);
        private int secKeySizeInBits = -1;
        private int secKeyUsagePurposes;

        public Builder(String str) {
            this.secKeyAlias = str;
        }

        public Builder addSecKeyCryptoAttr(int i, String... strArr) {
            this.secKeyCryptoAttr.put(Integer.valueOf(i), strArr);
            return this;
        }

        public Builder setSecKeyAuthDuration(int i) {
            this.secKeyAuthDuration = i;
            return this;
        }

        public Builder setSecKeySizeInBits(int i) {
            this.secKeySizeInBits = i;
            return this;
        }

        public Builder setSecKeyUsagePurposes(int i) {
            this.secKeyUsagePurposes = i;
            return this;
        }

        public Builder setSecKeyAlgorithmParameterSpec(AlgorithmParameterSpec algorithmParameterSpec) {
            this.secKeyAlgorithm = algorithmParameterSpec;
            return this;
        }

        public Builder setSecKeyAccessibleAttr(int i) {
            this.secKeyAccessibleAttr = i;
            return this;
        }

        public KeyGenAlgorithmParaSpec createKeyGenAlgorithmParaSpec() {
            return new KeyGenAlgorithmParaSpec(this);
        }
    }
}
