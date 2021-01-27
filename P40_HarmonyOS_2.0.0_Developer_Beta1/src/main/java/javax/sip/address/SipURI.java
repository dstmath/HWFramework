package javax.sip.address;

import java.text.ParseException;
import java.util.Iterator;
import javax.sip.InvalidArgumentException;
import javax.sip.header.Parameters;

public interface SipURI extends URI, Parameters {
    String getHeader(String str);

    Iterator getHeaderNames();

    String getHost();

    String getLrParam();

    String getMAddrParam();

    String getMethodParam();

    int getPort();

    int getTTLParam();

    String getTransportParam();

    String getUser();

    String getUserAtHost();

    String getUserAtHostPort();

    String getUserParam();

    String getUserPassword();

    String getUserType();

    boolean hasLrParam();

    boolean hasTransport();

    boolean isSecure();

    void removeUserType();

    void setHeader(String str, String str2);

    void setHost(String str) throws ParseException;

    void setLrParam();

    void setMAddrParam(String str) throws ParseException;

    void setMethodParam(String str) throws ParseException;

    void setPort(int i) throws InvalidArgumentException;

    void setSecure(boolean z);

    void setTTLParam(int i);

    void setTransportParam(String str) throws ParseException;

    void setUser(String str);

    void setUserParam(String str);

    void setUserPassword(String str);
}
