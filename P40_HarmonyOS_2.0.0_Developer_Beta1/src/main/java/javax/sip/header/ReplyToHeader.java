package javax.sip.header;

public interface ReplyToHeader extends HeaderAddress, Header, Parameters {
    public static final String NAME = "Reply-To";

    String getDisplayName();
}
