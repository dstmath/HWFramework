package android.database;

import android.os.Bundle;
import android.os.IInterface;
import android.os.RemoteException;

public interface IBulkCursor extends IInterface {
    public static final int CLOSE_TRANSACTION = 7;
    public static final int DEACTIVATE_TRANSACTION = 2;
    public static final int GET_CURSOR_WINDOW_TRANSACTION = 1;
    public static final int GET_EXTRAS_TRANSACTION = 5;
    public static final int ON_MOVE_TRANSACTION = 4;
    public static final int REQUERY_TRANSACTION = 3;
    public static final int RESPOND_TRANSACTION = 6;
    public static final String descriptor = "android.content.IBulkCursor";

    void close() throws RemoteException;

    void deactivate() throws RemoteException;

    Bundle getExtras() throws RemoteException;

    CursorWindow getWindow(int i) throws RemoteException;

    void onMove(int i) throws RemoteException;

    int requery(IContentObserver iContentObserver) throws RemoteException;

    Bundle respond(Bundle bundle) throws RemoteException;
}
