package gov.nist.javax.sip.message;

import gov.nist.core.Separators;
import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentTypeHeader;

public class ContentImpl implements Content {
    private String boundary;
    private Object content;
    private ContentDispositionHeader contentDispositionHeader;
    private ContentTypeHeader contentTypeHeader;

    public ContentImpl(String content, String boundary) {
        this.content = content;
        this.boundary = boundary;
    }

    public void setContent(Object content) {
        this.content = content;
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

    public void setContentDispositionHeader(ContentDispositionHeader contentDispositionHeader) {
        this.contentDispositionHeader = contentDispositionHeader;
    }

    public ContentDispositionHeader getContentDispositionHeader() {
        return this.contentDispositionHeader;
    }

    public void setContentTypeHeader(ContentTypeHeader contentTypeHeader) {
        this.contentTypeHeader = contentTypeHeader;
    }
}
