package android.security;

public class Scrypt {
    /* access modifiers changed from: package-private */
    public native byte[] nativeScrypt(byte[] bArr, byte[] bArr2, int i, int i2, int i3, int i4);

    public byte[] scrypt(byte[] password, byte[] salt, int n, int r, int p, int outLen) {
        return nativeScrypt(password, salt, n, r, p, outLen);
    }
}
