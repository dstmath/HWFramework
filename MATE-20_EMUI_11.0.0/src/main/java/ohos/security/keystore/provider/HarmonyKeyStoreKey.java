package ohos.security.keystore.provider;

import android.security.keystore.AndroidKeyStoreKey;
import java.security.Key;
import java.util.Objects;

public class HarmonyKeyStoreKey implements Key {
    private static final long serialVersionUID = 7878434542490631315L;
    private final String keyAlgorithm;
    private final String keyAlias;
    private final int keyUid;

    @Override // java.security.Key
    public byte[] getEncoded() {
        return null;
    }

    @Override // java.security.Key
    public String getFormat() {
        return null;
    }

    public HarmonyKeyStoreKey(String str, int i, String str2) {
        this.keyAlias = str;
        this.keyUid = i;
        this.keyAlgorithm = str2;
    }

    /* access modifiers changed from: package-private */
    public String getAlias() {
        return this.keyAlias;
    }

    /* access modifiers changed from: package-private */
    public int getUid() {
        return this.keyUid;
    }

    @Override // java.security.Key
    public String getAlgorithm() {
        return this.keyAlgorithm;
    }

    @Override // java.lang.Object
    public int hashCode() {
        String str = this.keyAlgorithm;
        int i = 0;
        int hashCode = ((str == null ? 0 : str.hashCode()) + 31) * 31;
        String str2 = this.keyAlias;
        if (str2 != null) {
            i = str2.hashCode();
        }
        return ((hashCode + i) * 31) + this.keyUid;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        HarmonyKeyStoreKey harmonyKeyStoreKey = (HarmonyKeyStoreKey) obj;
        if (Objects.equals(this.keyAlgorithm, harmonyKeyStoreKey.keyAlgorithm) && Objects.equals(this.keyAlias, harmonyKeyStoreKey.keyAlias)) {
            return this.keyUid == harmonyKeyStoreKey.keyUid;
        }
        return false;
    }

    public AndroidKeyStoreKey toAndroidKey() {
        return new AndroidKeyStoreKey(this.keyAlias, this.keyUid, this.keyAlgorithm);
    }
}
