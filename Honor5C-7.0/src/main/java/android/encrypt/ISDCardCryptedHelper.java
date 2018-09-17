package android.encrypt;

public interface ISDCardCryptedHelper {
    int addUserKeyAuth(int i, int i2, byte[] bArr, byte[] bArr2);

    int unlockKey(int i, int i2, byte[] bArr, byte[] bArr2);
}
