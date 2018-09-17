package gov.nist.javax.sip.parser;

import gov.nist.core.InternalErrorHandler;
import gov.nist.javax.sip.header.SIPHeaderNamesCache;
import gov.nist.javax.sip.header.extensions.ReferencesHeader;
import gov.nist.javax.sip.parser.extensions.JoinParser;
import gov.nist.javax.sip.parser.extensions.MinSEParser;
import gov.nist.javax.sip.parser.extensions.ReferencesParser;
import gov.nist.javax.sip.parser.extensions.ReferredByParser;
import gov.nist.javax.sip.parser.extensions.ReplacesParser;
import gov.nist.javax.sip.parser.extensions.SessionExpiresParser;
import gov.nist.javax.sip.parser.ims.PAccessNetworkInfoParser;
import gov.nist.javax.sip.parser.ims.PAssertedIdentityParser;
import gov.nist.javax.sip.parser.ims.PAssociatedURIParser;
import gov.nist.javax.sip.parser.ims.PCalledPartyIDParser;
import gov.nist.javax.sip.parser.ims.PChargingFunctionAddressesParser;
import gov.nist.javax.sip.parser.ims.PChargingVectorParser;
import gov.nist.javax.sip.parser.ims.PMediaAuthorizationParser;
import gov.nist.javax.sip.parser.ims.PPreferredIdentityParser;
import gov.nist.javax.sip.parser.ims.PVisitedNetworkIDParser;
import gov.nist.javax.sip.parser.ims.PathParser;
import gov.nist.javax.sip.parser.ims.PrivacyParser;
import gov.nist.javax.sip.parser.ims.SecurityClientParser;
import gov.nist.javax.sip.parser.ims.SecurityServerParser;
import gov.nist.javax.sip.parser.ims.SecurityVerifyParser;
import gov.nist.javax.sip.parser.ims.ServiceRouteParser;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.Hashtable;
import javax.sip.header.ReferToHeader;

public class ParserFactory {
    private static Class[] constructorArgs = new Class[1];
    private static Hashtable parserConstructorCache = new Hashtable();
    private static Hashtable<String, Class<? extends HeaderParser>> parserTable = new Hashtable();

    static {
        constructorArgs[0] = String.class;
        parserTable.put("Reply-To".toLowerCase(), ReplyToParser.class);
        parserTable.put("In-Reply-To".toLowerCase(), InReplyToParser.class);
        parserTable.put("Accept-Encoding".toLowerCase(), AcceptEncodingParser.class);
        parserTable.put("Accept-Language".toLowerCase(), AcceptLanguageParser.class);
        parserTable.put("t", ToParser.class);
        parserTable.put("To".toLowerCase(), ToParser.class);
        parserTable.put("From".toLowerCase(), FromParser.class);
        parserTable.put("f", FromParser.class);
        parserTable.put("CSeq".toLowerCase(), CSeqParser.class);
        parserTable.put("Via".toLowerCase(), ViaParser.class);
        parserTable.put("v", ViaParser.class);
        parserTable.put("Contact".toLowerCase(), ContactParser.class);
        parserTable.put("m", ContactParser.class);
        parserTable.put("Content-Type".toLowerCase(), ContentTypeParser.class);
        parserTable.put("c", ContentTypeParser.class);
        parserTable.put("Content-Length".toLowerCase(), ContentLengthParser.class);
        parserTable.put("l", ContentLengthParser.class);
        parserTable.put("Authorization".toLowerCase(), AuthorizationParser.class);
        parserTable.put("WWW-Authenticate".toLowerCase(), WWWAuthenticateParser.class);
        parserTable.put("Call-ID".toLowerCase(), CallIDParser.class);
        parserTable.put("i", CallIDParser.class);
        parserTable.put("Route".toLowerCase(), RouteParser.class);
        parserTable.put("Record-Route".toLowerCase(), RecordRouteParser.class);
        parserTable.put("Date".toLowerCase(), DateParser.class);
        parserTable.put("Proxy-Authorization".toLowerCase(), ProxyAuthorizationParser.class);
        parserTable.put("Proxy-Authenticate".toLowerCase(), ProxyAuthenticateParser.class);
        parserTable.put("Retry-After".toLowerCase(), RetryAfterParser.class);
        parserTable.put("Require".toLowerCase(), RequireParser.class);
        parserTable.put("Proxy-Require".toLowerCase(), ProxyRequireParser.class);
        parserTable.put("Timestamp".toLowerCase(), TimeStampParser.class);
        parserTable.put("Unsupported".toLowerCase(), UnsupportedParser.class);
        parserTable.put("User-Agent".toLowerCase(), UserAgentParser.class);
        parserTable.put("Supported".toLowerCase(), SupportedParser.class);
        parserTable.put("k", SupportedParser.class);
        parserTable.put("Server".toLowerCase(), ServerParser.class);
        parserTable.put("Subject".toLowerCase(), SubjectParser.class);
        parserTable.put("s", SubjectParser.class);
        parserTable.put("Subscription-State".toLowerCase(), SubscriptionStateParser.class);
        parserTable.put("Max-Forwards".toLowerCase(), MaxForwardsParser.class);
        parserTable.put("MIME-Version".toLowerCase(), MimeVersionParser.class);
        parserTable.put("Min-Expires".toLowerCase(), MinExpiresParser.class);
        parserTable.put("Organization".toLowerCase(), OrganizationParser.class);
        parserTable.put("Priority".toLowerCase(), PriorityParser.class);
        parserTable.put("RAck".toLowerCase(), RAckParser.class);
        parserTable.put("RSeq".toLowerCase(), RSeqParser.class);
        parserTable.put("Reason".toLowerCase(), ReasonParser.class);
        parserTable.put("Warning".toLowerCase(), WarningParser.class);
        parserTable.put("Expires".toLowerCase(), ExpiresParser.class);
        parserTable.put("Event".toLowerCase(), EventParser.class);
        parserTable.put("o", EventParser.class);
        parserTable.put("Error-Info".toLowerCase(), ErrorInfoParser.class);
        parserTable.put("Content-Language".toLowerCase(), ContentLanguageParser.class);
        parserTable.put("Content-Encoding".toLowerCase(), ContentEncodingParser.class);
        parserTable.put("e", ContentEncodingParser.class);
        parserTable.put("Content-Disposition".toLowerCase(), ContentDispositionParser.class);
        parserTable.put("Call-Info".toLowerCase(), CallInfoParser.class);
        parserTable.put("Authentication-Info".toLowerCase(), AuthenticationInfoParser.class);
        parserTable.put("Allow".toLowerCase(), AllowParser.class);
        parserTable.put("Allow-Events".toLowerCase(), AllowEventsParser.class);
        parserTable.put("u", AllowEventsParser.class);
        parserTable.put("Alert-Info".toLowerCase(), AlertInfoParser.class);
        parserTable.put("Accept".toLowerCase(), AcceptParser.class);
        parserTable.put(ReferToHeader.NAME.toLowerCase(), ReferToParser.class);
        parserTable.put("r", ReferToParser.class);
        parserTable.put("SIP-ETag".toLowerCase(), SIPETagParser.class);
        parserTable.put("SIP-If-Match".toLowerCase(), SIPIfMatchParser.class);
        parserTable.put("P-Access-Network-Info".toLowerCase(), PAccessNetworkInfoParser.class);
        parserTable.put("P-Asserted-Identity".toLowerCase(), PAssertedIdentityParser.class);
        parserTable.put("P-Preferred-Identity".toLowerCase(), PPreferredIdentityParser.class);
        parserTable.put("P-Charging-Vector".toLowerCase(), PChargingVectorParser.class);
        parserTable.put("P-Charging-Function-Addresses".toLowerCase(), PChargingFunctionAddressesParser.class);
        parserTable.put("P-Media-Authorization".toLowerCase(), PMediaAuthorizationParser.class);
        parserTable.put("Path".toLowerCase(), PathParser.class);
        parserTable.put("Privacy".toLowerCase(), PrivacyParser.class);
        parserTable.put("Service-Route".toLowerCase(), ServiceRouteParser.class);
        parserTable.put("P-Visited-Network-ID".toLowerCase(), PVisitedNetworkIDParser.class);
        parserTable.put("P-Associated-URI".toLowerCase(), PAssociatedURIParser.class);
        parserTable.put("P-Called-Party-ID".toLowerCase(), PCalledPartyIDParser.class);
        parserTable.put("Security-Server".toLowerCase(), SecurityServerParser.class);
        parserTable.put("Security-Client".toLowerCase(), SecurityClientParser.class);
        parserTable.put("Security-Verify".toLowerCase(), SecurityVerifyParser.class);
        parserTable.put("Referred-By".toLowerCase(), ReferredByParser.class);
        parserTable.put("b", ReferToParser.class);
        parserTable.put("Session-Expires".toLowerCase(), SessionExpiresParser.class);
        parserTable.put("x", SessionExpiresParser.class);
        parserTable.put("Min-SE".toLowerCase(), MinSEParser.class);
        parserTable.put("Replaces".toLowerCase(), ReplacesParser.class);
        parserTable.put("Join".toLowerCase(), JoinParser.class);
        parserTable.put(ReferencesHeader.NAME.toLowerCase(), ReferencesParser.class);
    }

    public static HeaderParser createParser(String line) throws ParseException {
        String headerName = Lexer.getHeaderName(line);
        String headerValue = Lexer.getHeaderValue(line);
        if (headerName == null || headerValue == null) {
            throw new ParseException("The header name or value is null", 0);
        }
        Class parserClass = (Class) parserTable.get(SIPHeaderNamesCache.toLowerCase(headerName));
        if (parserClass == null) {
            return new HeaderParser(line);
        }
        try {
            Constructor cons = (Constructor) parserConstructorCache.get(parserClass);
            if (cons == null) {
                cons = parserClass.getConstructor(constructorArgs);
                parserConstructorCache.put(parserClass, cons);
            }
            return (HeaderParser) cons.newInstance(new Object[]{line});
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
            return null;
        }
    }
}
