package gov.nist.javax.sip;

import java.security.cert.Certificate;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.sip.SipProvider;
import javax.sip.Transaction;

public interface TransactionExt extends Transaction {
    String getCipherSuite() throws UnsupportedOperationException;

    @Override // javax.sip.Transaction
    String getHost();

    Certificate[] getLocalCertificates() throws UnsupportedOperationException;

    @Override // javax.sip.Transaction
    String getPeerAddress();

    Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException;

    @Override // javax.sip.Transaction
    int getPeerPort();

    @Override // javax.sip.Transaction
    int getPort();

    @Override // javax.sip.Transaction
    SipProvider getSipProvider();

    @Override // javax.sip.Transaction
    String getTransport();
}
