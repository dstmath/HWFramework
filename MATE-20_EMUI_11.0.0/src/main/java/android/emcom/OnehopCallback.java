package android.emcom;

public interface OnehopCallback {
    String onOnehopCommonCallback(String str);

    int onOnehopDataReceived(String str, int i, byte[] bArr, int i2, String str2);

    int onOnehopDeviceConnectStateChanged(String str);

    int onOnehopDeviceListChanged(OnehopDeviceInfo[] onehopDeviceInfoArr);

    int onOnehopSendStateUpdated(String str);
}
