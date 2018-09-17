package javax.sip.header;

import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public interface ViaHeader extends Header, Parameters {
    public static final String NAME = "Via";

    String getBranch();

    String getHost();

    String getMAddr();

    int getPort();

    String getProtocol();

    int getRPort();

    String getReceived();

    String getSentByField();

    String getSentProtocolField();

    int getTTL();

    String getTransport();

    void setBranch(String str) throws ParseException;

    void setHost(String str) throws ParseException;

    void setMAddr(String str) throws ParseException;

    void setPort(int i) throws InvalidArgumentException;

    void setProtocol(String str) throws ParseException;

    void setRPort() throws InvalidArgumentException;

    void setReceived(String str) throws ParseException;

    void setTTL(int i) throws InvalidArgumentException;

    void setTransport(String str) throws ParseException;
}
