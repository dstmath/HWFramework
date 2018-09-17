package gov.nist.javax.sip.stack;

import gov.nist.core.InternalErrorHandler;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.ParameterNames;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.RequestLine;
import gov.nist.javax.sip.header.Route;
import gov.nist.javax.sip.header.RouteList;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.message.SIPRequest;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.sip.SipException;
import javax.sip.SipStack;
import javax.sip.address.Hop;
import javax.sip.address.Router;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

public class DefaultRouter implements Router {
    private Hop defaultRoute;
    private SipStackImpl sipStack;

    private DefaultRouter() {
    }

    public DefaultRouter(SipStack sipStack, String defaultRoute) {
        this.sipStack = (SipStackImpl) sipStack;
        if (defaultRoute != null) {
            try {
                this.defaultRoute = this.sipStack.getAddressResolver().resolveAddress(new HopImpl(defaultRoute));
            } catch (IllegalArgumentException ex) {
                ((SIPTransactionStack) sipStack).getStackLogger().logError("Invalid default route specification - need host:port/transport");
                throw ex;
            }
        }
    }

    public Hop getNextHop(Request request) throws SipException {
        SIPRequest sipRequest = (SIPRequest) request;
        RequestLine requestLine = sipRequest.getRequestLine();
        if (requestLine == null) {
            return this.defaultRoute;
        }
        URI requestURI = requestLine.getUri();
        if (requestURI == null) {
            throw new IllegalArgumentException("Bad message: Null requestURI");
        }
        RouteList routes = sipRequest.getRouteHeaders();
        Hop hop;
        if (routes != null) {
            URI uri = ((Route) routes.getFirst()).getAddress().getURI();
            if (uri.isSipURI()) {
                SipURI sipUri = (SipURI) uri;
                if (!sipUri.hasLrParam()) {
                    fixStrictRouting(sipRequest);
                    if (this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logDebug("Route post processing fixed strict routing");
                    }
                }
                hop = createHop(sipUri, request);
                if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("NextHop based on Route:" + hop);
                }
                return hop;
            }
            throw new SipException("First Route not a SIP URI");
        } else if (requestURI.isSipURI() && ((SipURI) requestURI).getMAddrParam() != null) {
            hop = createHop((SipURI) requestURI, request);
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Using request URI maddr to route the request = " + hop.toString());
            }
            return hop;
        } else if (this.defaultRoute != null) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Using outbound proxy to route the request = " + this.defaultRoute.toString());
            }
            return this.defaultRoute;
        } else if (requestURI.isSipURI()) {
            hop = createHop((SipURI) requestURI, request);
            if (hop != null && this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Used request-URI for nextHop = " + hop.toString());
            } else if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("returning null hop -- loop detected");
            }
            return hop;
        } else {
            InternalErrorHandler.handleException("Unexpected non-sip URI", this.sipStack.getStackLogger());
            return null;
        }
    }

    public void fixStrictRouting(SIPRequest req) {
        RouteList routes = req.getRouteHeaders();
        SipUri firstUri = (SipUri) ((Route) routes.getFirst()).getAddress().getURI();
        routes.removeFirst();
        AddressImpl addr = new AddressImpl();
        addr.setAddess(req.getRequestURI());
        routes.add((SIPHeader) new Route(addr));
        req.setRequestURI(firstUri);
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("post: fixStrictRouting" + req);
        }
    }

    private final Hop createHop(SipURI sipUri, Request request) {
        int port;
        String host;
        String transport = sipUri.isSecure() ? ParameterNames.TLS : sipUri.getTransportParam();
        if (transport == null) {
            transport = ((ViaHeader) request.getHeader("Via")).getTransport();
        }
        if (sipUri.getPort() != -1) {
            port = sipUri.getPort();
        } else if (transport.equalsIgnoreCase(ParameterNames.TLS)) {
            port = 5061;
        } else {
            port = 5060;
        }
        if (sipUri.getMAddrParam() != null) {
            host = sipUri.getMAddrParam();
        } else {
            host = sipUri.getHost();
        }
        return this.sipStack.getAddressResolver().resolveAddress(new HopImpl(host, port, transport));
    }

    public Hop getOutboundProxy() {
        return this.defaultRoute;
    }

    public ListIterator getNextHops(Request request) {
        try {
            LinkedList llist = new LinkedList();
            llist.add(getNextHop(request));
            return llist.listIterator();
        } catch (SipException e) {
            return null;
        }
    }
}
