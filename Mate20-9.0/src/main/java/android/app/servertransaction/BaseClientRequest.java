package android.app.servertransaction;

import android.app.ClientTransactionHandler;
import android.os.IBinder;

public interface BaseClientRequest extends ObjectPoolItem {
    void execute(ClientTransactionHandler clientTransactionHandler, IBinder iBinder, PendingTransactionActions pendingTransactionActions);

    void preExecute(ClientTransactionHandler client, IBinder token) {
    }

    void postExecute(ClientTransactionHandler client, IBinder token, PendingTransactionActions pendingActions) {
    }
}
