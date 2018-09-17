package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import gov.nist.javax.sip.address.GenericURI;
import gov.nist.javax.sip.header.extensions.Join;
import gov.nist.javax.sip.header.extensions.JoinHeader;
import gov.nist.javax.sip.header.extensions.MinSE;
import gov.nist.javax.sip.header.extensions.References;
import gov.nist.javax.sip.header.extensions.ReferencesHeader;
import gov.nist.javax.sip.header.extensions.ReferredBy;
import gov.nist.javax.sip.header.extensions.ReferredByHeader;
import gov.nist.javax.sip.header.extensions.Replaces;
import gov.nist.javax.sip.header.extensions.ReplacesHeader;
import gov.nist.javax.sip.header.extensions.SessionExpires;
import gov.nist.javax.sip.header.extensions.SessionExpiresHeader;
import gov.nist.javax.sip.header.ims.PAccessNetworkInfo;
import gov.nist.javax.sip.header.ims.PAccessNetworkInfoHeader;
import gov.nist.javax.sip.header.ims.PAssertedIdentity;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import gov.nist.javax.sip.header.ims.PAssertedService;
import gov.nist.javax.sip.header.ims.PAssertedServiceHeader;
import gov.nist.javax.sip.header.ims.PAssociatedURI;
import gov.nist.javax.sip.header.ims.PAssociatedURIHeader;
import gov.nist.javax.sip.header.ims.PCalledPartyID;
import gov.nist.javax.sip.header.ims.PCalledPartyIDHeader;
import gov.nist.javax.sip.header.ims.PChargingFunctionAddresses;
import gov.nist.javax.sip.header.ims.PChargingFunctionAddressesHeader;
import gov.nist.javax.sip.header.ims.PChargingVector;
import gov.nist.javax.sip.header.ims.PChargingVectorHeader;
import gov.nist.javax.sip.header.ims.PMediaAuthorization;
import gov.nist.javax.sip.header.ims.PMediaAuthorizationHeader;
import gov.nist.javax.sip.header.ims.PPreferredIdentity;
import gov.nist.javax.sip.header.ims.PPreferredIdentityHeader;
import gov.nist.javax.sip.header.ims.PPreferredService;
import gov.nist.javax.sip.header.ims.PPreferredServiceHeader;
import gov.nist.javax.sip.header.ims.PProfileKey;
import gov.nist.javax.sip.header.ims.PProfileKeyHeader;
import gov.nist.javax.sip.header.ims.PServedUser;
import gov.nist.javax.sip.header.ims.PServedUserHeader;
import gov.nist.javax.sip.header.ims.PUserDatabase;
import gov.nist.javax.sip.header.ims.PUserDatabaseHeader;
import gov.nist.javax.sip.header.ims.PVisitedNetworkID;
import gov.nist.javax.sip.header.ims.PVisitedNetworkIDHeader;
import gov.nist.javax.sip.header.ims.Path;
import gov.nist.javax.sip.header.ims.PathHeader;
import gov.nist.javax.sip.header.ims.Privacy;
import gov.nist.javax.sip.header.ims.PrivacyHeader;
import gov.nist.javax.sip.header.ims.SecurityClient;
import gov.nist.javax.sip.header.ims.SecurityClientHeader;
import gov.nist.javax.sip.header.ims.SecurityServer;
import gov.nist.javax.sip.header.ims.SecurityServerHeader;
import gov.nist.javax.sip.header.ims.SecurityVerify;
import gov.nist.javax.sip.header.ims.SecurityVerifyHeader;
import gov.nist.javax.sip.header.ims.ServiceRoute;
import gov.nist.javax.sip.header.ims.ServiceRouteHeader;
import gov.nist.javax.sip.parser.RequestLineParser;
import gov.nist.javax.sip.parser.StatusLineParser;
import gov.nist.javax.sip.parser.StringMsgParser;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import javax.sip.InvalidArgumentException;
import javax.sip.address.Address;
import javax.sip.address.URI;
import javax.sip.header.AcceptEncodingHeader;
import javax.sip.header.AcceptHeader;
import javax.sip.header.AcceptLanguageHeader;
import javax.sip.header.AlertInfoHeader;
import javax.sip.header.AllowEventsHeader;
import javax.sip.header.AllowHeader;
import javax.sip.header.AuthenticationInfoHeader;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.CallInfoHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentEncodingHeader;
import javax.sip.header.ContentLanguageHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.DateHeader;
import javax.sip.header.ErrorInfoHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.ExtensionHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.InReplyToHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.MimeVersionHeader;
import javax.sip.header.MinExpiresHeader;
import javax.sip.header.OrganizationHeader;
import javax.sip.header.PriorityHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.ProxyRequireHeader;
import javax.sip.header.RAckHeader;
import javax.sip.header.RSeqHeader;
import javax.sip.header.ReasonHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ReferToHeader;
import javax.sip.header.ReplyToHeader;
import javax.sip.header.RequireHeader;
import javax.sip.header.RetryAfterHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.SIPETagHeader;
import javax.sip.header.SIPIfMatchHeader;
import javax.sip.header.ServerHeader;
import javax.sip.header.SubjectHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.header.SupportedHeader;
import javax.sip.header.TimeStampHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.UnsupportedHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.header.WarningHeader;

public class HeaderFactoryImpl implements HeaderFactory, HeaderFactoryExt {
    private boolean stripAddressScopeZones;

    public void setPrettyEncoding(boolean flag) {
        SIPHeaderList.setPrettyEncode(flag);
    }

    public AcceptEncodingHeader createAcceptEncodingHeader(String encoding) throws ParseException {
        if (encoding == null) {
            throw new NullPointerException("the encoding parameter is null");
        }
        AcceptEncoding acceptEncoding = new AcceptEncoding();
        acceptEncoding.setEncoding(encoding);
        return acceptEncoding;
    }

    public AcceptHeader createAcceptHeader(String contentType, String contentSubType) throws ParseException {
        if (contentType == null || contentSubType == null) {
            throw new NullPointerException("contentType or subtype is null ");
        }
        Accept accept = new Accept();
        accept.setContentType(contentType);
        accept.setContentSubType(contentSubType);
        return accept;
    }

    public AcceptLanguageHeader createAcceptLanguageHeader(Locale language) {
        if (language == null) {
            throw new NullPointerException("null arg");
        }
        AcceptLanguage acceptLanguage = new AcceptLanguage();
        acceptLanguage.setAcceptLanguage(language);
        return acceptLanguage;
    }

    public AlertInfoHeader createAlertInfoHeader(URI alertInfo) {
        if (alertInfo == null) {
            throw new NullPointerException("null arg alertInfo");
        }
        AlertInfo a = new AlertInfo();
        a.setAlertInfo(alertInfo);
        return a;
    }

    public AllowEventsHeader createAllowEventsHeader(String eventType) throws ParseException {
        if (eventType == null) {
            throw new NullPointerException("null arg eventType");
        }
        AllowEvents allowEvents = new AllowEvents();
        allowEvents.setEventType(eventType);
        return allowEvents;
    }

    public AllowHeader createAllowHeader(String method) throws ParseException {
        if (method == null) {
            throw new NullPointerException("null arg method");
        }
        Allow allow = new Allow();
        allow.setMethod(method);
        return allow;
    }

    public AuthenticationInfoHeader createAuthenticationInfoHeader(String response) throws ParseException {
        if (response == null) {
            throw new NullPointerException("null arg response");
        }
        AuthenticationInfo auth = new AuthenticationInfo();
        auth.setResponse(response);
        return auth;
    }

    public AuthorizationHeader createAuthorizationHeader(String scheme) throws ParseException {
        if (scheme == null) {
            throw new NullPointerException("null arg scheme ");
        }
        Authorization auth = new Authorization();
        auth.setScheme(scheme);
        return auth;
    }

    public CSeqHeader createCSeqHeader(long sequenceNumber, String method) throws ParseException, InvalidArgumentException {
        if (sequenceNumber < 0) {
            throw new InvalidArgumentException("bad arg " + sequenceNumber);
        } else if (method == null) {
            throw new NullPointerException("null arg method");
        } else {
            CSeq cseq = new CSeq();
            cseq.setMethod(method);
            cseq.setSeqNumber(sequenceNumber);
            return cseq;
        }
    }

    public CSeqHeader createCSeqHeader(int sequenceNumber, String method) throws ParseException, InvalidArgumentException {
        return createCSeqHeader((long) sequenceNumber, method);
    }

    public CallIdHeader createCallIdHeader(String callId) throws ParseException {
        if (callId == null) {
            throw new NullPointerException("null arg callId");
        }
        CallID c = new CallID();
        c.setCallId(callId);
        return c;
    }

    public CallInfoHeader createCallInfoHeader(URI callInfo) {
        if (callInfo == null) {
            throw new NullPointerException("null arg callInfo");
        }
        CallInfo c = new CallInfo();
        c.setInfo(callInfo);
        return c;
    }

    public ContactHeader createContactHeader(Address address) {
        if (address == null) {
            throw new NullPointerException("null arg address");
        }
        Contact contact = new Contact();
        contact.setAddress(address);
        return contact;
    }

    public ContactHeader createContactHeader() {
        Contact contact = new Contact();
        contact.setWildCardFlag(true);
        contact.setExpires(0);
        return contact;
    }

    public ContentDispositionHeader createContentDispositionHeader(String contentDisposition) throws ParseException {
        if (contentDisposition == null) {
            throw new NullPointerException("null arg contentDisposition");
        }
        ContentDisposition c = new ContentDisposition();
        c.setDispositionType(contentDisposition);
        return c;
    }

    public ContentEncodingHeader createContentEncodingHeader(String encoding) throws ParseException {
        if (encoding == null) {
            throw new NullPointerException("null encoding");
        }
        ContentEncoding c = new ContentEncoding();
        c.setEncoding(encoding);
        return c;
    }

    public ContentLanguageHeader createContentLanguageHeader(Locale contentLanguage) {
        if (contentLanguage == null) {
            throw new NullPointerException("null arg contentLanguage");
        }
        ContentLanguage c = new ContentLanguage();
        c.setContentLanguage(contentLanguage);
        return c;
    }

    public ContentLengthHeader createContentLengthHeader(int contentLength) throws InvalidArgumentException {
        if (contentLength < 0) {
            throw new InvalidArgumentException("bad contentLength");
        }
        ContentLength c = new ContentLength();
        c.setContentLength(contentLength);
        return c;
    }

    public ContentTypeHeader createContentTypeHeader(String contentType, String contentSubType) throws ParseException {
        if (contentType == null || contentSubType == null) {
            throw new NullPointerException("null contentType or subType");
        }
        ContentType c = new ContentType();
        c.setContentType(contentType);
        c.setContentSubType(contentSubType);
        return c;
    }

    public DateHeader createDateHeader(Calendar date) {
        SIPDateHeader d = new SIPDateHeader();
        if (date == null) {
            throw new NullPointerException("null date");
        }
        d.setDate(date);
        return d;
    }

    public EventHeader createEventHeader(String eventType) throws ParseException {
        if (eventType == null) {
            throw new NullPointerException("null eventType");
        }
        Event event = new Event();
        event.setEventType(eventType);
        return event;
    }

    public ExpiresHeader createExpiresHeader(int expires) throws InvalidArgumentException {
        if (expires < 0) {
            throw new InvalidArgumentException("bad value " + expires);
        }
        Expires e = new Expires();
        e.setExpires(expires);
        return e;
    }

    public ExtensionHeader createExtensionHeader(String name, String value) throws ParseException {
        if (name == null) {
            throw new NullPointerException("bad name");
        }
        ExtensionHeaderImpl ext = new ExtensionHeaderImpl();
        ext.setName(name);
        ext.setValue(value);
        return ext;
    }

    public FromHeader createFromHeader(Address address, String tag) throws ParseException {
        if (address == null) {
            throw new NullPointerException("null address arg");
        }
        From from = new From();
        from.setAddress(address);
        if (tag != null) {
            from.setTag(tag);
        }
        return from;
    }

    public InReplyToHeader createInReplyToHeader(String callId) throws ParseException {
        if (callId == null) {
            throw new NullPointerException("null callId arg");
        }
        InReplyTo inReplyTo = new InReplyTo();
        inReplyTo.setCallId(callId);
        return inReplyTo;
    }

    public MaxForwardsHeader createMaxForwardsHeader(int maxForwards) throws InvalidArgumentException {
        if (maxForwards < 0 || maxForwards > 255) {
            throw new InvalidArgumentException("bad maxForwards arg " + maxForwards);
        }
        MaxForwards m = new MaxForwards();
        m.setMaxForwards(maxForwards);
        return m;
    }

    public MimeVersionHeader createMimeVersionHeader(int majorVersion, int minorVersion) throws InvalidArgumentException {
        if (majorVersion < 0 || minorVersion < 0) {
            throw new InvalidArgumentException("bad major/minor version");
        }
        MimeVersion m = new MimeVersion();
        m.setMajorVersion(majorVersion);
        m.setMinorVersion(minorVersion);
        return m;
    }

    public MinExpiresHeader createMinExpiresHeader(int minExpires) throws InvalidArgumentException {
        if (minExpires < 0) {
            throw new InvalidArgumentException("bad minExpires " + minExpires);
        }
        MinExpires min = new MinExpires();
        min.setExpires(minExpires);
        return min;
    }

    public ExtensionHeader createMinSEHeader(int expires) throws InvalidArgumentException {
        if (expires < 0) {
            throw new InvalidArgumentException("bad value " + expires);
        }
        MinSE e = new MinSE();
        e.setExpires(expires);
        return e;
    }

    public OrganizationHeader createOrganizationHeader(String organization) throws ParseException {
        if (organization == null) {
            throw new NullPointerException("bad organization arg");
        }
        Organization o = new Organization();
        o.setOrganization(organization);
        return o;
    }

    public PriorityHeader createPriorityHeader(String priority) throws ParseException {
        if (priority == null) {
            throw new NullPointerException("bad priority arg");
        }
        Priority p = new Priority();
        p.setPriority(priority);
        return p;
    }

    public ProxyAuthenticateHeader createProxyAuthenticateHeader(String scheme) throws ParseException {
        if (scheme == null) {
            throw new NullPointerException("bad scheme arg");
        }
        ProxyAuthenticate p = new ProxyAuthenticate();
        p.setScheme(scheme);
        return p;
    }

    public ProxyAuthorizationHeader createProxyAuthorizationHeader(String scheme) throws ParseException {
        if (scheme == null) {
            throw new NullPointerException("bad scheme arg");
        }
        ProxyAuthorization p = new ProxyAuthorization();
        p.setScheme(scheme);
        return p;
    }

    public ProxyRequireHeader createProxyRequireHeader(String optionTag) throws ParseException {
        if (optionTag == null) {
            throw new NullPointerException("bad optionTag arg");
        }
        ProxyRequire p = new ProxyRequire();
        p.setOptionTag(optionTag);
        return p;
    }

    public RAckHeader createRAckHeader(long rSeqNumber, long cSeqNumber, String method) throws InvalidArgumentException, ParseException {
        if (method == null) {
            throw new NullPointerException("Bad method");
        } else if (cSeqNumber < 0 || rSeqNumber < 0) {
            throw new InvalidArgumentException("bad cseq/rseq arg");
        } else {
            RAck rack = new RAck();
            rack.setMethod(method);
            rack.setCSequenceNumber(cSeqNumber);
            rack.setRSequenceNumber(rSeqNumber);
            return rack;
        }
    }

    public RAckHeader createRAckHeader(int rSeqNumber, int cSeqNumber, String method) throws InvalidArgumentException, ParseException {
        return createRAckHeader((long) rSeqNumber, (long) cSeqNumber, method);
    }

    public RSeqHeader createRSeqHeader(int sequenceNumber) throws InvalidArgumentException {
        return createRSeqHeader((long) sequenceNumber);
    }

    public RSeqHeader createRSeqHeader(long sequenceNumber) throws InvalidArgumentException {
        if (sequenceNumber < 0) {
            throw new InvalidArgumentException("invalid sequenceNumber arg " + sequenceNumber);
        }
        RSeq rseq = new RSeq();
        rseq.setSeqNumber(sequenceNumber);
        return rseq;
    }

    public ReasonHeader createReasonHeader(String protocol, int cause, String text) throws InvalidArgumentException, ParseException {
        if (protocol == null) {
            throw new NullPointerException("bad protocol arg");
        } else if (cause < 0) {
            throw new InvalidArgumentException("bad cause");
        } else {
            Reason reason = new Reason();
            reason.setProtocol(protocol);
            reason.setCause(cause);
            reason.setText(text);
            return reason;
        }
    }

    public RecordRouteHeader createRecordRouteHeader(Address address) {
        if (address == null) {
            throw new NullPointerException("Null argument!");
        }
        RecordRoute recordRoute = new RecordRoute();
        recordRoute.setAddress(address);
        return recordRoute;
    }

    public ReplyToHeader createReplyToHeader(Address address) {
        if (address == null) {
            throw new NullPointerException("null address");
        }
        ReplyTo replyTo = new ReplyTo();
        replyTo.setAddress(address);
        return replyTo;
    }

    public RequireHeader createRequireHeader(String optionTag) throws ParseException {
        if (optionTag == null) {
            throw new NullPointerException("null optionTag");
        }
        Require require = new Require();
        require.setOptionTag(optionTag);
        return require;
    }

    public RetryAfterHeader createRetryAfterHeader(int retryAfter) throws InvalidArgumentException {
        if (retryAfter < 0) {
            throw new InvalidArgumentException("bad retryAfter arg");
        }
        RetryAfter r = new RetryAfter();
        r.setRetryAfter(retryAfter);
        return r;
    }

    public RouteHeader createRouteHeader(Address address) {
        if (address == null) {
            throw new NullPointerException("null address arg");
        }
        Route route = new Route();
        route.setAddress(address);
        return route;
    }

    public ServerHeader createServerHeader(List product) throws ParseException {
        if (product == null) {
            throw new NullPointerException("null productList arg");
        }
        Server server = new Server();
        server.setProduct(product);
        return server;
    }

    public SubjectHeader createSubjectHeader(String subject) throws ParseException {
        if (subject == null) {
            throw new NullPointerException("null subject arg");
        }
        Subject s = new Subject();
        s.setSubject(subject);
        return s;
    }

    public SubscriptionStateHeader createSubscriptionStateHeader(String subscriptionState) throws ParseException {
        if (subscriptionState == null) {
            throw new NullPointerException("null subscriptionState arg");
        }
        SubscriptionState s = new SubscriptionState();
        s.setState(subscriptionState);
        return s;
    }

    public SupportedHeader createSupportedHeader(String optionTag) throws ParseException {
        if (optionTag == null) {
            throw new NullPointerException("null optionTag arg");
        }
        Supported supported = new Supported();
        supported.setOptionTag(optionTag);
        return supported;
    }

    public TimeStampHeader createTimeStampHeader(float timeStamp) throws InvalidArgumentException {
        if (timeStamp < 0.0f) {
            throw new IllegalArgumentException("illegal timeStamp");
        }
        TimeStamp t = new TimeStamp();
        t.setTimeStamp(timeStamp);
        return t;
    }

    public ToHeader createToHeader(Address address, String tag) throws ParseException {
        if (address == null) {
            throw new NullPointerException("null address");
        }
        To to = new To();
        to.setAddress(address);
        if (tag != null) {
            to.setTag(tag);
        }
        return to;
    }

    public UnsupportedHeader createUnsupportedHeader(String optionTag) throws ParseException {
        if (optionTag == null) {
            throw new NullPointerException(optionTag);
        }
        Unsupported unsupported = new Unsupported();
        unsupported.setOptionTag(optionTag);
        return unsupported;
    }

    public UserAgentHeader createUserAgentHeader(List product) throws ParseException {
        if (product == null) {
            throw new NullPointerException("null user agent");
        }
        UserAgent userAgent = new UserAgent();
        userAgent.setProduct(product);
        return userAgent;
    }

    public ViaHeader createViaHeader(String host, int port, String transport, String branch) throws ParseException, InvalidArgumentException {
        if (host == null || transport == null) {
            throw new NullPointerException("null arg");
        }
        Via via = new Via();
        if (branch != null) {
            via.setBranch(branch);
        }
        if (host.indexOf(58) >= 0 && host.indexOf(91) < 0) {
            if (this.stripAddressScopeZones) {
                int zoneStart = host.indexOf(37);
                if (zoneStart != -1) {
                    host = host.substring(0, zoneStart);
                }
            }
            host = '[' + host + ']';
        }
        via.setHost(host);
        via.setPort(port);
        via.setTransport(transport);
        return via;
    }

    public WWWAuthenticateHeader createWWWAuthenticateHeader(String scheme) throws ParseException {
        if (scheme == null) {
            throw new NullPointerException("null scheme");
        }
        WWWAuthenticate www = new WWWAuthenticate();
        www.setScheme(scheme);
        return www;
    }

    public WarningHeader createWarningHeader(String agent, int code, String comment) throws ParseException, InvalidArgumentException {
        if (agent == null) {
            throw new NullPointerException("null arg");
        }
        Warning warning = new Warning();
        warning.setAgent(agent);
        warning.setCode(code);
        warning.setText(comment);
        return warning;
    }

    public ErrorInfoHeader createErrorInfoHeader(URI errorInfo) {
        if (errorInfo != null) {
            return new ErrorInfo((GenericURI) errorInfo);
        }
        throw new NullPointerException("null arg");
    }

    public Header createHeader(String headerText) throws ParseException {
        SIPHeader sipHeader = new StringMsgParser().parseSIPHeader(headerText.trim());
        if (!(sipHeader instanceof SIPHeaderList)) {
            return sipHeader;
        }
        if (((SIPHeaderList) sipHeader).size() > 1) {
            throw new ParseException("Only singleton allowed " + headerText, 0);
        } else if (((SIPHeaderList) sipHeader).size() != 0) {
            return ((SIPHeaderList) sipHeader).getFirst();
        } else {
            try {
                return (Header) ((SIPHeaderList) sipHeader).getMyClass().newInstance();
            } catch (InstantiationException ex) {
                ex.printStackTrace();
                return null;
            } catch (IllegalAccessException ex2) {
                ex2.printStackTrace();
                return null;
            }
        }
    }

    public Header createHeader(String headerName, String headerValue) throws ParseException {
        if (headerName != null) {
            return createHeader(new StringBuffer().append(headerName).append(Separators.COLON).append(headerValue).toString());
        }
        throw new NullPointerException("header name is null");
    }

    public List createHeaders(String headers) throws ParseException {
        if (headers == null) {
            throw new NullPointerException("null arg!");
        }
        SIPHeader shdr = new StringMsgParser().parseSIPHeader(headers);
        if (shdr instanceof SIPHeaderList) {
            return (SIPHeaderList) shdr;
        }
        throw new ParseException("List of headers of this type is not allowed in a message", 0);
    }

    public ReferToHeader createReferToHeader(Address address) {
        if (address == null) {
            throw new NullPointerException("null address!");
        }
        ReferTo referTo = new ReferTo();
        referTo.setAddress(address);
        return referTo;
    }

    public ReferredByHeader createReferredByHeader(Address address) {
        if (address == null) {
            throw new NullPointerException("null address!");
        }
        ReferredBy referredBy = new ReferredBy();
        referredBy.setAddress(address);
        return referredBy;
    }

    public ReplacesHeader createReplacesHeader(String callId, String toTag, String fromTag) throws ParseException {
        Replaces replaces = new Replaces();
        replaces.setCallId(callId);
        replaces.setFromTag(fromTag);
        replaces.setToTag(toTag);
        return replaces;
    }

    public JoinHeader createJoinHeader(String callId, String toTag, String fromTag) throws ParseException {
        Join join = new Join();
        join.setCallId(callId);
        join.setFromTag(fromTag);
        join.setToTag(toTag);
        return join;
    }

    public SIPETagHeader createSIPETagHeader(String etag) throws ParseException {
        return new SIPETag(etag);
    }

    public SIPIfMatchHeader createSIPIfMatchHeader(String etag) throws ParseException {
        return new SIPIfMatch(etag);
    }

    public PAccessNetworkInfoHeader createPAccessNetworkInfoHeader() {
        return new PAccessNetworkInfo();
    }

    public PAssertedIdentityHeader createPAssertedIdentityHeader(Address address) throws NullPointerException, ParseException {
        if (address == null) {
            throw new NullPointerException("null address!");
        }
        PAssertedIdentity assertedIdentity = new PAssertedIdentity();
        assertedIdentity.setAddress(address);
        return assertedIdentity;
    }

    public PAssociatedURIHeader createPAssociatedURIHeader(Address assocURI) {
        if (assocURI == null) {
            throw new NullPointerException("null associatedURI!");
        }
        PAssociatedURI associatedURI = new PAssociatedURI();
        associatedURI.setAddress(assocURI);
        return associatedURI;
    }

    public PCalledPartyIDHeader createPCalledPartyIDHeader(Address address) {
        if (address == null) {
            throw new NullPointerException("null address!");
        }
        PCalledPartyID calledPartyID = new PCalledPartyID();
        calledPartyID.setAddress(address);
        return calledPartyID;
    }

    public PChargingFunctionAddressesHeader createPChargingFunctionAddressesHeader() {
        return new PChargingFunctionAddresses();
    }

    public PChargingVectorHeader createChargingVectorHeader(String icid) throws ParseException {
        if (icid == null) {
            throw new NullPointerException("null icid arg!");
        }
        PChargingVector chargingVector = new PChargingVector();
        chargingVector.setICID(icid);
        return chargingVector;
    }

    public PMediaAuthorizationHeader createPMediaAuthorizationHeader(String token) throws InvalidArgumentException, ParseException {
        if (token == null || token == "") {
            throw new InvalidArgumentException("The Media-Authorization-Token parameter is null or empty");
        }
        PMediaAuthorization mediaAuthorization = new PMediaAuthorization();
        mediaAuthorization.setMediaAuthorizationToken(token);
        return mediaAuthorization;
    }

    public PPreferredIdentityHeader createPPreferredIdentityHeader(Address address) {
        if (address == null) {
            throw new NullPointerException("null address!");
        }
        PPreferredIdentity preferredIdentity = new PPreferredIdentity();
        preferredIdentity.setAddress(address);
        return preferredIdentity;
    }

    public PVisitedNetworkIDHeader createPVisitedNetworkIDHeader() {
        return new PVisitedNetworkID();
    }

    public PathHeader createPathHeader(Address address) {
        if (address == null) {
            throw new NullPointerException("null address!");
        }
        Path path = new Path();
        path.setAddress(address);
        return path;
    }

    public PrivacyHeader createPrivacyHeader(String privacyType) {
        if (privacyType != null) {
            return new Privacy(privacyType);
        }
        throw new NullPointerException("null privacyType arg");
    }

    public ServiceRouteHeader createServiceRouteHeader(Address address) {
        if (address == null) {
            throw new NullPointerException("null address!");
        }
        ServiceRoute serviceRoute = new ServiceRoute();
        serviceRoute.setAddress(address);
        return serviceRoute;
    }

    public SecurityServerHeader createSecurityServerHeader() {
        return new SecurityServer();
    }

    public SecurityClientHeader createSecurityClientHeader() {
        return new SecurityClient();
    }

    public SecurityVerifyHeader createSecurityVerifyHeader() {
        return new SecurityVerify();
    }

    public PUserDatabaseHeader createPUserDatabaseHeader(String databaseName) {
        if (databaseName == null || databaseName.equals(Separators.SP)) {
            throw new NullPointerException("Database name is null");
        }
        PUserDatabase pUserDatabase = new PUserDatabase();
        pUserDatabase.setDatabaseName(databaseName);
        return pUserDatabase;
    }

    public PProfileKeyHeader createPProfileKeyHeader(Address address) {
        if (address == null) {
            throw new NullPointerException("Address is null");
        }
        PProfileKey pProfileKey = new PProfileKey();
        pProfileKey.setAddress(address);
        return pProfileKey;
    }

    public PServedUserHeader createPServedUserHeader(Address address) {
        if (address == null) {
            throw new NullPointerException("Address is null");
        }
        PServedUser psu = new PServedUser();
        psu.setAddress(address);
        return psu;
    }

    public PPreferredServiceHeader createPPreferredServiceHeader() {
        return new PPreferredService();
    }

    public PAssertedServiceHeader createPAssertedServiceHeader() {
        return new PAssertedService();
    }

    public SessionExpiresHeader createSessionExpiresHeader(int expires) throws InvalidArgumentException {
        if (expires < 0) {
            throw new InvalidArgumentException("bad value " + expires);
        }
        SessionExpires s = new SessionExpires();
        s.setExpires(expires);
        return s;
    }

    public SipRequestLine createRequestLine(String requestLine) throws ParseException {
        return new RequestLineParser(requestLine).parse();
    }

    public SipStatusLine createStatusLine(String statusLine) throws ParseException {
        return new StatusLineParser(statusLine).parse();
    }

    public ReferencesHeader createReferencesHeader(String callId, String rel) throws ParseException {
        ReferencesHeader retval = new References();
        retval.setCallId(callId);
        retval.setRel(rel);
        return retval;
    }

    public HeaderFactoryImpl() {
        this.stripAddressScopeZones = false;
        this.stripAddressScopeZones = Boolean.getBoolean("gov.nist.core.STRIP_ADDR_SCOPES");
    }
}
