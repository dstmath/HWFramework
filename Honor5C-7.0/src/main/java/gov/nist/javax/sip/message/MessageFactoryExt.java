package gov.nist.javax.sip.message;

import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ServerHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.message.MessageFactory;

public interface MessageFactoryExt extends MessageFactory {
    MultipartMimeContent createMultipartMimeContent(ContentTypeHeader contentTypeHeader, String[] strArr, String[] strArr2, String[] strArr3);

    void setDefaultContentEncodingCharset(String str) throws NullPointerException, IllegalArgumentException;

    void setDefaultServerHeader(ServerHeader serverHeader);

    void setDefaultUserAgentHeader(UserAgentHeader userAgentHeader);
}
