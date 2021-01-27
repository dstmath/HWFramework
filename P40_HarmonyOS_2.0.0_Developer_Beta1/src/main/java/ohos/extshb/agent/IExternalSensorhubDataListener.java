package ohos.extshb.agent;

import ohos.annotation.SystemApi;

@SystemApi
public interface IExternalSensorhubDataListener {
    void onDataReceived(byte b, byte b2, byte[] bArr);
}
