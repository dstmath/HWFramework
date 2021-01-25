package gov.nist.javax.sip;

import javax.sip.ServerTransaction;

public interface ServerTransactionExt extends ServerTransaction, TransactionExt {
    @Override // javax.sip.ServerTransaction
    ServerTransaction getCanceledInviteTransaction();
}
