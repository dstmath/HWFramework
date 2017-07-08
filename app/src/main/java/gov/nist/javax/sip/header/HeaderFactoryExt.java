package gov.nist.javax.sip.header;

import gov.nist.javax.sip.header.extensions.JoinHeader;
import gov.nist.javax.sip.header.extensions.ReferredByHeader;
import gov.nist.javax.sip.header.extensions.ReplacesHeader;
import gov.nist.javax.sip.header.extensions.SessionExpiresHeader;
import gov.nist.javax.sip.header.ims.PAccessNetworkInfoHeader;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import gov.nist.javax.sip.header.ims.PAssertedServiceHeader;
import gov.nist.javax.sip.header.ims.PAssociatedURIHeader;
import gov.nist.javax.sip.header.ims.PCalledPartyIDHeader;
import gov.nist.javax.sip.header.ims.PChargingFunctionAddressesHeader;
import gov.nist.javax.sip.header.ims.PChargingVectorHeader;
import gov.nist.javax.sip.header.ims.PMediaAuthorizationHeader;
import gov.nist.javax.sip.header.ims.PPreferredIdentityHeader;
import gov.nist.javax.sip.header.ims.PPreferredServiceHeader;
import gov.nist.javax.sip.header.ims.PProfileKeyHeader;
import gov.nist.javax.sip.header.ims.PServedUserHeader;
import gov.nist.javax.sip.header.ims.PUserDatabaseHeader;
import gov.nist.javax.sip.header.ims.PVisitedNetworkIDHeader;
import gov.nist.javax.sip.header.ims.PathHeader;
import gov.nist.javax.sip.header.ims.PrivacyHeader;
import gov.nist.javax.sip.header.ims.SecurityClientHeader;
import gov.nist.javax.sip.header.ims.SecurityServerHeader;
import gov.nist.javax.sip.header.ims.SecurityVerifyHeader;
import gov.nist.javax.sip.header.ims.ServiceRouteHeader;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.address.Address;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;

public interface HeaderFactoryExt extends HeaderFactory {
    PChargingVectorHeader createChargingVectorHeader(String str) throws ParseException;

    Header createHeader(String str) throws ParseException;

    JoinHeader createJoinHeader(String str, String str2, String str3) throws ParseException;

    PAccessNetworkInfoHeader createPAccessNetworkInfoHeader();

    PAssertedIdentityHeader createPAssertedIdentityHeader(Address address) throws NullPointerException, ParseException;

    PAssertedServiceHeader createPAssertedServiceHeader();

    PAssociatedURIHeader createPAssociatedURIHeader(Address address);

    PCalledPartyIDHeader createPCalledPartyIDHeader(Address address);

    PChargingFunctionAddressesHeader createPChargingFunctionAddressesHeader();

    PMediaAuthorizationHeader createPMediaAuthorizationHeader(String str) throws InvalidArgumentException, ParseException;

    PPreferredIdentityHeader createPPreferredIdentityHeader(Address address);

    PPreferredServiceHeader createPPreferredServiceHeader();

    PProfileKeyHeader createPProfileKeyHeader(Address address);

    PServedUserHeader createPServedUserHeader(Address address);

    PUserDatabaseHeader createPUserDatabaseHeader(String str);

    PVisitedNetworkIDHeader createPVisitedNetworkIDHeader();

    PathHeader createPathHeader(Address address);

    PrivacyHeader createPrivacyHeader(String str);

    ReferredByHeader createReferredByHeader(Address address);

    ReplacesHeader createReplacesHeader(String str, String str2, String str3) throws ParseException;

    SipRequestLine createRequestLine(String str) throws ParseException;

    SecurityClientHeader createSecurityClientHeader();

    SecurityServerHeader createSecurityServerHeader();

    SecurityVerifyHeader createSecurityVerifyHeader();

    ServiceRouteHeader createServiceRouteHeader(Address address);

    SessionExpiresHeader createSessionExpiresHeader(int i) throws InvalidArgumentException;

    SipStatusLine createStatusLine(String str) throws ParseException;
}
