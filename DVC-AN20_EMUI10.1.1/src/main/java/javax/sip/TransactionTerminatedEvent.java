package javax.sip;

import java.util.EventObject;

public class TransactionTerminatedEvent extends EventObject {
    private ClientTransaction mClientTransaction;
    private boolean mIsServerTransaction = false;
    private ServerTransaction mServerTransaction;

    public TransactionTerminatedEvent(Object source, ServerTransaction serverTransaction) {
        super(source);
        this.mServerTransaction = serverTransaction;
    }

    public TransactionTerminatedEvent(Object source, ClientTransaction clientTransaction) {
        super(source);
        this.mClientTransaction = clientTransaction;
    }

    public boolean isServerTransaction() {
        return this.mIsServerTransaction;
    }

    public ClientTransaction getClientTransaction() {
        return this.mClientTransaction;
    }

    public ServerTransaction getServerTransaction() {
        return this.mServerTransaction;
    }
}
