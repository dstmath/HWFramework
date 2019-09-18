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

    public boolean add(Content content) {
        return this.contentList.add((ContentImpl) content);
    }

    public ContentTypeHeader getContentTypeHeader() {
        return this.multipartMimeContentTypeHeader;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (Content content : this.contentList) {
            stringBuffer.append(content.toString());
        }
        return stringBuffer.toString();
    }

    public void createContentList(String body) throws ParseException {
        HeaderFactoryExt headerFactory;
        String str = body;
        int i = 0;
        try {
            HeaderFactoryExt headerFactory2 = new HeaderFactoryImpl();
            String delimiter = getContentTypeHeader().getParameter(BOUNDARY);
            if (delimiter == null) {
                this.contentList = new LinkedList();
                ContentImpl content = new ContentImpl(str, delimiter);
                content.setContentTypeHeader(getContentTypeHeader());
                this.contentList.add(content);
                return;
            }
            String[] fragments = str.split("--" + delimiter + Separators.NEWLINE);
            int length = fragments.length;
            int i2 = 0;
            while (i2 < length) {
                String nextPart = fragments[i2];
                if (nextPart != null) {
                    StringBuffer strbuf = new StringBuffer(nextPart);
                    while (strbuf.length() > 0 && (strbuf.charAt(i) == 13 || strbuf.charAt(i) == 10)) {
                        strbuf.deleteCharAt(i);
                    }
                    if (strbuf.length() == 0) {
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
                                String[] headerArray = headers.split(Separators.NEWLINE);
                                int length2 = headerArray.length;
                                int i3 = 0;
                                while (i3 < length2) {
                                    int i4 = length2;
                                    String hdr = headerArray[i3];
                                    HeaderFactoryExt headerFactory3 = headerFactory2;
                                    String str2 = hdr;
                                    Header header = headerFactory2.createHeader(hdr);
                                    if (header instanceof ContentTypeHeader) {
                                        content2.setContentTypeHeader((ContentTypeHeader) header);
                                    } else if (header instanceof ContentDispositionHeader) {
                                        content2.setContentDispositionHeader((ContentDispositionHeader) header);
                                    } else {
                                        throw new ParseException("Unexpected header type " + header.getName(), 0);
                                    }
                                    this.contentList.add(content2);
                                    i3++;
                                    length2 = i4;
                                    headerFactory2 = headerFactory3;
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
                    headerFactory2 = headerFactory;
                    String str3 = body;
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
        Content retval = null;
        if (this.contentList == null) {
            return null;
        }
        Iterator<Content> it = this.contentList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Content content = it.next();
            if (content.getContentTypeHeader().getContentType().equalsIgnoreCase(contentType) && content.getContentTypeHeader().getContentSubType().equalsIgnoreCase(contentSubtype)) {
                retval = content;
                break;
            }
        }
        return retval;
    }

    public void addContent(Content content) {
        add(content);
    }

    public Iterator<Content> getContents() {
        return this.contentList.iterator();
    }

    public int getContentCount() {
        return this.contentList.size();
    }
}
