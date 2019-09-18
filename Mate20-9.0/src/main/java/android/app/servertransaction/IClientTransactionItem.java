package android.app.servertransaction;

import android.app.ClientTransactionHandler;
import android.os.IBinder;

public interface IClientTransactionItem {
    void execute(ClientTransactionHandler clientTransactionHandler, IBinder iBinder, PendingTransactionActions pendingTransactionActions);
}
