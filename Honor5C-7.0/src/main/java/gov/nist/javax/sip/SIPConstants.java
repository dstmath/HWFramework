package gov.nist.javax.sip;

import gov.nist.javax.sip.address.ParameterNames;
import gov.nist.javax.sip.header.SIPHeaderNames;

public interface SIPConstants extends SIPHeaderNames, ParameterNames, gov.nist.javax.sip.header.ParameterNames {
    public static final String BRANCH_MAGIC_COOKIE = "z9hG4bK";
    public static final String BRANCH_MAGIC_COOKIE_LOWER_CASE = "z9hg4bk";
    public static final String BRANCH_MAGIC_COOKIE_UPPER_CASE = "Z9HG4BK";
    public static final int DEFAULT_PORT = 5060;
    public static final int DEFAULT_TLS_PORT = 5061;
    public static final String SIP_VERSION_STRING = "SIP/2.0";
}
