package ohos.softnet.connect;

public interface DataCallback {
    int onBlockReceive(String str, String str2, byte[] bArr, int i, String str3);

    int onByteReceive(String str, String str2, byte[] bArr, int i, String str3);

    String onCommonUpdate(String str);

    int onFileReceive(String str, String str2, String str3, String str4);

    int onSendFileStateUpdate(String str, String str2, int i, String str3);

    int onStreamReceive(String str, String str2, DataPayload dataPayload, String str3);
}
