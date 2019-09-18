package com.android.server;

public interface PersistentDataBlockManagerInternal {
    void forceOemUnlockEnabled(boolean z);

    byte[] getFrpCredentialHandle();

    void setFrpCredentialHandle(byte[] bArr);
}
