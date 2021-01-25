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

    @Override // gov.nist.javax.sip.message.Content
    public void setContent(Object content2) {
        this.content = content2;
    }

    @Override // gov.nist.javax.sip.message.Content
    public ContentTypeHeader getContentTypeHeader() {
        return this.contentTypeHeader;
    }

    @Override // gov.nist.javax.sip.message.Content
    public Object getContent() {
        return this.content;
    }

    @Override // gov.nist.javax.sip.message.Content
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

    @Override // gov.nist.javax.sip.message.Content
    public ContentDispositionHeader getContentDispositionHeader() {
        return this.contentDispositionHeader;
    }

    public void setContentTypeHeader(ContentTypeHeader contentTypeHeader2) {
        this.contentTypeHeader = contentTypeHeader2;
    }
}
