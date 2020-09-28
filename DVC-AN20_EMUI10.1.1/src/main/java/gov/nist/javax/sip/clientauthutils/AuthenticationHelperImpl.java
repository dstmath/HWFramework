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

    @Override // gov.nist.javax.sip.clientauthutils.AuthenticationHelper
    public ClientTransaction handleChallenge(Response challenge, ClientTransaction challengedTransaction, SipProvider transactionCreator, int cacheTime) throws SipException, NullPointerException {
        Request reoriginatedRequest;
        ListIterator authHeaders;
        CSeqHeader cSeq;
        String sipDomain;
        Request reoriginatedRequest2;
        SIPRequest challengedRequest;
        AuthorizationHeader authorization;
        String str;
        try {
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug("handleChallenge: " + challenge);
            }
            SIPRequest challengedRequest2 = (SIPRequest) challengedTransaction.getRequest();
            if (challengedRequest2.getToTag() == null && challengedTransaction.getDialog() != null) {
                if (challengedTransaction.getDialog().getState() == DialogState.CONFIRMED) {
                    Request reoriginatedRequest3 = challengedTransaction.getDialog().createRequest(challengedRequest2.getMethod());
                    Iterator<String> headerNames = challengedRequest2.getHeaderNames();
                    while (headerNames.hasNext()) {
                        String headerName = headerNames.next();
                        if (reoriginatedRequest3.getHeader(headerName) != null) {
                            ListIterator<Header> iterator = reoriginatedRequest3.getHeaders(headerName);
                            while (iterator.hasNext()) {
                                reoriginatedRequest3.addHeader(iterator.next());
                            }
                        }
                    }
                    reoriginatedRequest = reoriginatedRequest3;
                    removeBranchID(reoriginatedRequest);
                    if (challenge != null || reoriginatedRequest == null) {
                        throw new NullPointerException("A null argument was passed to handle challenge.");
                    }
                    if (challenge.getStatusCode() == 401) {
                        authHeaders = challenge.getHeaders("WWW-Authenticate");
                    } else if (challenge.getStatusCode() == 407) {
                        authHeaders = challenge.getHeaders("Proxy-Authenticate");
                    } else {
                        throw new IllegalArgumentException("Unexpected status code ");
                    }
                    if (authHeaders != null) {
                        reoriginatedRequest.removeHeader("Authorization");
                        reoriginatedRequest.removeHeader("Proxy-Authorization");
                        CSeqHeader cSeq2 = (CSeqHeader) reoriginatedRequest.getHeader("CSeq");
                        try {
                            cSeq2.setSeqNumber(cSeq2.getSeqNumber() + 1);
                            if (challengedRequest2.getRouteHeaders() == null) {
                                Hop hop = ((SIPClientTransaction) challengedTransaction).getNextHop();
                                SipURI sipUri = (SipURI) reoriginatedRequest.getRequestURI();
                                if (!hop.getHost().equalsIgnoreCase(sipUri.getHost()) && !hop.equals(this.sipStack.getRouter(challengedRequest2).getOutboundProxy())) {
                                    sipUri.setMAddrParam(hop.getHost());
                                }
                                if (hop.getPort() != -1) {
                                    sipUri.setPort(hop.getPort());
                                }
                            }
                            ClientTransaction retryTran = transactionCreator.getNewClientTransaction(reoriginatedRequest);
                            SipURI sipURI = (SipURI) challengedTransaction.getRequest().getRequestURI();
                            while (authHeaders.hasNext()) {
                                WWWAuthenticateHeader authHeader = (WWWAuthenticateHeader) authHeaders.next();
                                String realm = authHeader.getRealm();
                                String str2 = "";
                                if (this.accountManager instanceof SecureAccountManager) {
                                    UserCredentialHash credHash = ((SecureAccountManager) this.accountManager).getCredentialHash(challengedTransaction, realm);
                                    URI uri = reoriginatedRequest.getRequestURI();
                                    String sipDomain2 = credHash.getSipDomain();
                                    String method = reoriginatedRequest.getMethod();
                                    String uri2 = uri.toString();
                                    if (reoriginatedRequest.getContent() == null) {
                                        str = str2;
                                    } else {
                                        str = new String(reoriginatedRequest.getRawContent());
                                    }
                                    authorization = getAuthorization(method, uri2, str, authHeader, credHash);
                                    cSeq = cSeq2;
                                    reoriginatedRequest2 = reoriginatedRequest;
                                    challengedRequest = challengedRequest2;
                                    sipDomain = sipDomain2;
                                } else {
                                    UserCredentials userCreds = ((AccountManager) this.accountManager).getCredentials(challengedTransaction, realm);
                                    String sipDomain3 = userCreds.getSipDomain();
                                    String method2 = reoriginatedRequest.getMethod();
                                    String uri3 = reoriginatedRequest.getRequestURI().toString();
                                    if (reoriginatedRequest.getContent() != null) {
                                        str2 = new String(reoriginatedRequest.getRawContent());
                                    }
                                    cSeq = cSeq2;
                                    reoriginatedRequest2 = reoriginatedRequest;
                                    challengedRequest = challengedRequest2;
                                    authorization = getAuthorization(method2, uri3, str2, authHeader, userCreds);
                                    sipDomain = sipDomain3;
                                }
                                if (this.sipStack.isLoggingEnabled()) {
                                    StackLogger stackLogger2 = this.sipStack.getStackLogger();
                                    stackLogger2.logDebug("Created authorization header: " + authorization.toString());
                                }
                                if (cacheTime != 0) {
                                    this.cachedCredentials.cacheAuthorizationHeader(sipDomain, authorization, cacheTime);
                                }
                                reoriginatedRequest2.addHeader(authorization);
                                challengedRequest2 = challengedRequest;
                                reoriginatedRequest = reoriginatedRequest2;
                                cSeq2 = cSeq;
                            }
                            if (this.sipStack.isLoggingEnabled()) {
                                StackLogger stackLogger3 = this.sipStack.getStackLogger();
                                stackLogger3.logDebug("Returning authorization transaction." + retryTran);
                            }
                            return retryTran;
                        } catch (InvalidArgumentException e) {
                            throw new SipException("Invalid CSeq -- could not increment : " + cSeq2.getSeqNumber());
                        }
                    } else {
                        throw new IllegalArgumentException("Could not find WWWAuthenticate or ProxyAuthenticate headers");
                    }
                }
            }
            reoriginatedRequest = (Request) challengedRequest2.clone();
            removeBranchID(reoriginatedRequest);
            if (challenge != null) {
            }
            throw new NullPointerException("A null argument was passed to handle challenge.");
        } catch (SipException ex) {
            throw ex;
        } catch (Exception ex2) {
            this.sipStack.getStackLogger().logError("Unexpected exception ", ex2);
            throw new SipException("Unexpected exception ", ex2);
        }
    }

    private AuthorizationHeader getAuthorization(String method, String uri, String requestBody, WWWAuthenticateHeader authHeader, UserCredentials userCredentials) {
        AuthorizationHeader authorization;
        String qop = authHeader.getQop() != null ? "auth" : null;
        String response = MessageDigestAlgorithm.calculateResponse(authHeader.getAlgorithm(), userCredentials.getUserName(), authHeader.getRealm(), userCredentials.getPassword(), authHeader.getNonce(), "00000001", "xyz", method, uri, requestBody, qop, this.sipStack.getStackLogger());
        try {
            if (authHeader instanceof ProxyAuthenticateHeader) {
                authorization = this.headerFactory.createProxyAuthorizationHeader(authHeader.getScheme());
            } else {
                authorization = this.headerFactory.createAuthorizationHeader(authHeader.getScheme());
            }
            authorization.setUsername(userCredentials.getUserName());
            authorization.setRealm(authHeader.getRealm());
            authorization.setNonce(authHeader.getNonce());
            try {
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
                    authorization.setCNonce("xyz");
                    authorization.setNonceCount(Integer.parseInt("00000001"));
                }
                authorization.setResponse(response);
                return authorization;
            } catch (ParseException e) {
                throw new RuntimeException("Failed to create an authorization header!");
            }
        } catch (ParseException e2) {
            throw new RuntimeException("Failed to create an authorization header!");
        }
    }

    private AuthorizationHeader getAuthorization(String method, String uri, String requestBody, WWWAuthenticateHeader authHeader, UserCredentialHash userCredentials) {
        AuthorizationHeader authorization;
        String qop = authHeader.getQop() != null ? "auth" : null;
        String response = MessageDigestAlgorithm.calculateResponse(authHeader.getAlgorithm(), userCredentials.getHashUserDomainPassword(), authHeader.getNonce(), "00000001", "xyz", method, uri, requestBody, qop, this.sipStack.getStackLogger());
        try {
            if (authHeader instanceof ProxyAuthenticateHeader) {
                authorization = this.headerFactory.createProxyAuthorizationHeader(authHeader.getScheme());
            } else {
                authorization = this.headerFactory.createAuthorizationHeader(authHeader.getScheme());
            }
            authorization.setUsername(userCredentials.getUserName());
            authorization.setRealm(authHeader.getRealm());
            authorization.setNonce(authHeader.getNonce());
            try {
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
                    authorization.setCNonce("xyz");
                    authorization.setNonceCount(Integer.parseInt("00000001"));
                }
                authorization.setResponse(response);
                return authorization;
            } catch (ParseException e) {
                throw new RuntimeException("Failed to create an authorization header!");
            }
        } catch (ParseException e2) {
            throw new RuntimeException("Failed to create an authorization header!");
        }
    }

    private void removeBranchID(Request request) {
        ((ViaHeader) request.getHeader("Via")).removeParameter("branch");
    }

    @Override // gov.nist.javax.sip.clientauthutils.AuthenticationHelper
    public void setAuthenticationHeaders(Request request) {
        String callId = ((SIPRequest) request).getCallId().getCallId();
        request.removeHeader("Authorization");
        Collection<AuthorizationHeader> authHeaders = this.cachedCredentials.getCachedAuthorizationHeaders(callId);
        if (authHeaders != null) {
            for (AuthorizationHeader authHeader : authHeaders) {
                request.addHeader(authHeader);
            }
        } else if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("Could not find authentication headers for " + callId);
        }
    }

    @Override // gov.nist.javax.sip.clientauthutils.AuthenticationHelper
    public void removeCachedAuthenticationHeaders(String callId) {
        if (callId != null) {
            this.cachedCredentials.removeAuthenticationHeader(callId);
            return;
        }
        throw new NullPointerException("Null callId argument ");
    }
}
