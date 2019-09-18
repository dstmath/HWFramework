package android.app.servertransaction;

import java.util.ArrayList;
import java.util.List;

public class ClientTransactionUtils {
    static List<Object> getCallbacks(Object clientTransaction) {
        if (!(clientTransaction instanceof ClientTransaction)) {
            return null;
        }
        List<ClientTransactionItem> org = ((ClientTransaction) clientTransaction).getCallbacks();
        List<Object> ret = new ArrayList<>();
        ret.addAll(org);
        return ret;
    }

    static void addCallback(Object clientTransaction, Object clientTransactionItem) {
        if ((clientTransaction instanceof ClientTransaction) && (clientTransactionItem instanceof IClientTransactionItem)) {
            ((ClientTransaction) clientTransaction).addCallback(new ClientTransactionItemImpl((IClientTransactionItem) clientTransactionItem));
        }
    }
}
