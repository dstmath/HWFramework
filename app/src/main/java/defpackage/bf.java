package defpackage;

/* renamed from: bf */
final class bf {
    public static byte[] a(byte[] bArr, int i, byte[] bArr2, int i2) {
        return be.encode(bArr, i, bArr2, i2);
    }

    public static byte[] a(byte[] bArr, byte[] bArr2, int i) {
        return be.decode(bArr, 0, bArr2, i);
    }
}
