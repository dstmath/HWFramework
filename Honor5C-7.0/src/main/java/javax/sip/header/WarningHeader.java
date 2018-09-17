package javax.sip.header;

import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public interface WarningHeader extends Header {
    public static final int ATTRIBUTE_NOT_UNDERSTOOD = 10;
    public static final int INCOMPATIBLE_BANDWIDTH_UNITS = 20;
    public static final int INCOMPATIBLE_MEDIA_FORMAT = 21;
    public static final int INCOMPATIBLE_NETWORK_ADDRESS_FORMATS = 22;
    public static final int INCOMPATIBLE_NETWORK_PROTOCOL = 23;
    public static final int INCOMPATIBLE_TRANSPORT_PROTOCOL = 24;
    public static final int INSUFFICIENT_BANDWIDTH = 30;
    public static final int MEDIA_TYPE_NOT_AVAILABLE = 40;
    public static final int MISCELLANEOUS_WARNING = 99;
    public static final int MULTICAST_NOT_AVAILABLE = 50;
    public static final String NAME = "Warning";
    public static final int SESSION_DESCRIPTION_PARAMETER_NOT_UNDERSTOOD = 60;
    public static final int UNICAST_NOT_AVAILABLE = 51;

    String getAgent();

    int getCode();

    String getText();

    void setAgent(String str) throws ParseException;

    void setCode(int i) throws InvalidArgumentException;

    void setText(String str) throws ParseException;
}
