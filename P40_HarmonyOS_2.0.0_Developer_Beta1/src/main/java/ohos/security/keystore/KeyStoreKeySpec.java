package ohos.security.keystore;

import java.security.spec.KeySpec;
import java.util.Map;

public class KeyStoreKeySpec implements KeySpec {
    private int secKeyAccessibleAttr;
    private String secKeyAlias;
    private int secKeyAuthDuration;
    private final Map<Integer, String[]> secKeyCryptoAttrs;
    private int secKeySizeInBits;
    private int secKeySource;
    private int secKeyUsagePurposes;

    public KeyStoreKeySpec(String str, int i, int i2, int i3, Map<Integer, String[]> map) {
        this.secKeyAlias = str;
        this.secKeySource = i;
        this.secKeySizeInBits = i2;
        this.secKeyUsagePurposes = i3;
        this.secKeyCryptoAttrs = map;
    }

    public String getSecKeyAlias() {
        return this.secKeyAlias;
    }

    public int getSecKeySource() {
        return this.secKeySource;
    }

    public int getSecKeyUsagePurposes() {
        return this.secKeyUsagePurposes;
    }

    public int getSecKeySizeInBits() {
        return this.secKeySizeInBits;
    }

    public String[] getKeyCryptoAttr(int i) {
        return this.secKeyCryptoAttrs.get(Integer.valueOf(i));
    }

    public int getSecKeyAuthDuration() {
        return this.secKeyAuthDuration;
    }

    public boolean isKeyAccessible(int i) {
        return (this.secKeyAccessibleAttr & i) != 0;
    }

    public void setSecKeyAccessibleAttr(int i) {
        this.secKeyAccessibleAttr = i;
    }

    public void setAuthDuration(int i) {
        this.secKeyAuthDuration = i;
    }
}
