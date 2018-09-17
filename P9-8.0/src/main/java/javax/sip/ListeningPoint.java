package javax.sip;

import java.io.IOException;
import java.text.ParseException;
import javax.sip.header.ContactHeader;

public interface ListeningPoint extends Cloneable {
    public static final int PORT_5060 = 5060;
    public static final int PORT_5061 = 5061;
    public static final String SCTP = "SCTP";
    public static final String TCP = "TCP";
    public static final String TLS = "TLS";
    public static final String UDP = "UDP";

    ContactHeader createContactHeader();

    String getIPAddress();

    int getPort();

    String getSentBy();

    String getTransport();

    void sendHeartbeat(String str, int i) throws IOException;

    void setSentBy(String str) throws ParseException;
}
