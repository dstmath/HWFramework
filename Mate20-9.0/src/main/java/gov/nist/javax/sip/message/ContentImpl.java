package gov.nist.javax.sip.message;

import gov.nist.core.Separators;
import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentTypeHeader;

public class ContentImpl implements Content {
    private String boundary;
    private Object content;
    private ContentDispositionHeader contentDispositionHeader;
    private ContentTypeHeader contentTypeHeader;

    public ContentImpl(String content2, String boundary2) {
        this.content = content2;
        this.boundary = boundary2;
    }

    public void setContent(Object content2) {
        this.content = content2;
    }

    public ContentTypeHeader getContentTypeHeader() {
        return this.contentTypeHeader;
    }

    public Object getContent() {
        return this.content;
    }

    public String toString() {
        if (this.boundary == null) {
            return this.content.toString();
        }
        if (this.contentDispositionHeader != null) {
            return "--" + this.boundary + Separators.NEWLINE + getContentTypeHeader() + getContentDispositionHeader().toString() + Separators.NEWLINE + this.content.toString();
        }
        return "--" + this.boundary + Separators.NEWLINE + getContentTypeHeader() + Separators.NEWLINE + this.content.toString();
    }

    public void setContentDispositionHeader(ContentDispositionHeader contentDispositionHeader2) {
        this.contentDispositionHeader = contentDispositionHeader2;
    }

    public ContentDispositionHeader getContentDispositionHeader() {
        return this.contentDispositionHeader;
    }

    public void setContentTypeHeader(ContentTypeHeader contentTypeHeader2) {
        this.contentTypeHeader = contentTypeHeader2;
    }
}
