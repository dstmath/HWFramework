package gov.nist.javax.sip.message;

import gov.nist.core.Separators;
import gov.nist.javax.sip.header.HeaderFactoryExt;
import gov.nist.javax.sip.header.HeaderFactoryImpl;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;

public class MultipartMimeContentImpl implements MultipartMimeContent {
    public static String BOUNDARY = "boundary";
    private String boundary;
    private List<Content> contentList = new LinkedList();
    private ContentTypeHeader multipartMimeContentTypeHeader;

    public MultipartMimeContentImpl(ContentTypeHeader contentTypeHeader) {
        this.multipartMimeContentTypeHeader = contentTypeHeader;
        this.boundary = contentTypeHeader.getParameter(BOUNDARY);
    }

    @Override // gov.nist.javax.sip.message.MultipartMimeContent
    public boolean add(Content content) {
        return this.contentList.add((ContentImpl) content);
    }

    @Override // gov.nist.javax.sip.message.MultipartMimeContent
    public ContentTypeHeader getContentTypeHeader() {
        return this.multipartMimeContentTypeHeader;
    }

    @Override // gov.nist.javax.sip.message.MultipartMimeContent
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (Content content : this.contentList) {
            stringBuffer.append(content.toString());
        }
        return stringBuffer.toString();
    }

    public void createContentList(String body) throws ParseException {
        HeaderFactoryExt headerFactory;
        String str;
        String str2 = Separators.NEWLINE;
        int i = 0;
        try {
            HeaderFactoryExt headerFactory2 = new HeaderFactoryImpl();
            String delimiter = getContentTypeHeader().getParameter(BOUNDARY);
            if (delimiter == null) {
                this.contentList = new LinkedList();
                ContentImpl content = new ContentImpl(body, delimiter);
                content.setContentTypeHeader(getContentTypeHeader());
                this.contentList.add(content);
                return;
            }
            String[] fragments = body.split("--" + delimiter + str2);
            int length = fragments.length;
            int i2 = 0;
            while (i2 < length) {
                String nextPart = fragments[i2];
                if (nextPart != null) {
                    StringBuffer strbuf = new StringBuffer(nextPart);
                    while (strbuf.length() > 0 && (strbuf.charAt(i) == '\r' || strbuf.charAt(i) == '\n')) {
                        strbuf.deleteCharAt(i);
                    }
                    if (strbuf.length() == 0) {
                        str = str2;
                        headerFactory = headerFactory2;
                    } else {
                        String nextPart2 = strbuf.toString();
                        int position = nextPart2.indexOf("\r\n\r\n");
                        int off = 4;
                        if (position == -1) {
                            position = nextPart2.indexOf(Separators.RETURN);
                            off = 2;
                        }
                        if (position != -1) {
                            String rest = nextPart2.substring(position + off);
                            if (rest != null) {
                                String headers = nextPart2.substring(i, position);
                                ContentImpl content2 = new ContentImpl(rest, this.boundary);
                                String[] headerArray = headers.split(str2);
                                str = str2;
                                int length2 = headerArray.length;
                                int i3 = 0;
                                while (i3 < length2) {
                                    Header header = headerFactory2.createHeader(headerArray[i3]);
                                    if (header instanceof ContentTypeHeader) {
                                        content2.setContentTypeHeader((ContentTypeHeader) header);
                                    } else if (header instanceof ContentDispositionHeader) {
                                        content2.setContentDispositionHeader((ContentDispositionHeader) header);
                                    } else {
                                        throw new ParseException("Unexpected header type " + header.getName(), 0);
                                    }
                                    this.contentList.add(content2);
                                    i3++;
                                    length2 = length2;
                                    headerFactory2 = headerFactory2;
                                }
                                headerFactory = headerFactory2;
                            } else {
                                throw new ParseException("No content [" + nextPart2 + "]", 0);
                            }
                        } else {
                            throw new ParseException("no content type header found in " + nextPart2, 0);
                        }
                    }
                    i2++;
                    str2 = str;
                    headerFactory2 = headerFactory;
                    i = 0;
                } else {
                    return;
                }
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw new ParseException("Invalid Multipart mime format", 0);
        }
    }

    public Content getContentByType(String contentType, String contentSubtype) {
        List<Content> list = this.contentList;
        if (list == null) {
            return null;
        }
        for (Content content : list) {
            if (content.getContentTypeHeader().getContentType().equalsIgnoreCase(contentType) && content.getContentTypeHeader().getContentSubType().equalsIgnoreCase(contentSubtype)) {
                return content;
            }
        }
        return null;
    }

    @Override // gov.nist.javax.sip.message.MultipartMimeContent
    public void addContent(Content content) {
        add(content);
    }

    @Override // gov.nist.javax.sip.message.MultipartMimeContent
    public Iterator<Content> getContents() {
        return this.contentList.iterator();
    }

    @Override // gov.nist.javax.sip.message.MultipartMimeContent
    public int getContentCount() {
        return this.contentList.size();
    }
}
