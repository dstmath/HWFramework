package javax.sip.header;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import javax.sip.InvalidArgumentException;
import javax.sip.address.Address;
import javax.sip.address.URI;

public interface HeaderFactory {
    AcceptEncodingHeader createAcceptEncodingHeader(String str) throws ParseException;

    AcceptHeader createAcceptHeader(String str, String str2) throws ParseException;

    AcceptLanguageHeader createAcceptLanguageHeader(Locale locale);

    AlertInfoHeader createAlertInfoHeader(URI uri);

    AllowEventsHeader createAllowEventsHeader(String str) throws ParseException;

    AllowHeader createAllowHeader(String str) throws ParseException;

    AuthenticationInfoHeader createAuthenticationInfoHeader(String str) throws ParseException;

    AuthorizationHeader createAuthorizationHeader(String str) throws ParseException;

    CSeqHeader createCSeqHeader(int i, String str) throws ParseException, InvalidArgumentException;

    CSeqHeader createCSeqHeader(long j, String str) throws ParseException, InvalidArgumentException;

    CallIdHeader createCallIdHeader(String str) throws ParseException;

    CallInfoHeader createCallInfoHeader(URI uri);

    ContactHeader createContactHeader();

    ContactHeader createContactHeader(Address address);

    ContentDispositionHeader createContentDispositionHeader(String str) throws ParseException;

    ContentEncodingHeader createContentEncodingHeader(String str) throws ParseException;

    ContentLanguageHeader createContentLanguageHeader(Locale locale);

    ContentLengthHeader createContentLengthHeader(int i) throws InvalidArgumentException;

    ContentTypeHeader createContentTypeHeader(String str, String str2) throws ParseException;

    DateHeader createDateHeader(Calendar calendar);

    ErrorInfoHeader createErrorInfoHeader(URI uri);

    EventHeader createEventHeader(String str) throws ParseException;

    ExpiresHeader createExpiresHeader(int i) throws InvalidArgumentException;

    ExtensionHeader createExtensionHeader(String str, String str2) throws ParseException;

    FromHeader createFromHeader(Address address, String str) throws ParseException;

    Header createHeader(String str) throws ParseException;

    Header createHeader(String str, String str2) throws ParseException;

    List createHeaders(String str) throws ParseException;

    InReplyToHeader createInReplyToHeader(String str) throws ParseException;

    MaxForwardsHeader createMaxForwardsHeader(int i) throws InvalidArgumentException;

    MimeVersionHeader createMimeVersionHeader(int i, int i2) throws InvalidArgumentException;

    MinExpiresHeader createMinExpiresHeader(int i) throws InvalidArgumentException;

    OrganizationHeader createOrganizationHeader(String str) throws ParseException;

    PriorityHeader createPriorityHeader(String str) throws ParseException;

    ProxyAuthenticateHeader createProxyAuthenticateHeader(String str) throws ParseException;

    ProxyAuthorizationHeader createProxyAuthorizationHeader(String str) throws ParseException;

    ProxyRequireHeader createProxyRequireHeader(String str) throws ParseException;

    RAckHeader createRAckHeader(int i, int i2, String str) throws InvalidArgumentException, ParseException;

    RAckHeader createRAckHeader(long j, long j2, String str) throws InvalidArgumentException, ParseException;

    RSeqHeader createRSeqHeader(int i) throws InvalidArgumentException;

    RSeqHeader createRSeqHeader(long j) throws InvalidArgumentException;

    ReasonHeader createReasonHeader(String str, int i, String str2) throws InvalidArgumentException, ParseException;

    RecordRouteHeader createRecordRouteHeader(Address address);

    ReferToHeader createReferToHeader(Address address);

    ReplyToHeader createReplyToHeader(Address address);

    RequireHeader createRequireHeader(String str) throws ParseException;

    RetryAfterHeader createRetryAfterHeader(int i) throws InvalidArgumentException;

    RouteHeader createRouteHeader(Address address);

    SIPETagHeader createSIPETagHeader(String str) throws ParseException;

    SIPIfMatchHeader createSIPIfMatchHeader(String str) throws ParseException;

    ServerHeader createServerHeader(List list) throws ParseException;

    SubjectHeader createSubjectHeader(String str) throws ParseException;

    SubscriptionStateHeader createSubscriptionStateHeader(String str) throws ParseException;

    SupportedHeader createSupportedHeader(String str) throws ParseException;

    TimeStampHeader createTimeStampHeader(float f) throws InvalidArgumentException;

    ToHeader createToHeader(Address address, String str) throws ParseException;

    UnsupportedHeader createUnsupportedHeader(String str) throws ParseException;

    UserAgentHeader createUserAgentHeader(List list) throws ParseException;

    ViaHeader createViaHeader(String str, int i, String str2, String str3) throws InvalidArgumentException, ParseException;

    WWWAuthenticateHeader createWWWAuthenticateHeader(String str) throws ParseException;

    WarningHeader createWarningHeader(String str, int i, String str2) throws InvalidArgumentException, ParseException;

    void setPrettyEncoding(boolean z);
}
