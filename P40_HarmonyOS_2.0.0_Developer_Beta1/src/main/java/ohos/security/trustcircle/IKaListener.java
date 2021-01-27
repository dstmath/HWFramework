package ohos.security.trustcircle;

public interface IKaListener {
    void onError(long j, int i);

    void onResult(long j, int i, byte[] bArr, byte[] bArr2);
}
