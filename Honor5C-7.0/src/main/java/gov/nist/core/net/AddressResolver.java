package gov.nist.core.net;

import javax.sip.address.Hop;

public interface AddressResolver {
    Hop resolveAddress(Hop hop);
}
