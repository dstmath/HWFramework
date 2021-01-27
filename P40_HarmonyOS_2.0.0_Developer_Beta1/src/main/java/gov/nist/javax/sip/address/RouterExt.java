package gov.nist.javax.sip.address;

import javax.sip.address.Hop;
import javax.sip.address.Router;

public interface RouterExt extends Router {
    void transactionTimeout(Hop hop);
}
