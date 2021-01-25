package gov.nist.javax.sip.stack;

import gov.nist.core.InternalErrorHandler;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.ParameterNames;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.RequestLine;
import gov.nist.javax.sip.header.Route;
import gov.nist.javax.sip.header.RouteList;
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

    public DefaultRouter(SipStack sipStack2, String defaultRoute2) {
        this.sipStack = (SipStackImpl) sipStack2;
        if (defaultRoute2 != null) {
            try {
                this.defaultRoute = this.sipStack.getAddressResolver().resolveAddress(new HopImpl(defaultRoute2));
            } catch (IllegalArgumentException ex) {
                ((SIPTransactionStack) sipStack2).getStackLogger().logError("Invalid default route specification - need host:port/transport");
                throw ex;
            }
        }
    }

    @Override // javax.sip.address.Router
    public Hop getNextHop(Request request) throws SipException {
        SIPRequest sipRequest = (SIPRequest) request;
        RequestLine requestLine = sipRequest.getRequestLine();
        if (requestLine == null) {
            return this.defaultRoute;
        }
        URI requestURI = requestLine.getUri();
        if (requestURI != null) {
            RouteList routes = sipRequest.getRouteHeaders();
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
                    Hop hop = createHop(sipUri, request);
                    if (this.sipStack.isLoggingEnabled()) {
                        StackLogger stackLogger = this.sipStack.getStackLogger();
                        stackLogger.logDebug("NextHop based on Route:" + hop);
                    }
                    return hop;
                }
                throw new SipException("First Route not a SIP URI");
            } else if (requestURI.isSipURI() && ((SipURI) requestURI).getMAddrParam() != null) {
                Hop hop2 = createHop((SipURI) requestURI, request);
                if (this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger2 = this.sipStack.getStackLogger();
                    stackLogger2.logDebug("Using request URI maddr to route the request = " + hop2.toString());
                }
                return hop2;
            } else if (this.defaultRoute != null) {
                if (this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger3 = this.sipStack.getStackLogger();
                    stackLogger3.logDebug("Using outbound proxy to route the request = " + this.defaultRoute.toString());
                }
                return this.defaultRoute;
            } else if (requestURI.isSipURI()) {
                Hop hop3 = createHop((SipURI) requestURI, request);
                if (hop3 != null && this.sipStack.isLoggingEnabled()) {
                    StackLogger stackLogger4 = this.sipStack.getStackLogger();
                    stackLogger4.logDebug("Used request-URI for nextHop = " + hop3.toString());
                } else if (this.sipStack.isLoggingEnabled()) {
                    this.sipStack.getStackLogger().logDebug("returning null hop -- loop detected");
                }
                return hop3;
            } else {
                InternalErrorHandler.handleException("Unexpected non-sip URI", this.sipStack.getStackLogger());
                return null;
            }
        } else {
            throw new IllegalArgumentException("Bad message: Null requestURI");
        }
    }

    public void fixStrictRouting(SIPRequest req) {
        RouteList routes = req.getRouteHeaders();
        routes.removeFirst();
        AddressImpl addr = new AddressImpl();
        addr.setAddess(req.getRequestURI());
        routes.add((RouteList) new Route(addr));
        req.setRequestURI((SipUri) ((Route) routes.getFirst()).getAddress().getURI());
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("post: fixStrictRouting" + req);
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

    @Override // javax.sip.address.Router
    public Hop getOutboundProxy() {
        return this.defaultRoute;
    }

    @Override // javax.sip.address.Router
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
