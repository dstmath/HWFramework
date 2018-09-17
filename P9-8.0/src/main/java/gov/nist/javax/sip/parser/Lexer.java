package gov.nist.javax.sip.parser;

import gov.nist.core.LexerCore;
import gov.nist.core.Separators;
import gov.nist.javax.sip.header.extensions.ReferencesHeader;
import java.util.Hashtable;
import javax.sip.header.ReferToHeader;

public class Lexer extends LexerCore {
    public static String getHeaderName(String line) {
        if (line == null) {
            return null;
        }
        try {
            int begin = line.indexOf(Separators.COLON);
            String headerName = null;
            if (begin >= 1) {
                headerName = line.substring(0, begin).trim();
            }
            return headerName;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public Lexer(String lexerName, String buffer) {
        super(lexerName, buffer);
        selectLexer(lexerName);
    }

    public static String getHeaderValue(String line) {
        if (line == null) {
            return null;
        }
        try {
            return line.substring(line.indexOf(Separators.COLON) + 1);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void selectLexer(String lexerName) {
        synchronized (lexerTables) {
            this.currentLexer = (Hashtable) lexerTables.get(lexerName);
            this.currentLexerName = lexerName;
            if (this.currentLexer == null) {
                addLexer(lexerName);
                if (lexerName.equals("method_keywordLexer")) {
                    addKeyword("REGISTER", TokenTypes.REGISTER);
                    addKeyword("ACK", TokenTypes.ACK);
                    addKeyword("OPTIONS", TokenTypes.OPTIONS);
                    addKeyword("BYE", TokenTypes.BYE);
                    addKeyword("INVITE", TokenTypes.INVITE);
                    addKeyword("sip".toUpperCase(), TokenTypes.SIP);
                    addKeyword("sips".toUpperCase(), TokenTypes.SIPS);
                    addKeyword("SUBSCRIBE", TokenTypes.SUBSCRIBE);
                    addKeyword("NOTIFY", TokenTypes.NOTIFY);
                    addKeyword("MESSAGE", TokenTypes.MESSAGE);
                    addKeyword("PUBLISH", TokenTypes.PUBLISH);
                } else if (lexerName.equals("command_keywordLexer")) {
                    addKeyword("Error-Info".toUpperCase(), TokenTypes.ERROR_INFO);
                    addKeyword("Allow-Events".toUpperCase(), TokenTypes.ALLOW_EVENTS);
                    addKeyword("Authentication-Info".toUpperCase(), TokenTypes.AUTHENTICATION_INFO);
                    addKeyword("Event".toUpperCase(), TokenTypes.EVENT);
                    addKeyword("Min-Expires".toUpperCase(), TokenTypes.MIN_EXPIRES);
                    addKeyword("RSeq".toUpperCase(), TokenTypes.RSEQ);
                    addKeyword("RAck".toUpperCase(), TokenTypes.RACK);
                    addKeyword("Reason".toUpperCase(), TokenTypes.REASON);
                    addKeyword("Reply-To".toUpperCase(), TokenTypes.REPLY_TO);
                    addKeyword("Subscription-State".toUpperCase(), TokenTypes.SUBSCRIPTION_STATE);
                    addKeyword("Timestamp".toUpperCase(), TokenTypes.TIMESTAMP);
                    addKeyword("In-Reply-To".toUpperCase(), TokenTypes.IN_REPLY_TO);
                    addKeyword("MIME-Version".toUpperCase(), TokenTypes.MIME_VERSION);
                    addKeyword("Alert-Info".toUpperCase(), TokenTypes.ALERT_INFO);
                    addKeyword("From".toUpperCase(), TokenTypes.FROM);
                    addKeyword("To".toUpperCase(), TokenTypes.TO);
                    addKeyword(ReferToHeader.NAME.toUpperCase(), TokenTypes.REFER_TO);
                    addKeyword("Via".toUpperCase(), TokenTypes.VIA);
                    addKeyword("User-Agent".toUpperCase(), TokenTypes.USER_AGENT);
                    addKeyword("Server".toUpperCase(), TokenTypes.SERVER);
                    addKeyword("Accept-Encoding".toUpperCase(), TokenTypes.ACCEPT_ENCODING);
                    addKeyword("Accept".toUpperCase(), 2068);
                    addKeyword("Allow".toUpperCase(), TokenTypes.ALLOW);
                    addKeyword("Route".toUpperCase(), TokenTypes.ROUTE);
                    addKeyword("Authorization".toUpperCase(), TokenTypes.AUTHORIZATION);
                    addKeyword("Proxy-Authorization".toUpperCase(), TokenTypes.PROXY_AUTHORIZATION);
                    addKeyword("Retry-After".toUpperCase(), TokenTypes.RETRY_AFTER);
                    addKeyword("Proxy-Require".toUpperCase(), TokenTypes.PROXY_REQUIRE);
                    addKeyword("Content-Language".toUpperCase(), TokenTypes.CONTENT_LANGUAGE);
                    addKeyword("Unsupported".toUpperCase(), TokenTypes.UNSUPPORTED);
                    addKeyword("Supported".toUpperCase(), 2068);
                    addKeyword("Warning".toUpperCase(), TokenTypes.WARNING);
                    addKeyword("Max-Forwards".toUpperCase(), TokenTypes.MAX_FORWARDS);
                    addKeyword("Date".toUpperCase(), TokenTypes.DATE);
                    addKeyword("Priority".toUpperCase(), TokenTypes.PRIORITY);
                    addKeyword("Proxy-Authenticate".toUpperCase(), TokenTypes.PROXY_AUTHENTICATE);
                    addKeyword("Content-Encoding".toUpperCase(), TokenTypes.CONTENT_ENCODING);
                    addKeyword("Content-Length".toUpperCase(), TokenTypes.CONTENT_LENGTH);
                    addKeyword("Subject".toUpperCase(), TokenTypes.SUBJECT);
                    addKeyword("Content-Type".toUpperCase(), TokenTypes.CONTENT_TYPE);
                    addKeyword("Contact".toUpperCase(), TokenTypes.CONTACT);
                    addKeyword("Call-ID".toUpperCase(), TokenTypes.CALL_ID);
                    addKeyword("Require".toUpperCase(), TokenTypes.REQUIRE);
                    addKeyword("Expires".toUpperCase(), TokenTypes.EXPIRES);
                    addKeyword("Record-Route".toUpperCase(), TokenTypes.RECORD_ROUTE);
                    addKeyword("Organization".toUpperCase(), TokenTypes.ORGANIZATION);
                    addKeyword("CSeq".toUpperCase(), TokenTypes.CSEQ);
                    addKeyword("Accept-Language".toUpperCase(), TokenTypes.ACCEPT_LANGUAGE);
                    addKeyword("WWW-Authenticate".toUpperCase(), TokenTypes.WWW_AUTHENTICATE);
                    addKeyword("Call-Info".toUpperCase(), TokenTypes.CALL_INFO);
                    addKeyword("Content-Disposition".toUpperCase(), TokenTypes.CONTENT_DISPOSITION);
                    addKeyword(TokenNames.K.toUpperCase(), 2068);
                    addKeyword(TokenNames.C.toUpperCase(), TokenTypes.CONTENT_TYPE);
                    addKeyword(TokenNames.E.toUpperCase(), TokenTypes.CONTENT_ENCODING);
                    addKeyword(TokenNames.F.toUpperCase(), TokenTypes.FROM);
                    addKeyword(TokenNames.I.toUpperCase(), TokenTypes.CALL_ID);
                    addKeyword(TokenNames.M.toUpperCase(), TokenTypes.CONTACT);
                    addKeyword(TokenNames.L.toUpperCase(), TokenTypes.CONTENT_LENGTH);
                    addKeyword(TokenNames.S.toUpperCase(), TokenTypes.SUBJECT);
                    addKeyword(TokenNames.T.toUpperCase(), TokenTypes.TO);
                    addKeyword(TokenNames.U.toUpperCase(), TokenTypes.ALLOW_EVENTS);
                    addKeyword(TokenNames.V.toUpperCase(), TokenTypes.VIA);
                    addKeyword(TokenNames.R.toUpperCase(), TokenTypes.REFER_TO);
                    addKeyword(TokenNames.O.toUpperCase(), TokenTypes.EVENT);
                    addKeyword(TokenNames.X.toUpperCase(), TokenTypes.SESSIONEXPIRES_TO);
                    addKeyword("SIP-ETag".toUpperCase(), TokenTypes.SIP_ETAG);
                    addKeyword("SIP-If-Match".toUpperCase(), TokenTypes.SIP_IF_MATCH);
                    addKeyword("Session-Expires".toUpperCase(), TokenTypes.SESSIONEXPIRES_TO);
                    addKeyword("Min-SE".toUpperCase(), TokenTypes.MINSE_TO);
                    addKeyword("Referred-By".toUpperCase(), TokenTypes.REFERREDBY_TO);
                    addKeyword("Replaces".toUpperCase(), TokenTypes.REPLACES_TO);
                    addKeyword("Join".toUpperCase(), TokenTypes.JOIN_TO);
                    addKeyword("Path".toUpperCase(), TokenTypes.PATH);
                    addKeyword("Service-Route".toUpperCase(), TokenTypes.SERVICE_ROUTE);
                    addKeyword("P-Asserted-Identity".toUpperCase(), TokenTypes.P_ASSERTED_IDENTITY);
                    addKeyword("P-Preferred-Identity".toUpperCase(), TokenTypes.P_PREFERRED_IDENTITY);
                    addKeyword("Privacy".toUpperCase(), TokenTypes.PRIVACY);
                    addKeyword("P-Called-Party-ID".toUpperCase(), TokenTypes.P_CALLED_PARTY_ID);
                    addKeyword("P-Associated-URI".toUpperCase(), TokenTypes.P_ASSOCIATED_URI);
                    addKeyword("P-Visited-Network-ID".toUpperCase(), TokenTypes.P_VISITED_NETWORK_ID);
                    addKeyword("P-Charging-Function-Addresses".toUpperCase(), TokenTypes.P_CHARGING_FUNCTION_ADDRESSES);
                    addKeyword("P-Charging-Vector".toUpperCase(), TokenTypes.P_VECTOR_CHARGING);
                    addKeyword("P-Access-Network-Info".toUpperCase(), TokenTypes.P_ACCESS_NETWORK_INFO);
                    addKeyword("P-Media-Authorization".toUpperCase(), TokenTypes.P_MEDIA_AUTHORIZATION);
                    addKeyword("Security-Server".toUpperCase(), TokenTypes.SECURITY_SERVER);
                    addKeyword("Security-Verify".toUpperCase(), TokenTypes.SECURITY_VERIFY);
                    addKeyword("Security-Client".toUpperCase(), TokenTypes.SECURITY_CLIENT);
                    addKeyword("P-User-Database".toUpperCase(), TokenTypes.P_USER_DATABASE);
                    addKeyword("P-Profile-Key".toUpperCase(), TokenTypes.P_PROFILE_KEY);
                    addKeyword("P-Served-User".toUpperCase(), TokenTypes.P_SERVED_USER);
                    addKeyword("P-Preferred-Service".toUpperCase(), TokenTypes.P_PREFERRED_SERVICE);
                    addKeyword("P-Asserted-Service".toUpperCase(), TokenTypes.P_ASSERTED_SERVICE);
                    addKeyword(ReferencesHeader.NAME.toUpperCase(), TokenTypes.REFERENCES);
                } else if (lexerName.equals("status_lineLexer")) {
                    addKeyword("sip".toUpperCase(), TokenTypes.SIP);
                } else if (lexerName.equals("request_lineLexer")) {
                    addKeyword("sip".toUpperCase(), TokenTypes.SIP);
                } else if (lexerName.equals("sip_urlLexer")) {
                    addKeyword("tel".toUpperCase(), TokenTypes.TEL);
                    addKeyword("sip".toUpperCase(), TokenTypes.SIP);
                    addKeyword("sips".toUpperCase(), TokenTypes.SIPS);
                }
            }
        }
    }
}
