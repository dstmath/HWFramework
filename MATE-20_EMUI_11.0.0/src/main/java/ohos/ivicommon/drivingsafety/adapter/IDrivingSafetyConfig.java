package ohos.ivicommon.drivingsafety.adapter;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IDrivingSafetyConfig extends IRemoteBroker {
    int getDrivingSafetyConfigure(String str, String str2, StringBuffer stringBuffer) throws RemoteException, IllegalArgumentException;

    int setDrivingSafetyConfigure(String str, Boolean bool) throws RemoteException, IllegalArgumentException;
}
