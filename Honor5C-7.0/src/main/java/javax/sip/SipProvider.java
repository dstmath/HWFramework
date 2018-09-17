package javax.sip;

import java.util.TooManyListenersException;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

public interface SipProvider {
    void addListeningPoint(ListeningPoint listeningPoint) throws ObjectInUseException;

    void addSipListener(SipListener sipListener) throws TooManyListenersException;

    ListeningPoint getListeningPoint();

    ListeningPoint getListeningPoint(String str);

    ListeningPoint[] getListeningPoints();

    CallIdHeader getNewCallId();

    ClientTransaction getNewClientTransaction(Request request) throws TransactionUnavailableException;

    Dialog getNewDialog(Transaction transaction) throws SipException;

    ServerTransaction getNewServerTransaction(Request request) throws TransactionAlreadyExistsException, TransactionUnavailableException;

    SipStack getSipStack();

    boolean isAutomaticDialogSupportEnabled();

    void removeListeningPoint(ListeningPoint listeningPoint) throws ObjectInUseException;

    void removeListeningPoints();

    void removeSipListener(SipListener sipListener);

    void sendRequest(Request request) throws SipException;

    void sendResponse(Response response) throws SipException;

    void setAutomaticDialogSupportEnabled(boolean z);

    void setListeningPoint(ListeningPoint listeningPoint) throws ObjectInUseException;
}
