package javax.sip.message;

import java.text.ParseException;

public interface Response extends Message {
    public static final int ACCEPTED = 202;
    public static final int ADDRESS_INCOMPLETE = 484;
    public static final int ALTERNATIVE_SERVICE = 380;
    public static final int AMBIGUOUS = 485;
    public static final int BAD_EVENT = 489;
    public static final int BAD_EXTENSION = 420;
    public static final int BAD_GATEWAY = 502;
    public static final int BAD_REQUEST = 400;
    public static final int BUSY_EVERYWHERE = 600;
    public static final int BUSY_HERE = 486;
    public static final int CALL_IS_BEING_FORWARDED = 181;
    public static final int CALL_OR_TRANSACTION_DOES_NOT_EXIST = 481;
    public static final int CONDITIONAL_REQUEST_FAILED = 412;
    public static final int DECLINE = 603;
    public static final int DOES_NOT_EXIST_ANYWHERE = 604;
    public static final int EXTENSION_REQUIRED = 421;
    public static final int FORBIDDEN = 403;
    public static final int GONE = 410;
    public static final int INTERVAL_TOO_BRIEF = 423;
    public static final int LOOP_DETECTED = 482;
    public static final int MESSAGE_TOO_LARGE = 513;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int MOVED_PERMANENTLY = 301;
    public static final int MOVED_TEMPORARILY = 302;
    public static final int MULTIPLE_CHOICES = 300;
    public static final int NOT_ACCEPTABLE = 406;
    public static final int NOT_ACCEPTABLE_HERE = 488;
    public static final int NOT_FOUND = 404;
    public static final int NOT_IMPLEMENTED = 501;
    public static final int OK = 200;
    public static final int PAYMENT_REQUIRED = 402;
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
    public static final int QUEUED = 182;
    public static final int REQUEST_ENTITY_TOO_LARGE = 413;
    public static final int REQUEST_PENDING = 491;
    public static final int REQUEST_TERMINATED = 487;
    public static final int REQUEST_TIMEOUT = 408;
    public static final int REQUEST_URI_TOO_LONG = 414;
    public static final int RINGING = 180;
    public static final int SERVER_INTERNAL_ERROR = 500;
    public static final int SERVER_TIMEOUT = 504;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int SESSION_NOT_ACCEPTABLE = 606;
    public static final int SESSION_PROGRESS = 183;
    public static final int TEMPORARILY_UNAVAILABLE = 480;
    public static final int TOO_MANY_HOPS = 483;
    public static final int TRYING = 100;
    public static final int UNAUTHORIZED = 401;
    public static final int UNDECIPHERABLE = 493;
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int UNSUPPORTED_URI_SCHEME = 416;
    public static final int USE_PROXY = 305;
    public static final int VERSION_NOT_SUPPORTED = 505;

    String getReasonPhrase();

    int getStatusCode();

    void setReasonPhrase(String str) throws ParseException;

    void setStatusCode(int i) throws ParseException;
}
