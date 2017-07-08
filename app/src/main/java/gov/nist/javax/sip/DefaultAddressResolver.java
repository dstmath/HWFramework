package gov.nist.javax.sip;

import gov.nist.core.net.AddressResolver;
import gov.nist.javax.sip.stack.HopImpl;
import gov.nist.javax.sip.stack.MessageProcessor;
import javax.sip.address.Hop;

public class DefaultAddressResolver implements AddressResolver {
    public Hop resolveAddress(Hop inputAddress) {
        if (inputAddress.getPort() != -1) {
            return inputAddress;
        }
        return new HopImpl(inputAddress.getHost(), MessageProcessor.getDefaultPort(inputAddress.getTransport()), inputAddress.getTransport());
    }
}
