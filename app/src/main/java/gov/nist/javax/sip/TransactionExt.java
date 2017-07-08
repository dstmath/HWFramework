package gov.nist.javax.sip;

import java.security.cert.Certificate;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.sip.SipProvider;
import javax.sip.Transaction;

public interface TransactionExt extends Transaction {
    String getCipherSuite() throws UnsupportedOperationException;

    String getHost();

    Certificate[] getLocalCertificates() throws UnsupportedOperationException;

    String getPeerAddress();

    Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException;

    int getPeerPort();

    int getPort();

    SipProvider getSipProvider();

    String getTransport();
}
