package gov.nist.javax.sip;

import javax.sip.Dialog;
import javax.sip.ResponseEvent;
import javax.sip.message.Response;

public class ResponseEventExt extends ResponseEvent {
    private ClientTransactionExt m_originalTransaction;

    public ResponseEventExt(Object source, ClientTransactionExt clientTransaction, Dialog dialog, Response response) {
        super(source, clientTransaction, dialog, response);
        this.m_originalTransaction = clientTransaction;
    }

    public boolean isForkedResponse() {
        return super.getClientTransaction() == null && this.m_originalTransaction != null;
    }

    public void setOriginalTransaction(ClientTransactionExt originalTransaction) {
        this.m_originalTransaction = originalTransaction;
    }

    public ClientTransactionExt getOriginalTransaction() {
        return this.m_originalTransaction;
    }
}
