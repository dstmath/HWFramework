package javax.sip;

import java.io.Serializable;
import javax.sip.message.Request;

public interface Transaction extends Serializable {
    Object getApplicationData();

    String getBranchId();

    Dialog getDialog();

    String getHost();

    String getPeerAddress();

    int getPeerPort();

    int getPort();

    Request getRequest();

    int getRetransmitTimer() throws UnsupportedOperationException;

    SipProvider getSipProvider();

    TransactionState getState();

    String getTransport();

    void setApplicationData(Object obj);

    void setRetransmitTimer(int i) throws UnsupportedOperationException;

    void terminate() throws ObjectInUseException;
}
