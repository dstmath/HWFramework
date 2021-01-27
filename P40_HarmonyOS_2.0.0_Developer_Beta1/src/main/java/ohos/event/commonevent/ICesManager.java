package ohos.event.commonevent;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

interface ICesManager extends IRemoteBroker {
    public static final int GET_ACTION_CLASS_NAME = 5;
    public static final int REGISTER_CONVERT_ACTION = 4;
    public static final int SUCCESS = 0;
    public static final int TRANS_SYNC = 0;
    public static final String descriptor = "OHOS.Notification.ICommonEventManager";

    CommonEventConvertInfo getActionClassName(String str) throws RemoteException;

    void registerActionConvert(CommonEventConvertInfo commonEventConvertInfo) throws RemoteException;
}
