package javax.net.ssl;

import java.net.IDN;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class SNIHostName extends SNIServerName {
    private final String hostname;

    private static final class SNIHostNameMatcher extends SNIMatcher {
        private final Pattern pattern;

        SNIHostNameMatcher(String regex) {
            super(0);
            this.pattern = Pattern.compile(regex, 2);
        }

        public boolean matches(SNIServerName serverName) {
            SNIHostName hostname;
            if (serverName != null) {
                if (serverName instanceof SNIHostName) {
                    hostname = (SNIHostName) serverName;
                } else if (serverName.getType() == 0) {
                    try {
                        hostname = new SNIHostName(serverName.getEncoded());
                    } catch (IllegalArgumentException | NullPointerException e) {
                        return false;
                    }
                } else {
                    throw new IllegalArgumentException("The server name type is not host_name");
                }
                String asciiName = hostname.getAsciiName();
                if (this.pattern.matcher(asciiName).matches()) {
                    return true;
                }
                return this.pattern.matcher(IDN.toUnicode(asciiName)).matches();
            }
            throw new NullPointerException("The SNIServerName argument cannot be null");
        }
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    public SNIHostName(String hostname2) {
        super(0, hostname.getBytes(StandardCharsets.US_ASCII));
        String hostname3 = IDN.toASCII((String) Objects.requireNonNull(hostname2, "Server name value of host_name cannot be null"), 2);
        this.hostname = hostname3;
        checkHostName();
    }

    public SNIHostName(byte[] encoded) {
        super(0, encoded);
        try {
            this.hostname = IDN.toASCII(StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(encoded)).toString());
            checkHostName();
        } catch (RuntimeException | CharacterCodingException e) {
            throw new IllegalArgumentException("The encoded server name value is invalid", e);
        }
    }

    public String getAsciiName() {
        return this.hostname;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof SNIHostName) {
            return this.hostname.equalsIgnoreCase(((SNIHostName) other).hostname);
        }
        return false;
    }

    public int hashCode() {
        return (31 * 17) + this.hostname.toUpperCase(Locale.ENGLISH).hashCode();
    }

    public String toString() {
        return "type=host_name (0), value=" + this.hostname;
    }

    public static SNIMatcher createSNIMatcher(String regex) {
        if (regex != null) {
            return new SNIHostNameMatcher(regex);
        }
        throw new NullPointerException("The regular expression cannot be null");
    }

    private void checkHostName() {
        if (this.hostname.isEmpty()) {
            throw new IllegalArgumentException("Server name value of host_name cannot be empty");
        } else if (this.hostname.endsWith(".")) {
            throw new IllegalArgumentException("Server name value of host_name cannot have the trailing dot");
        }
    }
}
