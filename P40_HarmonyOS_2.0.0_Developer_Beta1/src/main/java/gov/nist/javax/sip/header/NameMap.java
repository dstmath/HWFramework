package gov.nist.javax.sip.header;

import gov.nist.core.PackageNames;
import gov.nist.javax.sip.header.ims.PAccessNetworkInfo;
import gov.nist.javax.sip.header.ims.PAssertedIdentity;
import gov.nist.javax.sip.header.ims.PAssociatedURI;
import gov.nist.javax.sip.header.ims.PCalledPartyID;
import gov.nist.javax.sip.header.ims.PChargingFunctionAddresses;
import gov.nist.javax.sip.header.ims.PChargingVector;
import gov.nist.javax.sip.header.ims.PMediaAuthorization;
import gov.nist.javax.sip.header.ims.PPreferredIdentity;
import gov.nist.javax.sip.header.ims.PVisitedNetworkID;
import gov.nist.javax.sip.header.ims.Path;
import gov.nist.javax.sip.header.ims.Privacy;
import gov.nist.javax.sip.header.ims.ServiceRoute;
import java.util.Hashtable;

public class NameMap implements SIPHeaderNames, PackageNames {
    static Hashtable nameMap;

    static {
        initializeNameMap();
    }

    protected static void putNameMap(String headerName, String className) {
        nameMap.put(headerName.toLowerCase(), className);
    }

    public static Class getClassFromName(String headerName) {
        String className = (String) nameMap.get(headerName.toLowerCase());
        if (className == null) {
            return null;
        }
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static void addExtensionHeader(String headerName, String className) {
        nameMap.put(headerName.toLowerCase(), className);
    }

    private static void initializeNameMap() {
        nameMap = new Hashtable();
        putNameMap("Min-Expires", MinExpires.class.getName());
        putNameMap("Error-Info", ErrorInfo.class.getName());
        putNameMap("MIME-Version", MimeVersion.class.getName());
        putNameMap("In-Reply-To", InReplyTo.class.getName());
        putNameMap("Allow", Allow.class.getName());
        putNameMap("Content-Language", ContentLanguage.class.getName());
        putNameMap("Call-Info", CallInfo.class.getName());
        putNameMap("CSeq", CSeq.class.getName());
        putNameMap("Alert-Info", AlertInfo.class.getName());
        putNameMap("Accept-Encoding", AcceptEncoding.class.getName());
        putNameMap("Accept", Accept.class.getName());
        putNameMap("Accept-Language", AcceptLanguage.class.getName());
        putNameMap("Record-Route", RecordRoute.class.getName());
        putNameMap("Timestamp", TimeStamp.class.getName());
        putNameMap("To", To.class.getName());
        putNameMap("Via", Via.class.getName());
        putNameMap("From", From.class.getName());
        putNameMap("Call-ID", CallID.class.getName());
        putNameMap("Authorization", Authorization.class.getName());
        putNameMap("Proxy-Authenticate", ProxyAuthenticate.class.getName());
        putNameMap("Server", Server.class.getName());
        putNameMap("Unsupported", Unsupported.class.getName());
        putNameMap("Retry-After", RetryAfter.class.getName());
        putNameMap("Content-Type", ContentType.class.getName());
        putNameMap("Content-Encoding", ContentEncoding.class.getName());
        putNameMap("Content-Length", ContentLength.class.getName());
        putNameMap("Route", Route.class.getName());
        putNameMap("Contact", Contact.class.getName());
        putNameMap("WWW-Authenticate", WWWAuthenticate.class.getName());
        putNameMap("Max-Forwards", MaxForwards.class.getName());
        putNameMap("Organization", Organization.class.getName());
        putNameMap("Proxy-Authorization", ProxyAuthorization.class.getName());
        putNameMap("Proxy-Require", ProxyRequire.class.getName());
        putNameMap("Require", Require.class.getName());
        putNameMap("Content-Disposition", ContentDisposition.class.getName());
        putNameMap("Subject", Subject.class.getName());
        putNameMap("User-Agent", UserAgent.class.getName());
        putNameMap("Warning", Warning.class.getName());
        putNameMap("Priority", Priority.class.getName());
        putNameMap("Date", SIPDateHeader.class.getName());
        putNameMap("Expires", Expires.class.getName());
        putNameMap("Supported", Supported.class.getName());
        putNameMap("Reply-To", ReplyTo.class.getName());
        putNameMap("Subscription-State", SubscriptionState.class.getName());
        putNameMap("Event", Event.class.getName());
        putNameMap("Allow-Events", AllowEvents.class.getName());
        putNameMap("Referred-By", "ReferredBy");
        putNameMap("Session-Expires", "SessionExpires");
        putNameMap("Min-SE", "MinSE");
        putNameMap("Replaces", "Replaces");
        putNameMap("Join", "Join");
        putNameMap("P-Access-Network-Info", PAccessNetworkInfo.class.getName());
        putNameMap("P-Asserted-Identity", PAssertedIdentity.class.getName());
        putNameMap("P-Associated-URI", PAssociatedURI.class.getName());
        putNameMap("P-Called-Party-ID", PCalledPartyID.class.getName());
        putNameMap("P-Charging-Function-Addresses", PChargingFunctionAddresses.class.getName());
        putNameMap("P-Charging-Vector", PChargingVector.class.getName());
        putNameMap("P-Media-Authorization", PMediaAuthorization.class.getName());
        putNameMap("Path", Path.class.getName());
        putNameMap("P-Preferred-Identity", PPreferredIdentity.class.getName());
        putNameMap("Privacy", Privacy.class.getName());
        putNameMap("Service-Route", ServiceRoute.class.getName());
        putNameMap("P-Visited-Network-ID", PVisitedNetworkID.class.getName());
    }
}
