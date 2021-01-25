package android.pc;

import android.os.Message;
import android.os.RemoteException;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DefaultHwPCManager implements HwPCManager {
    private static DefaultHwPCManager mInstance = new DefaultHwPCManager();

    @HwSystemApi
    public static DefaultHwPCManager getDefault() {
        return mInstance;
    }

    @Override // android.pc.HwPCManager
    @HwSystemApi
    public IHwPCManager getService() {
        return null;
    }

    @Override // android.pc.HwPCManager
    @HwSystemApi
    public void execVoiceCmd(Message message) throws RemoteException {
    }
}
