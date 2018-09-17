package gov.nist.javax.sip;

import javax.sip.ServerTransaction;

public interface ServerTransactionExt extends ServerTransaction, TransactionExt {
    ServerTransaction getCanceledInviteTransaction();
}
