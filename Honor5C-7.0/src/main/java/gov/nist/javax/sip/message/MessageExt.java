package gov.nist.javax.sip.message;

import java.text.ParseException;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;

public interface MessageExt extends Message {
    Object getApplicationData();

    CSeqHeader getCSeqHeader();

    CallIdHeader getCallIdHeader();

    ContentLengthHeader getContentLengthHeader();

    ContentTypeHeader getContentTypeHeader();

    String getFirstLine();

    FromHeader getFromHeader();

    MultipartMimeContent getMultipartMimeContent() throws ParseException;

    ToHeader getToHeader();

    ViaHeader getTopmostViaHeader();

    void setApplicationData(Object obj);
}
