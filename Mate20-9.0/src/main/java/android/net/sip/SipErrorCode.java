package android.net.sip;

public class SipErrorCode {
    public static final int CLIENT_ERROR = -4;
    public static final int CROSS_DOMAIN_AUTHENTICATION = -11;
    public static final int DATA_CONNECTION_LOST = -10;
    public static final int INVALID_CREDENTIALS = -8;
    public static final int INVALID_REMOTE_URI = -6;
    public static final int IN_PROGRESS = -9;
    public static final int NO_ERROR = 0;
    public static final int PEER_NOT_REACHABLE = -7;
    public static final int SERVER_ERROR = -2;
    public static final int SERVER_UNREACHABLE = -12;
    public static final int SOCKET_ERROR = -1;
    public static final int TIME_OUT = -5;
    public static final int TRANSACTION_TERMINTED = -3;

    public static String toString(int errorCode) {
        switch (errorCode) {
            case SERVER_UNREACHABLE /*-12*/:
                return "SERVER_UNREACHABLE";
            case CROSS_DOMAIN_AUTHENTICATION /*-11*/:
                return "CROSS_DOMAIN_AUTHENTICATION";
            case DATA_CONNECTION_LOST /*-10*/:
                return "DATA_CONNECTION_LOST";
            case IN_PROGRESS /*-9*/:
                return "IN_PROGRESS";
            case INVALID_CREDENTIALS /*-8*/:
                return "INVALID_CREDENTIALS";
            case PEER_NOT_REACHABLE /*-7*/:
                return "PEER_NOT_REACHABLE";
            case INVALID_REMOTE_URI /*-6*/:
                return "INVALID_REMOTE_URI";
            case TIME_OUT /*-5*/:
                return "TIME_OUT";
            case CLIENT_ERROR /*-4*/:
                return "CLIENT_ERROR";
            case TRANSACTION_TERMINTED /*-3*/:
                return "TRANSACTION_TERMINTED";
            case SERVER_ERROR /*-2*/:
                return "SERVER_ERROR";
            case SOCKET_ERROR /*-1*/:
                return "SOCKET_ERROR";
            case 0:
                return "NO_ERROR";
            default:
                return "UNKNOWN";
        }
    }

    private SipErrorCode() {
    }
}
