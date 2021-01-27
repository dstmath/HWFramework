package com.android.internal.widget;

import com.android.internal.widget.LockPatternUtils;

public abstract class LockSettingsInternal {
    public abstract long addEscrowToken(byte[] bArr, int i, LockPatternUtils.EscrowTokenStateChangeCallback escrowTokenStateChangeCallback);

    public abstract boolean isEscrowTokenActive(long j, int i);

    public abstract boolean removeEscrowToken(long j, int i);

    public abstract boolean setLockCredentialWithToken(byte[] bArr, int i, long j, byte[] bArr2, int i2, int i3);

    public abstract boolean unlockUserWithToken(long j, byte[] bArr, int i);
}
