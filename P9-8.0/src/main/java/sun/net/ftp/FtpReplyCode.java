package sun.net.ftp;

import java.net.HttpURLConnection;
import sun.util.logging.PlatformLogger;

public enum FtpReplyCode {
    RESTART_MARKER(110),
    SERVICE_READY_IN(120),
    DATA_CONNECTION_ALREADY_OPEN(125),
    FILE_STATUS_OK(150),
    COMMAND_OK(HttpURLConnection.HTTP_OK),
    NOT_IMPLEMENTED(HttpURLConnection.HTTP_ACCEPTED),
    SYSTEM_STATUS(211),
    DIRECTORY_STATUS(212),
    FILE_STATUS(213),
    HELP_MESSAGE(214),
    NAME_SYSTEM_TYPE(215),
    SERVICE_READY(220),
    SERVICE_CLOSING(221),
    DATA_CONNECTION_OPEN(225),
    CLOSING_DATA_CONNECTION(226),
    ENTERING_PASSIVE_MODE(227),
    ENTERING_EXT_PASSIVE_MODE(229),
    LOGGED_IN(230),
    SECURELY_LOGGED_IN(232),
    SECURITY_EXCHANGE_OK(234),
    SECURITY_EXCHANGE_COMPLETE(235),
    FILE_ACTION_OK(250),
    PATHNAME_CREATED(257),
    NEED_PASSWORD(331),
    NEED_ACCOUNT(332),
    NEED_ADAT(334),
    NEED_MORE_ADAT(335),
    FILE_ACTION_PENDING(350),
    SERVICE_NOT_AVAILABLE(421),
    CANT_OPEN_DATA_CONNECTION(425),
    CONNECTION_CLOSED(426),
    NEED_SECURITY_RESOURCE(431),
    FILE_ACTION_NOT_TAKEN(450),
    ACTION_ABORTED(451),
    INSUFFICIENT_STORAGE(452),
    COMMAND_UNRECOGNIZED(500),
    INVALID_PARAMETER(HttpURLConnection.HTTP_NOT_IMPLEMENTED),
    BAD_SEQUENCE(HttpURLConnection.HTTP_UNAVAILABLE),
    NOT_IMPLEMENTED_FOR_PARAMETER(HttpURLConnection.HTTP_GATEWAY_TIMEOUT),
    NOT_LOGGED_IN(530),
    NEED_ACCOUNT_FOR_STORING(532),
    PROT_LEVEL_DENIED(533),
    REQUEST_DENIED(534),
    FAILED_SECURITY_CHECK(535),
    UNSUPPORTED_PROT_LEVEL(536),
    PROT_LEVEL_NOT_SUPPORTED_BY_SECURITY(537),
    FILE_UNAVAILABLE(550),
    PAGE_TYPE_UNKNOWN(551),
    EXCEEDED_STORAGE(552),
    FILE_NAME_NOT_ALLOWED(553),
    PROTECTED_REPLY(631),
    UNKNOWN_ERROR(999);
    
    private final int value;

    private FtpReplyCode(int val) {
        this.value = val;
    }

    public int getValue() {
        return this.value;
    }

    public boolean isPositivePreliminary() {
        return this.value >= 100 && this.value < HttpURLConnection.HTTP_OK;
    }

    public boolean isPositiveCompletion() {
        return this.value >= HttpURLConnection.HTTP_OK && this.value < 300;
    }

    public boolean isPositiveIntermediate() {
        return this.value >= 300 && this.value < 400;
    }

    public boolean isTransientNegative() {
        return this.value >= 400 && this.value < 500;
    }

    public boolean isPermanentNegative() {
        return this.value >= 500 && this.value < 600;
    }

    public boolean isProtectedReply() {
        return this.value >= 600 && this.value < PlatformLogger.CONFIG;
    }

    public boolean isSyntax() {
        return (this.value / 10) - ((this.value / 100) * 10) == 0;
    }

    public boolean isInformation() {
        return (this.value / 10) - ((this.value / 100) * 10) == 1;
    }

    public boolean isConnection() {
        return (this.value / 10) - ((this.value / 100) * 10) == 2;
    }

    public boolean isAuthentication() {
        return (this.value / 10) - ((this.value / 100) * 10) == 3;
    }

    public boolean isUnspecified() {
        return (this.value / 10) - ((this.value / 100) * 10) == 4;
    }

    public boolean isFileSystem() {
        return (this.value / 10) - ((this.value / 100) * 10) == 5;
    }

    public static FtpReplyCode find(int v) {
        for (FtpReplyCode code : values()) {
            if (code.getValue() == v) {
                return code;
            }
        }
        return UNKNOWN_ERROR;
    }
}
