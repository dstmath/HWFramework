package sun.security.ssl;

import java.sql.Types;
import java.util.prefs.Preferences;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import sun.security.x509.GeneralNameInterface;
import sun.util.calendar.BaseCalendar;

final class Alerts {
    static final byte alert_access_denied = (byte) 49;
    static final byte alert_bad_certificate = (byte) 42;
    static final byte alert_bad_certificate_hash_value = (byte) 114;
    static final byte alert_bad_certificate_status_response = (byte) 113;
    static final byte alert_bad_record_mac = (byte) 20;
    static final byte alert_certificate_expired = (byte) 45;
    static final byte alert_certificate_revoked = (byte) 44;
    static final byte alert_certificate_unknown = (byte) 46;
    static final byte alert_certificate_unobtainable = (byte) 111;
    static final byte alert_close_notify = (byte) 0;
    static final byte alert_decode_error = (byte) 50;
    static final byte alert_decompression_failure = (byte) 30;
    static final byte alert_decrypt_error = (byte) 51;
    static final byte alert_decryption_failed = (byte) 21;
    static final byte alert_export_restriction = (byte) 60;
    static final byte alert_fatal = (byte) 2;
    static final byte alert_handshake_failure = (byte) 40;
    static final byte alert_illegal_parameter = (byte) 47;
    static final byte alert_insufficient_security = (byte) 71;
    static final byte alert_internal_error = (byte) 80;
    static final byte alert_no_certificate = (byte) 41;
    static final byte alert_no_renegotiation = (byte) 100;
    static final byte alert_protocol_version = (byte) 70;
    static final byte alert_record_overflow = (byte) 22;
    static final byte alert_unexpected_message = (byte) 10;
    static final byte alert_unknown_ca = (byte) 48;
    static final byte alert_unrecognized_name = (byte) 112;
    static final byte alert_unsupported_certificate = (byte) 43;
    static final byte alert_unsupported_extension = (byte) 110;
    static final byte alert_user_canceled = (byte) 90;
    static final byte alert_warning = (byte) 1;

    Alerts() {
    }

    static String alertDescription(byte code) {
        switch (code) {
            case GeneralNameInterface.NAME_MATCH /*0*/:
                return "close_notify";
            case BaseCalendar.OCTOBER /*10*/:
                return "unexpected_message";
            case Record.trailerSize /*20*/:
                return "bad_record_mac";
            case (byte) 21:
                return "decryption_failed";
            case ZipConstants.LOCLEN /*22*/:
                return "record_overflow";
            case AbstractSpinedBuffer.MAX_CHUNK_POWER /*30*/:
                return "decompression_failure";
            case (byte) 40:
                return "handshake_failure";
            case (byte) 41:
                return "no_certificate";
            case ZipConstants.CENOFF /*42*/:
                return "bad_certificate";
            case (byte) 43:
                return "unsupported_certificate";
            case (byte) 44:
                return "certificate_revoked";
            case (byte) 45:
                return "certificate_expired";
            case ZipConstants.CENHDR /*46*/:
                return "certificate_unknown";
            case (byte) 47:
                return "illegal_parameter";
            case (byte) 48:
                return "unknown_ca";
            case (byte) 49:
                return "access_denied";
            case (byte) 50:
                return "decode_error";
            case (byte) 51:
                return "decrypt_error";
            case (byte) 60:
                return "export_restriction";
            case Types.DATALINK /*70*/:
                return "protocol_version";
            case (byte) 71:
                return "insufficient_security";
            case Preferences.MAX_NAME_LENGTH /*80*/:
                return "internal_error";
            case (byte) 90:
                return "user_canceled";
            case (byte) 100:
                return "no_renegotiation";
            case (byte) 110:
                return "unsupported_extension";
            case (byte) 111:
                return "certificate_unobtainable";
            case (byte) 112:
                return "unrecognized_name";
            case (byte) 113:
                return "bad_certificate_status_response";
            case (byte) 114:
                return "bad_certificate_hash_value";
            default:
                return "<UNKNOWN ALERT: " + (code & 255) + ">";
        }
    }

    static SSLException getSSLException(byte description, String reason) {
        return getSSLException(description, null, reason);
    }

    static SSLException getSSLException(byte description, Throwable cause, String reason) {
        SSLException e;
        if (reason == null) {
            if (cause != null) {
                reason = cause.toString();
            } else {
                reason = "";
            }
        }
        switch (description) {
            case (byte) 40:
            case (byte) 41:
            case ZipConstants.CENOFF /*42*/:
            case (byte) 43:
            case (byte) 44:
            case (byte) 45:
            case ZipConstants.CENHDR /*46*/:
            case (byte) 48:
            case (byte) 49:
            case (byte) 51:
            case (byte) 60:
            case (byte) 71:
            case (byte) 110:
            case (byte) 111:
            case (byte) 112:
            case (byte) 113:
            case (byte) 114:
                e = new SSLHandshakeException(reason);
                break;
            default:
                e = new SSLException(reason);
                break;
        }
        if (cause != null) {
            e.initCause(cause);
        }
        return e;
    }
}
