package gov.nist.javax.sip.message;

import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentTypeHeader;

public interface Content {
    Object getContent();

    ContentDispositionHeader getContentDispositionHeader();

    ContentTypeHeader getContentTypeHeader();

    void setContent(Object obj);

    String toString();
}
