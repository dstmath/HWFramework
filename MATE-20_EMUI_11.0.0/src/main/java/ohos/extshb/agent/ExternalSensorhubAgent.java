package ohos.extshb.agent;

import ohos.extshb.manager.ExternalSensorhubManager;

public class ExternalSensorhubAgent {
    public CommandResult sendCommand(byte b, byte b2, byte[] bArr, boolean z) {
        return ExternalSensorhubManager.getInstance().sendCommand(b, b2, bArr, z);
    }

    public int subscribeSensorhubData(byte b, byte b2, IExternalSensorhubDataListener iExternalSensorhubDataListener) {
        return ExternalSensorhubManager.getInstance().subscribeDataListener(b, b2, iExternalSensorhubDataListener);
    }

    public int unsubscribeSensorhubData(byte b, byte b2, IExternalSensorhubDataListener iExternalSensorhubDataListener) {
        return ExternalSensorhubManager.getInstance().unsubscribeDataListener(b, b2, iExternalSensorhubDataListener);
    }

    public int queryMaxDataLen() {
        return ExternalSensorhubManager.getInstance().queryMaxDataLen();
    }
}
