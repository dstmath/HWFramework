package ohos.security.deviceauth;

public interface IHichainGroupCallback {
    void onError(long j, GroupOperationCode groupOperationCode, int i, String str);

    void onFinish(long j, GroupOperationCode groupOperationCode, String str);

    String onRequest(long j, GroupOperationCode groupOperationCode, String str);
}
