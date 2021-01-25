package ohos.rpc;

import java.io.FileDescriptor;

public interface IRemoteObject {
    public static final int DUMP_TRANSACTION = 1598311760;
    public static final int INTERFACE_TRANSACTION = 1598968902;
    public static final int MAX_TRANSACTION_ID = 16777215;
    public static final int MIN_TRANSACTION_ID = 1;
    public static final int PING_TRANSACTION = 1599098439;

    public interface DeathRecipient {
        void onRemoteDied();
    }

    boolean addDeathRecipient(DeathRecipient deathRecipient, int i);

    void dump(FileDescriptor fileDescriptor, String[] strArr) throws RemoteException;

    String getInterfaceDescriptor();

    boolean isObjectDead();

    IRemoteBroker queryLocalInterface(String str);

    boolean removeDeathRecipient(DeathRecipient deathRecipient, int i);

    boolean sendRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException;
}
