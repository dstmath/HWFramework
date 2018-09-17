package javax.sip.message;

import java.text.ParseException;
import java.util.List;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ServerHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.UserAgentHeader;

public interface MessageFactory {
    Request createRequest(String str) throws ParseException;

    Request createRequest(URI uri, String str, CallIdHeader callIdHeader, CSeqHeader cSeqHeader, FromHeader fromHeader, ToHeader toHeader, List list, MaxForwardsHeader maxForwardsHeader) throws ParseException;

    Request createRequest(URI uri, String str, CallIdHeader callIdHeader, CSeqHeader cSeqHeader, FromHeader fromHeader, ToHeader toHeader, List list, MaxForwardsHeader maxForwardsHeader, ContentTypeHeader contentTypeHeader, Object obj) throws ParseException;

    Request createRequest(URI uri, String str, CallIdHeader callIdHeader, CSeqHeader cSeqHeader, FromHeader fromHeader, ToHeader toHeader, List list, MaxForwardsHeader maxForwardsHeader, ContentTypeHeader contentTypeHeader, byte[] bArr) throws ParseException;

    Response createResponse(int i, CallIdHeader callIdHeader, CSeqHeader cSeqHeader, FromHeader fromHeader, ToHeader toHeader, List list, MaxForwardsHeader maxForwardsHeader) throws ParseException;

    Response createResponse(int i, CallIdHeader callIdHeader, CSeqHeader cSeqHeader, FromHeader fromHeader, ToHeader toHeader, List list, MaxForwardsHeader maxForwardsHeader, ContentTypeHeader contentTypeHeader, Object obj) throws ParseException;

    Response createResponse(int i, CallIdHeader callIdHeader, CSeqHeader cSeqHeader, FromHeader fromHeader, ToHeader toHeader, List list, MaxForwardsHeader maxForwardsHeader, ContentTypeHeader contentTypeHeader, byte[] bArr) throws ParseException;

    Response createResponse(int i, Request request) throws ParseException;

    Response createResponse(int i, Request request, ContentTypeHeader contentTypeHeader, Object obj) throws ParseException;

    Response createResponse(int i, Request request, ContentTypeHeader contentTypeHeader, byte[] bArr) throws ParseException;

    Response createResponse(String str) throws ParseException;

    void setDefaultContentEncodingCharset(String str) throws NullPointerException, IllegalArgumentException;

    void setDefaultServerHeader(ServerHeader serverHeader);

    void setDefaultUserAgentHeader(UserAgentHeader userAgentHeader);
}
