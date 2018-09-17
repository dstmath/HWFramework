package javax.sip;

import java.util.Collection;
import java.util.Iterator;
import javax.sip.address.Router;

public interface SipStack {
    ListeningPoint createListeningPoint(int i, String str) throws TransportNotSupportedException, InvalidArgumentException;

    ListeningPoint createListeningPoint(String str, int i, String str2) throws TransportNotSupportedException, InvalidArgumentException;

    SipProvider createSipProvider(ListeningPoint listeningPoint) throws ObjectInUseException;

    void deleteListeningPoint(ListeningPoint listeningPoint) throws ObjectInUseException;

    void deleteSipProvider(SipProvider sipProvider) throws ObjectInUseException;

    Collection getDialogs();

    String getIPAddress();

    Iterator getListeningPoints();

    Router getRouter();

    Iterator getSipProviders();

    String getStackName();

    boolean isRetransmissionFilterActive();

    void start() throws ProviderDoesNotExistException, SipException;

    void stop();
}
