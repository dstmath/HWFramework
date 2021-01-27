package com.android.server.backup.encryption.storage;

public class TertiaryKey {
    private final String mPackageName;
    private final String mSecondaryKeyAlias;
    private final byte[] mWrappedKeyBytes;

    public TertiaryKey(String secondaryKeyAlias, String packageName, byte[] wrappedKeyBytes) {
        this.mSecondaryKeyAlias = secondaryKeyAlias;
        this.mPackageName = packageName;
        this.mWrappedKeyBytes = wrappedKeyBytes;
    }

    public String getSecondaryKeyAlias() {
        return this.mSecondaryKeyAlias;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public byte[] getWrappedKeyBytes() {
        return this.mWrappedKeyBytes;
    }
}
