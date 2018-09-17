package gov.nist.javax.sip.clientauthutils;

import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.stack.SIPClientTransaction;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Timer;
import javax.sip.ClientTransaction;
import javax.sip.DialogState;
import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.Hop;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class AuthenticationHelperImpl implements AuthenticationHelper {
    private Object accountManager = null;
    private CredentialsCache cachedCredentials;
    private HeaderFactory headerFactory;
    private SipStackImpl sipStack;
    Timer timer;

    public AuthenticationHelperImpl(SipStackImpl sipStack, AccountManager accountManager, HeaderFactory headerFactory) {
        this.accountManager = accountManager;
        this.headerFactory = headerFactory;
        this.sipStack = sipStack;
        this.cachedCredentials = new CredentialsCache(sipStack.getTimer());
    }

    public AuthenticationHelperImpl(SipStackImpl sipStack, SecureAccountManager accountManager, HeaderFactory headerFactory) {
        this.accountManager = accountManager;
        this.headerFactory = headerFactory;
        this.sipStack = sipStack;
        this.cachedCredentials = new CredentialsCache(sipStack.getTimer());
    }

    public ClientTransaction handleChallenge(Response challenge, ClientTransaction challengedTransaction, SipProvider transactionCreator, int cacheTime) throws SipException, NullPointerException {
        CSeqHeader cSeq;
        try {
            Request reoriginatedRequest;
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("handleChallenge: " + challenge);
            }
            SIPRequest challengedRequest = (SIPRequest) challengedTransaction.getRequest();
            if (challengedRequest.getToTag() == null && challengedTransaction.getDialog() != null) {
                if (challengedTransaction.getDialog().getState() == DialogState.CONFIRMED) {
                    reoriginatedRequest = challengedTransaction.getDialog().createRequest(challengedRequest.getMethod());
                    Iterator<String> headerNames = challengedRequest.getHeaderNames();
                    while (headerNames.hasNext()) {
                        String headerName = (String) headerNames.next();
                        if (reoriginatedRequest.getHeader(headerName) != null) {
                            ListIterator<Header> iterator = reoriginatedRequest.getHeaders(headerName);
                            while (iterator.hasNext()) {
                                reoriginatedRequest.addHeader((Header) iterator.next());
                            }
                        }
                    }
                    removeBranchID(reoriginatedRequest);
                    if (challenge != null || reoriginatedRequest == null) {
                        throw new NullPointerException("A null argument was passed to handle challenge.");
                    }
                    ListIterator authHeaders;
                    if (challenge.getStatusCode() == Response.UNAUTHORIZED) {
                        authHeaders = challenge.getHeaders("WWW-Authenticate");
                    } else if (challenge.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED) {
                        authHeaders = challenge.getHeaders("Proxy-Authenticate");
                    } else {
                        throw new IllegalArgumentException("Unexpected status code ");
                    }
                    if (authHeaders == null) {
                        throw new IllegalArgumentException("Could not find WWWAuthenticate or ProxyAuthenticate headers");
                    }
                    reoriginatedRequest.removeHeader("Authorization");
                    reoriginatedRequest.removeHeader("Proxy-Authorization");
                    cSeq = (CSeqHeader) reoriginatedRequest.getHeader("CSeq");
                    cSeq.setSeqNumber(cSeq.getSeqNumber() + 1);
                    if (challengedRequest.getRouteHeaders() == null) {
                        Hop hop = ((SIPClientTransaction) challengedTransaction).getNextHop();
                        SipURI sipUri = (SipURI) reoriginatedRequest.getRequestURI();
                        if (!hop.getHost().equalsIgnoreCase(sipUri.getHost())) {
                            if ((hop.equals(this.sipStack.getRouter(challengedRequest).getOutboundProxy()) ^ 1) != 0) {
                                sipUri.setMAddrParam(hop.getHost());
                            }
                        }
                        if (hop.getPort() != -1) {
                            sipUri.setPort(hop.getPort());
                        }
                    }
                    ClientTransaction retryTran = transactionCreator.getNewClientTransaction(reoriginatedRequest);
                    SipURI requestUri = (SipURI) challengedTransaction.getRequest().getRequestURI();
                    while (authHeaders.hasNext()) {
                        String sipDomain;
                        AuthorizationHeader authorization;
                        WWWAuthenticateHeader authHeader = (WWWAuthenticateHeader) authHeaders.next();
                        String realm = authHeader.getRealm();
                        if (this.accountManager instanceof SecureAccountManager) {
                            UserCredentialHash credHash = ((SecureAccountManager) this.accountManager).getCredentialHash(challengedTransaction, realm);
                            URI uri = reoriginatedRequest.getRequestURI();
                            sipDomain = credHash.getSipDomain();
                            authorization = getAuthorization(reoriginatedRequest.getMethod(), uri.toString(), reoriginatedRequest.getContent() == null ? "" : new String(reoriginatedRequest.getRawContent()), authHeader, credHash);
                        } else {
                            UserCredentials userCreds = ((AccountManager) this.accountManager).getCredentials(challengedTransaction, realm);
                            sipDomain = userCreds.getSipDomain();
                            if (userCreds == null) {
                                throw new SipException("Cannot find user creds for the given user name and realm");
                            }
                            authorization = getAuthorization(reoriginatedRequest.getMethod(), reoriginatedRequest.getRequestURI().toString(), reoriginatedRequest.getContent() == null ? "" : new String(reoriginatedRequest.getRawContent()), authHeader, userCreds);
                        }
                        if (this.sipStack.isLoggingEnabled()) {
                            this.sipStack.getStackLogger().logDebug("Created authorization header: " + authorization.toString());
                        }
                        if (cacheTime != 0) {
                            this.cachedCredentials.cacheAuthorizationHeader(sipDomain, authorization, cacheTime);
                        }
                        reoriginatedRequest.addHeader(authorization);
                    }
                    if (this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logDebug("Returning authorization transaction." + retryTran);
                    }
                    return retryTran;
                }
            }
            reoriginatedRequest = (Request) challengedRequest.clone();
            removeBranchID(reoriginatedRequest);
            if (challenge != null) {
            }
            throw new NullPointerException("A null argument was passed to handle challenge.");
        } catch (InvalidArgumentException e) {
            throw new SipException("Invalid CSeq -- could not increment : " + cSeq.getSeqNumber());
        } catch (SipException ex) {
            throw ex;
        } catch (Throwable ex2) {
            this.sipStack.getStackLogger().logError("Unexpected exception ", ex2);
            throw new SipException("Unexpected exception ", ex2);
        }
    }

    private AuthorizationHeader getAuthorization(String method, String uri, String requestBody, WWWAuthenticateHeader authHeader, UserCredentials userCredentials) {
        String qop = authHeader.getQop() != null ? "auth" : null;
        String nc_value = "00000001";
        String cnonce = "xyz";
        String response = MessageDigestAlgorithm.calculateResponse(authHeader.getAlgorithm(), userCredentials.getUserName(), authHeader.getRealm(), userCredentials.getPassword(), authHeader.getNonce(), nc_value, cnonce, method, uri, requestBody, qop, this.sipStack.getStackLogger());
        try {
            AuthorizationHeader authorization;
            if (authHeader instanceof ProxyAuthenticateHeader) {
                authorization = this.headerFactory.createProxyAuthorizationHeader(authHeader.getScheme());
            } else {
                authorization = this.headerFactory.createAuthorizationHeader(authHeader.getScheme());
            }
            authorization.setUsername(userCredentials.getUserName());
            authorization.setRealm(authHeader.getRealm());
            authorization.setNonce(authHeader.getNonce());
            authorization.setParameter("uri", uri);
            authorization.setResponse(response);
            if (authHeader.getAlgorithm() != null) {
                authorization.setAlgorithm(authHeader.getAlgorithm());
            }
            if (authHeader.getOpaque() != null) {
                authorization.setOpaque(authHeader.getOpaque());
            }
            if (qop != null) {
                authorization.setQop(qop);
                authorization.setCNonce(cnonce);
                authorization.setNonceCount(Integer.parseInt(nc_value));
            }
            authorization.setResponse(response);
            return authorization;
        } catch (ParseException e) {
            throw new RuntimeException("Failed to create an authorization header!");
        }
    }

    private AuthorizationHeader getAuthorization(String method, String uri, String requestBody, WWWAuthenticateHeader authHeader, UserCredentialHash userCredentials) {
        String qop = authHeader.getQop() != null ? "auth" : null;
        String nc_value = "00000001";
        String cnonce = "xyz";
        String response = MessageDigestAlgorithm.calculateResponse(authHeader.getAlgorithm(), userCredentials.getHashUserDomainPassword(), authHeader.getNonce(), nc_value, cnonce, method, uri, requestBody, qop, this.sipStack.getStackLogger());
        try {
            AuthorizationHeader authorization;
            if (authHeader instanceof ProxyAuthenticateHeader) {
                authorization = this.headerFactory.createProxyAuthorizationHeader(authHeader.getScheme());
            } else {
                authorization = this.headerFactory.createAuthorizationHeader(authHeader.getScheme());
            }
            authorization.setUsername(userCredentials.getUserName());
            authorization.setRealm(authHeader.getRealm());
            authorization.setNonce(authHeader.getNonce());
            authorization.setParameter("uri", uri);
            authorization.setResponse(response);
            if (authHeader.getAlgorithm() != null) {
                authorization.setAlgorithm(authHeader.getAlgorithm());
            }
            if (authHeader.getOpaque() != null) {
                authorization.setOpaque(authHeader.getOpaque());
            }
            if (qop != null) {
                authorization.setQop(qop);
                authorization.setCNonce(cnonce);
                authorization.setNonceCount(Integer.parseInt(nc_value));
            }
            authorization.setResponse(response);
            return authorization;
        } catch (ParseException e) {
            throw new RuntimeException("Failed to create an authorization header!");
        }
    }

    private void removeBranchID(Request request) {
        ((ViaHeader) request.getHeader("Via")).removeParameter("branch");
    }

    public void setAuthenticationHeaders(Request request) {
        String callId = ((SIPRequest) request).getCallId().getCallId();
        request.removeHeader("Authorization");
        Collection<AuthorizationHeader> authHeaders = this.cachedCredentials.getCachedAuthorizationHeaders(callId);
        if (authHeaders == null) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Could not find authentication headers for " + callId);
            }
            return;
        }
        for (AuthorizationHeader authHeader : authHeaders) {
            request.addHeader(authHeader);
        }
    }

    public void removeCachedAuthenticationHeaders(String callId) {
        if (callId == null) {
            throw new NullPointerException("Null callId argument ");
        }
        this.cachedCredentials.removeAuthenticationHeader(callId);
    }
}
