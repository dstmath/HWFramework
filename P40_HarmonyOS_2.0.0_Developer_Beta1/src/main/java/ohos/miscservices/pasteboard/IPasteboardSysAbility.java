package ohos.miscservices.pasteboard;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IPasteboardSysAbility extends IRemoteBroker {
    public static final int CLEAR_PASTE_DATA = 1;
    public static final int DISTRIBUTE_PASTE_DATA = 3;
    public static final int HAS_PASTE_DATA = 2;
    public static final int SET_PASTE_DATA = 0;

    void addPasteDataChangedListener(IPasteDataChangedListener iPasteDataChangedListener) throws RemoteException;

    void clear() throws RemoteException;

    PasteData getPasteData() throws RemoteException;

    boolean hasPasteData() throws RemoteException;

    boolean querySysDistributedAttr();

    void removePasteDataChangedListener(IPasteDataChangedListener iPasteDataChangedListener) throws RemoteException;

    void setPasteData(PasteData pasteData) throws RemoteException;

    void setSysDistributedAttr(boolean z);
}
