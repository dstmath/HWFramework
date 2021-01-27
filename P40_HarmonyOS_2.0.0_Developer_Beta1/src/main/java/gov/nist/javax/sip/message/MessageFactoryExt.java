package gov.nist.javax.sip.message;

import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ServerHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.message.MessageFactory;

public interface MessageFactoryExt extends MessageFactory {
    MultipartMimeContent createMultipartMimeContent(ContentTypeHeader contentTypeHeader, String[] strArr, String[] strArr2, String[] strArr3);

    @Override // javax.sip.message.MessageFactory
    void setDefaultContentEncodingCharset(String str) throws NullPointerException, IllegalArgumentException;

    @Override // javax.sip.message.MessageFactory
    void setDefaultServerHeader(ServerHeader serverHeader);

    @Override // javax.sip.message.MessageFactory
    void setDefaultUserAgentHeader(UserAgentHeader userAgentHeader);
}
