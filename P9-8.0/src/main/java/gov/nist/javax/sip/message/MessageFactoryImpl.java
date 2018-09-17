package gov.nist.javax.sip.message;

import gov.nist.javax.sip.header.CSeq;
import gov.nist.javax.sip.header.CallID;
import gov.nist.javax.sip.header.ContentType;
import gov.nist.javax.sip.header.From;
import gov.nist.javax.sip.header.MaxForwards;
import gov.nist.javax.sip.header.RequestLine;
import gov.nist.javax.sip.header.StatusLine;
import gov.nist.javax.sip.header.To;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.parser.ParseExceptionListener;
import gov.nist.javax.sip.parser.StringMsgParser;
import java.text.ParseException;
import java.util.List;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ServerHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class MessageFactoryImpl implements MessageFactory, MessageFactoryExt {
    private static String defaultContentEncodingCharset = "UTF-8";
    private static ServerHeader server;
    private static UserAgentHeader userAgent;
    private boolean strict = true;
    private boolean testing = false;

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public void setTest(boolean flag) {
        this.testing = flag;
    }

    public Request createRequest(URI requestURI, String method, CallIdHeader callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via, MaxForwardsHeader maxForwards, ContentTypeHeader contentType, Object content) throws ParseException {
        if (requestURI == null || method == null || callId == null || cSeq == null || from == null || to == null || via == null || maxForwards == null || content == null || contentType == null) {
            throw new NullPointerException("Null parameters");
        }
        SIPRequest sipRequest = new SIPRequest();
        sipRequest.setRequestURI(requestURI);
        sipRequest.setMethod(method);
        sipRequest.setCallId(callId);
        sipRequest.setCSeq(cSeq);
        sipRequest.setFrom(from);
        sipRequest.setTo(to);
        sipRequest.setVia(via);
        sipRequest.setMaxForwards(maxForwards);
        sipRequest.setContent(content, contentType);
        if (userAgent != null) {
            sipRequest.setHeader(userAgent);
        }
        return sipRequest;
    }

    public Request createRequest(URI requestURI, String method, CallIdHeader callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via, MaxForwardsHeader maxForwards, byte[] content, ContentTypeHeader contentType) throws ParseException {
        if (requestURI == null || method == null || callId == null || cSeq == null || from == null || to == null || via == null || maxForwards == null || content == null || contentType == null) {
            throw new ParseException("JAIN-SIP Exception, some parameters are missing, unable to create the request", 0);
        }
        SIPRequest sipRequest = new SIPRequest();
        sipRequest.setRequestURI(requestURI);
        sipRequest.setMethod(method);
        sipRequest.setCallId(callId);
        sipRequest.setCSeq(cSeq);
        sipRequest.setFrom(from);
        sipRequest.setTo(to);
        sipRequest.setVia(via);
        sipRequest.setMaxForwards(maxForwards);
        sipRequest.setHeader((Header) (ContentType) contentType);
        sipRequest.setMessageContent(content);
        if (userAgent != null) {
            sipRequest.setHeader(userAgent);
        }
        return sipRequest;
    }

    public Request createRequest(URI requestURI, String method, CallIdHeader callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via, MaxForwardsHeader maxForwards) throws ParseException {
        if (requestURI == null || method == null || callId == null || cSeq == null || from == null || to == null || via == null || maxForwards == null) {
            throw new ParseException("JAIN-SIP Exception, some parameters are missing, unable to create the request", 0);
        }
        SIPRequest sipRequest = new SIPRequest();
        sipRequest.setRequestURI(requestURI);
        sipRequest.setMethod(method);
        sipRequest.setCallId(callId);
        sipRequest.setCSeq(cSeq);
        sipRequest.setFrom(from);
        sipRequest.setTo(to);
        sipRequest.setVia(via);
        sipRequest.setMaxForwards(maxForwards);
        if (userAgent != null) {
            sipRequest.setHeader(userAgent);
        }
        return sipRequest;
    }

    public Response createResponse(int statusCode, CallIdHeader callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via, MaxForwardsHeader maxForwards, Object content, ContentTypeHeader contentType) throws ParseException {
        if (callId == null || cSeq == null || from == null || to == null || via == null || maxForwards == null || content == null || contentType == null) {
            throw new NullPointerException(" unable to create the response");
        }
        SIPResponse sipResponse = new SIPResponse();
        StatusLine statusLine = new StatusLine();
        statusLine.setStatusCode(statusCode);
        statusLine.setReasonPhrase(SIPResponse.getReasonPhrase(statusCode));
        sipResponse.setStatusLine(statusLine);
        sipResponse.setCallId(callId);
        sipResponse.setCSeq(cSeq);
        sipResponse.setFrom(from);
        sipResponse.setTo(to);
        sipResponse.setVia(via);
        sipResponse.setMaxForwards(maxForwards);
        sipResponse.setContent(content, contentType);
        if (userAgent != null) {
            sipResponse.setHeader(userAgent);
        }
        return sipResponse;
    }

    public Response createResponse(int statusCode, CallIdHeader callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via, MaxForwardsHeader maxForwards, byte[] content, ContentTypeHeader contentType) throws ParseException {
        if (callId == null || cSeq == null || from == null || to == null || via == null || maxForwards == null || content == null || contentType == null) {
            throw new NullPointerException("Null params ");
        }
        SIPResponse sipResponse = new SIPResponse();
        sipResponse.setStatusCode(statusCode);
        sipResponse.setCallId(callId);
        sipResponse.setCSeq(cSeq);
        sipResponse.setFrom(from);
        sipResponse.setTo(to);
        sipResponse.setVia(via);
        sipResponse.setMaxForwards(maxForwards);
        sipResponse.setHeader((Header) (ContentType) contentType);
        sipResponse.setMessageContent(content);
        if (userAgent != null) {
            sipResponse.setHeader(userAgent);
        }
        return sipResponse;
    }

    public Response createResponse(int statusCode, CallIdHeader callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via, MaxForwardsHeader maxForwards) throws ParseException {
        if (callId == null || cSeq == null || from == null || to == null || via == null || maxForwards == null) {
            throw new ParseException("JAIN-SIP Exception, some parameters are missing, unable to create the response", 0);
        }
        SIPResponse sipResponse = new SIPResponse();
        sipResponse.setStatusCode(statusCode);
        sipResponse.setCallId(callId);
        sipResponse.setCSeq(cSeq);
        sipResponse.setFrom(from);
        sipResponse.setTo(to);
        sipResponse.setVia(via);
        sipResponse.setMaxForwards(maxForwards);
        if (userAgent != null) {
            sipResponse.setHeader(userAgent);
        }
        return sipResponse;
    }

    public Response createResponse(int statusCode, Request request, ContentTypeHeader contentType, Object content) throws ParseException {
        if (request == null || content == null || contentType == null) {
            throw new NullPointerException("null parameters");
        }
        SIPResponse sipResponse = ((SIPRequest) request).createResponse(statusCode);
        sipResponse.setContent(content, contentType);
        if (server != null) {
            sipResponse.setHeader(server);
        }
        return sipResponse;
    }

    public Response createResponse(int statusCode, Request request, ContentTypeHeader contentType, byte[] content) throws ParseException {
        if (request == null || content == null || contentType == null) {
            throw new NullPointerException("null Parameters");
        }
        SIPResponse sipResponse = ((SIPRequest) request).createResponse(statusCode);
        sipResponse.setHeader((Header) (ContentType) contentType);
        sipResponse.setMessageContent(content);
        if (server != null) {
            sipResponse.setHeader(server);
        }
        return sipResponse;
    }

    public Response createResponse(int statusCode, Request request) throws ParseException {
        if (request == null) {
            throw new NullPointerException("null parameters");
        }
        SIPResponse sipResponse = ((SIPRequest) request).createResponse(statusCode);
        sipResponse.removeContent();
        sipResponse.removeHeader("Content-Type");
        if (server != null) {
            sipResponse.setHeader(server);
        }
        return sipResponse;
    }

    public Request createRequest(URI requestURI, String method, CallIdHeader callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via, MaxForwardsHeader maxForwards, ContentTypeHeader contentType, byte[] content) throws ParseException {
        if (requestURI == null || method == null || callId == null || cSeq == null || from == null || to == null || via == null || maxForwards == null || content == null || contentType == null) {
            throw new NullPointerException("missing parameters");
        }
        SIPRequest sipRequest = new SIPRequest();
        sipRequest.setRequestURI(requestURI);
        sipRequest.setMethod(method);
        sipRequest.setCallId(callId);
        sipRequest.setCSeq(cSeq);
        sipRequest.setFrom(from);
        sipRequest.setTo(to);
        sipRequest.setVia(via);
        sipRequest.setMaxForwards(maxForwards);
        sipRequest.setContent(content, contentType);
        if (userAgent != null) {
            sipRequest.setHeader(userAgent);
        }
        return sipRequest;
    }

    public Response createResponse(int statusCode, CallIdHeader callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via, MaxForwardsHeader maxForwards, ContentTypeHeader contentType, Object content) throws ParseException {
        if (callId == null || cSeq == null || from == null || to == null || via == null || maxForwards == null || content == null || contentType == null) {
            throw new NullPointerException("missing parameters");
        }
        SIPResponse sipResponse = new SIPResponse();
        StatusLine statusLine = new StatusLine();
        statusLine.setStatusCode(statusCode);
        String reason = SIPResponse.getReasonPhrase(statusCode);
        if (reason == null) {
            throw new ParseException(statusCode + " Unknown", 0);
        }
        statusLine.setReasonPhrase(reason);
        sipResponse.setStatusLine(statusLine);
        sipResponse.setCallId(callId);
        sipResponse.setCSeq(cSeq);
        sipResponse.setFrom(from);
        sipResponse.setTo(to);
        sipResponse.setVia(via);
        sipResponse.setContent(content, contentType);
        if (userAgent != null) {
            sipResponse.setHeader(userAgent);
        }
        return sipResponse;
    }

    public Response createResponse(int statusCode, CallIdHeader callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via, MaxForwardsHeader maxForwards, ContentTypeHeader contentType, byte[] content) throws ParseException {
        if (callId == null || cSeq == null || from == null || to == null || via == null || maxForwards == null || content == null || contentType == null) {
            throw new NullPointerException("missing parameters");
        }
        SIPResponse sipResponse = new SIPResponse();
        StatusLine statusLine = new StatusLine();
        statusLine.setStatusCode(statusCode);
        String reason = SIPResponse.getReasonPhrase(statusCode);
        if (reason == null) {
            throw new ParseException(statusCode + " : Unknown", 0);
        }
        statusLine.setReasonPhrase(reason);
        sipResponse.setStatusLine(statusLine);
        sipResponse.setCallId(callId);
        sipResponse.setCSeq(cSeq);
        sipResponse.setFrom(from);
        sipResponse.setTo(to);
        sipResponse.setVia(via);
        sipResponse.setContent(content, contentType);
        if (userAgent != null) {
            sipResponse.setHeader(userAgent);
        }
        return sipResponse;
    }

    public Request createRequest(String requestString) throws ParseException {
        if (requestString == null || requestString.equals("")) {
            SIPRequest retval = new SIPRequest();
            retval.setNullRequest();
            return retval;
        }
        StringMsgParser smp = new StringMsgParser();
        smp.setStrict(this.strict);
        ParseExceptionListener parseExceptionListener = new ParseExceptionListener() {
            public void handleException(ParseException ex, SIPMessage sipMessage, Class headerClass, String headerText, String messageText) throws ParseException {
                if (!MessageFactoryImpl.this.testing) {
                    return;
                }
                if (headerClass == From.class || headerClass == To.class || headerClass == CallID.class || headerClass == MaxForwards.class || headerClass == Via.class || headerClass == RequestLine.class || headerClass == StatusLine.class || headerClass == CSeq.class) {
                    throw ex;
                }
                sipMessage.addUnparsed(headerText);
            }
        };
        if (this.testing) {
            smp.setParseExceptionListener(parseExceptionListener);
        }
        SIPMessage sipMessage = smp.parseSIPMessage(requestString);
        if (sipMessage instanceof SIPRequest) {
            return (SIPRequest) sipMessage;
        }
        throw new ParseException(requestString, 0);
    }

    public Response createResponse(String responseString) throws ParseException {
        if (responseString == null) {
            return new SIPResponse();
        }
        SIPMessage sipMessage = new StringMsgParser().parseSIPMessage(responseString);
        if (sipMessage instanceof SIPResponse) {
            return (SIPResponse) sipMessage;
        }
        throw new ParseException(responseString, 0);
    }

    public void setDefaultUserAgentHeader(UserAgentHeader userAgent) {
        userAgent = userAgent;
    }

    public void setDefaultServerHeader(ServerHeader server) {
        server = server;
    }

    public static UserAgentHeader getDefaultUserAgentHeader() {
        return userAgent;
    }

    public static ServerHeader getDefaultServerHeader() {
        return server;
    }

    public void setDefaultContentEncodingCharset(String charset) throws NullPointerException, IllegalArgumentException {
        if (charset == null) {
            throw new NullPointerException("Null argument!");
        }
        defaultContentEncodingCharset = charset;
    }

    public static String getDefaultContentEncodingCharset() {
        return defaultContentEncodingCharset;
    }

    public MultipartMimeContent createMultipartMimeContent(ContentTypeHeader multipartMimeCth, String[] contentType, String[] contentSubtype, String[] contentBody) {
        String boundary = multipartMimeCth.getParameter("boundary");
        MultipartMimeContentImpl retval = new MultipartMimeContentImpl(multipartMimeCth);
        for (int i = 0; i < contentType.length; i++) {
            ContentTypeHeader cth = new ContentType(contentType[i], contentSubtype[i]);
            ContentImpl contentImpl = new ContentImpl(contentBody[i], boundary);
            contentImpl.setContentTypeHeader(cth);
            retval.add(contentImpl);
        }
        return retval;
    }
}
