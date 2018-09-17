package gov.nist.javax.sip;

import gov.nist.core.net.AddressResolver;
import gov.nist.javax.sip.clientauthutils.AccountManager;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.clientauthutils.SecureAccountManager;
import gov.nist.javax.sip.header.extensions.JoinHeader;
import gov.nist.javax.sip.header.extensions.ReplacesHeader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Collection;
import javax.sip.Dialog;
import javax.sip.SipStack;
import javax.sip.header.HeaderFactory;

public interface SipStackExt extends SipStack {
    AuthenticationHelper getAuthenticationHelper(AccountManager accountManager, HeaderFactory headerFactory);

    Collection<Dialog> getDialogs();

    Dialog getJoinDialog(JoinHeader joinHeader);

    Dialog getReplacesDialog(ReplacesHeader replacesHeader);

    AuthenticationHelper getSecureAuthenticationHelper(SecureAccountManager secureAccountManager, HeaderFactory headerFactory);

    SocketAddress obtainLocalAddress(InetAddress inetAddress, int i, InetAddress inetAddress2, int i2) throws IOException;

    void setAddressResolver(AddressResolver addressResolver);

    void setEnabledCipherSuites(String[] strArr);
}
