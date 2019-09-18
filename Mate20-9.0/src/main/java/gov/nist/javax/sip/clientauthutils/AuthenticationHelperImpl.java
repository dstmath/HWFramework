package gov.nist.javax.sip.clientauthutils;

import gov.nist.core.StackLogger;
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

    public AuthenticationHelperImpl(SipStackImpl sipStack2, AccountManager accountManager2, HeaderFactory headerFactory2) {
        this.accountManager = accountManager2;
        this.headerFactory = headerFactory2;
        this.sipStack = sipStack2;
        this.cachedCredentials = new CredentialsCache(sipStack2.getTimer());
    }

    public AuthenticationHelperImpl(SipStackImpl sipStack2, SecureAccountManager accountManager2, HeaderFactory headerFactory2) {
        this.accountManager = accountManager2;
        this.headerFactory = headerFactory2;
        this.sipStack = sipStack2;
        this.cachedCredentials = new CredentialsCache(sipStack2.getTimer());
    }

    public ClientTransaction handleChallenge(Response challenge, ClientTransaction challengedTransaction, SipProvider transactionCreator, int cacheTime) throws SipException, NullPointerException {
        Request reoriginatedRequest;
        ListIterator authHeaders;
        WWWAuthenticateHeader authHeader;
        String sipDomain;
        String str;
        AuthorizationHeader authorization;
        String str2;
        Response response = challenge;
        ClientTransaction clientTransaction = challengedTransaction;
        int i = cacheTime;
        try {
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug("handleChallenge: " + response);
            }
            SIPRequest challengedRequest = (SIPRequest) challengedTransaction.getRequest();
            if (challengedRequest.getToTag() == null && challengedTransaction.getDialog() != null) {
                if (challengedTransaction.getDialog().getState() == DialogState.CONFIRMED) {
                    reoriginatedRequest = challengedTransaction.getDialog().createRequest(challengedRequest.getMethod());
                    Iterator<String> headerNames = challengedRequest.getHeaderNames();
                    while (headerNames.hasNext()) {
                        String headerName = headerNames.next();
                        if (reoriginatedRequest.getHeader(headerName) != null) {
                            ListIterator<Header> iterator = reoriginatedRequest.getHeaders(headerName);
                            while (iterator.hasNext()) {
                                reoriginatedRequest.addHeader(iterator.next());
                            }
                        }
                    }
                    Request reoriginatedRequest2 = reoriginatedRequest;
                    removeBranchID(reoriginatedRequest2);
                    if (response != null || reoriginatedRequest2 == null) {
                        SipProvider sipProvider = transactionCreator;
                        throw new NullPointerException("A null argument was passed to handle challenge.");
                    }
                    if (challenge.getStatusCode() == 401) {
                        authHeaders = response.getHeaders("WWW-Authenticate");
                    } else if (challenge.getStatusCode() == 407) {
                        authHeaders = response.getHeaders("Proxy-Authenticate");
                    } else {
                        SipProvider sipProvider2 = transactionCreator;
                        throw new IllegalArgumentException("Unexpected status code ");
                    }
                    ListIterator authHeaders2 = authHeaders;
                    if (authHeaders2 != null) {
                        reoriginatedRequest2.removeHeader("Authorization");
                        reoriginatedRequest2.removeHeader("Proxy-Authorization");
                        CSeqHeader cSeq = (CSeqHeader) reoriginatedRequest2.getHeader("CSeq");
                        try {
                            cSeq.setSeqNumber(cSeq.getSeqNumber() + 1);
                            if (challengedRequest.getRouteHeaders() == null) {
                                Hop hop = ((SIPClientTransaction) clientTransaction).getNextHop();
                                SipURI sipUri = (SipURI) reoriginatedRequest2.getRequestURI();
                                if (!hop.getHost().equalsIgnoreCase(sipUri.getHost()) && !hop.equals(this.sipStack.getRouter(challengedRequest).getOutboundProxy())) {
                                    sipUri.setMAddrParam(hop.getHost());
                                }
                                if (hop.getPort() != -1) {
                                    sipUri.setPort(hop.getPort());
                                }
                            }
                            ClientTransaction retryTran = transactionCreator.getNewClientTransaction(reoriginatedRequest2);
                            SipURI requestUri = (SipURI) challengedTransaction.getRequest().getRequestURI();
                            while (true) {
                                SipURI requestUri2 = requestUri;
                                if (authHeaders2.hasNext()) {
                                    WWWAuthenticateHeader authHeader2 = (WWWAuthenticateHeader) authHeaders2.next();
                                    String realm = authHeader2.getRealm();
                                    if (this.accountManager instanceof SecureAccountManager) {
                                        UserCredentialHash credHash = ((SecureAccountManager) this.accountManager).getCredentialHash(clientTransaction, realm);
                                        URI uri = reoriginatedRequest2.getRequestURI();
                                        sipDomain = credHash.getSipDomain();
                                        String method = reoriginatedRequest2.getMethod();
                                        String uri2 = uri.toString();
                                        if (reoriginatedRequest2.getContent() == null) {
                                            str2 = "";
                                            URI uri3 = uri;
                                        } else {
                                            URI uri4 = uri;
                                            str2 = new String(reoriginatedRequest2.getRawContent());
                                        }
                                        String str3 = realm;
                                        authHeader = authHeader2;
                                        authorization = getAuthorization(method, uri2, str2, authHeader2, credHash);
                                    } else {
                                        authHeader = authHeader2;
                                        UserCredentials userCreds = ((AccountManager) this.accountManager).getCredentials(clientTransaction, realm);
                                        sipDomain = userCreds.getSipDomain();
                                        if (userCreds != null) {
                                            String method2 = reoriginatedRequest2.getMethod();
                                            String uri5 = reoriginatedRequest2.getRequestURI().toString();
                                            if (reoriginatedRequest2.getContent() == null) {
                                                str = "";
                                            } else {
                                                str = new String(reoriginatedRequest2.getRawContent());
                                            }
                                            UserCredentials userCredentials = userCreds;
                                            authorization = getAuthorization(method2, uri5, str, authHeader, userCreds);
                                        } else {
                                            UserCredentials userCredentials2 = userCreds;
                                            throw new SipException("Cannot find user creds for the given user name and realm");
                                        }
                                    }
                                    String sipDomain2 = sipDomain;
                                    if (this.sipStack.isLoggingEnabled()) {
                                        StackLogger stackLogger2 = this.sipStack.getStackLogger();
                                        stackLogger2.logDebug("Created authorization header: " + authorization.toString());
                                    }
                                    if (i != 0) {
                                        this.cachedCredentials.cacheAuthorizationHeader(sipDomain2, authorization, i);
                                    }
                                    reoriginatedRequest2.addHeader(authorization);
                                    requestUri = requestUri2;
                                    WWWAuthenticateHeader wWWAuthenticateHeader = authHeader;
                                    Response response2 = challenge;
                                } else {
                                    if (this.sipStack.isLoggingEnabled()) {
                                        StackLogger stackLogger3 = this.sipStack.getStackLogger();
                                        stackLogger3.logDebug("Returning authorization transaction." + retryTran);
                                    }
                                    return retryTran;
                                }
                            }
                        } catch (InvalidArgumentException e) {
                            SipProvider sipProvider3 = transactionCreator;
                            throw new SipException("Invalid CSeq -- could not increment : " + cSeq.getSeqNumber());
                        } catch (SipException e2) {
                            ex = e2;
                            throw ex;
                        } catch (Exception e3) {
                            ex = e3;
                            this.sipStack.getStackLogger().logError("Unexpected exception ", ex);
                            throw new SipException("Unexpected exception ", ex);
                        }
                    } else {
                        SipProvider sipProvider4 = transactionCreator;
                        throw new IllegalArgumentException("Could not find WWWAuthenticate or ProxyAuthenticate headers");
                    }
                }
            }
            reoriginatedRequest = (Request) challengedRequest.clone();
            Request reoriginatedRequest22 = reoriginatedRequest;
            removeBranchID(reoriginatedRequest22);
            if (response != null) {
            }
            SipProvider sipProvider5 = transactionCreator;
            throw new NullPointerException("A null argument was passed to handle challenge.");
        } catch (SipException e4) {
            ex = e4;
            SipProvider sipProvider6 = transactionCreator;
            throw ex;
        } catch (Exception e5) {
            ex = e5;
            SipProvider sipProvider7 = transactionCreator;
            this.sipStack.getStackLogger().logError("Unexpected exception ", ex);
            throw new SipException("Unexpected exception ", ex);
        }
    }

    private AuthorizationHeader getAuthorization(String method, String uri, String requestBody, WWWAuthenticateHeader authHeader, UserCredentials userCredentials) {
        AuthorizationHeader authorization;
        String qop = authHeader.getQop() != null ? "auth" : null;
        String cnonce = "xyz";
        String nc_value = "00000001";
        String response = MessageDigestAlgorithm.calculateResponse(authHeader.getAlgorithm(), userCredentials.getUserName(), authHeader.getRealm(), userCredentials.getPassword(), authHeader.getNonce(), "00000001", "xyz", method, uri, requestBody, qop, this.sipStack.getStackLogger());
        try {
            if (authHeader instanceof ProxyAuthenticateHeader) {
                try {
                    authorization = this.headerFactory.createProxyAuthorizationHeader(authHeader.getScheme());
                } catch (ParseException e) {
                    String str = uri;
                    throw new RuntimeException("Failed to create an authorization header!");
                }
            } else {
                authorization = this.headerFactory.createAuthorizationHeader(authHeader.getScheme());
            }
            AuthorizationHeader authorization2 = authorization;
            authorization2.setUsername(userCredentials.getUserName());
            authorization2.setRealm(authHeader.getRealm());
            authorization2.setNonce(authHeader.getNonce());
            try {
                authorization2.setParameter("uri", uri);
                authorization2.setResponse(response);
                if (authHeader.getAlgorithm() != null) {
                    try {
                        authorization2.setAlgorithm(authHeader.getAlgorithm());
                    } catch (ParseException e2) {
                    }
                }
                if (authHeader.getOpaque() != null) {
                    authorization2.setOpaque(authHeader.getOpaque());
                }
                if (qop != null) {
                    authorization2.setQop(qop);
                    try {
                        authorization2.setCNonce(cnonce);
                    } catch (ParseException e3) {
                        String str2 = nc_value;
                        throw new RuntimeException("Failed to create an authorization header!");
                    }
                    try {
                        authorization2.setNonceCount(Integer.parseInt(nc_value));
                    } catch (ParseException e4) {
                        throw new RuntimeException("Failed to create an authorization header!");
                    }
                } else {
                    String str3 = nc_value;
                }
                authorization2.setResponse(response);
                return authorization2;
            } catch (ParseException e5) {
                String str4 = cnonce;
                String str5 = nc_value;
                throw new RuntimeException("Failed to create an authorization header!");
            }
        } catch (ParseException e6) {
            String str6 = uri;
            String str42 = cnonce;
            String str52 = nc_value;
            throw new RuntimeException("Failed to create an authorization header!");
        }
    }

    private AuthorizationHeader getAuthorization(String method, String uri, String requestBody, WWWAuthenticateHeader authHeader, UserCredentialHash userCredentials) {
        AuthorizationHeader authorization;
        String qop = authHeader.getQop() != null ? "auth" : null;
        String cnonce = "xyz";
        String response = MessageDigestAlgorithm.calculateResponse(authHeader.getAlgorithm(), userCredentials.getHashUserDomainPassword(), authHeader.getNonce(), "00000001", "xyz", method, uri, requestBody, qop, this.sipStack.getStackLogger());
        try {
            if (authHeader instanceof ProxyAuthenticateHeader) {
                try {
                    authorization = this.headerFactory.createProxyAuthorizationHeader(authHeader.getScheme());
                } catch (ParseException e) {
                    String str = uri;
                    throw new RuntimeException("Failed to create an authorization header!");
                }
            } else {
                authorization = this.headerFactory.createAuthorizationHeader(authHeader.getScheme());
            }
            AuthorizationHeader authorization2 = authorization;
            authorization2.setUsername(userCredentials.getUserName());
            authorization2.setRealm(authHeader.getRealm());
            authorization2.setNonce(authHeader.getNonce());
            try {
                authorization2.setParameter("uri", uri);
                authorization2.setResponse(response);
                if (authHeader.getAlgorithm() != null) {
                    try {
                        authorization2.setAlgorithm(authHeader.getAlgorithm());
                    } catch (ParseException e2) {
                    }
                }
                if (authHeader.getOpaque() != null) {
                    authorization2.setOpaque(authHeader.getOpaque());
                }
                if (qop != null) {
                    authorization2.setQop(qop);
                    try {
                        authorization2.setCNonce(cnonce);
                        authorization2.setNonceCount(Integer.parseInt("00000001"));
                    } catch (ParseException e3) {
                        throw new RuntimeException("Failed to create an authorization header!");
                    }
                }
                authorization2.setResponse(response);
                return authorization2;
            } catch (ParseException e4) {
                String str2 = cnonce;
                throw new RuntimeException("Failed to create an authorization header!");
            }
        } catch (ParseException e5) {
            String str3 = uri;
            String str22 = cnonce;
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
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug("Could not find authentication headers for " + callId);
            }
            return;
        }
        for (AuthorizationHeader authHeader : authHeaders) {
            request.addHeader(authHeader);
        }
    }

    public void removeCachedAuthenticationHeaders(String callId) {
        if (callId != null) {
            this.cachedCredentials.removeAuthenticationHeader(callId);
            return;
        }
        throw new NullPointerException("Null callId argument ");
    }
}
