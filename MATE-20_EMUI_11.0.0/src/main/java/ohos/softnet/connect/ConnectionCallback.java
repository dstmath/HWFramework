package ohos.softnet.connect;

public interface ConnectionCallback {
    void onConnectionInit(String str, String str2, String str3);

    void onConnectionStateUpdate(String str, String str2, int i, String str3);
}
